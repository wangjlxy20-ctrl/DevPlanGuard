package job.screen.dpgl110task;

import jakarta.enterprise.context.RequestScoped;

import com.yse.gb.gf.bld.front.BLDCommandImpl;
import com.yse.gb.gf.front.CommandException;
import com.yse.gb.gf.front.CommandHelper;
import com.yse.gb.gf.front.InputException;
import com.yse.gb.gf.front.KeyObj;

@RequestScoped
public class COMMAND_LNKMOV_TASK_CODE extends BLDCommandImpl {


    @Override
    protected void perform(final CommandHelper helper, final String nextScreenId, final KeyObj keyObj)
            throws InputException, CommandException, Throwable {
        //
        // 被点行的 task_id（前端 linkOpen 第二参数）
        String taskId = helper.getLinkValue();
        System.out.println("OPEN TASK EDIT, taskId=" + taskId);

        // 回显页依赖的上下文（当前列表隐藏域）
        String requirementId = helper.getParam("NMLTXT_REQ_ID");
        String projectId = helper.getParam("NMLTXT_PROJECT_ID");
        String projectRole = helper.getParam("NMLTXT_MY_ROLE");

        String targetScreenId = "DPGL110TASK";
        KeyObj targetKeyObj = helper.getKeyObj(targetScreenId);
        targetKeyObj.put("TASK_ID", taskId == null ? "" : taskId);
        targetKeyObj.put("REQUIREMENT_ID", requirementId == null ? "" : requirementId);
        targetKeyObj.put("PROJECT_ID", projectId == null ? "" : projectId);
        targetKeyObj.put("PROJECT_ROLE", projectRole == null ? "" : projectRole);

        helper.pageOpen(targetScreenId,targetKeyObj);
    }
}

