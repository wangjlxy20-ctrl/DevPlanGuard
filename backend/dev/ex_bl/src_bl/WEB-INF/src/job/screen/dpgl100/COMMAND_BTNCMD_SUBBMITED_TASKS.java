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
public class COMMAND_BTNCMD_SUBBMITED_TASKS extends com.yse.gb.gf.bld.front.BLDCommandImpl {
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
		String targetScreenId = "DPGL210SUBLIST";
		KeyObj targetKeyObj = helper.getKeyObj(targetScreenId);
		helper.pageOpen(targetScreenId, targetKeyObj);
	}
}
