package com.jeesite.modules.qa.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jeesite.common.entity.BaseEntity;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;

import java.io.Serial;
import java.util.Date;

/**
 * 月度检查记录 Entity
 *
 * @author wuchangyue
 * @version 2026-11-03
 */
@Table(name = "QA_INSPECT_RECORD", alias = "a", label = "月度检查记录", columns = {
		@Column(name = "id", attrName = "id", label = "主键ID（JeeSite IdGen 生成）", isPK = true),
		@Column(name = "record_no", attrName = "recordNo", label = "记录号，唯一，按年重置序列生成"),
		@Column(name = "project_id", attrName = "projectId", label = "项目台账ID，指向 QA_PROJECT.ID"),
		@Column(name = "project_code", attrName = "projectCode", label = "项目编号冗余，便于导出与跨模块对账"),
		@Column(name = "inspect_date", attrName = "inspectDate", label = "检查日期"),
		@Column(name = "inspect_month", attrName = "inspectMonth", label = "检查月份 yyyy-MM，月度统计与档案格子用"),
		@Column(name = "month_key", attrName = "monthKey", label = "月度占位键：有效记录=月份；作废记录=月份#序号，让出月位"),
		@Column(name = "inspect_by", attrName = "inspectBy", label = "检查人姓名"),
		@Column(name = "inspect_dept", attrName = "inspectDept", label = "检查部门"),
		@Column(name = "category_code", attrName = "categoryCode", label = "工程类别快照"),
		@Column(name = "cat_score", attrName = "catScore", label = "工程类别得分（满分20）"),
		@Column(name = "major_level_code", attrName = "majorLevelCode", label = "危大档位 super/major/none"),
		@Column(name = "major_base_score", attrName = "majorBaseScore", label = "危大档位基础分 25/30/35"),
		@Column(name = "major_deduct_sum", attrName = "majorDeductSum", label = "勾选扣分累加值（0~30）"),
		@Column(name = "major_deduct_count", attrName = "majorDeductCount", label = "勾选扣分项条数（0~3）"),
		@Column(name = "major_score", attrName = "majorScore", label = "危大工程得分=MAX(0,基础分-累加扣分)（满分35）"),
		@Column(name = "pit_level_code", attrName = "pitLevelCode", label = "生效基坑档位（勾选多项中分值最低者）"),
		@Column(name = "pit_score", attrName = "pitScore", label = "基坑工程得分 0~20"),
		@Column(name = "pit_pick_count", attrName = "pitPickCount", label = "基坑勾选档位条数，恒>=1"),
		@Column(name = "smart_score", attrName = "smartScore", label = "智慧监管系统检查分数 0~100"),
		@Column(name = "smart_source", attrName = "smartSource", label = "1 手工填写 / 2 接口同步"),
		@Column(name = "smart_sync_time", attrName = "smartSyncTime", label = "接口同步时间"),
		@Column(name = "daily_score", attrName = "dailyScore", label = "日常隐患排查得分=ROUND(智慧监管分*0.25)（满分25）"),
		@Column(name = "total_score", attrName = "totalScore", label = "总分 0~100"),
		@Column(name = "risk_level", attrName = "riskLevel", label = "定级结果 1 一级 / 2 二级 / 3 三级 / 4 四级"),
		@Column(name = "prev_record_id", attrName = "prevRecordId", label = "上次检查记录（同项目月份更早的最近一条）"),
		@Column(name = "prev_score", attrName = "prevScore", label = "上次总分快照"),
		@Column(name = "score_delta", attrName = "scoreDelta", label = "本次-上次"),
		@Column(name = "level_change", attrName = "levelChange", label = "0 持平 / 1 上调 / 2 下调"),
		@Column(name = "deduct_item_count", attrName = "deductItemCount", label = "扣分项数=危大勾选数+基坑非满分记1"),
		@Column(name = "conclusion", attrName = "conclusion", label = "检查结论与处置意见，默认取等级话术可改"),
		@Column(name = "record_status", attrName = "recordStatus", label = "0 草稿 / 1 已提交 / 2 已归档 / 3 已作废"),
		@Column(name = "submit_by", attrName = "submitBy", label = "提交人"),
		@Column(name = "submit_date", attrName = "submitDate", label = "提交时间"),
		@Column(name = "void_reason", attrName = "voidReason", label = "作废原因（状态3必填）"),
		@Column(includeEntity = DataEntity.class),
		@Column(includeEntity = BaseEntity.class),
	}, orderBy = "a.inspect_date DESC, a.id DESC"
)
public class QaInspectRecord extends DataEntity<QaInspectRecord> {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 记录号，唯一，按年重置序列生成
	 */
	protected String recordNo;

	/**
	 * 项目台账ID，指向 QA_PROJECT.ID
	 */
	protected String projectId;

	/**
	 * 项目编号冗余，便于导出与跨模块对账
	 */
	protected String projectCode;

	/**
	 * 检查日期
	 */
	protected Date inspectDate;

	/**
	 * 检查月份 yyyy-MM，月度统计与档案格子用
	 */
	protected String inspectMonth;

	/**
	 * 月度占位键：有效记录=月份；作废记录=月份#序号，让出月位
	 */
	protected String monthKey;

	/**
	 * 检查人姓名
	 */
	protected String inspectBy;

	/**
	 * 检查部门
	 */
	protected String inspectDept;

	/**
	 * 工程类别快照
	 */
	protected String categoryCode;

	/**
	 * 工程类别得分（满分20）
	 */
	protected Double catScore;

	/**
	 * 危大档位 super/major/none
	 */
	protected String majorLevelCode;

	/**
	 * 危大档位基础分 25/30/35
	 */
	protected Double majorBaseScore;

	/**
	 * 勾选扣分累加值（0~30）
	 */
	protected Double majorDeductSum;

	/**
	 * 勾选扣分项条数（0~3）
	 */
	protected Integer majorDeductCount;

	/**
	 * 危大工程得分=MAX(0,基础分-累加扣分)（满分35）
	 */
	protected Double majorScore;

	/**
	 * 生效基坑档位（勾选多项中分值最低者）
	 */
	protected String pitLevelCode;

	/**
	 * 基坑工程得分 0~20
	 */
	protected Double pitScore;

	/**
	 * 基坑勾选档位条数，恒>=1
	 */
	protected Integer pitPickCount;

	/**
	 * 智慧监管系统检查分数 0~100
	 */
	protected Double smartScore;

	/**
	 * 1 手工填写 / 2 接口同步
	 */
	protected String smartSource;

	/**
	 * 接口同步时间
	 */
	protected Date smartSyncTime;

	/**
	 * 日常隐患排查得分=ROUND(智慧监管分*0.25)（满分25）
	 */
	protected Double dailyScore;

	/**
	 * 总分 0~100
	 */
	protected Double totalScore;

	/**
	 * 定级结果 1 一级 / 2 二级 / 3 三级 / 4 四级
	 */
	protected String riskLevel;

	/**
	 * 上次检查记录（同项目月份更早的最近一条）
	 */
	protected String prevRecordId;

	/**
	 * 上次总分快照
	 */
	protected Double prevScore;

	/**
	 * 本次-上次
	 */
	protected Double scoreDelta;

	/**
	 * 0 持平 / 1 上调 / 2 下调
	 */
	protected String levelChange;

	/**
	 * 扣分项数=危大勾选数+基坑非满分记1
	 */
	protected Integer deductItemCount;

	/**
	 * 检查结论与处置意见，默认取等级话术可改
	 */
	protected String conclusion;

	/**
	 * 0 草稿 / 1 已提交 / 2 已归档 / 3 已作废
	 */
	protected String recordStatus;

	/**
	 * 提交人
	 */
	protected String submitBy;

	/**
	 * 提交时间
	 */
	protected Date submitDate;

	/**
	 * 作废原因（状态3必填）
	 */
	protected String voidReason;

	public QaInspectRecord() {
		this(null);
	}

	public QaInspectRecord(String id) {
		super(id);
	}

	public String getRecordNo() {
		return recordNo;
	}

	public void setRecordNo(String recordNo) {
		this.recordNo = recordNo;
	}

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getProjectCode() {
		return projectCode;
	}

	public void setProjectCode(String projectCode) {
		this.projectCode = projectCode;
	}

	@JsonFormat(pattern = "yyyy-MM-dd")
	public Date getInspectDate() {
		return inspectDate;
	}

	public void setInspectDate(Date inspectDate) {
		this.inspectDate = inspectDate;
	}

	public String getInspectMonth() {
		return inspectMonth;
	}

	public void setInspectMonth(String inspectMonth) {
		this.inspectMonth = inspectMonth;
	}

	public String getMonthKey() {
		return monthKey;
	}

	public void setMonthKey(String monthKey) {
		this.monthKey = monthKey;
	}

	public String getInspectBy() {
		return inspectBy;
	}

	public void setInspectBy(String inspectBy) {
		this.inspectBy = inspectBy;
	}

	public String getInspectDept() {
		return inspectDept;
	}

	public void setInspectDept(String inspectDept) {
		this.inspectDept = inspectDept;
	}

	public String getCategoryCode() {
		return categoryCode;
	}

	public void setCategoryCode(String categoryCode) {
		this.categoryCode = categoryCode;
	}

	public Double getCatScore() {
		return catScore;
	}

	public void setCatScore(Double catScore) {
		this.catScore = catScore;
	}

	public String getMajorLevelCode() {
		return majorLevelCode;
	}

	public void setMajorLevelCode(String majorLevelCode) {
		this.majorLevelCode = majorLevelCode;
	}

	public Double getMajorBaseScore() {
		return majorBaseScore;
	}

	public void setMajorBaseScore(Double majorBaseScore) {
		this.majorBaseScore = majorBaseScore;
	}

	public Double getMajorDeductSum() {
		return majorDeductSum;
	}

	public void setMajorDeductSum(Double majorDeductSum) {
		this.majorDeductSum = majorDeductSum;
	}

	public Integer getMajorDeductCount() {
		return majorDeductCount;
	}

	public void setMajorDeductCount(Integer majorDeductCount) {
		this.majorDeductCount = majorDeductCount;
	}

	public Double getMajorScore() {
		return majorScore;
	}

	public void setMajorScore(Double majorScore) {
		this.majorScore = majorScore;
	}

	public String getPitLevelCode() {
		return pitLevelCode;
	}

	public void setPitLevelCode(String pitLevelCode) {
		this.pitLevelCode = pitLevelCode;
	}

	public Double getPitScore() {
		return pitScore;
	}

	public void setPitScore(Double pitScore) {
		this.pitScore = pitScore;
	}

	public Integer getPitPickCount() {
		return pitPickCount;
	}

	public void setPitPickCount(Integer pitPickCount) {
		this.pitPickCount = pitPickCount;
	}

	public Double getSmartScore() {
		return smartScore;
	}

	public void setSmartScore(Double smartScore) {
		this.smartScore = smartScore;
	}

	public String getSmartSource() {
		return smartSource;
	}

	public void setSmartSource(String smartSource) {
		this.smartSource = smartSource;
	}

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date getSmartSyncTime() {
		return smartSyncTime;
	}

	public void setSmartSyncTime(Date smartSyncTime) {
		this.smartSyncTime = smartSyncTime;
	}

	public Double getDailyScore() {
		return dailyScore;
	}

	public void setDailyScore(Double dailyScore) {
		this.dailyScore = dailyScore;
	}

	public Double getTotalScore() {
		return totalScore;
	}

	public void setTotalScore(Double totalScore) {
		this.totalScore = totalScore;
	}

	public String getRiskLevel() {
		return riskLevel;
	}

	public void setRiskLevel(String riskLevel) {
		this.riskLevel = riskLevel;
	}

	public String getPrevRecordId() {
		return prevRecordId;
	}

	public void setPrevRecordId(String prevRecordId) {
		this.prevRecordId = prevRecordId;
	}

	public Double getPrevScore() {
		return prevScore;
	}

	public void setPrevScore(Double prevScore) {
		this.prevScore = prevScore;
	}

	public Double getScoreDelta() {
		return scoreDelta;
	}

	public void setScoreDelta(Double scoreDelta) {
		this.scoreDelta = scoreDelta;
	}

	public String getLevelChange() {
		return levelChange;
	}

	public void setLevelChange(String levelChange) {
		this.levelChange = levelChange;
	}

	public Integer getDeductItemCount() {
		return deductItemCount;
	}

	public void setDeductItemCount(Integer deductItemCount) {
		this.deductItemCount = deductItemCount;
	}

	public String getConclusion() {
		return conclusion;
	}

	public void setConclusion(String conclusion) {
		this.conclusion = conclusion;
	}

	public String getRecordStatus() {
		return recordStatus;
	}

	public void setRecordStatus(String recordStatus) {
		this.recordStatus = recordStatus;
	}

	public String getSubmitBy() {
		return submitBy;
	}

	public void setSubmitBy(String submitBy) {
		this.submitBy = submitBy;
	}

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date getSubmitDate() {
		return submitDate;
	}

	public void setSubmitDate(Date submitDate) {
		this.submitDate = submitDate;
	}

	public String getVoidReason() {
		return voidReason;
	}

	public void setVoidReason(String voidReason) {
		this.voidReason = voidReason;
	}

}
