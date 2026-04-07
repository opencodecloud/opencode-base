
package cloud.opencode.base.json.annotation;

import java.lang.annotation.*;

/**
 * JSON Include - Controls Property Inclusion Based on Value
 * JSON 包含 - 基于值控制属性的包含策略
 *
 * <p>This annotation controls when properties are included during serialization
 * based on their values. For example, null values or empty collections can be
 * excluded from the serialized output.</p>
 * <p>此注解根据属性值控制序列化时是否包含该属性。例如，可以从序列化输出中
 * 排除null值或空集合。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * @JsonInclude(JsonInclude.Include.NON_NULL)
 * public class User {
 *     private String name;
 *     private String email;    // excluded if null
 *     private List<String> tags; // excluded if null
 * }
 *
 * @JsonInclude(value = JsonInclude.Include.NON_EMPTY,
 *              content = JsonInclude.Include.NON_NULL)
 * public class Config {
 *     private Map<String, String> settings; // excluded if empty, null values excluded
 * }
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Exclude null values from serialization - 序列化时排除null值</li>
 *   <li>Exclude empty strings, collections, maps - 排除空字符串、集合、映射</li>
 *   <li>Exclude absent Optional values - 排除空的Optional值</li>
 *   <li>Content-level inclusion for maps and collections - 映射和集合的内容级别包含策略</li>
 *   <li>Custom filter support - 自定义过滤器支持</li>
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
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JsonInclude {

    /**
     * Inclusion strategy for property values
     * 属性值的包含策略
     */
    enum Include {
        /**
         * Always include the property, regardless of its value.
         * 始终包含该属性，无论其值是什么。
         */
        ALWAYS,

        /**
         * Exclude properties with null values.
         * 排除值为null的属性。
         */
        NON_NULL,

        /**
         * Exclude properties with null values or absent Optional values.
         * 排除值为null或Optional.empty()的属性。
         */
        NON_ABSENT,

        /**
         * Exclude properties with null values, empty strings, or empty collections/maps.
         * 排除值为null、空字符串或空集合/映射的属性。
         */
        NON_EMPTY,

        /**
         * Exclude properties that have their default value (e.g., 0 for int, null for objects).
         * 排除具有默认值的属性（例如int的0、对象的null）。
         */
        NON_DEFAULT,

        /**
         * Use a custom filter to determine inclusion.
         * 使用自定义过滤器来决定是否包含。
         */
        CUSTOM,

        /**
         * Use defaults from class-level annotation or global configuration.
         * 使用类级别注解或全局配置的默认值。
         */
        USE_DEFAULTS
    }

    /**
     * Inclusion strategy for the property value during serialization.
     * 序列化时属性值的包含策略。
     *
     * @return the inclusion strategy - 包含策略
     */
    Include value() default Include.ALWAYS;

    /**
     * Inclusion strategy for content of maps and collections.
     * 映射和集合内容的包含策略。
     *
     * <p>This controls inclusion of individual map values or collection elements,
     * rather than the map/collection property itself.</p>
     * <p>此策略控制映射中各个值或集合中各个元素的包含，而非映射/集合属性本身。</p>
     *
     * @return the content inclusion strategy - 内容包含策略
     */
    Include content() default Include.ALWAYS;
}
