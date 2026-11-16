package job.screen.dpgl110taskassign;

import biz.grandsight.ex.ui.ac.ShiroUserRole;
import biz.grandsight.ex.ui.ac.util.SessionHelper;
import com.yse.gb.gf.front.*;
import com.yse.gb.gf.front.html.*;
import com.yse.gb.util.FormatDate;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import job.entity.ProjectMember;
import job.entity.Task;
import job.service.ProjectService;
import job.service.TaskService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;



@RequestScoped
public class DPGL110TASKASSIGNCreator extends Creator {

	@Inject
	private TaskService taskService;

	@Inject
	private ProjectService projectService;

	
	@Override
	protected void createValueBean(final KeyObj keyObj, final ValueBean valueBean)
			throws CreatorException, Throwable {
		System.out.println("====DPGL110TASKASSIGN Creator executed====");


		// 顶部提示消息（校验失败/成功时，Command 放进 keyObj 的 MEMA_MSG）
		String msg = keyObj.get("MEMA_MSG");
		if (msg != null && msg.trim().length() > 0) {
			valueBean.setMsgHandleBean(new MsgHandleBean(new UserMessage(msg)));
		}

		// SYS TIME
		String now = FormatDate.get_YYYYMMDDhhmmss(new Date(System.currentTimeMillis()));
		valueBean.add(new Span("NMLLBL_NOW_DATE", now));

		//hidden
		String projectRole = keyObj.get("PROJECT_ROLE");
		String projectId = keyObj.get("PROJECT_ID");
		Input hiddenProjectRole = new Input("NMLTXT_MY_ROLE");
		hiddenProjectRole.setValue(projectRole);
		valueBean.add(hiddenProjectRole);
		Input hiddenProjectId = new Input("NMLTXT_PROJECT_ID");
		hiddenProjectId.setValue(projectId);
		valueBean.add(hiddenProjectId);

		// TASK_ID brought from DPGL110TASK
		String taskId = keyObj.get("TASK_ID");
		if (taskId == null) {
			taskId = "";
		}
		taskId = taskId.trim();
		System.out.println(">>>>DPGL110TASKASSIGN got TASK_ID: " + taskId);

		// 数据级权限：框架不做数据级校验，本画面只允许该任务所属项目的 ACTIVE LEAD 进入
		ShiroUserRole shiroUserRole = SessionHelper.getShiroUserRole(this);
		String currentUserId = (shiroUserRole == null) ? null : shiroUserRole.getUserId();
		System.out.println(">>>>DPGL110TASKASSIGN CURRENT LOGIN USER IS :" + currentUserId);

		Integer loginUserId = toIdOrNull(currentUserId);
		if (loginUserId == null) {
			throw new CreatorException(new UserMessage("Your session has expired, please log in again"));
		}

		// task_id → requirement_id → project_id（条件实体按主键定位）
		Integer clickedTaskId = toIdOrNull(taskId);
		Integer checkProjectId = (clickedTaskId == null)
				? null : taskService.getProjectId(taskRef(taskId));
		if (checkProjectId == null) {
			throw new CreatorException(new UserMessage("Can't find the project of the clicked task, please re-enter from the task list."));
		}

		ProjectMember leaderCondition = new ProjectMember();
		leaderCondition.setProjectId(checkProjectId);
		leaderCondition.setUserId(loginUserId);
		projectService.checkProjectLeader(leaderCondition);

		// 回填隐藏 task_id（提交时 COMMAND 用 getParam 读）
		Input hiddenTid = new Input("NMLTXT_TASK_ID");
		hiddenTid.setValue(taskId);
		valueBean.add(hiddenTid);

		// TASK INFO (READ ONLY)
		Task task = null;
		if (taskId.length() > 0) {
			task = taskService.getTask(taskRef(taskId));
		}
		if (task != null) {
			valueBean.add(new Span("NMLLBL_TASK_CODE", String.valueOf(task.getTaskCode())));
			valueBean.add(new Span("NMLLBL_TASK_NAME", String.valueOf(task.getTaskName())));
			valueBean.add(new Span("NMLLBL_TASK_TYPE", String.valueOf(task.getTaskType())));
		} else {
			valueBean.add(new Span("NMLLBL_TASK_CODE", "Task not found."));
			valueBean.add(new Span("NMLLBL_TASK_NAME", ""));
			valueBean.add(new Span("NMLLBL_TASK_TYPE", ""));
		}

		// TASK DUE DATE（选择受让人之前指定；点击弹日历，选中写回本框）
		Input dueDate = new Input("LNKINP_PLAN_END_DATE");
		valueBean.add(dueDate);

		// PROJECT MEMBER LIST（task -> requirement -> project -> project_member）
		List<ProjectMember> members = new ArrayList<>();
		if (taskId.length() > 0) {
			List<ProjectMember> found = taskService.getAssignableMembers(taskRef(taskId));
			if (found != null) {
				members = found;
			}
		}
		System.out.println("ASSIGN CANDIDATE COUNT = " + members.size());

		Table memberTable = new Table("TABLE_MEMBER");

		for (ProjectMember m : members) {
			String username = String.valueOf(m.getUsername());
			String role = String.valueOf(m.getProjectRole());
			if ("null".equals(role)) {
				role = "";
			}

			Row row = new Row();

			// CheckBox：两参数构造（name, 默认未勾选），value 放 user_id
			CheckBox cb = new CheckBox("NMLCHK_SELECT_ASSIGNEE", false);
			cb.setValue(String.valueOf(m.getUserId()));
			row.add(cb);

			row.add(new Span("NMLLBL_MEMBER_USERNAME", username));
			row.add(new Span("NMLLBL_PROJECT_ROLE", role));

			memberTable.add(row);
		}

		valueBean.add(memberTable);
	}

	
	private Task taskRef(String taskId) {
		Task task = new Task();
		task.setTaskId(Integer.valueOf(taskId.trim()));
		return task;
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
}
