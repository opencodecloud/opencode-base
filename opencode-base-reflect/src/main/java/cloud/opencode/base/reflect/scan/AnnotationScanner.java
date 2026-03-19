package cloud.opencode.base.reflect.scan;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

/**
 * Annotation Scanner
 * 注解扫描器
 *
 * <p>Scans for annotated elements (classes, methods, fields, etc.).</p>
 * <p>扫描被注解的元素（类、方法、字段等）。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Annotated class scanning - 被注解类扫描</li>
 *   <li>Annotated method scanning - 被注解方法扫描</li>
 *   <li>Annotated field scanning - 被注解字段扫描</li>
 *   <li>Meta-annotation support - 元注解支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * List<Class<?>> controllers = AnnotationScanner.from(classScanner)
 *     .includeMetaAnnotations()
 *     .findClassesWithAnnotation(Controller.class);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (mutable configuration state) - 线程安全: 否（可变配置状态）</li>
 *   <li>Null-safe: No (caller must ensure non-null arguments) - 空值安全: 否（调用方须确保非空参数）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
public final class AnnotationScanner {

    private final ClassScanner classScanner;
    private boolean includeMetaAnnotations;

    private AnnotationScanner(ClassScanner classScanner) {
        this.classScanner = classScanner;
        this.includeMetaAnnotations = false;
    }

    /**
     * Creates an AnnotationScanner from ClassScanner
     * 从ClassScanner创建AnnotationScanner
     *
     * @param classScanner the class scanner | 类扫描器
     * @return the annotation scanner | 注解扫描器
     */
    public static AnnotationScanner from(ClassScanner classScanner) {
        return new AnnotationScanner(classScanner);
    }

    /**
     * Creates an AnnotationScanner from ClassLoader
     * 从ClassLoader创建AnnotationScanner
     *
     * @param classLoader the class loader | 类加载器
     * @return the annotation scanner | 注解扫描器
     */
    public static AnnotationScanner from(ClassLoader classLoader) {
        return new AnnotationScanner(ClassScanner.from(classLoader));
    }

    /**
     * Creates an AnnotationScanner using context ClassLoader
     * 使用上下文ClassLoader创建AnnotationScanner
     *
     * @return the annotation scanner | 注解扫描器
     */
    public static AnnotationScanner create() {
        return from(ClassScanner.create());
    }

    /**
     * Sets the base package to scan
     * 设置要扫描的基础包
     *
     * @param packageName the package name | 包名
     * @return this scanner | 此扫描器
     */
    public AnnotationScanner inPackage(String packageName) {
        classScanner.inPackage(packageName);
        return this;
    }

    /**
     * Sets whether to include meta-annotations
     * 设置是否包含元注解
     *
     * @param include whether to include | 是否包含
     * @return this scanner | 此扫描器
     */
    public AnnotationScanner includeMetaAnnotations(boolean include) {
        this.includeMetaAnnotations = include;
        return this;
    }

    // ==================== Class Scanning | 类扫描 ====================

    /**
     * Finds all classes with annotation
     * 查找所有具有注解的类
     *
     * @param annotationClass the annotation class | 注解类
     * @return set of classes | 类集合
     */
    public Set<Class<?>> findClassesWithAnnotation(Class<? extends Annotation> annotationClass) {
        Set<Class<?>> result = new LinkedHashSet<>();
        for (Class<?> clazz : classScanner.scan()) {
            if (hasAnnotation(clazz, annotationClass)) {
                result.add(clazz);
            }
        }
        return result;
    }

    /**
     * Finds all classes with any of the annotations
     * 查找具有任一注解的所有类
     *
     * @param annotationClasses the annotation classes | 注解类
     * @return set of classes | 类集合
     */
    @SafeVarargs
    public final Set<Class<?>> findClassesWithAnyAnnotation(Class<? extends Annotation>... annotationClasses) {
        Set<Class<?>> result = new LinkedHashSet<>();
        for (Class<?> clazz : classScanner.scan()) {
            for (Class<? extends Annotation> annotationClass : annotationClasses) {
                if (hasAnnotation(clazz, annotationClass)) {
                    result.add(clazz);
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Finds all classes with all annotations
     * 查找具有所有注解的类
     *
     * @param annotationClasses the annotation classes | 注解类
     * @return set of classes | 类集合
     */
    @SafeVarargs
    public final Set<Class<?>> findClassesWithAllAnnotations(Class<? extends Annotation>... annotationClasses) {
        Set<Class<?>> result = new LinkedHashSet<>();
        for (Class<?> clazz : classScanner.scan()) {
            boolean hasAll = true;
            for (Class<? extends Annotation> annotationClass : annotationClasses) {
                if (!hasAnnotation(clazz, annotationClass)) {
                    hasAll = false;
                    break;
                }
            }
            if (hasAll) {
                result.add(clazz);
            }
        }
        return result;
    }

    // ==================== Method Scanning | 方法扫描 ====================

    /**
     * Finds all methods with annotation
     * 查找所有具有注解的方法
     *
     * @param annotationClass the annotation class | 注解类
     * @return map of class to methods | 类到方法的映射
     */
    public Map<Class<?>, Set<Method>> findMethodsWithAnnotation(Class<? extends Annotation> annotationClass) {
        Map<Class<?>, Set<Method>> result = new LinkedHashMap<>();
        for (Class<?> clazz : classScanner.scan()) {
            Set<Method> methods = new LinkedHashSet<>();
            for (Method method : clazz.getDeclaredMethods()) {
                if (hasAnnotation(method, annotationClass)) {
                    methods.add(method);
                }
            }
            if (!methods.isEmpty()) {
                result.put(clazz, methods);
            }
        }
        return result;
    }

    /**
     * Finds all methods with annotation as flat list
     * 查找所有具有注解的方法（扁平列表）
     *
     * @param annotationClass the annotation class | 注解类
     * @return list of methods | 方法列表
     */
    public List<Method> findAllMethodsWithAnnotation(Class<? extends Annotation> annotationClass) {
        List<Method> result = new ArrayList<>();
        for (Class<?> clazz : classScanner.scan()) {
            for (Method method : clazz.getDeclaredMethods()) {
                if (hasAnnotation(method, annotationClass)) {
                    result.add(method);
                }
            }
        }
        return result;
    }

    // ==================== Field Scanning | 字段扫描 ====================

    /**
     * Finds all fields with annotation
     * 查找所有具有注解的字段
     *
     * @param annotationClass the annotation class | 注解类
     * @return map of class to fields | 类到字段的映射
     */
    public Map<Class<?>, Set<Field>> findFieldsWithAnnotation(Class<? extends Annotation> annotationClass) {
        Map<Class<?>, Set<Field>> result = new LinkedHashMap<>();
        for (Class<?> clazz : classScanner.scan()) {
            Set<Field> fields = new LinkedHashSet<>();
            for (Field field : clazz.getDeclaredFields()) {
                if (hasAnnotation(field, annotationClass)) {
                    fields.add(field);
                }
            }
            if (!fields.isEmpty()) {
                result.put(clazz, fields);
            }
        }
        return result;
    }

    /**
     * Finds all fields with annotation as flat list
     * 查找所有具有注解的字段（扁平列表）
     *
     * @param annotationClass the annotation class | 注解类
     * @return list of fields | 字段列表
     */
    public List<Field> findAllFieldsWithAnnotation(Class<? extends Annotation> annotationClass) {
        List<Field> result = new ArrayList<>();
        for (Class<?> clazz : classScanner.scan()) {
            for (Field field : clazz.getDeclaredFields()) {
                if (hasAnnotation(field, annotationClass)) {
                    result.add(field);
                }
            }
        }
        return result;
    }

    // ==================== Constructor Scanning | 构造器扫描 ====================

    /**
     * Finds all constructors with annotation
     * 查找所有具有注解的构造器
     *
     * @param annotationClass the annotation class | 注解类
     * @return map of class to constructors | 类到构造器的映射
     */
    public Map<Class<?>, Set<Constructor<?>>> findConstructorsWithAnnotation(Class<? extends Annotation> annotationClass) {
        Map<Class<?>, Set<Constructor<?>>> result = new LinkedHashMap<>();
        for (Class<?> clazz : classScanner.scan()) {
            Set<Constructor<?>> constructors = new LinkedHashSet<>();
            for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
                if (hasAnnotation(constructor, annotationClass)) {
                    constructors.add(constructor);
                }
            }
            if (!constructors.isEmpty()) {
                result.put(clazz, constructors);
            }
        }
        return result;
    }

    // ==================== Parameter Scanning | 参数扫描 ====================

    /**
     * Finds all parameters with annotation
     * 查找所有具有注解的参数
     *
     * @param annotationClass the annotation class | 注解类
     * @return map of method to parameters | 方法到参数的映射
     */
    public Map<Executable, Set<Parameter>> findParametersWithAnnotation(Class<? extends Annotation> annotationClass) {
        Map<Executable, Set<Parameter>> result = new LinkedHashMap<>();
        for (Class<?> clazz : classScanner.scan()) {
            // Methods
            for (Method method : clazz.getDeclaredMethods()) {
                Set<Parameter> params = new LinkedHashSet<>();
                for (Parameter param : method.getParameters()) {
                    if (param.isAnnotationPresent(annotationClass)) {
                        params.add(param);
                    }
                }
                if (!params.isEmpty()) {
                    result.put(method, params);
                }
            }
            // Constructors
            for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
                Set<Parameter> params = new LinkedHashSet<>();
                for (Parameter param : constructor.getParameters()) {
                    if (param.isAnnotationPresent(annotationClass)) {
                        params.add(param);
                    }
                }
                if (!params.isEmpty()) {
                    result.put(constructor, params);
                }
            }
        }
        return result;
    }

    // ==================== Helper Methods | 辅助方法 ====================

    private boolean hasAnnotation(AnnotatedElement element, Class<? extends Annotation> annotationClass) {
        if (element.isAnnotationPresent(annotationClass)) {
            return true;
        }
        if (includeMetaAnnotations) {
            for (Annotation annotation : element.getAnnotations()) {
                if (annotation.annotationType().isAnnotationPresent(annotationClass)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Result holder for annotated element
     * 被注解元素结果持有者
     *
     * @param <T> the element type | 元素类型
     */
    public record AnnotatedMember<T>(Class<?> declaringClass, T element, Annotation annotation) {
    }
}
