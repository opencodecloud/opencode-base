package cloud.opencode.base.cache.spi;

import java.time.Duration;

/**
 * Expiry Policy SPI - Cache entry expiration strategy interface
 * 过期策略 SPI - 缓存条目过期策略接口
 *
 * <p>Provides interface for determining when cache entries should expire.</p>
 * <p>提供确定缓存条目何时过期的接口。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>TTL - Time To Live (expire after write) - 存活时间（写入后过期）</li>
 *   <li>TTI - Time To Idle (expire after access) - 空闲时间（访问后过期）</li>
 *   <li>Combined - Both TTL and TTI - 组合策略（同时支持 TTL 和 TTI）</li>
 *   <li>Custom - User-defined expiration - 自定义过期策略</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // TTL - expire 1 hour after write - 写入 1 小时后过期
 * ExpiryPolicy<String, User> ttl = ExpiryPolicy.ttl(Duration.ofHours(1));
 *
 * // TTI - expire 30 minutes after last access - 最后访问 30 分钟后过期
 * ExpiryPolicy<String, User> tti = ExpiryPolicy.tti(Duration.ofMinutes(30));
 *
 * // Combined - 组合策略
 * ExpiryPolicy<String, User> combined = ExpiryPolicy.combined(
 *     Duration.ofHours(1), Duration.ofMinutes(30));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable policies) - 线程安全: 是（不可变策略）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
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
public interface ExpiryPolicy<K, V> {

    /**
     * Duration indicating entry should never expire
     * 表示条目永不过期的时长
     */
    Duration INFINITE = Duration.ofNanos(Long.MAX_VALUE);

    /**
     * Calculate expiration duration after create
     * 计算创建后的过期时长
     *
     * @param key   the key | 键
     * @param value the value | 值
     * @return expiration duration | 过期时长
     */
    Duration expireAfterCreate(K key, V value);

    /**
     * Calculate expiration duration after update
     * 计算更新后的过期时长
     *
     * @param key             the key | 键
     * @param value           the value | 值
     * @param currentDuration current remaining duration | 当前剩余时长
     * @return new expiration duration | 新过期时长
     */
    Duration expireAfterUpdate(K key, V value, Duration currentDuration);

    /**
     * Calculate expiration duration after read
     * 计算读取后的过期时长
     *
     * @param key             the key | 键
     * @param value           the value | 值
     * @param currentDuration current remaining duration | 当前剩余时长
     * @return new expiration duration | 新过期时长
     */
    Duration expireAfterRead(K key, V value, Duration currentDuration);

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Create TTL (Time To Live) policy - expire after write
     * 创建 TTL（存活时间）策略 - 写入后过期
     *
     * @param duration expiration duration | 过期时长
     * @param <K>      key type | 键类型
     * @param <V>      value type | 值类型
     * @return TTL policy | TTL 策略
     */
    static <K, V> ExpiryPolicy<K, V> ttl(Duration duration) {
        return new cloud.opencode.base.cache.internal.expiry.TtlExpiryPolicy<>(duration);
    }

    /**
     * Create TTI (Time To Idle) policy - expire after access
     * 创建 TTI（空闲时间）策略 - 访问后过期
     *
     * @param duration expiration duration | 过期时长
     * @param <K>      key type | 键类型
     * @param <V>      value type | 值类型
     * @return TTI policy | TTI 策略
     */
    static <K, V> ExpiryPolicy<K, V> tti(Duration duration) {
        return new cloud.opencode.base.cache.internal.expiry.TtiExpiryPolicy<>(duration);
    }

    /**
     * Create combined TTL and TTI policy
     * 创建组合 TTL 和 TTI 策略
     *
     * @param ttl TTL duration | TTL 时长
     * @param tti TTI duration | TTI 时长
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return combined policy | 组合策略
     */
    static <K, V> ExpiryPolicy<K, V> combined(Duration ttl, Duration tti) {
        return new cloud.opencode.base.cache.internal.expiry.CombinedExpiryPolicy<>(ttl, tti);
    }

    /**
     * Create policy that never expires
     * 创建永不过期的策略
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return eternal policy | 永不过期策略
     */
    static <K, V> ExpiryPolicy<K, V> eternal() {
        return new ExpiryPolicy<>() {
            @Override
            public Duration expireAfterCreate(K key, V value) {
                return INFINITE;
            }

            @Override
            public Duration expireAfterUpdate(K key, V value, Duration currentDuration) {
                return INFINITE;
            }

            @Override
            public Duration expireAfterRead(K key, V value, Duration currentDuration) {
                return INFINITE;
            }
        };
    }
}
