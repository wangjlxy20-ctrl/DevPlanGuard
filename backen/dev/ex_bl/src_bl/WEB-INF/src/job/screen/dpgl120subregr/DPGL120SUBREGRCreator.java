package job.screen.dpgl120subregr;

import com.yse.gb.gf.front.html.Input;
import com.yse.gb.gf.front.html.Span;
import com.yse.gb.util.FormatDate;
import jakarta.enterprise.context.RequestScoped;

import com.yse.gb.gf.front.Creator;
import com.yse.gb.gf.front.CreatorException;
import com.yse.gb.gf.front.KeyObj;
import com.yse.gb.gf.front.ValueBean;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import job.entity.Task;
import job.service.SubmissionService;

import java.util.Date;



@RequestScoped
public class DPGL120SUBREGRCreator extends Creator {
	
	@Inject
	HttpServletRequest request;

	@Inject
	private SubmissionService submissionService;

	@Override
	protected void createValueBean(final KeyObj keyObj, final ValueBean valueBean)
			throws CreatorException, Throwable {
		// Write the code here
		System.out.println("====DPGL120SUBREGR Creator executed====");

		String now = FormatDate.get_YYYYMMDDhhmmss(new Date(System.currentTimeMillis()));
		valueBean.add(new Span("NMLLBL_NOW_DATE", now));

		//进入其他页面返回时能显示role
		String projectRole = keyObj.get("PROJECT_ROLE");
		String projectId = keyObj.get("PROJECT_ID");
		Input hiddenProjectRole = new Input("NMLTXT_MY_ROLE");
		hiddenProjectRole.setValue(projectRole);
		valueBean.add(hiddenProjectRole);
		Input hiddenProjectId = new Input("NMLTXT_PROJECT_ID");
		hiddenProjectId.setValue(String.valueOf(projectId));
		valueBean.add(hiddenProjectId);

		String taskId = keyObj.get("TASK_ID");
		System.out.println(">>>>DPGL120SUBREGR got TASK_ID: " + taskId);

		if (taskId == null || taskId.trim().isEmpty()) {
			return;
		}

		// 查询条件实体（只带 task_id）
		Task condition = new Task();
		condition.setTaskId(Integer.valueOf(taskId.trim()));

		Task task = submissionService.findRegressionById(condition);
		if (task == null) {
			return;
		}

		String taskCode   = String.valueOf(task.getTaskCode());
		String taskName   = String.valueOf(task.getTaskName());
		String defectCode = String.valueOf(task.getDefectCode());
		String bugTitle   = String.valueOf(task.getBugTitle());
		String severity   = String.valueOf(task.getSeverity());
		String bugDesc    = String.valueOf(task.getDescription());

		if ("null".equals(defectCode)) defectCode = "";
		if ("null".equals(bugTitle))   bugTitle = "";
		if ("null".equals(severity))   severity = "";
		if ("null".equals(bugDesc))    bugDesc = "";
		// REGRESSION TARGET (READ ONLY)
		valueBean.add(new Span("NMLLBL_TASK_CODE", taskCode));
		valueBean.add(new Span("NMLLBL_TASK_NAME", taskName));
		valueBean.add(new Span("NMLLBL_DEFECT_CODE", defectCode));
		valueBean.add(new Span("NMLLBL_BUG_TITLE", bugTitle));
		valueBean.add(new Span("NMLLBL_SEVERITY", severity));
		valueBean.add(new Span("NMLLBL_BUG_DESC_SHOW", bugDesc));

		//TextArea bugDescShow = new TextArea("NMLTXA_BUG_DESC_SHOW");
		//bugDescShow.setValue(bugDesc);
		//valueBean.add(bugDescShow);

		// HIDDEN task_id
		Input hiddenTid = new Input("NMLTXT_TASK_ID");
		hiddenTid.setValue(taskId);
		valueBean.add(hiddenTid);

		// 文件上传隐藏域（值由前端 JS 写入，这里只注册组件让框架接管）
		valueBean.add(new Input("NMLTXT_DELIVERABLE_FILE"));

	}
}
