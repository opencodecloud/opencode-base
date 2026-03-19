package cloud.opencode.base.core.builder;

import cloud.opencode.base.core.reflect.RecordUtil;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Record Builder - Fluent builder for Java Records
 * Record 构建器 - Java Record 的流式构建器
 *
 * <p>Creates Java Record instances with fluent API despite immutability.</p>
 * <p>使用流式 API 创建 Java Record 实例，绕过不可变性限制。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Component setting (set, setIfNotNull, setIf) - 组件设置</li>
 *   <li>Batch setting (setAll) - 批量设置</li>
 *   <li>Copy from existing record (from) - 从现有 Record 复制</li>
 *   <li>Build with validation (buildAndValidate) - 构建并验证</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * record User(String name, int age) {}
 *
 * User user = RecordBuilder.of(User.class)
 *     .set("name", "John")
 *     .set("age", 25)
 *     .build();
 *
 * User updated = RecordBuilder.from(user)
 *     .set("age", 26)
 *     .build();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (builder instance not thread-safe) - 线程安全: 否</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) where n = number of record components - O(n), n为记录组件数</li>
 *   <li>Space complexity: O(n) for component values - 组件值 O(n)</li>
 * </ul>
 *
 * @param <T> Record type - Record 类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public class RecordBuilder<T extends Record> implements Builder<T> {

    private final Class<T> recordClass;
    private final Map<String, Object> components = new LinkedHashMap<>();

    public RecordBuilder(Class<T> recordClass) {
        if (!recordClass.isRecord()) {
            throw new IllegalArgumentException("Class must be a record: " + recordClass.getName());
        }
        this.recordClass = recordClass;
    }

    /**
     * Creates a new builder for the given record class
     * 创建构建器
     */
    public static <T extends Record> RecordBuilder<T> of(Class<T> recordClass) {
        return new RecordBuilder<>(recordClass);
    }

    /**
     * Creates a builder from an existing record instance
     * 从现有 Record 创建构建器
     */
    public static <T extends Record> RecordBuilder<T> from(T record) {
        @SuppressWarnings("unchecked")
        Class<T> clazz = (Class<T>) record.getClass();
        RecordBuilder<T> builder = new RecordBuilder<>(clazz);
        Map<String, Object> map = RecordUtil.toMap(record);
        builder.components.putAll(map);
        return builder;
    }

    /**
     * Sets a component value
     * 设置组件值
     */
    public RecordBuilder<T> set(String componentName, Object value) {
        components.put(componentName, value);
        return this;
    }

    /**
     * Sets a component value only if the value is not null
     * 条件设置组件值（非 null 时设置）
     */
    public RecordBuilder<T> setIfNotNull(String componentName, Object value) {
        if (value != null) {
            components.put(componentName, value);
        }
        return this;
    }

    /**
     * Conditionally sets a component value
     * 条件设置组件值
     */
    public RecordBuilder<T> setIf(boolean condition, String componentName, Object value) {
        if (condition) {
            components.put(componentName, value);
        }
        return this;
    }

    /**
     * Sets multiple component values at once
     * 批量设置组件值
     */
    public RecordBuilder<T> setAll(Map<String, Object> props) {
        components.putAll(props);
        return this;
    }

    /**
     * Applies a configuration callback to this builder
     * 配置回调
     */
    public RecordBuilder<T> configure(Consumer<RecordBuilder<T>> consumer) {
        consumer.accept(this);
        return this;
    }

    @Override
    public T build() {
        return RecordUtil.fromMap(components, recordClass);
    }

    /**
     * Builds the record and validates it with the given validator
     * 构建并验证
     */
    public T buildAndValidate(Consumer<T> validator) {
        T record = build();
        validator.accept(record);
        return record;
    }
}
