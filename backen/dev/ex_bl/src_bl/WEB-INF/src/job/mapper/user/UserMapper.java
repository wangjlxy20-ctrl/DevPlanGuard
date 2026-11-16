package job.mapper.user;

import job.databaseSource.connectDS;
import job.entity.UserInfo;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.util.ArrayList;
import java.util.List;


public class UserMapper {

    
    public void addUser(UserInfo user) throws Exception {
        QueryRunner qr = connectDS.getQueryRunner();
        String sql =
                "INSERT INTO user_info(username,password,permission,created_at)"
                        + "VALUES (?,?,?,NOW())";
        qr.update(sql, user.getUserName(), user.getPassWord(), user.getPermission());
    }

    /**
     * 按指定的 user_id 建立业务档案（用于"AC 里新建账号后自动补档"）。
     * <p>user_id 必须沿用 AC 的编号，保证业务表和 AC 表能按同一个 id 关联；
     * password 列仅为兼容旧表结构写入占位值，登录只认 AC 的哈希密码。</p>
     */
    public void addUserWithId(final Integer userId, final String userName, final String permission) throws Exception {
        QueryRunner qr = connectDS.getQueryRunner();
        String sql = "INSERT INTO user_info(user_id, username, password, permission, created_at)"
                + " VALUES (?,?,?,?,NOW())";
        qr.update(sql, userId, userName, "AC_MANAGED", permission);
    }

    
    public void updateUserInfo(UserInfo user) throws Exception {
        QueryRunner qr = connectDS.getQueryRunner();
        String sql =
                "UPDATE user_info SET password = ?,permission = ? WHERE username = ?";
        qr.update(sql, user.getPassWord(), user.getPermission(), user.getUserName());
    }

    //AC
    public void syncFromAc(final Integer userId, final String userName, final String permission) throws Exception {
        QueryRunner qr = connectDS.getQueryRunner();
        String sql = "UPDATE user_info SET username = ?, permission = ? WHERE user_id = ?";
        qr.update(sql, userName, permission, userId);
    }

    
    public List<UserInfo> usersSearch(UserInfo condition) throws Exception {
        QueryRunner qr = connectDS.getQueryRunner();

        StringBuilder sql = new StringBuilder(
                "SELECT user_id,username,permission,created_at FROM user_info WHERE 1 = 1"
        );

        List<Object> params = new ArrayList<>();

        String username = condition.getUserName();
        String permission = condition.getPermission();
        String userId = (condition.getUserId() == null) ? null : String.valueOf(condition.getUserId());

        if (username != null && !username.trim().isEmpty()) {
            sql.append(" AND username LIKE ?");
            params.add("%" + username + "%");
        }

        if (permission != null && !permission.trim().isEmpty()) {
            sql.append(" AND permission = ?");
            params.add(permission.trim());
        }

        if (userId != null && !userId.trim().isEmpty()) {
            sql.append(" AND user_id = ?");
            params.add(userId.trim());
        }

        sql.append(" ORDER BY user_id");

        return qr.query(sql.toString(),
                new BeanListHandler<>(UserInfo.class, connectDS.BEAN_ROW_PROCESSOR),
                params.toArray());
    }

    
    public List<UserInfo> searchUsers(UserInfo condition, String projectId) throws Exception {
        QueryRunner qr = connectDS.getQueryRunner();

        String sql = "SELECT u.user_id, u.username "
                + "FROM user_info u "
                + "WHERE u.username LIKE ? "
                + "AND u.permission = 'MEMBER' "
                + "  AND u.user_id NOT IN ( "
                + "        SELECT user_id FROM project_member WHERE project_id = ?) "
                + "ORDER BY u.user_id";

        String keyword = (condition == null) ? null : condition.getUserName();
        String kw = (keyword == null) ? "" : keyword.trim();
        String likePattern = "%" + kw + "%";

        return qr.query(sql, new BeanListHandler<>(UserInfo.class, connectDS.BEAN_ROW_PROCESSOR),
                likePattern, Integer.parseInt(projectId.trim()));
    }

    
    public int getCreatorId(UserInfo user) throws Exception {
        QueryRunner qr = connectDS.getQueryRunner();
        String sql = "SELECT user_id FROM user_info WHERE username = ?";
        return qr.query(sql, new ScalarHandler<Integer>(), user.getUserName());
    }

    //AC
    public UserInfo findById(final Integer userId) throws Exception {
        QueryRunner qr = connectDS.getQueryRunner();
        String sql = "SELECT user_id, username, permission FROM user_info WHERE user_id = ?";
        List<UserInfo> list = qr.query(sql, new BeanListHandler<>(UserInfo.class, connectDS.BEAN_ROW_PROCESSOR),
                userId);
        return (list == null || list.isEmpty()) ? null : list.get(0);
    }

    //AC
    public UserInfo findByUsername(final String username) throws Exception {
        QueryRunner qr = connectDS.getQueryRunner();
        String sql = "SELECT user_id, username, permission FROM user_info WHERE username = ?";
        List<UserInfo> list = qr.query(sql, new BeanListHandler<>(UserInfo.class, connectDS.BEAN_ROW_PROCESSOR),
                username);
        return (list == null || list.isEmpty()) ? null : list.get(0);
    }

    
    public void deleteUser(String[] userIds) throws Exception {
        QueryRunner qr = connectDS.getQueryRunner();

        StringBuilder sql = new StringBuilder("DELETE FROM user_info WHERE user_id IN (");
        for (int i = 0; i < userIds.length; i++) {
            sql.append(i == 0 ? "?" : ", ?");
        }

        sql.append(")");
        qr.update(sql.toString(), (Object[]) userIds);
    }
}
