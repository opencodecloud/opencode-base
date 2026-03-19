package cloud.opencode.base.reflect.accessor;

import cloud.opencode.base.reflect.exception.OpenReflectException;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * Field-based Property Accessor
 * 基于字段的属性访问器
 *
 * <p>Accesses properties directly via Field reflection.</p>
 * <p>通过Field反射直接访问属性。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Direct field reflection access - 直接字段反射访问</li>
 *   <li>Static field support - 静态字段支持</li>
 *   <li>Final field detection - final字段检测</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * FieldAccessor<User> accessor = FieldAccessor.of(User.class, "name");
 * String name = (String) accessor.get(user);
 * accessor.set(user, "Alice");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (not synchronized) - 线程安全: 否（未同步）</li>
 *   <li>Null-safe: No (caller must ensure non-null target) - 空值安全: 否（调用方须确保非空目标）</li>
 * </ul>
 *
 * @param <T> the target type | 目标类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
public class FieldAccessor<T> implements PropertyAccessor<T> {

    private final Field field;
    private final Class<T> declaringClass;

    /**
     * Creates a FieldAccessor
     * 创建FieldAccessor
     *
     * @param field the field | 字段
     */
    @SuppressWarnings("unchecked")
    public FieldAccessor(Field field) {
        this.field = Objects.requireNonNull(field, "field must not be null");
        this.declaringClass = (Class<T>) field.getDeclaringClass();
        this.field.setAccessible(true);
    }

    /**
     * Creates a FieldAccessor for a field
     * 为字段创建FieldAccessor
     *
     * @param clazz     the class | 类
     * @param fieldName the field name | 字段名
     * @param <T>       the target type | 目标类型
     * @return the accessor | 访问器
     */
    public static <T> FieldAccessor<T> of(Class<T> clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            return new FieldAccessor<>(field);
        } catch (NoSuchFieldException e) {
            throw OpenReflectException.fieldNotFound(clazz, fieldName);
        }
    }

    /**
     * Gets the underlying Field
     * 获取底层Field
     *
     * @return the field | 字段
     */
    public Field getField() {
        return field;
    }

    @Override
    public String getName() {
        return field.getName();
    }

    @Override
    public Class<?> getType() {
        return field.getType();
    }

    @Override
    public Type getGenericType() {
        return field.getGenericType();
    }

    @Override
    public Class<T> getDeclaringClass() {
        return declaringClass;
    }

    @Override
    public boolean isReadable() {
        return true;
    }

    @Override
    public boolean isWritable() {
        return !java.lang.reflect.Modifier.isFinal(field.getModifiers());
    }

    @Override
    public Object get(T target) {
        try {
            return field.get(target);
        } catch (IllegalAccessException e) {
            throw new OpenReflectException("Failed to read field: " + field.getName(), e);
        }
    }

    @Override
    public void set(T target, Object value) {
        if (!isWritable()) {
            throw new IllegalStateException("Field is final: " + field.getName());
        }
        try {
            field.set(target, value);
        } catch (IllegalAccessException e) {
            throw new OpenReflectException("Failed to write field: " + field.getName(), e);
        }
    }

    /**
     * Gets static field value
     * 获取静态字段值
     *
     * @return the value | 值
     */
    public Object getStatic() {
        try {
            return field.get(null);
        } catch (IllegalAccessException e) {
            throw new OpenReflectException("Failed to read static field: " + field.getName(), e);
        }
    }

    /**
     * Sets static field value
     * 设置静态字段值
     *
     * @param value the value | 值
     */
    public void setStatic(Object value) {
        try {
            field.set(null, value);
        } catch (IllegalAccessException e) {
            throw new OpenReflectException("Failed to write static field: " + field.getName(), e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FieldAccessor<?> that)) return false;
        return field.equals(that.field);
    }

    @Override
    public int hashCode() {
        return field.hashCode();
    }

    @Override
    public String toString() {
        return "FieldAccessor[" + declaringClass.getSimpleName() + "." + field.getName() + "]";
    }
}
