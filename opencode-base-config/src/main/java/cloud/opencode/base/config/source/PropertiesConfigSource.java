package cloud.opencode.base.config.source;

import cloud.opencode.base.config.OpenConfigException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Properties File Configuration Source
 * Properties文件配置源
 *
 * <p>Loads configuration from .properties files, supporting both classpath and filesystem resources.</p>
 * <p>从.properties文件加载配置,支持类路径和文件系统资源。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Classpath and filesystem support - 支持类路径和文件系统</li>
 *   <li>Hot reload for file-based sources - 文件源支持热重载</li>
 *   <li>UTF-8 encoding support - UTF-8编码支持</li>
 *   <li>Last modified time tracking - 最后修改时间跟踪</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // From classpath
 * ConfigSource source = new PropertiesConfigSource("application.properties", true);
 *
 * // From filesystem
 * ConfigSource source = new PropertiesConfigSource(Path.of("/etc/app/config.properties"));
 *
 * // Hot reload
 * if (source.supportsReload()) {
 *     source.reload();
 * }
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) for loading - 时间复杂度: 加载为O(n)</li>
 *   <li>Properties cached in memory - 属性缓存在内存中</li>
 *   <li>Reload checks modification time - 重载检查修改时间</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Immutable properties map - 不可变属性映射</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
public class PropertiesConfigSource implements ConfigSource {

    private static final System.Logger LOG = System.getLogger(PropertiesConfigSource.class.getName());

    private final String name;
    private final Path filePath;
    private final boolean classpath;
    private volatile Map<String, String> properties;
    private volatile long lastModified;

    /**
     * Create properties source from classpath or filesystem
     * 从类路径或文件系统创建属性源
     *
     * @param resource resource path | 资源路径
     * @param classpath true for classpath, false for filesystem | 类路径为true,文件系统为false
     */
    public PropertiesConfigSource(String resource, boolean classpath) {
        this.name = resource;
        this.classpath = classpath;
        this.filePath = classpath ? null : Path.of(resource);
        load();
    }

    /**
     * Create properties source from filesystem path
     * 从文件系统路径创建属性源
     *
     * @param file file path | 文件路径
     */
    public PropertiesConfigSource(Path file) {
        this.name = file.toString();
        this.filePath = file;
        this.classpath = false;
        load();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public int getPriority() {
        return 50; // Default priority for properties files
    }

    @Override
    public boolean supportsReload() {
        return !classpath && filePath != null;
    }

    @Override
    public void reload() {
        if (supportsReload()) {
            try {
                long modified = Files.getLastModifiedTime(filePath).toMillis();
                if (modified > lastModified) {
                    load();
                    lastModified = modified;
                }
            } catch (IOException e) {
                LOG.log(System.Logger.Level.WARNING, "Failed to reload properties from " + filePath, e);
            }
        }
    }

    /**
     * Load properties from source
     * 从源加载属性
     */
    private void load() {
        Properties props = new Properties();
        try {
            if (classpath) {
                loadFromClasspath(props);
            } else {
                loadFromFile(props);
            }
        } catch (IOException e) {
            throw OpenConfigException.sourceLoadFailed(name, e);
        }

        this.properties = Map.copyOf(props.entrySet().stream()
            .collect(Collectors.toMap(
                e -> e.getKey().toString(),
                e -> e.getValue().toString()
            )));
    }

    /**
     * Load properties from classpath
     * 从类路径加载属性
     */
    private void loadFromClasspath(Properties props) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(name)) {
            if (is != null) {
                props.load(is);
            }
        }
    }

    /**
     * Load properties from file
     * 从文件加载属性
     */
    private void loadFromFile(Properties props) throws IOException {
        if (Files.exists(filePath)) {
            try (InputStream is = Files.newInputStream(filePath)) {
                props.load(is);
            }
            lastModified = Files.getLastModifiedTime(filePath).toMillis();
        }
    }
}
