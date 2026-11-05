package com.jeesite.modules.gongdi.dao;

import com.jeesite.common.mybatis.annotation.MyBatisDao;
import com.jeesite.modules.gongdi.entity.EnvMonitorVo;
import com.jeesite.modules.gongdi.entity.EnvRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 工地环境监测（噪声扬尘）查询 DAO
 * <p>
 * 【重要】本 DAO 绑定工地库（ZHGD_2，只读账号 read_all），
 * 严禁在此新增任何 INSERT / UPDATE / DELETE 语句。
 *
 * @author wuchangyue
 * @version 2026-10-29
 */
@MyBatisDao(dataSourceName = "gongdi")
public interface EnvMonitorDao {

	/**
	 * 一次查出「项目 + 环境监测设备」清单（INNER JOIN，已过滤已删除项目/设备）
	 * <p>
	 * 返回的每行对应一台设备，监测指标字段留空，由明细查询回填。
	 *
	 * @param ids 项目ID集合（非空）
	 */
	List<EnvMonitorVo> findProjectDevices(@Param("ids") List<String> ids);

	/**
	 * 查询某台设备的最新一条监测记录
	 * <p>
	 * 明细表按项目ID分表（Z_ENVIRONMENTAL_RECORD_{项目ID}），表名动态传入，
	 * 调用方必须先用纯数字白名单校验，避免 SQL 注入。
	 * <p>
	 *
	 * @param tableName 分表名，仅允许 Z_ENVIRONMENTAL_RECORD_ + 纯数字
	 * @param deviceId  设备ID
	 */
	EnvRecord findLatestRecord(@Param("tableName") String tableName, @Param("deviceId") Long deviceId);

}
