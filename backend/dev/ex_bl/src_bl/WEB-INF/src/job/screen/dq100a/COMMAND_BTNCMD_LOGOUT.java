package job.screen.dq100a;

import jakarta.enterprise.context.RequestScoped;

import org.apache.shiro.SecurityUtils;
import biz.grandsight.ex.ui.ac.util.SubjectHelper;

import com.yse.gb.gf.front.CommandException;
import com.yse.gb.gf.front.CommandHelper;
import com.yse.gb.gf.front.InputException;
import com.yse.gb.gf.front.KeyObj;


@RequestScoped
public class COMMAND_BTNCMD_LOGOUT extends com.yse.gb.gf.bld.front.BLDCommandImpl {
	

	@Override
	protected void perform(final CommandHelper helper, final String nextScreenId, final KeyObj keyObj)
			throws InputException, CommandException, Throwable {

		// 框架登出（Shiro Access Control）
		SubjectHelper.logout(helper, SecurityUtils.getSubject());

		//画面跳转（登出）
		String targetScreenId = "ACLOGIN";
		KeyObj targetKeyObj = helper.getKeyObj(targetScreenId);
		helper.pageOpen(targetScreenId, targetKeyObj);
	}
}
