package cloud.opencode.base.reflect;

import cloud.opencode.base.reflect.exception.OpenReflectException;
import cloud.opencode.base.reflect.type.TypeToken;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Field Facade Entry Class
 * 字段门面入口类
 *
 * <p>Provides common field operations API, internally delegates to FieldUtil.
 * Similar to Commons Lang FieldUtils.</p>
 * <p>提供常用字段操作API，内部委托给FieldUtil。
 * 对标Commons Lang FieldUtils。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Field retrieval (with inheritance) - 字段获取（含继承）</li>
 *   <li>Field read/write operations - 字段读写操作</li>
 *   <li>Batch field operations - 批量字段操作</li>
 *   <li>Annotation-based filtering - 基于注解的过滤</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Read field value
 * Object value = OpenField.readField(obj, "name");
 *
 * // Write field value
 * OpenField.writeField(obj, "name", "newValue");
 *
 * // Get all fields with annotation
 * List<Field> fields = OpenField.getFieldsWithAnnotation(User.class, Column.class);
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
public final class OpenField {

    private OpenField() {
    }

    // ==================== Field Retrieval | 字段获取 ====================

    /**
     * Gets a field (including inherited)
     * 获取字段（包含继承）
     *
     * @param clazz     the class | 类
     * @param fieldName the field name | 字段名
     * @return the field | 字段
     * @throws OpenReflectException if not found | 如果未找到
     */
    public static Field getField(Class<?> clazz, String fieldName) {
        return getField(clazz, fieldName, true);
    }

    /**
     * Gets a field with optional force access
     * 获取字段（可选强制访问）
     *
     * @param clazz       the class | 类
     * @param fieldName   the field name | 字段名
     * @param forceAccess whether to force access | 是否强制访问
     * @return the field | 字段
     * @throws OpenReflectException if not found | 如果未找到
     */
    public static Field getField(Class<?> clazz, String fieldName, boolean forceAccess) {
        Objects.requireNonNull(clazz, "clazz must not be null");
        Objects.requireNonNull(fieldName, "fieldName must not be null");

        Class<?> current = clazz;
        while (current != null) {
            try {
                Field field = current.getDeclaredField(fieldName);
                if (forceAccess) {
                    field.setAccessible(true);
                }
                return field;
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        throw OpenReflectException.fieldNotFound(clazz, fieldName);
    }

    /**
     * Gets a declared field (not inherited)
     * 获取声明的字段（不含继承）
     *
     * @param clazz     the class | 类
     * @param fieldName the field name | 字段名
     * @return the field | 字段
     * @throws OpenReflectException if not found | 如果未找到
     */
    public static Field getDeclaredField(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            throw OpenReflectException.fieldNotFound(clazz, fieldName);
        }
    }

    /**
     * Gets all fields (including inherited)
     * 获取所有字段（包含继承）
     *
     * @param clazz the class | 类
     * @return list of fields | 字段列表
     */
    public static List<Field> getAllFields(Class<?> clazz) {
        List<Field> result = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            Collections.addAll(result, current.getDeclaredFields());
            current = current.getSuperclass();
        }
        return result;
    }

    /**
     * Gets declared fields (not inherited)
     * 获取声明的字段（不含继承）
     *
     * @param clazz the class | 类
     * @return list of fields | 字段列表
     */
    public static List<Field> getDeclaredFields(Class<?> clazz) {
        return Arrays.asList(clazz.getDeclaredFields());
    }

    /**
     * Gets fields with specific annotation
     * 按注解获取字段
     *
     * @param clazz           the class | 类
     * @param annotationClass the annotation class | 注解类
     * @return list of fields | 字段列表
     */
    public static List<Field> getFieldsWithAnnotation(Class<?> clazz, Class<? extends Annotation> annotationClass) {
        return getAllFields(clazz).stream()
                .filter(f -> f.isAnnotationPresent(annotationClass))
                .toList();
    }

    /**
     * Gets fields of specific type
     * 按类型获取字段
     *
     * @param clazz     the class | 类
     * @param fieldType the field type | 字段类型
     * @return list of fields | 字段列表
     */
    public static List<Field> getFieldsOfType(Class<?> clazz, Class<?> fieldType) {
        return getAllFields(clazz).stream()
                .filter(f -> fieldType.isAssignableFrom(f.getType()))
                .toList();
    }

    /**
     * Gets fields with specific modifiers
     * 按修饰符获取字段
     *
     * @param clazz     the class | 类
     * @param modifiers the modifiers | 修饰符
     * @return list of fields | 字段列表
     */
    public static List<Field> getFieldsWithModifiers(Class<?> clazz, int... modifiers) {
        int combined = 0;
        for (int m : modifiers) {
            combined |= m;
        }
        final int mask = combined;
        return getAllFields(clazz).stream()
                .filter(f -> (f.getModifiers() & mask) == mask)
                .toList();
    }

    // ==================== Field Reading | 字段读取 ====================

    /**
     * Reads field value
     * 读取字段值
     *
     * @param target    the target object | 目标对象
     * @param fieldName the field name | 字段名
     * @return the value | 值
     */
    public static Object readField(Object target, String fieldName) {
        return readField(target, fieldName, true);
    }

    /**
     * Reads field value with optional force access
     * 读取字段值（可选强制访问）
     *
     * @param target      the target object | 目标对象
     * @param fieldName   the field name | 字段名
     * @param forceAccess whether to force access | 是否强制访问
     * @return the value | 值
     */
    public static Object readField(Object target, String fieldName, boolean forceAccess) {
        Field field = getField(target.getClass(), fieldName, forceAccess);
        return readField(field, target);
    }

    /**
     * Reads field value using Field object
     * 读取字段值（Field对象）
     *
     * @param field  the field | 字段
     * @param target the target object | 目标对象
     * @return the value | 值
     */
    public static Object readField(Field field, Object target) {
        try {
            field.setAccessible(true);
            return field.get(target);
        } catch (IllegalAccessException e) {
            throw OpenReflectException.fieldAccessFailed(field.getDeclaringClass(), field.getName(), e);
        }
    }

    /**
     * Reads field value with type safety
     * 读取字段值（泛型安全）
     *
     * @param target    the target object | 目标对象
     * @param fieldName the field name | 字段名
     * @param valueType the expected type | 期望类型
     * @param <T>       the value type | 值类型
     * @return the value | 值
     */
    @SuppressWarnings("unchecked")
    public static <T> T readField(Object target, String fieldName, Class<T> valueType) {
        Object value = readField(target, fieldName);
        if (value == null) {
            return null;
        }
        if (!valueType.isInstance(value)) {
            throw OpenReflectException.typeCastFailed(valueType, value);
        }
        return (T) value;
    }

    /**
     * Reads static field
     * 读取静态字段
     *
     * @param clazz     the class | 类
     * @param fieldName the field name | 字段名
     * @return the value | 值
     */
    public static Object readStaticField(Class<?> clazz, String fieldName) {
        Field field = getField(clazz, fieldName, true);
        return readField(field, (Object) null);
    }

    /**
     * Reads static field with type safety
     * 读取静态字段（泛型安全）
     *
     * @param clazz     the class | 类
     * @param fieldName the field name | 字段名
     * @param valueType the expected type | 期望类型
     * @param <T>       the value type | 值类型
     * @return the value | 值
     */
    @SuppressWarnings("unchecked")
    public static <T> T readStaticField(Class<?> clazz, String fieldName, Class<T> valueType) {
        Object value = readStaticField(clazz, fieldName);
        if (value == null) {
            return null;
        }
        if (!valueType.isInstance(value)) {
            throw OpenReflectException.typeCastFailed(valueType, value);
        }
        return (T) value;
    }

    // ==================== Field Writing | 字段写入 ====================

    /**
     * Writes field value
     * 写入字段值
     *
     * @param target    the target object | 目标对象
     * @param fieldName the field name | 字段名
     * @param value     the value | 值
     */
    public static void writeField(Object target, String fieldName, Object value) {
        writeField(target, fieldName, value, true);
    }

    /**
     * Writes field value with optional force access
     * 写入字段值（可选强制访问）
     *
     * @param target      the target object | 目标对象
     * @param fieldName   the field name | 字段名
     * @param value       the value | 值
     * @param forceAccess whether to force access | 是否强制访问
     */
    public static void writeField(Object target, String fieldName, Object value, boolean forceAccess) {
        Field field = getField(target.getClass(), fieldName, forceAccess);
        writeField(field, target, value);
    }

    /**
     * Writes field value using Field object
     * 写入字段值（Field对象）
     *
     * @param field  the field | 字段
     * @param target the target object | 目标对象
     * @param value  the value | 值
     */
    public static void writeField(Field field, Object target, Object value) {
        try {
            field.setAccessible(true);
            field.set(target, value);
        } catch (IllegalAccessException e) {
            throw OpenReflectException.fieldAccessFailed(field.getDeclaringClass(), field.getName(), e);
        }
    }

    /**
     * Writes static field
     * 写入静态字段
     *
     * @param clazz     the class | 类
     * @param fieldName the field name | 字段名
     * @param value     the value | 值
     */
    public static void writeStaticField(Class<?> clazz, String fieldName, Object value) {
        Field field = getField(clazz, fieldName, true);
        writeField(field, (Object) null, value);
    }

    /**
     * Removes final modifier and writes value
     * 移除final修饰符并写入值
     *
     * @param target    the target object | 目标对象
     * @param fieldName the field name | 字段名
     * @param value     the value | 值
     */
    public static void removeFinalAndWrite(Object target, String fieldName, Object value) {
        Field field = getField(target.getClass(), fieldName, true);
        try {
            // In JDK 12+, modifying final fields via reflection is restricted
            // This method may not work for truly final fields
            field.setAccessible(true);
            field.set(target, value);
        } catch (IllegalAccessException e) {
            throw OpenReflectException.fieldAccessFailed(field.getDeclaringClass(), fieldName, e);
        }
    }

    // ==================== Field Information | 字段信息 ====================

    /**
     * Gets field type
     * 获取字段类型
     *
     * @param clazz     the class | 类
     * @param fieldName the field name | 字段名
     * @return the field type | 字段类型
     */
    public static Class<?> getFieldType(Class<?> clazz, String fieldName) {
        return getField(clazz, fieldName).getType();
    }

    /**
     * Gets field generic type
     * 获取字段泛型类型
     *
     * @param clazz     the class | 类
     * @param fieldName the field name | 字段名
     * @return the generic type | 泛型类型
     */
    public static Type getFieldGenericType(Class<?> clazz, String fieldName) {
        return getField(clazz, fieldName).getGenericType();
    }

    /**
     * Gets field TypeToken
     * 获取字段TypeToken
     *
     * @param clazz     the class | 类
     * @param fieldName the field name | 字段名
     * @return the TypeToken | TypeToken
     */
    public static TypeToken<?> getFieldTypeToken(Class<?> clazz, String fieldName) {
        return TypeToken.of(getFieldGenericType(clazz, fieldName));
    }

    /**
     * Checks if field exists
     * 检查字段是否存在
     *
     * @param clazz     the class | 类
     * @param fieldName the field name | 字段名
     * @return true if exists | 如果存在返回true
     */
    public static boolean hasField(Class<?> clazz, String fieldName) {
        try {
            getField(clazz, fieldName);
            return true;
        } catch (OpenReflectException e) {
            return false;
        }
    }

    // ==================== Field Iteration | 字段遍历 ====================

    /**
     * Iterates all fields (including inherited)
     * 遍历所有字段（含继承）
     *
     * @param clazz  the class | 类
     * @param action the action | 操作
     */
    public static void forEach(Class<?> clazz, Consumer<Field> action) {
        getAllFields(clazz).forEach(action);
    }

    /**
     * Finds first matching field
     * 查找第一个匹配字段
     *
     * @param clazz     the class | 类
     * @param predicate the predicate | 谓词
     * @return Optional of field | 字段的Optional
     */
    public static Optional<Field> findFirst(Class<?> clazz, Predicate<Field> predicate) {
        return getAllFields(clazz).stream().filter(predicate).findFirst();
    }

    /**
     * Creates a field stream
     * 创建字段流
     *
     * @param clazz the class | 类
     * @return stream of fields | 字段流
     */
    public static Stream<Field> stream(Class<?> clazz) {
        return getAllFields(clazz).stream();
    }

    // ==================== Batch Operations | 批量操作 ====================

    /**
     * Reads all fields to Map
     * 批量读取为Map
     *
     * @param target the target object | 目标对象
     * @return map of field names to values | 字段名到值的映射
     */
    public static Map<String, Object> readFieldsToMap(Object target) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Field field : getAllFields(target.getClass())) {
            if (!Modifier.isStatic(field.getModifiers())) {
                result.put(field.getName(), readField(field, target));
            }
        }
        return result;
    }

    /**
     * Reads specific fields
     * 批量读取指定字段
     *
     * @param target     the target object | 目标对象
     * @param fieldNames the field names | 字段名
     * @return map of field names to values | 字段名到值的映射
     */
    public static Map<String, Object> readFields(Object target, String... fieldNames) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (String fieldName : fieldNames) {
            result.put(fieldName, readField(target, fieldName));
        }
        return result;
    }

    /**
     * Writes fields from Map
     * 从Map批量写入
     *
     * @param target the target object | 目标对象
     * @param values the values map | 值映射
     */
    public static void writeFieldsFromMap(Object target, Map<String, Object> values) {
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            if (hasField(target.getClass(), entry.getKey())) {
                writeField(target, entry.getKey(), entry.getValue());
            }
        }
    }
}
