package com.jeesite.modules.gongdi.entity;

import java.io.Serializable;

/**
 * 工程责任主体（XMXXDJ.supervision_responsibility_unit）
 * <p>
 * 一个工程下同一类型可能有多家单位，按工程与类型聚合后拼接展示。
 *
 * @author wuchangyue
 * @version 2026-11-03
 */
public class MunicipalResponsibilityUnit implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 工程ID，关联 supervision_engineering.id
	 */
	private Long engineeringId;

	/**
	 * 单位类型：建设单位 / 施工单位 / 监理单位 等
	 */
	private String unitType;

	/**
	 * 单位名称
	 */
	private String unitName;

	public Long getEngineeringId() {
		return engineeringId;
	}

	public void setEngineeringId(Long engineeringId) {
		this.engineeringId = engineeringId;
	}

	public String getUnitType() {
		return unitType;
	}

	public void setUnitType(String unitType) {
		this.unitType = unitType;
	}

	public String getUnitName() {
		return unitName;
	}

	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}

}
