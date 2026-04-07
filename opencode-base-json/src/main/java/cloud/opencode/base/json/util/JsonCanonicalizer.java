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
import cloud.opencode.base.json.OpenJson;
import cloud.opencode.base.json.exception.OpenJsonProcessingException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * JSON Canonicalizer - RFC 8785 JSON Canonicalization Scheme (JCS)
 * JSON 规范化器 - RFC 8785 JSON 规范化方案 (JCS)
 *
 * <p>Implements the JSON Canonicalization Scheme defined in RFC 8785,
 * producing deterministic JSON output suitable for digital signatures,
 * hashing, and comparison.</p>
 * <p>实现 RFC 8785 中定义的 JSON 规范化方案，
 * 生成适用于数字签名、哈希和比较的确定性 JSON 输出。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Object keys sorted by UTF-16 code unit values - 对象键按 UTF-16 代码单元值排序</li>
 *   <li>No whitespace between tokens - 标记之间无空白</li>
 *   <li>ECMAScript ES6 number serialization - ECMAScript ES6 数字序列化</li>
 *   <li>Minimal string escaping (only required escapes) - 最小化字符串转义</li>
 *   <li>Recursive with configurable depth limit (1000) - 递归处理，深度限制 1000</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Canonicalize a JsonNode
 * JsonNode node = JsonNode.object().put("b", 2).put("a", 1);
 * String canonical = JsonCanonicalizer.canonicalize(node);
 * // Result: {"a":1,"b":2}
 *
 * // Canonicalize a JSON string
 * String canonical = JsonCanonicalizer.canonicalize("{\"b\":2,\"a\":1}");
 * // Result: {"a":1,"b":2}
 * }</pre>
 *
 * <p><strong>Performance | 性能:</strong></p>
 * <ul>
 *   <li>Single-pass tree traversal with StringBuilder output - 单遍树遍历，StringBuilder 输出</li>
 *   <li>No intermediate object creation for primitives - 原始类型无中间对象创建</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 *   <li>Depth limit prevents stack overflow from deeply nested input - 深度限制防止深层嵌套输入的栈溢出</li>
 *   <li>Rejects NaN and Infinity (not valid JSON) - 拒绝 NaN 和 Infinity（非法 JSON）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see <a href="https://www.rfc-editor.org/rfc/rfc8785">RFC 8785</a>
 * @since JDK 25, opencode-base-json V1.0.3
 */
public final class JsonCanonicalizer {

    /**
     * Maximum nesting depth for recursive canonicalization.
     * 递归规范化的最大嵌套深度。
     */
    private static final int MAX_DEPTH = 1000;
    private static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();

    private JsonCanonicalizer() {
        // Utility class - 工具类
    }

    /**
     * Canonicalizes a JsonNode according to RFC 8785 JCS.
     * 按照 RFC 8785 JCS 规范化 JsonNode。
     *
     * @param node the JSON node to canonicalize - 要规范化的 JSON 节点
     * @return the canonicalized JSON string - 规范化后的 JSON 字符串
     * @throws OpenJsonProcessingException if the node is null, contains NaN/Infinity,
     *                                      or exceeds depth limit -
     *                                      如果节点为 null、包含 NaN/Infinity 或超过深度限制
     */
    public static String canonicalize(JsonNode node) {
        if (node == null) {
            throw new OpenJsonProcessingException(
                    "Cannot canonicalize null node",
                    OpenJsonProcessingException.ErrorType.SERIALIZATION_ERROR);
        }
        StringBuilder sb = new StringBuilder();
        writeNode(node, sb, 0);
        return sb.toString();
    }

    /**
     * Canonicalizes a JSON string according to RFC 8785 JCS.
     * 按照 RFC 8785 JCS 规范化 JSON 字符串。
     *
     * <p>Convenience method that parses the input string into a tree
     * and then canonicalizes it.</p>
     * <p>便捷方法，先将输入字符串解析为树，再进行规范化。</p>
     *
     * @param json the JSON string to canonicalize - 要规范化的 JSON 字符串
     * @return the canonicalized JSON string - 规范化后的 JSON 字符串
     * @throws OpenJsonProcessingException if the input is null, invalid JSON,
     *                                      contains NaN/Infinity, or exceeds depth limit -
     *                                      如果输入为 null、无效 JSON、包含 NaN/Infinity 或超过深度限制
     */
    public static String canonicalize(String json) {
        if (json == null) {
            throw new OpenJsonProcessingException(
                    "Cannot canonicalize null string",
                    OpenJsonProcessingException.ErrorType.SERIALIZATION_ERROR);
        }
        JsonNode node = OpenJson.parse(json);
        return canonicalize(node);
    }

    /**
     * Writes a JsonNode to the StringBuilder in canonical form.
     * 将 JsonNode 以规范化形式写入 StringBuilder。
     *
     * @param node  the node to write - 要写入的节点
     * @param sb    the output buffer - 输出缓冲区
     * @param depth the current nesting depth - 当前嵌套深度
     */
    private static void writeNode(JsonNode node, StringBuilder sb, int depth) {
        if (depth > MAX_DEPTH) {
            throw new OpenJsonProcessingException(
                    "JSON nesting depth exceeds maximum of " + MAX_DEPTH,
                    OpenJsonProcessingException.ErrorType.SERIALIZATION_ERROR);
        }

        switch (node) {
            case JsonNode.ObjectNode obj -> writeObject(obj, sb, depth);
            case JsonNode.ArrayNode arr -> writeArray(arr, sb, depth);
            case JsonNode.StringNode str -> writeString(str.value(), sb);
            case JsonNode.NumberNode num -> writeNumber(num.value(), sb);
            case JsonNode.BooleanNode bool -> sb.append(bool.value());
            case JsonNode.NullNode ignored -> sb.append("null");
        }
    }

    /**
     * Writes an object node with keys sorted by UTF-16 code unit values.
     * 将对象节点的键按 UTF-16 代码单元值排序后写入。
     *
     * @param obj   the object node - 对象节点
     * @param sb    the output buffer - 输出缓冲区
     * @param depth the current nesting depth - 当前嵌套深度
     */
    private static void writeObject(JsonNode.ObjectNode obj, StringBuilder sb, int depth) {
        sb.append('{');
        var keySet = obj.keys();
        int size = keySet.size();

        if (size == 0) {
            sb.append('}');
            return;
        }

        if (size == 1) {
            // Single key: no sorting or list allocation needed
            String key = keySet.iterator().next();
            writeString(key, sb);
            sb.append(':');
            writeNode(obj.get(key), sb, depth + 1);
            sb.append('}');
            return;
        }

        // Java String.compareTo uses UTF-16 code unit order, which matches RFC 8785
        List<String> keys = new ArrayList<>(keySet);
        Collections.sort(keys);

        for (int i = 0; i < keys.size(); i++) {
            if (i > 0) sb.append(',');
            String key = keys.get(i);
            writeString(key, sb);
            sb.append(':');
            writeNode(obj.get(key), sb, depth + 1);
        }
        sb.append('}');
    }

    /**
     * Writes an array node.
     * 写入数组节点。
     *
     * @param arr   the array node - 数组节点
     * @param sb    the output buffer - 输出缓冲区
     * @param depth the current nesting depth - 当前嵌套深度
     */
    private static void writeArray(JsonNode.ArrayNode arr, StringBuilder sb, int depth) {
        sb.append('[');
        for (int i = 0; i < arr.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            writeNode(arr.get(i), sb, depth + 1);
        }
        sb.append(']');
    }

    /**
     * Writes a string value with RFC 8785 escaping rules.
     * 按 RFC 8785 转义规则写入字符串值。
     *
     * <p>Only control characters (U+0000-U+001F), backslash, and double quote
     * are escaped. All other characters are output as-is.</p>
     * <p>仅转义控制字符 (U+0000-U+001F)、反斜杠和双引号。
     * 其他所有字符按原样输出。</p>
     *
     * @param value the string value - 字符串值
     * @param sb    the output buffer - 输出缓冲区
     */
    private static void writeString(String value, StringBuilder sb) {
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

    /**
     * Writes a number value following ECMAScript ES6 serialization rules.
     * 按 ECMAScript ES6 序列化规则写入数字值。
     *
     * <p>Rules:</p>
     * <ul>
     *   <li>NaN and Infinity throw an exception - NaN 和 Infinity 抛出异常</li>
     *   <li>-0 is serialized as 0 - -0 序列化为 0</li>
     *   <li>Integers have no decimal point - 整数无小数点</li>
     *   <li>Non-integers use minimal representation - 非整数使用最小表示</li>
     *   <li>Very large/small numbers use exponential notation - 极大/极小数使用指数表示</li>
     * </ul>
     *
     * @param value the number value - 数字值
     * @param sb    the output buffer - 输出缓冲区
     */
    private static void writeNumber(Number value, StringBuilder sb) {
        if (value instanceof Double d) {
            writeDouble(d, sb);
        } else if (value instanceof Float f) {
            writeDouble(f.doubleValue(), sb);
        } else if (value instanceof BigDecimal bd) {
            writeBigDecimal(bd, sb);
        } else if (value instanceof BigInteger bi) {
            sb.append(bi.toString());
        } else {
            // Integer, Long, Short, Byte — all integral
            sb.append(value.longValue());
        }
    }

    /**
     * Writes a double value following ES6 Number serialization.
     * 按 ES6 Number 序列化写入 double 值。
     *
     * @param d  the double value - double 值
     * @param sb the output buffer - 输出缓冲区
     */
    private static void writeDouble(double d, StringBuilder sb) {
        if (Double.isNaN(d) || Double.isInfinite(d)) {
            throw new OpenJsonProcessingException(
                    "Cannot canonicalize NaN or Infinity",
                    OpenJsonProcessingException.ErrorType.SERIALIZATION_ERROR);
        }

        // Handle -0 -> 0
        if (d == 0.0) {
            sb.append('0');
            return;
        }

        // Use ES6 number-to-string algorithm
        // Java's Double.toString follows similar rules but needs adjustment
        String result = es6NumberToString(d);
        sb.append(result);
    }

    /**
     * Converts a double to its ES6 / RFC 8785 string representation.
     * 将 double 转换为其 ES6 / RFC 8785 字符串表示。
     *
     * <p>Uses {@link Double#toString(double)} which produces the shortest round-trip
     * representation (Ryu algorithm since JDK 17), then reformats the notation to
     * match ES6 {@code Number.prototype.toString()} rules:</p>
     * <ul>
     *   <li>Integers in range (-1e21, 1e21): no decimal point (e.g. {@code 100})</li>
     *   <li>Non-integers: minimal decimal (e.g. {@code 1.5})</li>
     *   <li>Exponential: lowercase {@code e} with explicit sign (e.g. {@code 1e+21}, {@code 1e-7})</li>
     * </ul>
     *
     * @param d the double value (not NaN, not Infinity, not zero) - double 值
     * @return the ES6 string representation - ES6 字符串表示
     */
    private static String es6NumberToString(double d) {
        // Java's Double.toString already uses Ryu (shortest round-trip).
        // We only need to reformat its output to match ES6 conventions.
        String javaStr = Double.toString(d);

        int eIndex = javaStr.indexOf('E');
        if (eIndex >= 0) {
            int exp = Integer.parseInt(javaStr.substring(eIndex + 1));
            // ES6 rule: integers in range (-1e21, 1e21) use plain decimal form.
            // Java may render them in E notation (e.g., 1e20 → "1.0E20").
            // If the value is an integer and |value| < 1e21, expand to plain form.
            if (exp >= 0 && d == Math.floor(d) && Math.abs(d) < 1e21) {
                // Safe to cast: abs(d) < 1e21 ≤ Long.MAX_VALUE (~9.2e18) is not
                // always true (1e19, 1e20 > Long.MAX_VALUE), so use BigDecimal.
                return new BigDecimal(javaStr).toBigInteger().toString();
            }
            return reformatExponential(javaStr, eIndex);
        }

        // Non-exponential: Java outputs "1.5", "100.0", "-3.14", etc.
        // ES6 rule: if the value is an integer, strip ".0"
        if (javaStr.endsWith(".0")) {
            return javaStr.substring(0, javaStr.length() - 2);
        }

        return javaStr;
    }

    /**
     * Reformats Java's exponential notation to ES6 format.
     * 将 Java 的指数表示法重新格式化为 ES6 格式。
     *
     * <p>Java: {@code 1.5E21}, {@code 1.0E-7} → ES6: {@code 1.5e+21}, {@code 1e-7}</p>
     *
     * @param javaStr the Java string (contains 'E') - Java 字符串
     * @param eIndex  the index of 'E' - 'E' 的索引
     * @return the ES6 exponential notation - ES6 指数表示法
     */
    private static String reformatExponential(String javaStr, int eIndex) {
        String mantissa = javaStr.substring(0, eIndex);
        int exp = Integer.parseInt(javaStr.substring(eIndex + 1));

        // Strip trailing ".0" from mantissa (e.g. "1.0" → "1")
        if (mantissa.endsWith(".0")) {
            mantissa = mantissa.substring(0, mantissa.length() - 2);
        }

        // Build ES6 result with lowercase 'e' and explicit '+' for positive exponents
        return mantissa + "e" + (exp >= 0 ? "+" : "") + exp;
    }

    /**
     * Writes a BigDecimal value, converting to integer form if possible.
     * 写入 BigDecimal 值，如果可能则转换为整数形式。
     *
     * @param bd the BigDecimal value - BigDecimal 值
     * @param sb the output buffer - 输出缓冲区
     */
    private static void writeBigDecimal(BigDecimal bd, StringBuilder sb) {
        // Strip trailing zeros
        bd = bd.stripTrailingZeros();

        // Check if it's effectively an integer
        if (bd.scale() <= 0) {
            sb.append(bd.toBigInteger().toString());
        } else {
            sb.append(bd.toPlainString());
        }
    }
}
