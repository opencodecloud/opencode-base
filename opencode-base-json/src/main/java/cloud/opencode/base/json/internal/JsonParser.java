package cloud.opencode.base.json.internal;

import cloud.opencode.base.json.JsonNode;
import cloud.opencode.base.json.exception.OpenJsonProcessingException;

import java.math.BigDecimal;

/**
 * Built-in JSON Parser - Zero-Dependency JSON String to JsonNode Parser
 * 内置JSON解析器 - 零依赖的JSON字符串到JsonNode解析器
 *
 * <p>Recursive descent parser conforming to RFC 8259. Produces {@link JsonNode} trees.</p>
 * <p>符合 RFC 8259 的递归下降解析器。生成 {@link JsonNode} 树。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Full RFC 8259 JSON syntax support - 完整 RFC 8259 JSON 语法支持</li>
 *   <li>Unicode escape sequences (&#92;uXXXX) - Unicode 转义序列</li>
 *   <li>BigDecimal for precise number parsing - BigDecimal 精确数字解析</li>
 *   <li>Nesting depth limit (512) against stack overflow - 嵌套深度限制防止栈溢出</li>
 *   <li>Detailed error messages with position - 带位置的详细错误信息</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (create new instance per parse) - 线程安全: 否（每次解析创建新实例）</li>
 *   <li>Nesting depth limited to 512 - 嵌套深度限制为512</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) where n=input JSON string length - 时间复杂度: O(n)，n 为输入 JSON 字符串长度</li>
 *   <li>Space complexity: O(d) for the recursive call stack where d=nesting depth (max 512) - 空间复杂度: O(d) 用于递归调用栈，d 为嵌套深度（最大 512）</li>
 * </ul>
 *
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // See class-level documentation for usage
 * // 参见类级文档了解用法
 * }</pre>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
final class JsonParser {

    private static final int MAX_DEPTH = 512;

    private final String input;
    private int pos;
    private int depth;

    JsonParser(String input) {
        this.input = input;
        this.pos = 0;
        this.depth = 0;
    }

    /**
     * Parses the JSON string and returns the root JsonNode
     * 解析JSON字符串并返回根JsonNode
     *
     * @return the parsed JsonNode | 解析后的JsonNode
     */
    JsonNode parse() {
        skipWhitespace();
        if (pos >= input.length()) {
            throw error("empty input");
        }
        JsonNode result = parseValue();
        skipWhitespace();
        if (pos < input.length()) {
            throw error("unexpected trailing content");
        }
        return result;
    }

    private JsonNode parseValue() {
        skipWhitespace();
        if (pos >= input.length()) {
            throw error("unexpected end of input");
        }

        return switch (input.charAt(pos)) {
            case '{' -> parseObject();
            case '[' -> parseArray();
            case '"' -> new JsonNode.StringNode(parseString());
            case 't', 'f' -> parseBoolean();
            case 'n' -> parseNull();
            default -> {
                char c = input.charAt(pos);
                if (c == '-' || (c >= '0' && c <= '9')) {
                    yield parseNumber();
                }
                throw error("unexpected character: '" + c + "'");
            }
        };
    }

    private JsonNode.ObjectNode parseObject() {
        expect('{');
        pushDepth();
        JsonNode.ObjectNode obj = JsonNode.object();
        skipWhitespace();

        if (pos < input.length() && input.charAt(pos) == '}') {
            pos++;
            popDepth();
            return obj;
        }

        while (true) {
            skipWhitespace();
            if (pos >= input.length() || input.charAt(pos) != '"') {
                throw error("expected property name");
            }
            String key = parseString();
            skipWhitespace();
            expect(':');
            JsonNode value = parseValue();
            obj.put(key, value);

            skipWhitespace();
            if (pos >= input.length()) {
                throw error("unterminated object");
            }
            if (input.charAt(pos) == '}') {
                pos++;
                popDepth();
                return obj;
            }
            expect(',');
        }
    }

    private JsonNode.ArrayNode parseArray() {
        expect('[');
        pushDepth();
        JsonNode.ArrayNode arr = JsonNode.array();
        skipWhitespace();

        if (pos < input.length() && input.charAt(pos) == ']') {
            pos++;
            popDepth();
            return arr;
        }

        while (true) {
            arr.add(parseValue());
            skipWhitespace();
            if (pos >= input.length()) {
                throw error("unterminated array");
            }
            if (input.charAt(pos) == ']') {
                pos++;
                popDepth();
                return arr;
            }
            expect(',');
        }
    }

    private String parseString() {
        expect('"');
        StringBuilder sb = new StringBuilder();
        while (pos < input.length()) {
            char c = input.charAt(pos++);
            if (c == '"') {
                return sb.toString();
            }
            if (c == '\\') {
                if (pos >= input.length()) {
                    throw error("unterminated escape sequence");
                }
                char escaped = input.charAt(pos++);
                switch (escaped) {
                    case '"'  -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    case '/'  -> sb.append('/');
                    case 'b'  -> sb.append('\b');
                    case 'f'  -> sb.append('\f');
                    case 'n'  -> sb.append('\n');
                    case 'r'  -> sb.append('\r');
                    case 't'  -> sb.append('\t');
                    case 'u'  -> {
                        if (pos + 4 > input.length()) {
                            throw error("unterminated unicode escape");
                        }
                        String hex = input.substring(pos, pos + 4);
                        sb.append((char) Integer.parseInt(hex, 16));
                        pos += 4;
                    }
                    default -> throw error("invalid escape: \\" + escaped);
                }
            } else if (c < 0x20) {
                throw error("unescaped control character: 0x" + Integer.toHexString(c));
            } else {
                sb.append(c);
            }
        }
        throw error("unterminated string");
    }

    private JsonNode.NumberNode parseNumber() {
        int start = pos;
        if (pos < input.length() && input.charAt(pos) == '-') pos++;

        if (pos >= input.length()) throw error("unexpected end in number");

        // Integer part
        if (input.charAt(pos) == '0') {
            pos++;
        } else if (input.charAt(pos) >= '1' && input.charAt(pos) <= '9') {
            while (pos < input.length() && input.charAt(pos) >= '0' && input.charAt(pos) <= '9') pos++;
        } else {
            throw error("invalid number");
        }

        boolean isDecimal = false;

        // Fraction
        if (pos < input.length() && input.charAt(pos) == '.') {
            isDecimal = true;
            pos++;
            if (pos >= input.length() || input.charAt(pos) < '0' || input.charAt(pos) > '9') {
                throw error("invalid fraction");
            }
            while (pos < input.length() && input.charAt(pos) >= '0' && input.charAt(pos) <= '9') pos++;
        }

        // Exponent
        if (pos < input.length() && (input.charAt(pos) == 'e' || input.charAt(pos) == 'E')) {
            isDecimal = true;
            pos++;
            if (pos < input.length() && (input.charAt(pos) == '+' || input.charAt(pos) == '-')) pos++;
            if (pos >= input.length() || input.charAt(pos) < '0' || input.charAt(pos) > '9') {
                throw error("invalid exponent");
            }
            while (pos < input.length() && input.charAt(pos) >= '0' && input.charAt(pos) <= '9') pos++;
        }

        String numStr = input.substring(start, pos);
        if (isDecimal) {
            return new JsonNode.NumberNode(new BigDecimal(numStr));
        }
        try {
            long val = Long.parseLong(numStr);
            if (val >= Integer.MIN_VALUE && val <= Integer.MAX_VALUE) {
                return new JsonNode.NumberNode((int) val);
            }
            return new JsonNode.NumberNode(val);
        } catch (NumberFormatException e) {
            return new JsonNode.NumberNode(new BigDecimal(numStr));
        }
    }

    private JsonNode.BooleanNode parseBoolean() {
        if (input.startsWith("true", pos)) {
            pos += 4;
            return new JsonNode.BooleanNode(true);
        }
        if (input.startsWith("false", pos)) {
            pos += 5;
            return new JsonNode.BooleanNode(false);
        }
        throw error("expected boolean");
    }

    private JsonNode parseNull() {
        if (input.startsWith("null", pos)) {
            pos += 4;
            return JsonNode.nullNode();
        }
        throw error("expected null");
    }

    private void skipWhitespace() {
        while (pos < input.length()) {
            char c = input.charAt(pos);
            if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                pos++;
            } else {
                break;
            }
        }
    }

    private void expect(char expected) {
        if (pos >= input.length() || input.charAt(pos) != expected) {
            throw error("expected '" + expected + "'");
        }
        pos++;
    }

    private void pushDepth() {
        if (++depth > MAX_DEPTH) {
            throw error("nesting depth exceeds " + MAX_DEPTH);
        }
    }

    private void popDepth() {
        depth--;
    }

    private OpenJsonProcessingException error(String msg) {
        return OpenJsonProcessingException.parseError(
                msg + " at position " + pos, pos / 80 + 1, pos % 80 + 1);
    }
}
