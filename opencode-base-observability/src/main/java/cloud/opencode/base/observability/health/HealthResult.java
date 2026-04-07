package cloud.opencode.base.observability.health;

import cloud.opencode.base.observability.exception.ObservabilityException;

import java.time.Duration;

/**
 * Immutable result of a single health check execution.
 * 单次健康检查执行的不可变结果。
 *
 * <p>Contains the check name, status, optional detail message, and the duration
 * it took to execute. Factory methods {@link #up}, {@link #down}, and {@link #degraded}
 * provide convenient construction for common cases.</p>
 * <p>包含检查名称、状态、可选的详情消息以及执行耗时。
 * 工厂方法 {@link #up}、{@link #down} 和 {@link #degraded} 为常见场景提供便捷构造。</p>
 *
 * @param name     the health check name | 健康检查名称
 * @param status   the health status | 健康状态
 * @param detail   optional detail message (may be null) | 可选的详情消息（可为 null）
 * @param duration the time taken to execute the check | 执行检查所花费的时间
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-observability V1.0.3
 */
public record HealthResult(String name, HealthStatus status, String detail, Duration duration) {

    /**
     * Compact constructor with validation.
     * 带验证的紧凑构造器。
     *
     * @throws ObservabilityException if name is null/blank, status is null, or duration is null |
     *                                如果 name 为 null/空白、status 为 null 或 duration 为 null
     */
    public HealthResult {
        if (name == null || name.isBlank()) {
            throw new ObservabilityException("INVALID_HEALTH", "Health check name must not be null or blank");
        }
        if (status == null) {
            throw new ObservabilityException("INVALID_HEALTH", "Health status must not be null");
        }
        if (duration == null) {
            throw new ObservabilityException("INVALID_HEALTH", "Duration must not be null");
        }
        // detail CAN be null
    }

    /**
     * Creates an UP result with no detail.
     * 创建一个无详情的 UP 结果。
     *
     * @param name     the health check name | 健康检查名称
     * @param duration the time taken | 执行耗时
     * @return a new UP health result | 新的 UP 健康结果
     */
    public static HealthResult up(String name, Duration duration) {
        return new HealthResult(name, HealthStatus.UP, null, duration);
    }

    /**
     * Creates a DOWN result with a detail message.
     * 创建一个带详情消息的 DOWN 结果。
     *
     * @param name     the health check name | 健康检查名称
     * @param detail   the detail message | 详情消息
     * @param duration the time taken | 执行耗时
     * @return a new DOWN health result | 新的 DOWN 健康结果
     */
    public static HealthResult down(String name, String detail, Duration duration) {
        return new HealthResult(name, HealthStatus.DOWN, detail, duration);
    }

    /**
     * Creates a DEGRADED result with a detail message.
     * 创建一个带详情消息的 DEGRADED 结果。
     *
     * @param name     the health check name | 健康检查名称
     * @param detail   the detail message | 详情消息
     * @param duration the time taken | 执行耗时
     * @return a new DEGRADED health result | 新的 DEGRADED 健康结果
     */
    public static HealthResult degraded(String name, String detail, Duration duration) {
        return new HealthResult(name, HealthStatus.DEGRADED, detail, duration);
    }
}
