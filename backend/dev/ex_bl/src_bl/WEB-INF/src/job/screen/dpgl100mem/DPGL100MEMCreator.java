package job.screen.dpgl100mem;

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
import job.service.MemberService;
import job.service.ProjectService;
import java.util.Date;
import java.util.List;



@RequestScoped
public class DPGL100MEMCreator extends Creator {

	@Inject
	private ProjectService projectService;

	@Inject
	private MemberService memberService;

	
	@Override
	protected void createValueBean(final KeyObj keyObj, final ValueBean valueBean)
			throws CreatorException, Throwable {
		// Write the code here
		System.out.println("====DPGL100MEM Creator executed====");

		// SYS TIME
		String now = FormatDate.get_YYYYMMDDhhmmss(new Date(System.currentTimeMillis()));
		valueBean.add(new Span("NMLLBL_NOW_DATE", now));

		//GET PROJECT ID FROM COMMAND_LNKMOV_MEMBER IN DEGL100
		String projectId = keyObj.get("PROJECT_ID");
		System.out.println("DPGL100MEM HAS GET THE PROJECT_ID : " + projectId);

		Input hiddenProject = new Input("NMLTXT_PROJECT_ID");
		hiddenProject.setValue(projectId == null ? "" : projectId);
		valueBean.add(hiddenProject);

		Table memberTable = new Table("TABLE_MEMBER");

		// project info
		Project projectKey = new Project();
		projectKey.setProjectId(Integer.parseInt(projectId.trim()));
		Project project = projectService.getProject(projectKey);
		String	code = String.valueOf(project.getProjectCode());
		String	pname = String.valueOf(project.getProjectName());


		// member count
		ProjectMember memberCondition = new ProjectMember();
		memberCondition.setProjectId(Integer.parseInt(projectId.trim()));
		List<ProjectMember> membersList = memberService.searchMembers(memberCondition);

		for (ProjectMember member : membersList) {
			System.out.println(member);
		}

		//Fixed area: Project ID / PROJECT_Name / Number of members
		valueBean.add(new Span("NMLLBL_DISPLAY_INFO_PROJECT_ID", code));
		valueBean.add(new Span("NMLLBL_DISPLAY_INFO_PROJECT_NAME", pname));
		valueBean.add(new Span("NMLLBL_DISPLAY_INFO_MEMBER_COUNT", String.valueOf(membersList.size())));

		// List: one member per line
		for (ProjectMember m : membersList) {
			Row row = new Row();

			row.add(new Span("NMLLBL_MEMBER_NAME", String.valueOf(m.getUsername())));
			row.add(new Span("NMLLBL_MEMBER_ROLE", String.valueOf(m.getProjectRole())));
			row.add(new Span("NMLLBL_ASSIGNED_TASK_COUNT", String.valueOf(m.getAssignedTasks())));
			row.add(new Span("NMLLBL_COMPLETED_TASK_COUNT", String.valueOf(m.getCompletedTasks())));

			memberTable.add(row);
		}

		valueBean.add(memberTable);
	}


}
