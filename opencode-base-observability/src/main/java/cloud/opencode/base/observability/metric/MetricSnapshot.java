package cloud.opencode.base.observability.metric;

import cloud.opencode.base.observability.exception.ObservabilityException;

import java.util.Map;

/**
 * MetricSnapshot - An immutable snapshot of a metric's current state
 * MetricSnapshot - 指标当前状态的不可变快照
 *
 * <p>Contains the metric identifier, type name, and a map of key-value measurements.
 * All values are defensively copied to ensure immutability.</p>
 * <p>包含指标标识符、类型名称和键值测量值映射。
 * 所有值均进行防御性拷贝以确保不可变性。</p>
 *
 * @param id     the metric identifier | 指标标识符
 * @param type   the metric type (e.g. "counter", "gauge") | 指标类型
 * @param values the metric measurements | 指标测量值
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-observability V1.0.3
 */
public record MetricSnapshot(MetricId id, String type, Map<String, Object> values) {

    /**
     * Compact constructor with validation and defensive copy.
     * 紧凑构造器，包含验证和防御性拷贝。
     */
    public MetricSnapshot {
        if (id == null) {
            throw new ObservabilityException("INVALID_METRIC", "MetricId must not be null");
        }
        if (type == null || type.isBlank()) {
            throw new ObservabilityException("INVALID_METRIC", "Metric type must not be null or blank");
        }
        values = values == null ? Map.of() : Map.copyOf(values);
    }
}
