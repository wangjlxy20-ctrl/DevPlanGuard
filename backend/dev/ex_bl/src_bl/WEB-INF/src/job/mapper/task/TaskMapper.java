package job.mapper.task;

import job.databaseSource.connectDS;
import job.entity.ProjectMember;
import job.entity.Task;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.*;

import javax.naming.NamingException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class TaskMapper {


    // 审批开关
    public static final boolean REVIEW_ENABLED = false;

    private QueryRunner qr = connectDS.getQueryRunner();

    public TaskMapper() throws NamingException {
    }


    public Task selectById(Task t) throws Exception {

        String sql = "SELECT * "
                + "FROM task WHERE task_id = ?";

        return qr.query(sql, new BeanHandler<>(Task.class, connectDS.BEAN_ROW_PROCESSOR),
                t.getTaskId());
    }


    public List<Task> searchTasks(Task condition) throws Exception {

        //LEFT JOIN :Used to show unassigned tasks
        String sql = "SELECT t.task_id, t.priority, t.task_code, t.task_name, t.task_type, "
                + "t.planned_end_date, t.status, "
                + "t.assignee_id, u.username AS assignee_name "
                + "FROM task t "
                + "LEFT JOIN user_info u ON t.assignee_id = u.user_id "
                + "WHERE t.requirement_id = ? "
                + "ORDER BY t.task_id";

        return qr.query(sql, new BeanListHandler<>(Task.class, connectDS.BEAN_ROW_PROCESSOR),
                condition.getRequirementId());
    }


    //查询我的任务;
    //如果传了 PROJECT_ID，那就查询该PROJECT下的我的任务;
    public List<Task> searchMyTasks(Task condition) throws Exception {

        //days_left = due_date - today; negative means overdue
        String sql = "SELECT t.task_id, t.priority, t.task_type, t.task_code, t.task_name, t.status, "
                + "t.planned_end_date, "
                + "p.project_name, r.requirement_name, pm.project_role, "
                + "DATEDIFF(t.planned_end_date, CURDATE()) AS days_left "
                + "FROM task t "
                + "JOIN requirement r ON t.requirement_id = r.requirement_id "
                + "JOIN project p ON r.project_id = p.project_id "
                + "LEFT JOIN project_member pm ON pm.project_id = r.project_id "
                + "  AND pm.user_id = t.assignee_id "
                + "WHERE t.assignee_id = ? ";


        List<Object> params = new ArrayList<>();
        params.add(condition.getAssigneeId());

        //传了项目 ID 就追加过滤：只查该项目下的任务
        if (condition.getProjectId() != null) {
            sql += "AND r.project_id = ? ";
            params.add(condition.getProjectId());
        }

        sql += "ORDER BY t.planned_end_date, t.task_id";

        return qr.query(sql, new BeanListHandler<>(Task.class, connectDS.BEAN_ROW_PROCESSOR),
                params.toArray());
    }


    /**
     * 文件查看：列出当前用户参与的项目下，所有带附件的提交。
     * 用 EXISTS 判断用户是否项目成员，避免成员记录重复导致行重复。
     */
    public List<Task> listDeliverableFiles(Integer userId) throws Exception {

        String sql = "SELECT "
                + "t.task_id, t.task_code, t.task_name, t.task_type, "
                + "t.deliverable_file, "
                + "p.project_name, r.requirement_name, "
                + "u.username AS submitter_name, "
                + "DATE_FORMAT(t.submitted_at, '%Y/%m/%d %H:%i:%s') AS submitted_at_text "
                + "FROM task t "
                + "JOIN requirement r ON t.requirement_id = r.requirement_id "
                + "JOIN project p ON r.project_id = p.project_id "
                + "LEFT JOIN user_info u ON t.submitted_by = u.user_id "
                + "WHERE t.deliverable_file IS NOT NULL "
                + "AND t.deliverable_file <> '' "
                + "AND EXISTS ( "
                + "  SELECT 1 FROM project_member pm "
                + "  WHERE pm.project_id = r.project_id AND pm.user_id = ? "
                + ") "
                + "ORDER BY p.project_name, t.submitted_at DESC, t.task_id";

        return qr.query(sql, new BeanListHandler<>(Task.class, connectDS.BEAN_ROW_PROCESSOR),
                userId);
    }


    public Task selectForAutoTest(Task t) throws Exception {

        String sql = "SELECT task_id, task_code, task_name, task_type, status, "
                + "requirement_id, priority, deliverable_description, "
                + "planned_start_date, planned_end_date "
                + "FROM task WHERE task_id = ?";

        return qr.query(sql, new BeanHandler<>(Task.class, connectDS.BEAN_ROW_PROCESSOR),
                t.getTaskId());
    }

    
    public Task selectAssignTarget(Task t) throws Exception {

        String sql = "SELECT task_id, task_code, task_name, task_type, status, requirement_id "
                + "FROM task WHERE task_id = ?";

        return qr.query(sql, new BeanHandler<>(Task.class, connectDS.BEAN_ROW_PROCESSOR),
                t.getTaskId());
    }

    
    public Task selectForSubmit(Task t) throws Exception {

        String sql = "SELECT task_id, task_code, task_name, status FROM task WHERE task_id = ?";

        return qr.query(sql, new BeanHandler<>(Task.class, connectDS.BEAN_ROW_PROCESSOR),
                t.getTaskId());
    }

    
    public Task selectForDefect(Task t) throws Exception {

        String sql = "SELECT t.task_id, t.task_code, t.task_name, t.task_type, t.status, "
                + "t.severity, t.description, t.requirement_id, "
                + "p.task_code AS parent_code "
                + "FROM task t "
                + "LEFT JOIN task p ON t.parent_id = p.task_id "
                + "WHERE t.task_id = ?";

        return qr.query(sql, new BeanHandler<>(Task.class, connectDS.BEAN_ROW_PROCESSOR),
                t.getTaskId());
    }

    
    public Task selectForRegression(Task t) throws Exception {

        String sql = "SELECT t.task_id, t.task_code, t.task_name, t.task_type, t.status, "
                + "t.requirement_id, "
                + "d.task_code AS defect_code, d.task_name AS bug_title, "
                + "d.severity, d.description "
                + "FROM task t "
                + "LEFT JOIN task d ON t.parent_id = d.task_id "
                + "WHERE t.task_id = ?";

        return qr.query(sql, new BeanHandler<>(Task.class, connectDS.BEAN_ROW_PROCESSOR),
                t.getTaskId());
    }

    
    public List<ProjectMember> findProjectMembersByTask(Task t) throws Exception {

        String sql = "SELECT pm.user_id, u.username, pm.project_role "
                + "FROM task t "
                + "JOIN requirement r ON t.requirement_id = r.requirement_id "
                + "JOIN project_member pm ON pm.project_id = r.project_id "
                + "JOIN user_info u ON pm.user_id = u.user_id "
                + "WHERE t.task_id = ? "
                + "AND pm.member_status = 'ACTIVE' "
                + "AND pm.project_role <> 'LEAD' "
                + "ORDER BY pm.user_id";

        return qr.query(sql, new BeanListHandler<>(ProjectMember.class, connectDS.BEAN_ROW_PROCESSOR),
                t.getTaskId());
    }

    
    public String getParentId(Task t) throws Exception {

        String sql = "SELECT parent_id FROM task WHERE task_id = ?";
        Task row = qr.query(sql, new BeanHandler<>(Task.class, connectDS.BEAN_ROW_PROCESSOR),
                t.getTaskId());

        if (row == null || row.getParentId() == null) {
            return null;
        }
        return String.valueOf(row.getParentId());
    }

    // =====================================================================
    // 插入
    // =====================================================================

    
    public int insert(Task t) throws Exception {

        //Assignee 可以为空（未分配）
        Integer assigneeInt = t.getAssigneeId();

        // assigned_by = 谁指派的。没人被指派时（自动生成待分配的任务）保持 NULL，
        // 等 LEAD 在任务分配页指派时再写。
        Integer assignedByInt = (assigneeInt == null) ? null : t.getAssignedBy();

        // 有人 -> 直接开工;没人 -> 留在 TODO，等 LEAD 在任务分配页指派
        String statusSql = assigneeInt == null ? "'TODO'" : "'IN_PROGRESS'";

        // review_status：审批关闭时直接落 APPROVED，避免出现永远堆积的 PENDING
        String reviewSql = REVIEW_ENABLED ? "'PENDING'" : "'APPROVED'";

        String sql = "INSERT INTO task ("
                + "task_code, requirement_id, task_name, description, task_type, priority, "
                + "created_by, assignee_id, assigned_by, status, "
                + "planned_start_date, planned_end_date, risk_level, review_status, "
                + "test_plan, test_case_design, severity, affected_scope, root_cause"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, " + statusSql + ", ?, ?, 'LOW', " + reviewSql + ", "
                + "?, ?, ?, ?, ?)";

        return qr.update(sql,
                t.getTaskCode(),
                t.getRequirementId(),
                t.getTaskName(),
                t.getDescription(),
                t.getTaskType(),
                t.getPriority(),
                t.getCreatedBy(),
                assigneeInt,
                assignedByInt,
                t.getPlannedStartDate(),
                t.getPlannedEndDate(),
                t.getTestPlan(),
                t.getTestCaseDesign(),
                t.getSeverity(),
                t.getAffectedScope(),
                t.getRootCause());
    }

    
    public int insertAutoTestTask(Task t) throws Exception {

        String reviewSql = REVIEW_ENABLED ? "'PENDING'" : "'APPROVED'";

        String sql = "INSERT INTO task ("
                + "task_code, requirement_id, task_name, description, task_type, "
                + "parent_id, status, priority, assignee_id, assigned_by, "
                + "created_by, planned_start_date, planned_end_date, "
                + "actual_start_at, risk_level, review_status, created_at"
                + ") SELECT "
                + "?, t.requirement_id, ?, ?, 'TEST', "
                + "t.task_id, 'TODO', t.priority, NULL, NULL, "
                + "?, GREATEST(CURDATE(), t.planned_end_date), "
                + "DATE_ADD(GREATEST(CURDATE(), t.planned_end_date), INTERVAL 3 DAY), "
                + "NULL, 'LOW', " + reviewSql + ", NOW() "
                + "FROM task t WHERE t.task_id = ?";

        return qr.update(sql,
                t.getTaskCode(),
                t.getTaskName(),
                t.getDescription(),
                t.getCreatedBy(),
                t.getParentId());
    }

    
    public int insertRegressionTest(Task t) throws Exception {

        String sql = "INSERT INTO task ("
                + "task_code, requirement_id, task_name, description, task_type, "
                + "parent_id, status, priority, created_by, "
                + "planned_start_date, planned_end_date, risk_level, review_status"
                + ") "
                + "SELECT ?, requirement_id, CONCAT('Regression: ', task_name), description, "
                + "'REGRESSION_TEST', task_id, 'TODO', priority, ?, "
                + "CURDATE(), DATE_ADD(CURDATE(), INTERVAL 2 DAY), 'LOW', 'PENDING' "
                + "FROM task WHERE task_id = ?";

        return qr.update(sql,
                t.getTaskCode(),
                t.getCreatedBy(),
                t.getParentId());
    }

    
    public int insertDefectTask(Task t) throws Exception {

        String sql = "INSERT INTO task ("
                + "task_code, requirement_id, task_name, description, task_type, "
                + "parent_id, status, priority, severity, "
                + "created_by, planned_start_date, planned_end_date, "
                + "risk_level, review_status"
                + ") "
                + "SELECT ?, requirement_id, ?, ?, 'DEFECT', "
                + "task_id, 'TODO', priority, ?, "
                + "?, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 3 DAY), "
                + "'LOW', 'PENDING' "
                + "FROM task WHERE task_id = ?";

        return qr.update(sql,
                t.getTaskCode(),
                t.getTaskName(),
                t.getDescription(),
                t.getSeverity(),
                t.getCreatedBy(),
                t.getParentId());
    }

    
    public int insertRegressionDefect(Task t) throws Exception {

        String sql = "INSERT INTO task ("
                + "task_code, requirement_id, task_name, description, task_type, "
                + "parent_id, status, priority, created_by, "
                + "planned_start_date, planned_end_date, risk_level, review_status"
                + ") "
                + "SELECT ?, requirement_id, ?, ?, 'DEFECT', "
                + "task_id, 'TODO', priority, ?, "
                + "CURDATE(), DATE_ADD(CURDATE(), INTERVAL 3 DAY), 'LOW', 'PENDING' "
                + "FROM task WHERE task_id = ?";

        return qr.update(sql,
                t.getTaskCode(),
                t.getTaskName(),
                t.getDescription(),
                t.getCreatedBy(),
                t.getParentId());
    }

    // =====================================================================
    // 更新
    // =====================================================================

    
    public int assignTask(Task t) throws Exception {

        String sql = "UPDATE task SET assignee_id = ?, assigned_by = ?, "
                + "planned_end_date = ?, status = 'IN_PROGRESS' "
                + "WHERE task_id = ? AND status = 'TODO'";

        return qr.update(sql,
                t.getAssigneeId(),
                t.getAssignedBy(),
                t.getPlannedEndDate(),
                t.getTaskId());
    }

    
    public int submitDeliverable(Task t) throws Exception {

        // 每行末尾空格不能省
        String sql = "UPDATE task SET "
                + "deliverable_description = ?, "
                + "deliverable_url = ?, "
                + "deliverable_file = ?, "
                + "submission_note = ?, "
                + "submitted_by = ?, "
                + "submitted_at = NOW(), "
                + "status = 'SUBMITTED' "
                + "WHERE task_id = ?";

        return qr.update(sql,
                t.getDeliverableDescription(),
                t.getDeliverableUrl(),
                t.getDeliverableFile(),
                t.getSubmissionNote(),
                t.getSubmittedBy(),
                t.getTaskId());
    }

    
    public int submitDefect(Task t) throws Exception {

        String sql = "UPDATE task SET "
                + "status = 'SUBMITTED', "
                + "root_cause = ?, fix_method = ?, affected_scope = ?, "
                + "deliverable_url = ?, deliverable_file = ?, submission_note = ?, "
                + "submitted_by = ?, submitted_at = NOW(), actual_end_at = NOW() "
                + "WHERE task_id = ? AND status = 'IN_PROGRESS'";

        return qr.update(sql,
                t.getRootCause(),
                t.getFixMethod(),
                t.getAffectedScope(),
                t.getDeliverableUrl(),
                t.getDeliverableFile(),
                t.getSubmissionNote(),
                t.getSubmittedBy(),
                t.getTaskId());
    }

    
    public int updateTestResult(Task t) throws Exception {

        String sql = "UPDATE task SET "
                + "status = 'COMPLETED', "
                + "review_status = ?, "
                + "test_plan = ?, "
                + "test_case_design = ?, "
                + "passed_count = ?, "
                + "failed_count = ?, "
                + "deliverable_url = ?, "
                + "deliverable_file = ?, "
                + "submission_note = ?, "
                + "submitted_by = ?, "
                + "submitted_at = NOW(), "
                + "actual_end_at = NOW() "
                + "WHERE task_id = ? AND status = 'IN_PROGRESS'";

        return qr.update(sql,
                t.getReviewStatus(),
                t.getTestPlan(),
                t.getTestCaseDesign(),
                t.getPassedCount(),
                t.getFailedCount(),
                t.getDeliverableUrl(),
                t.getDeliverableFile(),
                t.getSubmissionNote(),
                t.getSubmittedBy(),
                t.getTaskId());
    }

    
    public int completeTask(Task t) throws Exception {

        String sql = "UPDATE task SET "
                + "status = 'COMPLETED', "
                + "review_status = 'PASSED', "
                + "actual_end_at = NOW() "
                + "WHERE task_id = ? AND status = 'SUBMITTED'";

        return qr.update(sql, t.getTaskId());
    }

    
    public int markTestPassed(Task t) throws Exception {

        String sql = "UPDATE task SET "
                + "review_status = 'PASSED' "
                + "WHERE task_id = ? AND review_status = 'FAILED'";

        return qr.update(sql, t.getTaskId());
    }


    
    public String getRequirementCode(String requirementId) throws Exception {

        String sql = "SELECT requirement_code FROM requirement WHERE requirement_id = ?";
        String reqCode = qr.query(sql, new ScalarHandler<String>(),
                Integer.parseInt(requirementId.trim()));
        return reqCode == null ? "REQ" : reqCode.trim();
    }

    
    public String generateTaskCode(String reqCode, String taskType) throws Exception {

        //type abbreviation
        String typeShort;
        if ("TEST".equals(taskType)) {
            typeShort = "TST";
        } else if ("DEFECT".equals(taskType)) {
            typeShort = "BUG";
        } else if ("DOCUMENT".equals(taskType)) {
            typeShort = "DOC";
        } else {
            typeShort = "DEV";
        }
        String prefix = reqCode.trim() + "-" + typeShort + "-";

        //Take the largest serial number of the same prefix
        String sql = "SELECT task_code FROM task WHERE task_code LIKE ? "
                + "ORDER BY task_code DESC LIMIT 1";
        String last = qr.query(sql, new ScalarHandler<String>(), prefix + "%");

        int next = 1;
        if (last != null && last.length() > prefix.length()) {
            String numPart = last.substring(prefix.length()).trim();
            try {
                next = Integer.parseInt(numPart) + 1;
            } catch (NumberFormatException e) {
                next = 1;
            }
        }
        //digits : 001, 002 ... 999
        return prefix + String.format("%03d", next);
    }

    
    public String generateDefectCode() throws Exception {

        String sql = "SELECT COUNT(*) FROM task WHERE task_type = 'DEFECT'";
        Long cnt = qr.query(sql, new ScalarHandler<Long>());

        return "DEF-" + String.format("%05d", (cnt == null ? 0 : cnt.intValue()) + 1);
    }

    
    public String generateRegTestCode() throws Exception {

        String sql = "SELECT COUNT(*) FROM task WHERE task_type = 'REGRESSION_TEST'";
        Long cnt = qr.query(sql, new ScalarHandler<Long>());

        return "RT-" + String.format("%05d", (cnt == null ? 0 : cnt.intValue()) + 1);
    }

    
    public int findTaskIdByCode(String taskCode) throws Exception {

        String sql = "SELECT task_id FROM task WHERE task_code = ?";
        Integer id = qr.query(sql, new ScalarHandler<Integer>(), taskCode);

        return id == null ? 0 : id.intValue();
    }

    
    public Long countOpenDefectsByTest(String testTaskId) throws Exception {

        String sql = "SELECT COUNT(*) FROM task "
                + "WHERE parent_id = ? AND task_type = 'DEFECT' "
                + "AND status <> 'COMPLETED'";

        Long cnt = qr.query(sql, new ScalarHandler<Long>(),
                Integer.parseInt(testTaskId.trim()));
        return cnt == null ? 0L : cnt;
    }



    public void insertStatusHistorySafely(String taskId, String fromStatus, String toStatus,
                                          String fromReview, String toReview,
                                          String operatorId, String comment) {

        TaskStatusHistoryMapper history = new TaskStatusHistoryMapper();
        try {
            history.insert(taskId, fromStatus, toStatus, fromReview, toReview,
                    operatorId, comment);
        } catch (Exception e) {
            try {
                history.insert(taskId, fromStatus, toStatus, operatorId, comment);
            } catch (Exception e2) {
                System.out.println("WARN: task_status_history not written: " + e2.getMessage());
            }
        }
    }

    
    public Map<String, Object> createTestTaskForDevTask(Task devTask, String operatorId)
            throws Exception {

        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("code", null);
        result.put("created", Boolean.FALSE);
        result.put("skipped", Boolean.FALSE);

        Task dev = selectForAutoTest(devTask);
        if (dev == null) {
            result.put("skipped", Boolean.TRUE);
            return result;
        }

        // 只有开发任务才派生测试任务
        String devType = dev.getTaskType();
        if (!"DEVELOPMENT".equals(devType)) {
            result.put("skipped", Boolean.TRUE);
            return result;
        }

        // 编号：REQ001-TST-001
        String requirementId = String.valueOf(dev.getRequirementId());
        String reqCode = getRequirementCode(requirementId);
        String testCode = generateTaskCode(reqCode, "TEST");

        String devName = dev.getTaskName();
        String testName = "Test for " + devName;

        // 沿用开发提交的交付说明，作为"要测什么"的依据
        String devDesc = dev.getDeliverableDescription();
        String testDesc = devDesc == null
                ? ("Auto-generated test task for " + devName)
                : ("Auto-generated test task for " + devName + " / " + devDesc);

        // 自动生成的任务不在生成时分配 —— 不查 TESTER，
        // assignee_id / assigned_by 均为 NULL，status = 'TODO'，
        // 等 LEAD 在任务分配页（DPGL110TASKASSIGN）指派后才变 IN_PROGRESS
        Task testTask = new Task();
        testTask.setTaskCode(testCode);
        testTask.setTaskName(testName);
        testTask.setDescription(testDesc);
        testTask.setCreatedBy(toIntOrNull(operatorId));
        testTask.setParentId(dev.getTaskId());
        insertAutoTestTask(testTask);

        // 记录新任务的状态历史
        int newId = findTaskIdByCode(testCode);
        if (newId > 0) {
            insertStatusHistorySafely(String.valueOf(newId), null, "TODO",
                    null, REVIEW_ENABLED ? "PENDING" : "APPROVED", operatorId,
                    "Auto-generated test task from dev task " + dev.getTaskId()
                            + " (unassigned, waiting for LEAD to assign)");
        }

        result.put("code", testCode);
        return result;
    }

    public List<Map<String, Object>> searchSubmittedTasks(String userId) throws Exception {

        String sql = "SELECT t.task_id, t.task_code, t.task_name, t.task_type, "
                + "p.project_id, p.project_name, "
                + "r.requirement_id, r.requirement_name, "
                + "t.assignee_id, u.username AS assignee_name, "
                // 提交时间在 SQL 里格式化，Java 端直接显示
                + "DATE_FORMAT(t.submitted_at, '%Y/%m/%d %H:%i:%s') AS submitted_at, "
                + "t.review_status "
                + "FROM task t "
                + "JOIN requirement r ON t.requirement_id = r.requirement_id "
                + "JOIN project p ON r.project_id = p.project_id "
                // 受让人可能为空，LEFT JOIN 容纳
                + "LEFT JOIN user_info u ON t.assignee_id = u.user_id "
                // 项目负责人(leader_id) 或 项目创建人(created_by) 都可查看/审批
                + "WHERE t.status = 'SUBMITTED' "
                + "  AND (p.leader_id = ? OR p.created_by = ?) "
                + "ORDER BY p.project_id, t.task_id";

        // 分别匹配 project.leader_id 和 project.created_by
        int uid = Integer.parseInt(userId.trim());
        return qr.query(sql, new MapListHandler(), uid, uid);
    }
    private Integer toIntOrNull(String s) {
        return (s == null || s.trim().isEmpty()) ? null : Integer.valueOf(s.trim());
    }

    public Map<String, Object> getTaskDetail(String taskId) throws Exception {

        // 每行末尾空格不能省
        String sql = "SELECT t.task_id, t.task_code, t.task_name, "
                + "t.description AS task_description, "
                + "t.task_type, t.priority, t.status, "
                + "p.project_id, p.project_name, p.project_description, "
                + "r.requirement_id, r.requirement_name, "
                + "r.description AS requirement_description, "
                // 受让人可能为空，LEFT JOIN
                + "t.assignee_id, u.username AS assignee_name, "
                // 提交时间在 SQL 里格式化
                + "DATE_FORMAT(t.submitted_at, '%Y/%m/%d %H:%i:%s') AS submitted_at, "
                // 通用提交字段
                + "t.deliverable_description, t.deliverable_url, t.submission_note, "
                // 类型专有字段
                + "t.test_plan, t.test_case_design, t.severity, "
                + "t.review_status "
                + "FROM task t "
                + "JOIN requirement r ON t.requirement_id = r.requirement_id "
                + "JOIN project p ON r.project_id = p.project_id "
                + "LEFT JOIN user_info u ON t.assignee_id = u.user_id "
                + "WHERE t.task_id = ?";

        return qr.query(sql, new MapHandler(), Integer.parseInt(taskId.trim()));
    }


    public int reviewTask(String taskId, String reviewStatus, String nextStatus)
            throws Exception {

        String sql = "UPDATE task SET review_status = ?, status = ? "
                + "WHERE task_id = ? AND status = 'SUBMITTED'";

        return qr.update(sql, reviewStatus, nextStatus,
                Integer.parseInt(taskId.trim()));
    }

    public boolean existsByTaskCode(String taskCode) throws Exception {

        String sql = "SELECT COUNT(*) FROM task WHERE task_code = ?";
        Long cnt = qr.query(sql, new ScalarHandler<Long>(), taskCode);
        return cnt != null && cnt.longValue() > 0;
    }

    /**
     * 查需求信息（project_id、requirement_name）。
     *
     * @param requirementId 需求 id
     * @return Map：project_id、requirement_name；查不到返回 null
     */
    public Map<String, Object> getRequirementInfo(String requirementId) throws Exception {

        String sql = "SELECT requirement_id, project_id, requirement_name "
                + "FROM requirement WHERE requirement_id = ?";

        return qr.query(sql, new MapHandler(), Integer.parseInt(requirementId.trim()));
    }

    //查某项目的在职成员（受让人候选）
    public List<Map<String, Object>> findProjectMembers(String projectId) throws Exception {

        // 注意：每行末尾空格不能省
        String sql = "SELECT pm.user_id, u.username "
                + "FROM project_member pm "
                + "JOIN user_info u ON pm.user_id = u.user_id "
                + "WHERE pm.project_id = ? "
                + "  AND pm.member_status = 'ACTIVE' "
                + "ORDER BY pm.user_id";

        return qr.query(sql, new MapListHandler(), Integer.parseInt(projectId.trim()));
    }

    //插入新任务
    public int insertTask(String taskCode, String requirementId, String taskName,
                          String description, String taskType, String priority,
                          String createdBy, String assigneeId, String assignedBy,
                          String plannedStart, String plannedEnd) throws Exception {

        // 注意：每行末尾空格不能省
        String sql = "INSERT INTO task ("
                + "task_code, requirement_id, task_name, description, task_type, priority, "
                + "created_by, assignee_id, assigned_by, status, "
                + "planned_start_date, planned_end_date, risk_level, review_status"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'TODO', ?, ?, 'LOW', 'PENDING')";

        // 受让人允许为空（未分配）
        Integer assigneeInt = (assigneeId == null || assigneeId.trim().length() == 0)
                ? null : Integer.parseInt(assigneeId.trim());

        return qr.update(sql,
                taskCode,
                Integer.parseInt(requirementId.trim()),
                taskName,
                description,
                taskType,
                priority,
                Integer.parseInt(createdBy.trim()),
                assigneeInt,
                Integer.parseInt(assignedBy.trim()),
                plannedStart,
                plannedEnd);
    }

    // 按 task_id 查回显所需的完整任务字段。
    public Map<String, Object> getTaskForEdit(String taskId) throws Exception {

        // 每行末尾空格不能省
        String sql = "SELECT t.task_id, t.task_name, t.description, t.task_type, "
                + "t.test_plan, t.test_case_design, t.severity, "
                + "t.priority, t.assignee_id, "
                + "DATE_FORMAT(t.planned_start_date, '%Y/%m/%d') AS planned_start_date, "
                + "DATE_FORMAT(t.planned_end_date, '%Y/%m/%d') AS planned_end_date, "
                + "r.requirement_id, r.requirement_name "
                + "FROM task t "
                + "JOIN requirement r ON t.requirement_id = r.requirement_id "
                + "WHERE t.task_id = ?";

        return qr.query(sql, new MapHandler(), Integer.parseInt(taskId.trim()));
    }

    

}
