package job.mapper.file;

import job.databaseSource.connectDS;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;

import javax.naming.NamingException;
import java.util.List;
import java.util.Map;

public class FileListMapper {

	private QueryRunner qr = connectDS.getQueryRunner();

    public FileListMapper() throws NamingException {
    }


    public List<Map<String, Object>> searchProjectFiles(String userId) throws Exception {

		// 每行末尾空格不能省
		String sql = "SELECT t.task_id, t.task_code, t.task_name, "
				+ "p.project_name, r.requirement_name, "
				// 提交时间在 SQL 里格式化，Java 端直接显示
				+ "DATE_FORMAT(t.submitted_at, '%Y/%m/%d %H:%i:%s') AS submitted_at, "
				// 提交人：task.submitted_by 关联 user_info
				+ "u.username AS submitter_name, "
				+ "t.deliverable_file "
				+ "FROM task t "
				+ "JOIN requirement r ON t.requirement_id = r.requirement_id "
				+ "JOIN project p ON r.project_id = p.project_id "
				// 提交人可能为空，LEFT JOIN 容纳
				+ "LEFT JOIN user_info u ON t.submitted_by = u.user_id "
				// 只取已上传文件的任务
				+ "WHERE t.deliverable_file IS NOT NULL "
				+ "  AND t.deliverable_file <> '' "
				// 项目负责人(leader_id) 或 项目创建人(created_by) 可查看
				+ "  AND (p.leader_id = ? OR p.created_by = ?) "
				+ "ORDER BY p.project_id, t.submitted_at DESC, t.task_id";

		// 两个 ? 都是当前用户：分别匹配 project.leader_id 和 project.created_by
		int uid = Integer.parseInt(userId.trim());
		return qr.query(sql, new MapListHandler(), uid, uid);
	}
}
