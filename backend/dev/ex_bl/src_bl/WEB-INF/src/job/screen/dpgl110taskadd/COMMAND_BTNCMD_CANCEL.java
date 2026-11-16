package job.screen.dpgl110taskadd;

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

		String requirementId = helper.getParam("NMLTXT_REQ_ID");
		String projectId = helper.getParam("NMLTXT_PROJECT_ID");
		String projectRole = helper.getParam("NMLTXT_MY_ROLE");
		String targetScreenId = "DPGL110TASK";
		KeyObj targetKeyObj = helper.getKeyObj(targetScreenId);
		targetKeyObj.put("REQUIREMENT_ID", requirementId == null ? "" : requirementId.trim());
		targetKeyObj.put("PROJECT_ROLE", projectId == null ? "" : projectRole);
		targetKeyObj.put("PROJECT_ID", projectRole == null ? "" : projectId);
		helper.pageOpen(targetScreenId, targetKeyObj);
	}
}
