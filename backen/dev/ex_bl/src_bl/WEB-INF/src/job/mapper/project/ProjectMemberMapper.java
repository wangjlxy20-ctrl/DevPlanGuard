package job.mapper.project;

import job.databaseSource.connectDS;
import job.entity.ProjectMember;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.util.List;

public class ProjectMemberMapper {


    public String selectProjectRole(ProjectMember member) throws Exception {
        QueryRunner qr = connectDS.getQueryRunner();
        String sql = "SELECT project_role from project_member WHERE user_id = ? ";
        return qr.query(sql, new ScalarHandler<String>(), member.getUserId());
    }


    public boolean isProjectLeader(ProjectMember condition) throws Exception {
        QueryRunner qr = connectDS.getQueryRunner();

        String sql = "SELECT COUNT(*) FROM project_member "
                + "WHERE project_id = ? AND user_id = ? "
                + "AND project_role = 'LEAD' AND member_status = 'ACTIVE'";

        Long count = qr.query(sql, new ScalarHandler<Long>(),
                condition.getProjectId(), condition.getUserId());
        return count != null && count.longValue() > 0;
    }

    public List<ProjectMember> selectMembers(ProjectMember condition) throws Exception {
        QueryRunner qr = connectDS.getQueryRunner();

        String sql = "SELECT pm.pm_id, pm.user_id, pm.project_role, pm.member_status, "
                + "       u.username, "
                + "       (SELECT COUNT(*) FROM task t "
                + "          JOIN requirement rq ON t.requirement_id = rq.requirement_id "
                + "         WHERE rq.project_id = pm.project_id AND t.assignee_id = pm.user_id) "
                + "       AS assigned_tasks, "
                + "       (SELECT COUNT(*) FROM task t "
                + "          JOIN requirement rq ON t.requirement_id = rq.requirement_id "
                + "         WHERE rq.project_id = pm.project_id AND t.assignee_id = pm.user_id "
                + "           AND t.status = 'COMPLETED') "
                + "       AS completed_tasks "
                + "FROM project_member pm "
                + "JOIN user_info u ON pm.user_id = u.user_id "
                + "WHERE pm.project_id = ? AND pm.member_status = 'ACTIVE' "
                + "ORDER BY pm.project_role, pm.pm_id";
        return qr.query(sql, new BeanListHandler<>(ProjectMember.class, connectDS.BEAN_ROW_PROCESSOR),
                condition.getProjectId());
    }


    public List<ProjectMember> selectAssignableMembers(ProjectMember condition) throws Exception {
        QueryRunner qr = connectDS.getQueryRunner();

        String sql = "SELECT pm.user_id, u.username "
                + "FROM project_member pm "
                + "JOIN user_info u ON pm.user_id = u.user_id "
                + "WHERE pm.project_id = ? "
                + "  AND pm.member_status = 'ACTIVE' "
                + " AND pm.project_role <> 'LEAD' "
                + "ORDER BY pm.user_id";

        return qr.query(sql, new BeanListHandler<>(ProjectMember.class, connectDS.BEAN_ROW_PROCESSOR),
                condition.getProjectId());
    }



    public int insert(ProjectMember member) throws Exception {
        QueryRunner qr = connectDS.getQueryRunner();

        String sql = "INSERT INTO project_member "
                + "(project_id, user_id, project_role, member_status) "
                + "VALUES (?, ?, ?, 'ACTIVE')";

        return qr.update(sql, member.getProjectId(), member.getUserId(), member.getProjectRole());
    }


    public int insertMembers(ProjectMember condition, List<String> userIds, List<String> roles) throws Exception {
        QueryRunner qr = connectDS.getQueryRunner();

        String sql = "INSERT IGNORE INTO project_member "
                + "(project_id, user_id, project_role, member_status) "
                + "VALUES (?, ?, ?, 'ACTIVE')";

        int count = 0;
        for (int i = 0; i < userIds.size(); i++) {
            String uid = userIds.get(i);
            if (uid != null && uid.trim().length() > 0) {
                int uidInt = Integer.parseInt(uid.trim());

                //Get the value from the dropdown in that row;
                //if not found, default to DEVELOPER
                String role = (roles != null && i < roles.size()) ? roles.get(i) : null;
                if (role == null || role.trim().length() == 0) {
                    role = "DEVELOPER";
                }

                count += qr.update(sql,
                        condition.getProjectId(),
                        uidInt,
                        role);
            }
        }
        return count;
    }
}
