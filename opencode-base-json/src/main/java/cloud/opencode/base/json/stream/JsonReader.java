
package cloud.opencode.base.json.stream;

import java.io.Closeable;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * JSON Reader - Streaming JSON Parser Interface
 * JSON 读取器 - 流式 JSON 解析器接口
 *
 * <p>This interface provides a streaming (pull-based) API for reading JSON.
 * It allows efficient parsing of large JSON documents without loading
 * the entire content into memory.</p>
 * <p>此接口提供用于读取 JSON 的流式（拉取式）API。
 * 它允许高效解析大型 JSON 文档，无需将整个内容加载到内存中。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * try (JsonReader reader = OpenJson.createReader(inputStream)) {
 *     reader.beginObject();
 *     while (reader.hasNext()) {
 *         String name = reader.nextName();
 *         if ("id".equals(name)) {
 *             long id = reader.nextLong();
 *         } else if ("name".equals(name)) {
 *             String value = reader.nextString();
 *         } else {
 *             reader.skipValue();
 *         }
 *     }
 *     reader.endObject();
 * }
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Streaming pull-based JSON parsing - 流式拉取式JSON解析</li>
 *   <li>Structure navigation (begin/end object/array) - 结构导航（开始/结束对象/数组）</li>
 *   <li>Path and position tracking for error reporting - 路径和位置跟踪用于错误报告</li>
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
public interface JsonReader extends Closeable {

    // ==================== Structure Navigation ====================

    /**
     * Consumes the next token and asserts it is START_OBJECT.
     * 消费下一个令牌并断言它是 START_OBJECT。
     */
    void beginObject();

    /**
     * Consumes the next token and asserts it is END_OBJECT.
     * 消费下一个令牌并断言它是 END_OBJECT。
     */
    void endObject();

    /**
     * Consumes the next token and asserts it is START_ARRAY.
     * 消费下一个令牌并断言它是 START_ARRAY。
     */
    void beginArray();

    /**
     * Consumes the next token and asserts it is END_ARRAY.
     * 消费下一个令牌并断言它是 END_ARRAY。
     */
    void endArray();

    // ==================== Token Inspection ====================

    /**
     * Returns true if the current array or object has more elements.
     * 如果当前数组或对象有更多元素则返回 true。
     *
     * @return true if more elements exist - 如果存在更多元素则返回 true
     */
    boolean hasNext();

    /**
     * Returns the type of the next token without consuming it.
     * 返回下一个令牌的类型但不消费它。
     *
     * @return the next token type - 下一个令牌类型
     */
    JsonToken peek();

    // ==================== Name Reading ====================

    /**
     * Returns the next property name and consumes it.
     * 返回下一个属性名并消费它。
     *
     * @return the property name - 属性名
     */
    String nextName();

    // ==================== Value Reading ====================

    /**
     * Returns the string value of the next token and consumes it.
     * 返回下一个令牌的字符串值并消费它。
     *
     * @return the string value - 字符串值
     */
    String nextString();

    /**
     * Returns the boolean value of the next token and consumes it.
     * 返回下一个令牌的布尔值并消费它。
     *
     * @return the boolean value - 布尔值
     */
    boolean nextBoolean();

    /**
     * Consumes the next null value.
     * 消费下一个 null 值。
     */
    void nextNull();

    /**
     * Returns the int value of the next token and consumes it.
     * 返回下一个令牌的 int 值并消费它。
     *
     * @return the int value - int 值
     */
    int nextInt();

    /**
     * Returns the long value of the next token and consumes it.
     * 返回下一个令牌的 long 值并消费它。
     *
     * @return the long value - long 值
     */
    long nextLong();

    /**
     * Returns the double value of the next token and consumes it.
     * 返回下一个令牌的 double 值并消费它。
     *
     * @return the double value - double 值
     */
    double nextDouble();

    /**
     * Returns the BigInteger value of the next token and consumes it.
     * 返回下一个令牌的 BigInteger 值并消费它。
     *
     * @return the BigInteger value - BigInteger 值
     */
    BigInteger nextBigInteger();

    /**
     * Returns the BigDecimal value of the next token and consumes it.
     * 返回下一个令牌的 BigDecimal 值并消费它。
     *
     * @return the BigDecimal value - BigDecimal 值
     */
    BigDecimal nextBigDecimal();

    /**
     * Returns the Number value of the next token and consumes it.
     * 返回下一个令牌的 Number 值并消费它。
     *
     * @return the Number value - Number 值
     */
    Number nextNumber();

    // ==================== Skip Operations ====================

    /**
     * Skips the next value recursively.
     * 递归跳过下一个值。
     */
    void skipValue();

    /**
     * Skips the rest of the current object.
     * 跳过当前对象的其余部分。
     */
    default void skipObject() {
        int depth = 1;
        while (depth > 0) {
            JsonToken token = peek();
            switch (token) {
                case START_OBJECT -> {
                    beginObject();
                    depth++;
                }
                case END_OBJECT -> {
                    endObject();
                    depth--;
                }
                case NAME -> nextName();
                default -> skipValue();
            }
        }
    }

    /**
     * Skips the rest of the current array.
     * 跳过当前数组的其余部分。
     */
    default void skipArray() {
        int depth = 1;
        while (depth > 0) {
            JsonToken token = peek();
            switch (token) {
                case START_ARRAY -> {
                    beginArray();
                    depth++;
                }
                case END_ARRAY -> {
                    endArray();
                    depth--;
                }
                default -> skipValue();
            }
        }
    }

    // ==================== Path Information ====================

    /**
     * Returns the current path in the JSON document.
     * 返回 JSON 文档中的当前路径。
     *
     * @return the JSON path - JSON 路径
     */
    String getPath();

    /**
     * Returns the current line number (1-based).
     * 返回当前行号（从1开始）。
     *
     * @return the line number - 行号
     */
    int getLineNumber();

    /**
     * Returns the current column number (1-based).
     * 返回当前列号（从1开始）。
     *
     * @return the column number - 列号
     */
    int getColumnNumber();

    // ==================== Configuration ====================

    /**
     * Sets whether this reader is lenient in parsing.
     * 设置此读取器是否宽松解析。
     *
     * @param lenient true for lenient parsing - 如果宽松解析则为 true
     */
    void setLenient(boolean lenient);

    /**
     * Returns whether this reader is lenient.
     * 返回此读取器是否宽松。
     *
     * @return true if lenient - 如果宽松则返回 true
     */
    boolean isLenient();

    // ==================== Lifecycle ====================

    /**
     * Closes this reader and releases resources.
     * 关闭此读取器并释放资源。
     */
    @Override
    void close();
}
