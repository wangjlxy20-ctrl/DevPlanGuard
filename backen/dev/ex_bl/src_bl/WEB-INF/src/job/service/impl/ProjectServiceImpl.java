package job.service.impl;

import com.yse.gb.gf.front.CommandException;
import com.yse.gb.gf.front.UserMessage;

import jakarta.enterprise.context.RequestScoped;

import job.entity.Project;
import job.entity.ProjectMember;
import job.entity.UserInfo;
import job.mapper.project.ProjectMapper;
import job.mapper.project.ProjectMemberMapper;
import job.mapper.user.UserMapper;
import job.service.ProjectService;

import java.util.List;


@RequestScoped
public class ProjectServiceImpl implements ProjectService {

    @Override
    public String getProjectRole(ProjectMember member) throws Exception {
        String projectRole = new ProjectMemberMapper().selectProjectRole(member);
        return projectRole;
    }

    
    @Override
    public void checkProjectLeader(ProjectMember condition) throws Exception {
        if (!new ProjectMemberMapper().isProjectLeader(condition)) {
            throw new CommandException(new UserMessage("You are not the leader of this project."));
        }
    }

    @Override
    public List<Project> searchProjects(Project condition, String keyword) throws Exception {
        List<Project> projectList = new ProjectMapper().searchLeaderProjects(condition, keyword);
        return projectList;
    }

    @Override
    public void checkProjectCodeAvailable(String projectCode) throws Exception {
        if (projectCode == null || projectCode.isEmpty()) {
            throw new CommandException(new UserMessage("Please first enter the PROJECT_CODE in the Project_Code_ID box before creating a new project."));
        }

        if (checkProjectCodeUniqueness(projectCode)) {
            throw new CommandException(new UserMessage("The project_code already exists, please enter it again."));
        }
    }

    // 原 dpgl100Service.getCheckProjectCodeUniqueness：仅本类内部使用，故不对外暴露
    private boolean checkProjectCodeUniqueness(String projectCode) throws Exception {
        Project condition = new Project();
        condition.setProjectCode(projectCode);
        return new ProjectMapper().existsByCode(condition);
    }

    @Override
    public Project getProject(Project project) throws Exception {
        return new ProjectMapper().selectById(project);
    }

    @Override
    public Project getProjectDetails(Project project) throws Exception {
        Project result = new ProjectMapper().selectByCode(project);
        return result;
    }

    @Override
    public List<Project> getMyProjects(Project condition) throws Exception {
        // Project 实体没有 user_id 字段，调用方用 createdBy 承载「当前用户 ID」
        List<Project> projectsList = new ProjectMapper().selectMyProjects(condition);
        return projectsList;
    }

    @Override
    public String getEntryScreenId(String projectRole) throws Exception {
        if ("LEAD".equals(projectRole)){
            return "DPGL110MNG";
        } else {
            return "DPGL120PJTMEM";
        }
    }

    @Override
    public int getCreatorId(UserInfo user) throws Exception {
        return new UserMapper().getCreatorId(user);
    }

    @Override
    public void createProject(Project project) throws Exception {
        new ProjectMapper().insert(project);
    }

    @Override
    public int getProjectIdByCode(Project project) throws Exception {
        return new ProjectMapper().selectIdByCode(project);
    }

    @Override
    public int addProjectMember(ProjectMember member) throws Exception {
        return new ProjectMemberMapper().insert(member);
    }
}
