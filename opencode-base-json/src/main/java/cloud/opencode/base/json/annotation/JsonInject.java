
package cloud.opencode.base.json.annotation;

import java.lang.annotation.*;

/**
 * JSON Inject - Injects Non-JSON Values During Deserialization
 * JSON 注入 - 反序列化时注入非 JSON 值
 *
 * <p>This annotation marks a field or parameter whose value should be injected
 * from an external source (not from the JSON input) during deserialization.
 * This is useful for injecting contextual data such as request metadata,
 * configuration values, or database connections.</p>
 * <p>此注解标记一个字段或参数，其值应在反序列化期间从外部来源（而非 JSON 输入）注入。
 * 适用于注入上下文数据，如请求元数据、配置值或数据库连接。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * public class AuditEntry {
 *     private String action;
 *
 *     @JsonInject("currentUser")
 *     private String auditor;
 *
 *     @JsonInject(value = "timestamp", useInput = false)
 *     private long createdAt;
 * }
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Inject external values during deserialization - 反序列化时注入外部值</li>
 *   <li>Optional injection id for named lookups - 可选的注入 ID 用于命名查找</li>
 *   <li>Control whether JSON input can override injected value - 控制 JSON 输入是否可覆盖注入值</li>
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
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JsonInject {

    /**
     * The injection identifier used to look up the value.
     * 用于查找值的注入标识符。
     *
     * @return the injection id, or empty for type-based resolution - 注入 ID，空则基于类型解析
     */
    String value() default "";

    /**
     * Whether JSON input can override the injected value.
     * JSON 输入是否可以覆盖注入的值。
     *
     * <p>If {@code true}, the JSON value takes precedence when present;
     * if {@code false}, the injected value is always used.</p>
     * <p>如果为 {@code true}，当 JSON 值存在时优先使用；
     * 如果为 {@code false}，始终使用注入的值。</p>
     *
     * @return true if JSON input can override - 如果 JSON 输入可覆盖则返回 true
     */
    boolean useInput() default true;
}
