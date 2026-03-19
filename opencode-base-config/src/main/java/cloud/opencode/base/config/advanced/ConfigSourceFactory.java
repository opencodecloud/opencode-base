package cloud.opencode.base.config.advanced;

import cloud.opencode.base.config.OpenConfigException;
import cloud.opencode.base.config.source.*;
import java.nio.file.Path;
import java.util.*;

/**
 * Configuration Source Factory
 * 配置源工厂
 *
 * <p>Factory for creating configuration sources from URI patterns.
 * Supports SPI extension for custom source types.</p>
 * <p>从URI模式创建配置源的工厂。支持SPI扩展自定义源类型。</p>
 *
 * <p><strong>Built-in URI Patterns | 内置URI模式:</strong></p>
 * <ul>
 *   <li>file:/path/to/config.properties - File system</li>
 *   <li>classpath:application.properties - Classpath resource</li>
 *   <li>env:APP_ - Environment variables with prefix</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ConfigSource fileSource = ConfigSourceFactory.create("file:/etc/app/config.properties");
 * ConfigSource cpSource = ConfigSourceFactory.create("classpath:application.properties");
 * ConfigSource envSource = ConfigSourceFactory.create("env:APP_");
 * }</pre>
 *
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Core ConfigSourceFactory functionality - ConfigSourceFactory核心功能</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
public final class ConfigSourceFactory {

    private static final List<ConfigSourceProvider> providers;

    static {
        providers = ServiceLoader.load(ConfigSourceProvider.class)
            .stream()
            .map(ServiceLoader.Provider::get)
            .sorted(Comparator.comparingInt(ConfigSourceProvider::priority).reversed())
            .toList();
    }

    public static ConfigSource create(String uri) {
        return create(uri, Map.of());
    }

    public static ConfigSource create(String uri, Map<String, Object> options) {
        for (ConfigSourceProvider provider : providers) {
            if (provider.supports(uri)) {
                return provider.create(uri, options);
            }
        }

        // Built-in support
        if (uri.startsWith("file:")) {
            return new PropertiesConfigSource(Path.of(uri.substring(5)));
        }
        if (uri.startsWith("classpath:")) {
            return new PropertiesConfigSource(uri.substring(10), true);
        }
        if (uri.startsWith("env:")) {
            return new EnvironmentConfigSource(uri.substring(4));
        }

        throw OpenConfigException.sourceNotSupported(uri);
    }
}
