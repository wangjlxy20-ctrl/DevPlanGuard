package job.screen.dpgl120subd;

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
import org.apache.logging.log4j.core.pattern.HtmlTextRenderer;
import java.awt.*;
import java.util.Date;



@RequestScoped
public class DPGL120SUBDCreator extends Creator {
	

	@Inject
	HttpServletRequest request;

	@Inject
	private SubmissionService submissionService;

	@Override
	protected void createValueBean(final KeyObj keyObj, final ValueBean valueBean)
			throws CreatorException, Throwable {
		// Write the code here
		System.out.println("====DPGL120SUBD Creator executed====");

		// SYS TIME
		String now = FormatDate.get_YYYYMMDDhhmmss(new Date(System.currentTimeMillis()));
		valueBean.add(new Span("NMLLBL_NOW_DATE", now));

		// TASK_ID brought from the member task list
		String taskId = keyObj.get("TASK_ID");
		System.out.println(">>>>DPGL120SUBD got TASK_ID: " + taskId);

		if (taskId == null || taskId.trim().isEmpty()) {
			return;
		}

		// LOAD DEFECT INFO
		// 查询条件实体（只带 task_id）
		Task condition = new Task();
		condition.setTaskId(Integer.valueOf(taskId.trim()));

		//进入其他页面返回时能显示role
		String projectRole = keyObj.get("PROJECT_ROLE");
		String projectId = keyObj.get("PROJECT_ID");
		Input hiddenProjectRole = new Input("NMLTXT_MY_ROLE");
		hiddenProjectRole.setValue(projectRole);
		valueBean.add(hiddenProjectRole);
		Input hiddenProjectId = new Input("NMLTXT_PROJECT_ID");
		hiddenProjectId.setValue(String.valueOf(projectId));
		valueBean.add(hiddenProjectId);

		Task task = submissionService.findDefectById(condition);
		if (task == null) {
			return;
		}

		String taskCode   = String.valueOf(task.getTaskCode());
		String bugTitle   = String.valueOf(task.getTaskName());
		String severity   = String.valueOf(task.getSeverity());
		String bugDesc    = String.valueOf(task.getDescription());
		String parentCode = String.valueOf(task.getParentCode());

		if ("null".equals(bugDesc))    bugDesc = "";
		if ("null".equals(parentCode)) parentCode = "";
		// BUG INFO (READ ONLY)
		valueBean.add(new Span("NMLLBL_TASK_CODE", taskCode));
		valueBean.add(new Span("NMLLBL_BUG_TITLE", bugTitle));
		valueBean.add(new Span("NMLLBL_SEVERITY", severity));
		valueBean.add(new Span("NMLLBL_BUG_DESC_SHOW", bugDesc));
		valueBean.add(new Span("NMLLBL_FROM_TEST", parentCode));

		//TextArea bugDescShow = new TextArea("NMLTXA_BUG_DESC_SHOW");
		//bugDescShow.setValue(bugDesc);
		//valueBean.add(bugDescShow);

		// HIDDEN task_id for submit
		Input hiddenTid = new Input("NMLTXT_TASK_ID");
		hiddenTid.setValue(taskId);
		valueBean.add(hiddenTid);

		// 文件上传隐藏域（值由前端 JS 写入，这里只注册组件让框架接管）
		valueBean.add(new Input("NMLTXT_DELIVERABLE_FILE"));
	}
}
