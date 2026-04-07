package cloud.opencode.base.reflect;

import cloud.opencode.base.reflect.exception.OpenReflectException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Collections;
import java.util.function.Predicate;

/**
 * Field Utility Class
 * 字段工具类
 *
 * <p>Provides low-level field operation utilities with caching.</p>
 * <p>提供带缓存的底层字段操作工具。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Field discovery with caching - 带缓存的字段发现</li>
 *   <li>Inherited field resolution - 继承字段解析</li>
 *   <li>Field filtering by type, modifier, annotation - 按类型、修饰符、注解过滤字段</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * List<Field> fields = FieldUtil.getAllFields(User.class);
 * Field field = FieldUtil.getField(User.class, "name");
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
 *   <li>Time complexity: O(1) for cached lookups; O(f) for first access where f is the number of fields (including inherited) - 时间复杂度: 缓存命中时 O(1)；首次访问为 O(f)，f为字段数量（含继承）</li>
 *   <li>Space complexity: O(f) for the cached fields per class - 空间复杂度: O(f)，每类缓存字段</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
public final class FieldUtil {

    private static final Map<Class<?>, List<Field>> FIELD_CACHE =
            Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<FieldKey, Field> SINGLE_FIELD_CACHE = new ConcurrentHashMap<>();

    /**
     * Checks that the class is not a JDK platform type
     * 检查类是否为JDK平台类型
     */
    private static void checkNotPlatformType(Class<?> clazz) {
        String pkg = clazz.getPackageName();
        if (pkg.startsWith("java.") || pkg.startsWith("javax.") || pkg.startsWith("sun.")
                || pkg.startsWith("jdk.") || pkg.startsWith("com.sun.")) {
            throw new OpenReflectException(clazz, "<field>", "setAccessible",
                    "setAccessible denied for platform type: " + clazz.getName());
        }
    }

    private FieldUtil() {
    }

    // ==================== Field Discovery | 字段发现 ====================

    /**
     * Gets all declared fields (no inheritance)
     * 获取所有声明的字段（不含继承）
     *
     * @param clazz the class | 类
     * @return array of fields | 字段数组
     */
    public static Field[] getDeclaredFields(Class<?> clazz) {
        return clazz.getDeclaredFields();
    }

    /**
     * Gets all fields including inherited (cached)
     * 获取所有字段包含继承（缓存）
     *
     * @param clazz the class | 类
     * @return list of fields | 字段列表
     */
    public static List<Field> getAllFields(Class<?> clazz) {
        return FIELD_CACHE.computeIfAbsent(clazz, FieldUtil::collectAllFields);
    }

    private static List<Field> collectAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                fields.add(field);
            }
            current = current.getSuperclass();
        }
        return Collections.unmodifiableList(fields);
    }

    /**
     * Gets field by name (cached)
     * 按名称获取字段（缓存）
     *
     * @param clazz     the class | 类
     * @param fieldName the field name | 字段名
     * @return the field or null | 字段或null
     */
    public static Field getField(Class<?> clazz, String fieldName) {
        FieldKey key = new FieldKey(clazz, fieldName);
        return SINGLE_FIELD_CACHE.computeIfAbsent(key, k -> findField(k.clazz(), k.fieldName()));
    }

    private static Field findField(Class<?> clazz, String fieldName) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    /**
     * Gets field or throws exception
     * 获取字段或抛出异常
     *
     * @param clazz     the class | 类
     * @param fieldName the field name | 字段名
     * @return the field | 字段
     * @throws OpenReflectException if not found | 如果未找到
     */
    public static Field getFieldOrThrow(Class<?> clazz, String fieldName) {
        Field field = getField(clazz, fieldName);
        if (field == null) {
            throw OpenReflectException.fieldNotFound(clazz, fieldName);
        }
        return field;
    }

    // ==================== Field Filtering | 字段过滤 ====================

    /**
     * Gets fields matching predicate
     * 获取匹配条件的字段
     *
     * @param clazz     the class | 类
     * @param predicate the predicate | 谓词
     * @return list of matching fields | 匹配的字段列表
     */
    public static List<Field> getFields(Class<?> clazz, Predicate<Field> predicate) {
        return getAllFields(clazz).stream().filter(predicate).toList();
    }

    /**
     * Gets fields with annotation
     * 获取带注解的字段
     *
     * @param clazz           the class | 类
     * @param annotationClass the annotation class | 注解类
     * @return list of fields | 字段列表
     */
    public static List<Field> getFieldsWithAnnotation(Class<?> clazz, Class<? extends Annotation> annotationClass) {
        return getFields(clazz, f -> f.isAnnotationPresent(annotationClass));
    }

    /**
     * Gets fields of specific type
     * 获取特定类型的字段
     *
     * @param clazz     the class | 类
     * @param fieldType the field type | 字段类型
     * @return list of fields | 字段列表
     */
    public static List<Field> getFieldsOfType(Class<?> clazz, Class<?> fieldType) {
        return getFields(clazz, f -> fieldType.isAssignableFrom(f.getType()));
    }

    /**
     * Gets non-static fields
     * 获取非静态字段
     *
     * @param clazz the class | 类
     * @return list of fields | 字段列表
     */
    public static List<Field> getInstanceFields(Class<?> clazz) {
        return getFields(clazz, f -> !Modifier.isStatic(f.getModifiers()));
    }

    /**
     * Gets static fields
     * 获取静态字段
     *
     * @param clazz the class | 类
     * @return list of fields | 字段列表
     */
    public static List<Field> getStaticFields(Class<?> clazz) {
        return getFields(clazz, f -> Modifier.isStatic(f.getModifiers()));
    }

    // ==================== Field Access | 字段访问 ====================

    /**
     * Gets field value by name, with unified Record/Class support.
     * 按名称获取字段值，统一支持 Record 和普通类。
     *
     * <p>For records, invokes the accessor method. For classes, uses field reflection.</p>
     * <p>对于 Record，调用访问器方法。对于普通类，使用字段反射。</p>
     *
     * @param object    the target object | 目标对象
     * @param fieldName the field name | 字段名
     * @return the field value | 字段值
     * @throws OpenReflectException if field not found or access failed | 如果字段未找到或访问失败
     */
    public static Object getFieldValue(Object object, String fieldName) {
        Objects.requireNonNull(object, "object must not be null");
        Objects.requireNonNull(fieldName, "fieldName must not be null");

        Class<?> clazz = object.getClass();

        // Handle records
        if (clazz.isRecord()) {
            for (RecordComponent component : clazz.getRecordComponents()) {
                if (component.getName().equals(fieldName)) {
                    try {
                        var accessor = component.getAccessor();
                        ReflectUtil.setAccessible(accessor, object);
                        return accessor.invoke(object);
                    } catch (Exception e) {
                        throw OpenReflectException.fieldAccessFailed(clazz, fieldName, e);
                    }
                }
            }
            throw OpenReflectException.fieldNotFound(clazz, fieldName);
        }

        // Handle regular classes
        Field field = getFieldOrThrow(clazz, fieldName);
        return getValue(field, object);
    }

    /**
     * Gets field value
     * 获取字段值
     *
     * @param field  the field | 字段
     * @param target the target object | 目标对象
     * @return the value | 值
     */
    public static Object getValue(Field field, Object target) {
        try {
            checkNotPlatformType(field.getDeclaringClass());
            ReflectUtil.setAccessible(field, target);
            return field.get(target);
        } catch (IllegalAccessException e) {
            throw OpenReflectException.fieldAccessFailed(field.getDeclaringClass(), field.getName(), e);
        }
    }

    /**
     * Gets field value with type cast
     * 获取字段值并转型
     *
     * @param field  the field | 字段
     * @param target the target object | 目标对象
     * @param type   the expected type | 期望类型
     * @param <T>    the type | 类型
     * @return the value | 值
     */
    @SuppressWarnings("unchecked")
    public static <T> T getValue(Field field, Object target, Class<T> type) {
        Object value = getValue(field, target);
        if (value == null) {
            return null;
        }
        if (!type.isInstance(value)) {
            throw OpenReflectException.typeCastFailed(type, value);
        }
        return (T) value;
    }

    /**
     * Sets field value
     * 设置字段值
     *
     * @param field  the field | 字段
     * @param target the target object | 目标对象
     * @param value  the value | 值
     */
    public static void setValue(Field field, Object target, Object value) {
        try {
            checkNotPlatformType(field.getDeclaringClass());
            ReflectUtil.setAccessible(field, target);
            field.set(target, value);
        } catch (IllegalAccessException e) {
            throw OpenReflectException.fieldAccessFailed(field.getDeclaringClass(), field.getName(), e);
        }
    }

    /**
     * Gets static field value
     * 获取静态字段值
     *
     * @param field the field | 字段
     * @return the value | 值
     */
    public static Object getStaticValue(Field field) {
        return getValue(field, null);
    }

    /**
     * Sets static field value
     * 设置静态字段值
     *
     * @param field the field | 字段
     * @param value the value | 值
     */
    public static void setStaticValue(Field field, Object value) {
        setValue(field, null, value);
    }

    // ==================== Field Information | 字段信息 ====================

    /**
     * Gets field type
     * 获取字段类型
     *
     * @param field the field | 字段
     * @return the type | 类型
     */
    public static Class<?> getType(Field field) {
        return field.getType();
    }

    /**
     * Gets field generic type
     * 获取字段泛型类型
     *
     * @param field the field | 字段
     * @return the generic type | 泛型类型
     */
    public static Type getGenericType(Field field) {
        return field.getGenericType();
    }

    /**
     * Checks if field is static
     * 检查字段是否为静态
     *
     * @param field the field | 字段
     * @return true if static | 如果是静态返回true
     */
    public static boolean isStatic(Field field) {
        return Modifier.isStatic(field.getModifiers());
    }

    /**
     * Checks if field is final
     * 检查字段是否为final
     *
     * @param field the field | 字段
     * @return true if final | 如果是final返回true
     */
    public static boolean isFinal(Field field) {
        return Modifier.isFinal(field.getModifiers());
    }

    /**
     * Checks if field is transient
     * 检查字段是否为transient
     *
     * @param field the field | 字段
     * @return true if transient | 如果是transient返回true
     */
    public static boolean isTransient(Field field) {
        return Modifier.isTransient(field.getModifiers());
    }

    /**
     * Checks if field is volatile
     * 检查字段是否为volatile
     *
     * @param field the field | 字段
     * @return true if volatile | 如果是volatile返回true
     */
    public static boolean isVolatile(Field field) {
        return Modifier.isVolatile(field.getModifiers());
    }

    // ==================== Cache Management | 缓存管理 ====================

    /**
     * Clears field cache
     * 清除字段缓存
     */
    public static void clearCache() {
        FIELD_CACHE.clear();
        SINGLE_FIELD_CACHE.clear();
    }

    /**
     * Clears cache for specific class
     * 清除特定类的缓存
     *
     * @param clazz the class | 类
     */
    public static void clearCache(Class<?> clazz) {
        FIELD_CACHE.remove(clazz);
        SINGLE_FIELD_CACHE.keySet().removeIf(key -> key.clazz() == clazz);
    }

    // ==================== Internal | 内部 ====================

    private record FieldKey(Class<?> clazz, String fieldName) {
    }
}
