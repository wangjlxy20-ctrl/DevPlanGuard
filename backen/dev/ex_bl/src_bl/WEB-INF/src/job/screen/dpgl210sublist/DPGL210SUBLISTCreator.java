package job.screen.dpgl210sublist;

import biz.grandsight.ex.ui.ac.ShiroUserRole;
import biz.grandsight.ex.ui.ac.util.SessionHelper;
import com.yse.gb.gf.front.Creator;
import com.yse.gb.gf.front.CreatorException;
import com.yse.gb.gf.front.KeyObj;
import com.yse.gb.gf.front.MsgHandleBean;
import com.yse.gb.gf.front.UserMessage;
import com.yse.gb.gf.front.ValueBean;
import com.yse.gb.gf.front.html.Input;
import com.yse.gb.gf.front.html.Row;
import com.yse.gb.gf.front.html.Span;
import com.yse.gb.gf.front.html.Table;
import com.yse.gb.util.FormatDate;
import jakarta.enterprise.context.RequestScoped;
import job.mapper.task.TaskMapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RequestScoped
public class DPGL210SUBLISTCreator extends Creator {

	@Override
	protected void createValueBean(final KeyObj keyObj, final ValueBean valueBean)
			throws CreatorException, Throwable {

		System.out.println("====DPGL210SUBLIST Creator executed====");

		String msg = keyObj.get("MEMA_MSG");
		if (msg != null && !msg.trim().isEmpty()) {
			valueBean.setMsgHandleBean(new MsgHandleBean(new UserMessage(msg)));
		}

		// 系统时间
		String now = FormatDate.get_YYYYMMDDhhmmss(new Date(System.currentTimeMillis()));
		valueBean.add(new Span("NMLLBL_NOW_DATE", now));

		// 当前登录用户
		ShiroUserRole shiroUserRole = SessionHelper.getShiroUserRole(this);
		String userId = (shiroUserRole == null) ? null : shiroUserRole.getUserId();

		Table submittedTable = new Table("TABLE_SUBMITTED");

		List<Map<String, Object>> list = new ArrayList<>();
		if (userId != null && !userId.trim().isEmpty()) {
			List<Map<String, Object>> result =
					new TaskMapper().searchSubmittedTasks(userId);
			if (result != null) {
				list = result;
			}
		}
		System.out.println("SUBMITTED TASK COUNT = " + list.size());

		// 配列：每行一个已提交任务，8 列
		for (Map<String, Object> t : list) {
			String taskId = str(t.get("task_id"));
			String taskCode = str(t.get("task_code"));
			String taskName = str(t.get("task_name"));
			String taskType = str(t.get("task_type"));
			String projectName = str(t.get("project_name"));
			String requirementName = str(t.get("requirement_name"));
			String assigneeName = str(t.get("assignee_name"));
			String submittedAt = str(t.get("submitted_at"));
			String reviewStatus = str(t.get("review_status"));

			Row row = new Row();

			// 1. Task Code：隐藏 task_id + 链接
			Input hiddenTid = new Input("NMLTXT_TASK_ID");
			hiddenTid.setValue(taskId);
			row.add(hiddenTid);

			Input codeLink = new Input("LNKMOV_TASK_CODE");
			codeLink.setClassName("LINK_TEXT");
			codeLink.setValue(taskCode);
			row.add(codeLink);

			// 2-8
			row.add(new Span("NMLLBL_TASK_NAME", taskName));
			row.add(new Span("NMLLBL_TASK_TYPE", taskType));
			row.add(new Span("NMLLBL_PROJECT_NAME", projectName));
			row.add(new Span("NMLLBL_REQ_NAME", requirementName));
			row.add(new Span("NMLLBL_ASSIGNEE_NAME", assigneeName));
			row.add(new Span("NMLLBL_SUBMITTED_AT", submittedAt));
			row.add(new Span("NMLLBL_REVIEW_STATUS", reviewStatus));

			submittedTable.add(row);
		}

		valueBean.add(submittedTable);
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

