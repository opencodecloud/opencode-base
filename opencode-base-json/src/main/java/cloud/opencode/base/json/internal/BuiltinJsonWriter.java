package cloud.opencode.base.json.internal;

import cloud.opencode.base.json.stream.JsonWriter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

/**
 * Built-in JSON Writer - Character-Based Streaming JSON Push Generator
 * 内置 JSON 写入器 - 基于字符的流式 JSON 推送生成器
 *
 * <p>A streaming push-generator implementation of {@link JsonWriter} that writes JSON
 * to a {@link Writer}. Generates JSON tokens one at a time, automatically handling
 * comma separation, indentation, and string escaping.</p>
 * <p>{@link JsonWriter} 的流式推送生成器实现，将 JSON 写入 {@link Writer}。
 * 逐个生成 JSON 令牌，自动处理逗号分隔、缩进和字符串转义。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Push-based streaming generation - 推送式流式生成</li>
 *   <li>Pretty-print with configurable indent - 可配置缩进的美化打印</li>
 *   <li>HTML-safe escaping mode - HTML 安全转义模式</li>
 *   <li>Method chaining API - 方法链 API</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * var sw = new StringWriter();
 * try (var writer = new BuiltinJsonWriter(sw)) {
 *     writer.beginObject()
 *           .name("id").value(1)
 *           .name("name").value("test")
 *           .endObject();
 * }
 * // sw.toString() -> {"id":1,"name":"test"}
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (create new instance per write) - 线程安全: 否（每次写入创建新实例）</li>
 *   <li>Escapes all control characters and optionally HTML chars - 转义所有控制字符和可选的 HTML 字符</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
public final class BuiltinJsonWriter implements JsonWriter {

    /**
     * Scope within the JSON structure, tracking whether a comma is needed.
     * JSON 结构中的作用域，跟踪是否需要逗号。
     */
    private enum Scope { EMPTY_OBJECT, NONEMPTY_OBJECT, EMPTY_ARRAY, NONEMPTY_ARRAY, DOCUMENT }

    private final Writer out;
    private final Deque<Scope> stack = new ArrayDeque<>();

    private String indent = "";
    private boolean serializeNulls = true;
    private boolean lenient;
    private boolean htmlSafe;
    private boolean closed;

    /** True when the next value needs a property name first. */
    private boolean expectName;

    /** Deferred property name, used to support serializeNulls=false in object scope. */
    private String deferredName;

    /**
     * Creates a new writer that writes to the given output.
     * 创建写入给定输出的新写入器。
     *
     * @param writer the output destination - 输出目标
     */
    public BuiltinJsonWriter(Writer writer) {
        this.out = Objects.requireNonNull(writer, "writer must not be null");
        stack.push(Scope.DOCUMENT);
    }

    // ==================== Structure Writing ====================

    @Override
    public JsonWriter beginObject() {
        beforeValue();
        write('{');
        stack.push(Scope.EMPTY_OBJECT);
        expectName = true;
        return this;
    }

    @Override
    public JsonWriter endObject() {
        Scope scope = stack.peek();
        if (scope != Scope.EMPTY_OBJECT && scope != Scope.NONEMPTY_OBJECT) {
            throw new IllegalStateException("not in an object");
        }
        if (scope == Scope.NONEMPTY_OBJECT) {
            newline();
        }
        stack.pop();
        write('}');
        expectName = !stack.isEmpty() && isObjectScope(stack.peek());
        return this;
    }

    @Override
    public JsonWriter beginArray() {
        beforeValue();
        write('[');
        stack.push(Scope.EMPTY_ARRAY);
        expectName = false;
        return this;
    }

    @Override
    public JsonWriter endArray() {
        Scope scope = stack.peek();
        if (scope != Scope.EMPTY_ARRAY && scope != Scope.NONEMPTY_ARRAY) {
            throw new IllegalStateException("not in an array");
        }
        if (scope == Scope.NONEMPTY_ARRAY) {
            newline();
        }
        stack.pop();
        write(']');
        expectName = !stack.isEmpty() && isObjectScope(stack.peek());
        return this;
    }

    // ==================== Name Writing ====================

    @Override
    public JsonWriter name(String name) {
        Objects.requireNonNull(name, "name must not be null");
        Scope scope = stack.peek();
        if (!isObjectScope(scope)) {
            throw new IllegalStateException("not in an object");
        }
        deferredName = name;
        expectName = false;
        return this;
    }

    /**
     * Writes the deferred property name if one is pending.
     * 如果有待写入的属性名则写入。
     */
    private void writeDeferredName() {
        if (deferredName != null) {
            Scope scope = stack.peek();
            if (scope == Scope.NONEMPTY_OBJECT) {
                write(',');
            }
            stack.pop();
            stack.push(Scope.NONEMPTY_OBJECT);
            newline();
            writeString(deferredName);
            write(':');
            if (!indent.isEmpty()) write(' ');
            deferredName = null;
        }
    }

    // ==================== Value Writing ====================

    @Override
    public JsonWriter value(String value) {
        if (value == null) {
            return nullValue();
        }
        beforeValue();
        writeString(value);
        afterValue();
        return this;
    }

    @Override
    public JsonWriter value(boolean value) {
        beforeValue();
        write(value ? "true" : "false");
        afterValue();
        return this;
    }

    @Override
    public JsonWriter value(int value) {
        beforeValue();
        write(Integer.toString(value));
        afterValue();
        return this;
    }

    @Override
    public JsonWriter value(long value) {
        beforeValue();
        write(Long.toString(value));
        afterValue();
        return this;
    }

    @Override
    public JsonWriter value(double value) {
        if (!lenient && (Double.isNaN(value) || Double.isInfinite(value))) {
            throw new IllegalArgumentException("numeric value must be finite: " + value);
        }
        beforeValue();
        write(Double.toString(value));
        afterValue();
        return this;
    }

    @Override
    public JsonWriter value(Number value) {
        if (value == null) {
            return nullValue();
        }
        String s = value.toString();
        if (!lenient && ("NaN".equals(s) || "Infinity".equals(s) || "-Infinity".equals(s))) {
            throw new IllegalArgumentException("numeric value must be finite: " + s);
        }
        beforeValue();
        write(s);
        afterValue();
        return this;
    }

    @Override
    public JsonWriter nullValue() {
        if (!serializeNulls && deferredName != null) {
            // Skip the null value and discard the deferred property name
            deferredName = null;
            expectName = true;
            return this;
        }
        beforeValue();
        write("null");
        afterValue();
        return this;
    }

    @Override
    public JsonWriter jsonValue(String json) {
        if (json == null) {
            return nullValue();
        }
        beforeValue();
        write(json);
        afterValue();
        return this;
    }

    // ==================== Configuration ====================

    @Override
    public JsonWriter setIndent(String indent) {
        this.indent = indent != null ? indent : "";
        return this;
    }

    @Override
    public JsonWriter setSerializeNulls(boolean serializeNulls) {
        this.serializeNulls = serializeNulls;
        return this;
    }

    @Override
    public JsonWriter setLenient(boolean lenient) {
        this.lenient = lenient;
        return this;
    }

    @Override
    public boolean isLenient() {
        return lenient;
    }

    @Override
    public JsonWriter setHtmlSafe(boolean htmlSafe) {
        this.htmlSafe = htmlSafe;
        return this;
    }

    @Override
    public boolean isHtmlSafe() {
        return htmlSafe;
    }

    // ==================== Lifecycle ====================

    @Override
    public void flush() {
        try {
            out.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void close() {
        if (closed) return;
        closed = true;
        try {
            out.flush();
            out.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // ==================== Internal Helpers ====================

    /**
     * Called before writing a value in an array context to handle commas.
     * 在数组上下文中写入值之前调用以处理逗号。
     */
    private void beforeValue() {
        writeDeferredName();
        Scope scope = stack.peek();
        if (scope == Scope.NONEMPTY_ARRAY) {
            write(',');
            newline();
        } else if (scope == Scope.EMPTY_ARRAY) {
            stack.pop();
            stack.push(Scope.NONEMPTY_ARRAY);
            newline();
        }
        // For object scopes, comma is handled in writeDeferredName()
    }

    /**
     * Called after writing a value to update state.
     * 在写入值后调用以更新状态。
     */
    private void afterValue() {
        Scope scope = stack.peek();
        if (isObjectScope(scope)) {
            expectName = true;
        }
    }

    /**
     * Writes a newline and indentation if pretty-printing.
     * 如果美化打印则写入换行和缩进。
     */
    private void newline() {
        if (indent.isEmpty()) return;
        write('\n');
        int depth = stack.size() - 1; // exclude DOCUMENT
        for (int i = 0; i < depth; i++) {
            write(indent);
        }
    }

    /**
     * Writes a JSON-escaped string surrounded by double quotes.
     * 写入由双引号包围的 JSON 转义字符串。
     */
    private void writeString(String s) {
        write('"');
        for (int i = 0, len = s.length(); i < len; i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> write("\\\"");
                case '\\' -> write("\\\\");
                case '\b' -> write("\\b");
                case '\f' -> write("\\f");
                case '\n' -> write("\\n");
                case '\r' -> write("\\r");
                case '\t' -> write("\\t");
                default -> {
                    if (c < 0x20) {
                        write("\\u");
                        write(String.format("%04x", (int) c));
                    } else if (htmlSafe && (c == '<' || c == '>' || c == '&' || c == '=' || c == '\'')) {
                        write("\\u");
                        write(String.format("%04x", (int) c));
                    } else {
                        write(c);
                    }
                }
            }
        }
        write('"');
    }

    private void write(char c) {
        try {
            out.write(c);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void write(String s) {
        try {
            out.write(s);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static boolean isObjectScope(Scope scope) {
        return scope == Scope.EMPTY_OBJECT || scope == Scope.NONEMPTY_OBJECT;
    }
}
