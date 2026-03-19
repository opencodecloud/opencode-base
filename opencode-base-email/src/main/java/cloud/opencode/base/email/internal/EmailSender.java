package cloud.opencode.base.email.internal;

import cloud.opencode.base.email.Email;
import cloud.opencode.base.email.SendResult;

/**
 * Email Sender Interface
 * 邮件发送器接口
 *
 * <p>Internal interface for email sending implementations.</p>
 * <p>邮件发送实现的内部接口。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Send email synchronously - 同步发送邮件</li>
 *   <li>Abstraction for different protocols - 不同协议的抽象</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation dependent - 线程安全: 取决于实现</li>
 * </ul>
 *
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * EmailSender sender = new SmtpEmailSender(config);
 * sender.send(email);
 * }</pre>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
public interface EmailSender {

    /**
     * Send an email synchronously
     * 同步发送邮件
     *
     * @param email the email to send | 要发送的邮件
     * @throws cloud.opencode.base.email.exception.EmailException if sending fails | 发送失败时抛出
     */
    void send(Email email);

    /**
     * Send an email and return the result with message ID
     * 发送邮件并返回包含消息ID的结果
     *
     * @param email the email to send | 要发送的邮件
     * @return the send result containing message ID | 包含消息ID的发送结果
     * @throws cloud.opencode.base.email.exception.EmailException if sending fails | 发送失败时抛出
     */
    default SendResult sendWithResult(Email email) {
        send(email);
        return SendResult.success(null); // Default implementation without message ID
    }

    /**
     * Close the sender and release resources
     * 关闭发送器并释放资源
     */
    default void close() {
        // Default no-op
    }
}
