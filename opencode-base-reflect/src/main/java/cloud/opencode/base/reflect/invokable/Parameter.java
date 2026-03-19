package cloud.opencode.base.reflect.invokable;

import cloud.opencode.base.reflect.type.TypeToken;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Objects;

/**
 * Parameter Wrapper
 * 参数包装
 *
 * <p>Wraps a method/constructor parameter with additional metadata.</p>
 * <p>包装方法/构造器参数及其附加元数据。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Type information access - 类型信息访问</li>
 *   <li>Parameter name access - 参数名访问</li>
 *   <li>Annotation access - 注解访问</li>
 *   <li>Parameter metadata - 参数元数据</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Parameter param = invokable.getParameters().get(0);
 * String name = param.getName();
 * TypeToken<?> type = param.getType();
 * boolean hasAnnotation = param.isAnnotationPresent(NotNull.class);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 *   <li>Null-safe: No (parameter must be non-null) - 空值安全: 否（参数须非空）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
public final class Parameter implements AnnotatedElement {

    private final java.lang.reflect.Parameter parameter;
    private final int index;

    /**
     * Creates a Parameter wrapper
     * 创建参数包装
     *
     * @param parameter the underlying parameter | 底层参数
     * @param index     the parameter index | 参数索引
     */
    public Parameter(java.lang.reflect.Parameter parameter, int index) {
        this.parameter = Objects.requireNonNull(parameter, "parameter must not be null");
        this.index = index;
    }

    /**
     * Gets the parameter type as TypeToken
     * 获取参数类型的TypeToken
     *
     * @return TypeToken of parameter type | 参数类型的TypeToken
     */
    public TypeToken<?> getType() {
        return TypeToken.of(parameter.getParameterizedType());
    }

    /**
     * Gets the raw parameter type
     * 获取原始参数类型
     *
     * @return the raw type | 原始类型
     */
    public Class<?> getRawType() {
        return parameter.getType();
    }

    /**
     * Gets the parameter name
     * 获取参数名
     *
     * @return the parameter name | 参数名
     */
    public String getName() {
        return parameter.getName();
    }

    /**
     * Checks if the parameter name is present
     * 检查参数名是否存在
     *
     * @return true if name is present | 如果名称存在返回true
     */
    public boolean isNamePresent() {
        return parameter.isNamePresent();
    }

    /**
     * Gets the parameter index
     * 获取参数索引
     *
     * @return the index | 索引
     */
    public int getIndex() {
        return index;
    }

    /**
     * Checks if this is a varargs parameter
     * 检查是否为可变参数
     *
     * @return true if varargs | 如果是可变参数返回true
     */
    public boolean isVarArgs() {
        return parameter.isVarArgs();
    }

    /**
     * Checks if this is an implicit parameter
     * 检查是否为隐式参数
     *
     * @return true if implicit | 如果是隐式参数返回true
     */
    public boolean isImplicit() {
        return parameter.isImplicit();
    }

    /**
     * Checks if this is a synthetic parameter
     * 检查是否为合成参数
     *
     * @return true if synthetic | 如果是合成参数返回true
     */
    public boolean isSynthetic() {
        return parameter.isSynthetic();
    }

    /**
     * Gets the underlying parameter
     * 获取底层参数
     *
     * @return the underlying parameter | 底层参数
     */
    public java.lang.reflect.Parameter getUnderlying() {
        return parameter;
    }

    // ==================== AnnotatedElement Implementation | AnnotatedElement实现 ====================

    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return parameter.isAnnotationPresent(annotationClass);
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        return parameter.getAnnotation(annotationClass);
    }

    @Override
    public Annotation[] getAnnotations() {
        return parameter.getAnnotations();
    }

    @Override
    public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationClass) {
        return parameter.getAnnotationsByType(annotationClass);
    }

    @Override
    public <A extends Annotation> A getDeclaredAnnotation(Class<A> annotationClass) {
        return parameter.getDeclaredAnnotation(annotationClass);
    }

    @Override
    public <A extends Annotation> A[] getDeclaredAnnotationsByType(Class<A> annotationClass) {
        return parameter.getDeclaredAnnotationsByType(annotationClass);
    }

    @Override
    public Annotation[] getDeclaredAnnotations() {
        return parameter.getDeclaredAnnotations();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Parameter other)) return false;
        return parameter.equals(other.parameter) && index == other.index;
    }

    @Override
    public int hashCode() {
        return Objects.hash(parameter, index);
    }

    @Override
    public String toString() {
        return String.format("Parameter[%d: %s %s]",
                index, getType(), getName());
    }
}
