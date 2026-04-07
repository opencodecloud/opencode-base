
package cloud.opencode.base.json.annotation;

import java.lang.annotation.*;

/**
 * JSON Type Name - Logical Type Name for Polymorphic Handling
 * JSON 类型名 - 多态处理的逻辑类型名
 *
 * <p>This annotation defines the logical type name for a class used in
 * polymorphic type handling. The name is used as the type identifier when
 * {@link JsonTypeInfo.Id#NAME} is selected as the identifier mechanism.</p>
 * <p>此注解定义用于多态类型处理的类的逻辑类型名。当选择 {@link JsonTypeInfo.Id#NAME}
 * 作为标识机制时，该名称用作类型标识符。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * @JsonTypeName("dog")
 * public class Dog extends Animal {
 *     private String breed;
 * }
 *
 * @JsonTypeName("cat")
 * public class Cat extends Animal {
 *     private boolean indoor;
 * }
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Define logical type name for polymorphic serialization - 定义多态序列化的逻辑类型名</li>
 *   <li>Works with {@link JsonTypeInfo} and {@link JsonSubTypes} - 与 {@link JsonTypeInfo} 和 {@link JsonSubTypes} 配合使用</li>
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
 * @see JsonSubTypes
 * @since JDK 25, opencode-base-json V1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JsonTypeName {

    /**
     * The logical type name for this class.
     * 此类的逻辑类型名。
     *
     * @return the logical name, or empty for default (typically simple class name) - 逻辑名，空则使用默认值（通常为简单类名）
     */
    String value() default "";
}
