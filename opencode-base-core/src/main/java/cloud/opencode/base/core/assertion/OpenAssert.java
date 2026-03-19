package cloud.opencode.base.core.assertion;

import cloud.opencode.base.core.exception.OpenIllegalArgumentException;
import cloud.opencode.base.core.exception.OpenIllegalStateException;

import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Assertion Utility - Comprehensive assertion methods for validation
 * 断言工具类 - 全面的验证断言方法
 *
 * <p>Provides assertion methods that throw OpenException subclasses for validation failures.</p>
 * <p>提供验证失败时抛出 OpenException 子类的断言方法。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Null checks (notNull) - 空值检查</li>
 *   <li>Boolean checks (isTrue, isFalse, state) - 布尔检查</li>
 *   <li>String checks (notEmpty, notBlank, matchesPattern) - 字符串检查</li>
 *   <li>Collection checks (notEmpty, noNullElements) - 集合检查</li>
 *   <li>Range checks (inclusiveBetween, exclusiveBetween) - 范围检查</li>
 *   <li>Index checks (validIndex) - 索引检查</li>
 *   <li>Type checks (isInstanceOf, isAssignableFrom) - 类型检查</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * OpenAssert.notNull(user, "User must not be null");
 * OpenAssert.notBlank(name, "Name must not be blank");
 * OpenAssert.isTrue(age > 0, "Age must be positive");
 * OpenAssert.inclusiveBetween(1, 100, value);
 * OpenAssert.validIndex(index, list.size());
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是 (无状态)</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public final class OpenAssert {

    private OpenAssert() {
    }

    // ==================== 基础断言 ====================

    /**
     * Asserts
     * 断言对象非 null
     */
    public static <T> T notNull(T object, String message) {
        if (object == null) {
            throw new OpenIllegalArgumentException(message);
        }
        return object;
    }

    /**
     * Asserts
     * 断言对象非 null（带格式化参数）
     */
    public static <T> T notNull(T object, String template, Object... args) {
        if (object == null) {
            throw new OpenIllegalArgumentException(String.format(template, args));
        }
        return object;
    }

    /**
     * Asserts
     * 断言条件为 true
     */
    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            throw new OpenIllegalArgumentException(message);
        }
    }

    /**
     * Asserts
     * 断言条件为 true（带格式化参数）
     */
    public static void isTrue(boolean expression, String template, Object... args) {
        if (!expression) {
            throw new OpenIllegalArgumentException(String.format(template, args));
        }
    }

    /**
     * Asserts
     * 断言条件为 false
     */
    public static void isFalse(boolean expression, String message) {
        if (expression) {
            throw new OpenIllegalArgumentException(message);
        }
    }

    /**
     * Asserts
     * 断言状态条件
     */
    public static void state(boolean expression, String message) {
        if (!expression) {
            throw new OpenIllegalStateException(message);
        }
    }

    // ==================== 字符串断言 ====================

    /**
     * Asserts
     * 断言字符串非空
     */
    public static <T extends CharSequence> T notEmpty(T cs, String message) {
        if (cs == null || cs.length() == 0) {
            throw new OpenIllegalArgumentException(message);
        }
        return cs;
    }

    /**
     * Asserts
     * 断言字符串非空白
     */
    public static <T extends CharSequence> T notBlank(T cs, String message) {
        if (cs == null || cs.toString().isBlank()) {
            throw new OpenIllegalArgumentException(message);
        }
        return cs;
    }

    /**
     * Asserts
     * 断言字符串匹配正则
     */
    public static void matchesPattern(CharSequence input, String pattern, String message) {
        if (input == null || !Pattern.matches(pattern, input)) {
            throw new OpenIllegalArgumentException(message);
        }
    }

    // ==================== 集合断言 ====================

    /**
     * Asserts
     * 断言集合非空
     */
    public static <T extends Collection<?>> T notEmpty(T collection, String message) {
        if (collection == null || collection.isEmpty()) {
            throw new OpenIllegalArgumentException(message);
        }
        return collection;
    }

    /**
     * Asserts
     * 断言 Map 非空
     */
    public static <T extends Map<?, ?>> T notEmpty(T map, String message) {
        if (map == null || map.isEmpty()) {
            throw new OpenIllegalArgumentException(message);
        }
        return map;
    }

    /**
     * Asserts
     * 断言数组非空
     */
    public static <T> T[] notEmpty(T[] array, String message) {
        if (array == null || array.length == 0) {
            throw new OpenIllegalArgumentException(message);
        }
        return array;
    }

    /**
     * Asserts
     * 断言数组无 null 元素
     */
    public static <T> T[] noNullElements(T[] array, String message) {
        if (array != null) {
            for (T element : array) {
                if (element == null) {
                    throw new OpenIllegalArgumentException(message);
                }
            }
        }
        return array;
    }

    /**
     * Asserts
     * 断言集合无 null 元素
     */
    public static <T extends Iterable<?>> T noNullElements(T iterable, String message) {
        if (iterable != null) {
            for (Object element : iterable) {
                if (element == null) {
                    throw new OpenIllegalArgumentException(message);
                }
            }
        }
        return iterable;
    }

    // ==================== 范围断言 ====================

    /**
     * Asserts
     * 断言值在包含边界的范围内 [start, end]
     */
    public static <T extends Comparable<T>> T inclusiveBetween(T start, T end, T value) {
        return inclusiveBetween(start, end, value, "Value must be between %s and %s (inclusive)");
    }

    /**
     * Asserts
     * 断言值在包含边界的范围内 [start, end]
     */
    public static <T extends Comparable<T>> T inclusiveBetween(T start, T end, T value, String message) {
        if (value.compareTo(start) < 0 || value.compareTo(end) > 0) {
            throw new OpenIllegalArgumentException(String.format(message, start, end));
        }
        return value;
    }

    /**
     * Asserts
     * 断言 long 值在包含边界的范围内
     */
    public static void inclusiveBetween(long start, long end, long value) {
        if (value < start || value > end) {
            throw new OpenIllegalArgumentException(
                    String.format("Value %d must be between %d and %d (inclusive)", value, start, end));
        }
    }

    /**
     * Asserts
     * 断言 double 值在包含边界的范围内
     */
    public static void inclusiveBetween(double start, double end, double value) {
        if (value < start || value > end) {
            throw new OpenIllegalArgumentException(
                    String.format("Value %f must be between %f and %f (inclusive)", value, start, end));
        }
    }

    /**
     * Asserts
     * 断言值在排除边界的范围内 (start, end)
     */
    public static <T extends Comparable<T>> T exclusiveBetween(T start, T end, T value) {
        return exclusiveBetween(start, end, value, "Value must be between %s and %s (exclusive)");
    }

    /**
     * Asserts
     * 断言值在排除边界的范围内 (start, end)
     */
    public static <T extends Comparable<T>> T exclusiveBetween(T start, T end, T value, String message) {
        if (value.compareTo(start) <= 0 || value.compareTo(end) >= 0) {
            throw new OpenIllegalArgumentException(String.format(message, start, end));
        }
        return value;
    }

    /**
     * Asserts
     * 断言 long 值在排除边界的范围内
     */
    public static void exclusiveBetween(long start, long end, long value) {
        if (value <= start || value >= end) {
            throw new OpenIllegalArgumentException(
                    String.format("Value %d must be between %d and %d (exclusive)", value, start, end));
        }
    }

    /**
     * Asserts
     * 断言 double 值在排除边界的范围内
     */
    public static void exclusiveBetween(double start, double end, double value) {
        if (value <= start || value >= end) {
            throw new OpenIllegalArgumentException(
                    String.format("Value %f must be between %f and %f (exclusive)", value, start, end));
        }
    }

    // ==================== 索引断言 ====================

    /**
     * Asserts
     * 断言索引有效 [0, size)
     */
    public static void validIndex(int index, int size) {
        validIndex(index, size, "Index %d out of bounds for size %d");
    }

    /**
     * Asserts
     * 断言索引有效 [0, size)
     */
    public static void validIndex(int index, int size, String message) {
        if (index < 0 || index >= size) {
            throw new OpenIllegalArgumentException(String.format(message, index, size));
        }
    }

    /**
     * Asserts
     * 断言数组索引有效
     */
    public static <T> T[] validIndex(T[] array, int index, String message) {
        notNull(array, "Array must not be null");
        if (index < 0 || index >= array.length) {
            throw new OpenIllegalArgumentException(message);
        }
        return array;
    }

    /**
     * Asserts
     * 断言字符序列索引有效
     */
    public static <T extends CharSequence> T validIndex(T chars, int index, String message) {
        notNull(chars, "CharSequence must not be null");
        if (index < 0 || index >= chars.length()) {
            throw new OpenIllegalArgumentException(message);
        }
        return chars;
    }

    /**
     * Asserts
     * 断言集合索引有效
     */
    public static <T extends Collection<?>> T validIndex(T collection, int index, String message) {
        notNull(collection, "Collection must not be null");
        if (index < 0 || index >= collection.size()) {
            throw new OpenIllegalArgumentException(message);
        }
        return collection;
    }

    // ==================== 类型断言 ====================

    /**
     * Asserts
     * 断言对象是指定类型的实例
     */
    public static void isInstanceOf(Class<?> type, Object obj, String message) {
        notNull(type, "Type must not be null");
        if (!type.isInstance(obj)) {
            throw new OpenIllegalArgumentException(message);
        }
    }

    /**
     * Asserts
     * 断言类型可赋值
     */
    public static void isAssignableFrom(Class<?> superType, Class<?> subType, String message) {
        notNull(superType, "SuperType must not be null");
        if (subType == null || !superType.isAssignableFrom(subType)) {
            throw new OpenIllegalArgumentException(message);
        }
    }
}
