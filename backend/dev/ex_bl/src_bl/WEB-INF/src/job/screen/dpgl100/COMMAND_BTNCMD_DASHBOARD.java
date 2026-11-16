package job.screen.dpgl100;

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
public class COMMAND_BTNCMD_DASHBOARD extends com.yse.gb.gf.bld.front.BLDCommandImpl {
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

//		String projectCode = helper.getLinkValue();
//		System.out.println("LINK_VALUE = " + projectCode);


		//画面跳转（项目详情）
		String targetScreenId = "DPGL_DYN_DASHBOARD";
		KeyObj targetKeyObj = helper.getKeyObj(targetScreenId);
//		targetKeyObj.put("PROJECT_CODE_ID",projectCode);
		helper.pageOpen(targetScreenId, targetKeyObj);
	}
}
