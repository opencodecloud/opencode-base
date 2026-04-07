package cloud.opencode.base.lunar;

import cloud.opencode.base.lunar.internal.LunarCalculator;
import cloud.opencode.base.lunar.zodiac.Constellation;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Solar Date
 * 公历日期
 *
 * <p>Immutable record representing a solar (Gregorian) calendar date.</p>
 * <p>表示公历（格里高利历）日期的不可变记录。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Solar date representation - 公历日期表示</li>
 *   <li>Lunar date conversion - 农历日期转换</li>
 *   <li>Constellation lookup - 星座查询</li>
 *   <li>LocalDate interop - LocalDate互操作</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SolarDate solar = new SolarDate(2024, 2, 10);
 * LunarDate lunar = solar.toLunar();
 * Constellation constellation = solar.getConstellation();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: N/A (primitive fields) - 空值安全: 不适用（原始类型字段）</li>
 * </ul>
 *
 * @param year the year | 年
 * @param month the month (1-12) | 月（1-12）
 * @param day the day | 日
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.0
 */
public record SolarDate(int year, int month, int day) {

    public SolarDate {
        // Eagerly validate by constructing LocalDate
        LocalDate.of(year, month, day);
    }

    /**
     * Create solar date from year, month, and day
     * 通过年月日创建公历日期
     *
     * @param year the year | 年
     * @param month the month (1-12) | 月（1-12）
     * @param day the day | 日
     * @return the solar date | 公历日期
     * @since V1.0.3
     */
    public static SolarDate of(int year, int month, int day) {
        return new SolarDate(year, month, day);
    }

    /**
     * Create solar date from LocalDate
     * 从LocalDate创建公历日期
     *
     * @param date the LocalDate | LocalDate
     * @return the solar date | 公历日期
     */
    public static SolarDate of(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");
        return new SolarDate(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
    }

    /**
     * Create solar date for today
     * 创建今天的公历日期
     *
     * @return the solar date | 公历日期
     */
    public static SolarDate today() {
        return of(LocalDate.now());
    }

    /**
     * Convert to LocalDate
     * 转换为LocalDate
     *
     * @return the LocalDate | LocalDate
     */
    public LocalDate toLocalDate() {
        return LocalDate.of(year, month, day);
    }

    /**
     * Convert to lunar date
     * 转换为农历日期
     *
     * @return the lunar date | 农历日期
     */
    public LunarDate toLunar() {
        return LunarCalculator.solarToLunar(toLocalDate());
    }

    /**
     * Get constellation
     * 获取星座
     *
     * @return the constellation | 星座
     */
    public Constellation getConstellation() {
        return Constellation.of(month, day);
    }

    /**
     * Check if leap year
     * 是否为闰年
     *
     * @return true if leap year | 如果是闰年返回true
     */
    public boolean isLeapYear() {
        return toLocalDate().isLeapYear();
    }

    /**
     * Get day of week (1=Monday, 7=Sunday)
     * 获取星期几（1=周一，7=周日）
     *
     * @return the day of week | 星期几
     */
    public int getDayOfWeek() {
        return toLocalDate().getDayOfWeek().getValue();
    }

    /**
     * Get day of week name in Chinese
     * 获取中文星期名
     *
     * @return the Chinese day name | 中文星期名
     */
    public String getDayOfWeekName() {
        String[] names = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        return names[getDayOfWeek() - 1];
    }

    /**
     * Get day of year
     * 获取一年中的第几天
     *
     * @return the day of year | 一年中的第几天
     */
    public int getDayOfYear() {
        return toLocalDate().getDayOfYear();
    }

    /**
     * Add days
     * 加天数
     *
     * @param days the days to add | 要加的天数
     * @return the new solar date | 新公历日期
     */
    public SolarDate plusDays(int days) {
        return of(toLocalDate().plusDays(days));
    }

    /**
     * Subtract days
     * 减天数
     *
     * @param days the days to subtract | 要减的天数
     * @return the new solar date | 新公历日期
     */
    public SolarDate minusDays(int days) {
        return of(toLocalDate().minusDays(days));
    }

    /**
     * Format as ISO date
     * 格式化为ISO日期
     *
     * @return the formatted string | 格式化字符串
     */
    public String format() {
        return String.format("%04d-%02d-%02d", year, month, day);
    }

    /**
     * Format as Chinese date
     * 格式化为中文日期
     *
     * @return the formatted string | 格式化字符串
     */
    public String formatChinese() {
        return String.format("%d年%d月%d日", year, month, day);
    }

    @Override
    public String toString() {
        return format();
    }
}
