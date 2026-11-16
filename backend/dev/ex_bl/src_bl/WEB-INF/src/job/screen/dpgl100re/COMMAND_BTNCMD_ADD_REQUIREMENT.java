package job.screen.dpgl100re;

import com.yse.gb.gf.front.*;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import job.entity.Requirement;
import job.service.RequirementService;


@RequestScoped
public class COMMAND_BTNCMD_ADD_REQUIREMENT extends com.yse.gb.gf.bld.front.BLDCommandImpl {

	@Inject
	private RequirementService requirementService;

	
	@Override
	protected void perform(final CommandHelper helper, final String nextScreenId, final KeyObj keyObj)
			throws InputException, CommandException, Throwable {
		String projectId = helper.getParam("NMLTXT_PROJECT_ID");

		//project_id 必填校验交给 service（失败由 service 抛原异常）
		//组装条件实体：project_id 缺失/空白时 projectId 保持 null，必填校验仍在 service 里做
		Requirement condition = new Requirement();
		if (projectId != null && projectId.trim().length() > 0) {
			condition.setProjectId(Integer.parseInt(projectId.trim()));
		}
		requirementService.checkAddRequirement(condition);

		//画面跳转
		String targetScreenId = "DPGL100REA";
		KeyObj targetKeyObj = helper.getKeyObj(targetScreenId);
		targetKeyObj.put("PROJECT_ID", projectId);
		helper.pageOpen(targetScreenId, targetKeyObj);

		// Information acquisition method when expanding the screen
		//
		// String userId =
		// 		keyObj.get("USER_ID");
		//

		// Information acquisition method when displaying on the screen (Span, etc.)
		// *) In the case of Input, it is possible to take the value 
		// at the time of display instead of the input value on the screen.
		//
		// String jobNo =
		// 		helper.getInitValue("NMLLBL_JOB_NO");
		//

		// Screen input information acquisition method (Input etc.)
		//
		// String tankNo =
		// 		helper.getParam("NMLTXT_TANK_NO");
		//

		// Input information acquisition method of list (Input, Checkbox, etc.)
		//
		// String[] deleteNames =
		// 		helper.getCheckedValues("NMLCHK_DELETE");
		//

		// Deployment parameters setting method  when expanding the screen
		// keyObj.put("JOB_NO",jobNo);
		//
	}
}
