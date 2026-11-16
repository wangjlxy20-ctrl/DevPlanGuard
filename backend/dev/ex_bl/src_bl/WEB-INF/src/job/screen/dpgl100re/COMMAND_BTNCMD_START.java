package job.screen.dpgl100re;

import com.yse.gb.gf.front.*;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import job.entity.Requirement;
import job.service.RequirementService;

@RequestScoped
public class COMMAND_BTNCMD_START extends com.yse.gb.gf.bld.front.BLDCommandImpl {

	@Inject
	private RequirementService requirementService;


	@Override
	protected void perform(final CommandHelper helper, final String nextScreenId, final KeyObj keyObj)
			throws InputException, CommandException, Throwable {
		String projectId = helper.getParam("NMLTXT_PROJECT_ID");
		if (projectId == null || projectId.trim().length() == 0) {
			throw new CommandException(new UserMessage(
					"Can't access the current project_id, please re-enter from the project list."));
		}

		// 按钮列 / 标识列：同一下标即被点击的那一行
		String[] btnCol = helper.getRowParams("BTNCMD_START");
		String[] codeCol = helper.getRowParams("NMLTXT_REQ_CODE");

		Requirement condition = new Requirement();
		condition.setProjectId(Integer.parseInt(projectId.trim()));

		requirementService.startRequirement(condition, btnCol, codeCol);
	}
}
