
package cloud.opencode.base.json.annotation;

import java.lang.annotation.*;

/**
 * JSON Ignore - Excludes Field from JSON Processing
 * JSON 忽略 - 从 JSON 处理中排除字段
 *
 * <p>This annotation marks a field to be excluded from JSON
 * serialization and/or deserialization.</p>
 * <p>此注解标记字段从 JSON 序列化和/或反序列化中排除。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * public class User {
 *     private String username;
 *
 *     @JsonIgnore
 *     private String password;  // Never serialized or deserialized
 *
 *     @JsonIgnore(serialize = false)
 *     private String tempToken; // Only deserialized, never serialized
 * }
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Selective serialization/deserialization exclusion - 选择性序列化/反序列化排除</li>
 *   <li>Independent control for read and write directions - 读写方向的独立控制</li>
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
public @interface JsonIgnore {

    /**
     * Whether to ignore during serialization.
     * 是否在序列化时忽略。
     *
     * @return true to ignore during serialization - 如果在序列化时忽略则返回 true
     */
    boolean serialize() default true;

    /**
     * Whether to ignore during deserialization.
     * 是否在反序列化时忽略。
     *
     * @return true to ignore during deserialization - 如果在反序列化时忽略则返回 true
     */
    boolean deserialize() default true;
}
