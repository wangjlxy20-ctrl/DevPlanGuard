package job.screen.dpgl120taskdetail;

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
public class COMMAND_BTNCMD_BACK extends com.yse.gb.gf.bld.front.BLDCommandImpl {
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

		System.out.println("SUBMIT CANCEL -> DPGL210SUBMITLIST");

		String projectId = helper.getParam("NMLTXT_PROJECT_ID");
		String projectRole = helper.getParam("NMLTXT_MY_ROLE");
		//画面跳转（回到我的任务列表）
		String targetScreenId = "DPGL210SUBLIST";
		KeyObj targetKeyObj = helper.getKeyObj(targetScreenId);
		targetKeyObj.put("PROJECT_ROLE", projectRole == null ? "" : projectRole);
		targetKeyObj.put("PROJECT_ID", projectId == null ? "" : projectId);
		//targetKeyObj.put("NMLTXT_TASK_ID",helper.getParam("NMLTXT_TASK_ID"));
		helper.pageOpen(targetScreenId, targetKeyObj);
	}
}
