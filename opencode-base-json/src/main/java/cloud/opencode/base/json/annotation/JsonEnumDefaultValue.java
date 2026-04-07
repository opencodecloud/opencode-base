
package cloud.opencode.base.json.annotation;

import java.lang.annotation.*;

/**
 * JSON Enum Default Value - Marks Default Enum Constant for Unknown Values
 * JSON 枚举默认值 - 标记未知值时的默认枚举常量
 *
 * <p>This marker annotation designates an enum constant as the fallback
 * value when an unrecognized string is encountered during deserialization.
 * At most one constant per enum type should carry this annotation.</p>
 * <p>此标记注解将一个枚举常量指定为在反序列化过程中遇到无法识别的字符串时的
 * 回退值。每个枚举类型最多应有一个常量携带此注解。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * public enum Color {
 *     RED,
 *     GREEN,
 *     BLUE,
 *
 *     @JsonEnumDefaultValue
 *     UNKNOWN
 * }
 * // Deserializing "PURPLE" will yield Color.UNKNOWN instead of an error
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Graceful handling of unknown enum values - 优雅处理未知枚举值</li>
 *   <li>Marker annotation with no attributes - 无属性的标记注解</li>
 *   <li>Applied to enum constants (fields) - 应用于枚举常量（字段）</li>
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
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JsonEnumDefaultValue {
}
