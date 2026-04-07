package cloud.opencode.base.pdf.internal.parser;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * PDF Content Stream Parser - Parses page content streams into operator sequences
 * PDF 内容流解析器 - 将页面内容流解析为操作符序列
 *
 * <p>Parses the content stream operators used in PDF page descriptions. Focuses on
 * text-related operators for text extraction, while also capturing graphics state
 * operators for proper text positioning.</p>
 * <p>解析 PDF 页面描述中使用的内容流操作符。重点解析文本相关操作符以支持文本提取，
 * 同时捕获图形状态操作符以确保正确的文本定位。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Text operators: BT, ET, Tf, Tm, Td, TD, T*, Tj, TJ, ', " - 文本操作符</li>
 *   <li>Graphics state: cm, q, Q - 图形状态</li>
 *   <li>Operand stack management - 操作数栈管理</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes — stateless static method - 线程安全: 是 — 无状态静态方法</li>
 *   <li>Bounded by input stream size - 受输入流大小限制</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.3
 */
public final class ContentStreamParser {

    private static final int MAX_OPERAND_STACK = 1000;

    private ContentStreamParser() {
        // Utility class
    }

    /**
     * Operator with name and operands
     * 操作符及其操作数
     *
     * @param name     operator name | 操作符名称
     * @param operands operand list | 操作数列表
     */
    public record Operator(String name, List<PdfObject> operands) {
        public Operator {
            Objects.requireNonNull(name, "name cannot be null");
            Objects.requireNonNull(operands, "operands cannot be null");
            operands = List.copyOf(operands);
        }
    }

    /**
     * Parses content stream data into a list of operators
     * 将内容流数据解析为操作符列表
     *
     * @param streamData content stream bytes | 内容流字节
     * @return list of operators | 操作符列表
     */
    public static List<Operator> parse(byte[] streamData) {
        Objects.requireNonNull(streamData, "streamData cannot be null");

        List<Operator> operators = new ArrayList<>();
        List<PdfObject> operandStack = new ArrayList<>();

        int pos = 0;
        int length = streamData.length;

        while (pos < length) {
            // Skip whitespace
            while (pos < length && isWhitespace(streamData[pos] & 0xFF)) {
                pos++;
            }
            if (pos >= length) break;

            int b = streamData[pos] & 0xFF;

            // Comment
            if (b == '%') {
                while (pos < length && streamData[pos] != '\n' && streamData[pos] != '\r') {
                    pos++;
                }
                continue;
            }

            // Number
            if (b == '+' || b == '-' || b == '.' || (b >= '0' && b <= '9')) {
                int start = pos;
                pos = readNumberToken(streamData, pos);
                String numStr = new String(streamData, start, pos - start, StandardCharsets.US_ASCII);
                try {
                    operandStack.add(new PdfObject.PdfNumber(Double.parseDouble(numStr)));
                } catch (NumberFormatException e) {
                    operandStack.add(new PdfObject.PdfNumber(0));
                }
                continue;
            }

            // Literal string
            if (b == '(') {
                pos++;
                StringBuilder sb = new StringBuilder();
                int depth = 1;
                while (pos < length && depth > 0) {
                    int c = streamData[pos++] & 0xFF;
                    if (c == '\\' && pos < length) {
                        int next = streamData[pos++] & 0xFF;
                        switch (next) {
                            case 'n' -> sb.append('\n');
                            case 'r' -> sb.append('\r');
                            case 't' -> sb.append('\t');
                            case 'b' -> sb.append('\b');
                            case 'f' -> sb.append('\f');
                            case '(' -> sb.append('(');
                            case ')' -> sb.append(')');
                            case '\\' -> sb.append('\\');
                            default -> {
                                if (next >= '0' && next <= '7') {
                                    int octal = next - '0';
                                    if (pos < length && (streamData[pos] & 0xFF) >= '0' && (streamData[pos] & 0xFF) <= '7') {
                                        octal = (octal << 3) | ((streamData[pos++] & 0xFF) - '0');
                                        if (pos < length && (streamData[pos] & 0xFF) >= '0' && (streamData[pos] & 0xFF) <= '7') {
                                            octal = (octal << 3) | ((streamData[pos++] & 0xFF) - '0');
                                        }
                                    }
                                    sb.append((char) (octal & 0xFF));
                                } else {
                                    sb.append((char) next);
                                }
                            }
                        }
                    } else if (c == '(') {
                        depth++;
                        sb.append('(');
                    } else if (c == ')') {
                        depth--;
                        if (depth > 0) sb.append(')');
                    } else {
                        sb.append((char) c);
                    }
                }
                operandStack.add(new PdfObject.PdfString(sb.toString()));
                continue;
            }

            // Hex string
            if (b == '<') {
                pos++;
                StringBuilder hex = new StringBuilder();
                while (pos < length && (streamData[pos] & 0xFF) != '>') {
                    int c = streamData[pos++] & 0xFF;
                    if (!isWhitespace(c)) {
                        hex.append((char) c);
                    }
                }
                if (pos < length) pos++; // skip '>'
                String hexStr = hex.toString();
                if (hexStr.length() % 2 != 0) hexStr += "0";
                StringBuilder decoded = new StringBuilder();
                for (int i = 0; i < hexStr.length(); i += 2) {
                    int hi = hexDigit(hexStr.charAt(i));
                    int lo = hexDigit(hexStr.charAt(i + 1));
                    decoded.append((char) ((Math.max(hi, 0) << 4) | Math.max(lo, 0)));
                }
                operandStack.add(new PdfObject.PdfString(decoded.toString()));
                continue;
            }

            // Name
            if (b == '/') {
                pos++;
                StringBuilder name = new StringBuilder();
                while (pos < length) {
                    int c = streamData[pos] & 0xFF;
                    if (isWhitespace(c) || isDelimiter(c)) break;
                    name.append((char) c);
                    pos++;
                }
                operandStack.add(new PdfObject.PdfName(name.toString()));
                continue;
            }

            // Array
            if (b == '[') {
                pos++;
                List<PdfObject> arrayElements = new ArrayList<>();
                while (pos < length) {
                    while (pos < length && isWhitespace(streamData[pos] & 0xFF)) pos++;
                    if (pos >= length || (streamData[pos] & 0xFF) == ']') {
                        if (pos < length) pos++;
                        break;
                    }
                    int elemB = streamData[pos] & 0xFF;
                    if (elemB == '(' || elemB == '<') {
                        // Parse inline string within array
                        int savedStackSize = operandStack.size();
                        // Temporarily re-parse from this position using a sub-approach
                        int subStart = pos;
                        // Use a simpler inline parse
                        InlineElement elem = parseInlineElement(streamData, pos);
                        if (elem != null) {
                            arrayElements.add(elem.value());
                            pos = elem.endPos();
                        } else {
                            pos++;
                        }
                    } else if (elemB == '+' || elemB == '-' || elemB == '.' || (elemB >= '0' && elemB <= '9')) {
                        int start = pos;
                        pos = readNumberToken(streamData, pos);
                        String numStr = new String(streamData, start, pos - start, StandardCharsets.US_ASCII);
                        try {
                            arrayElements.add(new PdfObject.PdfNumber(Double.parseDouble(numStr)));
                        } catch (NumberFormatException e) {
                            arrayElements.add(new PdfObject.PdfNumber(0));
                        }
                    } else if (elemB == '/') {
                        pos++;
                        StringBuilder name = new StringBuilder();
                        while (pos < length) {
                            int c = streamData[pos] & 0xFF;
                            if (isWhitespace(c) || isDelimiter(c)) break;
                            name.append((char) c);
                            pos++;
                        }
                        arrayElements.add(new PdfObject.PdfName(name.toString()));
                    } else {
                        pos++;
                    }
                }
                operandStack.add(new PdfObject.PdfArray(arrayElements));
                continue;
            }

            // Guard operand stack size
            if (operandStack.size() > MAX_OPERAND_STACK) {
                operandStack.removeFirst(); // discard oldest
            }

            // Keyword / operator
            if (isAlpha(b) || b == '\'' || b == '"') {
                int start = pos;
                if (b == '\'' || b == '"') {
                    pos++;
                } else {
                    while (pos < length) {
                        int c = streamData[pos] & 0xFF;
                        if (isWhitespace(c) || isDelimiter(c)) break;
                        pos++;
                    }
                }
                String word = new String(streamData, start, pos - start, StandardCharsets.US_ASCII);

                // Check for boolean/null
                if ("true".equals(word)) {
                    operandStack.add(new PdfObject.PdfBoolean(true));
                } else if ("false".equals(word)) {
                    operandStack.add(new PdfObject.PdfBoolean(false));
                } else if ("null".equals(word)) {
                    operandStack.add(new PdfObject.PdfNull());
                } else {
                    // It's an operator - flush operand stack
                    operators.add(new Operator(word, new ArrayList<>(operandStack)));
                    operandStack.clear();
                }
                continue;
            }

            // Unknown byte, skip
            pos++;
        }

        return List.copyOf(operators);
    }

    // ==================== Helper record for inline element parsing ====================

    private record InlineElement(PdfObject value, int endPos) {}

    private static InlineElement parseInlineElement(byte[] data, int pos) {
        int b = data[pos] & 0xFF;
        if (b == '(') {
            pos++;
            StringBuilder sb = new StringBuilder();
            int depth = 1;
            while (pos < data.length && depth > 0) {
                int c = data[pos++] & 0xFF;
                if (c == '\\' && pos < data.length) {
                    int next = data[pos++] & 0xFF;
                    switch (next) {
                        case 'n' -> sb.append('\n');
                        case 'r' -> sb.append('\r');
                        case 't' -> sb.append('\t');
                        case 'b' -> sb.append('\b');
                        case 'f' -> sb.append('\f');
                        case '(' -> sb.append('(');
                        case ')' -> sb.append(')');
                        case '\\' -> sb.append('\\');
                        default -> {
                            if (next >= '0' && next <= '7') {
                                int octal = next - '0';
                                if (pos < data.length && (data[pos] & 0xFF) >= '0' && (data[pos] & 0xFF) <= '7') {
                                    octal = (octal << 3) | ((data[pos++] & 0xFF) - '0');
                                    if (pos < data.length && (data[pos] & 0xFF) >= '0' && (data[pos] & 0xFF) <= '7') {
                                        octal = (octal << 3) | ((data[pos++] & 0xFF) - '0');
                                    }
                                }
                                sb.append((char) (octal & 0xFF));
                            } else {
                                sb.append((char) next);
                            }
                        }
                    }
                } else if (c == '(') {
                    depth++;
                    sb.append('(');
                } else if (c == ')') {
                    depth--;
                    if (depth > 0) sb.append(')');
                } else {
                    sb.append((char) c);
                }
            }
            return new InlineElement(new PdfObject.PdfString(sb.toString()), pos);
        }
        if (b == '<') {
            pos++;
            StringBuilder hex = new StringBuilder();
            while (pos < data.length && (data[pos] & 0xFF) != '>') {
                int c = data[pos++] & 0xFF;
                if (!isWhitespace(c)) hex.append((char) c);
            }
            if (pos < data.length) pos++;
            String hexStr = hex.toString();
            if (hexStr.length() % 2 != 0) hexStr += "0";
            StringBuilder decoded = new StringBuilder();
            for (int i = 0; i < hexStr.length(); i += 2) {
                int hi = hexDigit(hexStr.charAt(i));
                int lo = hexDigit(hexStr.charAt(i + 1));
                decoded.append((char) ((Math.max(hi, 0) << 4) | Math.max(lo, 0)));
            }
            return new InlineElement(new PdfObject.PdfString(decoded.toString()), pos);
        }
        return null;
    }

    // ==================== Utility Methods | 工具方法 ====================

    private static int readNumberToken(byte[] data, int pos) {
        int length = data.length;
        if (pos < length && (data[pos] == '+' || data[pos] == '-')) {
            pos++;
        }
        boolean hasDot = false;
        while (pos < length) {
            int c = data[pos] & 0xFF;
            if (c == '.' && !hasDot) {
                hasDot = true;
                pos++;
            } else if (c >= '0' && c <= '9') {
                pos++;
            } else {
                break;
            }
        }
        return pos;
    }

    private static boolean isWhitespace(int b) {
        return b == 0 || b == 9 || b == 10 || b == 12 || b == 13 || b == 32;
    }

    private static boolean isDelimiter(int b) {
        return b == '(' || b == ')' || b == '<' || b == '>' ||
               b == '[' || b == ']' || b == '{' || b == '}' ||
               b == '/' || b == '%';
    }

    private static boolean isAlpha(int b) {
        return (b >= 'a' && b <= 'z') || (b >= 'A' && b <= 'Z') || b == '*';
    }

    private static int hexDigit(int c) {
        if (c >= '0' && c <= '9') return c - '0';
        if (c >= 'a' && c <= 'f') return c - 'a' + 10;
        if (c >= 'A' && c <= 'F') return c - 'A' + 10;
        return -1;
    }
}
