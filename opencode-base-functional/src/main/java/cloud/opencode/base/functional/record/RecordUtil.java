package cloud.opencode.base.functional.record;

import cloud.opencode.base.functional.exception.OpenFunctionalException;
import cloud.opencode.base.functional.monad.Try;
import cloud.opencode.base.functional.optics.Lens;

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * RecordUtil - Utilities for working with Java Records
 * RecordUtil - 用于处理 Java Record 的工具
 *
 * <p>Provides reflection-based and functional utilities for manipulating
 * Java Records. Includes component access, modification, lens creation,
 * and conversion utilities.</p>
 * <p>提供基于反射和函数式的工具来操作 Java Record。包括组件访问、
 * 修改、透镜创建和转换工具。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Component introspection - 组件内省</li>
 *   <li>Value extraction - 值提取</li>
 *   <li>Record copying with modifications - 带修改的 Record 复制</li>
 *   <li>Automatic lens generation - 自动透镜生成</li>
 *   <li>Map conversion - Map 转换</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * record Person(String name, int age) {}
 *
 * // Get component names
 * List<String> names = RecordUtil.componentNames(Person.class);
 * // ["name", "age"]
 *
 * // Get component values
 * Person person = new Person("Alice", 30);
 * List<Object> values = RecordUtil.componentValues(person);
 * // ["Alice", 30]
 *
 * // Copy with modification
 * Person older = RecordUtil.copy(person, Map.of("age", 31));
 * // Person[name=Alice, age=31]
 *
 * // Create lens for component
 * Lens<Person, String> nameLens = RecordUtil.lens(Person.class, "name");
 * String name = nameLens.get(person);
 * Person renamed = nameLens.set(person, "Bob");
 *
 * // Convert to/from Map
 * Map<String, Object> map = RecordUtil.toMap(person);
 * Person fromMap = RecordUtil.fromMap(Person.class, map);
 *
 * // Check if class is record
 * boolean isRecord = RecordUtil.isRecord(Person.class); // true
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Reflection: Cached accessors - 反射: 缓存访问器</li>
 *   <li>Copy: O(n) where n = components - 复制: O(n) n = 组件数</li>
 *   <li>Lens: O(1) after creation - 透镜: 创建后 O(1)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (utility methods) - 线程安全: 是 (工具方法)</li>
 *   <li>Reflection: Uses standard accessors - 反射: 使用标准访问器</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
public final class RecordUtil {

    private RecordUtil() {
        // Utility class
    }

    // ==================== Introspection | 内省 ====================

    /**
     * Check if a class is a record
     * 检查类是否为 record
     *
     * @param clazz the class - 类
     * @return true if record
     */
    public static boolean isRecord(Class<?> clazz) {
        return clazz != null && clazz.isRecord();
    }

    /**
     * Check if an object is a record instance
     * 检查对象是否为 record 实例
     *
     * @param obj the object - 对象
     * @return true if record instance
     */
    public static boolean isRecordInstance(Object obj) {
        return obj != null && obj.getClass().isRecord();
    }

    /**
     * Get the names of record components
     * 获取 record 组件的名称
     *
     * @param recordClass the record class - record 类
     * @return list of component names
     */
    public static List<String> componentNames(Class<? extends Record> recordClass) {
        return Arrays.stream(recordClass.getRecordComponents())
                .map(RecordComponent::getName)
                .toList();
    }

    /**
     * Get the types of record components
     * 获取 record 组件的类型
     *
     * @param recordClass the record class - record 类
     * @return list of component types
     */
    public static List<Class<?>> componentTypes(Class<? extends Record> recordClass) {
        return Arrays.stream(recordClass.getRecordComponents())
                .map(RecordComponent::getType)
                .toList();
    }

    /**
     * Get record component count
     * 获取 record 组件数量
     *
     * @param recordClass the record class - record 类
     * @return number of components
     */
    public static int componentCount(Class<? extends Record> recordClass) {
        return recordClass.getRecordComponents().length;
    }

    // ==================== Value Access | 值访问 ====================

    /**
     * Get all component values from a record
     * 从 record 获取所有组件值
     *
     * @param record the record - record
     * @return list of values in component order
     */
    public static List<Object> componentValues(Record record) {
        RecordComponent[] components = record.getClass().getRecordComponents();
        List<Object> values = new ArrayList<>();
        for (RecordComponent component : components) {
            try {
                values.add(component.getAccessor().invoke(record));
            } catch (Exception e) {
                throw new OpenFunctionalException("Failed to get component value: " + component.getName(), e);
            }
        }
        return values;
    }

    /**
     * Get a specific component value by name
     * 按名称获取特定组件值
     *
     * @param record        the record - record
     * @param componentName component name - 组件名称
     * @param <T>           value type - 值类型
     * @return the value
     * @throws IllegalArgumentException if component not found
     */
    @SuppressWarnings("unchecked")
    public static <T> T getComponent(Record record, String componentName) {
        RecordComponent[] components = record.getClass().getRecordComponents();
        for (RecordComponent component : components) {
            if (component.getName().equals(componentName)) {
                try {
                    return (T) component.getAccessor().invoke(record);
                } catch (Exception e) {
                    throw new OpenFunctionalException("Failed to get component: " + componentName, e);
                }
            }
        }
        throw new IllegalArgumentException("Component not found: " + componentName);
    }

    /**
     * Get component value as Try
     * 获取组件值为 Try
     *
     * @param record        the record - record
     * @param componentName component name - 组件名称
     * @param <T>           value type - 值类型
     * @return Try containing value
     */
    public static <T> Try<T> getComponentTry(Record record, String componentName) {
        return Try.of(() -> getComponent(record, componentName));
    }

    // ==================== Modification | 修改 ====================

    /**
     * Copy a record with modifications
     * 带修改复制 record
     *
     * @param record        the record - record
     * @param modifications map of component name to new value - 组件名到新值的映射
     * @param <R>           record type - record 类型
     * @return new record with modifications
     */
    @SuppressWarnings("unchecked")
    public static <R extends Record> R copy(R record, Map<String, Object> modifications) {
        Class<? extends Record> recordClass = record.getClass();
        RecordComponent[] components = recordClass.getRecordComponents();

        Object[] args = new Object[components.length];
        Class<?>[] types = new Class<?>[components.length];

        for (int i = 0; i < components.length; i++) {
            RecordComponent component = components[i];
            types[i] = component.getType();
            String name = component.getName();

            if (modifications.containsKey(name)) {
                args[i] = modifications.get(name);
            } else {
                try {
                    args[i] = component.getAccessor().invoke(record);
                } catch (Exception e) {
                    throw new OpenFunctionalException("Failed to get component: " + component.getName(), e);
                }
            }
        }

        try {
            Constructor<?> constructor = recordClass.getDeclaredConstructor(types);
            return (R) constructor.newInstance(args);
        } catch (Exception e) {
            throw new OpenFunctionalException("Failed to create record copy", e);
        }
    }

    /**
     * Copy a record modifying single component
     * 复制 record 修改单个组件
     *
     * @param record        the record - record
     * @param componentName component to modify - 要修改的组件
     * @param newValue      new value - 新值
     * @param <R>           record type - record 类型
     * @return new record with modification
     */
    public static <R extends Record> R copyWith(R record, String componentName, Object newValue) {
        return copy(record, Map.of(componentName, newValue));
    }

    /**
     * Copy a record transforming a component
     * 复制 record 转换组件
     *
     * @param record        the record - record
     * @param componentName component to transform - 要转换的组件
     * @param transformer   transformation function - 转换函数
     * @param <R>           record type - record 类型
     * @param <T>           component type - 组件类型
     * @return new record with transformed component
     */
    public static <R extends Record, T> R copyTransforming(R record, String componentName,
                                                            Function<T, T> transformer) {
        T oldValue = getComponent(record, componentName);
        T newValue = transformer.apply(oldValue);
        return copyWith(record, componentName, newValue);
    }

    // ==================== Lens Creation | 透镜创建 ====================

    /**
     * Create a lens for a record component
     * 为 record 组件创建透镜
     *
     * @param recordClass   the record class - record 类
     * @param componentName component name - 组件名称
     * @param <R>           record type - record 类型
     * @param <T>           component type - 组件类型
     * @return lens for the component
     */
    public static <R extends Record, T> Lens<R, T> lens(Class<R> recordClass, String componentName) {
        return Lens.of(
            record -> getComponent(record, componentName),
            (record, value) -> copyWith(record, componentName, value)
        );
    }

    /**
     * Create lenses for all components
     * 为所有组件创建透镜
     *
     * @param recordClass the record class - record 类
     * @param <R>         record type - record 类型
     * @return map of component name to lens
     */
    public static <R extends Record> Map<String, Lens<R, ?>> lenses(Class<R> recordClass) {
        Map<String, Lens<R, ?>> result = new LinkedHashMap<>();
        for (RecordComponent component : recordClass.getRecordComponents()) {
            result.put(component.getName(), lens(recordClass, component.getName()));
        }
        return result;
    }

    // ==================== Map Conversion | Map 转换 ====================

    /**
     * Convert record to Map
     * 将 record 转换为 Map
     *
     * @param record the record - record
     * @return map of component names to values
     */
    public static Map<String, Object> toMap(Record record) {
        Map<String, Object> map = new LinkedHashMap<>();
        RecordComponent[] components = record.getClass().getRecordComponents();
        for (RecordComponent component : components) {
            try {
                Object value = component.getAccessor().invoke(record);
                map.put(component.getName(), value);
            } catch (Exception e) {
                throw new OpenFunctionalException("Failed to get component: " + component.getName(), e);
            }
        }
        return map;
    }

    /**
     * Create record from Map
     * 从 Map 创建 record
     *
     * @param recordClass the record class - record 类
     * @param map         map of component names to values - 组件名到值的映射
     * @param <R>         record type - record 类型
     * @return new record
     */
    @SuppressWarnings("unchecked")
    public static <R extends Record> R fromMap(Class<R> recordClass, Map<String, Object> map) {
        RecordComponent[] components = recordClass.getRecordComponents();
        Object[] args = new Object[components.length];
        Class<?>[] types = new Class<?>[components.length];

        for (int i = 0; i < components.length; i++) {
            RecordComponent component = components[i];
            types[i] = component.getType();
            args[i] = map.get(component.getName());
        }

        try {
            Constructor<?> constructor = recordClass.getDeclaredConstructor(types);
            return (R) constructor.newInstance(args);
        } catch (Exception e) {
            throw new OpenFunctionalException("Failed to create record from map", e);
        }
    }

    /**
     * Convert record to Map as Try
     * 将 record 转换为 Map 作为 Try
     *
     * @param record the record - record
     * @return Try containing map
     */
    public static Try<Map<String, Object>> toMapTry(Record record) {
        return Try.of(() -> toMap(record));
    }

    /**
     * Create record from Map as Try
     * 从 Map 创建 record 作为 Try
     *
     * @param recordClass the record class - record 类
     * @param map         map of values - 值的映射
     * @param <R>         record type - record 类型
     * @return Try containing record
     */
    public static <R extends Record> Try<R> fromMapTry(Class<R> recordClass, Map<String, Object> map) {
        return Try.of(() -> fromMap(recordClass, map));
    }

    // ==================== Comparison | 比较 ====================

    /**
     * Compare two records by components
     * 按组件比较两个 record
     *
     * @param r1 first record - 第一个 record
     * @param r2 second record - 第二个 record
     * @return map of differing components
     */
    public static Map<String, Object[]> diff(Record r1, Record r2) {
        if (!r1.getClass().equals(r2.getClass())) {
            throw new IllegalArgumentException("Records must be of same type");
        }

        Map<String, Object[]> differences = new LinkedHashMap<>();
        RecordComponent[] components = r1.getClass().getRecordComponents();

        for (RecordComponent component : components) {
            try {
                Object v1 = component.getAccessor().invoke(r1);
                Object v2 = component.getAccessor().invoke(r2);
                if (!Objects.equals(v1, v2)) {
                    differences.put(component.getName(), new Object[]{v1, v2});
                }
            } catch (Exception e) {
                throw new OpenFunctionalException("Failed to compare component: " + component.getName(), e);
            }
        }
        return differences;
    }

    // ==================== Factory Helper | 工厂辅助 ====================

    /**
     * Create a factory function for a record type
     * 为 record 类型创建工厂函数
     *
     * @param recordClass the record class - record 类
     * @param <R>         record type - record 类型
     * @return factory function accepting array of values
     */
    @SuppressWarnings("unchecked")
    public static <R extends Record> Function<Object[], R> factory(Class<R> recordClass) {
        RecordComponent[] components = recordClass.getRecordComponents();
        Class<?>[] types = Arrays.stream(components)
                .map(RecordComponent::getType)
                .toArray(Class<?>[]::new);

        try {
            Constructor<?> constructor = recordClass.getDeclaredConstructor(types);
            return args -> {
                try {
                    return (R) constructor.newInstance(args);
                } catch (Exception e) {
                    throw new OpenFunctionalException("Failed to create record", e);
                }
            };
        } catch (NoSuchMethodException e) {
            throw new OpenFunctionalException("No canonical constructor found", e);
        }
    }

    /**
     * Create a two-argument factory for records with 2 components
     * 为有 2 个组件的 record 创建双参数工厂
     *
     * @param recordClass the record class - record 类
     * @param <R>         record type - record 类型
     * @param <A>         first component type - 第一个组件类型
     * @param <B>         second component type - 第二个组件类型
     * @return BiFunction factory
     */
    public static <R extends Record, A, B> BiFunction<A, B, R> factory2(Class<R> recordClass) {
        Function<Object[], R> factory = factory(recordClass);
        return (a, b) -> factory.apply(new Object[]{a, b});
    }

    /**
     * Check if record has component with name
     * 检查 record 是否有指定名称的组件
     *
     * @param recordClass   the record class - record 类
     * @param componentName component name - 组件名称
     * @return true if component exists
     */
    public static boolean hasComponent(Class<? extends Record> recordClass, String componentName) {
        return Arrays.stream(recordClass.getRecordComponents())
                .anyMatch(c -> c.getName().equals(componentName));
    }
}
