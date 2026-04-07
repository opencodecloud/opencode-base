
package cloud.opencode.base.json;

import java.util.Objects;
import java.util.Set;

/**
 * Property Filter - Dynamic Property Filtering for JSON Serialization
 * 属性过滤器 - JSON 序列化的动态属性过滤
 *
 * <p>This interface provides dynamic filtering of properties during JSON
 * serialization. It supports {@code @JsonFilter} style filtering where
 * properties can be included or excluded based on name, value, or
 * declaring class.</p>
 * <p>此接口提供 JSON 序列化期间的动态属性过滤。它支持 {@code @JsonFilter}
 * 风格的过滤，其中属性可以根据名称、值或声明类进行包含或排除。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * // Include only specific properties
 * PropertyFilter filter = PropertyFilter.include("name", "email");
 *
 * // Exclude specific properties
 * PropertyFilter filter = PropertyFilter.exclude("password", "secret");
 *
 * // Exclude null values
 * PropertyFilter filter = PropertyFilter.includeNonNull();
 *
 * // Custom filter
 * PropertyFilter filter = (name, value, clazz) ->
 *     !name.startsWith("_");
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Dynamic property inclusion/exclusion - 动态属性包含/排除</li>
 *   <li>Static factory methods for common filters - 常见过滤器的静态工厂方法</li>
 *   <li>Composable with lambda expressions - 可通过 lambda 表达式组合</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless or immutable) - 线程安全: 是（无状态或不可变）</li>
 *   <li>Null-safe: Handles null values gracefully - 空值安全: 优雅处理 null 值</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
@FunctionalInterface
public interface PropertyFilter {

    /**
     * Determines whether a property should be included in the serialized output.
     * 确定属性是否应包含在序列化输出中。
     *
     * @param propertyName   the property name - 属性名称
     * @param value          the property value (may be null) - 属性值（可能为 null）
     * @param declaringClass the class that declares the property - 声明该属性的类
     * @return true if the property should be included - 如果应包含该属性则返回 true
     */
    boolean includeProperty(String propertyName, Object value, Class<?> declaringClass);

    // ==================== Static Factory Methods ====================

    /**
     * Returns a filter that includes all properties.
     * 返回包含所有属性的过滤器。
     *
     * @return a filter that always returns true - 始终返回 true 的过滤器
     */
    static PropertyFilter includeAll() {
        return (name, value, clazz) -> true;
    }

    /**
     * Returns a filter that excludes all properties.
     * 返回排除所有属性的过滤器。
     *
     * @return a filter that always returns false - 始终返回 false 的过滤器
     */
    static PropertyFilter excludeAll() {
        return (name, value, clazz) -> false;
    }

    /**
     * Returns a filter that includes only the specified properties (whitelist).
     * 返回仅包含指定属性的过滤器（白名单）。
     *
     * @param properties the property names to include - 要包含的属性名称
     * @return a whitelist filter - 白名单过滤器
     * @throws NullPointerException if properties is null - 如果 properties 为 null
     */
    static PropertyFilter include(String... properties) {
        Objects.requireNonNull(properties, "Properties must not be null");
        Set<String> allowed = Set.of(properties);
        return (name, value, clazz) -> allowed.contains(name);
    }

    /**
     * Returns a filter that excludes the specified properties (blacklist).
     * 返回排除指定属性的过滤器（黑名单）。
     *
     * @param properties the property names to exclude - 要排除的属性名称
     * @return a blacklist filter - 黑名单过滤器
     * @throws NullPointerException if properties is null - 如果 properties 为 null
     */
    static PropertyFilter exclude(String... properties) {
        Objects.requireNonNull(properties, "Properties must not be null");
        Set<String> excluded = Set.of(properties);
        return (name, value, clazz) -> !excluded.contains(name);
    }

    /**
     * Returns a filter that excludes properties with null values.
     * 返回排除 null 值属性的过滤器。
     *
     * @return a non-null filter - 非空过滤器
     */
    static PropertyFilter includeNonNull() {
        return (name, value, clazz) -> value != null;
    }
}
