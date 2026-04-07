
package cloud.opencode.base.json.annotation;

import java.lang.annotation.*;

/**
 * JSON Any Setter - Captures Unmatched JSON Properties
 * JSON 任意设置器 - 捕获未匹配的 JSON 属性
 *
 * <p>This annotation marks a method (with two arguments: key and value) or a
 * {@link java.util.Map} field to receive any JSON properties that do not map
 * to a known Java field during deserialization.</p>
 * <p>此注解标记一个方法（两个参数：键和值）或一个 {@link java.util.Map} 字段，
 * 用于在反序列化时接收任何未映射到已知 Java 字段的 JSON 属性。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * public class ExtensibleConfig {
 *     private String name;
 *
 *     @JsonAnySetter
 *     private Map<String, Object> extras = new LinkedHashMap<>();
 *
 *     // or via method:
 *     @JsonAnySetter
 *     public void setExtra(String key, Object value) {
 *         extras.put(key, value);
 *     }
 * }
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Captures all unmatched JSON properties - 捕获所有未匹配的 JSON 属性</li>
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
 * @see JsonAnyGetter
 * @since JDK 25, opencode-base-json V1.0.0
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JsonAnySetter {

    /**
     * Whether this any-setter is enabled.
     * 此任意设置器是否启用。
     *
     * @return true if enabled - 如果启用则返回 true
     */
    boolean enabled() default true;
}
