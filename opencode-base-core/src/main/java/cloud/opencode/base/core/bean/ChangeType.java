package cloud.opencode.base.core.bean;

/**
 * Change Type Enum - Describes the type of change between two object properties
 * 变更类型枚举 - 描述两个对象属性之间的变更类型
 *
 * <p>Used by {@link ObjectDiff} to classify each property difference.</p>
 * <p>由 {@link ObjectDiff} 使用，用于分类每个属性差异。</p>
 *
 * <ul>
 *   <li>{@link #ADDED} - Property exists only in the new object | 属性仅存在于新对象中</li>
 *   <li>{@link #REMOVED} - Property exists only in the old object | 属性仅存在于旧对象中</li>
 *   <li>{@link #MODIFIED} - Property value changed | 属性值已修改</li>
 *   <li>{@link #UNCHANGED} - Property value is the same | 属性值相同</li>
 *   <li>{@link #CIRCULAR_REFERENCE} - Circular reference detected | 检测到循环引用</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.3
 */
public enum ChangeType {

    /**
     * Property exists only in the new object
     * 属性仅存在于新对象中
     */
    ADDED,

    /**
     * Property exists only in the old object
     * 属性仅存在于旧对象中
     */
    REMOVED,

    /**
     * Property value changed
     * 属性值已修改
     */
    MODIFIED,

    /**
     * Property value is the same
     * 属性值相同
     */
    UNCHANGED,

    /**
     * Circular reference detected during deep comparison
     * 深度比较时检测到循环引用
     */
    CIRCULAR_REFERENCE
}
