package cloud.opencode.base.lunar.exception;

import java.io.Serial;

/**
 * Invalid Lunar Date Exception
 * 无效农历日期异常
 *
 * <p>Exception thrown when lunar date is invalid.</p>
 * <p>当农历日期无效时抛出的异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Invalid date detail tracking (year, month, day, leap) - 无效日期详情跟踪（年、月、日、闰）</li>
 *   <li>Descriptive error messages - 描述性错误消息</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw new InvalidLunarDateException("Invalid lunar date");
 * throw new InvalidLunarDateException(2024, 13, 1, false);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 *   <li>Null-safe: No (message must not be null) - 空值安全: 否（消息不能为null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.0
 */
public class InvalidLunarDateException extends LunarException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final int year;
    private final int month;
    private final int day;
    private final boolean isLeap;

    /**
     * Create exception with message
     * 创建带消息的异常
     *
     * @param message the error message | 错误消息
     */
    public InvalidLunarDateException(String message) {
        super(message, LunarErrorCode.INVALID_LUNAR_DATE);
        this.year = 0;
        this.month = 0;
        this.day = 0;
        this.isLeap = false;
    }

    /**
     * Create exception with date details
     * 创建带日期详情的异常
     *
     * @param year the year | 年
     * @param month the month | 月
     * @param day the day | 日
     * @param isLeap whether leap month | 是否闰月
     * @param reason the reason | 原因
     */
    public InvalidLunarDateException(int year, int month, int day, boolean isLeap, String reason) {
        super(String.format("Invalid lunar date: %d-%s%02d-%02d (%s)",
            year, isLeap ? "闰" : "", month, day, reason),
            LunarErrorCode.INVALID_LUNAR_DATE);
        this.year = year;
        this.month = month;
        this.day = day;
        this.isLeap = isLeap;
    }

    /**
     * Create exception for invalid leap month
     * 创建无效闰月异常
     *
     * @param year the year | 年
     * @param month the month | 月
     * @param actualLeapMonth the actual leap month (0 if none) | 实际闰月（0表示无闰月）
     * @return the exception | 异常
     */
    public static InvalidLunarDateException invalidLeapMonth(int year, int month, int actualLeapMonth) {
        String reason = actualLeapMonth > 0
            ? "Year " + year + " has leap month " + actualLeapMonth + ", not " + month
            : "Year " + year + " has no leap month";
        return new InvalidLunarDateException(year, month, 1, true, reason);
    }

    /**
     * Create exception for invalid day
     * 创建无效日期异常
     *
     * @param year the year | 年
     * @param month the month | 月
     * @param day the day | 日
     * @param isLeap whether leap month | 是否闰月
     * @param maxDay the maximum day | 最大日期
     * @return the exception | 异常
     */
    public static InvalidLunarDateException invalidDay(int year, int month, int day, boolean isLeap, int maxDay) {
        return new InvalidLunarDateException(year, month, day, isLeap,
            "Day must be between 1 and " + maxDay);
    }

    /**
     * Get year
     * 获取年
     *
     * @return the year | 年
     */
    public int getYear() {
        return year;
    }

    /**
     * Get month
     * 获取月
     *
     * @return the month | 月
     */
    public int getMonth() {
        return month;
    }

    /**
     * Get day
     * 获取日
     *
     * @return the day | 日
     */
    public int getDay() {
        return day;
    }

    /**
     * Check if leap month
     * 是否闰月
     *
     * @return true if leap month | 如果是闰月返回true
     */
    public boolean isLeap() {
        return isLeap;
    }
}
