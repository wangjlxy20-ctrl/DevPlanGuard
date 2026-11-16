package job.service;

import job.entity.UserInfo;

import java.util.List;


public interface UserService {
    
    List<UserInfo> searchUsers(UserInfo condition) throws Exception;

    
    void createUser(UserInfo user) throws Exception;

    
    void updateUser(UserInfo user) throws Exception;

    
    void deleteUsers(String[] userIds) throws Exception;
}
