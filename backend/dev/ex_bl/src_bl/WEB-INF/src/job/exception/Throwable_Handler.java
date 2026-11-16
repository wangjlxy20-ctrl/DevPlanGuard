package job.exception;

// import java.io.PrintWriter;
import java.io.IOException;

import com.yse.gb.gf.front.MsgHandleBean;
import com.yse.gb.gf.front.UserMessage;
import com.yse.gb.gf.front.exception.ExceptionFileWriter;
import com.yse.gb.gf.front.exception.ExceptionHandler;
// import com.yse.gb.gf.com.io.CreateFileInfo;

public class Throwable_Handler implements ExceptionHandler {

@Override
public MsgHandleBean execute(Throwable e){

	String text  = ""  ;
	String color = ""  ;
	String link  = ""  ;

	//エラー標準出力
	java.io.PrintStream printStream = System.out;
	e.printStackTrace(printStream);

	//エラーメッセージセット
	text = e.toString();

	//エラー詳細ファイル作成
	try{
		String fileUrl = ExceptionFileWriter.execute(e);
		link = fileUrl ;
	}catch(IOException exp){
		exp.printStackTrace(printStream);
	}

	MsgHandleBean msgBean = new MsgHandleBean(new UserMessage(text,color,link));

	return msgBean ;
}
}
