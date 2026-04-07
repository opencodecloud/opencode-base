package cloud.opencode.base.core.bean;

import java.util.Objects;

/**
 * Diff Record - Represents a single property difference between two objects
 * 差异记录 - 表示两个对象之间单个属性的差异
 *
 * <p>Captures the field name, old value, new value, and the type of change.</p>
 * <p>记录字段名称、旧值、新值以及变更类型。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Diff<String> diff = new Diff<>("name", "Alice", "Bob", ChangeType.MODIFIED);
 * String field = diff.fieldName();   // "name"
 * String oldVal = diff.oldValue();   // "Alice"
 * String newVal = diff.newValue();   // "Bob"
 * ChangeType type = diff.changeType(); // MODIFIED
 * }</pre>
 *
 * @param fieldName  the property name | 属性名称
 * @param oldValue   the old value (nullable) | 旧值（可为 null）
 * @param newValue   the new value (nullable) | 新值（可为 null）
 * @param changeType the type of change | 变更类型
 * @param <T>        the value type | 值类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.3
 */
public record Diff<T>(
        String fieldName,
        T oldValue,
        T newValue,
        ChangeType changeType
) {

    /**
     * Creates a Diff instance with validation
     * 创建 Diff 实例（带验证）
     *
     * @param fieldName  the property name | 属性名称
     * @param oldValue   the old value (nullable) | 旧值
     * @param newValue   the new value (nullable) | 新值
     * @param changeType the type of change | 变更类型
     */
    public Diff {
        Objects.requireNonNull(fieldName, "fieldName must not be null");
        Objects.requireNonNull(changeType, "changeType must not be null");
    }
}
