package cloud.opencode.base.config.advanced;

import cloud.opencode.base.config.Config;
import cloud.opencode.base.config.OpenConfig;
import java.nio.file.Path;

/**
 * Multi-Profile Configuration Loader
 * 多环境配置加载器
 *
 * <p>Loads configuration with environment-specific overrides based on active profile.</p>
 * <p>根据激活的环境加载带有环境特定覆盖的配置。</p>
 *
 * <p><strong>Profile Sources | 环境来源:</strong></p>
 * <ol>
 *   <li>APP_PROFILE environment variable | APP_PROFILE环境变量</li>
 *   <li>app.profile system property | app.profile系统属性</li>
 *   <li>Default: "default"</li>
 * </ol>
 *
 * <p><strong>Load Order (Low to High Priority) | 加载顺序(优先级从低到高):</strong></p>
 * <pre>
 * application.properties → application-{profile}.properties →
 * /etc/myapp/application.properties → system properties → env vars → command line
 * </pre>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Load with profile detection
 * Config config = MultiProfileConfig.load(args);
 *
 * // Profile-specific files:
 * // - application-dev.properties
 * // - application-prod.properties
 * // - application-staging.properties
 * }</pre>
 *
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Core MultiProfileConfig functionality - MultiProfileConfig核心功能</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Partial (validates inputs) - 空值安全: 部分（验证输入）</li>
 * </ul>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
public class MultiProfileConfig {

    public static Config load(String[] args) {
        String profile = System.getenv("APP_PROFILE");
        if (profile == null) {
            profile = System.getProperty("app.profile", "default");
        }

        return OpenConfig.builder()
            .addClasspathResource("application.properties")
            .addClasspathResource("application-" + profile + ".properties")
            .addFile(Path.of("/etc/myapp/application.properties"))
            .addSystemProperties()
            .addEnvironmentVariables("APP")
            .addCommandLineArgs(args)
            .build();
    }
}
