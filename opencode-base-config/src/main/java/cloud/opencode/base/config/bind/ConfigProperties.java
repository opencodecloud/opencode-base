package cloud.opencode.base.config.bind;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Configuration Properties Annotation
 * 配置属性注解
 *
 * <p>Marks a class as a configuration properties holder with a specific prefix.</p>
 * <p>将类标记为具有特定前缀的配置属性持有者。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Prefix-based configuration binding - 基于前缀的配置绑定</li>
 *   <li>Type-safe configuration POJOs - 类型安全的配置POJO</li>
 *   <li>Supports nested configuration - 支持嵌套配置</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * @ConfigProperties(prefix = "database")
 * public class DatabaseConfig {
 *     private String url;           // database.url
 *     private String username;      // database.username
 *     private int maxPoolSize;      // database.max-pool-size
 *
 *     @NestedConfig
 *     private PoolConfig pool;      // database.pool.*
 *
 *     // getters and setters
 * }
 *
 * // Binding
 * DatabaseConfig config = OpenConfig.bind("database", DatabaseConfig.class);
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
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigProperties {

    /**
     * Configuration prefix
     * 配置前缀
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * prefix = "database"        -> database.url, database.username
     * prefix = "app.server"      -> app.server.port, app.server.host
     * prefix = ""                -> root level properties
     * </pre>
     *
     * @return configuration prefix | 配置前缀
     */
    String prefix();
}
