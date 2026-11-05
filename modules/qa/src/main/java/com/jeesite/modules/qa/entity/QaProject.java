package com.jeesite.modules.qa.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jeesite.common.entity.BaseEntity;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;
import com.jeesite.common.mybatis.mapper.query.QueryType;

import java.io.Serial;
import java.util.Date;

/**
 * 质安项目台账 Entity
 *
 * @author wuchangyue
 * @version 2026-11-03
 */
@Table(name = "QA_PROJECT", alias = "a", label = "质安项目台账", columns = {
		@Column(name = "id", attrName = "id", label = "主键ID（JeeSite IdGen 生成）", isPK = true),
		@Column(name = "project_code", attrName = "projectCode", label = "项目编号，与市政台账同口径，唯一"),
		@Column(name = "project_name", attrName = "projectName", label = "项目名称（含标段全称）", queryType = QueryType.LIKE),
		@Column(name = "district_code", attrName = "districtCode", label = "责任片区编码，可空"),
		@Column(name = "district_name", attrName = "districtName", label = "责任片区名称冗余"),
		@Column(name = "category_code", attrName = "categoryCode", label = "工程类别 tunnel/bridge/road/minor，决定静态分"),
		@Column(name = "owner_unit", attrName = "ownerUnit", label = "建设单位（业主）"),
		@Column(name = "build_unit", attrName = "buildUnit", label = "施工单位，多家用顿号分隔"),
		@Column(name = "supervise_unit", attrName = "superviseUnit", label = "监理单位"),
		@Column(name = "contract_amount", attrName = "contractAmount", label = "合同金额（万元）"),
		@Column(name = "start_date", attrName = "startDate", label = "开工日期"),
		@Column(name = "project_mgr", attrName = "projectMgr", label = "项目负责人"),
		@Column(name = "mgr_phone", attrName = "mgrPhone", label = "联系电话"),
		@Column(name = "filing_no", attrName = "filingNo", label = "安全监督手续编号"),
		@Column(name = "build_status", attrName = "buildStatus", label = "1 在建 / 2 停工 / 3 完工未移交 / 4 已移交"),
		@Column(name = "monitor_flag", attrName = "monitorFlag", label = "是否在监 1 是 / 0 否，KPI 与覆盖率分母只算在监"),
		@Column(name = "last_record_id", attrName = "lastRecordId", label = "最近一次已提交检查记录ID（冗余）"),
		@Column(name = "last_inspect_date", attrName = "lastInspectDate", label = "最近检查日期（冗余）"),
		@Column(name = "last_inspect_month", attrName = "lastInspectMonth", label = "最近检查月份 yyyy-MM（冗余）"),
		@Column(name = "next_due_month", attrName = "nextDueMonth", label = "下次应检月份=最近检查月份次月，未检过为空"),
		@Column(name = "last_score", attrName = "lastScore", label = "最近得分（冗余）"),
		@Column(name = "last_level", attrName = "lastLevel", label = "最近等级 1~4（冗余）"),
		@Column(name = "inspect_count", attrName = "inspectCount", label = "累计检查次数"),
		@Column(includeEntity = DataEntity.class),
		@Column(includeEntity = BaseEntity.class),
	}, orderBy = "a.project_code"
)
public class QaProject extends DataEntity<QaProject> {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 项目编号，与市政台账同口径，唯一
	 */
	protected String projectCode;

	/**
	 * 项目名称（含标段全称）
	 */
	protected String projectName;

	/**
	 * 责任片区编码，可空
	 */
	protected String districtCode;

	/**
	 * 责任片区名称冗余
	 */
	protected String districtName;

	/**
	 * 工程类别 tunnel/bridge/road/minor，决定静态分
	 */
	protected String categoryCode;

	/**
	 * 建设单位（业主）
	 */
	protected String ownerUnit;

	/**
	 * 施工单位，多家用顿号分隔
	 */
	protected String buildUnit;

	/**
	 * 监理单位
	 */
	protected String superviseUnit;

	/**
	 * 合同金额（万元）
	 */
	protected Double contractAmount;

	/**
	 * 开工日期
	 */
	protected Date startDate;

	/**
	 * 项目负责人
	 */
	protected String projectMgr;

	/**
	 * 联系电话
	 */
	protected String mgrPhone;

	/**
	 * 安全监督手续编号
	 */
	protected String filingNo;

	/**
	 * 1 在建 / 2 停工 / 3 完工未移交 / 4 已移交
	 */
	protected String buildStatus;

	/**
	 * 是否在监 1 是 / 0 否，KPI 与覆盖率分母只算在监
	 */
	protected String monitorFlag;

	/**
	 * 最近一次已提交检查记录ID（冗余）
	 */
	protected String lastRecordId;

	/**
	 * 最近检查日期（冗余）
	 */
	protected Date lastInspectDate;

	/**
	 * 最近检查月份 yyyy-MM（冗余）
	 */
	protected String lastInspectMonth;

	/**
	 * 下次应检月份=最近检查月份次月，未检过为空
	 */
	protected String nextDueMonth;

	/**
	 * 最近得分（冗余）
	 */
	protected Double lastScore;

	/**
	 * 最近等级 1~4（冗余）
	 */
	protected String lastLevel;

	/**
	 * 累计检查次数
	 */
	protected Integer inspectCount;

	/**
	 * 检查状态：1 正常 / 2 待检查（非持久化，查询时按最近检查月推导）
	 */
	protected String checkStatus;

	public QaProject() {
		this(null);
	}

	public QaProject(String id) {
		super(id);
	}

	public String getProjectCode() {
		return projectCode;
	}

	public void setProjectCode(String projectCode) {
		this.projectCode = projectCode;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getDistrictCode() {
		return districtCode;
	}

	public void setDistrictCode(String districtCode) {
		this.districtCode = districtCode;
	}

	public String getDistrictName() {
		return districtName;
	}

	public void setDistrictName(String districtName) {
		this.districtName = districtName;
	}

	public String getCategoryCode() {
		return categoryCode;
	}

	public void setCategoryCode(String categoryCode) {
		this.categoryCode = categoryCode;
	}

	public String getOwnerUnit() {
		return ownerUnit;
	}

	public void setOwnerUnit(String ownerUnit) {
		this.ownerUnit = ownerUnit;
	}

	public String getBuildUnit() {
		return buildUnit;
	}

	public void setBuildUnit(String buildUnit) {
		this.buildUnit = buildUnit;
	}

	public String getSuperviseUnit() {
		return superviseUnit;
	}

	public void setSuperviseUnit(String superviseUnit) {
		this.superviseUnit = superviseUnit;
	}

	public Double getContractAmount() {
		return contractAmount;
	}

	public void setContractAmount(Double contractAmount) {
		this.contractAmount = contractAmount;
	}

	@JsonFormat(pattern = "yyyy-MM-dd")
	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public String getProjectMgr() {
		return projectMgr;
	}

	public void setProjectMgr(String projectMgr) {
		this.projectMgr = projectMgr;
	}

	public String getMgrPhone() {
		return mgrPhone;
	}

	public void setMgrPhone(String mgrPhone) {
		this.mgrPhone = mgrPhone;
	}

	public String getFilingNo() {
		return filingNo;
	}

	public void setFilingNo(String filingNo) {
		this.filingNo = filingNo;
	}

	public String getBuildStatus() {
		return buildStatus;
	}

	public void setBuildStatus(String buildStatus) {
		this.buildStatus = buildStatus;
	}

	public String getMonitorFlag() {
		return monitorFlag;
	}

	public void setMonitorFlag(String monitorFlag) {
		this.monitorFlag = monitorFlag;
	}

	public String getLastRecordId() {
		return lastRecordId;
	}

	public void setLastRecordId(String lastRecordId) {
		this.lastRecordId = lastRecordId;
	}

	@JsonFormat(pattern = "yyyy-MM-dd")
	public Date getLastInspectDate() {
		return lastInspectDate;
	}

	public void setLastInspectDate(Date lastInspectDate) {
		this.lastInspectDate = lastInspectDate;
	}

	public String getLastInspectMonth() {
		return lastInspectMonth;
	}

	public void setLastInspectMonth(String lastInspectMonth) {
		this.lastInspectMonth = lastInspectMonth;
	}

	public String getNextDueMonth() {
		return nextDueMonth;
	}

	public void setNextDueMonth(String nextDueMonth) {
		this.nextDueMonth = nextDueMonth;
	}

	public Double getLastScore() {
		return lastScore;
	}

	public void setLastScore(Double lastScore) {
		this.lastScore = lastScore;
	}

	public String getLastLevel() {
		return lastLevel;
	}

	public void setLastLevel(String lastLevel) {
		this.lastLevel = lastLevel;
	}

	public Integer getInspectCount() {
		return inspectCount;
	}

	public void setInspectCount(Integer inspectCount) {
		this.inspectCount = inspectCount;
	}

	public String getCheckStatus() {
		return checkStatus;
	}

	public void setCheckStatus(String checkStatus) {
		this.checkStatus = checkStatus;
	}

}
