package cloud.opencode.base.config.jdk25;

import cloud.opencode.base.config.source.*;
import java.nio.file.Path;
import java.time.Duration;

/**
 * Sealed Configuration Source Type for JDK 25
 * JDK 25的密封配置源类型
 *
 * <p>Provides type-safe configuration source definitions using JDK 25 sealed
 * types and record patterns. Enables exhaustive pattern matching for source handling.</p>
 * <p>使用JDK 25密封类型和记录模式提供类型安全的配置源定义。支持完备的模式匹配处理。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Sealed interface with record implementations - 密封接口与记录实现</li>
 *   <li>Pattern matching support - 模式匹配支持</li>
 *   <li>Type-safe source creation - 类型安全的源创建</li>
 *   <li>Exhaustive switch expressions - 完备的switch表达式</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create different source types
 * ConfigSourceType fileSource = new ConfigSourceType.File(Path.of("config.properties"));
 * ConfigSourceType envSource = new ConfigSourceType.Environment("APP");
 * ConfigSourceType sysSource = new ConfigSourceType.System();
 *
 * // Convert to ConfigSource
 * ConfigSource source = fileSource.toSource();
 *
 * // Pattern matching in switch
 * String desc = switch (sourceType) {
 *     case File(var path, var watch) -> "File: " + path;
 *     case Classpath(var res) -> "Classpath: " + res;
 *     case Environment(var prefix) -> "Env: " + prefix;
 *     case System() -> "System properties";
 *     case InMemory(var props) -> "Memory: " + props.size();
 * };
 * }</pre>
 *
 * <p><strong>Supported Types | 支持的类型:</strong></p>
 * <ul>
 *   <li>{@code File} - File system properties - 文件系统属性</li>
 *   <li>{@code Classpath} - Classpath resources - 类路径资源</li>
 *   <li>{@code Environment} - Environment variables - 环境变量</li>
 *   <li>{@code System} - System properties - 系统属性</li>
 *   <li>{@code InMemory} - In-memory map - 内存映射</li>
 * </ul>
 *
 *
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: Partial (validates inputs) - 空值安全: 部分（验证输入）</li>
 * </ul>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
public sealed interface ConfigSourceType permits
    ConfigSourceType.File,
    ConfigSourceType.Classpath,
    ConfigSourceType.Environment,
    ConfigSourceType.System,
    ConfigSourceType.InMemory {

    ConfigSource toSource();

    record File(Path path, boolean watchable) implements ConfigSourceType {
        public File(Path path) {
            this(path, true);
        }

        @Override
        public ConfigSource toSource() {
            return new PropertiesConfigSource(path);
        }
    }

    record Classpath(String resource) implements ConfigSourceType {
        @Override
        public ConfigSource toSource() {
            return new PropertiesConfigSource(resource, true);
        }
    }

    record Environment(String prefix) implements ConfigSourceType {
        public Environment() {
            this(null);
        }

        @Override
        public ConfigSource toSource() {
            return new EnvironmentConfigSource(prefix);
        }
    }

    record System() implements ConfigSourceType {
        @Override
        public ConfigSource toSource() {
            return new SystemPropertiesConfigSource();
        }
    }

    record InMemory(java.util.Map<String, String> properties) implements ConfigSourceType {
        @Override
        public ConfigSource toSource() {
            return new InMemoryConfigSource(properties);
        }
    }
}
