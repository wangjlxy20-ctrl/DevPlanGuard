package job.screen.dpgl310filelist;

import biz.grandsight.ex.ui.ac.ShiroUserRole;
import biz.grandsight.ex.ui.ac.util.SessionHelper;
import com.yse.gb.gf.front.*;
import com.yse.gb.gf.front.html.Input;
import com.yse.gb.gf.front.html.Row;
import com.yse.gb.gf.front.html.Span;
import com.yse.gb.gf.front.html.Table;
import com.yse.gb.util.FormatDate;
import jakarta.enterprise.context.RequestScoped;
import job.mapper.file.FileListMapper;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * DPGL310FILELIST 文件列表 Creator。
 * 取当前登录用户在其负责/创建的项目下，所有成员已提交的交付文件，
 * 逐行展示项目、需求、任务、提交时间、提交人，并提供下载链接。
 */
@RequestScoped
public class DPGL310FILELISTCreator extends Creator {

	@Override
	protected void createValueBean(final KeyObj keyObj, final ValueBean valueBean)
			throws CreatorException, Throwable {
		System.out.println("====DPGL310FILELIST Creator executed====");

		// SYS TIME
		String now = FormatDate.get_YYYYMMDDhhmmss(new Date(System.currentTimeMillis()));
		valueBean.add(new Span("NMLLBL_NOW_DATE", now));

		// 来源画面（BACK 时返回），入口未传则缺省 DPGL200
		String backScreen = keyObj.get("BACK_SCREEN");
		if (backScreen == null || backScreen.trim().isEmpty()) {
			backScreen = "DPGL200";
		}
		Input hiddenBack = new Input("NMLTXT_BACK_SCREEN");
		hiddenBack.setValue(backScreen.trim());
		valueBean.add(hiddenBack);

		// 当前登录用户（Creator 内用 Shiro，传 this）
		ShiroUserRole shiroUserRole = SessionHelper.getShiroUserRole(this);
		String userId = (shiroUserRole == null) ? null : shiroUserRole.getUserId();

		// 查文件列表（未登录或取不到用户时给空列表，不报错）
		List<Map<String, Object>> list = new java.util.ArrayList<>();
		if (userId != null && !userId.trim().isEmpty()) {
			List<Map<String, Object>> result = new FileListMapper().searchProjectFiles(userId);
			if (result != null) {
				list = result;
			}
		}
		System.out.println("PROJECT FILE COUNT = " + list.size());

		Table fileTable = new Table("TABLE_FILE_LIST");

		if (list.isEmpty()) {
			// 无数据：表格只有表头，另给一条提示
			valueBean.add(new Span("NMLLBL_EMPTY_TIP", "No files in your projects yet."));
			valueBean.add(fileTable);
			return;
		}

		for (Map<String, Object> t : list) {
			String projectName = nullToEmpty(t.get("project_name"));
			String requirementName = nullToEmpty(t.get("requirement_name"));
			String taskCode = nullToEmpty(t.get("task_code"));
			String taskName = nullToEmpty(t.get("task_name"));
			String submittedAt = nullToEmpty(t.get("submitted_at"));
			String submitterName = nullToEmpty(t.get("submitter_name"));
			String storedFile = nullToEmpty(t.get("deliverable_file"));

			Row row = new Row();
			row.add(new Span("NMLLBL_PROJECT_NAME", projectName));
			row.add(new Span("NMLLBL_REQUIREMENT_NAME", requirementName));
			row.add(new Span("NMLLBL_TASK_CODE", taskCode));
			row.add(new Span("NMLLBL_TASK_NAME", taskName));
			row.add(new Span("NMLLBL_SUBMITTED_AT", submittedAt));
			row.add(new Span("NMLLBL_SUBMITTER_NAME", submitterName));

			// 文件列：先隐藏域（onclick 据此取文件名），再可点链接（显示服务器保存名）
			Input hiddenFile = new Input("NMLTXT_DELIVERABLE_FILE");
			hiddenFile.setValue(storedFile);
			row.add(hiddenFile);

			Input fileLink = new Input("LNKMOV_FILE");
			fileLink.setClassName("LINK_TEXT");
			fileLink.setValue(storedFile);
			row.add(fileLink);

			fileTable.add(row);
		}

		// 空提示元素仍注册，保持与 HTML 模板组件一一对应（值为空即不显示文字）
		valueBean.add(new Span("NMLLBL_EMPTY_TIP", ""));
		valueBean.add(fileTable);
	}

	private String nullToEmpty(Object o) {
		return o == null ? "" : String.valueOf(o);
	}
}
