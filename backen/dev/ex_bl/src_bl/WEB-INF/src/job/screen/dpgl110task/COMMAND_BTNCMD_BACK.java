package job.screen.dpgl110task;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import com.yse.gb.gf.front.CommandException;
import com.yse.gb.gf.front.CommandHelper;
import com.yse.gb.gf.front.InputException;
import com.yse.gb.gf.front.KeyObj;

import job.entity.Requirement;
import job.service.RequirementService;


@RequestScoped
public class COMMAND_BTNCMD_BACK extends com.yse.gb.gf.bld.front.BLDCommandImpl {

	@Inject
	private RequirementService requirementService;

	
	@Override
	protected void perform(final CommandHelper helper, final String nextScreenId, final KeyObj keyObj)
			throws InputException, CommandException, Throwable {

		String projectId = helper.getParam("NMLTXT_PROJECT_ID");
		String projectRole = helper.getParam("NMLTXT_MY_ROLE");

		//画面跳转
		String targetScreenId = "DPGL110MNG";
		KeyObj targetKeyObj = helper.getKeyObj(targetScreenId);
		targetKeyObj.put("PROJECT_ROLE", projectId == null ? "" : projectRole);
		targetKeyObj.put("PROJECT_ID", projectRole == null ? "" : projectId);
		helper.pageOpen(targetScreenId, targetKeyObj);
	}
}
