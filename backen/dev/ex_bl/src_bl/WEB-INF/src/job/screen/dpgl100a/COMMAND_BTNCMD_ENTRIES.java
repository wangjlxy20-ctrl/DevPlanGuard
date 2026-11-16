package job.screen.dpgl100a;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import com.yse.gb.gf.front.CommandException;
import com.yse.gb.gf.front.CommandHelper;
import com.yse.gb.gf.front.InputException;
import com.yse.gb.gf.front.KeyObj;
import job.entity.Project;
import job.entity.ProjectMember;
import job.entity.UserInfo;
import job.service.ProjectService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


@RequestScoped
public class COMMAND_BTNCMD_ENTRIES extends com.yse.gb.gf.bld.front.BLDCommandImpl {

	@Inject
	private ProjectService projectService;

	
	@Override
	protected void perform(final CommandHelper helper, final String nextScreenId, final KeyObj keyObj)
			throws InputException, CommandException, Throwable {
		//GET DATE FROM FORM
		String projectCodeId = helper.getPreValue("NMLLBL_DISPLAY_INFO_PROJECT_CODE");
		System.out.println("DPGL100A HAS GET THE NMLLBL_DISPLAY_INFO_PROJECT_CODE : " + projectCodeId);

		String projectName = helper.getParam("NMLTXT_INPUT_INFO_PROJECT_NAME");
		String projectManager = helper.getParam("NMLDLD_MANAGER");
		System.out.println("DPGL100A HAS GET THE NMLDLD_MANAGER IS : " + projectManager);

		//
		//projectRoleMapper projectRoleMapper = new projectRoleMapper();
		//String projectRole = projectRoleMapper.projectRoleCheck(Integer.parseInt(projectManager));
		//ADD THE ROLE IN PROJECT_MEMBER;
        //addProjectMemberMapper addProjectMemberMapper = new addProjectMemberMapper();
		//addProjectMemberMapper.addProjectMembers(,projectManager,projectRole);

		String priority = helper.getParam("NMLDLD_INFO_PRIORITY");
		//THE PROJECT Status IS SET TO 'PLANNING' BY DEFAULT
		String startDateStr = helper.getParam("LNKINP_PLAN_DATE");
		String plannedEndDateStr = helper.getParam("LNKINP_PLAN_END_DATE");
		String createdBy = helper.getPreValue("NMLLBL_CREATOR_NAME");
		System.out.println("DPGL100A HAS GET THE NMLLBL_CREATOR_NAME : " + createdBy);
		//GET CREATOR ID THROUGH CREATOR NAME
		UserInfo creatorKey = new UserInfo();
		creatorKey.setUserName(createdBy);
		int createdById = projectService.getCreatorId(creatorKey);
		String description = helper.getParam("NMLTXA_DESCRIPTION");

		//项目数据组装成实体（leader_id 空值/非数字视为空，日期按原格式集解析）
		Project project = new Project();
		project.setProjectCode(projectCodeId);
		project.setProjectName(projectName);
		project.setProjectDescription(description);
		project.setLeaderId(parseId(projectManager));
		project.setStartDate(parseDate(startDateStr));
		project.setPlannedEndDate(parseDate(plannedEndDateStr));
		project.setPriority(priority);
		project.setCreatedBy(createdById);
		projectService.createProject(project);
		System.out.println("ADD NEW PROJECT SUCCESSFUL");

		System.out.println("THEN OPERATE THE PROJECT_MEMBER TABLE");

		//ADD THE ROLE IN PROJECT_MEMBER;
		Project codeKey = new Project();
		codeKey.setProjectCode(projectCodeId);
		int newProjectId = projectService.getProjectIdByCode(codeKey);

		//projectRoleMapper projectRoleMapper = new projectRoleMapper();
		//String projectRole = projectRoleMapper.projectRoleCheck(Integer.parseInt(projectManager));
		ProjectMember leadMember = new ProjectMember();
		leadMember.setProjectId(newProjectId);
		leadMember.setUserId(Integer.parseInt(projectManager.trim()));
		leadMember.setProjectRole("LEAD");
		projectService.addProjectMember(leadMember);

		//画面跳转
		String targetScreenId = "DPGL100";
		KeyObj targetKeyObj = helper.getKeyObj(targetScreenId);
		helper.pageOpen(targetScreenId, targetKeyObj);
	}

	
	private static Integer parseId(String value) {
		if (value == null || value.trim().isEmpty()) {
			return null;
		}
		try {
			return Integer.valueOf(value.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	
	private static Date parseDate(String value) throws ParseException {
		if (value == null || value.trim().isEmpty()) {
			return null;
		}
		String text = value.trim();
		String[] patterns = {"yyyy-MM-dd", "yyyy/MM/dd", "yyyy-MM-dd HH:mm:ss", "yyyy/MM/dd HH:mm:ss"};
		for (String pattern : patterns) {
			SimpleDateFormat format = new SimpleDateFormat(pattern);
			format.setLenient(false);
			try {
				return format.parse(text);
			} catch (ParseException ignored) {
				// 换下一种格式继续尝试
			}
		}
		throw new ParseException("Unsupported date format: " + value, 0);
	}
}
