package cloud.opencode.base.core.reflect;

import cloud.opencode.base.core.exception.OpenException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Method Utility Class - Method reflection operations
 * 方法工具类 - 方法反射操作
 *
 * <p>Provides utilities for method discovery, getter/setter detection, and invocation.</p>
 * <p>提供方法发现、Getter/Setter 检测和调用操作工具。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Get all/declared methods (with inheritance) - 获取全部/声明方法（含继承）</li>
 *   <li>Getter/Setter detection and property extraction - Getter/Setter 检测和属性提取</li>
 *   <li>Filter by annotation, return type, modifier - 按注解、返回类型、修饰符过滤</li>
 *   <li>Method invocation (instance/static) - 方法调用（实例/静态）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Get getter methods - 获取 Getter 方法
 * List<Method> getters = MethodUtil.getGetterMethods(User.class);
 *
 * // Check if getter - 检查是否为 Getter
 * boolean isGetter = MethodUtil.isGetter(method);
 *
 * // Get property name - 获取属性名
 * String prop = MethodUtil.getPropertyNameFromGetter(method);
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
 *   <li>Time complexity: O(n) where n = declared methods - O(n), n为声明的方法数</li>
 *   <li>Space complexity: O(1) per invocation - 每次调用 O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public final class MethodUtil {

    private MethodUtil() {
    }

    /**
     * Gets all methods (including superclass)
     * 获取所有方法（包括父类）
     */
    public static List<Method> getAllMethods(Class<?> clazz) {
        List<Method> methods = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            Collections.addAll(methods, current.getDeclaredMethods());
            current = current.getSuperclass();
        }
        return methods;
    }

    /**
     * Gets declared methods (excluding superclass)
     * 获取声明的方法（不包括父类）
     */
    public static List<Method> getDeclaredMethods(Class<?> clazz) {
        return Arrays.asList(clazz.getDeclaredMethods());
    }

    /**
     * Gets methods by name
     * 按名称获取方法
     */
    public static List<Method> getMethodsByName(Class<?> clazz, String name) {
        return getAllMethods(clazz).stream()
                .filter(m -> m.getName().equals(name))
                .toList();
    }

    /**
     * Gets a method (exact parameter type match)
     * 获取方法（精确匹配参数类型）
     */
    public static Optional<Method> getMethod(Class<?> clazz, String name, Class<?>... paramTypes) {
        return getAllMethods(clazz).stream()
                .filter(m -> m.getName().equals(name) && Arrays.equals(m.getParameterTypes(), paramTypes))
                .findFirst();
    }

    /**
     * Gets methods with the specified annotation
     * 获取带有指定注解的方法
     */
    public static List<Method> getMethodsWithAnnotation(Class<?> clazz, Class<? extends Annotation> annotation) {
        return getAllMethods(clazz).stream()
                .filter(m -> m.isAnnotationPresent(annotation))
                .toList();
    }

    /**
     * Gets methods with the specified return type
     * 获取指定返回类型的方法
     */
    public static List<Method> getMethodsByReturnType(Class<?> clazz, Class<?> returnType) {
        return getAllMethods(clazz).stream()
                .filter(m -> returnType.isAssignableFrom(m.getReturnType()))
                .toList();
    }

    /**
     * Gets getter methods
     * 获取 Getter 方法
     */
    public static List<Method> getGetterMethods(Class<?> clazz) {
        return getAllMethods(clazz).stream()
                .filter(MethodUtil::isGetter)
                .toList();
    }

    /**
     * Gets setter methods
     * 获取 Setter 方法
     */
    public static List<Method> getSetterMethods(Class<?> clazz) {
        return getAllMethods(clazz).stream()
                .filter(MethodUtil::isSetter)
                .toList();
    }

    /**
     * Gets static methods
     * 获取静态方法
     */
    public static List<Method> getStaticMethods(Class<?> clazz) {
        return getAllMethods(clazz).stream()
                .filter(m -> Modifier.isStatic(m.getModifiers()))
                .toList();
    }

    /**
     * Gets public methods
     * 获取公共方法
     */
    public static List<Method> getPublicMethods(Class<?> clazz) {
        return getAllMethods(clazz).stream()
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .toList();
    }

    /**
     * Checks if the method is a getter
     * 检查是否为 Getter 方法
     */
    public static boolean isGetter(Method method) {
        if (method.getParameterCount() != 0) return false;
        if (method.getReturnType() == void.class) return false;
        String name = method.getName();
        if (name.startsWith("get") && name.length() > 3) {
            return Character.isUpperCase(name.charAt(3));
        }
        if (name.startsWith("is") && name.length() > 2) {
            return (method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class)
                    && Character.isUpperCase(name.charAt(2));
        }
        return false;
    }

    /**
     * Checks if the method is a setter
     * 检查是否为 Setter 方法
     */
    public static boolean isSetter(Method method) {
        if (method.getParameterCount() != 1) return false;
        String name = method.getName();
        return name.startsWith("set") && name.length() > 3 && Character.isUpperCase(name.charAt(3));
    }

    /**
     * Gets the property name from a getter method name
     * 从 Getter 方法名获取属性名
     */
    public static String getPropertyNameFromGetter(Method method) {
        String name = method.getName();
        if (name.startsWith("get") && name.length() > 3) {
            return Character.toLowerCase(name.charAt(3)) + name.substring(4);
        }
        if (name.startsWith("is") && name.length() > 2) {
            return Character.toLowerCase(name.charAt(2)) + name.substring(3);
        }
        return null;
    }

    /**
     * Gets the property name from a setter method name
     * 从 Setter 方法名获取属性名
     */
    public static String getPropertyNameFromSetter(Method method) {
        String name = method.getName();
        if (name.startsWith("set") && name.length() > 3) {
            return Character.toLowerCase(name.charAt(3)) + name.substring(4);
        }
        return null;
    }

    /**
     * Gets the generic return type of a method
     * 获取方法的泛型返回类型
     */
    public static Type getGenericReturnType(Method method) {
        return method.getGenericReturnType();
    }

    /**
     * Gets the generic parameter types of a method
     * 获取方法的泛型参数类型
     */
    public static Type[] getGenericParameterTypes(Method method) {
        return method.getGenericParameterTypes();
    }

    /**
     * Invokes a method
     * 调用方法
     */
    @SuppressWarnings("unchecked")
    public static <T> T invoke(Object obj, Method method, Object... args) {
        try {
            method.setAccessible(true);
            return (T) method.invoke(obj, args);
        } catch (Exception e) {
            throw new OpenException("Failed to invoke method: " + method.getName(), e);
        }
    }

    /**
     * Invokes a static method
     * 调用静态方法
     */
    @SuppressWarnings("unchecked")
    public static <T> T invokeStatic(Method method, Object... args) {
        try {
            method.setAccessible(true);
            return (T) method.invoke(null, args);
        } catch (Exception e) {
            throw new OpenException("Failed to invoke static method: " + method.getName(), e);
        }
    }

    /**
     * Checks if the method is abstract
     * 检查方法是否为 abstract
     */
    public static boolean isAbstract(Method method) {
        return Modifier.isAbstract(method.getModifiers());
    }

    /**
     * Checks if the method is synchronized
     * 检查方法是否为 synchronized
     */
    public static boolean isSynchronized(Method method) {
        return Modifier.isSynchronized(method.getModifiers());
    }

    /**
     * Checks if the method is native
     * 检查方法是否为 native
     */
    public static boolean isNative(Method method) {
        return Modifier.isNative(method.getModifiers());
    }

    /**
     * Checks if the method is a default interface method
     * 检查方法是否为 default 接口方法
     */
    public static boolean isDefault(Method method) {
        return method.isDefault();
    }
}
