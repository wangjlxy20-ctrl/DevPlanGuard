package job.mapper.dashboard;

import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;

import job.databaseSource.connectDS;

import javax.naming.NamingException;

public class LeaderDashboardMapper {

    private final QueryRunner qr = connectDS.getQueryRunner();

    public LeaderDashboardMapper() throws NamingException {
    }


    // 下拉框：当前用户管理（leader 或 创建人）的所有项目

    public List<Map<String, Object>> selectManagedProjects(final Integer userId) throws Exception {
        String sql = "SELECT project_id, project_code, project_name "
                + "FROM project "
                + "WHERE leader_id = ? OR created_by = ? "
                + "ORDER BY project_id";
        return qr.query(sql, new MapListHandler(), userId, userId);
    }


    // 完成率仪表盘（rotate）：总任务数、已完成数

    public Map<String, Object> selectCompletion(final Integer projectId) throws Exception {
        String sql = "SELECT "
                + "COUNT(*) AS total, "
                + "SUM(CASE WHEN t.status = 'COMPLETED' THEN 1 ELSE 0 END) AS completed "
                + "FROM task t "
                + "JOIN requirement r ON t.requirement_id = r.requirement_id "
                + "WHERE r.project_id = ?";
        return qr.query(sql, new MapHandler(), projectId);
    }


    //任务状态分布（垂直柱图 level）

    public List<Map<String, Object>> selectStatusDistribution(final Integer projectId) throws Exception {
        String sql = "SELECT t.status AS label, COUNT(*) AS cnt "
                + "FROM task t "
                + "JOIN requirement r ON t.requirement_id = r.requirement_id "
                + "WHERE r.project_id = ? "
                + "GROUP BY t.status "
                + "ORDER BY FIELD(t.status, 'TODO', 'IN_PROGRESS', 'SUBMITTED', 'COMPLETED')";
        return qr.query(sql, new MapListHandler(), projectId);
    }


    // 纳期分布（垂直柱图 level + color）：未完成任务按计划结束日分档

    public List<Map<String, Object>> selectDueDistribution(final Integer projectId) throws Exception {
        String sql = "SELECT CASE "
                + "WHEN t.planned_end_date IS NULL THEN '未排期' "
                + "WHEN DATEDIFF(t.planned_end_date, CURDATE()) < 0 THEN '已逾期' "
                + "WHEN DATEDIFF(t.planned_end_date, CURDATE()) <= 7 THEN '7天内' "
                + "WHEN DATEDIFF(t.planned_end_date, CURDATE()) <= 30 THEN '30天内' "
                + "ELSE '30天以上' "
                + "END AS label, COUNT(*) AS cnt "
                + "FROM task t "
                + "JOIN requirement r ON t.requirement_id = r.requirement_id "
                + "WHERE r.project_id = ? AND t.status <> 'COMPLETED' "
                + "GROUP BY label "
                + "ORDER BY FIELD(label, '已逾期', '7天内', '30天内', '30天以上', '未排期')";
        return qr.query(sql, new MapListHandler(), projectId);
    }

    //成员负载（水平柱图 level）：每个成员手上的任务数

    public List<Map<String, Object>> selectMemberWorkload(final Integer projectId) throws Exception {
        String sql = "SELECT u.user_id, u.username, COUNT(t.task_id) AS cnt "
                + "FROM task t "
                + "JOIN requirement r ON t.requirement_id = r.requirement_id "
                + "LEFT JOIN user_info u ON t.assignee_id = u.user_id "
                + "WHERE r.project_id = ? AND t.assignee_id IS NOT NULL "
                + "GROUP BY u.user_id, u.username "
                + "ORDER BY cnt DESC, u.user_id";
        return qr.query(sql, new MapListHandler(), projectId);
    }


    //关键数字卡片（text + color）：待审批 / 待分配 / 逾期 / 未关闭缺陷

    public Map<String, Object> selectKeyMetrics(final Integer projectId) throws Exception {
        String sql = "SELECT "
                + "SUM(CASE WHEN t.status = 'SUBMITTED' THEN 1 ELSE 0 END) AS pending_review, "
                + "SUM(CASE WHEN t.assignee_id IS NULL AND t.status = 'TODO' THEN 1 ELSE 0 END) AS unassigned, "
                + "SUM(CASE WHEN t.planned_end_date < CURDATE() AND t.status <> 'COMPLETED' THEN 1 ELSE 0 END) AS overdue, "
                + "SUM(CASE WHEN t.task_type = 'DEFECT' AND t.status <> 'COMPLETED' THEN 1 ELSE 0 END) AS open_defects "
                + "FROM task t "
                + "JOIN requirement r ON t.requirement_id = r.requirement_id "
                + "WHERE r.project_id = ?";
        return qr.query(sql, new MapHandler(), projectId);
    }


    // 缺陷严重度（柱图 level + color）

    public List<Map<String, Object>> selectDefectSeverity(final Integer projectId) throws Exception {
        String sql = "SELECT COALESCE(t.severity, '未分级') AS label, COUNT(*) AS cnt "
                + "FROM task t "
                + "JOIN requirement r ON t.requirement_id = r.requirement_id "
                + "WHERE r.project_id = ? AND t.task_type = 'DEFECT' "
                + "GROUP BY t.severity "
                + "ORDER BY FIELD(COALESCE(t.severity, '未分级'), "
                + "'CRITICAL', 'HIGH', 'MEDIUM', 'LOW', '未分级')";
        return qr.query(sql, new MapListHandler(), projectId);
    }
}
