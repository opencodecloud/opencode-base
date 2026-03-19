package cloud.opencode.base.money.exception;

/**
 * Money Exception
 * 金额异常基类
 *
 * <p>Base exception for all money-related errors.</p>
 * <p>所有金额相关错误的基础异常类。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Base exception for all money-related errors - 所有金额相关错误的基础异常</li>
 *   <li>Carries MoneyErrorCode for error classification - 携带MoneyErrorCode用于错误分类</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw new MoneyException("Operation failed", MoneyErrorCode.UNKNOWN);
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
public class MoneyException extends RuntimeException {

    private final MoneyErrorCode errorCode;

    /**
     * Create money exception with message
     * 创建带消息的金额异常
     *
     * @param message the error message | 错误消息
     */
    public MoneyException(String message) {
        super(message);
        this.errorCode = MoneyErrorCode.UNKNOWN;
    }

    /**
     * Create money exception with message and error code
     * 创建带消息和错误码的金额异常
     *
     * @param message the error message | 错误消息
     * @param errorCode the error code | 错误码
     */
    public MoneyException(String message, MoneyErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Create money exception with message, cause, and error code
     * 创建带消息、原因和错误码的金额异常
     *
     * @param message the error message | 错误消息
     * @param cause the cause | 原因
     * @param errorCode the error code | 错误码
     */
    public MoneyException(String message, Throwable cause, MoneyErrorCode errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * Create money exception with message and cause
     * 创建带消息和原因的金额异常
     *
     * @param message the error message | 错误消息
     * @param cause the cause | 原因
     */
    public MoneyException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = MoneyErrorCode.UNKNOWN;
    }

    /**
     * Get error code
     * 获取错误码
     *
     * @return the error code | 错误码
     */
    public MoneyErrorCode getErrorCode() {
        return errorCode;
    }
}
