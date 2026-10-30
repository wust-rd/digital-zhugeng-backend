package com.jeesite.modules.gongdi.web;

import com.jeesite.common.config.Global;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.gongdi.entity.EnvMonitorVo;
import com.jeesite.modules.gongdi.service.EnvMonitorService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 工地环境监测（噪声扬尘）开放接口
 * <p>
 * 【免登录】路径不在 adminPath（/a）之下，命中 shiro 过滤器链 {@code /** = anon}：
 * <pre>
 *   GET http://127.0.0.1:8980/js/api/env/latest
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
	 * 查询各项目环境监测设备的最新监测值
	 *
	 * @return 每台设备一条，含项目信息、噪声/温度/湿度/扬尘最新值与颜色标记
	 */
	@RequestMapping(value = "latest", method = {RequestMethod.GET, RequestMethod.POST})
	@ResponseBody
	public String latest() {

		long start = System.currentTimeMillis();
		try {
			List<EnvMonitorVo> list = envMonitorService.queryLatest();
			logger.info("[环境监测] 返回 {} 条设备记录，耗时 {}ms", list.size(), System.currentTimeMillis() - start);
			return renderResult(Global.TRUE, "查询成功", list);
		} catch (Exception e) {
			logger.error("[环境监测] 接口查询异常", e);
			return renderResult(Global.FALSE, "查询失败：" + e.getMessage());
		}
	}

}
