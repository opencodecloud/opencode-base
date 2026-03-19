/**
 * Advanced Features and SPI Extension Package
 * 高级功能和SPI扩展包
 *
 * <p>This package provides advanced configuration features and SPI extension
 * points for custom implementations.</p>
 * <p>此包提供高级配置功能和用于自定义实现的SPI扩展点。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Configuration source SPI - 配置源SPI</li>
 *   <li>Type converter SPI - 类型转换器SPI</li>
 *   <li>Multi-profile configuration - 多环境配置</li>
 *   <li>Multi-tenant configuration - 多租户配置</li>
 *   <li>Encrypted configuration - 加密配置</li>
 *   <li>Remote configuration sources - 远程配置源</li>
 * </ul>
 *
 * <p><strong>SPI Interfaces | SPI接口:</strong></p>
 * <ul>
 *   <li>{@link cloud.opencode.base.config.advanced.ConfigSourceProvider} - Custom source provider - 自定义配置源提供者</li>
 *   <li>{@link cloud.opencode.base.config.advanced.ConfigConverterProvider} - Custom converter provider - 自定义转换器提供者</li>
 *   <li>{@link cloud.opencode.base.config.advanced.ConfigSourceFactory} - Source factory with URI support - 支持URI的配置源工厂</li>
 * </ul>
 *
 * <p><strong>Advanced Components | 高级组件:</strong></p>
 * <ul>
 *   <li>{@link cloud.opencode.base.config.advanced.MultiProfileConfig} - Multi-environment loading - 多环境加载</li>
 *   <li>{@link cloud.opencode.base.config.advanced.TenantConfigManager} - Multi-tenant management - 多租户管理</li>
 *   <li>{@link cloud.opencode.base.config.advanced.EncryptedConfigProcessor} - Encrypted value handling - 加密值处理</li>
 *   <li>{@link cloud.opencode.base.config.advanced.HttpConfigSourceProvider} - HTTP source provider - HTTP配置源提供者</li>
 * </ul>
 *
 * <p><strong>URI Formats | URI格式:</strong></p>
 * <pre>
 * file:/path/to/config.properties   - File system
 * classpath:application.properties  - Classpath
 * env:APP_                          - Environment variables with prefix
 * http://config-server/config       - Remote HTTP (via SPI)
 * consul://localhost:8500/myapp     - Consul (via SPI)
 * </pre>
 *
 * <p><strong>SPI Registration Example | SPI注册示例:</strong></p>
 * <pre>
 * # META-INF/services/cloud.opencode.base.config.advanced.ConfigSourceProvider
 * com.example.ConsulConfigSourceProvider
 * com.example.RedisConfigSourceProvider
 * </pre>
 *
 * <p><strong>Multi-Profile Example | 多环境示例:</strong></p>
 * <pre>{@code
 * Config config = MultiProfileConfig.load(args);
 * // Loads: application.properties → application-{profile}.properties
 * // Profile from: APP_PROFILE env var or app.profile system property
 * }</pre>
 *
 * <p><strong>Encrypted Config Example | 加密配置示例:</strong></p>
 * <pre>{@code
 * // application.properties
 * database.password=ENC(base64EncodedEncryptedValue)
 *
 * // Usage
 * SecretKey key = loadSecretKey();
 * Config config = EncryptedConfigProcessor.createEncryptedConfig(
 *     OpenConfig.getGlobal(), key);
 * String password = config.getString("database.password"); // Auto-decrypted
 * }</pre>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
package cloud.opencode.base.config.advanced;
