package job.screen.dq100aa;

import com.yse.gb.gf.front.html.Span;
import com.yse.gb.util.FormatDate;
import jakarta.enterprise.context.RequestScoped;

import com.yse.gb.gf.front.Creator;
import com.yse.gb.gf.front.CreatorException;
import com.yse.gb.gf.front.KeyObj;
import com.yse.gb.gf.front.ValueBean;

import java.util.Date;



@RequestScoped
public class DQ100AACreator extends Creator {
	
	@Override
	protected void createValueBean(final KeyObj keyObj, final ValueBean valueBean)
			throws CreatorException, Throwable {
		// Write the code here
		System.out.println("====DQCreator100 已被执行====");
		//TIME
		long nowtime = System.currentTimeMillis();
		Date nowDate = new Date(nowtime);
		String strNowDate = FormatDate.get_YYYYMMDDhhmmss(nowDate);
		Span spanNowDate = new Span("NMLLBL_NOW_DATE", strNowDate);
		valueBean.add(spanNowDate);

	}
}
