package job.screen.dpgl100re;

import com.yse.gb.gf.front.html.Input;
import com.yse.gb.gf.front.html.Row;
import com.yse.gb.gf.front.html.Span;
import com.yse.gb.gf.front.html.Table;
import com.yse.gb.util.FormatDate;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import com.yse.gb.gf.front.Creator;
import com.yse.gb.gf.front.CreatorException;
import com.yse.gb.gf.front.KeyObj;
import com.yse.gb.gf.front.ValueBean;
import job.entity.Project;
import job.entity.Requirement;
import job.service.RequirementService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;



@RequestScoped
public class DPGL100RECreator extends Creator {

	@Inject
	private RequirementService requirementService;


	@Override
	protected void createValueBean(final KeyObj keyObj, final ValueBean valueBean)
			throws CreatorException, Throwable {
		// Write the code here
		System.out.println("====DPGL100RE Creator executed====");

		// TIME
		long nowtime = System.currentTimeMillis();
		Date nowDate = new Date(nowtime);
		String strNowDate = FormatDate.get_YYYYMMDDhhmmss(nowDate);
		Span spanNowDate = new Span("NMLLBL_NOW_DATE", strNowDate);
		valueBean.add(spanNowDate);

		//get the requirement related project id.
		// GET FROM DPGL100_COMMAND_LNKMOV_REQUIREMENT
		String projectId = keyObj.get("PROJECT_ID");
		System.out.println("DPGL100RE HAS GET THE PROJECT_ID FROM DPGL100 COMMAND_LNKMOV_REQUIREMENT: " + projectId);

		// hidden fixed filed
		Input hiddenPid = new Input("NMLTXT_PROJECT_ID");
		hiddenPid.setValue(projectId == null ? "" : projectId);
		valueBean.add(hiddenPid);

		Table table = new Table("TABLE_REQUIREMENT");

		// Null handling
		if (projectId == null || projectId.trim().length() == 0) {
			valueBean.add(new Span("NMLLBL_DISPLAY_INFO",
					"No project context, please enter from the project list."));
			valueBean.add(new Input("LNKMOV_MANAGER"));
			valueBean.add(new Span("NMLLBL_TOTAL_REQ", "0"));
			valueBean.add(table);
			return;
		}

		// project info（条件实体：project_id 来自画面 hidden 域）
		Integer pid = Integer.parseInt(projectId.trim());
		Project projectCondition = new Project();
		projectCondition.setProjectId(pid);
		Project project = requirementService.getProject(projectCondition);
		String code = "";
		String pname = "";
		if (project != null) {
			code = String.valueOf(project.getProjectCode());
			pname = String.valueOf(project.getProjectName());
		}

		// req list（条件实体：同一 project_id）
		Requirement reqCondition = new Requirement();
		reqCondition.setProjectId(pid);
		List<Requirement> list = requirementService.searchRequirements(reqCondition);
		if (list == null) {
			list = new ArrayList<>();
		}

		// manager username : owner_name is joined from the project leader
		String managerName = "";
		if (project != null) {
			String owner = project.getOwnerName();
			managerName = owner == null ? "" : owner;
		}

		// fixed filed：project info
		valueBean.add(new Span("NMLLBL_DISPLAY_INFO",
				"Project : " + code + "  " + pname));

		// fixed area：unique manager ,click to send message
		Input manager = new Input("LNKMOV_MANAGER");
		manager.setValue(managerName);
		valueBean.add(manager);

		// total requirement count
		valueBean.add(new Span("NMLLBL_TOTAL_REQ", String.valueOf(list.size())));

		for (Requirement r : list) {
			Row row = new Row();

			String reqCode = String.valueOf(r.getRequirementCode());
			String status = String.valueOf(r.getStatus());
			String priority = String.valueOf(r.getPriority()).toUpperCase();

			// priority block：HIGH red , MEDIUM yellow , LOW green
			row.add(new Span("NMLLBL_PRIORITY_FLAG", priority));

			// 行内标识
			//service  getRowParams("NMLTXT_REQ_CODE") 按行下标取值
			Input hiddenCode = new Input("NMLTXT_REQ_CODE");
			hiddenCode.setValue(reqCode);
			row.add(hiddenCode);

			// req link：click to modify
			Input edit = new Input("LNKMOV_REQ_EDIT");
			edit.setValue(reqCode);
			row.add(edit);


			row.add(new Span("NMLLBL_REQ_NAME", String.valueOf(r.getRequirementName())));
			row.add(new Span("NMLLBL_PROJECT_NAME", String.valueOf(r.getProjectName())));
			row.add(new Span("NMLLBL_PLAN_START", fmtDate(r.getPlannedStartDate())));
			row.add(new Span("NMLLBL_PLAN_END", fmtDate(r.getPlannedEndDate())));
			row.add(new Span("NMLLBL_STATUS", status));

			//Start : ONLY 'PLANNED' CAN USE
			// 按钮组件每一行都要 add，不可用时用 setVisible(false) 隐藏；
			boolean canStart = "PLANNED".equals(status);
			Input start = new Input("BTNCMD_START");
			start.setValue("START");
			start.setVisible(canStart);
			row.add(start);
			row.add(new Span("NMLLBL_START_STATE", canStart ? "" : "-"));

			//Complete : 'IN_PROGRESS'&& DONE CAN USE
			Requirement statsCondition = new Requirement();
			statsCondition.setRequirementId(r.getRequirementId());
			boolean canComplete = "IN_PROGRESS".equals(status)
					&& allTasksDone(requirementService.getTaskStats(statsCondition));
			Input complete = new Input("BTNCMD_COMPLETE");
			complete.setValue("COMPLETE");
			complete.setVisible(canComplete);
			row.add(complete);
			row.add(new Span("NMLLBL_COMPLETE_STATE", canComplete ? "" : "-"));

			table.add(row);
		}

		valueBean.add(table);
	}

	//Task Completion Status Check:
	//At least one task, and all tasks are done, only then is it considered requirement completed.
	private boolean allTasksDone(Requirement stats) {
		if (stats == null) {
			return false;
		}

		long total = stats.getTotalTasks().longValue();
		Integer openObj = stats.getOpenTasks();
		long open = openObj == null ? 0 : openObj.longValue();
		return total > 0 && open == 0;
	}

	//DATE row
	private String fmtDate(Object d) {
		return d == null ? "" : d.toString().replace("-", "/");
	}
}

