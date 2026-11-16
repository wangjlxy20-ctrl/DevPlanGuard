package job.screen.dpgl100a;

import com.yse.gb.gf.front.html.Option;
import com.yse.gb.gf.front.html.Select;
import com.yse.gb.gf.front.html.Span;
import com.yse.gb.util.FormatDate;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import com.yse.gb.gf.front.Creator;
import com.yse.gb.gf.front.CreatorException;
import com.yse.gb.gf.front.KeyObj;
import com.yse.gb.gf.front.ValueBean;
import job.entity.UserInfo;
import job.service.UserService;

import java.util.Date;
import java.util.List;



@RequestScoped
public class DPGL100ACreator extends Creator {

	@Inject
	private UserService userService;

	
	@Override
	protected void createValueBean(final KeyObj keyObj, final ValueBean valueBean)
			throws CreatorException, Throwable {
		// Write the code here
		//TIME
		long nowtime = System.currentTimeMillis();
		Date nowDate = new Date(nowtime);
		String strNowDate = FormatDate.get_YYYYMMDDhhmmss(nowDate);
		Span spanNowDate = new Span("NMLLBL_NOW_DATE", strNowDate);
		valueBean.add(spanNowDate);
		//CREATED AT TIME
		valueBean.add(new Span("NMLLBL_NOW_DATE_CREATED_AT",FormatDate.get_YYYYMMDDhhmmss(new Date(System.currentTimeMillis()))));
		//PROJECT CODE ID
		valueBean.add((new Span("NMLLBL_DISPLAY_INFO_PROJECT_CODE",keyObj.get("PROJECT_CODE_ID"))));
		//CREATED BY
		valueBean.add((new Span("NMLLBL_CREATOR_NAME",keyObj.get("CREATOR_NAME"))));
		//Dropdown list
		Select managerList = new Select("NMLDLD_MANAGER");
		managerList.add(new Option("","--select--"));
		//invoke Mapper to query（检索条件组装成实体：permission = 'MEMBER'）
		UserInfo condition = new UserInfo();
		condition.setPermission("MEMBER");
		List<UserInfo> usersList =  userService.searchUsers(condition);
		if (usersList != null){
			for (UserInfo u : usersList){
				String user_id = String.valueOf(u.getUserId());
				String userName = String.valueOf(u.getUserName());
				managerList.add(new Option(user_id,userName));
			}
		}
		valueBean.add(managerList);



	}
}
