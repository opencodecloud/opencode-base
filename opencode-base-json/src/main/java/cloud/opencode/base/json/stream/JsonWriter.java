
package cloud.opencode.base.json.stream;

import java.io.Closeable;
import java.io.Flushable;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * JSON Writer - Streaming JSON Generator Interface
 * JSON 写入器 - 流式 JSON 生成器接口
 *
 * <p>This interface provides a streaming (push-based) API for writing JSON.
 * It allows efficient generation of large JSON documents without building
 * an in-memory tree structure.</p>
 * <p>此接口提供用于写入 JSON 的流式（推送式）API。
 * 它允许高效生成大型 JSON 文档，无需构建内存中的树结构。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * try (JsonWriter writer = OpenJson.createWriter(outputStream)) {
 *     writer.beginObject()
 *           .name("id").value(123)
 *           .name("name").value("John")
 *           .name("tags").beginArray()
 *               .value("java")
 *               .value("json")
 *           .endArray()
 *           .endObject();
 * }
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Streaming push-based JSON generation - 流式推送式JSON生成</li>
 *   <li>Method chaining API for fluent writing - 方法链API实现流畅写入</li>
 *   <li>Configurable indentation, null serialization, and HTML-safe mode - 可配置缩进、null序列化和HTML安全模式</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation-dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: Partial (validates inputs) - 空值安全: 部分（验证输入）</li>
 * </ul>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
public interface JsonWriter extends Closeable, Flushable {

    // ==================== Structure Writing ====================

    /**
     * Begins writing a JSON object.
     * 开始写入 JSON 对象。
     *
     * @return this writer for method chaining - 用于方法链的此写入器
     */
    JsonWriter beginObject();

    /**
     * Ends writing a JSON object.
     * 结束写入 JSON 对象。
     *
     * @return this writer for method chaining - 用于方法链的此写入器
     */
    JsonWriter endObject();

    /**
     * Begins writing a JSON array.
     * 开始写入 JSON 数组。
     *
     * @return this writer for method chaining - 用于方法链的此写入器
     */
    JsonWriter beginArray();

    /**
     * Ends writing a JSON array.
     * 结束写入 JSON 数组。
     *
     * @return this writer for method chaining - 用于方法链的此写入器
     */
    JsonWriter endArray();

    // ==================== Name Writing ====================

    /**
     * Writes a property name.
     * 写入属性名。
     *
     * @param name the property name - 属性名
     * @return this writer for method chaining - 用于方法链的此写入器
     */
    JsonWriter name(String name);

    // ==================== Value Writing ====================

    /**
     * Writes a string value.
     * 写入字符串值。
     *
     * @param value the string value - 字符串值
     * @return this writer for method chaining - 用于方法链的此写入器
     */
    JsonWriter value(String value);

    /**
     * Writes a boolean value.
     * 写入布尔值。
     *
     * @param value the boolean value - 布尔值
     * @return this writer for method chaining - 用于方法链的此写入器
     */
    JsonWriter value(boolean value);

    /**
     * Writes an int value.
     * 写入 int 值。
     *
     * @param value the int value - int 值
     * @return this writer for method chaining - 用于方法链的此写入器
     */
    JsonWriter value(int value);

    /**
     * Writes a long value.
     * 写入 long 值。
     *
     * @param value the long value - long 值
     * @return this writer for method chaining - 用于方法链的此写入器
     */
    JsonWriter value(long value);

    /**
     * Writes a double value.
     * 写入 double 值。
     *
     * @param value the double value - double 值
     * @return this writer for method chaining - 用于方法链的此写入器
     */
    JsonWriter value(double value);

    /**
     * Writes a float value.
     * 写入 float 值。
     *
     * @param value the float value - float 值
     * @return this writer for method chaining - 用于方法链的此写入器
     */
    default JsonWriter value(float value) {
        return value((double) value);
    }

    /**
     * Writes a Number value.
     * 写入 Number 值。
     *
     * @param value the Number value - Number 值
     * @return this writer for method chaining - 用于方法链的此写入器
     */
    JsonWriter value(Number value);

    /**
     * Writes a BigInteger value.
     * 写入 BigInteger 值。
     *
     * @param value the BigInteger value - BigInteger 值
     * @return this writer for method chaining - 用于方法链的此写入器
     */
    default JsonWriter value(BigInteger value) {
        return value((Number) value);
    }

    /**
     * Writes a BigDecimal value.
     * 写入 BigDecimal 值。
     *
     * @param value the BigDecimal value - BigDecimal 值
     * @return this writer for method chaining - 用于方法链的此写入器
     */
    default JsonWriter value(BigDecimal value) {
        return value((Number) value);
    }

    /**
     * Writes a null value.
     * 写入 null 值。
     *
     * @return this writer for method chaining - 用于方法链的此写入器
     */
    JsonWriter nullValue();

    /**
     * Writes a raw JSON value (without escaping).
     * 写入原始 JSON 值（不转义）。
     *
     * @param json the raw JSON string - 原始 JSON 字符串
     * @return this writer for method chaining - 用于方法链的此写入器
     */
    JsonWriter jsonValue(String json);

    // ==================== Convenience Methods ====================

    /**
     * Writes a name/value pair.
     * 写入名称/值对。
     *
     * @param name  the property name - 属性名
     * @param value the string value - 字符串值
     * @return this writer for method chaining - 用于方法链的此写入器
     */
    default JsonWriter property(String name, String value) {
        return name(name).value(value);
    }

    /**
     * Writes a name/value pair.
     * 写入名称/值对。
     *
     * @param name  the property name - 属性名
     * @param value the boolean value - 布尔值
     * @return this writer for method chaining - 用于方法链的此写入器
     */
    default JsonWriter property(String name, boolean value) {
        return name(name).value(value);
    }

    /**
     * Writes a name/value pair.
     * 写入名称/值对。
     *
     * @param name  the property name - 属性名
     * @param value the int value - int 值
     * @return this writer for method chaining - 用于方法链的此写入器
     */
    default JsonWriter property(String name, int value) {
        return name(name).value(value);
    }

    /**
     * Writes a name/value pair.
     * 写入名称/值对。
     *
     * @param name  the property name - 属性名
     * @param value the long value - long 值
     * @return this writer for method chaining - 用于方法链的此写入器
     */
    default JsonWriter property(String name, long value) {
        return name(name).value(value);
    }

    /**
     * Writes a name/value pair.
     * 写入名称/值对。
     *
     * @param name  the property name - 属性名
     * @param value the double value - double 值
     * @return this writer for method chaining - 用于方法链的此写入器
     */
    default JsonWriter property(String name, double value) {
        return name(name).value(value);
    }

    /**
     * Writes a name/value pair.
     * 写入名称/值对。
     *
     * @param name  the property name - 属性名
     * @param value the Number value - Number 值
     * @return this writer for method chaining - 用于方法链的此写入器
     */
    default JsonWriter property(String name, Number value) {
        return name(name).value(value);
    }

    /**
     * Writes a name/null pair.
     * 写入名称/null 对。
     *
     * @param name the property name - 属性名
     * @return this writer for method chaining - 用于方法链的此写入器
     */
    default JsonWriter propertyNull(String name) {
        return name(name).nullValue();
    }

    // ==================== Configuration ====================

    /**
     * Sets the indentation string for pretty printing.
     * 设置美化打印的缩进字符串。
     *
     * @param indent the indentation (e.g., "  " or "\t") - 缩进（如 "  " 或 "\t"）
     * @return this writer for method chaining - 用于方法链的此写入器
     */
    JsonWriter setIndent(String indent);

    /**
     * Sets whether to serialize nulls.
     * 设置是否序列化 null 值。
     *
     * @param serializeNulls true to serialize nulls - 如果序列化 null 则为 true
     * @return this writer for method chaining - 用于方法链的此写入器
     */
    JsonWriter setSerializeNulls(boolean serializeNulls);

    /**
     * Sets whether this writer is lenient.
     * 设置此写入器是否宽松。
     *
     * @param lenient true for lenient writing - 如果宽松写入则为 true
     * @return this writer for method chaining - 用于方法链的此写入器
     */
    JsonWriter setLenient(boolean lenient);

    /**
     * Returns whether this writer is lenient.
     * 返回此写入器是否宽松。
     *
     * @return true if lenient - 如果宽松则返回 true
     */
    boolean isLenient();

    /**
     * Sets whether HTML-safe mode is enabled.
     * 设置是否启用 HTML 安全模式。
     *
     * @param htmlSafe true to escape HTML characters - 如果转义 HTML 字符则为 true
     * @return this writer for method chaining - 用于方法链的此写入器
     */
    JsonWriter setHtmlSafe(boolean htmlSafe);

    /**
     * Returns whether HTML-safe mode is enabled.
     * 返回是否启用 HTML 安全模式。
     *
     * @return true if HTML-safe - 如果 HTML 安全则返回 true
     */
    boolean isHtmlSafe();

    // ==================== Lifecycle ====================

    /**
     * Flushes any buffered data to the underlying output.
     * 将任何缓冲数据刷新到底层输出。
     */
    @Override
    void flush();

    /**
     * Closes this writer and releases resources.
     * 关闭此写入器并释放资源。
     */
    @Override
    void close();
}
