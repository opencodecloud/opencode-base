package cloud.opencode.base.reflect.bean;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * BeanDiff - Before/after bean field change detection
 * Bean 差异 - Bean 字段前后变更检测
 *
 * <p>Computes the set of changed fields between two bean instances of the same type.
 * Useful for audit trails, change tracking, and conditional update generation.</p>
 * <p>计算同类型两个 Bean 实例之间已变更字段的集合。
 * 适用于审计追踪、变更跟踪和条件更新生成。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Field-level comparison with before/after values - 字段级比较，含前后值</li>
 *   <li>Skips static and final fields by default - 默认跳过 static 和 final 字段</li>
 *   <li>Configurable field filter via annotation or predicate - 可通过注解或谓词配置字段过滤</li>
 *   <li>Inheritance support — compares fields from all superclasses - 继承支持，比较所有父类字段</li>
 *   <li>Immutable result object safe to pass across layers - 不可变结果对象，可跨层安全传递</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Basic diff
 * BeanDiff.Result diff = BeanDiff.diff(before, after);
 *
 * // Diff with annotation exclusion (e.g., @Transient)
 * BeanDiff.Result diff = BeanDiff.diff(before, after, MyTransient.class);
 *
 * // Diff with custom field filter
 * BeanDiff.Result diff = BeanDiff.diff(before, after, f -> !f.getName().equals("password"));
 *
 * if (diff.hasChanges()) {
 *     auditLog.record(diff.changes());
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility, immutable result) - 线程安全: 是（无状态工具，不可变结果）</li>
 *   <li>Null-safe: No (caller must ensure non-null bean arguments) - 空值安全: 否（调用方须确保非空bean参数）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
public final class BeanDiff {

    private BeanDiff() {}

    /**
     * A single field change: the field name, before-value, and after-value.
     * 单个字段变更：字段名、变更前值和变更后值。
     *
     * @param field  the field name | 字段名
     * @param before the value before the change (may be null) | 变更前值（可为 null）
     * @param after  the value after the change (may be null) | 变更后值（可为 null）
     */
    public record Change(String field, Object before, Object after) {

        public boolean wasNull() {
            return before == null && after != null;
        }

        public boolean isNowNull() {
            return before != null && after == null;
        }

        @Override
        public String toString() {
            return field + ": " + before + " -> " + after;
        }
    }

    /**
     * The result of a diff operation, containing all detected field changes.
     * 差异操作的结果，包含所有检测到的字段变更。
     *
     * @param changes an ordered map of field-name to {@link Change} | 字段名到 {@link Change} 的有序映射
     */
    public record Result(Map<String, Change> changes) {

        public boolean hasChanges() {
            return !changes.isEmpty();
        }

        public Change get(String fieldName) {
            return changes.get(fieldName);
        }

        public int size() {
            return changes.size();
        }

        @Override
        public String toString() {
            if (changes.isEmpty()) return "BeanDiff.Result[no changes]";
            StringBuilder sb = new StringBuilder("BeanDiff.Result[");
            changes.values().forEach(c -> sb.append(c).append("; "));
            sb.setLength(sb.length() - 2);
            sb.append("]");
            return sb.toString();
        }
    }

    /**
     * Computes the diff between {@code before} and {@code after} beans.
     * No fields are excluded except static and final.
     * 计算两个 Bean 之间的差异。仅排除 static 和 final 字段。
     *
     * @param before the original bean | 原始 Bean
     * @param after  the modified bean | 修改后的 Bean
     * @param <T>    the bean type | Bean 类型
     * @return diff result | 差异结果
     */
    public static <T> Result diff(T before, T after) {
        return diff(before, after, _ -> true);
    }

    /**
     * Computes the diff, excluding fields annotated with the given annotation.
     * 计算差异，排除带指定注解的字段。
     *
     * @param before          the original bean | 原始 Bean
     * @param after           the modified bean | 修改后的 Bean
     * @param excludeAnnotation annotation class to exclude | 要排除的注解类
     * @param <T>             the bean type | Bean 类型
     * @return diff result | 差异结果
     */
    public static <T> Result diff(T before, T after, Class<? extends Annotation> excludeAnnotation) {
        return diff(before, after, f -> !f.isAnnotationPresent(excludeAnnotation));
    }

    /**
     * Computes the diff with a custom field filter.
     * 使用自定义字段过滤器计算差异。
     *
     * @param before      the original bean | 原始 Bean
     * @param after       the modified bean | 修改后的 Bean
     * @param fieldFilter predicate that returns {@code true} for fields to include | 返回 true 表示包含该字段
     * @param <T>         the bean type | Bean 类型
     * @return diff result | 差异结果
     */
    public static <T> Result diff(T before, T after, Predicate<Field> fieldFilter) {
        if (before == null && after == null) {
            return new Result(Collections.emptyMap());
        }
        if (before == null || after == null) {
            throw new IllegalArgumentException("Both before and after must be non-null, or both null");
        }
        if (!before.getClass().equals(after.getClass())) {
            throw new IllegalArgumentException(
                "Type mismatch: " + before.getClass().getName() + " vs " + after.getClass().getName());
        }

        Map<String, Change> changes = new LinkedHashMap<>();
        collectChanges(before.getClass(), before, after, changes, fieldFilter);
        return new Result(Collections.unmodifiableMap(changes));
    }

    private static void collectChanges(Class<?> type, Object before, Object after,
                                        Map<String, Change> changes, Predicate<Field> fieldFilter) {
        if (type == null || type == Object.class) return;

        collectChanges(type.getSuperclass(), before, after, changes, fieldFilter);

        for (Field field : type.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) continue;
            if (Modifier.isFinal(field.getModifiers())) continue;
            if (!fieldFilter.test(field)) continue;

            field.setAccessible(true);
            try {
                Object bVal = field.get(before);
                Object aVal = field.get(after);
                if (!Objects.equals(bVal, aVal)) {
                    changes.put(field.getName(), new Change(field.getName(), bVal, aVal));
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(
                    "Failed to access field '" + field.getName() + "' on " + type.getName(), e);
            }
        }
    }
}
