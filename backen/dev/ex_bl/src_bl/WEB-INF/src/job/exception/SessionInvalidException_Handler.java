package job.exception;

import java.io.IOException;


import com.yse.gb.gf.front.MsgHandleBean;
import com.yse.gb.gf.front.UserMessage;
import com.yse.gb.gf.front.exception.ExceptionHandler;

import biz.grandsight.ex.globalization.Globalization;
import biz.grandsight.ex.ui.bl.multilingual.MessageResources;

public class SessionInvalidException_Handler implements ExceptionHandler {

@Override
public MsgHandleBean execute(Throwable e){
	MsgHandleBean msgBean;
	try {
		MessageResources msgResources = new MessageResources();
		String lang = Globalization.GetDefaultLocaleForMessage();
		String errorMsgId = "MSG_SESSION_INVALID_HANDLER_001";
		String errorMsg = msgResources.getResource(lang, errorMsgId);

		msgBean = new MsgHandleBean(new UserMessage(errorMsg));
	} catch (IOException e1) {
		msgBean = new MsgHandleBean(e1);
	}

	msgBean.setErrorPage("/_SessionErrorPage.html") ;

	return msgBean ;




}
}
