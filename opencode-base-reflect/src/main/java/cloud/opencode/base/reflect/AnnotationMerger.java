package cloud.opencode.base.reflect;

import cloud.opencode.base.reflect.exception.OpenReflectException;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

/**
 * Annotation Merger for composed annotation attribute resolution
 * 注解合并器 - 组合注解属性解析
 *
 * <p>Resolves composed (meta-) annotation attributes, similar to Spring's
 * {@code AnnotatedElementUtils} but standalone and dependency-free.
 * When a composed annotation declares an attribute with the same name as
 * one in its meta-annotation, the composed value overrides the meta value.</p>
 * <p>解析组合（元）注解属性，类似于 Spring 的 {@code AnnotatedElementUtils}，
 * 但独立且无依赖。当组合注解声明了与其元注解同名的属性时，
 * 组合注解的值将覆盖元注解的值。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Given a composed annotation that meta-annotates @BaseAnnotation
 * @BaseAnnotation(priority = 10)
 * public @interface ComposedAnnotation {
 *     String value() default "composed";
 * }
 *
 * @ComposedAnnotation(value = "test")
 * class MyClass {}
 *
 * // Retrieve merged @BaseAnnotation with overridden attributes
 * BaseAnnotation merged = AnnotationMerger.getMergedAnnotation(
 *         MyClass.class, BaseAnnotation.class);
 * // merged.value() == "test"  (overridden by ComposedAnnotation)
 * // merged.priority() == 10   (from meta-annotation)
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Yes (null element returns null/empty) - 空值安全: 是（null 元素返回 null/空）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(d) where d is the meta-annotation depth - 时间复杂度: O(d)，d 为元注解深度</li>
 *   <li>Space complexity: O(a) where a is the number of annotation attributes - 空间复杂度: O(a)，a 为注解属性数量</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see AnnotationUtil
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.3
 */
public final class AnnotationMerger {

    /** Maximum recursion depth to prevent infinite loops in cyclic meta-annotations. */
    private static final int MAX_DEPTH = 8;

    private AnnotationMerger() {
    }

    // ==================== Public API | 公共接口 ====================

    /**
     * Gets a merged annotation, resolving composed annotation attribute overrides.
     * 获取合并后的注解，解析组合注解属性覆盖
     *
     * <p>If the annotation is directly present on the element, it is returned as-is.
     * If found via a meta-annotation, attributes with matching names from the
     * composed annotation override the meta-annotation defaults.</p>
     * <p>如果注解直接存在于元素上，则直接返回。如果通过元注解找到，
     * 则组合注解中同名的属性将覆盖元注解的默认值。</p>
     *
     * @param element        the annotated element | 被注解元素
     * @param annotationType the target annotation type | 目标注解类型
     * @param <A>            the annotation type | 注解类型
     * @return the merged annotation, or {@code null} if not found | 合并后的注解，未找到则返回 null
     */
    public static <A extends Annotation> A getMergedAnnotation(
            AnnotatedElement element, Class<A> annotationType) {
        if (element == null || annotationType == null) {
            return null;
        }

        // 1. Direct annotation - no merging needed
        A direct = element.getAnnotation(annotationType);
        if (direct != null) {
            return direct;
        }

        // 2. Search meta-annotations and merge (fresh visited set per top-level annotation)
        for (Annotation annotation : element.getAnnotations()) {
            Set<Class<? extends Annotation>> visited = new HashSet<>();
            MergeResult<A> result = findAndMerge(annotation, annotationType, visited, 0);
            if (result != null) {
                return result.synthesized();
            }
        }
        return null;
    }

    /**
     * Gets merged annotation attributes as a map.
     * 获取合并后的注解属性映射
     *
     * <p>Returns all attributes of the target annotation type with composed
     * annotation overrides applied. Returns {@code null} if the annotation
     * is not found on the element.</p>
     * <p>返回目标注解类型的所有属性，应用组合注解的覆盖。
     * 如果在元素上未找到注解，则返回 null。</p>
     *
     * @param element        the annotated element | 被注解元素
     * @param annotationType the target annotation type | 目标注解类型
     * @param <A>            the annotation type | 注解类型
     * @return map of attribute names to values, or {@code null} if not found | 属性名到值的映射，未找到则返回 null
     */
    public static <A extends Annotation> Map<String, Object> getMergedAttributes(
            AnnotatedElement element, Class<A> annotationType) {
        if (element == null || annotationType == null) {
            return null;
        }

        // 1. Direct annotation
        A direct = element.getAnnotation(annotationType);
        if (direct != null) {
            return extractAttributes(direct);
        }

        // 2. Search meta-annotations and collect merged attributes (fresh visited per top-level)
        for (Annotation annotation : element.getAnnotations()) {
            Set<Class<? extends Annotation>> visited = new HashSet<>();
            MergeResult<A> result = findAndMerge(annotation, annotationType, visited, 0);
            if (result != null) {
                return result.attributes();
            }
        }
        return null;
    }

    /**
     * Finds all merged annotations of a type, including from composed annotations.
     * 查找所有合并后的指定类型注解，包括来自组合注解的
     *
     * <p>Searches the element's annotations and their meta-annotation hierarchies
     * for all occurrences of the target annotation type.</p>
     * <p>搜索元素的注解及其元注解层次结构，查找目标注解类型的所有出现。</p>
     *
     * @param element        the annotated element | 被注解元素
     * @param annotationType the target annotation type | 目标注解类型
     * @param <A>            the annotation type | 注解类型
     * @return list of merged annotations (never null) | 合并后的注解列表（不为 null）
     */
    public static <A extends Annotation> List<A> findAllMergedAnnotations(
            AnnotatedElement element, Class<A> annotationType) {
        if (element == null || annotationType == null) {
            return List.of();
        }

        List<A> results = new ArrayList<>();

        // 1. Direct annotation
        A direct = element.getAnnotation(annotationType);
        if (direct != null) {
            results.add(direct);
        }

        // 2. Search meta-annotations (fresh visited per top-level)
        for (Annotation annotation : element.getAnnotations()) {
            Set<Class<? extends Annotation>> visited = new HashSet<>();
            collectMerged(annotation, annotationType, visited, results, 0);
        }

        return Collections.unmodifiableList(results);
    }

    /**
     * Checks if an annotation type is present, either directly or as a meta-annotation.
     * 检查注解类型是否存在（直接注解或元注解）
     *
     * @param element        the annotated element | 被注解元素
     * @param annotationType the annotation type to check | 要检查的注解类型
     * @return {@code true} if present | 如果存在返回 true
     */
    public static boolean isAnnotationPresent(
            AnnotatedElement element, Class<? extends Annotation> annotationType) {
        if (element == null || annotationType == null) {
            return false;
        }

        // Direct check
        if (element.isAnnotationPresent(annotationType)) {
            return true;
        }

        // Meta-annotation check (fresh visited per top-level)
        for (Annotation annotation : element.getAnnotations()) {
            Set<Class<? extends Annotation>> visited = new HashSet<>();
            if (isMetaPresent(annotation.annotationType(), annotationType, visited, 0)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Synthesizes an annotation instance from a map of attributes.
     * 从属性映射合成注解实例
     *
     * <p>Creates a dynamic proxy that implements the given annotation type,
     * returning attribute values from the provided map. If an attribute is
     * not in the map, its default value is used.</p>
     * <p>创建实现给定注解类型的动态代理，从提供的映射中返回属性值。
     * 如果属性不在映射中，则使用其默认值。</p>
     *
     * @param annotationType the annotation type to synthesize | 要合成的注解类型
     * @param attributes     the attribute values | 属性值
     * @param <A>            the annotation type | 注解类型
     * @return the synthesized annotation instance | 合成的注解实例
     * @throws OpenReflectException if synthesis fails | 如果合成失败
     */
    @SuppressWarnings("unchecked")
    public static <A extends Annotation> A synthesize(
            Class<A> annotationType, Map<String, Object> attributes) {
        Objects.requireNonNull(annotationType, "annotationType must not be null");
        Objects.requireNonNull(attributes, "attributes must not be null");
        if (!annotationType.isAnnotation()) {
            throw new OpenReflectException(
                    "Not an annotation type: " + annotationType.getName());
        }

        Map<String, Object> resolvedAttributes = new LinkedHashMap<>(attributes);

        // Fill in defaults for any missing attributes
        for (Method method : annotationType.getDeclaredMethods()) {
            if (method.getParameterCount() == 0 && !resolvedAttributes.containsKey(method.getName())) {
                Object defaultValue = method.getDefaultValue();
                if (defaultValue != null) {
                    resolvedAttributes.put(method.getName(), defaultValue);
                }
            }
        }

        InvocationHandler handler = new AnnotationInvocationHandler<>(annotationType, resolvedAttributes);

        try {
            return (A) Proxy.newProxyInstance(
                    annotationType.getClassLoader(),
                    new Class<?>[]{ annotationType },
                    handler);
        } catch (Exception e) {
            throw new OpenReflectException(
                    "Failed to synthesize annotation @" + annotationType.getSimpleName(), e);
        }
    }

    // ==================== Internal | 内部实现 ====================

    /**
     * Holds the result of a merge operation: the synthesized annotation and its attributes.
     */
    private record MergeResult<A extends Annotation>(A synthesized, Map<String, Object> attributes) {
    }

    /**
     * Recursively searches for and merges a target annotation from a composed annotation's
     * meta-annotation hierarchy.
     */
    private static <A extends Annotation> MergeResult<A> findAndMerge(
            Annotation composedAnnotation, Class<A> targetType,
            Set<Class<? extends Annotation>> visited, int depth) {

        Class<? extends Annotation> composedType = composedAnnotation.annotationType();

        if (!visited.add(composedType) || depth >= MAX_DEPTH) {
            return null;
        }

        // Check if the composed annotation is directly meta-annotated with the target
        A metaAnnotation = composedType.getAnnotation(targetType);
        if (metaAnnotation != null) {
            Map<String, Object> merged = mergeAttributes(metaAnnotation, composedAnnotation);
            A synthesized = synthesize(targetType, merged);
            return new MergeResult<>(synthesized, merged);
        }

        // Recurse into meta-annotations
        for (Annotation meta : composedType.getAnnotations()) {
            if (!meta.annotationType().getPackageName().startsWith("java.lang")) {
                MergeResult<A> result = findAndMerge(meta, targetType, visited, depth + 1);
                if (result != null) {
                    // Also overlay the outermost composed annotation's attributes
                    Map<String, Object> remerged = new LinkedHashMap<>(result.attributes());
                    overrideMatchingAttributes(remerged, composedAnnotation);
                    A synthesized = synthesize(targetType, remerged);
                    return new MergeResult<>(synthesized, remerged);
                }
            }
        }
        return null;
    }

    /**
     * Collects all merged annotations of the target type from the meta-annotation hierarchy.
     */
    private static <A extends Annotation> void collectMerged(
            Annotation composedAnnotation, Class<A> targetType,
            Set<Class<? extends Annotation>> visited, List<A> results, int depth) {

        Class<? extends Annotation> composedType = composedAnnotation.annotationType();

        if (!visited.add(composedType) || depth >= MAX_DEPTH) {
            return;
        }

        // Check if the composed annotation is directly meta-annotated with the target
        A metaAnnotation = composedType.getAnnotation(targetType);
        if (metaAnnotation != null) {
            Map<String, Object> merged = mergeAttributes(metaAnnotation, composedAnnotation);
            results.add(synthesize(targetType, merged));
        }

        // Recurse into meta-annotations
        for (Annotation meta : composedType.getAnnotations()) {
            if (!meta.annotationType().getPackageName().startsWith("java.lang")) {
                collectMerged(meta, targetType, visited, results, depth + 1);
            }
        }
    }

    /**
     * Checks recursively if a target annotation type is present in the meta-annotation hierarchy.
     */
    private static boolean isMetaPresent(
            Class<? extends Annotation> currentType, Class<? extends Annotation> targetType,
            Set<Class<? extends Annotation>> visited, int depth) {

        if (!visited.add(currentType) || depth >= MAX_DEPTH) {
            return false;
        }

        if (currentType.isAnnotationPresent(targetType)) {
            return true;
        }

        for (Annotation meta : currentType.getAnnotations()) {
            if (!meta.annotationType().getPackageName().startsWith("java.lang")) {
                if (isMetaPresent(meta.annotationType(), targetType, visited, depth + 1)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Merges attributes: starts with the meta-annotation's attribute values,
     * then overrides with matching attributes from the composed annotation.
     */
    private static Map<String, Object> mergeAttributes(
            Annotation metaAnnotation, Annotation composedAnnotation) {

        Map<String, Object> merged = extractAttributes(metaAnnotation);
        overrideMatchingAttributes(merged, composedAnnotation);
        return merged;
    }

    /**
     * Overrides entries in the target map with attribute values from the composed annotation
     * where the attribute name matches and the composed value differs from its default.
     */
    private static void overrideMatchingAttributes(
            Map<String, Object> target, Annotation composedAnnotation) {

        Class<? extends Annotation> composedType = composedAnnotation.annotationType();

        for (Method method : composedType.getDeclaredMethods()) {
            if (method.getParameterCount() != 0) {
                continue;
            }
            String name = method.getName();
            if (!target.containsKey(name)) {
                continue;
            }
            try {
                Object composedValue = method.invoke(composedAnnotation);
                Object defaultValue = method.getDefaultValue();

                // Override only if the composed annotation explicitly set the value
                // (i.e., the attribute has a default and the composed value differs from it)
                if (defaultValue != null && !attributeEquals(composedValue, defaultValue)) {
                    target.put(name, composedValue);
                } else if (defaultValue == null) {
                    // Required attribute (no default) — always override
                    target.put(name, composedValue);
                }
            } catch (Exception ignored) {
                // Skip attributes that cannot be read
            }
        }
    }

    /**
     * Extracts all attributes from an annotation into a map.
     */
    private static Map<String, Object> extractAttributes(Annotation annotation) {
        Map<String, Object> attributes = new LinkedHashMap<>();
        for (Method method : annotation.annotationType().getDeclaredMethods()) {
            if (method.getParameterCount() == 0) {
                try {
                    attributes.put(method.getName(), method.invoke(annotation));
                } catch (Exception ignored) {
                    // Skip attributes that cannot be read
                }
            }
        }
        return attributes;
    }

    /**
     * Compares two attribute values for equality, handling arrays correctly.
     */
    private static boolean attributeEquals(Object a, Object b) {
        if (Objects.equals(a, b)) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        if (a.getClass().isArray() && b.getClass().isArray()) {
            return Arrays.deepEquals(new Object[]{ a }, new Object[]{ b });
        }
        return false;
    }

    // ==================== Annotation Proxy Handler | 注解代理处理器 ====================

    /**
     * InvocationHandler for synthesized annotation instances.
     * 合成注解实例的调用处理器
     */
    private static final class AnnotationInvocationHandler<A extends Annotation> implements InvocationHandler {

        private final Class<A> annotationType;
        private final Map<String, Object> attributes;

        AnnotationInvocationHandler(Class<A> annotationType, Map<String, Object> attributes) {
            this.annotationType = annotationType;
            this.attributes = Map.copyOf(attributes);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            String name = method.getName();

            return switch (name) {
                case "annotationType" -> annotationType;
                case "toString" -> toAnnotationString();
                case "hashCode" -> computeHashCode();
                case "equals" -> args != null && args.length > 0 && isEqual(args[0]);
                default -> attributes.getOrDefault(name, method.getDefaultValue());
            };
        }

        private String toAnnotationString() {
            StringJoiner joiner = new StringJoiner(", ", "@" + annotationType.getSimpleName() + "(", ")");
            attributes.forEach((k, v) -> {
                String valueStr = v != null && v.getClass().isArray()
                        ? Arrays.deepToString(new Object[]{ v })
                        : String.valueOf(v);
                joiner.add(k + "=" + valueStr);
            });
            return joiner.toString();
        }

        private int computeHashCode() {
            int result = 0;
            for (Method method : annotationType.getDeclaredMethods()) {
                if (method.getParameterCount() != 0) {
                    continue;
                }
                String name = method.getName();
                Object value = attributes.get(name);
                if (value == null) {
                    continue;
                }
                int memberHash = (127 * name.hashCode()) ^ memberValueHashCode(value);
                result += memberHash;
            }
            return result;
        }

        private int memberValueHashCode(Object value) {
            if (value.getClass().isArray()) {
                return switch (value) {
                    case boolean[] a -> Arrays.hashCode(a);
                    case byte[] a -> Arrays.hashCode(a);
                    case char[] a -> Arrays.hashCode(a);
                    case double[] a -> Arrays.hashCode(a);
                    case float[] a -> Arrays.hashCode(a);
                    case int[] a -> Arrays.hashCode(a);
                    case long[] a -> Arrays.hashCode(a);
                    case short[] a -> Arrays.hashCode(a);
                    default -> Arrays.hashCode((Object[]) value);
                };
            }
            return value.hashCode();
        }

        private boolean isEqual(Object other) {
            if (!annotationType.isInstance(other)) {
                return false;
            }
            for (Method method : annotationType.getDeclaredMethods()) {
                if (method.getParameterCount() != 0) {
                    continue;
                }
                try {
                    Object thisValue = attributes.containsKey(method.getName())
                            ? attributes.get(method.getName())
                            : method.getDefaultValue();
                    Object otherValue = method.invoke(other);
                    if (!attributeEquals(thisValue, otherValue)) {
                        return false;
                    }
                } catch (Exception e) {
                    return false;
                }
            }
            return true;
        }
    }
}
