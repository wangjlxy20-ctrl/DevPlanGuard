package job.svg;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;

import com.yse.gb.datacol.DataItem;
import com.yse.gb.datacol.DataItemList;
import com.yse.gb.gf.ezg.logic.EzgCustomCollection;
import com.yse.gb.gf.front.CommandHelper;

import job.databaseSource.connectDS;

public class SubmitTrendCollection extends EzgCustomCollection {

    private static final int DAYS = 7;

    @Override
    public void ezgReadData(final DataItemList list, final Hashtable params, final CommandHelper helper) {

        int[] counts = new int[DAYS];
        try {
            Integer userId = currentUserId();
            if (userId != null) {
                counts = queryDailyCounts(userId);
            }
        } catch (Exception e) {
            System.out.println(">>>SUBMIT_TREND read failed: " + e.getMessage());
        }

        // ===== 新增：近 7 天每天的日期标签，随当前日期动态滚动 =====
        String[] dateLabels = buildDateLabels();

        int total = 0;
        for (int i = 0; i < DAYS; i++) {
            total += counts[i];
        }
        System.out.println(">>>SUBMIT_TREND counts=" + java.util.Arrays.toString(counts) + " total=" + total);

        if (list == null) {
            System.out.println(">>>SUBMIT_TREND list is NULL");
            return;
        }
        StringBuilder dbg = new StringBuilder(">>>SUBMIT_TREND list ids: ");
        for (int i = 0; i < list.size(); i++) {
            dbg.append(list.get(i) == null ? "null" : list.get(i).get_dataItemId()).append(" | ");
        }
        System.out.println(dbg.toString());
        for (int i = 0; i < list.size(); i++) {
            DataItem item = list.get(i);
            if (item == null) {
                continue;
            }
            // 修改：多传入 dateLabels
            String value = resolve(item.get_dataItemId(), counts, total, dateLabels);
            if (value != null) {
                item.set_dataValue(value);
            }
        }
    }

    /** 把"元素 id + 种类"换算成数值：barNlevel / valNtext / total_valuetext / dateNtext。 */
    private String resolve(final String dataItemId, final int[] counts, final int total,
                           final String[] dateLabels) {
        if (dataItemId == null) {
            return null;
        }
        String id = dataItemId.trim();
        // 框架的 id 形如 bar0_level / val0_text / total_value_text
        if (id.endsWith("_level")) {
            id = id.substring(0, id.length() - "_level".length());
        } else if (id.endsWith("_text")) {
            id = id.substring(0, id.length() - "_text".length());
        } else if (id.endsWith("level")) {
            id = id.substring(0, id.length() - "level".length());
        } else if (id.endsWith("text")) {
            id = id.substring(0, id.length() - "text".length());
        }
        while (id.endsWith("_")) {
            id = id.substring(0, id.length() - 1);
        }

        if ("total_value".equals(id) || "total".equals(id)) {
            return String.valueOf(total);
        }
        if (id.startsWith("bar") || id.startsWith("val")) {
            String idxStr = id.substring(3);
            try {
                int idx = Integer.parseInt(idxStr);
                if (idx >= 0 && idx < DAYS) {
                    return String.valueOf(counts[idx]);
                }
            } catch (NumberFormatException ignore) {
                return null;
            }
        }

        // ===== 日期元素 day0 ~ day6，返回对应那天的日期 =====
        if (id.startsWith("day")) {
            String idxStr = id.substring(3);
            try {
                int idx = Integer.parseInt(idxStr);
                if (idx >= 0 && idx < DAYS) {
                    return dateLabels[idx];
                }
            } catch (NumberFormatException ignore) {
                return null;
            }
        }
        return null;
    }

    /**
     * 新增：生成近 7 天每天的日期标签。
     * 原理：先回到 7 天前的 0 点，再逐天 +1。
     * 因为基准是"今天"，所以整个窗口每天自动滚动，日期不是写死的。
     */
    private String[] buildDateLabels() {
        String[] labels = new String[DAYS];

        // 想改日期格式就改这里：MM/dd、M/d、yyyy-MM-dd 都可以
        SimpleDateFormat df = new SimpleDateFormat("MM/dd");

        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        // 回到 7 天前，与 queryDailyCounts 的 from 对齐
        c.add(Calendar.DAY_OF_MONTH, -(DAYS - 1));

        for (int i = 0; i < DAYS; i++) {
            labels[i] = df.format(c.getTime());
            c.add(Calendar.DAY_OF_MONTH, 1);
        }
        return labels;
    }

    private Integer currentUserId() {
        try {
            Object principal = org.apache.shiro.SecurityUtils.getSubject().getPrincipal();
            if (principal != null) {
                String s = String.valueOf(principal).trim();
                if (!s.isEmpty() && !"null".equalsIgnoreCase(s)) {
                    return Integer.valueOf(s);
                }
            }
        } catch (Exception ignore) {
            // 取不到就按 0 处理
        }
        return null;
    }

    private int[] queryDailyCounts(final Integer userId) throws Exception {
        int[] counts = new int[DAYS];
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        Date from = new Date(today.getTimeInMillis() - (DAYS - 1L) * 24L * 60L * 60L * 1000L);

        QueryRunner qr = connectDS.getQueryRunner();
        String sql = "SELECT DATE_FORMAT(t.submitted_at, '%Y-%m-%d') AS d, COUNT(*) AS c "
                + "FROM task t "
                + "WHERE t.submitted_by = ? AND t.submitted_at >= ? "
                + "GROUP BY DATE_FORMAT(t.submitted_at, '%Y-%m-%d')";

        List<Map<String, Object>> rows = qr.query(sql, new MapListHandler(), userId, from);
        if (rows != null) {
            for (Map<String, Object> row : rows) {
                Object dayObj = row.get("d");
                Object cntObj = row.get("c");
                if (dayObj == null || cntObj == null) {
                    continue;
                }
                int idx = dayIndex(String.valueOf(dayObj), from, df);
                if (idx >= 0 && idx < DAYS) {
                    counts[idx] = ((Number) cntObj).intValue();
                }
            }
        }
        return counts;
    }

    private int dayIndex(final String day, final Date from, final SimpleDateFormat df) {
        try {
            Date d = df.parse(day);
            long diff = d.getTime() - from.getTime();
            int idx = (int) Math.round(diff / (24.0 * 60 * 60 * 1000));
            if (idx < 0) {
                idx = 0;
            }
            if (idx > DAYS - 1) {
                idx = DAYS - 1;
            }
            return idx;
        } catch (Exception e) {
            return -1;
        }
    }
}
