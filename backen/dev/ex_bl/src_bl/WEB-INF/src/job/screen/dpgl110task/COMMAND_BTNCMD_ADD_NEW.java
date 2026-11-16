package job.screen.dpgl110task;

import jakarta.enterprise.context.RequestScoped;

import com.yse.gb.gf.front.CommandException;
import com.yse.gb.gf.front.CommandHelper;
import com.yse.gb.gf.front.InputException;
import com.yse.gb.gf.front.KeyObj;


@RequestScoped
public class COMMAND_BTNCMD_ADD_NEW extends com.yse.gb.gf.bld.front.BLDCommandImpl {
	
	@Override
	protected void perform(final CommandHelper helper, final String nextScreenId, final KeyObj keyObj)
			throws InputException, CommandException, Throwable {
		// REFill in the requirement_id
		// so the Creator on this page can continue rendering according to this requirement
		String rid = helper.getParam("NMLTXT_REQ_ID");
		String reqCode = helper.getParam("NMLTXT_REQ_CODE");
		String projectId = helper.getParam("NMLTXT_PROJECT_ID");
		String projectRole = helper.getParam("NMLTXT_MY_ROLE");

		//画面跳转
		String targetScreenId = "DPGL110TASKADD";
		KeyObj targetKeyObj = helper.getKeyObj(targetScreenId);
		targetKeyObj.put("REQUIREMENT_ID", rid == null ? "" : rid);
		targetKeyObj.put("REQUIREMENT_CODE",reqCode == null ?"":reqCode);
		targetKeyObj.put("PROJECT_ROLE", projectId == null ? "" : projectRole);
		targetKeyObj.put("PROJECT_ID", projectRole == null ? "" : projectId);

		helper.pageOpen(targetScreenId, targetKeyObj);
	}
}
