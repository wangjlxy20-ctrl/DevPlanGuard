package job.service;

import job.entity.ChartPoint;
import job.entity.Task;

import java.util.List;


public interface DashboardService {

    
    List<ChartPoint> countMyTaskStatus(Integer userId) throws Exception;

    
    List<ChartPoint> countMyDefectSeverity(Integer userId) throws Exception;

    
    List<ChartPoint> countProjectTaskCount(Integer userId) throws Exception;

    
    List<ChartPoint> countProjectDueStatus(Integer projectId) throws Exception;

    
    List<Task> getProjectRiskTasks(Integer projectId) throws Exception;

    
    List<ChartPoint> countProjectDefectSeverity(Integer projectId) throws Exception;
}
