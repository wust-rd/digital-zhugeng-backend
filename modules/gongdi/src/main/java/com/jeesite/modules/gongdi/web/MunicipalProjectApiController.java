package com.jeesite.modules.gongdi.web;

import com.jeesite.common.config.Global;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.gongdi.entity.MunicipalProjectVo;
import com.jeesite.modules.gongdi.service.MunicipalProjectService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 市政工程项目基本信息开放接口
 * <p>
 * 【免登录】路径不在 adminPath（/a）之下，命中 shiro 过滤器链 {@code /** = anon}：
 * <pre>
 *   GET http://127.0.0.1:8980/js/api/muni/project/list
 * </pre>
 * 返回结构沿用框架约定：{@code {"result":"true","message":"查询成功","data":[...]}}
 *
 * @author wuchangyue
 * @version 2026-11-03
 */
@Controller
@RequestMapping("/api/muni")
public class MunicipalProjectApiController extends BaseController {

	private final MunicipalProjectService municipalProjectService;

	public MunicipalProjectApiController(MunicipalProjectService municipalProjectService) {
		this.municipalProjectService = municipalProjectService;
	}

	/**
	 * 查询市政工程项目基本信息
	 *
	 * @return 清单每条一行，共 106 行
	 */
	@RequestMapping(value = "project/list", method = {RequestMethod.GET, RequestMethod.POST})
	@ResponseBody
	public String projectList() {

		long start = System.currentTimeMillis();
		try {
			List<MunicipalProjectVo> list = municipalProjectService.queryProjectList();
			logger.info("[市政项目] 返回 {} 条项目记录，耗时 {}ms", list.size(), System.currentTimeMillis() - start);
			return renderResult(Global.TRUE, "查询成功", list);
		} catch (Exception e) {
			logger.error("[市政项目] 接口查询异常", e);
			return renderResult(Global.FALSE, "查询失败：" + e.getMessage());
		}
	}

}
