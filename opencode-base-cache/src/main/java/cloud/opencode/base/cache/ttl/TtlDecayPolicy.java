package cloud.opencode.base.cache.ttl;

import java.time.Duration;
import java.util.Objects;

/**
 * TTL Decay Policy - TTL that decreases over time or access count
 * TTL 衰减策略 - 随时间或访问次数递减的 TTL
 *
 * <p>Implements TTL that decays based on various factors, useful for
 * gradually expiring cold data while keeping hot data longer.</p>
 * <p>实现基于各种因素衰减的 TTL，适用于逐渐过期冷数据同时保持热数据更长时间。</p>
 *
 * <p><strong>Decay Modes | 衰减模式:</strong></p>
 * <ul>
 *   <li>Linear decay - 线性衰减</li>
 *   <li>Exponential decay - 指数衰减</li>
 *   <li>Step decay - 阶梯衰减</li>
 *   <li>Access-count decay - 访问次数衰减</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Linear decay: starts at 1 hour, decays to 5 minutes over 10 accesses
 * TtlDecayPolicy decay = TtlDecayPolicy.linear(
 *     Duration.ofHours(1),     // initial TTL
 *     Duration.ofMinutes(5),   // minimum TTL
 *     10                       // decay after N accesses
 * );
 *
 * // Exponential decay: halves TTL each time until minimum
 * TtlDecayPolicy decay = TtlDecayPolicy.exponential(
 *     Duration.ofHours(1),     // initial TTL
 *     Duration.ofMinutes(5),   // minimum TTL
 *     0.5                      // decay factor
 * );
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Linear TTL decay - 线性 TTL 衰减</li>
 *   <li>Exponential TTL decay - 指数 TTL 衰减</li>
 *   <li>Step-based TTL decay - 阶梯 TTL 衰减</li>
 *   <li>Configurable minimum TTL - 可配置最小 TTL</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (sealed interface, immutable implementations) - 线程安全: 是（密封接口，不可变实现）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V2.0.5
 */
public sealed interface TtlDecayPolicy permits
        TtlDecayPolicy.LinearDecay,
        TtlDecayPolicy.ExponentialDecay,
        TtlDecayPolicy.StepDecay,
        TtlDecayPolicy.NoDecay {

    /**
     * Calculate decayed TTL based on access count
     * 根据访问次数计算衰减后的 TTL
     *
     * @param accessCount number of times entry has been accessed | 条目被访问的次数
     * @return decayed TTL | 衰减后的 TTL
     */
    Duration calculateDecayedTtl(long accessCount);

    /**
     * Get initial TTL (before any decay)
     * 获取初始 TTL（任何衰减之前）
     *
     * @return initial TTL | 初始 TTL
     */
    Duration initialTtl();

    /**
     * Get minimum TTL (floor)
     * 获取最小 TTL（下限）
     *
     * @return minimum TTL | 最小 TTL
     */
    Duration minimumTtl();

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Create no-decay policy (constant TTL)
     * 创建不衰减策略（恒定 TTL）
     *
     * @param ttl the constant TTL | 恒定 TTL
     * @return decay policy | 衰减策略
     */
    static TtlDecayPolicy none(Duration ttl) {
        return new NoDecay(ttl);
    }

    /**
     * Create linear decay policy
     * 创建线性衰减策略
     *
     * @param initialTtl initial TTL | 初始 TTL
     * @param minimumTtl minimum TTL (floor) | 最小 TTL（下限）
     * @param decaySteps number of steps to reach minimum | 达到最小值的步数
     * @return decay policy | 衰减策略
     */
    static TtlDecayPolicy linear(Duration initialTtl, Duration minimumTtl, int decaySteps) {
        return new LinearDecay(initialTtl, minimumTtl, decaySteps);
    }

    /**
     * Create exponential decay policy
     * 创建指数衰减策略
     *
     * @param initialTtl  initial TTL | 初始 TTL
     * @param minimumTtl  minimum TTL (floor) | 最小 TTL（下限）
     * @param decayFactor decay factor (0.0 to 1.0) | 衰减因子（0.0 到 1.0）
     * @return decay policy | 衰减策略
     */
    static TtlDecayPolicy exponential(Duration initialTtl, Duration minimumTtl, double decayFactor) {
        return new ExponentialDecay(initialTtl, minimumTtl, decayFactor);
    }

    /**
     * Create step decay policy
     * 创建阶梯衰减策略
     *
     * @param steps array of (accessThreshold, ttl) pairs | (访问阈值, TTL) 对数组
     * @return decay policy | 衰减策略
     */
    static TtlDecayPolicy step(Step... steps) {
        return new StepDecay(steps);
    }

    // ==================== Implementations | 实现 ====================

    /**
     * No decay - constant TTL
     * 不衰减 - 恒定 TTL
     */
    record NoDecay(Duration ttl) implements TtlDecayPolicy {
        @Override
        public Duration calculateDecayedTtl(long accessCount) {
            return ttl;
        }

        @Override
        public Duration initialTtl() {
            return ttl;
        }

        @Override
        public Duration minimumTtl() {
            return ttl;
        }
    }

    /**
     * Linear decay implementation
     * 线性衰减实现
     */
    record LinearDecay(Duration initialTtl, Duration minimumTtl, int decaySteps) implements TtlDecayPolicy {
        public LinearDecay {
            Objects.requireNonNull(initialTtl, "initialTtl cannot be null");
            Objects.requireNonNull(minimumTtl, "minimumTtl cannot be null");
            if (decaySteps <= 0) {
                throw new IllegalArgumentException("decaySteps must be positive");
            }
            if (initialTtl.compareTo(minimumTtl) < 0) {
                throw new IllegalArgumentException("initialTtl must be >= minimumTtl");
            }
        }

        @Override
        public Duration calculateDecayedTtl(long accessCount) {
            if (accessCount <= 0) {
                return initialTtl;
            }
            if (accessCount >= decaySteps) {
                return minimumTtl;
            }
            long initialMillis = initialTtl.toMillis();
            long minMillis = minimumTtl.toMillis();
            long decayPerStep = (initialMillis - minMillis) / decaySteps;
            long decayedMillis = initialMillis - (decayPerStep * accessCount);
            return Duration.ofMillis(Math.max(decayedMillis, minMillis));
        }
    }

    /**
     * Exponential decay implementation
     * 指数衰减实现
     */
    record ExponentialDecay(Duration initialTtl, Duration minimumTtl, double decayFactor) implements TtlDecayPolicy {
        public ExponentialDecay {
            Objects.requireNonNull(initialTtl, "initialTtl cannot be null");
            Objects.requireNonNull(minimumTtl, "minimumTtl cannot be null");
            if (decayFactor <= 0 || decayFactor >= 1) {
                throw new IllegalArgumentException("decayFactor must be between 0 and 1 (exclusive)");
            }
        }

        @Override
        public Duration calculateDecayedTtl(long accessCount) {
            if (accessCount <= 0) {
                return initialTtl;
            }
            long initialMillis = initialTtl.toMillis();
            long minMillis = minimumTtl.toMillis();
            double decayed = initialMillis * Math.pow(decayFactor, accessCount);
            return Duration.ofMillis(Math.max((long) decayed, minMillis));
        }
    }

    /**
     * Step decay implementation
     * 阶梯衰减实现
     */
    record StepDecay(Step[] steps) implements TtlDecayPolicy {
        public StepDecay {
            if (steps == null || steps.length == 0) {
                throw new IllegalArgumentException("steps cannot be empty");
            }
        }

        @Override
        public Duration calculateDecayedTtl(long accessCount) {
            Duration result = steps[0].ttl();
            for (Step step : steps) {
                if (accessCount >= step.threshold()) {
                    result = step.ttl();
                }
            }
            return result;
        }

        @Override
        public Duration initialTtl() {
            return steps[0].ttl();
        }

        @Override
        public Duration minimumTtl() {
            return steps[steps.length - 1].ttl();
        }
    }

    /**
     * Step definition for step decay
     * 阶梯衰减的步骤定义
     *
     * @param threshold access count threshold | 访问计数阈值
     * @param ttl       TTL at this step | 此步骤的 TTL
     */
    record Step(long threshold, Duration ttl) {
        public Step {
            Objects.requireNonNull(ttl, "ttl cannot be null");
            if (threshold < 0) {
                throw new IllegalArgumentException("threshold cannot be negative");
            }
        }

        /**
         * Create a step
         * 创建步骤
         *
         * @param threshold access threshold | 访问阈值
         * @param ttl       TTL at this step | 此步骤的 TTL
         * @return step | 步骤
         */
        public static Step of(long threshold, Duration ttl) {
            return new Step(threshold, ttl);
        }
    }
}
