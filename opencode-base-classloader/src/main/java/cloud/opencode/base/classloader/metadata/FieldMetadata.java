package cloud.opencode.base.classloader.metadata;

import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Field Metadata - Immutable field information
 * 字段元数据 - 不可变的字段信息
 *
 * <p>Represents field metadata read from class files without loading the class.</p>
 * <p>表示从类文件读取的字段元数据，无需加载类。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Field type and name - 字段类型和名称</li>
 *   <li>Modifier information - 修饰符信息</li>
 *   <li>Constant value access - 常量值访问</li>
 *   <li>Annotation information - 注解信息</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * FieldMetadata field = classMetadata.fields().get(0);
 * String name = field.fieldName();
 * String type = field.fieldType();
 * boolean isStatic = field.isStatic();
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
public final class FieldMetadata {

    private final String fieldName;
    private final String fieldType;
    private final int modifiers;
    private final Object constantValue;
    private final List<AnnotationMetadata> annotations;

    /**
     * Create field metadata
     * 创建字段元数据
     *
     * @param fieldName     field name | 字段名
     * @param fieldType     field type | 字段类型
     * @param modifiers     modifier flags | 修饰符标志
     * @param constantValue constant value | 常量值
     * @param annotations   field annotations | 字段注解
     */
    public FieldMetadata(String fieldName, String fieldType, int modifiers,
                         Object constantValue, List<AnnotationMetadata> annotations) {
        this.fieldName = Objects.requireNonNull(fieldName, "Field name must not be null");
        this.fieldType = Objects.requireNonNull(fieldType, "Field type must not be null");
        this.modifiers = modifiers;
        this.constantValue = constantValue;
        this.annotations = annotations != null ? List.copyOf(annotations) : List.of();
    }

    // ==================== Getters ====================

    /**
     * Get field name
     * 获取字段名
     *
     * @return field name | 字段名
     */
    public String fieldName() {
        return fieldName;
    }

    /**
     * Get field type
     * 获取字段类型
     *
     * @return field type | 字段类型
     */
    public String fieldType() {
        return fieldType;
    }

    /**
     * Get modifiers
     * 获取修饰符
     *
     * @return modifier flags | 修饰符标志
     */
    public int modifiers() {
        return modifiers;
    }

    /**
     * Get constant value
     * 获取常量值
     *
     * @return constant value or null | 常量值或 null
     */
    public Object constantValue() {
        return constantValue;
    }

    /**
     * Get annotations
     * 获取注解
     *
     * @return list of annotations | 注解列表
     */
    public List<AnnotationMetadata> annotations() {
        return annotations;
    }

    // ==================== Modifier Checks | 修饰符检查 ====================

    /**
     * Check if static
     * 检查是否为静态
     *
     * @return true if static | 是静态返回 true
     */
    public boolean isStatic() {
        return Modifier.isStatic(modifiers);
    }

    /**
     * Check if final
     * 检查是否为 final
     *
     * @return true if final | 是 final 返回 true
     */
    public boolean isFinal() {
        return Modifier.isFinal(modifiers);
    }

    /**
     * Check if transient
     * 检查是否为 transient
     *
     * @return true if transient | 是 transient 返回 true
     */
    public boolean isTransient() {
        return Modifier.isTransient(modifiers);
    }

    /**
     * Check if volatile
     * 检查是否为 volatile
     *
     * @return true if volatile | 是 volatile 返回 true
     */
    public boolean isVolatile() {
        return Modifier.isVolatile(modifiers);
    }

    /**
     * Check if public
     * 检查是否为 public
     *
     * @return true if public | 是 public 返回 true
     */
    public boolean isPublic() {
        return Modifier.isPublic(modifiers);
    }

    /**
     * Check if private
     * 检查是否为 private
     *
     * @return true if private | 是 private 返回 true
     */
    public boolean isPrivate() {
        return Modifier.isPrivate(modifiers);
    }

    /**
     * Check if protected
     * 检查是否为 protected
     *
     * @return true if protected | 是 protected 返回 true
     */
    public boolean isProtected() {
        return Modifier.isProtected(modifiers);
    }

    // ==================== Convenience Methods | 便捷方法 ====================

    /**
     * Check if has constant value
     * 检查是否有常量值
     *
     * @return true if has constant value | 有常量值返回 true
     */
    public boolean hasConstantValue() {
        return constantValue != null;
    }

    /**
     * Check if has specified annotation
     * 检查是否有指定注解
     *
     * @param annotationClassName annotation class name | 注解类名
     * @return true if has annotation | 有注解返回 true
     */
    public boolean hasAnnotation(String annotationClassName) {
        return annotations.stream()
                .anyMatch(a -> a.annotationType().equals(annotationClassName));
    }

    /**
     * Get specified annotation
     * 获取指定注解
     *
     * @param annotationClassName annotation class name | 注解类名
     * @return optional annotation | 可选的注解
     */
    public Optional<AnnotationMetadata> getAnnotation(String annotationClassName) {
        return annotations.stream()
                .filter(a -> a.annotationType().equals(annotationClassName))
                .findFirst();
    }

    /**
     * Get simple field type name
     * 获取简单字段类型名
     *
     * @return simple type name | 简单类型名
     */
    public String getSimpleTypeName() {
        int lastDot = fieldType.lastIndexOf('.');
        return lastDot != -1 ? fieldType.substring(lastDot + 1) : fieldType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FieldMetadata that)) return false;
        return fieldName.equals(that.fieldName) && fieldType.equals(that.fieldType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fieldName, fieldType);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Modifier.toString(modifiers));
        if (!sb.isEmpty()) sb.append(" ");
        sb.append(getSimpleTypeName()).append(" ").append(fieldName);
        return sb.toString();
    }
}
