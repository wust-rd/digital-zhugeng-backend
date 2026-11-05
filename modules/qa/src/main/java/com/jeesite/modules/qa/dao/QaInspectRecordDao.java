package com.jeesite.modules.qa.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.qa.entity.QaInspectRecord;

/**
 * 月度检查记录 DAO
 *
 * @author wuchangyue
 * @version 2026-11-03
 */
@MyBatisDao
public interface QaInspectRecordDao extends CrudDao<QaInspectRecord> {

	/**
	 * 取检查记录号序列的下一个值
	 *
	 * @return 序列值
	 */
	Long nextRecordNo();

}
