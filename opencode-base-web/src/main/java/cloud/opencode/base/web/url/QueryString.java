package cloud.opencode.base.web.url;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Query String - URL Query String Parser/Builder
 * 查询字符串 - URL 查询字符串解析器/构建器
 *
 * <p>This class handles URL query string parsing and construction with
 * support for multi-value parameters.</p>
 * <p>此类处理 URL 查询字符串的解析和构建，支持多值参数。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * // Parse query string
 * QueryString qs = QueryString.parse("name=John&age=30");
 * String name = qs.get("name"); // "John"
 *
 * // Build query string
 * String query = QueryString.builder()
 *     .add("name", "John")
 *     .add("age", "30")
 *     .build()
 *     .toString(); // "name=John&age=30"
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Query string parsing and building - 查询字符串解析和构建</li>
 *   <li>Multi-value parameter support - 多值参数支持</li>
 *   <li>Immutable with functional modification - 不可变带函数式修改</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * QueryString qs = QueryString.parse("name=John&age=30");
 * String name = qs.get("name");
 * QueryString added = qs.with("page", "1");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 否</li>
 *   <li>Null-safe: Yes (parse handles null) - 是（parse处理null）</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
public final class QueryString {

    private final Map<String, List<String>> params;

    private QueryString(Map<String, List<String>> params) {
        this.params = params;
    }

    // ==================== Factory Methods ====================

    /**
     * Creates an empty query string.
     * 创建空的查询字符串。
     *
     * @return the query string - 查询字符串
     */
    public static QueryString empty() {
        return new QueryString(new LinkedHashMap<>());
    }

    /**
     * Parses a query string.
     * 解析查询字符串。
     *
     * @param queryString the query string (without leading '?') - 查询字符串
     * @return the parsed query string - 解析的查询字符串
     */
    public static QueryString parse(String queryString) {
        Map<String, List<String>> params = new LinkedHashMap<>();

        if (queryString == null || queryString.isEmpty()) {
            return new QueryString(params);
        }

        // Remove leading ? if present
        if (queryString.startsWith("?")) {
            queryString = queryString.substring(1);
        }

        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            if (pair.isEmpty()) {
                continue;
            }
            int eqIndex = pair.indexOf('=');
            String key;
            String value;
            if (eqIndex >= 0) {
                key = decode(pair.substring(0, eqIndex));
                value = decode(pair.substring(eqIndex + 1));
            } else {
                key = decode(pair);
                value = "";
            }
            params.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
        }

        return new QueryString(params);
    }

    /**
     * Creates from a map.
     * 从 Map 创建。
     *
     * @param map the parameter map - 参数 Map
     * @return the query string - 查询字符串
     */
    public static QueryString of(Map<String, String> map) {
        Map<String, List<String>> params = new LinkedHashMap<>();
        map.forEach((k, v) -> params.put(k, new ArrayList<>(List.of(v))));
        return new QueryString(params);
    }

    /**
     * Creates a builder.
     * 创建构建器。
     *
     * @return the builder - 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    // ==================== Getters ====================

    /**
     * Gets a parameter value.
     * 获取参数值。
     *
     * @param name the parameter name - 参数名
     * @return the value or null - 值或 null
     */
    public String get(String name) {
        List<String> values = params.get(name);
        return values != null && !values.isEmpty() ? values.get(0) : null;
    }

    /**
     * Gets a parameter value with default.
     * 获取参数值（带默认值）。
     *
     * @param name         the parameter name - 参数名
     * @param defaultValue the default value - 默认值
     * @return the value or default - 值或默认值
     */
    public String get(String name, String defaultValue) {
        String value = get(name);
        return value != null ? value : defaultValue;
    }

    /**
     * Gets all values for a parameter.
     * 获取参数的所有值。
     *
     * @param name the parameter name - 参数名
     * @return the values - 值列表
     */
    public List<String> getAll(String name) {
        List<String> values = params.get(name);
        return values != null ? List.copyOf(values) : List.of();
    }

    /**
     * Checks if parameter exists.
     * 检查参数是否存在。
     *
     * @param name the parameter name - 参数名
     * @return true if exists - 如果存在返回 true
     */
    public boolean has(String name) {
        return params.containsKey(name);
    }

    /**
     * Gets all parameter names.
     * 获取所有参数名。
     *
     * @return the parameter names - 参数名集合
     */
    public Set<String> names() {
        return Collections.unmodifiableSet(params.keySet());
    }

    /**
     * Gets the number of parameters.
     * 获取参数数量。
     *
     * @return the size - 大小
     */
    public int size() {
        return params.size();
    }

    /**
     * Checks if empty.
     * 检查是否为空。
     *
     * @return true if empty - 如果为空返回 true
     */
    public boolean isEmpty() {
        return params.isEmpty();
    }

    /**
     * Converts to map (first value only).
     * 转换为 Map（仅第一个值）。
     *
     * @return the map - Map
     */
    public Map<String, String> toMap() {
        Map<String, String> map = new LinkedHashMap<>();
        params.forEach((k, v) -> {
            if (!v.isEmpty()) {
                map.put(k, v.get(0));
            }
        });
        return map;
    }

    /**
     * Converts to multi-value map.
     * 转换为多值 Map。
     *
     * @return the multi-value map - 多值 Map
     */
    public Map<String, List<String>> toMultiMap() {
        Map<String, List<String>> map = new LinkedHashMap<>();
        params.forEach((k, v) -> map.put(k, List.copyOf(v)));
        return map;
    }

    // ==================== Modification ====================

    /**
     * Creates a new query string with added parameter.
     * 创建添加参数后的新查询字符串。
     *
     * @param name  the parameter name - 参数名
     * @param value the parameter value - 参数值
     * @return the new query string - 新查询字符串
     */
    public QueryString with(String name, String value) {
        Map<String, List<String>> newParams = new LinkedHashMap<>(params);
        newParams.computeIfAbsent(name, k -> new ArrayList<>()).add(value);
        return new QueryString(newParams);
    }

    /**
     * Creates a new query string without specified parameter.
     * 创建删除指定参数后的新查询字符串。
     *
     * @param name the parameter name - 参数名
     * @return the new query string - 新查询字符串
     */
    public QueryString without(String name) {
        Map<String, List<String>> newParams = new LinkedHashMap<>(params);
        newParams.remove(name);
        return new QueryString(newParams);
    }

    // ==================== Encoding/Decoding ====================

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    // ==================== Output ====================

    /**
     * Returns the encoded query string.
     * 返回编码的查询字符串。
     *
     * @return the query string - 查询字符串
     */
    @Override
    public String toString() {
        if (params.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder(64);
        boolean first = true;
        for (Map.Entry<String, List<String>> entry : params.entrySet()) {
            String encodedName = encode(entry.getKey());
            for (String value : entry.getValue()) {
                if (!first) sb.append('&');
                first = false;
                sb.append(encodedName).append('=').append(encode(value));
            }
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QueryString that)) return false;
        return params.equals(that.params);
    }

    @Override
    public int hashCode() {
        return params.hashCode();
    }

    // ==================== Builder ====================

    /**
     * Query string builder.
     * 查询字符串构建器。
     */
    public static final class Builder {
        private final Map<String, List<String>> params = new LinkedHashMap<>();

        private Builder() {
        }

        /**
         * Adds a parameter.
         * 添加参数。
         *
         * @param name  the parameter name - 参数名
         * @param value the parameter value - 参数值
         * @return this builder - 此构建器
         */
        public Builder add(String name, String value) {
            params.computeIfAbsent(name, k -> new ArrayList<>()).add(value != null ? value : "");
            return this;
        }

        /**
         * Adds a parameter if value is not null.
         * 如果值不为 null，添加参数。
         *
         * @param name  the parameter name - 参数名
         * @param value the parameter value - 参数值
         * @return this builder - 此构建器
         */
        public Builder addIfNotNull(String name, String value) {
            if (value != null) {
                add(name, value);
            }
            return this;
        }

        /**
         * Adds a parameter if value is not empty.
         * 如果值不为空，添加参数。
         *
         * @param name  the parameter name - 参数名
         * @param value the parameter value - 参数值
         * @return this builder - 此构建器
         */
        public Builder addIfNotEmpty(String name, String value) {
            if (value != null && !value.isEmpty()) {
                add(name, value);
            }
            return this;
        }

        /**
         * Sets a parameter (replaces existing).
         * 设置参数（替换现有的）。
         *
         * @param name  the parameter name - 参数名
         * @param value the parameter value - 参数值
         * @return this builder - 此构建器
         */
        public Builder set(String name, String value) {
            params.put(name, new ArrayList<>(List.of(value != null ? value : "")));
            return this;
        }

        /**
         * Adds all parameters from map.
         * 从 Map 添加所有参数。
         *
         * @param map the parameters - 参数
         * @return this builder - 此构建器
         */
        public Builder addAll(Map<String, String> map) {
            map.forEach(this::add);
            return this;
        }

        /**
         * Removes a parameter.
         * 删除参数。
         *
         * @param name the parameter name - 参数名
         * @return this builder - 此构建器
         */
        public Builder remove(String name) {
            params.remove(name);
            return this;
        }

        /**
         * Clears all parameters.
         * 清除所有参数。
         *
         * @return this builder - 此构建器
         */
        public Builder clear() {
            params.clear();
            return this;
        }

        /**
         * Builds the query string.
         * 构建查询字符串。
         *
         * @return the query string - 查询字符串
         */
        public QueryString build() {
            return new QueryString(new LinkedHashMap<>(params));
        }
    }
}
