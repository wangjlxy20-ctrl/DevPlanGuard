package job.screen.dpgl120taskdetail;

import jakarta.enterprise.context.RequestScoped;

import com.yse.gb.gf.bld.front.BLDCommandImpl;
import com.yse.gb.gf.front.CommandException;
import com.yse.gb.gf.front.CommandHelper;
import com.yse.gb.gf.front.InputException;
import com.yse.gb.gf.front.KeyObj;
import job.mapper.task.TaskMapper;

@RequestScoped
public class COMMAND_BTNCMD_APPROVE extends BLDCommandImpl {

	@Override
	protected void perform(final CommandHelper helper, final String nextScreenId, final KeyObj keyObj)
			throws InputException, CommandException, Throwable {

		String taskId = helper.getParam("NMLTXT_TASK_ID");
		String targetScreenId = "DPGL210SUBLIST";

		if (taskId == null || taskId.trim().isEmpty()) {
			openList(helper, targetScreenId, "Task id is missing, please retry from the list.");
			return;
		}

		int cnt = new TaskMapper()
				.reviewTask(taskId.trim(), "PASSED", "DONE");

		String msg = (cnt > 0)
				? "Task approved and marked as completed."
				: "This task is no longer pending review.";
		openList(helper, targetScreenId, msg);
	}

	private void openList(final CommandHelper helper, final String screenId, final String msg)
			throws Exception {
		KeyObj targetKeyObj = helper.getKeyObj(screenId);
		targetKeyObj.put("MEMA_MSG", msg);
		helper.pageOpen(screenId, targetKeyObj);
	}
}
