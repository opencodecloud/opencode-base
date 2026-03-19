package cloud.opencode.base.reflect.accessor;

import java.lang.reflect.Type;

/**
 * Property Accessor Interface
 * 属性访问器接口
 *
 * <p>Unified interface for property read/write operations.</p>
 * <p>属性读写操作的统一接口。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Property read/write operations - 属性读写操作</li>
 *   <li>Type and generic type access - 类型和泛型类型访问</li>
 *   <li>Readability/writability checking - 可读/可写检查</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * PropertyAccessor<User> accessor = PropertyAccessors.create(User.class, "name");
 * Object value = accessor.get(user);
 * accessor.set(user, "Alice");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Depends on implementation - 线程安全: 取决于实现</li>
 *   <li>Null-safe: No (caller must ensure non-null target) - 空值安全: 否（调用方须确保非空目标）</li>
 * </ul>
 *
 * @param <T> the target type | 目标类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
public interface PropertyAccessor<T> {

    /**
     * Gets the property name
     * 获取属性名
     *
     * @return the property name | 属性名
     */
    String getName();

    /**
     * Gets the property type
     * 获取属性类型
     *
     * @return the property type | 属性类型
     */
    Class<?> getType();

    /**
     * Gets the generic property type
     * 获取泛型属性类型
     *
     * @return the generic type | 泛型类型
     */
    Type getGenericType();

    /**
     * Gets the declaring class
     * 获取声明类
     *
     * @return the declaring class | 声明类
     */
    Class<T> getDeclaringClass();

    /**
     * Checks if property is readable
     * 检查属性是否可读
     *
     * @return true if readable | 如果可读返回true
     */
    boolean isReadable();

    /**
     * Checks if property is writable
     * 检查属性是否可写
     *
     * @return true if writable | 如果可写返回true
     */
    boolean isWritable();

    /**
     * Gets the property value
     * 获取属性值
     *
     * @param target the target object | 目标对象
     * @return the value | 值
     * @throws IllegalStateException if not readable | 如果不可读
     */
    Object get(T target);

    /**
     * Gets the property value with type
     * 获取属性值（带类型）
     *
     * @param target the target object | 目标对象
     * @param type   the expected type | 期望类型
     * @param <V>    the value type | 值类型
     * @return the value | 值
     */
    @SuppressWarnings("unchecked")
    default <V> V get(T target, Class<V> type) {
        Object value = get(target);
        return (V) value;
    }

    /**
     * Sets the property value
     * 设置属性值
     *
     * @param target the target object | 目标对象
     * @param value  the value | 值
     * @throws IllegalStateException if not writable | 如果不可写
     */
    void set(T target, Object value);

    /**
     * Sets the property value if writable
     * 如果可写则设置属性值
     *
     * @param target the target object | 目标对象
     * @param value  the value | 值
     * @return true if set | 如果已设置返回true
     */
    default boolean setIfWritable(T target, Object value) {
        if (isWritable()) {
            set(target, value);
            return true;
        }
        return false;
    }

    /**
     * Gets property value or default
     * 获取属性值或默认值
     *
     * @param target       the target object | 目标对象
     * @param defaultValue the default value | 默认值
     * @param <V>          the value type | 值类型
     * @return the value or default | 值或默认值
     */
    @SuppressWarnings("unchecked")
    default <V> V getOrDefault(T target, V defaultValue) {
        Object value = get(target);
        return value != null ? (V) value : defaultValue;
    }
}
