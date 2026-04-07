package cloud.opencode.base.yml.transform;

import cloud.opencode.base.yml.OpenYml;
import cloud.opencode.base.yml.exception.OpenYmlException;

import java.util.List;
import java.util.Map;

/**
 * YmlJson - YAML to JSON bidirectional converter with no external dependencies
 * YmlJson - 无外部依赖的 YAML 与 JSON 双向转换器
 *
 * <p>Converts between YAML and JSON formats using only JDK built-in capabilities.
 * JSON generation is done via StringBuilder; JSON parsing leverages the fact that
 * JSON is valid YAML 1.2, so the existing YAML parser handles it.</p>
 * <p>使用纯 JDK 内置能力在 YAML 和 JSON 格式之间进行转换。
 * JSON 生成通过 StringBuilder 完成；JSON 解析利用 JSON 是有效 YAML 1.2 的特性，
 * 由现有 YAML 解析器处理。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>YAML string to JSON string conversion - YAML 字符串到 JSON 字符串转换</li>
 *   <li>Map to JSON string conversion - Map 到 JSON 字符串转换</li>
 *   <li>JSON string to YAML Map conversion - JSON 字符串到 YAML Map 转换</li>
 *   <li>JSON string to YAML string conversion - JSON 字符串到 YAML 字符串转换</li>
 *   <li>Pretty-print support with 2-space indentation - 2 空格缩进的美化打印</li>
 *   <li>Proper JSON string escaping (security critical) - 正确的 JSON 字符串转义（安全关键）</li>
 *   <li>Max depth limit of 50 to prevent stack overflow - 最大深度 50 限制防止栈溢出</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // YAML to JSON
 * String json = YmlJson.toJson("server:\n  port: 8080");
 * // => {"server":{"port":8080}}
 *
 * // Pretty JSON
 * String pretty = YmlJson.toJson("server:\n  port: 8080", true);
 *
 * // JSON to YAML Map
 * Map<String, Object> data = YmlJson.fromJson("{\"port\": 8080}");
 *
 * // JSON to YAML string
 * String yaml = YmlJson.fromJsonToYaml("{\"port\": 8080}");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>All JSON special characters properly escaped - 所有 JSON 特殊字符正确转义</li>
 *   <li>Depth limit prevents stack overflow from deeply nested structures - 深度限制防止深层嵌套导致的栈溢出</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.3
 */
public final class YmlJson {

    /**
     * Maximum nesting depth to prevent stack overflow.
     * 防止栈溢出的最大嵌套深度。
     */
    private static final int MAX_DEPTH = 50;

    /**
     * Indentation string for pretty printing (2 spaces).
     * 美化打印的缩进字符串（2 个空格）。
     */
    private static final String INDENT = "  ";

    /**
     * Pre-built hex escape sequences for control characters (U+0000 to U+001F).
     * Avoids String.format() allocation in hot path.
     * 预构建的控制字符十六进制转义序列（U+0000 到 U+001F），避免热路径中 String.format() 分配。
     */
    private static final String[] CONTROL_CHAR_ESCAPES = new String[0x20];

    static {
        for (int i = 0; i < 0x20; i++) {
            CONTROL_CHAR_ESCAPES[i] = "\\u" + String.format("%04x", i);
        }
    }

    private YmlJson() {
        throw new AssertionError("Utility class - do not instantiate");
    }

    // ================================
    // YAML to JSON
    // ================================

    /**
     * Converts a YAML string to a compact JSON string.
     * 将 YAML 字符串转换为紧凑 JSON 字符串。
     *
     * @param yaml the YAML string | YAML 字符串
     * @return the JSON string | JSON 字符串
     * @throws OpenYmlException if the YAML is invalid or exceeds depth limit | 如果 YAML 无效或超过深度限制
     */
    public static String toJson(String yaml) {
        return toJson(yaml, false);
    }

    /**
     * Converts a YAML string to a JSON string with optional pretty-printing.
     * 将 YAML 字符串转换为 JSON 字符串，可选美化打印。
     *
     * @param yaml   the YAML string | YAML 字符串
     * @param pretty whether to pretty-print the output | 是否美化打印输出
     * @return the JSON string | JSON 字符串
     * @throws OpenYmlException if the YAML is invalid or exceeds depth limit | 如果 YAML 无效或超过深度限制
     */
    public static String toJson(String yaml, boolean pretty) {
        if (yaml == null) {
            throw new OpenYmlException("YAML input must not be null");
        }
        Map<String, Object> data = OpenYml.load(yaml);
        return toJson(data, pretty);
    }

    /**
     * Converts a Map to a compact JSON string.
     * 将 Map 转换为紧凑 JSON 字符串。
     *
     * @param data the data map | 数据映射
     * @return the JSON string | JSON 字符串
     * @throws OpenYmlException if the data exceeds depth limit | 如果数据超过深度限制
     */
    public static String toJson(Map<String, Object> data) {
        return toJson(data, false);
    }

    /**
     * Converts a Map to a JSON string with optional pretty-printing.
     * 将 Map 转换为 JSON 字符串，可选美化打印。
     *
     * @param data   the data map | 数据映射
     * @param pretty whether to pretty-print the output | 是否美化打印输出
     * @return the JSON string | JSON 字符串
     * @throws OpenYmlException if the data exceeds depth limit | 如果数据超过深度限制
     */
    public static String toJson(Map<String, Object> data, boolean pretty) {
        if (data == null) {
            throw new OpenYmlException("Data must not be null");
        }
        StringBuilder sb = new StringBuilder();
        writeValue(sb, data, 0, pretty);
        return sb.toString();
    }

    // ================================
    // JSON to YAML
    // ================================

    /**
     * Parses a JSON string to a YAML-compatible Map.
     * 将 JSON 字符串解析为 YAML 兼容的 Map。
     *
     * <p>Since JSON is valid YAML 1.2, this method uses the existing YAML parser.</p>
     * <p>由于 JSON 是有效的 YAML 1.2，此方法使用现有的 YAML 解析器。</p>
     *
     * @param json the JSON string | JSON 字符串
     * @return the parsed map | 解析后的映射
     * @throws OpenYmlException if the JSON is invalid | 如果 JSON 无效
     */
    public static Map<String, Object> fromJson(String json) {
        if (json == null) {
            throw new OpenYmlException("JSON input must not be null");
        }
        return OpenYml.load(json);
    }

    /**
     * Converts a JSON string to a YAML string.
     * 将 JSON 字符串转换为 YAML 字符串。
     *
     * @param json the JSON string | JSON 字符串
     * @return the YAML string | YAML 字符串
     * @throws OpenYmlException if the JSON is invalid | 如果 JSON 无效
     */
    public static String fromJsonToYaml(String json) {
        if (json == null) {
            throw new OpenYmlException("JSON input must not be null");
        }
        Map<String, Object> data = OpenYml.load(json);
        return OpenYml.dump(data);
    }

    // ================================
    // Internal JSON generation
    // ================================

    /**
     * Writes a value to the StringBuilder as JSON.
     * 将值作为 JSON 写入 StringBuilder。
     *
     * @param sb     the string builder | 字符串构建器
     * @param value  the value to write | 要写入的值
     * @param depth  the current nesting depth | 当前嵌套深度
     * @param pretty whether to pretty-print | 是否美化打印
     */
    @SuppressWarnings("unchecked")
    private static void writeValue(StringBuilder sb, Object value, int depth, boolean pretty) {
        if (depth > MAX_DEPTH) {
            throw new OpenYmlException("JSON serialization exceeds maximum depth of " + MAX_DEPTH);
        }

        if (value == null) {
            sb.append("null");
        } else if (value instanceof Map<?, ?> map) {
            writeMap(sb, (Map<String, Object>) map, depth, pretty);
        } else if (value instanceof List<?> list) {
            writeList(sb, list, depth, pretty);
        } else if (value instanceof String str) {
            writeString(sb, str);
        } else if (value instanceof Boolean bool) {
            sb.append(bool ? "true" : "false");
        } else if (value instanceof Number number) {
            writeNumber(sb, number);
        } else {
            // Fallback: treat as string
            writeString(sb, value.toString());
        }
    }

    /**
     * Writes a Map as a JSON object.
     * 将 Map 写为 JSON 对象。
     */
    private static void writeMap(StringBuilder sb, Map<String, Object> map, int depth, boolean pretty) {
        sb.append('{');
        if (map.isEmpty()) {
            sb.append('}');
            return;
        }

        int newDepth = depth + 1;
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) {
                sb.append(',');
            }
            first = false;

            if (pretty) {
                sb.append('\n');
                appendIndent(sb, newDepth);
            }

            writeString(sb, entry.getKey());
            sb.append(':');
            if (pretty) {
                sb.append(' ');
            }
            writeValue(sb, entry.getValue(), newDepth, pretty);
        }

        if (pretty) {
            sb.append('\n');
            appendIndent(sb, depth);
        }
        sb.append('}');
    }

    /**
     * Writes a List as a JSON array.
     * 将 List 写为 JSON 数组。
     */
    private static void writeList(StringBuilder sb, List<?> list, int depth, boolean pretty) {
        sb.append('[');
        if (list.isEmpty()) {
            sb.append(']');
            return;
        }

        int newDepth = depth + 1;
        boolean first = true;
        for (Object item : list) {
            if (!first) {
                sb.append(',');
            }
            first = false;

            if (pretty) {
                sb.append('\n');
                appendIndent(sb, newDepth);
            }
            writeValue(sb, item, newDepth, pretty);
        }

        if (pretty) {
            sb.append('\n');
            appendIndent(sb, depth);
        }
        sb.append(']');
    }

    /**
     * Writes a JSON-escaped string.
     * 写入 JSON 转义的字符串。
     *
     * <p>Escapes all required JSON special characters per RFC 8259:</p>
     * <ul>
     *   <li>{@code "} - quotation mark</li>
     *   <li>{@code \} - reverse solidus</li>
     *   <li>{@code /} - solidus (not escaped; optional per RFC 8259, YAML parsers don't support \/)</li>
     *   <li>{@code \b} - backspace</li>
     *   <li>{@code \f} - form feed</li>
     *   <li>{@code \n} - newline</li>
     *   <li>{@code \r} - carriage return</li>
     *   <li>{@code \t} - tab</li>
     *   <li>All control characters (U+0000 to U+001F) - Unicode escape</li>
     * </ul>
     */
    private static void writeString(StringBuilder sb, String str) {
        sb.append('"');
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '/' -> sb.append("/");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(CONTROL_CHAR_ESCAPES[c]);
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        sb.append('"');
    }

    /**
     * Writes a number value, handling special floating-point cases.
     * 写入数值，处理特殊浮点情况。
     */
    private static void writeNumber(StringBuilder sb, Number number) {
        if (number instanceof Double d) {
            if (d.isInfinite() || d.isNaN()) {
                sb.append("null");
            } else {
                long longVal = d.longValue();
                if (d == (double) longVal) {
                    sb.append(longVal);
                } else {
                    sb.append(d);
                }
            }
        } else if (number instanceof Float f) {
            if (f.isInfinite() || f.isNaN()) {
                sb.append("null");
            } else {
                long longVal = f.longValue();
                if (f == (float) longVal) {
                    sb.append(longVal);
                } else {
                    sb.append(f);
                }
            }
        } else {
            sb.append(number);
        }
    }

    /**
     * Appends indentation for pretty-printing.
     * 为美化打印添加缩进。
     */
    private static void appendIndent(StringBuilder sb, int depth) {
        for (int i = 0; i < depth; i++) {
            sb.append(INDENT);
        }
    }
}
