package cloud.opencode.base.lunar.exception;

/**
 * Lunar Error Code
 * 农历错误码枚举
 *
 * <p>Error codes for lunar calendar operations.</p>
 * <p>农历操作的错误码。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Categorized error codes (conversion, range, validation) - 分类错误码（转换、范围、验证）</li>
 *   <li>Numeric code and description - 数字代码和描述</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * LunarErrorCode code = LunarErrorCode.DATE_OUT_OF_RANGE;
 * int numeric = code.getCode();
 * String desc = code.getDescription();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable enum) - 线程安全: 是（不可变枚举）</li>
 *   <li>Null-safe: N/A - 空值安全: 不适用</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.0
 */
public enum LunarErrorCode {

    /**
     * Unknown error | 未知错误
     */
    UNKNOWN(0, "未知错误"),

    // ============ Conversion errors 1xxx ============

    /**
     * Conversion failed | 日期转换失败
     */
    CONVERSION_FAILED(1001, "日期转换失败"),

    /**
     * Solar to lunar failed | 公历转农历失败
     */
    SOLAR_TO_LUNAR_FAILED(1002, "公历转农历失败"),

    /**
     * Lunar to solar failed | 农历转公历失败
     */
    LUNAR_TO_SOLAR_FAILED(1003, "农历转公历失败"),

    // ============ Range errors 2xxx ============

    /**
     * Year out of range | 年份超出范围
     */
    YEAR_OUT_OF_RANGE(2001, "年份超出范围"),

    /**
     * Month out of range | 月份超出范围
     */
    MONTH_OUT_OF_RANGE(2002, "月份超出范围"),

    /**
     * Day out of range | 日期超出范围
     */
    DAY_OUT_OF_RANGE(2003, "日期超出范围"),

    /**
     * Date out of range | 日期超出支持范围
     */
    DATE_OUT_OF_RANGE(2004, "日期超出支持范围"),

    // ============ Validation errors 3xxx ============

    /**
     * Invalid lunar date | 无效农历日期
     */
    INVALID_LUNAR_DATE(3001, "无效农历日期"),

    /**
     * Invalid leap month | 无效闰月
     */
    INVALID_LEAP_MONTH(3002, "无效闰月"),

    /**
     * Invalid day | 无效日期
     */
    INVALID_DAY(3003, "无效日期"),

    /**
     * Null input | 输入为空
     */
    NULL_INPUT(3004, "输入为空"),

    // ============ Data errors 4xxx ============

    /**
     * Data not loaded | 农历数据未加载
     */
    DATA_NOT_LOADED(4001, "农历数据未加载"),

    /**
     * Data corrupted | 农历数据损坏
     */
    DATA_CORRUPTED(4002, "农历数据损坏");

    private final int code;
    private final String description;

    LunarErrorCode(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * Get error code
     * 获取错误码
     *
     * @return the error code | 错误码
     */
    public int getCode() {
        return code;
    }

    /**
     * Get description
     * 获取描述
     *
     * @return the description | 描述
     */
    public String getDescription() {
        return description;
    }
}
