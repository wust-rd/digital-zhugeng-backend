package com.jeesite.modules.gongdi.util;

import com.jeesite.modules.gongdi.enums.EnvLevel;

import java.math.BigDecimal;

/**
 * 噪声 / 扬尘监测值的颜色等级判定：实测值直接与标准阈值比较
 * <p>
 * 阈值（见《噪声和扬尘国家标准.png》）：
 * <pre>
 *   噪声昼间（06:00-22:00，限值 70dB） 红 ≥ 70   橙 ≥ 52.5    黄 ≥ 35      蓝 < 35
 *   噪声夜间（22:00-06:00，限值 55dB） 红 ≥ 55   橙 ≥ 41.25   黄 ≥ 27.5    蓝 < 27.5
 *   扬尘 TSP（限值 600 μg/m³）        红 ≥ 600  橙 ≥ 450     黄 ≥ 300     蓝 < 300
 * </pre>
 *
 * @author wuchangyue
 * @version 2026-10-29
 */
public final class EnvLevelEvaluator {

	/**
	 * 昼间起始时刻（含）：06:00
	 */
	public static final int DAY_START_HOUR = 6;

	/**
	 * 夜间起始时刻（含）：22:00
	 */
	public static final int NIGHT_START_HOUR = 22;

	// ---- 噪声昼间，限值 70dB ----
	private static final BigDecimal NOISE_DAY_RED = new BigDecimal("70");
	private static final BigDecimal NOISE_DAY_ORANGE = new BigDecimal("52.5");
	private static final BigDecimal NOISE_DAY_YELLOW = new BigDecimal("35");

	// ---- 噪声夜间，限值 55dB ----
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
	 * 判断是否夜间（22:00-06:00）；无法解析时按昼间处理
	 */
	public static boolean isNight(Integer hour) {
		if (hour == null) {
			return false;
		}
		return hour >= NIGHT_START_HOUR || hour < DAY_START_HOUR;
	}

	/**
	 * 解析数值型监测值
	 *
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
	 * 判定噪声等级
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
	 * 判定扬尘等级
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
