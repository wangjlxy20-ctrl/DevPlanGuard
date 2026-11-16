package job.screen.dpgl100rea;

import biz.grandsight.ex.ui.ac.ShiroUserRole;
import biz.grandsight.ex.ui.ac.util.SessionHelper;
import jakarta.enterprise.context.RequestScoped;

import com.yse.gb.gf.front.CommandException;
import com.yse.gb.gf.front.CommandHelper;
import com.yse.gb.gf.front.InputException;
import com.yse.gb.gf.front.KeyObj;
import jakarta.inject.Inject;
import job.entity.Requirement;
import job.service.RequirementService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


@RequestScoped
public class COMMAND_BTNCMD_ENTRIES extends com.yse.gb.gf.bld.front.BLDCommandImpl {

	@Inject
	private RequirementService requirementService;

	@Override
	protected void perform(final CommandHelper helper, final String nextScreenId, final KeyObj keyObj)
			throws InputException, CommandException, Throwable {

		// 隐藏项目 ID（HTML 同名隐藏域提交）
		String projectId = helper.getParam("NMLTXT_PROJECT_ID");

		// 隐藏需求编码：有值 = 编辑模式（新增时该隐藏域为空）
		String reqCodeHidden = helper.getParam("NMLTXT_REQ_CODE");
		boolean isEdit = reqCodeHidden != null && reqCodeHidden.trim().length() > 0;
		System.out.println("ENTRIES: projectId=" + projectId + ", isEdit=" + isEdit);

		// 表单各输入项（组件名与 HTML 一致）
		String requirementCode =
				helper.getParam("NMLTXT_INPUT_INFO_REQUIREMENT_CODE");
		String requirementName =
				helper.getParam("NMLTXT_INPUT_INFO_REQUIREMENT_NAME");
		String priority =
				helper.getParam("NMLDLD_INFO_PRIORITY");
		String startDate =
				helper.getParam("LNKINP_PLAN_DATE");
		String plannedEndDate =
				helper.getParam("LNKINP_PLAN_END_DATE");
		String description =
				helper.getParam("NMLTXA_DESCRIPTION");

		ShiroUserRole shiroUserRole = SessionHelper.getShiroUserRole(helper);
		String createdBy = (shiroUserRole == null) ? null : shiroUserRole.getUserName();

		// 组装实体
		Requirement requirement = new Requirement();
		requirement.setProjectId(Integer.parseInt(projectId.trim()));
		// 编辑用隐藏编码定位（可见框只读但值也会提交，二者一致）；新增用表单输入的编码
		requirement.setRequirementCode(isEdit ? reqCodeHidden : requirementCode);
		requirement.setRequirementName(requirementName);
		requirement.setDescription(description);
		requirement.setPriority(priority);
		requirement.setPlannedStartDate(toDate(startDate));
		requirement.setPlannedEndDate(toDate(plannedEndDate));
		requirement.setCreatedBy(createdBy);

		// 按模式分流
		if (isEdit) {
			requirementService.updateRequirement(requirement);
		} else {
			requirementService.createRequirement(requirement);
		}

		// 回到该项目需求列表
		String targetScreenId = "DPGL100RE";
		KeyObj targetKeyObj = helper.getKeyObj(targetScreenId);
		targetKeyObj.put("PROJECT_ID", projectId == null ? "" : projectId);
		helper.pageOpen(targetScreenId, targetKeyObj);
	}


	private Date toDate(String value) throws ParseException {
		if (value == null || value.trim().length() == 0) {
			return null;
		}

		String text = value.trim().replace('/', '-');
		String[] patterns = {"yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM-dd'T'HH:mm:ss"};

		for (String pattern : patterns) {
			SimpleDateFormat format = new SimpleDateFormat(pattern);
			format.setLenient(false);
			try {
				return format.parse(text);
			} catch (ParseException e) {
				// 尝试下一个格式
			}
		}

		throw new ParseException("Unsupported date format: " + value, 0);
	}
}