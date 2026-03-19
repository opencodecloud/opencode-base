package cloud.opencode.base.sms.message;

import java.time.Instant;

/**
 * SMS Result
 * 短信结果
 *
 * <p>Result of sending an SMS message.</p>
 * <p>发送短信消息的结果。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Success/failure status with message ID - 成功/失败状态及消息ID</li>
 *   <li>Error code and message for failures - 失败时的错误码和消息</li>
 *   <li>Factory methods for success and failure - 成功和失败的工厂方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SmsResult ok = SmsResult.success("msg123", "13800138000");
 * SmsResult fail = SmsResult.failure("13800138000", "1001", "Send failed");
 * boolean sent = ok.success(); // true
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 * </ul>
 *
 * @param success whether send succeeded | 是否发送成功
 * @param messageId the message ID | 消息ID
 * @param phoneNumber the phone number | 手机号码
 * @param errorCode the error code if failed | 失败时的错误码
 * @param errorMessage the error message if failed | 失败时的错误消息
 * @param timestamp the timestamp | 时间戳
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 */
public record SmsResult(
    boolean success,
    String messageId,
    String phoneNumber,
    String errorCode,
    String errorMessage,
    Instant timestamp
) {
    /**
     * Create success result
     * 创建成功结果
     *
     * @param messageId the message ID | 消息ID
     * @param phoneNumber the phone number | 手机号码
     * @return the result | 结果
     */
    public static SmsResult success(String messageId, String phoneNumber) {
        return new SmsResult(true, messageId, phoneNumber, null, null, Instant.now());
    }

    /**
     * Create failure result
     * 创建失败结果
     *
     * @param phoneNumber the phone number | 手机号码
     * @param errorCode the error code | 错误码
     * @param errorMessage the error message | 错误消息
     * @return the result | 结果
     */
    public static SmsResult failure(String phoneNumber, String errorCode, String errorMessage) {
        return new SmsResult(false, null, phoneNumber, errorCode, errorMessage, Instant.now());
    }

    /**
     * Check if failed
     * 检查是否失败
     *
     * @return true if failed | 如果失败返回true
     */
    public boolean isFailed() {
        return !success;
    }

    /**
     * Create success result without phone
     * 创建不带手机号的成功结果
     *
     * @param messageId the message ID | 消息ID
     * @return the result | 结果
     */
    public static SmsResult success(String messageId) {
        return new SmsResult(true, messageId, null, null, null, Instant.now());
    }

    /**
     * Create failure result without phone
     * 创建不带手机号的失败结果
     *
     * @param errorCode the error code | 错误码
     * @param errorMessage the error message | 错误消息
     * @return the result | 结果
     */
    public static SmsResult failure(String errorCode, String errorMessage) {
        return new SmsResult(false, null, null, errorCode, errorMessage, Instant.now());
    }
}
