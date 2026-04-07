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

import cloud.opencode.base.json.exception.OpenJsonProcessingException;

/**
 * JSON Strings - String-level JSON Utilities
 * JSON 字符串 - 字符串级 JSON 工具类
 *
 * <p>Provides low-level string manipulation utilities for JSON processing,
 * including escaping, unescaping, validation, minification, and pretty-printing.
 * These operations work directly on JSON strings without building a tree model.</p>
 * <p>提供用于 JSON 处理的低级字符串操作工具，
 * 包括转义、反转义、验证、压缩和格式化。
 * 这些操作直接在 JSON 字符串上工作，不构建树模型。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>RFC 8259 compliant string escaping and unescaping - 符合 RFC 8259 的字符串转义和反转义</li>
 *   <li>Fast JSON validation without tree construction - 快速 JSON 验证，无需构建树</li>
 *   <li>Whitespace removal (minification) - 空白移除（压缩）</li>
 *   <li>Pretty-printing with configurable indentation - 可配置缩进的格式化</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Escape a string for use inside a JSON value
 * String escaped = JsonStrings.escape("Hello\nWorld");
 * // Result: Hello\nWorld
 *
 * // Unescape a JSON string value
 * String unescaped = JsonStrings.unescape("Hello\\nWorld");
 * // Result: Hello\nWorld
 *
 * // Validate JSON
 * boolean valid = JsonStrings.isValid("{\"key\":\"value\"}"); // true
 *
 * // Minify JSON
 * String minified = JsonStrings.minify("{ \"a\" : 1 }");
 * // Result: {"a":1}
 *
 * // Pretty-print JSON
 * String pretty = JsonStrings.prettyPrint("{\"a\":1,\"b\":[1,2]}");
 * }</pre>
 *
 * <p><strong>Performance | 性能:</strong></p>
 * <ul>
 *   <li>All operations are single-pass with O(n) complexity - 所有操作都是单遍 O(n) 复杂度</li>
 *   <li>No intermediate tree or object construction - 无中间树或对象构建</li>
 *   <li>StringBuilder-based output for minimal allocation - 基于 StringBuilder 的输出，最小化分配</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 *   <li>Input validation prevents injection - 输入验证防止注入</li>
 *   <li>Depth limit in validation/pretty-print prevents stack overflow - 验证/格式化中的深度限制防止栈溢出</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see <a href="https://www.rfc-editor.org/rfc/rfc8259">RFC 8259</a>
 * @since JDK 25, opencode-base-json V1.0.3
 */
public final class JsonStrings {

    /**
     * Maximum nesting depth for validation and formatting.
     * 验证和格式化的最大嵌套深度。
     */
    private static final int MAX_DEPTH = 1000;
    private static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();

    /**
     * Default indentation string (2 spaces).
     * 默认缩进字符串（2个空格）。
     */
    private static final String DEFAULT_INDENT = "  ";

    private JsonStrings() {
        // Utility class - 工具类
    }

    // ==================== Escape / Unescape ====================

    /**
     * Escapes a string value for use inside a JSON string (RFC 8259).
     * 转义字符串值以在 JSON 字符串中使用 (RFC 8259)。
     *
     * <p>Escape rules:</p>
     * <ul>
     *   <li>{@code "} → {@code \"}</li>
     *   <li>{@code \} → {@code \\}</li>
     *   <li>U+0008 → {@code \b}</li>
     *   <li>U+000C → {@code \f}</li>
     *   <li>U+000A → {@code \n}</li>
     *   <li>U+000D → {@code \r}</li>
     *   <li>U+0009 → {@code \t}</li>
     *   <li>Other control chars (U+0000-U+001F) are escaped as unicode escapes</li>
     *   <li>{@code /} is NOT escaped (optional per RFC 8259)</li>
     * </ul>
     *
     * @param value the string to escape - 要转义的字符串
     * @return the escaped string (without surrounding quotes) - 转义后的字符串（不含外围引号）
     * @throws OpenJsonProcessingException if value is null - 如果值为 null
     */
    public static String escape(String value) {
        if (value == null) {
            throw new OpenJsonProcessingException(
                    "Cannot escape null string",
                    OpenJsonProcessingException.ErrorType.SERIALIZATION_ERROR);
        }
        // Fast path: scan for chars needing escape. Most strings have none.
        int firstEscape = -1;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c < 0x20 || c == '"' || c == '\\') {
                firstEscape = i;
                break;
            }
        }
        if (firstEscape < 0) {
            return value; // No escaping needed — zero allocation
        }
        // Copy the clean prefix, then escape from firstEscape onward
        StringBuilder sb = new StringBuilder(value.length() + 16);
        sb.append(value, 0, firstEscape);
        for (int i = firstEscape; i < value.length(); i++) {
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
                        sb.append('\\').append('u').append(HEX_DIGITS[(c >> 12) & 0xF]).append(HEX_DIGITS[(c >> 8) & 0xF]).append(HEX_DIGITS[(c >> 4) & 0xF]).append(HEX_DIGITS[c & 0xF]);
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }

    /**
     * Unescapes a JSON string value (reverse of escape).
     * 反转义 JSON 字符串值（escape 的逆操作）。
     *
     * <p>Handles all JSON escape sequences including unicode escapes
     * and surrogate pairs.</p>
     * <p>处理所有 JSON 转义序列，包括 Unicode 转义和代理对。</p>
     *
     * @param value the escaped string to unescape (without surrounding quotes) -
     *              要反转义的字符串（不含外围引号）
     * @return the unescaped string - 反转义后的字符串
     * @throws OpenJsonProcessingException if value is null or contains invalid escape sequences -
     *                                      如果值为 null 或包含无效的转义序列
     */
    public static String unescape(String value) {
        if (value == null) {
            throw new OpenJsonProcessingException(
                    "Cannot unescape null string",
                    OpenJsonProcessingException.ErrorType.PARSE_ERROR);
        }
        if (value.indexOf('\\') < 0) {
            return value; // Fast path: no escapes
        }
        StringBuilder sb = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c != '\\') {
                sb.append(c);
                continue;
            }
            // Escape sequence
            i++;
            if (i >= value.length()) {
                throw new OpenJsonProcessingException(
                        "Incomplete escape sequence at end of string",
                        OpenJsonProcessingException.ErrorType.PARSE_ERROR);
            }
            char next = value.charAt(i);
            switch (next) {
                case '"' -> sb.append('"');
                case '\\' -> sb.append('\\');
                case '/' -> sb.append('/');
                case 'b' -> sb.append('\b');
                case 'f' -> sb.append('\f');
                case 'n' -> sb.append('\n');
                case 'r' -> sb.append('\r');
                case 't' -> sb.append('\t');
                case 'u' -> {
                    if (i + 4 >= value.length()) {
                        throw new OpenJsonProcessingException(
                                "Incomplete unicode escape at position " + (i - 1),
                                OpenJsonProcessingException.ErrorType.PARSE_ERROR);
                    }
                    String hex = value.substring(i + 1, i + 5);
                    int codePoint;
                    try {
                        codePoint = Integer.parseInt(hex, 16);
                    } catch (NumberFormatException e) {
                        throw new OpenJsonProcessingException(
                                "Invalid unicode escape: " + '\\' + "u" + hex,
                                OpenJsonProcessingException.ErrorType.PARSE_ERROR);
                    }
                    i += 4;

                    // Check for surrogate pair
                    if (Character.isHighSurrogate((char) codePoint)) {
                        // Expect low surrogate next
                        if (i + 1 < value.length() && value.charAt(i + 1) == '\\'
                                && i + 2 < value.length() && value.charAt(i + 2) == 'u') {
                            if (i + 6 < value.length()) {
                                String lowHex = value.substring(i + 3, i + 7);
                                int lowSurrogate;
                                try {
                                    lowSurrogate = Integer.parseInt(lowHex, 16);
                                } catch (NumberFormatException e) {
                                    throw new OpenJsonProcessingException(
                                            "Invalid unicode escape in surrogate pair: " + '\\' + "u" + lowHex,
                                            OpenJsonProcessingException.ErrorType.PARSE_ERROR);
                                }
                                if (Character.isLowSurrogate((char) lowSurrogate)) {
                                    int combined = Character.toCodePoint(
                                            (char) codePoint, (char) lowSurrogate);
                                    sb.appendCodePoint(combined);
                                    i += 6; // Skip unicode escape
                                    continue;
                                }
                            }
                        }
                        // Lone high surrogate — output as-is
                        sb.append((char) codePoint);
                    } else {
                        sb.append((char) codePoint);
                    }
                }
                default -> throw new OpenJsonProcessingException(
                        "Invalid escape sequence: \\" + next,
                        OpenJsonProcessingException.ErrorType.PARSE_ERROR);
            }
        }
        return sb.toString();
    }

    // ==================== Validation ====================

    /**
     * Validates whether the input is valid JSON per RFC 8259.
     * 验证输入是否为有效的 RFC 8259 JSON。
     *
     * <p>Uses a simple state machine parser without building a tree.
     * Returns false for null, empty, or blank input.</p>
     * <p>使用简单的状态机解析器，不构建树。
     * 对于 null、空或空白输入返回 false。</p>
     *
     * @param json the string to validate - 要验证的字符串
     * @return true if the input is valid JSON - 如果输入是有效的 JSON 则返回 true
     */
    public static boolean isValid(String json) {
        if (json == null || json.isBlank()) {
            return false;
        }
        try {
            int[] pos = {0};
            skipWhitespace(json, pos);
            if (pos[0] >= json.length()) {
                return false;
            }
            if (!parseValue(json, pos, 0)) {
                return false;
            }
            skipWhitespace(json, pos);
            return pos[0] == json.length(); // No trailing content
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== Minify / Pretty-Print ====================

    /**
     * Removes all unnecessary whitespace from a JSON string.
     * 移除 JSON 字符串中所有不必要的空白。
     *
     * <p>Parses the JSON and re-serializes it without whitespace.
     * Throws an exception if the input is not valid JSON.</p>
     * <p>解析 JSON 并重新序列化，不带空白。
     * 如果输入不是有效的 JSON，则抛出异常。</p>
     *
     * @param json the JSON string to minify - 要压缩的 JSON 字符串
     * @return the minified JSON string - 压缩后的 JSON 字符串
     * @throws OpenJsonProcessingException if the input is null or invalid JSON -
     *                                      如果输入为 null 或无效的 JSON
     */
    public static String minify(String json) {
        if (json == null) {
            throw new OpenJsonProcessingException(
                    "Cannot minify null string",
                    OpenJsonProcessingException.ErrorType.PARSE_ERROR);
        }
        StringBuilder sb = new StringBuilder(json.length());
        int[] pos = {0};
        skipWhitespace(json, pos);
        if (pos[0] >= json.length()) {
            throw new OpenJsonProcessingException(
                    "Cannot minify empty JSON",
                    OpenJsonProcessingException.ErrorType.PARSE_ERROR);
        }
        emitValue(json, pos, sb, 0);
        skipWhitespace(json, pos);
        if (pos[0] != json.length()) {
            throw new OpenJsonProcessingException(
                    "Unexpected content after JSON value at position " + pos[0],
                    OpenJsonProcessingException.ErrorType.PARSE_ERROR);
        }
        return sb.toString();
    }

    /**
     * Pretty-prints a JSON string with 2-space indentation.
     * 使用 2 空格缩进格式化 JSON 字符串。
     *
     * @param json the JSON string to format - 要格式化的 JSON 字符串
     * @return the formatted JSON string - 格式化后的 JSON 字符串
     * @throws OpenJsonProcessingException if the input is null or invalid JSON -
     *                                      如果输入为 null 或无效的 JSON
     */
    public static String prettyPrint(String json) {
        return prettyPrint(json, DEFAULT_INDENT);
    }

    /**
     * Pretty-prints a JSON string with the specified indentation.
     * 使用指定缩进格式化 JSON 字符串。
     *
     * @param json   the JSON string to format - 要格式化的 JSON 字符串
     * @param indent the indentation string (e.g., "  " or "\t") - 缩进字符串
     * @return the formatted JSON string - 格式化后的 JSON 字符串
     * @throws OpenJsonProcessingException if the input is null or invalid JSON -
     *                                      如果输入为 null 或无效的 JSON
     */
    public static String prettyPrint(String json, String indent) {
        if (json == null) {
            throw new OpenJsonProcessingException(
                    "Cannot pretty-print null string",
                    OpenJsonProcessingException.ErrorType.PARSE_ERROR);
        }
        if (indent == null) {
            throw new OpenJsonProcessingException(
                    "Indent must not be null",
                    OpenJsonProcessingException.ErrorType.PARSE_ERROR);
        }
        StringBuilder sb = new StringBuilder(json.length() * 2);
        int[] pos = {0};
        skipWhitespace(json, pos);
        if (pos[0] >= json.length()) {
            throw new OpenJsonProcessingException(
                    "Cannot pretty-print empty JSON",
                    OpenJsonProcessingException.ErrorType.PARSE_ERROR);
        }
        emitValuePretty(json, pos, sb, 0, indent);
        skipWhitespace(json, pos);
        if (pos[0] != json.length()) {
            throw new OpenJsonProcessingException(
                    "Unexpected content after JSON value at position " + pos[0],
                    OpenJsonProcessingException.ErrorType.PARSE_ERROR);
        }
        return sb.toString();
    }

    // ==================== Validation Helpers ====================

    /**
     * Parses a JSON value at the current position for validation.
     * 在当前位置解析 JSON 值用于验证。
     */
    private static boolean parseValue(String json, int[] pos, int depth) {
        if (depth > MAX_DEPTH) {
            return false;
        }
        skipWhitespace(json, pos);
        if (pos[0] >= json.length()) {
            return false;
        }
        char c = json.charAt(pos[0]);
        return switch (c) {
            case '{' -> parseObject(json, pos, depth);
            case '[' -> parseArray(json, pos, depth);
            case '"' -> parseString(json, pos);
            case 't' -> parseLiteral(json, pos, "true");
            case 'f' -> parseLiteral(json, pos, "false");
            case 'n' -> parseLiteral(json, pos, "null");
            default -> {
                if (c == '-' || (c >= '0' && c <= '9')) {
                    yield parseNumber(json, pos);
                }
                yield false;
            }
        };
    }

    private static boolean parseObject(String json, int[] pos, int depth) {
        pos[0]++; // skip '{'
        skipWhitespace(json, pos);
        if (pos[0] >= json.length()) {
            return false;
        }
        if (json.charAt(pos[0]) == '}') {
            pos[0]++;
            return true;
        }
        while (true) {
            skipWhitespace(json, pos);
            if (pos[0] >= json.length() || json.charAt(pos[0]) != '"') {
                return false;
            }
            if (!parseString(json, pos)) {
                return false;
            }
            skipWhitespace(json, pos);
            if (pos[0] >= json.length() || json.charAt(pos[0]) != ':') {
                return false;
            }
            pos[0]++;
            if (!parseValue(json, pos, depth + 1)) {
                return false;
            }
            skipWhitespace(json, pos);
            if (pos[0] >= json.length()) {
                return false;
            }
            char next = json.charAt(pos[0]);
            if (next == '}') {
                pos[0]++;
                return true;
            }
            if (next != ',') {
                return false;
            }
            pos[0]++;
        }
    }

    private static boolean parseArray(String json, int[] pos, int depth) {
        pos[0]++; // skip '['
        skipWhitespace(json, pos);
        if (pos[0] >= json.length()) {
            return false;
        }
        if (json.charAt(pos[0]) == ']') {
            pos[0]++;
            return true;
        }
        while (true) {
            if (!parseValue(json, pos, depth + 1)) {
                return false;
            }
            skipWhitespace(json, pos);
            if (pos[0] >= json.length()) {
                return false;
            }
            char next = json.charAt(pos[0]);
            if (next == ']') {
                pos[0]++;
                return true;
            }
            if (next != ',') {
                return false;
            }
            pos[0]++;
        }
    }

    private static boolean parseString(String json, int[] pos) {
        if (pos[0] >= json.length() || json.charAt(pos[0]) != '"') {
            return false;
        }
        pos[0]++; // skip opening quote
        while (pos[0] < json.length()) {
            char c = json.charAt(pos[0]);
            if (c == '"') {
                pos[0]++;
                return true;
            }
            if (c == '\\') {
                pos[0]++;
                if (pos[0] >= json.length()) {
                    return false;
                }
                char esc = json.charAt(pos[0]);
                if (esc == 'u') {
                    // Expect 4 hex digits
                    if (pos[0] + 4 >= json.length()) {
                        return false;
                    }
                    for (int j = 1; j <= 4; j++) {
                        char h = json.charAt(pos[0] + j);
                        if (!isHexDigit(h)) {
                            return false;
                        }
                    }
                    pos[0] += 4;
                } else if (esc != '"' && esc != '\\' && esc != '/' && esc != 'b'
                        && esc != 'f' && esc != 'n' && esc != 'r' && esc != 't') {
                    return false;
                }
            } else if (c < 0x20) {
                // Unescaped control character
                return false;
            }
            pos[0]++;
        }
        return false; // Unterminated string
    }

    private static boolean parseNumber(String json, int[] pos) {
        int start = pos[0];
        // Optional minus
        if (pos[0] < json.length() && json.charAt(pos[0]) == '-') {
            pos[0]++;
        }
        // Integer part
        if (pos[0] >= json.length()) {
            return false;
        }
        if (json.charAt(pos[0]) == '0') {
            pos[0]++;
        } else if (json.charAt(pos[0]) >= '1' && json.charAt(pos[0]) <= '9') {
            pos[0]++;
            while (pos[0] < json.length() && json.charAt(pos[0]) >= '0' && json.charAt(pos[0]) <= '9') {
                pos[0]++;
            }
        } else {
            return false;
        }
        // Fraction
        if (pos[0] < json.length() && json.charAt(pos[0]) == '.') {
            pos[0]++;
            if (pos[0] >= json.length() || json.charAt(pos[0]) < '0' || json.charAt(pos[0]) > '9') {
                return false;
            }
            while (pos[0] < json.length() && json.charAt(pos[0]) >= '0' && json.charAt(pos[0]) <= '9') {
                pos[0]++;
            }
        }
        // Exponent
        if (pos[0] < json.length() && (json.charAt(pos[0]) == 'e' || json.charAt(pos[0]) == 'E')) {
            pos[0]++;
            if (pos[0] < json.length() && (json.charAt(pos[0]) == '+' || json.charAt(pos[0]) == '-')) {
                pos[0]++;
            }
            if (pos[0] >= json.length() || json.charAt(pos[0]) < '0' || json.charAt(pos[0]) > '9') {
                return false;
            }
            while (pos[0] < json.length() && json.charAt(pos[0]) >= '0' && json.charAt(pos[0]) <= '9') {
                pos[0]++;
            }
        }
        return pos[0] > start;
    }

    private static boolean parseLiteral(String json, int[] pos, String literal) {
        if (pos[0] + literal.length() > json.length()) {
            return false;
        }
        for (int i = 0; i < literal.length(); i++) {
            if (json.charAt(pos[0] + i) != literal.charAt(i)) {
                return false;
            }
        }
        pos[0] += literal.length();
        return true;
    }

    // ==================== Emit Helpers (Minify) ====================

    /**
     * Emits a JSON value in minified form.
     * 以压缩形式输出 JSON 值。
     */
    private static void emitValue(String json, int[] pos, StringBuilder sb, int depth) {
        if (depth > MAX_DEPTH) {
            throw new OpenJsonProcessingException(
                    "JSON nesting depth exceeds maximum of " + MAX_DEPTH,
                    OpenJsonProcessingException.ErrorType.PARSE_ERROR);
        }
        skipWhitespace(json, pos);
        if (pos[0] >= json.length()) {
            throw parseError(json, pos[0]);
        }
        char c = json.charAt(pos[0]);
        switch (c) {
            case '{' -> emitObject(json, pos, sb, depth);
            case '[' -> emitArray(json, pos, sb, depth);
            case '"' -> emitString(json, pos, sb);
            case 't' -> emitLiteral(json, pos, sb, "true");
            case 'f' -> emitLiteral(json, pos, sb, "false");
            case 'n' -> emitLiteral(json, pos, sb, "null");
            default -> {
                if (c == '-' || (c >= '0' && c <= '9')) {
                    emitNumber(json, pos, sb);
                } else {
                    throw parseError(json, pos[0]);
                }
            }
        }
    }

    private static void emitObject(String json, int[] pos, StringBuilder sb, int depth) {
        sb.append('{');
        pos[0]++; // skip '{'
        skipWhitespace(json, pos);
        if (pos[0] < json.length() && json.charAt(pos[0]) == '}') {
            pos[0]++;
            sb.append('}');
            return;
        }
        boolean first = true;
        while (true) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            skipWhitespace(json, pos);
            emitString(json, pos, sb); // key
            skipWhitespace(json, pos);
            expect(json, pos, ':');
            sb.append(':');
            emitValue(json, pos, sb, depth + 1);
            skipWhitespace(json, pos);
            if (pos[0] >= json.length()) {
                throw parseError(json, pos[0]);
            }
            if (json.charAt(pos[0]) == '}') {
                pos[0]++;
                sb.append('}');
                return;
            }
            expect(json, pos, ',');
        }
    }

    private static void emitArray(String json, int[] pos, StringBuilder sb, int depth) {
        sb.append('[');
        pos[0]++; // skip '['
        skipWhitespace(json, pos);
        if (pos[0] < json.length() && json.charAt(pos[0]) == ']') {
            pos[0]++;
            sb.append(']');
            return;
        }
        boolean first = true;
        while (true) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            emitValue(json, pos, sb, depth + 1);
            skipWhitespace(json, pos);
            if (pos[0] >= json.length()) {
                throw parseError(json, pos[0]);
            }
            if (json.charAt(pos[0]) == ']') {
                pos[0]++;
                sb.append(']');
                return;
            }
            expect(json, pos, ',');
        }
    }

    private static void emitString(String json, int[] pos, StringBuilder sb) {
        if (pos[0] >= json.length() || json.charAt(pos[0]) != '"') {
            throw parseError(json, pos[0]);
        }
        sb.append('"');
        pos[0]++; // skip opening quote
        while (pos[0] < json.length()) {
            char c = json.charAt(pos[0]);
            if (c == '"') {
                sb.append('"');
                pos[0]++;
                return;
            }
            if (c == '\\') {
                sb.append(c);
                pos[0]++;
                if (pos[0] >= json.length()) {
                    throw parseError(json, pos[0]);
                }
                char esc = json.charAt(pos[0]);
                sb.append(esc);
                if (esc == 'u') {
                    // Copy 4 hex digits
                    for (int j = 0; j < 4; j++) {
                        pos[0]++;
                        if (pos[0] >= json.length()) {
                            throw parseError(json, pos[0]);
                        }
                        sb.append(json.charAt(pos[0]));
                    }
                }
                pos[0]++;
                continue;
            }
            sb.append(c);
            pos[0]++;
        }
        throw parseError(json, pos[0]); // Unterminated string
    }

    private static void emitNumber(String json, int[] pos, StringBuilder sb) {
        int start = pos[0];
        // Optional minus
        if (pos[0] < json.length() && json.charAt(pos[0]) == '-') {
            pos[0]++;
        }
        // Integer part — require at least one digit
        if (pos[0] >= json.length() || json.charAt(pos[0]) < '0' || json.charAt(pos[0]) > '9') {
            throw parseError(json, pos[0]);
        }
        if (json.charAt(pos[0]) == '0') {
            pos[0]++;
        } else {
            while (pos[0] < json.length() && json.charAt(pos[0]) >= '0' && json.charAt(pos[0]) <= '9') {
                pos[0]++;
            }
        }
        // Fraction
        if (pos[0] < json.length() && json.charAt(pos[0]) == '.') {
            pos[0]++;
            while (pos[0] < json.length() && json.charAt(pos[0]) >= '0' && json.charAt(pos[0]) <= '9') {
                pos[0]++;
            }
        }
        // Exponent
        if (pos[0] < json.length() && (json.charAt(pos[0]) == 'e' || json.charAt(pos[0]) == 'E')) {
            pos[0]++;
            if (pos[0] < json.length() && (json.charAt(pos[0]) == '+' || json.charAt(pos[0]) == '-')) {
                pos[0]++;
            }
            while (pos[0] < json.length() && json.charAt(pos[0]) >= '0' && json.charAt(pos[0]) <= '9') {
                pos[0]++;
            }
        }
        sb.append(json, start, pos[0]);
    }

    private static void emitLiteral(String json, int[] pos, StringBuilder sb, String literal) {
        for (int i = 0; i < literal.length(); i++) {
            if (pos[0] + i >= json.length() || json.charAt(pos[0] + i) != literal.charAt(i)) {
                throw parseError(json, pos[0]);
            }
        }
        sb.append(literal);
        pos[0] += literal.length();
    }

    // ==================== Emit Helpers (Pretty-Print) ====================

    /**
     * Emits a JSON value in pretty-printed form.
     * 以格式化形式输出 JSON 值。
     */
    private static void emitValuePretty(String json, int[] pos, StringBuilder sb,
                                        int depth, String indent) {
        if (depth > MAX_DEPTH) {
            throw new OpenJsonProcessingException(
                    "JSON nesting depth exceeds maximum of " + MAX_DEPTH,
                    OpenJsonProcessingException.ErrorType.PARSE_ERROR);
        }
        skipWhitespace(json, pos);
        if (pos[0] >= json.length()) {
            throw parseError(json, pos[0]);
        }
        char c = json.charAt(pos[0]);
        switch (c) {
            case '{' -> emitObjectPretty(json, pos, sb, depth, indent);
            case '[' -> emitArrayPretty(json, pos, sb, depth, indent);
            case '"' -> emitString(json, pos, sb); // Strings don't need extra formatting
            case 't' -> emitLiteral(json, pos, sb, "true");
            case 'f' -> emitLiteral(json, pos, sb, "false");
            case 'n' -> emitLiteral(json, pos, sb, "null");
            default -> {
                if (c == '-' || (c >= '0' && c <= '9')) {
                    emitNumber(json, pos, sb);
                } else {
                    throw parseError(json, pos[0]);
                }
            }
        }
    }

    private static void emitObjectPretty(String json, int[] pos, StringBuilder sb,
                                         int depth, String indent) {
        pos[0]++; // skip '{'
        skipWhitespace(json, pos);
        if (pos[0] < json.length() && json.charAt(pos[0]) == '}') {
            pos[0]++;
            sb.append("{}");
            return;
        }
        sb.append("{\n");
        boolean first = true;
        while (true) {
            if (!first) {
                sb.append(",\n");
            }
            first = false;
            appendIndent(sb, depth + 1, indent);
            skipWhitespace(json, pos);
            emitString(json, pos, sb); // key
            sb.append(": ");
            skipWhitespace(json, pos);
            expect(json, pos, ':');
            emitValuePretty(json, pos, sb, depth + 1, indent);
            skipWhitespace(json, pos);
            if (pos[0] >= json.length()) {
                throw parseError(json, pos[0]);
            }
            if (json.charAt(pos[0]) == '}') {
                pos[0]++;
                sb.append('\n');
                appendIndent(sb, depth, indent);
                sb.append('}');
                return;
            }
            expect(json, pos, ',');
        }
    }

    private static void emitArrayPretty(String json, int[] pos, StringBuilder sb,
                                        int depth, String indent) {
        pos[0]++; // skip '['
        skipWhitespace(json, pos);
        if (pos[0] < json.length() && json.charAt(pos[0]) == ']') {
            pos[0]++;
            sb.append("[]");
            return;
        }
        sb.append("[\n");
        boolean first = true;
        while (true) {
            if (!first) {
                sb.append(",\n");
            }
            first = false;
            appendIndent(sb, depth + 1, indent);
            emitValuePretty(json, pos, sb, depth + 1, indent);
            skipWhitespace(json, pos);
            if (pos[0] >= json.length()) {
                throw parseError(json, pos[0]);
            }
            if (json.charAt(pos[0]) == ']') {
                pos[0]++;
                sb.append('\n');
                appendIndent(sb, depth, indent);
                sb.append(']');
                return;
            }
            expect(json, pos, ',');
        }
    }

    // ==================== Common Helpers ====================

    private static void skipWhitespace(String json, int[] pos) {
        while (pos[0] < json.length()) {
            char c = json.charAt(pos[0]);
            if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                pos[0]++;
            } else {
                break;
            }
        }
    }

    private static void expect(String json, int[] pos, char expected) {
        if (pos[0] >= json.length() || json.charAt(pos[0]) != expected) {
            throw new OpenJsonProcessingException(
                    "Expected '" + expected + "' at position " + pos[0],
                    OpenJsonProcessingException.ErrorType.PARSE_ERROR);
        }
        pos[0]++;
    }

    private static boolean isHexDigit(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }

    private static void appendIndent(StringBuilder sb, int depth, String indent) {
        for (int i = 0; i < depth; i++) {
            sb.append(indent);
        }
    }

    private static OpenJsonProcessingException parseError(String json, int position) {
        return new OpenJsonProcessingException(
                "Invalid JSON at position " + position,
                OpenJsonProcessingException.ErrorType.PARSE_ERROR);
    }
}
