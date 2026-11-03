package com.jeesite.modules.qa.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

/**
 * 检查填报提交体
 * <p>
 * 主表字段直接继承检查记录，附加两份勾选明细。
 * 分值一律由后端按编码查标准，前端只传编码。
 *
 * @author wuchangyue
 * @version 2026-11-03
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class QaInspectForm extends QaInspectRecord {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 危大扣分项勾选明细
	 */
	private List<QaInspectDeduct> deductList = new ArrayList<>();

	/**
	 * 基坑档位勾选明细
	 */
	private List<QaInspectPit> pitList = new ArrayList<>();

	public List<QaInspectDeduct> getDeductList() {
		return deductList;
	}

	public void setDeductList(List<QaInspectDeduct> deductList) {
		this.deductList = deductList;
	}

	public List<QaInspectPit> getPitList() {
		return pitList;
	}

	public void setPitList(List<QaInspectPit> pitList) {
		this.pitList = pitList;
	}

}
