package cloud.opencode.base.test.assertion;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Open Assertions
 * 开放断言
 *
 * <p>Fluent assertion utilities for testing.</p>
 * <p>用于测试的流式断言工具。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fluent assertion API - 流式断言API</li>
 *   <li>Object, string, collection, number assertions - 对象、字符串、集合、数值断言</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * assertThat("hello").isNotBlank().contains("ell");
 * assertThat(List.of(1, 2, 3)).hasSize(3).contains(2);
 * assertThatThrownBy(() -> { throw new RuntimeException(); })
 *     .isInstanceOf(RuntimeException.class);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
public final class OpenAssertions {

    private OpenAssertions() {
        // Utility class
    }

    // === Object assertions ===

    public static <T> ObjectAssertion<T> assertThat(T actual) {
        return new ObjectAssertion<>(actual);
    }

    public static StringAssertion assertThat(String actual) {
        return new StringAssertion(actual);
    }

    public static <T> CollectionAssertion<T> assertThat(Collection<T> actual) {
        return new CollectionAssertion<>(actual);
    }

    public static <K, V> MapAssertion<K, V> assertThat(Map<K, V> actual) {
        return new MapAssertion<>(actual);
    }

    public static NumberAssertion assertThat(Number actual) {
        return new NumberAssertion(actual);
    }

    public static BooleanAssertion assertThat(Boolean actual) {
        return new BooleanAssertion(actual);
    }

    public static ThrowableAssertion assertThatThrownBy(Runnable runnable) {
        try {
            runnable.run();
            throw new AssertionError("Expected exception but none was thrown");
        } catch (Throwable t) {
            return new ThrowableAssertion(t);
        }
    }

    public static void assertThatCode(Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable t) {
            throw new AssertionError("Expected no exception but got: " + t.getMessage(), t);
        }
    }

    // === Assertion classes ===

    public static class ObjectAssertion<T> {
        protected final T actual;

        public ObjectAssertion(T actual) {
            this.actual = actual;
        }

        public ObjectAssertion<T> isNull() {
            if (actual != null) {
                throw new AssertionError("Expected null but was: " + actual);
            }
            return this;
        }

        public ObjectAssertion<T> isNotNull() {
            if (actual == null) {
                throw new AssertionError("Expected non-null value");
            }
            return this;
        }

        public ObjectAssertion<T> isEqualTo(T expected) {
            if (!Objects.equals(actual, expected)) {
                throw new AssertionError("Expected: " + expected + " but was: " + actual);
            }
            return this;
        }

        public ObjectAssertion<T> isNotEqualTo(T other) {
            if (Objects.equals(actual, other)) {
                throw new AssertionError("Expected not equal to: " + other);
            }
            return this;
        }

        public ObjectAssertion<T> isSameAs(T expected) {
            if (actual != expected) {
                throw new AssertionError("Expected same instance");
            }
            return this;
        }

        public ObjectAssertion<T> isInstanceOf(Class<?> type) {
            isNotNull();
            if (!type.isInstance(actual)) {
                throw new AssertionError("Expected instance of " + type.getName() + " but was " + actual.getClass().getName());
            }
            return this;
        }

        public ObjectAssertion<T> matches(Predicate<T> predicate) {
            if (!predicate.test(actual)) {
                throw new AssertionError("Value does not match predicate: " + actual);
            }
            return this;
        }

        public ObjectAssertion<T> satisfies(java.util.function.Consumer<T> requirement) {
            requirement.accept(actual);
            return this;
        }
    }

    public static class StringAssertion extends ObjectAssertion<String> {
        public StringAssertion(String actual) {
            super(actual);
        }

        public StringAssertion isEmpty() {
            if (actual == null || !actual.isEmpty()) {
                throw new AssertionError("Expected empty string but was: " + actual);
            }
            return this;
        }

        public StringAssertion isNotEmpty() {
            if (actual == null || actual.isEmpty()) {
                throw new AssertionError("Expected non-empty string");
            }
            return this;
        }

        public StringAssertion isBlank() {
            if (actual == null || !actual.isBlank()) {
                throw new AssertionError("Expected blank string but was: " + actual);
            }
            return this;
        }

        public StringAssertion isNotBlank() {
            if (actual == null || actual.isBlank()) {
                throw new AssertionError("Expected non-blank string");
            }
            return this;
        }

        public StringAssertion contains(String substring) {
            isNotNull();
            if (!actual.contains(substring)) {
                throw new AssertionError("Expected to contain: " + substring + " but was: " + actual);
            }
            return this;
        }

        public StringAssertion startsWith(String prefix) {
            isNotNull();
            if (!actual.startsWith(prefix)) {
                throw new AssertionError("Expected to start with: " + prefix + " but was: " + actual);
            }
            return this;
        }

        public StringAssertion endsWith(String suffix) {
            isNotNull();
            if (!actual.endsWith(suffix)) {
                throw new AssertionError("Expected to end with: " + suffix + " but was: " + actual);
            }
            return this;
        }

        public StringAssertion hasLength(int length) {
            isNotNull();
            if (actual.length() != length) {
                throw new AssertionError("Expected length " + length + " but was " + actual.length());
            }
            return this;
        }

        public StringAssertion matchesRegex(String regex) {
            isNotNull();
            if (!actual.matches(regex)) {
                throw new AssertionError("Expected to match regex: " + regex);
            }
            return this;
        }
    }

    public static class CollectionAssertion<T> extends ObjectAssertion<Collection<T>> {
        public CollectionAssertion(Collection<T> actual) {
            super(actual);
        }

        public CollectionAssertion<T> isEmpty() {
            isNotNull();
            if (!actual.isEmpty()) {
                throw new AssertionError("Expected empty collection but had " + actual.size() + " elements");
            }
            return this;
        }

        public CollectionAssertion<T> isNotEmpty() {
            isNotNull();
            if (actual.isEmpty()) {
                throw new AssertionError("Expected non-empty collection");
            }
            return this;
        }

        public CollectionAssertion<T> hasSize(int size) {
            isNotNull();
            if (actual.size() != size) {
                throw new AssertionError("Expected size " + size + " but was " + actual.size());
            }
            return this;
        }

        public CollectionAssertion<T> contains(T element) {
            isNotNull();
            if (!actual.contains(element)) {
                throw new AssertionError("Expected to contain: " + element);
            }
            return this;
        }

        public CollectionAssertion<T> doesNotContain(T element) {
            isNotNull();
            if (actual.contains(element)) {
                throw new AssertionError("Expected not to contain: " + element);
            }
            return this;
        }

        @SafeVarargs
        public final CollectionAssertion<T> containsAll(T... elements) {
            isNotNull();
            for (T element : elements) {
                if (!actual.contains(element)) {
                    throw new AssertionError("Expected to contain: " + element);
                }
            }
            return this;
        }
    }

    public static class MapAssertion<K, V> extends ObjectAssertion<Map<K, V>> {
        public MapAssertion(Map<K, V> actual) {
            super(actual);
        }

        public MapAssertion<K, V> isEmpty() {
            isNotNull();
            if (!actual.isEmpty()) {
                throw new AssertionError("Expected empty map but had " + actual.size() + " entries");
            }
            return this;
        }

        public MapAssertion<K, V> isNotEmpty() {
            isNotNull();
            if (actual.isEmpty()) {
                throw new AssertionError("Expected non-empty map");
            }
            return this;
        }

        public MapAssertion<K, V> hasSize(int size) {
            isNotNull();
            if (actual.size() != size) {
                throw new AssertionError("Expected size " + size + " but was " + actual.size());
            }
            return this;
        }

        public MapAssertion<K, V> containsKey(K key) {
            isNotNull();
            if (!actual.containsKey(key)) {
                throw new AssertionError("Expected to contain key: " + key);
            }
            return this;
        }

        public MapAssertion<K, V> containsValue(V value) {
            isNotNull();
            if (!actual.containsValue(value)) {
                throw new AssertionError("Expected to contain value: " + value);
            }
            return this;
        }

        public MapAssertion<K, V> containsEntry(K key, V value) {
            isNotNull();
            if (!Objects.equals(actual.get(key), value)) {
                throw new AssertionError("Expected entry " + key + "=" + value);
            }
            return this;
        }
    }

    public static class NumberAssertion extends ObjectAssertion<Number> {
        public NumberAssertion(Number actual) {
            super(actual);
        }

        public NumberAssertion isZero() {
            isNotNull();
            if (actual.doubleValue() != 0) {
                throw new AssertionError("Expected zero but was: " + actual);
            }
            return this;
        }

        public NumberAssertion isPositive() {
            isNotNull();
            if (actual.doubleValue() <= 0) {
                throw new AssertionError("Expected positive but was: " + actual);
            }
            return this;
        }

        public NumberAssertion isNegative() {
            isNotNull();
            if (actual.doubleValue() >= 0) {
                throw new AssertionError("Expected negative but was: " + actual);
            }
            return this;
        }

        public NumberAssertion isGreaterThan(Number other) {
            isNotNull();
            if (actual.doubleValue() <= other.doubleValue()) {
                throw new AssertionError("Expected > " + other + " but was: " + actual);
            }
            return this;
        }

        public NumberAssertion isLessThan(Number other) {
            isNotNull();
            if (actual.doubleValue() >= other.doubleValue()) {
                throw new AssertionError("Expected < " + other + " but was: " + actual);
            }
            return this;
        }

        public NumberAssertion isBetween(Number start, Number end) {
            isNotNull();
            double value = actual.doubleValue();
            if (value < start.doubleValue() || value > end.doubleValue()) {
                throw new AssertionError("Expected between " + start + " and " + end + " but was: " + actual);
            }
            return this;
        }
    }

    public static class BooleanAssertion extends ObjectAssertion<Boolean> {
        public BooleanAssertion(Boolean actual) {
            super(actual);
        }

        public BooleanAssertion isTrue() {
            isNotNull();
            if (!actual) {
                throw new AssertionError("Expected true but was false");
            }
            return this;
        }

        public BooleanAssertion isFalse() {
            isNotNull();
            if (actual) {
                throw new AssertionError("Expected false but was true");
            }
            return this;
        }
    }

    public static class ThrowableAssertion extends ObjectAssertion<Throwable> {
        public ThrowableAssertion(Throwable actual) {
            super(actual);
        }

        @Override
        public ThrowableAssertion isInstanceOf(Class<?> type) {
            if (!type.isInstance(actual)) {
                throw new AssertionError("Expected exception of type " + type.getName() +
                    " but was " + actual.getClass().getName());
            }
            return this;
        }

        public ThrowableAssertion hasMessage(String message) {
            if (!Objects.equals(actual.getMessage(), message)) {
                throw new AssertionError("Expected message: " + message + " but was: " + actual.getMessage());
            }
            return this;
        }

        public ThrowableAssertion hasMessageContaining(String substring) {
            if (actual.getMessage() == null || !actual.getMessage().contains(substring)) {
                throw new AssertionError("Expected message containing: " + substring);
            }
            return this;
        }

        public ThrowableAssertion hasCauseInstanceOf(Class<? extends Throwable> type) {
            if (actual.getCause() == null || !type.isInstance(actual.getCause())) {
                throw new AssertionError("Expected cause of type " + type.getName());
            }
            return this;
        }
    }
}
