package cloud.opencode.base.cron;

import java.time.DayOfWeek;

/**
 * Cron Builder - Fluent API for Building Cron Expressions
 * Cron构建器 - 构建Cron表达式的流式API
 *
 * <p>Type-safe builder for constructing valid cron expressions
 * with input validation and support for special characters (L, W, #).</p>
 * <p>用于构建有效Cron表达式的类型安全构建器，
 * 支持输入校验和特殊字符（L、W、#）。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fluent API for 5-field and 6-field cron expressions - 流式API支持5字段和6字段cron表达式</li>
 *   <li>Type-safe day-of-week via {@link DayOfWeek} enum - 通过 {@link DayOfWeek} 枚举实现类型安全的星期设置</li>
 *   <li>Special characters: L (last), W (weekday), # (nth occurrence) - 特殊字符：L（最后）、W（工作日）、#（第N个）</li>
 *   <li>Input validation on all setters - 所有设置器都有输入校验</li>
 *   <li>Convenience factories: everySeconds, everyMinutes, everyHours - 便捷工厂方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Every day at 10:30 | 每天10:30
 * CronBuilder.every().day().at(10, 30).buildExpression()    // "30 10 * * *"
 *
 * // Weekdays at 9:00 | 工作日9:00
 * CronBuilder.every().weekdays().at(9, 0).buildExpression() // "0 9 * * 1-5"
 *
 * // Every 5 seconds | 每5秒
 * CronBuilder.everySeconds(5).buildExpression()             // "0/5 * * * * *"
 *
 * // Last day of month at 18:00 | 每月最后一天18:00
 * CronBuilder.create().lastDayOfMonth().at(18, 0).buildExpression() // "0 18 L * *"
 *
 * // 3rd Friday of every month at 10:00 | 每月第三个周五10:00
 * CronBuilder.create().nthDayOfWeek(DayOfWeek.FRIDAY, 3).at(10, 0).buildExpression()
 *     // "0 10 * * 5#3"
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (not shared across threads) - 线程安全: 否（不跨线程共享）</li>
 *   <li>Null-safe: Yes (rejects null DayOfWeek) - 空值安全: 是</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) - each setter and buildExpression are constant time - 时间复杂度: O(1)，每个 setter 和 buildExpression 均为常量时间</li>
 *   <li>Space complexity: O(1) - stores at most 6 field strings - 空间复杂度: O(1) 最多存储 6 个字段字符串</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see CronExpression
 * @see OpenCron#builder()
 * @since JDK 25, opencode-base-cron V1.0.0
 */
public final class CronBuilder {

    private String second = "0";
    private String minute = "*";
    private String hour = "*";
    private String dayOfMonth = "*";
    private String month = "*";
    private String dayOfWeek = "*";
    private boolean hasSeconds = false;

    private CronBuilder() {
    }

    // ==================== Static Factory Methods | 静态工厂方法 ====================

    /**
     * Creates a new builder
     * 创建新构建器
     *
     * @return the builder | 构建器
     */
    public static CronBuilder create() {
        return new CronBuilder();
    }

    /**
     * Creates a new builder with fluent "every" semantics
     * 创建具有"每"语义的新构建器
     *
     * @return the builder | 构建器
     */
    public static CronBuilder every() {
        return new CronBuilder();
    }

    /**
     * Creates schedule that fires every N seconds (6-field cron)
     * 创建每N秒触发的调度（6字段cron）
     *
     * @param n the interval in seconds (1-59) | 秒间隔（1-59）
     * @return the builder | 构建器
     * @throws IllegalArgumentException if n is out of range | 如果n超出范围
     */
    public static CronBuilder everySeconds(int n) {
        if (n <= 0 || n > 59) {
            throw new IllegalArgumentException("Second interval must be 1-59, got: " + n);
        }
        CronBuilder builder = new CronBuilder();
        builder.second = "0/" + n;
        builder.hasSeconds = true;
        return builder;
    }

    /**
     * Creates schedule that fires every N minutes
     * 创建每N分钟触发的调度
     *
     * @param n the interval in minutes (1-59) | 分钟间隔（1-59）
     * @return the builder | 构建器
     * @throws IllegalArgumentException if n is out of range | 如果n超出范围
     */
    public static CronBuilder everyMinutes(int n) {
        if (n <= 0 || n > 59) {
            throw new IllegalArgumentException("Minute interval must be 1-59, got: " + n);
        }
        CronBuilder builder = new CronBuilder();
        builder.minute = "*/" + n;
        return builder;
    }

    /**
     * Creates schedule that fires every N hours
     * 创建每N小时触发的调度
     *
     * @param n the interval in hours (1-23) | 小时间隔（1-23）
     * @return the builder | 构建器
     * @throws IllegalArgumentException if n is out of range | 如果n超出范围
     */
    public static CronBuilder everyHours(int n) {
        if (n <= 0 || n > 23) {
            throw new IllegalArgumentException("Hour interval must be 1-23, got: " + n);
        }
        CronBuilder builder = new CronBuilder();
        builder.minute = "0";
        builder.hour = "*/" + n;
        return builder;
    }

    /**
     * Creates schedule that fires every N days at midnight
     * 创建每N天午夜触发的调度
     *
     * @param n the interval in days (1-31) | 天间隔（1-31）
     * @return the builder | 构建器
     * @throws IllegalArgumentException if n is out of range | 如果n超出范围
     */
    public static CronBuilder everyDays(int n) {
        if (n <= 0 || n > 31) {
            throw new IllegalArgumentException("Day interval must be 1-31, got: " + n);
        }
        CronBuilder builder = new CronBuilder();
        builder.minute = "0";
        builder.hour = "0";
        builder.dayOfMonth = n == 1 ? "*" : "*/" + n;
        return builder;
    }

    // ==================== Time Setters | 时间设置 ====================

    /**
     * Sets hour and minute for execution
     * 设置执行的小时和分钟
     *
     * @param hour   the hour (0-23) | 小时（0-23）
     * @param minute the minute (0-59) | 分钟（0-59）
     * @return this builder | 此构建器
     * @throws IllegalArgumentException if out of range | 如果超出范围
     */
    public CronBuilder at(int hour, int minute) {
        if (hour < 0 || hour > 23) throw new IllegalArgumentException("Hour must be 0-23, got: " + hour);
        if (minute < 0 || minute > 59) throw new IllegalArgumentException("Minute must be 0-59, got: " + minute);
        this.hour = String.valueOf(hour);
        this.minute = String.valueOf(minute);
        return this;
    }

    /**
     * Sets the second field (enables 6-field cron format)
     * 设置秒字段（启用6字段cron格式）
     *
     * @param s the second (0-59) | 秒（0-59）
     * @return this builder | 此构建器
     * @throws IllegalArgumentException if out of range | 如果超出范围
     */
    public CronBuilder second(int s) {
        if (s < 0 || s > 59) throw new IllegalArgumentException("Second must be 0-59, got: " + s);
        this.second = String.valueOf(s);
        this.hasSeconds = true;
        return this;
    }

    /**
     * Sets the minute field
     * 设置分钟字段
     *
     * @param m the minute (0-59) | 分钟（0-59）
     * @return this builder | 此构建器
     * @throws IllegalArgumentException if out of range | 如果超出范围
     */
    public CronBuilder minute(int m) {
        if (m < 0 || m > 59) throw new IllegalArgumentException("Minute must be 0-59, got: " + m);
        this.minute = String.valueOf(m);
        return this;
    }

    /**
     * Sets the hour field
     * 设置小时字段
     *
     * @param h the hour (0-23) | 小时（0-23）
     * @return this builder | 此构建器
     * @throws IllegalArgumentException if out of range | 如果超出范围
     */
    public CronBuilder hour(int h) {
        if (h < 0 || h > 23) throw new IllegalArgumentException("Hour must be 0-23, got: " + h);
        this.hour = String.valueOf(h);
        return this;
    }

    // ==================== Day Setters | 日期设置 ====================

    /**
     * Sets to every day (default, no-op)
     * 设置为每天（默认，无操作）
     *
     * @return this builder | 此构建器
     */
    public CronBuilder day() {
        return this;
    }

    /**
     * Sets the day-of-month field
     * 设置月中日字段
     *
     * @param d the day of month (1-31) | 月中日（1-31）
     * @return this builder | 此构建器
     * @throws IllegalArgumentException if out of range | 如果超出范围
     */
    public CronBuilder dayOfMonth(int d) {
        if (d < 1 || d > 31) throw new IllegalArgumentException("Day of month must be 1-31, got: " + d);
        this.dayOfMonth = String.valueOf(d);
        return this;
    }

    /**
     * Sets the month field
     * 设置月份字段
     *
     * @param m the month (1-12) | 月份（1-12）
     * @return this builder | 此构建器
     * @throws IllegalArgumentException if out of range | 如果超出范围
     */
    public CronBuilder month(int m) {
        if (m < 1 || m > 12) throw new IllegalArgumentException("Month must be 1-12, got: " + m);
        this.month = String.valueOf(m);
        return this;
    }

    /**
     * Sets to the last day of the month (L)
     * 设置为月最后一天（L）
     *
     * @return this builder | 此构建器
     */
    public CronBuilder lastDayOfMonth() {
        this.dayOfMonth = "L";
        return this;
    }

    /**
     * Sets to N days before the last day of month (L-N)
     * 设置为月最后一天前N天（L-N）
     *
     * @param offset the offset from last day (0-30) | 距最后一天的偏移（0-30）
     * @return this builder | 此构建器
     * @throws IllegalArgumentException if offset is out of range | 如果偏移超出范围
     */
    public CronBuilder lastDayOfMonth(int offset) {
        if (offset < 0 || offset > 30) throw new IllegalArgumentException("Offset must be 0-30, got: " + offset);
        this.dayOfMonth = offset == 0 ? "L" : "L-" + offset;
        return this;
    }

    /**
     * Sets to the nearest weekday to a specific day (nW)
     * 设置为最接近指定日的工作日（nW）
     *
     * @param day the target day of month (1-31) | 目标月中日（1-31）
     * @return this builder | 此构建器
     * @throws IllegalArgumentException if day is out of range | 如果日期超出范围
     */
    public CronBuilder nearestWeekday(int day) {
        if (day < 1 || day > 31) throw new IllegalArgumentException("Day must be 1-31, got: " + day);
        this.dayOfMonth = day + "W";
        return this;
    }

    /**
     * Sets to the last weekday of the month (LW)
     * 设置为月最后一个工作日（LW）
     *
     * @return this builder | 此构建器
     */
    public CronBuilder lastWeekdayOfMonth() {
        this.dayOfMonth = "LW";
        return this;
    }

    // ==================== Day of Week Setters | 星期设置 ====================

    /**
     * Sets to Monday
     * 设置为星期一
     *
     * @return this builder | 此构建器
     */
    public CronBuilder monday() { this.dayOfWeek = "1"; return this; }

    /**
     * Sets to Tuesday
     * 设置为星期二
     *
     * @return this builder | 此构建器
     */
    public CronBuilder tuesday() { this.dayOfWeek = "2"; return this; }

    /**
     * Sets to Wednesday
     * 设置为星期三
     *
     * @return this builder | 此构建器
     */
    public CronBuilder wednesday() { this.dayOfWeek = "3"; return this; }

    /**
     * Sets to Thursday
     * 设置为星期四
     *
     * @return this builder | 此构建器
     */
    public CronBuilder thursday() { this.dayOfWeek = "4"; return this; }

    /**
     * Sets to Friday
     * 设置为星期五
     *
     * @return this builder | 此构建器
     */
    public CronBuilder friday() { this.dayOfWeek = "5"; return this; }

    /**
     * Sets to Saturday
     * 设置为星期六
     *
     * @return this builder | 此构建器
     */
    public CronBuilder saturday() { this.dayOfWeek = "6"; return this; }

    /**
     * Sets to Sunday
     * 设置为星期日
     *
     * @return this builder | 此构建器
     */
    public CronBuilder sunday() { this.dayOfWeek = "0"; return this; }

    /**
     * Sets to weekdays (Monday-Friday)
     * 设置为工作日（周一到周五）
     *
     * @return this builder | 此构建器
     */
    public CronBuilder weekdays() {
        this.dayOfWeek = "1-5";
        return this;
    }

    /**
     * Sets to weekends (Saturday-Sunday)
     * 设置为周末（周六和周日）
     *
     * @return this builder | 此构建器
     */
    public CronBuilder weekends() {
        this.dayOfWeek = "0,6";
        return this;
    }

    /**
     * Sets the day of week from {@link DayOfWeek}
     * 从 {@link DayOfWeek} 设置星期几
     *
     * @param dow the day of week | 星期几
     * @return this builder | 此构建器
     */
    public CronBuilder dayOfWeek(DayOfWeek dow) {
        this.dayOfWeek = String.valueOf(dow.getValue() % 7);
        return this;
    }

    /**
     * Sets to the Nth occurrence of a day of week in the month (n#m)
     * 设置为月中第N个某星期几（n#m）
     *
     * @param dow the day of week | 星期几
     * @param nth the ordinal (1-5) | 序号（1-5）
     * @return this builder | 此构建器
     * @throws IllegalArgumentException if nth is out of range | 如果序号超出范围
     */
    public CronBuilder nthDayOfWeek(DayOfWeek dow, int nth) {
        if (nth < 1 || nth > 5) throw new IllegalArgumentException("nth must be 1-5, got: " + nth);
        this.dayOfWeek = (dow.getValue() % 7) + "#" + nth;
        return this;
    }

    /**
     * Sets to the last occurrence of a day of week in the month (nL)
     * 设置为月中最后一个某星期几（nL）
     *
     * @param dow the day of week | 星期几
     * @return this builder | 此构建器
     */
    public CronBuilder lastDayOfWeek(DayOfWeek dow) {
        this.dayOfWeek = (dow.getValue() % 7) + "L";
        return this;
    }

    // ==================== Day/Month Range | 日期/月份范围 ====================

    /**
     * Sets the day-of-month field to a range (e.g., 10-20)
     * 设置月中日字段为范围（如10-20）
     *
     * @param from the start day (1-31) | 起始日（1-31）
     * @param to   the end day (1-31) | 结束日（1-31）
     * @return this builder | 此构建器
     * @throws IllegalArgumentException if out of range | 如果超出范围
     */
    public CronBuilder dayOfMonthRange(int from, int to) {
        validateRange(from, 1, 31, "day of month");
        validateRange(to, 1, 31, "day of month");
        if (from > to) {
            throw new IllegalArgumentException("from must be <= to for day of month range, got: " + from + "-" + to);
        }
        this.dayOfMonth = from + "-" + to;
        return this;
    }

    /**
     * Sets the month field to a range (e.g., 3-9)
     * 设置月份字段为范围（如3-9）
     *
     * @param from the start month (1-12) | 起始月份（1-12）
     * @param to   the end month (1-12) | 结束月份（1-12）
     * @return this builder | 此构建器
     * @throws IllegalArgumentException if out of range | 如果超出范围
     */
    public CronBuilder monthRange(int from, int to) {
        validateRange(from, 1, 12, "month");
        validateRange(to, 1, 12, "month");
        if (from > to) {
            throw new IllegalArgumentException("from must be <= to for month range, got: " + from + "-" + to);
        }
        this.month = from + "-" + to;
        return this;
    }

    // ==================== Range/Step Helpers | 范围/步长辅助 ====================

    /**
     * Sets the second field to a range (enables 6-field cron)
     * 设置秒字段为范围（启用6字段cron）
     *
     * @param from the start (0-59) | 起始（0-59）
     * @param to   the end (0-59) | 结束（0-59）
     * @return this builder | 此构建器
     * @throws IllegalArgumentException if out of range | 如果超出范围
     */
    public CronBuilder secondRange(int from, int to) {
        validateRange(from, 0, 59, "second");
        validateRange(to, 0, 59, "second");
        this.second = from + "-" + to;
        this.hasSeconds = true;
        return this;
    }

    /**
     * Sets the minute field to a range
     * 设置分钟字段为范围
     *
     * @param from the start (0-59) | 起始（0-59）
     * @param to   the end (0-59) | 结束（0-59）
     * @return this builder | 此构建器
     * @throws IllegalArgumentException if out of range | 如果超出范围
     */
    public CronBuilder minuteRange(int from, int to) {
        validateRange(from, 0, 59, "minute");
        validateRange(to, 0, 59, "minute");
        this.minute = from + "-" + to;
        return this;
    }

    /**
     * Sets the hour field to a range
     * 设置小时字段为范围
     *
     * @param from the start (0-23) | 起始（0-23）
     * @param to   the end (0-23) | 结束（0-23）
     * @return this builder | 此构建器
     * @throws IllegalArgumentException if out of range | 如果超出范围
     */
    public CronBuilder hourRange(int from, int to) {
        validateRange(from, 0, 23, "hour");
        validateRange(to, 0, 23, "hour");
        this.hour = from + "-" + to;
        return this;
    }

    private static void validateRange(int value, int min, int max, String field) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(
                    field + " must be " + min + "-" + max + ", got: " + value);
        }
    }

    // ==================== Build Methods | 构建方法 ====================

    /**
     * Builds the cron expression string
     * 构建Cron表达式字符串
     *
     * @return the cron expression | Cron表达式
     */
    public String buildExpression() {
        if (hasSeconds) {
            return String.join(" ", second, minute, hour, dayOfMonth, month, dayOfWeek);
        }
        return String.join(" ", minute, hour, dayOfMonth, month, dayOfWeek);
    }

    /**
     * Builds and parses the cron expression into a {@link CronExpression}
     * 构建并解析Cron表达式为 {@link CronExpression}
     *
     * @return the parsed expression | 解析后的表达式
     */
    public CronExpression build() {
        return CronExpression.parse(buildExpression());
    }
}
