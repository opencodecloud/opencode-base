
package cloud.opencode.base.json.annotation;

import cloud.opencode.base.json.adapter.JsonTypeAdapter;

import java.lang.annotation.*;

/**
 * JSON Deserialize - Specifies Custom Deserializer
 * JSON 反序列化 - 指定自定义反序列化器
 *
 * <p>This annotation specifies a custom deserializer for a field, method, class,
 * or parameter. It allows fine-grained control over how JSON values are
 * converted to Java objects.</p>
 * <p>此注解为字段、方法、类或参数指定自定义反序列化器。
 * 它允许对 JSON 值如何转换为 Java 对象进行细粒度控制。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * public class Order {
 *     @JsonDeserialize(using = MoneyAdapter.class)
 *     private Money totalPrice;
 *
 *     @JsonDeserialize(contentUsing = ItemAdapter.class)
 *     private List<Item> items;
 *
 *     @JsonDeserialize(as = LinkedHashMap.class)
 *     private Map<String, Object> metadata;
 *
 *     @JsonDeserialize(builder = Order.Builder.class)
 *     public static class ImmutableOrder { }
 * }
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Custom deserializer for values - 值的自定义反序列化器</li>
 *   <li>Custom deserializer for collection elements - 集合元素的自定义反序列化器</li>
 *   <li>Custom deserializer for map keys - Map 键的自定义反序列化器</li>
 *   <li>Concrete type specification via {@code as}/{@code contentAs}/{@code keyAs} - 通过 as/contentAs/keyAs 指定具体类型</li>
 *   <li>Builder class for immutable types - 不可变类型的 Builder 类</li>
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
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JsonDeserialize {

    /**
     * The deserializer class to use for the annotated value.
     * 用于注解值的反序列化器类。
     *
     * @return the deserializer class, or {@code JsonTypeAdapter.None.class} for default
     *         - 反序列化器类，{@code JsonTypeAdapter.None.class} 表示使用默认
     */
    Class<? extends JsonTypeAdapter<?>> using() default JsonTypeAdapter.None.class;

    /**
     * The deserializer class to use for collection/array elements.
     * 用于集合/数组元素的反序列化器类。
     *
     * @return the content deserializer class - 内容反序列化器类
     */
    Class<? extends JsonTypeAdapter<?>> contentUsing() default JsonTypeAdapter.None.class;

    /**
     * The deserializer class to use for map keys.
     * 用于 Map 键的反序列化器类。
     *
     * @return the key deserializer class - 键反序列化器类
     */
    Class<? extends JsonTypeAdapter<?>> keyUsing() default JsonTypeAdapter.None.class;

    /**
     * The concrete type to deserialize the value as.
     * 将值反序列化为的具体类型。
     *
     * <p>Useful for deserializing interface or abstract types to concrete implementations.</p>
     * <p>用于将接口或抽象类型反序列化为具体实现。</p>
     *
     * @return the target type, or {@code Void.class} for default - 目标类型，{@code Void.class} 表示使用默认
     */
    Class<?> as() default Void.class;

    /**
     * The concrete type to deserialize collection/array elements as.
     * 将集合/数组元素反序列化为的具体类型。
     *
     * @return the content target type - 内容目标类型
     */
    Class<?> contentAs() default Void.class;

    /**
     * The concrete type to deserialize map keys as.
     * 将 Map 键反序列化为的具体类型。
     *
     * @return the key target type - 键目标类型
     */
    Class<?> keyAs() default Void.class;

    /**
     * The builder class to use for constructing immutable types.
     * 用于构造不可变类型的 Builder 类。
     *
     * <p>The builder class should follow the builder pattern with a
     * {@code build()} method that returns the target type.</p>
     * <p>Builder 类应遵循构建者模式，包含返回目标类型的 {@code build()} 方法。</p>
     *
     * @return the builder class, or {@code Void.class} if not applicable
     *         - Builder 类，{@code Void.class} 表示不适用
     */
    Class<?> builder() default Void.class;
}
