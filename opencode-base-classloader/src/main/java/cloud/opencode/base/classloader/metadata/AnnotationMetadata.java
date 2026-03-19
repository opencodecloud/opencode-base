package cloud.opencode.base.classloader.metadata;

import java.util.*;

/**
 * Annotation Metadata - Immutable annotation information
 * 注解元数据 - 不可变的注解信息
 *
 * <p>Represents annotation metadata read from class files without loading the class.</p>
 * <p>表示从类文件读取的注解元数据，无需加载类。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Annotation type information - 注解类型信息</li>
 *   <li>Annotation attributes access - 注解属性访问</li>
 *   <li>Runtime visibility check - 运行时可见性检查</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * AnnotationMetadata annotation = classMetadata.getAnnotation("Service").orElseThrow();
 * String value = annotation.getAttribute("value", String.class).orElse("");
 * boolean isRuntime = annotation.isRuntimeVisible();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是 (不可变)</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.0
 */
public final class AnnotationMetadata {

    private final String annotationType;
    private final Map<String, Object> attributes;
    private final boolean runtimeVisible;

    /**
     * Create annotation metadata
     * 创建注解元数据
     *
     * @param annotationType  annotation type name | 注解类型名称
     * @param attributes      annotation attributes | 注解属性
     * @param runtimeVisible  whether visible at runtime | 是否运行时可见
     */
    public AnnotationMetadata(String annotationType, Map<String, Object> attributes, boolean runtimeVisible) {
        this.annotationType = Objects.requireNonNull(annotationType, "Annotation type must not be null");
        this.attributes = attributes != null ? Map.copyOf(attributes) : Map.of();
        this.runtimeVisible = runtimeVisible;
    }

    // ==================== Getters ====================

    /**
     * Get annotation type name
     * 获取注解类型名称
     *
     * @return annotation type name | 注解类型名称
     */
    public String annotationType() {
        return annotationType;
    }

    /**
     * Get annotation attributes
     * 获取注解属性
     *
     * @return unmodifiable map of attributes | 不可修改的属性映射
     */
    public Map<String, Object> attributes() {
        return attributes;
    }

    /**
     * Check if annotation is visible at runtime
     * 检查注解是否运行时可见
     *
     * @return true if runtime visible | 运行时可见返回 true
     */
    public boolean isRuntimeVisible() {
        return runtimeVisible;
    }

    // ==================== Convenience Methods | 便捷方法 ====================

    /**
     * Get attribute value with type
     * 获取指定类型的属性值
     *
     * @param name attribute name | 属性名
     * @param type expected type | 期望类型
     * @param <T>  type parameter | 类型参数
     * @return optional attribute value | 可选的属性值
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getAttribute(String name, Class<T> type) {
        Object value = attributes.get(name);
        if (value != null && type.isInstance(value)) {
            return Optional.of((T) value);
        }
        return Optional.empty();
    }

    /**
     * Get attribute value with type and default
     * 获取指定类型的属性值，带默认值
     *
     * @param name         attribute name | 属性名
     * @param type         expected type | 期望类型
     * @param defaultValue default value | 默认值
     * @param <T>          type parameter | 类型参数
     * @return attribute value or default | 属性值或默认值
     */
    public <T> T getAttribute(String name, Class<T> type, T defaultValue) {
        return getAttribute(name, type).orElse(defaultValue);
    }

    /**
     * Get 'value' attribute
     * 获取 'value' 属性
     *
     * @return optional value | 可选的值
     */
    public Optional<Object> getValue() {
        return Optional.ofNullable(attributes.get("value"));
    }

    /**
     * Get 'value' attribute as String
     * 获取 'value' 属性作为字符串
     *
     * @return optional string value | 可选的字符串值
     */
    public Optional<String> getStringValue() {
        return getAttribute("value", String.class);
    }

    /**
     * Check if attribute exists
     * 检查属性是否存在
     *
     * @param name attribute name | 属性名
     * @return true if exists | 存在返回 true
     */
    public boolean hasAttribute(String name) {
        return attributes.containsKey(name);
    }

    /**
     * Get all attribute names
     * 获取所有属性名
     *
     * @return set of attribute names | 属性名集合
     */
    public Set<String> getAttributeNames() {
        return attributes.keySet();
    }

    /**
     * Get simple type name (without package)
     * 获取简单类型名（不含包名）
     *
     * @return simple name | 简单名称
     */
    public String getSimpleName() {
        int lastDot = annotationType.lastIndexOf('.');
        return lastDot != -1 ? annotationType.substring(lastDot + 1) : annotationType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AnnotationMetadata that)) return false;
        return annotationType.equals(that.annotationType) && attributes.equals(that.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(annotationType, attributes);
    }

    @Override
    public String toString() {
        return "@" + getSimpleName() + (attributes.isEmpty() ? "" : attributes);
    }
}
