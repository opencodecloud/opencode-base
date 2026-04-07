
package cloud.opencode.base.json.annotation;

import java.lang.annotation.*;

/**
 * JSON Unwrapped - Unwraps Nested Object Properties
 * JSON 展开 - 展开嵌套对象属性
 *
 * <p>This annotation indicates that a nested object's properties should be
 * serialized as if they belong to the parent object, effectively "unwrapping"
 * the nested structure. During deserialization, the flat properties are
 * "wrapped" back into the nested object.</p>
 * <p>此注解表示嵌套对象的属性应序列化为父对象的属性，
 * 有效地"展开"嵌套结构。反序列化时，扁平属性将被
 * "包装"回嵌套对象。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * public class Order {
 *     private String orderId;
 *
 *     @JsonUnwrapped
 *     private Address shippingAddress;
 *
 *     @JsonUnwrapped(prefix = "billing_")
 *     private Address billingAddress;
 * }
 *
 * public class Address {
 *     private String street;
 *     private String city;
 * }
 *
 * // Without @JsonUnwrapped:
 * // {"orderId":"1", "shippingAddress":{"street":"...", "city":"..."}}
 *
 * // With @JsonUnwrapped:
 * // {"orderId":"1", "street":"...", "city":"...", "billing_street":"...", "billing_city":"..."}
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Flatten nested object properties into parent - 将嵌套对象属性展开到父级</li>
 *   <li>Prefix support to avoid name conflicts - 前缀支持以避免名称冲突</li>
 *   <li>Suffix support for additional naming control - 后缀支持以进行额外的命名控制</li>
 *   <li>Can be disabled via {@code enabled = false} - 可通过 {@code enabled = false} 禁用</li>
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
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JsonUnwrapped {

    /**
     * Whether unwrapping is enabled.
     * 是否启用展开。
     *
     * <p>Can be set to {@code false} to programmatically disable unwrapping.</p>
     * <p>可设置为 {@code false} 以编程方式禁用展开。</p>
     *
     * @return true if unwrapping is enabled (default) - 如果启用展开则返回 true（默认）
     */
    boolean enabled() default true;

    /**
     * Prefix to prepend to unwrapped property names.
     * 添加到展开属性名前的前缀。
     *
     * <p>Useful for avoiding name conflicts when multiple objects are unwrapped
     * into the same parent.</p>
     * <p>在多个对象展开到同一父级时，用于避免名称冲突。</p>
     *
     * @return the prefix, or empty string for no prefix - 前缀，空字符串表示无前缀
     */
    String prefix() default "";

    /**
     * Suffix to append to unwrapped property names.
     * 添加到展开属性名后的后缀。
     *
     * @return the suffix, or empty string for no suffix - 后缀，空字符串表示无后缀
     */
    String suffix() default "";
}
