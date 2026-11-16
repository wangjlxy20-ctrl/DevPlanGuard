package job.screen.dq100a;

import com.yse.gb.gf.front.*;
import jakarta.enterprise.context.RequestScoped;

import java.util.Arrays;


@RequestScoped
public class COMMAND_BTNCMD_MODIFY extends com.yse.gb.gf.bld.front.BLDCommandImpl {

	

	@Override
	protected void perform(final CommandHelper helper, final String nextScreenId, final KeyObj keyObj)
			throws InputException, CommandException, Throwable {

		//拿 NMLCHK_SELECT_ROW 这列的值
		String[] rowValues = helper.getRowParams("NMLCHK_SELECT_ROW");
		System.out.println("MODIFY ROW HAD SELECTED:" + Arrays.toString(rowValues));

		String modifyId = null;
		if (rowValues != null){
			for (int i = 0;i < rowValues.length;i++){
				if (rowValues[i] != null && rowValues[i].length() > 0){
					modifyId = rowValues[i];
					break;
				}
			}
		}

		System.out.println("MODIFY TARGET ID:" + modifyId);

		//
		if (modifyId == null){
			new CommandException(new UserMessage("Please Check the Users you want to modify"));
		}

		//画面跳转
		String targetScreenId = "DQ100AU";
		KeyObj targetKeyObj = helper.getKeyObj(targetScreenId);
		targetKeyObj.put("MODIFY_ID",modifyId == null ? "" : modifyId);
		helper.pageOpen(targetScreenId, targetKeyObj);
	}
}
