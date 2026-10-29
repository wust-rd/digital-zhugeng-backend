/**
 * Copyright (c) 2013-Now https://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 */
package com.jeesite.modules.gongdi.entity;

import java.io.Serializable;

/**
 * 环境监测明细记录（各项目分表 Z_ENVIRONMENTAL_RECORD_{项目ID} 的最新一条）
 *
 * @author wuchangyue
 * @version 2026-10-29
 */
public class EnvRecord implements Serializable {

	private static final long serialVersionUID = 1L;

	/** 设备ID */
	private Long accessDeviceId;

	/**
	 * 项目ID。
	 * 仅 UNION ALL 版（{@code findLatestRecordsUnionAll}）会填充——
	 * 一次查 10 张分表时，必须由 SQL 用常量带出"这条记录属于哪个项目"，
	 * 否则回来无法和设备对应上。单表版不填。
	 */
	private Long engineeringId;

	/** 噪声 */
	private String noise;

	/** 温度 */
	private String temp;

	/** 湿度 */
	private String humid;

	/** 扬尘（TSP） */
	private String tsp;

	/** 采集时间（已格式化为 yyyy-MM-dd HH:mm:ss） */
	private String recordTime;

	public Long getAccessDeviceId() {
		return accessDeviceId;
	}

	public void setAccessDeviceId(Long accessDeviceId) {
		this.accessDeviceId = accessDeviceId;
	}

	public Long getEngineeringId() {
		return engineeringId;
	}

	public void setEngineeringId(Long engineeringId) {
		this.engineeringId = engineeringId;
	}

	public String getNoise() {
		return noise;
	}

	public void setNoise(String noise) {
		this.noise = noise;
	}

	public String getTemp() {
		return temp;
	}

	public void setTemp(String temp) {
		this.temp = temp;
	}

	public String getHumid() {
		return humid;
	}

	public void setHumid(String humid) {
		this.humid = humid;
	}

	public String getTsp() {
		return tsp;
	}

	public void setTsp(String tsp) {
		this.tsp = tsp;
	}

	public String getRecordTime() {
		return recordTime;
	}

	public void setRecordTime(String recordTime) {
		this.recordTime = recordTime;
	}

}
