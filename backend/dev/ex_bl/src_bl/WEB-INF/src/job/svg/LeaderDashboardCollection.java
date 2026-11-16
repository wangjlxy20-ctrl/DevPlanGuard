package job.svg;

import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.yse.gb.datacol.DataItem;
import com.yse.gb.datacol.DataItemList;
import com.yse.gb.gf.ezg.logic.EzgCustomCollection;
import com.yse.gb.gf.front.CommandHelper;

import jakarta.servlet.http.HttpSession;

import job.mapper.dashboard.LeaderDashboardMapper;

import javax.naming.NamingException;

/**
 * Leader 看板统一数据源类（datasource_id = LEADER_DASHBOARD）。
 *
 * 每个图形的隐藏 iframe 每 500ms 调用一次本类，每次只传该图形的受控元素。
 * 本类从 session 读当前 project_id，再按元素 id 填值。
 *
 * 解析方式：手工按最后一个下划线拆分元素 id 与变换类型（参照已验证的
 * SubmitTrendCollection），不依赖框架静态方法名。
 *
 * 元素 id 前缀：
 *   sbar/sval：任务状态分布（垂直柱）
 *   dbar/dval：纳期分布（垂直柱，dbar0 额外 color 红色）
 *   vbar/vval：缺陷严重度（水平柱，vbar0 额外 color 红色）
 *   wmlbl/wbar/wval：成员负载（水平柱，成员名动态）
 *   kval：KPI 数字卡片（text + color + blink）
 */
public class LeaderDashboardCollection extends EzgCustomCollection {

    /** 本类自持 session key，不再依赖 DPGL101Creator 的常量。 */
    private static final String SESSION_PROJECT_ID = "DASH_PROJECT_ID";

    private static final String[] STATUS_ORDER = {
            "TODO", "IN_PROGRESS", "SUBMITTED", "COMPLETED" };

    private static final String[] DUE_ORDER = {
            "已逾期", "7天内", "30天内", "30天以上", "未排期" };

    private static final String[] SEVERITY_ORDER = {
            "CRITICAL", "HIGH", "MEDIUM", "LOW", "未分级" };

    /** 成员负载最多显示行数（与 SVG 预留行数一致）。 */
    private static final int MAX_WORKLOAD_ROWS = 8;

    // KPI 颜色（框架 setProperty 需要 "rgb(r,g,b)"）
    private static final String GREEN = "rgb(89,161,79)";
    private static final String YELLOW = "rgb(237,201,72)";
    private static final String ORANGE = "rgb(242,142,43)";
    private static final String DANGER = "rgb(225,87,89)";

    /** 闪烁间隔（秒）；0 表示不闪烁。 */
    private static final String BLINK_FAST = "0.5";
    private static final String BLINK_OFF = "0";

    /** KPI 指标 key 固定顺序，与 selectKeyMetrics 的别名一致。 */
    private static final String[] KPI_KEYS = {
            "pending_review", "unassigned", "overdue", "open_defects" };

    /** 已逾期/严重柱子的红色（框架 setProperty 需要 "rgb(r,g,b)"）。 */
    private static final String RED_RGB = "rgb(225,87,89)";

    /**
     * 延迟初始化：不在字段加载时 new（避免框架实例化数据源类的时机 JNDI 不可用），
     * 第一次真正取数时（请求线程内）才创建，参照 SubmitTrendCollection 的做法。
     */
    private LeaderDashboardMapper mapper;

    private LeaderDashboardMapper mapper() throws NamingException {
        if (mapper == null) {
            mapper = new LeaderDashboardMapper();
        }
        return mapper;
    }

    @Override
    public void ezgReadData(final DataItemList list, final Hashtable params,
                            final CommandHelper helper) {

        if (list == null) {
            return;
        }

        Integer projectId = resolveProjectId(helper);
        System.out.println(">>>LDASH projectId=" + projectId + " listSize=" + list.size());
        if (projectId == null) {
            System.out.println(">>>LDASH no project_id in session, skip this round");
            return;
        }

        Map<String, Integer> statusMap = loadStatusMap(projectId);
        Map<String, Integer> dueMap = loadDueMap(projectId);
        Map<String, Integer> severityMap = loadSeverityMap(projectId);
        List<Map<String, Object>> workloadRows = loadWorkloadRows(projectId);
        Map<String, Object> keyMetrics = loadKeyMetrics(projectId);

        for (int i = 0; i < list.size(); i++) {
            DataItem item = list.get(i);
            if (item == null) {
                continue;
            }

            // 手工解析（参照已验证可用的 SubmitTrendCollection）：
            // 框架 id 形如 sbar0_level，最后一个下划线分隔“元素 id”和“变换类型”。
            // 这里直接复刻框架 lastIndexOf 的逻辑，不依赖 getGBObjID 方法名。
            String rawId = item.get_dataItemId();
            String objId;
            String kind;
            int under = rawId.lastIndexOf("_");
            if (under >= 0) {
                objId = rawId.substring(0, under);
                kind = rawId.substring(under + 1);
            } else {
                objId = rawId;
                kind = "";
            }
            System.out.println(">>>LDASH item raw=" + rawId
                    + " | obj=" + objId + " | kind=" + kind);

            String value = resolve(objId, kind, statusMap, dueMap, severityMap,
                    workloadRows, keyMetrics);
            if (value != null) {
                item.set_dataValue(value);
            } else {
                System.out.println(">>>LDASH no value for obj=" + objId + " kind=" + kind);
            }
        }
    }

    private String resolve(final String objId, final String kind,
                           final Map<String, Integer> statusMap,
                           final Map<String, Integer> dueMap,
                           final Map<String, Integer> severityMap,
                           final List<Map<String, Object>> workloadRows,
                           final Map<String, Object> keyMetrics) {
        if (objId == null) {
            return null;
        }
        String id = objId.trim();

        // 任务状态分布
        if (id.startsWith("sbar") || id.startsWith("sval")) {
            int idx = trailingIndex(id);
            if (idx < 0 || idx >= STATUS_ORDER.length) {
                return null;
            }
            return String.valueOf(safeInt(statusMap.get(STATUS_ORDER[idx])));
        }

        // 纳期分布
        if (id.startsWith("dbar") || id.startsWith("dval")) {
            int idx = trailingIndex(id);
            if (idx < 0 || idx >= DUE_ORDER.length) {
                return null;
            }
            if ("color".equals(kind)) {
                return idx == 0 ? RED_RGB : null;
            }
            return String.valueOf(safeInt(dueMap.get(DUE_ORDER[idx])));
        }

        // 缺陷严重度（水平柱）
        if (id.startsWith("vbar") || id.startsWith("vval")) {
            int idx = trailingIndex(id);
            if (idx < 0 || idx >= SEVERITY_ORDER.length) {
                return null;
            }
            if ("color".equals(kind)) {
                return idx == 0 ? RED_RGB : null;
            }
            return String.valueOf(safeInt(severityMap.get(SEVERITY_ORDER[idx])));
        }

        // 成员负载（水平柱，成员数不固定）
        if (id.startsWith("wmlbl") || id.startsWith("wbar") || id.startsWith("wval")) {
            int idx = trailingIndex(id);
            if (idx < 0 || idx >= MAX_WORKLOAD_ROWS) {
                return null;
            }
            Map<String, Object> row = (idx < workloadRows.size()) ? workloadRows.get(idx) : null;
            if (id.startsWith("wmlbl")) {
                // 成员名，没有数据时显示 "-"
                return (row == null || row.get("username") == null)
                        ? "-" : String.valueOf(row.get("username"));
            }
            int cnt = (row == null || row.get("cnt") == null)
                    ? 0 : ((Number) row.get("cnt")).intValue();
            return String.valueOf(cnt);
        }

        // KPI 数字卡片（text 数字 / color 颜色 / blink 闪烁）
        if (id.startsWith("kval")) {
            int idx = trailingIndex(id);
            if (idx < 0 || idx >= KPI_KEYS.length) {
                return null;
            }
            int value = metricInt(keyMetrics, KPI_KEYS[idx]);
            if ("color".equals(kind)) {
                return kpiColor(idx, value);
            }
            if ("blink".equals(kind)) {
                return kpiBlink(idx, value);
            }
            // text：数字本身
            return String.valueOf(value);
        }

        return null;
    }

    /** 提取 id 末尾的连续数字作为下标；末尾不是数字时返回 -1。 */
    private int trailingIndex(final String s) {
        if (s == null || s.isEmpty()) {
            return -1;
        }
        int i = s.length() - 1;
        while (i >= 0 && Character.isDigit(s.charAt(i))) {
            i--;
        }
        if (i == s.length() - 1) {
            return -1; // 末尾没有数字
        }
        try {
            return Integer.parseInt(s.substring(i + 1));
        } catch (Exception e) {
            return -1;
        }
    }

    /** 各指标的颜色阈值。 */
    private String kpiColor(final int idx, final int value) {
        switch (idx) {
            case 0: // 待审批：0 绿，1~5 黄，>5 红
                return value == 0 ? GREEN : (value <= 5 ? YELLOW : DANGER);
            case 1: // 待分配：0 绿，1~3 黄，>3 红
                return value == 0 ? GREEN : (value <= 3 ? YELLOW : DANGER);
            case 2: // 逾期：0 绿，>0 红
                return value == 0 ? GREEN : DANGER;
            default: // 未关闭缺陷：0 绿，1~3 橙，>3 红
                return value == 0 ? GREEN : (value <= 3 ? ORANGE : DANGER);
        }
    }

    /** 各指标达到危险阈值才闪烁。 */
    private String kpiBlink(final int idx, final int value) {
        boolean danger;
        switch (idx) {
            case 0: danger = value > 5; break;
            case 1: danger = value > 3; break;
            case 2: danger = value > 0; break;
            default: danger = value > 3; break;
        }
        return danger ? BLINK_FAST : BLINK_OFF;
    }

    private Map<String, Integer> loadStatusMap(final Integer projectId) {
        Map<String, Integer> result = new LinkedHashMap<>();
        for (String s : STATUS_ORDER) {
            result.put(s, 0);
        }
        try {
            List<Map<String, Object>> rows = mapper().selectStatusDistribution(projectId);
            if (rows != null) {
                for (Map<String, Object> row : rows) {
                    Object label = row.get("label");
                    Object cnt = row.get("cnt");
                    if (label != null && cnt != null) {
                        result.put(String.valueOf(label), ((Number) cnt).intValue());
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(">>>LDASH status read failed: " + e);
        }
        return result;
    }

    private Map<String, Integer> loadDueMap(final Integer projectId) {
        Map<String, Integer> result = new LinkedHashMap<>();
        for (String s : DUE_ORDER) {
            result.put(s, 0);
        }
        try {
            List<Map<String, Object>> rows = mapper().selectDueDistribution(projectId);
            if (rows != null) {
                for (Map<String, Object> row : rows) {
                    Object label = row.get("label");
                    Object cnt = row.get("cnt");
                    if (label != null && cnt != null) {
                        result.put(String.valueOf(label), ((Number) cnt).intValue());
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(">>>LDASH due read failed: " + e);
        }
        return result;
    }

    private Map<String, Integer> loadSeverityMap(final Integer projectId) {
        Map<String, Integer> result = new LinkedHashMap<>();
        for (String s : SEVERITY_ORDER) {
            result.put(s, 0);
        }
        try {
            List<Map<String, Object>> rows = mapper().selectDefectSeverity(projectId);
            if (rows != null) {
                for (Map<String, Object> row : rows) {
                    Object label = row.get("label");
                    Object cnt = row.get("cnt");
                    if (label != null && cnt != null) {
                        result.put(String.valueOf(label), ((Number) cnt).intValue());
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(">>>LDASH severity read failed: " + e);
        }
        return result;
    }

    private List<Map<String, Object>> loadWorkloadRows(final Integer projectId) {
        try {
            List<Map<String, Object>> rows = mapper().selectMemberWorkload(projectId);
            if (rows != null && rows.size() > MAX_WORKLOAD_ROWS) {
                return rows.subList(0, MAX_WORKLOAD_ROWS);
            }
            return rows;
        } catch (Exception e) {
            System.out.println(">>>LDASH workload read failed: " + e);
            return java.util.Collections.emptyList();
        }
    }

    private Map<String, Object> loadKeyMetrics(final Integer projectId) {
        try {
            Map<String, Object> row = mapper().selectKeyMetrics(projectId);
            System.out.println(">>>LDASH keyMetrics projectId=" + projectId + " row=" + row);
            if (row != null) {
                return row;
            }
        } catch (Exception e) {
            System.out.println(">>>LDASH key metrics read failed: " + e);
        }
        return java.util.Collections.emptyMap();
    }

    private int metricInt(final Map<String, Object> metrics, final String key) {
        Object v = metrics.get(key);
        if (v == null) {
            return 0;
        }
        if (v instanceof Number) {
            return ((Number) v).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(v).trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private Integer resolveProjectId(final CommandHelper helper) {
        try {
            HttpSession session = helper.getSession();
            if (session != null) {
                Object pid = session.getAttribute(SESSION_PROJECT_ID);
                if (pid != null) {
                    return Integer.valueOf(String.valueOf(pid).trim());
                }
            }
        } catch (Exception e) {
            System.out.println(">>>LDASH project_id read failed: " + e);
        }
        return null;
    }

    private int safeInt(final Integer v) {
        return v == null ? 0 : v;
    }
}
