package cloud.opencode.base.core.page;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Sort - Sort specification for queries
 * 排序 - 查询排序规范
 *
 * <p>Represents an ordered collection of sort orders, each consisting of a property name
 * and a direction (ASC or DESC). Compatible with Spring Data semantics while having no
 * dependency on Spring Data Commons.</p>
 * <p>表示有序的排序规范集合，每项包含属性名和方向（ASC 或 DESC）。
 * 与 Spring Data 语义兼容，但不依赖 Spring Data Commons。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fluent builder API for composing sort orders - 流式构建 API 组合排序规范</li>
 *   <li>ASC / DESC direction with null handling - 支持 ASC / DESC 及 null 处理</li>
 *   <li>Immutable value object — safe to share and cache - 不可变值对象，可安全共享和缓存</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Sort sort = Sort.by("name").and(Sort.by(Sort.Direction.DESC, "createdAt"));
 * Sort descAge = Sort.by(Sort.Direction.DESC, "age");
 * List<Sort.Order> orders = sort.getOrders();
 * String sql = "ORDER BY " + sort.toSql();  // name ASC, created_at DESC
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public final class Sort {

    /**
     * Sort direction.
     * 排序方向。
     */
    public enum Direction {
        /** Ascending order | 升序 */
        ASC,
        /** Descending order | 降序 */
        DESC;

        /**
         * Returns the opposite direction.
         * 返回相反方向。
         *
         * @return the opposite direction | 相反方向
         */
        public Direction reverse() {
            return this == ASC ? DESC : ASC;
        }

        /**
         * Returns {@code true} if this is ascending.
         * 如果为升序则返回 {@code true}。
         */
        public boolean isAscending() {
            return this == ASC;
        }
    }

    /**
     * A single sort order: property + direction.
     * 单个排序规范：属性 + 方向。
     *
     * @param property  the property or column name | 属性或列名
     * @param direction the sort direction | 排序方向
     */
    public record Order(String property, Direction direction) {

        private static final java.util.regex.Pattern SAFE_PROPERTY =
                java.util.regex.Pattern.compile("[a-zA-Z_][a-zA-Z0-9_.]*");

        public Order {
            java.util.Objects.requireNonNull(property, "property must not be null");
            java.util.Objects.requireNonNull(direction, "direction must not be null");
            if (property.isBlank()) {
                throw new IllegalArgumentException("property must not be blank");
            }
        }

        public static Order asc(String property) {
            return new Order(property, Direction.ASC);
        }

        public static Order desc(String property) {
            return new Order(property, Direction.DESC);
        }

        public Order reversed() {
            return new Order(property, direction.reverse());
        }

        /**
         * Renders this order as a SQL fragment, e.g. {@code name ASC}.
         * 渲染为 SQL 片段，例如 {@code name ASC}。
         *
         * @return SQL fragment | SQL 片段
         */
        public String toSql() {
            if (!SAFE_PROPERTY.matcher(property).matches()) {
                throw new IllegalArgumentException(
                        "Unsafe property name for SQL: " + property);
            }
            return property + " " + direction.name();
        }
    }

    /** Unsorted singleton. 无排序单例。 */
    public static final Sort UNSORTED = new Sort(Collections.emptyList());

    private final List<Order> orders;

    private Sort(List<Order> orders) {
        this.orders = Collections.unmodifiableList(new ArrayList<>(orders));
    }

    // ---- Static factories ----

    public static Sort by(String property) {
        return new Sort(List.of(Order.asc(property)));
    }

    public static Sort by(Direction direction, String property) {
        return new Sort(List.of(new Order(property, direction)));
    }

    public static Sort by(Direction direction, String... properties) {
        List<Order> list = new ArrayList<>(properties.length);
        for (String p : properties) {
            list.add(new Order(p, direction));
        }
        return new Sort(list);
    }

    public static Sort by(Order... orders) {
        return new Sort(Arrays.asList(orders));
    }

    public static Sort by(List<Order> orders) {
        return new Sort(orders);
    }

    public static Sort unsorted() {
        return UNSORTED;
    }

    // ---- Instance API ----

    public List<Order> getOrders() {
        return orders;
    }

    public boolean isUnsorted() {
        return orders.isEmpty();
    }

    public Sort and(Sort other) {
        List<Order> merged = new ArrayList<>(orders.size() + other.orders.size());
        merged.addAll(orders);
        merged.addAll(other.orders);
        return new Sort(merged);
    }

    /**
     * Renders this Sort as a SQL ORDER BY fragment (without the {@code ORDER BY} keyword).
     * 渲染为 SQL ORDER BY 片段（不含 {@code ORDER BY} 关键字）。
     *
     * @return SQL fragment, or empty string if unsorted | SQL 片段，无排序时为空字符串
     */
    public String toSql() {
        if (orders.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < orders.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(orders.get(i).toSql());
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return isUnsorted() ? "Sort.UNSORTED" : "Sort[" + toSql() + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Sort other)) return false;
        return orders.equals(other.orders);
    }

    @Override
    public int hashCode() {
        return orders.hashCode();
    }
}
