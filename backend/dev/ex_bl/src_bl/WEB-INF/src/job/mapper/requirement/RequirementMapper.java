package job.mapper.requirement;

import job.databaseSource.connectDS;
import job.entity.Project;
import job.entity.Requirement;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import javax.naming.NamingException;
import java.util.List;


public class RequirementMapper {

    private final QueryRunner qr = connectDS.getQueryRunner();

    public RequirementMapper() throws NamingException {
    }


    public Requirement selectById(Requirement r) throws Exception {
        String sql = "SELECT * "
                + "FROM requirement WHERE requirement_id = ?";
        return qr.query(sql, new BeanHandler<>(Requirement.class, connectDS.BEAN_ROW_PROCESSOR),
                r.getRequirementId());
    }


    public String selectNameById(Requirement r) throws Exception {
        String sql = "SELECT requirement_name FROM requirement WHERE requirement_id = ?";
        String name = qr.query(sql, new ScalarHandler<String>(), r.getRequirementId());
        return name == null ? "" : name;
    }


    public String selectCodeById(Requirement r) throws Exception {
        String sql = "SELECT requirement_code FROM requirement WHERE requirement_id = ? ";
        String reqCode = qr.query(sql, new ScalarHandler<String>(), r.getRequirementId());
        return reqCode == null ? "" : reqCode;
    }


    public void insert(Requirement r) throws Exception {
        String sql = "INSERT INTO requirement ("
                + "project_id, "
                + "requirement_code, "
                + "requirement_name, "
                + "description, "
                + "priority, "
                + "planned_start_date, "
                + "planned_end_date, "
                + "created_by"
                + ") VALUES (?, ?, ?, ?, ?, ?,?,?)";
        qr.update(sql,
                r.getProjectId(),
                r.getRequirementCode(),
                r.getRequirementName(),
                r.getDescription(),
                r.getPriority(),
                r.getPlannedStartDate(),
                r.getPlannedEndDate(),
                r.getCreatedBy());
    }


    public List<Requirement> selectByProject(Requirement condition) throws Exception {
        String sql = "SELECT r.requirement_id, r.requirement_code, r.requirement_name, "
                + "       r.project_id, p.project_code, p.project_name, "
                + "       p.leader_id, u.username AS owner_name, "
                + "       r.priority, "
                + "       r.planned_start_date, r.planned_end_date, "
                + "       r.actual_start_date, r.actual_end_date, r.status "
                + "FROM requirement r "
                + "JOIN project p ON r.project_id = p.project_id "
                + "LEFT JOIN user_info u ON p.leader_id = u.user_id "
                + "WHERE r.project_id = ? "
                + "ORDER BY r.requirement_code";
        return qr.query(sql, new BeanListHandler<>(Requirement.class, connectDS.BEAN_ROW_PROCESSOR),
                condition.getProjectId());
    }


    public Requirement selectTaskStats(Requirement r) throws Exception {
        String sql = "SELECT COUNT(*) AS total_tasks, "
                + "       SUM(CASE WHEN status <> 'COMPLETED' THEN 1 ELSE 0 END) AS open_tasks "
                + "FROM task WHERE requirement_id = ?";
        return qr.query(sql, new BeanHandler<>(Requirement.class, connectDS.BEAN_ROW_PROCESSOR),
                r.getRequirementId());
    }


    public Requirement selectTaskStatsByCode(Requirement r) throws Exception {
        String sql = "SELECT COUNT(*) AS total_tasks, "
                + "       SUM(CASE WHEN t.status <> 'COMPLETED' THEN 1 ELSE 0 END) AS open_tasks "
                + "FROM task t "
                + "JOIN requirement r ON t.requirement_id = r.requirement_id "
                + "WHERE r.project_id = ? AND r.requirement_code = ?";
        return qr.query(sql, new BeanHandler<>(Requirement.class, connectDS.BEAN_ROW_PROCESSOR),
                r.getProjectId(), r.getRequirementCode());
    }


    public int start(Requirement r) throws Exception {
        String sql = "UPDATE requirement "
                + "SET status = 'IN_PROGRESS', actual_start_date = CURDATE() "
                + "WHERE project_id = ? AND requirement_code = ? "
                + "AND actual_start_date IS NULL";
        return qr.update(sql, r.getProjectId(), r.getRequirementCode());
    }


    public int complete(Requirement r) throws Exception {
        String sql = "UPDATE requirement "
                + "SET status = 'COMPLETED', actual_end_date = CURDATE() "
                + "WHERE project_id = ? AND requirement_code = ? "
                + "AND actual_end_date IS NULL";
        return qr.update(sql, r.getProjectId(), r.getRequirementCode());
    }


    public List<Requirement> selectMyLeadRequirements(String userId, String projectId) throws Exception {
        String sql = "SELECT r.priority, r.requirement_code, r.requirement_id, r.requirement_name, r.project_id, "
                + "p.project_name, p.planned_end_date AS project_due, pm.project_role "
                + "FROM requirement r "
                + "JOIN project p ON r.project_id = p.project_id "
                + "JOIN project_member pm ON pm.project_id = r.project_id "
                + "WHERE pm.user_id = ? "
                + "  AND r.project_id = ? "
                + "  AND pm.project_role = 'LEAD' "
                + "  AND pm.member_status = 'ACTIVE' "
                + "  AND p.status <> 'COMPLETED' "
                + "ORDER BY r.project_id, r.requirement_id";
        return qr.query(sql, new BeanListHandler<>(Requirement.class, connectDS.BEAN_ROW_PROCESSOR),
                Integer.parseInt(userId.trim()), Integer.parseInt(projectId.trim()));
    }


    public Project selectProject(Project p) throws Exception {
        String sql = "SELECT p.project_id, p.project_code, p.project_name, "
                + "       p.leader_id, u.username AS owner_name "
                + "FROM project p "
                + "LEFT JOIN user_info u ON p.leader_id = u.user_id "
                + "WHERE p.project_id = ?";
        return qr.query(sql, new BeanHandler<>(Project.class, connectDS.BEAN_ROW_PROCESSOR),
                p.getProjectId());
    }

    // ================= 新增：编辑回显 / 更新 =================

    /**
     * 编辑回显：按 (project_id, requirement_code) 查该需求完整一行。
     * 项目内 requirement_code 唯一，可精确定位。
     */
    public Requirement selectByProjectAndCode(String projectId, String reqCode) throws Exception {
        String sql = "SELECT * "
                + "FROM requirement "
                + "WHERE project_id = ? AND requirement_code = ?";
        return qr.query(sql, new BeanHandler<>(Requirement.class, connectDS.BEAN_ROW_PROCESSOR),
                Integer.parseInt(projectId.trim()), reqCode);
    }

    /**
     * 编辑更新：只更新允许在表单修改的 5 个字段。
     * 不更新 project_id / requirement_code / created_by / status / actual_*（流程字段）。
     * 定位仍用 project_id + requirement_code。
     */
    public int update(Requirement r) throws Exception {
        String sql = "UPDATE requirement SET "
                + "requirement_name = ?, "
                + "description = ?, "
                + "priority = ?, "
                + "planned_start_date = ?, "
                + "planned_end_date = ? "
                + "WHERE project_id = ? AND requirement_code = ?";
        return qr.update(sql,
                r.getRequirementName(),
                r.getDescription(),
                r.getPriority(),
                r.getPlannedStartDate(),
                r.getPlannedEndDate(),
                r.getProjectId(),
                r.getRequirementCode());
    }
}
