package cloud.opencode.base.email;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Batch Email Send Result
 * 批量邮件发送结果
 *
 * <p>Contains the results of a batch email send operation,
 * including individual results for each email.</p>
 * <p>包含批量邮件发送操作的结果，包括每封邮件的单独结果。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Individual results per email - 每封邮件的单独结果</li>
 *   <li>Success/failure counts - 成功/失败计数</li>
 *   <li>Total duration tracking - 总耗时跟踪</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * BatchSendResult result = sender.sendBatch(emails);
 * System.out.println("Sent: " + result.successCount() + "/" + result.totalCount());
 * result.failures().forEach(f -> log.error("Failed: {}", f.error()));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.3
 */
public record BatchSendResult(
        List<ItemResult> results,
        Instant startedAt,
        Duration duration
) {

    /**
     * Get the total number of emails in the batch
     * 获取批量中的邮件总数
     *
     * @return the total count | 总数
     */
    public int totalCount() {
        return results.size();
    }

    /**
     * Get the number of successfully sent emails
     * 获取成功发送的邮件数量
     *
     * @return the success count | 成功数量
     */
    public int successCount() {
        return (int) results.stream().filter(ItemResult::success).count();
    }

    /**
     * Get the number of failed emails
     * 获取发送失败的邮件数量
     *
     * @return the failure count | 失败数量
     */
    public int failureCount() {
        return totalCount() - successCount();
    }

    /**
     * Check if all emails were sent successfully
     * 检查是否所有邮件都已成功发送
     *
     * @return true if all succeeded | 全部成功返回true
     */
    public boolean allSucceeded() {
        return results.stream().allMatch(ItemResult::success);
    }

    /**
     * Get only the failed results
     * 仅获取失败的结果
     *
     * @return list of failed results | 失败结果列表
     */
    public List<ItemResult> failures() {
        return results.stream().filter(r -> !r.success()).toList();
    }

    /**
     * Get only the successful results
     * 仅获取成功的结果
     *
     * @return list of successful results | 成功结果列表
     */
    public List<ItemResult> successes() {
        return results.stream().filter(ItemResult::success).toList();
    }

    /**
     * Individual email send result within a batch
     * 批量中单封邮件的发送结果
     *
     * @param email     the email that was sent | 被发送的邮件
     * @param messageId the message ID (null if failed) | 消息ID（失败时为null）
     * @param success   whether the send succeeded | 是否发送成功
     * @param error     the error message (null if succeeded) | 错误消息（成功时为null）
     * @param cause     the exception cause (null if succeeded) | 异常原因（成功时为null）
     */
    public record ItemResult(
            Email email,
            String messageId,
            boolean success,
            String error,
            Throwable cause
    ) {
        /**
         * Create a success result
         * 创建成功结果
         *
         * @param email     the email | 邮件
         * @param messageId the message ID | 消息ID
         * @return the result | 结果
         */
        public static ItemResult success(Email email, String messageId) {
            return new ItemResult(email, messageId, true, null, null);
        }

        /**
         * Create a failure result
         * 创建失败结果
         *
         * @param email the email | 邮件
         * @param cause the exception | 异常
         * @return the result | 结果
         */
        public static ItemResult failure(Email email, Throwable cause) {
            return new ItemResult(email, null, false, cause.getMessage(), cause);
        }
    }
}
