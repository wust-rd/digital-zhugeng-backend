package com.jeesite.modules.qa.dao;

import com.jeesite.common.dao.CrudDao;
import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.qa.entity.QaProject;

/**
 * 质安项目台账 DAO
 * <p>
 * 增删改查全部沿用框架按实体注解生成的语句，空值覆盖由 SqlMap.updateNullValue 控制。
 *
 * @author wuchangyue
 * @version 2026-11-03
 */
@MyBatisDao
public interface QaProjectDao extends CrudDao<QaProject> {

}
