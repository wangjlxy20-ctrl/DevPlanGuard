package job.service.impl;

import com.yse.gb.gf.front.CommandException;
import com.yse.gb.gf.front.UserMessage;

import jakarta.enterprise.context.RequestScoped;
import job.entity.Project;
import job.entity.Requirement;
import job.mapper.requirement.RequirementMapper;
import job.service.RequirementService;

import java.util.Arrays;
import java.util.List;

@RequestScoped
public class RequirementServiceImpl implements RequirementService {

    @Override
    public Project getProject(Project project) throws Exception {
        Project projectInfo = new RequirementMapper().selectProject(project);
        return projectInfo;
    }

    @Override
    public Integer getProjectId(Requirement requirement) throws Exception {
        Requirement reqInfo = new RequirementMapper().selectById(requirement);
        return (reqInfo == null) ? null : reqInfo.getProjectId();
    }

    @Override
    public List<Requirement> searchRequirements(Requirement condition) throws Exception {
        List<Requirement> requirementList = new RequirementMapper().selectByProject(condition);
        return requirementList;
    }

    @Override
    public List<Requirement> getMyLeadRequirements(String userId, String projectId) throws Exception {
        List<Requirement> requirementsList = new RequirementMapper().selectMyLeadRequirements(userId, projectId);
        return requirementsList;
    }

    @Override
    public Requirement getTaskStats(Requirement condition) throws Exception {
        Requirement stats = new RequirementMapper().selectTaskStats(condition);
        return stats;
    }

    @Override
    public Requirement getTaskStatsByCode(Requirement condition) throws Exception {
        Requirement stats = new RequirementMapper().selectTaskStatsByCode(condition);
        return stats;
    }


    @Override
    public void startRequirement(Requirement condition, String[] btnCol, String[] codeCol) throws Exception {
        System.out.println("START BTN COL : " + Arrays.toString(btnCol));
        System.out.println("START CODE COL: " + Arrays.toString(codeCol));

        String reqCode = null;
        if (btnCol != null) {
            for (int i = 0; i < btnCol.length; i++) {
                if (btnCol[i] != null && btnCol[i].trim().length() > 0) {
                    if (codeCol != null && i < codeCol.length) {
                        reqCode = codeCol[i];
                    }
                    break;
                }
            }
        }
        System.out.println("START TARGET: projectId=" + condition.getProjectId() + ", reqCode=" + reqCode);

        if (reqCode == null || reqCode.trim().length() == 0) {
            throw new CommandException(new UserMessage("Can't find the requirement to start"));
        }

        Requirement target = new Requirement();
        target.setProjectId(condition.getProjectId());
        target.setRequirementCode(reqCode);

        int startCnt = getStartRequirement(target);
        System.out.println("The affect row is : " + startCnt);
        System.out.println("START DONE: " + reqCode);
    }


    @Override
    public void completeRequirement(Requirement condition, String[] btnCol, String[] codeCol) throws Exception {
        System.out.println("COMPLETE BTN COL : " + Arrays.toString(btnCol));
        System.out.println("COMPLETE CODE COL: " + Arrays.toString(codeCol));

        String reqCode = null;
        if (btnCol != null) {
            for (int i = 0; i < btnCol.length; i++) {
                if (btnCol[i] != null && btnCol[i].trim().length() > 0) {
                    if (codeCol != null && i < codeCol.length) {
                        reqCode = codeCol[i];
                    }
                    break;
                }
            }
        }
        System.out.println("COMPLETE TARGET: projectId=" + condition.getProjectId() + ", reqCode=" + reqCode);

        if (reqCode == null || reqCode.trim().length() == 0) {
            throw new CommandException(new UserMessage("Can't locate the requirement to complete"));
        }

        Requirement target = new Requirement();
        target.setProjectId(condition.getProjectId());
        target.setRequirementCode(reqCode);

        Requirement stats = getTaskStatsByCode(target);
        int total = toInt(stats == null ? null : stats.getTotalTasks());
        int open = toInt(stats == null ? null : stats.getOpenTasks());
        System.out.println("COMPLETE CHECK: total=" + total + ", open=" + open);

        if (total == 0) {
            throw new CommandException(new UserMessage("This request doesn't have any tasks yet, so it can't be completed."));
        }
        if (open > 0) {
            throw new CommandException(new UserMessage("Also have " + open + " unfinished task, so the requirement can't be completed."));
        }

        getCompleteRequirement(target);
        System.out.println("COMPLETE DONE: " + reqCode);
    }


    @Override
    public void checkAddRequirement(Requirement condition) throws Exception {
        Integer projectId = (condition == null) ? null : condition.getProjectId();
        System.out.println("DPGL100RE : ADD REQUIREMENT: projectId=" + projectId);

        if (projectId == null) {
            throw new CommandException(new UserMessage("Can't access the current project_id, please re-enter from the project list."));
        }
    }

    @Override
    public void createRequirement(Requirement requirement) throws Exception {
        new RequirementMapper().insert(requirement);
    }

    @Override
    public Requirement getRequirementInfo(Requirement requirement) throws Exception {
        Requirement reqInfo = new RequirementMapper().selectById(requirement);
        return reqInfo;
    }

    @Override
    public String getRequirementCode(Requirement requirement) throws Exception {
        String reqCode = new RequirementMapper().selectCodeById(requirement);
        return reqCode;
    }

    @Override
    public String getRequirementName(Requirement requirement) throws Exception {
        String requirementName = new RequirementMapper().selectNameById(requirement);
        return requirementName;
    }

    // ===== 编辑复用新增实现 =====

    @Override
    public Requirement getRequirementByCode(Requirement condition) throws Exception {
        Requirement reqInfo = new RequirementMapper()
                .selectByProjectAndCode(String.valueOf(condition.getProjectId()),
                        condition.getRequirementCode());
        return reqInfo;
    }

    @Override
    public void updateRequirement(Requirement requirement) throws Exception {
        int updateCnt = new RequirementMapper().update(requirement);
        System.out.println("UPDATE REQUIREMENT affect row is : " + updateCnt);
    }

    // ---- 内部实现 ----

    private int getStartRequirement(Requirement condition) throws Exception {
        int startCnt = new RequirementMapper().start(condition);
        return startCnt;
    }

    private int getCompleteRequirement(Requirement condition) throws Exception {
        int completeCnt = new RequirementMapper().complete(condition);
        return completeCnt;
    }

    private int toInt(Object o) {
        if (o == null) {
            return 0;
        }
        if (o instanceof Number) {
            return ((Number) o).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(o).trim());
        } catch (Exception e) {
            return 0;
        }
    }
}