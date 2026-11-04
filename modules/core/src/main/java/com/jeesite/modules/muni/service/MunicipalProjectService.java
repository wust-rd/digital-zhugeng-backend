package com.jeesite.modules.muni.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.modules.muni.dao.MunicipalProjectDao;
import com.jeesite.modules.muni.entity.MunicipalEngineering;
import com.jeesite.modules.muni.entity.MunicipalProjectVo;
import com.jeesite.modules.muni.entity.MunicipalResponsibilityUnit;
import com.jeesite.modules.muni.util.ContactSplitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 市政工程项目基本信息查询
 * <p>
 * 以清单（106 行）为骨架：清单里填了项目ID的行，取登记库 XMXXDJ 的工程与责任主体信息覆盖；
 * 没填ID的行原样用清单值。库中字段为空时回退清单值，保证每行都有可展示内容。
 *
 * @author wuchangyue
 * @version 2026-11-03
 */
@Service
public class MunicipalProjectService {

	private static final Logger logger = LoggerFactory.getLogger(MunicipalProjectService.class);

	/**
	 * 清单基线文件，随 jar 打包
	 */
	private static final String BASELINE_PATH = "muni/municipal-project-baseline.json";

	/**
	 * 单次 IN 查询的ID数量上限
	 */
	private static final int BATCH_SIZE = 500;

	/**
	 * 责任主体类型：建设单位
	 */
	private static final String UNIT_BUILD = "建设单位";

	/**
	 * 责任主体类型：施工单位
	 */
	private static final String UNIT_CONSTRUCT = "施工单位";

	/**
	 * 责任主体类型：监理单位
	 */
	private static final String UNIT_SUPERVISE = "监理单位";

	/**
	 * 多家单位的分隔符
	 */
	private static final String UNIT_SEPARATOR = "、";

	private final MunicipalProjectDao municipalProjectDao;

	/**
	 * 解析清单用的 JSON 工具，自行创建，不依赖容器注入
	 */
	private final ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * 清单原文，首次读取后缓存，避免每次请求读文件
	 */
	private volatile String baselineJson;

	public MunicipalProjectService(MunicipalProjectDao municipalProjectDao) {
		this.municipalProjectDao = municipalProjectDao;
	}

	/**
	 * 查询市政工程项目基本信息
	 *
	 * @return 按清单序号排列，条数与清单一致
	 */
	public List<MunicipalProjectVo> queryProjectList() {

		List<MunicipalProjectVo> rows = loadBaseline();
		for (MunicipalProjectVo row : rows) {
			String[] contact = ContactSplitter.split(row.getRawContact());
			row.setContactName(contact[0]);
			row.setContactPhone(contact[1]);
		}
		List<Long> ids = collectProjectIds(rows);
		if (ids.isEmpty()) {
			logger.info("[市政项目] 清单无项目ID，全部取清单值，共 {} 条", rows.size());
			return rows;
		}

		Map<Long, MunicipalEngineering> engineeringMap = new HashMap<>(ids.size());
		Map<Long, Map<String, Set<String>>> unitMap = new HashMap<>(ids.size());
		for (int i = 0; i < ids.size(); i += BATCH_SIZE) {
			List<Long> batch = ids.subList(i, Math.min(i + BATCH_SIZE, ids.size()));
			fillEngineering(engineeringMap, batch);
			fillUnits(unitMap, batch);
		}

		int matched = 0;
		for (MunicipalProjectVo row : rows) {
			row.setDataSource(MunicipalProjectVo.SOURCE_EXCEL);
			MunicipalEngineering engineering = engineeringMap.get(row.getProjectId());
			if (engineering == null) {
				continue;
			}
			applyEngineering(row, engineering, unitMap.get(row.getProjectId()));
			matched++;
		}
		logger.info("[市政项目] 清单 {} 条，登记库命中 {} 条，未命中 {} 条",
			rows.size(), matched, rows.size() - matched);
		return rows;
	}

	/**
	 * 用登记库信息覆盖清单行，清单名称与总投资保留在 Excel 字段，库值单独落库字段
	 */
	private void applyEngineering(MunicipalProjectVo row, MunicipalEngineering engineering,
			Map<String, Set<String>> units) {

		row.setDataSource(MunicipalProjectVo.SOURCE_DB);
		row.setNameDb(engineering.getEngineeringName());
		row.setInvestDb(normalizeAmount(engineering.getContractAmount()));
		row.setDbArea(engineering.getArea());
		row.setJurisdiction(engineering.getJurisdiction());
		row.setLongitude(engineering.getLongitude());
		row.setLatitude(engineering.getLatitude());
		row.setPlannedStartTime(engineering.getPlannedStartTime());
		row.setPlannedCompletionTime(engineering.getPlannedCompletionTime());
		row.setKeyProjects(engineering.getKeyProjects());
		row.setReportRequired(engineering.getReportRequired());
		row.setAddress(coalesce(engineering.getAddress(), row.getAddress()));
		row.setConstructionScale(coalesce(engineering.getConstructionScale(), row.getConstructionScale()));
		row.setPermitNumber(coalesce(engineering.getPermitNumber(), row.getPermitNumber()));
		row.setBuildStatus(coalesce(engineering.getProjectState(), row.getBuildStatus()));
		row.setBuildUnit(coalesce(unitText(units, UNIT_BUILD), row.getBuildUnit()));
		row.setConstructUnit(coalesce(unitText(units, UNIT_CONSTRUCT), row.getConstructUnit()));
		row.setSuperviseUnit(coalesce(unitText(units, UNIT_SUPERVISE), row.getSuperviseUnit()));
	}

	/**
	 * 批量补工程基本信息
	 */
	private void fillEngineering(Map<Long, MunicipalEngineering> target, List<Long> ids) {

		for (MunicipalEngineering engineering : municipalProjectDao.findEngineeringList(ids)) {
			target.put(engineering.getEngineeringId(), engineering);
		}
	}

	/**
	 * 批量补责任主体，按工程与类型聚合去重
	 */
	private void fillUnits(Map<Long, Map<String, Set<String>>> target, List<Long> ids) {

		for (MunicipalResponsibilityUnit unit : municipalProjectDao.findUnitList(ids)) {
			if (StringUtils.isBlank(unit.getUnitType()) || StringUtils.isBlank(unit.getUnitName())) {
				continue;
			}
			target.computeIfAbsent(unit.getEngineeringId(), key -> new HashMap<>())
				.computeIfAbsent(unit.getUnitType(), key -> new LinkedHashSet<>())
				.add(unit.getUnitName().trim());
		}
	}

	/**
	 * 拼接同一类型的多单位名称
	 */
	private String unitText(Map<String, Set<String>> units, String type) {

		if (units == null) {
			return null;
		}
		Set<String> names = units.get(type);
		if (names == null || names.isEmpty()) {
			return null;
		}
		return String.join(UNIT_SEPARATOR, names);
	}

	/**
	 * 合同金额去掉千分位，原值不是数字时原样返回
	 */
	private String normalizeAmount(String raw) {

		if (StringUtils.isBlank(raw)) {
			return null;
		}
		String value = raw.replace(",", "").replace("，", "").trim();
		return value.isEmpty() ? null : value;
	}

	/**
	 * 取第一个非空值
	 */
	private String coalesce(String value, String fallback) {

		return StringUtils.isNotBlank(value) ? value.trim() : fallback;
	}

	/**
	 * 收集清单中填了项目ID的行，去重保序
	 */
	private List<Long> collectProjectIds(List<MunicipalProjectVo> rows) {

		Set<Long> ids = new LinkedHashSet<>();
		for (MunicipalProjectVo row : rows) {
			if (row.getProjectId() != null) {
				ids.add(row.getProjectId());
			}
		}
		return new ArrayList<>(ids);
	}

	/**
	 * 读取清单基线，每次返回新对象，调用方可直接修改
	 */
	private List<MunicipalProjectVo> loadBaseline() {

		if (baselineJson == null) {
			synchronized (this) {
				if (baselineJson == null) {
					baselineJson = readBaseline();
				}
			}
		}
		try {
			return objectMapper.readValue(baselineJson, new TypeReference<List<MunicipalProjectVo>>() {
			});
		} catch (IOException e) {
			throw new IllegalStateException("市政项目清单解析失败：" + e.getMessage(), e);
		}
	}

	/**
	 * 从 classpath 读取清单原文
	 */
	private String readBaseline() {

		try (InputStream in = new ClassPathResource(BASELINE_PATH).getInputStream()) {
			return new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new IllegalStateException("市政项目清单读取失败：" + BASELINE_PATH, e);
		}
	}

}
