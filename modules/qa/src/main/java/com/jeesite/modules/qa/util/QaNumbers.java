package com.jeesite.modules.qa.util;

import java.util.List;

/**
 * 质安统计数值计算
 * <p>
 * 工作台的平均分、覆盖率、四维得分率统一走本类，保证同一口径。
 *
 * @author wuchangyue
 * @version 2026-11-03
 */
public final class QaNumbers {

	/**
	 * 保留小数位数：一位
	 */
	private static final double SCALE = 10.0;

	private QaNumbers() {
	}

	/**
	 * 空值转零
	 *
	 * @param value 数值
	 * @return 空值返回 0，否则原值
	 */
	public static double zeroIfNull(Double value) {

		return value == null ? 0.00 : value;
	}

	/**
	 * 平均值
	 *
	 * @param values 数值列表
	 * @return 平均值；列表为空返回 0
	 */
	public static double average(List<Double> values) {

		if (values == null || values.isEmpty()) {
			return 0.00;
		}
		double sum = 0.00;
		for (Double value : values) {
			sum += zeroIfNull(value);
		}
		return sum / values.size();
	}

	/**
	 * 保留一位小数
	 *
	 * @param value 数值
	 * @return 四舍五入到一位小数
	 */
	public static double round1(double value) {

		return Math.round(value * SCALE) / SCALE;
	}

}
