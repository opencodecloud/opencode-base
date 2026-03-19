package cloud.opencode.base.reflect.record;

import cloud.opencode.base.reflect.exception.OpenReflectException;

import java.lang.reflect.Constructor;
import java.util.*;

/**
 * Record Facade Entry Class
 * Record门面入口类
 *
 * <p>Provides common record operations API.</p>
 * <p>提供常用record操作API。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Record detection - Record检测</li>
 *   <li>Component access - 组件访问</li>
 *   <li>Record creation - Record创建</li>
 *   <li>Record copying - Record复制</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Check if record
 * boolean isRecord = OpenRecord.isRecord(User.class);
 *
 * // Get component values
 * Map<String, Object> values = OpenRecord.toMap(userRecord);
 *
 * // Copy with modifications
 * User modified = OpenRecord.copyWith(user, Map.of("name", "Alice"));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: No (caller must ensure non-null arguments) - 空值安全: 否（调用方须确保非空参数）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
public final class OpenRecord {

    private OpenRecord() {
    }

    // ==================== Record Detection | Record检测 ====================

    /**
     * Checks if a class is a record
     * 检查类是否为record
     *
     * @param clazz the class | 类
     * @return true if record | 如果是record返回true
     */
    public static boolean isRecord(Class<?> clazz) {
        return clazz != null && clazz.isRecord();
    }

    /**
     * Checks if an object is a record instance
     * 检查对象是否为record实例
     *
     * @param obj the object | 对象
     * @return true if record instance | 如果是record实例返回true
     */
    public static boolean isRecordInstance(Object obj) {
        return obj != null && obj.getClass().isRecord();
    }

    /**
     * Requires class to be a record
     * 要求类为record
     *
     * @param clazz the class | 类
     * @param <T>   the type | 类型
     * @return the class | 类
     * @throws IllegalArgumentException if not a record | 如果不是record
     */
    public static <T> Class<T> requireRecord(Class<T> clazz) {
        if (!isRecord(clazz)) {
            throw new IllegalArgumentException("Class is not a record: " + clazz.getName());
        }
        return clazz;
    }

    // ==================== Component Access | 组件访问 ====================

    /**
     * Gets record components
     * 获取record组件
     *
     * @param recordClass the record class | record类
     * @return list of components | 组件列表
     */
    public static List<RecordComponent> getComponents(Class<?> recordClass) {
        requireRecord(recordClass);
        java.lang.reflect.RecordComponent[] comps = recordClass.getRecordComponents();
        List<RecordComponent> result = new ArrayList<>(comps.length);
        for (java.lang.reflect.RecordComponent comp : comps) {
            result.add(new RecordComponent(comp));
        }
        return result;
    }

    /**
     * Gets component by name
     * 按名称获取组件
     *
     * @param recordClass   the record class | record类
     * @param componentName the component name | 组件名
     * @return the component or null | 组件或null
     */
    public static RecordComponent getComponent(Class<?> recordClass, String componentName) {
        for (RecordComponent comp : getComponents(recordClass)) {
            if (comp.getName().equals(componentName)) {
                return comp;
            }
        }
        return null;
    }

    /**
     * Gets component by index
     * 按索引获取组件
     *
     * @param recordClass the record class | record类
     * @param index       the index | 索引
     * @return the component | 组件
     */
    public static RecordComponent getComponent(Class<?> recordClass, int index) {
        List<RecordComponent> components = getComponents(recordClass);
        if (index < 0 || index >= components.size()) {
            throw new IndexOutOfBoundsException("Component index: " + index);
        }
        return components.get(index);
    }

    /**
     * Gets component names
     * 获取组件名称
     *
     * @param recordClass the record class | record类
     * @return list of names | 名称列表
     */
    public static List<String> getComponentNames(Class<?> recordClass) {
        return getComponents(recordClass).stream()
                .map(RecordComponent::getName)
                .toList();
    }

    /**
     * Gets component types
     * 获取组件类型
     *
     * @param recordClass the record class | record类
     * @return list of types | 类型列表
     */
    public static List<Class<?>> getComponentTypes(Class<?> recordClass) {
        return getComponents(recordClass).stream()
                .map(RecordComponent::getType)
                .toList();
    }

    /**
     * Gets number of components
     * 获取组件数量
     *
     * @param recordClass the record class | record类
     * @return the count | 数量
     */
    public static int getComponentCount(Class<?> recordClass) {
        requireRecord(recordClass);
        return recordClass.getRecordComponents().length;
    }

    // ==================== Value Access | 值访问 ====================

    /**
     * Gets component value from a record
     * 从record获取组件值
     *
     * @param record        the record | record
     * @param componentName the component name | 组件名
     * @return the value | 值
     */
    public static Object getValue(Record record, String componentName) {
        RecordComponent comp = getComponent(record.getClass(), componentName);
        if (comp == null) {
            throw new IllegalArgumentException("Unknown component: " + componentName);
        }
        return comp.getValue(record);
    }

    /**
     * Gets component value with type
     * 获取组件值（带类型）
     *
     * @param record        the record | record
     * @param componentName the component name | 组件名
     * @param type          the expected type | 期望类型
     * @param <T>           the value type | 值类型
     * @return the value | 值
     */
    @SuppressWarnings("unchecked")
    public static <T> T getValue(Record record, String componentName, Class<T> type) {
        return (T) getValue(record, componentName);
    }

    /**
     * Gets all component values
     * 获取所有组件值
     *
     * @param record the record | record
     * @return array of values | 值数组
     */
    public static Object[] getValues(Record record) {
        List<RecordComponent> components = getComponents(record.getClass());
        Object[] values = new Object[components.size()];
        for (int i = 0; i < components.size(); i++) {
            values[i] = components.get(i).getValue(record);
        }
        return values;
    }

    /**
     * Converts record to map
     * 将record转换为map
     *
     * @param record the record | record
     * @return the map | 映射
     */
    public static Map<String, Object> toMap(Record record) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (RecordComponent comp : getComponents(record.getClass())) {
            result.put(comp.getName(), comp.getValue(record));
        }
        return result;
    }

    // ==================== Record Creation | Record创建 ====================

    /**
     * Creates a record builder
     * 创建record构建器
     *
     * @param recordClass the record class | record类
     * @param <T>         the record type | record类型
     * @return the builder | 构建器
     */
    public static <T extends Record> RecordBuilder<T> builder(Class<T> recordClass) {
        return RecordBuilder.of(recordClass);
    }

    /**
     * Creates a record from values
     * 从值创建record
     *
     * @param recordClass the record class | record类
     * @param values      the component values | 组件值
     * @param <T>         the record type | record类型
     * @return the record | record
     */
    public static <T extends Record> T create(Class<T> recordClass, Object... values) {
        requireRecord(recordClass);
        List<RecordComponent> components = getComponents(recordClass);

        if (values.length != components.size()) {
            throw new IllegalArgumentException("Expected " + components.size() +
                    " values but got " + values.length);
        }

        Class<?>[] paramTypes = components.stream()
                .map(RecordComponent::getType)
                .toArray(Class[]::new);

        try {
            Constructor<T> constructor = recordClass.getDeclaredConstructor(paramTypes);
            constructor.setAccessible(true);
            return constructor.newInstance(values);
        } catch (Exception e) {
            throw OpenReflectException.instantiationFailed(recordClass, e);
        }
    }

    /**
     * Creates a record from a map
     * 从map创建record
     *
     * @param recordClass the record class | record类
     * @param values      the values map | 值映射
     * @param <T>         the record type | record类型
     * @return the record | record
     */
    public static <T extends Record> T fromMap(Class<T> recordClass, Map<String, ?> values) {
        RecordBuilder<T> builder = builder(recordClass);
        builder.setAll(values);
        return builder.build();
    }

    // ==================== Record Copying | Record复制 ====================

    /**
     * Creates a copy of a record
     * 创建record的副本
     *
     * @param record the record to copy | 要复制的record
     * @param <T>    the record type | record类型
     * @return the copy | 副本
     */
    @SuppressWarnings("unchecked")
    public static <T extends Record> T copy(T record) {
        return (T) create(record.getClass(), getValues(record));
    }

    /**
     * Creates a copy with modified values
     * 创建带修改值的副本
     *
     * @param record       the record to copy | 要复制的record
     * @param modifications the modifications | 修改
     * @param <T>          the record type | record类型
     * @return the modified copy | 修改后的副本
     */
    @SuppressWarnings("unchecked")
    public static <T extends Record> T copyWith(T record, Map<String, ?> modifications) {
        RecordBuilder<T> builder = RecordBuilder.from(record);
        builder.setAll(modifications);
        return builder.build();
    }

    /**
     * Creates a copy with a single modified value
     * 创建带单个修改值的副本
     *
     * @param record        the record to copy | 要复制的record
     * @param componentName the component to modify | 要修改的组件
     * @param newValue      the new value | 新值
     * @param <T>           the record type | record类型
     * @return the modified copy | 修改后的副本
     */
    @SuppressWarnings("unchecked")
    public static <T extends Record> T copyWith(T record, String componentName, Object newValue) {
        RecordBuilder<T> builder = RecordBuilder.from(record);
        builder.set(componentName, newValue);
        return builder.build();
    }

    // ==================== Comparison | 比较 ====================

    /**
     * Compares two records component by component
     * 逐组件比较两个record
     *
     * @param record1 the first record | 第一个record
     * @param record2 the second record | 第二个record
     * @return map of differing component names to their values | 不同组件名到其值的映射
     */
    public static Map<String, Object[]> diff(Record record1, Record record2) {
        if (!record1.getClass().equals(record2.getClass())) {
            throw new IllegalArgumentException("Records must be of the same type");
        }

        Map<String, Object[]> differences = new LinkedHashMap<>();
        for (RecordComponent comp : getComponents(record1.getClass())) {
            Object v1 = comp.getValue(record1);
            Object v2 = comp.getValue(record2);
            if (!Objects.equals(v1, v2)) {
                differences.put(comp.getName(), new Object[]{v1, v2});
            }
        }
        return differences;
    }

    /**
     * Checks if two records have the same values
     * 检查两个record是否有相同的值
     *
     * @param record1 the first record | 第一个record
     * @param record2 the second record | 第二个record
     * @return true if equal | 如果相等返回true
     */
    public static boolean valuesEqual(Record record1, Record record2) {
        return diff(record1, record2).isEmpty();
    }
}
