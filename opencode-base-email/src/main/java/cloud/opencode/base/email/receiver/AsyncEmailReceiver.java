package cloud.opencode.base.email.receiver;

import cloud.opencode.base.email.EmailReceiveConfig;
import cloud.opencode.base.email.ReceivedEmail;
import cloud.opencode.base.email.internal.EmailReceiver;
import cloud.opencode.base.email.query.EmailQuery;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

/**
 * Async Email Receiver Wrapper
 * 异步邮件接收器包装
 *
 * <p>Wraps any EmailReceiver to provide async operations.</p>
 * <p>包装任何EmailReceiver以提供异步操作。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Async receive with CompletableFuture - 使用CompletableFuture的异步接收</li>
 *   <li>Virtual thread support (JDK 21+) - 虚拟线程支持（JDK 21+）</li>
 *   <li>Callback support - 回调支持</li>
 *   <li>Builder pattern - 构建器模式</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create async receiver with builder
 * AsyncEmailReceiver asyncReceiver = AsyncEmailReceiver.builder()
 *     .config(config)
 *     .build();
 *
 * // Async receive
 * asyncReceiver.receiveAsync(query)
 *     .thenAccept(emails -> processEmails(emails))
 *     .exceptionally(e -> { log.error("Failed", e); return null; });
 *
 * // Async receive with callback
 * asyncReceiver.receiveAsync(query, (emails, error) -> {
 *     if (error != null) {
 *         log.error("Failed", error);
 *     } else {
 *         processEmails(emails);
 *     }
 * });
 * }</pre>
 *
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
public class AsyncEmailReceiver implements AutoCloseable {

    private final EmailReceiver delegate;
    private final ExecutorService executor;
    private final boolean ownsDelegate;
    private final boolean ownsExecutor;

    /**
     * Create async receiver with delegate
     * 使用委托创建异步接收器
     *
     * @param delegate the underlying receiver | 底层接收器
     */
    public AsyncEmailReceiver(EmailReceiver delegate) {
        this(delegate, createDefaultExecutor(), false, true);
    }

    /**
     * Create async receiver with delegate and executor
     * 使用委托和执行器创建异步接收器
     *
     * @param delegate     the underlying receiver | 底层接收器
     * @param executor     the executor for async operations | 异步操作的执行器
     * @param ownsDelegate whether this wrapper owns the delegate lifecycle | 是否拥有委托的生命周期
     * @param ownsExecutor whether this wrapper owns the executor lifecycle | 是否拥有执行器的生命周期
     */
    private AsyncEmailReceiver(EmailReceiver delegate, ExecutorService executor,
                                boolean ownsDelegate, boolean ownsExecutor) {
        if (delegate == null) {
            throw new IllegalArgumentException("Delegate receiver cannot be null");
        }
        this.delegate = delegate;
        this.executor = executor != null ? executor : createDefaultExecutor();
        this.ownsDelegate = ownsDelegate;
        this.ownsExecutor = ownsExecutor || executor == null;
    }

    private static ExecutorService createDefaultExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    /**
     * Create a new builder
     * 创建新的构建器
     *
     * @return the builder | 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    // ==================== Async Methods ====================

    /**
     * Receive unread emails asynchronously
     * 异步接收未读邮件
     *
     * @return future containing received emails | 包含接收邮件的future
     */
    public CompletableFuture<List<ReceivedEmail>> receiveUnreadAsync() {
        return CompletableFuture.supplyAsync(() -> {
            ensureConnected();
            return delegate.receiveUnread();
        }, executor);
    }

    /**
     * Receive emails asynchronously with query
     * 使用查询异步接收邮件
     *
     * @param query the email query | 邮件查询
     * @return future containing received emails | 包含接收邮件的future
     */
    public CompletableFuture<List<ReceivedEmail>> receiveAsync(EmailQuery query) {
        return CompletableFuture.supplyAsync(() -> {
            ensureConnected();
            return delegate.receive(query);
        }, executor);
    }

    /**
     * Receive emails asynchronously with callback
     * 使用回调异步接收邮件
     *
     * @param query    the email query | 邮件查询
     * @param callback callback on completion | 完成时的回调
     * @return future containing received emails | 包含接收邮件的future
     */
    public CompletableFuture<List<ReceivedEmail>> receiveAsync(
            EmailQuery query,
            BiConsumer<List<ReceivedEmail>, Throwable> callback) {
        return receiveAsync(query)
                .whenComplete((emails, error) -> {
                    if (callback != null) {
                        callback.accept(emails, error);
                    }
                });
    }

    /**
     * Receive all emails asynchronously
     * 异步接收所有邮件
     *
     * @return future containing received emails | 包含接收邮件的future
     */
    public CompletableFuture<List<ReceivedEmail>> receiveAllAsync() {
        return CompletableFuture.supplyAsync(() -> {
            ensureConnected();
            return delegate.receiveAll();
        }, executor);
    }

    /**
     * Receive email by ID asynchronously
     * 异步通过ID接收邮件
     *
     * @param messageId the message ID | 消息ID
     * @return future containing received email or null | 包含接收邮件或null的future
     */
    public CompletableFuture<ReceivedEmail> receiveByIdAsync(String messageId) {
        return CompletableFuture.supplyAsync(() -> {
            ensureConnected();
            return delegate.receiveById(messageId);
        }, executor);
    }

    /**
     * Mark email as read asynchronously
     * 异步标记邮件为已读
     *
     * @param messageId the message ID | 消息ID
     * @return future that completes when done | 完成时的future
     */
    public CompletableFuture<Void> markAsReadAsync(String messageId) {
        return CompletableFuture.runAsync(() -> {
            ensureConnected();
            delegate.markAsRead(messageId);
        }, executor);
    }

    /**
     * Mark email as unread asynchronously
     * 异步标记邮件为未读
     *
     * @param messageId the message ID | 消息ID
     * @return future that completes when done | 完成时的future
     */
    public CompletableFuture<Void> markAsUnreadAsync(String messageId) {
        return CompletableFuture.runAsync(() -> {
            ensureConnected();
            delegate.markAsUnread(messageId);
        }, executor);
    }

    /**
     * Set email flagged status asynchronously
     * 异步设置邮件标记状态
     *
     * @param messageId the message ID | 消息ID
     * @param flagged   true to flag, false to unflag | true为标记，false为取消标记
     * @return future that completes when done | 完成时的future
     */
    public CompletableFuture<Void> setFlaggedAsync(String messageId, boolean flagged) {
        return CompletableFuture.runAsync(() -> {
            ensureConnected();
            delegate.setFlagged(messageId, flagged);
        }, executor);
    }

    /**
     * Delete email asynchronously
     * 异步删除邮件
     *
     * @param messageId the message ID | 消息ID
     * @return future that completes when done | 完成时的future
     */
    public CompletableFuture<Void> deleteAsync(String messageId) {
        return CompletableFuture.runAsync(() -> {
            ensureConnected();
            delegate.delete(messageId);
        }, executor);
    }

    /**
     * Move email to folder asynchronously
     * 异步移动邮件到文件夹
     *
     * @param messageId    the message ID | 消息ID
     * @param targetFolder the target folder | 目标文件夹
     * @return future that completes when done | 完成时的future
     */
    public CompletableFuture<Void> moveToFolderAsync(String messageId, String targetFolder) {
        return CompletableFuture.runAsync(() -> {
            ensureConnected();
            delegate.moveToFolder(messageId, targetFolder);
        }, executor);
    }

    /**
     * List folders asynchronously
     * 异步列出文件夹
     *
     * @return future containing folder names | 包含文件夹名称的future
     */
    public CompletableFuture<List<String>> listFoldersAsync() {
        return CompletableFuture.supplyAsync(() -> {
            ensureConnected();
            return delegate.listFolders();
        }, executor);
    }

    /**
     * Get message count asynchronously
     * 异步获取消息数量
     *
     * @param folder the folder name | 文件夹名称
     * @return future containing message count | 包含消息数量的future
     */
    public CompletableFuture<Integer> getMessageCountAsync(String folder) {
        return CompletableFuture.supplyAsync(() -> {
            ensureConnected();
            return delegate.getMessageCount(folder);
        }, executor);
    }

    /**
     * Get unread count asynchronously
     * 异步获取未读数量
     *
     * @param folder the folder name | 文件夹名称
     * @return future containing unread count | 包含未读数量的future
     */
    public CompletableFuture<Integer> getUnreadCountAsync(String folder) {
        return CompletableFuture.supplyAsync(() -> {
            ensureConnected();
            return delegate.getUnreadCount(folder);
        }, executor);
    }

    // ==================== Lifecycle Methods ====================

    /**
     * Connect to mail server
     * 连接到邮件服务器
     */
    public void connect() {
        delegate.connect();
    }

    /**
     * Connect to mail server asynchronously
     * 异步连接到邮件服务器
     *
     * @return future that completes when connected | 连接完成时的future
     */
    public CompletableFuture<Void> connectAsync() {
        return CompletableFuture.runAsync(delegate::connect, executor);
    }

    /**
     * Disconnect from mail server
     * 断开与邮件服务器的连接
     */
    public void disconnect() {
        delegate.disconnect();
    }

    /**
     * Check if connected
     * 检查是否已连接
     *
     * @return true if connected | 已连接返回true
     */
    public boolean isConnected() {
        return delegate.isConnected();
    }

    /**
     * Get the underlying receiver
     * 获取底层接收器
     *
     * @return the delegate receiver | 委托接收器
     */
    public EmailReceiver getDelegate() {
        return delegate;
    }

    @Override
    public void close() {
        if (ownsDelegate) {
            delegate.close();
        }
        if (ownsExecutor) {
            executor.shutdown();
        }
    }

    private void ensureConnected() {
        if (!delegate.isConnected()) {
            delegate.connect();
        }
    }

    // ==================== Builder ====================

    /**
     * Async Email Receiver Builder
     * 异步邮件接收器构建器
     */
    public static class Builder {
        private EmailReceiver receiver;
        private EmailReceiveConfig config;
        private ExecutorService executor;
        private boolean useVirtualThreads = true;

        /**
         * Set the underlying receiver
         * 设置底层接收器
         *
         * @param receiver the receiver | 接收器
         * @return this builder | 构建器
         */
        public Builder receiver(EmailReceiver receiver) {
            this.receiver = receiver;
            return this;
        }

        /**
         * Set the configuration (creates receiver automatically)
         * 设置配置（自动创建接收器）
         *
         * @param config the receive configuration | 接收配置
         * @return this builder | 构建器
         */
        public Builder config(EmailReceiveConfig config) {
            this.config = config;
            return this;
        }

        /**
         * Set the executor for async operations
         * 设置异步操作的执行器
         *
         * @param executor the executor | 执行器
         * @return this builder | 构建器
         */
        public Builder executor(ExecutorService executor) {
            this.executor = executor;
            return this;
        }

        /**
         * Use virtual threads (default: true)
         * 使用虚拟线程（默认：true）
         *
         * @param useVirtualThreads true to use virtual threads | true使用虚拟线程
         * @return this builder | 构建器
         */
        public Builder useVirtualThreads(boolean useVirtualThreads) {
            this.useVirtualThreads = useVirtualThreads;
            return this;
        }

        /**
         * Build the async receiver
         * 构建异步接收器
         *
         * @return the async receiver | 异步接收器
         */
        public AsyncEmailReceiver build() {
            EmailReceiver delegate = receiver;
            boolean ownsDelegate = false;

            if (delegate == null && config != null) {
                delegate = config.isImap()
                        ? new ImapEmailReceiver(config)
                        : new Pop3EmailReceiver(config);
                ownsDelegate = true;
            }

            if (delegate == null) {
                throw new IllegalStateException("Either receiver or config must be provided");
            }

            ExecutorService exec = executor;
            if (exec == null) {
                exec = useVirtualThreads
                        ? Executors.newVirtualThreadPerTaskExecutor()
                        : Executors.newCachedThreadPool();
            }

            return new AsyncEmailReceiver(delegate, exec, ownsDelegate, executor == null);
        }
    }
}
