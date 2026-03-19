package cloud.opencode.base.reflect.accessor;

import cloud.opencode.base.reflect.exception.OpenReflectException;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * MethodHandle-based Property Accessor
 * 基于MethodHandle的属性访问器
 *
 * <p>High-performance property access using MethodHandle.
 * Faster than reflection after warmup.</p>
 * <p>使用MethodHandle的高性能属性访问。
 * 预热后比反射更快。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>MethodHandle-based high-performance access - 基于MethodHandle的高性能访问</li>
 *   <li>Field and method-based accessor creation - 基于字段和方法的访问器创建</li>
 *   <li>Faster than reflection after JIT warmup - JIT预热后比反射更快</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * MethodHandleAccessor<User> accessor = MethodHandleAccessor.of(User.class, "name");
 * String name = (String) accessor.get(user);
 * accessor.set(user, "Alice");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (MethodHandle is immutable and thread-safe) - 线程安全: 是（MethodHandle不可变且线程安全）</li>
 *   <li>Null-safe: No (caller must ensure non-null target) - 空值安全: 否（调用方须确保非空目标）</li>
 * </ul>
 *
 * @param <T> the target type | 目标类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
public class MethodHandleAccessor<T> implements PropertyAccessor<T> {

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    private final String name;
    private final Class<T> declaringClass;
    private final Class<?> type;
    private final Type genericType;
    private final MethodHandle getter;
    private final MethodHandle setter;

    private MethodHandleAccessor(String name, Class<T> declaringClass, Class<?> type,
                                  Type genericType, MethodHandle getter, MethodHandle setter) {
        this.name = name;
        this.declaringClass = declaringClass;
        this.type = type;
        this.genericType = genericType;
        this.getter = getter;
        this.setter = setter;
    }

    /**
     * Creates a MethodHandleAccessor from a field
     * 从字段创建MethodHandleAccessor
     *
     * @param field the field | 字段
     * @param <T>   the target type | 目标类型
     * @return the accessor | 访问器
     */
    @SuppressWarnings("unchecked")
    public static <T> MethodHandleAccessor<T> fromField(Field field) {
        Objects.requireNonNull(field, "field must not be null");
        field.setAccessible(true);

        try {
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(field.getDeclaringClass(), LOOKUP);
            MethodHandle getter = lookup.unreflectGetter(field);
            MethodHandle setter = java.lang.reflect.Modifier.isFinal(field.getModifiers())
                    ? null : lookup.unreflectSetter(field);

            return new MethodHandleAccessor<>(
                    field.getName(),
                    (Class<T>) field.getDeclaringClass(),
                    field.getType(),
                    field.getGenericType(),
                    getter,
                    setter
            );
        } catch (IllegalAccessException e) {
            throw new OpenReflectException("Failed to create MethodHandle for field: " + field.getName(), e);
        }
    }

    /**
     * Creates a MethodHandleAccessor from getter/setter methods
     * 从getter/setter方法创建MethodHandleAccessor
     *
     * @param name           the property name | 属性名
     * @param declaringClass the declaring class | 声明类
     * @param getter         the getter method (can be null) | getter方法（可为null）
     * @param setter         the setter method (can be null) | setter方法（可为null）
     * @param <T>            the target type | 目标类型
     * @return the accessor | 访问器
     */
    public static <T> MethodHandleAccessor<T> fromMethods(String name, Class<T> declaringClass,
                                                           Method getter, Method setter) {
        if (getter == null && setter == null) {
            throw new IllegalArgumentException("At least one of getter or setter must be provided");
        }

        try {
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(declaringClass, LOOKUP);

            MethodHandle getterHandle = null;
            MethodHandle setterHandle = null;
            Class<?> type;
            Type genericType;

            if (getter != null) {
                getter.setAccessible(true);
                getterHandle = lookup.unreflect(getter);
                type = getter.getReturnType();
                genericType = getter.getGenericReturnType();
            } else {
                setter.setAccessible(true);
                type = setter.getParameterTypes()[0];
                genericType = setter.getGenericParameterTypes()[0];
            }

            if (setter != null) {
                setter.setAccessible(true);
                setterHandle = lookup.unreflect(setter);
            }

            return new MethodHandleAccessor<>(name, declaringClass, type, genericType, getterHandle, setterHandle);
        } catch (IllegalAccessException e) {
            throw new OpenReflectException("Failed to create MethodHandle for property: " + name, e);
        }
    }

    /**
     * Creates a MethodHandleAccessor for a field by name
     * 按名称为字段创建MethodHandleAccessor
     *
     * @param clazz     the class | 类
     * @param fieldName the field name | 字段名
     * @param <T>       the target type | 目标类型
     * @return the accessor | 访问器
     */
    public static <T> MethodHandleAccessor<T> of(Class<T> clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            return fromField(field);
        } catch (NoSuchFieldException e) {
            throw OpenReflectException.fieldNotFound(clazz, fieldName);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public Type getGenericType() {
        return genericType;
    }

    @Override
    public Class<T> getDeclaringClass() {
        return declaringClass;
    }

    @Override
    public boolean isReadable() {
        return getter != null;
    }

    @Override
    public boolean isWritable() {
        return setter != null;
    }

    @Override
    public Object get(T target) {
        if (getter == null) {
            throw new IllegalStateException("Property is not readable: " + name);
        }
        try {
            return getter.invoke(target);
        } catch (Throwable e) {
            throw new OpenReflectException("Failed to read property: " + name, e);
        }
    }

    @Override
    public void set(T target, Object value) {
        if (setter == null) {
            throw new IllegalStateException("Property is not writable: " + name);
        }
        try {
            setter.invoke(target, value);
        } catch (Throwable e) {
            throw new OpenReflectException("Failed to write property: " + name, e);
        }
    }

    /**
     * Gets the getter MethodHandle
     * 获取getter MethodHandle
     *
     * @return the getter handle or null | getter句柄或null
     */
    public MethodHandle getGetterHandle() {
        return getter;
    }

    /**
     * Gets the setter MethodHandle
     * 获取setter MethodHandle
     *
     * @return the setter handle or null | setter句柄或null
     */
    public MethodHandle getSetterHandle() {
        return setter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MethodHandleAccessor<?> that)) return false;
        return name.equals(that.name) && declaringClass.equals(that.declaringClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, declaringClass);
    }

    @Override
    public String toString() {
        return "MethodHandleAccessor[" + declaringClass.getSimpleName() + "." + name + "]";
    }
}
