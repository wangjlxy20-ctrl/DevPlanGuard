package job.screen.dpgl100;

import com.yse.gb.gf.front.*;
import jakarta.enterprise.context.RequestScoped;


@RequestScoped
public class COMMAND_BTNCMD_BACK extends com.yse.gb.gf.bld.front.BLDCommandImpl {
	
	@Override
	protected void perform(final CommandHelper helper, final String nextScreenId, final KeyObj keyObj)
			throws InputException, CommandException, Throwable {

		System.out.println(">>>DPGL100 COMMAND_BTNCMD_BACK : THIS IS THE FIRST SCREEN ");

		throw new CommandException(new UserMessage("THIS IS THE FIRST SCREEN FOR YOU!"));

	}
}
