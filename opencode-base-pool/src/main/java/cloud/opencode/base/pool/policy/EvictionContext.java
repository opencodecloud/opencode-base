package cloud.opencode.base.pool.policy;

import java.time.Instant;

/**
 * EvictionContext - Eviction Context Record (JDK 25 Record)
 * EvictionContext - 驱逐上下文记录 (JDK 25 Record)
 *
 * <p>Provides contextual information for eviction policy decisions.</p>
 * <p>为驱逐策略决策提供上下文信息。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Pool state information - 池状态信息</li>
 *   <li>Immutable record type - 不可变记录类型</li>
 *   <li>Eviction timing info - 驱逐时间信息</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * EvictionContext ctx = new EvictionContext(5, 3, 10, Instant.now());
 * if (ctx.currentIdleCount() > ctx.maxTotal() / 2) {
 *     // trigger aggressive eviction
 * }
 * }</pre>
 *
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 * @param currentIdleCount   the current idle object count - 当前空闲对象数
 * @param currentActiveCount the current active object count - 当前活跃对象数
 * @param maxTotal           the maximum total objects - 最大对象总数
 * @param evictionTime       the eviction time - 驱逐时间
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.0
 */
public record EvictionContext(
        int currentIdleCount,
        int currentActiveCount,
        int maxTotal,
        Instant evictionTime
) {

    /**
     * Gets the total object count.
     * 获取对象总数。
     *
     * @return the total count - 总数
     */
    public int totalCount() {
        return currentIdleCount + currentActiveCount;
    }

    /**
     * Checks if the pool is at capacity.
     * 检查池是否已满。
     *
     * @return true if at capacity - 如果已满返回true
     */
    public boolean isAtCapacity() {
        return totalCount() >= maxTotal;
    }

    /**
     * Gets the idle ratio.
     * 获取空闲比率。
     *
     * @return the idle ratio (0.0 to 1.0) - 空闲比率 (0.0到1.0)
     */
    public double idleRatio() {
        int total = totalCount();
        return total == 0 ? 0.0 : (double) currentIdleCount / total;
    }
}
