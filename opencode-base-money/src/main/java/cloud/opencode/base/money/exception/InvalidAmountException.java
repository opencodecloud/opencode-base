package cloud.opencode.base.money.exception;

/**
 * Invalid Amount Exception
 * 无效金额异常
 *
 * <p>Exception thrown when an invalid amount is provided.</p>
 * <p>当提供无效金额时抛出此异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Exception for invalid monetary amounts - 无效金额异常</li>
 *   <li>Carries the invalid value for diagnostics - 携带无效值用于诊断</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw new InvalidAmountException("Amount too large", "999999999999");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-money V1.0.0
 */
public class InvalidAmountException extends MoneyException {

    private final String invalidValue;

    /**
     * Create invalid amount exception
     * 创建无效金额异常
     *
     * @param message the error message | 错误消息
     */
    public InvalidAmountException(String message) {
        super(message, MoneyErrorCode.INVALID_AMOUNT);
        this.invalidValue = null;
    }

    /**
     * Create invalid amount exception with invalid value
     * 创建带无效值的无效金额异常
     *
     * @param message the error message | 错误消息
     * @param invalidValue the invalid value | 无效的值
     */
    public InvalidAmountException(String message, String invalidValue) {
        super(message, MoneyErrorCode.INVALID_AMOUNT);
        this.invalidValue = invalidValue;
    }

    /**
     * Create invalid amount exception with message and cause
     * 创建带消息和原因的无效金额异常
     *
     * @param message the error message | 错误消息
     * @param cause the cause | 原因
     */
    public InvalidAmountException(String message, Throwable cause) {
        super(message, cause, MoneyErrorCode.INVALID_AMOUNT);
        this.invalidValue = null;
    }

    /**
     * Create invalid amount exception with error code
     * 创建带错误码的无效金额异常
     *
     * @param message the error message | 错误消息
     * @param errorCode the error code | 错误码
     */
    public InvalidAmountException(String message, MoneyErrorCode errorCode) {
        super(message, errorCode);
        this.invalidValue = null;
    }

    /**
     * Create exception for format error
     * 创建格式错误的异常
     *
     * @param value the invalid value | 无效的值
     * @return the exception | 异常
     */
    public static InvalidAmountException formatError(String value) {
        return new InvalidAmountException(
            "Invalid amount format: " + value,
            value
        );
    }

    /**
     * Create exception for precision error
     * 创建精度错误的异常
     *
     * @param value the value | 值
     * @param maxScale the maximum scale | 最大精度
     * @return the exception | 异常
     */
    public static InvalidAmountException precisionError(String value, int maxScale) {
        InvalidAmountException ex = new InvalidAmountException(
            String.format("Amount precision exceeds %d decimal places: %s", maxScale, value),
            MoneyErrorCode.AMOUNT_PRECISION_ERROR
        );
        return ex;
    }

    /**
     * Create exception for overflow error
     * 创建溢出错误的异常
     *
     * @param value the value | 值
     * @return the exception | 异常
     */
    public static InvalidAmountException overflow(String value) {
        return new InvalidAmountException(
            "Amount overflow: " + value,
            MoneyErrorCode.AMOUNT_OVERFLOW
        );
    }

    /**
     * Get invalid value
     * 获取无效的值
     *
     * @return the invalid value | 无效的值
     */
    public String getInvalidValue() {
        return invalidValue;
    }
}
