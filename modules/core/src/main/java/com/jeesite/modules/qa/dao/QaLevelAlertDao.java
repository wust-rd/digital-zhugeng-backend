package com.jeesite.modules.qa.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.qa.entity.QaLevelAlert;

/**
 * 等级变化预警 DAO
 * <p>
 * 增删改查全部沿用框架按实体注解生成的语句，记录作废时按记录ID物理删除用 phyDeleteByEntity。
 *
 * @author wuchangyue
 * @version 2026-11-03
 */
@MyBatisDao
public interface QaLevelAlertDao extends CrudDao<QaLevelAlert> {

}
