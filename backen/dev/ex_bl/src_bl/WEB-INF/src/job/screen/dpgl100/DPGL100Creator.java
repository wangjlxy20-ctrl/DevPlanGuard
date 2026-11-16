package job.screen.dpgl100;

import biz.grandsight.ex.ui.ac.ShiroUserRole;
import biz.grandsight.ex.ui.ac.util.SessionHelper;
import com.yse.gb.gf.front.html.Input;
import com.yse.gb.gf.front.html.Row;
import com.yse.gb.gf.front.html.Span;
import com.yse.gb.gf.front.html.Table;
import com.yse.gb.util.FormatDate;
import jakarta.enterprise.context.RequestScoped;
import com.yse.gb.gf.front.Creator;
import com.yse.gb.gf.front.CreatorException;
import com.yse.gb.gf.front.KeyObj;
import com.yse.gb.gf.front.ValueBean;
import jakarta.inject.Inject;
import job.entity.Project;
import job.entity.ProjectMember;
import job.mapper.project.ProjectMapper;
import job.service.ProjectService;

import java.util.Date;
import java.util.List;
import java.util.Map;



@RequestScoped
public class DPGL100Creator extends Creator {

	@Inject
	private ProjectService projectService;


	@Override
	protected void createValueBean(final KeyObj keyObj, final ValueBean valueBean)
			throws CreatorException, Throwable {
		// Write the code here
		System.out.println("====DQCreator100 being executed====");
		//TIME
		long nowtime = System.currentTimeMillis();
		Date nowDate = new Date(nowtime);
		String strNowDate = FormatDate.get_YYYYMMDDhhmmss(nowDate);
		Span spanNowDate = new Span("NMLLBL_NOW_DATE", strNowDate);
		valueBean.add(spanNowDate);

		//GET THE DATE FROM ACLOGIN
		ShiroUserRole shiroUserRole = SessionHelper.getShiroUserRole(this);
		String user_id     = (shiroUserRole == null) ? null : shiroUserRole.getUserId();
		System.out.println("DPGL100 GETS user_id FROM SESSION SUCCESSFULLY : " + user_id );
		String username   = (shiroUserRole == null) ? null : shiroUserRole.getUserName();
		String permission = (shiroUserRole == null) ? null : shiroUserRole.getRoleIdsString();

		System.out.println("=== ShiroUserRole dump ===");
		System.out.println("getUserId()        = " + shiroUserRole.getUserId());
		System.out.println("getUserName()      = " + shiroUserRole.getUserName());
		System.out.println("getRoleIdsString() = " + shiroUserRole.getRoleIdsString());

		if (user_id == null || user_id.trim().isEmpty()) {
			valueBean.add(new Span("NMLLBL_DISPLAY_INFO_USERNAME", "No login user. Please sign in again."));
			return;
		}
		Integer sessionUserId = Integer.parseInt(user_id.trim());

		ProjectMember roleKey = new ProjectMember();
		roleKey.setUserId(sessionUserId);
		String projectRole = projectService.getProjectRole(roleKey);


		valueBean.add(new Span("NMLLBL_DISPLAY_INFO_USERNAME",username));
		valueBean.add(new Span("NMLLBL_DISPLAY_INFO_PERMISSION",permission));
		valueBean.add(new Span("NMLLBL_DISPLAY_INFO_ROLE",projectRole));
		//get the input info
		//String projectCodeId = ;

		//GET PROJECT KEYWORD TO SEARCH Fuzzy search
		String keyword = keyObj.get("PROJECT_CODE_ID");
		System.out.println("SEARCH KEYWORD IN CREATOR : " + keyword);

		//Refill the search box and keep what the user just typed
		Input searchInput = new Input("NMLTXT_INPUT_INFO");
		searchInput.setValue(keyword == null ? "" : keyword);
		valueBean.add(searchInput);

		//检索条件组装成实体：created_by 限定当前用户，关键字仍是标量
		Project condition = new Project();
		condition.setCreatedBy(sessionUserId);
		List<Project> projectList = projectService.searchProjects(condition,keyword);

		Table projectTable = new Table("TABLE_PROJECT");
		ProjectMapper projectMapper = new ProjectMapper();
		if (projectList != null){
			for (Project p : projectList){
				Row row = new Row();
				String projectCode = String.valueOf(p.getProjectCode());
				String projectId = String.valueOf(p.getProjectId());
				Input link = new Input("LNKMOV_DETAILS");
				link.setValue(projectCode);
				row.add(link);
				row.add(new Span("NMLLBL_PROJECT_NAME",String.valueOf(p.getProjectName())));

				// 该项目的需求数 / 在职成员数
				Map<String, Object> count = projectMapper.countReqAndMember(projectId);
				String reqCount = String.valueOf(count == null ? 0 : count.get("req_count"));
				String memberCount = String.valueOf(count == null ? 0 : count.get("member_count"));

				// 隐藏 project_id：两个链接点击时都从它取跳转依据
				Input hiddenPid = new Input("NMLTXT_PROJECT_ID");
				hiddenPid.setValue(projectId);
				row.add(hiddenPid);

				// 链接显示需求数量，点击仍携带 project_id
				Input linkReq = new Input("LNKMOV_REQUIREMENT");
				linkReq.setValue(reqCount);
				row.add(linkReq);

				// 链接显示成员数量
				Input linkMem = new Input("LNKMOV_MEMBER");
				linkMem.setValue(memberCount);
				row.add(linkMem);

				projectTable.add(row);

			}
		}
		valueBean.add(projectTable);

	}
}

