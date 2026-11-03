package com.jeesite.modules.muni.entity;

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
	 * 建设规模
	 */
	private String constructionScale;

	/**
	 * 合同金额（万元），原始值可能带千分位
	 */
	private String contractAmount;

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
