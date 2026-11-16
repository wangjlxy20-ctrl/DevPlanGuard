package job.mapper.task;

import job.databaseSource.connectDS;
import org.apache.commons.dbutils.QueryRunner;


public class TaskStatusHistoryMapper {

    public TaskStatusHistoryMapper() {
    }

    
    public int insert(String taskId, String fromStatus, String toStatus,
                      String fromReview, String toReview,
                      String operatorId, String comment) throws Exception {

        QueryRunner qr = connectDS.getQueryRunner();

        String sql = "INSERT INTO task_status_history "
                + "(task_id, from_status, to_status, from_review_status, to_review_status, "
                + "operator_id, comment) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        return qr.update(sql,
                Integer.parseInt(taskId.trim()),
                fromStatus, toStatus, fromReview, toReview,
                Integer.parseInt(operatorId.trim()),
                comment);
    }

    
    public int insert(String taskId, String fromStatus, String toStatus,
                      String operatorId, String comment) throws Exception {

        QueryRunner qr = connectDS.getQueryRunner();

        String sql = "INSERT INTO task_status_history "
                + "(task_id, from_status, to_status, operator_id, comment) "
                + "VALUES (?, ?, ?, ?, ?)";

        return qr.update(sql,
                Integer.parseInt(taskId.trim()),
                fromStatus, toStatus,
                Integer.parseInt(operatorId.trim()),
                comment);
    }
}
