package job.service;

import job.entity.ProjectMember;
import job.entity.Task;

import java.util.List;


public interface TaskService {

    
    List<Task> searchTasks(Task condition) throws Exception;

    
    Task getTask(Task task) throws Exception;

    
    Integer getProjectId(Task task) throws Exception;

    
    String getAssignTargetScreenId(Task task) throws Exception;

    
    List<ProjectMember> getAssignableMembers(Task task) throws Exception;

    
    String[] createTask(Task task) throws Exception;

    
    String[] submitAssignment(Task task, String[] assigneeRows) throws Exception;

    
    String resolveRequirementId(Task task) throws Exception;

    
    List<Task> getMyTasks(String userId,String projectId) throws Exception;

    
    String getSubmitScreenId(String taskType) throws Exception;

    
    String[] resolveSubmitTarget(String buttonName, String[] taskIdCol, String[] taskTypeCol)
            throws Exception;

    int assignTask(String taskId, String assigneeId, String assignedBy, String dueDate) throws Exception;
}
