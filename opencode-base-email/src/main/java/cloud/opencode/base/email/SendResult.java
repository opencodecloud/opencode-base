package cloud.opencode.base.email;

import java.time.Instant;

/**
 * Email Send Result
 * 邮件发送结果
 *
 * <p>Result of sending an email, containing the message ID for tracking.</p>
 * <p>发送邮件的结果，包含用于跟踪的消息ID。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Message ID for tracking - 用于跟踪的消息ID</li>
 *   <li>Send timestamp - 发送时间戳</li>
 *   <li>Success status - 成功状态</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SendResult result = OpenEmail.sendWithResult(email);
 * System.out.println("Message ID: " + result.messageId());
 * System.out.println("Sent at: " + result.sentAt());
 * }</pre>
 *
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
public record SendResult(
        String messageId,
        Instant sentAt,
        boolean success
) {

    /**
     * Create a successful send result
     * 创建成功的发送结果
     *
     * @param messageId the message ID | 消息ID
     * @return the send result | 发送结果
     */
    public static SendResult success(String messageId) {
        return new SendResult(messageId, Instant.now(), true);
    }

    /**
     * Create a failed send result
     * 创建失败的发送结果
     *
     * @return the send result | 发送结果
     */
    public static SendResult failure() {
        return new SendResult(null, Instant.now(), false);
    }

    /**
     * Check if the message has a message ID
     * 检查消息是否有消息ID
     *
     * @return true if message ID is present | 存在消息ID返回true
     */
    public boolean hasMessageId() {
        return messageId != null && !messageId.isBlank();
    }
}
