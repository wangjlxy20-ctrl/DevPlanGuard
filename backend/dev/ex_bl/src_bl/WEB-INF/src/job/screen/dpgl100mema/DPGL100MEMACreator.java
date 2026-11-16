package job.screen.dpgl100mema;

import com.yse.gb.gf.front.*;
import com.yse.gb.gf.front.html.*;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import job.entity.ProjectMember;
import job.entity.UserInfo;
import job.service.MemberService;
import java.util.List;



@RequestScoped
public class DPGL100MEMACreator extends Creator {
	

	@Inject
	private HttpServletRequest request;

	@Inject
	private MemberService memberService;

	@Override
	protected void createValueBean(final KeyObj keyObj, final ValueBean valueBean)
			throws CreatorException, Throwable {
		// Write the code here
		System.out.println("====DPGL100MEMA Creator executed====");

		//project_id: after searching, take MEMA_PROJECT_ID;
		String projectId = keyObj.get("MEMA_PROJECT_ID");
		if (projectId == null || projectId.trim().isEmpty()) {
			//从 url路径中取;
			projectId = request.getParameter("PROJECT_ID");
			//getHelper().get_params().get();
			System.out.println("FIRST TIME ENTERING DPGLMEMEACreator, and projectId is : " +  projectId);
		}

		String keyword = keyObj.get("MEMA_KEYWORD");
		if (keyword == null) {
			keyword = "";
		}


		Input hiddenProject = new Input("NMLTXT_PROJECT_ID");
		hiddenProject.setValue(projectId == null ? "" : projectId);
		System.out.println("Hidden project NMLTXT_PROJECT_ID HAS GET THE project_id :" + projectId);
		valueBean.add(hiddenProject);


		Input searchInput = new Input("NMLTXT_SEARCH_USER_NAME");
		searchInput.setValue(keyword);
		valueBean.add(searchInput);


		Table dataTable = new Table("TABLE_MEMBER_DATA");


		//Check candidate users (fuzzy/all, excluding current project members)
		//检索条件组装成实体：项目范围（project_id）+ 关键字
		ProjectMember condition = new ProjectMember();
		condition.setProjectId(Integer.parseInt(projectId.trim()));
		List<UserInfo> users = memberService.searchAddableUsers(condition, keyword);
		System.out.println("CANDIDATE USER COUNT = " + users.size());

		for (UserInfo u : users) {
			String userId = String.valueOf(u.getUserId());
			String username = String.valueOf(u.getUserName());
			Row row = new Row();

			//Checkbox: value = user_id
			// after checking it getRowParams can retrieve it
			CheckBox chk = new CheckBox("NMLCHK_SELECT_ROW", false);
			chk.setValue(userId);
			row.add(chk);

			row.add(new Span("NMLLBL_USER_ID", userId));
			row.add(new Span("NMLLBL_DISPLAY_USERNAME", username));

			//Project-level roles: one dropdown per row
			//(options are hard-coded in the HTML template, default is DEVELOPER)
			//Create a project role dropdown for current members
			Select roleSelect = new Select("NMLDLD_INFO");

			roleSelect.add(new Option("DEVELOPER", "DEVELOPER"));
			roleSelect.add(new Option("TESTER", "TESTER"));
			roleSelect.add(new Option("TECHNICALWRITER","TechnicalWriter"));

			row.add(roleSelect);

			dataTable.add(row);
		}

		valueBean.add(dataTable);
	}
}
