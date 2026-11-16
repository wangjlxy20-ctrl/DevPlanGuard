package job.screen.dpgl100re;

import jakarta.enterprise.context.RequestScoped;

import com.yse.gb.gf.front.CommandException;
import com.yse.gb.gf.front.CommandHelper;
import com.yse.gb.gf.front.InputException;
import com.yse.gb.gf.front.KeyObj;


@RequestScoped
public class COMMAND_BTNCMD_BACK extends com.yse.gb.gf.bld.front.BLDCommandImpl {
	
	@Override
	protected void perform(final CommandHelper helper, final String nextScreenId, final KeyObj keyObj)
			throws InputException, CommandException, Throwable {

		String projectId = helper.getParam("NMLTXT_PROJECT_ID");
		//画面跳转
		String targetScreenId = "DPGL100";
		KeyObj targetKeyObj = helper.getKeyObj(targetScreenId);
		targetKeyObj.put("PROJECT_ID",projectId);
		helper.pageOpen(targetScreenId, targetKeyObj);
	}
}
