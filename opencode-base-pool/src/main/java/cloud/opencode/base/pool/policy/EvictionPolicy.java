package cloud.opencode.base.pool.policy;

import cloud.opencode.base.pool.PooledObject;

import java.time.Duration;
import java.util.List;

/**
 * EvictionPolicy - Sealed Eviction Policy Interface (JDK 25 Sealed)
 * EvictionPolicy - 密封驱逐策略接口 (JDK 25 Sealed)
 *
 * <p>Sealed interface for type-safe eviction policies. Uses JDK 25
 * sealed types and pattern matching for exhaustive handling.</p>
 * <p>用于类型安全驱逐策略的密封接口。使用JDK 25密封类型和模式匹配进行穷尽处理。</p>
 *
 * <p><strong>Implementations | 实现:</strong></p>
 * <ul>
 *   <li>IdleTime - Evict based on idle duration - 基于空闲时长驱逐</li>
 *   <li>LRU - Least Recently Used - 最近最少使用</li>
 *   <li>LFU - Least Frequently Used - 最不经常使用</li>
 *   <li>Composite - Combine multiple policies - 组合多个策略</li>
 * </ul>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Sealed interface with exhaustive pattern matching support - 密封接口，支持穷尽模式匹配</li>
 *   <li>Idle-time-based eviction with configurable duration - 基于空闲时间的驱逐，可配置时长</li>
 *   <li>LRU and LFU eviction strategies - LRU和LFU驱逐策略</li>
 *   <li>Composite policy combining multiple strategies with AND/OR logic - 组合策略，使用AND/OR逻辑组合多个策略</li>
 *   <li>Immutable record implementations for thread safety - 不可变记录实现，确保线程安全</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Pattern matching (exhaustive)
 * String desc = switch (policy) {
 *     case EvictionPolicy.IdleTime<T>(var maxIdle) ->
 *         "Evict if idle > " + maxIdle;
 *     case EvictionPolicy.LRU<T>(var max) ->
 *         "Keep max " + max + " objects";
 *     case EvictionPolicy.LFU<T>(var minCount) ->
 *         "Evict if borrowed < " + minCount;
 *     case EvictionPolicy.Composite<T>(var policies, var all) ->
 *         "Composite: " + (all ? "ALL" : "ANY");
 * };
 * }</pre>
 *
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable records) - 线程安全: 是（不可变记录）</li>
 * </ul>
 * @param <T> the pooled object type - 池化对象类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.0
 */
public sealed interface EvictionPolicy<T>
        permits EvictionPolicy.IdleTime, EvictionPolicy.LRU,
                EvictionPolicy.LFU, EvictionPolicy.Composite {

    /**
     * Determines if the object should be evicted.
     * 确定对象是否应被驱逐。
     *
     * @param obj     the pooled object - 池化对象
     * @param context the eviction context - 驱逐上下文
     * @return true if should evict - 如果应驱逐返回true
     */
    boolean evict(PooledObject<T> obj, EvictionContext context);

    /**
     * Idle time based eviction policy.
     * 基于空闲时间的驱逐策略。
     *
     * <p>Evicts objects that have been idle longer than the max idle time.</p>
     * <p>驱逐空闲时间超过最大空闲时间的对象。</p>
     *
     * @param <T>         the pooled object type - 池化对象类型
     * @param maxIdleTime the maximum idle time - 最大空闲时间
     */
    record IdleTime<T>(Duration maxIdleTime) implements EvictionPolicy<T> {
        @Override
        public boolean evict(PooledObject<T> obj, EvictionContext context) {
            return obj.getIdleDuration().compareTo(maxIdleTime) > 0;
        }
    }

    /**
     * Least Recently Used eviction policy.
     * 最近最少使用驱逐策略。
     *
     * <p>Evicts objects when the idle count exceeds maxObjects.</p>
     * <p>当空闲数量超过maxObjects时驱逐对象。</p>
     *
     * @param <T>        the pooled object type - 池化对象类型
     * @param maxObjects the maximum objects to keep - 保留的最大对象数
     */
    record LRU<T>(int maxObjects) implements EvictionPolicy<T> {
        @Override
        public boolean evict(PooledObject<T> obj, EvictionContext context) {
            return context.currentIdleCount() > maxObjects;
        }
    }

    /**
     * Least Frequently Used eviction policy.
     * 最不经常使用驱逐策略。
     *
     * <p>Evicts objects that have been borrowed fewer than minBorrowCount times.</p>
     * <p>驱逐借用次数少于minBorrowCount的对象。</p>
     *
     * @param <T>            the pooled object type - 池化对象类型
     * @param minBorrowCount the minimum borrow count - 最小借用次数
     */
    record LFU<T>(long minBorrowCount) implements EvictionPolicy<T> {
        @Override
        public boolean evict(PooledObject<T> obj, EvictionContext context) {
            return obj.getBorrowCount() < minBorrowCount;
        }
    }

    /**
     * Composite eviction policy.
     * 组合驱逐策略。
     *
     * <p>Combines multiple policies with AND or OR logic.</p>
     * <p>使用AND或OR逻辑组合多个策略。</p>
     *
     * @param <T>        the pooled object type - 池化对象类型
     * @param policies   the policies to combine - 要组合的策略
     * @param requireAll true for AND, false for OR - true表示AND，false表示OR
     */
    record Composite<T>(List<EvictionPolicy<T>> policies, boolean requireAll)
            implements EvictionPolicy<T> {
        @Override
        public boolean evict(PooledObject<T> obj, EvictionContext context) {
            if (requireAll) {
                return policies.stream().allMatch(p -> p.evict(obj, context));
            }
            return policies.stream().anyMatch(p -> p.evict(obj, context));
        }
    }
}
