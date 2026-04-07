
package cloud.opencode.base.json.annotation;

import java.lang.annotation.*;

/**
 * JSON Filter - Specifies a Named Property Filter
 * JSON 过滤器 - 指定命名的属性过滤器
 *
 * <p>This annotation associates a named filter with a type, method, or field.
 * The filter can then be used to dynamically include or exclude properties
 * during serialization based on runtime logic.</p>
 * <p>此注解将命名过滤器与类型、方法或字段关联。该过滤器可用于在序列化期间
 * 基于运行时逻辑动态包含或排除属性。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * @JsonFilter("sensitiveFilter")
 * public class UserProfile {
 *     private String name;
 *     private String email;
 *     private String ssn;
 * }
 * // The "sensitiveFilter" can be configured at runtime to exclude "ssn"
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Dynamic property filtering at runtime - 运行时动态属性过滤</li>
 *   <li>Named filters for reusability - 命名过滤器以便复用</li>
 *   <li>Applicable to types, methods, and fields - 可应用于类型、方法和字段</li>
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
public @interface JsonFilter {

    /**
     * The filter identifier.
     * 过滤器标识符。
     *
     * @return the filter id - 过滤器 ID
     */
    String value();
}
