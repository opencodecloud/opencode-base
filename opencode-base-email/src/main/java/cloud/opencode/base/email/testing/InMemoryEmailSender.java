package cloud.opencode.base.email.testing;

import cloud.opencode.base.email.Email;
import cloud.opencode.base.email.SendResult;
import cloud.opencode.base.email.internal.EmailSender;

import cloud.opencode.base.email.exception.EmailErrorCode;
import cloud.opencode.base.email.exception.EmailSendException;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

/**
 * In-Memory Email Sender for Testing
 * 内存邮件发送器（用于测试）
 *
 * <p>Captures all sent emails in memory without actually sending them.
 * Useful for unit and integration tests.</p>
 * <p>在内存中捕获所有发送的邮件而不实际发送。适用于单元和集成测试。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Capture sent emails in memory - 在内存中捕获发送的邮件</li>
 *   <li>Query by recipient, subject, content - 按收件人、主题、内容查询</li>
 *   <li>Thread-safe operations - 线程安全操作</li>
 *   <li>Failure simulation support - 模拟发送失败支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * InMemoryEmailSender sender = new InMemoryEmailSender();
 * OpenEmail.configure(config, sender);
 *
 * OpenEmail.sendText("user@example.com", "Test", "Hello");
 *
 * assertThat(sender.getSentCount()).isEqualTo(1);
 * assertThat(sender.getLastEmail().subject()).isEqualTo("Test");
 * assertThat(sender.findByRecipient("user@example.com")).hasSize(1);
 *
 * sender.clear();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (CopyOnWriteArrayList) - 线程安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.3
 */
public class InMemoryEmailSender implements EmailSender {

    private final CopyOnWriteArrayList<Email> sentEmails = new CopyOnWriteArrayList<>();
    private volatile Predicate<Email> failureSimulator;

    @Override
    public void send(Email email) {
        Objects.requireNonNull(email, "email must not be null");
        if (failureSimulator != null && failureSimulator.test(email)) {
            throw new EmailSendException(
                    "Simulated send failure",
                    email,
                    EmailErrorCode.MESSAGE_REJECTED
            );
        }
        sentEmails.add(email);
    }

    @Override
    public SendResult sendWithResult(Email email) {
        send(email);
        return SendResult.success("<" + UUID.randomUUID() + "@test>");
    }

    /**
     * Get all sent emails
     * 获取所有已发送邮件
     *
     * @return unmodifiable list of sent emails | 不可修改的已发送邮件列表
     */
    public List<Email> getSentEmails() {
        return List.copyOf(sentEmails);
    }

    /**
     * Get the last sent email
     * 获取最后一封已发送邮件
     *
     * @return the last email or null if none sent | 最后一封邮件，未发送则返回null
     */
    public Email getLastEmail() {
        List<Email> snapshot = List.copyOf(sentEmails);
        if (snapshot.isEmpty()) {
            return null;
        }
        return snapshot.getLast();
    }

    /**
     * Get the number of sent emails
     * 获取已发送邮件数量
     *
     * @return the count | 数量
     */
    public int getSentCount() {
        return sentEmails.size();
    }

    /**
     * Find emails sent to a specific recipient
     * 查找发送到特定收件人的邮件
     *
     * @param recipient the recipient email address | 收件人邮箱地址
     * @return matching emails | 匹配的邮件列表
     */
    public List<Email> findByRecipient(String recipient) {
        Objects.requireNonNull(recipient, "recipient must not be null");
        return sentEmails.stream()
                .filter(e -> e.getAllRecipients().stream()
                        .anyMatch(r -> r.equalsIgnoreCase(recipient)))
                .toList();
    }

    /**
     * Find emails by subject (contains match)
     * 按主题查找邮件（包含匹配）
     *
     * @param subjectPart the subject part to match | 要匹配的主题部分
     * @return matching emails | 匹配的邮件列表
     */
    public List<Email> findBySubject(String subjectPart) {
        Objects.requireNonNull(subjectPart, "subjectPart must not be null");
        return sentEmails.stream()
                .filter(e -> e.subject() != null && e.subject().contains(subjectPart))
                .toList();
    }

    /**
     * Find emails matching a predicate
     * 查找匹配谓词的邮件
     *
     * @param predicate the filter predicate | 过滤谓词
     * @return matching emails | 匹配的邮件列表
     */
    public List<Email> findBy(Predicate<Email> predicate) {
        Objects.requireNonNull(predicate, "predicate must not be null");
        return sentEmails.stream().filter(predicate).toList();
    }

    /**
     * Check if any email was sent to the recipient
     * 检查是否有邮件发送到该收件人
     *
     * @param recipient the recipient email address | 收件人邮箱地址
     * @return true if at least one email was sent | 至少发送一封返回true
     */
    public boolean hasSentTo(String recipient) {
        return !findByRecipient(recipient).isEmpty();
    }

    /**
     * Clear all captured emails
     * 清空所有已捕获的邮件
     */
    public void clear() {
        sentEmails.clear();
    }

    /**
     * Set failure simulator - emails matching the predicate will throw an exception
     * 设置失败模拟器 - 匹配谓词的邮件将抛出异常
     *
     * @param failureSimulator the failure predicate, or null to disable | 失败谓词，null禁用
     */
    public void simulateFailure(Predicate<Email> failureSimulator) {
        this.failureSimulator = failureSimulator;
    }

    /**
     * Remove failure simulator
     * 移除失败模拟器
     */
    public void clearFailureSimulator() {
        this.failureSimulator = null;
    }
}
