package job.screen.dpgl110mng;

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
import com.yse.gb.gf.front.UserMessage;
import com.yse.gb.gf.front.ValueBean;
import jakarta.inject.Inject;
import job.entity.ProjectMember;
import job.entity.Requirement;
import job.service.ProjectService;
import job.service.RequirementService;

import java.util.*;



@RequestScoped
public class DPGL110MNGCreator extends Creator {

	@Inject
	private RequirementService requirementService;

	@Inject
	private ProjectService projectService;

	
	@Override
	protected void createValueBean(final KeyObj keyObj, final ValueBean valueBean)
			throws CreatorException, Throwable {
		// Write the code here
		System.out.println("====DPGL110MNG Creator executed====");

		// SYS TIME
		String now = FormatDate.get_YYYYMMDDhhmmss(new Date(System.currentTimeMillis()));
		valueBean.add(new Span("NMLLBL_NOW_DATE", now));

		// LOGIN INFO
		ShiroUserRole shiroUserRole = SessionHelper.getShiroUserRole(this);
		String userId = (shiroUserRole == null) ? null : shiroUserRole.getUserId();
		String username = (shiroUserRole == null) ? null : shiroUserRole.getUserName();

		Table reqTable = new Table("TABLE_REQUIREMENT");

		// If not logged in, show a message
		if (userId == null || userId.trim().length() == 0) {
			valueBean.add(new Span("NMLLBL_HELLO_USERNAME", "Please login first."));
			valueBean.add(reqTable);
			return;
		}

		Integer loginUserId = toIdOrNull(userId);
		if (loginUserId == null) {
			throw new CreatorException(new UserMessage("Can't get the logged-in user info"));
		}

		Integer checkProjectId = toIdOrNull(keyObj.get("PROJECT_ID"));
		if (checkProjectId == null) {
			throw new CreatorException(new UserMessage("Can't get the current project_id, please re-enter from the project list."));
		}

		ProjectMember leaderCondition = new ProjectMember();
		leaderCondition.setProjectId(checkProjectId);
		leaderCondition.setUserId(loginUserId);
		projectService.checkProjectLeader(leaderCondition);

		List<Requirement> list = requirementService.getMyLeadRequirements(userId, String.valueOf(checkProjectId));
		if (list == null) {
			list = new ArrayList<>();
		}
		System.out.println("MY REQUIREMENT COUNT = " + list.size());

		// Fixed area
		valueBean.add(new Span("NMLLBL_HELLO_USERNAME", username == null ? "" : username));
		// Fixed area
		String projectRole = keyObj.get("PROJECT_ROLE");
		System.out.println(">>>>DPGL110MNG HAVE GET THE PROJECT_ROLE FROM @#DEGL200#@ :" + projectRole );
		valueBean.add(new Span("NMLLBL_MY_ROLE", projectRole));

		//进入其他页面返回时能显示role
		Input hiddenProjectRole = new Input("NMLTXT_MY_ROLE");
		hiddenProjectRole.setValue(projectRole);
		valueBean.add(hiddenProjectRole);
		Input hiddenProjectId = new Input("NMLTXT_PROJECT_ID");
		hiddenProjectId.setValue(String.valueOf(checkProjectId));
		valueBean.add(hiddenProjectId);

		// fixed area：Count the number of projects (distinct project_id) and the number of requirements
		Set<String> projectIdSet = new HashSet<>();
		for (Requirement r : list) {
			Integer pid = r.getProjectId();
			if (pid != null) {
				projectIdSet.add(String.valueOf(pid));
			}
		}
		String stat = "YOU MANAGED " + projectIdSet.size() + " PROJECTS，WITH A TOTAL OF " + list.size() + " REQUIREMENTS";
		valueBean.add(new Span("NMLLBL_MY_STAT", stat));
		System.out.println("<<< DPGL110MNG: PROJECT ACCOUNT IS : " + projectIdSet.size());
		System.out.println("<<< DPGL110MNG: TOTAL REQUIREMENT ACCOUNT IS : " +list.size());

		// Array: One requirement per row, 5 columns
		for (Requirement r : list) {
			String requirementId = String.valueOf(r.getRequirementId());
			String requirementCode = String.valueOf(r.getRequirementCode());
			String requirementName = String.valueOf(r.getRequirementName());
			String projectName = String.valueOf(r.getProjectName());
			String priority = String.valueOf(r.getPriority());

			// Project due date（p.planned_end_date）
			String projectDue = String.valueOf(r.getProjectDue());
			if ("null".equals(projectDue)) {
				projectDue = "";
			}

			Row row = new Row();


			row.add(new Span("NMLLBL_PRIORITY", priority));

			row.add(new Span("NMLLBL_REQ_CODE", requirementCode));

			// Requirement name link
			// Hide requirement_id in the same cell
			Input hiddenRid = new Input("NMLTXT_TASK_REQ_ID");
			hiddenRid.setValue(requirementId);
			row.add(hiddenRid);

			Input reqLink = new Input("LNKMOV_REQUIREMENT");
			reqLink.setClassName("LINK_TEXT");
			reqLink.setValue(requirementName);
			row.add(reqLink);


			row.add(new Span("NMLLBL_PROJECT_NAME", projectName));
			// DUE DATE
			row.add(new Span("NMLLBL_PROJECT_DUE", projectDue));

			reqTable.add(row);
		}

		valueBean.add(reqTable);
	}

	
	private Integer toIdOrNull(String value) {
		if (value == null) {
			return null;
		}
		String v = value.trim();
		if (v.length() == 0 || "null".equals(v)) {
			return null;
		}
		try {
			return Integer.valueOf(v);
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
