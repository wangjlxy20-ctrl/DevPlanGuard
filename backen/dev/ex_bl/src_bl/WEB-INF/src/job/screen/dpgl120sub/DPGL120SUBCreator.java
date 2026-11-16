package job.screen.dpgl120sub;

import com.yse.gb.gf.front.*;
import com.yse.gb.gf.front.html.Input;
import com.yse.gb.gf.front.html.Span;
import com.yse.gb.gf.front.html.Textarea;
import com.yse.gb.util.FormatDate;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import job.entity.Task;
import job.service.SubmissionService;

import java.util.Date;



@RequestScoped
public class DPGL120SUBCreator extends Creator {

	@Inject
	private SubmissionService submissionService;


	@Override
	protected void createValueBean(final KeyObj keyObj, final ValueBean valueBean)
			throws CreatorException, Throwable {
		// Write the code here
		System.out.println("====DPGL120SUB Creator executed====");

		// notification info
		String msg = keyObj.get("MEMA_MSG");
		if (msg != null && msg.trim().length() > 0) {
			valueBean.setMsgHandleBean(new MsgHandleBean(new UserMessage(msg)));
		}

		// TIME
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


		String taskCodeText = "";
		String taskNameText = "";
		String deliverableDescText = "";
		String deliverableUrlText = "";
		String submissionNoteText = "";
		String deliverableFileText = "";

		String taskId = keyObj.get("TASK_ID");
		if (taskId == null) {
			taskId = "";
		}
		taskId = taskId.trim();

		String taskType = keyObj.get("TASK_TYPE");
		System.out.println(">>>DPGL120SUB >>>> TASK_TYPE IS : " + taskType);

		Input hiddenTid = new Input("NMLTXT_TASK_ID");
		Input deliverableUrl = new Input("NMLTXT_DELIVERABLE_URL");
		Input deliverableFile = new Input("NMLTXT_DELIVERABLE_FILE");

		if (taskId.isEmpty()) {
			taskNameText = "No task selected.";
			valueBean.add(hiddenTid);
			valueBean.add(new Span("NMLLBL_TASK_CODE", taskCodeText));
			valueBean.add(new Span("NMLLBL_TASK_NAME", taskNameText));
			valueBean.add(new Textarea("NMLTXA_DELIVERABLE_DESCRIPTION", deliverableDescText));
			valueBean.add(deliverableUrl);
			valueBean.add(deliverableFile);
			valueBean.add(new Textarea("NMLTXA_SUBMISSION_NOTE", submissionNoteText));
			return;
		}

		// hidden refill : task_id
		hiddenTid.setValue(taskId);


		// 查询条件实体（只带 task_id）
		Task condition = new Task();
		condition.setTaskId(Integer.valueOf(taskId.trim()));

		Task t = submissionService.getTaskInfo(condition);
		if (t == null) {
			taskNameText = "Task not found.";
		} else {
			taskCodeText = String.valueOf(t.getTaskCode());
			taskNameText = String.valueOf(t.getTaskName());
		}

		// Show back the content the user filled in when validation fails
		String backDesc = keyObj.get("BACK_DELIVERABLE_DESCRIPTION");
		if (backDesc != null) {
			deliverableDescText = backDesc;
		}
		String backUrl = keyObj.get("BACK_DELIVERABLE_URL");
		if (backUrl != null) {
			deliverableUrlText = backUrl;
		}
		String backNote = keyObj.get("BACK_SUBMISSION_NOTE");
		if (backNote != null) {
			submissionNoteText = backNote;
		}
		String backFile = keyObj.get("BACK_DELIVERABLE_FILE");
		if (backFile != null) {
			deliverableFileText = backFile;
		}

		deliverableUrl.setValue(deliverableUrlText);
		deliverableFile.setValue(deliverableFileText);
		valueBean.add(hiddenTid);
		valueBean.add(new Span("NMLLBL_TASK_TYPE", taskType));
		valueBean.add(new Span("NMLLBL_TASK_CODE", taskCodeText));

		valueBean.add(new Span("NMLLBL_TASK_NAME", taskNameText));
		valueBean.add(new Textarea("NMLTXA_DELIVERABLE_DESCRIPTION", deliverableDescText));
		valueBean.add(deliverableUrl);
		valueBean.add(deliverableFile);
		valueBean.add(new Textarea("NMLTXA_SUBMISSION_NOTE", submissionNoteText));
	}
}
