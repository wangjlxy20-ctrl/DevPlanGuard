package job.screen.dpgl110mng;

import jakarta.enterprise.context.RequestScoped;

import com.yse.gb.gf.front.CommandException;
import com.yse.gb.gf.front.CommandHelper;
import com.yse.gb.gf.front.InputException;
import com.yse.gb.gf.front.KeyObj;


@RequestScoped
public class COMMAND_LNKMOV_REQUIREMENT extends com.yse.gb.gf.bld.front.BLDCommandImpl {
	
	@Override
	protected void perform(final CommandHelper helper, final String nextScreenId, final KeyObj keyObj)
			throws InputException, CommandException, Throwable {

		// getLinkValue = the hidden requirement_id passed from the front-end linkOpen
		String requirementId = helper.getLinkValue();
		System.out.println("OPEN TASK PAGE: requirementId=" + requirementId);
		String projectId = helper.getParam("NMLTXT_PROJECT_ID");
		String projectRole = helper.getParam("NMLTXT_MY_ROLE");

		//画面跳转
		String targetScreenId = "DPGL110TASK";
		KeyObj targetKeyObj = helper.getKeyObj(targetScreenId);
		targetKeyObj.put("REQUIREMENT_ID", requirementId == null ? "" : requirementId);
		targetKeyObj.put("PROJECT_ROLE", projectRole == null ? "" : projectRole);
		targetKeyObj.put("PROJECT_ID", projectId == null ? "" : projectId);

		helper.pageOpen(targetScreenId, targetKeyObj);
	}
}
