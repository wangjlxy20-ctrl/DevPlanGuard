package job.screen.dq100au;

import com.yse.gb.gf.front.*;
import com.yse.gb.gf.front.html.Span;
import com.yse.gb.util.FormatDate;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import job.entity.UserInfo;
import job.service.UserService;

import java.util.Date;
import java.util.List;



@RequestScoped
public class DQ100AUCreator extends Creator {
	@Inject
	private UserService userService;

	
	@Override
	protected void createValueBean(final KeyObj keyObj, final ValueBean valueBean)
			throws CreatorException, Throwable {
		//Last Screen dq100a Delete
		// Write the code here
		//TIME
		long nowtime = System.currentTimeMillis();
		Date nowDate = new Date(nowtime);
		String strNowDate = FormatDate.get_YYYYMMDDhhmmss(nowDate);
		Span spanNowDate = new Span("NMLLBL_NOW_DATE", strNowDate);
		valueBean.add(spanNowDate);

		//
		String modifyID = keyObj.get("MODIFY_ID");



		if(modifyID != null && modifyID.length() > 0){
			//存的是我要修改的信息
			//条件组装成实体：画面里的 MODIFY_ID 是字符串，实体里是 Integer
			UserInfo condition = new UserInfo();
			condition.setUserId(parseUserId(modifyID));
			List<UserInfo> userInfoList = userService.searchUsers(condition);
			System.out.println("SEARCH RESULT : " + userInfoList);
			//
			if (userInfoList != null  && !userInfoList.isEmpty()){
				UserInfo u = userInfoList.get(0);//取第一条
				keyObj.put("NMLLBL_DISPLAY_INFO",String.valueOf(u.getUserName()));
				//keyObj.put("NMLDLD_PERMISSION",String.valueOf(u.getPermission()));
			}else {
				System.out.println("userInfoList is null");
			}



		}


		new FreesiaScreenSupporter().setDefaultValueBean(keyObj,valueBean);
	}

	
	private static Integer parseUserId(String userId) {
		if (userId == null || userId.trim().isEmpty()) {
			return null;
		}
		try {
			return Integer.valueOf(userId.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
