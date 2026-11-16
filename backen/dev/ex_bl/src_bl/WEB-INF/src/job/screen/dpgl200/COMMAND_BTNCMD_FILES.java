package job.screen.dpgl200;

import jakarta.enterprise.context.RequestScoped;

import com.yse.gb.gf.front.CommandException;
import com.yse.gb.gf.front.CommandHelper;
import com.yse.gb.gf.front.InputException;
import com.yse.gb.gf.front.KeyObj;

/**
 * Button processing class.<br>
 * <!-- TYPE DESCRIPTION -->
 *
 * <pre>
 * </pre>
 */
@RequestScoped
public class COMMAND_BTNCMD_FILES extends com.yse.gb.gf.bld.front.BLDCommandImpl {
	/**
	 * Button processing execution.<br>
	 * This process is called after the end of {@link #getNextScreenId (CommandHelper, KeyObj)}.<br>
	 * <!-- METHOD DESCRIPTION -->
	 *
	 * <pre>
	 * </pre>
	 *
	 * @param helper
	 *            Command helper
	 * @param nextScreenId
	 *            Deployment screen ID
	 * @param keyObj
	 *            For storing information to be passed to the expansion destination screen
	 */
	@Override
	protected void perform(final CommandHelper helper, final String nextScreenId, final KeyObj keyObj)
			throws InputException, CommandException, Throwable {
		System.out.println("DPGL200 SUBMITTED TASKS -> DPGL210SUBLIST");
		String targetScreenId = "DPGL310FILELIST";
		KeyObj targetKeyObj = helper.getKeyObj(targetScreenId);
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
