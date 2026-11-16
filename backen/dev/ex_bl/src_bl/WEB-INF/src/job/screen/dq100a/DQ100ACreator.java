package job.screen.dq100a;

import com.yse.gb.gf.front.html.*;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import com.yse.gb.gf.front.Creator;
import com.yse.gb.gf.front.CreatorException;
import com.yse.gb.gf.front.KeyObj;
import com.yse.gb.gf.front.ValueBean;
import job.entity.UserInfo;
import job.service.UserService;

import java.util.List;



@RequestScoped
public class DQ100ACreator extends Creator {

	@Inject
	private UserService userService;

	
	@Override
	protected void createValueBean(final KeyObj keyObj, final ValueBean valueBean)
			throws CreatorException, Throwable {
		// Write the code here
		String usernameSearch = keyObj.get("USERNAME_SEARCH");
		String permissionSearch = keyObj.get("PERMISSION_SEARCH");

		if (usernameSearch != null || permissionSearch != null) {

			Table usersSearchedTable = new Table("TABLE_USER_DATA");

			//检索条件组装成实体（空串 = 该条件不生效）
			UserInfo condition = new UserInfo();
			condition.setUserName(usernameSearch);
			condition.setPermission(permissionSearch);
			List<UserInfo> usersList = userService.searchUsers(condition);

			String modifyId = keyObj.get("MODIFY_ID");
			String modifyUsername = keyObj.get("MODIFY_USERNAME");

			System.out.println("Search Results : " + usersList);

			for (int i = 0; i < usersList.size(); i++) {
				UserInfo user = usersList.get(i);

				Row row = new Row();
				CheckBox checkBox = new CheckBox("NMLCHK_SELECT_ROW", false);
				checkBox.setValue(String.valueOf(user.getUserId()));
				row.add(checkBox);

				Span spanUsername = new Span("NMLLBL_DQ_USERNAME", String.valueOf(user.getUserName()));
				Span spanPermission = new Span("NMLLBL_DQ_PERMISSION", String.valueOf(user.getPermission()));
				Span spanTime = new Span("NMLLBL_DQ_TIME", String.valueOf(user.getCreatedAt()));

				if (modifyUsername != null && modifyUsername.equals(String.valueOf(user.getUserName()))){
					spanUsername.setColor("red");
					spanPermission.setColor("red");
				}

				row.add(spanUsername);
				row.add(spanPermission);
				row.add(spanTime);

				usersSearchedTable.add(row);
			}
			valueBean.add(usersSearchedTable);
		}
	}


}
