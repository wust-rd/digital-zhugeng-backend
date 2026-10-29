/**
 * Copyright (c) 2013-Now https://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 */
package com.jeesite.modules.gongdi.web;

import com.jeesite.common.config.Global;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.gongdi.entity.EnvMonitorVo;
import com.jeesite.modules.gongdi.service.EnvMonitorService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 工地环境监测（噪声扬尘）开放接口
 * <p>
 * 【免登录】路径不在 adminPath（/a）之下，命中 shiro 过滤器链 {@code /** = anon}，
 * 无需登录即可直接调用：
 * <pre>
 *   GET http://127.0.0.1:8980/js/api/env/latest
 *   GET http://127.0.0.1:8980/js/api/env/latest?projectIds=635320,644423
 * </pre>
 * 返回结构沿用框架约定：{@code {"result":"true","message":"查询成功","data":[...]}}
 *
 * @author wuchangyue
 * @version 2026-10-29
 */
@Controller
@RequestMapping("/api/env")
public class EnvMonitorController extends BaseController {

	private final EnvMonitorService envMonitorService;

	public EnvMonitorController(EnvMonitorService envMonitorService) {
		this.envMonitorService = envMonitorService;
	}

	/**
	 * 查询项目下所有环境监测设备的最新监测值
	 *
	 * @param projectIds 项目ID，多个用英文逗号分隔；不传则使用配置中的默认 10 个项目
	 * @param sqlMode    SQL 实现模式（仅用于对比验证，正式调用不必传）：
	 *                   {@code dynamic} 逐设备动态表名（默认）/
	 *                   {@code hardcoded} 10 个项目写死后 UNION ALL 一次查完，两者结果一致
	 * @return 每个设备一条记录，含项目基本信息与噪声/温度/湿度/扬尘最新值
	 */
	@RequestMapping(value = "latest", method = {RequestMethod.GET, RequestMethod.POST})
	@ResponseBody
	public String latest(@RequestParam(value = "projectIds", required = false) String projectIds,
			@RequestParam(value = "sqlMode", required = false) String sqlMode) {

		long start = System.currentTimeMillis();
		try {
			List<String> ids = envMonitorService.resolveProjectIds(projectIds);
			List<EnvMonitorVo> list = envMonitorService.queryLatest(ids, sqlMode);
			logger.info("[环境监测] 项目 {} 共返回 {} 条设备记录，sqlMode={}，耗时 {}ms",
					ids, list.size(), sqlMode == null ? "default" : sqlMode, System.currentTimeMillis() - start);
			return renderResult(Global.TRUE, "查询成功", list);
		} catch (Exception e) {
			logger.error("[环境监测] 接口查询异常，projectIds={}，sqlMode={}", projectIds, sqlMode, e);
			return renderResult(Global.FALSE, "查询失败：" + e.getMessage());
		}
	}

}
