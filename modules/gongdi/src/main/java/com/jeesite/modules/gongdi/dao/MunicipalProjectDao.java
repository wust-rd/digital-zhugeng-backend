package com.jeesite.modules.gongdi.dao;

import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.gongdi.entity.MunicipalEngineering;
import com.jeesite.modules.gongdi.entity.MunicipalResponsibilityUnit;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 项目信息登记库（XMXXDJ）只读查询 DAO
 * <p>
 * 【重要】本 DAO 绑定 xmxxdj 数据源（只读账号 read_all），
 * 严禁在此新增任何 INSERT / UPDATE / DELETE 语句。
 *
 * @author wuchangyue
 * @version 2026-11-03
 */
@MyBatisDao(dataSourceName = "xmxxdj")
public interface MunicipalProjectDao {

	/**
	 * 按工程ID批量查工程基本信息
	 *
	 * @param ids 工程ID集合（非空）
	 * @return 工程列表
	 */
	List<MunicipalEngineering> findEngineeringList(@Param("ids") List<Long> ids);

	/**
	 * 按工程ID批量查责任主体
	 * <p>
	 * 只取建设单位、施工单位、监理单位三类，其余类型（设计、勘察等）不返回。
	 *
	 * @param ids 工程ID集合（非空）
	 * @return 按工程ID与类型排序的单位列表
	 */
	List<MunicipalResponsibilityUnit> findUnitList(@Param("ids") List<Long> ids);

}
