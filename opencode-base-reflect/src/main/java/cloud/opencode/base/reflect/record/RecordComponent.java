package cloud.opencode.base.reflect.record;

import cloud.opencode.base.reflect.exception.OpenReflectException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Optional;

/**
 * Record Component Wrapper
 * Record组件包装器
 *
 * <p>Wraps a java.lang.reflect.RecordComponent with additional utilities.</p>
 * <p>用额外工具包装java.lang.reflect.RecordComponent。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Component type and name access - 组件类型和名称访问</li>
 *   <li>Accessor method invocation - 访问器方法调用</li>
 *   <li>Annotation access on components - 组件上的注解访问</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * List<RecordComponent> components = OpenRecord.getComponents(User.class);
 * RecordComponent comp = components.get(0);
 * String name = comp.getName();
 * Object value = comp.getValue(userRecord);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 *   <li>Null-safe: No (component must be non-null) - 空值安全: 否（组件须非空）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
public class RecordComponent {

    private final java.lang.reflect.RecordComponent component;
    private final Method accessor;

    /**
     * Creates a RecordComponent wrapper
     * 创建RecordComponent包装器
     *
     * @param component the underlying component | 底层组件
     */
    public RecordComponent(java.lang.reflect.RecordComponent component) {
        this.component = Objects.requireNonNull(component, "component must not be null");
        this.accessor = component.getAccessor();
        this.accessor.setAccessible(true);
    }

    /**
     * Gets the component name
     * 获取组件名称
     *
     * @return the name | 名称
     */
    public String getName() {
        return component.getName();
    }

    /**
     * Gets the component type
     * 获取组件类型
     *
     * @return the type | 类型
     */
    public Class<?> getType() {
        return component.getType();
    }

    /**
     * Gets the generic type
     * 获取泛型类型
     *
     * @return the generic type | 泛型类型
     */
    public Type getGenericType() {
        return component.getGenericType();
    }

    /**
     * Gets the accessor method
     * 获取访问器方法
     *
     * @return the accessor | 访问器
     */
    public Method getAccessor() {
        return accessor;
    }

    /**
     * Gets the declaring record class
     * 获取声明的record类
     *
     * @return the declaring class | 声明类
     */
    public Class<?> getDeclaringClass() {
        return component.getDeclaringRecord();
    }

    /**
     * Gets the value from a record instance
     * 从record实例获取值
     *
     * @param record the record instance | record实例
     * @return the value | 值
     */
    public Object getValue(Object record) {
        try {
            return accessor.invoke(record);
        } catch (Exception e) {
            throw new OpenReflectException("Failed to get record component value: " + getName(), e);
        }
    }

    /**
     * Gets the value with type
     * 获取值（带类型）
     *
     * @param record the record instance | record实例
     * @param type   the expected type | 期望类型
     * @param <T>    the value type | 值类型
     * @return the value | 值
     */
    @SuppressWarnings("unchecked")
    public <T> T getValue(Object record, Class<T> type) {
        return (T) getValue(record);
    }

    /**
     * Gets an annotation from this component
     * 从此组件获取注解
     *
     * @param annotationClass the annotation class | 注解类
     * @param <A>             the annotation type | 注解类型
     * @return the annotation or null | 注解或null
     */
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        return component.getAnnotation(annotationClass);
    }

    /**
     * Finds an annotation (Optional)
     * 查找注解（Optional）
     *
     * @param annotationClass the annotation class | 注解类
     * @param <A>             the annotation type | 注解类型
     * @return Optional of annotation | 注解的Optional
     */
    public <A extends Annotation> Optional<A> findAnnotation(Class<A> annotationClass) {
        return Optional.ofNullable(getAnnotation(annotationClass));
    }

    /**
     * Checks if annotation is present
     * 检查注解是否存在
     *
     * @param annotationClass the annotation class | 注解类
     * @return true if present | 如果存在返回true
     */
    public boolean hasAnnotation(Class<? extends Annotation> annotationClass) {
        return component.isAnnotationPresent(annotationClass);
    }

    /**
     * Gets all annotations on this component
     * 获取此组件上的所有注解
     *
     * @return array of annotations | 注解数组
     */
    public Annotation[] getAnnotations() {
        return component.getAnnotations();
    }

    /**
     * Gets the underlying RecordComponent
     * 获取底层RecordComponent
     *
     * @return the underlying component | 底层组件
     */
    public java.lang.reflect.RecordComponent unwrap() {
        return component;
    }

    /**
     * Gets the index of this component
     * 获取此组件的索引
     *
     * @return the index | 索引
     */
    public int getIndex() {
        java.lang.reflect.RecordComponent[] components = component.getDeclaringRecord().getRecordComponents();
        for (int i = 0; i < components.length; i++) {
            if (components[i] == component || components[i].getName().equals(component.getName())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Checks if this component is primitive
     * 检查此组件是否为原始类型
     *
     * @return true if primitive | 如果是原始类型返回true
     */
    public boolean isPrimitive() {
        return component.getType().isPrimitive();
    }

    /**
     * Checks if this component is an array
     * 检查此组件是否为数组
     *
     * @return true if array | 如果是数组返回true
     */
    public boolean isArray() {
        return component.getType().isArray();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RecordComponent that)) return false;
        return component.equals(that.component);
    }

    @Override
    public int hashCode() {
        return component.hashCode();
    }

    @Override
    public String toString() {
        return "RecordComponent[" + component.getDeclaringRecord().getSimpleName() +
                "." + getName() + " : " + getType().getSimpleName() + "]";
    }
}
