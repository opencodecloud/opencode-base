
package cloud.opencode.base.json.annotation;

import java.lang.annotation.*;

/**
 * JSON Sub Types - Known Subtypes for Polymorphic Deserialization
 * JSON 子类型 - 多态反序列化的已知子类型
 *
 * <p>This annotation lists known subtypes of a polymorphic base type, enabling
 * the JSON framework to resolve concrete implementations during deserialization.
 * It is typically used in conjunction with {@link JsonTypeInfo}.</p>
 * <p>此注解列出多态基类型的已知子类型，使 JSON 框架能够在反序列化期间
 * 解析具体实现。通常与 {@link JsonTypeInfo} 一起使用。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * @JsonTypeInfo(id = JsonTypeInfo.Id.NAME, property = "type")
 * @JsonSubTypes({
 *     @JsonSubTypes.Type(value = Circle.class, name = "circle"),
 *     @JsonSubTypes.Type(value = Rectangle.class, name = "rectangle"),
 *     @JsonSubTypes.Type(value = Triangle.class, name = "triangle", names = {"tri", "三角形"})
 * })
 * public abstract class Shape {
 *     public abstract double area();
 * }
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Declare known subtypes for polymorphic deserialization - 声明多态反序列化的已知子类型</li>
 *   <li>Map logical names to concrete implementations - 将逻辑名映射到具体实现</li>
 *   <li>Support alternative name aliases for subtypes - 支持子类型的别名</li>
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
 * @see JsonTypeInfo
 * @see JsonTypeName
 * @since JDK 25, opencode-base-json V1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JsonSubTypes {

    /**
     * The known subtypes.
     * 已知的子类型。
     *
     * @return array of known subtype declarations - 已知子类型声明的数组
     */
    Type[] value();

    /**
     * Known Subtype Declaration
     * 已知子类型声明
     *
     * <p>Declares a known subtype with its class and optional logical name(s)
     * for polymorphic type resolution.</p>
     * <p>声明一个已知子类型及其类和可选的逻辑名，用于多态类型解析。</p>
     *
     * @author Leon Soo
     * <a href="https://leonsoo.com">www.LeonSoo.com</a>
     * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
     * @since JDK 25, opencode-base-json V1.0.0
     */
    @Target({})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface Type {

        /**
         * The subtype class.
         * 子类型的类。
         *
         * @return the concrete implementation class - 具体实现类
         */
        Class<?> value();

        /**
         * The primary logical type name for this subtype.
         * 此子类型的主要逻辑类型名。
         *
         * @return the logical name, or empty for default - 逻辑名，空则使用默认值
         */
        String name() default "";

        /**
         * Alternative logical type names (aliases) for this subtype.
         * 此子类型的替代逻辑类型名（别名）。
         *
         * @return array of alternative names - 替代名称的数组
         */
        String[] names() default {};
    }
}
