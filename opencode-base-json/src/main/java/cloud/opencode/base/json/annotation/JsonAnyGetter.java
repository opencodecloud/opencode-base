
package cloud.opencode.base.json.annotation;

import java.lang.annotation.*;

/**
 * JSON Any Getter - Serializes Map Entries as Regular Properties
 * JSON 任意获取器 - 将 Map 条目序列化为普通属性
 *
 * <p>This annotation marks a no-argument method returning a {@link java.util.Map}
 * or a {@link java.util.Map} field whose entries are serialized as regular
 * JSON properties alongside other declared properties.</p>
 * <p>此注解标记一个无参方法（返回 {@link java.util.Map}）或一个
 * {@link java.util.Map} 字段，其条目将与其他声明的属性一起序列化为普通 JSON 属性。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * public class ExtensibleConfig {
 *     private String name;
 *     private Map<String, Object> extras = new LinkedHashMap<>();
 *
 *     @JsonAnyGetter
 *     public Map<String, Object> getExtras() {
 *         return extras;
 *     }
 * }
 * // Serializes as: {"name":"...", "key1":"val1", "key2":"val2"}
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Flattens Map entries into the parent JSON object - 将 Map 条目展平到父 JSON 对象中</li>
 *   <li>Supports both method and Map field targets - 支持方法和 Map 字段目标</li>
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
 * @see JsonAnySetter
 * @since JDK 25, opencode-base-json V1.0.0
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JsonAnyGetter {

    /**
     * Whether this any-getter is enabled.
     * 此任意获取器是否启用。
     *
     * @return true if enabled - 如果启用则返回 true
     */
    boolean enabled() default true;
}
