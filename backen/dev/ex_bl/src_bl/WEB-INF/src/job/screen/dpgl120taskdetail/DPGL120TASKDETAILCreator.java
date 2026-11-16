package job.screen.dpgl120taskdetail;

import com.yse.gb.gf.front.Creator;
import com.yse.gb.gf.front.CreatorException;
import com.yse.gb.gf.front.KeyObj;
import com.yse.gb.gf.front.ValueBean;
import com.yse.gb.gf.front.html.Input;
import com.yse.gb.gf.front.html.Span;
import com.yse.gb.util.FormatDate;
import jakarta.enterprise.context.RequestScoped;
import job.mapper.task.TaskMapper;

import java.util.Date;
import java.util.Map;


@RequestScoped
public class DPGL120TASKDETAILCreator extends Creator {

	@Override
	protected void createValueBean(final KeyObj keyObj, final ValueBean valueBean)
			throws CreatorException, Throwable {

		System.out.println("====DPGL120TASKDETAIL Creator executed====");

		// 系统时间
		String now = FormatDate.get_YYYYMMDDhhmmss(new Date(System.currentTimeMillis()));
		valueBean.add(new Span("NMLLBL_NOW_DATE", now));

		// 列表页传来的 task_id
		String taskId = keyObj.get("TASK_ID");
		if (taskId != null) {
			taskId = taskId.trim();
		}

		Map<String, Object> d = null;
		if (taskId != null && !taskId.isEmpty()) {
			d = new TaskMapper().getTaskDetail(taskId);
		}

		if (d == null) {
			// 查不到：给标题区一个提示，其余留空
			valueBean.add(new Span("NMLLBL_DISPLAY_INFO", "Task not found, please go back and retry."));
			return;
		}

		// 审批命令取当前任务 id
		Input hiddenTaskId = new Input("NMLTXT_TASK_ID");
		hiddenTaskId.setValue(taskId);
		valueBean.add(hiddenTaskId);

		// Project
		valueBean.add(new Span("NMLLBL_PROJECT_NAME", str(d.get("project_name"))));
		valueBean.add(new Span("NMLLBL_PROJECT_DESCRIPTION", str(d.get("project_description"))));

		// Requirement
		valueBean.add(new Span("NMLLBL_REQUIREMENT_NAME", str(d.get("requirement_name"))));
		valueBean.add(new Span("NMLLBL_REQUIREMENT_DESCRIPTION", str(d.get("requirement_description"))));

		// Task
		valueBean.add(new Span("NMLLBL_TASK_CODE", str(d.get("task_code"))));
		valueBean.add(new Span("NMLLBL_TASK_NAME", str(d.get("task_name"))));
		valueBean.add(new Span("NMLLBL_TASK_TYPE", str(d.get("task_type"))));
		valueBean.add(new Span("NMLLBL_PRIORITY", str(d.get("priority"))));
		valueBean.add(new Span("NMLLBL_ASSIGNEE_NAME", str(d.get("assignee_name"))));
		valueBean.add(new Span("NMLLBL_STATUS", str(d.get("status"))));
		valueBean.add(new Span("NMLLBL_TASK_DESCRIPTION", str(d.get("task_description"))));

		// Submission（通用）
		valueBean.add(new Span("NMLLBL_SUBMITTED_AT", str(d.get("submitted_at"))));
		valueBean.add(new Span("NMLLBL_DELIVERABLE_DESC", str(d.get("deliverable_description"))));
		valueBean.add(new Span("NMLLBL_DELIVERABLE_URL", str(d.get("deliverable_url"))));
		valueBean.add(new Span("NMLLBL_SUBMISSION_NOTE", str(d.get("submission_note"))));
		valueBean.add(new Span("NMLLBL_REVIEW_STATUS", str(d.get("review_status"))));

		// 类型专有字段（不属于当前类型时数据库值为 null，显示空；区块本身由 JS 隐藏）
		valueBean.add(new Span("NMLLBL_TEST_PLAN", str(d.get("test_plan"))));
		valueBean.add(new Span("NMLLBL_TEST_CASE_DESIGN", str(d.get("test_case_design"))));
		valueBean.add(new Span("NMLLBL_SEVERITY", str(d.get("severity"))));
	}

	private String str(Object o) {
		if (o == null) {
			return "";
		}
		String s = String.valueOf(o);
		if ("null".equals(s)) {
			return "";
		}
		return s;
	}
}

