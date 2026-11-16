package job.screen.dpgl110mng;

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



		//画面跳转
		//String projectId = helper.getParam("NMLTXT_PROJECT_ID");
		//String projectRole = helper.getParam("NMLTXT_MY_ROLE");
		String targetScreenId = "DPGL200";
		KeyObj targetKeyObj = helper.getKeyObj(targetScreenId);
		//targetKeyObj.put(,projectId);
		helper.pageOpen(targetScreenId, targetKeyObj);
	}
}
