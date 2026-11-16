package job.screen.dpgl120pjtmem;

import jakarta.enterprise.context.RequestScoped;

import com.yse.gb.gf.front.CommandException;
import com.yse.gb.gf.front.CommandHelper;
import com.yse.gb.gf.front.InputException;
import com.yse.gb.gf.front.KeyObj;


@RequestScoped
public class COMMAND_LNKMOV_DETAILS extends com.yse.gb.gf.bld.front.BLDCommandImpl {

	@Override
	protected void perform(final CommandHelper helper, final String nextScreenId, final KeyObj keyObj)
			throws InputException, CommandException, Throwable {

		String taskId = helper.getLinkValue();
		System.out.println("<<<DPGL120PJTMEM : LNKMOV_TASK_CODE clicked, task_id=" + taskId);

		//画面跳转
		String targetScreenId = "DPGL120DETAIL";
		KeyObj targetKeyObj = helper.getKeyObj(targetScreenId);
		targetKeyObj.put("TASK_ID", taskId);
		helper.pageOpen(targetScreenId, targetKeyObj);

	}
}
