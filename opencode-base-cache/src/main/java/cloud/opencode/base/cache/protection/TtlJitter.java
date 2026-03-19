package cloud.opencode.base.cache.protection;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * TTL Jitter — Prevents cache avalanche by randomizing expiration times.
 * TTL 抖动 — 通过随机化过期时间防止缓存雪崩。
 *
 * <p>When many cache entries share the same TTL, they expire simultaneously and
 * cause a thundering-herd surge to the backend. This utility applies random jitter
 * or deterministic stagger offsets to spread expirations over a configurable window,
 * and provides a mutex-based refresh gate to ensure only one thread reloads a stale entry.</p>
 * <p>当许多缓存条目共享相同的 TTL 时，它们会同时过期并导致对后端的惊群涌入。
 * 此工具通过添加随机抖动或确定性错开偏移来将过期分散到可配置的时间窗口，
 * 并提供基于互斥锁的刷新门控，确保只有一个线程重新加载过期条目。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Random TTL jitter to distribute expiration times - 随机 TTL 抖动以分散过期时间</li>
 *   <li>Staggered TTL for batch-loaded entries - 批量加载条目的错开 TTL</li>
 *   <li>Mutex-based refresh to prevent thundering herd - 基于互斥锁的刷新以防止惊群效应</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * TtlJitter jitter = TtlJitter.of(Duration.ofSeconds(30)); // up to 30s extra
 *
 * // Randomize a single entry TTL
 * Duration ttl = jitter.apply(Duration.ofMinutes(10));
 *
 * // Stagger TTLs across a batch of 100 entries
 * for (int i = 0; i < 100; i++) {
 *     Duration ttl = jitter.stagger(Duration.ofMinutes(10), 100, i);
 * }
 *
 * // Mutex-protected refresh (only one thread reloads)
 * String value = jitter.mutexRefresh(() -> loadFromDB(), staleValue);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>ThreadLocalRandom for jitter — no shared state contention - 使用 ThreadLocalRandom 无争用</li>
 *   <li>ReentrantLock.tryLock() for non-blocking mutex refresh - 非阻塞互斥刷新</li>
 *   <li>O(1) for all operations - 所有操作 O(1)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (ThreadLocalRandom + ReentrantLock) - 线程安全: 是</li>
 *   <li>Null-safe: Partial (null baseTtl is returned unchanged) - 空值安全: 部分（null baseTtl 原样返回）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
public final class TtlJitter {

    private final Duration jitterRange;
    private final ReentrantLock refreshLock = new ReentrantLock();

    private TtlJitter(Duration jitterRange) {
        if (jitterRange == null || jitterRange.isNegative()) {
            throw new IllegalArgumentException("jitterRange must not be null or negative");
        }
        this.jitterRange = jitterRange;
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates a TtlJitter with the given maximum jitter range.
     * 创建具有给定最大抖动范围的 TtlJitter。
     *
     * @param jitterRange the maximum random extra duration added to TTL | 添加到 TTL 的最大随机额外时长
     * @return a new TtlJitter | 新的 TtlJitter
     */
    public static TtlJitter of(Duration jitterRange) {
        return new TtlJitter(jitterRange);
    }

    /**
     * Creates a TtlJitter with a default 30-second jitter range.
     * 创建具有默认 30 秒抖动范围的 TtlJitter。
     *
     * @return a TtlJitter with 30s range | 具有 30 秒范围的 TtlJitter
     */
    public static TtlJitter defaults() {
        return new TtlJitter(Duration.ofSeconds(30));
    }

    // ==================== Jitter Operations | 抖动操作 ====================

    /**
     * Applies a random jitter offset to the given TTL.
     * 对给定的 TTL 添加随机抖动偏移。
     *
     * <p>The added jitter is uniformly distributed in {@code [0, jitterRange]}.</p>
     * <p>添加的抖动在 {@code [0, jitterRange]} 范围内均匀分布。</p>
     *
     * @param baseTtl the base TTL to randomize | 要随机化的基础 TTL
     * @return the jittered TTL, or {@code baseTtl} unchanged if it is null or zero |
     *         添加抖动后的 TTL；如果 baseTtl 为 null 或 zero 则原样返回
     */
    public Duration apply(Duration baseTtl) {
        if (baseTtl == null || baseTtl.isZero()) {
            return baseTtl;
        }
        long jitterMillis = ThreadLocalRandom.current().nextLong(0, jitterRange.toMillis() + 1);
        return baseTtl.plusMillis(jitterMillis);
    }

    /**
     * Calculates a deterministic stagger offset for the entry at {@code index} in a batch.
     * 为批次中 {@code index} 位置的条目计算确定性错开偏移。
     *
     * <p>Distributes a batch of {@code batchSize} entries evenly across the jitter range so that
     * no two entries in the batch share the same expiration time.</p>
     * <p>将 {@code batchSize} 个条目均匀分布在抖动范围内，使批次中的任何两个条目都不共享相同的过期时间。</p>
     *
     * @param baseTtl   the base TTL | 基础 TTL
     * @param batchSize the total number of entries in the batch | 批次中的条目总数
     * @param index     the zero-based index of this entry | 此条目的零基索引
     * @return the staggered TTL for this entry | 此条目的错开 TTL
     */
    public Duration stagger(Duration baseTtl, int batchSize, int index) {
        if (baseTtl == null || batchSize <= 1) {
            return baseTtl;
        }
        long staggerStep = jitterRange.toMillis() / batchSize;
        return baseTtl.plusMillis(staggerStep * index);
    }

    /**
     * Executes a refresh action with mutex protection.
     * Only the thread that acquires the lock performs the refresh; others return the stale value immediately.
     * 使用互斥锁保护执行刷新操作。只有获取锁的线程执行刷新；其他线程立即返回过期值。
     *
     * @param <V>           the value type | 值类型
     * @param refreshAction the action that loads the fresh value | 加载新值的操作
     * @param staleValue    the value to return if another thread holds the lock | 另一个线程持有锁时返回的值
     * @return the refreshed value, or {@code staleValue} if the lock was already held |
     *         刷新后的值；如果锁已被持有则返回 {@code staleValue}
     */
    public <V> V mutexRefresh(Supplier<V> refreshAction, V staleValue) {
        if (refreshLock.tryLock()) {
            try {
                return refreshAction.get();
            } finally {
                refreshLock.unlock();
            }
        }
        return staleValue;
    }

    // ==================== Metrics | 指标 ====================

    /**
     * Returns the configured maximum jitter range.
     * 返回配置的最大抖动范围。
     *
     * @return the jitter range | 抖动范围
     */
    public Duration getJitterRange() {
        return jitterRange;
    }

    @Override
    public String toString() {
        return "TtlJitter{jitterRange=" + jitterRange + "}";
    }
}
