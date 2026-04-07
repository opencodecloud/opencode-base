
package cloud.opencode.base.json.path;

import cloud.opencode.base.json.JsonNode;
import cloud.opencode.base.json.exception.OpenJsonProcessingException;

import java.util.*;
import java.util.regex.Pattern;

/**
 * JSON Pointer - RFC 6901 Implementation
 * JSON Pointer - RFC 6901 实现
 *
 * <p>This class implements JSON Pointer (RFC 6901), a syntax for
 * identifying specific values within a JSON document.</p>
 * <p>此类实现 JSON Pointer（RFC 6901），用于标识 JSON 文档中特定值的语法。</p>
 *
 * <p><strong>Syntax | 语法:</strong></p>
 * <ul>
 *   <li>{@code ""} - Root document - 根文档</li>
 *   <li>{@code "/foo"} - Property "foo" of root - 根的属性 "foo"</li>
 *   <li>{@code "/foo/0"} - First element of array "foo" - 数组 "foo" 的第一个元素</li>
 *   <li>{@code "/a~1b"} - Property "a/b" (/ escaped as ~1) - 属性 "a/b"（/ 转义为 ~1）</li>
 *   <li>{@code "/m~0n"} - Property "m~n" (~ escaped as ~0) - 属性 "m~n"（~ 转义为 ~0）</li>
 * </ul>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * JsonNode root = OpenJson.parse("{\"foo\":{\"bar\":[1,2,3]}}");
 *
 * // Access nested values
 * JsonPointer pointer = JsonPointer.parse("/foo/bar/1");
 * JsonNode value = pointer.evaluate(root);  // Returns 2
 *
 * // Modify values
 * JsonPointer.parse("/foo/bar/0").set(root, JsonNode.of(10));
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>RFC 6901 JSON Pointer implementation - RFC 6901 JSON Pointer实现</li>
 *   <li>Path-based navigation with escaping support - 支持转义的路径导航</li>
 *   <li>Pointer composition and decomposition - 指针组合和分解</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: Partial (validates inputs) - 空值安全: 部分（验证输入）</li>
 * </ul>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://tools.ietf.org/html/rfc6901">RFC 6901 - JSON Pointer</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
public final class JsonPointer {

    /**
     * Empty/root pointer
     * 空/根指针
     */
    public static final JsonPointer ROOT = new JsonPointer("", List.of());

    private static final Pattern ARRAY_INDEX_PATTERN = Pattern.compile("^(0|[1-9]\\d*)$");

    private static final int CACHE_SIZE = 256;
    private static final Map<String, JsonPointer> PARSE_CACHE = Collections.synchronizedMap(
            new LinkedHashMap<>(CACHE_SIZE, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, JsonPointer> eldest) {
                    return size() > CACHE_SIZE;
                }
            });

    /**
     * Original pointer string
     * 原始指针字符串
     */
    private final String pointer;

    /**
     * Reference tokens
     * 引用令牌
     */
    private final List<String> tokens;

    private JsonPointer(String pointer, List<String> tokens) {
        this.pointer = pointer;
        this.tokens = tokens;
    }

    /**
     * Parses a JSON Pointer string.
     * 解析 JSON Pointer 字符串。
     *
     * @param pointer the pointer string (e.g., "/foo/bar") - 指针字符串
     * @return the JsonPointer instance - JsonPointer 实例
     * @throws OpenJsonProcessingException if the pointer is invalid - 如果指针无效
     */
    public static JsonPointer parse(String pointer) {
        Objects.requireNonNull(pointer, "Pointer must not be null");
        if (pointer.isEmpty()) {
            return ROOT;
        }
        JsonPointer cached = PARSE_CACHE.get(pointer);
        if (cached != null) {
            return cached;
        }
        if (!pointer.startsWith("/")) {
            throw OpenJsonProcessingException.pathError(
                    "JSON Pointer must start with '/' or be empty: " + pointer);
        }
        String[] parts = pointer.substring(1).split("/", -1);
        List<String> tokens = new ArrayList<>(parts.length);
        for (String part : parts) {
            tokens.add(unescape(part));
        }
        JsonPointer result = new JsonPointer(pointer, List.copyOf(tokens));
        PARSE_CACHE.put(pointer, result);
        return result;
    }

    /**
     * Creates a JsonPointer from reference tokens.
     * 从引用令牌创建 JsonPointer。
     *
     * @param tokens the reference tokens - 引用令牌
     * @return the JsonPointer instance - JsonPointer 实例
     */
    public static JsonPointer of(String... tokens) {
        if (tokens == null || tokens.length == 0) {
            return ROOT;
        }

        StringBuilder sb = new StringBuilder();
        for (String token : tokens) {
            Objects.requireNonNull(token, "Token must not be null");
            sb.append('/').append(escape(token));
        }
        return new JsonPointer(sb.toString(), List.of(tokens));
    }

    /**
     * Creates a JsonPointer from reference tokens.
     * 从引用令牌创建 JsonPointer。
     *
     * @param tokens the reference tokens - 引用令牌
     * @return the JsonPointer instance - JsonPointer 实例
     */
    public static JsonPointer of(List<String> tokens) {
        return of(tokens.toArray(String[]::new));
    }

    /**
     * Evaluates this pointer against a JSON node.
     * 对 JSON 节点求值此指针。
     *
     * @param root the root node - 根节点
     * @return the node at this pointer location - 此指针位置的节点
     * @throws OpenJsonProcessingException if path is invalid - 如果路径无效
     */
    public JsonNode evaluate(JsonNode root) {
        Objects.requireNonNull(root, "Root node must not be null");

        JsonNode current = root;
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            current = evaluateToken(current, token, i);
            if (current == null) {
                throw OpenJsonProcessingException.pathError(
                        "Path not found: " + toPointerString(tokens.subList(0, i + 1)));
            }
        }
        return current;
    }

    /**
     * Evaluates this pointer, returning null if path doesn't exist.
     * 求值此指针，如果路径不存在则返回 null。
     *
     * @param root the root node - 根节点
     * @return the node at this pointer location, or null - 此指针位置的节点，或 null
     */
    public JsonNode evaluateOrNull(JsonNode root) {
        if (root == null) {
            return null;
        }

        JsonNode current = root;
        for (String token : tokens) {
            current = evaluateTokenSafe(current, token);
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    /**
     * Checks if this pointer exists in the given node.
     * 检查此指针是否存在于给定节点中。
     *
     * @param root the root node - 根节点
     * @return true if the path exists - 如果路径存在则返回 true
     */
    public boolean exists(JsonNode root) {
        return evaluateOrNull(root) != null;
    }

    private JsonNode evaluateToken(JsonNode node, String token, int index) {
        if (node.isObject()) {
            return node.get(token);
        } else if (node.isArray()) {
            if (!isArrayIndex(token)) {
                throw OpenJsonProcessingException.pathError(
                        "Invalid array index: " + token + " at " +
                                toPointerString(tokens.subList(0, index + 1)));
            }
            int arrayIndex;
            try {
                arrayIndex = Integer.parseInt(token);
            } catch (NumberFormatException e) {
                throw OpenJsonProcessingException.pathError(
                        "Array index out of range: " + token + " at " +
                                toPointerString(tokens.subList(0, index + 1)));
            }
            if (arrayIndex >= node.size()) {
                return null;
            }
            return node.get(arrayIndex);
        }
        return null;
    }

    private JsonNode evaluateTokenSafe(JsonNode node, String token) {
        if (node.isObject()) {
            return node.get(token);
        } else if (node.isArray() && isArrayIndex(token)) {
            int arrayIndex;
            try {
                arrayIndex = Integer.parseInt(token);
            } catch (NumberFormatException e) {
                return null;
            }
            if (arrayIndex < node.size()) {
                return node.get(arrayIndex);
            }
        }
        return null;
    }

    /**
     * Returns the parent pointer.
     * 返回父指针。
     *
     * @return the parent pointer, or ROOT if this is ROOT - 父指针，如果是 ROOT 则返回 ROOT
     */
    public JsonPointer parent() {
        if (tokens.isEmpty()) {
            return ROOT;
        }
        return of(tokens.subList(0, tokens.size() - 1));
    }

    /**
     * Appends a property name to this pointer.
     * 向此指针追加属性名。
     *
     * @param property the property name - 属性名
     * @return a new pointer with the appended property - 追加属性后的新指针
     */
    public JsonPointer append(String property) {
        List<String> newTokens = new ArrayList<>(tokens);
        newTokens.add(property);
        return of(newTokens);
    }

    /**
     * Appends an array index to this pointer.
     * 向此指针追加数组索引。
     *
     * @param index the array index - 数组索引
     * @return a new pointer with the appended index - 追加索引后的新指针
     */
    public JsonPointer append(int index) {
        return append(String.valueOf(index));
    }

    /**
     * Returns the last reference token.
     * 返回最后一个引用令牌。
     *
     * @return the last token, or null for ROOT - 最后一个令牌，ROOT 返回 null
     */
    public String getLastToken() {
        return tokens.isEmpty() ? null : tokens.getLast();
    }

    /**
     * Returns the reference tokens.
     * 返回引用令牌。
     *
     * @return unmodifiable list of tokens - 不可变的令牌列表
     */
    public List<String> getTokens() {
        return tokens;
    }

    /**
     * Returns the depth (number of tokens).
     * 返回深度（令牌数量）。
     *
     * @return the depth - 深度
     */
    public int depth() {
        return tokens.size();
    }

    /**
     * Returns whether this is the root pointer.
     * 返回此指针是否为根指针。
     *
     * @return true if root - 如果是根则返回 true
     */
    public boolean isRoot() {
        return tokens.isEmpty();
    }

    /**
     * Returns the pointer string representation.
     * 返回指针字符串表示。
     *
     * @return the pointer string - 指针字符串
     */
    @Override
    public String toString() {
        return pointer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JsonPointer that)) return false;
        return pointer.equals(that.pointer);
    }

    @Override
    public int hashCode() {
        return pointer.hashCode();
    }

    // ==================== Utility Methods ====================

    private static String escape(String token) {
        return token.replace("~", "~0").replace("/", "~1");
    }

    private static String unescape(String token) {
        return token.replace("~1", "/").replace("~0", "~");
    }

    private static boolean isArrayIndex(String token) {
        if (token.isEmpty() || token.length() > 10) return false;
        if (token.charAt(0) == '0') return token.length() == 1;
        for (int i = 0; i < token.length(); i++) {
            char c = token.charAt(i);
            if (c < '0' || c > '9') return false;
        }
        return true;
    }

    private static String toPointerString(List<String> tokens) {
        if (tokens.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String token : tokens) {
            sb.append('/').append(escape(token));
        }
        return sb.toString();
    }
}
