package com.jeesite.modules.qa.constant;

import com.jeesite.common.lang.StringUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 质安检查评分标准
 * <p>
 * 前端页面固定使用同一份口径，库内只存编码与得分。
 * 标准换版时改本类与前端常量，不改动历史记录。
 *
 * @author wuchangyue
 * @version 2026-11-03
 */
public final class QaStandard {

	/**
	 * 智慧监管分数折算系数
	 */
	public static final double SMART_RATIO = 0.25;

	/**
	 * 检查记录状态：草稿
	 */
	public static final String RECORD_STATUS_DRAFT = "0";

	/**
	 * 检查记录状态：已提交
	 */
	public static final String RECORD_STATUS_SUBMIT = "1";

	/**
	 * 检查记录状态：已归档
	 */
	public static final String RECORD_STATUS_ARCHIVE = "2";

	/**
	 * 检查记录状态：已作废
	 */
	public static final String RECORD_STATUS_VOID = "3";

	/**
	 * 检查状态：正常
	 */
	public static final String CHECK_STATUS_NORMAL = "1";

	/**
	 * 检查状态：待检查
	 */
	public static final String CHECK_STATUS_PENDING = "2";

	/**
	 * 智慧监管分数来源：手工填写
	 */
	public static final String SMART_SOURCE_MANUAL = "1";

	/**
	 * 智慧监管分数来源：接口同步
	 */
	public static final String SMART_SOURCE_SYNC = "2";

	/**
	 * 预警类型：分数下降
	 */
	public static final String ALERT_SCORE_DOWN = "1";

	/**
	 * 预警类型：等级下调
	 */
	public static final String ALERT_LEVEL_DOWN = "2";

	/**
	 * 预警类型：连续两次下降
	 */
	public static final String ALERT_TWICE_DOWN = "3";

	/**
	 * 作废记录让出月位时使用的分隔符，取值 = 月份 + 分隔符 + 序号
	 */
	public static final String MONTH_KEY_VOID_SEPARATOR = "#";

	/**
	 * 工程类别静态分
	 */
	private static final Map<String, Double> CATEGORY_SCORE = new LinkedHashMap<>();

	/**
	 * 危大档位基础分
	 */
	private static final Map<String, Double> MAJOR_BASE_SCORE = new LinkedHashMap<>();

	/**
	 * 危大扣分项分值
	 */
	private static final Map<String, Double> DEDUCT_SCORE = new LinkedHashMap<>();

	/**
	 * 基坑档位分值
	 */
	private static final Map<String, Double> PIT_SCORE = new LinkedHashMap<>();

	/**
	 * 风险等级名称
	 */
	private static final Map<String, String> LEVEL_NAME = new LinkedHashMap<>();

	/**
	 * 风险等级处置话术
	 */
	private static final Map<String, String> DISPOSAL_TEXT = new LinkedHashMap<>();

	/**
	 * 等级变化：持平
	 */
	public static final String LEVEL_CHANGE_SAME = "0";

	/**
	 * 等级变化：上调
	 */
	public static final String LEVEL_CHANGE_UP = "1";

	/**
	 * 等级变化：下调
	 */
	public static final String LEVEL_CHANGE_DOWN = "2";

	static {
		CATEGORY_SCORE.put("tunnel", 5.00);
		CATEGORY_SCORE.put("bridge", 10.00);
		CATEGORY_SCORE.put("road", 15.00);
		CATEGORY_SCORE.put("minor", 20.00);

		MAJOR_BASE_SCORE.put("super", 25.00);
		MAJOR_BASE_SCORE.put("major", 30.00);
		MAJOR_BASE_SCORE.put("none", 35.00);

		DEDUCT_SCORE.put("plan", 15.00);
		DEDUCT_SCORE.put("site", 5.00);
		DEDUCT_SCORE.put("accept", 10.00);

		PIT_SCORE.put("P0", 0.00);
		PIT_SCORE.put("P1", 5.00);
		PIT_SCORE.put("P2", 10.00);
		PIT_SCORE.put("P3", 15.00);
		PIT_SCORE.put("P4", 20.00);

		LEVEL_NAME.put("1", "一级（重大风险）");
		LEVEL_NAME.put("2", "二级（较大风险）");
		LEVEL_NAME.put("3", "三级（一般风险）");
		LEVEL_NAME.put("4", "四级（低风险）");

		DISPOSAL_TEXT.put("1", "存在重大风险，责令暂停相关工序作业，限期整改并挂牌督办，整改完成经复核后方可复工。");
		DISPOSAL_TEXT.put("2", "存在较大风险，责令限期整改，加密监督检查频次，整改情况须按期书面反馈。");
		DISPOSAL_TEXT.put("3", "存在一般风险，责令限期整改一般隐患，加强日常巡查与自查自纠。");
		DISPOSAL_TEXT.put("4", "风险较低，保持现有管控措施，继续按月度开展常态化检查。");
	}

	private QaStandard() {
	}

	/**
	 * 工程类别静态分
	 *
	 * @param code 类别编码
	 * @return 分值；编码不在闭集内返回 null
	 */
	public static Double categoryScore(String code) {

		return CATEGORY_SCORE.get(code);
	}

	/**
	 * 危大档位基础分
	 *
	 * @param code 档位编码
	 * @return 分值；编码不在闭集内返回 null
	 */
	public static Double majorBaseScore(String code) {

		return MAJOR_BASE_SCORE.get(code);
	}

	/**
	 * 危大扣分项分值
	 *
	 * @param code 扣分项编码
	 * @return 分值；编码不在闭集内返回 null
	 */
	public static Double deductScore(String code) {

		return DEDUCT_SCORE.get(code);
	}

	/**
	 * 基坑档位分值
	 *
	 * @param code 档位编码
	 * @return 分值；编码不在闭集内返回 null
	 */
	public static Double pitScore(String code) {

		return PIT_SCORE.get(code);
	}

	/**
	 * 危大扣分项编码集合
	 *
	 * @return 按标准顺序排列的编码集合
	 */
	public static Map<String, Double> deductItems() {

		return Collections.unmodifiableMap(DEDUCT_SCORE);
	}

	/**
	 * 基坑档位编码集合
	 *
	 * @return 按标准顺序排列的编码集合
	 */
	public static Map<String, Double> pitItems() {

		return Collections.unmodifiableMap(PIT_SCORE);
	}

	/**
	 * 按总分定级
	 *
	 * @param total 总分
	 * @return 等级 1~4
	 */
	public static String riskLevel(double total) {

		if (total <= 40) {
			return "1";
		}
		if (total <= 60) {
			return "2";
		}
		if (total <= 80) {
			return "3";
		}
		return "4";
	}

	/**
	 * 风险等级名称
	 *
	 * @param level 等级
	 * @return 名称
	 */
	public static String levelName(String level) {

		return LEVEL_NAME.get(level);
	}

	/**
	 * 是否为已提交或已归档状态
	 * <p>
	 * 台账回写、档案格子、环比对比只认这两个状态，草稿与作废记录不算检查过。
	 *
	 * @param status 记录状态
	 * @return 已提交或已归档返回 true
	 */
	public static boolean isSubmitted(String status) {

		return RECORD_STATUS_SUBMIT.equals(status) || RECORD_STATUS_ARCHIVE.equals(status);
	}

	/**
	 * 等级变化
	 *
	 * @param prevLevel 上次等级
	 * @param currLevel 本次等级
	 * @return 1 上调 / 2 下调 / 0 持平
	 */
	public static String levelChange(String prevLevel, String currLevel) {

		if (StringUtils.isBlank(prevLevel) || StringUtils.isBlank(currLevel)
				|| prevLevel.equals(currLevel)) {
			return LEVEL_CHANGE_SAME;
		}
		return currLevel.compareTo(prevLevel) > 0 ? LEVEL_CHANGE_UP : LEVEL_CHANGE_DOWN;
	}

	/**
	 * 生成检查结论
	 *
	 * @param level 等级
	 * @return 该等级对应的处置话术
	 */
	public static String conclusion(String level) {

		return DISPOSAL_TEXT.get(level);
	}

}
