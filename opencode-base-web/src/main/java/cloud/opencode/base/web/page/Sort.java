package cloud.opencode.base.web.page;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Sort
 * 排序
 *
 * <p>Represents sorting criteria for queries.</p>
 * <p>表示查询的排序条件。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Multi-property sort support - 多属性排序支持</li>
 *   <li>SQL ORDER BY clause generation - SQL ORDER BY 子句生成</li>
 *   <li>Sort string parsing - 排序字符串解析</li>
 *   <li>SQL injection prevention via identifier validation - 通过标识符验证防止 SQL 注入</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Simple sort
 * Sort sort = Sort.asc("name");
 * Sort sort = Sort.desc("createdAt");
 *
 * // Multi-property sort
 * Sort sort = Sort.asc("name").andDesc("createdAt");
 *
 * // Parse sort string
 * Sort sort = Sort.parse("name,asc;age,desc");
 *
 * // Generate SQL
 * String sql = sort.toSql(); // "name ASC, age DESC"
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: Yes (null orders default to empty list) - 空值安全: 是（null 排序默认为空列表）</li>
 *   <li>SQL injection safe: Yes (identifier validation and keyword blocking) - SQL 注入安全: 是（标识符验证和关键字阻止）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(k) for parse() and toSql() where k is the number of sort orders - 时间复杂度: O(k)，k 为排序条件数 - parse() 和 toSql() 均线性遍历排序条件</li>
 *   <li>Space complexity: O(k) - order list proportional to the number of sort criteria - 空间复杂度: O(k) - 排序条件列表与条件数成正比</li>
 * </ul>
 *
 * @param orders the sort orders | 排序顺序列表
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
public record Sort(List<Order> orders) {

    /**
     * Pattern for valid SQL identifiers (alphanumeric, underscores, dots for qualified names)
     * 有效SQL标识符的模式（字母数字、下划线、点用于限定名称）
     */
    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)*$");

    /**
     * Maximum identifier length to prevent DoS
     * 最大标识符长度以防止DoS
     */
    private static final int MAX_IDENTIFIER_LENGTH = 128;

    /**
     * SQL keywords that should not be used as identifiers (security protection)
     * 不应作为标识符使用的SQL关键字（安全保护）
     */
    private static final Set<String> SQL_KEYWORDS = Set.of(
            "SELECT", "INSERT", "UPDATE", "DELETE", "DROP", "CREATE", "ALTER", "TRUNCATE",
            "UNION", "JOIN", "WHERE", "FROM", "INTO", "VALUES", "SET", "AND", "OR", "NOT",
            "NULL", "TRUE", "FALSE", "EXEC", "EXECUTE", "XP_", "SP_", "GRANT", "REVOKE",
            "COMMIT", "ROLLBACK", "SAVEPOINT", "DECLARE", "CURSOR", "FETCH", "OPEN", "CLOSE"
    );

    /**
     * Compact constructor
     * 紧凑构造函数
     */
    public Sort {
        orders = orders != null ? List.copyOf(orders) : List.of();
    }

    /**
     * Validates a SQL identifier to prevent SQL injection.
     * 验证SQL标识符以防止SQL注入。
     *
     * @param identifier the identifier to validate | 要验证的标识符
     * @throws IllegalArgumentException if the identifier is invalid | 如果标识符无效
     */
    private static void validateIdentifier(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            throw new IllegalArgumentException("SQL identifier cannot be null or blank");
        }
        if (identifier.length() > MAX_IDENTIFIER_LENGTH) {
            throw new IllegalArgumentException("SQL identifier exceeds maximum length: " + MAX_IDENTIFIER_LENGTH);
        }
        if (!IDENTIFIER_PATTERN.matcher(identifier).matches()) {
            throw new IllegalArgumentException("Invalid SQL identifier: " + identifier);
        }
        // Check each part of qualified name against SQL keywords
        for (String part : identifier.toUpperCase().split("\\.")) {
            if (SQL_KEYWORDS.contains(part)) {
                throw new IllegalArgumentException("SQL keyword cannot be used as identifier: " + part);
            }
        }
    }

    /**
     * Sort Order
     * 排序顺序
     *
     * @param property the property to sort by | 排序属性
     * @param direction the sort direction | 排序方向
     */
    public record Order(String property, Direction direction) {

        /**
         * Compact constructor with validation
         * 带验证的紧凑构造函数
         */
        public Order {
            validateIdentifier(property);
            if (direction == null) {
                direction = Direction.ASC;
            }
        }

        /**
         * Create ascending order
         * 创建升序
         *
         * @param property the property | 属性
         * @return the order | 排序
         */
        public static Order asc(String property) {
            return new Order(property, Direction.ASC);
        }

        /**
         * Create descending order
         * 创建降序
         *
         * @param property the property | 属性
         * @return the order | 排序
         */
        public static Order desc(String property) {
            return new Order(property, Direction.DESC);
        }

        /**
         * Check if ascending
         * 检查是否升序
         *
         * @return true if ascending | 如果升序返回true
         */
        public boolean isAscending() {
            return direction == Direction.ASC;
        }

        /**
         * Check if descending
         * 检查是否降序
         *
         * @return true if descending | 如果降序返回true
         */
        public boolean isDescending() {
            return direction == Direction.DESC;
        }
    }

    /**
     * Sort Direction
     * 排序方向
     */
    public enum Direction {
        ASC, DESC
    }

    /**
     * Create unsorted
     * 创建无排序
     *
     * @return the sort | 排序
     */
    public static Sort unsorted() {
        return new Sort(List.of());
    }

    /**
     * Create sort by property ascending
     * 按属性升序创建排序
     *
     * @param property the property | 属性
     * @return the sort | 排序
     */
    public static Sort asc(String property) {
        return new Sort(List.of(Order.asc(property)));
    }

    /**
     * Create sort by property descending
     * 按属性降序创建排序
     *
     * @param property the property | 属性
     * @return the sort | 排序
     */
    public static Sort desc(String property) {
        return new Sort(List.of(Order.desc(property)));
    }

    /**
     * Create sort by multiple properties
     * 按多个属性创建排序
     *
     * @param orders the orders | 排序列表
     * @return the sort | 排序
     */
    public static Sort by(Order... orders) {
        return new Sort(List.of(orders));
    }

    /**
     * Create sort by multiple properties
     * 按多个属性创建排序
     *
     * @param orders the orders | 排序列表
     * @return the sort | 排序
     */
    public static Sort by(List<Order> orders) {
        return new Sort(orders);
    }

    /**
     * Parse sort string
     * 解析排序字符串
     *
     * <p>Format: "property1,asc;property2,desc"</p>
     *
     * @param sortString the sort string | 排序字符串
     * @return the sort | 排序
     */
    public static Sort parse(String sortString) {
        if (sortString == null || sortString.isBlank()) {
            return unsorted();
        }

        List<Order> orders = new ArrayList<>();
        String[] parts = sortString.split(";");
        for (String part : parts) {
            String[] pair = part.trim().split(",");
            if (pair.length >= 1) {
                String property = pair[0].trim();
                Direction direction = Direction.ASC;
                if (pair.length >= 2 && "desc".equalsIgnoreCase(pair[1].trim())) {
                    direction = Direction.DESC;
                }
                orders.add(new Order(property, direction));
            }
        }
        return new Sort(orders);
    }

    /**
     * Add and return new sort
     * 添加并返回新排序
     *
     * @param order the order to add | 要添加的排序
     * @return the new sort | 新排序
     */
    public Sort and(Order order) {
        List<Order> newOrders = new ArrayList<>(orders);
        newOrders.add(order);
        return new Sort(newOrders);
    }

    /**
     * Add ascending sort
     * 添加升序
     *
     * @param property the property | 属性
     * @return the new sort | 新排序
     */
    public Sort andAsc(String property) {
        return and(Order.asc(property));
    }

    /**
     * Add descending sort
     * 添加降序
     *
     * @param property the property | 属性
     * @return the new sort | 新排序
     */
    public Sort andDesc(String property) {
        return and(Order.desc(property));
    }

    /**
     * Check if sorted
     * 检查是否有排序
     *
     * @return true if sorted | 如果有排序返回true
     */
    public boolean isSorted() {
        return !orders.isEmpty();
    }

    /**
     * Check if unsorted
     * 检查是否无排序
     *
     * @return true if unsorted | 如果无排序返回true
     */
    public boolean isUnsorted() {
        return orders.isEmpty();
    }

    /**
     * Get first order
     * 获取第一个排序
     *
     * @return the first order or null | 第一个排序或null
     */
    public Order getFirst() {
        return orders.isEmpty() ? null : orders.getFirst();
    }

    /**
     * Convert to SQL ORDER BY clause
     * 转换为SQL ORDER BY子句
     *
     * @return the SQL clause | SQL子句
     */
    public String toSql() {
        if (orders.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < orders.size(); i++) {
            if (i > 0) sb.append(", ");
            Order order = orders.get(i);
            // Defense in depth: validate even though Order constructor validates
            validateIdentifier(order.property());
            sb.append(order.property()).append(" ").append(order.direction().name());
        }
        return sb.toString();
    }
}
