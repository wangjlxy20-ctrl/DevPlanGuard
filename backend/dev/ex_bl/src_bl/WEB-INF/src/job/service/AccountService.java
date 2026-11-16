package job.service;

import job.entity.UserInfo;


public interface AccountService {

    
    void createAccount(UserInfo user, String roleId) throws Exception;

    
    void updateAccount(UserInfo user, String roleId) throws Exception;

    
    void deleteAccount(String userId) throws Exception;
}
