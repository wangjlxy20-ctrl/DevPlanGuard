package job.screen.dq100;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;

import biz.grandsight.ex.ui.ac.ShiroRoleManager;
import biz.grandsight.ex.ui.ac.ShiroUserRole;
import biz.grandsight.ex.ui.ac.util.SessionHelper;

import job.entity.UserInfo;
import job.mapper.user.UserMapper;

import com.yse.gb.gf.bld.front.BLDCommandImpl;
import com.yse.gb.gf.front.CommandException;
import com.yse.gb.gf.front.CommandHelper;
import com.yse.gb.gf.front.KeyObj;
import com.yse.gb.gf.front.UserMessage;

import jakarta.enterprise.context.RequestScoped;

/**
 * DQ100（DevQuality Login）的登录命令。
 *
 * <p>认证走框架 Shiro（shiro.ini 里的 UserJdbcRealm 查 AC 库 users 表，
 * 密码为 SHA-256 加盐格式），成功后按角色分流：</p>
 * <ul>
 *   <li>LEADER → DPGL100（项目列表）</li>
 *   <li>ADMIN  → ACM000（管理菜单）</li>
 *   <li>其他   → DPGL200（我的项目）</li>
 * </ul>
 */
@RequestScoped
public class COMMAND_BTNCMD_LOGIN extends BLDCommandImpl {

    private static final String SCREEN_LEADER = "DPGL100";
    private static final String SCREEN_MEMBER = "DPGL200";
    private static final String SCREEN_ADMIN = "ACM000";


    @Override
    protected void perform(final CommandHelper helper, final String nextScreenId, final KeyObj keyObj)
            throws CommandException, Throwable {

        String username = helper.getParam("NMLTXT_USERNAME");
        String password = helper.getParam("NMLTXT_PASSWORD");

        if (isBlank(username) || isBlank(password)) {
            backToLogin(helper, "USERNAME or PASSWORD can't be empty.", username);
            return;
        }

//        String lang = helper.getParam("GRANDSIGHT_EX_LANG");
//        if (isBlank(lang)) {
//            lang = "en";
//        }
//        helper.getSession().setAttribute("GRANDSIGHT_EX_LANG", lang);

        Subject subject = SecurityUtils.getSubject();
        try {
            // 不调用 subject.logout()：logout 会让当前会话失效，之后再写会话属性会抛
            // IllegalStateException: Session is invalid。subject.login() 会自行替换身份。

            UsernamePasswordToken token = new UsernamePasswordToken(username.trim(), password);
            subject.login(token);          // Shiro 认证：查 AC 库 users 表 + 密码哈希校验
            token.clear();

            // 业务身份：框架账号 id（AC users.user_id，如 "2"）= 业务 user_info.user_id
            ShiroUserRole role = ShiroRoleManager.getUserRole(username.trim());
            SessionHelper.setShiroUserRole(helper, role);

            UserInfo bizUser = null;
            try {
                Integer frameworkUserId = parseId((role == null) ? username.trim() : role.getUserId());
                if (frameworkUserId != null) {
                    UserMapper userMapper = new UserMapper();
                    bizUser = userMapper.findById(frameworkUserId);

                    // AC 里新建的账号：业务库没有档案时自动补一条（user_id 沿用 AC 的编号）
                    if (bizUser == null) {
                        String displayName = (role == null || isBlank(role.getUserName()))
                                ? username.trim() : role.getUserName();
                        String permission = mapRoleToPermission(role);
                        userMapper.addUserWithId(frameworkUserId, displayName, permission);
                        bizUser = userMapper.findById(frameworkUserId);
                        System.out.println(">>>DQ100 LOGIN auto-provisioned user_info: id=" + frameworkUserId
                                + " name=" + displayName + " permission=" + permission);
                    } else {
                        // 已有档案：AC 的显示名/角色变了就同步过来（AC 是唯一权威）
                        String displayName = (role == null || isBlank(role.getUserName()))
                                ? bizUser.getUserName() : role.getUserName();
                        String permission = mapRoleToPermission(role);
                        if (!equalSafe(displayName, bizUser.getUserName())
                                || !equalSafe(permission, bizUser.getPermission())) {
                            userMapper.syncFromAc(frameworkUserId, displayName, permission);
                            bizUser = userMapper.findById(frameworkUserId);
                            System.out.println(">>>DQ100 LOGIN synced from AC: id=" + frameworkUserId
                                    + " name=" + displayName + " permission=" + permission);
                        }
                    }
                }
            } catch (Exception ignore) {
                System.out.println(">>>DQ100 LOGIN user_info lookup failed: " + ignore.getMessage());
            }

            String businessRole = (bizUser == null) ? null : bizUser.getPermission();   // LEADER / MEMBER
            String roleIds = (role == null) ? null : role.getRoleIdsString();           // 框架角色（ADMIN 等）

            String target = decideScreen(businessRole,roleIds);

            System.out.println(">>>DQ100 LOGIN user=" + username
                    + " frameworkUserId=" + (role == null ? "null" : role.getUserId())
                    + " businessUserId=" + (bizUser == null ? "null" : bizUser.getUserId())
                    + " businessName=" + (bizUser == null ? "null" : bizUser.getUserName())
                    + " businessRole=" + businessRole
                    + " frameworkRoles=" + roleIds
                    + " -> " + target);

            KeyObj targetKeyObj = helper.getKeyObj(target);
            if (bizUser != null && bizUser.getUserId() != null) {
                targetKeyObj.put("USER_ID", String.valueOf(bizUser.getUserId()));
            }
            helper.pageOpen(target, targetKeyObj);

        } catch (AuthenticationException e) {
            System.out.println(">>>DQ100 LOGIN failed for " + username + " : " + e.getMessage());
            backToLogin(helper, "Incorrect USERNAME or PASSWORD.", username);
        }
    }

    private String decideScreen(final String businessRole, final String frameworkRoleIds) throws CommandException {
        if (hasRole(businessRole, "LEADER")) {
            return SCREEN_LEADER;
        }
        if (hasRole(businessRole, "MEMBER")) {
            return SCREEN_MEMBER;
        }
        if (hasRole(frameworkRoleIds, "ADMIN")) {
            return SCREEN_ADMIN;
        }
        if (hasRole(frameworkRoleIds, "LEADER")) {
            return SCREEN_LEADER;
        }
        throw new CommandException(new UserMessage("The ID doesn't exist, please contact the admin to register an account."));
    }

    private boolean hasRole(final String roleIds, final String role) {
        if (roleIds == null) {
            return false;
        }
        String[] tokens = roleIds.split("[,;|\\s]+");
        for (int i = 0; i < tokens.length; i++) {
            if (role.equalsIgnoreCase(tokens[i].trim())) {
                return true;
            }
        }
        return false;
    }

    private void backToLogin(final CommandHelper helper, final String message, final String username)
            throws Throwable {
        KeyObj back = helper.getKeyObj("DQ100");
        back.put("LOGIN_ERROR", message);
        back.put("LOGIN_USER", username == null ? "" : username);
        helper.pageOpen("DQ100", back);
    }

    private boolean equalSafe(final String a, final String b) {
        return (a == null) ? (b == null) : a.equals(b);
    }


    private String mapRoleToPermission(final ShiroUserRole role) {
        String roleIds = (role == null) ? null : role.getRoleIdsString();
        if (hasRole(roleIds, "ADMIN")) {
            return "ADMIN";
        }
        if (hasRole(roleIds, "LEADER")) {
            return "LEADER";
        }
        return "MEMBER";
    }

    private Integer parseId(final String s) {
        if (s == null || s.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean isBlank(final String s) {
        return s == null || s.trim().isEmpty();
    }
}
