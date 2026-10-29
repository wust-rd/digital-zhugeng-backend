/**
 * Copyright (c) 2013-Now https://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 */
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
	 * 查询某台设备的最新一条监测记录（动态表名版，逐设备调用）
	 * <p>
	 * 明细表按项目ID分表（Z_ENVIRONMENTAL_RECORD_{项目ID}），表名需动态传入，
	 * 调用方必须先用后缀白名单（纯数字）校验，避免 SQL 注入。
	 * <p>
	 * 取数策略：先用主键（趋势自增）倒序取最近 {@code limit} 条，再按原始 RECORD_TIME 倒序取第一条。
	 * 原因是明细表只在主键上有索引（部分分表连 ACCESS_DEVICE_ID 索引都没有，
	 * 且工地库为只读账号、无法加索引），直接 ORDER BY RECORD_TIME 会退化为全表排序——
	 * 实测 165 万行分表：主键法 11ms，时间法 3900ms。
	 *
	 * @param tableName 分表名，仅允许 Z_ENVIRONMENTAL_RECORD_ + 纯数字
	 * @param deviceId  设备ID
	 * @param limit     主键倒序候选条数：1 = 纯主键法（最快，但主键最大 ≠ 时间最新时会取错）；
	 *                  200 = 取最近 200 条再按时间兜底（默认，成本几乎相同）
	 */
	EnvRecord findLatestRecord(@Param("tableName") String tableName,
			@Param("deviceId") Long deviceId,
			@Param("limit") int limit);

	/**
	 * 一次查出 10 个项目下所有环境监测设备的最新监测记录（UNION ALL 硬编码版）
	 * <p>
	 * 10 个项目的分表名<b>全部写死在 XML 里</b>，用 UNION ALL 串成一条 SQL，
	 * 一次数据库往返即可取回全部设备的最新值，替代逐设备循环调用。
	 * <ul>
	 *   <li>SQL 中没有 ${}，表名是常量，不存在表名注入面；</li>
	 *   <li>每条记录由 SQL 用常量 {@code PROJECT_ID} 带出所属项目，回填时按
	 *       「项目ID + 设备ID」精确匹配；</li>
	 *   <li>代价：新增/减少项目必须改 XML 的 UNION ALL 段，项目清单固定为这 10 个。</li>
	 * </ul>
	 *
	 * @return 每台设备一条最新记录；无数据的设备不返回
	 */
	List<EnvRecord> findLatestRecordsUnionAll();

}
