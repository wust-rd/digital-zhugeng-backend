package com.jeesite.modules.qa.util;

import com.jeesite.common.entity.DataEntity;
import com.jeesite.common.lang.StringUtils;
import com.jeesite.modules.sys.entity.User;

/**
 * 质安实体保存前的公共处理
 * <p>
 * 框架的 preInsert 只填主键与操作人，不补租户编码，故在此统一兜底。
 *
 * @author wuchangyue
 * @version 2026-11-03
 */
public final class QaEntities {

	/**
	 * 单库部署下的默认租户编码
	 */
	private static final String DEFAULT_CORP_CODE = "0";

	/**
	 * 取不到当前用户时的操作人
	 */
	private static final String SYSTEM_USER_NAME = "系统";

	private QaEntities() {
	}

	/**
	 * 新增前统一处理：填主键与操作人，并补租户编码
	 *
	 * @param entity 实体
	 */
	public static <T extends DataEntity<?>> void prepareInsert(T entity) {

		entity.preInsert();
		if (StringUtils.isBlank(entity.getCorpCode())) {
			entity.setCorpCode(DEFAULT_CORP_CODE);
		}
	}

	/**
	 * 当前用户姓名
	 *
	 * @param entity 实体
	 * @return 姓名；取不到时返回系统
	 */
	public static String currentUserName(DataEntity<?> entity) {

		User user = entity.currentUser();
		String name = user == null ? null : user.getUserName();
		return StringUtils.isNotBlank(name) ? name : SYSTEM_USER_NAME;
	}

}
