package job.screen.dq100a;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import com.yse.gb.gf.front.CommandException;
import com.yse.gb.gf.front.CommandHelper;
import com.yse.gb.gf.front.InputException;
import com.yse.gb.gf.front.KeyObj;
import job.service.UserService;

import java.util.ArrayList;
import java.util.List;


@RequestScoped
public class COMMAND_BTNCMD_DELETE extends com.yse.gb.gf.bld.front.BLDCommandImpl {

	@Inject
	private UserService userService;

	
	@Override
	protected void perform(final CommandHelper helper, final String nextScreenId, final KeyObj keyObj)
			throws InputException, CommandException, Throwable {

		//Save the original search ceriteria
		String usernameSearch = helper.getParam("NMLTXT_USERNAME");
		String permissionSearch = helper.getParam("NMLDLD_PERMISSION");

		String[] rowValues = helper.getRowParams("NMLCHK_SELECT_ROW");
		List<String> user_idsList = new ArrayList<>();

		if (rowValues != null) {
			for (int i = 0; i < rowValues.length; i++) {
				if (rowValues[i] != null && !rowValues[i].isEmpty()) {
					user_idsList.add(rowValues[i]);
				}
			}
		}

		if (user_idsList.isEmpty()) {
			return;
		}

		String[] user_idsS = user_idsList.toArray(new String[0]);
		userService.deleteUsers(user_idsS);

		System.out.println("The ID got : " + user_idsS);

		//画面跳转（ECHO BACK THE ORIGINAL QUERY CONDITIONS）
		String targetScreenId = "DQ100A";
		KeyObj targetKeyObj = helper.getKeyObj(targetScreenId);
		targetKeyObj.put("USERNAME_SEARCH", usernameSearch);
		targetKeyObj.put("PERMISSION_SEARCH", permissionSearch);
		helper.pageOpen(targetScreenId, targetKeyObj);
	}
}
