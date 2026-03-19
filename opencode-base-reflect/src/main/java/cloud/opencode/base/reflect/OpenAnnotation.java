package cloud.opencode.base.reflect;

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Predicate;

/**
 * Annotation Facade Entry Class
 * 注解门面入口类
 *
 * <p>Provides common annotation operations API.</p>
 * <p>提供常用注解操作API。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Annotation retrieval - 注解获取</li>
 *   <li>Annotation presence checking - 注解存在检查</li>
 *   <li>Annotation attribute access - 注解属性访问</li>
 *   <li>Meta-annotation support - 元注解支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Check if annotation is present
 * boolean present = OpenAnnotation.isAnnotationPresent(method, Deprecated.class);
 *
 * // Get annotation attribute value
 * Object value = OpenAnnotation.getAttributeValue(annotation, "value");
 *
 * // Find inherited annotation
 * Optional<MyAnnotation> ann = OpenAnnotation.findAnnotationInherited(MyClass.class, MyAnnotation.class);
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
public final class OpenAnnotation {

    private OpenAnnotation() {
    }

    // ==================== Annotation Retrieval | 注解获取 ====================

    /**
     * Gets an annotation from an element
     * 从元素获取注解
     *
     * @param element         the annotated element | 被注解元素
     * @param annotationClass the annotation class | 注解类
     * @param <A>             the annotation type | 注解类型
     * @return the annotation or null | 注解或null
     */
    public static <A extends Annotation> A getAnnotation(AnnotatedElement element, Class<A> annotationClass) {
        return element.getAnnotation(annotationClass);
    }

    /**
     * Gets an annotation from an element (Optional)
     * 从元素获取注解（Optional）
     *
     * @param element         the annotated element | 被注解元素
     * @param annotationClass the annotation class | 注解类
     * @param <A>             the annotation type | 注解类型
     * @return Optional of annotation | 注解的Optional
     */
    public static <A extends Annotation> Optional<A> findAnnotation(AnnotatedElement element, Class<A> annotationClass) {
        return Optional.ofNullable(element.getAnnotation(annotationClass));
    }

    /**
     * Gets all annotations from an element
     * 获取元素的所有注解
     *
     * @param element the annotated element | 被注解元素
     * @return array of annotations | 注解数组
     */
    public static Annotation[] getAnnotations(AnnotatedElement element) {
        return element.getAnnotations();
    }

    /**
     * Gets declared annotations from an element
     * 获取元素声明的注解（不含继承）
     *
     * @param element the annotated element | 被注解元素
     * @return array of annotations | 注解数组
     */
    public static Annotation[] getDeclaredAnnotations(AnnotatedElement element) {
        return element.getDeclaredAnnotations();
    }

    /**
     * Gets annotations by type (including repeatable)
     * 按类型获取注解（包含可重复注解）
     *
     * @param element         the annotated element | 被注解元素
     * @param annotationClass the annotation class | 注解类
     * @param <A>             the annotation type | 注解类型
     * @return array of annotations | 注解数组
     */
    public static <A extends Annotation> A[] getAnnotationsByType(AnnotatedElement element, Class<A> annotationClass) {
        return element.getAnnotationsByType(annotationClass);
    }

    // ==================== Inherited Annotation | 继承注解 ====================

    /**
     * Finds annotation including inherited (for classes)
     * 查找注解（包含继承，用于类）
     *
     * @param clazz           the class | 类
     * @param annotationClass the annotation class | 注解类
     * @param <A>             the annotation type | 注解类型
     * @return Optional of annotation | 注解的Optional
     */
    public static <A extends Annotation> Optional<A> findAnnotationInherited(Class<?> clazz, Class<A> annotationClass) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            A annotation = current.getAnnotation(annotationClass);
            if (annotation != null) {
                return Optional.of(annotation);
            }
            current = current.getSuperclass();
        }
        return Optional.empty();
    }

    /**
     * Finds annotation on method including overridden methods
     * 查找方法注解（包含被重写的方法）
     *
     * @param method          the method | 方法
     * @param annotationClass the annotation class | 注解类
     * @param <A>             the annotation type | 注解类型
     * @return Optional of annotation | 注解的Optional
     */
    public static <A extends Annotation> Optional<A> findAnnotationOnMethod(Method method, Class<A> annotationClass) {
        A annotation = method.getAnnotation(annotationClass);
        if (annotation != null) {
            return Optional.of(annotation);
        }

        // Check superclass
        Class<?> superClass = method.getDeclaringClass().getSuperclass();
        if (superClass != null && superClass != Object.class) {
            try {
                Method superMethod = superClass.getMethod(method.getName(), method.getParameterTypes());
                return findAnnotationOnMethod(superMethod, annotationClass);
            } catch (NoSuchMethodException ignored) {
            }
        }

        // Check interfaces
        for (Class<?> iface : method.getDeclaringClass().getInterfaces()) {
            try {
                Method ifaceMethod = iface.getMethod(method.getName(), method.getParameterTypes());
                annotation = ifaceMethod.getAnnotation(annotationClass);
                if (annotation != null) {
                    return Optional.of(annotation);
                }
            } catch (NoSuchMethodException ignored) {
            }
        }

        return Optional.empty();
    }

    // ==================== Presence Checking | 存在检查 ====================

    /**
     * Checks if annotation is present
     * 检查注解是否存在
     *
     * @param element         the annotated element | 被注解元素
     * @param annotationClass the annotation class | 注解类
     * @return true if present | 如果存在返回true
     */
    public static boolean isAnnotationPresent(AnnotatedElement element, Class<? extends Annotation> annotationClass) {
        return element.isAnnotationPresent(annotationClass);
    }

    /**
     * Checks if any of the annotations are present
     * 检查是否存在任一注解
     *
     * @param element           the annotated element | 被注解元素
     * @param annotationClasses the annotation classes | 注解类
     * @return true if any present | 如果任一存在返回true
     */
    @SafeVarargs
    public static boolean isAnyAnnotationPresent(AnnotatedElement element, Class<? extends Annotation>... annotationClasses) {
        for (Class<? extends Annotation> annotationClass : annotationClasses) {
            if (element.isAnnotationPresent(annotationClass)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if all annotations are present
     * 检查是否所有注解都存在
     *
     * @param element           the annotated element | 被注解元素
     * @param annotationClasses the annotation classes | 注解类
     * @return true if all present | 如果全部存在返回true
     */
    @SafeVarargs
    public static boolean isAllAnnotationsPresent(AnnotatedElement element, Class<? extends Annotation>... annotationClasses) {
        for (Class<? extends Annotation> annotationClass : annotationClasses) {
            if (!element.isAnnotationPresent(annotationClass)) {
                return false;
            }
        }
        return true;
    }

    // ==================== Attribute Access | 属性访问 ====================

    /**
     * Gets annotation attribute value
     * 获取注解属性值
     *
     * @param annotation    the annotation | 注解
     * @param attributeName the attribute name | 属性名
     * @return the value or null | 值或null
     */
    public static Object getAttributeValue(Annotation annotation, String attributeName) {
        try {
            Method method = annotation.annotationType().getMethod(attributeName);
            return method.invoke(annotation);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            return null;
        }
    }

    /**
     * Gets annotation attribute value with type
     * 获取注解属性值（带类型）
     *
     * @param annotation    the annotation | 注解
     * @param attributeName the attribute name | 属性名
     * @param type          the expected type | 期望类型
     * @param <T>           the value type | 值类型
     * @return the value or null | 值或null
     */
    @SuppressWarnings("unchecked")
    public static <T> T getAttributeValue(Annotation annotation, String attributeName, Class<T> type) {
        Object value = getAttributeValue(annotation, attributeName);
        if (value != null && type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }

    /**
     * Gets all attribute values as a map
     * 获取所有属性值（Map形式）
     *
     * @param annotation the annotation | 注解
     * @return map of attribute name to value | 属性名到值的映射
     */
    public static Map<String, Object> getAttributeValues(Annotation annotation) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Method method : annotation.annotationType().getDeclaredMethods()) {
            if (method.getParameterCount() == 0 && !method.getDeclaringClass().equals(Annotation.class)) {
                try {
                    result.put(method.getName(), method.invoke(annotation));
                } catch (IllegalAccessException | InvocationTargetException ignored) {
                }
            }
        }
        return result;
    }

    /**
     * Gets the default value of an annotation attribute
     * 获取注解属性的默认值
     *
     * @param annotationClass the annotation class | 注解类
     * @param attributeName   the attribute name | 属性名
     * @return the default value or null | 默认值或null
     */
    public static Object getDefaultValue(Class<? extends Annotation> annotationClass, String attributeName) {
        try {
            Method method = annotationClass.getMethod(attributeName);
            return method.getDefaultValue();
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    // ==================== Meta-Annotation | 元注解 ====================

    /**
     * Checks if annotation is a meta-annotation of another
     * 检查是否为元注解
     *
     * @param annotation     the annotation | 注解
     * @param metaAnnotation the meta-annotation class | 元注解类
     * @return true if is meta-annotated | 如果被元注解标注返回true
     */
    public static boolean isMetaAnnotationPresent(Class<? extends Annotation> annotation,
                                                   Class<? extends Annotation> metaAnnotation) {
        return annotation.isAnnotationPresent(metaAnnotation);
    }

    /**
     * Finds meta-annotation on an annotation
     * 在注解上查找元注解
     *
     * @param annotation     the annotation | 注解
     * @param metaAnnotation the meta-annotation class | 元注解类
     * @param <A>            the meta-annotation type | 元注解类型
     * @return Optional of meta-annotation | 元注解的Optional
     */
    public static <A extends Annotation> Optional<A> findMetaAnnotation(Class<? extends Annotation> annotation,
                                                                         Class<A> metaAnnotation) {
        return Optional.ofNullable(annotation.getAnnotation(metaAnnotation));
    }

    /**
     * Gets all meta-annotations on an annotation
     * 获取注解上的所有元注解
     *
     * @param annotation the annotation | 注解
     * @return list of meta-annotations | 元注解列表
     */
    public static List<Annotation> getMetaAnnotations(Class<? extends Annotation> annotation) {
        List<Annotation> result = new ArrayList<>();
        for (Annotation meta : annotation.getAnnotations()) {
            // Exclude standard meta-annotations
            String typeName = meta.annotationType().getName();
            if (!typeName.startsWith("java.lang.annotation.")) {
                result.add(meta);
            }
        }
        return result;
    }

    // ==================== Annotation Information | 注解信息 ====================

    /**
     * Checks if annotation is runtime-retained
     * 检查注解是否运行时保留
     *
     * @param annotationClass the annotation class | 注解类
     * @return true if runtime retained | 如果运行时保留返回true
     */
    public static boolean isRuntimeRetained(Class<? extends Annotation> annotationClass) {
        Retention retention = annotationClass.getAnnotation(Retention.class);
        return retention != null && retention.value() == RetentionPolicy.RUNTIME;
    }

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
     * Gets the container annotation for a repeatable annotation
     * 获取可重复注解的容器注解
     *
     * @param annotationClass the repeatable annotation class | 可重复注解类
     * @return Optional of container class | 容器类的Optional
     */
    public static Optional<Class<? extends Annotation>> getRepeatableContainer(Class<? extends Annotation> annotationClass) {
        Repeatable repeatable = annotationClass.getAnnotation(Repeatable.class);
        return repeatable != null ? Optional.of(repeatable.value()) : Optional.empty();
    }

    /**
     * Gets all annotation attributes (methods)
     * 获取所有注解属性（方法）
     *
     * @param annotationClass the annotation class | 注解类
     * @return list of attribute methods | 属性方法列表
     */
    public static List<Method> getAttributes(Class<? extends Annotation> annotationClass) {
        List<Method> result = new ArrayList<>();
        for (Method method : annotationClass.getDeclaredMethods()) {
            if (method.getParameterCount() == 0) {
                result.add(method);
            }
        }
        return result;
    }

    // ==================== Class Annotation Scanning | 类注解扫描 ====================

    /**
     * Gets all annotated fields in a class
     * 获取类中所有被注解的字段
     *
     * @param clazz           the class | 类
     * @param annotationClass the annotation class | 注解类
     * @return list of fields | 字段列表
     */
    public static List<Field> getAnnotatedFields(Class<?> clazz, Class<? extends Annotation> annotationClass) {
        List<Field> result = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(annotationClass)) {
                result.add(field);
            }
        }
        return result;
    }

    /**
     * Gets all annotated methods in a class
     * 获取类中所有被注解的方法
     *
     * @param clazz           the class | 类
     * @param annotationClass the annotation class | 注解类
     * @return list of methods | 方法列表
     */
    public static List<Method> getAnnotatedMethods(Class<?> clazz, Class<? extends Annotation> annotationClass) {
        List<Method> result = new ArrayList<>();
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(annotationClass)) {
                result.add(method);
            }
        }
        return result;
    }

    /**
     * Gets all annotated constructors in a class
     * 获取类中所有被注解的构造器
     *
     * @param clazz           the class | 类
     * @param annotationClass the annotation class | 注解类
     * @return list of constructors | 构造器列表
     */
    public static List<Constructor<?>> getAnnotatedConstructors(Class<?> clazz, Class<? extends Annotation> annotationClass) {
        List<Constructor<?>> result = new ArrayList<>();
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            if (constructor.isAnnotationPresent(annotationClass)) {
                result.add(constructor);
            }
        }
        return result;
    }

    /**
     * Gets all annotated parameters in a method
     * 获取方法中所有被注解的参数
     *
     * @param method          the method | 方法
     * @param annotationClass the annotation class | 注解类
     * @return list of parameters | 参数列表
     */
    public static List<java.lang.reflect.Parameter> getAnnotatedParameters(Method method, Class<? extends Annotation> annotationClass) {
        List<java.lang.reflect.Parameter> result = new ArrayList<>();
        for (java.lang.reflect.Parameter param : method.getParameters()) {
            if (param.isAnnotationPresent(annotationClass)) {
                result.add(param);
            }
        }
        return result;
    }

    // ==================== Filtering | 过滤 ====================

    /**
     * Filters annotations by predicate
     * 按谓词过滤注解
     *
     * @param element   the annotated element | 被注解元素
     * @param predicate the predicate | 谓词
     * @return list of matching annotations | 匹配的注解列表
     */
    public static List<Annotation> filterAnnotations(AnnotatedElement element, Predicate<Annotation> predicate) {
        List<Annotation> result = new ArrayList<>();
        for (Annotation annotation : element.getAnnotations()) {
            if (predicate.test(annotation)) {
                result.add(annotation);
            }
        }
        return result;
    }

    /**
     * Gets annotations annotated with a specific meta-annotation
     * 获取被特定元注解标注的注解
     *
     * @param element        the annotated element | 被注解元素
     * @param metaAnnotation the meta-annotation class | 元注解类
     * @return list of annotations | 注解列表
     */
    public static List<Annotation> getAnnotationsWithMeta(AnnotatedElement element, Class<? extends Annotation> metaAnnotation) {
        List<Annotation> result = new ArrayList<>();
        for (Annotation annotation : element.getAnnotations()) {
            if (annotation.annotationType().isAnnotationPresent(metaAnnotation)) {
                result.add(annotation);
            }
        }
        return result;
    }
}
