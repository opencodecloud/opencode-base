package cloud.opencode.base.json.internal;

import cloud.opencode.base.json.stream.JsonReader;
import cloud.opencode.base.json.stream.JsonToken;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

/**
 * Built-in JSON Reader - Character-Based Streaming JSON Pull Parser
 * 内置 JSON 读取器 - 基于字符的流式 JSON 拉取解析器
 *
 * <p>A streaming pull-parser implementation of {@link JsonReader} that reads JSON
 * from a {@link Reader}. Parses JSON tokens one at a time for memory-efficient
 * processing of large documents.</p>
 * <p>{@link JsonReader} 的流式拉取解析器实现，从 {@link Reader} 读取 JSON。
 * 逐个解析 JSON 令牌，实现大文档的内存高效处理。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Pull-based streaming parse - 拉取式流式解析</li>
 *   <li>Line/column tracking for error reporting - 行/列追踪用于错误报告</li>
 *   <li>Lenient mode (comments, single quotes, unquoted names) - 宽松模式（注释、单引号、无引号名称）</li>
 *   <li>Nesting depth limited to 512 - 嵌套深度限制为 512</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * try (var reader = new BuiltinJsonReader(new StringReader("{\"a\":1}"))) {
 *     reader.beginObject();
 *     String name = reader.nextName();   // "a"
 *     int value = reader.nextInt();      // 1
 *     reader.endObject();
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (create new instance per parse) - 线程安全: 否（每次解析创建新实例）</li>
 *   <li>Nesting depth limited to 512 - 嵌套深度限制为 512</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
public final class BuiltinJsonReader implements JsonReader {

    private static final int MAX_DEPTH = 512;
    private static final int BUFFER_SIZE = 4096;
    private static final int MAX_STRING_LENGTH = 20_000_000; // 20MB defense-in-depth limit

    /**
     * Scope within the JSON structure.
     * JSON 结构中的作用域。
     */
    private enum Scope { EMPTY_OBJECT, DANGLING_NAME, NONEMPTY_OBJECT, EMPTY_ARRAY, NONEMPTY_ARRAY, DOCUMENT }

    private final Reader in;
    private final char[] buf = new char[BUFFER_SIZE];
    private int bufPos;
    private int bufLimit;

    private final Deque<Scope> stack = new ArrayDeque<>();
    private final Deque<String> pathNames = new ArrayDeque<>();
    private int[] pathIndicesArr = new int[32];
    private int pathIndicesTop = -1;

    private boolean lenient;
    private int line = 1;
    private int column = 1;

    private JsonToken peeked;
    private String peekedString;
    private Number peekedNumber;
    private boolean closed;
    private final StringBuilder reusableSb = new StringBuilder(64);

    /**
     * Creates a new reader from the given character source.
     * 从给定的字符源创建新的读取器。
     *
     * @param reader the character source - 字符源
     */
    public BuiltinJsonReader(Reader reader) {
        this.in = Objects.requireNonNull(reader, "reader must not be null");
        stack.push(Scope.DOCUMENT);
    }

    // ==================== Structure Navigation ====================

    @Override
    public void beginObject() {
        consumeExpected(JsonToken.START_OBJECT);
        pushScope(Scope.EMPTY_OBJECT);
    }

    @Override
    public void endObject() {
        consumeExpected(JsonToken.END_OBJECT);
        popScope();
    }

    @Override
    public void beginArray() {
        consumeExpected(JsonToken.START_ARRAY);
        pushScope(Scope.EMPTY_ARRAY);
    }

    @Override
    public void endArray() {
        consumeExpected(JsonToken.END_ARRAY);
        popScope();
    }

    // ==================== Token Inspection ====================

    @Override
    public boolean hasNext() {
        JsonToken t = peek();
        return t != JsonToken.END_OBJECT && t != JsonToken.END_ARRAY && t != JsonToken.END_DOCUMENT;
    }

    @Override
    public JsonToken peek() {
        if (peeked != null) {
            return peeked;
        }
        peeked = doPeek();
        return peeked;
    }

    // ==================== Name Reading ====================

    @Override
    public String nextName() {
        consumeExpected(JsonToken.NAME);
        String name = peekedString;
        peekedString = null;
        // After consuming name, switch to DANGLING_NAME (waiting for value)
        stack.pop();
        stack.push(Scope.DANGLING_NAME);
        if (!pathNames.isEmpty()) {
            pathNames.pop();
        }
        pathNames.push(name);
        return name;
    }

    // ==================== Value Reading ====================

    @Override
    public String nextString() {
        JsonToken t = peek();
        if (t == JsonToken.STRING) {
            consume();
            String v = peekedString;
            peekedString = null;
            advanceIndex();
            return v;
        }
        if (t == JsonToken.NUMBER) {
            consume();
            String v = peekedNumber != null ? peekedNumber.toString() : peekedString;
            peekedString = null;
            peekedNumber = null;
            advanceIndex();
            return v;
        }
        if (t == JsonToken.BOOLEAN) {
            consume();
            String v = peekedString;
            peekedString = null;
            advanceIndex();
            return v;
        }
        throw unexpected("a string", t);
    }

    @Override
    public boolean nextBoolean() {
        consumeExpected(JsonToken.BOOLEAN);
        boolean v = "true".equals(peekedString);
        peekedString = null;
        advanceIndex();
        return v;
    }

    @Override
    public void nextNull() {
        consumeExpected(JsonToken.NULL);
        peekedString = null;
        advanceIndex();
    }

    @Override
    public int nextInt() {
        ensureNumeric();
        int v;
        if (peekedNumber != null) {
            v = peekedNumber.intValue();
        } else {
            v = Integer.parseInt(peekedString);
        }
        consume();
        peekedString = null;
        peekedNumber = null;
        advanceIndex();
        return v;
    }

    @Override
    public long nextLong() {
        ensureNumeric();
        long v;
        if (peekedNumber != null) {
            v = peekedNumber.longValue();
        } else {
            v = Long.parseLong(peekedString);
        }
        consume();
        peekedString = null;
        peekedNumber = null;
        advanceIndex();
        return v;
    }

    @Override
    public double nextDouble() {
        ensureNumeric();
        double v;
        if (peekedNumber != null) {
            v = peekedNumber.doubleValue();
        } else {
            v = Double.parseDouble(peekedString);
        }
        consume();
        peekedString = null;
        peekedNumber = null;
        advanceIndex();
        return v;
    }

    @Override
    public BigInteger nextBigInteger() {
        ensureNumeric();
        BigInteger v;
        if (peekedNumber instanceof BigInteger bi) {
            v = bi;
        } else if (peekedNumber instanceof BigDecimal bd) {
            v = bd.toBigIntegerExact();
        } else if (peekedNumber != null) {
            v = BigInteger.valueOf(peekedNumber.longValue());
        } else {
            v = new BigInteger(peekedString);
        }
        consume();
        peekedString = null;
        peekedNumber = null;
        advanceIndex();
        return v;
    }

    @Override
    public BigDecimal nextBigDecimal() {
        ensureNumeric();
        BigDecimal v;
        if (peekedNumber instanceof BigDecimal bd) {
            v = bd;
        } else if (peekedNumber != null) {
            v = new BigDecimal(peekedNumber.toString());
        } else {
            v = new BigDecimal(peekedString);
        }
        consume();
        peekedString = null;
        peekedNumber = null;
        advanceIndex();
        return v;
    }

    @Override
    public Number nextNumber() {
        ensureNumeric();
        Number v = peekedNumber != null ? peekedNumber : new BigDecimal(peekedString);
        consume();
        peekedString = null;
        peekedNumber = null;
        advanceIndex();
        return v;
    }

    // ==================== Skip Operations ====================

    @Override
    public void skipValue() {
        int depth = 0;
        do {
            JsonToken t = peek();
            switch (t) {
                case START_OBJECT -> { beginObject(); depth++; }
                case END_OBJECT -> { endObject(); depth--; }
                case START_ARRAY -> { beginArray(); depth++; }
                case END_ARRAY -> { endArray(); depth--; }
                case NAME -> nextName();
                case STRING, BOOLEAN, NULL -> { consume(); peekedString = null; advanceIndex(); }
                case NUMBER -> { consume(); peekedString = null; peekedNumber = null; advanceIndex(); }
                case END_DOCUMENT -> { return; }
                default -> throw error("unexpected token: " + t);
            }
        } while (depth > 0);
    }

    // ==================== Path Information ====================

    @Override
    public String getPath() {
        var sb = new StringBuilder("$");
        var scopeIt = stack.descendingIterator();
        var nameIt = pathNames.descendingIterator();
        int idxPos = 0;
        // skip DOCUMENT scope
        if (scopeIt.hasNext()) {
            scopeIt.next();
        }
        while (scopeIt.hasNext()) {
            Scope s = scopeIt.next();
            switch (s) {
                case EMPTY_OBJECT, DANGLING_NAME, NONEMPTY_OBJECT -> {
                    if (nameIt.hasNext()) {
                        sb.append('.').append(nameIt.next());
                    }
                }
                case EMPTY_ARRAY, NONEMPTY_ARRAY -> {
                    if (idxPos <= pathIndicesTop) {
                        sb.append('[').append(pathIndicesArr[idxPos++]).append(']');
                    }
                }
                default -> { /* document scope */ }
            }
        }
        return sb.toString();
    }

    @Override
    public int getLineNumber() {
        return line;
    }

    @Override
    public int getColumnNumber() {
        return column;
    }

    // ==================== Configuration ====================

    @Override
    public void setLenient(boolean lenient) {
        this.lenient = lenient;
    }

    @Override
    public boolean isLenient() {
        return lenient;
    }

    // ==================== Lifecycle ====================

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        try {
            in.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // ==================== Internal Parsing ====================

    /**
     * Peeks the next token from the input.
     * 从输入中窥视下一个令牌。
     */
    private JsonToken doPeek() {
        Scope scope = stack.peek();

        switch (scope) {
            case EMPTY_ARRAY -> {
                stack.pop();
                stack.push(Scope.NONEMPTY_ARRAY);
                pushPathIndex(0);
                int c = nextNonWhitespace();
                if (c == ']') {
                    return JsonToken.END_ARRAY;
                }
                pushBack();
                return peekValue();
            }
            case NONEMPTY_ARRAY -> {
                int c = nextNonWhitespace();
                if (c == ']') {
                    return JsonToken.END_ARRAY;
                }
                if (c == ',') {
                    // increment array index
                    if (pathIndicesTop >= 0) {
                        pathIndicesArr[pathIndicesTop]++;
                    }
                    return peekValue();
                }
                throw syntaxError("expected ',' or ']'");
            }
            case EMPTY_OBJECT -> {
                stack.pop();
                stack.push(Scope.NONEMPTY_OBJECT);
                pathNames.push("");
                int c = nextNonWhitespace();
                if (c == '}') {
                    return JsonToken.END_OBJECT;
                }
                return peekName(c);
            }
            case DANGLING_NAME -> {
                // After name was consumed, now read the value
                stack.pop();
                stack.push(Scope.NONEMPTY_OBJECT);
                return peekValue();
            }
            case NONEMPTY_OBJECT -> {
                int c = nextNonWhitespace();
                if (c == '}') {
                    return JsonToken.END_OBJECT;
                }
                if (c == ',') {
                    c = nextNonWhitespace();
                    return peekName(c);
                }
                throw syntaxError("expected ',' or '}'");
            }
            case DOCUMENT -> {
                int c = nextNonWhitespace();
                if (c == -1) {
                    return JsonToken.END_DOCUMENT;
                }
                pushBack();
                return peekValue();
            }
            default -> throw error("unexpected scope: " + scope);
        }
    }

    /**
     * Peeks a property name.
     * 窥视属性名。
     */
    private JsonToken peekName(int firstChar) {
        int c = firstChar;
        if (c == '"') {
            peekedString = readString('"');
        } else if (c == '\'' && lenient) {
            peekedString = readString('\'');
        } else if (lenient && isUnquotedNameChar((char) c)) {
            peekedString = readUnquotedName((char) c);
        } else {
            throw syntaxError("expected property name");
        }
        int colon = nextNonWhitespace();
        if (colon != ':') {
            throw syntaxError("expected ':'");
        }
        return JsonToken.NAME;
    }

    /**
     * Peeks a value token.
     * 窥视值令牌。
     */
    private JsonToken peekValue() {
        int c = nextNonWhitespace();
        return switch (c) {
            case '"' -> {
                peekedString = readString('"');
                yield JsonToken.STRING;
            }
            case '\'' -> {
                if (!lenient) throw syntaxError("single-quoted strings not allowed");
                peekedString = readString('\'');
                yield JsonToken.STRING;
            }
            case '{' -> JsonToken.START_OBJECT;
            case '[' -> JsonToken.START_ARRAY;
            case 't', 'f' -> {
                pushBack();
                peekedString = readLiteral();
                if ("true".equals(peekedString) || "false".equals(peekedString)) {
                    yield JsonToken.BOOLEAN;
                }
                throw syntaxError("unexpected value: " + peekedString);
            }
            case 'n' -> {
                pushBack();
                peekedString = readLiteral();
                if ("null".equals(peekedString)) {
                    yield JsonToken.NULL;
                }
                throw syntaxError("unexpected value: " + peekedString);
            }
            default -> {
                if (c == '-' || (c >= '0' && c <= '9')) {
                    pushBack();
                    String num = readNumber();
                    peekedString = num;
                    peekedNumber = parseNumber(num);
                    yield JsonToken.NUMBER;
                }
                if (lenient && c == '/') {
                    pushBack();
                    skipComment();
                    yield peekValue();
                }
                throw syntaxError("unexpected character: " + (char) c);
            }
        };
    }

    /**
     * Reads a quoted string, handling escape sequences.
     * 读取引号字符串，处理转义序列。
     */
    private String readString(char quote) {
        reusableSb.setLength(0);
        var sb = reusableSb;
        while (true) {
            int c = readChar();
            if (c == -1) throw syntaxError("unterminated string");
            if (c == quote) return sb.toString();
            if (sb.length() >= MAX_STRING_LENGTH) {
                throw syntaxError("string length exceeds maximum of " + MAX_STRING_LENGTH);
            }
            if (c == '\\') {
                int esc = readChar();
                switch (esc) {
                    case '"', '\\', '/' -> sb.append((char) esc);
                    case 'b' -> sb.append('\b');
                    case 'f' -> sb.append('\f');
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    case 't' -> sb.append('\t');
                    case 'u' -> {
                        int codeUnit = 0;
                        for (int i = 0; i < 4; i++) {
                            int h = readChar();
                            if (h == -1) throw syntaxError("unterminated unicode escape");
                            int digit = Character.digit((char) h, 16);
                            if (digit == -1) throw syntaxError("invalid hex digit in unicode escape: " + (char) h);
                            codeUnit = (codeUnit << 4) | digit;
                        }
                        if (Character.isHighSurrogate((char) codeUnit)) {
                            int next1 = readChar();
                            int next2 = readChar();
                            if (next1 == '\\' && next2 == 'u') {
                                int lowSurrogate = 0;
                                for (int i = 0; i < 4; i++) {
                                    int h = readChar();
                                    if (h == -1) throw syntaxError("unterminated unicode escape in surrogate pair");
                                    int digit = Character.digit((char) h, 16);
                                    if (digit == -1) throw syntaxError("invalid hex digit in surrogate pair: " + (char) h);
                                    lowSurrogate = (lowSurrogate << 4) | digit;
                                }
                                if (Character.isLowSurrogate((char) lowSurrogate)) {
                                    int codePoint = Character.toCodePoint((char) codeUnit, (char) lowSurrogate);
                                    sb.append(Character.toChars(codePoint));
                                } else {
                                    sb.append((char) codeUnit);
                                    sb.append((char) lowSurrogate);
                                }
                            } else {
                                sb.append((char) codeUnit);
                                if (next2 != -1) pushBack();
                                if (next1 != -1) pushBack();
                            }
                        } else {
                            sb.append((char) codeUnit);
                        }
                    }
                    case '\'' -> {
                        if (lenient) sb.append('\'');
                        else throw syntaxError("invalid escape: \\'");
                    }
                    default -> {
                        if (lenient) { sb.append((char) esc); }
                        else throw syntaxError("invalid escape: \\" + (char) esc);
                    }
                }
            } else if (c < 0x20 && !lenient) {
                throw syntaxError("unescaped control character: 0x" + Integer.toHexString(c));
            } else {
                sb.append((char) c);
            }
        }
    }

    /**
     * Reads a numeric literal from the input.
     * 从输入中读取数字字面量。
     */
    private String readNumber() {
        var sb = new StringBuilder();
        int c = readChar();
        // optional minus
        if (c == '-') { sb.append('-'); c = readChar(); }
        // integer part
        if (c == '0') {
            sb.append('0');
            c = readChar();
        } else if (c >= '1' && c <= '9') {
            while (c >= '0' && c <= '9') { sb.append((char) c); c = readChar(); }
        } else {
            throw syntaxError("expected digit");
        }
        // fraction
        if (c == '.') {
            sb.append('.');
            c = readChar();
            if (c < '0' || c > '9') throw syntaxError("expected digit after '.'");
            while (c >= '0' && c <= '9') { sb.append((char) c); c = readChar(); }
        }
        // exponent
        if (c == 'e' || c == 'E') {
            sb.append((char) c);
            c = readChar();
            if (c == '+' || c == '-') { sb.append((char) c); c = readChar(); }
            if (c < '0' || c > '9') throw syntaxError("expected digit in exponent");
            while (c >= '0' && c <= '9') { sb.append((char) c); c = readChar(); }
        }
        if (c != -1) pushBack();
        return sb.toString();
    }

    /**
     * Parses a number string to the most appropriate Number type.
     * 将数字字符串解析为最合适的 Number 类型。
     */
    private Number parseNumber(String s) {
        try {
            if (s.indexOf('.') < 0 && s.indexOf('e') < 0 && s.indexOf('E') < 0) {
                long l = Long.parseLong(s);
                if (l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE) {
                    return (int) l;
                }
                return l;
            }
            return new BigDecimal(s);
        } catch (NumberFormatException e) {
            return new BigDecimal(s);
        }
    }

    /**
     * Reads a literal (true, false, null).
     * 读取字面量（true、false、null）。
     */
    private String readLiteral() {
        var sb = new StringBuilder();
        int c = readChar();
        while (c != -1 && Character.isLetterOrDigit(c)) {
            sb.append((char) c);
            c = readChar();
        }
        if (c != -1) pushBack();
        return sb.toString();
    }

    /**
     * Reads an unquoted name (lenient mode).
     * 读取无引号名称（宽松模式）。
     */
    private String readUnquotedName(char first) {
        var sb = new StringBuilder();
        sb.append(first);
        while (true) {
            int c = readChar();
            if (c == -1 || !isUnquotedNameChar((char) c)) {
                if (c != -1) pushBack();
                return sb.toString();
            }
            sb.append((char) c);
        }
    }

    private boolean isUnquotedNameChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_' || c == '-' || c == '$';
    }

    /**
     * Skips a comment (lenient mode).
     * 跳过注释（宽松模式）。
     */
    private void skipComment() {
        int c = readChar(); // '/'
        int next = readChar();
        if (next == '/') {
            // line comment
            while (true) {
                c = readChar();
                if (c == -1 || c == '\n') return;
            }
        } else if (next == '*') {
            // block comment
            int prev = 0;
            while (true) {
                c = readChar();
                if (c == -1) throw syntaxError("unterminated comment");
                if (prev == '*' && c == '/') return;
                prev = c;
            }
        } else {
            throw syntaxError("unexpected '/'");
        }
    }

    // ==================== Buffer / IO ====================

    /**
     * Reads a single character, tracking line and column.
     * 读取单个字符，跟踪行和列。
     */
    private int readChar() {
        if (closed) throw error("reader is closed");
        if (bufPos >= bufLimit) {
            if (!fillBuffer()) return -1;
        }
        char c = buf[bufPos++];
        if (c == '\n') { line++; column = 1; }
        else { column++; }
        return c;
    }

    /**
     * Pushes back the last read character.
     * 回退最后读取的字符。
     */
    private void pushBack() {
        if (bufPos > 0) {
            bufPos--;
            char c = buf[bufPos];
            if (c == '\n') { line--; /* column not precisely tracked on pushback */ }
            else { column--; }
        }
    }

    private boolean fillBuffer() {
        try {
            int read = in.read(buf, 0, buf.length);
            if (read <= 0) return false;
            bufPos = 0;
            bufLimit = read;
            return true;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Reads the next non-whitespace character, skipping comments in lenient mode.
     * 读取下一个非空白字符，在宽松模式下跳过注释。
     */
    private int nextNonWhitespace() {
        while (true) {
            int c = readChar();
            if (c == -1) return -1;
            if (c == ' ' || c == '\t' || c == '\n' || c == '\r') continue;
            if (lenient && c == '/') {
                pushBack();
                skipComment();
                continue;
            }
            return c;
        }
    }

    // ==================== Scope Management ====================

    private void pushScope(Scope scope) {
        if (stack.size() >= MAX_DEPTH) {
            throw error("nesting depth exceeds " + MAX_DEPTH);
        }
        stack.push(scope);
    }

    private void popScope() {
        Scope exiting = stack.pop();
        if (exiting == Scope.NONEMPTY_ARRAY || exiting == Scope.EMPTY_ARRAY) {
            popPathIndex();
        }
        if (!pathNames.isEmpty() && !stack.isEmpty()) {
            Scope parent = stack.peek();
            if (parent == Scope.NONEMPTY_OBJECT || parent == Scope.EMPTY_OBJECT || parent == Scope.DANGLING_NAME) {
                // keep parent name
            } else if (parent == Scope.NONEMPTY_ARRAY || parent == Scope.EMPTY_ARRAY) {
                // keep parent index
            }
        }
    }

    private void advanceIndex() {
        if (pathIndicesTop >= 0) {
            // index incremented on comma in doPeek
        }
    }

    private void pushPathIndex(int value) {
        pathIndicesTop++;
        if (pathIndicesTop >= pathIndicesArr.length) {
            pathIndicesArr = java.util.Arrays.copyOf(pathIndicesArr, pathIndicesArr.length * 2);
        }
        pathIndicesArr[pathIndicesTop] = value;
    }

    private void popPathIndex() {
        if (pathIndicesTop >= 0) pathIndicesTop--;
    }

    // ==================== Helpers ====================

    private void consume() {
        peeked = null;
    }

    private void consumeExpected(JsonToken expected) {
        JsonToken t = peek();
        if (t != expected) {
            throw unexpected(expected.name(), t);
        }
        consume();
    }

    private void ensureNumeric() {
        JsonToken t = peek();
        if (t != JsonToken.NUMBER) {
            throw unexpected("a number", t);
        }
    }

    private IllegalStateException unexpected(String expected, JsonToken actual) {
        return error("expected " + expected + " but was " + actual + " at line " + line + " column " + column);
    }

    private IllegalStateException syntaxError(String message) {
        return error(message + " at line " + line + " column " + column);
    }

    private IllegalStateException error(String message) {
        return new IllegalStateException(message);
    }
}
