package cloud.opencode.base.core.reflect;

import cloud.opencode.base.core.exception.OpenException;

import java.lang.reflect.*;
import java.util.*;

/**
 * Reflection Utility Class - Core reflection operations with caching
 * 反射工具类 - 核心反射操作，支持缓存优化
 *
 * <p>Provides cached reflection operations for fields, methods, and constructors.</p>
 * <p>提供字段、方法和构造器的缓存反射操作。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Instance creation (with/without args) - 实例创建（带参/无参）</li>
 *   <li>Method invocation (instance/static) - 方法调用（实例/静态）</li>
 *   <li>Field access (get/set, instance/static) - 字段访问（读写，实例/静态）</li>
 *   <li>ClassValue-based caching for performance - 基于 ClassValue 的高性能缓存</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create instance - 创建实例
 * User user = ReflectUtil.newInstance(User.class);
 *
 * // Invoke method - 调用方法
 * String name = ReflectUtil.invoke(user, "getName");
 *
 * // Get/Set field - 获取/设置字段
 * Object value = ReflectUtil.getFieldValue(user, "name");
 * ReflectUtil.setFieldValue(user, "name", "Leon");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (ClassValue caching) - 线程安全: 是 (ClassValue 缓存)</li>
 *   <li>Null-safe: No (throws exceptions) - 空值安全: 否 (抛出异常)</li>
 * </ul>
 *
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) for class hierarchy traversal - 类层次遍历 O(n)</li>
 *   <li>Space complexity: O(1) per cached lookup - 每次缓存查找 O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public final class ReflectUtil {

    private static final ClassValue<Field[]> FIELD_CACHE = new ClassValue<>() {
        @Override
        protected Field[] computeValue(Class<?> type) {
            List<Field> fields = new ArrayList<>();
            Class<?> current = type;
            while (current != null && current != Object.class) {
                Collections.addAll(fields, current.getDeclaredFields());
                current = current.getSuperclass();
            }
            return fields.toArray(new Field[0]);
        }
    };

    private static final ClassValue<Method[]> METHOD_CACHE = new ClassValue<>() {
        @Override
        protected Method[] computeValue(Class<?> type) {
            List<Method> methods = new ArrayList<>();
            Class<?> current = type;
            while (current != null && current != Object.class) {
                Collections.addAll(methods, current.getDeclaredMethods());
                current = current.getSuperclass();
            }
            return methods.toArray(new Method[0]);
        }
    };

    private static final ClassValue<Constructor<?>[]> CONSTRUCTOR_CACHE = new ClassValue<>() {
        @Override
        protected Constructor<?>[] computeValue(Class<?> type) {
            return type.getDeclaredConstructors();
        }
    };

    private ReflectUtil() {
    }

    // ==================== 实例创建 ====================

    /**
     * Creates an instance (no-arg constructor)
     * 创建实例（无参构造）
     */
    public static <T> T newInstance(Class<T> clazz) {
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            throw new OpenException("core", "REFLECT-001", "Failed to create instance: " + clazz.getName(), e);
        }
    }

    /**
     * Creates an instance (with constructor arguments)
     * 创建实例（带参构造）
     */
    @SuppressWarnings("unchecked")
    public static <T> T newInstance(Class<T> clazz, Object... args) {
        try {
            Class<?>[] paramTypes = new Class<?>[args.length];
            for (int i = 0; i < args.length; i++) {
                paramTypes[i] = args[i] != null ? args[i].getClass() : Object.class;
            }
            Constructor<?> constructor = findMatchingConstructor(clazz, paramTypes);
            if (constructor == null) {
                throw new NoSuchMethodException("No matching constructor found");
            }
            constructor.setAccessible(true);
            return (T) constructor.newInstance(args);
        } catch (Exception e) {
            throw new OpenException("core", "REFLECT-001", "Failed to create instance: " + clazz.getName(), e);
        }
    }

    // ==================== 方法调用 ====================

    /**
     * Invokes a method
     * 调用方法
     */
    @SuppressWarnings("unchecked")
    public static <T> T invoke(Object obj, String methodName, Object... args) {
        try {
            Class<?> clazz = obj.getClass();
            Class<?>[] paramTypes = new Class<?>[args.length];
            for (int i = 0; i < args.length; i++) {
                paramTypes[i] = args[i] != null ? args[i].getClass() : Object.class;
            }
            Method method = findMatchingMethod(clazz, methodName, paramTypes);
            if (method == null) {
                throw new NoSuchMethodException("Method not found: " + methodName);
            }
            method.setAccessible(true);
            return (T) method.invoke(obj, args);
        } catch (Exception e) {
            throw new OpenException("core", "REFLECT-002", "Failed to invoke method: " + methodName, e);
        }
    }

    /**
     * Invokes a static method
     * 调用静态方法
     */
    @SuppressWarnings("unchecked")
    public static <T> T invokeStatic(Class<?> clazz, String methodName, Object... args) {
        try {
            Class<?>[] paramTypes = new Class<?>[args.length];
            for (int i = 0; i < args.length; i++) {
                paramTypes[i] = args[i] != null ? args[i].getClass() : Object.class;
            }
            Method method = findMatchingMethod(clazz, methodName, paramTypes);
            if (method == null) {
                throw new NoSuchMethodException("Static method not found: " + methodName);
            }
            method.setAccessible(true);
            return (T) method.invoke(null, args);
        } catch (Exception e) {
            throw new OpenException("core", "REFLECT-002", "Failed to invoke static method: " + methodName, e);
        }
    }

    // ==================== 字段操作 ====================

    /**
     * Gets the field value
     * 获取字段值
     */
    @SuppressWarnings("unchecked")
    public static <T> T getFieldValue(Object obj, String fieldName) {
        try {
            Field field = getField(obj.getClass(), fieldName);
            if (field == null) {
                throw new NoSuchFieldException("Field not found: " + fieldName);
            }
            field.setAccessible(true);
            return (T) field.get(obj);
        } catch (Exception e) {
            throw new OpenException("core", "REFLECT-003", "Failed to get field value: " + fieldName, e);
        }
    }

    /**
     * Sets the field value
     * 设置字段值
     */
    public static void setFieldValue(Object obj, String fieldName, Object value) {
        try {
            Field field = getField(obj.getClass(), fieldName);
            if (field == null) {
                throw new NoSuchFieldException("Field not found: " + fieldName);
            }
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            throw new OpenException("core", "REFLECT-003", "Failed to set field value: " + fieldName, e);
        }
    }

    /**
     * Gets the static field value
     * 获取静态字段值
     */
    @SuppressWarnings("unchecked")
    public static <T> T getStaticFieldValue(Class<?> clazz, String fieldName) {
        try {
            Field field = getField(clazz, fieldName);
            if (field == null) {
                throw new NoSuchFieldException("Field not found: " + fieldName);
            }
            field.setAccessible(true);
            return (T) field.get(null);
        } catch (Exception e) {
            throw new OpenException("core", "REFLECT-003", "Failed to get static field value: " + fieldName, e);
        }
    }

    /**
     * Sets the static field value
     * 设置静态字段值
     */
    public static void setStaticFieldValue(Class<?> clazz, String fieldName, Object value) {
        try {
            Field field = getField(clazz, fieldName);
            if (field == null) {
                throw new NoSuchFieldException("Field not found: " + fieldName);
            }
            field.setAccessible(true);
            field.set(null, value);
        } catch (Exception e) {
            throw new OpenException("core", "REFLECT-003", "Failed to set static field value: " + fieldName, e);
        }
    }

    // ==================== 获取成员 ====================

    /**
     * Gets all fields (including superclass)
     * 获取所有字段（包括父类）
     */
    public static Field[] getFields(Class<?> clazz) {
        return FIELD_CACHE.get(clazz).clone();
    }

    /**
     * Gets the specified field
     * 获取指定字段
     */
    public static Field getField(Class<?> clazz, String fieldName) {
        for (Field field : getFields(clazz)) {
            if (field.getName().equals(fieldName)) {
                return field;
            }
        }
        return null;
    }

    /**
     * Gets all methods (including superclass)
     * 获取所有方法（包括父类）
     */
    public static Method[] getMethods(Class<?> clazz) {
        return METHOD_CACHE.get(clazz).clone();
    }

    /**
     * Gets the specified method
     * 获取指定方法
     */
    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        try {
            return clazz.getMethod(methodName, paramTypes);
        } catch (NoSuchMethodException e) {
            for (Method method : getMethods(clazz)) {
                if (method.getName().equals(methodName) && Arrays.equals(method.getParameterTypes(), paramTypes)) {
                    return method;
                }
            }
            return null;
        }
    }

    /**
     * Gets all constructors
     * 获取所有构造器
     */
    public static Constructor<?>[] getConstructors(Class<?> clazz) {
        return CONSTRUCTOR_CACHE.get(clazz).clone();
    }

    /**
     * Gets the default constructor
     * 获取默认构造器
     */
    @SuppressWarnings("unchecked")
    public static <T> Constructor<T> getDefaultConstructor(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    // ==================== 辅助方法 ====================

    private static Constructor<?> findMatchingConstructor(Class<?> clazz, Class<?>[] paramTypes) {
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            if (isParameterMatch(constructor.getParameterTypes(), paramTypes)) {
                return constructor;
            }
        }
        return null;
    }

    private static Method findMatchingMethod(Class<?> clazz, String methodName, Class<?>[] paramTypes) {
        for (Method method : getMethods(clazz)) {
            if (method.getName().equals(methodName) && isParameterMatch(method.getParameterTypes(), paramTypes)) {
                return method;
            }
        }
        return null;
    }

    private static boolean isParameterMatch(Class<?>[] declaredTypes, Class<?>[] actualTypes) {
        if (declaredTypes.length != actualTypes.length) return false;
        for (int i = 0; i < declaredTypes.length; i++) {
            if (!isAssignable(declaredTypes[i], actualTypes[i])) {
                return false;
            }
        }
        return true;
    }

    private static boolean isAssignable(Class<?> target, Class<?> source) {
        if (target.isAssignableFrom(source)) return true;
        if (target.isPrimitive() || source.isPrimitive()) {
            return primitiveAssignable(target, source);
        }
        return false;
    }

    private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPER = Map.of(
            boolean.class, Boolean.class,
            byte.class, Byte.class,
            char.class, Character.class,
            short.class, Short.class,
            int.class, Integer.class,
            long.class, Long.class,
            float.class, Float.class,
            double.class, Double.class
    );

    private static boolean primitiveAssignable(Class<?> target, Class<?> source) {
        Class<?> targetWrapper = PRIMITIVE_TO_WRAPPER.getOrDefault(target, target);
        Class<?> sourceWrapper = PRIMITIVE_TO_WRAPPER.getOrDefault(source, source);
        return targetWrapper.isAssignableFrom(sourceWrapper);
    }
}
