package job.service;

import job.entity.Project;
import job.entity.Requirement;

import java.util.List;


public interface RequirementService {


    Project getProject(Project project) throws Exception;


    Integer getProjectId(Requirement requirement) throws Exception;


    List<Requirement> searchRequirements(Requirement condition) throws Exception;


    List<Requirement> getMyLeadRequirements(String userId, String projectId) throws Exception;


    Requirement getTaskStats(Requirement condition) throws Exception;


    Requirement getTaskStatsByCode(Requirement condition) throws Exception;


    void startRequirement(Requirement condition, String[] btnCol, String[] codeCol) throws Exception;


    void completeRequirement(Requirement condition, String[] btnCol, String[] codeCol) throws Exception;


    void checkAddRequirement(Requirement condition) throws Exception;


    void createRequirement(Requirement requirement) throws Exception;


    Requirement getRequirementInfo(Requirement requirement) throws Exception;


    String getRequirementCode(Requirement requirement) throws Exception;


    String getRequirementName(Requirement requirement) throws Exception;

    // ===== 编辑复用新增 =====

    /** 编辑回显：按 condition 里的 projectId + requirementCode 查完整需求。 */
    Requirement getRequirementByCode(Requirement condition) throws Exception;

    /** 编辑提交：更新需求的可编辑字段。 */
    void updateRequirement(Requirement requirement) throws Exception;
}
