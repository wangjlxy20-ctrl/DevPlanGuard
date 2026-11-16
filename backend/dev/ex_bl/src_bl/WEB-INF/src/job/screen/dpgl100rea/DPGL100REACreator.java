package job.screen.dpgl100rea;

import biz.grandsight.ex.ui.ac.ShiroUserRole;
import biz.grandsight.ex.ui.ac.util.SessionHelper;
import com.yse.gb.gf.front.CreatorException;
import com.yse.gb.gf.front.UserMessage;
import com.yse.gb.gf.front.html.Input;
import com.yse.gb.gf.front.html.Select;
import com.yse.gb.gf.front.html.Span;
import com.yse.gb.gf.front.html.Textarea;
import com.yse.gb.util.FormatDate;
import jakarta.enterprise.context.RequestScoped;

import jakarta.inject.Inject;
import job.entity.Project;
import job.entity.Requirement;
import job.service.RequirementService;

import java.text.SimpleDateFormat;
import java.util.Date;


@RequestScoped
public class DPGL100REACreator extends com.yse.gb.gf.front.Creator {

	@Inject
	private RequirementService requirementService;

	@Override
	protected void createValueBean(final com.yse.gb.gf.front.KeyObj keyObj,
								   final com.yse.gb.gf.front.ValueBean valueBean)
			throws CreatorException, Throwable {

		System.out.println("====DPGL100REA Creator executed====");

		// 系统时间
		String strNowDate = FormatDate.get_YYYYMMDDhhmmss(new Date(System.currentTimeMillis()));
		valueBean.add(new Span("NMLLBL_NOW_DATE", strNowDate));

		// 入参：项目 ID + 需求编码（REQ_CODE 有值 = 编辑模式）
		String projectId = keyObj.get("PROJECT_ID");
		String reqCode = keyObj.get("REQ_CODE");
		boolean isEdit = reqCode != null && reqCode.trim().length() > 0;
		System.out.println("DPGL100REA: projectId=" + projectId
				+ ", isEdit=" + isEdit + ", reqCode=" + reqCode);

		// 隐藏项目 ID（HTML 已有同名隐藏域）
		Input hiddenPid = new Input("NMLTXT_PROJECT_ID");
		hiddenPid.setValue(projectId == null ? "" : projectId);
		valueBean.add(hiddenPid);

		// 隐藏需求编码：提交时带回，Command 据此判断模式并定位 update
		Input hiddenReqCode = new Input("NMLTXT_REQ_CODE");
		hiddenReqCode.setValue(isEdit ? reqCode : "");
		valueBean.add(hiddenReqCode);

		// 表单组件（组件名与 HTML 完全一致）
		Input inputCode = new Input("NMLTXT_INPUT_INFO_REQUIREMENT_CODE");
		Input inputName = new Input("NMLTXT_INPUT_INFO_REQUIREMENT_NAME");
		Select selectPriority = new Select("NMLDLD_INFO_PRIORITY");
		// 选项写死在 HTML（静态）。设 false 让框架保留 HTML 选项、只用 selected() 设选中；
		// 否则默认 true 会用 Java 端空 _options 覆盖，下拉被清空，提交时 priority=null
		selectPriority.isOptionUpdate(false);
		Input inputStart = new Input("LNKINP_PLAN_DATE");
		Input inputEnd = new Input("LNKINP_PLAN_END_DATE");
		Textarea textareaDesc = new Textarea("NMLTXA_DESCRIPTION");

		// 固定区：项目名（HTML 组件 NMLLBL_DISPLAY_INFO_PROJECT_NAME）
		if (projectId != null && projectId.trim().length() > 0) {
			Project projectCondition = new Project();
			projectCondition.setProjectId(Integer.parseInt(projectId.trim()));
			Project projectInfo = requirementService.getProject(projectCondition);
			valueBean.add(new Span("NMLLBL_DISPLAY_INFO_PROJECT_NAME",
					projectInfo == null ? "" : projectInfo.getProjectName()));
		}

		// 登录校验（使用用户信息前先判空，防止 NPE）
		ShiroUserRole shiroUserRole = SessionHelper.getShiroUserRole(this);
		if (shiroUserRole == null) {
			throw new CreatorException(new UserMessage(
					"Your session has expired, please log in again"));
		}
		String createdBy = shiroUserRole.getUserName();
		if (createdBy == null || createdBy.trim().isEmpty()) {
			throw new CreatorException("Can't get the logged-in user info");
		}
		valueBean.add(new Span("NMLLBL_CREATOR_NAME", createdBy));

		if (isEdit) {
			// 编辑模式：按 project_id + requirement_code 查完整需求并回显
			Requirement condition = new Requirement();
			condition.setProjectId(Integer.parseInt(projectId.trim()));
			condition.setRequirementCode(reqCode);
			Requirement req = requirementService.getRequirementByCode(condition);

			if (req != null) {
				inputCode.setValue(req.getRequirementCode());
				inputName.setValue(req.getRequirementName());
				// Select 无 setValue，用 selected(value) 设选中项
				selectPriority.selected(req.getPriority());
				inputStart.setValue(fmtDate(req.getPlannedStartDate()));
				inputEnd.setValue(fmtDate(req.getPlannedEndDate()));
				// 描述用两参构造回显
				textareaDesc = new Textarea("NMLTXA_DESCRIPTION",
						req.getDescription() == null ? "" : req.getDescription());
			}

			// 业务编码编辑时只读：值仍会随表单提交，但用户不能改
			inputCode.setReadOnly(true);
		}

		// 按 HTML 表单顺序加入组件
		valueBean.add(inputCode);
		valueBean.add(inputName);
		valueBean.add(selectPriority);
		valueBean.add(inputStart);
		valueBean.add(inputEnd);
		valueBean.add(textareaDesc);
	}

	/** Date → yyyy/MM/dd；null 返回空串。 */
	private String fmtDate(Date d) {
		if (d == null) {
			return "";
		}
		return new SimpleDateFormat("yyyy/MM/dd").format(d);
	}
}