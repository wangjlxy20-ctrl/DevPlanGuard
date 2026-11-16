package job.screen.dpgl101;

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
		String preScreenId = null;
		try {
			preScreenId = helper.getPreScreenId();
		} catch (Throwable e) {
			// 框架画面栈为空时会抛 EmptyStackException，视为没有上一画面
			preScreenId = null;
		}

		if (preScreenId == null || preScreenId.trim().isEmpty()) {
			// 没有上一画面：提示并停在本画面，不做跳转（Back 按钮不做禁用）
			System.out.println(">>>DPGL101 COMMAND_BTNCMD_BACK : previous screen is empty. stay on DPGL101.");
			return;
		}

		String targetScreenId = preScreenId.trim();
		System.out.println(">>>DPGL101 COMMAND_BTNCMD_BACK : back to " + targetScreenId);

		//画面跳转（不 new 空 KeyObj：框架按画面 ID 管理 keyObj，目标画面自己那份原样带回）
		KeyObj targetKeyObj = helper.getKeyObj(targetScreenId);
		helper.pageOpen(targetScreenId, targetKeyObj);
	}
}
