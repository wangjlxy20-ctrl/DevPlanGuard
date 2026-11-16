package job.mapper.project;

import job.databaseSource.connectDS;
import job.entity.Project;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ProjectMapper {
    public Map<String, Object> countReqAndMember(String projectId) throws Exception {

        QueryRunner qr = connectDS.getQueryRunner();
        // 相关子查询：分别从 requirement、project_member 统计，外层锚定当前 project_id
        String sql = "SELECT "
                + "(SELECT COUNT(*) FROM requirement r "
                + "   WHERE r.project_id = p.project_id) AS req_count, "
                + "(SELECT COUNT(*) FROM project_member pm "
                + "   WHERE pm.project_id = p.project_id "
                + "     AND pm.member_status = 'ACTIVE') AS member_count "
                + "FROM project p "
                + "WHERE p.project_id = ?";

        return qr.query(sql, new MapHandler(), Integer.parseInt(projectId.trim()));
    }


    public Project selectByCode(Project project) throws Exception {
        QueryRunner qr = connectDS.getQueryRunner();

        String sql = "SELECT p.project_id,p.project_code,p.project_name,p.project_description,p.start_date,p.planned_end_date,p.actual_end_date,status,priority, "
                + "u1.username AS leader_name,u2.username AS creator_name "
                + "FROM project p "
                + "LEFT JOIN user_info u1 ON p.leader_id = u1.user_id "
                + "LEFT JOIN user_info u2 ON p.created_by = u2.user_id "
                + "WHERE p.project_code = ?";
        return qr.query(sql, new BeanHandler<>(Project.class, connectDS.BEAN_ROW_PROCESSOR),
                project.getProjectCode());
    }


    public Project selectById(Project project) throws Exception {
        QueryRunner qr = connectDS.getQueryRunner();

        String sql = "SELECT project_id, project_code, project_name "
                + "FROM project WHERE project_id = ?";
        return qr.query(sql, new BeanHandler<>(Project.class, connectDS.BEAN_ROW_PROCESSOR),
                project.getProjectId());
    }

    public int selectIdByCode(Project project) throws Exception {
        QueryRunner qr = connectDS.getQueryRunner();

        String sql = "SELECT project_id FROM project WHERE project_code = ?";
        return qr.query(sql, new ScalarHandler<Integer>(), project.getProjectCode());
    }

    public void insert(Project project) throws Exception {
        QueryRunner qr = connectDS.getQueryRunner();

        String sql = "INSERT INTO project (project_code,project_name,project_description,leader_id,start_date,planned_end_date,priority,created_by) "
                + "VALUES(?,?,?,?,?,?,?,?)";
        qr.update(sql,
                project.getProjectCode(),
                project.getProjectName(),
                project.getProjectDescription(),
                project.getLeaderId(),
                project.getStartDate(),
                project.getPlannedEndDate(),
                project.getPriority(),
                project.getCreatedBy());
    }


    public List<Project> searchLeaderProjects(Project condition, String keyword) throws Exception {
        QueryRunner qr = connectDS.getQueryRunner();

        StringBuilder sql = new StringBuilder(
                "SELECT project_code, project_name,project_id FROM project WHERE created_by = ?");
        List<Object> params = new ArrayList<>();
        params.add(condition.getCreatedBy());

        if (keyword != null && keyword.trim().length() > 0) {
            sql.append(" AND project_code LIKE ?");
            params.add(keyword.trim() + "%");
        }

        sql.append(" ORDER BY project_id");

        return qr.query(sql.toString(),
                new BeanListHandler<>(Project.class, connectDS.BEAN_ROW_PROCESSOR),
                params.toArray());
    }


    public List<Project> selectMyProjects(Project condition) throws Exception {
        QueryRunner qr = connectDS.getQueryRunner();

        String sql = "SELECT p.project_id, p.project_code, p.project_name, "
                + "p.status AS project_status, p.planned_end_date, "
                + "pm.project_role "
                + "FROM project_member pm "
                + "JOIN project p ON pm.project_id = p.project_id "
                + "WHERE pm.user_id = ? "
                + "  AND pm.member_status = 'ACTIVE' "
                + "ORDER BY p.planned_end_date ASC, p.project_id ASC";
        return qr.query(sql, new BeanListHandler<>(Project.class, connectDS.BEAN_ROW_PROCESSOR),
                condition.getUserId());
    }

    public boolean existsByCode(Project project) throws Exception {
        QueryRunner qr = connectDS.getQueryRunner();

        String sql = "SELECT COUNT(*) FROM project WHERE project_code = ?";
        Long count = qr.query(sql, new ScalarHandler<Long>(), project.getProjectCode());
        return count != null && count > 0;
    }
}
