package cloud.opencode.base.cache.protection;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Backoff Strategy - Enumeration of retry backoff strategies
 * 退避策略 - 重试退避策略枚举
 *
 * <p>Defines different strategies for calculating delay between retry attempts.</p>
 * <p>定义计算重试尝试之间延迟的不同策略。</p>
 *
 * <p><strong>Strategies | 策略:</strong></p>
 * <ul>
 *   <li>FIXED - Fixed interval between retries - 固定间隔重试</li>
 *   <li>LINEAR - Linear growth of delay - 延迟线性增长</li>
 *   <li>EXPONENTIAL - Exponential growth of delay - 延迟指数增长</li>
 *   <li>RANDOM - Random delay within bounds - 边界内随机延迟</li>
 *   <li>EXPONENTIAL_JITTER - Exponential with full jitter - 带完全抖动的指数退避</li>
 * </ul>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fixed interval backoff - 固定间隔退避</li>
 *   <li>Linear growth backoff - 线性增长退避</li>
 *   <li>Exponential backoff - 指数退避</li>
 *   <li>Exponential with jitter - 带抖动的指数退避</li>
 *   <li>Random delay backoff - 随机延迟退避</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Duration delay = BackoffStrategy.EXPONENTIAL.calculateDelay(
 *     3, Duration.ofMillis(100), Duration.ofSeconds(10), 2.0);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable enum) - 线程安全: 是（不可变枚举）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
public enum BackoffStrategy {

    /** Fixed delay strategy | 固定延迟策略 */
    FIXED {
        @Override
        public Duration calculateDelay(int attempt, Duration initialDelay, Duration maxDelay, double multiplier) {
            return initialDelay;
        }
    },

    /** Linear delay strategy | 线性延迟策略 */
    LINEAR {
        @Override
        public Duration calculateDelay(int attempt, Duration initialDelay, Duration maxDelay, double multiplier) {
            long baseMillis = initialDelay.toMillis();
            long maxMillis = maxDelay.toMillis();
            if (attempt != 0 && baseMillis > maxMillis / Math.max(attempt, 1)) {
                return maxDelay;
            }
            long delayMillis = baseMillis * attempt;
            return Duration.ofMillis(Math.min(delayMillis, maxMillis));
        }
    },

    /** Exponential delay strategy | 指数延迟策略 */
    EXPONENTIAL {
        @Override
        public Duration calculateDelay(int attempt, Duration initialDelay, Duration maxDelay, double multiplier) {
            double rawDelay = initialDelay.toMillis() * Math.pow(multiplier, attempt - 1);
            long maxMillis = maxDelay.toMillis();
            if (Double.isNaN(rawDelay) || Double.isInfinite(rawDelay) || rawDelay < 0 || rawDelay >= maxMillis) {
                return maxDelay;
            }
            return Duration.ofMillis(Math.min((long) rawDelay, maxMillis));
        }
    },

    /** Random delay strategy | 随机延迟策略 */
    RANDOM {
        @Override
        public Duration calculateDelay(int attempt, Duration initialDelay, Duration maxDelay, double multiplier) {
            long minMillis = initialDelay.toMillis();
            long maxMillis = maxDelay.toMillis();
            long randomDelay = ThreadLocalRandom.current().nextLong(minMillis, Math.max(minMillis + 1, maxMillis));
            return Duration.ofMillis(randomDelay);
        }
    },

    /** Exponential delay with jitter strategy | 带抖动的指数延迟策略 */
    EXPONENTIAL_JITTER {
        @Override
        public Duration calculateDelay(int attempt, Duration initialDelay, Duration maxDelay, double multiplier) {
            double rawDelay = initialDelay.toMillis() * Math.pow(multiplier, Math.max(attempt - 1, 0));
            long maxMillis = maxDelay.toMillis();
            long cappedMs;
            if (Double.isNaN(rawDelay) || Double.isInfinite(rawDelay) || rawDelay < 0 || rawDelay >= maxMillis) {
                cappedMs = maxMillis;
            } else {
                cappedMs = Math.min((long) rawDelay, maxMillis);
            }
            long jitteredMs = cappedMs > 0 ? ThreadLocalRandom.current().nextLong(cappedMs + 1) : 0;
            return Duration.ofMillis(jitteredMs);
        }
    };

    /**
     * Calculates the delay for the given attempt
     * 计算给定尝试的延迟
     *
     * @param attempt the current attempt number (1-based) | 当前尝试次数（从1开始）
     * @param initialDelay the initial delay | 初始延迟
     * @param maxDelay the maximum delay | 最大延迟
     * @param multiplier the multiplier for exponential backoff | 指数退避的倍数
     * @return the calculated delay | 计算的延迟
     */
    public abstract Duration calculateDelay(int attempt, Duration initialDelay, Duration maxDelay, double multiplier);
}
