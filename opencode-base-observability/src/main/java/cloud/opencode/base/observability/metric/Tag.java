package cloud.opencode.base.observability.metric;

import cloud.opencode.base.observability.exception.ObservabilityException;

/**
 * Tag - A key-value pair for metric dimensioning
 * Tag - 用于指标维度化的键值对
 *
 * <p>Tags provide dimensional metadata to metrics, enabling filtering and grouping
 * in monitoring systems. Keys must be non-blank; values must be non-null.</p>
 * <p>标签为指标提供维度元数据，支持监控系统中的过滤和分组。
 * 键不能为空白；值不能为 null。</p>
 *
 * @param key   the tag key, must not be null or blank | 标签键，不能为 null 或空白
 * @param value the tag value, must not be null | 标签值，不能为 null
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-observability V1.0.3
 */
public record Tag(String key, String value) {

    /**
     * Compact constructor with validation.
     * 紧凑构造器，包含参数验证。
     */
    public Tag {
        if (key == null || key.isBlank()) {
            throw new ObservabilityException("INVALID_TAG", "Tag key must not be null or blank");
        }
        if (containsLineBreak(key)) {
            throw new ObservabilityException("INVALID_TAG", "Tag key must not contain line-break characters");
        }
        if (value == null) {
            throw new ObservabilityException("INVALID_TAG", "Tag value must not be null");
        }
        if (containsLineBreak(value)) {
            throw new ObservabilityException("INVALID_TAG", "Tag value must not contain line-break characters");
        }
    }

    private static boolean containsLineBreak(String s) {
        return s.indexOf('\n') >= 0 || s.indexOf('\r') >= 0;
    }

    /**
     * Creates a new Tag with the given key and value.
     * 使用给定的键和值创建新标签。
     *
     * @param key   the tag key | 标签键
     * @param value the tag value | 标签值
     * @return a new Tag instance | 新的 Tag 实例
     * @throws ObservabilityException if key is null/blank or value is null | 如果键为 null/空白或值为 null
     */
    public static Tag of(String key, String value) {
        return new Tag(key, value);
    }
}
