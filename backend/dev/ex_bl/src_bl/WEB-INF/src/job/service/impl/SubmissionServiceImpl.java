package job.service.impl;

import com.yse.gb.gf.front.CommandException;
import com.yse.gb.gf.front.InputException;
import com.yse.gb.gf.front.UserMessage;
import jakarta.enterprise.context.RequestScoped;
import job.entity.Task;
import job.mapper.task.TaskMapper;
import job.mapper.task.TaskStatusHistoryMapper;
import job.service.SubmissionService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@RequestScoped
public class SubmissionServiceImpl implements SubmissionService {

    // =========================================================
    // 查询（提交页打开时用）
    // =========================================================

    @Override
    public Task getTaskInfo(Task task) throws Exception {
        return new TaskMapper().selectForSubmit(task);
    }

    @Override
    public Task findTaskById(Task task) throws Exception {
        return new TaskMapper().selectById(task);
    }

    @Override
    public Task findDefectById(Task task) throws Exception {
        return new TaskMapper().selectForDefect(task);
    }

    @Override
    public Task findRegressionById(Task task) throws Exception {
        return new TaskMapper().selectForRegression(task);
    }

    // =========================================================
    // 四类提交
    // =========================================================

    @Override
    public String[] submitDeliverable(Task task) throws Exception {

        // 实体字段 → 原来的局部变量（下面的校验顺序、异常文案与日志照旧）
        String taskId = toText(task.getTaskId());
        String userId = toText(task.getSubmittedBy());

        // Check: task_id is required
        if (isBlank(taskId)) {
            throw new CommandException(new UserMessage("Task information is missing, please re-enter from the task list."));
        }

        // 登录用户（提交人）
        if (isBlank(userId)) {
            throw new CommandException(new UserMessage("Login user is missing, please log in again."));
        }

        String message;
        String generatedTestCode = null;

        try {
            int rows = submitTask(task);
            System.out.println("TASK SUBMITTED, task_id=" + taskId + ", rows=" + rows);
            message = "The task has been submitted, and the status has been updated to SUBMITTED.";
        } catch (Exception e) {
            System.out.println("TASK SUBMIT ERROR: " + e.getMessage());
            throw new CommandException("Submission failed：" + e.getMessage());
        }

        // 开发提交成功后，自动派生一个 TEST 子任务（parent_id 指向本任务）
        try {
            Map<String, Object> r = createTestTaskForDevTask(task, userId);

            Boolean skipped = (Boolean) r.get("skipped");
            Boolean created = (Boolean) r.get("created");
            String code = (String) r.get("code");

            if (skipped != null && skipped.booleanValue()) {
                System.out.println(">>> No test task generated (task_type is not DEVELOPMENT).");
            } else if (created != null && created.booleanValue()) {
                System.out.println(">>> TEST TASK GENERATED: " + code);
                message = "The task has been submitted. A test task ("
                        + code + ") was created automatically.";
                generatedTestCode = code;
            } else {
                System.out.println(">>> Test task already exists, skip generation: " + code);
                message = "The task has been submitted. The test task ("
                        + code + ") already exists.";
                generatedTestCode = code;
            }
        } catch (Exception e) {
            System.out.println(">>> TEST TASK GENERATION ERROR: " + e.getMessage());
            message = "The task has been submitted, but the test task could not be created automatically.";
        }

        return new String[] { message, generatedTestCode };
    }

    @Override
    public String submitTestResult(Task task, List<String[]> bugRows) throws Exception {

        System.out.println("====DPGL120SUBT SUBMIT executed====");

        // 实体字段 → 原来的局部变量（下面的校验顺序、异常文案与日志照旧）。
        // passedCount / failedCount 在实体里是 Integer，转回原字符串形式再走原校验；
        // reviewStatus 承载画面的 Test Result（PASSED / FAILED）
        String userId = toText(task.getSubmittedBy());
        String taskId = toText(task.getTaskId());
        String testPlan = task.getTestPlan();
        String testCaseDesign = task.getTestCaseDesign();
        String passedCount = toText(task.getPassedCount());
        String failedCount = toText(task.getFailedCount());
        String testResult = task.getReviewStatus();

        // 登录用户
        if (isBlank(userId)) {
            throw new CommandException(new UserMessage("Session expired, please login again."));
        }

        System.out.println("SUBMIT PARAMS: taskId=" + taskId + ", result=" + testResult);

        // 校验
        if (isBlank(taskId)) {
            throw new InputException(new UserMessage("Task ID is missing."));
        }
        if (isBlank(testPlan)) {
            throw new InputException(new UserMessage("Please input Test Plan."));
        }
        if (isBlank(testCaseDesign)) {
            throw new InputException(new UserMessage("Please input Test Case Design."));
        }
        if (isBlank(passedCount) || isBlank(failedCount)) {
            throw new InputException(new UserMessage("Please input Passed / Failed count."));
        }
        if (!passedCount.trim().matches("\\d+") || !failedCount.trim().matches("\\d+")) {
            throw new InputException(new UserMessage("Passed / Failed count must be numbers."));
        }
        if (isBlank(testResult)) {
            throw new InputException(new UserMessage("Please select Test Result."));
        }

        // 任务存在 + 状态校验（防止重复提交）
        Task taskRow = findTaskById(task);
        if (taskRow == null) {
            throw new InputException(new UserMessage("Task not found."));
        }
        if (!"IN_PROGRESS".equals(String.valueOf(taskRow.getStatus()))) {
            throw new InputException(new UserMessage("This task is not in progress."));
        }

        // FAILED 时必须至少一条 BUG
        List<String[]> bugs = new ArrayList<String[]>();
        if ("FAILED".equals(testResult)) {
            for (int i = 0; i < bugRows.size(); i++) {
                String[] row = bugRows.get(i);
                String title    = row[0];
                String severity = row[1];
                String desc     = row[2];

                if (isBlank(title)) {
                    continue; // 空行跳过
                }
                if (isBlank(severity)) {
                    throw new InputException(new UserMessage("Bug " + (i + 1) + ": please select Severity."));
                }
                if (isBlank(desc)) {
                    throw new InputException(new UserMessage("Bug " + (i + 1) + ": please input Description."));
                }
                bugs.add(new String[]{title, severity, desc});
            }
            if (bugs.isEmpty()) {
                throw new InputException(new UserMessage(
                        "Test Result is FAILED, please fill in at least one bug."));
            }
        }

        //  更新 TEST 任务 + 写状态历史
        int rows = updateTestResult(task);
        if (rows == 0) {
            throw new CommandException(new UserMessage("Submit failed: task status has changed."));
        }
        insertHistory(taskId, "IN_PROGRESS", "COMPLETED",
                "PENDING", testResult, userId, "Test result submitted");

        // 5. FAILED：每条 BUG 创建一条 DEFECT 任务
        if ("FAILED".equals(testResult)) {
            for (String[] bug : bugs) {
                String defectCode = generateDefectCode();
                insertDefectTask(taskId, defectCode, bug[0], bug[1], bug[2], userId);
                System.out.println("DEFECT created: " + defectCode);
            }
        }

        // PASSED：级联完成父任务（DEV）
        if ("PASSED".equals(testResult)) {
            Integer parentId = taskRow.getParentId();
            if (parentId != null && !"null".equals(String.valueOf(parentId))) {
                int cascaded = completeParentTask(String.valueOf(parentId));
                if (cascaded > 0) {
                    insertHistory(String.valueOf(parentId), "SUBMITTED", "COMPLETED",
                            "PENDING", "PASSED", userId, "Cascade complete: test passed");
                }
            }
        }

        // 结果消息（回列表页的 MEMA_MSG）
        return "Test result submitted: " + String.valueOf(taskRow.getTaskCode())
                + ("FAILED".equals(testResult) ? " (" + bugs.size() + " defect(s) created)" : "");
    }

    @Override
    public String submitDefectFix(Task task) throws Exception {

        // 实体字段 → 原来的局部变量（下面的校验顺序、异常文案与日志照旧）
        String userId = toText(task.getSubmittedBy());
        String taskId = toText(task.getTaskId());
        String rootCause = task.getRootCause();
        String fixMethod = task.getFixMethod();
        String affectedScope = task.getAffectedScope();

        // 1. 登录用户
        if (isBlank(userId)) {
            throw new CommandException(new UserMessage("Session expired, please login again."));
        }

        // 2. 校验
        if (isBlank(taskId))        throw new InputException(new UserMessage("Task ID is missing."));
        if (isBlank(rootCause))     throw new InputException(new UserMessage("Please input Root Cause."));
        if (isBlank(fixMethod))     throw new InputException(new UserMessage("Please input Fix Method."));
        if (isBlank(affectedScope)) throw new InputException(new UserMessage("Please input Affected Scope."));

        Task taskRow = findDefectById(task);
        if (taskRow == null) {
            throw new InputException(new UserMessage("Defect task not found."));
        }
        if (!"IN_PROGRESS".equals(String.valueOf(taskRow.getStatus()))) {
            throw new InputException(new UserMessage("This defect is not in progress."));
        }

        // 3. 更新 DEFECT：IN_PROGRESS -> SUBMITTED（等回归测试验收）
        int rows = submitDefect(task);
        if (rows == 0) {
            throw new CommandException(new UserMessage("Submit failed: task status has changed."));
        }
        insertHistory(taskId, "IN_PROGRESS", "SUBMITTED",
                "PENDING", "PENDING", userId, "Defect fix submitted");

        // 4. 自动创建回归测试任务（parent_id = 这条 DEFECT）
        String rtCode = generateRegTestCode();
        insertRegressionTest(taskId, rtCode, userId);

        // 5. 结果消息
        return "Defect fix submitted: " + String.valueOf(taskRow.getTaskCode())
                + " (regression test " + rtCode + " created)";
    }

    @Override
    public String submitRegressionResult(Task task) throws Exception {

        System.out.println("====DPGL120SUBREGR SUBMIT executed====");

        // 实体字段 → 原来的局部变量（下面的校验顺序、异常文案与日志照旧）。
        // passedCount / failedCount 在实体里是 Integer，转回原字符串形式再走原校验；
        // reviewStatus 承载画面的 Test Result（PASSED / FAILED）
        String userId = toText(task.getSubmittedBy());
        String taskId = toText(task.getTaskId());
        String testPlan = task.getTestPlan();
        String testCaseDesign = task.getTestCaseDesign();
        String passedCount = toText(task.getPassedCount());
        String failedCount = toText(task.getFailedCount());
        String testResult = task.getReviewStatus();

        // 1. 登录用户
        if (isBlank(userId)) {
            throw new CommandException(new UserMessage("Session expired, please login again."));
        }

        // 2. 校验
        if (isBlank(taskId)) {
            throw new InputException(new UserMessage("Task ID is missing."));
        }
        if (isBlank(testPlan)) {
            throw new InputException(new UserMessage("Please input Test Plan."));
        }
        if (isBlank(testCaseDesign)) {
            throw new InputException(new UserMessage("Please input Test Case Design."));
        }
        if (isBlank(passedCount) || isBlank(failedCount)) {
            throw new InputException(new UserMessage("Please input Passed / Failed count."));
        }
        if (!passedCount.trim().matches("\\d+") || !failedCount.trim().matches("\\d+")) {
            throw new InputException(new UserMessage("Passed / Failed count must be numbers."));
        }
        if (isBlank(testResult)) {
            throw new InputException(new UserMessage("Please select Test Result."));
        }

        Task taskRow = findRegressionById(task);
        if (taskRow == null) {
            throw new InputException(new UserMessage("Regression task not found."));
        }
        if (!"IN_PROGRESS".equals(String.valueOf(taskRow.getStatus()))) {
            throw new InputException(new UserMessage("This regression task is not in progress."));
        }

        // 3. 提交回归结果 + 历史
        int rows = updateRegressionResult(task);
        if (rows == 0) {
            throw new CommandException(new UserMessage("Submit failed: task status has changed."));
        }
        insertHistory(taskId, "IN_PROGRESS", "COMPLETED",
                "PENDING", testResult, userId, "Regression result submitted");

        // 4. 分支处理
        String defectId = getParentId(taskId);   // RT 的父任务就是 DEFECT

        if ("FAILED".equals(testResult) && defectId != null) {
            // 回归失败：新建 DEFECT 挂回原 TEST（DEFECT 的父任务）
            String testTaskId = getParentId(defectId);

            if (testTaskId != null) {
                String defectCode = generateDefectCode();
                String bugTitle = "Regression failed: " + String.valueOf(taskRow.getTaskName());
                insertDefectTask(testTaskId, defectCode, bugTitle,
                        "Regression test result: FAILED", userId);
            }
        }

        // 先统一在下方处理，FAILED 时只需要新建 DEFECT，不做级联
        if ("PASSED".equals(testResult) && defectId != null) {
            // (a) DEFECT -> COMPLETED
            int defectDone = completeDefect(defectId);
            if (defectDone > 0) {
                insertHistory(defectId, "SUBMITTED", "COMPLETED",
                        "PENDING", "PASSED", userId, "Regression passed: defect fixed");
            }

            // (b) 查 DEFECT 的父任务（TEST）
            String testTaskId = getParentId(defectId);

            // (c) 该 TEST 下所有 DEFECT 都完成了吗？
            if (testTaskId != null
                    && countOpenDefectsByTest(testTaskId).longValue() == 0) {
                markTestPassed(testTaskId);
                insertHistory(testTaskId, "COMPLETED", "COMPLETED",
                        "FAILED", "PASSED", userId, "All defects fixed, test passed");

                // (d) 级联：TEST 通过 -> DEV -> COMPLETED
                String devTaskId = getParentId(testTaskId);
                if (devTaskId != null) {
                    int devDone = completeDevTask(devTaskId);
                    if (devDone > 0) {
                        insertHistory(devTaskId, "SUBMITTED", "COMPLETED",
                                "PENDING", "PASSED", userId, "Cascade complete: test passed");
                    }
                }
            }
        }

        // 5. 原画面不回填 MEMA_MSG，返回 null 让 COMMAND 跳过 put
        return null;
    }

    // =========================================================
    // 以下为 private：原一画面一服务的 mapper 转发方法，仅供上面 submit* 内部调用
    // =========================================================

    // ---------------- 原 dpgl120subServiceImpl ----------------

    
    private int submitTask(Task task) throws Exception {
        return new TaskMapper().submitDeliverable(task);
    }

    
    private Map<String, Object> createTestTaskForDevTask(Task devTask, String operatorId)
            throws Exception {
        return new TaskMapper().createTestTaskForDevTask(devTask, operatorId);
    }

    // ---------------- 原 dpgl120subtServiceImpl ----------------

    
    private int updateTestResult(Task task) throws Exception {
        return new TaskMapper().updateTestResult(task);
    }

    
    private void insertHistory(String taskId, String fromStatus, String toStatus,
                               String fromReview, String toReview,
                               String operatorId, String comment) throws Exception {
        new TaskStatusHistoryMapper().insert(taskId, fromStatus, toStatus,
                fromReview, toReview, operatorId, comment);
    }

    
    private String generateDefectCode() throws Exception {
        return new TaskMapper().generateDefectCode();
    }

    
    private void insertDefectTask(String parentTaskId, String taskCode,
                                  String bugTitle, String severity, String bugDesc,
                                  String createdBy) throws Exception {
        Task task = new Task();
        task.setParentId(Integer.valueOf(parentTaskId.trim()));
        task.setTaskCode(taskCode);
        task.setTaskName(bugTitle);
        task.setSeverity(severity);
        task.setDescription(bugDesc);
        task.setCreatedBy(Integer.valueOf(createdBy.trim()));
        new TaskMapper().insertDefectTask(task);
    }

    
    private int completeParentTask(String parentId) throws Exception {
        return new TaskMapper().completeTask(taskRef(parentId));
    }

    // ---------------- 原 dpgl120subdServiceImpl ----------------

    
    private int submitDefect(Task task) throws Exception {
        return new TaskMapper().submitDefect(task);
    }

    
    private String generateRegTestCode() throws Exception {
        return new TaskMapper().generateRegTestCode();
    }

    
    private void insertRegressionTest(String defectTaskId, String rtCode,
                                      String createdBy) throws Exception {
        Task task = new Task();
        task.setParentId(Integer.valueOf(defectTaskId.trim()));
        task.setTaskCode(rtCode);
        task.setCreatedBy(Integer.valueOf(createdBy.trim()));
        new TaskMapper().insertRegressionTest(task);
    }

    // ---------------- 原 dpgl120subregrServiceImpl ----------------

    
    private int updateRegressionResult(Task task) throws Exception {
        return new TaskMapper().updateTestResult(task);
    }

    
    private String getParentId(String taskId) throws Exception {
        return new TaskMapper().getParentId(taskRef(taskId));
    }

    
    private void insertDefectTask(String testTaskId, String taskCode,
                                  String bugTitle, String bugDesc,
                                  String createdBy) throws Exception {
        Task task = new Task();
        task.setParentId(Integer.valueOf(testTaskId.trim()));
        task.setTaskCode(taskCode);
        task.setTaskName(bugTitle);
        task.setDescription(bugDesc);
        task.setCreatedBy(Integer.valueOf(createdBy.trim()));
        new TaskMapper().insertRegressionDefect(task);
    }

    
    private int completeDefect(String defectId) throws Exception {
        return new TaskMapper().completeTask(taskRef(defectId));
    }

    
    private Long countOpenDefectsByTest(String testTaskId) throws Exception {
        return new TaskMapper().countOpenDefectsByTest(testTaskId);
    }

    
    private int markTestPassed(String testTaskId) throws Exception {
        return new TaskMapper().markTestPassed(taskRef(testTaskId));
    }

    
    private int completeDevTask(String devTaskId) throws Exception {
        return new TaskMapper().completeTask(taskRef(devTaskId));
    }

    
    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    
    private String toText(Integer value) {
        return (value == null) ? null : String.valueOf(value);
    }

    
    private Task taskRef(String taskId) {
        Task task = new Task();
        task.setTaskId(Integer.valueOf(taskId.trim()));
        return task;
    }
}
