package cloud.opencode.base.core.reflect;

import cloud.opencode.base.core.exception.OpenException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Constructor Utility Class - Constructor reflection operations
 * 构造器工具类 - 构造器反射操作
 *
 * <p>Provides utilities for constructor discovery, filtering, and instance creation.</p>
 * <p>提供构造器发现、过滤和实例创建操作工具。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Get all/public/default constructors - 获取全部/公共/默认构造器</li>
 *   <li>Get by parameter types or annotation - 按参数类型或注解获取</li>
 *   <li>Get min/max args constructors - 获取最少/最多参数构造器</li>
 *   <li>Instance creation with type-safe API - 类型安全的实例创建</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Get default constructor - 获取默认构造器
 * Optional<Constructor<User>> ctor = ConstructorUtil.getDefaultConstructor(User.class);
 *
 * // Create instance - 创建实例
 * User user = ConstructorUtil.newInstance(User.class);
 *
 * // Get constructor by param types - 按参数类型获取
 * Optional<Constructor<User>> ctor = ConstructorUtil.getConstructor(User.class, String.class);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是 (无状态)</li>
 *   <li>Null-safe: Returns Optional - 空值安全: 返回 Optional</li>
 * </ul>
 *
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) where n = declared constructors - O(n), n为声明的构造器数</li>
 *   <li>Space complexity: O(1) per invocation - 每次调用 O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public final class ConstructorUtil {

    private ConstructorUtil() {
    }

    /**
     * Gets all constructors
     * 获取所有构造器
     */
    public static <T> List<Constructor<T>> getAllConstructors(Class<T> clazz) {
        @SuppressWarnings("unchecked")
        Constructor<T>[] constructors = (Constructor<T>[]) clazz.getDeclaredConstructors();
        return Arrays.asList(constructors);
    }

    /**
     * Gets public constructors
     * 获取公共构造器
     */
    public static <T> List<Constructor<T>> getPublicConstructors(Class<T> clazz) {
        @SuppressWarnings("unchecked")
        Constructor<T>[] constructors = (Constructor<T>[]) clazz.getConstructors();
        return Arrays.asList(constructors);
    }

    /**
     * Gets the default constructor (no-arg)
     * 获取默认构造器（无参）
     */
    public static <T> Optional<Constructor<T>> getDefaultConstructor(Class<T> clazz) {
        try {
            return Optional.of(clazz.getDeclaredConstructor());
        } catch (NoSuchMethodException e) {
            return Optional.empty();
        }
    }

    /**
     * Gets the constructor with specified parameter types
     * 获取指定参数类型的构造器
     */
    public static <T> Optional<Constructor<T>> getConstructor(Class<T> clazz, Class<?>... paramTypes) {
        try {
            return Optional.of(clazz.getDeclaredConstructor(paramTypes));
        } catch (NoSuchMethodException e) {
            return Optional.empty();
        }
    }

    /**
     * Gets constructors with the specified annotation
     * 获取带有指定注解的构造器
     */
    public static <T> List<Constructor<T>> getConstructorsWithAnnotation(Class<T> clazz, Class<? extends Annotation> annotation) {
        return getAllConstructors(clazz).stream()
                .filter(c -> c.isAnnotationPresent(annotation))
                .toList();
    }

    /**
     * Gets the constructor with the fewest parameters
     * 获取参数数量最少的构造器
     */
    public static <T> Optional<Constructor<T>> getMinArgsConstructor(Class<T> clazz) {
        return getAllConstructors(clazz).stream()
                .min(Comparator.comparingInt(Constructor::getParameterCount));
    }

    /**
     * Gets the constructor with the most parameters
     * 获取参数数量最多的构造器
     */
    public static <T> Optional<Constructor<T>> getMaxArgsConstructor(Class<T> clazz) {
        return getAllConstructors(clazz).stream()
                .max(Comparator.comparingInt(Constructor::getParameterCount));
    }

    /**
     * Checks if a default constructor exists
     * 检查是否有默认构造器
     */
    public static boolean hasDefaultConstructor(Class<?> clazz) {
        return getDefaultConstructor(clazz).isPresent();
    }

    /**
     * Checks if the constructor is public
     * 检查构造器是否为 public
     */
    public static boolean isPublic(Constructor<?> constructor) {
        return Modifier.isPublic(constructor.getModifiers());
    }

    /**
     * Checks if the constructor is private
     * 检查构造器是否为 private
     */
    public static boolean isPrivate(Constructor<?> constructor) {
        return Modifier.isPrivate(constructor.getModifiers());
    }

    /**
     * Creates an instance
     * 创建实例
     */
    public static <T> T newInstance(Constructor<T> constructor, Object... args) {
        try {
            constructor.setAccessible(true);
            return constructor.newInstance(args);
        } catch (Exception e) {
            throw new OpenException("Failed to create instance", e);
        }
    }

    /**
     * Creates an instance using the default constructor
     * 使用默认构造器创建实例
     */
    public static <T> T newInstance(Class<T> clazz) {
        Optional<Constructor<T>> constructor = getDefaultConstructor(clazz);
        if (constructor.isEmpty()) {
            throw new OpenException("No default constructor found for: " + clazz.getName());
        }
        return newInstance(constructor.get());
    }

    /**
     * Gets constructor parameter names (requires -parameters compile flag)
     * 获取构造器参数名称（需要编译时 -parameters 参数）
     */
    public static String[] getParameterNames(Constructor<?> constructor) {
        return Arrays.stream(constructor.getParameters())
                .map(java.lang.reflect.Parameter::getName)
                .toArray(String[]::new);
    }
}
