
package cloud.opencode.base.json.annotation;

import cloud.opencode.base.json.adapter.JsonTypeAdapter;

import java.lang.annotation.*;

/**
 * JSON Serialize - Specifies Custom Serializer
 * JSON 序列化 - 指定自定义序列化器
 *
 * <p>This annotation specifies a custom serializer for a field, method, or class.
 * It allows fine-grained control over how values are converted to JSON.</p>
 * <p>此注解为字段、方法或类指定自定义序列化器。
 * 它允许对值如何转换为 JSON 进行细粒度控制。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * public class Order {
 *     @JsonSerialize(using = MoneyAdapter.class)
 *     private Money totalPrice;
 *
 *     @JsonSerialize(contentUsing = ItemAdapter.class)
 *     private List<Item> items;
 *
 *     @JsonSerialize(keyUsing = CurrencyAdapter.class)
 *     private Map<Currency, BigDecimal> balances;
 *
 *     @JsonSerialize(as = CharSequence.class)
 *     private StringBuilder description;
 * }
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Custom serializer for values - 值的自定义序列化器</li>
 *   <li>Custom serializer for collection elements - 集合元素的自定义序列化器</li>
 *   <li>Custom serializer for map keys - Map 键的自定义序列化器</li>
 *   <li>Type coercion via {@code as}/{@code contentAs}/{@code keyAs} - 通过 as/contentAs/keyAs 进行类型转换</li>
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
public @interface JsonSerialize {

    /**
     * The serializer class to use for the annotated value.
     * 用于注解值的序列化器类。
     *
     * @return the serializer class, or {@code JsonTypeAdapter.None.class} for default
     *         - 序列化器类，{@code JsonTypeAdapter.None.class} 表示使用默认
     */
    Class<? extends JsonTypeAdapter<?>> using() default JsonTypeAdapter.None.class;

    /**
     * The serializer class to use for collection/array elements.
     * 用于集合/数组元素的序列化器类。
     *
     * @return the content serializer class - 内容序列化器类
     */
    Class<? extends JsonTypeAdapter<?>> contentUsing() default JsonTypeAdapter.None.class;

    /**
     * The serializer class to use for map keys.
     * 用于 Map 键的序列化器类。
     *
     * @return the key serializer class - 键序列化器类
     */
    Class<? extends JsonTypeAdapter<?>> keyUsing() default JsonTypeAdapter.None.class;

    /**
     * The type to serialize the value as (supertype or interface).
     * 将值序列化为的类型（超类型或接口）。
     *
     * @return the target type, or {@code Void.class} for default - 目标类型，{@code Void.class} 表示使用默认
     */
    Class<?> as() default Void.class;

    /**
     * The type to serialize collection/array elements as.
     * 将集合/数组元素序列化为的类型。
     *
     * @return the content target type - 内容目标类型
     */
    Class<?> contentAs() default Void.class;

    /**
     * The type to serialize map keys as.
     * 将 Map 键序列化为的类型。
     *
     * @return the key target type - 键目标类型
     */
    Class<?> keyAs() default Void.class;
}
