package job.screen.dpgl200;

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

		// 框架 Helper#getPreScreenId()：上一个画面（本画面是被谁 pageOpen 出来的）
		String preScreenId = "DPGL200";

		String targetScreenId = preScreenId.trim();
		System.out.println(">>>THIS IS THE FIRST SCREEN : " + targetScreenId);

		//画面跳转（不 new 空 KeyObj：框架按画面 ID 管理 keyObj，目标画面自己那份原样带回）
		KeyObj targetKeyObj = helper.getKeyObj(targetScreenId);
		helper.pageOpen(targetScreenId, targetKeyObj);
	}
}
