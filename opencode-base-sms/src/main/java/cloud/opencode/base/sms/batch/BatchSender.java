package cloud.opencode.base.sms.batch;

import cloud.opencode.base.sms.message.SmsMessage;
import cloud.opencode.base.sms.message.SmsResult;
import cloud.opencode.base.sms.provider.SmsProvider;
import cloud.opencode.base.sms.validation.PhoneValidator;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Batch Sender
 * 批量发送器
 *
 * <p>Advanced batch SMS sender with parallelism and progress tracking.</p>
 * <p>支持并行和进度跟踪的高级批量短信发送器。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Configurable concurrency and batch size - 可配置并发数和批量大小</li>
 *   <li>Progress callback support - 进度回调支持</li>
 *   <li>Phone number validation before send - 发送前手机号验证</li>
 *   <li>Timeout control - 超时控制</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * BatchSender sender = BatchSender.builder(provider)
 *     .batchSize(50)
 *     .concurrency(5)
 *     .validatePhones(true)
 *     .build();
 * BatchResult result = sender.send(messages);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable configuration, thread-safe execution) - 线程安全: 是（不可变配置，线程安全执行）</li>
 *   <li>Null-safe: Yes (empty result for null/empty input) - 空值安全: 是（空输入返回空结果）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 */
public final class BatchSender {

    private static final int DEFAULT_BATCH_SIZE = 100;
    private static final int DEFAULT_CONCURRENCY = 10;
    private static final Duration DEFAULT_TIMEOUT = Duration.ofMinutes(5);

    private final SmsProvider provider;
    private final int batchSize;
    private final int concurrency;
    private final Duration timeout;
    private final boolean validatePhones;
    private final Consumer<BatchProgress> progressCallback;

    private BatchSender(Builder builder) {
        this.provider = builder.provider;
        this.batchSize = builder.batchSize;
        this.concurrency = builder.concurrency;
        this.timeout = builder.timeout;
        this.validatePhones = builder.validatePhones;
        this.progressCallback = builder.progressCallback;
    }

    /**
     * Create builder
     * 创建构建器
     *
     * @param provider the SMS provider | 短信提供商
     * @return the builder | 构建器
     */
    public static Builder builder(SmsProvider provider) {
        return new Builder(provider);
    }

    /**
     * Send batch messages
     * 批量发送消息
     *
     * @param messages the messages | 消息列表
     * @return the batch result | 批量结果
     */
    public BatchResult send(List<SmsMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return BatchResult.empty();
        }

        Instant startTime = Instant.now();
        List<SmsMessage> validMessages = validatePhones
            ? filterValidMessages(messages)
            : messages;

        List<SmsResult> results = sendWithConcurrency(validMessages);
        return BatchResult.of(results, startTime);
    }

    /**
     * Send batch messages asynchronously
     * 异步批量发送消息
     *
     * @param messages the messages | 消息列表
     * @return the future result | 异步结果
     */
    public CompletableFuture<BatchResult> sendAsync(List<SmsMessage> messages) {
        return CompletableFuture.supplyAsync(() -> send(messages));
    }

    /**
     * Send to multiple phones with same template
     * 使用相同模板发送给多个手机号
     *
     * @param phones the phone numbers | 手机号列表
     * @param templateMessage the template message | 模板消息
     * @return the batch result | 批量结果
     */
    public BatchResult sendToPhones(List<String> phones, SmsMessage templateMessage) {
        List<SmsMessage> messages = phones.stream()
            .map(phone -> SmsMessage.builder()
                .phoneNumber(phone)
                .content(templateMessage.content())
                .templateId(templateMessage.templateId())
                .variables(templateMessage.variables())
                .build())
            .toList();
        return send(messages);
    }

    /**
     * Filter valid messages
     * 过滤有效消息
     */
    private List<SmsMessage> filterValidMessages(List<SmsMessage> messages) {
        return messages.stream()
            .filter(m -> PhoneValidator.isValid(m.phoneNumber()))
            .toList();
    }

    /**
     * Send with concurrency control
     * 带并发控制的发送
     */
    private List<SmsResult> sendWithConcurrency(List<SmsMessage> messages) {
        int total = messages.size();
        AtomicInteger completed = new AtomicInteger(0);
        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger failure = new AtomicInteger(0);

        List<SmsResult> results = new ArrayList<>(total);

        Semaphore semaphore = new Semaphore(concurrency);

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<CompletableFuture<SmsResult>> futures = new ArrayList<>(total);

            // Process in batches
            for (int i = 0; i < total; i += batchSize) {
                int end = Math.min(i + batchSize, total);
                List<SmsMessage> batch = messages.subList(i, end);

                for (SmsMessage message : batch) {
                    CompletableFuture<SmsResult> future = CompletableFuture
                        .supplyAsync(() -> {
                            try {
                                semaphore.acquire();
                                try {
                                    return provider.send(message);
                                } finally {
                                    semaphore.release();
                                }
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                return SmsResult.failure(message.phoneNumber(), "INTERRUPTED", e.getMessage());
                            }
                        }, executor)
                        .orTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                        .handle((result, ex) -> {
                            if (ex != null) {
                                failure.incrementAndGet();
                                return SmsResult.failure(
                                    message.phoneNumber(),
                                    "TIMEOUT",
                                    ex.getMessage()
                                );
                            }
                            if (result.success()) {
                                success.incrementAndGet();
                            } else {
                                failure.incrementAndGet();
                            }
                            return result;
                        })
                        .whenComplete((result, ex) -> {
                            int done = completed.incrementAndGet();
                            reportProgress(done, total, success.get(), failure.get());
                        });

                    futures.add(future);
                }
            }

            // Wait for all futures
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .orTimeout(timeout.toMillis() * 2, TimeUnit.MILLISECONDS)
                .join();

            for (CompletableFuture<SmsResult> future : futures) {
                results.add(future.join());
            }
        }

        return results;
    }

    /**
     * Report progress
     * 报告进度
     */
    private void reportProgress(int completed, int total, int success, int failure) {
        if (progressCallback != null) {
            progressCallback.accept(new BatchProgress(completed, total, success, failure));
        }
    }

    /**
     * Batch Progress
     * 批量进度
     *
     * @param completed completed count | 已完成数
     * @param total total count | 总数
     * @param success success count | 成功数
     * @param failure failure count | 失败数
     */
    public record BatchProgress(int completed, int total, int success, int failure) {
        /**
         * Get progress percentage
         * 获取进度百分比
         *
         * @return the percentage | 百分比
         */
        public double getPercentage() {
            if (total == 0) return 0.0;
            return (double) completed / total * 100;
        }

        /**
         * Check if complete
         * 检查是否完成
         *
         * @return true if complete | 如果完成返回true
         */
        public boolean isComplete() {
            return completed >= total;
        }
    }

    /**
     * Builder for BatchSender
     * BatchSender构建器
     */
    public static class Builder {
        private final SmsProvider provider;
        private int batchSize = DEFAULT_BATCH_SIZE;
        private int concurrency = DEFAULT_CONCURRENCY;
        private Duration timeout = DEFAULT_TIMEOUT;
        private boolean validatePhones = true;
        private Consumer<BatchProgress> progressCallback;

        Builder(SmsProvider provider) {
            this.provider = provider;
        }

        /**
         * Set batch size
         * 设置批量大小
         *
         * @param batchSize the batch size | 批量大小
         * @return this builder | 此构建器
         */
        public Builder batchSize(int batchSize) {
            this.batchSize = batchSize;
            return this;
        }

        /**
         * Set concurrency
         * 设置并发数
         *
         * @param concurrency the concurrency | 并发数
         * @return this builder | 此构建器
         */
        public Builder concurrency(int concurrency) {
            this.concurrency = concurrency;
            return this;
        }

        /**
         * Set timeout
         * 设置超时
         *
         * @param timeout the timeout | 超时时间
         * @return this builder | 此构建器
         */
        public Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        /**
         * Set phone validation
         * 设置手机号验证
         *
         * @param validate whether to validate | 是否验证
         * @return this builder | 此构建器
         */
        public Builder validatePhones(boolean validate) {
            this.validatePhones = validate;
            return this;
        }

        /**
         * Set progress callback
         * 设置进度回调
         *
         * @param callback the callback | 回调函数
         * @return this builder | 此构建器
         */
        public Builder onProgress(Consumer<BatchProgress> callback) {
            this.progressCallback = callback;
            return this;
        }

        /**
         * Build batch sender
         * 构建批量发送器
         *
         * @return the batch sender | 批量发送器
         */
        public BatchSender build() {
            return new BatchSender(this);
        }
    }
}
