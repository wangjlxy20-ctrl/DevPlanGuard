package job.screen.dpgl210sublist;

import com.yse.gb.gf.bld.front.BLDCommandImpl;
import com.yse.gb.gf.front.CommandException;
import com.yse.gb.gf.front.CommandHelper;
import com.yse.gb.gf.front.InputException;
import com.yse.gb.gf.front.KeyObj;
import jakarta.enterprise.context.RequestScoped;

/**
 * 配列第一列「Task Code」LNKMOV_TASK_CODE。
 * 前端 linkOpen 把被点行的 task_id 传过来，这里放入 keyObj，
 * 跳转到任务详情页 DPGL120TASKDETAIL。
 */
@RequestScoped
public class COMMAND_LNKMOV_TASK_CODE extends BLDCommandImpl {

    @Override
    protected void perform(final CommandHelper helper, final String nextScreenId, final KeyObj keyObj)
            throws InputException, CommandException, Throwable {
        // 详情页 Creator 直接从 keyObj 取 TASK_ID，无需额外处理
        // 被点行的 task_id（前端 linkOpen 第二参数）
        String taskId = helper.getLinkValue();
        System.out.println("OPEN TASK DETAIL, taskId=" + taskId);
        String targetScreenId = "DPGL120TASKDETAIL";
        KeyObj targetKeyObj = helper.getKeyObj(targetScreenId);
        targetKeyObj.put("TASK_ID", taskId == null ? "" : taskId);
        helper.pageOpen(targetScreenId,targetKeyObj);


    }
}
