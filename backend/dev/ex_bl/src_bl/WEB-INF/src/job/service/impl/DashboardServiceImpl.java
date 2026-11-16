package job.service.impl;

import jakarta.enterprise.context.RequestScoped;

import job.entity.ChartPoint;
import job.entity.Task;
import job.mapper.dashboard.DashboardMapper;
import job.service.DashboardService;

import java.util.ArrayList;
import java.util.List;


@RequestScoped
public class DashboardServiceImpl implements DashboardService {

    @Override
    public List<ChartPoint> countMyTaskStatus(Integer userId) throws Exception {
        if (userId == null) {
            return new ArrayList<>();
        }
        return new DashboardMapper().countMyTaskStatus(userId);
    }

    @Override
    public List<ChartPoint> countMyDefectSeverity(Integer userId) throws Exception {
        if (userId == null) {
            return new ArrayList<>();
        }
        return new DashboardMapper().countMyDefectSeverity(userId);
    }

    @Override
    public List<ChartPoint> countProjectTaskCount(Integer userId) throws Exception {
        if (userId == null) {
            return new ArrayList<>();
        }
        return new DashboardMapper().countProjectTaskCount(userId);
    }

    @Override
    public List<ChartPoint> countProjectDueStatus(Integer projectId) throws Exception {
        if (projectId == null) {
            return new ArrayList<>();
        }
        return new DashboardMapper().countProjectDueStatus(projectId);
    }

    @Override
    public List<Task> getProjectRiskTasks(Integer projectId) throws Exception {
        if (projectId == null) {
            return new ArrayList<>();
        }
        return new DashboardMapper().selectProjectRiskTasks(projectId);
    }

    @Override
    public List<ChartPoint> countProjectDefectSeverity(Integer projectId) throws Exception {
        if (projectId == null) {
            return new ArrayList<>();
        }
        return new DashboardMapper().countProjectDefectSeverity(projectId);
    }
}
