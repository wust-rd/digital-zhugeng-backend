/**
 * Copyright (c) 2013-Now https://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 */
package com.jeesite.modules.gongdi.util;

import com.jeesite.modules.gongdi.enums.EnvLevel;

import java.math.BigDecimal;

/**
 * 噪声 / 扬尘监测值的颜色等级判定：实测值直接与标准阈值比较
 * <p>
 * 判定标准（见《噪声和扬尘国家标准.png》）：
 * <ol>
 *   <li><b>噪声</b>——GB 12523-2025《建筑施工场界噪声排放标准》（2026-01-01 实施），
 *       施工场界排放限值不分功能区，统一为：昼间（06:00-22:00）70dB、夜间（22:00-06:00）55dB。</li>
 *   <li><b>扬尘 TSP</b>——国家、湖北省、武汉市均无标准，参考江苏 DB32/4437-2022、
 *       辽宁 DB21/2642-2016《施工及堆料场地扬尘排放标准》，暂按不大于 600 μg/m³。</li>
 * </ol>
 * 各档阈值直接取自标准表，实测值与之逐一比较：
 * <pre>
 *   噪声昼间  红 ≥ 70      橙 ≥ 52.5    黄 ≥ 35      蓝 &lt; 35
 *   噪声夜间  红 ≥ 55      橙 ≥ 41.25   黄 ≥ 27.5    蓝 &lt; 27.5
 *   扬尘 TSP  红 ≥ 600     橙 ≥ 450     黄 ≥ 300     蓝 &lt; 300
 * </pre>
 * <p>
 * 【两点说明】
 * <ul>
 *   <li>原图「备注」列把 70dB 那组标成「夜间」、55dB 那组标成「昼间」，与顶部
 *       「昼间 70dB / 夜间 55dB」的说明矛盾，属制图笔误；本类以国家标准的时段限值为准。</li>
 *   <li>原图 55dB 组的蓝色写作「&lt; 35dB」，与黄色下界 27.5 重叠，按标准应为 &lt; 27.5dB，
 *       本类按 27.5 判定。扬尘组 450 / 300 两处写作「大于」，为不留空档按「大于等于」处理
 *       （与噪声组口径一致）。</li>
 * </ul>
 * 日后若要调整分档，只改本类顶部这几组常量即可，无需改判定逻辑。
 *
 * @author wuchangyue
 * @version 2026-10-29
 */
public final class EnvLevelEvaluator {

	/** 昼间起始时刻（含）：06:00 */
	public static final int DAY_START_HOUR = 6;

	/** 夜间起始时刻（含）：22:00 */
	public static final int NIGHT_START_HOUR = 22;

	// ---- 噪声昼间（06:00-22:00），限值 70dB ----
	private static final BigDecimal NOISE_DAY_RED = new BigDecimal("70");
	private static final BigDecimal NOISE_DAY_ORANGE = new BigDecimal("52.5");
	private static final BigDecimal NOISE_DAY_YELLOW = new BigDecimal("35");

	// ---- 噪声夜间（22:00-06:00），限值 55dB ----
	private static final BigDecimal NOISE_NIGHT_RED = new BigDecimal("55");
	private static final BigDecimal NOISE_NIGHT_ORANGE = new BigDecimal("41.25");
	private static final BigDecimal NOISE_NIGHT_YELLOW = new BigDecimal("27.5");

	// ---- 扬尘 TSP，限值 600 μg/m³ ----
	private static final BigDecimal TSP_RED = new BigDecimal("600");
	private static final BigDecimal TSP_ORANGE = new BigDecimal("450");
	private static final BigDecimal TSP_YELLOW = new BigDecimal("300");

	private EnvLevelEvaluator() {
	}

	/**
	 * 从采集时间 yyyy-MM-dd HH:mm:ss 中取小时（0-23）
	 *
	 * @param recordTime 采集时间，允许为 null 或格式异常
	 * @return 小时；无法解析时返回 null
	 */
	public static Integer parseHour(String recordTime) {
		if (recordTime == null || recordTime.length() < 13) {
			return null;
		}
		try {
			int hour = Integer.parseInt(recordTime.substring(11, 13));
			return (hour >= 0 && hour <= 23) ? hour : null;
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * 判断是否夜间（22:00-06:00）
	 *
	 * @param hour 小时，可为 null
	 * @return 无法解析时按昼间处理（返回 false）
	 */
	public static boolean isNight(Integer hour) {
		if (hour == null) {
			return false;
		}
		return hour >= NIGHT_START_HOUR || hour < DAY_START_HOUR;
	}

	/**
	 * 该时段适用的噪声限值
	 *
	 * @param night 是否夜间
	 * @return 昼间 70dB / 夜间 55dB
	 */
	public static BigDecimal noiseLimit(boolean night) {
		return night ? NOISE_NIGHT_RED : NOISE_DAY_RED;
	}

	/**
	 * 扬尘限值
	 *
	 * @return 600 μg/m³
	 */
	public static BigDecimal tspLimit() {
		return TSP_RED;
	}

	/**
	 * 解析数值型监测值
	 *
	 * @param value 原始字符串，如 "52.3"
	 * @return 数值；为 null、空串或非数字时返回 null
	 */
	public static BigDecimal parse(String value) {
		if (value == null) {
			return null;
		}
		String v = value.trim();
		if (v.isEmpty()) {
			return null;
		}
		try {
			return new BigDecimal(v);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * 判定噪声等级：实测值直接与昼间/夜间的分档阈值比较
	 *
	 * @param noise 噪声值（dB），null 表示无数据
	 * @param night 是否按夜间限值判定
	 * @return 颜色等级；noise 为 null 时返回 null
	 */
	public static EnvLevel evalNoise(BigDecimal noise, boolean night) {
		if (noise == null) {
			return null;
		}
		if (night) {
			return byThreshold(noise, NOISE_NIGHT_RED, NOISE_NIGHT_ORANGE, NOISE_NIGHT_YELLOW);
		}
		return byThreshold(noise, NOISE_DAY_RED, NOISE_DAY_ORANGE, NOISE_DAY_YELLOW);
	}

	/**
	 * 判定扬尘等级：实测值直接与 TSP 分档阈值比较
	 *
	 * @param tsp TSP 值（μg/m³），null 表示无数据
	 * @return 颜色等级；tsp 为 null 时返回 null
	 */
	public static EnvLevel evalTsp(BigDecimal tsp) {
		if (tsp == null) {
			return null;
		}
		return byThreshold(tsp, TSP_RED, TSP_ORANGE, TSP_YELLOW);
	}

	/**
	 * 依次与红、橙、黄的阈值比较，命中即返回，都不满足则为蓝
	 *
	 * @param value  实测值
	 * @param red    红色阈值（≥ 该值为红）
	 * @param orange 橙色阈值
	 * @param yellow 黄色阈值
	 * @return 颜色等级
	 */
	private static EnvLevel byThreshold(BigDecimal value, BigDecimal red, BigDecimal orange, BigDecimal yellow) {
		if (value.compareTo(red) >= 0) {
			return EnvLevel.RED;
		}
		if (value.compareTo(orange) >= 0) {
			return EnvLevel.ORANGE;
		}
		if (value.compareTo(yellow) >= 0) {
			return EnvLevel.YELLOW;
		}
		return EnvLevel.BLUE;
	}

}
