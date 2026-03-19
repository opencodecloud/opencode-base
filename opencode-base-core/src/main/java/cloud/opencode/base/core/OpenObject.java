package cloud.opencode.base.core;

import java.io.*;
import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Object Utility Class - Comprehensive object operations
 * 对象工具类 - 全面的对象操作支持
 *
 * <p>Provides null-safe operations, default value handling, comparison, cloning and type checking.</p>
 * <p>提供空值安全操作、默认值处理、比较、克隆和类型检查功能。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Null checking (isNull, isEmpty, isAnyNull) - 空值检查</li>
 *   <li>Default value handling (defaultIfNull, firstNonNull) - 默认值处理</li>
 *   <li>Null-safe property access (nullSafeGet) - 空值安全属性访问</li>
 *   <li>Object comparison (equals, deepEquals, compare) - 对象比较</li>
 *   <li>Clone and serialization - 克隆和序列化</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Null checking | 空值检查
 * boolean isNull = OpenObject.isNull(obj);
 * boolean isEmpty = OpenObject.isEmpty(obj);
 *
 * // Default value | 默认值
 * String name = OpenObject.defaultIfNull(userName, "Guest");
 * String first = OpenObject.firstNonNull(a, b, c);
 *
 * // Null-safe access | 安全获取
 * String city = OpenObject.nullSafeGet(user, u -> u.getAddress().getCity(), "Unknown");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public final class OpenObject {

    private OpenObject() {
        // 工具类不可实例化
    }

    // ==================== 空值判断 ====================

    /**
     * Returns true if the object is null.
     * 检查对象是否为 null
     *
     * @param obj the object | 对象
     * @return true if null | 如果为 null 返回 true
     */
    public static boolean isNull(Object obj) {
        return obj == null;
    }

    /**
     * Returns true if the object is not null.
     * 检查对象是否非 null
     *
     * @param obj the object | 对象
     * @return true if not null | 如果非 null 返回 true
     */
    public static boolean isNotNull(Object obj) {
        return obj != null;
    }

    /**
     * Returns true if the object is considered empty.
     * 检查对象是否为空
     * <p>
     * 以下情况视为空：
     * <ul>
     *   <li>null</li>
     *   <li>空字符串 ""</li>
     *   <li>空数组 (length == 0)</li>
     *   <li>空集合 (isEmpty())</li>
     *   <li>空 Map (isEmpty())</li>
     *   <li>空 Optional (isEmpty())</li>
     * </ul>
     *
     * @param obj the object | 对象
     * @return true if empty | 如果为空返回 true
     */
    public static boolean isEmpty(Object obj) {
        if (obj == null) {
            return true;
        }
        if (obj instanceof CharSequence cs) {
            return cs.isEmpty();
        }
        if (obj instanceof Collection<?> c) {
            return c.isEmpty();
        }
        if (obj instanceof Map<?, ?> m) {
            return m.isEmpty();
        }
        if (obj.getClass().isArray()) {
            return Array.getLength(obj) == 0;
        }
        if (obj instanceof Optional<?> opt) {
            return opt.isEmpty();
        }
        return false;
    }

    /**
     * Returns true if the object is not considered empty.
     * 检查对象是否非空
     *
     * @param obj the object | 对象
     * @return true if not empty | 如果非空返回 true
     */
    public static boolean isNotEmpty(Object obj) {
        return !isEmpty(obj);
    }

    /**
     * Returns true if any of the values is null.
     * 检查是否任意一个为 null
     *
     * @param values array of objects | 对象数组
     * @return true if any value is null | 如果任意一个为 null 返回 true
     */
    public static boolean isAnyNull(Object... values) {
        if (values == null || values.length == 0) {
            return true;
        }
        for (Object value : values) {
            if (value == null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if all of the values are null.
     * 检查是否全部为 null
     *
     * @param values array of objects | 对象数组
     * @return true if all values are null | 如果全部为 null 返回 true
     */
    public static boolean isAllNull(Object... values) {
        if (values == null || values.length == 0) {
            return true;
        }
        for (Object value : values) {
            if (value != null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if any of the values is empty.
     * 检查是否任意一个为空
     *
     * @param values array of objects | 对象数组
     * @return true if any value is empty | 如果任意一个为空返回 true
     */
    public static boolean isAnyEmpty(Object... values) {
        if (values == null || values.length == 0) {
            return true;
        }
        for (Object value : values) {
            if (isEmpty(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if all of the values are empty.
     * 检查是否全部为空
     *
     * @param values array of objects | 对象数组
     * @return true if all values are empty | 如果全部为空返回 true
     */
    public static boolean isAllEmpty(Object... values) {
        if (values == null || values.length == 0) {
            return true;
        }
        for (Object value : values) {
            if (!isEmpty(value)) {
                return false;
            }
        }
        return true;
    }

    // ==================== 默认值 ====================

    /**
     * Returns the object if non-null, otherwise returns the default value.
     * 如果对象为 null，返回默认值
     *
     * @param object       the object | 对象
     * @param defaultValue default value | 默认值
     * @param <T>          object type | 对象类型
     * @return object or default value | 对象本身或默认值
     */
    public static <T> T defaultIfNull(T object, T defaultValue) {
        return object != null ? object : defaultValue;
    }

    /**
     * Returns the object if non-null, otherwise invokes the supplier for the default value.
     * 如果对象为 null，使用 Supplier 获取默认值
     *
     * @param object          the object | 对象
     * @param defaultSupplier default value supplier | 默认值提供者
     * @param <T>             object type | 对象类型
     * @return object or default value | 对象本身或默认值
     */
    public static <T> T defaultIfNull(T object, Supplier<T> defaultSupplier) {
        return object != null ? object : (defaultSupplier != null ? defaultSupplier.get() : null);
    }

    /**
     * Returns the object if non-empty, otherwise returns the default value.
     * 如果对象为空，返回默认值
     *
     * @param object       the object | 对象
     * @param defaultValue default value | 默认值
     * @param <T>          object type | 对象类型
     * @return object or default value | 对象本身或默认值
     */
    @SuppressWarnings("unchecked")
    public static <T> T defaultIfEmpty(T object, T defaultValue) {
        return isEmpty(object) ? defaultValue : object;
    }

    /**
     * Returns the first non-null value from the array, or null if all are null.
     * 返回第一个非 null 值
     *
     * @param values value array | 值数组
     * @param <T>    value type | 值类型
     * @return first non-null value, or null if all null | 第一个非 null 值，如果全部为 null 则返回 null
     */
    @SafeVarargs
    public static <T> T firstNonNull(T... values) {
        if (values == null || values.length == 0) {
            return null;
        }
        for (T value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    /**
     * Returns the first non-null value, falling back to the supplier if all are null.
     * 返回第一个非 null 值，全部为 null 时使用 Supplier
     *
     * @param defaultSupplier default value supplier | 默认值提供者
     * @param values          value array | 值数组
     * @param <T>             value type | 值类型
     * @return first non-null value or default | 第一个非 null 值或默认值
     */
    @SafeVarargs
    public static <T> T firstNonNull(Supplier<T> defaultSupplier, T... values) {
        T result = firstNonNull(values);
        return result != null ? result : (defaultSupplier != null ? defaultSupplier.get() : null);
    }

    /**
     * Returns the object if non-null, otherwise invokes the supplier (JDK 9+ style).
     * 对象非 null 则返回，否则使用 Supplier（JDK 9+ 风格）
     *
     * @param obj      the object | 对象
     * @param supplier default value supplier | 默认值提供者
     * @param <T>      object type | 对象类型
     * @return object or default value | 对象本身或默认值
     */
    public static <T> T requireNonNullElseGet(T obj, Supplier<? extends T> supplier) {
        return obj != null ? obj : Objects.requireNonNull(supplier, "supplier").get();
    }

    // ==================== 安全获取 ====================

    /**
     * Null-safe property accessor: applies the getter and returns null if the object is null.
     * null 安全的属性获取
     *
     * @param obj    the object | 对象
     * @param getter property getter function | 属性获取函数
     * @param <T>    object type | 对象类型
     * @param <R>    return type | 返回值类型
     * @return property value, or null if the object is null or NullPointerException occurs | 属性值，如果对象为 null 或获取过程中出现 null 则返回 null
     */
    public static <T, R> R nullSafeGet(T obj, Function<T, R> getter) {
        return nullSafeGet(obj, getter, null);
    }

    /**
     * Null-safe property accessor with default value.
     * null 安全的属性获取（带默认值）
     *
     * @param obj          the object | 对象
     * @param getter       property getter function | 属性获取函数
     * @param defaultValue default value | 默认值
     * @param <T>          object type | 对象类型
     * @param <R>          return type | 返回值类型
     * @return property value or default | 属性值或默认值
     */
    public static <T, R> R nullSafeGet(T obj, Function<T, R> getter, R defaultValue) {
        if (obj == null || getter == null) {
            return defaultValue;
        }
        try {
            R result = getter.apply(obj);
            return result != null ? result : defaultValue;
        } catch (NullPointerException e) {
            return defaultValue;
        }
    }

    /**
     * Null-safe property accessor returning an Optional.
     * 链式 null 安全获取（返回 Optional）
     *
     * @param obj    the object | 对象
     * @param getter property getter function | 属性获取函数
     * @param <T>    object type | 对象类型
     * @param <R>    return type | 返回值类型
     * @return Optional wrapping the result | Optional 包装的结果
     */
    public static <T, R> Optional<R> nullSafeGetOptional(T obj, Function<T, R> getter) {
        return Optional.ofNullable(nullSafeGet(obj, getter));
    }

    // ==================== 比较 ====================

    /**
     * Null-safe equality check.
     * null 安全的对象比较
     *
     * @param obj1 first object | 对象1
     * @param obj2 second object | 对象2
     * @return true if equal | 如果相等返回 true
     */
    public static boolean equals(Object obj1, Object obj2) {
        return Objects.equals(obj1, obj2);
    }

    /**
     * Returns true if the two objects are not equal.
     * 检查两个对象是否不相等
     *
     * @param obj1 first object | 对象1
     * @param obj2 second object | 对象2
     * @return true if not equal | 如果不相等返回 true
     */
    public static boolean notEquals(Object obj1, Object obj2) {
        return !Objects.equals(obj1, obj2);
    }

    /**
     * Performs a deep equality comparison of two objects.
     * 深度比较两个对象
     *
     * @param obj1 first object | 对象1
     * @param obj2 second object | 对象2
     * @return true if deeply equal | 如果深度相等返回 true
     */
    public static boolean deepEquals(Object obj1, Object obj2) {
        return Objects.deepEquals(obj1, obj2);
    }

    /**
     * Compares two Comparable objects, with null treated as less than non-null.
     * 比较两个 Comparable 对象
     *
     * @param c1  first object | 对象1
     * @param c2  second object | 对象2
     * @param <T> object type | 对象类型
     * @return comparison result | 比较结果
     */
    public static <T extends Comparable<? super T>> int compare(T c1, T c2) {
        return compare(c1, c2, false);
    }

    /**
     * Compares two Comparable objects with configurable null ordering.
     * 比较两个 Comparable 对象（可指定 null 排序位置）
     *
     * @param c1          first object | 对象1
     * @param c2          second object | 对象2
     * @param nullGreater whether null sorts after non-null values | null 是否排在后面
     * @param <T>         object type | 对象类型
     * @return comparison result | 比较结果
     */
    public static <T extends Comparable<? super T>> int compare(T c1, T c2, boolean nullGreater) {
        if (c1 == c2) {
            return 0;
        }
        if (c1 == null) {
            return nullGreater ? 1 : -1;
        }
        if (c2 == null) {
            return nullGreater ? -1 : 1;
        }
        return c1.compareTo(c2);
    }

    /**
     * Returns the larger of the two Comparable values.
     * 返回较大值
     *
     * @param a   first value | 值1
     * @param b   second value | 值2
     * @param <T> value type | 值类型
     * @return the larger value | 较大值
     */
    public static <T extends Comparable<? super T>> T max(T a, T b) {
        return compare(a, b) >= 0 ? a : b;
    }

    /**
     * Returns the smaller of the two Comparable values.
     * 返回较小值
     *
     * @param a   first value | 值1
     * @param b   second value | 值2
     * @param <T> value type | 值类型
     * @return the smaller value | 较小值
     */
    public static <T extends Comparable<? super T>> T min(T a, T b) {
        return compare(a, b) <= 0 ? a : b;
    }

    // ==================== 类型判断 ====================

    /**
     * Returns true if the object is a primitive type or its wrapper.
     * 检查是否为基本类型或其包装类型
     *
     * @param obj the object | 对象
     * @return true if primitive or wrapper | 如果是基本类型或包装类型返回 true
     */
    public static boolean isBasicType(Object obj) {
        if (obj == null) {
            return false;
        }
        return isPrimitiveOrWrapper(obj.getClass());
    }

    /**
     * Returns true if the object is an array.
     * 检查是否为数组
     *
     * @param obj the object | 对象
     * @return true if it is an array | 如果是数组返回 true
     */
    public static boolean isArray(Object obj) {
        return obj != null && obj.getClass().isArray();
    }

    /**
     * Returns true if the object is a primitive array.
     * 检查是否为原始类型数组
     *
     * @param obj the object | 对象
     * @return true if it is a primitive array | 如果是原始类型数组返回 true
     */
    public static boolean isPrimitiveArray(Object obj) {
        if (obj == null) {
            return false;
        }
        Class<?> clazz = obj.getClass();
        return clazz.isArray() && clazz.getComponentType().isPrimitive();
    }

    /**
     * Returns true if the object is an instance of the specified class.
     * 检查对象是否为指定类型的实例
     *
     * @param obj   the object | 对象
     * @param clazz the class | 类型
     * @return true if it is an instance | 如果是实例返回 true
     */
    public static boolean isInstance(Object obj, Class<?> clazz) {
        return clazz != null && clazz.isInstance(obj);
    }

    /**
     * Returns the runtime type of the object, or null if the object is null.
     * 获取对象的类型
     *
     * @param obj the object | 对象
     * @return the class, or null if object is null | 类型，如果对象为 null 返回 null
     */
    public static Class<?> getType(Object obj) {
        return obj != null ? obj.getClass() : null;
    }

    /**
     * Returns true if the class is a primitive wrapper type.
     * 检查是否为包装类型
     *
     * @param clazz the class | 类型
     * @return true if it is a wrapper type | 如果是包装类型返回 true
     */
    public static boolean isWrapperType(Class<?> clazz) {
        return clazz == Integer.class || clazz == Long.class || clazz == Double.class
                || clazz == Float.class || clazz == Short.class || clazz == Byte.class
                || clazz == Character.class || clazz == Boolean.class;
    }

    /**
     * Returns true if the class is a primitive type or its wrapper.
     * 检查是否为原始类型或包装类型
     *
     * @param clazz the class | 类型
     * @return true if primitive or wrapper | 如果是原始类型或包装类型返回 true
     */
    public static boolean isPrimitiveOrWrapper(Class<?> clazz) {
        return clazz != null && (clazz.isPrimitive() || isWrapperType(clazz));
    }

    // ==================== 哈希 ====================

    /**
     * Computes the hash code for the given values.
     * 计算对象的哈希值
     *
     * @param values objects to hash | 对象数组
     * @return hash code | 哈希值
     */
    public static int hashCode(Object... values) {
        return Objects.hash(values);
    }

    /**
     * Returns the identity hash code of the object.
     * 获取对象的身份哈希值
     *
     * @param obj the object | 对象
     * @return identity hash code | 身份哈希值
     */
    public static int identityHashCode(Object obj) {
        return System.identityHashCode(obj);
    }

    // ==================== 克隆 ====================

    /**
     * Clones the object if it implements Cloneable, otherwise returns null.
     * 克隆对象
     * <p>
     * 如果对象实现了 Cloneable 接口，则调用 clone 方法。
     *
     * @param obj the object | 对象
     * @param <T> object type | 对象类型
     * @return cloned object, or null if unable to clone | 克隆后的对象，如果无法克隆返回 null
     */
    @SuppressWarnings("unchecked")
    public static <T> T clone(T obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Cloneable) {
            try {
                var method = obj.getClass().getMethod("clone");
                return (T) method.invoke(obj);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Clones the object if possible, otherwise returns the original.
     * 克隆对象，如果无法克隆则返回原对象
     *
     * @param obj the object | 对象
     * @param <T> object type | 对象类型
     * @return cloned object or original | 克隆后的对象或原对象
     */
    public static <T> T cloneIfPossible(T obj) {
        T cloned = clone(obj);
        return cloned != null ? cloned : obj;
    }

    // ==================== 序列化 ====================

    /**
     * Serializes the object to a byte array.
     * 序列化对象为字节数组
     *
     * @param obj serializable object | 可序列化对象
     * @return byte array | 字节数组
     * @throws IllegalStateException if serialization fails | 如果序列化失败
     */
    public static byte[] serialize(Serializable obj) {
        if (obj == null) {
            return new byte[0];
        }
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(obj);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Serialization failed", e);
        }
    }

    /**
     * Deserializes a byte array to an object.
     * 反序列化字节数组为对象
     *
     * @param bytes byte array | 字节数组
     * @param <T>   object type | 对象类型
     * @return deserialized object | 反序列化后的对象
     * @throws IllegalStateException if deserialization fails | 如果反序列化失败
     */
    @SuppressWarnings("unchecked")
    public static <T> T deserialize(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            return (T) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException("Deserialization failed", e);
        }
    }

    // ==================== 字符串 ====================

    /**
     * Returns a string representation of the object, or "null" if the object is null.
     * 对象转字符串
     *
     * @param obj the object | 对象
     * @return string representation | 字符串表示
     */
    public static String toString(Object obj) {
        return toString(obj, "null");
    }

    /**
     * Returns a string representation of the object, or the specified default if null.
     * 对象转字符串（可指定 null 默认值）
     *
     * @param obj         the object | 对象
     * @param nullDefault value to return when object is null | null 时的默认值
     * @return string representation | 字符串表示
     */
    public static String toString(Object obj, String nullDefault) {
        if (obj == null) {
            return nullDefault;
        }
        if (obj.getClass().isArray()) {
            if (obj instanceof Object[] arr) {
                return Arrays.deepToString(arr);
            }
            if (obj instanceof int[] arr) {
                return Arrays.toString(arr);
            }
            if (obj instanceof long[] arr) {
                return Arrays.toString(arr);
            }
            if (obj instanceof double[] arr) {
                return Arrays.toString(arr);
            }
            if (obj instanceof float[] arr) {
                return Arrays.toString(arr);
            }
            if (obj instanceof boolean[] arr) {
                return Arrays.toString(arr);
            }
            if (obj instanceof byte[] arr) {
                return Arrays.toString(arr);
            }
            if (obj instanceof char[] arr) {
                return Arrays.toString(arr);
            }
            if (obj instanceof short[] arr) {
                return Arrays.toString(arr);
            }
        }
        return obj.toString();
    }

    /**
     * Returns a debug string with the object's class name, identity hash and value.
     * 转为调试字符串（包含类型信息）
     *
     * @param obj the object | 对象
     * @return debug string | 调试字符串
     */
    public static String toDebugString(Object obj) {
        if (obj == null) {
            return "null";
        }
        return obj.getClass().getSimpleName() + "@" +
                Integer.toHexString(System.identityHashCode(obj)) +
                "[" + toString(obj, "") + "]";
    }

    // ==================== Optional ====================

    /**
     * Wraps the object in an Optional.
     * 将对象包装为 Optional
     *
     * @param obj the object | 对象
     * @param <T> object type | 对象类型
     * @return Optional wrapping | Optional 包装
     */
    public static <T> Optional<T> toOptional(T obj) {
        return Optional.ofNullable(obj);
    }
}
