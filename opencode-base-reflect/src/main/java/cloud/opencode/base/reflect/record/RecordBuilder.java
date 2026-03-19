package cloud.opencode.base.reflect.record;

import cloud.opencode.base.reflect.exception.OpenReflectException;

import java.lang.reflect.Constructor;
import java.util.*;

/**
 * Record Builder
 * Record构建器
 *
 * <p>Fluent builder for creating record instances.</p>
 * <p>用于创建record实例的流式构建器。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fluent record instance creation - 流式record实例创建</li>
 *   <li>Named component value setting - 命名组件值设置</li>
 *   <li>Type-safe record construction - 类型安全的record构造</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * User user = RecordBuilder.of(User.class)
 *     .set("name", "Alice")
 *     .set("age", 25)
 *     .build();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (builder pattern, not thread-safe during construction) - 线程安全: 否（构建器模式，构建期间非线程安全）</li>
 *   <li>Null-safe: No (record class must be non-null) - 空值安全: 否（record类须非空）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: build() O(c) where c is the number of record components - 时间复杂度: build() 为 O(c)，c为 record 组件数量</li>
 *   <li>Space complexity: O(c) for storing component values - 空间复杂度: O(c)，存储组件值</li>
 * </ul>
 *
 * @param <T> the record type | record类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
public class RecordBuilder<T extends Record> {

    private final Class<T> recordClass;
    private final List<RecordComponent> components;
    private final Map<String, Object> values;
    private final Constructor<T> constructor;

    /**
     * Creates a RecordBuilder
     * 创建RecordBuilder
     *
     * @param recordClass the record class | record类
     */
    @SuppressWarnings("unchecked")
    public RecordBuilder(Class<T> recordClass) {
        if (!recordClass.isRecord()) {
            throw new IllegalArgumentException("Class is not a record: " + recordClass.getName());
        }

        this.recordClass = recordClass;
        this.components = new ArrayList<>();
        this.values = new LinkedHashMap<>();

        // Get components
        for (java.lang.reflect.RecordComponent comp : recordClass.getRecordComponents()) {
            components.add(new RecordComponent(comp));
        }

        // Get canonical constructor
        Class<?>[] paramTypes = components.stream()
                .map(RecordComponent::getType)
                .toArray(Class[]::new);

        try {
            this.constructor = recordClass.getDeclaredConstructor(paramTypes);
            this.constructor.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw OpenReflectException.constructorNotFound(recordClass, paramTypes);
        }
    }

    /**
     * Creates a RecordBuilder for a record class
     * 为record类创建RecordBuilder
     *
     * @param recordClass the record class | record类
     * @param <T>         the record type | record类型
     * @return the builder | 构建器
     */
    public static <T extends Record> RecordBuilder<T> of(Class<T> recordClass) {
        return new RecordBuilder<>(recordClass);
    }

    /**
     * Creates a RecordBuilder copying from an existing record
     * 从现有record复制创建RecordBuilder
     *
     * @param record the record to copy | 要复制的record
     * @param <T>    the record type | record类型
     * @return the builder | 构建器
     */
    @SuppressWarnings("unchecked")
    public static <T extends Record> RecordBuilder<T> from(T record) {
        RecordBuilder<T> builder = of((Class<T>) record.getClass());
        for (RecordComponent comp : builder.components) {
            builder.values.put(comp.getName(), comp.getValue(record));
        }
        return builder;
    }

    /**
     * Sets a component value by name
     * 按名称设置组件值
     *
     * @param name  the component name | 组件名
     * @param value the value | 值
     * @return this builder | 此构建器
     */
    public RecordBuilder<T> set(String name, Object value) {
        RecordComponent comp = findComponent(name);
        if (comp == null) {
            throw new IllegalArgumentException("Unknown component: " + name);
        }
        values.put(name, value);
        return this;
    }

    /**
     * Sets a component value by index
     * 按索引设置组件值
     *
     * @param index the component index | 组件索引
     * @param value the value | 值
     * @return this builder | 此构建器
     */
    public RecordBuilder<T> set(int index, Object value) {
        if (index < 0 || index >= components.size()) {
            throw new IndexOutOfBoundsException("Component index: " + index);
        }
        values.put(components.get(index).getName(), value);
        return this;
    }

    /**
     * Sets multiple values from a map
     * 从map设置多个值
     *
     * @param map the values map | 值映射
     * @return this builder | 此构建器
     */
    public RecordBuilder<T> setAll(Map<String, ?> map) {
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            String name = entry.getKey();
            if (findComponent(name) != null) {
                values.put(name, entry.getValue());
            }
        }
        return this;
    }

    /**
     * Sets all values from an existing record
     * 从现有record设置所有值
     *
     * @param record the record | record
     * @return this builder | 此构建器
     */
    public RecordBuilder<T> copyFrom(T record) {
        for (RecordComponent comp : components) {
            values.put(comp.getName(), comp.getValue(record));
        }
        return this;
    }

    /**
     * Sets a value if absent
     * 如果不存在则设置值
     *
     * @param name  the component name | 组件名
     * @param value the value | 值
     * @return this builder | 此构建器
     */
    public RecordBuilder<T> setIfAbsent(String name, Object value) {
        if (!values.containsKey(name)) {
            set(name, value);
        }
        return this;
    }

    /**
     * Sets a value if not null
     * 如果非null则设置值
     *
     * @param name  the component name | 组件名
     * @param value the value | 值
     * @return this builder | 此构建器
     */
    public RecordBuilder<T> setIfNotNull(String name, Object value) {
        if (value != null) {
            set(name, value);
        }
        return this;
    }

    /**
     * Clears a specific value
     * 清除特定值
     *
     * @param name the component name | 组件名
     * @return this builder | 此构建器
     */
    public RecordBuilder<T> clear(String name) {
        values.remove(name);
        return this;
    }

    /**
     * Clears all values
     * 清除所有值
     *
     * @return this builder | 此构建器
     */
    public RecordBuilder<T> clearAll() {
        values.clear();
        return this;
    }

    /**
     * Gets the current value for a component
     * 获取组件的当前值
     *
     * @param name the component name | 组件名
     * @return the value or null | 值或null
     */
    public Object getValue(String name) {
        return values.get(name);
    }

    /**
     * Checks if a value is set
     * 检查是否已设置值
     *
     * @param name the component name | 组件名
     * @return true if set | 如果已设置返回true
     */
    public boolean hasValue(String name) {
        return values.containsKey(name);
    }

    /**
     * Gets the record class
     * 获取record类
     *
     * @return the record class | record类
     */
    public Class<T> getRecordClass() {
        return recordClass;
    }

    /**
     * Gets the components
     * 获取组件
     *
     * @return list of components | 组件列表
     */
    public List<RecordComponent> getComponents() {
        return Collections.unmodifiableList(components);
    }

    /**
     * Builds the record instance
     * 构建record实例
     *
     * @return the record | record
     */
    public T build() {
        Object[] args = new Object[components.size()];
        for (int i = 0; i < components.size(); i++) {
            RecordComponent comp = components.get(i);
            Object value = values.get(comp.getName());

            if (value == null && comp.getType().isPrimitive()) {
                value = getDefaultPrimitiveValue(comp.getType());
            }

            args[i] = value;
        }

        try {
            return constructor.newInstance(args);
        } catch (Exception e) {
            throw OpenReflectException.instantiationFailed(recordClass, e);
        }
    }

    /**
     * Builds the record with validation
     * 构建record（带验证）
     *
     * @return the record | record
     * @throws IllegalStateException if required values are missing | 如果缺少必需值
     */
    public T buildValidated() {
        List<String> missing = new ArrayList<>();
        for (RecordComponent comp : components) {
            if (!values.containsKey(comp.getName()) && !comp.getType().isPrimitive()) {
                missing.add(comp.getName());
            }
        }
        if (!missing.isEmpty()) {
            throw new IllegalStateException("Missing required components: " + missing);
        }
        return build();
    }

    private RecordComponent findComponent(String name) {
        for (RecordComponent comp : components) {
            if (comp.getName().equals(name)) {
                return comp;
            }
        }
        return null;
    }

    private Object getDefaultPrimitiveValue(Class<?> type) {
        if (type == boolean.class) return false;
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0f;
        if (type == double.class) return 0d;
        if (type == char.class) return '\0';
        return null;
    }
}
