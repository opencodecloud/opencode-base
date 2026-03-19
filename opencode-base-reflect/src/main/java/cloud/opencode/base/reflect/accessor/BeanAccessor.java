package cloud.opencode.base.reflect.accessor;

import cloud.opencode.base.reflect.OpenMethod;
import cloud.opencode.base.reflect.exception.OpenReflectException;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * Bean Property Accessor (via getter/setter)
 * Bean属性访问器（通过getter/setter）
 *
 * <p>Accesses properties via getter and setter methods.</p>
 * <p>通过getter和setter方法访问属性。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Getter/setter-based property access - 基于getter/setter的属性访问</li>
 *   <li>Auto-discovery of getter/setter methods - 自动发现getter/setter方法</li>
 *   <li>Boolean property support (isXxx) - Boolean属性支持（isXxx）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * BeanAccessor<User> accessor = BeanAccessor.of(User.class, "name");
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
public class BeanAccessor<T> implements PropertyAccessor<T> {

    private final String name;
    private final Class<T> declaringClass;
    private final Method getter;
    private final Method setter;
    private final Class<?> type;
    private final Type genericType;

    /**
     * Creates a BeanAccessor
     * 创建BeanAccessor
     *
     * @param name           the property name | 属性名
     * @param declaringClass the declaring class | 声明类
     * @param getter         the getter method (can be null) | getter方法（可为null）
     * @param setter         the setter method (can be null) | setter方法（可为null）
     */
    public BeanAccessor(String name, Class<T> declaringClass, Method getter, Method setter) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.declaringClass = Objects.requireNonNull(declaringClass, "declaringClass must not be null");

        if (getter == null && setter == null) {
            throw new IllegalArgumentException("At least one of getter or setter must be provided");
        }

        this.getter = getter;
        this.setter = setter;

        if (getter != null) {
            getter.setAccessible(true);
            this.type = getter.getReturnType();
            this.genericType = getter.getGenericReturnType();
        } else {
            setter.setAccessible(true);
            this.type = setter.getParameterTypes()[0];
            this.genericType = setter.getGenericParameterTypes()[0];
        }
    }

    /**
     * Creates a BeanAccessor for a property
     * 为属性创建BeanAccessor
     *
     * @param clazz        the class | 类
     * @param propertyName the property name | 属性名
     * @param <T>          the target type | 目标类型
     * @return the accessor | 访问器
     */
    public static <T> BeanAccessor<T> of(Class<T> clazz, String propertyName) {
        Method getter = findGetter(clazz, propertyName);
        Method setter = findSetter(clazz, propertyName, getter != null ? getter.getReturnType() : null);

        if (getter == null && setter == null) {
            throw OpenReflectException.fieldNotFound(clazz, propertyName);
        }

        return new BeanAccessor<>(propertyName, clazz, getter, setter);
    }

    /**
     * Gets the getter method
     * 获取getter方法
     *
     * @return the getter or null | getter或null
     */
    public Method getGetter() {
        return getter;
    }

    /**
     * Gets the setter method
     * 获取setter方法
     *
     * @return the setter or null | setter或null
     */
    public Method getSetter() {
        return setter;
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
        } catch (Exception e) {
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
        } catch (Exception e) {
            throw new OpenReflectException("Failed to write property: " + name, e);
        }
    }

    private static Method findGetter(Class<?> clazz, String propertyName) {
        String capitalized = capitalize(propertyName);

        // Try getXxx
        try {
            Method method = clazz.getMethod("get" + capitalized);
            if (method.getReturnType() != void.class) {
                return method;
            }
        } catch (NoSuchMethodException ignored) {
        }

        // Try isXxx for boolean
        try {
            Method method = clazz.getMethod("is" + capitalized);
            if (method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class) {
                return method;
            }
        } catch (NoSuchMethodException ignored) {
        }

        return null;
    }

    private static Method findSetter(Class<?> clazz, String propertyName, Class<?> type) {
        String setterName = "set" + capitalize(propertyName);

        // If we know the type, try exact match
        if (type != null) {
            try {
                return clazz.getMethod(setterName, type);
            } catch (NoSuchMethodException ignored) {
            }
        }

        // Search for any setter with that name
        for (Method method : clazz.getMethods()) {
            if (method.getName().equals(setterName) && method.getParameterCount() == 1) {
                return method;
            }
        }

        return null;
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BeanAccessor<?> that)) return false;
        return name.equals(that.name) && declaringClass.equals(that.declaringClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, declaringClass);
    }

    @Override
    public String toString() {
        return "BeanAccessor[" + declaringClass.getSimpleName() + "." + name + "]";
    }
}
