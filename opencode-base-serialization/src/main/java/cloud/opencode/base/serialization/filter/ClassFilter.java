
package cloud.opencode.base.serialization.filter;

import java.util.Objects;

/**
 * ClassFilter - Deserialization Class Filter Interface
 * 反序列化类过滤器接口
 *
 * <p>A functional interface that determines whether a class is allowed for deserialization.
 * Provides combinator methods (and, or, negate) for building composite filter logic,
 * and static factory methods for common cases.</p>
 * <p>一个函数式接口，用于判断某个类是否允许反序列化。
 * 提供组合方法（and、or、negate）来构建复合过滤逻辑，
 * 以及静态工厂方法用于常见场景。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Functional interface for lambda usage - 函数式接口，支持 Lambda</li>
 *   <li>Combinator methods: and, or, negate - 组合方法：与、或、非</li>
 *   <li>Factory methods: allowAll, denyAll - 工厂方法：全部允许、全部拒绝</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Simple lambda filter
 * ClassFilter filter = className -> className.startsWith("com.myapp.");
 *
 * // Combine filters
 * ClassFilter combined = DefaultClassFilter.secure()
 *     .and(className -> className.startsWith("com.myapp."));
 *
 * // Use factory methods
 * ClassFilter noFilter = ClassFilter.allowAll();
 * ClassFilter blockAll = ClassFilter.denyAll();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Depends on implementation - 线程安全: 取决于实现</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see ClassFilterBuilder
 * @see DefaultClassFilter
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-serialization V1.0.3
 */
@FunctionalInterface
public interface ClassFilter {

    /**
     * Checks if the given class is allowed for deserialization.
     * 检查给定的类是否允许反序列化。
     *
     * @param className the fully qualified class name to check | 要检查的完全限定类名
     * @return {@code true} if the class is allowed, {@code false} otherwise |
     *         如果允许该类则返回 {@code true}，否则返回 {@code false}
     */
    boolean isAllowed(String className);

    /**
     * Returns a filter that allows all classes.
     * 返回允许所有类的过滤器。
     *
     * <p><strong>Warning:</strong> Using this filter disables all deserialization protection.
     * Only use in trusted environments.</p>
     * <p><strong>警告：</strong>使用此过滤器将禁用所有反序列化保护。仅在可信环境中使用。</p>
     *
     * @return a filter that allows all classes | 允许所有类的过滤器
     */
    static ClassFilter allowAll() {
        return className -> className != null;
    }

    /**
     * Returns a filter that denies all classes.
     * 返回拒绝所有类的过滤器。
     *
     * <p>Useful as a starting point for building allowlists.</p>
     * <p>可作为构建白名单的起点。</p>
     *
     * @return a filter that denies all classes | 拒绝所有类的过滤器
     */
    static ClassFilter denyAll() {
        return className -> false;
    }

    /**
     * Combines this filter with another using AND logic.
     * 使用 AND 逻辑组合此过滤器与另一个过滤器。
     *
     * <p>The resulting filter allows a class only if both this filter
     * and the other filter allow it.</p>
     * <p>结果过滤器仅在两个过滤器都允许时才允许一个类。</p>
     *
     * @param other the other filter to combine with | 要组合的另一个过滤器
     * @return a new filter that is the conjunction of both | 两者合取的新过滤器
     * @throws NullPointerException if other is null | 当 other 为 null 时抛出
     */
    default ClassFilter and(ClassFilter other) {
        Objects.requireNonNull(other, "other must not be null");
        return className -> this.isAllowed(className) && other.isAllowed(className);
    }

    /**
     * Combines this filter with another using OR logic.
     * 使用 OR 逻辑组合此过滤器与另一个过滤器。
     *
     * <p>The resulting filter allows a class if either this filter
     * or the other filter allows it.</p>
     * <p>结果过滤器在任一过滤器允许时即允许一个类。</p>
     *
     * @param other the other filter to combine with | 要组合的另一个过滤器
     * @return a new filter that is the disjunction of both | 两者析取的新过滤器
     * @throws NullPointerException if other is null | 当 other 为 null 时抛出
     */
    default ClassFilter or(ClassFilter other) {
        Objects.requireNonNull(other, "other must not be null");
        return className -> this.isAllowed(className) || other.isAllowed(className);
    }

    /**
     * Negates this filter.
     * 对此过滤器取反。
     *
     * <p>The resulting filter allows a class only if this filter denies it,
     * and vice versa.</p>
     * <p>结果过滤器仅在此过滤器拒绝时才允许，反之亦然。</p>
     *
     * @return a new filter that is the negation of this filter | 此过滤器取反的新过滤器
     */
    default ClassFilter negate() {
        return className -> !this.isAllowed(className);
    }
}
