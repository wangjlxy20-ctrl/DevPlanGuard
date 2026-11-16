package job.service.impl;

import com.yse.gb.gf.front.CommandException;
import com.yse.gb.gf.front.UserMessage;

import jakarta.enterprise.context.RequestScoped;

import job.entity.ProjectMember;
import job.entity.Requirement;
import job.entity.Task;
import job.mapper.requirement.RequirementMapper;
import job.mapper.task.TaskMapper;
import job.service.TaskService;

import java.util.ArrayList;
import java.util.List;

@RequestScoped
public class TaskServiceImpl implements TaskService {

    @Override
    public List<Task> searchTasks(Task condition) throws Exception {
        List<Task> tasksList = new TaskMapper().searchTasks(condition);
        return tasksList;
    }

    @Override
    public Task getTask(Task task) throws Exception {
        Task detail = new TaskMapper().selectById(task);
        return detail;
    }

    @Override
    public Integer getProjectId(Task task) throws Exception {
        Task currentTask = new TaskMapper().selectById(task);
        if (currentTask == null || currentTask.getRequirementId() == null) {
            return null;
        }

        Requirement requirement = new Requirement();
        requirement.setRequirementId(currentTask.getRequirementId());
        Requirement requirementInfo = new RequirementMapper().selectById(requirement);
        return (requirementInfo == null) ? null : requirementInfo.getProjectId();
    }

    @Override
    public String getAssignTargetScreenId(Task task) throws Exception {
        if (task == null || task.getTaskId() == null) {
            throw new CommandException("Can't find the clicked task.");
        }

        Task loaded;
        try {
            loaded = getTask(task);
        } catch (Exception e) {
            throw new CommandException("Failed to load task info.");
        }

        if (loaded != null && loaded.getAssigneeId() == null) {
            return "DPGL110TASKASSIGN";
        } else {
            return "DPGL110TASKASSIGNEE";
        }
    }

    @Override
    public List<ProjectMember> getAssignableMembers(Task task) throws Exception {
        List<ProjectMember> members = new TaskMapper().findProjectMembersByTask(task);
        return members;
    }

    @Override
    public String[] createTask(Task task) throws Exception {
        if (task == null || task.getCreatedBy() == null) {
            return new String[]{"Your login has expired, please log in again.", null};
        }
        if (task.getRequirementId() == null) {
            return new String[]{"Missing requirement_id information, please go back and enter from the requirement list.", null};
        }
        if (isBlank(task.getTaskName())) {
            return new String[]{"Task Name : can't be empty", null};
        }
        if (task.getPlannedStartDate() == null) {
            return new String[]{"Planned Start Date : can't be empty", null};
        }
        if (task.getPlannedEndDate() == null) {
            return new String[]{"Planned End Date : can't be empty", null};
        }
        if (task.getPlannedEndDate().compareTo(task.getPlannedStartDate()) < 0) {
            return new String[]{"The planned end date can't be earlier than the planned start date.", null};
        }
        if ("TEST".equals(task.getTaskType()) && isBlank(task.getTestPlan())) {
            return new String[]{"Test Plan : can't be empty when the task type is TEST.", null};
        }
        if ("DEFECT".equals(task.getTaskType()) && isBlank(task.getSeverity())) {
            return new String[]{"Severity : please select a severity when the task type is DEFECT.", null};
        }

        // AUTO GENERATE task_code : REQ_CODE + type + serial number
        String taskCode;
        try {
            // 沿用 TaskMapper 版（带 "REQ" 兜底 + trim）：
            // RequirementService.getRequirementCode 用的是 requirementMapper 版（无兜底），不能替换
            String reqCode = new TaskMapper().getRequirementCode(
                    String.valueOf(task.getRequirementId()));
            taskCode = getGenerateTaskCode(reqCode,
                    isBlank(task.getTaskType()) ? "DEVELOPMENT" : task.getTaskType());
        } catch (Exception e) {
            System.out.println("GENERATE TASK CODE FAILED: " + e.getMessage());
            return new String[]{"Failed to generate the task code, please try again.", null};
        }

        //INSERT
        int n = getAddTask(task, taskCode.trim());

        System.out.println("TASK INSERTED, rows = " + n);

        return new String[]{null, "Task created successfully：" + taskCode.trim()};
    }

    @Override
    public String[] submitAssignment(Task task, String[] assigneeRows) throws Exception {
        if (task == null || task.getAssignedBy() == null) {
            return new String[]{"Session expired, please login again.", "", null};
        }
        if (task.getTaskId() == null) {
            return new String[]{"Task ID is missing.", "", null};
        }
        if (task.getPlannedEndDate() == null) {
            return new String[]{"Please specify the task due date.", "", null};
        }

        // 按行取复选框：勾选行=user_id，未勾选=null
        List<String> picked = new ArrayList<>();
        if (assigneeRows != null) {
            for (String r : assigneeRows) {
                if (r != null && r.trim().length() > 0 && !"null".equals(r.trim())) {
                    picked.add(r.trim());
                }
            }
        }
        System.out.println("PICKED MEMBERS = " + picked);

        if (picked.isEmpty()) {
            return new String[]{"Please select a member to assign.", "", null};
        }
        if (picked.size() > 1) {
            return new String[]{"Please select only ONE member.", "", null};
        }

        String assigneeId = picked.getFirst();

        // 校验任务存在且为 TODO（并记下 requirement_id）
        String requirementId = "";
        try {
            Task currentTask = getTaskById(task);
            if (currentTask == null) {
                return new String[]{"Task not found.", requirementId, null};
            }
            if (!"TODO".equals(String.valueOf(currentTask.getStatus()))) {
                return new String[]{"This task is not waiting for assignment.", requirementId, null};
            }
            requirementId = String.valueOf(currentTask.getRequirementId());
        } catch (Exception e) {
            return new String[]{"Check failed: " + e.getMessage(), requirementId, null};
        }

        // 执行分配（含纳期）
        int rows = getAssignTask(task, Integer.valueOf(assigneeId));
        System.out.println("ASSIGN DONE, task_id=" + task.getTaskId() + ", rows=" + rows);

        if (rows == 0) {
            throw new CommandException("Assign failed: task status has changed.");
        }

        return new String[]{null, requirementId, "Task assigned to user " + assigneeId + "."};
    }

    @Override
    public String resolveRequirementId(Task task) throws Exception {
        String requirementId = "";
        if (task != null && task.getTaskId() != null) {
            try {
                Task currentTask = getTaskById(task);
                if (currentTask != null) {
                    requirementId = String.valueOf(currentTask.getRequirementId());
                }
            } catch (Exception e) {
                System.out.println("CANCEL resolve requirement error: " + e.getMessage());
            }
        }
        return requirementId;
    }

    @Override
    public List<Task> getMyTasks(String userId,String projectId) throws Exception {
        Task condition = new Task();
        condition.setAssigneeId(Integer.valueOf(userId.trim()));
        condition.setProjectId(Integer.valueOf(projectId));
        List<Task> taskList = new TaskMapper().searchMyTasks(condition);
        return taskList;
    }

    @Override
    public String getSubmitScreenId(String taskType) throws Exception {
        if ("DEVELOPMENT".equals(taskType)){
            System.out.println(">>>>> DPGL120PJTMEM WILL ENTER TO >>>> DPGL120SUB ");
            return "DPGL120SUB";
        } else if ("TEST".equals(taskType)) {

            System.out.println(">>>>> DPGL120PJTMEM WILL ENTER TO >>>> DPGL120SUBT ");
            return "DPGL120SUBT";
        } else if ("DEFECT".equals(taskType)) {
            System.out.println(">>>>> DPGL120PJTMEM WILL ENTER TO >>>> DPGL120SUBD ");
            return "DPGL120SUBD";
        }else if ("REGRESSION_TEST".equals(taskType)) {
            System.out.println(">>>>> DPGL120PJTMEM WILL ENTER TO >>>> DPGL120SUBREGR ");
            return "DPGL120SUBREGR";
        }
        else {
            System.out.println(">>>>> DPGL120PJTMEM WILL ENTER TO >>>> DPGL120SUB ");
            return "DPGL120SUB";
        }
    }

    @Override
    public String[] resolveSubmitTarget(String buttonName, String[] taskIdCol, String[] taskTypeCol)
            throws Exception {
        String taskId = null;
        String taskType = null;

        if (buttonName != null && buttonName.startsWith("BTNCMD_ADD_R_")) {
            int rowIdx = Integer.parseInt(buttonName.substring("BTNCMD_ADD_R_".length()));

            if (taskIdCol != null && rowIdx >= 0 && rowIdx < taskIdCol.length) {
                taskId = taskIdCol[rowIdx];
                if (taskTypeCol != null && rowIdx < taskTypeCol.length) {
                    taskType = taskTypeCol[rowIdx];
                }
            }
        }

        System.out.println("DPGL120SUB COMMAND >>>> ADD TARGET: taskId=" + taskId
                + ", taskType=" + taskType);

        //画面跳转（按任务类型分流到对应的提交画面）
        String targetScreenId = getSubmitScreenId(taskType);

        if (taskId == null || taskId.trim().length() == 0) {
            throw new CommandException(new UserMessage("Can't find the task to submit"));
        }

        System.out.println("DPGL120PJTMEM>>> SUBMIT DONE: taskId=" + taskId);
        System.out.println("DPGL120PJTMEM>>> SUBMIT DONE: taskType=" + taskType);

        return new String[] { taskId, taskType, targetScreenId };
    }

    @Override
    public int assignTask(String taskId, String assigneeId, String assignedBy, String dueDate) throws Exception {

        return 0;
    }


    private String getGenerateTaskCode(String reqCode, String taskType) throws Exception {
        String taskCode = new TaskMapper().generateTaskCode(reqCode, taskType);
        return taskCode;
    }

    
    private int getAddTask(Task form, String taskCode) throws Exception {

        Task task = new Task();
        task.setTaskCode(taskCode);
        task.setRequirementId(form.getRequirementId());
        task.setTaskName(form.getTaskName().trim());
        task.setDescription(form.getDescription());
        task.setTaskType(isBlank(form.getTaskType()) ? "DEVELOPMENT" : form.getTaskType());
        task.setPriority(isBlank(form.getPriority()) ? "MEDIUM" : form.getPriority());
        task.setCreatedBy(form.getCreatedBy());
        // assignee / assigned_by 允许为空（自动生成待分配的任务）
        task.setAssigneeId(form.getAssigneeId());
        task.setAssignedBy(form.getCreatedBy());
        task.setPlannedStartDate(form.getPlannedStartDate());
        task.setPlannedEndDate(form.getPlannedEndDate());
        task.setTestPlan(isBlank(form.getTestPlan()) ? null : form.getTestPlan().trim());
        task.setTestCaseDesign(isBlank(form.getTestCaseDesign())
                ? null : form.getTestCaseDesign().trim());
        task.setSeverity(isBlank(form.getSeverity()) ? null : form.getSeverity().trim());
        // affected_scope / root_cause：原调用点传 null，实体保持 null

        int n = new TaskMapper().insert(task);
        return n;
    }

    
    private int getAssignTask(Task form, Integer assigneeId) throws Exception {
        Task task = new Task();
        task.setTaskId(form.getTaskId());
        task.setAssigneeId(assigneeId);
        task.setAssignedBy(form.getAssignedBy());
        task.setPlannedEndDate(form.getPlannedEndDate());

        int rows = new TaskMapper().assignTask(task);
        return rows;
    }

    
    private Task getTaskById(Task task) throws Exception {
        Task currentTask = new TaskMapper().selectAssignTarget(task);
        return currentTask;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().length() == 0;
    }
}
