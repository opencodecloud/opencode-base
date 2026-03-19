
package cloud.opencode.base.json.annotation;

import java.lang.annotation.*;

/**
 * JSON Property - Defines JSON Field Mapping
 * JSON 属性 - 定义 JSON 字段映射
 *
 * <p>This annotation maps a Java field to a JSON property name.
 * It can be used on fields, getters, or setters.</p>
 * <p>此注解将 Java 字段映射到 JSON 属性名。可用于字段、getter 或 setter 方法。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * public class User {
 *     @JsonProperty("user_name")
 *     private String userName;
 *
 *     @JsonProperty(value = "email", required = true)
 *     private String email;
 *
 *     @JsonProperty(access = Access.READ_ONLY)
 *     private LocalDateTime createdAt;
 * }
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Custom JSON property name mapping - 自定义JSON属性名映射</li>
 *   <li>Required field validation during deserialization - 反序列化时的必填字段验证</li>
 *   <li>Access control (read-only, write-only) - 访问控制（只读、只写）</li>
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
public @interface JsonProperty {

    /**
     * Access type for property
     * 属性的访问类型
     */
    enum Access {
        /**
         * Property is both serialized and deserialized.
         * 属性既序列化也反序列化。
         */
        AUTO,

        /**
         * Property is only serialized (read-only).
         * 属性仅序列化（只读）。
         */
        READ_ONLY,

        /**
         * Property is only deserialized (write-only).
         * 属性仅反序列化（只写）。
         */
        WRITE_ONLY
    }

    /**
     * The JSON property name.
     * JSON 属性名。
     *
     * @return the property name, or empty for default field name - 属性名，空则使用默认字段名
     */
    String value() default "";

    /**
     * Whether this property is required during deserialization.
     * 反序列化时此属性是否必需。
     *
     * @return true if required - 如果必需则返回 true
     */
    boolean required() default false;

    /**
     * The default value as a JSON literal if the property is absent.
     * 如果属性不存在时的默认值（JSON 字面量）。
     *
     * @return the default value - 默认值
     */
    String defaultValue() default "";

    /**
     * The access type for this property.
     * 此属性的访问类型。
     *
     * @return the access type - 访问类型
     */
    Access access() default Access.AUTO;

    /**
     * Index for property ordering during serialization.
     * 序列化时属性排序的索引。
     *
     * @return the index (lower = first), or -1 for default order - 索引（越小越靠前），-1 表示默认顺序
     */
    int index() default -1;
}
