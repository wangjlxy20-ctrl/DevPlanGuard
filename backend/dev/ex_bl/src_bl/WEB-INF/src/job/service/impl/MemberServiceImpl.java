package job.service.impl;

import jakarta.enterprise.context.RequestScoped;

import job.entity.ProjectMember;
import job.entity.UserInfo;
import job.mapper.project.ProjectMemberMapper;
import job.mapper.user.UserMapper;
import job.service.MemberService;

import java.util.ArrayList;
import java.util.List;

@RequestScoped
public class MemberServiceImpl implements MemberService {
    @Override
    public List<ProjectMember> searchMembers(ProjectMember condition) throws Exception {
        return new ProjectMemberMapper().selectMembers(condition);
    }

    @Override
    public List<UserInfo> searchAddableUsers(ProjectMember condition, String keyword) throws Exception {
        UserInfo userCondition = new UserInfo();
        userCondition.setUserName(keyword);
        // mapper 的第二个参数是字符串形态的 project_id（其签名保持不变），由实体取 project_id 后转型
        Integer projectId = (condition == null) ? null : condition.getProjectId();
        return new UserMapper().searchUsers(userCondition, projectId == null ? null : String.valueOf(projectId));
    }

    @Override
    public boolean hasAnySelected(String[] selectedRows) throws Exception {
        boolean any = false;
        if (selectedRows != null) {
            for (String r : selectedRows) {
                if (r != null && r.trim().length() > 0) {
                    any = true;
                    break;
                }
            }
        }
        System.out.println("DPGL100MEMA_ADD, ANY_SELECTED = " + any);
        return any;
    }

    @Override
    public int addSelectedMembers(String projectId, String[] selectedRows, String[] roleRows) throws Exception {
        //选出 被多选框勾选的 成员;
        List<String> idList = new ArrayList<>();
        List<String> roleList = new ArrayList<>();
        if (selectedRows != null) {
            for (int i = 0; i < selectedRows.length; i++) {
                if (selectedRows[i] != null && selectedRows[i].trim().length() > 0) {
                    idList.add(selectedRows[i]);
                    //Match characters with the same line number i
                    String role = (roleRows != null && i < roleRows.length) ? roleRows[i] : null;
                    roleList.add(role == null ? "" : role);
                }
            }
        }
        System.out.println("MEMA selected ids = " + idList + ", roles = " + roleList);

        if (idList.isEmpty()) {
            return 0;
        }

        int added = addMembers(projectId, idList, roleList);
        System.out.println("MEMA ADDED MEMBER COUNT = " + added);
        return added;
    }

    @Override
    public List<ProjectMember> searchAssignableMembers(ProjectMember condition) throws Exception {
        List<ProjectMember> members = new ProjectMemberMapper().selectAssignableMembers(condition);
        return members;
    }

    
    private int addMembers(String projectId, List<String> userIds, List<String> roles) throws Exception {
        ProjectMember condition = new ProjectMember();
        condition.setProjectId(Integer.parseInt(projectId.trim()));
        return new ProjectMemberMapper().insertMembers(condition, userIds, roles);
    }
}
