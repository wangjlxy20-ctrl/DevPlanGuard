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
public class ProjectMember {
    private Integer pmId;
    private Integer projectId;
    private Integer userId;
    private String projectRole;
    private String memberStatus;
    private Date joinAt;
    private Date leaveAt;
    private Integer addedBy;
    private Date createdAt;
    private Date updatedAt;

    // 联查展示字段
    private String username;

    //成员任务统计
    private Integer assignedTasks;
    private Integer completedTasks;
}
