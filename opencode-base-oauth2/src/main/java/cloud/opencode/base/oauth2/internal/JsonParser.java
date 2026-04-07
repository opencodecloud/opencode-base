package cloud.opencode.base.oauth2.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Internal JSON Parser Utility
 * 内部 JSON 解析工具
 *
 * <p>Lightweight JSON parser for OAuth2 protocol responses. Consolidates duplicate
 * JSON parsing logic from OAuth2Client and TokenRefresher into a single utility.</p>
 * <p>用于 OAuth2 协议响应的轻量级 JSON 解析器。将 OAuth2Client 和 TokenRefresher 中的
 * 重复 JSON 解析逻辑合并到一个工具类中。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>String field extraction with escaped quote handling - 字符串字段提取，处理转义引号</li>
 *   <li>Long/Boolean field extraction - 长整型/布尔值字段提取</li>
 *   <li>Unicode escape handling - Unicode 转义处理</li>
 *   <li>Flat JSON object parsing to Map - 扁平 JSON 对象解析为 Map</li>
 *   <li>Complete field key matching (no partial matches) - 完整字段键匹配（无部分匹配）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Extract string field
 * String token = JsonParser.getString(json, "access_token");
 *
 * // Extract long field
 * Long expiresIn = JsonParser.getLong(json, "expires_in");
 *
 * // Parse flat JSON object
 * Map<String, Object> map = JsonParser.parseObject(json);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Yes (returns null for invalid input) - 空值安全: 是（对无效输入返回 null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.3
 */
public final class JsonParser {

    private JsonParser() {
        // Utility class, no instances
    }

    /**
     * Extract a string value from a JSON field.
     * 从 JSON 字段中提取字符串值。
     *
     * <p>Handles escaped quotes, unicode escapes, and null values.</p>
     * <p>处理转义引号、Unicode 转义和 null 值。</p>
     *
     * @param json  the JSON string | JSON 字符串
     * @param field the field name | 字段名
     * @return the string value or null if not found | 字符串值，未找到则返回 null
     */
    public static String getString(String json, String field) {
        if (json == null || json.isEmpty()) return null;

        String pattern = "\"" + field + "\"";
        int idx = findFieldIndex(json, field);
        if (idx < 0) return null;

        int start = skipToValue(json, idx + pattern.length());
        if (start < 0) return null;

        // Handle null value
        if (start + 4 <= json.length() && json.regionMatches(start, "null", 0, 4)) {
            return null;
        }

        // Check if it's a string value
        if (json.charAt(start) != '"') return null;

        // Find end of string, handling escaped quotes
        return parseStringValue(json, start + 1);
    }

    /**
     * Extract a long value from a JSON field.
     * 从 JSON 字段中提取长整型值。
     *
     * @param json  the JSON string | JSON 字符串
     * @param field the field name | 字段名
     * @return the long value or null if not found | 长整型值，未找到则返回 null
     */
    public static Long getLong(String json, String field) {
        if (json == null || json.isEmpty()) return null;

        String pattern = "\"" + field + "\"";
        int idx = findFieldIndex(json, field);
        if (idx < 0) return null;

        int start = skipToValue(json, idx + pattern.length());
        if (start < 0) return null;

        // Handle null value
        if (start + 4 <= json.length() && json.regionMatches(start, "null", 0, 4)) {
            return null;
        }

        // Find end of number
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) {
            end++;
        }

        if (end > start) {
            try {
                return Long.parseLong(json.substring(start, end));
            } catch (NumberFormatException e) {
                return null;
            }
        }

        return null;
    }

    /**
     * Extract a boolean value from a JSON field.
     * 从 JSON 字段中提取布尔值。
     *
     * @param json  the JSON string | JSON 字符串
     * @param field the field name | 字段名
     * @return the boolean value or null if not found | 布尔值，未找到则返回 null
     */
    static Boolean getBoolean(String json, String field) {
        if (json == null || json.isEmpty()) return null;

        String pattern = "\"" + field + "\"";
        int idx = findFieldIndex(json, field);
        if (idx < 0) return null;

        int start = skipToValue(json, idx + pattern.length());
        if (start < 0) return null;

        // Handle null value
        if (start + 4 <= json.length() && json.regionMatches(start, "null", 0, 4)) {
            return null;
        }

        if (start + 4 <= json.length() && json.regionMatches(start, "true", 0, 4)) {
            return Boolean.TRUE;
        }
        if (start + 5 <= json.length() && json.regionMatches(start, "false", 0, 5)) {
            return Boolean.FALSE;
        }

        return null;
    }

    /**
     * Parse a flat JSON object to a Map.
     * 将扁平 JSON 对象解析为 Map。
     *
     * <p>Supports string, number (long), boolean, null values, and string arrays.
     * Nested objects are skipped.</p>
     * <p>支持字符串、数字（长整型）、布尔值、null 值和字符串数组。嵌套对象会被跳过。</p>
     *
     * @param json the JSON string | JSON 字符串
     * @return the parsed map, or empty map if null/invalid | 解析后的 Map，null/无效时返回空 Map
     */
    public static Map<String, Object> parseObject(String json) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (json == null || json.isEmpty()) return result;

        // Trim and check for object braces
        String trimmed = json.strip();
        if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) {
            return result;
        }

        // Remove outer braces
        String content = trimmed.substring(1, trimmed.length() - 1).strip();
        if (content.isEmpty()) return result;

        int pos = 0;
        while (pos < content.length()) {
            // Skip whitespace and commas
            while (pos < content.length() && (Character.isWhitespace(content.charAt(pos)) || content.charAt(pos) == ',')) {
                pos++;
            }
            if (pos >= content.length()) break;

            // Expect opening quote for key
            if (content.charAt(pos) != '"') break;
            pos++;

            // Find end of key
            int keyEnd = content.indexOf('"', pos);
            if (keyEnd < 0) break;
            String key = content.substring(pos, keyEnd);
            pos = keyEnd + 1;

            // Skip to colon
            while (pos < content.length() && Character.isWhitespace(content.charAt(pos))) pos++;
            if (pos >= content.length() || content.charAt(pos) != ':') break;
            pos++;

            // Skip whitespace
            while (pos < content.length() && Character.isWhitespace(content.charAt(pos))) pos++;
            if (pos >= content.length()) break;

            char ch = content.charAt(pos);

            if (ch == '"') {
                // String value
                String value = parseStringValue(content, pos + 1);
                result.put(key, value);
                // Advance past the string
                pos = skipPastString(content, pos);
            } else if (ch == 'n' && pos + 4 <= content.length() && content.regionMatches(pos, "null", 0, 4)) {
                result.put(key, null);
                pos += 4;
            } else if (ch == 't' && pos + 4 <= content.length() && content.regionMatches(pos, "true", 0, 4)) {
                result.put(key, Boolean.TRUE);
                pos += 4;
            } else if (ch == 'f' && pos + 5 <= content.length() && content.regionMatches(pos, "false", 0, 5)) {
                result.put(key, Boolean.FALSE);
                pos += 5;
            } else if (Character.isDigit(ch) || ch == '-') {
                // Number value
                int numEnd = pos;
                boolean isFloat = false;
                while (numEnd < content.length()) {
                    char nc = content.charAt(numEnd);
                    if (nc == '.' || nc == 'e' || nc == 'E') {
                        isFloat = true;
                        numEnd++;
                    } else if (Character.isDigit(nc) || nc == '-' || nc == '+') {
                        numEnd++;
                    } else {
                        break;
                    }
                }
                String numStr = content.substring(pos, numEnd);
                try {
                    if (isFloat) {
                        result.put(key, Double.parseDouble(numStr));
                    } else {
                        result.put(key, Long.parseLong(numStr));
                    }
                } catch (NumberFormatException e) {
                    // Skip invalid number
                }
                pos = numEnd;
            } else if (ch == '[') {
                // Parse string array
                List<String> arrayResult = new ArrayList<>();
                int arrayPos = pos + 1;
                while (arrayPos < content.length()) {
                    while (arrayPos < content.length() && Character.isWhitespace(content.charAt(arrayPos))) arrayPos++;
                    if (arrayPos >= content.length() || content.charAt(arrayPos) == ']') {
                        arrayPos++;
                        break;
                    }
                    if (content.charAt(arrayPos) == ',') {
                        arrayPos++;
                        continue;
                    }
                    if (content.charAt(arrayPos) == '"') {
                        String elem = parseStringValue(content, arrayPos + 1);
                        if (elem != null) {
                            arrayResult.add(elem);
                        }
                        arrayPos = skipPastString(content, arrayPos);
                    } else {
                        // Skip non-string array element
                        arrayPos = skipValue(content, arrayPos);
                        if (arrayPos < content.length() && content.charAt(arrayPos) == ',') arrayPos++;
                    }
                }
                result.put(key, Collections.unmodifiableList(arrayResult));
                pos = arrayPos;
            } else {
                // Skip unknown value type (objects)
                pos = skipValue(content, pos);
            }
        }

        return result;
    }

    /**
     * Unescape a JSON string value between the given indices.
     * 反转义给定索引之间的 JSON 字符串值。
     *
     * <p>Handles standard JSON escapes including unicode (backslash-uXXXX).</p>
     * <p>处理标准 JSON 转义，包括 unicode（反斜杠-uXXXX）。</p>
     *
     * @param json  the JSON string | JSON 字符串
     * @param start start index (inclusive) | 起始索引（包含）
     * @param end   end index (exclusive) | 结束索引（不包含）
     * @return the unescaped string | 反转义后的字符串
     */
    public static String unescape(String json, int start, int end) {
        StringBuilder sb = new StringBuilder(end - start);
        for (int i = start; i < end; i++) {
            char c = json.charAt(i);
            if (c == '\\' && i + 1 < end) {
                char esc = json.charAt(i + 1);
                switch (esc) {
                    case '"', '\\', '/' -> sb.append(esc);
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    case 't' -> sb.append('\t');
                    case 'b' -> sb.append('\b');
                    case 'f' -> sb.append('\f');
                    case 'u' -> {
                        if (i + 5 < end) {
                            try {
                                sb.append((char) Integer.parseInt(json.substring(i + 2, i + 6), 16));
                                i += 4;
                            } catch (NumberFormatException e) {
                                sb.append('\\');
                                sb.append(esc);
                            }
                        } else {
                            sb.append('\\');
                            sb.append(esc);
                        }
                    }
                    default -> { sb.append('\\'); sb.append(esc); }
                }
                i++; // skip escape char (loop will increment past it)
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    // ==================== Private Helper Methods ====================

    /**
     * Find the index of a JSON field key, ensuring it is a complete key match.
     * 查找 JSON 字段键的索引，确保是完整的键匹配。
     *
     * @param json  the JSON string | JSON 字符串
     * @param field the field name | 字段名称
     * @return the index of the field pattern, or -1 if not found | 字段模式的索引，未找到则返回 -1
     */
    private static int findFieldIndex(String json, String field) {
        String pattern = "\"" + field + "\"";
        int searchFrom = 0;
        while (searchFrom < json.length()) {
            int idx = json.indexOf(pattern, searchFrom);
            if (idx < 0) return -1;
            // Ensure this is not part of a larger key
            if (idx == 0 || !Character.isLetterOrDigit(json.charAt(idx - 1))) {
                // Right-boundary check: verify next non-whitespace char after pattern is ':'
                int afterPattern = idx + pattern.length();
                while (afterPattern < json.length() && Character.isWhitespace(json.charAt(afterPattern))) {
                    afterPattern++;
                }
                if (afterPattern < json.length() && json.charAt(afterPattern) == ':') {
                    return idx;
                }
                // Otherwise, this is a field name inside a string value — continue searching
            }
            searchFrom = idx + 1;
        }
        return -1;
    }

    /**
     * Skip to the value portion after finding a field key.
     * 跳到字段键之后的值部分。
     *
     * @param json     the JSON string | JSON 字符串
     * @param fieldEnd the index after the closing quote of the field key | 字段键闭合引号之后的索引
     * @return the start index of the value, or -1 if invalid | 值的起始索引，无效则返回 -1
     */
    private static int skipToValue(String json, int fieldEnd) {
        int colonIdx = json.indexOf(':', fieldEnd);
        if (colonIdx < 0) return -1;
        int start = colonIdx + 1;
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
            start++;
        }
        return start < json.length() ? start : -1;
    }

    /**
     * Parse a string value starting after the opening quote.
     * 解析从开始引号之后开始的字符串值。
     *
     * @param json  the JSON string | JSON 字符串
     * @param start the index after the opening quote | 开始引号之后的索引
     * @return the parsed string or null | 解析后的字符串或 null
     */
    private static String parseStringValue(String json, int start) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '\\' && i + 1 < json.length()) {
                char next = json.charAt(i + 1);
                switch (next) {
                    case '"', '\\', '/' -> { sb.append(next); i++; }
                    case 'n' -> { sb.append('\n'); i++; }
                    case 'r' -> { sb.append('\r'); i++; }
                    case 't' -> { sb.append('\t'); i++; }
                    case 'b' -> { sb.append('\b'); i++; }
                    case 'f' -> { sb.append('\f'); i++; }
                    case 'u' -> {
                        if (i + 5 < json.length()) {
                            String hex = json.substring(i + 2, i + 6);
                            try {
                                sb.append((char) Integer.parseInt(hex, 16));
                                i += 5;
                            } catch (NumberFormatException e) {
                                sb.append(c);
                            }
                        } else {
                            sb.append(c);
                        }
                    }
                    default -> sb.append(c);
                }
            } else if (c == '"') {
                return sb.toString();
            } else {
                sb.append(c);
            }
        }
        return null;
    }

    /**
     * Skip past a JSON string value (including opening and closing quotes).
     * 跳过 JSON 字符串值（包括开始和结束引号）。
     *
     * @param json  the JSON string | JSON 字符串
     * @param start the index of the opening quote | 开始引号的索引
     * @return the index after the closing quote | 结束引号之后的索引
     */
    private static int skipPastString(String json, int start) {
        // start points to opening quote
        for (int i = start + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '\\' && i + 1 < json.length()) {
                i++; // skip escaped char
            } else if (c == '"') {
                return i + 1;
            }
        }
        return json.length();
    }

    /**
     * Skip a JSON value (string, number, boolean, null, array, object).
     * 跳过 JSON 值（字符串、数字、布尔值、null、数组、对象）。
     *
     * @param json  the JSON string | JSON 字符串
     * @param start the start index of the value | 值的起始索引
     * @return the index after the value | 值之后的索引
     */
    private static int skipValue(String json, int start) {
        if (start >= json.length()) return json.length();
        char ch = json.charAt(start);
        if (ch == '"') return skipPastString(json, start);
        if (ch == '{') return skipBracket(json, start, '{', '}');
        if (ch == '[') return skipBracket(json, start, '[', ']');
        // primitive: skip to next comma, } or ]
        int pos = start;
        while (pos < json.length()) {
            char c = json.charAt(pos);
            if (c == ',' || c == '}' || c == ']') return pos;
            pos++;
        }
        return pos;
    }

    /**
     * Skip a bracketed JSON structure (object or array).
     * 跳过带括号的 JSON 结构（对象或数组）。
     *
     * @param json  the JSON string | JSON 字符串
     * @param start the start index | 起始索引
     * @param open  the opening bracket character | 开始括号字符
     * @param close the closing bracket character | 结束括号字符
     * @return the index after the closing bracket | 结束括号之后的索引
     */
    private static int skipBracket(String json, int start, char open, char close) {
        int depth = 0;
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '"') {
                i = skipPastString(json, i) - 1;
            } else if (c == open) {
                depth++;
            } else if (c == close) {
                depth--;
                if (depth == 0) return i + 1;
            }
        }
        return json.length();
    }
}
