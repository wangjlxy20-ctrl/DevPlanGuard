package job.screen.dpgl110taskadd;

import biz.grandsight.ex.ui.ac.ShiroUserRole;
import biz.grandsight.ex.ui.ac.util.SessionHelper;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import com.yse.gb.gf.front.CommandException;
import com.yse.gb.gf.front.CommandHelper;
import com.yse.gb.gf.front.InputException;
import com.yse.gb.gf.front.KeyObj;
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

		//Form values
		String requirementId = helper.getParam("NMLTXT_REQ_ID");
		String taskName = helper.getParam("NMLTXT_TASK_NAME");
		String description = helper.getParam("NMLTXA_DESCRIPTION");
		String taskType = helper.getParam("NMLDLD_TASK_TYPE");
		String priority = helper.getParam("NMLOPT_PRIORITY");
		String assigneeId = helper.getParam("NMLDLD_ASSIGNEE");
		String planStart = helper.getParam("LNKINP_PLAN_DATE");
		String planEnd = helper.getParam("LNKINP_PLAN_END_DATE");

		//TEST / DEFECT extra values (the rows are shown by the page JS)
		String testPlan = helper.getParam("NMLTXT_TEST_PLAN");
		String testCaseDesign = helper.getParam("NMLTXA_TEST_CASE_DESIGN");
		String severity = helper.getParam("NMLDLD_SEVERITY");

		// current login user_id（Shiro）
		ShiroUserRole shiroUserRole = SessionHelper.getShiroUserRole(helper);
		String uid = (shiroUserRole == null) ? null : shiroUserRole.getUserId();
		String currentUserId = (uid == null) ? "" : uid;

		// refill: requirement_id
		String refillRequirementId = (requirementId == null) ? "" : requirementId.trim();
		String projectId = helper.getParam("NMLTXT_PROJECT_ID");
		String projectRole = helper.getParam("NMLTXT_MY_ROLE");

		// 表单值组装成 Task 实体（createdBy = 当前登录用户 id）
		Task task = new Task();
		task.setCreatedBy(toIdOrNull(currentUserId));
		task.setRequirementId(toIdOrNull(requirementId));
		task.setTaskName(taskName);
		task.setDescription(description);
		task.setTaskType(taskType);
		task.setPriority(priority);
		task.setAssigneeId(toIdOrNull(assigneeId));
		task.setPlannedStartDate(toDate(planStart));
		task.setPlannedEndDate(toDate(planEnd));
		task.setTestPlan(testPlan);
		task.setTestCaseDesign(testCaseDesign);
		task.setSeverity(severity);

		// 校验、task_code 生成、插入与成功消息全部在 service；结果 = {错误信息, 成功信息}
		String[] result = taskService.createTask(task);

		// Validation failed: go back to the form page
		String errorMsg = result[0];
		if (errorMsg != null) {
			System.out.println("TASK ADD VALIDATION FAILED: " + errorMsg);
			String targetScreenId = "DPGL110TASKADD";
			KeyObj targetKeyObj = helper.getKeyObj(targetScreenId);
			targetKeyObj.put("REQUIREMENT_ID", refillRequirementId);
			targetKeyObj.put("MEMA_MSG", errorMsg);
			targetKeyObj.put("PROJECT_ROLE", projectId == null ? "" : projectRole);
			targetKeyObj.put("PROJECT_ID", projectRole == null ? "" : projectId);
			helper.pageOpen(targetScreenId, targetKeyObj);
			return;
		}

		//画面跳转（RETURN TO THE TASK LIST）
		String targetScreenId = "DPGL110TASK";
		KeyObj targetKeyObj = helper.getKeyObj(targetScreenId);
		targetKeyObj.put("REQUIREMENT_ID", refillRequirementId);
		targetKeyObj.put("MEMA_MSG", result[1]);
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
