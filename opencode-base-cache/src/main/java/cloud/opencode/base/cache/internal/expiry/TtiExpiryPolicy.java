package cloud.opencode.base.cache.internal.expiry;

import cloud.opencode.base.cache.spi.ExpiryPolicy;

import java.time.Duration;

/**
 * TTI Expiry Policy - Time To Idle expiration strategy
 * TTI 过期策略 - 空闲时间过期策略
 *
 * <p>Entries expire after a fixed duration since last access.</p>
 * <p>条目在最后访问后固定时间后过期。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Expiration after last access - 最后访问后过期</li>
 *   <li>Renewal on read - 读取时续期</li>
 *   <li>Renewal on update - 更新时续期</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Expire 30 minutes after last access - 最后访问 30 分钟后过期
 * ExpiryPolicy<String, Session> policy = new TtiExpiryPolicy<>(Duration.ofMinutes(30));
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
public final class TtiExpiryPolicy<K, V> implements ExpiryPolicy<K, V> {

    private final Duration tti;

    /**
     * TtiExpiryPolicy | TtiExpiryPolicy
     * @param tti the tti | tti
     */
    public TtiExpiryPolicy(Duration tti) {
        this.tti = tti;
    }

    @Override
    public Duration expireAfterCreate(K key, V value) {
        return tti;
    }

    @Override
    public Duration expireAfterUpdate(K key, V value, Duration currentDuration) {
        return tti; // Reset TTI on update
    }

    @Override
    public Duration expireAfterRead(K key, V value, Duration currentDuration) {
        return tti; // Reset TTI on read
    }
}
