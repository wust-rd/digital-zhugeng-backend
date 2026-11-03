package com.jeesite.modules.qa.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jeesite.common.entity.BaseEntity;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;

import java.io.Serial;
import java.util.Date;

/**
 * 等级变化预警 Entity
 *
 * @author wuchangyue
 * @version 2026-11-03
 */
@Table(name = "QA_LEVEL_ALERT", alias = "a", label = "等级变化预警", columns = {
		@Column(name = "id", attrName = "id", label = "主键ID（JeeSite IdGen 生成）", isPK = true),
		@Column(name = "project_id", attrName = "projectId", label = "项目台账ID，指向 QA_PROJECT.ID"),
		@Column(name = "prev_record_id", attrName = "prevRecordId", label = "上次检查记录ID"),
		@Column(name = "curr_record_id", attrName = "currRecordId", label = "本次检查记录ID"),
		@Column(name = "prev_score", attrName = "prevScore", label = "上次得分"),
		@Column(name = "curr_score", attrName = "currScore", label = "本次得分"),
		@Column(name = "score_delta", attrName = "scoreDelta", label = "差值，负数=下降"),
		@Column(name = "prev_level", attrName = "prevLevel", label = "上次等级"),
		@Column(name = "curr_level", attrName = "currLevel", label = "本次等级"),
		@Column(name = "alert_type", attrName = "alertType", label = "1 分数下降 / 2 等级下调 / 3 连续两次下降"),
		@Column(name = "alert_msg", attrName = "alertMsg", label = "预警文案，如 84 → 72 分 · 三级 → 三级"),
		@Column(name = "push_status", attrName = "pushStatus", label = "0 未推送 / 1 已推送 / 2 失败"),
		@Column(name = "push_time", attrName = "pushTime", label = "推送时间"),
		@Column(name = "receiver", attrName = "receiver", label = "接收人（项目负责人）"),
		@Column(name = "push_channel", attrName = "pushChannel", label = "SMS / WECHAT / APP"),
		@Column(includeEntity = DataEntity.class),
		@Column(includeEntity = BaseEntity.class),
	}, orderBy = "a.create_date DESC"
)
public class QaLevelAlert extends DataEntity<QaLevelAlert> {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 项目台账ID，指向 QA_PROJECT.ID
	 */
	protected String projectId;

	/**
	 * 上次检查记录ID
	 */
	protected String prevRecordId;

	/**
	 * 本次检查记录ID
	 */
	protected String currRecordId;

	/**
	 * 上次得分
	 */
	protected Double prevScore;

	/**
	 * 本次得分
	 */
	protected Double currScore;

	/**
	 * 差值，负数=下降
	 */
	protected Double scoreDelta;

	/**
	 * 上次等级
	 */
	protected String prevLevel;

	/**
	 * 本次等级
	 */
	protected String currLevel;

	/**
	 * 1 分数下降 / 2 等级下调 / 3 连续两次下降
	 */
	protected String alertType;

	/**
	 * 预警文案，如 84 → 72 分 · 三级 → 三级
	 */
	protected String alertMsg;

	/**
	 * 0 未推送 / 1 已推送 / 2 失败
	 */
	protected String pushStatus;

	/**
	 * 推送时间
	 */
	protected Date pushTime;

	/**
	 * 接收人（项目负责人）
	 */
	protected String receiver;

	/**
	 * SMS / WECHAT / APP
	 */
	protected String pushChannel;

	public QaLevelAlert() {
		this(null);
	}

	public QaLevelAlert(String id) {
		super(id);
	}

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getPrevRecordId() {
		return prevRecordId;
	}

	public void setPrevRecordId(String prevRecordId) {
		this.prevRecordId = prevRecordId;
	}

	public String getCurrRecordId() {
		return currRecordId;
	}

	public void setCurrRecordId(String currRecordId) {
		this.currRecordId = currRecordId;
	}

	public Double getPrevScore() {
		return prevScore;
	}

	public void setPrevScore(Double prevScore) {
		this.prevScore = prevScore;
	}

	public Double getCurrScore() {
		return currScore;
	}

	public void setCurrScore(Double currScore) {
		this.currScore = currScore;
	}

	public Double getScoreDelta() {
		return scoreDelta;
	}

	public void setScoreDelta(Double scoreDelta) {
		this.scoreDelta = scoreDelta;
	}

	public String getPrevLevel() {
		return prevLevel;
	}

	public void setPrevLevel(String prevLevel) {
		this.prevLevel = prevLevel;
	}

	public String getCurrLevel() {
		return currLevel;
	}

	public void setCurrLevel(String currLevel) {
		this.currLevel = currLevel;
	}

	public String getAlertType() {
		return alertType;
	}

	public void setAlertType(String alertType) {
		this.alertType = alertType;
	}

	public String getAlertMsg() {
		return alertMsg;
	}

	public void setAlertMsg(String alertMsg) {
		this.alertMsg = alertMsg;
	}

	public String getPushStatus() {
		return pushStatus;
	}

	public void setPushStatus(String pushStatus) {
		this.pushStatus = pushStatus;
	}

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date getPushTime() {
		return pushTime;
	}

	public void setPushTime(Date pushTime) {
		this.pushTime = pushTime;
	}

	public String getReceiver() {
		return receiver;
	}

	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}

	public String getPushChannel() {
		return pushChannel;
	}

	public void setPushChannel(String pushChannel) {
		this.pushChannel = pushChannel;
	}

}
