package cloud.opencode.base.reflect;

import cloud.opencode.base.reflect.exception.OpenReflectException;

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Annotation Utility Class
 * 注解工具类
 *
 * <p>Provides utilities for annotation discovery and processing.</p>
 * <p>提供注解发现和处理的工具。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Annotation discovery with caching - 带缓存的注解发现</li>
 *   <li>Inherited annotation resolution - 继承注解解析</li>
 *   <li>Repeatable annotation support - 可重复注解支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Annotation ann = AnnotationUtil.findAnnotation(element, MyAnnotation.class);
 * List<Annotation> all = AnnotationUtil.findAnnotations(element);
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
 *   <li>Time complexity: O(1) for cached lookups; O(h) for first access where h is the class hierarchy depth - 时间复杂度: 缓存命中时 O(1)；首次访问为 O(h)，h为类层次深度</li>
 *   <li>Space complexity: O(a) for the annotation cache where a is the number of distinct annotations - 空间复杂度: O(a)，a为不同注解的数量</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
public final class AnnotationUtil {

    private static final Map<AnnotationKey, Annotation> ANNOTATION_CACHE = new ConcurrentHashMap<>();
    private static final Map<AnnotationKey, List<Annotation>> ANNOTATIONS_CACHE = new ConcurrentHashMap<>();

    private AnnotationUtil() {
    }

    // ==================== Annotation Discovery | 注解发现 ====================

    /**
     * Gets annotation (cached)
     * 获取注解（缓存）
     *
     * @param element         the annotated element | 被注解元素
     * @param annotationClass the annotation class | 注解类
     * @param <A>             the annotation type | 注解类型
     * @return the annotation or null | 注解或null
     */
    @SuppressWarnings("unchecked")
    public static <A extends Annotation> A getAnnotation(AnnotatedElement element, Class<A> annotationClass) {
        AnnotationKey key = new AnnotationKey(element, annotationClass);
        return (A) ANNOTATION_CACHE.computeIfAbsent(key, k -> element.getAnnotation(annotationClass));
    }

    /**
     * Gets annotation as Optional
     * 获取注解为Optional
     *
     * @param element         the annotated element | 被注解元素
     * @param annotationClass the annotation class | 注解类
     * @param <A>             the annotation type | 注解类型
     * @return Optional of annotation | 注解的Optional
     */
    public static <A extends Annotation> Optional<A> findAnnotation(AnnotatedElement element, Class<A> annotationClass) {
        return Optional.ofNullable(getAnnotation(element, annotationClass));
    }

    /**
     * Checks if annotation is present
     * 检查注解是否存在
     *
     * @param element         the annotated element | 被注解元素
     * @param annotationClass the annotation class | 注解类
     * @return true if present | 如果存在返回true
     */
    public static boolean hasAnnotation(AnnotatedElement element, Class<? extends Annotation> annotationClass) {
        return element.isAnnotationPresent(annotationClass);
    }

    /**
     * Gets all annotations
     * 获取所有注解
     *
     * @param element the annotated element | 被注解元素
     * @return array of annotations | 注解数组
     */
    public static Annotation[] getAnnotations(AnnotatedElement element) {
        return element.getAnnotations();
    }

    /**
     * Gets declared annotations
     * 获取声明的注解
     *
     * @param element the annotated element | 被注解元素
     * @return array of annotations | 注解数组
     */
    public static Annotation[] getDeclaredAnnotations(AnnotatedElement element) {
        return element.getDeclaredAnnotations();
    }

    /**
     * Gets repeatable annotations
     * 获取可重复注解
     *
     * @param element         the annotated element | 被注解元素
     * @param annotationClass the annotation class | 注解类
     * @param <A>             the annotation type | 注解类型
     * @return array of annotations | 注解数组
     */
    public static <A extends Annotation> A[] getRepeatableAnnotations(AnnotatedElement element,
            Class<A> annotationClass) {
        return element.getAnnotationsByType(annotationClass);
    }

    // ==================== Meta-Annotation Search | 元注解搜索 ====================

    /**
     * Finds annotation including meta-annotations
     * 查找注解包含元注解
     *
     * @param element         the annotated element | 被注解元素
     * @param annotationClass the annotation class | 注解类
     * @param <A>             the annotation type | 注解类型
     * @return the annotation or null | 注解或null
     */
    @SuppressWarnings("unchecked")
    public static <A extends Annotation> A findMetaAnnotation(AnnotatedElement element, Class<A> annotationClass) {
        // Direct annotation
        A direct = element.getAnnotation(annotationClass);
        if (direct != null) {
            return direct;
        }
        // Meta-annotation search
        Set<Class<? extends Annotation>> visited = new HashSet<>();
        for (Annotation annotation : element.getAnnotations()) {
            A found = findMetaAnnotationRecursive(annotation.annotationType(), annotationClass, visited);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static <A extends Annotation> A findMetaAnnotationRecursive(Class<? extends Annotation> annotationType,
            Class<A> targetClass, Set<Class<? extends Annotation>> visited) {
        if (!visited.add(annotationType)) {
            return null;
        }
        A direct = annotationType.getAnnotation(targetClass);
        if (direct != null) {
            return direct;
        }
        for (Annotation meta : annotationType.getAnnotations()) {
            if (!meta.annotationType().getPackageName().startsWith("java.lang")) {
                A found = findMetaAnnotationRecursive(meta.annotationType(), targetClass, visited);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    /**
     * Checks if has meta-annotation
     * 检查是否有元注解
     *
     * @param element         the annotated element | 被注解元素
     * @param annotationClass the annotation class | 注解类
     * @return true if has | 如果有返回true
     */
    public static boolean hasMetaAnnotation(AnnotatedElement element, Class<? extends Annotation> annotationClass) {
        return findMetaAnnotation(element, annotationClass) != null;
    }

    // ==================== Annotation Attributes | 注解属性 ====================

    /**
     * Gets annotation attribute value
     * 获取注解属性值
     *
     * @param annotation    the annotation | 注解
     * @param attributeName the attribute name | 属性名
     * @return the value | 值
     */
    public static Object getAttributeValue(Annotation annotation, String attributeName) {
        try {
            Method method = annotation.annotationType().getDeclaredMethod(attributeName);
            return method.invoke(annotation);
        } catch (NoSuchMethodException e) {
            return null;
        } catch (Exception e) {
            throw new OpenReflectException("Failed to get annotation attribute: " + attributeName, e);
        }
    }

    /**
     * Gets annotation attribute value with type
     * 获取注解属性值（指定类型）
     *
     * @param annotation    the annotation | 注解
     * @param attributeName the attribute name | 属性名
     * @param type          the expected type | 期望类型
     * @param <T>           the type | 类型
     * @return the value | 值
     */
    @SuppressWarnings("unchecked")
    public static <T> T getAttributeValue(Annotation annotation, String attributeName, Class<T> type) {
        Object value = getAttributeValue(annotation, attributeName);
        if (value == null) {
            return null;
        }
        if (!type.isInstance(value)) {
            throw new ClassCastException("Annotation attribute '" + attributeName +
                                         "' is not of type " + type.getName());
        }
        return (T) value;
    }

    /**
     * Gets all annotation attributes as map
     * 获取所有注解属性为Map
     *
     * @param annotation the annotation | 注解
     * @return map of attributes | 属性映射
     */
    public static Map<String, Object> getAttributes(Annotation annotation) {
        Map<String, Object> attributes = new LinkedHashMap<>();
        for (Method method : annotation.annotationType().getDeclaredMethods()) {
            if (method.getParameterCount() == 0) {
                try {
                    attributes.put(method.getName(), method.invoke(annotation));
                } catch (Exception ignored) {
                }
            }
        }
        return attributes;
    }

    /**
     * Gets annotation "value" attribute
     * 获取注解"value"属性
     *
     * @param annotation the annotation | 注解
     * @return the value | 值
     */
    public static Object getValue(Annotation annotation) {
        return getAttributeValue(annotation, "value");
    }

    /**
     * Gets annotation "value" attribute with type
     * 获取注解"value"属性（指定类型）
     *
     * @param annotation the annotation | 注解
     * @param type       the expected type | 期望类型
     * @param <T>        the type | 类型
     * @return the value | 值
     */
    public static <T> T getValue(Annotation annotation, Class<T> type) {
        return getAttributeValue(annotation, "value", type);
    }

    // ==================== Annotation Information | 注解信息 ====================

    /**
     * Checks if annotation is repeatable
     * 检查注解是否可重复
     *
     * @param annotationClass the annotation class | 注解类
     * @return true if repeatable | 如果可重复返回true
     */
    public static boolean isRepeatable(Class<? extends Annotation> annotationClass) {
        return annotationClass.isAnnotationPresent(Repeatable.class);
    }

    /**
     * Gets repeatable container class
     * 获取可重复注解容器类
     *
     * @param annotationClass the annotation class | 注解类
     * @return the container class or null | 容器类或null
     */
    public static Class<? extends Annotation> getRepeatableContainer(Class<? extends Annotation> annotationClass) {
        Repeatable repeatable = annotationClass.getAnnotation(Repeatable.class);
        return repeatable != null ? repeatable.value() : null;
    }

    /**
     * Gets annotation type
     * 获取注解类型
     *
     * @param annotation the annotation | 注解
     * @return the annotation type | 注解类型
     */
    public static Class<? extends Annotation> getAnnotationType(Annotation annotation) {
        return annotation.annotationType();
    }

    /**
     * Gets annotation attribute methods
     * 获取注解属性方法
     *
     * @param annotationClass the annotation class | 注解类
     * @return list of methods | 方法列表
     */
    public static List<Method> getAttributeMethods(Class<? extends Annotation> annotationClass) {
        List<Method> methods = new ArrayList<>();
        for (Method method : annotationClass.getDeclaredMethods()) {
            if (method.getParameterCount() == 0 && method.getDeclaringClass() == annotationClass) {
                methods.add(method);
            }
        }
        return methods;
    }

    // ==================== Inheritance | 继承 ====================

    /**
     * Finds annotation on class hierarchy
     * 在类层次结构中查找注解
     *
     * @param clazz           the class | 类
     * @param annotationClass the annotation class | 注解类
     * @param <A>             the annotation type | 注解类型
     * @return the annotation or null | 注解或null
     */
    public static <A extends Annotation> A findAnnotationOnClass(Class<?> clazz, Class<A> annotationClass) {
        // Check current class
        A annotation = clazz.getAnnotation(annotationClass);
        if (annotation != null) {
            return annotation;
        }
        // Check superclass
        Class<?> superclass = clazz.getSuperclass();
        if (superclass != null && superclass != Object.class) {
            annotation = findAnnotationOnClass(superclass, annotationClass);
            if (annotation != null) {
                return annotation;
            }
        }
        // Check interfaces
        for (Class<?> iface : clazz.getInterfaces()) {
            annotation = findAnnotationOnClass(iface, annotationClass);
            if (annotation != null) {
                return annotation;
            }
        }
        return null;
    }

    /**
     * Collects all annotations of type on class hierarchy
     * 收集类层次结构中所有指定类型的注解
     *
     * @param clazz           the class | 类
     * @param annotationClass the annotation class | 注解类
     * @param <A>             the annotation type | 注解类型
     * @return list of annotations | 注解列表
     */
    public static <A extends Annotation> List<A> findAllAnnotationsOnClass(Class<?> clazz, Class<A> annotationClass) {
        List<A> annotations = new ArrayList<>();
        collectAnnotations(clazz, annotationClass, annotations, new HashSet<>());
        return annotations;
    }

    private static <A extends Annotation> void collectAnnotations(Class<?> clazz, Class<A> annotationClass,
            List<A> result, Set<Class<?>> visited) {
        if (clazz == null || clazz == Object.class || !visited.add(clazz)) {
            return;
        }
        A annotation = clazz.getAnnotation(annotationClass);
        if (annotation != null) {
            result.add(annotation);
        }
        collectAnnotations(clazz.getSuperclass(), annotationClass, result, visited);
        for (Class<?> iface : clazz.getInterfaces()) {
            collectAnnotations(iface, annotationClass, result, visited);
        }
    }

    // ==================== Cache Management | 缓存管理 ====================

    /**
     * Clears annotation cache
     * 清除注解缓存
     */
    public static void clearCache() {
        ANNOTATION_CACHE.clear();
        ANNOTATIONS_CACHE.clear();
    }

    // ==================== Internal | 内部 ====================

    private record AnnotationKey(AnnotatedElement element, Class<? extends Annotation> annotationClass) {
    }
}
