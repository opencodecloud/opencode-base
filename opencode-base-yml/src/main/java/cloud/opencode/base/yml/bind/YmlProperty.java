package cloud.opencode.base.yml.bind;

import java.lang.annotation.*;

/**
 * YML Property - Binds a field to a YAML property
 * YML 属性 - 将字段绑定到 YAML 属性
 *
 * <p>This annotation maps a Java field to a YAML property path.</p>
 * <p>此注解将 Java 字段映射到 YAML 属性路径。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Map fields to specific YAML property paths - 将字段映射到特定 YAML 属性路径</li>
 *   <li>Default value support for missing properties - 缺失属性的默认值支持</li>
 *   <li>Required property validation - 必需属性验证</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * public class ServerConfig {
 *     @YmlProperty("server.port")
 *     private int port;
 *
 *     @YmlProperty(value = "server.host", defaultValue = "localhost")
 *     private String host;
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (annotation is inherently immutable) - 线程安全: 是（注解本身不可变）</li>
 *   <li>Null-safe: N/A - 空值安全: 不适用</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface YmlProperty {

    /**
     * The property path (e.g., "server.port").
     * 属性路径（如 "server.port"）。
     *
     * @return the property path | 属性路径
     */
    String value();

    /**
     * Default value if property is not found.
     * 如果未找到属性时的默认值。
     *
     * @return the default value | 默认值
     */
    String defaultValue() default "";

    /**
     * Whether the property is required.
     * 属性是否必需。
     *
     * @return true if required | 如果必需则返回 true
     */
    boolean required() default false;
}
