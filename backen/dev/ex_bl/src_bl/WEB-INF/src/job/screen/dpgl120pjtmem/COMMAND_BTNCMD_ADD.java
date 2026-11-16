package job.screen.dpgl120pjtmem;

import com.yse.gb.gf.front.*;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import job.service.TaskService;

@RequestScoped
public class COMMAND_BTNCMD_ADD extends com.yse.gb.gf.bld.front.BLDCommandImpl {

	@Inject
	private TaskService taskService;

	
	@Override
	protected void perform(final CommandHelper helper, final String nextScreenId, final KeyObj keyObj)
			throws InputException, CommandException, Throwable {

		//取参：按钮名 + 任务 id / 类型两列（原样，不改参数名）
		String buttonName = helper.getParam("BUTTON_NAME");
		System.out.println("DPGL120SUB BUTTON_NAME = " + buttonName);

		String[] taskIdCol = helper.getRowParams("NMLTXT_TASK_ID");
		String[] taskTypeCol = helper.getRowParams("NMLTXT_TASK_TYPE");


		String projectId = helper.getParam("NMLTXT_PROJECT_ID");
		String projectRole = helper.getParam("NMLTXT_MY_ROLE");

		//service:解析点击行 → 按任务类型选提交画面 → 任务不存在校验
		//返回 {taskId, taskType, targetScreenId}
		String[] submitTarget = taskService.resolveSubmitTarget(buttonName, taskIdCol, taskTypeCol);
		String taskId = submitTarget[0];
		String taskType = submitTarget[1];
		String targetScreenId = submitTarget[2];

		KeyObj targetKeyObj = helper.getKeyObj(targetScreenId);
		targetKeyObj.put("TASK_ID", taskId);
		targetKeyObj.put("TASK_TYPE",taskType);
		targetKeyObj.put("PROJECT_ROLE", projectRole == null ? "" : projectRole);
		targetKeyObj.put("PROJECT_ID", projectId == null ? "" : projectId);

		//画面跳转
		helper.pageOpen(targetScreenId, targetKeyObj);

	}
}
