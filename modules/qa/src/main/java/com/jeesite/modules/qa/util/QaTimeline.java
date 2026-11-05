package com.jeesite.modules.qa.util;

import com.jeesite.modules.qa.entity.QaInspectRecord;

import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Date;

/**
 * 检查月份与记录时间线
 * <p>
 * 月份一律用 yyyy-MM 字符串比较，可与库中 MONTH_KEY、INSPECT_MONTH 直接对齐。
 *
 * @author wuchangyue
 * @version 2026-11-03
 */
public final class QaTimeline {

	private QaTimeline() {
	}

	/**
	 * 日期所属月份
	 *
	 * @param date 日期
	 * @return 月份 yyyy-MM
	 */
	public static String monthOf(Date date) {

		return YearMonth.from(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()).toString();
	}

	/**
	 * 月份的下一个月
	 *
	 * @param month 月份 yyyy-MM
	 * @return 下一个月 yyyy-MM
	 */
	public static String nextMonth(String month) {

		return YearMonth.parse(month).plusMonths(1).toString();
	}

	/**
	 * 比较两条记录的检查时间先后
	 * <p>
	 * 先比检查月份，月份相同再比检查日期；日期缺失时视为相同。
	 *
	 * @param a 记录 A
	 * @param b 记录 B
	 * @return A 晚于 B 返回正数，早于返回负数，相同返回 0
	 */
	public static int compare(QaInspectRecord a, QaInspectRecord b) {

		String monthA = a.getInspectMonth() == null ? "" : a.getInspectMonth();
		String monthB = b.getInspectMonth() == null ? "" : b.getInspectMonth();
		int result = monthA.compareTo(monthB);
		if (result != 0) {
			return result;
		}
		if (a.getInspectDate() == null || b.getInspectDate() == null) {
			return 0;
		}
		return a.getInspectDate().compareTo(b.getInspectDate());
	}

}
