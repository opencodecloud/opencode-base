package cloud.opencode.base.observability.health;

import java.util.Collection;

/**
 * Enumeration of possible health check statuses.
 * 健康检查状态枚举。
 *
 * <p>Represents the overall health of a component: {@link #UP} (healthy),
 * {@link #DOWN} (unhealthy), or {@link #DEGRADED} (partially healthy).
 * The {@link #aggregate} method computes the worst-case status from a collection.</p>
 * <p>表示组件的整体健康状态：{@link #UP}（健康）、{@link #DOWN}（不健康）
 * 或 {@link #DEGRADED}（部分健康）。{@link #aggregate} 方法从集合中计算最差状态。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-observability V1.0.3
 */
public enum HealthStatus {

    /**
     * The component is healthy.
     * 组件健康。
     */
    UP,

    /**
     * The component is unhealthy.
     * 组件不健康。
     */
    DOWN,

    /**
     * The component is partially healthy.
     * 组件部分健康。
     */
    DEGRADED;

    /**
     * Aggregates a collection of statuses into a single worst-case status.
     * 将一组状态聚合为单个最差状态。
     *
     * <p>{@link #DOWN} takes highest priority, followed by {@link #DEGRADED},
     * then {@link #UP}. A null or empty collection returns {@link #UP}.</p>
     * <p>{@link #DOWN} 优先级最高，其次是 {@link #DEGRADED}，然后是 {@link #UP}。
     * null 或空集合返回 {@link #UP}。</p>
     *
     * @param statuses the collection of statuses to aggregate | 要聚合的状态集合
     * @return the aggregated worst-case status | 聚合后的最差状态
     */
    public static HealthStatus aggregate(Collection<HealthStatus> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return UP;
        }
        boolean hasDown = false;
        boolean hasDegraded = false;
        for (HealthStatus s : statuses) {
            if (s == DOWN) {
                hasDown = true;
            } else if (s == DEGRADED) {
                hasDegraded = true;
            }
        }
        if (hasDown) {
            return DOWN;
        }
        if (hasDegraded) {
            return DEGRADED;
        }
        return UP;
    }
}
