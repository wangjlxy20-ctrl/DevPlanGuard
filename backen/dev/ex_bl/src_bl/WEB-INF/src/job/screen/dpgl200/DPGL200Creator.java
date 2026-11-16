package job.screen.dpgl200;

import biz.grandsight.ex.ui.ac.ShiroUserRole;
import biz.grandsight.ex.ui.ac.util.SessionHelper;
import com.yse.gb.gf.front.html.Input;
import com.yse.gb.gf.front.html.Row;
import com.yse.gb.gf.front.html.Span;
import com.yse.gb.gf.front.html.Table;
import com.yse.gb.gf.front.svg.chart.SvgBarChart;
import com.yse.gb.gf.front.svg.chart.SvgPieChart;
import com.yse.gb.gf.front.svg.chart.SvgChartDefine;
import com.yse.gb.util.FormatDate;
import jakarta.enterprise.context.RequestScoped;

import com.yse.gb.gf.front.Creator;
import com.yse.gb.gf.front.CreatorException;
import com.yse.gb.gf.front.KeyObj;
import com.yse.gb.gf.front.ValueBean;
import jakarta.inject.Inject;
import job.entity.ChartPoint;
import job.entity.Project;
import job.service.DashboardService;
import job.service.ProjectService;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;



@RequestScoped
public class DPGL200Creator extends Creator {
	
	@Inject
	private ProjectService projectService;

	
	@Inject
	private DashboardService dashboardService;

	
	private static final String[] PIE_COLORS = {
			"#4E79A7", "#F28E2B", "#59A14F", "#E15759",
			"#76B7B2", "#EDC948", "#B07AA1", "#FF9DA7" };

	
	private static final Color[] BAR_COLORS = {
			new Color(0x4E79A7), new Color(0xF28E2B),
			new Color(0x59A14F), new Color(0xE15759) };

	@Override
	protected void createValueBean(final KeyObj keyObj, final ValueBean valueBean)
			throws CreatorException, Throwable {
		System.out.println("====DPGL200 Creator executed====");

		// LOGIN INFO (same way as DPGL120PJTMEMCreator)
		ShiroUserRole shiroUserRole = SessionHelper.getShiroUserRole(this);
		String userId = (shiroUserRole == null) ? null : shiroUserRole.getUserId();
		String username = (shiroUserRole == null) ? null : shiroUserRole.getUserName();
		String permission = (shiroUserRole == null) ? null : shiroUserRole.getRoleIdsString();

		// 三张图的数据都按当前登录用户过滤
		Integer myUserId = toIntegerOrNull(userId);

		// SYS TIME
		String now = FormatDate.get_YYYYMMDDhhmmss(new Date(System.currentTimeMillis()));
		valueBean.add(new Span("NMLLBL_NOW_DATE", now));

		// Welcome line: username / permission
		// NOTE: permission & username come from the ShiroUserRole of the logged-in user
		valueBean.add(new Span("NMLLBL_DISPLAY_INFO_USERNAME", username == null ? "" : username));
		valueBean.add(new Span("NMLLBL_DISPLAY_INFO_PERMISSION", permission == null ? "" : permission));


		// Project table
		Table projectTable = new Table("TABLE_PROJECT");

		List<Project> list = new ArrayList<>();
		if (userId != null && !userId.trim().isEmpty()) {

			Project condition = new Project();
			condition.setUserId(Integer.parseInt(userId.trim()));
			List<Project> result = projectService.getMyProjects(condition);
			if (result != null) {
				list = result;
			}
		}
		System.out.println("MY PROJECT COUNT = " + list.size());

		// total requirement count
		valueBean.add(new Span("NMLLBL_TOTAL_REQ", String.valueOf(list.size())));

		int rowNo = 0;
		for (Project p : list) {
			String projectCode = str(p.getProjectCode());
			String projectName = str(p.getProjectName());
			String projectRole = str(p.getProjectRole());
			String projectStatus = str(p.getProjectStatus());
			String endDate = str(p.getPlannedEndDate());
			String projectId = str(p.getProjectId());

			Row row = new Row();

			row.add(new Span("NMLLBL_PROJECT_CODE", projectCode));
			row.add(new Span("NMLLBL_PROJECT_NAME", projectName));
			row.add(new Span("NMLLBL_PROJECT_ROLE", projectRole));
			row.add(new Span("NMLLBL_PROJECT_STATUS", projectStatus));
			row.add(new Span("NMLLBL_PROJECT_END_DATE", endDate));

			// Hidden: this row's project_id, read by the ENTER onclick
			Input hiddenPid = new Input("NMLTXT_PROJECT_ID");
			hiddenPid.setValue(projectId);
			row.add(hiddenPid);

			Input hiddenProjectRole = new Input("NMLTXT_PROJECT_ROLE");
			hiddenProjectRole.setValue(projectRole);
			row.add(hiddenProjectRole);

			// ENTER Command：按钮名带行号后缀，
			Input enterCommand = new Input("BTNCMD_ENTRIES_R_" + rowNo);
			enterCommand.setClassName("BTNCMD");
			enterCommand.setValue("ENTER");
			row.add(enterCommand);

			projectTable.add(row);
			rowNo++;
		}

		valueBean.add(projectTable);

		// 三张 SVG 图（数据都按当前登录用户）
		addDashboardCharts(valueBean, myUserId);
	}

	
	private void addDashboardCharts(final ValueBean valueBean, final Integer userId) {
		if (userId == null) {
			System.out.println("====DPGL200 CHARTS SKIPPED (no login user)====");
			return;
		}

		// 我的任务状态分布（饼图）：task WHERE assignee_id = ? GROUP BY status
		try {
			List<ChartPoint> statusPoints = dashboardService.countMyTaskStatus(userId);
			if (statusPoints == null || statusPoints.isEmpty()) {
				System.out.println("====DPGL200 CHART CHART_MY_TASK_STATUS SKIPPED (no data)====");
			} else {
				double[] data = toData(statusPoints);
				valueBean.add(new SvgPieChart("CHART_MY_TASK_STATUS", SvgChartDefine._ELEMENT_ID,
						170, 115, 120, 70, data, toPieColors(data.length)));
				valueBean.add(new Span("CHART_MY_TASK_LEGEND", toLegend(statusPoints)));
				System.out.println("====DPGL200 CHART CHART_MY_TASK_STATUS = " + data.length
						+ " groups: " + describe(statusPoints) + "====");
			}
		} catch (Exception e) {
			System.out.println("WARN: DPGL200 CHART_MY_TASK_STATUS failed: " + e.getMessage());
		}

		// 我的缺陷严重度分布（饼图）：task WHERE assignee_id = ? AND task_type = 'DEFECT' GROUP BY severity
		try {
			List<ChartPoint> severityPoints = dashboardService.countMyDefectSeverity(userId);
			if (severityPoints == null || severityPoints.isEmpty()) {
				System.out.println("====DPGL200 CHART CHART_MY_DEFECT_SEVERITY SKIPPED (no data)====");
			} else {
				double[] data = toData(severityPoints);
				valueBean.add(new SvgPieChart("CHART_MY_DEFECT_SEVERITY", SvgChartDefine._ELEMENT_ID,
						170, 115, 120, 70, data, toPieColors(data.length)));
				valueBean.add(new Span("CHART_MY_DEFECT_LEGEND", toLegend(severityPoints)));
				System.out.println("====DPGL200 CHART CHART_MY_DEFECT_SEVERITY = " + data.length
						+ " groups: " + describe(severityPoints) + "====");
			}
		} catch (Exception e) {
			System.out.println("WARN: DPGL200 CHART_MY_DEFECT_SEVERITY failed: " + e.getMessage());
		}

		// 我参与项目的任务数（柱状图）：project_member ACTIVE 的项目 LEFT JOIN 需求 / 任务聚合
		try {
			List<ChartPoint> projectPoints = dashboardService.countProjectTaskCount(userId);

			double[] data;
			Color[] colors;
			double max;
			if (projectPoints == null || projectPoints.isEmpty()) {
				// 空数据：画一根 0 的柱子，max 固定 1，避免除零
				data = new double[] { 0 };
				colors = new Color[] { BAR_COLORS[0] };
				max = 1;
			} else {
				data = toData(projectPoints);
				colors = toBarColors(data.length);

				// max 取数据最大值向上取整（全 0 时仍给 1，避免除零）
				double maxValue = 0;
				for (double v : data) {
					if (v > maxValue) {
						maxValue = v;
					}
				}
				max = Math.ceil(maxValue);
				if (max < 1) {
					max = 1;
				}
			}

			valueBean.add(new SvgBarChart("CHART_PROJECT_TASKS", SvgChartDefine._ELEMENT_ID,
					data, colors, 0, max, SvgBarChart._DIRECTION_UPPER));
			valueBean.add(new Span("CHART_PROJECT_LEGEND", toLegend(projectPoints)));
			System.out.println("====DPGL200 CHART CHART_PROJECT_TASKS = " + data.length
					+ " bars, max = " + max + ", data: " + describe(projectPoints) + "====");
		} catch (Exception e) {
			System.out.println("WARN: DPGL200 CHART_PROJECT_TASKS failed: " + e.getMessage());
		}
	}

	
	private String describe(List<ChartPoint> points) {
		if (points == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (ChartPoint p : points) {
			if (sb.length() > 0) {
				sb.append(", ");
			}
			sb.append(p == null ? "" : str(p.getLabel()));
			sb.append('=');
			sb.append(p == null ? "" : str(p.getValue()));
		}
		return sb.toString();
	}

	
	private double[] toData(List<ChartPoint> points) {
		double[] data = new double[points.size()];
		for (int i = 0; i < points.size(); i++) {
			ChartPoint p = points.get(i);
			Integer value = (p == null) ? null : p.getValue();
			data[i] = (value == null) ? 0d : value.doubleValue();
		}
		return data;
	}

	
	private String[] toPieColors(int size) {
		String[] colors = new String[size];
		for (int i = 0; i < size; i++) {
			colors[i] = PIE_COLORS[i % PIE_COLORS.length];
		}
		return colors;
	}

	
	private Color[] toBarColors(int size) {
		Color[] colors = new Color[size];
		for (int i = 0; i < size; i++) {
			colors[i] = BAR_COLORS[i % BAR_COLORS.length];
		}
		return colors;
	}

	
	private Integer toIntegerOrNull(String s) {
		if (s == null || s.trim().isEmpty()) {
			return null;
		}
		try {
			return Integer.valueOf(s.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	
	private String str(Object o) {
		if (o == null) {
			return "";
		}
		String s = String.valueOf(o);
		if ("null".equals(s)) {
			return "";
		}
		return s;
	}
	
	private String toLegend(final List<ChartPoint> points) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < points.size(); i++) {
			if (i > 0) {
				sb.append("  |  ");
			}
			sb.append(points.get(i).getLabel()).append(": ").append(points.get(i).getValue());
		}
		return sb.toString();
	}
}