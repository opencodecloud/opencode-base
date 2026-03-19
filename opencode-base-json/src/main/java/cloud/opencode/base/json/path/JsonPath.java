
package cloud.opencode.base.json.path;

import cloud.opencode.base.json.JsonNode;
import cloud.opencode.base.json.exception.OpenJsonProcessingException;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JSON Path - XPath-style Query Language for JSON
 * JSON Path - JSON 的 XPath 风格查询语言
 *
 * <p>This class implements JSONPath, an XPath-like query language for JSON.
 * It supports complex queries including wildcards, filters, and array slices.</p>
 * <p>此类实现 JSONPath，一种类似 XPath 的 JSON 查询语言。
 * 它支持复杂查询，包括通配符、过滤器和数组切片。</p>
 *
 * <p><strong>Syntax | 语法:</strong></p>
 * <ul>
 *   <li>{@code $} - Root element - 根元素</li>
 *   <li>{@code .property} or {@code ['property']} - Child property - 子属性</li>
 *   <li>{@code ..property} - Recursive descent - 递归下降</li>
 *   <li>{@code *} - Wildcard (all children) - 通配符（所有子元素）</li>
 *   <li>{@code [n]} - Array index - 数组索引</li>
 *   <li>{@code [start:end]} - Array slice - 数组切片</li>
 *   <li>{@code [?(@.price < 10)]} - Filter expression - 过滤表达式</li>
 * </ul>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * String json = "{\"store\":{\"books\":[{\"title\":\"A\",\"price\":10},{\"title\":\"B\",\"price\":20}]}}";
 * JsonNode root = OpenJson.parse(json);
 *
 * // Simple property access
 * List<JsonNode> titles = JsonPath.read(root, "$.store.books[*].title");
 *
 * // Filter expression
 * List<JsonNode> cheap = JsonPath.read(root, "$.store.books[?(@.price < 15)]");
 *
 * // Recursive descent
 * List<JsonNode> allPrices = JsonPath.read(root, "$..price");
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>XPath-style query language for JSON navigation - JSON导航的XPath风格查询语言</li>
 *   <li>Wildcard, filter, array slice, and recursive descent support - 通配符、过滤器、数组切片和递归下降支持</li>
 *   <li>Compiled path expressions for reuse - 可重用的编译路径表达式</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: Partial (validates inputs) - 空值安全: 部分（验证输入）</li>
 * </ul>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://goessner.net/articles/JsonPath/">JSONPath Specification</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
public final class JsonPath {

    private static final Pattern DOT_NOTATION = Pattern.compile("\\.(\\w+|\\*)");
    private static final Pattern BRACKET_NOTATION = Pattern.compile("\\['([^']+)'\\]|\\[\"([^\"]+)\"\\]");
    private static final Pattern ARRAY_INDEX = Pattern.compile("\\[(\\d+)\\]");
    private static final Pattern ARRAY_SLICE = Pattern.compile("\\[(-?\\d*):(-?\\d*)(?::(-?\\d+))?\\]");
    private static final Pattern WILDCARD_ARRAY = Pattern.compile("\\[\\*\\]");
    private static final Pattern FILTER_EXPR = Pattern.compile("\\[\\?\\((.+?)\\)\\]");
    private static final Pattern RECURSIVE_DESCENT = Pattern.compile("\\.\\.");
    private static final Pattern WORD_PATTERN = Pattern.compile("\\w+");
    private static final Pattern SIMPLE_FILTER = Pattern.compile("@\\.(\\w+)\\s*(==|!=|<|>|<=|>=)\\s*(.+)");
    private static final Pattern EXISTS_FILTER = Pattern.compile("@\\.(\\w+)");

    /**
     * Original path expression
     * 原始路径表达式
     */
    private final String expression;

    /**
     * Compiled path segments
     * 编译的路径段
     */
    private final List<PathSegment> segments;

    private JsonPath(String expression, List<PathSegment> segments) {
        this.expression = expression;
        this.segments = segments;
    }

    /**
     * Compiles a JSONPath expression.
     * 编译 JSONPath 表达式。
     *
     * @param expression the JSONPath expression - JSONPath 表达式
     * @return the compiled JsonPath - 编译的 JsonPath
     * @throws OpenJsonProcessingException if expression is invalid - 如果表达式无效
     */
    public static JsonPath compile(String expression) {
        Objects.requireNonNull(expression, "Expression must not be null");

        if (!expression.startsWith("$")) {
            throw OpenJsonProcessingException.pathError(
                    "JSONPath expression must start with '$': " + expression);
        }

        List<PathSegment> segments = parse(expression.substring(1));
        return new JsonPath(expression, segments);
    }

    /**
     * Reads all matching values from a JSON node.
     * 从 JSON 节点读取所有匹配的值。
     *
     * @param root       the root node - 根节点
     * @param expression the JSONPath expression - JSONPath 表达式
     * @return list of matching nodes - 匹配节点列表
     */
    public static List<JsonNode> read(JsonNode root, String expression) {
        return compile(expression).evaluate(root);
    }

    /**
     * Reads the first matching value from a JSON node.
     * 从 JSON 节点读取第一个匹配的值。
     *
     * @param root       the root node - 根节点
     * @param expression the JSONPath expression - JSONPath 表达式
     * @return the first matching node, or null if none - 第一个匹配节点，如果没有则返回 null
     */
    public static JsonNode readFirst(JsonNode root, String expression) {
        List<JsonNode> results = read(root, expression);
        return results.isEmpty() ? null : results.getFirst();
    }

    /**
     * Checks if any node matches the expression.
     * 检查是否有任何节点匹配表达式。
     *
     * @param root       the root node - 根节点
     * @param expression the JSONPath expression - JSONPath 表达式
     * @return true if at least one match exists - 如果至少有一个匹配则返回 true
     */
    public static boolean exists(JsonNode root, String expression) {
        return !read(root, expression).isEmpty();
    }

    /**
     * Evaluates this path against a JSON node.
     * 对 JSON 节点求值此路径。
     *
     * @param root the root node - 根节点
     * @return list of matching nodes - 匹配节点列表
     */
    public List<JsonNode> evaluate(JsonNode root) {
        Objects.requireNonNull(root, "Root node must not be null");

        List<JsonNode> current = List.of(root);
        for (PathSegment segment : segments) {
            current = segment.evaluate(current);
            if (current.isEmpty()) {
                break;
            }
        }
        return current;
    }

    /**
     * Returns the first matching node.
     * 返回第一个匹配节点。
     *
     * @param root the root node - 根节点
     * @return the first match, or null - 第一个匹配，或 null
     */
    public JsonNode evaluateFirst(JsonNode root) {
        List<JsonNode> results = evaluate(root);
        return results.isEmpty() ? null : results.getFirst();
    }

    /**
     * Returns the path expression.
     * 返回路径表达式。
     *
     * @return the expression - 表达式
     */
    public String getExpression() {
        return expression;
    }

    @Override
    public String toString() {
        return expression;
    }

    // ==================== Parsing ====================

    private static List<PathSegment> parse(String path) {
        List<PathSegment> segments = new ArrayList<>();
        int pos = 0;

        while (pos < path.length()) {
            // Recursive descent
            if (path.startsWith("..", pos)) {
                pos += 2;
                String property = parseProperty(path, pos);
                if (property != null) {
                    segments.add(new RecursiveDescentSegment(property));
                    pos += property.length() + (path.charAt(pos) == '.' ? 1 : 0);
                }
                continue;
            }

            // Dot notation
            Matcher dotMatcher = DOT_NOTATION.matcher(path.substring(pos));
            if (dotMatcher.lookingAt()) {
                String property = dotMatcher.group(1);
                if ("*".equals(property)) {
                    segments.add(new WildcardSegment());
                } else {
                    segments.add(new PropertySegment(property));
                }
                pos += dotMatcher.end();
                continue;
            }

            // Bracket notation for property
            Matcher bracketMatcher = BRACKET_NOTATION.matcher(path.substring(pos));
            if (bracketMatcher.lookingAt()) {
                String property = bracketMatcher.group(1) != null ?
                        bracketMatcher.group(1) : bracketMatcher.group(2);
                segments.add(new PropertySegment(property));
                pos += bracketMatcher.end();
                continue;
            }

            // Array wildcard
            Matcher wildcardMatcher = WILDCARD_ARRAY.matcher(path.substring(pos));
            if (wildcardMatcher.lookingAt()) {
                segments.add(new ArrayWildcardSegment());
                pos += wildcardMatcher.end();
                continue;
            }

            // Filter expression
            Matcher filterMatcher = FILTER_EXPR.matcher(path.substring(pos));
            if (filterMatcher.lookingAt()) {
                segments.add(new FilterSegment(filterMatcher.group(1)));
                pos += filterMatcher.end();
                continue;
            }

            // Array slice
            Matcher sliceMatcher = ARRAY_SLICE.matcher(path.substring(pos));
            if (sliceMatcher.lookingAt()) {
                String startStr = sliceMatcher.group(1);
                String endStr = sliceMatcher.group(2);
                String stepStr = sliceMatcher.group(3);
                Integer start = startStr.isEmpty() ? null : Integer.parseInt(startStr);
                Integer end = endStr.isEmpty() ? null : Integer.parseInt(endStr);
                Integer step = stepStr == null ? null : Integer.parseInt(stepStr);
                segments.add(new ArraySliceSegment(start, end, step));
                pos += sliceMatcher.end();
                continue;
            }

            // Array index
            Matcher indexMatcher = ARRAY_INDEX.matcher(path.substring(pos));
            if (indexMatcher.lookingAt()) {
                int index = Integer.parseInt(indexMatcher.group(1));
                segments.add(new ArrayIndexSegment(index));
                pos += indexMatcher.end();
                continue;
            }

            // Unknown token
            throw OpenJsonProcessingException.pathError(
                    "Invalid JSONPath syntax at position " + pos + ": " + path);
        }

        return segments;
    }

    private static String parseProperty(String path, int pos) {
        if (pos >= path.length()) {
            return null;
        }
        Matcher matcher = WORD_PATTERN.matcher(path.substring(pos));
        if (matcher.lookingAt()) {
            return matcher.group();
        }
        return null;
    }

    // ==================== Path Segments ====================

    private sealed interface PathSegment {
        List<JsonNode> evaluate(List<JsonNode> nodes);
    }

    private record PropertySegment(String property) implements PathSegment {
        @Override
        public List<JsonNode> evaluate(List<JsonNode> nodes) {
            List<JsonNode> result = new ArrayList<>();
            for (JsonNode node : nodes) {
                if (node.isObject()) {
                    JsonNode child = node.get(property);
                    if (child != null) {
                        result.add(child);
                    }
                }
            }
            return result;
        }
    }

    private record ArrayIndexSegment(int index) implements PathSegment {
        @Override
        public List<JsonNode> evaluate(List<JsonNode> nodes) {
            List<JsonNode> result = new ArrayList<>();
            for (JsonNode node : nodes) {
                if (node.isArray()) {
                    int actualIndex = index < 0 ? node.size() + index : index;
                    if (actualIndex >= 0 && actualIndex < node.size()) {
                        result.add(node.get(actualIndex));
                    }
                }
            }
            return result;
        }
    }

    private record ArraySliceSegment(Integer start, Integer end, Integer step) implements PathSegment {
        @Override
        public List<JsonNode> evaluate(List<JsonNode> nodes) {
            List<JsonNode> result = new ArrayList<>();
            for (JsonNode node : nodes) {
                if (node.isArray()) {
                    int size = node.size();
                    int s = start != null ? normalizeIndex(start, size) : 0;
                    int e = end != null ? normalizeIndex(end, size) : size;
                    int st = step != null ? step : 1;

                    if (st > 0) {
                        for (int i = s; i < e && i < size; i += st) {
                            if (i >= 0) {
                                result.add(node.get(i));
                            }
                        }
                    } else if (st < 0) {
                        for (int i = s; i > e && i >= 0; i += st) {
                            if (i < size) {
                                result.add(node.get(i));
                            }
                        }
                    }
                }
            }
            return result;
        }

        private int normalizeIndex(int idx, int size) {
            return idx < 0 ? size + idx : idx;
        }
    }

    private record WildcardSegment() implements PathSegment {
        @Override
        public List<JsonNode> evaluate(List<JsonNode> nodes) {
            List<JsonNode> result = new ArrayList<>();
            for (JsonNode node : nodes) {
                if (node.isObject()) {
                    for (String key : node.keys()) {
                        result.add(node.get(key));
                    }
                } else if (node.isArray()) {
                    for (int i = 0; i < node.size(); i++) {
                        result.add(node.get(i));
                    }
                }
            }
            return result;
        }
    }

    private record ArrayWildcardSegment() implements PathSegment {
        @Override
        public List<JsonNode> evaluate(List<JsonNode> nodes) {
            List<JsonNode> result = new ArrayList<>();
            for (JsonNode node : nodes) {
                if (node.isArray()) {
                    for (int i = 0; i < node.size(); i++) {
                        result.add(node.get(i));
                    }
                }
            }
            return result;
        }
    }

    private record RecursiveDescentSegment(String property) implements PathSegment {

        private static final int MAX_RECURSION_DEPTH = 1000;

        @Override
        public List<JsonNode> evaluate(List<JsonNode> nodes) {
            List<JsonNode> result = new ArrayList<>();
            for (JsonNode node : nodes) {
                collectRecursive(node, property, result, 0);
            }
            return result;
        }

        private void collectRecursive(JsonNode node, String prop, List<JsonNode> result, int depth) {
            if (depth > MAX_RECURSION_DEPTH) {
                throw OpenJsonProcessingException.pathError(
                    "Recursive descent exceeded maximum depth (" + MAX_RECURSION_DEPTH
                    + "); JSON structure may be too deeply nested");
            }
            if (node.isObject()) {
                JsonNode child = node.get(prop);
                if (child != null) {
                    result.add(child);
                }
                for (String key : node.keys()) {
                    collectRecursive(node.get(key), prop, result, depth + 1);
                }
            } else if (node.isArray()) {
                for (int i = 0; i < node.size(); i++) {
                    collectRecursive(node.get(i), prop, result, depth + 1);
                }
            }
        }
    }

    private record FilterSegment(String expression) implements PathSegment {
        @Override
        public List<JsonNode> evaluate(List<JsonNode> nodes) {
            List<JsonNode> result = new ArrayList<>();
            for (JsonNode node : nodes) {
                if (node.isArray()) {
                    for (int i = 0; i < node.size(); i++) {
                        JsonNode element = node.get(i);
                        if (matchesFilter(element, expression)) {
                            result.add(element);
                        }
                    }
                }
            }
            return result;
        }

        private boolean matchesFilter(JsonNode node, String expr) {
            // Simple filter implementation for common patterns
            // @.property operator value
            Matcher matcher = SIMPLE_FILTER.matcher(expr.trim());

            if (matcher.matches()) {
                String property = matcher.group(1);
                String operator = matcher.group(2);
                String valueStr = matcher.group(3).trim();

                JsonNode propValue = node.get(property);
                if (propValue == null) {
                    return false;
                }

                return compareValues(propValue, operator, valueStr);
            }

            // @.property (existence check)
            matcher = EXISTS_FILTER.matcher(expr.trim());
            if (matcher.matches()) {
                return node.get(matcher.group(1)) != null;
            }

            return false;
        }

        private boolean compareValues(JsonNode node, String operator, String valueStr) {
            // Remove quotes for string comparison
            if (valueStr.startsWith("'") && valueStr.endsWith("'")) {
                valueStr = valueStr.substring(1, valueStr.length() - 1);
            } else if (valueStr.startsWith("\"") && valueStr.endsWith("\"")) {
                valueStr = valueStr.substring(1, valueStr.length() - 1);
            }

            if (node.isNumber()) {
                try {
                    double nodeValue = node.asDouble();
                    double compareValue = Double.parseDouble(valueStr);
                    return switch (operator) {
                        case "==" -> nodeValue == compareValue;
                        case "!=" -> nodeValue != compareValue;
                        case "<" -> nodeValue < compareValue;
                        case ">" -> nodeValue > compareValue;
                        case "<=" -> nodeValue <= compareValue;
                        case ">=" -> nodeValue >= compareValue;
                        default -> false;
                    };
                } catch (NumberFormatException e) {
                    return false;
                }
            } else if (node.isString()) {
                String nodeValue = node.asString();
                return switch (operator) {
                    case "==" -> nodeValue.equals(valueStr);
                    case "!=" -> !nodeValue.equals(valueStr);
                    default -> false;
                };
            } else if (node.isBoolean()) {
                boolean nodeValue = node.asBoolean();
                boolean compareValue = Boolean.parseBoolean(valueStr);
                return switch (operator) {
                    case "==" -> nodeValue == compareValue;
                    case "!=" -> nodeValue != compareValue;
                    default -> false;
                };
            }

            return false;
        }
    }
}
