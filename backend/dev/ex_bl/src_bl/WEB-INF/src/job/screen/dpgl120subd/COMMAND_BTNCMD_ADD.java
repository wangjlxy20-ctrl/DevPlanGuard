package job.screen.dpgl120subd;

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

		//取登录用户（web 层，null 安全三元式，不用 if）
		ShiroUserRole shiroUserRole = SessionHelper.getShiroUserRole(helper);
		String userId = (shiroUserRole == null) ? null : shiroUserRole.getUserId();

		// 取表单参数（原参数名一个不改）
		String taskId         = helper.getParam("NMLTXT_TASK_ID");
		String rootCause      = helper.getParam("NMLTXA_ROOT_CAUSE");
		String fixMethod      = helper.getParam("NMLTXA_FIX_METHOD");
		String affectedScope  = helper.getParam("NMLTXA_AFFECTED_SCOPE");
		String deliverableUrl = helper.getParam("NMLTXT_DELIVERABLE_URL");
		String submissionNote = helper.getParam("NMLTXA_SUBMISSION_NOTE");
		String deliverableFile = helper.getParam("NMLTXT_DELIVERABLE_FILE");

		//取到的值先组装成 Task 实体，再交给 service（失败由 service 抛原异常）
		Task task = new Task();
		task.setTaskId(toInt(taskId));
		task.setSubmittedBy(toInt(userId));
		task.setRootCause(rootCause);
		task.setFixMethod(fixMethod);
		task.setAffectedScope(affectedScope);
		task.setDeliverableUrl(deliverableUrl);
		task.setSubmissionNote(submissionNote);
		task.setDeliverableFile(deliverableFile);

		// 业务与校验全部交给 service（失败由 service 抛原异常）
		String message = submissionService.submitDefectFix(task);

		// 画面跳转（回到我的任务列表）
		String targetScreenId = "DPGL120PJTMEM";
		KeyObj targetKeyObj = helper.getKeyObj(targetScreenId);
		targetKeyObj.put("MEMA_MSG", message);
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
