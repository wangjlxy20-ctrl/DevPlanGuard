package job.screen.dpgl110task;

import com.yse.gb.gf.front.*;
import com.yse.gb.gf.front.html.Input;
import com.yse.gb.gf.front.html.Row;
import com.yse.gb.gf.front.html.Span;
import com.yse.gb.gf.front.html.Table;
import com.yse.gb.util.FormatDate;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import job.entity.Requirement;
import job.entity.Task;
import job.service.RequirementService;
import job.service.TaskService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;



@RequestScoped
public class DPGL110TASKCreator extends Creator {

	@Inject
	private RequirementService requirementService;

	@Inject
	private TaskService taskService;



	//COMPLETED STATUS VALUE
	private static final String DONE = "COMPLETED";
	@Override
	protected void createValueBean(final KeyObj keyObj, final ValueBean valueBean)
			throws CreatorException, Throwable {
		// Write the code here
		System.out.println("====DPGL110TASK Creator executed ##FROM DEGL110MNG###====");

		// SYS TIME
		String now = FormatDate.get_YYYYMMDDhhmmss(new Date(System.currentTimeMillis()));
		valueBean.add(new Span("NMLLBL_NOW_DATE", now));

		// requirement_id：
		// On the last page, click the request link and put it in
		String requirementId = keyObj.get("REQUIREMENT_ID");

		//hidden
		String projectRole = keyObj.get("PROJECT_ROLE");
		String projectId = keyObj.get("PROJECT_ID");
		Input hiddenProjectRole = new Input("NMLTXT_MY_ROLE");
		hiddenProjectRole.setValue(projectRole);
		valueBean.add(hiddenProjectRole);
		Input hiddenProjectId = new Input("NMLTXT_PROJECT_ID");
		hiddenProjectId.setValue(projectId);
		valueBean.add(hiddenProjectId);

		// Backfill Hidden: requirement_id（USING WHEN :BACK/NEW ）
		Input hiddenReq = new Input("NMLTXT_REQ_ID");
		hiddenReq.setValue(requirementId);
		valueBean.add(hiddenReq);

		// 需求条件实体（requirement_id）
		Requirement reqRef = new Requirement();
		reqRef.setRequirementId(Integer.valueOf(requirementId.trim()));

		//BackFill Hidden
		//requirement_code
		String reqCode = requirementService.getRequirementCode(reqRef);
		Input hiddenReqCode = new Input("NMLTXT_REQ_CODE");
		hiddenReqCode.setValue(reqCode);
		valueBean.add(hiddenReqCode);
		//组件显示
		valueBean.add(new Span("NMLLBL_REQ_CODE",reqCode));

		Table taskTable = new Table("TABLE_TASK");

		//REQ_NAME
		String requirementName = requirementService.getRequirementName(reqRef);
		valueBean.add(new Span("NMLLBL_REQ_NAME", requirementName));

		// TASK（条件实体：requirement_id）
		Task condition = new Task();
		condition.setRequirementId(reqRef.getRequirementId());
		List<Task> list = taskService.searchTasks(condition);
		if (list == null) {
			list = new ArrayList<>();
		}
		System.out.println("TASK COUNT FOR REQUIREMENT " + requirementId + " = " + list.size());

		//Stats: Total / Completed / Pending
		int total = list.size();
		int done = 0;
		for (Task t : list) {
			String stObj = t.getStatus();
			if (stObj != null && DONE.equalsIgnoreCase(stObj.trim())) {
				done++;
			}
		}
		int undone = total - done;
		valueBean.add(new Span("NMLLBL_STAT_TOTAL", String.valueOf(total)));
		valueBean.add(new Span("NMLLBL_STAT_DONE", String.valueOf(done)));
		valueBean.add(new Span("NMLLBL_STAT_UNDONE", String.valueOf(undone)));


		for (Task t : list) {
			String priority = String.valueOf(t.getPriority());
			String taskCode = String.valueOf(t.getTaskCode());
			String taskName = String.valueOf(t.getTaskName());
			String taskType = String.valueOf(t.getTaskType());
			String status = String.valueOf(t.getStatus());

			// TASK DUE
			String taskDue = String.valueOf(t.getPlannedEndDate());


			Object assigneeNameObj = t.getAssigneeName();
			String assigneeName = assigneeNameObj == null ? "Unassigned" : String.valueOf(assigneeNameObj);

			Object taskIdObj = t.getTaskId();
			String taskId = taskIdObj == null ? "" : String.valueOf(taskIdObj);
			System.out.println(">>>>DPGL110TASKCREATOR : TASK_ID IS : " + taskId);

			Row row = new Row();

			row.add(new Span("NMLLBL_PRIORITY", priority));
			// task_code 链接：点击进编辑页回显（task_id 隐藏域已在该行）
			Input codeLink = new Input("LNKMOV_TASK_CODE");
			codeLink.setClassName("LINK_TEXT");
			codeLink.setValue(taskCode);
			row.add(codeLink);
			row.add(new Span("NMLLBL_TASK_NAME", taskName));
			row.add(new Span("NMLLBL_TASK_TYPE", taskType));
			row.add(new Span("NMLLBL_TASK_DUE", taskDue));
			row.add(new Span("NMLLBL_TASK_STATUS", status));

			//Hidden: task_id
			Input hiddenTaskId = new Input("NMLTXT_TASK_ID");
			hiddenTaskId.setValue(taskId);
			row.add(hiddenTaskId);

			//LNKMOV shows assignee name
			Input assigneeLink = new Input("LNKMOV_TASK_ASSIGNEE_NAME");
			assigneeLink.setClassName("LINK_TEXT");
			assigneeLink.setValue(assigneeName);
			row.add(assigneeLink);

			taskTable.add(row);

		}

		valueBean.add(taskTable);
	}
}
