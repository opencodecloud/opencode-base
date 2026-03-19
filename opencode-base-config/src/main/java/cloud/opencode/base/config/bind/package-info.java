/**
 * Configuration Binding Package
 * 配置绑定包
 *
 * <p>This package provides configuration binding capabilities for mapping
 * configuration values to POJOs and Records.</p>
 * <p>此包提供配置绑定能力，用于将配置值映射到POJO和Record。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>POJO binding with reflection - 反射POJO绑定</li>
 *   <li>JDK 25 Record binding - JDK 25 Record绑定</li>
 *   <li>Nested configuration support - 嵌套配置支持</li>
 *   <li>Type-safe property mapping - 类型安全的属性映射</li>
 *   <li>Annotation-based configuration - 基于注解的配置</li>
 * </ul>
 *
 * <p><strong>Annotations | 注解:</strong></p>
 * <ul>
 *   <li>{@link cloud.opencode.base.config.bind.ConfigProperties} - Mark class with configuration prefix - 标记类的配置前缀</li>
 *   <li>{@link cloud.opencode.base.config.bind.NestedConfig} - Mark nested configuration field - 标记嵌套配置字段</li>
 * </ul>
 *
 * <p><strong>POJO Binding Example | POJO绑定示例:</strong></p>
 * <pre>{@code
 * @ConfigProperties(prefix = "database")
 * public class DatabaseConfig {
 *     private String url;
 *     private String username;
 *     private String password;
 *     private int maxPoolSize = 10;
 *     private Duration connectionTimeout = Duration.ofSeconds(30);
 *
 *     @NestedConfig
 *     private PoolConfig pool;
 *     // getters and setters...
 * }
 *
 * DatabaseConfig config = OpenConfig.bind("database", DatabaseConfig.class);
 * }</pre>
 *
 * <p><strong>Record Binding Example | Record绑定示例:</strong></p>
 * <pre>{@code
 * record DatabaseConfig(
 *     @Required String url,
 *     @Required String username,
 *     @Required String password,
 *     @DefaultValue("10") int maxPoolSize,
 *     @DefaultValue("30s") Duration connectionTimeout,
 *     PoolConfig pool
 * ) {}
 *
 * DatabaseConfig config = OpenConfig.bind("database", DatabaseConfig.class);
 * }</pre>
 *
 * <p><strong>Property Name Conversion | 属性名转换:</strong></p>
 * <pre>
 * Field: maxPoolSize  →  Config key: max-pool-size (kebab-case)
 * Field: connectionTimeout  →  Config key: connection-timeout
 * </pre>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
package cloud.opencode.base.config.bind;
