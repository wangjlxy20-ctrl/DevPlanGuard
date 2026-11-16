package job.screen.dpgl110taskadd;

import biz.grandsight.ex.ui.ac.ShiroUserRole;
import biz.grandsight.ex.ui.ac.util.SessionHelper;
import com.yse.gb.gf.front.*;
import com.yse.gb.gf.front.html.*;
import com.yse.gb.util.FormatDate;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import job.entity.ProjectMember;
import job.entity.Requirement;
import job.mapper.task.TaskMapper;
import job.service.MemberService;
import job.service.ProjectService;
import job.service.RequirementService;
import java.util.List;
import java.util.Map;



@RequestScoped
public class DPGL110TASKADDCreator extends Creator {


	@Inject
	private RequirementService requirementService;

	@Inject
	private MemberService memberService;

	@Inject
	private ProjectService projectService;

	@Override
	protected void createValueBean(final KeyObj keyObj, final ValueBean valueBean)
			throws CreatorException, Throwable {
		// Write the code here
		System.out.println("====DPGL110TASKADD Creator executed====");

		// Top notification message
		String msg = keyObj.get("MEMA_MSG");
		if (msg != null && !msg.trim().isEmpty()) {
			valueBean.setMsgHandleBean(new MsgHandleBean(new UserMessage(msg)));
		}

		// SYS TIME
		String now = FormatDate.get_YYYYMMDDhhmmss(new java.util.Date(System.currentTimeMillis()));
		valueBean.add(new Span("NMLLBL_NOW_DATE", now));

		// CURRENT LOGIN USER
		ShiroUserRole shiroUserRole = SessionHelper.getShiroUserRole(this);
		String userId = (shiroUserRole == null) ? null : shiroUserRole.getUserId();
		System.out.println("DPGL110TASKADD>>>CURRENT LOGIN USER IS :" + userId);

		//hidden
		String projectRole = keyObj.get("PROJECT_ROLE");
		String projectId = keyObj.get("PROJECT_ID");
		Input hiddenProjectRole = new Input("NMLTXT_MY_ROLE");
		hiddenProjectRole.setValue(projectRole);
		valueBean.add(hiddenProjectRole);
		Input hiddenProjectId = new Input("NMLTXT_PROJECT_ID");
		hiddenProjectId.setValue(projectId);
		valueBean.add(hiddenProjectId);


		// requirement_id：Carry over from the previous page
		String requirementId = keyObj.get("REQUIREMENT_ID");
		requirementId = requirementId.trim();
		System.out.println("DPGL110TASKADD>>>CURRENT REQUIREMENT_ID IS :" + requirementId);

		// 数据级权限：框架不做数据级校验，本画面只允许该需求所属项目的 ACTIVE LEAD 进入
		Integer loginUserId = toIdOrNull(userId);
		if (loginUserId == null) {
			throw new CreatorException(new UserMessage("Your session has expired, please log in again"));
		}

		// 条件实体：requirement_id（权限校验与需求信息查询共用）
		Requirement reqRef = new Requirement();
		reqRef.setRequirementId(toIdOrNull(requirementId));

		Integer checkProjectId = (reqRef.getRequirementId() == null)
				? null : requirementService.getProjectId(reqRef);
		if (checkProjectId == null) {
			throw new CreatorException(new UserMessage("Can't find the project of the current requirement, please re-enter from the requirement list."));
		}

		ProjectMember leaderCondition = new ProjectMember();
		leaderCondition.setProjectId(checkProjectId);
		leaderCondition.setUserId(loginUserId);
		projectService.checkProjectLeader(leaderCondition);

		// REFILL && HIDDEN: requirement_id
		Input hiddenReq = new Input("NMLTXT_REQ_ID");
		hiddenReq.setValue(requirementId);
		valueBean.add(hiddenReq);

		//get reqCode
		String reqCode = keyObj.get("REQUIREMENT_CODE");

		// SEARCH REQ INFO ：project_id、requirement_name（条件实体：requirement_id）
		Requirement reqInfo = requirementService.getRequirementInfo(reqRef);

		Object pname = reqInfo.getRequirementName();



		Span reqName = new Span("NMLLBL_REQ_NAME", (String) pname);
		Input taskName = new Input("NMLTXT_TASK_NAME");
		Textarea description = new Textarea("NMLTXA_DESCRIPTION");

		//SELECT
		Select taskType = new Select("NMLDLD_TASK_TYPE");
		taskType.add(new Option("DEVELOPMENT", "DEVELOPMENT"));
		taskType.add(new Option("TEST", "TEST"));
		taskType.add(new Option("DEFECT", "DEFECT"));
		taskType.add(new Option("DOCUMENT", "DOCUMENT"));

		// ===== TEST type extra fields (display is controlled by the page JS) =====
		Input testPlan = new Input("NMLTXT_TEST_PLAN");
		Textarea testCaseDesign = new Textarea("NMLTXA_TEST_CASE_DESIGN");

		// ===== DEFECT type extra fields =====
		Select severity = new Select("NMLDLD_SEVERITY");
		severity.add(new Option("", "-- Please select --"));
		severity.add(new Option("HIGH", "HIGH"));
		severity.add(new Option("MEDIUM", "MEDIUM"));
		severity.add(new Option("LOW", "LOW"));

		Radio priority = new Radio("NMLOPT_PRIORITY");
		Select assignee = new Select("NMLDLD_ASSIGNEE");
		Input planStart = new Input("LNKINP_PLAN_DATE");
		Input planEnd = new Input("LNKINP_PLAN_END_DATE");

		// Dynamically fill in the assignee (current member of this project)
		if (!projectId.isEmpty()) {
			// 条件实体：project_id（成员检索按项目定位）
			ProjectMember condition = new ProjectMember();
			condition.setProjectId(Integer.valueOf(projectId.trim()));
			List<ProjectMember> members = memberService.searchAssignableMembers(condition);
			if (members != null) {
				for (ProjectMember m : members) {
					String uid = String.valueOf(m.getUserId());
					String uname = String.valueOf(m.getUsername());
					assignee.add(new Option(uid, uname));
				}
				System.out.println("ASSIGNEE CANDIDATE COUNT = " + members.size());
			}
		}

		// ===== 编辑回显模式：链接命令带 TASK_ID 时，查任务并回填 =====
		String editTaskId = keyObj.get("TASK_ID");
		boolean isEdit = editTaskId != null && !editTaskId.trim().isEmpty();
		System.out.println("DPGL110TASKADD isEdit = " + isEdit + ", taskId = " + editTaskId);

		if (isEdit) {
			TaskMapper taskMapper = new TaskMapper();
			Map<String, Object> tv = taskMapper.getTaskForEdit(editTaskId);

			if (tv != null) {
				taskName.setValue(toStr(tv.get("task_name")));
				// Textarea 用两参构造回显
				description = new Textarea("NMLTXA_DESCRIPTION", toStr(tv.get("description")));
				// Select 无 setValue，用 selected 设选中
				taskType.selected(toStr(tv.get("task_type")));
				testPlan.setValue(toStr(tv.get("test_plan")));
				testCaseDesign = new Textarea("NMLTXA_TEST_CASE_DESIGN",
						toStr(tv.get("test_case_design")));
				severity.selected(toStr(tv.get("severity")));
				// Radio 无 selected，用 checked(序号)；序号 1-based，按 value 映射
				priority.checked(priorityCheckedNumber(toStr(tv.get("priority"))));
				// assignee 已 add 项目成员，这里选中原受让人
				assignee.selected(toStr(tv.get("assignee_id")));
				planStart.setValue(toStr(tv.get("planned_start_date")));
				planEnd.setValue(toStr(tv.get("planned_end_date")));
			}

			// 隐藏 task_id 随表单带回，供后续提交区分更新/新增
			Input hiddenEditTaskId = new Input("NMLTXT_TASK_ID");
			hiddenEditTaskId.setValue(editTaskId);
			valueBean.add(hiddenEditTaskId);
		}

		valueBean.add(reqName);
		valueBean.add(taskName);
		valueBean.add(description);
		valueBean.add(taskType);
		// TEST / DEFECT extra components
		valueBean.add(testPlan);
		valueBean.add(testCaseDesign);
		valueBean.add(severity);
		valueBean.add(priority);
		valueBean.add(assignee);
		valueBean.add(planStart);
		valueBean.add(planEnd);
	}


	private Integer toIdOrNull(String value) {
		if (value == null) {
			return null;
		}
		String v = value.trim();
		if (v.length() == 0 || "null".equals(v)) {
			return null;
		}
		try {
			return Integer.valueOf(v);
		} catch (NumberFormatException e) {
			return null;
		}
	}


	private String toStr(Object value) {
		if (value == null) {
			return "";
		}
		String v = String.valueOf(value).trim();
		return "null".equals(v) ? "" : v;
	}

	//
	private int priorityCheckedNumber(String priorityValue) {
		if (priorityValue == null) {
			return 2;
		}
		switch (priorityValue.trim()) {
			case "HIGH":
				return 1;
			case "LOW":
				return 3;
			case "MEDIUM":
			default:
				return 2;
		}
	}
}

