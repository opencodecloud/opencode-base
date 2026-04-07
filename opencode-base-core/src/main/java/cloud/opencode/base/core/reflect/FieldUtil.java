package cloud.opencode.base.core.reflect;

import cloud.opencode.base.core.exception.OpenException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Field Utility Class - Field reflection operations
 * 字段工具类 - 字段反射操作
 *
 * <p>Provides utilities for field discovery, filtering, and access operations.</p>
 * <p>提供字段发现、过滤和访问操作工具。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Get all/declared fields (with inheritance) - 获取全部/声明字段（含继承）</li>
 *   <li>Filter by annotation, type, modifier - 按注解、类型、修饰符过滤</li>
 *   <li>Field value get/set operations - 字段值读写操作</li>
 *   <li>Modifier checks (static, final, transient) - 修饰符检查</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Get all fields - 获取所有字段
 * List<Field> fields = FieldUtil.getAllFields(User.class);
 *
 * // Get by annotation - 按注解获取
 * List<Field> annotated = FieldUtil.getFieldsWithAnnotation(User.class, Column.class);
 *
 * // Get/Set value - 读写值
 * Object value = FieldUtil.getValue(user, field);
 * FieldUtil.setValue(user, field, newValue);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是 (无状态)</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 *
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) where n = declared fields - O(n), n为声明的字段数</li>
 *   <li>Space complexity: O(1) per access - 每次访问 O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public final class FieldUtil {

    private FieldUtil() {
    }

    /**
     * Gets all fields (including superclass)
     * 获取所有字段（包括父类）
     */
    public static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            Collections.addAll(fields, current.getDeclaredFields());
            current = current.getSuperclass();
        }
        return fields;
    }

    /**
     * Gets declared fields (excluding superclass)
     * 获取声明的字段（不包括父类）
     */
    public static List<Field> getDeclaredFields(Class<?> clazz) {
        return Arrays.asList(clazz.getDeclaredFields());
    }

    /**
     * Gets a field by name
     * 按名称获取字段
     */
    public static Optional<Field> getFieldByName(Class<?> clazz, String name) {
        return getAllFields(clazz).stream()
                .filter(f -> f.getName().equals(name))
                .findFirst();
    }

    /**
     * Gets fields with the specified annotation
     * 获取带有指定注解的字段
     */
    public static List<Field> getFieldsWithAnnotation(Class<?> clazz, Class<? extends Annotation> annotation) {
        return getAllFields(clazz).stream()
                .filter(f -> f.isAnnotationPresent(annotation))
                .toList();
    }

    /**
     * Gets fields of the specified type
     * 获取指定类型的字段
     */
    public static List<Field> getFieldsByType(Class<?> clazz, Class<?> fieldType) {
        return getAllFields(clazz).stream()
                .filter(f -> fieldType.isAssignableFrom(f.getType()))
                .toList();
    }

    /**
     * Gets static fields
     * 获取静态字段
     */
    public static List<Field> getStaticFields(Class<?> clazz) {
        return getAllFields(clazz).stream()
                .filter(f -> Modifier.isStatic(f.getModifiers()))
                .toList();
    }

    /**
     * Gets instance fields (non-static)
     * 获取实例字段（非静态）
     */
    public static List<Field> getInstanceFields(Class<?> clazz) {
        return getAllFields(clazz).stream()
                .filter(f -> !Modifier.isStatic(f.getModifiers()))
                .toList();
    }

    /**
     * Gets public fields
     * 获取公共字段
     */
    public static List<Field> getPublicFields(Class<?> clazz) {
        return getAllFields(clazz).stream()
                .filter(f -> Modifier.isPublic(f.getModifiers()))
                .toList();
    }

    /**
     * Gets the generic type of a field
     * 获取字段的泛型类型
     */
    public static Type getGenericType(Field field) {
        return field.getGenericType();
    }

    /**
     * Gets the field value
     * 获取字段值
     */
    @SuppressWarnings("unchecked")
    public static <T> T getValue(Object obj, Field field) {
        try {
            field.setAccessible(true);
            return (T) field.get(obj);
        } catch (IllegalAccessException e) {
            throw new OpenException("Failed to get field value: " + field.getName(), e);
        }
    }

    /**
     * Sets the field value
     * 设置字段值
     */
    public static void setValue(Object obj, Field field, Object value) {
        if (java.lang.reflect.Modifier.isFinal(field.getModifiers())) {
            throw new OpenException("Cannot set final field: " + field.getDeclaringClass().getName()
                    + "." + field.getName());
        }
        try {
            field.setAccessible(true);
            field.set(obj, value);
        } catch (IllegalAccessException e) {
            throw new OpenException("Failed to set field value: " + field.getName(), e);
        }
    }

    /**
     * Checks if the field is final
     * 检查字段是否为 final
     */
    public static boolean isFinal(Field field) {
        return Modifier.isFinal(field.getModifiers());
    }

    /**
     * Checks if the field is static
     * 检查字段是否为 static
     */
    public static boolean isStatic(Field field) {
        return Modifier.isStatic(field.getModifiers());
    }

    /**
     * Checks if the field is transient
     * 检查字段是否为 transient
     */
    public static boolean isTransient(Field field) {
        return Modifier.isTransient(field.getModifiers());
    }

    /**
     * Checks if the field is volatile
     * 检查字段是否为 volatile
     */
    public static boolean isVolatile(Field field) {
        return Modifier.isVolatile(field.getModifiers());
    }

    /**
     * Gets the list of field names
     * 获取字段名列表
     */
    public static List<String> getFieldNames(Class<?> clazz) {
        return getAllFields(clazz).stream()
                .map(Field::getName)
                .toList();
    }

    /**
     * Creates a field name to field mapping
     * 创建字段名到字段的映射
     */
    public static Map<String, Field> getFieldMap(Class<?> clazz) {
        Map<String, Field> map = new LinkedHashMap<>();
        for (Field field : getAllFields(clazz)) {
            map.putIfAbsent(field.getName(), field);
        }
        return map;
    }
}
