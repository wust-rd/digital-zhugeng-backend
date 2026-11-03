package com.jeesite.modules.qa.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.qa.entity.QaInspectDeduct;

/**
 * 危大扣分明细 DAO
 * <p>
 * 增删改查全部沿用框架按实体注解生成的语句，按记录批量清理用 phyDeleteByEntity。
 *
 * @author wuchangyue
 * @version 2026-11-03
 */
@MyBatisDao
public interface QaInspectDeductDao extends CrudDao<QaInspectDeduct> {

}
