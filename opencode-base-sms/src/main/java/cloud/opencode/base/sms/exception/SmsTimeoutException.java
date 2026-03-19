package cloud.opencode.base.sms.exception;

import java.time.Duration;

/**
 * SMS Timeout Exception
 * 短信超时异常
 *
 * <p>Exception thrown when SMS operations time out.</p>
 * <p>短信操作超时时抛出的异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Timeout type classification (CONNECTION, READ, TOTAL) - 超时类型分类</li>
 *   <li>Timeout duration and operation tracking - 超时时长和操作跟踪</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw new SmsTimeoutException(SmsErrorCode.SEND_TIMEOUT,
 *     SmsTimeoutException.TimeoutType.READ, Duration.ofSeconds(30), "sendSms");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 */
public class SmsTimeoutException extends SmsException {

    /**
     * Timeout type enumeration
     * 超时类型枚举
     */
    public enum TimeoutType {
        /** Connection timeout | 连接超时 */
        CONNECTION,
        /** Read/response timeout | 读取/响应超时 */
        READ,
        /** Total operation timeout | 总操作超时 */
        TOTAL
    }

    private final TimeoutType timeoutType;
    private final Duration timeout;
    private final String operation;

    public SmsTimeoutException(SmsErrorCode errorCode, TimeoutType timeoutType, Duration timeout) {
        super(errorCode);
        this.timeoutType = timeoutType;
        this.timeout = timeout;
        this.operation = null;
    }

    public SmsTimeoutException(SmsErrorCode errorCode, TimeoutType timeoutType, Duration timeout, String operation) {
        super(errorCode, operation + " timed out after " + timeout.toMillis() + "ms");
        this.timeoutType = timeoutType;
        this.timeout = timeout;
        this.operation = operation;
    }

    public SmsTimeoutException(SmsErrorCode errorCode, TimeoutType timeoutType, Duration timeout, Throwable cause) {
        super(errorCode, cause);
        this.timeoutType = timeoutType;
        this.timeout = timeout;
        this.operation = null;
    }

    /**
     * Gets the timeout type
     * 获取超时类型
     *
     * @return timeout type | 超时类型
     */
    public TimeoutType getTimeoutType() {
        return timeoutType;
    }

    /**
     * Gets the timeout duration
     * 获取超时时长
     *
     * @return timeout duration | 超时时长
     */
    public Duration getTimeout() {
        return timeout;
    }

    /**
     * Gets the operation that timed out
     * 获取超时的操作
     *
     * @return operation name or null | 操作名称或null
     */
    public String getOperation() {
        return operation;
    }

    /**
     * Creates a connection timeout exception
     * 创建连接超时异常
     *
     * @param timeout timeout duration | 超时时长
     * @return timeout exception | 超时异常
     */
    public static SmsTimeoutException connectionTimeout(Duration timeout) {
        return new SmsTimeoutException(SmsErrorCode.CONNECTION_TIMEOUT, TimeoutType.CONNECTION, timeout);
    }

    /**
     * Creates a connection timeout exception with cause
     * 创建带原因的连接超时异常
     *
     * @param timeout timeout duration | 超时时长
     * @param cause original exception | 原始异常
     * @return timeout exception | 超时异常
     */
    public static SmsTimeoutException connectionTimeout(Duration timeout, Throwable cause) {
        return new SmsTimeoutException(SmsErrorCode.CONNECTION_TIMEOUT, TimeoutType.CONNECTION, timeout, cause);
    }

    /**
     * Creates a read timeout exception
     * 创建读取超时异常
     *
     * @param timeout timeout duration | 超时时长
     * @return timeout exception | 超时异常
     */
    public static SmsTimeoutException readTimeout(Duration timeout) {
        return new SmsTimeoutException(SmsErrorCode.READ_TIMEOUT, TimeoutType.READ, timeout);
    }

    /**
     * Creates a read timeout exception with cause
     * 创建带原因的读取超时异常
     *
     * @param timeout timeout duration | 超时时长
     * @param cause original exception | 原始异常
     * @return timeout exception | 超时异常
     */
    public static SmsTimeoutException readTimeout(Duration timeout, Throwable cause) {
        return new SmsTimeoutException(SmsErrorCode.READ_TIMEOUT, TimeoutType.READ, timeout, cause);
    }

    /**
     * Creates a send timeout exception
     * 创建发送超时异常
     *
     * @param timeout timeout duration | 超时时长
     * @param operation operation description | 操作描述
     * @return timeout exception | 超时异常
     */
    public static SmsTimeoutException sendTimeout(Duration timeout, String operation) {
        return new SmsTimeoutException(SmsErrorCode.SEND_TIMEOUT, TimeoutType.TOTAL, timeout, operation);
    }
}
