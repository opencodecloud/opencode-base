package cloud.opencode.base.reflect.record;

import cloud.opencode.base.reflect.exception.OpenReflectException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Record Utility Class
 * Record工具类
 *
 * <p>Provides low-level record operation utilities with caching.</p>
 * <p>提供带缓存的底层record操作工具。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Record component discovery with caching - 带缓存的record组件发现</li>
 *   <li>Canonical constructor resolution - 规范构造器解析</li>
 *   <li>Accessor method caching - 访问器方法缓存</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * RecordComponent[] components = RecordUtil.getComponents(User.class);
 * Constructor<?> ctor = RecordUtil.getCanonicalConstructor(User.class);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (uses ConcurrentHashMap for caching) - 线程安全: 是（使用ConcurrentHashMap缓存）</li>
 *   <li>Null-safe: No (caller must ensure non-null arguments) - 空值安全: 否（调用方须确保非空参数）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) for cached lookups; O(c) for first access where c is the number of record components - 时间复杂度: 缓存命中时 O(1)；首次访问为 O(c)，c为 record 组件数量</li>
 *   <li>Space complexity: O(c) for the cached component and accessor maps - 空间复杂度: O(c)，缓存组件和访问器映射</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
public final class RecordUtil {

    private static final Map<Class<?>, java.lang.reflect.RecordComponent[]> COMPONENT_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Constructor<?>> CANONICAL_CONSTRUCTOR_CACHE = new ConcurrentHashMap<>();
    private static final Map<ComponentKey, Method> ACCESSOR_CACHE = new ConcurrentHashMap<>();

    private RecordUtil() {
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
     * @throws IllegalArgumentException if not a record | 如果不是record
     */
    public static void requireRecord(Class<?> clazz) {
        if (!isRecord(clazz)) {
            throw new IllegalArgumentException("Class is not a record: " + clazz);
        }
    }

    // ==================== Component Discovery | 组件发现 ====================

    /**
     * Gets record components (cached)
     * 获取record组件（缓存）
     *
     * @param recordClass the record class | record类
     * @return array of record components | record组件数组
     */
    public static java.lang.reflect.RecordComponent[] getRecordComponents(Class<?> recordClass) {
        requireRecord(recordClass);
        return COMPONENT_CACHE.computeIfAbsent(recordClass, Class::getRecordComponents).clone();
    }

    /**
     * Gets component count
     * 获取组件数量
     *
     * @param recordClass the record class | record类
     * @return the count | 数量
     */
    public static int getComponentCount(Class<?> recordClass) {
        return getRecordComponents(recordClass).length;
    }

    /**
     * Gets component names
     * 获取组件名称
     *
     * @param recordClass the record class | record类
     * @return list of names | 名称列表
     */
    public static List<String> getComponentNames(Class<?> recordClass) {
        java.lang.reflect.RecordComponent[] components = getRecordComponents(recordClass);
        List<String> names = new ArrayList<>(components.length);
        for (java.lang.reflect.RecordComponent comp : components) {
            names.add(comp.getName());
        }
        return names;
    }

    /**
     * Gets component types
     * 获取组件类型
     *
     * @param recordClass the record class | record类
     * @return array of types | 类型数组
     */
    public static Class<?>[] getComponentTypes(Class<?> recordClass) {
        java.lang.reflect.RecordComponent[] components = getRecordComponents(recordClass);
        Class<?>[] types = new Class<?>[components.length];
        for (int i = 0; i < components.length; i++) {
            types[i] = components[i].getType();
        }
        return types;
    }

    /**
     * Gets component by name
     * 按名称获取组件
     *
     * @param recordClass   the record class | record类
     * @param componentName the component name | 组件名
     * @return the component or null | 组件或null
     */
    public static java.lang.reflect.RecordComponent getComponent(Class<?> recordClass, String componentName) {
        for (java.lang.reflect.RecordComponent comp : getRecordComponents(recordClass)) {
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
    public static java.lang.reflect.RecordComponent getComponent(Class<?> recordClass, int index) {
        java.lang.reflect.RecordComponent[] components = getRecordComponents(recordClass);
        if (index < 0 || index >= components.length) {
            throw new IndexOutOfBoundsException("Component index: " + index);
        }
        return components[index];
    }

    // ==================== Accessor Methods | 访问器方法 ====================

    /**
     * Gets accessor method for a component (cached)
     * 获取组件的访问器方法（缓存）
     *
     * @param recordClass   the record class | record类
     * @param componentName the component name | 组件名
     * @return the accessor method | 访问器方法
     */
    public static Method getAccessor(Class<?> recordClass, String componentName) {
        ComponentKey key = new ComponentKey(recordClass, componentName);
        return ACCESSOR_CACHE.computeIfAbsent(key, k -> {
            java.lang.reflect.RecordComponent comp = getComponent(k.recordClass(), k.componentName());
            return comp != null ? comp.getAccessor() : null;
        });
    }

    /**
     * Gets all accessor methods
     * 获取所有访问器方法
     *
     * @param recordClass the record class | record类
     * @return list of accessor methods | 访问器方法列表
     */
    public static List<Method> getAccessors(Class<?> recordClass) {
        java.lang.reflect.RecordComponent[] components = getRecordComponents(recordClass);
        List<Method> accessors = new ArrayList<>(components.length);
        for (java.lang.reflect.RecordComponent comp : components) {
            accessors.add(comp.getAccessor());
        }
        return accessors;
    }

    // ==================== Value Access | 值访问 ====================

    /**
     * Gets component value
     * 获取组件值
     *
     * @param record        the record | record
     * @param componentName the component name | 组件名
     * @return the value | 值
     */
    public static Object getComponentValue(Record record, String componentName) {
        Method accessor = getAccessor(record.getClass(), componentName);
        if (accessor == null) {
            throw new OpenReflectException("Component not found: " + componentName);
        }
        try {
            return accessor.invoke(record);
        } catch (Exception e) {
            throw new OpenReflectException("Failed to get component value: " + componentName, e);
        }
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
    public static <T> T getComponentValue(Record record, String componentName, Class<T> type) {
        return (T) getComponentValue(record, componentName);
    }

    /**
     * Gets all component values
     * 获取所有组件值
     *
     * @param record the record | record
     * @return array of values | 值数组
     */
    public static Object[] getComponentValues(Record record) {
        java.lang.reflect.RecordComponent[] components = getRecordComponents(record.getClass());
        Object[] values = new Object[components.length];
        for (int i = 0; i < components.length; i++) {
            try {
                values[i] = components[i].getAccessor().invoke(record);
            } catch (Exception e) {
                throw new OpenReflectException("Failed to get component value", e);
            }
        }
        return values;
    }

    // ==================== Constructor | 构造器 ====================

    /**
     * Gets canonical constructor (cached)
     * 获取规范构造器（缓存）
     *
     * @param recordClass the record class | record类
     * @param <T>         the record type | record类型
     * @return the canonical constructor | 规范构造器
     */
    @SuppressWarnings("unchecked")
    public static <T extends Record> Constructor<T> getCanonicalConstructor(Class<T> recordClass) {
        return (Constructor<T>) CANONICAL_CONSTRUCTOR_CACHE.computeIfAbsent(recordClass, clazz -> {
            Class<?>[] paramTypes = getComponentTypes(clazz);
            try {
                return clazz.getDeclaredConstructor(paramTypes);
            } catch (NoSuchMethodException e) {
                throw new OpenReflectException("Canonical constructor not found", e);
            }
        });
    }

    /**
     * Creates a new record instance
     * 创建新的record实例
     *
     * @param recordClass the record class | record类
     * @param values      the component values | 组件值
     * @param <T>         the record type | record类型
     * @return the record instance | record实例
     */
    public static <T extends Record> T newInstance(Class<T> recordClass, Object... values) {
        try {
            // Resolve fresh constructor to avoid mutating the cached one's accessibility
            Class<?>[] paramTypes = getComponentTypes(recordClass);
            Constructor<T> ctor = recordClass.getDeclaredConstructor(paramTypes);
            ctor.setAccessible(true);
            return ctor.newInstance(values);
        } catch (NoSuchMethodException e) {
            throw new OpenReflectException("Canonical constructor not found for " + recordClass.getName(), e);
        } catch (Exception e) {
            throw new OpenReflectException("Failed to create record instance", e);
        }
    }

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
        Class<T> recordClass = (Class<T>) record.getClass();
        Object[] values = getComponentValues(record);
        return newInstance(recordClass, values);
    }

    // ==================== Conversion | 转换 ====================

    /**
     * Converts record to map
     * 将record转换为map
     *
     * @param record the record | record
     * @return the map | 映射
     */
    public static Map<String, Object> toMap(Record record) {
        java.lang.reflect.RecordComponent[] components = getRecordComponents(record.getClass());
        Map<String, Object> map = new LinkedHashMap<>(components.length);
        for (java.lang.reflect.RecordComponent comp : components) {
            try {
                map.put(comp.getName(), comp.getAccessor().invoke(record));
            } catch (Exception e) {
                throw new OpenReflectException("Failed to convert record to map", e);
            }
        }
        return map;
    }

    /**
     * Creates record from map
     * 从map创建record
     *
     * @param recordClass the record class | record类
     * @param map         the source map | 源映射
     * @param <T>         the record type | record类型
     * @return the record | record
     */
    public static <T extends Record> T fromMap(Class<T> recordClass, Map<String, ?> map) {
        List<String> names = getComponentNames(recordClass);
        Object[] values = new Object[names.size()];
        for (int i = 0; i < names.size(); i++) {
            values[i] = map.get(names.get(i));
        }
        return newInstance(recordClass, values);
    }

    // ==================== Cache Management | 缓存管理 ====================

    /**
     * Clears all caches
     * 清除所有缓存
     */
    public static void clearCache() {
        COMPONENT_CACHE.clear();
        CANONICAL_CONSTRUCTOR_CACHE.clear();
        ACCESSOR_CACHE.clear();
    }

    /**
     * Clears cache for specific class
     * 清除特定类的缓存
     *
     * @param clazz the class | 类
     */
    public static void clearCache(Class<?> clazz) {
        COMPONENT_CACHE.remove(clazz);
        CANONICAL_CONSTRUCTOR_CACHE.remove(clazz);
        ACCESSOR_CACHE.keySet().removeIf(key -> key.recordClass() == clazz);
    }

    // ==================== Internal | 内部 ====================

    private record ComponentKey(Class<?> recordClass, String componentName) {
    }
}
