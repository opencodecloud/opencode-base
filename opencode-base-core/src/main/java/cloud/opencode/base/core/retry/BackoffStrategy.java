package cloud.opencode.base.core.retry;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * BackoffStrategy - Defines delay calculation between retry attempts
 * 退避策略 - 定义重试之间的延迟计算方式
 *
 * <p>Sealed interface with built-in implementations: Fixed, Exponential,
 * ExponentialWithJitter, and Fibonacci.</p>
 * <p>密封接口，内置实现：固定延迟、指数退避、带抖动的指数退避、斐波那契退避。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.3
 */
public sealed interface BackoffStrategy permits BackoffStrategy.Fixed, BackoffStrategy.Exponential,
        BackoffStrategy.ExponentialWithJitter, BackoffStrategy.Fibonacci {

    /**
     * Calculate the delay for the given attempt number.
     * 计算给定重试次数的延迟。
     *
     * @param attempt attempt number, starting from 1 - 重试次数，从1开始
     * @return the delay duration - 延迟时长
     */
    Duration delay(int attempt);

    /**
     * Create a fixed backoff strategy.
     * 创建固定退避策略。
     *
     * @param delay the fixed delay between retries - 重试之间的固定延迟
     * @return a fixed backoff strategy - 固定退避策略
     */
    static BackoffStrategy fixed(Duration delay) {
        return new Fixed(delay);
    }

    /**
     * Create an exponential backoff strategy.
     * 创建指数退避策略。
     *
     * @param initialDelay the initial delay - 初始延迟
     * @param multiplier   the multiplier applied per attempt - 每次重试的乘数
     * @return an exponential backoff strategy - 指数退避策略
     */
    static BackoffStrategy exponential(Duration initialDelay, double multiplier) {
        return new Exponential(initialDelay, multiplier);
    }

    /**
     * Create an exponential backoff strategy with jitter.
     * 创建带抖动的指数退避策略。
     *
     * @param initialDelay the initial delay - 初始延迟
     * @param multiplier   the multiplier applied per attempt - 每次重试的乘数
     * @param jitterFactor jitter factor in [0.0, 1.0] - 抖动因子，范围 [0.0, 1.0]
     * @return an exponential-with-jitter backoff strategy - 带抖动的指数退避策略
     */
    static BackoffStrategy exponentialWithJitter(Duration initialDelay, double multiplier, double jitterFactor) {
        return new ExponentialWithJitter(initialDelay, multiplier, jitterFactor);
    }

    /**
     * Create a Fibonacci backoff strategy.
     * 创建斐波那契退避策略。
     *
     * @param initialDelay the initial delay (used as the first two Fibonacci values) - 初始延迟（作为前两个斐波那契值）
     * @return a Fibonacci backoff strategy - 斐波那契退避策略
     */
    static BackoffStrategy fibonacci(Duration initialDelay) {
        return new Fibonacci(initialDelay);
    }

    /**
     * Fixed backoff - returns the same delay for every attempt.
     * 固定退避 - 每次重试返回相同的延迟。
     *
     * @param delay the fixed delay - 固定延迟
     */
    record Fixed(Duration delay) implements BackoffStrategy {
        public Fixed {
            Objects.requireNonNull(delay, "delay must not be null");
            if (delay.isNegative() || delay.isZero()) {
                throw new IllegalArgumentException("delay must be positive");
            }
        }

        @Override
        public Duration delay(int attempt) {
            return delay;
        }
    }

    /**
     * Exponential backoff - delay grows by a multiplier each attempt.
     * 指数退避 - 延迟按乘数逐次增长。
     *
     * @param initialDelay the initial delay - 初始延迟
     * @param multiplier   the multiplier (must be &gt;= 1.0) - 乘数（必须 &gt;= 1.0）
     */
    record Exponential(Duration initialDelay, double multiplier) implements BackoffStrategy {
        public Exponential {
            Objects.requireNonNull(initialDelay, "initialDelay must not be null");
            if (initialDelay.isNegative() || initialDelay.isZero()) {
                throw new IllegalArgumentException("initialDelay must be positive");
            }
            if (multiplier < 1.0) {
                throw new IllegalArgumentException("multiplier must be >= 1.0, got: " + multiplier);
            }
        }

        @Override
        public Duration delay(int attempt) {
            long millis = initialDelay.toMillis();
            double factor = Math.pow(multiplier, attempt - 1);
            long result = (long) Math.min(millis * factor, Long.MAX_VALUE / 2);
            return Duration.ofMillis(result);
        }
    }

    /**
     * Exponential backoff with jitter - adds randomness to prevent thundering herd.
     * 带抖动的指数退避 - 添加随机性以防止惊群效应。
     *
     * @param initialDelay the initial delay - 初始延迟
     * @param multiplier   the multiplier (must be &gt;= 1.0) - 乘数（必须 &gt;= 1.0）
     * @param jitterFactor jitter factor in [0.0, 1.0] - 抖动因子，范围 [0.0, 1.0]
     */
    record ExponentialWithJitter(Duration initialDelay, double multiplier, double jitterFactor) implements BackoffStrategy {
        public ExponentialWithJitter {
            Objects.requireNonNull(initialDelay, "initialDelay must not be null");
            if (initialDelay.isNegative() || initialDelay.isZero()) {
                throw new IllegalArgumentException("initialDelay must be positive");
            }
            if (multiplier < 1.0) {
                throw new IllegalArgumentException("multiplier must be >= 1.0, got: " + multiplier);
            }
            if (jitterFactor < 0.0 || jitterFactor > 1.0) {
                throw new IllegalArgumentException("jitterFactor must be in [0.0, 1.0], got: " + jitterFactor);
            }
        }

        @Override
        public Duration delay(int attempt) {
            long millis = initialDelay.toMillis();
            double factor = Math.pow(multiplier, attempt - 1);
            long baseMillis = (long) Math.min(millis * factor, Long.MAX_VALUE / 2);
            long jitterRange = (long) Math.min((double) baseMillis * jitterFactor, Long.MAX_VALUE / 2);
            if (jitterRange <= 0) {
                return Duration.ofMillis(baseMillis);
            }
            long jitter = ThreadLocalRandom.current().nextLong(-jitterRange, jitterRange + 1);
            return Duration.ofMillis(Math.max(1, baseMillis + jitter));
        }
    }

    /**
     * Fibonacci backoff - delay follows the Fibonacci sequence.
     * 斐波那契退避 - 延迟按斐波那契数列增长。
     *
     * @param initialDelay the initial delay (first two Fibonacci values) - 初始延迟（前两个斐波那契值）
     */
    record Fibonacci(Duration initialDelay) implements BackoffStrategy {
        public Fibonacci {
            Objects.requireNonNull(initialDelay, "initialDelay must not be null");
            if (initialDelay.isNegative() || initialDelay.isZero()) {
                throw new IllegalArgumentException("initialDelay must be positive");
            }
        }

        @Override
        public Duration delay(int attempt) {
            long millis = initialDelay.toMillis();
            long a = millis, b = millis;
            for (int i = 2; i < attempt; i++) {
                long next = (b > Long.MAX_VALUE - a) ? Long.MAX_VALUE : a + b;
                a = b;
                b = next;
                if (b == Long.MAX_VALUE) {
                    return Duration.ofMillis(Long.MAX_VALUE);
                }
            }
            return Duration.ofMillis(attempt <= 1 ? a : b);
        }
    }
}
