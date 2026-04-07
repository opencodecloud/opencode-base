
package cloud.opencode.base.json.annotation;

import java.lang.annotation.*;

/**
 * JSON Raw Value - Includes String Value as Raw JSON
 * JSON 原始值 - 将字符串值作为原始 JSON 包含
 *
 * <p>This annotation marks a field or method whose {@link String} value should
 * be included verbatim as raw JSON output, without quoting or escaping.
 * This is useful when a field already contains pre-serialized JSON.</p>
 * <p>此注解标记一个字段或方法，其 {@link String} 值将按原样作为原始 JSON 输出，
 * 不进行引号包裹或转义。当字段已包含预序列化的 JSON 时非常有用。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * public class Event {
 *     private String name;
 *
 *     @JsonRawValue
 *     private String payload = "{\"key\":\"value\"}";
 * }
 * // Serializes as: {"name":"click","payload":{"key":"value"}}
 * // NOT:           {"name":"click","payload":"{\"key\":\"value\"}"}
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Embeds pre-serialized JSON without extra quoting - 嵌入预序列化的 JSON，不添加额外引号</li>
 *   <li>Can be disabled via {@code value = false} - 可通过 {@code value = false} 禁用</li>
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
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JsonRawValue {

    /**
     * Whether the raw value behavior is enabled.
     * 是否启用原始值行为。
     *
     * @return true if raw value output is enabled - 如果启用原始值输出则返回 true
     */
    boolean value() default true;
}
