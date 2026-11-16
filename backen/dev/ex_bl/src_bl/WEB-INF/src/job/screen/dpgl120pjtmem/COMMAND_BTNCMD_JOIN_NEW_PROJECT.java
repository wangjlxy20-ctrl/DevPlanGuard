package job.screen.dpgl120pjtmem;

import jakarta.enterprise.context.RequestScoped;

import com.yse.gb.gf.front.CommandException;
import com.yse.gb.gf.front.CommandHelper;
import com.yse.gb.gf.front.InputException;
import com.yse.gb.gf.front.KeyObj;


@RequestScoped
public class COMMAND_BTNCMD_JOIN_NEW_PROJECT extends com.yse.gb.gf.bld.front.BLDCommandImpl {

	@Override
	protected void perform(final CommandHelper helper, final String nextScreenId, final KeyObj keyObj)
			throws InputException, CommandException, Throwable {

		//画面跳转（功能待开发：回原画面并带提示）
		String targetScreenId = "DPGL120PJTMEM";
		KeyObj targetKeyObj = helper.getKeyObj(targetScreenId);
		targetKeyObj.put("MEMA_MSG", "JOIN NEW PROJECT 功能待开发。");
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
