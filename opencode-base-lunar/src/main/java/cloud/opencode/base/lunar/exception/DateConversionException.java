package cloud.opencode.base.lunar.exception;

import java.io.Serial;

/**
 * Date Conversion Exception
 * 日期转换异常
 *
 * <p>Exception thrown when date conversion fails.</p>
 * <p>当日期转换失败时抛出的异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Solar-to-lunar conversion errors - 公历转农历错误</li>
 *   <li>Lunar-to-solar conversion errors - 农历转公历错误</li>
 *   <li>Cause chaining support - 原因链支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw new DateConversionException("Cannot convert date: out of range");
 * throw new DateConversionException("Conversion failed", cause);
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
public class DateConversionException extends LunarException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Create conversion exception
     * 创建转换异常
     *
     * @param message the error message | 错误消息
     */
    public DateConversionException(String message) {
        super(message, LunarErrorCode.CONVERSION_FAILED);
    }

    /**
     * Create conversion exception with cause
     * 创建带原因的转换异常
     *
     * @param message the error message | 错误消息
     * @param cause the cause | 原因
     */
    public DateConversionException(String message, Throwable cause) {
        super(message, cause, LunarErrorCode.CONVERSION_FAILED);
    }

    /**
     * Create solar to lunar conversion exception
     * 创建公历转农历异常
     *
     * @param year the year | 年
     * @param month the month | 月
     * @param day the day | 日
     * @return the exception | 异常
     */
    public static DateConversionException solarToLunar(int year, int month, int day) {
        return new DateConversionException(
            String.format("Failed to convert solar date %d-%02d-%02d to lunar", year, month, day));
    }

    /**
     * Create lunar to solar conversion exception
     * 创建农历转公历异常
     *
     * @param year the year | 年
     * @param month the month | 月
     * @param day the day | 日
     * @param isLeap whether leap month | 是否闰月
     * @return the exception | 异常
     */
    public static DateConversionException lunarToSolar(int year, int month, int day, boolean isLeap) {
        return new DateConversionException(
            String.format("Failed to convert lunar date %d-%s%02d-%02d to solar",
                year, isLeap ? "闰" : "", month, day));
    }
}
