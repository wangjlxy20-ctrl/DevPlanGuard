package job.screen.dpgl_dyn_dashboard;

import java.util.List;
import java.util.Map;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import com.yse.gb.gf.front.Creator;
import com.yse.gb.gf.front.CreatorException;
import com.yse.gb.gf.front.KeyObj;
import com.yse.gb.gf.front.ValueBean;
import com.yse.gb.gf.front.html.Option;
import com.yse.gb.gf.front.html.Select;
import com.yse.gb.gf.front.html.Span;
import com.yse.gb.util.FormatDate;
import biz.grandsight.ex.ui.ac.ShiroUserRole;
import biz.grandsight.ex.ui.ac.util.SessionHelper;
import job.mapper.dashboard.LeaderDashboardMapper;


@RequestScoped
public class DPGL_DYN_DASHBOARDCreator extends Creator {

	@Inject
	private HttpServletRequest request;

	public static final String SESSION_PROJECT_ID = "DASH_PROJECT_ID";

	@Override
	protected void createValueBean(final KeyObj keyObj, final ValueBean valueBean)
			throws CreatorException, Throwable {

		System.out.println("====DPGL101 Creator executed====");

		// 1. 系统时间
		String now = FormatDate.get_YYYYMMDDhhmmss(new java.util.Date(System.currentTimeMillis()));
		valueBean.add(new Span("NMLLBL_NOW_DATE", now));

		// 2. 当前登录用户（Shiro，Creator 内可靠）
		ShiroUserRole role = SessionHelper.getShiroUserRole(this);
		String userId = (role == null) ? null : role.getUserId();
		if (userId == null || userId.trim().isEmpty()) {
			valueBean.add(new Span("NMLLBL_LOGIN_HINT", "No login user. Please sign in again."));
			return;
		}
		Integer currentUserId = Integer.valueOf(userId.trim());

		// 3. 查我管理的项目
		LeaderDashboardMapper mapper = new LeaderDashboardMapper();
		List<Map<String, Object>> projects = mapper.selectManagedProjects(currentUserId);

		Select projectSelect = new Select("NMLDLD_PROJECT");

		// 没有任何项目：给一个空下拉，下方图形数据源会自行兜底
		if (projects == null || projects.isEmpty()) {
			System.out.println("====DPGL101 no managed project====");
			valueBean.add(projectSelect);
			valueBean.add(new Span("NMLLBL_PROJECT_HINT", "You are not managing any project."));
			return;
		}

		// 4. 填充下拉：value=project_id，显示 project_code - project_name
		for (Map<String, Object> p : projects) {
			String pid = String.valueOf(p.get("project_id"));
			String code = String.valueOf(p.get("project_code"));
			String name = String.valueOf(p.get("project_name"));
			projectSelect.add(new Option(pid, code + " - " + name));
		}

		// 5. 确定当前项目：session 合法就用，否则默认第一个
		Integer currentProjectId = null;
		HttpSession session = request.getSession();
		Object sessPid = session.getAttribute(SESSION_PROJECT_ID);
		Integer sessProjectId = toInteger(sessPid);
		Integer firstProjectId = toInteger(projects.get(0).get("project_id"));

		if (sessProjectId != null && containsProject(projects, sessProjectId)) {
			currentProjectId = sessProjectId;
		} else {
			currentProjectId = firstProjectId;
			session.setAttribute(SESSION_PROJECT_ID, currentProjectId);
		}

		// Select 无 setValue，用 selected 设选中项
		projectSelect.selected(String.valueOf(currentProjectId));
		valueBean.add(projectSelect);

		System.out.println("====DPGL101 current project_id=" + currentProjectId
				+ " , managed count=" + projects.size() + "====");
	}

	private boolean containsProject(final List<Map<String, Object>> projects, final Integer projectId) {
		if (projectId == null) {
			return false;
		}
		for (Map<String, Object> p : projects) {
			Integer pid = toInteger(p.get("project_id"));
			if (projectId.equals(pid)) {
				return true;
			}
		}
		return false;
	}

	private Integer toInteger(final Object o) {
		if (o == null) {
			return null;
		}
		String s = String.valueOf(o).trim();
		if (s.isEmpty() || "null".equalsIgnoreCase(s)) {
			return null;
		}
		try {
			return Integer.valueOf(s);
		} catch (NumberFormatException e) {
			return null;
		}
	}
}

