
package cloud.opencode.base.json.annotation;

import java.lang.annotation.*;

/**
 * JSON Creator - Marks a Constructor or Factory Method for Deserialization
 * JSON 创建器 - 标记用于反序列化的构造函数或工厂方法
 *
 * <p>This annotation indicates that the annotated constructor or static factory method
 * should be used to create instances during JSON deserialization.</p>
 * <p>此注解表示被标注的构造函数或静态工厂方法应在 JSON 反序列化时用于创建实例。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * public class User {
 *     private final String name;
 *     private final int age;
 *
 *     @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
 *     public User(@JsonProperty("name") String name,
 *                 @JsonProperty("age") int age) {
 *         this.name = name;
 *         this.age = age;
 *     }
 *
 *     @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
 *     public static User fromString(String value) {
 *         return new User(value, 0);
 *     }
 * }
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Constructor-based deserialization - 基于构造函数的反序列化</li>
 *   <li>Factory method deserialization - 工厂方法反序列化</li>
 *   <li>Delegating mode for single-value types - 委托模式用于单值类型</li>
 *   <li>Properties mode for multi-argument constructors - 属性模式用于多参数构造函数</li>
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
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JsonCreator {

    /**
     * Creator mode that defines how arguments are bound.
     * 创建器模式，定义参数的绑定方式。
     */
    enum Mode {
        /**
         * Auto-detect the creator mode based on context.
         * 根据上下文自动检测创建器模式。
         */
        DEFAULT,

        /**
         * Single-argument creator where the argument represents the entire JSON value.
         * 单参数创建器，参数表示整个 JSON 值。
         */
        DELEGATING,

        /**
         * Multi-argument creator where each argument corresponds to a JSON property.
         * Use with {@link JsonProperty} on parameters to specify property names.
         * 多参数创建器，每个参数对应一个 JSON 属性。
         * 与参数上的 {@link JsonProperty} 配合使用以指定属性名。
         */
        PROPERTIES,

        /**
         * Explicitly disable this creator.
         * 显式禁用此创建器。
         */
        DISABLED
    }

    /**
     * The creator mode to use.
     * 使用的创建器模式。
     *
     * @return the creator mode, defaults to {@link Mode#DEFAULT} - 创建器模式，默认为 {@link Mode#DEFAULT}
     */
    Mode mode() default Mode.DEFAULT;
}
