package com.jeesite.modules.muni.util;

import com.jeesite.common.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 联系人字段拆分
 * <p>
 * 清单的「联系人联系方式」一列格式五花八门：姓名与手机号可能用空格、换行、斜杠、逗号、冒号、
 * 分号分隔，也可能一个单元格里塞了多个联系人；手机号中间还可能带空格。
 * 这里把该列拆成「姓名」和「联系电话」两个字段，多联系人时按出现顺序用「、」拼接，顺序一一对应。
 *
 * @author wuchangyue
 * @version 2026-11-04
 */
public final class ContactSplitter {

	/**
	 * 手机号：1 开头、第二位 3~9，中间允许一个空格
	 */
	private static final Pattern PHONE = Pattern.compile("1[3-9]\\d ?\\d{4} ?\\d{4}");

	/**
	 * 分隔符
	 */
	private static final Pattern SEPARATOR = Pattern.compile("[\\n\\r；;,，/|\\\\：:]");

	/**
	 * 括号内容
	 */
	private static final Pattern BRACKET = Pattern.compile("[（(][^)）]*[)）]");

	/**
	 * 单位 / 岗位说明前缀
	 */
	private static final Pattern UNIT_PREFIX = Pattern.compile(
		"建设单位联系人|联系方式|联系人|安全总监|一工区|二工区|三工区|总监|经理|中信|中交二航|武汉市政|中建三局|中铁|誉城|市政");

	private static final String NAME_JOINER = "、";

	private ContactSplitter() {
	}

	/**
	 * 拆分联系人
	 *
	 * @param contact 原始「联系人联系方式」值
	 * @return [姓名, 电话]，两项都可能为空
	 */
	public static String[] split(String contact) {

		if (StringUtils.isBlank(contact)) {
			return new String[]{null, null};
		}

		List<PhoneSpan> spans = new ArrayList<>();
		Matcher matcher = PHONE.matcher(contact);
		while (matcher.find()) {
			spans.add(new PhoneSpan(matcher.start(), matcher.end(), matcher.group().replace(" ", "")));
		}
		if (spans.isEmpty()) {
			return new String[]{contact.trim(), null};
		}

		List<String> names = new ArrayList<>();
		List<String> phones = new ArrayList<>();
		for (int i = 0; i < spans.size(); i++) {
			PhoneSpan span = spans.get(i);
			if (phones.contains(span.phone)) {
				continue;
			}
			int prevEnd = i == 0 ? 0 : spans.get(i - 1).end;
			String name = cleanName(contact.substring(prevEnd, span.start));
			names.add(name);
			phones.add(span.phone);
		}

		String nameText = joinNonBlank(names);
		String phoneText = joinNonBlank(phones);
		return new String[]{nameText.isEmpty() ? null : nameText,
			phoneText.isEmpty() ? null : phoneText};
	}

	/**
	 * 清洗姓名片段：去分隔符、冒号、括号内容与单位前缀
	 */
	private static String cleanName(String segment) {

		String value = SEPARATOR.matcher(segment).replaceAll("");
		value = BRACKET.matcher(value).replaceAll("");
		value = UNIT_PREFIX.matcher(value).replaceAll("");
		value = value.replaceAll("\\s+", "");
		return value.trim();
	}

	/**
	 * 拼接非空片段
	 */
	private static String joinNonBlank(List<String> values) {

		List<String> list = new ArrayList<>();
		for (String value : values) {
			if (StringUtils.isNotBlank(value)) {
				list.add(value.trim());
			}
		}
		return String.join(NAME_JOINER, list);
	}

	/**
	 * 电话出现的位置与号码
	 */
	private static final class PhoneSpan {

		private final int start;
		private final int end;
		private final String phone;

		private PhoneSpan(int start, int end, String phone) {
			this.start = start;
			this.end = end;
			this.phone = phone;
		}
	}

}
