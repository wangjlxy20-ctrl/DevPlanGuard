package job.service.impl;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import job.entity.UserInfo;
import job.mapper.user.UserMapper;
import job.service.AccountService;
import job.service.UserService;

import java.util.List;


@RequestScoped
public class UserServiceImpl implements UserService {

    @Inject
    private AccountService accountService;

    @Override
    public List<UserInfo> searchUsers(UserInfo condition) throws Exception {
        return new UserMapper().usersSearch(condition);
    }

    @Override
    public void createUser(UserInfo user) throws Exception {
        new UserMapper().addUser(user);

        // 业务库自增出 user_id 后，用同一个 id 建框架账号
        Integer userId = new UserMapper().getCreatorId(user);
        if (userId != null) {
            UserInfo account = new UserInfo();
            account.setUserId(userId);
            account.setUserName(user.getUserName());
            account.setPassWord(user.getPassWord());
            accountService.createAccount(account, user.getPermission());
        }
    }

    @Override
    public void updateUser(UserInfo user) throws Exception {
        new UserMapper().updateUserInfo(user);

        // 业务资料与框架账号以 username 关联，先按 username 反查 user_id
        Integer userId = new UserMapper().getCreatorId(user);
        if (userId != null) {
            UserInfo account = new UserInfo();
            account.setUserId(userId);
            account.setUserName(user.getUserName());
            account.setPassWord(user.getPassWord());
            accountService.updateAccount(account, user.getPermission());
        }
    }

    @Override
    public void deleteUsers(String[] userIds) throws Exception {
        if (userIds != null) {
            for (String userId : userIds) {
                accountService.deleteAccount(userId);
            }
        }
        new UserMapper().deleteUser(userIds);
    }


}
