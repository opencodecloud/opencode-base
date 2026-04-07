package cloud.opencode.base.lunar;

import cloud.opencode.base.lunar.internal.LunarCalculator;
import cloud.opencode.base.lunar.internal.LunarData;

/**
 * Lunar Month information record
 * 农历月信息记录
 *
 * <p>Immutable record representing a specific month in the Chinese lunar calendar,
 * including days count, big/small month classification, naming, and navigation.</p>
 * <p>表示中国农历特定月份的不可变记录，包含天数、大小月分类、命名和导航功能。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Month day count (29 or 30) - 月份天数（29或30）</li>
 *   <li>Big month / small month classification - 大月/小月分类</li>
 *   <li>Chinese month naming (e.g. "正月", "闰四月") - 中文月份命名</li>
 *   <li>First / last day access - 首日/末日访问</li>
 *   <li>Previous / next month navigation - 上月/下月导航</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * LunarMonth m = LunarMonth.of(2024, 1);
 * int days = m.getDays();           // 29 or 30
 * boolean big = m.isBig();          // 大月
 * String name = m.getName();        // "正月"
 * LunarDate first = m.getFirstDay();
 * LunarDate last  = m.getLastDay();
 * LunarMonth next = m.next();
 * LunarMonth prev = m.previous();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: N/A (primitive fields) - 空值安全: 不适用（原始类型字段）</li>
 * </ul>
 *
 * @param year the lunar year | 农历年
 * @param month the lunar month (1-12) | 农历月（1-12）
 * @param isLeapMonth whether this is a leap month | 是否闰月
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.3
 */
public record LunarMonth(int year, int month, boolean isLeapMonth) {

    /**
     * Month names | 月份名
     */
    private static final String[] MONTH_NAMES = {
        "正", "二", "三", "四", "五", "六", "七", "八", "九", "十", "冬", "腊"
    };

    /**
     * Compact constructor — validates year range, month, and leap month consistency
     * 紧凑构造函数 — 验证年份范围、月份和闰月一致性
     */
    public LunarMonth {
        if (!LunarData.isSupported(year)) {
            throw new IllegalArgumentException(
                    "Year must be between " + LunarData.MIN_YEAR
                            + " and " + LunarData.MAX_YEAR + ", got: " + year);
        }
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException(
                    "Month must be between 1 and 12, got: " + month);
        }
        if (isLeapMonth) {
            int leapMonth = LunarData.getLeapMonth(year);
            if (leapMonth != month) {
                throw new IllegalArgumentException(
                        "Year " + year + " has no leap month " + month);
            }
        }
    }

    /**
     * Create a non-leap month
     * 创建非闰月
     *
     * @param year the lunar year | 农历年
     * @param month the lunar month (1-12) | 农历月
     * @return the LunarMonth | 农历月
     */
    public static LunarMonth of(int year, int month) {
        return new LunarMonth(year, month, false);
    }

    /**
     * Create a month with explicit leap flag
     * 创建指定闰月标志的月份
     *
     * @param year the lunar year | 农历年
     * @param month the lunar month (1-12) | 农历月
     * @param isLeap whether leap month | 是否闰月
     * @return the LunarMonth | 农历月
     */
    public static LunarMonth of(int year, int month, boolean isLeap) {
        return new LunarMonth(year, month, isLeap);
    }

    /**
     * Get the number of days in this month (29 or 30)
     * 获取此月天数（29或30）
     *
     * @return days in this month | 此月天数
     */
    public int getDays() {
        return LunarCalculator.getMonthDays(year, month, isLeapMonth);
    }

    /**
     * Check if this is a big month (30 days, 大月)
     * 检查是否为大月（30天）
     *
     * @return true if 30 days | 如果30天返回true
     */
    public boolean isBig() {
        return getDays() == 30;
    }

    /**
     * Check if this is a small month (29 days, 小月)
     * 检查是否为小月（29天）
     *
     * @return true if 29 days | 如果29天返回true
     */
    public boolean isSmall() {
        return getDays() == 29;
    }

    /**
     * Get the Chinese name of this month (e.g. "正月", "闰四月")
     * 获取此月的中文名称（如"正月"、"闰四月"）
     *
     * @return the month name | 月份名称
     */
    public String getName() {
        return (isLeapMonth ? "闰" : "") + MONTH_NAMES[month - 1] + "月";
    }

    /**
     * Get the first day of this month
     * 获取此月的第一天
     *
     * @return the first day | 第一天
     */
    public LunarDate getFirstDay() {
        return LunarDate.of(year, month, 1, isLeapMonth);
    }

    /**
     * Get the last day of this month
     * 获取此月的最后一天
     *
     * @return the last day | 最后一天
     */
    public LunarDate getLastDay() {
        return LunarDate.of(year, month, getDays(), isLeapMonth);
    }

    /**
     * Get the next month, handling leap month and year boundary
     * 获取下一个月，处理闰月和跨年
     *
     * <p>If this is a regular month and the year has a leap month equal to this month,
     * the next month is the leap version. Otherwise, it advances to the next regular month,
     * wrapping to month 1 of the next year after month 12.</p>
     * <p>如果这是常规月且该年闰月等于此月，则下一月为闰月。
     * 否则进入下一个常规月，12月之后进入下一年的正月。</p>
     *
     * @return the next month | 下一个月
     * @throws IllegalArgumentException if the next year is out of range | 如果下一年超出范围
     */
    public LunarMonth next() {
        int leapMonth = LunarData.getLeapMonth(year);

        // If this is a regular month and this year has a leap month equal to this month,
        // the next is the leap version
        if (!isLeapMonth && leapMonth == month) {
            return new LunarMonth(year, month, true);
        }

        // Otherwise advance to next regular month
        if (month < 12) {
            return new LunarMonth(year, month + 1, false);
        }

        // Wrap to next year
        return new LunarMonth(year + 1, 1, false);
    }

    /**
     * Get the previous month, handling leap month and year boundary
     * 获取上一个月，处理闰月和跨年
     *
     * <p>If this is a leap month, the previous is the regular version of the same month.
     * If this is month 1 (regular), it goes to month 12 of the previous year.
     * Otherwise, if the previous regular month has a leap version, that leap month
     * is returned; otherwise the regular previous month.</p>
     * <p>如果是闰月，则上一月为同月的常规版本。
     * 如果是正月（非闰），则为上一年腊月。
     * 否则，如果上一月有闰月则返回闰月，否则返回常规上一月。</p>
     *
     * @return the previous month | 上一个月
     * @throws IllegalArgumentException if the previous year is out of range | 如果上一年超出范围
     */
    public LunarMonth previous() {
        // If this is a leap month, previous is the regular version
        if (isLeapMonth) {
            return new LunarMonth(year, month, false);
        }

        // If month 1, go to previous year's month 12 (or its leap if exists)
        if (month == 1) {
            int prevYear = year - 1;
            if (!LunarData.isSupported(prevYear)) {
                throw new IllegalArgumentException(
                        "Cannot navigate before minimum supported year: " + LunarData.MIN_YEAR);
            }
            int prevLeap = LunarData.getLeapMonth(prevYear);
            if (prevLeap == 12) {
                return new LunarMonth(prevYear, 12, true);
            }
            return new LunarMonth(prevYear, 12, false);
        }

        // Check if the previous month has a leap version
        int leapMonth = LunarData.getLeapMonth(year);
        if (leapMonth == month - 1) {
            return new LunarMonth(year, month - 1, true);
        }

        return new LunarMonth(year, month - 1, false);
    }

    @Override
    public String toString() {
        return getName();
    }
}
