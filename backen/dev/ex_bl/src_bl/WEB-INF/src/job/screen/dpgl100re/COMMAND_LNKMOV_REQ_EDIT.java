package job.screen.dpgl100re;

import jakarta.enterprise.context.RequestScoped;

import com.yse.gb.gf.front.CommandException;
import com.yse.gb.gf.front.CommandHelper;
import com.yse.gb.gf.front.InputException;
import com.yse.gb.gf.front.KeyObj;


@RequestScoped
public class COMMAND_LNKMOV_REQ_EDIT extends com.yse.gb.gf.bld.front.BLDCommandImpl {

	@Override
	protected void perform(final CommandHelper helper, final String nextScreenId, final KeyObj keyObj)
			throws InputException, CommandException, Throwable {

		// linkValue = 该行 LNKMOV_REQ_EDIT 的 value（requirement_code）
		String reqCode = helper.getLinkValue();
		String projectId = helper.getParam("NMLTXT_PROJECT_ID");
		System.out.println("EDIT REQ: projectId=" + projectId + ", reqCode=" + reqCode);

		// 跳到复用表单页 DPGL100REA（带 REQ_CODE = 编辑模式）
		String targetScreenId = "DPGL100REA";
		KeyObj targetKeyObj = helper.getKeyObj(targetScreenId);
		targetKeyObj.put("PROJECT_ID", projectId == null ? "" : projectId);
		targetKeyObj.put("REQ_CODE", reqCode == null ? "" : reqCode);
		helper.pageOpen(targetScreenId, targetKeyObj);
	}
}
