package job.screen.dpgl200;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import com.yse.gb.gf.front.CommandException;
import com.yse.gb.gf.front.CommandHelper;
import com.yse.gb.gf.front.InputException;
import com.yse.gb.gf.front.KeyObj;
import job.service.ProjectService;

import java.util.Arrays;



@RequestScoped
public class COMMAND_BTNCMD_ENTRIES extends com.yse.gb.gf.bld.front.BLDCommandImpl {

	@Inject
	private ProjectService projectService;

	private String projectId;
	private String projectRole;


	@Override
	protected void perform(final CommandHelper helper, final String nextScreenId, final KeyObj keyObj)
			throws InputException, CommandException, Throwable {
		projectId = null;
		projectRole = null;

		// 被点击按钮的完整名字（BTNCMD_ENTRIES_R_1）
		String buttonName = helper.getParam("BUTTON_NAME");
		System.out.println(">>>DPGL200 BUTTON_NAME = " + buttonName);

		if (buttonName != null && buttonName.startsWith("BTNCMD_ENTRIES_R_")) {
			int rowIdx = -1;
			try {
				rowIdx = Integer.parseInt(buttonName.substring("BTNCMD_ENTRIES_R_".length()));
			} catch (NumberFormatException e) {
				System.out.println(">>>DPGL200 invalid BUTTON_NAME suffix : " + buttonName);
			}

			if (rowIdx >= 0) {
				// 行数据数组
				String[] pidCol = helper.getRowParams("NMLTXT_PROJECT_ID");
				String[] roleCol = helper.getRowParams("NMLTXT_PROJECT_ROLE");

				if (pidCol != null && rowIdx < pidCol.length) {
					projectId = pidCol[rowIdx];
					if (roleCol != null && rowIdx < roleCol.length) {
						projectRole = roleCol[rowIdx];
					}
				}
			}

			System.out.println("DPGL200>>>> THE CHOSEN PROJECT_ID IS : " + projectId);
			System.out.println("DPGL200>>>> THE CHOSEN PROJECT_ROLE IS : " + projectRole);

			//画面跳转（按 project_role 分流：LEAD → DPGL110MNG / 其余 → DPGL120PJTMEM）
			String targetScreenId = projectService.getEntryScreenId(projectRole);
			KeyObj targetKeyObj = helper.getKeyObj(targetScreenId);

			//带去下一个页面
			targetKeyObj.put("PROJECT_ID", projectId);
			targetKeyObj.put("PROJECT_ROLE", projectRole);
			helper.pageOpen(targetScreenId, targetKeyObj);
		}
	}
}
