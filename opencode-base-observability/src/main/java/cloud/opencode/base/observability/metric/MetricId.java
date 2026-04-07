package cloud.opencode.base.observability.metric;

import cloud.opencode.base.observability.exception.ObservabilityException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * MetricId - Unique identifier for a metric, composed of name and tags
 * MetricId - 指标的唯一标识符，由名称和标签组成
 *
 * <p>Tags are defensively copied and sorted by key for consistent equality semantics.
 * Two MetricIds with the same name and same tags (regardless of original order) are equal.</p>
 * <p>标签进行防御性拷贝并按键排序，以确保一致的相等性语义。
 * 具有相同名称和相同标签（无论原始顺序）的两个 MetricId 相等。</p>
 *
 * @param name the metric name, must not be null or blank | 指标名称，不能为 null 或空白
 * @param tags the immutable sorted list of tags | 不可变的排序标签列表
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-observability V1.0.3
 */
public record MetricId(String name, List<Tag> tags) {

    /**
     * Compact constructor with validation and defensive copy.
     * 紧凑构造器，包含验证和防御性拷贝。
     */
    private static final Comparator<Tag> TAG_KEY_ORDER = Comparator.comparing(Tag::key);

    public MetricId {
        if (name == null || name.isBlank()) {
            throw new ObservabilityException("INVALID_METRIC", "Metric name must not be null or blank");
        }
        if (name.indexOf('\n') >= 0 || name.indexOf('\r') >= 0) {
            throw new ObservabilityException("INVALID_METRIC", "Metric name must not contain line-break characters");
        }
        if (tags == null || tags.isEmpty()) {
            tags = List.of();
        } else {
            // Sort in-place on a mutable copy, then wrap as unmodifiable.
            // Avoids Stream pipeline allocation (saves ~3 objects per construction).
            // 在可变副本上原地排序，然后包装为不可修改列表。
            // 避免 Stream 管道分配（每次构造节省约 3 个对象）。
            var sorted = new ArrayList<>(tags);
            sorted.sort(TAG_KEY_ORDER);
            tags = List.copyOf(sorted);
        }
    }

    /**
     * Creates a MetricId with the given name and optional tags.
     * 使用给定名称和可选标签创建 MetricId。
     *
     * @param name the metric name | 指标名称
     * @param tags the tags | 标签
     * @return a new MetricId instance | 新的 MetricId 实例
     * @throws ObservabilityException if name is null or blank | 如果名称为 null 或空白
     */
    public static MetricId of(String name, Tag... tags) {
        return new MetricId(name, tags == null ? List.of() : List.of(tags));
    }
}
