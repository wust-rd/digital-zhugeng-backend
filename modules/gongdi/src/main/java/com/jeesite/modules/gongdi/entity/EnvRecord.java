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

	/**
	 * 设备ID
	 */
	private Long accessDeviceId;

	/**
	 * 噪声
	 */
	private String noise;

	/**
	 * 温度
	 */
	private String temp;

	/**
	 * 湿度
	 */
	private String humid;

	/**
	 * 扬尘（TSP）
	 */
	private String tsp;

	/**
	 * 采集时间（已格式化为 yyyy-MM-dd HH:mm:ss）
	 */
	private String recordTime;

	public Long getAccessDeviceId() {
		return accessDeviceId;
	}

	public void setAccessDeviceId(Long accessDeviceId) {
		this.accessDeviceId = accessDeviceId;
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
