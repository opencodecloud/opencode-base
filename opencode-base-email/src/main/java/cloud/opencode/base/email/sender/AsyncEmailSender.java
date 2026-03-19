package cloud.opencode.base.email.sender;

import cloud.opencode.base.email.Email;
import cloud.opencode.base.email.EmailConfig;
import cloud.opencode.base.email.exception.EmailException;
import cloud.opencode.base.email.internal.EmailSender;
import cloud.opencode.base.email.retry.EmailRetryExecutor;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

/**
 * Async Email Sender
 * 异步邮件发送器
 *
 * <p>Email sender with async support using virtual threads.</p>
 * <p>使用虚拟线程的异步邮件发送器。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Virtual threads (JDK 21+) - 虚拟线程</li>
 *   <li>CompletableFuture support - CompletableFuture支持</li>
 *   <li>Built-in retry mechanism - 内置重试机制</li>
 *   <li>Configurable thread pool - 可配置线程池</li>
 *   <li>Batch sending support - 批量发送支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * AsyncEmailSender sender = AsyncEmailSender.builder()
 *     .sender(new SmtpEmailSender(config))
 *     .build();
 *
 * // Single async send
 * CompletableFuture<Void> future = sender.sendAsync(email);
 * future.thenRun(() -> log.info("Sent successfully"));
 *
 * // Batch async send
 * List<CompletableFuture<Void>> futures = sender.sendAllAsync(emails);
 * CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
public class AsyncEmailSender implements EmailSender, AutoCloseable {

    private final EmailSender delegate;
    private final ExecutorService executor;
    private final EmailRetryExecutor retryExecutor;
    private final boolean ownsExecutor;

    /**
     * Create async sender with delegate sender
     * 使用代理发送器创建异步发送器
     *
     * @param delegate the delegate sender | 代理发送器
     */
    public AsyncEmailSender(EmailSender delegate) {
        this(delegate, createDefaultExecutor(), null, true);
    }

    /**
     * Create async sender with delegate and retry executor
     * 使用代理发送器和重试执行器创建异步发送器
     *
     * @param delegate      the delegate sender | 代理发送器
     * @param retryExecutor the retry executor | 重试执行器
     */
    public AsyncEmailSender(EmailSender delegate, EmailRetryExecutor retryExecutor) {
        this(delegate, createDefaultExecutor(), retryExecutor, true);
    }

    /**
     * Create async sender with all parameters
     * 使用所有参数创建异步发送器
     *
     * @param delegate      the delegate sender | 代理发送器
     * @param executor      the executor service | 执行器服务
     * @param retryExecutor the retry executor | 重试执行器
     * @param ownsExecutor  whether to shutdown executor on close | 关闭时是否关闭执行器
     */
    public AsyncEmailSender(EmailSender delegate, ExecutorService executor,
                            EmailRetryExecutor retryExecutor, boolean ownsExecutor) {
        this.delegate = delegate;
        this.executor = executor;
        this.retryExecutor = retryExecutor;
        this.ownsExecutor = ownsExecutor;
    }

    @Override
    public void send(Email email) {
        if (retryExecutor != null) {
            retryExecutor.executeWithRetry(email, delegate);
        } else {
            delegate.send(email);
        }
    }

    /**
     * Send email asynchronously
     * 异步发送邮件
     *
     * @param email the email to send | 要发送的邮件
     * @return future that completes when sent | 发送完成时的future
     */
    public CompletableFuture<Void> sendAsync(Email email) {
        return CompletableFuture.runAsync(() -> send(email), executor);
    }

    /**
     * Send email asynchronously with callback
     * 使用回调异步发送邮件
     *
     * @param email    the email to send | 要发送的邮件
     * @param callback callback on completion (email, exception or null) | 完成时的回调
     * @return future that completes when sent | 发送完成时的future
     */
    public CompletableFuture<Void> sendAsync(Email email,
                                             BiConsumer<Email, Throwable> callback) {
        return sendAsync(email)
                .whenComplete((v, e) -> {
                    if (callback != null) {
                        callback.accept(email, e);
                    }
                });
    }

    /**
     * Send multiple emails asynchronously
     * 异步发送多封邮件
     *
     * @param emails the emails to send | 要发送的邮件列表
     * @return list of futures | future列表
     */
    public List<CompletableFuture<Void>> sendAllAsync(List<Email> emails) {
        return emails.stream()
                .map(this::sendAsync)
                .toList();
    }

    /**
     * Send multiple emails and wait for all to complete
     * 发送多封邮件并等待全部完成
     *
     * @param emails the emails to send | 要发送的邮件列表
     * @return future that completes when all sent | 全部发送完成时的future
     */
    public CompletableFuture<Void> sendAllAndWait(List<Email> emails) {
        List<CompletableFuture<Void>> futures = sendAllAsync(emails);
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    @Override
    public void close() {
        if (ownsExecutor && executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        delegate.close();
    }

    /**
     * Create default executor using virtual threads
     */
    private static ExecutorService createDefaultExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    /**
     * Get delegate sender
     * 获取代理发送器
     *
     * @return the delegate sender | 代理发送器
     */
    public EmailSender getDelegate() {
        return delegate;
    }

    /**
     * Get executor service
     * 获取执行器服务
     *
     * @return the executor | 执行器
     */
    public ExecutorService getExecutor() {
        return executor;
    }

    /**
     * Create a builder
     * 创建构建器
     *
     * @return the builder | 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for AsyncEmailSender
     * AsyncEmailSender构建器
     */
    public static class Builder {
        private EmailSender sender;
        private ExecutorService executor;
        private EmailRetryExecutor retryExecutor;
        private int corePoolSize = 5;
        private int maxPoolSize = 20;
        private int queueCapacity = 1000;
        private String threadNamePrefix = "email-sender-";
        private boolean useVirtualThreads = true;

        /**
         * Set delegate sender
         * 设置代理发送器
         *
         * @param sender the sender | 发送器
         * @return this builder | 构建器
         */
        public Builder sender(EmailSender sender) {
            this.sender = sender;
            return this;
        }

        /**
         * Set delegate sender from config
         * 从配置设置代理发送器
         *
         * @param config the email config | 邮件配置
         * @return this builder | 构建器
         */
        public Builder config(EmailConfig config) {
            this.sender = new SmtpEmailSender(config);
            return this;
        }

        /**
         * Set custom executor
         * 设置自定义执行器
         *
         * @param executor the executor | 执行器
         * @return this builder | 构建器
         */
        public Builder executor(ExecutorService executor) {
            this.executor = executor;
            return this;
        }

        /**
         * Set retry executor
         * 设置重试执行器
         *
         * @param retryExecutor the retry executor | 重试执行器
         * @return this builder | 构建器
         */
        public Builder retryExecutor(EmailRetryExecutor retryExecutor) {
            this.retryExecutor = retryExecutor;
            return this;
        }

        /**
         * Set core pool size (for platform threads)
         * 设置核心线程池大小（用于平台线程）
         *
         * @param corePoolSize core pool size | 核心线程池大小
         * @return this builder | 构建器
         */
        public Builder corePoolSize(int corePoolSize) {
            this.corePoolSize = corePoolSize;
            return this;
        }

        /**
         * Set max pool size (for platform threads)
         * 设置最大线程池大小（用于平台线程）
         *
         * @param maxPoolSize max pool size | 最大线程池大小
         * @return this builder | 构建器
         */
        public Builder maxPoolSize(int maxPoolSize) {
            this.maxPoolSize = maxPoolSize;
            return this;
        }

        /**
         * Set queue capacity (for platform threads)
         * 设置队列容量（用于平台线程）
         *
         * @param queueCapacity queue capacity | 队列容量
         * @return this builder | 构建器
         */
        public Builder queueCapacity(int queueCapacity) {
            this.queueCapacity = queueCapacity;
            return this;
        }

        /**
         * Set thread name prefix
         * 设置线程名称前缀
         *
         * @param prefix the prefix | 前缀
         * @return this builder | 构建器
         */
        public Builder threadNamePrefix(String prefix) {
            this.threadNamePrefix = prefix;
            return this;
        }

        /**
         * Use virtual threads (default: true)
         * 使用虚拟线程（默认: true）
         *
         * @param useVirtualThreads true to use virtual threads | true使用虚拟线程
         * @return this builder | 构建器
         */
        public Builder useVirtualThreads(boolean useVirtualThreads) {
            this.useVirtualThreads = useVirtualThreads;
            return this;
        }

        /**
         * Build the async sender
         * 构建异步发送器
         *
         * @return the async sender | 异步发送器
         */
        public AsyncEmailSender build() {
            if (sender == null) {
                throw new EmailException("Sender is required");
            }

            boolean ownsExecutor = false;
            if (executor == null) {
                executor = createExecutor();
                ownsExecutor = true;
            }

            return new AsyncEmailSender(sender, executor, retryExecutor, ownsExecutor);
        }

        private ExecutorService createExecutor() {
            if (useVirtualThreads) {
                return Executors.newThreadPerTaskExecutor(
                        Thread.ofVirtual().name(threadNamePrefix, 0).factory()
                );
            } else {
                AtomicInteger counter = new AtomicInteger(0);
                return new ThreadPoolExecutor(
                        corePoolSize,
                        maxPoolSize,
                        60L, TimeUnit.SECONDS,
                        new LinkedBlockingQueue<>(queueCapacity),
                        r -> {
                            Thread t = new Thread(r);
                            t.setName(threadNamePrefix + counter.getAndIncrement());
                            t.setDaemon(true);
                            return t;
                        },
                        new ThreadPoolExecutor.CallerRunsPolicy()
                );
            }
        }
    }
}
