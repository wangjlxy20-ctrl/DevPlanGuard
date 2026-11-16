package job.screen.dpgl120sub;

import biz.grandsight.ex.ui.ac.ShiroUserRole;
import biz.grandsight.ex.ui.ac.util.SessionHelper;
import com.yse.gb.gf.front.*;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import job.entity.Task;
import job.service.SubmissionService;


@RequestScoped
public class COMMAND_BTNCMD_ADD extends com.yse.gb.gf.bld.front.BLDCommandImpl {

	@Inject
	private SubmissionService submissionService;


	@Override
	protected void perform(final CommandHelper helper, final String nextScreenId, final KeyObj keyObj)
			throws InputException, CommandException, Throwable {

		// 取登录用户
		ShiroUserRole shiroUserRole = SessionHelper.getShiroUserRole(helper);
		String userId = (shiroUserRole == null) ? null : shiroUserRole.getUserId();

		// 取表单参数
		String taskId = helper.getParam("NMLTXT_TASK_ID");
		String deliverableDescription = helper.getParam("NMLTXA_DELIVERABLE_DESCRIPTION");
		String deliverableUrl = helper.getParam("NMLTXT_DELIVERABLE_URL");
		String submissionNote = helper.getParam("NMLTXA_SUBMISSION_NOTE");
		String deliverableFile = helper.getParam("NMLTXT_DELIVERABLE_FILE");

		// 空值归零
		taskId = (taskId == null) ? "" : taskId.trim();
		deliverableDescription = (deliverableDescription == null) ? "" : deliverableDescription.trim();
		deliverableUrl = (deliverableUrl == null) ? "" : deliverableUrl;
		submissionNote = (submissionNote == null) ? "" : submissionNote;
		deliverableFile = (deliverableFile == null) ? "" : deliverableFile;

		if (taskId.length() != 0 && deliverableDescription.length() == 0) {
			String targetScreenId = "DPGL120SUB";
			KeyObj targetKeyObj = helper.getKeyObj(targetScreenId);
			targetKeyObj.put("MEMA_MSG", "Deliverable Description NOT NULL。");
			targetKeyObj.put("TASK_ID", taskId);
			targetKeyObj.put("BACK_DELIVERABLE_DESCRIPTION", deliverableDescription);
			targetKeyObj.put("BACK_DELIVERABLE_URL", deliverableUrl);
			targetKeyObj.put("BACK_SUBMISSION_NOTE", submissionNote);
			targetKeyObj.put("BACK_DELIVERABLE_FILE", deliverableFile);
			helper.pageOpen(targetScreenId, targetKeyObj);
			return;
		}

		// 取到的值先组装成 Task 实体，再交给 service
		Task task = new Task();
		task.setTaskId(toInt(taskId));
		task.setSubmittedBy(toInt(userId));
		task.setDeliverableDescription(deliverableDescription);
		task.setDeliverableUrl(deliverableUrl);
		task.setSubmissionNote(submissionNote);
		task.setDeliverableFile(deliverableFile);

		String[] result = submissionService.submitDeliverable(task);
		String message = result[0];
		String generatedTestCode = result[1];

		// 画面跳转（回到我的任务列表）
		String targetScreenId = "DPGL120PJTMEM";
		KeyObj targetKeyObj = helper.getKeyObj(targetScreenId);
		targetKeyObj.put("MEMA_MSG", message);
		if (generatedTestCode != null) {
			targetKeyObj.put("GENERATED_TEST_CODE", generatedTestCode);
		}
		System.out.println("SUBMIT CANCEL -> DPGL120PJTMEM");

		String projectId = helper.getParam("NMLTXT_PROJECT_ID");
		String projectRole = helper.getParam("NMLTXT_MY_ROLE");
		targetKeyObj.put("PROJECT_ROLE", projectRole == null ? "" : projectRole);
		targetKeyObj.put("PROJECT_ID", projectId == null ? "" : projectId);
		//targetKeyObj.put("NMLTXT_TASK_ID",helper.getParam("NMLTXT_TASK_ID"));
		helper.pageOpen(targetScreenId, targetKeyObj);

	}


	private Integer toInt(String s) {
		String v = (s == null) ? "" : s.trim();
		return v.matches("\\d+") ? Integer.valueOf(v) : null;
	}
}
