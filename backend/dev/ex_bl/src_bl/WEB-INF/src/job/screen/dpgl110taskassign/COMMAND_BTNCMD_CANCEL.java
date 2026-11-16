package job.screen.dpgl110taskassign;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import com.yse.gb.gf.front.CommandException;
import com.yse.gb.gf.front.CommandHelper;
import com.yse.gb.gf.front.InputException;
import com.yse.gb.gf.front.KeyObj;
import job.entity.Task;
import job.service.TaskService;


@RequestScoped
public class COMMAND_BTNCMD_CANCEL extends com.yse.gb.gf.bld.front.BLDCommandImpl {

	@Inject
	private TaskService taskService;

	
	@Override
	protected void perform(final CommandHelper helper, final String nextScreenId, final KeyObj keyObj)
			throws InputException, CommandException, Throwable {

		//从其他页面进入或返回此页面：用requirement_id渲染或二次渲染任务列表
		System.out.println("ASSIGN CANCEL -> DPGL110TASK");

		String taskId = helper.getParam("NMLTXT_TASK_ID");
		String projectId = helper.getParam("NMLTXT_PROJECT_ID");
		String projectRole = helper.getParam("NMLTXT_MY_ROLE");

		String requirementId = "";
		if (taskId != null && !taskId.trim().isEmpty()) {
			try {
				Task condition = new Task();
				condition.setTaskId(Integer.valueOf(taskId.trim()));
				Task task = taskService.getTask(condition);
				if (task != null) {
					requirementId = String.valueOf(task.getRequirementId());
				}
			} catch (Exception e) {
				System.out.println("CANCEL resolve requirement error: " + e.getMessage());
			}
		}

		//画面跳转（RETURN TO THE TASK LIST）
		String targetScreenId = "DPGL110TASK";
		KeyObj targetKeyObj = helper.getKeyObj(targetScreenId);
		targetKeyObj.put("REQUIREMENT_ID", requirementId);
		targetKeyObj.put("PROJECT_ROLE", projectId == null ? "" : projectRole);
		targetKeyObj.put("PROJECT_ID", projectRole == null ? "" : projectId);
		helper.pageOpen(targetScreenId, targetKeyObj);
	}
}
