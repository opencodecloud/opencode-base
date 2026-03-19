package cloud.opencode.base.cache.internal.expiry;

import cloud.opencode.base.cache.spi.ExpiryPolicy;

import java.time.Duration;

/**
 * Combined Expiry Policy - TTL and TTI combined expiration strategy
 * 组合过期策略 - TTL 和 TTI 组合过期策略
 *
 * <p>Entries expire based on both creation time (TTL) and access time (TTI).</p>
 * <p>条目基于创建时间（TTL）和访问时间（TTI）过期。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Maximum lifetime (TTL) - 最大存活时间（TTL）</li>
 *   <li>Idle timeout (TTI) - 空闲超时（TTI）</li>
 *   <li>Entry expires when either condition is met - 任一条件满足时过期</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Max 1 hour, idle 30 minutes - 最大 1 小时，空闲 30 分钟
 * ExpiryPolicy<String, Session> policy = new CombinedExpiryPolicy<>(
 *     Duration.ofHours(1), Duration.ofMinutes(30));
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
public final class CombinedExpiryPolicy<K, V> implements ExpiryPolicy<K, V> {

    private final Duration ttl;
    private final Duration tti;

    public CombinedExpiryPolicy(Duration ttl, Duration tti) {
        this.ttl = ttl;
        this.tti = tti;
    }

    @Override
    public Duration expireAfterCreate(K key, V value) {
        // Use the shorter of TTL and TTI for initial expiration
        return min(ttl, tti);
    }

    @Override
    public Duration expireAfterUpdate(K key, V value, Duration currentDuration) {
        // Reset to TTI on update, but don't exceed original TTL
        return min(ttl, tti);
    }

    @Override
    public Duration expireAfterRead(K key, V value, Duration currentDuration) {
        // Reset idle timer but respect TTL
        // The current duration represents remaining TTL
        return min(currentDuration, tti);
    }

    private Duration min(Duration a, Duration b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.compareTo(b) <= 0 ? a : b;
    }
}
