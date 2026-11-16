package job.screen.dpgl100;

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

		String projectId = helper.getLinkValue();
		System.out.println("DPGL100 : PREPARED TO OPEN DPGL100RE (REQUIREMENT), PROJECT_ID = " + projectId);

		//画面跳转（需求列表）
		String targetScreenId = "DPGL100RE";
		KeyObj targetKeyObj = helper.getKeyObj(targetScreenId);
		targetKeyObj.put("PROJECT_ID", projectId == null ? "" : projectId);
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
