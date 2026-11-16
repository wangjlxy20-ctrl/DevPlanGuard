package job.screen.dpgl100mema;

import jakarta.enterprise.context.RequestScoped;

import com.yse.gb.gf.front.CommandException;
import com.yse.gb.gf.front.CommandHelper;
import com.yse.gb.gf.front.InputException;
import com.yse.gb.gf.front.KeyObj;


@RequestScoped
public class COMMAND_BTNCMD_SEARCH extends com.yse.gb.gf.bld.front.BLDCommandImpl {
	
	@Override
	protected void perform(final CommandHelper helper, final String nextScreenId, final KeyObj keyObj)
			throws InputException, CommandException, Throwable {
		String pid = helper.getParam("NMLTXT_PROJECT_ID");
		String kw = helper.getParam("NMLTXT_SEARCH_USER_NAME");

		//画面跳转
		String targetScreenId = "DPGL100MEMA";
		KeyObj targetKeyObj = helper.getKeyObj(targetScreenId);
		targetKeyObj.put("MEMA_PROJECT_ID", pid == null ? "" : pid);
		targetKeyObj.put("MEMA_KEYWORD", kw == null ? "" : kw);

		System.out.println("DPGL100MEMA COMMAND_SEARCH, PROJECT_ID = " + pid + ", KEYWORD = " + kw);

		helper.pageOpen(targetScreenId, targetKeyObj);
	}
}
