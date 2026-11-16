package job.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChartPoint {

    //分组名//任务状态 / 缺陷严重度 / 项目名//label
    private String label;

    //该分组的计数->value
    private Integer value;
}
