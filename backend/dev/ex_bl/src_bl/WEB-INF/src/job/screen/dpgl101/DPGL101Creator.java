package job.screen.dpgl101;

import com.yse.gb.gf.front.html.Row;
import com.yse.gb.gf.front.html.Span;
import com.yse.gb.gf.front.html.Table;
import com.yse.gb.gf.front.svg.chart.SvgBarChart;
import com.yse.gb.gf.front.svg.chart.SvgChartDefine;
import com.yse.gb.gf.front.svg.chart.SvgPieChart;
import com.yse.gb.util.FormatDate;
import jakarta.enterprise.context.RequestScoped;
import com.yse.gb.gf.front.Creator;
import com.yse.gb.gf.front.CreatorException;
import com.yse.gb.gf.front.KeyObj;
import com.yse.gb.gf.front.ValueBean;
import jakarta.inject.Inject;
import job.entity.ChartPoint;
import job.entity.Project;
import job.entity.Task;
import job.service.DashboardService;
import job.service.ProjectService;
import java.awt.Color;
import java.util.Date;
import java.util.List;




@RequestScoped
public class DPGL101Creator extends Creator {

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

	private long toLong(Object o) {
		return o == null ? 0L : Long.parseLong(String.valueOf(o));
	}

	
	private String str(Object o) {
		if (o == null) {
			return "";
		}
		String s = String.valueOf(o);
		return "null".equals(s) ? "" : s;
	}

	


	@Override
	protected void createValueBean(final KeyObj keyObj, final ValueBean valueBean)
			throws CreatorException, Throwable {
		// Write the code here
		System.out.println("====DQCreator101 being executed====");
		//TIME
		long nowtime = System.currentTimeMillis();
		Date nowDate = new Date(nowtime);
		String strNowDate = FormatDate.get_YYYYMMDDhhmmss(nowDate);
		Span spanNowDate = new Span("NMLLBL_NOW_DATE", strNowDate);
		valueBean.add(spanNowDate);
		//Render and fill in the basic project information
		String projectCode = keyObj.get("PROJECT_CODE_ID");
		System.out.println("DPGL101CREATOR HAS GET THE PROJECT_CODE_ID : " + projectCode);
		if (projectCode == null || projectCode.trim().isEmpty()) {
			valueBean.add(new Span("NMLLBL_PROJECT_CODE", "No project context. Please open a project from the list."));
			return;
		}
		//项目编码组装成实体
		Project key = new Project();
		key.setProjectCode(projectCode);
		Project project = projectService.getProjectDetails(key);

		if(project != null){
			valueBean.add(new Span("NMLLBL_DISPLAY_INFO_PROJECT_CODE",String.valueOf(project.getProjectCode())));
			valueBean.add(new Span("NMLLBL_DISPLAY_INFO_PROJECT_NAME",String.valueOf(project.getProjectName())));
			valueBean.add(new Span("NMLLBL_DISPLAY_INFO_DESCRIPTION",String.valueOf(project.getProjectDescription())));
			valueBean.add(new Span("NMLLBL_DISPLAY_INFO_PROJECT_LEADER",String.valueOf(project.getLeaderName())));
			valueBean.add(new Span("NMLLBL_DISPLAY_INFO_PRIORITY",String.valueOf(project.getPriority())));
			valueBean.add(new Span("NMLLBL_DISPLAY_INFO_STATUS",String.valueOf(project.getStatus())));
			valueBean.add(new Span("NMLLBL_DISPLAY_INFO_START_TIME",String.valueOf(project.getStartDate())));
			valueBean.add(new Span("NMLLBL_DISPLAY_INFO_PLANNED_END_TIME",String.valueOf(project.getPlannedEndDate())));
			valueBean.add(new Span("NMLLBL_DISPLAY_INFO_ACTUAL_END_TIME",String.valueOf(project.getActualEndDate())));
			valueBean.add(new Span("NMLLBL_DISPLAY_INFO_PROJECT_CREATOR",String.valueOf(project.getCreatorName())));
		}

		// 看板图表按「当前项目」取数;projectId 取不到时整块跳过，不影响上面的项目信息
		Integer projectId = (project == null) ? null : project.getProjectId();
		addDashboardCharts(valueBean, projectId);

	}

	
	private void addDashboardCharts(final ValueBean valueBean, final Integer projectId) {
		if (projectId == null) {
			System.out.println("====DPGL101 CHARTS SKIPPED (no projectId)====");
			return;
		}

		// 任务纳期分布（柱状图）：未完成任务按计划结束日相对今天分档
		try {
			List<ChartPoint> duePoints = dashboardService.countProjectDueStatus(projectId);
			if (duePoints == null || duePoints.isEmpty()) {
				System.out.println("====DPGL101 CHART CHART_DUE_STATUS SKIPPED (no data)====");
			} else {
				//把 LIST<ChartPoint> 转成 double[];
				double[] data = toData(duePoints);

				double maxValue = 0;
				for (double v : data) {
					if (v > maxValue) {
						maxValue = v;
					}
				}

				// max 取数据最大值向上取整
				// 全 0 时仍给 1，避免除零
				double max = Math.ceil(maxValue);
				if (max < 1) {
					max = 1;
				}

				SvgBarChart barChart = new SvgBarChart("CHART_DUE_STATUS", SvgChartDefine._ELEMENT_ID,
						data, toBarColors(data.length), 0, max, SvgBarChart._DIRECTION_UPPER);
				barChart.setMargin(12);
				valueBean.add(barChart);
				valueBean.add(new Span("CHART_DUE_LEGEND", toLegend(duePoints)));
				System.out.println("====DPGL101 CHART CHART_DUE_STATUS = " + data.length
						+ " bars, max = " + max + ", data: " + describe(duePoints) + "====");
			}
		} catch (Exception e) {
			System.out.println("WARN: DPGL101 CHART CHART_DUE_STATUS failed: " + e.getMessage());
		}

		//  风险任务 TOP10（表格）：逾期 / 有未关闭缺陷的未完成任务
		addRiskTaskTable(valueBean, projectId);

		// 缺陷严重度分布（饼图）：当前项目的 DEFECT 任务按 severity 分组。
		//    容器独立，从 (0,0) 开始画
		try {
			List<ChartPoint> severityPoints = dashboardService.countProjectDefectSeverity(projectId);
			if (severityPoints == null || severityPoints.isEmpty()) {
				System.out.println("====DPGL101 CHART CHART_DEFECT_SEVERITY SKIPPED (no data)====");
			} else {
				double[] data = toData(severityPoints);
				valueBean.add(new SvgPieChart("CHART_DEFECT_SEVERITY", SvgChartDefine._ELEMENT_ID,
						20, 20, 360, 190, data, toPieColors(data.length)));
				valueBean.add(new Span("CHART_SEVERITY_LEGEND", toLegend(severityPoints)));
				System.out.println("====DPGL101 CHART CHART_DEFECT_SEVERITY = " + data.length
						+ " groups: " + describe(severityPoints) + "====");
			}
		} catch (Exception e) {
			System.out.println("WARN: DPGL101 CHART CHART_DEFECT_SEVERITY failed: " + e.getMessage());
		}
	}

	
	private void addRiskTaskTable(final ValueBean valueBean, final Integer projectId) {
		try {
			List<Task> riskTasks = dashboardService.getProjectRiskTasks(projectId);
			if (riskTasks == null || riskTasks.isEmpty()) {
				System.out.println("====DPGL101 TABLE TABLE_RISK_TASKS SKIPPED (no data)====");
				return;
			}

			Table riskTaskTable = new Table("TABLE_RISK_TASKS");

			// 表头已在 DPGL101.html定义
			//add 数据行
			// 每行由 <tbody> 里的模板 <tr> 循环渲染。
			StringBuilder log = new StringBuilder();
			for (Task task : riskTasks) {
				if (task == null) {
					continue;
				}

				String assignee = str(task.getAssigneeName());
				Integer daysLeft = task.getDaysLeft();
				Integer openDefects = task.getOpenDefects();

				Row row = new Row();
				row.add(new Span("NMLLBL_RISK_TASK_CODE", str(task.getTaskCode())));
				row.add(new Span("NMLLBL_RISK_TASK_NAME", str(task.getTaskName())));
				row.add(new Span("NMLLBL_RISK_TASK_STATUS", str(task.getStatus())));
				row.add(new Span("NMLLBL_RISK_TASK_ASSIGNEE", assignee.isEmpty() ? "-" : assignee));
				row.add(new Span("NMLLBL_RISK_TASK_DAYS_LEFT",
						(daysLeft == null || daysLeft < 0) ? "-" : String.valueOf(daysLeft)));
				row.add(new Span("NMLLBL_RISK_TASK_OPEN_DEFECTS",
						openDefects == null ? "-" : String.valueOf(openDefects)));
				riskTaskTable.add(row);

				if (log.length() > 0) {
					log.append(", ");
				}
				log.append(str(task.getTaskCode()));
				log.append("(daysLeft=").append(daysLeft == null ? "-" : String.valueOf(daysLeft));
				log.append(", openDefects=")
						.append(openDefects == null ? "-" : String.valueOf(openDefects))
						.append(')');
			}

			valueBean.add(riskTaskTable);
			System.out.println("====DPGL101 TABLE TABLE_RISK_TASKS = " + riskTasks.size()
					+ " rows: " + log + "====");
		} catch (Exception e) {
			System.out.println("WARN: DPGL101 TABLE TABLE_RISK_TASKS failed: " + e.getMessage());
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
			sb.append(p == null || p.getLabel() == null ? "" : p.getLabel());
			sb.append('=');
			sb.append(p == null || p.getValue() == null ? "" : p.getValue());
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

	private String toTextBar(final List<ChartPoint> points) {
		int max = 1;
		for (ChartPoint p : points) {
			if (p.getValue() != null && p.getValue() > max) {
				max = p.getValue();
			}
		}
		StringBuilder sb = new StringBuilder();
		for (ChartPoint p : points) {
			int v = (p.getValue() == null) ? 0 : p.getValue();
			int blocks = (int) Math.round(v * 20.0 / max);
			sb.append(p.getLabel()).append(" ");
			for (int i = 0; i < blocks; i++) {
				sb.append('\u2588');
			}
			sb.append(" ").append(v).append("　　");
		}
		return sb.toString();
	}

	private String[] toLabelsEn(final List<ChartPoint> points) {
		String[] labels = new String[points.size()];
		for (int i = 0; i < points.size(); i++) {
			String l = points.get(i).getLabel();
			if ("已逾期".equals(l)) {
				labels[i] = "Overdue";
			} else if ("7天内".equals(l)) {
				labels[i] = "<=7d";
			} else if ("30天内".equals(l)) {
				labels[i] = "<=30d";
			} else if ("30天以上".equals(l)) {
				labels[i] = ">30d";
			} else if ("未排期".equals(l)) {
				labels[i] = "No due";
			} else {
				labels[i] = l;
			}
		}
		return labels;
	}

	private String toLegend(final List<ChartPoint> points) {
		String[] labels = toLabelsEn(points);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < points.size(); i++) {
			if (i > 0) {
				sb.append("  |  ");
			}
			sb.append(labels[i]).append(": ").append(points.get(i).getValue());
		}
		return sb.toString();
	}
}