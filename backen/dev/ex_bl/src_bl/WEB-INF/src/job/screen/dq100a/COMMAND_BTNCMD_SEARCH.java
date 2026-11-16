package job.screen.dq100a;

import jakarta.enterprise.context.RequestScoped;

import com.yse.gb.gf.front.CommandException;
import com.yse.gb.gf.front.CommandHelper;
import com.yse.gb.gf.front.InputException;
import com.yse.gb.gf.front.KeyObj;


@RequestScoped
public class COMMAND_BTNCMD_SEARCH extends com.yse.gb.gf.bld.front.BLDCommandImpl {

	

	@Override
	protected void perform(final CommandHelper helper, final String nextScreenId, final KeyObj keyObj)
			throws InputException, CommandException, Throwable {

		String usernameSearch = helper.getParam("NMLTXT_USERNAME");
		String permissionSearch = helper.getParam("NMLDLD_PERMISSION");

		//画面跳转（带回检索条件）
		String targetScreenId = "DQ100A";
		KeyObj targetKeyObj = helper.getKeyObj(targetScreenId);
		targetKeyObj.put("USERNAME_SEARCH",usernameSearch);
		targetKeyObj.put("PERMISSION_SEARCH",permissionSearch);
		helper.pageOpen(targetScreenId, targetKeyObj);
	}

}
