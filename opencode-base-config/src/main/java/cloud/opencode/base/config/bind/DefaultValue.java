package cloud.opencode.base.config.bind;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Default Value Annotation
 * 默认值注解
 *
 * <p>Specifies a default value for a configuration field or record component when
 * the corresponding configuration key is not present.</p>
 * <p>当对应的配置键不存在时,为配置字段或Record组件指定默认值。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Default values for missing configuration - 缺失配置的默认值</li>
 *   <li>Supports POJO fields and record components - 支持POJO字段和Record组件</li>
 *   <li>Automatic type conversion via ConverterRegistry - 通过ConverterRegistry自动类型转换</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // On POJO fields
 * public class ServerConfig {
 *     @DefaultValue("localhost")
 *     private String host;
 *
 *     @DefaultValue("8080")
 *     private int port;
 *
 *     @DefaultValue("true")
 *     private boolean enabled;
 *
 *     @DefaultValue("30s")
 *     private Duration timeout;
 * }
 *
 * // On record components
 * @ConfigProperties(prefix = "server")
 * public record ServerConfig(
 *     @DefaultValue("localhost") String host,
 *     @DefaultValue("8080") int port
 * ) {}
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: N/A - 空值安全: 不适用</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.3
 */
@Target({ElementType.RECORD_COMPONENT, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DefaultValue {

    /**
     * The default value as a string, to be converted to the field's type
     * 作为字符串的默认值,将被转换为字段的类型
     *
     * @return default value string | 默认值字符串
     */
    String value();
}
