package job.service;

import job.entity.Project;
import job.entity.ProjectMember;
import job.entity.UserInfo;

import java.util.List;


public interface ProjectService {

    
    String getProjectRole(ProjectMember member) throws Exception;

    
    void checkProjectLeader(ProjectMember condition) throws Exception;

    
    List<Project> searchProjects(Project condition, String keyword) throws Exception;

    
    void checkProjectCodeAvailable(String projectCode) throws Exception;

    
    Project getProject(Project project) throws Exception;

    
    Project getProjectDetails(Project project) throws Exception;

    
    List<Project> getMyProjects(Project condition) throws Exception;

    
    String getEntryScreenId(String projectRole) throws Exception;

    
    int getCreatorId(UserInfo user) throws Exception;

    
    void createProject(Project project) throws Exception;

    
    int getProjectIdByCode(Project project) throws Exception;

    
    int addProjectMember(ProjectMember member) throws Exception;
}
