package job.screen.dq100au;

import com.yse.gb.gf.front.*;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import job.entity.UserInfo;
import job.service.UserService;

import java.util.Objects;


@RequestScoped
public class COMMAND_BTNCMD_MODIFY extends com.yse.gb.gf.bld.front.BLDCommandImpl {

	@Inject
	private UserService userService;

	


	@Override
	protected void perform(final CommandHelper helper, final String nextScreenId, final KeyObj keyObj)
			throws InputException, CommandException, Throwable {
		//
		String username = helper.getPreValue("NMLLBL_DISPLAY_INFO");
		System.out.println("MODIFIED USERNAME IS : " + username);


		//get new password and permission
		String newPassword = helper.getParam("NMLTXT_PASSWORD");
		System.out.println("THE NEW PASSWORD IS: " + newPassword);

		String newPasswordAgain = helper.getParam("NMLTXT_PASSWORD_AGAIN");
		System.out.println("The NEW PASSWORD AGAIN IS : " + newPasswordAgain);

		String permission = helper.getParam("NMLDLD_PERMISSION");

		if (!Objects.equals(newPassword, newPasswordAgain)){
			throw new CommandException(new UserMessage("The passwords you entered don't match,please try again"));
		}

		//修改的数据组装成实体（用户名取自画面上显示的原值）
		UserInfo user = new UserInfo();
		user.setUserName(username);
		user.setPassWord(newPassword);
		user.setPermission(permission);
		userService.updateUser(user);

		System.out.println("UPDATE SUCCESSFUL");

		//画面跳转
		String targetScreenId = "DQ100A";
		KeyObj targetKeyObj = helper.getKeyObj(targetScreenId);
		targetKeyObj.put("MODIFY_USERNAME",username);
		targetKeyObj.put("USERNAME_SEARCH","");
		targetKeyObj.put("PERMISSION_SEARCH","");
		helper.pageOpen(targetScreenId, targetKeyObj);
	}
}
