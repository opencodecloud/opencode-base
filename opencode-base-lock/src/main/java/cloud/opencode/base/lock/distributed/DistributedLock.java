package cloud.opencode.base.lock.distributed;

import cloud.opencode.base.lock.Lock;
import cloud.opencode.base.lock.spi.DistributedLockProvider;

import java.time.Duration;
import java.util.Optional;

/**
 * Distributed Lock Interface with TTL and Extension Support
 * 支持TTL和延长的分布式锁接口
 *
 * <p>Extends the base Lock interface with distributed lock specific features
 * such as TTL management, lock extension, and fencing token support.</p>
 * <p>扩展基础Lock接口，增加分布式锁特定功能，如TTL管理、锁延长和防护令牌支持。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>TTL-based lock expiration - 基于TTL的锁过期</li>
 *   <li>Lock extension/renewal - 锁延长/续期</li>
 *   <li>Remaining TTL query - 剩余TTL查询</li>
 *   <li>Expiration check - 过期检查</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create distributed lock | 创建分布式锁
 * DistributedLock lock = provider.createLock("order:12345", config);
 *
 * try (var guard = lock.lock()) {
 *     // Critical section | 临界区
 *     processOrder("12345");
 *
 *     // Extend lock if needed | 如需要可延长锁
 *     if (!lock.isExpired()) {
 *         lock.extend(Duration.ofSeconds(30));
 *     }
 * }
 *
 * // Check remaining TTL | 检查剩余TTL
 * lock.getRemainingTtl().ifPresent(ttl ->
 *     System.out.println("Remaining: " + ttl));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation dependent - 线程安全: 依赖实现</li>
 *   <li>Supports fencing tokens - 支持防护令牌</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see Lock
 * @see DistributedLockConfig
 * @see DistributedLockProvider
 * @since JDK 25, opencode-base-lock V1.0.0
 */
public interface DistributedLock extends Lock<String> {

    /**
     * Gets the unique name of this distributed lock
     * 获取此分布式锁的唯一名称
     *
     * @return the lock name | 锁名称
     */
    String getName();

    /**
     * Gets the lock value/token for ownership verification
     * 获取用于所有权验证的锁值/令牌
     *
     * @return the lock value | 锁值
     */
    String getValue();

    /**
     * Extends the lock TTL by the specified duration
     * 将锁的TTL延长指定时长
     *
     * @param duration the duration to extend | 要延长的时长
     * @return true if extended successfully | true表示延长成功
     */
    boolean extend(Duration duration);

    /**
     * Gets the remaining time-to-live of the lock
     * 获取锁的剩余生存时间
     *
     * @return remaining TTL, or empty if lock not held | 剩余TTL，如果未持有锁则为空
     */
    Optional<Duration> getRemainingTtl();

    /**
     * Checks if the lock has expired
     * 检查锁是否已过期
     *
     * @return true if expired | true表示已过期
     */
    boolean isExpired();
}
