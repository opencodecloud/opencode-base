package cloud.opencode.base.test.assertion;

import cloud.opencode.base.test.exception.AssertionException;
import cloud.opencode.base.test.exception.TestErrorCode;

import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Map Assert - Fluent assertions for Map instances
 * Map断言 - Map实例的流式断言
 *
 * <p>Provides comprehensive assertion methods for {@link Map} types including
 * size checks, key/value containment, and predicate-based matching.</p>
 * <p>为 {@link Map} 类型提供全面的断言方法，包括大小检查、键/值包含和基于谓词的匹配。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fluent assertion API for maps - Map的流式断言API</li>
 *   <li>Size, key, value containment checks - 大小、键、值包含检查</li>
 *   <li>Key-value pair assertions - 键值对断言</li>
 *   <li>Predicate-based matching (allKeysMatch, allValuesMatch) - 基于谓词的匹配</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * MapAssert.assertThat(map)
 *     .isNotEmpty()
 *     .hasSize(3)
 *     .containsKey("name")
 *     .containsEntry("age", 30)
 *     .doesNotContainKey("password");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (not designed for concurrent use) - 线程安全: 否（非设计用于并发使用）</li>
 *   <li>Null-safe: Yes (validates non-null map) - 空值安全: 是（验证非空Map）</li>
 * </ul>
 *
 * @param <K> the key type | 键类型
 * @param <V> the value type | 值类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.3
 */
public final class MapAssert<K, V> {

    private final Map<K, V> actual;

    private MapAssert(Map<K, V> actual) {
        this.actual = actual;
    }

    /**
     * Creates assertion for a map.
     * 为Map创建断言。
     *
     * @param actual the actual map | 实际Map
     * @param <K>    the key type | 键类型
     * @param <V>    the value type | 值类型
     * @return the assertion | 断言
     */
    public static <K, V> MapAssert<K, V> assertThat(Map<K, V> actual) {
        return new MapAssert<>(actual);
    }

    /**
     * Asserts that the map is null.
     * 断言Map为null。
     *
     * @return this | 此对象
     */
    public MapAssert<K, V> isNull() {
        if (actual != null) {
            throw new AssertionException("Expected null but was: " + actual);
        }
        return this;
    }

    /**
     * Asserts that the map is not null.
     * 断言Map不为null。
     *
     * @return this | 此对象
     */
    public MapAssert<K, V> isNotNull() {
        if (actual == null) {
            throw new AssertionException("Expected not null but was null");
        }
        return this;
    }

    /**
     * Asserts that the map is empty.
     * 断言Map为空。
     *
     * @return this | 此对象
     */
    public MapAssert<K, V> isEmpty() {
        isNotNull();
        if (!actual.isEmpty()) {
            throw new AssertionException("Expected empty map but had " + actual.size() + " entries");
        }
        return this;
    }

    /**
     * Asserts that the map is not empty.
     * 断言Map不为空。
     *
     * @return this | 此对象
     */
    public MapAssert<K, V> isNotEmpty() {
        isNotNull();
        if (actual.isEmpty()) {
            throw new AssertionException("Expected non-empty map but was empty");
        }
        return this;
    }

    /**
     * Asserts that the map has the specified size.
     * 断言Map具有指定大小。
     *
     * @param expectedSize the expected size | 期望大小
     * @return this | 此对象
     */
    public MapAssert<K, V> hasSize(int expectedSize) {
        isNotNull();
        if (actual.size() != expectedSize) {
            throw new AssertionException("Expected size " + expectedSize + " but was " + actual.size());
        }
        return this;
    }

    /**
     * Asserts that the map has size greater than the specified value.
     * 断言Map大小大于指定值。
     *
     * @param size the size | 大小
     * @return this | 此对象
     */
    public MapAssert<K, V> hasSizeGreaterThan(int size) {
        isNotNull();
        if (actual.size() <= size) {
            throw new AssertionException("Expected size > " + size + " but was " + actual.size());
        }
        return this;
    }

    /**
     * Asserts that the map has size less than the specified value.
     * 断言Map大小小于指定值。
     *
     * @param size the size | 大小
     * @return this | 此对象
     */
    public MapAssert<K, V> hasSizeLessThan(int size) {
        isNotNull();
        if (actual.size() >= size) {
            throw new AssertionException("Expected size < " + size + " but was " + actual.size());
        }
        return this;
    }

    /**
     * Asserts that the map contains the specified key.
     * 断言Map包含指定键。
     *
     * @param key the key | 键
     * @return this | 此对象
     */
    public MapAssert<K, V> containsKey(K key) {
        isNotNull();
        if (!actual.containsKey(key)) {
            throw new AssertionException("Expected to contain key <" + key + "> but did not");
        }
        return this;
    }

    /**
     * Asserts that the map does not contain the specified key.
     * 断言Map不包含指定键。
     *
     * @param key the key | 键
     * @return this | 此对象
     */
    public MapAssert<K, V> doesNotContainKey(K key) {
        isNotNull();
        if (actual.containsKey(key)) {
            throw new AssertionException("Expected not to contain key <" + key + "> but did");
        }
        return this;
    }

    /**
     * Asserts that the map contains the specified value.
     * 断言Map包含指定值。
     *
     * @param value the value | 值
     * @return this | 此对象
     */
    public MapAssert<K, V> containsValue(V value) {
        isNotNull();
        if (!actual.containsValue(value)) {
            throw new AssertionException("Expected to contain value <" + value + "> but did not");
        }
        return this;
    }

    /**
     * Asserts that the map does not contain the specified value.
     * 断言Map不包含指定值。
     *
     * @param value the value | 值
     * @return this | 此对象
     */
    public MapAssert<K, V> doesNotContainValue(V value) {
        isNotNull();
        if (actual.containsValue(value)) {
            throw new AssertionException("Expected not to contain value <" + value + "> but did");
        }
        return this;
    }

    /**
     * Asserts that the map contains the specified key-value entry.
     * 断言Map包含指定的键值对。
     *
     * @param key   the key | 键
     * @param value the value | 值
     * @return this | 此对象
     */
    public MapAssert<K, V> containsEntry(K key, V value) {
        isNotNull();
        if (!actual.containsKey(key)) {
            throw new AssertionException(
                    "Expected to contain entry <" + key + "=" + value + "> but key was not found");
        }
        V actualValue = actual.get(key);
        if (!Objects.equals(actualValue, value)) {
            throw new AssertionException(TestErrorCode.ASSERTION_EQUALS,
                    "entry key <" + key + ">: expected value <" + value + "> but was <" + actualValue + ">");
        }
        return this;
    }

    /**
     * Asserts that the map does not contain the specified key-value entry.
     * 断言Map不包含指定的键值对。
     *
     * @param key   the key | 键
     * @param value the value | 值
     * @return this | 此对象
     */
    public MapAssert<K, V> doesNotContainEntry(K key, V value) {
        isNotNull();
        if (actual.containsKey(key) && Objects.equals(actual.get(key), value)) {
            throw new AssertionException(
                    "Expected not to contain entry <" + key + "=" + value + "> but did");
        }
        return this;
    }

    /**
     * Asserts that the map contains all specified keys.
     * 断言Map包含所有指定键。
     *
     * @param keys the keys | 键
     * @return this | 此对象
     */
    @SafeVarargs
    public final MapAssert<K, V> containsKeys(K... keys) {
        Objects.requireNonNull(keys, "keys must not be null");
        isNotNull();
        for (K key : keys) {
            if (!actual.containsKey(key)) {
                throw new AssertionException("Expected to contain key <" + key + "> but did not");
            }
        }
        return this;
    }

    /**
     * Asserts that all keys match the given predicate.
     * 断言所有键匹配给定谓词。
     *
     * @param predicate the predicate | 谓词
     * @return this | 此对象
     */
    public MapAssert<K, V> allKeysMatch(Predicate<K> predicate) {
        isNotNull();
        for (K key : actual.keySet()) {
            if (!predicate.test(key)) {
                throw new AssertionException("Key <" + key + "> does not match predicate");
            }
        }
        return this;
    }

    /**
     * Asserts that all values match the given predicate.
     * 断言所有值匹配给定谓词。
     *
     * @param predicate the predicate | 谓词
     * @return this | 此对象
     */
    public MapAssert<K, V> allValuesMatch(Predicate<V> predicate) {
        isNotNull();
        for (Map.Entry<K, V> entry : actual.entrySet()) {
            if (!predicate.test(entry.getValue())) {
                throw new AssertionException(
                        "Value <" + entry.getValue() + "> for key <" + entry.getKey()
                                + "> does not match predicate");
            }
        }
        return this;
    }

    /**
     * Asserts that the map equals the expected map.
     * 断言Map等于期望的Map。
     *
     * @param expected the expected map | 期望Map
     * @return this | 此对象
     */
    public MapAssert<K, V> isEqualTo(Map<K, V> expected) {
        if (!Objects.equals(actual, expected)) {
            throw new AssertionException(TestErrorCode.ASSERTION_EQUALS,
                    "expected <" + expected + "> but was <" + actual + ">");
        }
        return this;
    }
}
