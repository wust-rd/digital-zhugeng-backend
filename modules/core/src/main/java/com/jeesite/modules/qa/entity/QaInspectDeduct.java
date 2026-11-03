package com.jeesite.modules.qa.entity;

import com.jeesite.common.entity.BaseEntity;
import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.mybatis.annotation.Column;
import com.jeesite.common.mybatis.annotation.Table;

import java.io.Serial;

/**
 * 危大扣分明细 Entity
 *
 * @author wuchangyue
 * @version 2026-11-03
 */
@Table(name = "QA_INSPECT_DEDUCT", alias = "a", label = "危大扣分明细", columns = {
		@Column(name = "id", attrName = "id", label = "主键ID（JeeSite IdGen 生成）", isPK = true),
		@Column(name = "record_id", attrName = "recordId", label = "检查记录ID，指向 QA_INSPECT_RECORD.ID"),
		@Column(name = "deduct_code", attrName = "deductCode", label = "plan 未编审方案及论证 / site 未旁站或与方案不一致 / accept 未验收即进下道工序"),
		@Column(name = "deduct_score", attrName = "deductScore", label = "扣分值快照 15/5/10"),
		@Column(includeEntity = DataEntity.class),
		@Column(includeEntity = BaseEntity.class),
	}, orderBy = "a.deduct_code"
)
public class QaInspectDeduct extends DataEntity<QaInspectDeduct> {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 检查记录ID，指向 QA_INSPECT_RECORD.ID
	 */
	protected String recordId;

	/**
	 * plan 未编审方案及论证 / site 未旁站或与方案不一致 / accept 未验收即进下道工序
	 */
	protected String deductCode;

	/**
	 * 扣分值快照 15/5/10
	 */
	protected Double deductScore;

	public QaInspectDeduct() {
		this(null);
	}

	public QaInspectDeduct(String id) {
		super(id);
	}

	public String getRecordId() {
		return recordId;
	}

	public void setRecordId(String recordId) {
		this.recordId = recordId;
	}

	public String getDeductCode() {
		return deductCode;
	}

	public void setDeductCode(String deductCode) {
		this.deductCode = deductCode;
	}

	public Double getDeductScore() {
		return deductScore;
	}

	public void setDeductScore(Double deductScore) {
		this.deductScore = deductScore;
	}

}
