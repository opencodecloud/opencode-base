
package cloud.opencode.base.json.annotation;

import java.lang.annotation.*;

/**
 * JSON Property Order - Controls Property Serialization Order
 * JSON 属性排序 - 控制属性序列化顺序
 *
 * <p>This annotation specifies the order in which properties are serialized.
 * Properties listed in {@link #value()} appear first in the specified order,
 * followed by any remaining properties.</p>
 * <p>此注解指定属性序列化的顺序。{@link #value()} 中列出的属性按指定顺序优先出现，
 * 其余属性随后输出。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * @JsonPropertyOrder(value = {"id", "name", "email"}, alphabetic = true)
 * public class User {
 *     private String email;
 *     private String name;
 *     private long id;
 *     private String phone;
 * }
 * // Serialized order: id, name, email, phone (remaining sorted alphabetically)
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Explicit property ordering - 显式属性排序</li>
 *   <li>Alphabetic sorting for remaining properties - 剩余属性按字母排序</li>
 *   <li>Deterministic serialization output - 确定性的序列化输出</li>
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
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JsonPropertyOrder {

    /**
     * Ordered list of property names that define serialization order.
     * 定义序列化顺序的属性名有序列表。
     *
     * <p>Properties listed here appear first, in the specified order.
     * Any remaining properties follow after.</p>
     * <p>列出的属性按指定顺序优先出现，其余属性随后输出。</p>
     *
     * @return the ordered property names - 有序属性名数组
     */
    String[] value() default {};

    /**
     * Whether to sort remaining properties alphabetically.
     * 是否按字母顺序排列剩余属性。
     *
     * <p>If true, properties not listed in {@link #value()} are sorted
     * alphabetically. If false, they appear in their natural order.</p>
     * <p>如果为true，未在 {@link #value()} 中列出的属性按字母顺序排列。
     * 如果为false，按其自然顺序出现。</p>
     *
     * @return true to sort remaining properties alphabetically - 为true时按字母排序剩余属性
     */
    boolean alphabetic() default false;
}
