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
public class Requirement {
    private Integer requirementId;
    private Integer projectId;
    private String requirementCode;
    private String requirementName;
    private String description;
    private String priority;
    private String status;
    private Date plannedStartDate;
    private Date plannedEndDate;
    private Date actualStartDate;
    private Date actualEndDate;
    private String createdBy;
    private Date createdAt;
    private Date updatedAt;

    //联查展示字段
    private String projectCode;
    private String projectName;
    private Integer leaderId;
    private String ownerName;
    private Date projectDue;
    private String projectRole;

    //任务统计字段
    private Integer totalTasks;
    private Integer openTasks;
}
