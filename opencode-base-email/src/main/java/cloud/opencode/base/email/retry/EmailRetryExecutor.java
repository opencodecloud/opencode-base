package cloud.opencode.base.email.retry;

import cloud.opencode.base.email.Email;
import cloud.opencode.base.email.exception.EmailException;
import cloud.opencode.base.email.internal.EmailSender;

import java.time.Duration;
import java.util.function.Consumer;

/**
 * Email Retry Executor
 * 邮件发送重试执行器
 *
 * <p>Executes email sending with automatic retry on failures.</p>
 * <p>在发送失败时自动重试的邮件发送执行器。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Configurable retry count - 可配置重试次数</li>
 *   <li>Exponential backoff - 指数退避</li>
 *   <li>Retry callback support - 重试回调支持</li>
 *   <li>Only retry retryable errors - 仅重试可重试错误</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * EmailRetryExecutor executor = new EmailRetryExecutor(
 *     3,                          // max retries
 *     Duration.ofSeconds(1),      // initial delay
 *     2.0                         // backoff multiplier
 * );
 *
 * executor.executeWithRetry(email, sender);
 *
 * // With callback
 * executor.executeWithRetry(email, sender, (attempt, e) -> {
 *     log.warn("Retry attempt {}: {}", attempt, e.getMessage());
 * });
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
public class EmailRetryExecutor {

    private final int maxRetries;
    private final Duration initialDelay;
    private final double backoffMultiplier;
    private final Duration maxDelay;

    /**
     * Create retry executor with default settings
     * 使用默认设置创建重试执行器
     */
    public EmailRetryExecutor() {
        this(3, Duration.ofSeconds(1), 2.0);
    }

    /**
     * Create retry executor with custom settings
     * 使用自定义设置创建重试执行器
     *
     * @param maxRetries        max retry attempts | 最大重试次数
     * @param initialDelay      initial delay between retries | 重试间隔初始延迟
     * @param backoffMultiplier backoff multiplier | 退避因子
     */
    public EmailRetryExecutor(int maxRetries, Duration initialDelay, double backoffMultiplier) {
        this(maxRetries, initialDelay, backoffMultiplier, Duration.ofMinutes(5));
    }

    /**
     * Create retry executor with all settings
     * 使用所有设置创建重试执行器
     *
     * @param maxRetries        max retry attempts | 最大重试次数
     * @param initialDelay      initial delay between retries | 重试间隔初始延迟
     * @param backoffMultiplier backoff multiplier | 退避因子
     * @param maxDelay          max delay cap | 最大延迟上限
     */
    public EmailRetryExecutor(int maxRetries, Duration initialDelay,
                              double backoffMultiplier, Duration maxDelay) {
        this.maxRetries = maxRetries;
        this.initialDelay = initialDelay;
        this.backoffMultiplier = backoffMultiplier;
        this.maxDelay = maxDelay;
    }

    /**
     * Execute email sending with retry
     * 执行带重试的邮件发送
     *
     * @param email  the email to send | 要发送的邮件
     * @param sender the email sender | 邮件发送器
     * @throws EmailException if all retries fail | 所有重试失败时抛出
     */
    public void executeWithRetry(Email email, EmailSender sender) {
        executeWithRetry(email, sender, null);
    }

    /**
     * Execute email sending with retry and callback
     * 执行带重试和回调的邮件发送
     *
     * @param email         the email to send | 要发送的邮件
     * @param sender        the email sender | 邮件发送器
     * @param retryCallback callback on each retry attempt | 每次重试时的回调
     * @throws EmailException if all retries fail | 所有重试失败时抛出
     */
    public void executeWithRetry(Email email, EmailSender sender,
                                 RetryCallback retryCallback) {
        int attempt = 0;
        Duration delay = initialDelay;
        EmailException lastException = null;

        while (attempt <= maxRetries) {
            try {
                sender.send(email);
                return; // Success
            } catch (EmailException e) {
                lastException = e;
                attempt++;

                // Check if retryable
                if (!e.isRetryable()) {
                    throw e;
                }

                // Check if max retries reached
                if (attempt > maxRetries) {
                    throw e;
                }

                // Invoke callback
                if (retryCallback != null) {
                    retryCallback.onRetry(attempt, e);
                }

                // Sleep before retry
                sleep(delay);

                // Calculate next delay with exponential backoff
                delay = calculateNextDelay(delay);
            }
        }

        // Should not reach here, but just in case
        if (lastException != null) {
            throw lastException;
        }
    }

    /**
     * Calculate next delay with exponential backoff
     */
    private Duration calculateNextDelay(Duration currentDelay) {
        long nextMillis = (long) (currentDelay.toMillis() * backoffMultiplier);
        return Duration.ofMillis(Math.min(nextMillis, maxDelay.toMillis()));
    }

    /**
     * Sleep for specified duration
     */
    private void sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EmailException("Retry interrupted", e);
        }
    }

    /**
     * Get max retries
     * 获取最大重试次数
     *
     * @return max retries | 最大重试次数
     */
    public int getMaxRetries() {
        return maxRetries;
    }

    /**
     * Get initial delay
     * 获取初始延迟
     *
     * @return initial delay | 初始延迟
     */
    public Duration getInitialDelay() {
        return initialDelay;
    }

    /**
     * Get backoff multiplier
     * 获取退避因子
     *
     * @return backoff multiplier | 退避因子
     */
    public double getBackoffMultiplier() {
        return backoffMultiplier;
    }

    /**
     * Get max delay
     * 获取最大延迟
     *
     * @return max delay | 最大延迟
     */
    public Duration getMaxDelay() {
        return maxDelay;
    }

    /**
     * Retry callback interface
     * 重试回调接口
     */
    @FunctionalInterface
    public interface RetryCallback {
        /**
         * Called on each retry attempt
         * 每次重试时调用
         *
         * @param attempt   the attempt number (1-based) | 尝试次数（从1开始）
         * @param exception the exception that caused the retry | 导致重试的异常
         */
        void onRetry(int attempt, EmailException exception);
    }

    /**
     * Create a builder for EmailRetryExecutor
     * 创建EmailRetryExecutor构建器
     *
     * @return the builder | 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for EmailRetryExecutor
     * EmailRetryExecutor构建器
     */
    public static class Builder {
        private int maxRetries = 3;
        private Duration initialDelay = Duration.ofSeconds(1);
        private double backoffMultiplier = 2.0;
        private Duration maxDelay = Duration.ofMinutes(5);

        /**
         * Set max retries
         * 设置最大重试次数
         *
         * @param maxRetries max retry attempts | 最大重试次数
         * @return this builder | 构建器
         */
        public Builder maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        /**
         * Set initial delay
         * 设置初始延迟
         *
         * @param initialDelay initial delay | 初始延迟
         * @return this builder | 构建器
         */
        public Builder initialDelay(Duration initialDelay) {
            this.initialDelay = initialDelay;
            return this;
        }

        /**
         * Set backoff multiplier
         * 设置退避因子
         *
         * @param multiplier backoff multiplier | 退避因子
         * @return this builder | 构建器
         */
        public Builder backoffMultiplier(double multiplier) {
            this.backoffMultiplier = multiplier;
            return this;
        }

        /**
         * Set max delay cap
         * 设置最大延迟上限
         *
         * @param maxDelay max delay | 最大延迟
         * @return this builder | 构建器
         */
        public Builder maxDelay(Duration maxDelay) {
            this.maxDelay = maxDelay;
            return this;
        }

        /**
         * Build the executor
         * 构建执行器
         *
         * @return the executor | 执行器
         */
        public EmailRetryExecutor build() {
            return new EmailRetryExecutor(maxRetries, initialDelay, backoffMultiplier, maxDelay);
        }
    }
}
