package cloud.opencode.base.test.data;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * EdgeCases - Generates boundary and edge case values for common types
 * 边界用例 - 为常见类型生成边界值和边缘用例值
 *
 * <p>Provides pre-built lists of boundary values for primitive types, strings,
 * collections, and date/time types. Useful for parameterized tests that need
 * to cover edge conditions.</p>
 * <p>为基本类型、字符串、集合和日期/时间类型提供预构建的边界值列表。
 * 适用于需要覆盖边界条件的参数化测试。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Numeric boundary values (min, max, zero, overflow edges) - 数值边界值（最小、最大、零、溢出边界）</li>
 *   <li>String edge cases (null, empty, whitespace) - 字符串边缘用例（null、空、空白）</li>
 *   <li>Collection edge cases (null, empty, singleton with null) - 集合边缘用例（null、空、含null的单元素）</li>
 *   <li>Date and Duration boundaries - 日期和持续时间边界</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Test all integer edge cases
 * for (Integer value : EdgeCases.forInt()) {
 *     assertDoesNotThrow(() -> myMethod(value));
 * }
 *
 * // Use with JUnit @MethodSource
 * static Stream<String> stringEdgeCases() {
 *     return EdgeCases.forString().stream();
 * }
 *
 * // Non-null string variants
 * for (String s : EdgeCases.forStringNonNull()) {
 *     assertEquals(s, myTrimMethod(s).trim());
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (all methods return immutable or independent lists) - 线程安全: 是（所有方法返回不可变或独立的列表）</li>
 *   <li>Null-safe: Some lists contain null by design - 空值安全: 部分列表设计上包含null</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.3
 */
public final class EdgeCases {

    private static final String LONG_STRING;
    private static final List<String> STRING_EDGE_CASES;
    private static final List<String> STRING_NON_NULL_EDGE_CASES;
    private static final List<List<?>> LIST_EDGE_CASES;
    private static final List<List<?>> LIST_NON_NULL_EDGE_CASES;

    static {
        char[] chars = new char[128];
        java.util.Arrays.fill(chars, 'a');
        LONG_STRING = new String(chars);

        List<String> strList = new ArrayList<>(7);
        strList.add(null);
        strList.add("");
        strList.add(" ");
        strList.add("\t");
        strList.add("\n");
        strList.add("a");
        strList.add(LONG_STRING);
        STRING_EDGE_CASES = Collections.unmodifiableList(strList);

        STRING_NON_NULL_EDGE_CASES = List.of("", " ", "\t", "\n", "a", LONG_STRING);

        List<List<?>> listWithNull = new ArrayList<>(3);
        listWithNull.add(null);
        listWithNull.add(Collections.emptyList());
        listWithNull.add(Collections.singletonList(null));
        LIST_EDGE_CASES = Collections.unmodifiableList(listWithNull);

        List<List<?>> listNonNull = new ArrayList<>(2);
        listNonNull.add(Collections.emptyList());
        listNonNull.add(Collections.singletonList(null));
        LIST_NON_NULL_EDGE_CASES = Collections.unmodifiableList(listNonNull);
    }

    private EdgeCases() {
        // utility class
    }

    /**
     * Returns edge case values for int/Integer.
     * 返回int/Integer的边缘用例值。
     *
     * <p>Includes: MIN_VALUE, -1, 0, 1, MAX_VALUE</p>
     * <p>包括: MIN_VALUE, -1, 0, 1, MAX_VALUE</p>
     *
     * @return list of edge case integers | 边缘用例整数列表
     */
    public static List<Integer> forInt() {
        return List.of(
            Integer.MIN_VALUE,
            -1,
            0,
            1,
            Integer.MAX_VALUE
        );
    }

    /**
     * Returns edge case values for long/Long.
     * 返回long/Long的边缘用例值。
     *
     * <p>Includes: MIN_VALUE, -1L, 0L, 1L, MAX_VALUE</p>
     * <p>包括: MIN_VALUE, -1L, 0L, 1L, MAX_VALUE</p>
     *
     * @return list of edge case longs | 边缘用例长整数列表
     */
    public static List<Long> forLong() {
        return List.of(
            Long.MIN_VALUE,
            -1L,
            0L,
            1L,
            Long.MAX_VALUE
        );
    }

    /**
     * Returns edge case values for double/Double.
     * 返回double/Double的边缘用例值。
     *
     * <p>Includes: NEGATIVE_INFINITY, MIN_VALUE, -1.0, -0.0, 0.0,
     * MIN_NORMAL, 1.0, MAX_VALUE, POSITIVE_INFINITY, NaN</p>
     * <p>包括: 负无穷, 最小值, -1.0, -0.0, 0.0,
     * 最小正常值, 1.0, 最大值, 正无穷, NaN</p>
     *
     * @return list of edge case doubles | 边缘用例双精度浮点数列表
     */
    public static List<Double> forDouble() {
        return List.of(
            Double.NEGATIVE_INFINITY,
            Double.MIN_VALUE,
            -1.0,
            -0.0,
            0.0,
            Double.MIN_NORMAL,
            1.0,
            Double.MAX_VALUE,
            Double.POSITIVE_INFINITY,
            Double.NaN
        );
    }

    /**
     * Returns edge case values for float/Float.
     * 返回float/Float的边缘用例值。
     *
     * <p>Includes: NEGATIVE_INFINITY, MIN_VALUE, -1.0f, -0.0f, 0.0f,
     * MIN_NORMAL, 1.0f, MAX_VALUE, POSITIVE_INFINITY, NaN</p>
     * <p>包括: 负无穷, 最小值, -1.0f, -0.0f, 0.0f,
     * 最小正常值, 1.0f, 最大值, 正无穷, NaN</p>
     *
     * @return list of edge case floats | 边缘用例单精度浮点数列表
     */
    public static List<Float> forFloat() {
        return List.of(
            Float.NEGATIVE_INFINITY,
            Float.MIN_VALUE,
            -1.0f,
            -0.0f,
            0.0f,
            Float.MIN_NORMAL,
            1.0f,
            Float.MAX_VALUE,
            Float.POSITIVE_INFINITY,
            Float.NaN
        );
    }

    /**
     * Returns edge case values for String, including null.
     * 返回String的边缘用例值，包括null。
     *
     * <p>Includes: null, "", " ", "\t", "\n", "a", 128-char string</p>
     * <p>包括: null, 空串, 空格, 制表符, 换行符, 单字符, 128字符串</p>
     *
     * @return list of edge case strings (contains null) | 边缘用例字符串列表（包含null）
     */
    public static List<String> forString() {
        return STRING_EDGE_CASES;
    }

    /**
     * Returns edge case values for String, excluding null.
     * 返回String的边缘用例值，不包括null。
     *
     * <p>Includes: "", " ", "\t", "\n", "a", 128-char string</p>
     * <p>包括: 空串, 空格, 制表符, 换行符, 单字符, 128字符串</p>
     *
     * @return list of edge case strings (no null) | 边缘用例字符串列表（无null）
     */
    public static List<String> forStringNonNull() {
        return STRING_NON_NULL_EDGE_CASES;
    }

    /**
     * Returns edge case values for List, including null.
     * 返回List的边缘用例值，包括null。
     *
     * <p>Includes: null, empty list, singleton list containing null</p>
     * <p>包括: null, 空列表, 包含null的单元素列表</p>
     *
     * @param <T> the element type | 元素类型
     * @return list of edge case lists (contains null entries) | 边缘用例列表的列表（包含null条目）
     */
    @SuppressWarnings("unchecked")
    public static <T> List<List<T>> forList() {
        return (List<List<T>>) (List<?>) LIST_EDGE_CASES;
    }

    /**
     * Returns edge case values for List, excluding null.
     * 返回List的边缘用例值，不包括null。
     *
     * <p>Includes: empty list, singleton list containing null</p>
     * <p>包括: 空列表, 包含null的单元素列表</p>
     *
     * @param <T> the element type | 元素类型
     * @return list of edge case lists (no null list) | 边缘用例列表的列表（无null列表）
     */
    @SuppressWarnings("unchecked")
    public static <T> List<List<T>> forListNonNull() {
        return (List<List<T>>) (List<?>) LIST_NON_NULL_EDGE_CASES;
    }

    /**
     * Returns edge case values for byte/Byte.
     * 返回byte/Byte的边缘用例值。
     *
     * <p>Includes: MIN_VALUE, -1, 0, 1, MAX_VALUE</p>
     * <p>包括: MIN_VALUE, -1, 0, 1, MAX_VALUE</p>
     *
     * @return list of edge case bytes | 边缘用例字节列表
     */
    public static List<Byte> forByte() {
        return List.of(
            Byte.MIN_VALUE,
            (byte) -1,
            (byte) 0,
            (byte) 1,
            Byte.MAX_VALUE
        );
    }

    /**
     * Returns edge case values for short/Short.
     * 返回short/Short的边缘用例值。
     *
     * <p>Includes: MIN_VALUE, -1, 0, 1, MAX_VALUE</p>
     * <p>包括: MIN_VALUE, -1, 0, 1, MAX_VALUE</p>
     *
     * @return list of edge case shorts | 边缘用例短整数列表
     */
    public static List<Short> forShort() {
        return List.of(
            Short.MIN_VALUE,
            (short) -1,
            (short) 0,
            (short) 1,
            Short.MAX_VALUE
        );
    }

    /**
     * Returns edge case values for char/Character.
     * 返回char/Character的边缘用例值。
     *
     * <p>Includes: MIN_VALUE (0), 'a', 'z', 'A', 'Z', '0', '9', MAX_VALUE</p>
     * <p>包括: 最小值(0), 'a', 'z', 'A', 'Z', '0', '9', 最大值</p>
     *
     * @return list of edge case characters | 边缘用例字符列表
     */
    public static List<Character> forChar() {
        return List.of(
            Character.MIN_VALUE,
            'a',
            'z',
            'A',
            'Z',
            '0',
            '9',
            Character.MAX_VALUE
        );
    }

    /**
     * Returns edge case values for boolean/Boolean.
     * 返回boolean/Boolean的边缘用例值。
     *
     * <p>Includes: true, false</p>
     * <p>包括: true, false</p>
     *
     * @return list of edge case booleans | 边缘用例布尔值列表
     */
    public static List<Boolean> forBoolean() {
        return List.of(true, false);
    }

    /**
     * Returns edge case values for LocalDate.
     * 返回LocalDate的边缘用例值。
     *
     * <p>Includes: MIN, epoch (1970-01-01), today, MAX</p>
     * <p>包括: 最小值, 纪元(1970-01-01), 今天, 最大值</p>
     *
     * @return list of edge case dates | 边缘用例日期列表
     */
    public static List<LocalDate> forLocalDate() {
        return List.of(
            LocalDate.MIN,
            LocalDate.EPOCH,
            LocalDate.now(),
            LocalDate.MAX
        );
    }

    /**
     * Returns edge case values for Duration.
     * 返回Duration的边缘用例值。
     *
     * <p>Includes: negative (-1s), ZERO, 1ms, 1s, 1h, 1day, max supported duration</p>
     * <p>包括: 负值(-1秒), 零, 1毫秒, 1秒, 1小时, 1天, 最大支持的持续时间</p>
     *
     * @return list of edge case durations | 边缘用例持续时间列表
     */
    public static List<Duration> forDuration() {
        return List.of(
            Duration.ofSeconds(-1),
            Duration.ZERO,
            Duration.ofMillis(1),
            Duration.ofSeconds(1),
            Duration.ofHours(1),
            Duration.ofDays(1),
            Duration.ofSeconds(Long.MAX_VALUE, 999_999_999)
        );
    }
}
