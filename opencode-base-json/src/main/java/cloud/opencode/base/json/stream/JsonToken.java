
package cloud.opencode.base.json.stream;

/**
 * JSON Token - Token Types for Streaming JSON Parser
 * JSON 令牌 - 流式 JSON 解析器的令牌类型
 *
 * <p>This enum represents the different token types that can be
 * encountered when parsing JSON using a streaming parser.</p>
 * <p>此枚举表示使用流式解析器解析 JSON 时可能遇到的不同令牌类型。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * try (JsonReader reader = OpenJson.createReader(inputStream)) {
 *     while (reader.hasNext()) {
 *         JsonToken token = reader.peek();
 *         switch (token) {
 *             case START_OBJECT -> reader.beginObject();
 *             case NAME -> {
 *                 String name = reader.nextName();
 *                 System.out.println("Field: " + name);
 *             }
 *             case STRING -> System.out.println("Value: " + reader.nextString());
 *             case END_OBJECT -> reader.endObject();
 *         }
 *     }
 * }
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Enumeration of all JSON token types - 所有JSON令牌类型的枚举</li>
 *   <li>Token classification (scalar, structure, value) - 令牌分类（标量、结构、值）</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: N/A - 空值安全: 不适用</li>
 * </ul>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
public enum JsonToken {

    /**
     * Start of a JSON object ({).
     * JSON 对象的开始 ({)。
     */
    START_OBJECT,

    /**
     * End of a JSON object (}).
     * JSON 对象的结束 (})。
     */
    END_OBJECT,

    /**
     * Start of a JSON array ([).
     * JSON 数组的开始 ([)。
     */
    START_ARRAY,

    /**
     * End of a JSON array (]).
     * JSON 数组的结束 (])。
     */
    END_ARRAY,

    /**
     * A JSON property name.
     * JSON 属性名。
     */
    NAME,

    /**
     * A JSON string value.
     * JSON 字符串值。
     */
    STRING,

    /**
     * A JSON number value.
     * JSON 数字值。
     */
    NUMBER,

    /**
     * A JSON boolean value (true or false).
     * JSON 布尔值 (true 或 false)。
     */
    BOOLEAN,

    /**
     * A JSON null value.
     * JSON null 值。
     */
    NULL,

    /**
     * End of JSON document.
     * JSON 文档结束。
     */
    END_DOCUMENT,

    /**
     * Not available - parser needs to be advanced.
     * 不可用 - 解析器需要前进。
     */
    NOT_AVAILABLE;

    /**
     * Returns whether this token starts a structured value (object or array).
     * 返回此令牌是否开始一个结构化值（对象或数组）。
     *
     * @return true if START_OBJECT or START_ARRAY - 如果是 START_OBJECT 或 START_ARRAY 则返回 true
     */
    public boolean isStructureStart() {
        return this == START_OBJECT || this == START_ARRAY;
    }

    /**
     * Returns whether this token ends a structured value (object or array).
     * 返回此令牌是否结束一个结构化值（对象或数组）。
     *
     * @return true if END_OBJECT or END_ARRAY - 如果是 END_OBJECT 或 END_ARRAY 则返回 true
     */
    public boolean isStructureEnd() {
        return this == END_OBJECT || this == END_ARRAY;
    }

    /**
     * Returns whether this token represents a scalar value.
     * 返回此令牌是否表示标量值。
     *
     * @return true if STRING, NUMBER, BOOLEAN, or NULL - 如果是 STRING、NUMBER、BOOLEAN 或 NULL 则返回 true
     */
    public boolean isScalarValue() {
        return this == STRING || this == NUMBER || this == BOOLEAN || this == NULL;
    }

    /**
     * Returns whether this token represents any value (scalar or structured).
     * 返回此令牌是否表示任何值（标量或结构化）。
     *
     * @return true if this is a value token - 如果是值令牌则返回 true
     */
    public boolean isValue() {
        return isScalarValue() || isStructureStart();
    }

    /**
     * Returns whether this is a numeric token.
     * 返回此令牌是否为数字令牌。
     *
     * @return true if NUMBER - 如果是 NUMBER 则返回 true
     */
    public boolean isNumeric() {
        return this == NUMBER;
    }
}
