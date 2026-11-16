package job.screen.dq100aa;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import com.yse.gb.gf.front.CommandException;
import com.yse.gb.gf.front.CommandHelper;
import com.yse.gb.gf.front.InputException;
import com.yse.gb.gf.front.KeyObj;
import job.entity.UserInfo;
import job.service.UserService;


@RequestScoped
public class COMMAND_BTNCMD_ADD_NEW extends com.yse.gb.gf.bld.front.BLDCommandImpl {

	@Inject
	private UserService userService;

	
	@Override
	protected void perform(final CommandHelper helper, final String nextScreenId, final KeyObj keyObj)
			throws InputException, CommandException, Throwable {

		String username = helper.getParam("NMLTXT_USERNAME");
		String password = helper.getParam("NMLTXT_PASSWORD");
		String permission = helper.getParam("NMLDLD_PERMISSION");

		//新增用户的数据组装成实体
		UserInfo user = new UserInfo();
		user.setUserName(username);
		user.setPassWord(password);
		user.setPermission(permission);
		userService.createUser(user);
		System.out.println("ADD A NEW USER SUCCESSFUL");

		//画面跳转（传入查询条件）
		String targetScreenId = "DQ100A";
		KeyObj targetKeyObj = helper.getKeyObj(targetScreenId);
		targetKeyObj.put("USERNAME_SEARCH","");
		targetKeyObj.put("PERMISSION_SEARCH","");
		helper.pageOpen(targetScreenId, targetKeyObj);
	}
}
