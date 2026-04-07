
package cloud.opencode.base.json.annotation;

import java.lang.annotation.*;

/**
 * JSON Type Info - Polymorphic Type Handling Configuration
 * JSON 类型信息 - 多态类型处理配置
 *
 * <p>This annotation configures how polymorphic type information is serialized
 * and deserialized. It defines the type identifier mechanism and the inclusion
 * strategy for type metadata in JSON.</p>
 * <p>此注解配置多态类型信息的序列化和反序列化方式。它定义了类型标识机制以及
 * JSON 中类型元数据的包含策略。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * @JsonTypeInfo(id = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
 * @JsonSubTypes({
 *     @JsonSubTypes.Type(value = Dog.class, name = "dog"),
 *     @JsonSubTypes.Type(value = Cat.class, name = "cat")
 * })
 * public abstract class Animal {
 *     private String name;
 * }
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Multiple type identifier strategies (class name, logical name, custom) - 多种类型标识策略（类名、逻辑名、自定义）</li>
 *   <li>Flexible inclusion modes (property, wrapper, external) - 灵活的包含模式（属性、包装器、外部）</li>
 *   <li>Default implementation fallback for unknown types - 未知类型的默认实现回退</li>
 *   <li>Visibility control for type identifier - 类型标识的可见性控制</li>
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
 * @see JsonSubTypes
 * @see JsonTypeName
 * @since JDK 25, opencode-base-json V1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JsonTypeInfo {

    /**
     * Type identifier mechanism
     * 类型标识机制
     */
    enum Id {
        /**
         * Fully qualified Java class name.
         * 完全限定的 Java 类名。
         */
        CLASS,

        /**
         * Minimal class name (without common package prefix).
         * 最小类名（不含公共包前缀）。
         */
        MINIMAL_CLASS,

        /**
         * Logical type name (must be defined via {@link JsonTypeName} or {@link JsonSubTypes.Type}).
         * 逻辑类型名（须通过 {@link JsonTypeName} 或 {@link JsonSubTypes.Type} 定义）。
         */
        NAME,

        /**
         * Custom type identifier resolved by application-specific logic.
         * 由应用程序特定逻辑解析的自定义类型标识。
         */
        CUSTOM
    }

    /**
     * Type information inclusion strategy
     * 类型信息包含策略
     */
    enum As {
        /**
         * Type info is included as a JSON property of the object.
         * 类型信息作为对象的 JSON 属性包含。
         */
        PROPERTY,

        /**
         * Type info wraps the object in a single-entry JSON object.
         * 类型信息将对象包装在单条目 JSON 对象中。
         */
        WRAPPER_OBJECT,

        /**
         * Type info wraps the object in a two-element JSON array.
         * 类型信息将对象包装在双元素 JSON 数组中。
         */
        WRAPPER_ARRAY,

        /**
         * Type info uses an existing property already present in the object.
         * 类型信息使用对象中已存在的属性。
         */
        EXISTING_PROPERTY,

        /**
         * Type info is included as a separate sibling property outside the object.
         * 类型信息作为对象外部的单独兄弟属性包含。
         */
        EXTERNAL_PROPERTY
    }

    /**
     * The type identifier mechanism to use.
     * 要使用的类型标识机制。
     *
     * @return the type identifier - 类型标识
     */
    Id id();

    /**
     * The inclusion strategy for type information.
     * 类型信息的包含策略。
     *
     * @return the inclusion strategy, default is {@link As#PROPERTY} - 包含策略，默认为 {@link As#PROPERTY}
     */
    As include() default As.PROPERTY;

    /**
     * The property name used for the type identifier when using property-based inclusion.
     * 使用基于属性的包含方式时，类型标识所使用的属性名。
     *
     * @return the property name, default is "@type" - 属性名，默认为 "@type"
     */
    String property() default "@type";

    /**
     * The default implementation class to use when the type identifier is unknown or missing.
     * 当类型标识未知或缺失时使用的默认实现类。
     *
     * @return the default implementation class, {@link Void} means none - 默认实现类，{@link Void} 表示无
     */
    Class<?> defaultImpl() default Void.class;

    /**
     * Whether the type identifier property should remain visible to the deserializer
     * after type resolution.
     * 类型解析后，类型标识属性是否对反序列化器保持可见。
     *
     * @return true if visible, default is false - 如果可见则返回 true，默认为 false
     */
    boolean visible() default false;
}
