/*
 * Copyright 2025 Leon Soo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cloud.opencode.base.json.util;

import cloud.opencode.base.json.JsonNode;
import cloud.opencode.base.json.exception.OpenJsonProcessingException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JSON Flattener - Converts nested JSON to flat dot-notation key-value maps and back.
 * JSON 扁平化工具 - 将嵌套 JSON 转换为扁平的点分隔键值映射，并支持反向操作。
 *
 * <p>This utility flattens a nested {@link JsonNode} tree into a single-level map where
 * keys represent the full path to each leaf value. It also supports unflattening a flat
 * map back into a nested tree structure.</p>
 * <p>此工具将嵌套的 {@link JsonNode} 树扁平化为单层映射，其中键表示每个叶值的完整路径。
 * 同时支持将扁平映射反向还原为嵌套树结构。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Flatten nested JSON objects and arrays to dot-notation keys - 将嵌套 JSON 对象和数组扁平化为点分隔键</li>
 *   <li>Configurable separator and array notation style - 可配置分隔符和数组表示法风格</li>
 *   <li>Unflatten flat maps back to nested JSON trees - 将扁平映射反向还原为嵌套 JSON 树</li>
 *   <li>Preserves empty objects, empty arrays, and null values - 保留空对象、空数组和 null 值</li>
 *   <li>Depth guard against stack overflow - 深度保护防止栈溢出</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Flatten with default settings
 * JsonNode obj = JsonNode.object()
 *     .put("a", JsonNode.object().put("b", 1))
 *     .put("c", JsonNode.array().add(2).add(3));
 * Map<String, JsonNode> flat = JsonFlattener.flatten(obj);
 * // {"a.b": NumberNode(1), "c[0]": NumberNode(2), "c[1]": NumberNode(3)}
 *
 * // Unflatten back to nested structure
 * JsonNode restored = JsonFlattener.unflatten(flat);
 * }</pre>
 *
 * <p><strong>Performance | 性能:</strong></p>
 * <ul>
 *   <li>O(n) time complexity where n is the total number of nodes - O(n) 时间复杂度，n 为节点总数</li>
 *   <li>Stateless and thread-safe - 无状态且线程安全</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 *   <li>Depth-limited to prevent stack overflow attacks - 深度限制以防止栈溢出攻击</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.3
 */
public final class JsonFlattener {

    private JsonFlattener() {
        throw new AssertionError("No instances");
    }

    /**
     * Configuration for flatten/unflatten operations.
     * 扁平化/反扁平化操作的配置。
     *
     * @param separator            the key separator (e.g. ".") - 键分隔符（如 "."）
     * @param bracketArrayNotation whether to use bracket notation for arrays (e.g. "[0]") - 是否使用方括号数组表示法（如 "[0]"）
     * @param maxDepth             the maximum nesting depth before throwing an exception - 抛出异常前的最大嵌套深度
     */
    public record FlattenConfig(String separator, boolean bracketArrayNotation, int maxDepth) {

        /**
         * Default configuration: "." separator, bracket array notation, max depth 1000.
         * 默认配置："." 分隔符，方括号数组表示法，最大深度 1000。
         */
        public static final FlattenConfig DEFAULT = new FlattenConfig(".", true, 1000);

        /**
         * Constructs a FlattenConfig with validation.
         * 构造带验证的 FlattenConfig。
         *
         * @param separator            the key separator - 键分隔符
         * @param bracketArrayNotation whether to use bracket array notation - 是否使用方括号数组表示法
         * @param maxDepth             the maximum nesting depth - 最大嵌套深度
         * @throws IllegalArgumentException if separator is null/empty or maxDepth is non-positive
         *                                  如果分隔符为 null/空或最大深度为非正数
         */
        public FlattenConfig {
            Objects.requireNonNull(separator, "separator must not be null");
            if (separator.isEmpty()) {
                throw new IllegalArgumentException("separator must not be empty");
            }
            if (maxDepth <= 0) {
                throw new IllegalArgumentException("maxDepth must be positive, got: " + maxDepth);
            }
        }
    }

    // ==================== Flatten ====================

    /**
     * Flattens a JSON tree into a flat map using the default configuration.
     * 使用默认配置将 JSON 树扁平化为平面映射。
     *
     * <p>Uses "." as separator and bracket notation "[n]" for array indices.</p>
     * <p>使用 "." 作为分隔符，"[n]" 作为数组索引表示法。</p>
     *
     * @param node the JSON node to flatten - 要扁平化的 JSON 节点
     * @return an ordered map of flattened key-value pairs - 扁平化键值对的有序映射
     * @throws OpenJsonProcessingException if the node exceeds maximum depth - 如果节点超过最大深度
     * @throws NullPointerException        if node is null - 如果节点为 null
     */
    public static Map<String, JsonNode> flatten(JsonNode node) {
        return flatten(node, FlattenConfig.DEFAULT);
    }

    /**
     * Flattens a JSON tree into a flat map using a custom separator.
     * 使用自定义分隔符将 JSON 树扁平化为平面映射。
     *
     * @param node      the JSON node to flatten - 要扁平化的 JSON 节点
     * @param separator the key separator - 键分隔符
     * @return an ordered map of flattened key-value pairs - 扁平化键值对的有序映射
     * @throws OpenJsonProcessingException if the node exceeds maximum depth - 如果节点超过最大深度
     * @throws NullPointerException        if node or separator is null - 如果节点或分隔符为 null
     */
    public static Map<String, JsonNode> flatten(JsonNode node, String separator) {
        Objects.requireNonNull(separator, "separator must not be null");
        return flatten(node, new FlattenConfig(separator, true, 1000));
    }

    /**
     * Flattens a JSON tree into a flat map using the specified configuration.
     * 使用指定配置将 JSON 树扁平化为平面映射。
     *
     * <p>Object keys are joined with the separator. Array indices use bracket
     * notation (e.g. "[0]") when {@code bracketArrayNotation} is true,
     * otherwise dot notation (e.g. ".0").</p>
     * <p>对象键使用分隔符连接。数组索引在 {@code bracketArrayNotation} 为 true 时
     * 使用方括号表示法（如 "[0]"），否则使用点表示法（如 ".0"）。</p>
     *
     * @param node   the JSON node to flatten - 要扁平化的 JSON 节点
     * @param config the flatten configuration - 扁平化配置
     * @return an ordered map of flattened key-value pairs - 扁平化键值对的有序映射
     * @throws OpenJsonProcessingException if the node exceeds maximum depth - 如果节点超过最大深度
     * @throws NullPointerException        if node or config is null - 如果节点或配置为 null
     */
    public static Map<String, JsonNode> flatten(JsonNode node, FlattenConfig config) {
        Objects.requireNonNull(node, "node must not be null");
        Objects.requireNonNull(config, "config must not be null");

        Map<String, JsonNode> result = new LinkedHashMap<>();
        // Use StringBuilder to avoid intermediate String allocations during prefix building
        doFlatten(node, new StringBuilder(64), config, result, 0);
        return result;
    }

    /**
     * Recursive flattening implementation using StringBuilder for prefix building.
     * Uses append + setLength pattern to avoid intermediate String allocations.
     */
    private static void doFlatten(JsonNode node, StringBuilder prefix, FlattenConfig config,
                                  Map<String, JsonNode> result, int depth) {
        if (depth > config.maxDepth()) {
            throw new OpenJsonProcessingException(
                    "Maximum flatten depth exceeded: " + config.maxDepth(),
                    OpenJsonProcessingException.ErrorType.PARSE_ERROR);
        }

        if (node.isObject()) {
            if (node.size() == 0) {
                // Preserve empty objects
                if (!prefix.isEmpty()) {
                    result.put(prefix.toString(), node);
                }
                return;
            }
            for (String key : node.keys()) {
                if (key.contains(config.separator()) || key.contains("[")) {
                    throw new OpenJsonProcessingException(
                            "Object key '" + key + "' contains separator '" + config.separator()
                                    + "' or '['; use a different separator to flatten this document",
                            OpenJsonProcessingException.ErrorType.SERIALIZATION_ERROR);
                }
                int savedLen = prefix.length();
                if (savedLen > 0) prefix.append(config.separator());
                prefix.append(key);
                doFlatten(node.get(key), prefix, config, result, depth + 1);
                prefix.setLength(savedLen);
            }
        } else if (node.isArray()) {
            if (node.size() == 0) {
                // Preserve empty arrays
                if (!prefix.isEmpty()) {
                    result.put(prefix.toString(), node);
                }
                return;
            }
            for (int i = 0; i < node.size(); i++) {
                int savedLen = prefix.length();
                if (config.bracketArrayNotation()) {
                    prefix.append('[').append(i).append(']');
                } else {
                    if (savedLen > 0) prefix.append(config.separator());
                    prefix.append(i);
                }
                doFlatten(node.get(i), prefix, config, result, depth + 1);
                prefix.setLength(savedLen);
            }
        } else {
            // Leaf value: string, number, boolean, null
            result.put(prefix.toString(), node);
        }
    }

    // ==================== Unflatten ====================

    /**
     * Unflattens a flat map back into a nested JSON tree using the default "." separator.
     * 使用默认的 "." 分隔符将平面映射反向还原为嵌套 JSON 树。
     *
     * @param map the flat key-value map - 平面键值映射
     * @return the nested JSON tree - 嵌套 JSON 树
     * @throws OpenJsonProcessingException if the map contains conflicting keys - 如果映射包含冲突的键
     * @throws NullPointerException        if map is null - 如果映射为 null
     */
    public static JsonNode unflatten(Map<String, JsonNode> map) {
        return unflatten(map, ".");
    }

    /**
     * Unflattens a flat map back into a nested JSON tree using a custom separator.
     * 使用自定义分隔符将平面映射反向还原为嵌套 JSON 树。
     *
     * @param map       the flat key-value map - 平面键值映射
     * @param separator the key separator - 键分隔符
     * @return the nested JSON tree - 嵌套 JSON 树
     * @throws OpenJsonProcessingException if the map contains conflicting keys - 如果映射包含冲突的键
     * @throws NullPointerException        if map or separator is null - 如果映射或分隔符为 null
     */
    public static JsonNode unflatten(Map<String, JsonNode> map, String separator) {
        Objects.requireNonNull(map, "map must not be null");
        Objects.requireNonNull(separator, "separator must not be null");

        if (map.isEmpty()) {
            return JsonNode.object();
        }

        // Determine if the root should be an array or object
        JsonNode root = detectRootType(map, separator);

        for (Map.Entry<String, JsonNode> entry : map.entrySet()) {
            String key = entry.getKey();
            JsonNode value = entry.getValue();
            setNestedValue(root, key, value, separator);
        }

        return root;
    }

    /** Pattern for bracket array index notation, e.g. "[0]". */
    private static final Pattern BRACKET_INDEX_PATTERN = Pattern.compile("\\[(\\d+)]");

    /**
     * Detects whether the root container should be an object or array based on the keys.
     */
    private static JsonNode detectRootType(Map<String, JsonNode> map, String separator) {
        for (String key : map.keySet()) {
            // If the first key starts with "[", root is an array
            if (key.startsWith("[")) {
                return JsonNode.array();
            }
            // If it starts with a digit and has no separator before it, check if it's purely numeric
            String firstSegment = extractFirstSegment(key, separator);
            if (!isArrayIndex(firstSegment)) {
                return JsonNode.object();
            }
        }
        // All first segments are numeric: could be array with dot notation
        // Check if all first segments are purely numeric
        boolean allNumeric = true;
        for (String key : map.keySet()) {
            String firstSegment = extractFirstSegment(key, separator);
            if (!isNumeric(firstSegment)) {
                allNumeric = false;
                break;
            }
        }
        return allNumeric ? JsonNode.array() : JsonNode.object();
    }

    /**
     * Extracts the first path segment from a key.
     */
    private static String extractFirstSegment(String key, String separator) {
        // Check for bracket notation first
        if (key.startsWith("[")) {
            Matcher m = BRACKET_INDEX_PATTERN.matcher(key);
            if (m.lookingAt()) {
                return key.substring(0, m.end());
            }
        }
        int sepIdx = key.indexOf(separator);
        int bracketIdx = key.indexOf('[');
        if (sepIdx < 0 && bracketIdx < 0) {
            return key;
        }
        if (sepIdx < 0) {
            return key.substring(0, bracketIdx);
        }
        if (bracketIdx < 0) {
            return key.substring(0, sepIdx);
        }
        return key.substring(0, Math.min(sepIdx, bracketIdx));
    }

    /**
     * Checks if a segment represents an array index (bracket or numeric).
     */
    private static boolean isArrayIndex(String segment) {
        return BRACKET_INDEX_PATTERN.matcher(segment).matches() || isNumeric(segment);
    }

    /**
     * Checks if a string is a non-negative integer.
     */
    private static boolean isNumeric(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Tokenizes a flattened key into path segments.
     */
    private static String[] tokenize(String key, String separator) {
        // First, normalize bracket notation to separator + index
        // e.g. "a[0].b[1]" -> "a.0.b.1" with separator "."
        StringBuilder normalized = new StringBuilder();
        int i = 0;
        while (i < key.length()) {
            if (key.charAt(i) == '[') {
                int close = key.indexOf(']', i);
                if (close > i + 1) {
                    String index = key.substring(i + 1, close);
                    if (isNumeric(index)) {
                        if (!normalized.isEmpty() && !endsWith(normalized, separator)) {
                            normalized.append(separator);
                        }
                        normalized.append(index);
                        i = close + 1;
                        continue;
                    }
                }
                normalized.append(key.charAt(i));
                i++;
            } else {
                normalized.append(key.charAt(i));
                i++;
            }
        }

        return splitBySeparator(normalized.toString(), separator);
    }

    /**
     * Checks if a StringBuilder ends with the given string without creating a temporary String.
     */
    private static boolean endsWith(StringBuilder sb, String suffix) {
        int sbLen = sb.length();
        int suffixLen = suffix.length();
        if (sbLen < suffixLen) {
            return false;
        }
        for (int i = 0; i < suffixLen; i++) {
            if (sb.charAt(sbLen - suffixLen + i) != suffix.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Splits a string by the separator (not regex, literal split).
     */
    private static String[] splitBySeparator(String str, String separator) {
        if (str.isEmpty()) {
            return new String[]{""};
        }
        java.util.List<String> parts = new java.util.ArrayList<>();
        int start = 0;
        int idx;
        while ((idx = str.indexOf(separator, start)) >= 0) {
            parts.add(str.substring(start, idx));
            start = idx + separator.length();
        }
        parts.add(str.substring(start));
        return parts.toArray(new String[0]);
    }

    /**
     * Sets a value at the nested path within the root container.
     */
    private static void setNestedValue(JsonNode root, String key, JsonNode value, String separator) {
        String[] tokens = tokenize(key, separator);

        JsonNode current = root;
        for (int i = 0; i < tokens.length - 1; i++) {
            String token = tokens[i];
            String nextToken = tokens[i + 1];
            boolean nextIsArray = isNumeric(nextToken);

            if (current instanceof JsonNode.ObjectNode obj) {
                JsonNode child = obj.get(token);
                if (child == null) {
                    child = nextIsArray ? JsonNode.array() : JsonNode.object();
                    obj.put(token, child);
                }
                current = child;
            } else if (current instanceof JsonNode.ArrayNode arr) {
                int index = parseIndex(token);
                ensureArraySize(arr, index + 1);
                JsonNode child = arr.get(index);
                if (child == null || child.isNull()) {
                    child = nextIsArray ? JsonNode.array() : JsonNode.object();
                    arr.set(index, child);
                }
                current = child;
            }
        }

        // Set the final value
        String lastToken = tokens[tokens.length - 1];
        if (current instanceof JsonNode.ObjectNode obj) {
            obj.put(lastToken, value);
        } else if (current instanceof JsonNode.ArrayNode arr) {
            int index = parseIndex(lastToken);
            ensureArraySize(arr, index + 1);
            arr.set(index, value);
        }
    }

    /**
     * Parses an array index from a token, wrapping NumberFormatException.
     */
    private static final int MAX_ARRAY_INDEX = 1_000;

    private static int parseIndex(String token) {
        try {
            int index = Integer.parseInt(token);
            if (index < 0 || index > MAX_ARRAY_INDEX) {
                throw new OpenJsonProcessingException(
                        "Array index out of safe range [0, " + MAX_ARRAY_INDEX + "]: " + index,
                        OpenJsonProcessingException.ErrorType.PARSE_ERROR);
            }
            return index;
        } catch (NumberFormatException e) {
            throw new OpenJsonProcessingException(
                    "Invalid array index in flattened key: " + token,
                    OpenJsonProcessingException.ErrorType.PARSE_ERROR);
        }
    }

    /**
     * Ensures an array has at least the specified number of elements,
     * padding with null nodes as needed.
     */
    private static void ensureArraySize(JsonNode.ArrayNode arr, int requiredSize) {
        int gap = requiredSize - arr.size();
        if (gap > MAX_ARRAY_INDEX + 1) {
            throw new OpenJsonProcessingException(
                    "Array index gap " + gap + " exceeds maximum of " + MAX_ARRAY_INDEX,
                    OpenJsonProcessingException.ErrorType.SERIALIZATION_ERROR);
        }
        while (arr.size() < requiredSize) {
            arr.addNull();
        }
    }
}
