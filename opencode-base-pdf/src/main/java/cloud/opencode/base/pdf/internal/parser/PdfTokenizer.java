package cloud.opencode.base.pdf.internal.parser;

import cloud.opencode.base.pdf.exception.OpenPdfException;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * PDF Lexical Tokenizer - Reads PDF tokens from raw byte data
 * PDF 词法分析器 - 从原始字节数据中读取 PDF 令牌
 *
 * <p>Implements the PDF lexical conventions as defined in ISO 32000-1 Section 7.2.
 * Supports all PDF token types including numbers, strings (literal and hex),
 * names, booleans, null, and keywords.</p>
 * <p>实现 ISO 32000-1 第 7.2 节定义的 PDF 词法约定。
 * 支持所有 PDF 令牌类型，包括数字、字符串（文字和十六进制）、名称、布尔值、空值和关键字。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Full PDF lexical analysis with position tracking - 完整的 PDF 词法分析与位置跟踪</li>
 *   <li>Literal string with escape sequences and nested parentheses - 支持转义序列和嵌套括号的文字字符串</li>
 *   <li>Hexadecimal string decoding - 十六进制字符串解码</li>
 *   <li>Name object with #xx hex escape handling - 名称对象的 #xx 十六进制转义处理</li>
 *   <li>Seek to arbitrary position for random access parsing - 支持跳转到任意位置的随机访问解析</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No — mutable position state - 线程安全: 否 — 可变位置状态</li>
 *   <li>String length bounded by input data size - 字符串长度受输入数据大小限制</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.3
 */
public final class PdfTokenizer {

    private static final int MAX_PARSE_DEPTH = 100;
    private static final int MAX_COLLECTION_SIZE = 100_000;

    private final byte[] data;
    private int position;
    private int parseDepth = 0;

    /**
     * Creates a tokenizer for the given PDF data
     * 为给定的 PDF 数据创建分词器
     *
     * @param data PDF byte data | PDF 字节数据
     */
    public PdfTokenizer(byte[] data) {
        this.data = Objects.requireNonNull(data, "data cannot be null").clone();
        this.position = 0;
    }

    /**
     * Package-private constructor for trusted internal use (no defensive copy).
     * 包私有构造器，用于内部受信调用（不做防御性拷贝）。
     */
    PdfTokenizer(byte[] data, boolean trusted) {
        this.data = Objects.requireNonNull(data, "data cannot be null");
    }

    /**
     * Gets current read position
     * 获取当前读取位置
     *
     * @return current position | 当前位置
     */
    public int getPosition() {
        return position;
    }

    /**
     * Seeks to specified position
     * 跳转到指定位置
     *
     * @param pos target position | 目标位置
     * @throws IllegalArgumentException if position is out of bounds | 位置越界时抛出异常
     */
    public void seekTo(int pos) {
        if (pos < 0 || pos > data.length) {
            throw new IllegalArgumentException("Position out of bounds: " + pos);
        }
        this.position = pos;
    }

    /**
     * Checks if there is more data to read
     * 检查是否有更多数据可读
     *
     * @return true if more data available | 如果有更多数据返回 true
     */
    public boolean hasMore() {
        return position < data.length;
    }

    /**
     * Gets the total data length
     * 获取数据总长度
     *
     * @return data length | 数据长度
     */
    public int length() {
        return data.length;
    }

    /**
     * Reads a single line from current position
     * 从当前位置读取一行
     *
     * @return the line content | 行内容
     */
    private static final int MAX_LINE_LENGTH = 4096;

    public String readLine() {
        StringBuilder sb = new StringBuilder();
        while (position < data.length) {
            int b = data[position++] & 0xFF;
            if (b == '\r') {
                if (position < data.length && (data[position] & 0xFF) == '\n') {
                    position++;
                }
                break;
            }
            if (b == '\n') {
                break;
            }
            sb.append((char) b);
            if (sb.length() > MAX_LINE_LENGTH) {
                break;
            }
        }
        return sb.toString();
    }

    /**
     * Skips whitespace and comments, then reads the next PDF token
     * 跳过空白和注释，然后读取下一个 PDF 令牌
     *
     * @return next PDF object token | 下一个 PDF 对象令牌
     * @throws OpenPdfException if token cannot be parsed | 无法解析令牌时抛出异常
     */
    public PdfObject readToken() {
        while (true) {
            skipWhitespaceAndComments();
            if (position >= data.length) {
                return null;
            }

            int b = data[position] & 0xFF;

            // Name: /xxx
            if (b == '/') {
                return readName();
            }

            // Literal string: (...)
            if (b == '(') {
                return readLiteralString();
            }

            // Hex string or dictionary
            if (b == '<') {
                if (position + 1 < data.length && (data[position + 1] & 0xFF) == '<') {
                    if (++parseDepth > MAX_PARSE_DEPTH) {
                        throw new OpenPdfException("PDF_PARSE", "Maximum nesting depth exceeded");
                    }
                    try {
                        return readDictionary();
                    } finally {
                        parseDepth--;
                    }
                }
                return readHexString();
            }

            // Array
            if (b == '[') {
                if (++parseDepth > MAX_PARSE_DEPTH) {
                    throw new OpenPdfException("PDF_PARSE", "Maximum nesting depth exceeded");
                }
                try {
                    return readArray();
                } finally {
                    parseDepth--;
                }
            }

            // Number (including negative)
            if (b == '+' || b == '-' || b == '.' || (b >= '0' && b <= '9')) {
                return readNumber();
            }

            // Keyword (true, false, null, R, obj, endobj, stream, endstream, etc.)
            if (isAlpha(b)) {
                return readKeyword();
            }

            // End markers - skip single char and continue loop
            if (b == ']' || b == '>') {
                position++;
                continue;
            }

            // Unknown byte - skip and try next
            position++;
        }
    }

    /**
     * Reads the next keyword string without converting to PdfObject
     * 读取下一个关键字字符串，不转换为 PdfObject
     *
     * @return keyword string | 关键字字符串
     */
    public String readKeywordString() {
        skipWhitespaceAndComments();
        if (position >= data.length) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        while (position < data.length) {
            int b = data[position] & 0xFF;
            if (isWhitespace(b) || isDelimiter(b)) {
                break;
            }
            sb.append((char) b);
            position++;
        }
        return sb.toString();
    }

    /**
     * Peeks at next non-whitespace byte without consuming
     * 查看下一个非空白字节但不消费
     *
     * @return next byte, or -1 if at end | 下一个字节，到达末尾返回 -1
     */
    public int peekNextNonWhitespace() {
        int saved = position;
        skipWhitespaceAndComments();
        int result = position < data.length ? (data[position] & 0xFF) : -1;
        position = saved;
        return result;
    }

    /**
     * Reads raw bytes from current position
     * 从当前位置读取原始字节
     *
     * @param length number of bytes to read | 要读取的字节数
     * @return read bytes | 读取的字节
     */
    public byte[] readBytes(int length) {
        if (length < 0) {
            throw new IllegalArgumentException("length cannot be negative");
        }
        int actualLength = Math.min(length, data.length - position);
        byte[] result = new byte[actualLength];
        System.arraycopy(data, position, result, 0, actualLength);
        position += actualLength;
        return result;
    }

    // ==================== Private Parsing Methods | 私有解析方法 ====================

    private PdfObject.PdfName readName() {
        position++; // skip '/'
        StringBuilder sb = new StringBuilder();
        while (position < data.length) {
            int b = data[position] & 0xFF;
            if (isWhitespace(b) || isDelimiter(b)) {
                break;
            }
            if (b == '#' && position + 2 < data.length) {
                // Hex escape: #xx
                int hi = hexDigit(data[position + 1] & 0xFF);
                int lo = hexDigit(data[position + 2] & 0xFF);
                if (hi >= 0 && lo >= 0) {
                    sb.append((char) ((hi << 4) | lo));
                    position += 3;
                    continue;
                }
            }
            sb.append((char) b);
            position++;
        }
        return new PdfObject.PdfName(sb.toString());
    }

    private PdfObject.PdfString readLiteralString() {
        position++; // skip '('
        StringBuilder sb = new StringBuilder();
        int depth = 1;
        while (position < data.length && depth > 0) {
            int b = data[position++] & 0xFF;
            if (b == '\\' && position < data.length) {
                int next = data[position++] & 0xFF;
                switch (next) {
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    case 't' -> sb.append('\t');
                    case 'b' -> sb.append('\b');
                    case 'f' -> sb.append('\f');
                    case '(' -> sb.append('(');
                    case ')' -> sb.append(')');
                    case '\\' -> sb.append('\\');
                    case '\r' -> {
                        // Line continuation
                        if (position < data.length && (data[position] & 0xFF) == '\n') {
                            position++;
                        }
                    }
                    case '\n' -> {
                        // Line continuation - nothing to append
                    }
                    default -> {
                        // Octal escape: \nnn (1-3 digits)
                        if (next >= '0' && next <= '7') {
                            int octal = next - '0';
                            if (position < data.length && (data[position] & 0xFF) >= '0' && (data[position] & 0xFF) <= '7') {
                                octal = (octal << 3) | ((data[position++] & 0xFF) - '0');
                                if (position < data.length && (data[position] & 0xFF) >= '0' && (data[position] & 0xFF) <= '7') {
                                    octal = (octal << 3) | ((data[position++] & 0xFF) - '0');
                                }
                            }
                            sb.append((char) (octal & 0xFF));
                        } else {
                            // Unknown escape - just append the character
                            sb.append((char) next);
                        }
                    }
                }
            } else if (b == '(') {
                depth++;
                sb.append('(');
            } else if (b == ')') {
                depth--;
                if (depth > 0) {
                    sb.append(')');
                }
            } else {
                sb.append((char) b);
            }
        }
        return new PdfObject.PdfString(sb.toString());
    }

    private PdfObject.PdfString readHexString() {
        position++; // skip '<'
        StringBuilder hex = new StringBuilder();
        while (position < data.length) {
            int b = data[position++] & 0xFF;
            if (b == '>') break;
            if (!isWhitespace(b)) {
                hex.append((char) b);
            }
        }
        String hexStr = hex.toString();
        // Pad with trailing zero if odd length
        if (hexStr.length() % 2 != 0) {
            hexStr = hexStr + "0";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hexStr.length(); i += 2) {
            int hi = hexDigit(hexStr.charAt(i));
            int lo = hexDigit(hexStr.charAt(i + 1));
            if (hi < 0 || lo < 0) {
                throw OpenPdfException.invalidFormat("Invalid hex string character");
            }
            sb.append((char) ((hi << 4) | lo));
        }
        return new PdfObject.PdfString(sb.toString());
    }

    private PdfObject.PdfArray readArray() {
        position++; // skip '['
        java.util.List<PdfObject> elements = new java.util.ArrayList<>();
        while (position < data.length) {
            skipWhitespaceAndComments();
            if (position >= data.length) break;
            if ((data[position] & 0xFF) == ']') {
                position++;
                break;
            }
            PdfObject element = readToken();
            if (element == null) break;

            // Check for indirect reference: N G R
            element = maybeResolveReference(element, elements);
            elements.add(element);
            if (elements.size() > MAX_COLLECTION_SIZE) {
                throw new OpenPdfException("PDF_PARSE", "Array exceeds maximum size");
            }
        }
        return new PdfObject.PdfArray(elements);
    }

    private PdfObject.PdfDictionary readDictionary() {
        position += 2; // skip '<<'
        java.util.Map<String, PdfObject> entries = new java.util.LinkedHashMap<>();
        while (position < data.length) {
            skipWhitespaceAndComments();
            if (position >= data.length) break;
            // Check for '>>'
            if ((data[position] & 0xFF) == '>' && position + 1 < data.length && (data[position + 1] & 0xFF) == '>') {
                position += 2;
                break;
            }
            // Read key (must be a name)
            PdfObject keyObj = readToken();
            if (keyObj == null) break;
            if (!(keyObj instanceof PdfObject.PdfName name)) {
                // Skip non-name keys
                continue;
            }
            // Read value
            PdfObject value = readToken();
            if (value == null) break;

            // Check if value+next form an indirect reference
            value = maybeResolveReferenceForDict(value);
            entries.put(name.value(), value);
            if (entries.size() > MAX_COLLECTION_SIZE) {
                throw new OpenPdfException("PDF_PARSE", "Dictionary exceeds maximum size");
            }
        }
        return new PdfObject.PdfDictionary(entries);
    }

    private PdfObject.PdfNumber readNumber() {
        StringBuilder sb = new StringBuilder();
        if (position < data.length) {
            int b = data[position] & 0xFF;
            if (b == '+' || b == '-') {
                sb.append((char) b);
                position++;
            }
        }
        boolean hasDot = false;
        while (position < data.length) {
            int b = data[position] & 0xFF;
            if (b == '.' && !hasDot) {
                hasDot = true;
                sb.append('.');
                position++;
            } else if (b >= '0' && b <= '9') {
                sb.append((char) b);
                position++;
            } else {
                break;
            }
        }
        String numStr = sb.toString();
        if (numStr.isEmpty() || numStr.equals("+") || numStr.equals("-") || numStr.equals(".")) {
            return new PdfObject.PdfNumber(0);
        }
        try {
            return new PdfObject.PdfNumber(Double.parseDouble(numStr));
        } catch (NumberFormatException e) {
            return new PdfObject.PdfNumber(0);
        }
    }

    private PdfObject readKeyword() {
        StringBuilder sb = new StringBuilder();
        while (position < data.length) {
            int b = data[position] & 0xFF;
            if (isWhitespace(b) || isDelimiter(b)) break;
            sb.append((char) b);
            position++;
        }
        String word = sb.toString();
        return switch (word) {
            case "true" -> new PdfObject.PdfBoolean(true);
            case "false" -> new PdfObject.PdfBoolean(false);
            case "null" -> new PdfObject.PdfNull();
            default ->
                // Return as PdfName for keywords like obj, endobj, stream, etc.
                    new PdfObject.PdfName(word);
        };
    }

    /**
     * Checks if last two elements + current "R" form an indirect reference
     * 检查最后两个元素 + 当前 "R" 是否构成间接引用
     */
    private PdfObject maybeResolveReference(PdfObject current, java.util.List<PdfObject> elements) {
        if (current instanceof PdfObject.PdfName name && "R".equals(name.value())) {
            if (elements.size() >= 2) {
                PdfObject gen = elements.get(elements.size() - 1);
                PdfObject obj = elements.get(elements.size() - 2);
                if (gen instanceof PdfObject.PdfNumber g && obj instanceof PdfObject.PdfNumber o) {
                    elements.remove(elements.size() - 1);
                    elements.remove(elements.size() - 1);
                    return new PdfObject.PdfReference(o.intValue(), g.intValue());
                }
            }
        }
        return current;
    }

    /**
     * For dictionary values, checks if next tokens form "N R" reference pattern
     * 对于字典值，检查后续令牌是否形成 "N R" 引用模式
     */
    private PdfObject maybeResolveReferenceForDict(PdfObject value) {
        if (value instanceof PdfObject.PdfNumber objNum) {
            int saved = position;
            skipWhitespaceAndComments();
            if (position < data.length) {
                int b = data[position] & 0xFF;
                if (b >= '0' && b <= '9') {
                    PdfObject genObj = readNumber();
                    if (genObj instanceof PdfObject.PdfNumber gen) {
                        skipWhitespaceAndComments();
                        if (position < data.length && (data[position] & 0xFF) == 'R') {
                            // Check next char is delimiter/whitespace
                            if (position + 1 >= data.length || isWhitespace(data[position + 1] & 0xFF) || isDelimiter(data[position + 1] & 0xFF)) {
                                position++;
                                return new PdfObject.PdfReference(objNum.intValue(), gen.intValue());
                            }
                        }
                    }
                }
            }
            // Not a reference, restore position
            position = saved;
        }
        return value;
    }

    // ==================== Utility Methods | 工具方法 ====================

    /**
     * Skips whitespace characters and PDF comments
     * 跳过空白字符和 PDF 注释
     */
    public void skipWhitespaceAndComments() {
        while (position < data.length) {
            int b = data[position] & 0xFF;
            if (isWhitespace(b)) {
                position++;
            } else if (b == '%') {
                // Skip comment until end of line
                while (position < data.length) {
                    int c = data[position++] & 0xFF;
                    if (c == '\r' || c == '\n') break;
                }
            } else {
                break;
            }
        }
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
        return (b >= 'a' && b <= 'z') || (b >= 'A' && b <= 'Z');
    }

    private static int hexDigit(int b) {
        if (b >= '0' && b <= '9') return b - '0';
        if (b >= 'a' && b <= 'f') return b - 'a' + 10;
        if (b >= 'A' && b <= 'F') return b - 'A' + 10;
        return -1;
    }

    /**
     * Searches backward from end of data for a string
     * 从数据末尾向前搜索字符串
     *
     * @param target string to find | 要查找的字符串
     * @return position of target, or -1 if not found | 目标位置，未找到返回 -1
     */
    public int searchBackward(String target) {
        byte[] targetBytes = target.getBytes(StandardCharsets.US_ASCII);
        for (int i = data.length - targetBytes.length; i >= 0; i--) {
            boolean found = true;
            for (int j = 0; j < targetBytes.length; j++) {
                if (data[i + j] != targetBytes[j]) {
                    found = false;
                    break;
                }
            }
            if (found) return i;
        }
        return -1;
    }
}
