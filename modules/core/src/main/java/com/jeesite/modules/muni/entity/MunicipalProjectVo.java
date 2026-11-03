package com.jeesite.modules.muni.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * 市政工程项目基本信息
 * <p>
 * 一条对应清单中的一行：清单里有项目ID的取登记库信息，没有的原样用清单值。
 * 库中字段为空时回退清单值，保证 106 行都有可展示内容。
 *
 * @author wuchangyue
 * @version 2026-11-03
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MunicipalProjectVo implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 数据来源：db 取登记库，excel 取清单
	 */
	public static final String SOURCE_DB = "db";

	/**
	 * 数据来源：清单（登记库无此项目ID）
	 */
	public static final String SOURCE_EXCEL = "excel";

	// ========== 清单信息 ==========

	/**
	 * 清单序号
	 */
	private Integer seq;

	/**
	 * 项目ID，清单无则为空
	 */
	private Long projectId;

	/**
	 * 数据来源：db / excel
	 */
	private String dataSource;

	/**
	 * 清单中的项目名称
	 */
	@JsonProperty("name")
	private String listProjectName;

	/**
	 * 行业分类
	 */
	private String industry;

	/**
	 * 区域（清单口径）
	 */
	private String district;

	// ========== 项目基本信息 ==========

	/**
	 * 项目名称，取自登记库，库中为空时用清单名称
	 */
	private String projectName;

	/**
	 * 登记库中的行政区
	 */
	private String dbArea;

	/**
	 * 项目地点
	 */
	private String address;

	/**
	 * 面积、长度
	 */
	private String constructionScale;

	/**
	 * 总投资（万元），已去掉千分位
	 */
	private String contractAmount;

	/**
	 * 结构（层数、类型）
	 */
	private String structure;

	/**
	 * 联系人联系方式
	 */
	private String contact;

	/**
	 * 形象进度
	 */
	private String progress;

	// ========== 责任主体 ==========

	/**
	 * 建设单位
	 */
	private String buildUnit;

	/**
	 * 施工单位
	 */
	private String constructUnit;

	/**
	 * 监理单位
	 */
	private String superviseUnit;

	// ========== 监管信息 ==========

	/**
	 * 在建 / 停工
	 */
	private String buildStatus;

	/**
	 * 施工许可证编码
	 */
	private String permitNumber;

	/**
	 * 管辖范围，取自登记库
	 */
	private String jurisdiction;

	/**
	 * 主要风险点
	 */
	private String riskPoint;

	public Integer getSeq() {
		return seq;
	}

	public void setSeq(Integer seq) {
		this.seq = seq;
	}

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	public String getDataSource() {
		return dataSource;
	}

	public void setDataSource(String dataSource) {
		this.dataSource = dataSource;
	}

	public String getListProjectName() {
		return listProjectName;
	}

	public void setListProjectName(String listProjectName) {
		this.listProjectName = listProjectName;
	}

	public String getIndustry() {
		return industry;
	}

	public void setIndustry(String industry) {
		this.industry = industry;
	}

	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	/**
	 * 项目名称
	 * <p>
	 * 登记库有名称时优先，否则回退清单名称，调用方无需再判空。
	 */
	public String getProjectName() {
		return projectName == null || projectName.isEmpty() ? listProjectName : projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getDbArea() {
		return dbArea;
	}

	public void setDbArea(String dbArea) {
		this.dbArea = dbArea;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getConstructionScale() {
		return constructionScale;
	}

	public void setConstructionScale(String constructionScale) {
		this.constructionScale = constructionScale;
	}

	public String getContractAmount() {
		return contractAmount;
	}

	public void setContractAmount(String contractAmount) {
		this.contractAmount = contractAmount;
	}

	public String getStructure() {
		return structure;
	}

	public void setStructure(String structure) {
		this.structure = structure;
	}

	public String getContact() {
		return contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	public String getProgress() {
		return progress;
	}

	public void setProgress(String progress) {
		this.progress = progress;
	}

	public String getBuildUnit() {
		return buildUnit;
	}

	public void setBuildUnit(String buildUnit) {
		this.buildUnit = buildUnit;
	}

	public String getConstructUnit() {
		return constructUnit;
	}

	public void setConstructUnit(String constructUnit) {
		this.constructUnit = constructUnit;
	}

	public String getSuperviseUnit() {
		return superviseUnit;
	}

	public void setSuperviseUnit(String superviseUnit) {
		this.superviseUnit = superviseUnit;
	}

	public String getBuildStatus() {
		return buildStatus;
	}

	public void setBuildStatus(String buildStatus) {
		this.buildStatus = buildStatus;
	}

	public String getPermitNumber() {
		return permitNumber;
	}

	public void setPermitNumber(String permitNumber) {
		this.permitNumber = permitNumber;
	}

	public String getJurisdiction() {
		return jurisdiction;
	}

	public void setJurisdiction(String jurisdiction) {
		this.jurisdiction = jurisdiction;
	}

	public String getRiskPoint() {
		return riskPoint;
	}

	public void setRiskPoint(String riskPoint) {
		this.riskPoint = riskPoint;
	}

}
