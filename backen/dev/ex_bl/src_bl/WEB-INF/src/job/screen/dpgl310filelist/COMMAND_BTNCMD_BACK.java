package job.screen.dpgl310filelist;

import biz.grandsight.ex.ui.ac.ShiroUserRole;
import biz.grandsight.ex.ui.ac.util.SessionHelper;
import jakarta.enterprise.context.RequestScoped;

import com.yse.gb.gf.front.CommandException;
import com.yse.gb.gf.front.CommandHelper;
import com.yse.gb.gf.front.InputException;
import com.yse.gb.gf.front.KeyObj;

import java.util.Map;

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

		ShiroUserRole shiroUserRole = SessionHelper.getShiroUserRole(helper);
		Map<String, String> roles = (shiroUserRole == null) ? null : shiroUserRole.getRoles();

		System.out.println("CURRENT SHIRO ROLES = "
				+ (shiroUserRole == null ? "null" : shiroUserRole.getRoleIdsString()));


		// 框架 Helper#getPreScreenId()
		String preScreenId = "DPGL200";// 1) 从 Shiro 会话取当前用户的角色串

		// 按 Map 的 key 判断角色，决定跳转
		if (roles != null && roles.containsKey("LEADER")) {
			preScreenId = "DPGL100";
		}

		String targetScreenId = preScreenId.trim();
		System.out.println(">>>GO BACK TO : " + targetScreenId);

		KeyObj targetKeyObj = helper.getKeyObj(targetScreenId);
		helper.pageOpen(targetScreenId, targetKeyObj);
	}
}
