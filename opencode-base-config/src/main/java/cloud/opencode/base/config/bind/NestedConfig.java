package cloud.opencode.base.config.bind;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Nested Configuration Annotation
 * 嵌套配置注解
 *
 * <p>Marks a field as a nested configuration object.</p>
 * <p>将字段标记为嵌套配置对象。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Nested configuration binding - 嵌套配置绑定</li>
 *   <li>Optional custom prefix - 可选的自定义前缀</li>
 *   <li>Recursive binding support - 递归绑定支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * @ConfigProperties(prefix = "application")
 * public class AppConfig {
 *     private String name;
 *
 *     @NestedConfig  // Uses field name as sub-prefix
 *     private ServerConfig server;  // application.server.*
 *
 *     @NestedConfig(prefix = "db")  // Custom prefix
 *     private DatabaseConfig database;  // application.db.*
 * }
 *
 * public class ServerConfig {
 *     private int port;      // application.server.port
 *     private String host;   // application.server.host
 * }
 * }</pre>
 *
 *
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: N/A - 空值安全: 不适用</li>
 * </ul>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NestedConfig {

    /**
     * Custom prefix for nested configuration
     * 嵌套配置的自定义前缀
     *
     * <p>If empty, uses the field name as the sub-prefix.</p>
     * <p>如果为空,使用字段名作为子前缀。</p>
     *
     * @return nested configuration prefix | 嵌套配置前缀
     */
    String prefix() default "";
}
