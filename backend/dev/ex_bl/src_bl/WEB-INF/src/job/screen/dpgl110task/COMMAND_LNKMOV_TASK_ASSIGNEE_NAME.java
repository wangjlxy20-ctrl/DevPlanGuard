package job.screen.dpgl110task;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import com.yse.gb.gf.front.CommandException;
import com.yse.gb.gf.front.CommandHelper;
import com.yse.gb.gf.front.InputException;
import com.yse.gb.gf.front.KeyObj;

import job.entity.Task;
import job.service.TaskService;

@RequestScoped
public class COMMAND_LNKMOV_TASK_ASSIGNEE_NAME extends com.yse.gb.gf.bld.front.BLDCommandImpl {

	@Inject
	private TaskService taskService;

	@Override
	protected void perform(final CommandHelper helper, final String nextScreenId, final KeyObj keyObj)
			throws InputException, CommandException, Throwable {

		System.out.println(">>>> DPGL110TASK **NEW LNKMOV COMMAND IS RUNNING** <<<<");

		// getLinkValue = 被点击那行 LNKMOV 传过来的 task_id
		String taskId = helper.getLinkValue();
		System.out.println("CLICKED TASK_ID = " + taskId);

		String projectId = helper.getParam("NMLTXT_PROJECT_ID");
		String projectRole = helper.getParam("NMLTXT_MY_ROLE");
		// 查库判断未分配 / 已分配（任务存在性与 assignee_id 判断在 service）
		Task clickedTask = new Task();
		clickedTask.setTaskId(toIdOrNull(taskId));
		String targetScreenId = taskService.getAssignTargetScreenId(clickedTask);

		String rid = helper.getParam("NMLTXT_REQ_ID");

		//画面跳转
		KeyObj targetKeyObj = helper.getKeyObj(targetScreenId);
		targetKeyObj.put("REQUIREMENT_ID", rid == null ? "" : rid);
		targetKeyObj.put("TASK_ID", taskId == null ? "" : taskId);
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
}
