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
public class Project {
    private Integer projectId;
    private String projectCode;
    private String projectName;
    private String projectDescription;
    private Integer leaderId;
    private Date startDate;
    private Date plannedEndDate;
    private Date actualEndDate;
    private String status;
    private String priority;
    private Integer createdBy;
    private Date createdAt;
    private Date updateAt;

    // ---- 联查展示字段----
    private String leaderName;
    private String creatorName;
    private String projectStatus;
    private String projectRole;
    private String ownerName;
    //
    private int userId;
}
