package job.screen.dpgl100mema;

import jakarta.enterprise.context.RequestScoped;

import com.yse.gb.gf.front.CommandException;
import com.yse.gb.gf.front.CommandHelper;
import com.yse.gb.gf.front.InputException;
import com.yse.gb.gf.front.KeyObj;
import jakarta.inject.Inject;
import job.service.MemberService;


@RequestScoped
public class COMMAND_BTNCMD_ADD extends com.yse.gb.gf.bld.front.BLDCommandImpl {

	@Inject
	private MemberService memberService;

	
	@Override
	protected void perform(final CommandHelper helper, final String nextScreenId, final KeyObj keyObj)
			throws InputException, CommandException, Throwable {

		//取参：勾选列（用户 id）与角色列（原样，不改列名）
		String[] selectedRows = helper.getRowParams("NMLCHK_SELECT_ROW");
		String[] roleRows = helper.getRowParams("NMLDLD_INFO");

		//勾选扫描：是否有勾选行（判断在 service）
		boolean any = memberService.hasAnySelected(selectedRows);

		String pid = helper.getParam("NMLTXT_PROJECT_ID");

		if (!any) {
			//返回父屏幕时，带上查询条件，避免画面不连贯
			//Not checked: Keep child window conditions prompt
			String kw = helper.getParam("NMLTXT_SEARCH_USER_NAME");
			String targetScreenId = "DPGL100MEMA";
			KeyObj targetKeyObj = helper.getKeyObj(targetScreenId);
			targetKeyObj.put("MEMA_PROJECT_ID", pid);
			targetKeyObj.put("MEMA_KEYWORD", kw == null ? "" : kw);
			targetKeyObj.put("MEMA_MSG", "Please select at least one user.");
			helper.pageOpen(targetScreenId, targetKeyObj);
			return;
		}

		//勾选行的 id / role 两列对齐 + 批量写库，全部在 service
		memberService.addSelectedMembers(pid, selectedRows, roleRows);

		//Back to the father screen, Father Creator looks it up again using project_id
		String targetScreenId = "DPGL100MEM";
		KeyObj targetKeyObj = helper.getKeyObj(targetScreenId);
		targetKeyObj.put("PROJECT_ID", pid);
		helper.pageOpen(targetScreenId, targetKeyObj);
	}
}
