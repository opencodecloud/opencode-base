
package cloud.opencode.base.json.annotation;

import java.lang.annotation.*;

/**
 * JSON Value - Marks a Method or Field as the Serialized Representation
 * JSON 值 - 标记方法或字段作为序列化表示
 *
 * <p>This annotation indicates that the return value of the annotated method (or the value
 * of the annotated field) should be used as the JSON representation of the object.
 * Only one {@code @JsonValue} annotation is allowed per class.</p>
 * <p>此注解表示被标注方法的返回值（或被标注字段的值）应作为该对象的 JSON 表示。
 * 每个类中只允许使用一个 {@code @JsonValue} 注解。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * public enum Status {
 *     ACTIVE("active"),
 *     INACTIVE("inactive");
 *
 *     private final String code;
 *
 *     Status(String code) {
 *         this.code = code;
 *     }
 *
 *     @JsonValue
 *     public String getCode() {
 *         return code;
 *     }
 * }
 *
 * public class Wrapper {
 *     @JsonValue
 *     private final String rawValue;
 *
 *     public Wrapper(String rawValue) {
 *         this.rawValue = rawValue;
 *     }
 * }
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Custom serialization form for any class - 为任意类自定义序列化形式</li>
 *   <li>Commonly used with enums for custom JSON values - 常用于枚举的自定义 JSON 值</li>
 *   <li>Supports both methods and fields - 支持方法和字段</li>
 *   <li>Can be disabled via {@code value = false} - 可通过 {@code value = false} 禁用</li>
 * </ul>
 *
 * <p><strong>Constraints | 约束:</strong></p>
 * <ul>
 *   <li>Only one {@code @JsonValue} annotation per class - 每个类只能有一个 {@code @JsonValue} 注解</li>
 *   <li>The method return value becomes the entire JSON representation - 方法返回值成为整个 JSON 表示</li>
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
public @interface JsonValue {

    /**
     * Whether this annotation is active.
     * 此注解是否生效。
     *
     * <p>Set to {@code false} to disable a {@code @JsonValue} inherited from a superclass.</p>
     * <p>设为 {@code false} 可禁用从父类继承的 {@code @JsonValue}。</p>
     *
     * @return true if enabled, false if disabled - 启用返回 true，禁用返回 false
     */
    boolean value() default true;
}
