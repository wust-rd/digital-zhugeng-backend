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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 工地噪声扬尘查询服务：返回各项目下每台环境监测设备（DEVICE_TYPE = 1）的最新一条
 * 监测记录（噪声 / 温度 / 湿度 / 扬尘），并给出综合颜色标记。一台设备一行。
 * <p>
 * 数据源：工地库 ZHGD_2（只读）
 *
 * @author wuchangyue
 * @version 2026-10-29
 */
@Service
public class EnvMonitorService {

	private static final Logger logger = LoggerFactory.getLogger(EnvMonitorService.class);

	/**
	 * 明细分表前缀，完整表名 = 前缀 + 项目ID
	 */
	private static final String RECORD_TABLE_PREFIX = "Z_ENVIRONMENTAL_RECORD_";

	/**
	 * 项目ID（同时作为表名后缀）白名单：纯数字，防动态表名注入
	 */
	private static final Pattern NUMERIC = Pattern.compile("^\\d{1,19}$");

	/**
	 * 查询的项目ID，逗号分隔
	 */
	@Value("${gongdi-env.project-ids:635320,644423,632474,636613,650154,636685,635686,632472,636927,635418}")
	private String projectIds;

	/**
	 * 已停报、不再查询的设备ID，逗号分隔；置空即恢复全部查询
	 */
	@Value("${gongdi-env.exclude-device-ids:}")
	private String excludeDeviceIds;

	private final EnvMonitorDao envMonitorDao;

	public EnvMonitorService(EnvMonitorDao envMonitorDao) {
		this.envMonitorDao = envMonitorDao;
	}

	/**
	 * 查询各项目环境监测设备的最新监测值
	 *
	 * @return 每台设备一条；无设备的项目不产生数据
	 */
	public List<EnvMonitorVo> queryLatest() {

		List<String> ids = resolveProjectIds();
		if (ids.isEmpty()) {
			logger.warn("[环境监测] 未配置任何有效项目ID，返回空结果");
			return new ArrayList<>();
		}

		// 1. 一次 JOIN 取出「项目 + 设备」清单，作为结果骨架
		List<EnvMonitorVo> list = envMonitorDao.findProjectDevices(ids);

		// 2. 剔除停报设备。必须在明细查询之前，被排除的设备一条 SQL 都不会发
		Set<Long> excluded = resolveExcludeDevices();
		if (!excluded.isEmpty() && !list.isEmpty()) {
			int before = list.size();
			list.removeIf(vo -> vo.getAccessDeviceId() != null && excluded.contains(vo.getAccessDeviceId()));
			if (before != list.size()) {
				logger.info("[环境监测] 按配置排除停报设备，设备清单 {} → {} 条", before, list.size());
			}
		}
		if (list.isEmpty()) {
			logger.warn("[环境监测] 项目 {} 下无有效环境监测设备或全部被排除", ids);
			return list;
		}

		// 3. 逐台设备补最新监测值（明细按项目分表，无法合并成一条 SQL）
		for (EnvMonitorVo vo : list) {
			fillLatest(vo);
		}
		return list;
	}

	/**
	 * 查某台设备的最新一条记录并回填到结果行；无数据或查询失败时保留该行，hasData 为 false
	 */
	private void fillLatest(EnvMonitorVo vo) {
		try {
			EnvRecord record = envMonitorDao.findLatestRecord(
				RECORD_TABLE_PREFIX + vo.getEngineeringId(), vo.getAccessDeviceId());
			if (record == null) {
				return;
			}
			vo.setNoise(record.getNoise());
			vo.setTemp(record.getTemp());
			vo.setHumid(record.getHumid());
			vo.setTsp(record.getTsp());
			vo.setRecordTime(record.getRecordTime());
			vo.setHasData(true);
			applyLevel(vo);
		} catch (Exception ex) {
			logger.warn("[环境监测] 设备 {}（项目 {}）最新数据查询失败：{}",
				vo.getAccessDeviceId(), vo.getEngineeringId(), ex.getMessage());
		}
	}

	/**
	 * 按《噪声和扬尘国家标准》定级：噪声、扬尘各按自身标准分档，
	 * 综合标记取两者中优先级最高的一个（1红 &gt; 2橙 &gt; 3黄 &gt; 4蓝）。
	 * 任一指标无有效数值时不参与判定；两者都无值时为 null。
	 */
	private void applyLevel(EnvMonitorVo vo) {
		// 昼夜时段决定噪声用哪套阈值；采集时间无法解析时按昼间处理
		boolean night = EnvLevelEvaluator.isNight(EnvLevelEvaluator.parseHour(vo.getRecordTime()));

		EnvLevel noiseLevel = EnvLevelEvaluator.evalNoise(EnvLevelEvaluator.parse(vo.getNoise()), night);
		EnvLevel tspLevel = EnvLevelEvaluator.evalTsp(EnvLevelEvaluator.parse(vo.getTsp()));
		EnvLevel overall = EnvLevel.worst(noiseLevel, tspLevel);

		if (overall != null) {
			vo.setColorLevel(overall.getRank());
		}
	}

	/**
	 * 解析配置中的项目ID串，去重并丢弃非法值
	 */
	private List<String> resolveProjectIds() {
		Set<String> set = new LinkedHashSet<>();
		if (projectIds == null) {
			return new ArrayList<>();
		}
		for (String id : projectIds.split(",")) {
			String v = id.trim();
			if (v.isEmpty()) {
				continue;
			}
			if (NUMERIC.matcher(v).matches()) {
				set.add(v);
			} else {
				logger.warn("[环境监测] 忽略非法项目ID：{}", v);
			}
		}
		return new ArrayList<>(set);
	}

	/**
	 * 解析「排除设备ID」配置；未配置或全为非法值时返回空集合（不排除任何设备）
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
