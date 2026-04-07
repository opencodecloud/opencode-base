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

import java.util.ArrayList;
import java.util.List;

/**
 * JSON Truncator - Truncates Large JSON for Logging
 * JSON 截断器 - 截断大型 JSON 用于日志记录
 *
 * <p>Provides utilities to truncate large JSON structures for safe logging,
 * debugging, and display purposes. Supports both string-level truncation
 * (fast, may produce invalid JSON) and tree-level truncation (produces
 * valid JSON with truncation markers).</p>
 * <p>提供截断大型 JSON 结构的工具，用于安全的日志记录、调试和显示。
 * 支持字符串级截断（快速，可能产生无效 JSON）和树级截断
 * （产生带截断标记的有效 JSON）。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>String-level truncation for quick logging - 字符串级截断用于快速日志记录</li>
 *   <li>Tree-level truncation with configurable limits - 可配置限制的树级截断</li>
 *   <li>Summary generation for node type and size - 节点类型和大小的摘要生成</li>
 *   <li>Configurable via {@link TruncateConfig} record - 通过 TruncateConfig 记录配置</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // String-level truncation
 * String result = JsonTruncator.truncate(longJson, 200);
 *
 * // Tree-level truncation with default config
 * String result = JsonTruncator.truncate(node, TruncateConfig.DEFAULT);
 *
 * // Tree-level truncation with custom config
 * TruncateConfig config = new TruncateConfig(512, 5, 50, 3, "...");
 * String result = JsonTruncator.truncate(node, config);
 *
 * // Quick summary
 * String summary = JsonTruncator.summary(node); // "Object{5 properties}"
 * }</pre>
 *
 * <p><strong>Performance | 性能:</strong></p>
 * <ul>
 *   <li>String truncation is O(1) - no parsing - 字符串截断为 O(1)，无需解析</li>
 *   <li>Tree truncation is O(n) bounded by config limits - 树截断为 O(n)，受配置限制约束</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 *   <li>Depth-limited to prevent stack overflow - 深度限制防止栈溢出</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.3
 */
public final class JsonTruncator {

    private static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();

    private JsonTruncator() {
        // Utility class - 工具类
    }

    /**
     * Truncation configuration record.
     * 截断配置记录。
     *
     * @param maxLength        soft limit for output characters; actual output may be slightly larger
     *                          due to structural closing brackets (default 1024) -
     *                          输出字符数的软限制；实际输出可能因结构闭合括号而略大（默认 1024）
     * @param maxArrayElements maximum array items to show (default 3) - 显示的最大数组项数
     * @param maxStringLength  maximum string value characters (default 100) - 最大字符串值字符数
     * @param maxDepth         maximum nesting depth (default 5) - 最大嵌套深度
     * @param truncationMarker the truncation marker string (default "...") - 截断标记字符串
     */
    public record TruncateConfig(
            int maxLength,
            int maxArrayElements,
            int maxStringLength,
            int maxDepth,
            String truncationMarker
    ) {
        /**
         * Default truncation configuration.
         * 默认截断配置。
         */
        public static final TruncateConfig DEFAULT = new TruncateConfig(1024, 3, 100, 5, "...");

        /**
         * Validates the configuration parameters.
         * 验证配置参数。
         */
        public TruncateConfig {
            if (maxLength < 1) {
                throw new IllegalArgumentException("maxLength must be positive");
            }
            if (maxArrayElements < 0) {
                throw new IllegalArgumentException("maxArrayElements must be non-negative");
            }
            if (maxStringLength < 1) {
                throw new IllegalArgumentException("maxStringLength must be positive");
            }
            if (maxDepth < 1) {
                throw new IllegalArgumentException("maxDepth must be positive");
            }
            if (truncationMarker == null) {
                throw new IllegalArgumentException("truncationMarker must not be null");
            }
        }
    }

    /**
     * Truncates a JSON string to the specified maximum length.
     * 将 JSON 字符串截断到指定的最大长度。
     *
     * <p>If the JSON string length is within maxLength, it is returned as-is.
     * Otherwise, it is truncated at maxLength and the marker {@code "...(truncated)"}
     * is appended. The result may not be valid JSON.</p>
     * <p>如果 JSON 字符串长度在 maxLength 以内，则原样返回。
     * 否则，在 maxLength 处截断并追加标记 {@code "...(truncated)"}。
     * 结果可能不是有效 JSON。</p>
     *
     * @param json      the JSON string to truncate - 要截断的 JSON 字符串
     * @param maxLength the maximum length of the output - 输出的最大长度
     * @return the truncated string - 截断后的字符串
     * @throws OpenJsonProcessingException if maxLength is not positive -
     *                                      如果 maxLength 不是正数
     */
    public static String truncate(String json, int maxLength) {
        if (maxLength < 1) {
            throw new OpenJsonProcessingException(
                    "maxLength must be positive, got: " + maxLength,
                    OpenJsonProcessingException.ErrorType.SERIALIZATION_ERROR);
        }
        if (json == null) {
            return "null";
        }
        if (json.length() <= maxLength) {
            return json;
        }
        return json.substring(0, maxLength) + "...(truncated)";
    }

    /**
     * Truncates a JsonNode tree according to the specified configuration.
     * 根据指定配置截断 JsonNode 树。
     *
     * <p>Produces valid JSON with truncation markers as string values.
     * Arrays are trimmed to maxArrayElements with a marker indicating remaining count.
     * Strings are trimmed to maxStringLength with the truncation marker appended.
     * Objects beyond maxDepth are replaced with a depth marker.</p>
     * <p>生成带有截断标记字符串值的有效 JSON。
     * 数组被截断到 maxArrayElements 项，带有剩余计数标记。
     * 字符串被截断到 maxStringLength，附加截断标记。
     * 超过 maxDepth 的对象被深度标记替换。</p>
     *
     * @param node   the JSON node to truncate - 要截断的 JSON 节点
     * @param config the truncation configuration - 截断配置
     * @return the truncated JSON string - 截断后的 JSON 字符串
     * @throws OpenJsonProcessingException if node or config is null -
     *                                      如果节点或配置为 null
     */
    public static String truncate(JsonNode node, TruncateConfig config) {
        if (node == null) {
            throw new OpenJsonProcessingException(
                    "Cannot truncate null node",
                    OpenJsonProcessingException.ErrorType.SERIALIZATION_ERROR);
        }
        if (config == null) {
            throw new OpenJsonProcessingException(
                    "TruncateConfig must not be null",
                    OpenJsonProcessingException.ErrorType.SERIALIZATION_ERROR);
        }
        StringBuilder sb = new StringBuilder();
        writeNodeTruncated(node, sb, 0, config);
        return sb.toString();
    }

    /**
     * Returns a brief summary of the node's type and size.
     * 返回节点类型和大小的简要摘要。
     *
     * <p>Examples:</p>
     * <ul>
     *   <li>{@code Object{5 properties}}</li>
     *   <li>{@code Array[100 elements]}</li>
     *   <li>{@code String(50 chars)}</li>
     *   <li>{@code Number(42)}</li>
     *   <li>{@code Boolean(true)}</li>
     *   <li>{@code null}</li>
     * </ul>
     *
     * @param node the JSON node to summarize - 要摘要的 JSON 节点
     * @return the summary string - 摘要字符串
     */
    public static String summary(JsonNode node) {
        if (node == null) {
            return "null";
        }
        return switch (node) {
            case JsonNode.ObjectNode obj ->
                    "Object{" + obj.size() + (obj.size() == 1 ? " property" : " properties") + "}";
            case JsonNode.ArrayNode arr ->
                    "Array[" + arr.size() + (arr.size() == 1 ? " element" : " elements") + "]";
            case JsonNode.StringNode str ->
                    "String(" + str.value().length() + (str.value().length() == 1 ? " char" : " chars") + ")";
            case JsonNode.NumberNode num ->
                    "Number(" + num.value() + ")";
            case JsonNode.BooleanNode bool ->
                    "Boolean(" + bool.value() + ")";
            case JsonNode.NullNode ignored ->
                    "null";
        };
    }

    /**
     * Writes a truncated representation of a node to the StringBuilder.
     * 将节点的截断表示写入 StringBuilder。
     *
     * @param node   the node to write - 要写入的节点
     * @param sb     the output buffer - 输出缓冲区
     * @param depth  the current nesting depth - 当前嵌套深度
     * @param config the truncation configuration - 截断配置
     */
    private static void writeNodeTruncated(JsonNode node, StringBuilder sb, int depth,
                                           TruncateConfig config) {
        switch (node) {
            case JsonNode.ObjectNode obj -> writeObjectTruncated(obj, sb, depth, config);
            case JsonNode.ArrayNode arr -> writeArrayTruncated(arr, sb, depth, config);
            case JsonNode.StringNode str -> writeStringTruncated(str.value(), sb, config);
            case JsonNode.NumberNode num -> sb.append(num.value());
            case JsonNode.BooleanNode bool -> sb.append(bool.value());
            case JsonNode.NullNode ignored -> sb.append("null");
        }
    }

    /**
     * Writes a truncated object node.
     * 写入截断的对象节点。
     */
    private static void writeObjectTruncated(JsonNode.ObjectNode obj, StringBuilder sb,
                                             int depth, TruncateConfig config) {
        if (depth >= config.maxDepth()) {
            sb.append("\"").append(config.truncationMarker())
                    .append("(Object with ").append(obj.size())
                    .append(obj.size() == 1 ? " property" : " properties").append(")\"");
            return;
        }
        sb.append('{');
        boolean first = true;
        for (String key : obj.keys()) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            writeJsonString(key, sb);
            sb.append(':');
            writeNodeTruncated(obj.get(key), sb, depth + 1, config);
        }
        sb.append('}');
    }

    /**
     * Writes a truncated array node.
     * 写入截断的数组节点。
     */
    private static void writeArrayTruncated(JsonNode.ArrayNode arr, StringBuilder sb,
                                            int depth, TruncateConfig config) {
        if (depth >= config.maxDepth()) {
            sb.append("\"").append(config.truncationMarker())
                    .append("(Array with ").append(arr.size())
                    .append(arr.size() == 1 ? " element" : " elements").append(")\"");
            return;
        }
        sb.append('[');
        int limit = Math.min(arr.size(), config.maxArrayElements());
        for (int i = 0; i < limit; i++) {
            if (i > 0) {
                sb.append(',');
            }
            writeNodeTruncated(arr.get(i), sb, depth + 1, config);
        }
        int remaining = arr.size() - limit;
        if (remaining > 0) {
            if (limit > 0) {
                sb.append(',');
            }
            sb.append("\"").append(config.truncationMarker())
                    .append("(").append(remaining).append(" more)\"");
        }
        sb.append(']');
    }

    /**
     * Writes a truncated string value.
     * 写入截断的字符串值。
     */
    private static void writeStringTruncated(String value, StringBuilder sb,
                                             TruncateConfig config) {
        if (value.length() <= config.maxStringLength()) {
            writeJsonString(value, sb);
        } else {
            String truncated = value.substring(0, config.maxStringLength()) + config.truncationMarker();
            writeJsonString(truncated, sb);
        }
    }

    /**
     * Writes a properly escaped JSON string.
     * 写入正确转义的 JSON 字符串。
     */
    private static void writeJsonString(String value, StringBuilder sb) {
        sb.append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append("\\u").append(HEX_DIGITS[(c >> 12) & 0xF]).append(HEX_DIGITS[(c >> 8) & 0xF]).append(HEX_DIGITS[(c >> 4) & 0xF]).append(HEX_DIGITS[c & 0xF]);
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        sb.append('"');
    }
}
