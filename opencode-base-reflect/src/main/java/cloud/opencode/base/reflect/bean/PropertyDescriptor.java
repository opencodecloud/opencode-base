package cloud.opencode.base.reflect.bean;

import cloud.opencode.base.reflect.exception.OpenReflectException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Optional;

/**
 * Property Descriptor
 * 属性描述符
 *
 * <p>Describes a bean property with its getter, setter, and field.</p>
 * <p>描述bean属性及其getter、setter和字段。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Getter/setter/field access - getter/setter/字段访问</li>
 *   <li>Property readability/writability checking - 属性可读/可写检查</li>
 *   <li>Annotation access on property - 属性上的注解访问</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * PropertyDescriptor desc = OpenBean.getPropertyDescriptors(User.class).get("name");
 * Object value = desc.getValue(user);
 * desc.setValue(user, "Alice");
 * boolean writable = desc.isWritable();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (not synchronized) - 线程安全: 否（未同步）</li>
 *   <li>Null-safe: No (caller must ensure non-null target) - 空值安全: 否（调用方须确保非空目标）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
public class PropertyDescriptor {

    private final String name;
    private final Class<?> propertyType;
    private final Type genericType;
    private final Method readMethod;
    private final Method writeMethod;
    private final Field field;
    private final Class<?> declaringClass;

    /**
     * Creates a PropertyDescriptor
     * 创建PropertyDescriptor
     *
     * @param name           the property name | 属性名
     * @param propertyType   the property type | 属性类型
     * @param genericType    the generic type | 泛型类型
     * @param readMethod     the getter method (can be null) | getter方法（可为null）
     * @param writeMethod    the setter method (can be null) | setter方法（可为null）
     * @param field          the field (can be null) | 字段（可为null）
     * @param declaringClass the declaring class | 声明类
     */
    public PropertyDescriptor(String name, Class<?> propertyType, Type genericType,
                               Method readMethod, Method writeMethod, Field field,
                               Class<?> declaringClass) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.propertyType = Objects.requireNonNull(propertyType, "propertyType must not be null");
        this.genericType = genericType != null ? genericType : propertyType;
        this.readMethod = readMethod;
        this.writeMethod = writeMethod;
        this.field = field;
        this.declaringClass = Objects.requireNonNull(declaringClass, "declaringClass must not be null");

        if (readMethod != null) {
            readMethod.setAccessible(true);
        }
        if (writeMethod != null) {
            writeMethod.setAccessible(true);
        }
        if (field != null) {
            field.setAccessible(true);
        }
    }

    /**
     * Gets the property name
     * 获取属性名
     *
     * @return the name | 名称
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the property type
     * 获取属性类型
     *
     * @return the type | 类型
     */
    public Class<?> getPropertyType() {
        return propertyType;
    }

    /**
     * Gets the generic type
     * 获取泛型类型
     *
     * @return the generic type | 泛型类型
     */
    public Type getGenericType() {
        return genericType;
    }

    /**
     * Gets the read method (getter)
     * 获取读取方法（getter）
     *
     * @return the read method or null | 读取方法或null
     */
    public Method getReadMethod() {
        return readMethod;
    }

    /**
     * Gets the write method (setter)
     * 获取写入方法（setter）
     *
     * @return the write method or null | 写入方法或null
     */
    public Method getWriteMethod() {
        return writeMethod;
    }

    /**
     * Gets the field
     * 获取字段
     *
     * @return the field or null | 字段或null
     */
    public Field getField() {
        return field;
    }

    /**
     * Gets the declaring class
     * 获取声明类
     *
     * @return the declaring class | 声明类
     */
    public Class<?> getDeclaringClass() {
        return declaringClass;
    }

    /**
     * Checks if property is readable
     * 检查属性是否可读
     *
     * @return true if readable | 如果可读返回true
     */
    public boolean isReadable() {
        return readMethod != null || field != null;
    }

    /**
     * Checks if property is writable
     * 检查属性是否可写
     *
     * @return true if writable | 如果可写返回true
     */
    public boolean isWritable() {
        return writeMethod != null ||
                (field != null && !java.lang.reflect.Modifier.isFinal(field.getModifiers()));
    }

    /**
     * Gets the property value
     * 获取属性值
     *
     * @param target the target object | 目标对象
     * @return the value | 值
     */
    public Object getValue(Object target) {
        try {
            if (readMethod != null) {
                return readMethod.invoke(target);
            } else if (field != null) {
                return field.get(target);
            }
            throw new IllegalStateException("Property is not readable: " + name);
        } catch (Exception e) {
            throw new OpenReflectException("Failed to get property: " + name, e);
        }
    }

    /**
     * Sets the property value
     * 设置属性值
     *
     * @param target the target object | 目标对象
     * @param value  the value | 值
     */
    public void setValue(Object target, Object value) {
        try {
            if (writeMethod != null) {
                writeMethod.invoke(target, value);
            } else if (field != null && !java.lang.reflect.Modifier.isFinal(field.getModifiers())) {
                field.set(target, value);
            } else {
                throw new IllegalStateException("Property is not writable: " + name);
            }
        } catch (Exception e) {
            throw new OpenReflectException("Failed to set property: " + name, e);
        }
    }

    /**
     * Gets an annotation from this property
     * 从此属性获取注解
     *
     * @param annotationClass the annotation class | 注解类
     * @param <A>             the annotation type | 注解类型
     * @return Optional of annotation | 注解的Optional
     */
    public <A extends Annotation> Optional<A> getAnnotation(Class<A> annotationClass) {
        // Check field first
        if (field != null) {
            A annotation = field.getAnnotation(annotationClass);
            if (annotation != null) {
                return Optional.of(annotation);
            }
        }
        // Then check getter
        if (readMethod != null) {
            A annotation = readMethod.getAnnotation(annotationClass);
            if (annotation != null) {
                return Optional.of(annotation);
            }
        }
        // Finally check setter
        if (writeMethod != null) {
            A annotation = writeMethod.getAnnotation(annotationClass);
            if (annotation != null) {
                return Optional.of(annotation);
            }
        }
        return Optional.empty();
    }

    /**
     * Checks if property has annotation
     * 检查属性是否有注解
     *
     * @param annotationClass the annotation class | 注解类
     * @return true if has annotation | 如果有注解返回true
     */
    public boolean hasAnnotation(Class<? extends Annotation> annotationClass) {
        return getAnnotation(annotationClass).isPresent();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PropertyDescriptor that)) return false;
        return name.equals(that.name) && declaringClass.equals(that.declaringClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, declaringClass);
    }

    @Override
    public String toString() {
        return "PropertyDescriptor[" + declaringClass.getSimpleName() + "." + name +
                " : " + propertyType.getSimpleName() + "]";
    }
}
