package com.jeesite.modules.qa.web;

import com.jeesite.common.config.Global;
import com.jeesite.common.web.BaseController;
import com.jeesite.modules.qa.entity.QaInspectForm;
import com.jeesite.modules.qa.entity.QaInspectRecord;
import com.jeesite.modules.qa.entity.QaLevelAlert;
import com.jeesite.modules.qa.entity.QaProject;
import com.jeesite.modules.qa.service.QaInspectService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.function.Supplier;

/**
 * 质安常态化检查接口
 * <p>
 * 路径在 adminPath 之下，需登录后访问。
 * 返回结构沿用框架约定：result / message / data。
 *
 * @author wuchangyue
 * @version 2026-11-03
 */
@Controller
@RequestMapping("${adminPath}/qa")
public class QaController extends BaseController {

	private final QaInspectService qaInspectService;

	public QaController(QaInspectService qaInspectService) {
		this.qaInspectService = qaInspectService;
	}

	/**
	 * 武汉市下辖区与功能区
	 */
	@RequestMapping(value = "district/list")
	@ResponseBody
	public String districtList() {

		return execute("查询行政区字典", () -> qaInspectService.findDistrictList());
	}

	/**
	 * 项目台账列表
	 */
	@RequestMapping(value = "project/list")
	@ResponseBody
	public String projectList(QaProject project) {

		return execute("查询项目台账", () -> qaInspectService.findProjectList(project));
	}

	/**
	 * 项目台账详情
	 */
	@RequestMapping(value = "project/get")
	@ResponseBody
	public String projectGet(String id) {

		return execute("查询项目台账", () -> qaInspectService.getProject(id));
	}

	/**
	 * 保存项目台账
	 */
	@PostMapping(value = "project/save")
	@ResponseBody
	public String projectSave(@RequestBody QaProject project) {

		return execute("保存项目台账", () -> {
			qaInspectService.saveProject(project);
			return project.getId();
		});
	}

	/**
	 * 检查记录列表
	 */
	@RequestMapping(value = "inspect/list")
	@ResponseBody
	public String inspectList(QaInspectRecord where) {

		return execute("查询检查记录", () -> qaInspectService.findRecordList(where));
	}

	/**
	 * 检查记录详情，含两张勾选明细
	 */
	@RequestMapping(value = "inspect/detail")
	@ResponseBody
	public String inspectDetail(String id) {

		return execute("查询检查记录详情", () -> qaInspectService.getRecordDetail(id));
	}

	/**
	 * 保存检查记录
	 */
	@PostMapping(value = "inspect/save")
	@ResponseBody
	public String inspectSave(@RequestBody QaInspectForm form) {

		return execute("保存检查记录", () -> qaInspectService.saveInspect(form));
	}

	/**
	 * 作废检查记录
	 */
	@PostMapping(value = "inspect/void")
	@ResponseBody
	public String inspectVoid(String id, String reason) {

		return execute("作废检查记录", () -> {
			qaInspectService.voidRecord(id, reason);
			return id;
		});
	}

	/**
	 * 项目档案近 12 月检查格子
	 */
	@RequestMapping(value = "inspect/archive")
	@ResponseBody
	public String inspectArchive(String projectId) {

		return execute("查询项目档案", () -> qaInspectService.archive(projectId));
	}

	/**
	 * 工作台统计
	 */
	@RequestMapping(value = "inspect/dashboard")
	@ResponseBody
	public String inspectDashboard() {

		return execute("查询工作台统计", () -> qaInspectService.dashboard());
	}

	/**
	 * 等级预警列表
	 */
	@RequestMapping(value = "alert/list")
	@ResponseBody
	public String alertList(QaLevelAlert where) {

		return execute("查询等级预警", () -> qaInspectService.findAlertList(where));
	}

	/**
	 * 统一执行与异常转换
	 */
	private String execute(String action, Supplier<Object> task) {

		try {
			return renderResult(Global.TRUE, action + "成功", task.get());
		} catch (IllegalArgumentException e) {
			return renderResult(Global.FALSE, e.getMessage());
		} catch (Exception e) {
			Throwable root = e;
			while (root.getCause() != null && root.getCause() != root) {
				root = root.getCause();
			}
			logger.error("[质安检查] " + action + "失败", e);
			String message = root.getMessage() == null
				? root.getClass().getSimpleName() : root.getMessage().replaceAll("\\s+", " ").trim();
			return renderResult(Global.FALSE, action + "失败：" + message);
		}
	}

}
