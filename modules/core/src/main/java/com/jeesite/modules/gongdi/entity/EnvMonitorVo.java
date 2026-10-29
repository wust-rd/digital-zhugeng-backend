/**
 * Copyright (c) 2013-Now https://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 */
package com.jeesite.modules.gongdi.entity;

import java.io.Serializable;

/**
 * 工地噪声扬尘最新监测值（项目 + 设备 + 最新指标，一行对应一台设备）
 *
 * @author wuchangyue
 * @version 2026-10-29
 */
public class EnvMonitorVo implements Serializable {

	private static final long serialVersionUID = 1L;

	// ========== 项目信息 ==========

	/** 项目ID */
	private Long engineeringId;

	/** 项目名称 */
	private String engineeringName;

	/** 经度 */
	private String lng;

	/** 纬度 */
	private String lat;

	/** 项目地址 */
	private String address;

	// ========== 设备信息 ==========

	/** 设备ID */
	private Long accessDeviceId;

	/** 设备编号 */
	private String deviceNo;

	/** 设备名称 */
	private String deviceName;

	/** 监测部位 */
	private String monitorPlace;

	/** 设备状态 */
	private Integer deviceStatus;

	/** 是否在线（1 在线 0 离线） */
	private Integer isOnline;

	// ========== 最新监测值 ==========

	/** 噪声 */
	private String noise;

	/** 温度 */
	private String temp;

	/** 湿度 */
	private String humid;

	/** 扬尘（TSP） */
	private String tsp;

	/** 采集时间 yyyy-MM-dd HH:mm:ss */
	private String recordTime;

	/** 是否有监测数据（false 表示设备存在但分表内无有效记录） */
	private boolean hasData;

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

	public String getLng() {
		return lng;
	}

	public void setLng(String lng) {
		this.lng = lng;
	}

	public String getLat() {
		return lat;
	}

	public void setLat(String lat) {
		this.lat = lat;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Long getAccessDeviceId() {
		return accessDeviceId;
	}

	public void setAccessDeviceId(Long accessDeviceId) {
		this.accessDeviceId = accessDeviceId;
	}

	public String getDeviceNo() {
		return deviceNo;
	}

	public void setDeviceNo(String deviceNo) {
		this.deviceNo = deviceNo;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public String getMonitorPlace() {
		return monitorPlace;
	}

	public void setMonitorPlace(String monitorPlace) {
		this.monitorPlace = monitorPlace;
	}

	public Integer getDeviceStatus() {
		return deviceStatus;
	}

	public void setDeviceStatus(Integer deviceStatus) {
		this.deviceStatus = deviceStatus;
	}

	public Integer getIsOnline() {
		return isOnline;
	}

	public void setIsOnline(Integer isOnline) {
		this.isOnline = isOnline;
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

	public boolean isHasData() {
		return hasData;
	}

	public void setHasData(boolean hasData) {
		this.hasData = hasData;
	}

}
