package com.jeesite.modules.qa.entity;

import com.jeesite.common.entity.BaseEntity;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;

import java.io.Serial;

/**
 * 基坑档位明细 Entity
 *
 * @author wuchangyue
 * @version 2026-11-03
 */
@Table(name = "QA_INSPECT_PIT", alias = "a", label = "基坑档位明细", columns = {
		@Column(name = "id", attrName = "id", label = "主键ID（JeeSite IdGen 生成）", isPK = true),
		@Column(name = "record_id", attrName = "recordId", label = "检查记录ID，指向 QA_INSPECT_RECORD.ID"),
		@Column(name = "pit_code", attrName = "pitCode", label = "P0~P4 基坑档位"),
		@Column(name = "pit_score", attrName = "pitScore", label = "档位分快照 0/5/10/15/20"),
		@Column(name = "is_effective", attrName = "isEffective", label = "1 生效（分值最低即扣分最大）/ 0 仅勾选未生效"),
		@Column(includeEntity = DataEntity.class),
		@Column(includeEntity = BaseEntity.class),
	}, orderBy = "a.pit_code"
)
public class QaInspectPit extends DataEntity<QaInspectPit> {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 检查记录ID，指向 QA_INSPECT_RECORD.ID
	 */
	protected String recordId;

	/**
	 * P0~P4 基坑档位
	 */
	protected String pitCode;

	/**
	 * 档位分快照 0/5/10/15/20
	 */
	protected Double pitScore;

	/**
	 * 1 生效（分值最低即扣分最大）/ 0 仅勾选未生效
	 */
	protected String isEffective;

	public QaInspectPit() {
		this(null);
	}

	public QaInspectPit(String id) {
		super(id);
	}

	public String getRecordId() {
		return recordId;
	}

	public void setRecordId(String recordId) {
		this.recordId = recordId;
	}

	public String getPitCode() {
		return pitCode;
	}

	public void setPitCode(String pitCode) {
		this.pitCode = pitCode;
	}

	public Double getPitScore() {
		return pitScore;
	}

	public void setPitScore(Double pitScore) {
		this.pitScore = pitScore;
	}

	public String getIsEffective() {
		return isEffective;
	}

	public void setIsEffective(String isEffective) {
		this.isEffective = isEffective;
	}

}
