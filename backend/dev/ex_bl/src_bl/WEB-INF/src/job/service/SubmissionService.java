package job.service;

import job.entity.Task;

import java.util.List;


public interface SubmissionService {

    
    Task getTaskInfo(Task task) throws Exception;

    
    Task findTaskById(Task task) throws Exception;

    
    Task findDefectById(Task task) throws Exception;

    
    Task findRegressionById(Task task) throws Exception;

    
    String[] submitDeliverable(Task task) throws Exception;

    
    String submitTestResult(Task task, List<String[]> bugRows) throws Exception;

    
    String submitDefectFix(Task task) throws Exception;

    
    String submitRegressionResult(Task task) throws Exception;
}
