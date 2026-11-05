package com.jeesite.modules.gongdi.entity;

import java.io.Serializable;

/**
 * 项目信息登记库的工程基本信息（XMXXDJ.supervision_engineering，只读取用字段）
 *
 * @author wuchangyue
 * @version 2026-11-03
 */
public class MunicipalEngineering implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 工程ID
	 */
	private Long engineeringId;

	/**
	 * 工程名称
	 */
	private String engineeringName;

	/**
	 * 行政区
	 */
	private String area;

	/**
	 * 工程地址
	 */
	private String address;

	/**
	 * 经度
	 */
	private String longitude;

	/**
	 * 纬度
	 */
	private String latitude;

	/**
	 * 建设规模
	 */
	private String constructionScale;

	/**
	 * 合同金额（万元），原始值可能带千分位
	 */
	private String contractAmount;

	/**
	 * 计划开工时间
	 */
	private java.util.Date plannedStartTime;

	/**
	 * 计划竣工时间
	 */
	private java.util.Date plannedCompletionTime;

	/**
	 * 是否重点项目（1 是 0 否）
	 */
	private Integer keyProjects;

	/**
	 * 是否须报建（1 是 0 否）
	 */
	private Integer reportRequired;

	/**
	 * 施工许可证编号
	 */
	private String permitNumber;

	/**
	 * 工程状态
	 */
	private String projectState;

	/**
	 * 管辖范围
	 */
	private String jurisdiction;

	public Long getEngineeringId() {
		return engineeringId;
	}

	public void setEngineeringId(Long engineeringId) {
		this.engineeringId = engineeringId;
	}

	public String getEngineeringName() {
		return engineeringName;
	}

	public void setEngineeringName(String engineeringName) {
		this.engineeringName = engineeringName;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
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

	public java.util.Date getPlannedStartTime() {
		return plannedStartTime;
	}

	public void setPlannedStartTime(java.util.Date plannedStartTime) {
		this.plannedStartTime = plannedStartTime;
	}

	public java.util.Date getPlannedCompletionTime() {
		return plannedCompletionTime;
	}

	public void setPlannedCompletionTime(java.util.Date plannedCompletionTime) {
		this.plannedCompletionTime = plannedCompletionTime;
	}

	public Integer getKeyProjects() {
		return keyProjects;
	}

	public void setKeyProjects(Integer keyProjects) {
		this.keyProjects = keyProjects;
	}

	public Integer getReportRequired() {
		return reportRequired;
	}

	public void setReportRequired(Integer reportRequired) {
		this.reportRequired = reportRequired;
	}

	public String getPermitNumber() {
		return permitNumber;
	}

	public void setPermitNumber(String permitNumber) {
		this.permitNumber = permitNumber;
	}

	public String getProjectState() {
		return projectState;
	}

	public void setProjectState(String projectState) {
		this.projectState = projectState;
	}

	public String getJurisdiction() {
		return jurisdiction;
	}

	public void setJurisdiction(String jurisdiction) {
		this.jurisdiction = jurisdiction;
	}

}
