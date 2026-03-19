package cloud.opencode.base.classloader.scanner;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

/**
 * Annotation Scanner - Scans for annotated elements
 * 注解扫描器 - 扫描带注解的元素
 *
 * <p>Scans classes, methods, fields and constructors for annotations.</p>
 * <p>扫描类、方法、字段和构造器上的注解。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Scan annotated classes - 扫描带注解的类</li>
 *   <li>Scan annotated methods - 扫描带注解的方法</li>
 *   <li>Scan annotated fields - 扫描带注解的字段</li>
 *   <li>Scan meta-annotations - 扫描元注解</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * AnnotationScanner scanner = AnnotationScanner.of("com.example");
 * Set<Class<?>> services = scanner.scanClasses(Service.class);
 * Set<Method> getMappings = scanner.scanMethods(GetMapping.class);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.0
 */
public class AnnotationScanner {

    private final ClassScanner classScanner;

    private AnnotationScanner(String basePackage) {
        this.classScanner = ClassScanner.of(basePackage);
    }

    /**
     * Create annotation scanner
     * 创建注解扫描器
     *
     * @param basePackage base package | 基础包
     * @return scanner | 扫描器
     */
    public static AnnotationScanner of(String basePackage) {
        return new AnnotationScanner(basePackage);
    }

    /**
     * Set class loader
     * 设置类加载器
     *
     * @param classLoader class loader | 类加载器
     * @return this scanner | 此扫描器
     */
    public AnnotationScanner classLoader(ClassLoader classLoader) {
        this.classScanner.classLoader(classLoader);
        return this;
    }

    /**
     * Include inner classes
     * 包含内部类
     *
     * @param include include inner classes | 包含内部类
     * @return this scanner | 此扫描器
     */
    public AnnotationScanner includeInnerClasses(boolean include) {
        this.classScanner.includeInnerClasses(include);
        return this;
    }

    // ==================== Scan Methods | 扫描方法 ====================

    /**
     * Scan classes with annotation
     * 扫描带注解的类
     *
     * @param annotation annotation class | 注解类
     * @return set of annotated classes | 带注解的类集合
     */
    public Set<Class<?>> scanClasses(Class<? extends Annotation> annotation) {
        Objects.requireNonNull(annotation, "Annotation must not be null");
        return classScanner.scanWithAnnotation(annotation);
    }

    /**
     * Scan methods with annotation
     * 扫描带注解的方法
     *
     * @param annotation annotation class | 注解类
     * @return set of annotated methods | 带注解的方法集合
     */
    public Set<Method> scanMethods(Class<? extends Annotation> annotation) {
        Objects.requireNonNull(annotation, "Annotation must not be null");
        Set<Method> result = new HashSet<>();

        Set<Class<?>> allClasses = classScanner.scan();
        for (Class<?> clazz : allClasses) {
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(annotation)) {
                    result.add(method);
                }
            }
        }

        return result;
    }

    /**
     * Scan fields with annotation
     * 扫描带注解的字段
     *
     * @param annotation annotation class | 注解类
     * @return set of annotated fields | 带注解的字段集合
     */
    public Set<Field> scanFields(Class<? extends Annotation> annotation) {
        Objects.requireNonNull(annotation, "Annotation must not be null");
        Set<Field> result = new HashSet<>();

        Set<Class<?>> allClasses = classScanner.scan();
        for (Class<?> clazz : allClasses) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(annotation)) {
                    result.add(field);
                }
            }
        }

        return result;
    }

    /**
     * Scan constructors with annotation
     * 扫描带注解的构造器
     *
     * @param annotation annotation class | 注解类
     * @return set of annotated constructors | 带注解的构造器集合
     */
    public Set<Constructor<?>> scanConstructors(Class<? extends Annotation> annotation) {
        Objects.requireNonNull(annotation, "Annotation must not be null");
        Set<Constructor<?>> result = new HashSet<>();

        Set<Class<?>> allClasses = classScanner.scan();
        for (Class<?> clazz : allClasses) {
            for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
                if (constructor.isAnnotationPresent(annotation)) {
                    result.add(constructor);
                }
            }
        }

        return result;
    }

    /**
     * Scan classes with meta-annotation (annotation's annotation)
     * 扫描带元注解（注解的注解）的类
     *
     * @param metaAnnotation meta-annotation class | 元注解类
     * @return set of classes with annotations that have the meta-annotation | 带有元注解的注解的类集合
     */
    public Set<Class<?>> scanMetaAnnotated(Class<? extends Annotation> metaAnnotation) {
        Objects.requireNonNull(metaAnnotation, "Meta-annotation must not be null");
        Set<Class<?>> result = new HashSet<>();

        Set<Class<?>> allClasses = classScanner.scan();
        for (Class<?> clazz : allClasses) {
            for (Annotation annotation : clazz.getAnnotations()) {
                if (annotation.annotationType().isAnnotationPresent(metaAnnotation)) {
                    result.add(clazz);
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Scan parameters with annotation
     * 扫描带注解的参数
     *
     * @param annotation annotation class | 注解类
     * @return map of method to annotated parameter indices | 方法到带注解参数索引的映射
     */
    public Map<Method, List<Integer>> scanParameters(Class<? extends Annotation> annotation) {
        Objects.requireNonNull(annotation, "Annotation must not be null");
        Map<Method, List<Integer>> result = new HashMap<>();

        Set<Class<?>> allClasses = classScanner.scan();
        for (Class<?> clazz : allClasses) {
            for (Method method : clazz.getDeclaredMethods()) {
                Annotation[][] paramAnnotations = method.getParameterAnnotations();
                List<Integer> indices = new ArrayList<>();
                for (int i = 0; i < paramAnnotations.length; i++) {
                    for (Annotation ann : paramAnnotations[i]) {
                        if (annotation.isInstance(ann)) {
                            indices.add(i);
                            break;
                        }
                    }
                }
                if (!indices.isEmpty()) {
                    result.put(method, indices);
                }
            }
        }

        return result;
    }

    /**
     * Find all classes that have any method with the given annotation
     * 查找所有拥有带指定注解方法的类
     *
     * @param annotation annotation class | 注解类
     * @return set of classes | 类集合
     */
    public Set<Class<?>> scanClassesWithAnnotatedMethods(Class<? extends Annotation> annotation) {
        Objects.requireNonNull(annotation, "Annotation must not be null");
        Set<Class<?>> result = new HashSet<>();

        Set<Class<?>> allClasses = classScanner.scan();
        for (Class<?> clazz : allClasses) {
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(annotation)) {
                    result.add(clazz);
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Find all classes that have any field with the given annotation
     * 查找所有拥有带指定注解字段的类
     *
     * @param annotation annotation class | 注解类
     * @return set of classes | 类集合
     */
    public Set<Class<?>> scanClassesWithAnnotatedFields(Class<? extends Annotation> annotation) {
        Objects.requireNonNull(annotation, "Annotation must not be null");
        Set<Class<?>> result = new HashSet<>();

        Set<Class<?>> allClasses = classScanner.scan();
        for (Class<?> clazz : allClasses) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(annotation)) {
                    result.add(clazz);
                    break;
                }
            }
        }

        return result;
    }
}
