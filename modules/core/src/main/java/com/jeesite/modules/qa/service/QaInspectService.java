package com.jeesite.modules.qa.service;

import com.jeesite.common.lang.StringUtils;
import com.jeesite.common.mybatis.mapper.query.QueryType;
import com.jeesite.modules.qa.constant.QaStandard;
import com.jeesite.modules.qa.dao.QaInspectDeductDao;
import com.jeesite.modules.qa.dao.QaInspectPitDao;
import com.jeesite.modules.qa.dao.QaInspectRecordDao;
import com.jeesite.modules.qa.dao.QaLevelAlertDao;
import com.jeesite.modules.qa.dao.QaProjectDao;
import com.jeesite.modules.qa.entity.QaInspectDeduct;
import com.jeesite.modules.qa.entity.QaInspectForm;
import com.jeesite.modules.qa.entity.QaInspectPit;
import com.jeesite.modules.qa.entity.QaInspectRecord;
import com.jeesite.modules.qa.entity.QaLevelAlert;
import com.jeesite.modules.qa.entity.QaProject;
import com.jeesite.modules.qa.util.QaEntities;
import com.jeesite.modules.qa.util.QaNumbers;
import com.jeesite.modules.qa.util.QaTimeline;
import com.jeesite.modules.sys.entity.Area;
import com.jeesite.modules.sys.service.AreaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 质安常态化检查服务
 * <p>
 * 覆盖项目台账、月度检查填报、四维评分定级、台账回写与等级预警。
 * 评分标准与前端共用 QaStandard 一份口径，分值按编码查标准，不接受前端传入的分值。
 *
 * @author wuchangyue
 * @version 2026-11-03
 */
@Service
public class QaInspectService {

	private static final Logger logger = LoggerFactory.getLogger(QaInspectService.class);

	/**
	 * 武汉市行政区划码
	 */
	private static final String WUHAN_AREA_CODE = "420100";

	private final QaProjectDao qaProjectDao;
	private final QaInspectRecordDao qaInspectRecordDao;
	private final QaInspectDeductDao qaInspectDeductDao;
	private final QaInspectPitDao qaInspectPitDao;
	private final QaLevelAlertDao qaLevelAlertDao;
	private final AreaService areaService;

	public QaInspectService(QaProjectDao qaProjectDao, QaInspectRecordDao qaInspectRecordDao,
			QaInspectDeductDao qaInspectDeductDao, QaInspectPitDao qaInspectPitDao,
			QaLevelAlertDao qaLevelAlertDao, AreaService areaService) {

		this.qaProjectDao = qaProjectDao;
		this.qaInspectRecordDao = qaInspectRecordDao;
		this.qaInspectDeductDao = qaInspectDeductDao;
		this.qaInspectPitDao = qaInspectPitDao;
		this.qaLevelAlertDao = qaLevelAlertDao;
		this.areaService = areaService;
	}

	/**
	 * 武汉市下辖区与功能区
	 * <p>
	 * 取自框架行政区表，国标 13 区由框架初始化，4 个功能区由 QA_INIT_DATA.sql 补齐。
	 *
	 * @return 按区划码升序的区域列表
	 */
	public List<Area> findDistrictList() {

		Area where = new Area();
		where.setParentCode(WUHAN_AREA_CODE);
		where.setStatus(Area.STATUS_NORMAL);
		return areaService.findList(where);
	}

	/**
	 * 查询项目台账列表
	 *
	 * @param where 查询条件
	 * @return 项目列表
	 */
	public List<QaProject> findProjectList(QaProject where) {

		return qaProjectDao.findList(where);
	}

	/**
	 * 按主键取项目台账
	 *
	 * @param id 项目ID
	 * @return 项目，不存在返回 null
	 */
	public QaProject getProject(String id) {

		return qaProjectDao.get(new QaProject(id));
	}

	/**
	 * 保存项目台账
	 * <p>
	 * 新增与修改按框架的 isNewRecord 判定，与 CrudService.save 口径一致。
	 *
	 * @param project 项目
	 */
	@Transactional
	public void saveProject(QaProject project) {

		project.setIsNewRecord(StringUtils.isBlank(project.getId()));
		if (project.getIsNewRecord()) {
			QaEntities.prepareInsert(project);
			qaProjectDao.insert(project);
		} else {
			project.preUpdate();
			qaProjectDao.update(project);
		}
	}

	/**
	 * 查询检查记录列表
	 *
	 * @param where 查询条件
	 * @return 记录列表
	 */
	public List<QaInspectRecord> findRecordList(QaInspectRecord where) {

		return qaInspectRecordDao.findList(where);
	}

	/**
	 * 取检查记录详情，含两张勾选明细
	 *
	 * @param id 记录ID
	 * @return 主表与明细
	 */
	public Map<String, Object> getRecordDetail(String id) {

		QaInspectRecord record = qaInspectRecordDao.get(new QaInspectRecord(id));
		if (record == null) {
			throw new IllegalArgumentException("检查记录不存在：" + id);
		}
		Map<String, Object> detail = new LinkedHashMap<>();
		detail.put("record", record);
		detail.put("deductList", findDeductList(id));
		detail.put("pitList", findPitList(id));
		return detail;
	}

	/**
	 * 保存检查记录
	 * <p>
	 * 新增时占用月位；草稿可原地修改，已提交的记录只能作废后重录。
	 * 分值全部由后端计算，提交后回写台账并生成等级预警。
	 *
	 * @param form 填报内容
	 * @return 记录ID
	 */
	@Transactional
	public String saveInspect(QaInspectForm form) {

		validateForm(form);
		QaProject project = getProject(form.getProjectId());
		if (project == null) {
			throw new IllegalArgumentException("项目不存在：" + form.getProjectId());
		}

		String inspectMonth = QaTimeline.monthOf(form.getInspectDate());
		form.setIsNewRecord(StringUtils.isBlank(form.getId()));
		boolean isNew = form.getIsNewRecord();
		List<QaInspectRecord> all = findAllRecords(project.getId());
		QaInspectRecord old = null;

		if (isNew) {
			for (QaInspectRecord r : all) {
				if (inspectMonth.equals(r.getMonthKey())) {
					throw new IllegalArgumentException("该项目 " + inspectMonth + " 已有检查记录，不可重复填报");
				}
			}
		} else {
			old = qaInspectRecordDao.get(new QaInspectRecord(form.getId()));
			if (old == null) {
				throw new IllegalArgumentException("检查记录不存在：" + form.getId());
			}
			if (!QaStandard.RECORD_STATUS_DRAFT.equals(old.getRecordStatus())) {
				throw new IllegalArgumentException("已提交的记录不可修改，请作废后重录");
			}
		}

		List<QaInspectDeduct> deducts = normalizeDeduct(form.getDeductList());
		List<QaInspectPit> pits = normalizePit(form.getPitList());
		QaInspectRecord prev = findPrevRecord(all, inspectMonth, isNew ? null : old.getId());
		QaInspectRecord record = isNew ? new QaInspectRecord() : old;
		fillRecord(record, form, project, inspectMonth, prev, deducts, pits);

		if (isNew) {
			record.setMonthKey(inspectMonth);
			record.setRecordNo(nextRecordNo());
			QaEntities.prepareInsert(record);
			qaInspectRecordDao.insert(record);
		} else {
			record.setMonthKey(old.getMonthKey());
			record.setRecordNo(old.getRecordNo());
			record.preUpdate();
			qaInspectRecordDao.update(record);
		}

		saveDetails(record.getId(), deducts, pits);
		if (QaStandard.isSubmitted(record.getRecordStatus())) {
			createAlert(record);
		}
		rewriteProject(project.getId());

		logger.info("[质安检查] 保存记录 {}，项目 {}，月份 {}，总分 {}",
			record.getId(), project.getProjectCode(), inspectMonth, record.getTotalScore());
		return record.getId();
	}

	/**
	 * 作废检查记录，让出月位以便重录
	 *
	 * @param id     记录ID
	 * @param reason 作废原因
	 */
	@Transactional
	public void voidRecord(String id, String reason) {

		if (StringUtils.isBlank(id)) {
			throw new IllegalArgumentException("记录ID不能为空");
		}
		if (StringUtils.isBlank(reason)) {
			throw new IllegalArgumentException("作废原因不能为空");
		}
		QaInspectRecord record = qaInspectRecordDao.get(new QaInspectRecord(id));
		if (record == null) {
			throw new IllegalArgumentException("检查记录不存在：" + id);
		}
		if (QaStandard.RECORD_STATUS_VOID.equals(record.getRecordStatus())) {
			throw new IllegalArgumentException("该记录已作废");
		}

		int voidCount = 0;
		for (QaInspectRecord r : findAllRecords(record.getProjectId())) {
			if (QaStandard.RECORD_STATUS_VOID.equals(r.getRecordStatus())
					&& record.getInspectMonth().equals(r.getInspectMonth())) {
				voidCount++;
			}
		}
		record.setMonthKey(record.getInspectMonth()
			+ QaStandard.MONTH_KEY_VOID_SEPARATOR + (voidCount + 1));
		record.setRecordStatus(QaStandard.RECORD_STATUS_VOID);
		record.setVoidReason(reason);
		record.preUpdate();
		qaInspectRecordDao.update(record);

		QaLevelAlert alertWhere = new QaLevelAlert();
		alertWhere.setCurrRecordId(id);
		qaLevelAlertDao.phyDeleteByEntity(alertWhere);

		rewriteProject(record.getProjectId());
		logger.info("[质安检查] 作废记录 {}，原因：{}", id, reason);
	}

	/**
	 * 项目档案近 12 个月检查格子
	 *
	 * @param projectId 项目ID
	 * @return 每月一条，含是否有记录与得分
	 */
	public List<Map<String, Object>> archive(String projectId) {

		Map<String, QaInspectRecord> byMonth = new LinkedHashMap<>();
		for (QaInspectRecord r : findSubmittedRecords(projectId)) {
			byMonth.putIfAbsent(r.getInspectMonth(), r);
		}
		List<Map<String, Object>> result = new ArrayList<>();
		YearMonth now = YearMonth.now();
		for (int i = 11; i >= 0; i--) {
			String month = now.minusMonths(i).toString();
			QaInspectRecord r = byMonth.get(month);
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("month", month);
			row.put("checked", r != null);
			row.put("recordId", r == null ? null : r.getId());
			row.put("score", r == null ? null : r.getTotalScore());
			row.put("level", r == null ? null : r.getRiskLevel());
			result.add(row);
		}
		return result;
	}

	/**
	 * 工作台统计
	 * <p>
	 * 全部按有效记录聚合，作废记录不进统计；各区排名按平均分升序。
	 *
	 * @return KPI 与图表数据
	 */
	public Map<String, Object> dashboard() {

		QaProject projectWhere = new QaProject();
		projectWhere.setMonitorFlag("1");
		List<QaProject> monitored = qaProjectDao.findList(projectWhere);
		List<QaInspectRecord> valid = findSubmittedRecords(null);
		String curMonth = YearMonth.now().toString();

		Map<String, QaInspectRecord> latestByProject = new LinkedHashMap<>();
		for (QaInspectRecord r : valid) {
			QaInspectRecord cur = latestByProject.get(r.getProjectId());
			if (cur == null || QaTimeline.compare(r, cur) > 0) {
				latestByProject.put(r.getProjectId(), r);
			}
		}

		int checkedThisMonth = 0;
		for (QaInspectRecord r : valid) {
			if (curMonth.equals(r.getInspectMonth())) {
				checkedThisMonth++;
			}
		}

		int highRisk = 0;
		int pending = 0;
		int deductItemTotal = 0;
		double scoreSum = 0.00;
		int scoreCount = 0;
		Map<String, Integer> levelDist = new LinkedHashMap<>();
		Map<String, Integer> categoryDist = new LinkedHashMap<>();
		Map<String, List<Double>> byDistrict = new LinkedHashMap<>();

		for (QaProject p : monitored) {
			if ("1".equals(p.getLastLevel()) || "2".equals(p.getLastLevel())) {
				highRisk++;
			}
			if (StringUtils.isBlank(p.getLastInspectMonth()) || StringUtils.isBlank(p.getNextDueMonth())
					|| p.getNextDueMonth().compareTo(curMonth) <= 0) {
				pending++;
			}
			if (p.getLastLevel() != null) {
				levelDist.merge(p.getLastLevel(), 1, Integer::sum);
			}
			if (p.getLastScore() != null) {
				scoreSum += p.getLastScore();
				scoreCount++;
				String district = StringUtils.isNotBlank(p.getDistrictName()) ? p.getDistrictName() : "未分区";
				byDistrict.computeIfAbsent(district, k -> new ArrayList<>()).add(p.getLastScore());
			}
			categoryDist.merge(p.getCategoryCode(), 1, Integer::sum);
		}
		for (QaInspectRecord r : latestByProject.values()) {
			deductItemTotal += r.getDeductItemCount() == null ? 0 : r.getDeductItemCount();
		}

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("projectCount", monitored.size());
		result.put("checkedThisMonth", checkedThisMonth);
		result.put("coverageRate", monitored.isEmpty() ? 0.00 : QaNumbers.round1(checkedThisMonth * 100.0 / monitored.size()));
		result.put("highRiskCount", highRisk);
		result.put("deductItemTotal", deductItemTotal);
		result.put("pendingCount", pending);
		result.put("avgScore", scoreCount == 0 ? null : QaNumbers.round1(scoreSum / scoreCount));
		result.put("levelDistribution", levelDist);
		result.put("categoryDistribution", categoryDist);

		double cat = 0.00, major = 0.00, pit = 0.00, daily = 0.00;
		int n = latestByProject.size();
		for (QaInspectRecord r : latestByProject.values()) {
			cat += QaNumbers.zeroIfNull(r.getCatScore());
			major += QaNumbers.zeroIfNull(r.getMajorScore());
			pit += QaNumbers.zeroIfNull(r.getPitScore());
			daily += QaNumbers.zeroIfNull(r.getDailyScore());
		}
		Map<String, Object> dim = new LinkedHashMap<>();
		if (n > 0) {
			dim.put("category", QaNumbers.round1(cat / n / 20 * 100));
			dim.put("major", QaNumbers.round1(major / n / 35 * 100));
			dim.put("pit", QaNumbers.round1(pit / n / 20 * 100));
			dim.put("daily", QaNumbers.round1(daily / n / 25 * 100));
		}
		result.put("dimensionRates", dim);

		List<Map<String, Object>> ranking = new ArrayList<>();
		for (Map.Entry<String, List<Double>> e : byDistrict.entrySet()) {
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("name", e.getKey());
			row.put("avgScore", QaNumbers.round1(QaNumbers.average(e.getValue())));
			row.put("projectCount", e.getValue().size());
			ranking.add(row);
		}
		ranking.sort(Comparator.comparingDouble(row -> (Double) row.get("avgScore")));
		result.put("districtRanking", ranking);

		List<Map<String, Object>> trend = new ArrayList<>();
		YearMonth now = YearMonth.now();
		for (int i = 7; i >= 0; i--) {
			String month = now.minusMonths(i).toString();
			List<Double> scores = new ArrayList<>();
			for (QaInspectRecord r : valid) {
				if (month.equals(r.getInspectMonth()) && r.getTotalScore() != null) {
					scores.add(r.getTotalScore());
				}
			}
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("month", month);
			row.put("avgScore", scores.isEmpty() ? null : QaNumbers.round1(QaNumbers.average(scores)));
			row.put("recordCount", scores.size());
			trend.add(row);
		}
		result.put("trend", trend);

		Set<String> validIds = new LinkedHashSet<>();
		for (QaInspectRecord r : valid) {
			validIds.add(r.getId());
		}
		Map<String, Integer> deductDist = new LinkedHashMap<>();
		for (String code : QaStandard.deductItems().keySet()) {
			deductDist.put(code, 0);
		}
		for (QaInspectDeduct d : qaInspectDeductDao.findList(new QaInspectDeduct())) {
			if (validIds.contains(d.getRecordId()) && d.getDeductCode() != null) {
				deductDist.merge(d.getDeductCode(), 1, Integer::sum);
			}
		}
		Map<String, Integer> pitDist = new LinkedHashMap<>();
		for (String code : QaStandard.pitItems().keySet()) {
			pitDist.put(code, 0);
		}
		for (QaInspectRecord r : valid) {
			if (r.getPitLevelCode() != null) {
				pitDist.merge(r.getPitLevelCode(), 1, Integer::sum);
			}
		}
		Map<String, Object> deductDistribution = new LinkedHashMap<>();
		deductDistribution.put("deduct", deductDist);
		deductDistribution.put("pit", pitDist);
		result.put("deductDistribution", deductDistribution);

		return result;
	}

	/**
	 * 查询等级预警列表
	 *
	 * @param where 查询条件
	 * @return 预警列表
	 */
	public List<QaLevelAlert> findAlertList(QaLevelAlert where) {

		return qaLevelAlertDao.findList(where);
	}

	/**
	 * 提交前置校验
	 */
	private void validateForm(QaInspectForm form) {

		if (form == null) {
			throw new IllegalArgumentException("填报内容不能为空");
		}
		if (StringUtils.isBlank(form.getProjectId())) {
			throw new IllegalArgumentException("项目ID不能为空");
		}
		if (form.getInspectDate() == null) {
			throw new IllegalArgumentException("检查日期不能为空");
		}
		if (StringUtils.isBlank(form.getMajorLevelCode())) {
			throw new IllegalArgumentException("危大工程档位必选");
		}
		if (QaStandard.majorBaseScore(form.getMajorLevelCode()) == null) {
			throw new IllegalArgumentException("未知危大工程档位：" + form.getMajorLevelCode());
		}
		if (form.getSmartScore() == null) {
			throw new IllegalArgumentException("智慧监管系统分数必填");
		}
	}

	/**
	 * 危大扣分项规范化：按编码查标准分值、去重、拒绝未知编码
	 */
	private List<QaInspectDeduct> normalizeDeduct(List<QaInspectDeduct> list) {

		List<QaInspectDeduct> result = new ArrayList<>();
		if (list == null) {
			return result;
		}
		Set<String> seen = new LinkedHashSet<>();
		for (QaInspectDeduct item : list) {
			if (item == null || StringUtils.isBlank(item.getDeductCode())) {
				continue;
			}
			String code = item.getDeductCode();
			Double score = QaStandard.deductScore(code);
			if (score == null) {
				throw new IllegalArgumentException("未知扣分项编码：" + code);
			}
			if (!seen.add(code)) {
				throw new IllegalArgumentException("扣分项重复勾选：" + code);
			}
			item.setDeductScore(score);
			result.add(item);
		}
		return result;
	}

	/**
	 * 基坑档位规范化：按编码查标准分值、去重、兜底 P4、标出生效的最低分档
	 * <p>
	 * P4 与 P0~P3 互斥；未勾选时兜底 P4，故明细至少一行。
	 */
	private List<QaInspectPit> normalizePit(List<QaInspectPit> list) {

		List<QaInspectPit> result = new ArrayList<>();
		if (list != null) {
			Set<String> seen = new LinkedHashSet<>();
			for (QaInspectPit item : list) {
				if (item == null || StringUtils.isBlank(item.getPitCode())) {
					continue;
				}
				String code = item.getPitCode();
				Double score = QaStandard.pitScore(code);
				if (score == null) {
					throw new IllegalArgumentException("未知基坑档位编码：" + code);
				}
				if (!seen.add(code)) {
					throw new IllegalArgumentException("基坑档位重复勾选：" + code);
				}
				item.setPitScore(score);
				result.add(item);
			}
		}
		if (result.isEmpty()) {
			QaInspectPit p4 = new QaInspectPit();
			p4.setPitCode("P4");
			p4.setPitScore(QaStandard.pitScore("P4"));
			result.add(p4);
		}

		boolean hasP4 = false;
		boolean hasOther = false;
		for (QaInspectPit p : result) {
			if ("P4".equals(p.getPitCode())) {
				hasP4 = true;
			} else {
				hasOther = true;
			}
		}
		if (hasP4 && hasOther) {
			throw new IllegalArgumentException("基坑 P4 与 P0~P3 互斥，不能同时勾选");
		}

		String effective = null;
		double min = Double.MAX_VALUE;
		for (String code : QaStandard.pitItems().keySet()) {
			for (QaInspectPit p : result) {
				if (code.equals(p.getPitCode()) && p.getPitScore() < min) {
					min = p.getPitScore();
					effective = code;
				}
			}
		}
		for (QaInspectPit p : result) {
			p.setIsEffective(effective != null && effective.equals(p.getPitCode()) ? "1" : "0");
		}
		return result;
	}

	/**
	 * 计算四维得分并写入检查记录
	 * <p>
	 * 危大扣分累加，基坑取最低分档，两项口径相反。
	 */
	private void fillRecord(QaInspectRecord record, QaInspectForm form, QaProject project,
			String inspectMonth, QaInspectRecord prev, List<QaInspectDeduct> deducts,
			List<QaInspectPit> pits) {

		Double category = QaStandard.categoryScore(project.getCategoryCode());
		if (category == null) {
			throw new IllegalArgumentException("项目工程类别未配置或不合法：" + project.getCategoryCode());
		}
		double majorBase = QaStandard.majorBaseScore(form.getMajorLevelCode());
		double deductSum = 0.00;
		for (QaInspectDeduct d : deducts) {
			deductSum += d.getDeductScore();
		}
		double majorScore = Math.max(0, majorBase - deductSum);

		double pitScore = 0.00;
		String pitLevelCode = null;
		for (QaInspectPit p : pits) {
			if ("1".equals(p.getIsEffective())) {
				pitScore = p.getPitScore();
				pitLevelCode = p.getPitCode();
			}
		}

		double smart = Math.max(0, Math.min(100, form.getSmartScore()));
		double daily = Math.round(smart * QaStandard.SMART_RATIO);
		double total = category + majorScore + pitScore + daily;
		String level = QaStandard.riskLevel(total);

		record.setProjectId(project.getId());
		record.setProjectCode(project.getProjectCode());
		record.setInspectDate(form.getInspectDate());
		record.setInspectMonth(inspectMonth);
		record.setInspectBy(resolveInspectBy(form));
		record.setInspectDept(form.getInspectDept());
		record.setCategoryCode(project.getCategoryCode());
		record.setCatScore(category);
		record.setMajorLevelCode(form.getMajorLevelCode());
		record.setMajorBaseScore(majorBase);
		record.setMajorDeductSum(deductSum);
		record.setMajorDeductCount(deducts.size());
		record.setMajorScore(majorScore);
		record.setPitLevelCode(pitLevelCode);
		record.setPitScore(pitScore);
		record.setPitPickCount(pits.size());
		record.setSmartScore(smart);
		record.setSmartSource(StringUtils.isBlank(form.getSmartSource())
			? QaStandard.SMART_SOURCE_MANUAL : form.getSmartSource());
		record.setSmartSyncTime(QaStandard.SMART_SOURCE_SYNC.equals(record.getSmartSource()) ? new Date() : null);
		record.setDailyScore(daily);
		record.setTotalScore(total);
		record.setRiskLevel(level);

		if (prev == null) {
			record.setPrevRecordId(null);
			record.setPrevScore(null);
			record.setScoreDelta(null);
			record.setLevelChange("0");
		} else {
			double prevScore = QaNumbers.zeroIfNull(prev.getTotalScore());
			record.setPrevRecordId(prev.getId());
			record.setPrevScore(prevScore);
			record.setScoreDelta(total - prevScore);
			record.setLevelChange(QaStandard.levelChange(prev.getRiskLevel(), level));
		}

		record.setDeductItemCount(deducts.size() + (pitScore < 20 ? 1 : 0));
		record.setConclusion(StringUtils.isNotBlank(form.getConclusion())
			? form.getConclusion() : QaStandard.conclusion(level));

		String status = StringUtils.isBlank(form.getRecordStatus())
			? QaStandard.RECORD_STATUS_DRAFT : form.getRecordStatus();
		record.setRecordStatus(status);
		if (QaStandard.RECORD_STATUS_SUBMIT.equals(status)) {
			record.setSubmitBy(StringUtils.isNotBlank(form.getSubmitBy())
				? form.getSubmitBy() : QaEntities.currentUserName(record));
			record.setSubmitDate(new Date());
		} else {
			record.setSubmitBy(null);
			record.setSubmitDate(null);
		}
		record.setVoidReason(null);
	}

	/**
	 * 覆盖写入两张勾选明细
	 * <p>
	 * 旧明细按记录ID物理清理：明细表有 (记录ID, 编码) 唯一键，逻辑删除的行会继续占位。
	 */
	private void saveDetails(String recordId, List<QaInspectDeduct> deducts, List<QaInspectPit> pits) {

		QaInspectDeduct deductWhere = new QaInspectDeduct();
		deductWhere.setRecordId(recordId);
		qaInspectDeductDao.phyDeleteByEntity(deductWhere);

		QaInspectPit pitWhere = new QaInspectPit();
		pitWhere.setRecordId(recordId);
		qaInspectPitDao.phyDeleteByEntity(pitWhere);

		if (!deducts.isEmpty()) {
			List<QaInspectDeduct> rows = new ArrayList<>(deducts.size());
			for (QaInspectDeduct item : deducts) {
				QaInspectDeduct row = new QaInspectDeduct();
				row.setRecordId(recordId);
				row.setDeductCode(item.getDeductCode());
				row.setDeductScore(item.getDeductScore());
				QaEntities.prepareInsert(row);
				rows.add(row);
			}
			qaInspectDeductDao.insertBatch(rows);
		}

		List<QaInspectPit> pitRows = new ArrayList<>(pits.size());
		for (QaInspectPit item : pits) {
			QaInspectPit row = new QaInspectPit();
			row.setRecordId(recordId);
			row.setPitCode(item.getPitCode());
			row.setPitScore(item.getPitScore());
			row.setIsEffective(item.getIsEffective());
			QaEntities.prepareInsert(row);
			pitRows.add(row);
		}
		qaInspectPitDao.insertBatch(pitRows);
	}

	/**
	 * 回写项目台账的最近一次检查结果
	 * <p>
	 * 记录作废后要把空值写回，故开启空值覆盖，框架默认更新会跳过空值。
	 */
	private void rewriteProject(String projectId) {

		QaProject project = getProject(projectId);
		if (project == null) {
			return;
		}
		List<QaInspectRecord> valid = findSubmittedRecords(projectId);
		QaInspectRecord latest = null;
		for (QaInspectRecord r : valid) {
			if (latest == null || QaTimeline.compare(r, latest) > 0) {
				latest = r;
			}
		}
		project.setLastRecordId(latest == null ? null : latest.getId());
		project.setLastInspectDate(latest == null ? null : latest.getInspectDate());
		project.setLastInspectMonth(latest == null ? null : latest.getInspectMonth());
		project.setNextDueMonth(latest == null ? null : QaTimeline.nextMonth(latest.getInspectMonth()));
		project.setLastScore(latest == null ? null : latest.getTotalScore());
		project.setLastLevel(latest == null ? null : latest.getRiskLevel());
		project.setInspectCount(valid.size());
		project.sqlMap().updateNullValue();
		project.preUpdate();
		qaProjectDao.update(project);
	}

	/**
	 * 生成等级变化预警，一条记录最多一条，取最严重类型
	 */
	private void createAlert(QaInspectRecord record) {

		if (record.getPrevRecordId() == null || record.getScoreDelta() == null) {
			return;
		}
		QaInspectRecord prev = qaInspectRecordDao.get(new QaInspectRecord(record.getPrevRecordId()));
		double delta = record.getScoreDelta();
		boolean scoreDown = delta < 0;
		boolean levelDown = "2".equals(record.getLevelChange());
		boolean twiceDown = scoreDown && prev != null
			&& prev.getScoreDelta() != null && prev.getScoreDelta() < 0;

		String type = null;
		String msg = null;
		if (twiceDown) {
			type = QaStandard.ALERT_TWICE_DOWN;
			msg = "连续两次检查得分下降，需重点督办";
		} else if (levelDown) {
			type = QaStandard.ALERT_LEVEL_DOWN;
			msg = "风险等级下调，需加密监督检查频次";
		} else if (scoreDown) {
			type = QaStandard.ALERT_SCORE_DOWN;
			msg = "本次得分较上次下降 " + QaNumbers.round1(Math.abs(delta)) + " 分";
		}
		if (type == null) {
			return;
		}

		QaLevelAlert alert = new QaLevelAlert();
		alert.setProjectId(record.getProjectId());
		alert.setPrevRecordId(record.getPrevRecordId());
		alert.setCurrRecordId(record.getId());
		alert.setPrevScore(record.getPrevScore());
		alert.setCurrScore(record.getTotalScore());
		alert.setScoreDelta(delta);
		alert.setPrevLevel(prev == null ? null : prev.getRiskLevel());
		alert.setCurrLevel(record.getRiskLevel());
		alert.setAlertType(type);
		alert.setAlertMsg(msg);
		alert.setPushStatus("0");
		QaEntities.prepareInsert(alert);
		qaLevelAlertDao.insert(alert);
	}

	/**
	 * 取同项目月份更早的最近一条有效记录
	 */
	private QaInspectRecord findPrevRecord(List<QaInspectRecord> all, String inspectMonth, String selfId) {

		QaInspectRecord prev = null;
		for (QaInspectRecord r : all) {
			if (r.getId().equals(selfId) || !QaStandard.isSubmitted(r.getRecordStatus())) {
				continue;
			}
			if (r.getInspectMonth() == null || r.getInspectMonth().compareTo(inspectMonth) >= 0) {
				continue;
			}
			if (prev == null || QaTimeline.compare(r, prev) > 0) {
				prev = r;
			}
		}
		return prev;
	}

	/**
	 * 按月份取项目的全部检查记录
	 */
	private List<QaInspectRecord> findAllRecords(String projectId) {

		QaInspectRecord where = new QaInspectRecord();
		where.setProjectId(projectId);
		return qaInspectRecordDao.findList(where);
	}

	/**
	 * 按项目取已提交与已归档的记录
	 * <p>
	 * 台账回写、档案格子、环比对比只认已提交，草稿与作废记录不算检查过。
	 *
	 * @param projectId 项目ID，为空取全部项目
	 * @return 记录列表
	 */
	private List<QaInspectRecord> findSubmittedRecords(String projectId) {

		QaInspectRecord where = new QaInspectRecord();
		if (StringUtils.isNotBlank(projectId)) {
			where.setProjectId(projectId);
		}
		where.sqlMap().getWhere().and("record_status", QueryType.IN,
			new String[]{QaStandard.RECORD_STATUS_SUBMIT, QaStandard.RECORD_STATUS_ARCHIVE});
		return qaInspectRecordDao.findList(where);
	}

	/**
	 * 查询某记录的危大扣分明细
	 */
	private List<QaInspectDeduct> findDeductList(String recordId) {

		QaInspectDeduct where = new QaInspectDeduct();
		where.setRecordId(recordId);
		return qaInspectDeductDao.findList(where);
	}

	/**
	 * 查询某记录的基坑档位明细
	 */
	private List<QaInspectPit> findPitList(String recordId) {

		QaInspectPit where = new QaInspectPit();
		where.setRecordId(recordId);
		return qaInspectPitDao.findList(where);
	}

	/**
	 * 生成检查记录号：JY + 两位年份 + 四位序号
	 */
	private String nextRecordNo() {

		Long seq = qaInspectRecordDao.nextRecordNo();
		if (seq == null) {
			throw new IllegalArgumentException("检查记录号序列未返回数值");
		}
		return "JY" + String.format("%02d", YearMonth.now().getYear() % 100)
			+ String.format("%04d", seq);
	}

	/**
	 * 检查人姓名，未填时取当前用户
	 */
	private String resolveInspectBy(QaInspectForm form) {

		return StringUtils.isNotBlank(form.getInspectBy())
			? form.getInspectBy() : QaEntities.currentUserName(form);
	}

}
