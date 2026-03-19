package cloud.opencode.base.money.exception;

/**
 * Money Error Code
 * 金额错误码
 *
 * <p>Error codes for money-related operations.</p>
 * <p>金额相关操作的错误码。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Enumeration of money-related error codes - 金额相关错误码枚举</li>
 *   <li>Bilingual error messages (English and Chinese) - 双语错误消息（英文和中文）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * MoneyErrorCode code = MoneyErrorCode.INVALID_AMOUNT;
 * System.out.println(code.getMessage());    // "Invalid amount"
 * System.out.println(code.getMessageZh());  // "无效金额"
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable enum) - 线程安全: 是（不可变枚举）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-money V1.0.0
 */
public enum MoneyErrorCode {

    // Unknown | 未知
    UNKNOWN(0, "Unknown error", "未知错误"),

    // Amount errors | 金额错误 1xxx
    INVALID_AMOUNT(1001, "Invalid amount", "无效金额"),
    AMOUNT_FORMAT_ERROR(1002, "Amount format error", "金额格式错误"),
    AMOUNT_OVERFLOW(1003, "Amount overflow", "金额溢出"),
    AMOUNT_PRECISION_ERROR(1004, "Amount precision error", "金额精度超限"),
    AMOUNT_NEGATIVE(1005, "Amount cannot be negative", "金额不能为负"),
    NULL_AMOUNT(1006, "Amount cannot be null", "金额不能为空"),

    // Currency errors | 币种错误 2xxx
    CURRENCY_MISMATCH(2001, "Currency mismatch", "币种不匹配"),
    UNSUPPORTED_CURRENCY(2002, "Unsupported currency", "不支持的币种"),
    NULL_CURRENCY(2003, "Currency cannot be null", "币种不能为空"),

    // Exchange rate errors | 汇率错误 3xxx
    RATE_NOT_FOUND(3001, "Exchange rate not found", "汇率未找到"),
    RATE_EXPIRED(3002, "Exchange rate expired", "汇率已过期"),
    RATE_INVALID(3003, "Invalid exchange rate", "无效汇率"),

    // Calculation errors | 计算错误 4xxx
    ALLOCATION_ERROR(4001, "Allocation calculation error", "分摊计算错误"),
    ZERO_DIVISOR(4002, "Divisor cannot be zero", "除数不能为零"),
    ZERO_RATIO(4003, "Ratio cannot be zero", "比例不能为零"),
    INVALID_RATIO(4004, "Invalid ratio", "无效比例");

    private final int code;
    private final String message;
    private final String messageZh;

    MoneyErrorCode(int code, String message, String messageZh) {
        this.code = code;
        this.message = message;
        this.messageZh = messageZh;
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
     * Get error message
     * 获取错误消息
     *
     * @return the error message | 错误消息
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get Chinese error message
     * 获取中文错误消息
     *
     * @return the Chinese error message | 中文错误消息
     */
    public String getMessageZh() {
        return messageZh;
    }
}
