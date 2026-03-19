package cloud.opencode.base.cache.internal.expiry;

import cloud.opencode.base.cache.spi.ExpiryPolicy;

import java.time.Duration;

/**
 * TTL Expiry Policy - Time To Live expiration strategy
 * TTL 过期策略 - 存活时间过期策略
 *
 * <p>Entries expire after a fixed duration since creation/update.</p>
 * <p>条目在创建/更新后固定时间后过期。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fixed expiration after write - 写入后固定过期</li>
 *   <li>Reset on update - 更新时重置</li>
 *   <li>No renewal on read - 读取不续期</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Expire 1 hour after write - 写入 1 小时后过期
 * ExpiryPolicy<String, User> policy = new TtlExpiryPolicy<>(Duration.ofHours(1));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
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
public final class TtlExpiryPolicy<K, V> implements ExpiryPolicy<K, V> {

    private final Duration ttl;

    public TtlExpiryPolicy(Duration ttl) {
        this.ttl = ttl;
    }

    @Override
    public Duration expireAfterCreate(K key, V value) {
        return ttl;
    }

    @Override
    public Duration expireAfterUpdate(K key, V value, Duration currentDuration) {
        return ttl; // Reset TTL on update
    }

    @Override
    public Duration expireAfterRead(K key, V value, Duration currentDuration) {
        return currentDuration; // No change on read
    }
}
