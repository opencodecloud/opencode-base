package cloud.opencode.base.deepclone;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Set;

/**
 * Functional interface for filtering fields during cloning
 * 克隆过程中过滤字段的函数式接口
 *
 * <p>Determines which fields should be included in the deep clone process.
 * Provides factory methods for common filtering patterns and supports
 * composition via {@link #and(FieldFilter)}, {@link #or(FieldFilter)},
 * and {@link #negate()}.</p>
 * <p>决定哪些字段应包含在深度克隆过程中。提供常见过滤模式的工厂方法，
 * 并支持通过 {@link #and(FieldFilter)}、{@link #or(FieldFilter)}
 * 和 {@link #negate()} 进行组合。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-deepclone V1.0.3
 */
@FunctionalInterface
public interface FieldFilter {

    /**
     * Tests whether the given field should be included in cloning
     * 测试给定字段是否应包含在克隆中
     *
     * @param field the field to test | 要测试的字段
     * @return true if the field should be cloned | 如果字段应被克隆返回true
     */
    boolean accept(Field field);

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates a filter that accepts all fields
     * 创建接受所有字段的过滤器
     *
     * @return a filter that always returns true | 总是返回true的过滤器
     */
    static FieldFilter acceptAll() {
        return field -> true;
    }

    /**
     * Creates a filter that excludes fields with the given names
     * 创建排除指定名称字段的过滤器
     *
     * @param names the field names to exclude | 要排除的字段名
     * @return the filter | 过滤器
     */
    static FieldFilter excludeNames(String... names) {
        Objects.requireNonNull(names, "names must not be null");
        Set<String> nameSet = Set.of(names);
        return field -> !nameSet.contains(field.getName());
    }

    /**
     * Creates a filter that only includes fields with the given names
     * 创建仅包含指定名称字段的过滤器
     *
     * @param names the field names to include | 要包含的字段名
     * @return the filter | 过滤器
     */
    static FieldFilter includeNames(String... names) {
        Objects.requireNonNull(names, "names must not be null");
        Set<String> nameSet = Set.of(names);
        return field -> nameSet.contains(field.getName());
    }

    /**
     * Creates a filter that excludes fields of the given types
     * 创建排除指定类型字段的过滤器
     *
     * @param types the field types to exclude | 要排除的字段类型
     * @return the filter | 过滤器
     */
    static FieldFilter excludeTypes(Class<?>... types) {
        Objects.requireNonNull(types, "types must not be null");
        Set<Class<?>> typeSet = Set.of(types);
        return field -> !typeSet.contains(field.getType());
    }

    /**
     * Creates a filter that excludes fields annotated with the given annotation
     * 创建排除具有指定注解字段的过滤器
     *
     * @param annotationType the annotation type | 注解类型
     * @return the filter | 过滤器
     */
    static FieldFilter excludeAnnotated(Class<? extends Annotation> annotationType) {
        Objects.requireNonNull(annotationType, "annotationType must not be null");
        return field -> !field.isAnnotationPresent(annotationType);
    }

    // ==================== Composition Methods | 组合方法 ====================

    /**
     * Returns a composed filter that represents a logical AND
     * 返回表示逻辑AND的组合过滤器
     *
     * @param other the other filter | 另一个过滤器
     * @return the composed filter | 组合过滤器
     */
    default FieldFilter and(FieldFilter other) {
        Objects.requireNonNull(other, "other must not be null");
        return field -> this.accept(field) && other.accept(field);
    }

    /**
     * Returns a composed filter that represents a logical OR
     * 返回表示逻辑OR的组合过滤器
     *
     * @param other the other filter | 另一个过滤器
     * @return the composed filter | 组合过滤器
     */
    default FieldFilter or(FieldFilter other) {
        Objects.requireNonNull(other, "other must not be null");
        return field -> this.accept(field) || other.accept(field);
    }

    /**
     * Returns a filter that represents the logical negation
     * 返回表示逻辑取反的过滤器
     *
     * @return the negated filter | 取反过滤器
     */
    default FieldFilter negate() {
        return field -> !this.accept(field);
    }
}
