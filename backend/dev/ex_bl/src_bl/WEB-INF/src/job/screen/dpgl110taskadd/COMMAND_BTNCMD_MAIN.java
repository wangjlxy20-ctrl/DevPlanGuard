package job.screen.dpgl110taskadd;

import biz.grandsight.ex.ui.ac.ShiroUserRole;
import biz.grandsight.ex.ui.ac.util.SessionHelper;
import jakarta.enterprise.context.RequestScoped;

import com.yse.gb.gf.front.CommandException;
import com.yse.gb.gf.front.CommandHelper;
import com.yse.gb.gf.front.InputException;
import com.yse.gb.gf.front.KeyObj;


@RequestScoped
public class COMMAND_BTNCMD_MAIN extends com.yse.gb.gf.bld.front.BLDCommandImpl {

	
	private static final String SCREEN_THIS = "DPGL110TASKADD";

	
	private static final String SCREEN_DPGL100 = "DPGL100";

	
	private static final String SCREEN_ACM000 = "ACM000";

	
	private static final String SCREEN_DPGL200 = "DPGL200";

	
	@Override
	protected void perform(final CommandHelper helper, final String nextScreenId, final KeyObj keyObj)
			throws InputException, CommandException, Throwable {

		ShiroUserRole shiroUserRole = SessionHelper.getShiroUserRole(helper);
		String roleIds = (shiroUserRole == null) ? null : shiroUserRole.getRoleIdsString();

		String targetScreenId = getDashboardScreenId(roleIds);
		System.out.println(">>>DPGL110TASKADD COMMAND_BTNCMD_MAIN : roleIds = " + roleIds
				+ " -> " + targetScreenId);

		if (SCREEN_THIS.equals(targetScreenId)) {
			// 目标就是本画面：不跳转（避免自己重开自己）
			return;
		}

		//画面跳转（沿用目标画面自己的 keyObj，不 new 空 KeyObj）
		KeyObj targetKeyObj = helper.getKeyObj(targetScreenId);
		helper.pageOpen(targetScreenId, targetKeyObj);
	}

	
	private String getDashboardScreenId(final String roleIds) {
		if (hasRole(roleIds, "LEADER")) {
			return SCREEN_DPGL100;
		}
		if (hasRole(roleIds, "ADMIN")) {
			return SCREEN_ACM000;
		}
		return SCREEN_DPGL200;
	}

	
	private boolean hasRole(final String roleIds, final String role) {
		if (roleIds == null) {
			return false;
		}
		String[] tokens = roleIds.split("[,;|\\s]+");
		for (int i = 0; i < tokens.length; i++) {
			if (role.equalsIgnoreCase(tokens[i].trim())) {
				return true;
			}
		}
		return false;
	}
}
