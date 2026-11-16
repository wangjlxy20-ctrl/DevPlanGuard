package job.screen.dpgl110taskassign;

import biz.grandsight.ex.ui.ac.ShiroUserRole;
import biz.grandsight.ex.ui.ac.util.SessionHelper;
import com.yse.gb.gf.front.*;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import job.entity.Task;
import job.service.TaskService;

import java.text.SimpleDateFormat;
import java.util.Date;


@RequestScoped
public class COMMAND_BTNCMD_ADD extends com.yse.gb.gf.bld.front.BLDCommandImpl {

	@Inject
	private TaskService taskService;

	
	@Override
	protected void perform(final CommandHelper helper, final String nextScreenId, final KeyObj keyObj)
			throws InputException, CommandException, Throwable {

		System.out.println("====DPGL110TASKASSIGN ASSIGN executed====");

		String projectId = helper.getParam("NMLTXT_PROJECT_ID");
		String projectRole = helper.getParam("NMLTXT_MY_ROLE");

		// 取 task_id（隐藏字段）
		String taskId = helper.getParam("NMLTXT_TASK_ID");
		taskId = (taskId == null) ? "" : taskId.trim();

		// 取纳期（选择受让人之前指定）
		String dueDate = helper.getParam("LNKINP_PLAN_END_DATE");
		dueDate = (dueDate == null) ? "" : dueDate.trim();
		System.out.println("DUE DATE = " + dueDate);

		// 当前登录用户 id（Shiro）
		ShiroUserRole shiroUserRole = SessionHelper.getShiroUserRole(helper);
		String uid = (shiroUserRole == null) ? null : shiroUserRole.getUserId();
		String currentUserId = (uid == null) ? "" : uid;

		// 受让人复选框原始行值（空行过滤/校验在 service）
		String[] assigneeRows = helper.getRowParams("NMLCHK_SELECT_ASSIGNEE");

		//  组装 Task：task_id / 纳期 / 分配人（assigned_by = 当前登录用户）
		Task task = new Task();
		task.setTaskId(toIdOrNull(taskId));
		task.setPlannedEndDate(toDate(dueDate));
		task.setAssignedBy(toIdOrNull(currentUserId));

		// 业务与校验全部交给 service；结果 = {错误信息, requirement_id, 成功信息}
		String[] result = taskService.submitAssignment(task, assigneeRows);

		// 校验失败：回本页提示，不执行分配
		String errorMsg = result[0];
		if (errorMsg != null) {
			System.out.println("ASSIGN VALIDATION FAILED: " + errorMsg);
			String targetScreenId = "DPGL110TASKASSIGN";
			KeyObj targetKeyObj = helper.getKeyObj(targetScreenId);
			targetKeyObj.put("TASK_ID", taskId);
			targetKeyObj.put("MEMA_MSG", errorMsg);
			targetKeyObj.put("PROJECT_ROLE", projectId == null ? "" : projectRole);
			targetKeyObj.put("PROJECT_ID", projectRole == null ? "" : projectId);
			helper.pageOpen(targetScreenId, targetKeyObj);
			return;
		}

		// 8. 回任务列表：带 requirement_id 保持上下文 + 成功提示
		String targetScreenId = "DPGL110TASK";
		KeyObj targetKeyObj = helper.getKeyObj(targetScreenId);
		targetKeyObj.put("REQUIREMENT_ID", result[1]);
		targetKeyObj.put("MEMA_MSG", result[2]);
		targetKeyObj.put("PROJECT_ROLE", projectId == null ? "" : projectRole);
		targetKeyObj.put("PROJECT_ID", projectRole == null ? "" : projectId);
		helper.pageOpen(targetScreenId, targetKeyObj);
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

	
	private Date toDate(String s) {
		if (s == null || s.trim().length() == 0) {
			return null;
		}
		String value = s.trim();
		String[] patterns = {
				"yyyy-MM-dd", "yyyy/MM/dd", "yyyyMMdd",
				"yyyy-MM-dd HH:mm:ss", "yyyy/MM/dd HH:mm:ss"
		};
		for (int i = 0; i < patterns.length; i++) {
			try {
				SimpleDateFormat format = new SimpleDateFormat(patterns[i]);
				format.setLenient(false);
				Date date = format.parse(value);
				if (date != null) {
					return date;
				}
			} catch (Exception e) {
				// 换下一个格式再试
			}
		}
		throw new IllegalArgumentException("Invalid date: " + value);
	}
}
