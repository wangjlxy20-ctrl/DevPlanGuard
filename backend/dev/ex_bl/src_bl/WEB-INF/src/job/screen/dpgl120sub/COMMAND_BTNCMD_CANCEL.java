package job.screen.dpgl120sub;

import jakarta.enterprise.context.RequestScoped;

import com.yse.gb.gf.front.CommandException;
import com.yse.gb.gf.front.CommandHelper;
import com.yse.gb.gf.front.InputException;
import com.yse.gb.gf.front.KeyObj;


@RequestScoped
public class COMMAND_BTNCMD_CANCEL extends com.yse.gb.gf.bld.front.BLDCommandImpl {

	
	@Override
	protected void perform(final CommandHelper helper, final String nextScreenId, final KeyObj keyObj)
			throws InputException, CommandException, Throwable {



		System.out.println("SUBMIT CANCEL -> DPGL120PJTMEM");

		String projectId = helper.getParam("NMLTXT_PROJECT_ID");
		String projectRole = helper.getParam("NMLTXT_MY_ROLE");
		//画面跳转（回到我的任务列表）
		String targetScreenId = "DPGL120PJTMEM";
		KeyObj targetKeyObj = helper.getKeyObj(targetScreenId);
		targetKeyObj.put("PROJECT_ROLE", projectRole == null ? "" : projectRole);
		targetKeyObj.put("PROJECT_ID", projectId == null ? "" : projectId);
		//targetKeyObj.put("NMLTXT_TASK_ID",helper.getParam("NMLTXT_TASK_ID"));
		helper.pageOpen(targetScreenId, targetKeyObj);
	}
}
