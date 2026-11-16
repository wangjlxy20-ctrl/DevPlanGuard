package job.service.impl;

import biz.grandsight.ex.ui.ac.io.dao.UserRoleTableDao;
import biz.grandsight.ex.ui.ac.io.dao.UserTableDao;
import biz.grandsight.ex.ui.ac.io.mybatis.session.ACSqlSessionFactory;
import biz.grandsight.ex.ui.ac.util.PasswordHelper;
import jakarta.enterprise.context.RequestScoped;
import job.entity.UserInfo;
import job.service.AccountService;
import org.apache.ibatis.session.SqlSession;

import java.util.EnumMap;
import java.util.Map;


@RequestScoped
public class AccountServiceImpl implements AccountService {

    @Override
    public void createAccount(UserInfo user, String roleId) throws Exception {
        String userId = toUserId(user);
        if (isBlank(userId)) {
            return;
        }
        try (SqlSession session = ACSqlSessionFactory.openSession(true)) {
            if (UserTableDao.countByPrimaryKey(session, userId) > 0L) {
                updateAccountInSession(session, userId, user.getUserName(), user.getPassWord(), roleId);
                return;
            }

            Map<UserTableDao.COLUMNS, Object> row = new EnumMap<UserTableDao.COLUMNS, Object>(UserTableDao.COLUMNS.class);
            row.put(UserTableDao.COLUMNS.USER_ID, userId);
            row.put(UserTableDao.COLUMNS.USER_NAME, user.getUserName());
            row.put(UserTableDao.COLUMNS.PASSWORD, PasswordHelper.getEncryptedPassword(user.getPassWord()));
            row.put(UserTableDao.COLUMNS.DISPLAY_ORDER, Integer.valueOf((int) (UserTableDao.countAllRecord(session) + 1)));
            row.put(UserTableDao.COLUMNS.REGISTRATION_TYPE, Integer.valueOf(0));
            UserTableDao.insert(session, row);

            replaceRole(session, userId, roleId);
        }
    }

    @Override
    public void updateAccount(UserInfo user, String roleId) throws Exception {
        String userId = toUserId(user);
        if (isBlank(userId)) {
            return;
        }
        try (SqlSession session = ACSqlSessionFactory.openSession(true)) {
            updateAccountInSession(session, userId, user.getUserName(), user.getPassWord(), roleId);
        }
    }

    @Override
    public void deleteAccount(String userId) throws Exception {
        if (isBlank(userId)) {
            return;
        }
        try (SqlSession session = ACSqlSessionFactory.openSession(true)) {
            UserRoleTableDao.deleteByUserId(session, userId);
            UserTableDao.deleteByPrimaryKey(session, userId);
        }
    }

    
    private void updateAccountInSession(SqlSession session, String userId, String userName,
                                        String rawPassword, String roleId) throws Exception {
        Map<UserTableDao.COLUMNS, Object> current = UserTableDao.selectByPrimaryKey(session, userId);

        if (current == null) {
            Map<UserTableDao.COLUMNS, Object> row = new EnumMap<UserTableDao.COLUMNS, Object>(UserTableDao.COLUMNS.class);
            row.put(UserTableDao.COLUMNS.USER_ID, userId);
            row.put(UserTableDao.COLUMNS.USER_NAME, userName);
            row.put(UserTableDao.COLUMNS.PASSWORD, PasswordHelper.getEncryptedPassword(rawPassword));
            row.put(UserTableDao.COLUMNS.DISPLAY_ORDER, Integer.valueOf((int) (UserTableDao.countAllRecord(session) + 1)));
            row.put(UserTableDao.COLUMNS.REGISTRATION_TYPE, Integer.valueOf(0));
            UserTableDao.insert(session, row);
        } else {
            Map<UserTableDao.COLUMNS, Object> row = new EnumMap<UserTableDao.COLUMNS, Object>(UserTableDao.COLUMNS.class);
            row.put(UserTableDao.COLUMNS.USER_ID, userId);
            row.put(UserTableDao.COLUMNS.USER_NAME, userName == null ? current.get(UserTableDao.COLUMNS.USER_NAME) : userName);
            row.put(UserTableDao.COLUMNS.PASSWORD, isBlank(rawPassword)
                    ? current.get(UserTableDao.COLUMNS.PASSWORD)
                    : PasswordHelper.getEncryptedPassword(rawPassword));
            row.put(UserTableDao.COLUMNS.DISPLAY_ORDER, current.get(UserTableDao.COLUMNS.DISPLAY_ORDER));
            row.put(UserTableDao.COLUMNS.REGISTRATION_TYPE, current.get(UserTableDao.COLUMNS.REGISTRATION_TYPE));
            UserTableDao.updateByPrimaryKey(session, row);
        }

        replaceRole(session, userId, roleId);
    }

    
    private void replaceRole(SqlSession session, String userId, String roleId) throws Exception {
        UserRoleTableDao.deleteByUserId(session, userId);
        if (isBlank(roleId)) {
            return;
        }
        Map<UserRoleTableDao.COLUMNS, Object> row = new EnumMap<UserRoleTableDao.COLUMNS, Object>(UserRoleTableDao.COLUMNS.class);
        row.put(UserRoleTableDao.COLUMNS.USER_ID, userId);
        row.put(UserRoleTableDao.COLUMNS.ROLE_ID, roleId);
        UserRoleTableDao.insert(session, row);
    }

    
    private static String toUserId(UserInfo user) {
        if (user == null || user.getUserId() == null) {
            return null;
        }
        return String.valueOf(user.getUserId());
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
