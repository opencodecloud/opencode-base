/**
 * Configuration Sources Package
 * 配置源包
 *
 * <p>This package provides various configuration source implementations for loading
 * configuration from different locations and formats.</p>
 * <p>此包提供各种配置源实现，用于从不同位置和格式加载配置。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Multiple source types - 多种配置源类型</li>
 *   <li>Priority-based merging - 基于优先级的合并</li>
 *   <li>Hot reload support - 热更新支持</li>
 *   <li>SPI extensibility - SPI扩展性</li>
 * </ul>
 *
 * <p><strong>Built-in Sources | 内置配置源:</strong></p>
 * <ul>
 *   <li>{@link cloud.opencode.base.config.source.PropertiesConfigSource} - Properties file source (priority: 50) - Properties文件源</li>
 *   <li>{@link cloud.opencode.base.config.source.YamlConfigSource} - YAML file source (priority: 50) - YAML文件源</li>
 *   <li>{@link cloud.opencode.base.config.source.EnvironmentConfigSource} - Environment variables (priority: 100) - 环境变量源</li>
 *   <li>{@link cloud.opencode.base.config.source.SystemPropertiesConfigSource} - System properties (priority: 50) - 系统属性源</li>
 *   <li>{@link cloud.opencode.base.config.source.CommandLineConfigSource} - Command line arguments (priority: 200) - 命令行参数源</li>
 *   <li>{@link cloud.opencode.base.config.source.InMemoryConfigSource} - In-memory configuration (priority: 10) - 内存配置源</li>
 *   <li>{@link cloud.opencode.base.config.source.CompositeConfigSource} - Composite source with priority merging - 组合源(优先级合并)</li>
 * </ul>
 *
 * <p><strong>Priority Order (High to Low) | 优先级顺序(高到低):</strong></p>
 * <pre>
 * Command Line (200) → Environment (100) → System Properties (50) →
 * Properties/YAML (50) → InMemory (10)
 * </pre>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Properties source from classpath
 * ConfigSource props = new PropertiesConfigSource("application.properties", true);
 *
 * // Properties source from file system
 * ConfigSource fileProps = new PropertiesConfigSource(Path.of("/etc/app/config.properties"));
 *
 * // Environment variables with prefix filtering
 * ConfigSource env = new EnvironmentConfigSource("APP_");
 *
 * // Command line arguments
 * ConfigSource cmdLine = new CommandLineConfigSource(args);
 *
 * // Composite source
 * ConfigSource composite = new CompositeConfigSource(List.of(props, env, cmdLine));
 * }</pre>
 *
 * <p><strong>Implementing Custom Sources | 实现自定义配置源:</strong></p>
 * <pre>{@code
 * public class RedisConfigSource implements ConfigSource {
 *     @Override
 *     public String getName() { return "redis"; }
 *
 *     @Override
 *     public Map<String, String> getProperties() {
 *         // Load from Redis
 *     }
 *
 *     @Override
 *     public int getPriority() { return 80; }
 *
 *     @Override
 *     public boolean supportsReload() { return true; }
 * }
 * }</pre>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
package cloud.opencode.base.config.source;
