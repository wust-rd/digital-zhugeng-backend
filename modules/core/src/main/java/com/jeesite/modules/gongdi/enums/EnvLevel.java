/**
 * Copyright (c) 2013-Now https://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 */
package com.jeesite.modules.gongdi.enums;

/**
 * 噪声 / 扬尘监测值的颜色等级
 * <p>
 * 四档为闭区间衔接的阶梯，{@link #getRank()} 数字越小优先级越高（红 &gt; 橙 &gt; 黄 &gt; 蓝），
 * 综合判定时取优先级最高（rank 最小）的一个。
 *
 * @author wuchangyue
 * @version 2026-10-29
 */
public enum EnvLevel {

	/** 红色：达到或超过限值，优先级最高 */
	RED("红色", 1),

	/** 橙色：达到限值的 75% 及以上、未到 100% */
	ORANGE("橙色", 2),

	/** 黄色：达到限值的 50% 及以上、未到 75% */
	YELLOW("黄色", 3),

	/** 蓝色：低于限值的 50%，优先级最低 */
	BLUE("蓝色", 4);

	/** 中文名称，用于直接展示 */
	private final String label;

	/** 优先级，1 最高 */
	private final int rank;

	EnvLevel(String label, int rank) {
		this.label = label;
		this.rank = rank;
	}

	public String getLabel() {
		return label;
	}

	public int getRank() {
		return rank;
	}

	/** 枚举名即接口返回的颜色代码，如 RED / ORANGE / YELLOW / BLUE */
	public String getCode() {
		return name();
	}

	/**
	 * 取两个等级中优先级更高的一个（rank 更小者胜出）；任一为 null 时返回另一个
	 *
	 * @param a 等级 A，可为 null（表示该项无数据、不参与判定）
	 * @param b 等级 B，可为 null
	 * @return 优先级更高的等级；两者都为 null 时返回 null
	 */
	public static EnvLevel worst(EnvLevel a, EnvLevel b) {
		if (a == null) {
			return b;
		}
		if (b == null) {
			return a;
		}
		return a.rank <= b.rank ? a : b;
	}

}
