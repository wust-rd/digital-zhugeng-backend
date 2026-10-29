/**
 * Copyright (c) 2013-Now https://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 */
package com.jeesite.modules.gongdi.service;

import com.jeesite.modules.gongdi.dao.EnvMonitorDao;
import com.jeesite.modules.gongdi.entity.EnvMonitorVo;
import com.jeesite.modules.gongdi.entity.EnvRecord;
import com.jeesite.modules.gongdi.enums.EnvLevel;
import com.jeesite.modules.gongdi.util.EnvLevelEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 工地噪声扬尘查询服务
 * <p>
 * 业务口径：给定若干项目，返回每个项目下每台「环境监测设备（DEVICE_TYPE = 1）」的
 * 最新一条监测记录（噪声 NOISE、温度 TEMP、湿度 HUMID、扬尘 TSP），并带上项目基本信息。
 * 一个项目有多台设备时，每台设备各返回一条。
 * <p>
 * 数据来源：工地库 ZHGD_2（只读）
 * <p>
 * 明细查询有两种实现，取到的数据完全一致，可通过 {@code gongdi-env.sql-mode} 切换：
 * <ol>
 *   <li><b>dynamic（默认）</b>——逐台设备调用，表名动态拼接（纯数字白名单校验），
 *       单台走主键倒序，11 台合计约 100ms；</li>
 *   <li><b>hardcoded</b>——10 个项目的分表名全部写死在 XML 中用 UNION ALL 串成一条 SQL，
 *       一次往返取回全部，SQL 可离线审计；代价是每段全表扫窗口排序，大表到秒级，
 *       且项目清单固定为这 10 个。</li>
 * </ol>
 *
 * @author wuchangyue
 * @version 2026-10-29
 */
@Service
public class EnvMonitorService {

	private static final Logger logger = LoggerFactory.getLogger(EnvMonitorService.class);

	/** 各项目环境监测明细分表前缀，完整表名 = 前缀 + 项目ID */
	private static final String RECORD_TABLE_PREFIX = "Z_ENVIRONMENTAL_RECORD_";

	/** 项目ID / 表名后缀白名单：纯数字，防止动态表名注入 */
	private static final Pattern NUMERIC = Pattern.compile("^\\d{1,19}$");

	/** SQL 实现模式：动态表名，逐设备调用 */
	public static final String MODE_DYNAMIC = "dynamic";

	/** SQL 实现模式：10 个项目写死 + UNION ALL，一次查完 */
	public static final String MODE_HARDCODED = "hardcoded";

	/**
	 * 硬编码 UNION ALL 版已写入 SQL 的项目清单（与 EnvMonitorDao.xml 的 10 段 UNION ALL 一一对应）。
	 * 用于：硬编码模式下把 SQL 里没有的项目提前剔除，避免"查了却没有数据"的误解。
	 */
	private static final Set<String> FIXED_TABLE_PROJECTS = Set.of(
			"635320", "644423", "632474", "636613", "650154",
			"636685", "635686", "632472", "636927", "635418");

	/** 默认查询的 10 个项目，可用配置 gongdi-env.project-ids 覆盖 */
	@Value("${gongdi-env.project-ids:635320,644423,632474,636613,650154,636685,635686,632472,636927,635418}")
	private String defaultProjectIds;

	/** SQL 实现模式，可用 gongdi-env.sql-mode 配置；接口参数可临时覆盖 */
	@Value("${gongdi-env.sql-mode:dynamic}")
	private String defaultSqlMode;

	/**
	 * dynamic 模式下取最新值时，主键倒序的候选条数，可用 gongdi-env.candidate-limit 配置。
	 * <ul>
	 *   <li>1 → 纯主键法：SQL 最短、最快，但"主键最大即时间最新"的假设一旦被
	 *       补录/回溯导入打破，会取到错误记录且不报错；</li>
	 *   <li>>1 → 多扫 N 行按 RECORD_TIME 兜底；实测 N=200 时耗时与 1 几乎相同。</li>
	 * </ul>
	 * hardcoded（UNION ALL）模式不使用该参数。
	 */
	@Value("${gongdi-env.candidate-limit:1}")
	private int candidateLimit;

	/**
	 * 临时排除的设备ID（英文逗号分隔），可用 gongdi-env.exclude-device-ids 配置。
	 * <p>
	 * 用途：部分设备虽仍挂在项目下，但已停报很久（最后一条数据停在数月甚至数年前），
	 * 每次查询不但拿不到有意义的值，还要多付一次数据库往返。配置后可整行剔除。
	 * <p>
	 * 过滤发生在明细查询【之前】，被排除的设备不会产生任何 SQL。清空该项即恢复全部查询。
	 */
	@Value("${gongdi-env.exclude-device-ids:}")
	private String excludeDeviceIds;

	private final EnvMonitorDao envMonitorDao;

	public EnvMonitorService(EnvMonitorDao envMonitorDao) {
		this.envMonitorDao = envMonitorDao;
	}

	/**
	 * 查询指定项目下所有环境监测设备的最新监测值（使用配置的默认 SQL 模式）
	 *
	 * @param projectIds 项目ID列表；为空则返回空集合（默认项目由 resolveProjectIds 处理）
	 * @return 每台设备一条；无设备的项目不产生数据
	 */
	public List<EnvMonitorVo> queryLatest(List<String> projectIds) {
		return queryLatest(projectIds, null);
	}

	/**
	 * 查询指定项目下所有环境监测设备的最新监测值
	 *
	 * @param projectIds 项目ID列表；为空则返回空集合（默认项目由 resolveProjectIds 处理）
	 * @param sqlMode    SQL 实现模式：{@link #MODE_DYNAMIC}（默认）或 {@link #MODE_HARDCODED}；
	 *                   传 null/空/非法值时使用配置 gongdi-env.sql-mode
	 * @return 每台设备一条；无设备的项目不产生数据
	 */
	public List<EnvMonitorVo> queryLatest(List<String> projectIds, String sqlMode) {

		List<String> ids = normalize(projectIds);
		if (ids.isEmpty()) {
			logger.warn("[环境监测] 未指定任何有效项目ID，返回空结果");
			return new ArrayList<>();
		}

		// 1. 一次 JOIN 查出「项目 + 设备」清单，作为结果骨架
		List<EnvMonitorVo> list = envMonitorDao.findProjectDevices(ids);

		// 2. 剔除 configured 排除的设备（停报设备）。
		//    必须在明细查询之前过滤，这样被排除的设备一条 SQL 都不会发。
		Set<Long> excluded = resolveExcludeDevices();
		if (!excluded.isEmpty() && !list.isEmpty()) {
			int before = list.size();
			list.removeIf(vo -> vo.getAccessDeviceId() != null && excluded.contains(vo.getAccessDeviceId()));
			if (before != list.size()) {
				logger.info("[环境监测] 按配置排除 {} 台停报设备，设备清单 {} → {} 条",
						excluded.size(), before, list.size());
			}
		}
		if (list.isEmpty()) {
			logger.warn("[环境监测] 项目 {} 下无有效环境监测设备(DEVICE_TYPE=1)或全部被排除", ids);
			return list;
		}

		// 3. 补最新监测值
		if (MODE_HARDCODED.equalsIgnoreCase(resolveSqlMode(sqlMode))) {
			fillByUnionAll(list);
		} else {
			fillByDeviceLoop(list);
		}
		return list;
	}

	/**
	 * hardcoded 模式：一次 UNION ALL 查回 10 个项目全部设备的最新记录，
	 * 再按「项目ID + 设备ID」回填到骨架行上。
	 * <p>
	 * 只调用一次数据库，不再按设备循环。
	 */
	private void fillByUnionAll(List<EnvMonitorVo> list) {

		long start = System.currentTimeMillis();
		List<EnvRecord> records = envMonitorDao.findLatestRecordsUnionAll();
		long cost = System.currentTimeMillis() - start;

		// 用「项目ID_设备ID」做联合键：不同项目的设备ID理论上不重复，
		// 但按项目+设备双键匹配更稳妥，避免任何撞号风险。
		Map<String, EnvRecord> recordMap = new HashMap<>(records.size() * 2);
		for (EnvRecord r : records) {
			recordMap.put(deviceKey(r.getEngineeringId(), r.getAccessDeviceId()), r);
		}

		int filled = 0;
		for (EnvMonitorVo vo : list) {
			EnvRecord r = recordMap.get(deviceKey(vo.getEngineeringId(), vo.getAccessDeviceId()));
			if (r != null) {
				fillVo(vo, r);
				filled++;
			} else if (!FIXED_TABLE_PROJECTS.contains(String.valueOf(vo.getEngineeringId()))) {
				logger.warn("[环境监测] 项目 {} 未写入硬编码 UNION ALL 清单，该设备无数据（请同步 EnvMonitorDao.xml）",
						vo.getEngineeringId());
			}
		}
		logger.info("[环境监测] UNION ALL 模式：SQL 返回 {} 条，回填 {} 条，SQL 耗时 {}ms",
				records.size(), filled, cost);
	}

	/**
	 * dynamic 模式：逐台设备查最新记录（每台设备对应一张物理分表，无法合并成一条普通 SQL）。
	 * <p>
	 * 单次查询走主键倒序，11 台设备合计约 100ms。单台失败（分表未建、数据异常）
	 * 不影响整体，保留该设备行并标记 hasData=false。
	 */
	private void fillByDeviceLoop(List<EnvMonitorVo> list) {
		for (EnvMonitorVo vo : list) {
			try {
				EnvRecord record = envMonitorDao.findLatestRecord(
						RECORD_TABLE_PREFIX + vo.getEngineeringId(),
						vo.getAccessDeviceId(), candidateLimit());
				if (record != null) {
					fillVo(vo, record);
				}
			} catch (Exception ex) {
				logger.warn("[环境监测] 设备 {}（项目 {}）最新数据查询失败：{}",
						vo.getAccessDeviceId(), vo.getEngineeringId(), ex.getMessage());
			}
		}
	}

	/**
	 * 把明细记录写到结果行上
	 */
	private void fillVo(EnvMonitorVo vo, EnvRecord r) {
		vo.setNoise(r.getNoise());
		vo.setTemp(r.getTemp());
		vo.setHumid(r.getHumid());
		vo.setTsp(r.getTsp());
		vo.setRecordTime(r.getRecordTime());
		vo.setHasData(true);
		applyLevel(vo);
	}

	/**
	 * 按《噪声和扬尘国家标准》给这一行定级。
	 * <p>
	 * 噪声与扬尘各按自身标准分档定级，综合标记取两者中优先级最高的一个
	 * （1 红 &gt; 2 橙 &gt; 3 黄 &gt; 4 蓝，见 {@link EnvLevel#worst}）。
	 * 任一指标无有效数值时不参与综合判定；两者都无值时标记为 null。
	 *
	 * @param vo 已填好噪声/扬尘/采集时间的结果行
	 */
	private void applyLevel(EnvMonitorVo vo) {

		// 昼夜时段决定噪声用哪套阈值；采集时间无法解析时按昼间处理
		Integer hour = EnvLevelEvaluator.parseHour(vo.getRecordTime());
		boolean night = EnvLevelEvaluator.isNight(hour);

		EnvLevel noiseLevel = EnvLevelEvaluator.evalNoise(EnvLevelEvaluator.parse(vo.getNoise()), night);
		EnvLevel tspLevel = EnvLevelEvaluator.evalTsp(EnvLevelEvaluator.parse(vo.getTsp()));
		EnvLevel overall = EnvLevel.worst(noiseLevel, tspLevel);

		if (overall != null) {
			vo.setColorLevel(overall.getRank());
		}
	}

	/**
	 * 联合键：项目ID + 设备ID
	 */
	private static String deviceKey(Long engineeringId, Long accessDeviceId) {
		return engineeringId + "_" + accessDeviceId;
	}

	/**
	 * dynamic 模式的候选条数兜底：配置写成 0 或负数会让 SQL 变成 FETCH FIRST 0 ROWS（恒无结果），
	 * 这里强制收敛到 [1, 10000]，避免配置失误造成"全设备无数据"。
	 */
	private int candidateLimit() {
		return Math.min(Math.max(candidateLimit, 1), 10000);
	}

	/**
	 * 解析 SQL 模式：接口参数优先，非法值回落到配置默认值
	 */
	private String resolveSqlMode(String sqlMode) {
		if (MODE_DYNAMIC.equalsIgnoreCase(sqlMode) || MODE_HARDCODED.equalsIgnoreCase(sqlMode)) {
			return sqlMode;
		}
		if (sqlMode != null && !sqlMode.trim().isEmpty()) {
			logger.warn("[环境监测] 忽略非法 sqlMode：{}，使用默认 {}", sqlMode, defaultSqlMode);
		}
		return defaultSqlMode;
	}

	/**
	 * 解析并校验项目ID串（逗号分隔），非法值直接丢弃
	 *
	 * @param rawIds 逗号分隔的项目ID，可为空
	 * @return 去重后的项目ID列表；未传则使用配置中的默认项目
	 */
	public List<String> resolveProjectIds(String rawIds) {
		String source = (rawIds == null || rawIds.trim().isEmpty()) ? defaultProjectIds : rawIds;
		List<String> list = new ArrayList<>();
		if (source == null) {
			return list;
		}
		for (String id : source.split(",")) {
			String v = id.trim();
			if (v.isEmpty()) {
				continue;
			}
			if (!NUMERIC.matcher(v).matches()) {
				logger.warn("[环境监测] 忽略非法项目ID：{}", v);
				continue;
			}
			list.add(v);
		}
		return list;
	}

	/**
	 * 去重 + 合法性校验
	 */
	private List<String> normalize(List<String> projectIds) {
		Set<String> set = new LinkedHashSet<>();
		if (projectIds != null) {
			for (String id : projectIds) {
				if (id == null) {
					continue;
				}
				String v = id.trim();
				if (NUMERIC.matcher(v).matches()) {
					set.add(v);
				} else {
					logger.warn("[环境监测] 忽略非法项目ID：{}", v);
				}
			}
		}
		return new ArrayList<>(set);
	}

	/**
	 * 解析「排除设备ID」配置；未配置或全为非法值时返回空集合（即不排除任何设备）
	 */
	private Set<Long> resolveExcludeDevices() {
		Set<Long> set = new LinkedHashSet<>();
		if (excludeDeviceIds == null || excludeDeviceIds.trim().isEmpty()) {
			return set;
		}
		for (String s : excludeDeviceIds.split(",")) {
			String v = s.trim();
			if (v.isEmpty()) {
				continue;
			}
			try {
				set.add(Long.valueOf(v));
			} catch (NumberFormatException e) {
				logger.warn("[环境监测] 忽略非法排除设备ID：{}", v);
			}
		}
		return set;
	}

}
