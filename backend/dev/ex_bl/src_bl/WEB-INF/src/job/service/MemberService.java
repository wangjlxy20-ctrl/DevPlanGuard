package job.service;

import job.entity.ProjectMember;
import job.entity.UserInfo;

import java.util.List;


public interface MemberService {
    
    List<ProjectMember> searchMembers(ProjectMember condition) throws Exception;

    
    List<UserInfo> searchAddableUsers(ProjectMember condition, String keyword) throws Exception;

    
    boolean hasAnySelected(String[] selectedRows) throws Exception;

    
    int addSelectedMembers(String projectId, String[] selectedRows, String[] roleRows) throws Exception;

    
    List<ProjectMember> searchAssignableMembers(ProjectMember condition) throws Exception;
}
