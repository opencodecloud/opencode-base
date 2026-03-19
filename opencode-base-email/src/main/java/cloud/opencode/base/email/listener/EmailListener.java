package cloud.opencode.base.email.listener;

import cloud.opencode.base.email.ReceivedEmail;

/**
 * Email Listener Interface
 * 邮件监听器接口
 *
 * <p>Callback interface for receiving email notifications.</p>
 * <p>接收邮件通知的回调接口。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>New email notification - 新邮件通知</li>
 *   <li>Email deletion notification - 邮件删除通知</li>
 *   <li>Flag change notification - 标记变更通知</li>
 *   <li>Error handling - 错误处理</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * EmailListener listener = new EmailListener() {
 *     @Override
 *     public void onNewEmail(ReceivedEmail email) {
 *         System.out.println("New email from: " + email.from());
 *         processNewEmail(email);
 *     }
 *
 *     @Override
 *     public void onError(Throwable error) {
 *         log.error("Email listener error", error);
 *     }
 * };
 *
 * // Use with EmailIdleMonitor
 * EmailIdleMonitor monitor = EmailIdleMonitor.builder()
 *     .config(config)
 *     .listener(listener)
 *     .build();
 * monitor.start();
 * }</pre>
 *
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation dependent - 线程安全: 取决于实现</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
@FunctionalInterface
public interface EmailListener {

    /**
     * Called when a new email is received
     * 收到新邮件时调用
     *
     * @param email the received email | 接收到的邮件
     */
    void onNewEmail(ReceivedEmail email);

    /**
     * Called when an email is deleted
     * 邮件被删除时调用
     *
     * @param messageId the deleted message ID | 被删除的消息ID
     */
    default void onEmailDeleted(String messageId) {
        // Default no-op
    }

    /**
     * Called when email flags change
     * 邮件标记变更时调用
     *
     * @param messageId the message ID | 消息ID
     * @param flagName  the flag name (SEEN, FLAGGED, etc.) | 标记名称
     * @param value     the new flag value | 新的标记值
     */
    default void onFlagsChanged(String messageId, String flagName, boolean value) {
        // Default no-op
    }

    /**
     * Called when an error occurs
     * 发生错误时调用
     *
     * @param error the error | 错误
     */
    default void onError(Throwable error) {
        // Default no-op - subclasses should override to handle errors
    }

    /**
     * Called when the listener starts monitoring
     * 监听器开始监听时调用
     *
     * @param folder the folder being monitored | 被监听的文件夹
     */
    default void onMonitoringStarted(String folder) {
        // Default no-op
    }

    /**
     * Called when the listener stops monitoring
     * 监听器停止监听时调用
     *
     * @param folder the folder that was monitored | 之前被监听的文件夹
     */
    default void onMonitoringStopped(String folder) {
        // Default no-op
    }

    /**
     * Called when connection is lost and reconnecting
     * 连接丢失并正在重连时调用
     *
     * @param attempt the reconnection attempt number | 重连尝试次数
     */
    default void onReconnecting(int attempt) {
        // Default no-op
    }

    /**
     * Called when successfully reconnected
     * 成功重连时调用
     */
    default void onReconnected() {
        // Default no-op
    }

    /**
     * Create a simple listener that only handles new emails
     * 创建仅处理新邮件的简单监听器
     *
     * @param handler the new email handler | 新邮件处理器
     * @return the listener | 监听器
     */
    static EmailListener onNewEmail(java.util.function.Consumer<ReceivedEmail> handler) {
        return handler::accept;
    }

    /**
     * Create a listener that handles both new emails and errors
     * 创建处理新邮件和错误的监听器
     *
     * @param emailHandler the new email handler | 新邮件处理器
     * @param errorHandler the error handler | 错误处理器
     * @return the listener | 监听器
     */
    static EmailListener of(
            java.util.function.Consumer<ReceivedEmail> emailHandler,
            java.util.function.Consumer<Throwable> errorHandler) {
        return new EmailListener() {
            @Override
            public void onNewEmail(ReceivedEmail email) {
                emailHandler.accept(email);
            }

            @Override
            public void onError(Throwable error) {
                errorHandler.accept(error);
            }
        };
    }
}
