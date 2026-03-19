package cloud.opencode.base.cron;

import java.util.Map;

/**
 * Cron Field Definition - Enum for Cron Expression Fields
 * Cron字段定义 - Cron表达式字段枚举
 *
 * <p>Defines the valid range, display name, and name aliases for each cron field.
 * Handles case-insensitive alias resolution (MON→1, JAN→1, etc.).</p>
 * <p>定义每个Cron字段的有效范围、显示名称和名称别名。
 * 处理不区分大小写的别名解析（MON→1、JAN→1等）。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Range validation per field - 每字段范围校验</li>
 *   <li>Month aliases: JAN-DEC → 1-12 - 月份别名</li>
 *   <li>Day-of-week aliases: SUN-SAT → 0-6 - 星期别名</li>
 *   <li>Case-insensitive alias resolution - 不区分大小写别名解析</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable enum) - 线程安全: 是（不可变枚举）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Access cron field properties
 * // 访问cron字段属性
 * CronField field = CronField.HOUR;
 * int min = field.min(); // 0
 * int max = field.max(); // 23
 * boolean valid = field.isValid(15); // true
 * }</pre>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cron V1.0.0
 */
public enum CronField {

    /**
     * Seconds field (0-59)
     * 秒字段（0-59）
     */
    SECOND("second", 0, 59, Map.of()),

    /**
     * Minutes field (0-59)
     * 分钟字段（0-59）
     */
    MINUTE("minute", 0, 59, Map.of()),

    /**
     * Hours field (0-23)
     * 小时字段（0-23）
     */
    HOUR("hour", 0, 23, Map.of()),

    /**
     * Day of month field (1-31)
     * 月中日字段（1-31）
     */
    DAY_OF_MONTH("day-of-month", 1, 31, Map.of()),

    /**
     * Month field (1-12)
     * 月份字段（1-12）
     */
    MONTH("month", 1, 12, Map.ofEntries(
            Map.entry("JAN", "1"), Map.entry("FEB", "2"), Map.entry("MAR", "3"),
            Map.entry("APR", "4"), Map.entry("MAY", "5"), Map.entry("JUN", "6"),
            Map.entry("JUL", "7"), Map.entry("AUG", "8"), Map.entry("SEP", "9"),
            Map.entry("OCT", "10"), Map.entry("NOV", "11"), Map.entry("DEC", "12")
    )),

    /**
     * Day of week field (0-6, SUN=0)
     * 星期字段（0-6，SUN=0）
     */
    DAY_OF_WEEK("day-of-week", 0, 6, Map.of(
            "SUN", "0", "MON", "1", "TUE", "2", "WED", "3",
            "THU", "4", "FRI", "5", "SAT", "6"
    ));

    private final String displayName;
    private final int min;
    private final int max;
    private final Map<String, String> aliases;

    CronField(String displayName, int min, int max, Map<String, String> aliases) {
        this.displayName = displayName;
        this.min = min;
        this.max = max;
        this.aliases = aliases;
    }

    /**
     * Gets the display name of this field
     * 获取此字段的显示名称
     *
     * @return the display name | 显示名称
     */
    public String displayName() {
        return displayName;
    }

    /**
     * Gets the minimum value
     * 获取最小值
     *
     * @return the minimum | 最小值
     */
    public int min() {
        return min;
    }

    /**
     * Gets the maximum value
     * 获取最大值
     *
     * @return the maximum | 最大值
     */
    public int max() {
        return max;
    }

    /**
     * Resolves name aliases in a field string (case-insensitive)
     * 解析字段字符串中的名称别名（不区分大小写）
     *
     * <p>Replaces aliases like MON, TUE, JAN, FEB with their numeric values.
     * Handles aliases within ranges and lists (e.g., "MON-FRI" becomes "1-5").</p>
     * <p>将 MON、TUE、JAN、FEB 等别名替换为数字值。
     * 处理范围和列表中的别名（例如 "MON-FRI" 变为 "1-5"）。</p>
     *
     * @param field the field string | 字段字符串
     * @return the resolved field string | 解析后的字段字符串
     */
    public String resolveAliases(String field) {
        if (aliases.isEmpty() || field == null) {
            return field;
        }
        String result = field.toUpperCase();
        for (Map.Entry<String, String> entry : aliases.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * Checks if the given value is within the valid range
     * 检查给定值是否在有效范围内
     *
     * @param value the value to check | 要检查的值
     * @return true if valid | 如果有效返回true
     */
    public boolean isInRange(int value) {
        return value >= min && value <= max;
    }
}
