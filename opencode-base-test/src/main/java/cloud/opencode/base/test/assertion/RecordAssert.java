package cloud.opencode.base.test.assertion;

import cloud.opencode.base.test.exception.AssertionException;
import cloud.opencode.base.test.exception.TestErrorCode;

import java.lang.reflect.RecordComponent;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Record Assert - Fluent assertions for Java Record components
 * Record断言 - Java Record组件的流式断言
 *
 * <p>Provides assertion methods for verifying Java Record component values
 * by name using reflection on {@link RecordComponent}.</p>
 * <p>通过反射 {@link RecordComponent}，按名称验证 Java Record 组件值的断言方法。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Component value assertion by name - 按名称断言组件值</li>
 *   <li>Null/non-null checks for components - 组件的空/非空检查</li>
 *   <li>Type checking for components - 组件类型检查</li>
 *   <li>Fluent chaining API - 流式链式API</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * record Person(String name, int age) {}
 *
 * RecordAssert.assertThat(new Person("Alice", 30))
 *     .hasComponent("name", "Alice")
 *     .hasComponent("age", 30)
 *     .componentIsNotNull("name");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (not designed for concurrent use) - 线程安全: 否（非设计用于并发使用）</li>
 *   <li>Null-safe: Yes (validates non-null record) - 空值安全: 是（验证非空Record）</li>
 * </ul>
 *
 * @param <R> the record type | Record类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.3
 */
public final class RecordAssert<R extends Record> {

    private final R actual;
    private final Map<String, RecordComponent> componentMap;

    private RecordAssert(R actual) {
        this.actual = actual;
        if (actual != null) {
            RecordComponent[] components = actual.getClass().getRecordComponents();
            Map<String, RecordComponent> map = new LinkedHashMap<>(components.length);
            for (RecordComponent c : components) {
                map.put(c.getName(), c);
            }
            this.componentMap = Map.copyOf(map);
        } else {
            this.componentMap = Map.of();
        }
    }

    /**
     * Creates assertion for a record instance.
     * 为Record实例创建断言。
     *
     * @param actual the actual record | 实际Record
     * @param <R>    the record type | Record类型
     * @return the assertion | 断言
     */
    public static <R extends Record> RecordAssert<R> assertThat(R actual) {
        return new RecordAssert<>(actual);
    }

    /**
     * Asserts that the record is null.
     * 断言Record为null。
     *
     * @return this | 此对象
     */
    public RecordAssert<R> isNull() {
        if (actual != null) {
            throw new AssertionException("Expected null record but was: " + actual);
        }
        return this;
    }

    /**
     * Asserts that the record is not null.
     * 断言Record不为null。
     *
     * @return this | 此对象
     */
    public RecordAssert<R> isNotNull() {
        if (actual == null) {
            throw new AssertionException("Expected non-null record but was null");
        }
        return this;
    }

    /**
     * Asserts that a named component has the expected value.
     * 断言指定名称的组件具有期望值。
     *
     * @param componentName the component name | 组件名称
     * @param expectedValue the expected value | 期望值
     * @return this | 此对象
     */
    public RecordAssert<R> hasComponent(String componentName, Object expectedValue) {
        Objects.requireNonNull(componentName, "componentName must not be null");
        isNotNull();
        Object value = getComponentValue(componentName);
        if (!Objects.equals(value, expectedValue)) {
            throw new AssertionException(TestErrorCode.ASSERTION_EQUALS,
                    "component '" + componentName + "': expected <" + expectedValue + "> but was <" + value + ">");
        }
        return this;
    }

    /**
     * Asserts that a named component is null.
     * 断言指定名称的组件为null。
     *
     * @param componentName the component name | 组件名称
     * @return this | 此对象
     */
    public RecordAssert<R> componentIsNull(String componentName) {
        Objects.requireNonNull(componentName, "componentName must not be null");
        isNotNull();
        Object value = getComponentValue(componentName);
        if (value != null) {
            throw new AssertionException(TestErrorCode.ASSERTION_NULL,
                    "component '" + componentName + "': expected null but was <" + value + ">");
        }
        return this;
    }

    /**
     * Asserts that a named component is not null.
     * 断言指定名称的组件不为null。
     *
     * @param componentName the component name | 组件名称
     * @return this | 此对象
     */
    public RecordAssert<R> componentIsNotNull(String componentName) {
        Objects.requireNonNull(componentName, "componentName must not be null");
        isNotNull();
        Object value = getComponentValue(componentName);
        if (value == null) {
            throw new AssertionException(TestErrorCode.ASSERTION_NULL,
                    "component '" + componentName + "': expected non-null but was null");
        }
        return this;
    }

    /**
     * Asserts that a named component is of the expected type.
     * 断言指定名称的组件是期望的类型。
     *
     * @param componentName the component name | 组件名称
     * @param expectedType  the expected type | 期望类型
     * @return this | 此对象
     */
    public RecordAssert<R> componentIsInstanceOf(String componentName, Class<?> expectedType) {
        Objects.requireNonNull(componentName, "componentName must not be null");
        Objects.requireNonNull(expectedType, "expectedType must not be null");
        isNotNull();
        Object value = getComponentValue(componentName);
        if (value == null) {
            throw new AssertionException(
                    "component '" + componentName + "': expected instance of " + expectedType.getName()
                            + " but was null");
        }
        if (!expectedType.isInstance(value)) {
            throw new AssertionException(
                    "component '" + componentName + "': expected instance of " + expectedType.getName()
                            + " but was " + value.getClass().getName());
        }
        return this;
    }

    /**
     * Asserts that the record has the specified number of components.
     * 断言Record具有指定数量的组件。
     *
     * @param expectedCount the expected component count | 期望的组件数量
     * @return this | 此对象
     */
    public RecordAssert<R> hasComponentCount(int expectedCount) {
        isNotNull();
        if (componentMap.size() != expectedCount) {
            throw new AssertionException(
                    "Expected " + expectedCount + " components but had " + componentMap.size());
        }
        return this;
    }

    /**
     * Asserts that the record has a component with the given name.
     * 断言Record具有指定名称的组件。
     *
     * @param componentName the component name | 组件名称
     * @return this | 此对象
     */
    public RecordAssert<R> hasComponentNamed(String componentName) {
        Objects.requireNonNull(componentName, "componentName must not be null");
        isNotNull();
        if (!componentMap.containsKey(componentName)) {
            throw new AssertionException("Expected component named '" + componentName + "' but not found");
        }
        return this;
    }

    /**
     * Asserts that the record equals the expected record.
     * 断言Record等于期望的Record。
     *
     * @param expected the expected record | 期望的Record
     * @return this | 此对象
     */
    public RecordAssert<R> isEqualTo(R expected) {
        if (!Objects.equals(actual, expected)) {
            throw new AssertionException(TestErrorCode.ASSERTION_EQUALS,
                    "expected <" + expected + "> but was <" + actual + ">");
        }
        return this;
    }

    // ==================== Helper Methods | 辅助方法 ====================

    private Object getComponentValue(String componentName) {
        RecordComponent component = componentMap.get(componentName);
        if (component == null) {
            throw new AssertionException("No component named '" + componentName + "' found in "
                    + actual.getClass().getSimpleName());
        }
        try {
            return component.getAccessor().invoke(actual);
        } catch (ReflectiveOperationException e) {
            throw new AssertionException(
                    "Failed to access component '" + componentName + "': " + e.getMessage(), e);
        }
    }
}
