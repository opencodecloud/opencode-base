package cloud.opencode.base.core.reflect;

import cloud.opencode.base.core.exception.OpenException;

import java.lang.reflect.*;
import java.util.*;

/**
 * Record Utility Class - Java Record type operations
 * Record 工具类 - Java Record 类型操作
 *
 * <p>Provides utilities for working with Java Record types (JDK 16+).</p>
 * <p>提供 Java Record 类型（JDK 16+）的操作工具。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Record type detection - Record 类型检测</li>
 *   <li>Component access (names, types, values) - 组件访问（名称、类型、值）</li>
 *   <li>Record to/from Map conversion - Record 与 Map 转换</li>
 *   <li>Immutable copy with modifications - 不可变复制（带修改）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Check if record - 检查是否为 Record
 * boolean isRecord = RecordUtil.isRecord(User.class);
 *
 * // Record to Map - Record 转 Map
 * Map<String, Object> map = RecordUtil.toMap(userRecord);
 *
 * // Copy with modification - 带修改复制
 * User updated = RecordUtil.copyWith(user, "name", "NewName");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是 (无状态)</li>
 *   <li>Null-safe: Partial (throws on null record) - 空值安全: 部分 (null 抛异常)</li>
 * </ul>
 *
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) where n = record components - O(n), n为记录组件数</li>
 *   <li>Space complexity: O(n) for component array - 组件数组 O(n)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public final class RecordUtil {

    private RecordUtil() {
    }

    /**
     * Checks if the class is a Record type
     * 检查类是否为 Record 类型
     */
    public static boolean isRecord(Class<?> clazz) {
        return clazz != null && clazz.isRecord();
    }

    /**
     * Checks if the object is a Record instance
     * 检查对象是否为 Record 实例
     */
    public static boolean isRecord(Object obj) {
        return obj != null && obj.getClass().isRecord();
    }

    /**
     * Gets all components of the Record
     * 获取 Record 的所有组件
     */
    public static RecordComponent[] getComponents(Class<?> recordClass) {
        if (!isRecord(recordClass)) {
            throw new OpenException("core", "RECORD-001", "Class is not a record: " + recordClass.getName());
        }
        return recordClass.getRecordComponents();
    }

    /**
     * Gets the list of Record component names
     * 获取 Record 组件名列表
     */
    public static List<String> getComponentNames(Class<?> recordClass) {
        return Arrays.stream(getComponents(recordClass))
                .map(RecordComponent::getName)
                .toList();
    }

    /**
     * Gets the Record component type mapping
     * 获取 Record 组件类型映射
     */
    public static Map<String, Class<?>> getComponentTypes(Class<?> recordClass) {
        Map<String, Class<?>> types = new LinkedHashMap<>();
        for (RecordComponent component : getComponents(recordClass)) {
            types.put(component.getName(), component.getType());
        }
        return types;
    }

    /**
     * Gets the Record component value
     * 获取 Record 组件值
     */
    @SuppressWarnings("unchecked")
    public static <T> T getComponentValue(Object record, String componentName) {
        if (!isRecord(record)) {
            throw new OpenException("core", "RECORD-001", "Object is not a record");
        }
        try {
            Method accessor = record.getClass().getMethod(componentName);
            return (T) accessor.invoke(record);
        } catch (Exception e) {
            throw new OpenException("core", "RECORD-002", "Failed to get component value: " + componentName, e);
        }
    }

    /**
     * Converts a Record to Map
     * Record 转 Map
     */
    public static Map<String, Object> toMap(Object record) {
        if (!isRecord(record)) {
            throw new OpenException("core", "RECORD-001", "Object is not a record");
        }
        Map<String, Object> map = new LinkedHashMap<>();
        for (RecordComponent component : record.getClass().getRecordComponents()) {
            try {
                Method accessor = component.getAccessor();
                accessor.setAccessible(true);
                map.put(component.getName(), accessor.invoke(record));
            } catch (Exception e) {
                throw new OpenException("core", "RECORD-002", "Failed to convert record to map", e);
            }
        }
        return map;
    }

    /**
     * Converts a Map to Record
     * Map 转 Record
     */
    @SuppressWarnings("unchecked")
    public static <T extends Record> T fromMap(Map<String, ?> map, Class<T> recordClass) {
        if (!isRecord(recordClass)) {
            throw new OpenException("core", "RECORD-001", "Class is not a record: " + recordClass.getName());
        }
        try {
            RecordComponent[] components = recordClass.getRecordComponents();
            Class<?>[] paramTypes = new Class<?>[components.length];
            Object[] args = new Object[components.length];

            for (int i = 0; i < components.length; i++) {
                paramTypes[i] = components[i].getType();
                args[i] = convertValue(map.get(components[i].getName()), components[i].getType());
            }

            Constructor<T> constructor = recordClass.getDeclaredConstructor(paramTypes);
            constructor.setAccessible(true);
            return constructor.newInstance(args);
        } catch (Exception e) {
            throw new OpenException("core", "RECORD-003", "Failed to create record from map", e);
        }
    }

    /**
     * Copies a Record and modifies the specified component
     * 复制 Record 并修改指定组件
     */
    @SuppressWarnings("unchecked")
    public static <T extends Record> T copyWith(T record, String componentName, Object newValue) {
        if (!isRecord(record)) {
            throw new OpenException("core", "RECORD-001", "Object is not a record");
        }
        Map<String, Object> map = toMap(record);
        map.put(componentName, newValue);
        return (T) fromMap(map, record.getClass());
    }

    /**
     * Copies a Record and modifies multiple components
     * 复制 Record 并修改多个组件
     */
    @SuppressWarnings("unchecked")
    public static <T extends Record> T copyWith(T record, Map<String, Object> changes) {
        if (!isRecord(record)) {
            throw new OpenException("core", "RECORD-001", "Object is not a record");
        }
        Map<String, Object> map = toMap(record);
        map.putAll(changes);
        return (T) fromMap(map, record.getClass());
    }

    /**
     * Compares two Records for equality (based on components)
     * 比较两个 Record 是否相等（基于组件）
     */
    public static boolean equals(Object record1, Object record2) {
        if (record1 == null || record2 == null) return false;
        if (record1 == record2) return true;
        if (!isRecord(record1) || !isRecord(record2)) return false;
        if (record1.getClass() != record2.getClass()) return false;

        Map<String, Object> map1 = toMap(record1);
        Map<String, Object> map2 = toMap(record2);
        return map1.equals(map2);
    }

    /**
     * Gets the Record component count
     * 获取 Record 组件数量
     */
    public static int getComponentCount(Class<?> recordClass) {
        return getComponents(recordClass).length;
    }

    /**
     * Checks if the Record has the specified component
     * 检查 Record 是否有指定组件
     */
    public static boolean hasComponent(Class<?> recordClass, String componentName) {
        return getComponentNames(recordClass).contains(componentName);
    }

    /**
     * Gets the generic type of a component
     * 获取组件的泛型类型
     */
    public static Type getComponentGenericType(Class<?> recordClass, String componentName) {
        for (RecordComponent component : getComponents(recordClass)) {
            if (component.getName().equals(componentName)) {
                return component.getGenericType();
            }
        }
        return null;
    }

    private static Object convertValue(Object value, Class<?> targetType) {
        if (value == null) return getDefaultValue(targetType);
        if (targetType.isInstance(value)) return value;

        // 基本类型转换
        if (targetType == int.class || targetType == Integer.class) {
            return ((Number) value).intValue();
        }
        if (targetType == long.class || targetType == Long.class) {
            return ((Number) value).longValue();
        }
        if (targetType == double.class || targetType == Double.class) {
            return ((Number) value).doubleValue();
        }
        if (targetType == float.class || targetType == Float.class) {
            return ((Number) value).floatValue();
        }
        if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.valueOf(value.toString());
        }
        if (targetType == String.class) {
            return value.toString();
        }

        return value;
    }

    private static Object getDefaultValue(Class<?> type) {
        if (type.isPrimitive()) {
            if (type == boolean.class) return false;
            if (type == byte.class) return (byte) 0;
            if (type == char.class) return '\0';
            if (type == short.class) return (short) 0;
            if (type == int.class) return 0;
            if (type == long.class) return 0L;
            if (type == float.class) return 0.0f;
            if (type == double.class) return 0.0d;
        }
        return null;
    }
}
