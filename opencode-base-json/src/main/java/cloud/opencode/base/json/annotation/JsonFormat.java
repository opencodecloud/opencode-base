
package cloud.opencode.base.json.annotation;

import java.lang.annotation.*;

/**
 * JSON Format - Specifies Serialization Format
 * JSON 格式 - 指定序列化格式
 *
 * <p>This annotation specifies the format for serializing and deserializing
 * values, particularly useful for dates and numbers.</p>
 * <p>此注解指定序列化和反序列化值的格式，特别适用于日期和数字。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * public class Event {
 *     @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
 *     private LocalDateTime eventTime;
 *
 *     @JsonFormat(shape = Shape.STRING, pattern = "0.00")
 *     private BigDecimal price;
 *
 *     @JsonFormat(shape = Shape.NUMBER)
 *     private Status status;  // Enum serialized as number
 * }
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Date/time pattern formatting - 日期/时间模式格式化</li>
 *   <li>Shape control for value output (string, number, array, etc.) - 值输出形状控制</li>
 *   <li>Timezone and locale configuration - 时区和区域设置配置</li>
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
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JsonFormat {

    /**
     * Shape of the JSON value
     * JSON 值的形状
     */
    enum Shape {
        /**
         * Default shape based on type.
         * 基于类型的默认形状。
         */
        ANY,

        /**
         * Force scalar (non-structured) value.
         * 强制标量（非结构化）值。
         */
        SCALAR,

        /**
         * Force array output.
         * 强制数组输出。
         */
        ARRAY,

        /**
         * Force object output.
         * 强制对象输出。
         */
        OBJECT,

        /**
         * Force number output.
         * 强制数字输出。
         */
        NUMBER,

        /**
         * Force integer number output.
         * 强制整数输出。
         */
        NUMBER_INT,

        /**
         * Force floating-point number output.
         * 强制浮点数输出。
         */
        NUMBER_FLOAT,

        /**
         * Force string output.
         * 强制字符串输出。
         */
        STRING,

        /**
         * Force boolean output.
         * 强制布尔值输出。
         */
        BOOLEAN
    }

    /**
     * The pattern for formatting (e.g., date patterns).
     * 格式化模式（如日期模式）。
     *
     * @return the format pattern - 格式模式
     */
    String pattern() default "";

    /**
     * The shape of the serialized value.
     * 序列化值的形状。
     *
     * @return the shape - 形状
     */
    Shape shape() default Shape.ANY;

    /**
     * The timezone for date/time values.
     * 日期/时间值的时区。
     *
     * @return the timezone ID (e.g., "UTC", "Asia/Shanghai") - 时区ID
     */
    String timezone() default "";

    /**
     * The locale for formatting.
     * 格式化的区域设置。
     *
     * @return the locale (e.g., "zh_CN", "en_US") - 区域设置
     */
    String locale() default "";

    /**
     * Whether lenient parsing is enabled.
     * 是否启用宽松解析。
     *
     * @return true for lenient parsing - 如果宽松解析则返回 true
     */
    boolean lenient() default false;
}
