package cloud.opencode.base.core.bean;

import cloud.opencode.base.core.exception.OpenException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * Bean Property Descriptor - Describes a single bean property
 * Bean 属性描述符 - 描述单个 Bean 属性
 *
 * <p>Encapsulates property metadata including name, type, getter, setter, and field.</p>
 * <p>封装属性元数据，包括名称、类型、Getter、Setter 和字段。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Property metadata (name, type, readable, writable) - 属性元数据</li>
 *   <li>Value get/set operations - 值读写操作</li>
 *   <li>Generic type information - 泛型类型信息</li>
 *   <li>Annotation access (from method/field) - 注解访问</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Get property descriptor - 获取属性描述符
 * PropertyDescriptor pd = OpenBean.getPropertyDescriptor(User.class, "name").orElse(null);
 *
 * // Check readable/writable - 检查可读/可写
 * boolean readable = pd.isReadable();
 * boolean writable = pd.isWritable();
 *
 * // Get/Set value - 读写值
 * Object value = pd.getValue(user);
 * pd.setValue(user, newValue);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是 (不可变)</li>
 *   <li>Null-safe: Partial (throws on null bean) - 空值安全: 部分</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public final class PropertyDescriptor {

    private final String name;
    private final Class<?> type;
    private final Method readMethod;
    private final Method writeMethod;
    private final Field field;

    public PropertyDescriptor(String name, Class<?> type, Method readMethod,
                              Method writeMethod, Field field) {
        this.name = name;
        this.type = type;
        this.readMethod = readMethod;
        this.writeMethod = writeMethod;
        this.field = field;
    }

    // ==================== Getter ====================

    public String name() {
        return name;
    }

    public Class<?> type() {
        return type;
    }

    public Method readMethod() {
        return readMethod;
    }

    public Method writeMethod() {
        return writeMethod;
    }

    public Field field() {
        return field;
    }

    // ==================== 状态判断 ====================

    /**
     * Checks whether this property is readable
     * 是否可读
     */
    public boolean isReadable() {
        return readMethod != null;
    }

    /**
     * Checks whether this property is writable
     * 是否可写
     */
    public boolean isWritable() {
        return writeMethod != null;
    }

    /**
     * Checks whether this property has a corresponding field
     * 是否有对应字段
     */
    public boolean hasField() {
        return field != null;
    }

    // ==================== 值操作 ====================

    /**
     * Gets the property value from the given bean
     * 获取属性值
     */
    public Object getValue(Object bean) {
        if (readMethod != null) {
            try {
                readMethod.setAccessible(true);
                return readMethod.invoke(bean);
            } catch (Exception e) {
                throw new OpenException("Failed to get property value: " + name, e);
            }
        }
        if (field != null) {
            try {
                field.setAccessible(true);
                return field.get(bean);
            } catch (Exception e) {
                throw new OpenException("Failed to get field value: " + name, e);
            }
        }
        throw new OpenException("Property not readable: " + name);
    }

    /**
     * Sets the property value on the given bean
     * 设置属性值
     */
    public void setValue(Object bean, Object value) {
        if (writeMethod != null) {
            try {
                writeMethod.setAccessible(true);
                writeMethod.invoke(bean, value);
                return;
            } catch (Exception e) {
                throw new OpenException("Failed to set property value: " + name, e);
            }
        }
        if (field != null) {
            try {
                field.setAccessible(true);
                field.set(bean, value);
                return;
            } catch (Exception e) {
                throw new OpenException("Failed to set field value: " + name, e);
            }
        }
        throw new OpenException("Property not writable: " + name);
    }

    // ==================== 类型信息 ====================

    /**
     * Gets the generic type of this property
     * 获取泛型类型
     */
    public Type getGenericType() {
        if (readMethod != null) {
            return readMethod.getGenericReturnType();
        }
        if (field != null) {
            return field.getGenericType();
        }
        return type;
    }

    // ==================== 注解操作 ====================

    /**
     * Gets the specified annotation from this property
     * 获取属性注解
     */
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        A annotation = null;
        if (readMethod != null) {
            annotation = readMethod.getAnnotation(annotationClass);
        }
        if (annotation == null && writeMethod != null) {
            annotation = writeMethod.getAnnotation(annotationClass);
        }
        if (annotation == null && field != null) {
            annotation = field.getAnnotation(annotationClass);
        }
        return annotation;
    }

    /**
     * Checks whether this property has the specified annotation
     * 检查是否有指定注解
     */
    public boolean hasAnnotation(Class<? extends Annotation> annotationClass) {
        return getAnnotation(annotationClass) != null;
    }

    @Override
    public String toString() {
        return "PropertyDescriptor{" +
                "name='" + name + '\'' +
                ", type=" + type.getSimpleName() +
                ", readable=" + isReadable() +
                ", writable=" + isWritable() +
                '}';
    }
}
