package job.screen.dpgl100;

import com.yse.gb.gf.front.*;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import job.service.ProjectService;


@RequestScoped
public class COMMAND_BTNCMD_ADD_NEW extends com.yse.gb.gf.bld.front.BLDCommandImpl {

	@Inject
	private ProjectService projectService;

	
	@Override
	protected void perform(final CommandHelper helper, final String nextScreenId, final KeyObj keyObj)
			throws InputException, CommandException, Throwable {
		//get input info
		String projectCodeId = helper.getParam("NMLTXT_INPUT_INFO");
		System.out.println("DPGL100 COMMAND HAS GET THE projectCodeId : " + projectCodeId);

		//新建项目编号校验,交给 service，失败抛原异常
		projectService.checkProjectCodeAvailable(projectCodeId);

		//画面跳转（新建项目）
		String targetScreenId = "DPGL100A";
		KeyObj targetKeyObj = helper.getKeyObj(targetScreenId);
		targetKeyObj.put("PROJECT_CODE_ID",projectCodeId);

		String creatorName = helper.getPreValue("NMLLBL_DISPLAY_INFO_USERNAME");
		System.out.println("DPGL100 COMMAND HAS GET THE CREATOR NAME : " + creatorName);
		targetKeyObj.put("CREATOR_NAME",creatorName);

		helper.pageOpen(targetScreenId, targetKeyObj);
	}
}
