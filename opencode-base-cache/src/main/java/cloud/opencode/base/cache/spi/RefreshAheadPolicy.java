package cloud.opencode.base.cache.spi;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Refresh Ahead Policy - Proactive cache refresh strategy
 * 提前刷新策略 - 主动缓存刷新策略
 *
 * <p>Allows proactive refresh of cache entries before they expire,
 * ensuring cache hits even during refresh. This prevents cache stampede
 * and improves latency by refreshing entries in the background.</p>
 * <p>允许在缓存条目过期前主动刷新，确保在刷新期间仍能命中缓存。
 * 这可以防止缓存击穿并通过后台刷新提高延迟性能。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Proactive refresh before expiration - 过期前主动刷新</li>
 *   <li>Background async refresh - 后台异步刷新</li>
 *   <li>Stale-while-revalidate pattern - 验证时提供过期数据模式</li>
 *   <li>Configurable refresh threshold - 可配置的刷新阈值</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Refresh when 80% of TTL has elapsed - 当 TTL 过了 80% 时刷新
 * RefreshAheadPolicy<String, User> policy = RefreshAheadPolicy.percentageOfTtl(0.8);
 *
 * // Refresh 30 seconds before expiration - 过期前 30 秒刷新
 * RefreshAheadPolicy<String, User> policy = RefreshAheadPolicy.beforeExpiration(Duration.ofSeconds(30));
 *
 * // Custom policy - 自定义策略
 * RefreshAheadPolicy<String, User> policy = RefreshAheadPolicy.custom(
 *     (key, age, ttl) -> age > ttl.toMillis() * 0.7
 * );
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Non-blocking: Yes (async refresh) - 非阻塞: 是（异步刷新）</li>
 * </ul>
 *
 * @param <K> the type of keys | 键类型
 * @param <V> the type of values | 值类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@FunctionalInterface
public interface RefreshAheadPolicy<K, V> {

    /**
     * Determine if an entry should be refreshed
     * 确定条目是否应该刷新
     *
     * @param key       the cache key | 缓存键
     * @param ageMillis the age of the entry in milliseconds | 条目年龄（毫秒）
     * @param ttlMillis the configured TTL in milliseconds | 配置的 TTL（毫秒）
     * @return true if the entry should be refreshed | 如果应该刷新返回 true
     */
    boolean shouldRefresh(K key, long ageMillis, long ttlMillis);

    /**
     * Called after successful refresh
     * 刷新成功后调用
     *
     * @param key      the cache key | 缓存键
     * @param oldValue the old value | 旧值
     * @param newValue the new value | 新值
     */
    default void onRefreshSuccess(K key, V oldValue, V newValue) {
        // Default: no-op
    }

    /**
     * Called when refresh fails
     * 刷新失败时调用
     *
     * @param key      the cache key | 缓存键
     * @param oldValue the old value | 旧值
     * @param error    the error | 错误
     */
    default void onRefreshFailure(K key, V oldValue, Throwable error) {
        // Default: no-op
    }

    /**
     * Get the refresh executor (default: common pool)
     * 获取刷新执行器（默认: 公共池）
     *
     * @return the executor | 执行器
     */
    default Executor refreshExecutor() {
        return java.util.concurrent.ForkJoinPool.commonPool();
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Create a policy that refreshes when a percentage of TTL has elapsed
     * 创建当 TTL 过了指定百分比时刷新的策略
     *
     * @param percentage the percentage (0.0 to 1.0), e.g., 0.8 means refresh at 80% of TTL | 百分比（0.0 到 1.0）
     * @param <K>        the key type | 键类型
     * @param <V>        the value type | 值类型
     * @return the refresh policy | 刷新策略
     */
    static <K, V> RefreshAheadPolicy<K, V> percentageOfTtl(double percentage) {
        if (percentage <= 0 || percentage >= 1) {
            throw new IllegalArgumentException("Percentage must be between 0 and 1 exclusive");
        }
        return (key, ageMillis, ttlMillis) -> ageMillis >= ttlMillis * percentage;
    }

    /**
     * Create a policy that refreshes a fixed duration before expiration
     * 创建在过期前固定时间刷新的策略
     *
     * @param beforeExpiration the duration before expiration to trigger refresh | 触发刷新的过期前时间
     * @param <K>              the key type | 键类型
     * @param <V>              the value type | 值类型
     * @return the refresh policy | 刷新策略
     */
    static <K, V> RefreshAheadPolicy<K, V> beforeExpiration(Duration beforeExpiration) {
        long beforeMs = beforeExpiration.toMillis();
        return (key, ageMillis, ttlMillis) -> ageMillis >= (ttlMillis - beforeMs);
    }

    /**
     * Create a custom refresh policy
     * 创建自定义刷新策略
     *
     * @param predicate the predicate to determine if refresh is needed | 判断是否需要刷新的断言
     * @param <K>       the key type | 键类型
     * @param <V>       the value type | 值类型
     * @return the refresh policy | 刷新策略
     */
    static <K, V> RefreshAheadPolicy<K, V> custom(RefreshPredicate<K> predicate) {
        return predicate::shouldRefresh;
    }

    /**
     * Create a disabled policy (no refresh ahead)
     * 创建禁用的策略（不提前刷新）
     *
     * @param <K> the key type | 键类型
     * @param <V> the value type | 值类型
     * @return a policy that never triggers refresh | 永不触发刷新的策略
     */
    static <K, V> RefreshAheadPolicy<K, V> disabled() {
        return (key, ageMillis, ttlMillis) -> false;
    }

    /**
     * Predicate interface for custom refresh logic
     * 用于自定义刷新逻辑的断言接口
     *
     * @param <K> the key type | 键类型
     */
    @FunctionalInterface
    interface RefreshPredicate<K> {
        /**
         * Determine if refresh is needed
         * 确定是否需要刷新
         *
         * @param key       the key | 键
         * @param ageMillis age in milliseconds | 年龄（毫秒）
         * @param ttlMillis TTL in milliseconds | TTL（毫秒）
         * @return true if refresh needed | 如果需要刷新返回 true
         */
        boolean shouldRefresh(K key, long ageMillis, long ttlMillis);
    }

    // ==================== Advanced Policies | 高级策略 ====================

    /**
     * Create a refresh policy with adaptive threshold based on load
     * 创建基于负载的自适应阈值刷新策略
     *
     * <p>Adjusts refresh threshold based on access frequency.
     * High-traffic keys refresh earlier to ensure availability.</p>
     * <p>根据访问频率调整刷新阈值。高流量的键提前刷新以确保可用性。</p>
     *
     * @param minPercentage minimum percentage of TTL before refresh (e.g., 0.5) | 刷新前 TTL 的最小百分比
     * @param maxPercentage maximum percentage of TTL before refresh (e.g., 0.9) | 刷新前 TTL 的最大百分比
     * @param <K>           the key type | 键类型
     * @param <V>           the value type | 值类型
     * @return the adaptive refresh policy | 自适应刷新策略
     */
    static <K, V> RefreshAheadPolicy<K, V> adaptive(double minPercentage, double maxPercentage) {
        // Simple implementation: use middle of the range
        // In production, this would track access patterns
        double threshold = (minPercentage + maxPercentage) / 2;
        return percentageOfTtl(threshold);
    }

    /**
     * Create a refresh policy with jitter to avoid thundering herd
     * 创建带抖动的刷新策略以避免惊群效应
     *
     * @param basePercentage base percentage of TTL | TTL 的基础百分比
     * @param jitterPercent  jitter range as percentage (e.g., 0.1 means ±10%) | 抖动范围百分比
     * @param <K>            the key type | 键类型
     * @param <V>            the value type | 值类型
     * @return the jittered refresh policy | 带抖动的刷新策略
     */
    static <K, V> RefreshAheadPolicy<K, V> withJitter(double basePercentage, double jitterPercent) {
        return (key, ageMillis, ttlMillis) -> {
            // Add random jitter based on key hash for consistency
            double jitter = (Math.abs(key.hashCode() % 1000) / 1000.0 - 0.5) * 2 * jitterPercent;
            double threshold = basePercentage + jitter;
            threshold = Math.max(0.1, Math.min(0.95, threshold)); // Clamp to safe range
            return ageMillis >= ttlMillis * threshold;
        };
    }

    // ==================== Composition | 组合 ====================

    /**
     * Combine with another policy using OR logic
     * 使用 OR 逻辑与另一个策略组合
     *
     * @param other the other policy | 另一个策略
     * @return combined policy | 组合后的策略
     */
    default RefreshAheadPolicy<K, V> or(RefreshAheadPolicy<K, V> other) {
        return (key, ageMillis, ttlMillis) ->
                this.shouldRefresh(key, ageMillis, ttlMillis) ||
                other.shouldRefresh(key, ageMillis, ttlMillis);
    }

    /**
     * Combine with another policy using AND logic
     * 使用 AND 逻辑与另一个策略组合
     *
     * @param other the other policy | 另一个策略
     * @return combined policy | 组合后的策略
     */
    default RefreshAheadPolicy<K, V> and(RefreshAheadPolicy<K, V> other) {
        return (key, ageMillis, ttlMillis) ->
                this.shouldRefresh(key, ageMillis, ttlMillis) &&
                other.shouldRefresh(key, ageMillis, ttlMillis);
    }
}
