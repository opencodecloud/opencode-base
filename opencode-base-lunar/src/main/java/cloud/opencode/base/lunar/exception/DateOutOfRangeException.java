package cloud.opencode.base.lunar.exception;

/**
 * Date Out Of Range Exception
 * 日期越界异常
 *
 * <p>Exception thrown when date is outside supported range (1900-2100).</p>
 * <p>当日期超出支持范围（1900-2100）时抛出的异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Year range validation (1900-2100) - 年份范围验证（1900-2100）</li>
 *   <li>Year value tracking - 年份值跟踪</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw new DateOutOfRangeException(1850);
 * throw new DateOutOfRangeException("Year 2200 is out of range");
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
public class DateOutOfRangeException extends LunarException {

    /**
     * Minimum supported year | 最小支持年份
     */
    public static final int MIN_YEAR = 1900;

    /**
     * Maximum supported year | 最大支持年份
     */
    public static final int MAX_YEAR = 2100;

    private final int year;

    /**
     * Create exception with message
     * 创建带消息的异常
     *
     * @param message the error message | 错误消息
     */
    public DateOutOfRangeException(String message) {
        super(message, LunarErrorCode.DATE_OUT_OF_RANGE);
        this.year = 0;
    }

    /**
     * Create exception with year
     * 创建带年份的异常
     *
     * @param year the year out of range | 越界的年份
     */
    public DateOutOfRangeException(int year) {
        super(String.format("Year %d is out of range [%d, %d]", year, MIN_YEAR, MAX_YEAR),
            LunarErrorCode.YEAR_OUT_OF_RANGE);
        this.year = year;
    }

    /**
     * Get the year
     * 获取年份
     *
     * @return the year | 年份
     */
    public int getYear() {
        return year;
    }

    /**
     * Get minimum year
     * 获取最小年份
     *
     * @return the minimum year | 最小年份
     */
    public int getMinYear() {
        return MIN_YEAR;
    }

    /**
     * Get maximum year
     * 获取最大年份
     *
     * @return the maximum year | 最大年份
     */
    public int getMaxYear() {
        return MAX_YEAR;
    }
}
