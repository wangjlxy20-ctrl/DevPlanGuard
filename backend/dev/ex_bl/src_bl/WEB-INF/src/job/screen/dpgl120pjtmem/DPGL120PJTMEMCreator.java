package job.screen.dpgl120pjtmem;

import biz.grandsight.ex.ui.ac.ShiroUserRole;
import biz.grandsight.ex.ui.ac.util.SessionHelper;
import com.yse.gb.gf.front.*;
import com.yse.gb.gf.front.html.Input;
import com.yse.gb.gf.front.html.Row;
import com.yse.gb.gf.front.html.Span;
import com.yse.gb.gf.front.html.Table;
import com.yse.gb.util.FormatDate;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import job.entity.Task;
import job.service.TaskService;
import java.util.Date;
import java.util.List;



@RequestScoped
public class DPGL120PJTMEMCreator extends Creator {
	

	@Inject
	private TaskService taskService;

	@Override
	protected void createValueBean(final KeyObj keyObj, final ValueBean valueBean)
			throws CreatorException, Throwable {
		// Write the code here
		System.out.println("====DPGL120PJTMEM Creator executed====");

		// SYS TIME
		String now = FormatDate.get_YYYYMMDDhhmmss(new Date(System.currentTimeMillis()));
		valueBean.add(new Span("NMLLBL_NOW_DATE", now));

		// LOGIN INFO
		ShiroUserRole shiroUserRole = SessionHelper.getShiroUserRole(this);
		String	userId = (shiroUserRole == null) ? null : shiroUserRole.getUserId();
		String	username = (shiroUserRole == null) ? null : shiroUserRole.getUserName();

		// PROJECT_ROLE

		String projectRole = keyObj.get("PROJECT_ROLE");
		System.out.println(">>>>>DPGL120PJTMEM :THE LOGIN USER'S PROJECT_ROLE IS : " + projectRole);

		Table taskTable = new Table("TABLE_MEMBER_TASK");


		//拿到点击的项目id
		String projectId = keyObj.get("PROJECT_ID");
		List<Task> list = taskService.getMyTasks(userId,projectId);
		System.out.println("MY ASSIGNED TASK COUNT = " + list.size());

		// FIXED AREA：HELLO
		valueBean.add(new Span("NMLLBL_HELLO_USERNAME", username == null ? "" : username));
		valueBean.add(new Span("NMLLBL_MY_ROLE", projectRole == null ? "" : projectRole));
		//FIXED AREA:NUMBER OF TASKS
		valueBean.add(new Span("NMLLBL_TASK_COUNT", String.valueOf(list.size())));
		//hidden
		//进入其他页面返回时能显示role
		Input hiddenProjectRole = new Input("NMLTXT_MY_ROLE");
		hiddenProjectRole.setValue(projectRole);
		valueBean.add(hiddenProjectRole);
		Input hiddenProjectId = new Input("NMLTXT_PROJECT_ID");
		hiddenProjectId.setValue(String.valueOf(projectId));
		valueBean.add(hiddenProjectId);


		int rowNo = 0;
		for (Task t : list) {
			String taskId = String.valueOf(t.getTaskId());
			System.out.println(">>>>DPGL120PJTMEM has get the task_id:" + taskId);

			String taskCode = String.valueOf(t.getTaskCode());
			String taskName = String.valueOf(t.getTaskName());
			String taskType = String.valueOf(t.getTaskType());
			String projectName = String.valueOf(t.getProjectName());
			String requirementName = String.valueOf(t.getRequirementName());
			String priority = String.valueOf(t.getPriority());
			String status = String.valueOf(t.getStatus());
			String reviewStatus = String.valueOf(t.getReviewStatus());

			// DUE DATE
			String taskDue = String.valueOf(t.getPlannedEndDate());

			// LEFT DAY
			String daysLeft = String.valueOf(t.getDaysLeft());

			Row row = new Row();
			row.add(new Span("NMLLBL_PRIORITY", priority));

			Input hiddenTid = new Input("NMLTXT_TASK_ID");
			hiddenTid.setValue(taskId);
			row.add(hiddenTid);

			Input hiddenType = new Input("NMLTXT_TASK_TYPE");
			hiddenType.setValue(taskType);
			row.add(hiddenType);

			Input codeLink = new Input("LNKMOV_TASK_CODE");
			codeLink.setClassName("LINK_TEXT");
			codeLink.setValue(taskCode);
			row.add(codeLink);


			row.add(new Span("NMLLBL_TASK_NAME", taskName));

			row.add(new Span("NMLLBL_TASK_TYPE", taskType));

			row.add(new Span("NMLLBL_PROJECT_NAME", projectName));

			row.add(new Span("NMLLBL_REQ_NAME", requirementName));

			row.add(new Span("NMLLBL_TASK_DUE", taskDue));

			row.add(new Span("NMLLBL_DAYS_LEFT", daysLeft));

			//SUBMIT：已提交的行不再渲染按钮，显示文字
			if (hasSubmitted(status, reviewStatus)) {
				row.add(new Span("NMLLBL_SUBMITTED_STATE", "SUBMITTED"));
				// 模板里的按钮会被克隆，这里用空值 Input 抹掉它，使该行无法再提交
				Input blank = new Input("BTNCMD_ADD_R_" + rowNo);
				blank.setClassName("BTNCMD_HIDE");
				blank.setValue("");
				row.add(blank);
			} else {
				Input submitAdd = new Input("BTNCMD_ADD_R_" + rowNo);
				submitAdd.setClassName("BTNCMD");
				submitAdd.setValue("SUBMIT");
				row.add(submitAdd);
			}
			// 只有 IN_PROGRESS 可点;SUBMITTED/COMPLETED 等状态变灰
			boolean disabled = !"IN_PROGRESS".equals(status);

			System.out.println("rowNo=" + rowNo + ", status=" + status + ", disabled=" + disabled);

			taskTable.add(row);
			rowNo++;
		}

		valueBean.add(taskTable);
	}
	
	private boolean hasSubmitted(final String status, final String reviewStatus) {
		return "SUBMITTED".equals(status) || "COMPLETED".equals(status)
				|| "PASSED".equals(reviewStatus) || "FAILED".equals(reviewStatus);
	}
}