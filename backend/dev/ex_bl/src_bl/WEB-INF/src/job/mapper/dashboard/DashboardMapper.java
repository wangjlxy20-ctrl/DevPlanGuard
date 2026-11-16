package job.mapper.dashboard;

import job.databaseSource.connectDS;
import job.entity.ChartPoint;
import job.entity.Task;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import java.util.List;


public class DashboardMapper {

    //DPGL200
    //我的任务状态分布：task 按 status 分组计数
    public List<ChartPoint> countMyTaskStatus(Integer userId) throws Exception {
        QueryRunner qr = connectDS.getQueryRunner();

        String sql = "SELECT status AS label, COUNT(*) AS `value` "
                + "FROM task "
                + "WHERE assignee_id = ? "
                + "GROUP BY status "
                + "ORDER BY status";

        return qr.query(sql, new BeanListHandler<>(ChartPoint.class, connectDS.BEAN_ROW_PROCESSOR),
                userId);
    }

    //DPGL200
    //我的缺陷严重度分布：我的 DEFECT 任务按 severity 分组计数
    public List<ChartPoint> countMyDefectSeverity(Integer userId) throws Exception {
        QueryRunner qr = connectDS.getQueryRunner();

        String sql = "SELECT severity AS label, COUNT(*) AS `value` "
                + "FROM task "
                + "WHERE assignee_id = ? AND task_type = 'DEFECT' "
                + "GROUP BY severity "
                + "ORDER BY severity";

        return qr.query(sql, new BeanListHandler<>(ChartPoint.class, connectDS.BEAN_ROW_PROCESSOR),
                userId);
    }

    //DPGL200
    //我参与项目 且 分配给我 的任务数
    public List<ChartPoint> countProjectTaskCount(Integer userId) throws Exception {
        QueryRunner qr = connectDS.getQueryRunner();

        String sql = "SELECT p.project_name AS label, COUNT(t.task_id) AS `value` "
                + "FROM project p "
                + "JOIN project_member pm ON pm.project_id = p.project_id "
                + "JOIN requirement r ON r.project_id = p.project_id "
                + "JOIN task t ON t.requirement_id = r.requirement_id "
                + "WHERE pm.user_id = ? AND pm.member_status = 'ACTIVE' "
                + "AND t.assignee_id = ? "
                + "GROUP BY p.project_id, p.project_name "
                + "ORDER BY p.project_id";

        return qr.query(sql, new BeanListHandler<>(ChartPoint.class, connectDS.BEAN_ROW_PROCESSOR),
                userId,userId);
    }

    //DPGL101
    //当前项目的任务纳期分布（柱状图）：只看未完成的任务，按计划结束日相对今天分档
    public List<ChartPoint> countProjectDueStatus(Integer projectId) throws Exception {
        QueryRunner qr = connectDS.getQueryRunner();

        String sql = "SELECT CASE "
                + "WHEN t.planned_end_date IS NULL THEN '未排期' "
                + "WHEN DATEDIFF(t.planned_end_date, CURDATE()) < 0 THEN '已逾期' "
                + "WHEN DATEDIFF(t.planned_end_date, CURDATE()) <= 7 THEN '7天内' "
                + "WHEN DATEDIFF(t.planned_end_date, CURDATE()) <= 30 THEN '30天内' "
                + "ELSE '30天以上' "
                + "END AS label, COUNT(*) AS `value` "
                + "FROM task t "
                + "JOIN requirement r ON t.requirement_id = r.requirement_id "
                + "WHERE r.project_id = ? AND t.status <> 'COMPLETED' "
                + "GROUP BY label "
                + "ORDER BY FIELD(label,'已逾期','7天内','30天内','30天以上','未排期')";

        return qr.query(sql, new BeanListHandler<>(ChartPoint.class, connectDS.BEAN_ROW_PROCESSOR),
                projectId);
    }

    //DPGL101
    //当前项目的 未完成任务 风险分布（饼图）
    //当前项目的缺陷严重度分布,task_type = 'DEFECT' 的任务
    public List<ChartPoint> countProjectDefectSeverity(Integer projectId) throws Exception {
        QueryRunner qr = connectDS.getQueryRunner();

        String sql = "SELECT COALESCE(t.severity, '未分级') AS label, COUNT(*) AS `value` "
                + "FROM task t "
                + "JOIN requirement r ON t.requirement_id = r.requirement_id "
                + "WHERE r.project_id = ? AND t.task_type = 'DEFECT' "
                + "GROUP BY label "
                + "ORDER BY label";

        return qr.query(sql, new BeanListHandler<>(ChartPoint.class, connectDS.BEAN_ROW_PROCESSOR),
                projectId);
    }


    //DPGL101
    // 当前项目里 ， 没有完成的任务中 ，已经逾期 或者有 没有关闭缺陷子任务的， 取前10个，
    //按逾期天数 和 缺陷数 排序;
    public List<Task> selectProjectRiskTasks(Integer projectId) throws Exception {
        QueryRunner qr = connectDS.getQueryRunner();

        String sql = "SELECT t.task_id, t.task_code, t.task_name, t.status, "
                + "t.planned_end_date, "
                + "DATEDIFF(CURDATE(), t.planned_end_date) AS days_left, "
                + "u.username AS assignee_name, "
                + "(SELECT COUNT(*) FROM task d WHERE d.parent_id = t.task_id "
                + "AND d.task_type = 'DEFECT' AND d.status <> 'COMPLETED') AS open_defects "
                + "FROM task t "
                + "JOIN requirement r ON t.requirement_id = r.requirement_id "
                + "LEFT JOIN user_info u ON t.assignee_id = u.user_id "
                + "WHERE r.project_id = ? "
                + "AND t.status <> 'COMPLETED' "
                + "AND ( t.planned_end_date < CURDATE() "
                + "OR EXISTS (SELECT 1 FROM task d WHERE d.parent_id = t.task_id "
                + "AND d.task_type = 'DEFECT' AND d.status <> 'COMPLETED') ) "
                + "ORDER BY days_left DESC, open_defects DESC "
                + "LIMIT 10";

        return qr.query(sql, new BeanListHandler<>(Task.class, connectDS.BEAN_ROW_PROCESSOR),
                projectId);
    }
}
