package job.entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Task {

    //basic info
    private Integer taskId;
    private Integer parentId;
    private String taskCode;
    private Integer requirementId;
    private String taskName;
    private String description;
    private String taskType;
    private String priority;
    private Integer createdBy;
    private Integer assigneeId;
    private Integer assignedBy;
    private String status;
    private Date plannedStartDate;
    private Date plannedEndDate;
    private Date actualStartAt;
    private Date actualEndAt;
    private String riskLevel;

    //公共提交信息
    private String deliverableDescription;
    private String deliverableUrl;
    private String deliverableFile;
    private Date submittedAt;
    private String submissionNote;
    private Integer submittedBy;
    //测试任务专属
    private String testPlan;
    private String testCaseDesign;
    private Integer passedCount;
    private Integer failedCount;
    //DEFECT 任务专属
    private String severity;
    private String rootCause;
    private String fixMethod;
    private String affectedScope;
    //审批字段
    private String reviewStatus;
    private Integer reviewedBy;
    private Date reviewedAt;
    private Date createdAt;
    private Date updatedAt;

    //联查展示字段
    private String assigneeName;
    private String projectName;
    private String requirementName;
    private String projectRole;
    private String parentCode;
    private String defectCode;
    private String bugTitle;

    //该任务下未关闭的 DEFECT 子任务数
    private Integer openDefects;

    private Integer daysLeft;
    //仅用查询
    private Integer projectId;

}
