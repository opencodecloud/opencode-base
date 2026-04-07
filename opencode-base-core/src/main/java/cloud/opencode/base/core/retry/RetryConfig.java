package cloud.opencode.base.core.retry;

import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * RetryConfig - Immutable retry configuration record
 * 重试配置 - 不可变的重试配置记录
 *
 * <p>Encapsulates all retry parameters in an immutable record, suitable for sharing
 * across multiple {@link Retry} invocations. Sharing is safe when all callback fields
 * (retryOn, abortOn, onRetry, onSuccess, onExhausted, retryOnResult) are stateless.</p>
 * <p>将所有重试参数封装在不可变记录中，适合在多个 {@link Retry} 调用之间共享。
 * 当所有回调字段（retryOn、abortOn、onRetry、onSuccess、onExhausted、retryOnResult）无状态时，共享是安全的。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.3
 */
public record RetryConfig(
        int maxAttempts,
        BackoffStrategy backoff,
        @Nullable Duration maxDelay,
        @Nullable Duration timeout,
        Predicate<Throwable> retryOn,
        Predicate<Throwable> abortOn,
        Predicate<Object> retryOnResult,
        BiConsumer<Integer, Throwable> onRetry,
        Consumer<Object> onSuccess,
        Consumer<Throwable> onExhausted
) {

    /**
     * Default configuration: 3 attempts, 100ms fixed delay.
     * 默认配置：3次尝试，100毫秒固定延迟。
     */
    public static final RetryConfig DEFAULT = new RetryConfig(
            3, BackoffStrategy.fixed(Duration.ofMillis(100)),
            null, null, null, null, null, null, null, null);

    public RetryConfig {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be >= 1, got: " + maxAttempts);
        }
        if (backoff == null) {
            backoff = BackoffStrategy.fixed(Duration.ofMillis(100));
        }
        if (timeout != null && (timeout.isNegative() || timeout.isZero())) {
            throw new IllegalArgumentException("timeout must be positive");
        }
        if (retryOn == null) {
            retryOn = ex -> true;
        }
        if (abortOn == null) {
            abortOn = ex -> false;
        }
        if (retryOnResult == null) {
            retryOnResult = result -> false;
        }
        if (onRetry == null) {
            onRetry = (a, e) -> {};
        }
        if (onSuccess == null) {
            onSuccess = result -> {};
        }
        if (onExhausted == null) {
            onExhausted = ex -> {};
        }
    }

    /**
     * Backward-compatible constructor without timeout, retryOnResult, onSuccess, onExhausted.
     * 向后兼容的构造函数，不含 timeout、retryOnResult、onSuccess、onExhausted。
     */
    public RetryConfig(
            int maxAttempts,
            BackoffStrategy backoff,
            @Nullable Duration maxDelay,
            Predicate<Throwable> retryOn,
            Predicate<Throwable> abortOn,
            BiConsumer<Integer, Throwable> onRetry
    ) {
        this(maxAttempts, backoff, maxDelay, null, retryOn, abortOn, null, onRetry, null, null);
    }
}
