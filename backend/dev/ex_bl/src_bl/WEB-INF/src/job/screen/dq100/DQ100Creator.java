package job.screen.dq100;

import com.yse.gb.gf.front.Creator;
import com.yse.gb.gf.front.CreatorException;
import com.yse.gb.gf.front.KeyObj;
import com.yse.gb.gf.front.ValueBean;
import com.yse.gb.gf.front.html.Span;
import com.yse.gb.util.FormatDate;

import jakarta.enterprise.context.RequestScoped;

import java.util.Date;

/**
 * 登录画面（DQ100，DevQuality Login）。
 *
 * <p>只负责把画面上的动态内容填好：当前时间。认证逻辑在
 * {@link COMMAND_BTNCMD_LOGIN} 里，走框架的 Shiro。</p>
 */
@RequestScoped
public class DQ100Creator extends Creator {

    @Override
    protected void createValueBean(final KeyObj keyObj, final ValueBean valueBean)
            throws CreatorException, Throwable {

        System.out.println("====DQ100 login screen being executed====");
        String now = FormatDate.get_YYYYMMDDhhmmss(new Date());
        valueBean.add(new Span("NMLLBL_NOW_DATE", now));
    }
}
