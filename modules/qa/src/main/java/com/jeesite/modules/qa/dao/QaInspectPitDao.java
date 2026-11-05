package com.jeesite.modules.qa.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.qa.entity.QaInspectPit;

/**
 * 基坑档位明细 DAO
 * <p>
 * 增删改查全部沿用框架按实体注解生成的语句，按记录批量清理用 phyDeleteByEntity。
 *
 * @author wuchangyue
 * @version 2026-11-03
 */
@MyBatisDao
public interface QaInspectPitDao extends CrudDao<QaInspectPit> {

}
