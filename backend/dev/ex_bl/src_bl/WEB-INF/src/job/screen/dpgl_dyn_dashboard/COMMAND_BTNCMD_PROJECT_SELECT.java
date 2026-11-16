package job.screen.dpgl_dyn_dashboard;

import jakarta.enterprise.context.RequestScoped;
import jakarta.servlet.http.HttpSession;

import com.yse.gb.gf.bld.front.BLDCommandImpl;
import com.yse.gb.gf.front.CommandException;
import com.yse.gb.gf.front.CommandHelper;
import com.yse.gb.gf.front.InputException;
import com.yse.gb.gf.front.KeyObj;


@RequestScoped
public class COMMAND_BTNCMD_PROJECT_SELECT extends BLDCommandImpl {

    @Override
    protected void perform(final CommandHelper helper, final String nextScreenId, final KeyObj keyObj)
            throws InputException, CommandException, Throwable {

        String projectId = helper.getParam("NMLDLD_PROJECT");
        HttpSession session = helper.getSession();

        if (projectId != null && !projectId.trim().isEmpty() && session != null) {
            try {
                Integer pid = Integer.valueOf(projectId.trim());
                session.setAttribute(DPGL_DYN_DASHBOARDCreator.SESSION_PROJECT_ID, pid);
                System.out.println("====DPGL101 project switched -> " + pid + "====");
            } catch (NumberFormatException e) {
                System.out.println("====DPGL101 invalid project_id: " + projectId + "====");
            }
        }
    }

    @Override
    protected String getNextScreenId(final CommandHelper helper, final KeyObj keyObj)
            throws CommandException {
        // 切换后留在本页，由本页 Creator 重新渲染
        return "DPGL_DYN_DASHBOARD";
    }
}
