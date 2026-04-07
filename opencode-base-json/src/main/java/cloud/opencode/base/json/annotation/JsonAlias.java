
package cloud.opencode.base.json.annotation;

import java.lang.annotation.*;

/**
 * JSON Alias - Defines Alternative Deserialization Names
 * JSON 别名 - 定义反序列化时的替代名称
 *
 * <p>This annotation defines one or more alternative names that are accepted
 * during deserialization in addition to the primary property name. Aliases
 * are only used for deserialization (reading); serialization always uses
 * the primary name.</p>
 * <p>此注解定义一个或多个在反序列化时接受的替代名称，作为主属性名的补充。
 * 别名仅用于反序列化（读取）；序列化始终使用主名称。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * public class User {
 *     @JsonAlias({"user_name", "username", "login"})
 *     private String name;
 *
 *     @JsonAlias({"mail", "e-mail"})
 *     @JsonProperty("email")
 *     private String email;
 * }
 * // All of these JSON inputs map to "name":
 * // {"name":"Alice"}, {"user_name":"Alice"}, {"username":"Alice"}, {"login":"Alice"}
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Multiple alternative names for deserialization - 反序列化时的多个替代名称</li>
 *   <li>Works with {@link JsonProperty} for primary name - 与 {@link JsonProperty} 配合使用主名称</li>
 *   <li>Supports fields, methods, and parameters - 支持字段、方法和参数</li>
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
 * @see JsonProperty
 * @since JDK 25, opencode-base-json V1.0.0
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JsonAlias {

    /**
     * The alternative names accepted during deserialization.
     * 反序列化时接受的替代名称。
     *
     * @return the array of alias names - 别名数组
     */
    String[] value();
}
