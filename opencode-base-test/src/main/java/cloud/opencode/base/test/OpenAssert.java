package cloud.opencode.base.test;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Assertion Entry Class - Provides comprehensive assertion methods
 * 断言入口类 - 提供全面的断言方法
 *
 * <p>Zero-dependency assertion library with support for basic, collection,
 * string, numeric, and exception assertions.</p>
 * <p>零依赖断言库，支持基础、集合、字符串、数值和异常断言。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Basic assertions (true, false, null, equals) - 基础断言</li>
 *   <li>Collection assertions (empty, size, contains) - 集合断言</li>
 *   <li>String assertions (blank, contains, matches) - 字符串断言</li>
 *   <li>Numeric assertions (greater, less, between) - 数值断言</li>
 *   <li>Exception assertions (throws, doesNotThrow) - 异常断言</li>
 *   <li>Timeout assertions - 超时断言</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * OpenAssert.assertTrue(condition);
 * OpenAssert.assertEquals(expected, actual);
 * OpenAssert.assertContains("hello", list);
 * OpenAssert.assertThrows(IllegalArgumentException.class, () -> { throw new IllegalArgumentException(); });
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Yes (null checks where applicable) - 空值安全: 是（在适用处进行空值检查）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
public final class OpenAssert {

    private OpenAssert() {
    }

    // ==================== Basic Assertions | 基础断言 ====================

    /**
     * Asserts that condition is true
     * 断言条件为真
     *
     * @param condition the condition | 条件
     */
    public static void assertTrue(boolean condition) {
        assertTrue(condition, "Expected true but was false");
    }

    /**
     * Asserts that condition is true with message
     * 断言条件为真（带消息）
     *
     * @param condition the condition | 条件
     * @param message   the message | 消息
     */
    public static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    /**
     * Asserts that condition is true with lazy message
     * 断言条件为真（延迟消息）
     *
     * @param condition       the condition | 条件
     * @param messageSupplier the message supplier | 消息提供者
     */
    public static void assertTrue(boolean condition, Supplier<String> messageSupplier) {
        if (!condition) {
            throw new AssertionError(messageSupplier.get());
        }
    }

    /**
     * Asserts that condition is false
     * 断言条件为假
     *
     * @param condition the condition | 条件
     */
    public static void assertFalse(boolean condition) {
        assertFalse(condition, "Expected false but was true");
    }

    /**
     * Asserts that condition is false with message
     * 断言条件为假（带消息）
     *
     * @param condition the condition | 条件
     * @param message   the message | 消息
     */
    public static void assertFalse(boolean condition, String message) {
        assertTrue(!condition, message);
    }

    /**
     * Asserts that object is null
     * 断言对象为null
     *
     * @param obj the object | 对象
     */
    public static void assertNull(Object obj) {
        assertNull(obj, "Expected null but was: " + obj);
    }

    /**
     * Asserts that object is null with message
     * 断言对象为null（带消息）
     *
     * @param obj     the object | 对象
     * @param message the message | 消息
     */
    public static void assertNull(Object obj, String message) {
        assertTrue(obj == null, message);
    }

    /**
     * Asserts that object is not null
     * 断言对象不为null
     *
     * @param obj the object | 对象
     */
    public static void assertNotNull(Object obj) {
        assertNotNull(obj, "Expected not null but was null");
    }

    /**
     * Asserts that object is not null with message
     * 断言对象不为null（带消息）
     *
     * @param obj     the object | 对象
     * @param message the message | 消息
     */
    public static void assertNotNull(Object obj, String message) {
        assertTrue(obj != null, message);
    }

    // ==================== Equality Assertions | 相等断言 ====================

    /**
     * Asserts that objects are equal
     * 断言对象相等
     *
     * @param expected the expected value | 期望值
     * @param actual   the actual value | 实际值
     */
    public static void assertEquals(Object expected, Object actual) {
        assertEquals(expected, actual, "Expected: " + expected + " but was: " + actual);
    }

    /**
     * Asserts that objects are equal with message
     * 断言对象相等（带消息）
     *
     * @param expected the expected value | 期望值
     * @param actual   the actual value | 实际值
     * @param message  the message | 消息
     */
    public static void assertEquals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(message);
        }
    }

    /**
     * Asserts that objects are not equal
     * 断言对象不相等
     *
     * @param unexpected the unexpected value | 不期望值
     * @param actual     the actual value | 实际值
     */
    public static void assertNotEquals(Object unexpected, Object actual) {
        if (Objects.equals(unexpected, actual)) {
            throw new AssertionError("Expected not equal but was: " + actual);
        }
    }

    /**
     * Asserts that objects are the same instance
     * 断言对象是相同实例
     *
     * @param expected the expected instance | 期望实例
     * @param actual   the actual instance | 实际实例
     */
    public static void assertSame(Object expected, Object actual) {
        if (expected != actual) {
            throw new AssertionError("Expected same instance");
        }
    }

    /**
     * Asserts that objects are not the same instance
     * 断言对象不是相同实例
     *
     * @param unexpected the unexpected instance | 不期望实例
     * @param actual     the actual instance | 实际实例
     */
    public static void assertNotSame(Object unexpected, Object actual) {
        if (unexpected == actual) {
            throw new AssertionError("Expected not same instance");
        }
    }

    // ==================== Numeric Assertions | 数值断言 ====================

    /**
     * Asserts that doubles are equal within delta
     * 断言双精度数在误差范围内相等
     *
     * @param expected the expected value | 期望值
     * @param actual   the actual value | 实际值
     * @param delta    the maximum delta | 最大误差
     */
    public static void assertEquals(double expected, double actual, double delta) {
        if (Math.abs(expected - actual) > delta) {
            throw new AssertionError(
                    "Expected: " + expected + " but was: " + actual + " (delta: " + delta + ")");
        }
    }

    /**
     * Asserts that actual is greater than expected
     * 断言实际值大于期望值
     *
     * @param actual   the actual value | 实际值
     * @param expected the expected value | 期望值
     * @param <T>      the comparable type | 可比较类型
     */
    @SuppressWarnings("unchecked")
    public static <T extends Comparable<T>> void assertGreaterThan(T actual, T expected) {
        if (actual.compareTo(expected) <= 0) {
            throw new AssertionError("Expected " + actual + " > " + expected);
        }
    }

    /**
     * Asserts that actual is less than expected
     * 断言实际值小于期望值
     *
     * @param actual   the actual value | 实际值
     * @param expected the expected value | 期望值
     * @param <T>      the comparable type | 可比较类型
     */
    @SuppressWarnings("unchecked")
    public static <T extends Comparable<T>> void assertLessThan(T actual, T expected) {
        if (actual.compareTo(expected) >= 0) {
            throw new AssertionError("Expected " + actual + " < " + expected);
        }
    }

    /**
     * Asserts that actual is between min and max (inclusive)
     * 断言实际值在最小值和最大值之间（包含）
     *
     * @param actual the actual value | 实际值
     * @param min    the minimum value | 最小值
     * @param max    the maximum value | 最大值
     * @param <T>    the comparable type | 可比较类型
     */
    @SuppressWarnings("unchecked")
    public static <T extends Comparable<T>> void assertBetween(T actual, T min, T max) {
        if (actual.compareTo(min) < 0 || actual.compareTo(max) > 0) {
            throw new AssertionError("Expected " + actual + " between " + min + " and " + max);
        }
    }

    // ==================== Collection Assertions | 集合断言 ====================

    /**
     * Asserts that collection is empty
     * 断言集合为空
     *
     * @param collection the collection | 集合
     */
    public static void assertEmpty(Collection<?> collection) {
        if (collection == null || !collection.isEmpty()) {
            throw new AssertionError("Expected empty collection");
        }
    }

    /**
     * Asserts that collection is not empty
     * 断言集合不为空
     *
     * @param collection the collection | 集合
     */
    public static void assertNotEmpty(Collection<?> collection) {
        if (collection == null || collection.isEmpty()) {
            throw new AssertionError("Expected non-empty collection");
        }
    }

    /**
     * Asserts collection size
     * 断言集合大小
     *
     * @param expected   the expected size | 期望大小
     * @param collection the collection | 集合
     */
    public static void assertSize(int expected, Collection<?> collection) {
        assertNotNull(collection);
        if (collection.size() != expected) {
            throw new AssertionError("Expected size: " + expected + " but was: " + collection.size());
        }
    }

    /**
     * Asserts that collection contains element
     * 断言集合包含元素
     *
     * @param element    the element | 元素
     * @param collection the collection | 集合
     */
    public static void assertContains(Object element, Collection<?> collection) {
        assertNotNull(collection);
        if (!collection.contains(element)) {
            throw new AssertionError("Collection does not contain: " + element);
        }
    }

    /**
     * Asserts that map is empty
     * 断言映射为空
     *
     * @param map the map | 映射
     */
    public static void assertEmpty(Map<?, ?> map) {
        if (map == null || !map.isEmpty()) {
            throw new AssertionError("Expected empty map");
        }
    }

    /**
     * Asserts that map contains key
     * 断言映射包含键
     *
     * @param key the key | 键
     * @param map the map | 映射
     */
    public static void assertContainsKey(Object key, Map<?, ?> map) {
        assertNotNull(map);
        if (!map.containsKey(key)) {
            throw new AssertionError("Map does not contain key: " + key);
        }
    }

    // ==================== String Assertions | 字符串断言 ====================

    /**
     * Asserts that string is blank
     * 断言字符串为空白
     *
     * @param str the string | 字符串
     */
    public static void assertBlank(String str) {
        if (str != null && !str.isBlank()) {
            throw new AssertionError("Expected blank string but was: " + str);
        }
    }

    /**
     * Asserts that string is not blank
     * 断言字符串不为空白
     *
     * @param str the string | 字符串
     */
    public static void assertNotBlank(String str) {
        if (str == null || str.isBlank()) {
            throw new AssertionError("Expected non-blank string");
        }
    }

    /**
     * Asserts that string contains substring
     * 断言字符串包含子串
     *
     * @param expected the expected substring | 期望子串
     * @param actual   the actual string | 实际字符串
     */
    public static void assertContains(String expected, String actual) {
        assertNotNull(actual);
        if (!actual.contains(expected)) {
            throw new AssertionError("Expected string to contain: " + expected + " but was: " + actual);
        }
    }

    /**
     * Asserts that string starts with prefix
     * 断言字符串以前缀开始
     *
     * @param prefix the prefix | 前缀
     * @param actual the actual string | 实际字符串
     */
    public static void assertStartsWith(String prefix, String actual) {
        assertNotNull(actual);
        if (!actual.startsWith(prefix)) {
            throw new AssertionError("Expected string to start with: " + prefix);
        }
    }

    /**
     * Asserts that string ends with suffix
     * 断言字符串以后缀结束
     *
     * @param suffix the suffix | 后缀
     * @param actual the actual string | 实际字符串
     */
    public static void assertEndsWith(String suffix, String actual) {
        assertNotNull(actual);
        if (!actual.endsWith(suffix)) {
            throw new AssertionError("Expected string to end with: " + suffix);
        }
    }

    /**
     * Asserts that string matches regex
     * 断言字符串匹配正则表达式
     *
     * @param regex  the regex pattern | 正则表达式
     * @param actual the actual string | 实际字符串
     */
    public static void assertMatches(String regex, String actual) {
        assertNotNull(actual);
        if (!actual.matches(regex)) {
            throw new AssertionError("Expected string to match: " + regex);
        }
    }

    // ==================== Exception Assertions | 异常断言 ====================

    /**
     * Asserts that executable throws expected exception
     * 断言可执行对象抛出预期异常
     *
     * @param expectedType the expected exception type | 期望异常类型
     * @param executable   the executable | 可执行对象
     * @param <T>          the exception type | 异常类型
     * @return the thrown exception | 抛出的异常
     */
    public static <T extends Throwable> T assertThrows(Class<T> expectedType, Executable executable) {
        try {
            executable.execute();
            throw new AssertionError("Expected " + expectedType.getName() + " to be thrown");
        } catch (Throwable t) {
            if (expectedType.isInstance(t)) {
                return expectedType.cast(t);
            }
            throw new AssertionError(
                    "Expected " + expectedType.getName() + " but was " + t.getClass().getName(), t);
        }
    }

    /**
     * Asserts that executable does not throw
     * 断言可执行对象不抛出异常
     *
     * @param executable the executable | 可执行对象
     */
    public static void assertDoesNotThrow(Executable executable) {
        try {
            executable.execute();
        } catch (Throwable t) {
            throw new AssertionError("Expected no exception but was: " + t, t);
        }
    }

    // ==================== Timeout Assertions | 超时断言 ====================

    /**
     * Asserts that executable completes within timeout
     * 断言可执行对象在超时内完成
     *
     * @param timeout    the timeout duration | 超时时间
     * @param executable the executable | 可执行对象
     */
    public static void assertTimeout(Duration timeout, Executable executable) {
        long start = System.nanoTime();
        try {
            executable.execute();
        } catch (Throwable t) {
            throw new AssertionError("Execution failed", t);
        }
        long elapsed = System.nanoTime() - start;
        if (elapsed > timeout.toNanos()) {
            throw new AssertionError(
                    "Execution exceeded timeout of " + timeout.toMillis() + "ms (actual: " +
                            (elapsed / 1_000_000) + "ms)");
        }
    }

    /**
     * Fails unconditionally
     * 无条件失败
     */
    public static void fail() {
        throw new AssertionError("Test failed");
    }

    /**
     * Fails unconditionally with message
     * 无条件失败（带消息）
     *
     * @param message the message | 消息
     */
    public static void fail(String message) {
        throw new AssertionError(message);
    }

    // ==================== Functional Interface | 函数式接口 ====================

    /**
     * Executable functional interface for exception assertions
     * 用于异常断言的可执行函数式接口
     */
    @FunctionalInterface
    public interface Executable {
        /**
         * Executes the operation
         * 执行操作
         *
         * @throws Throwable if execution fails | 如果执行失败
         */
        void execute() throws Throwable;
    }
}
