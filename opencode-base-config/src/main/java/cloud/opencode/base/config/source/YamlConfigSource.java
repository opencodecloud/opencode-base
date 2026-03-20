package cloud.opencode.base.config.source;

import cloud.opencode.base.config.OpenConfigException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * YAML Configuration Source
 * YAML配置源
 *
 * <p>Loads configuration from .yml/.yaml files when opencode-base-yml module is available.
 * Uses reflection to detect yml module presence, allowing graceful degradation.</p>
 * <p>当opencode-base-yml模块可用时从.yml/.yaml文件加载配置。
 * 使用反射检测yml模块存在性，允许优雅降级。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Optional yml module dependency - yml模块可选依赖</li>
 *   <li>Classpath and filesystem support - 支持类路径和文件系统</li>
 *   <li>Hot reload for file-based sources - 文件源支持热重载</li>
 *   <li>Nested YAML flattening to dot notation - 嵌套YAML扁平化为点号记法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Check if YAML is supported
 * if (YamlConfigSource.isYamlSupported()) {
 *     ConfigSource source = new YamlConfigSource("application.yml", true);
 * }
 *
 * // From filesystem
 * ConfigSource source = new YamlConfigSource(Path.of("/etc/app/config.yml"));
 * }</pre>
 *
 * <p><strong>YAML Flattening Example | YAML扁平化示例:</strong></p>
 * <pre>{@code
 * # Input YAML:
 * server:
 *   port: 8080
 *   host: localhost
 * database:
 *   url: jdbc:mysql://localhost/db
 *
 * # Flattened to:
 * server.port=8080
 * server.host=localhost
 * database.url=jdbc:mysql://localhost/db
 * }</pre>
 *
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
public class YamlConfigSource implements ConfigSource {

    private static final System.Logger logger = System.getLogger(YamlConfigSource.class.getName());

    private static final String OPEN_YML_CLASS = "cloud.opencode.base.yml.OpenYml";
    private static final String YML_DOCUMENT_CLASS = "cloud.opencode.base.yml.YmlDocument";

    private static final boolean YAML_AVAILABLE;
    private static final Method LOAD_FILE_METHOD;
    private static final Method LOAD_STREAM_METHOD;
    private static final Method AS_MAP_METHOD;

    static {
        boolean available = false;
        Method loadFile = null;
        Method loadStream = null;
        Method asMap = null;

        try {
            Class<?> openYmlClass = Class.forName(OPEN_YML_CLASS);
            Class<?> ymlDocClass = Class.forName(YML_DOCUMENT_CLASS);

            loadFile = openYmlClass.getMethod("loadFile", Path.class);
            loadStream = openYmlClass.getMethod("loadStream", InputStream.class);
            asMap = ymlDocClass.getMethod("asMap");

            available = true;
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            // yml module not available
        }

        YAML_AVAILABLE = available;
        LOAD_FILE_METHOD = loadFile;
        LOAD_STREAM_METHOD = loadStream;
        AS_MAP_METHOD = asMap;
    }

    private final String name;
    private final Path filePath;
    private final boolean classpath;
    private volatile Map<String, String> properties;
    private volatile long lastModified;

    /**
     * Check if YAML support is available
     * 检查YAML支持是否可用
     *
     * @return true if opencode-base-yml module is present | yml模块存在返回true
     */
    public static boolean isYamlSupported() {
        return YAML_AVAILABLE;
    }

    /**
     * Create YAML source from classpath or filesystem
     * 从类路径或文件系统创建YAML源
     *
     * @param resource resource path | 资源路径
     * @param classpath true for classpath, false for filesystem | 类路径为true,文件系统为false
     * @throws OpenConfigException if YAML module not available | YAML模块不可用时抛出异常
     */
    public YamlConfigSource(String resource, boolean classpath) {
        checkYamlAvailable();
        this.name = resource;
        this.classpath = classpath;
        this.filePath = classpath ? null : Path.of(resource);
        load();
    }

    /**
     * Create YAML source from filesystem path
     * 从文件系统路径创建YAML源
     *
     * @param file file path | 文件路径
     * @throws OpenConfigException if YAML module not available | YAML模块不可用时抛出异常
     */
    public YamlConfigSource(Path file) {
        checkYamlAvailable();
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
        return 50; // Same as properties files
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
                logger.log(System.Logger.Level.ERROR, "Failed to reload YAML config source: " + name, e);
            }
        }
    }

    /**
     * Check if YAML module is available
     * 检查YAML模块是否可用
     */
    private void checkYamlAvailable() {
        if (!YAML_AVAILABLE) {
            throw OpenConfigException.sourceLoadFailed(name,
                    new UnsupportedOperationException(
                            "YAML support requires opencode-base-yml module. " +
                            "Add opencode-base-yml dependency to enable YAML configuration files."));
        }
    }

    /**
     * Load YAML from source
     * 从源加载YAML
     */
    private void load() {
        try {
            Map<String, Object> yamlData;
            if (classpath) {
                yamlData = loadFromClasspath();
            } else {
                yamlData = loadFromFile();
            }

            // Flatten nested map to dot notation
            Map<String, String> flattened = new HashMap<>();
            flatten("", yamlData, flattened, 0);
            this.properties = Map.copyOf(flattened);

        } catch (Exception e) {
            throw OpenConfigException.sourceLoadFailed(name, e);
        }
    }

    /**
     * Load YAML from classpath
     * 从类路径加载YAML
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> loadFromClasspath() throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(name)) {
            if (is == null) {
                return Map.of();
            }
            Object doc = LOAD_STREAM_METHOD.invoke(null, is);
            return (Map<String, Object>) AS_MAP_METHOD.invoke(doc);
        }
    }

    /**
     * Load YAML from file
     * 从文件加载YAML
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> loadFromFile() throws Exception {
        if (!Files.exists(filePath)) {
            return Map.of();
        }
        Object doc = LOAD_FILE_METHOD.invoke(null, filePath);
        lastModified = Files.getLastModifiedTime(filePath).toMillis();
        return (Map<String, Object>) AS_MAP_METHOD.invoke(doc);
    }

    private static final int MAX_FLATTEN_DEPTH = 64;

    /**
     * Flatten nested map to dot notation
     * 将嵌套Map扁平化为点号记法
     *
     * @param prefix current key prefix | 当前键前缀
     * @param source source map | 源Map
     * @param target target flattened map | 目标扁平化Map
     * @param depth  current recursion depth | 当前递归深度
     */
    @SuppressWarnings("unchecked")
    private void flatten(String prefix, Map<String, Object> source, Map<String, String> target, int depth) {
        if (source == null) {
            return;
        }
        if (depth > MAX_FLATTEN_DEPTH) {
            throw new OpenConfigException("YAML nesting depth exceeds maximum of " + MAX_FLATTEN_DEPTH
                    + " at prefix: " + prefix);
        }

        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Map) {
                // Nested map - recurse
                flatten(key, (Map<String, Object>) value, target, depth + 1);
            } else if (value instanceof List<?> list) {
                // List - flatten with index notation
                for (int i = 0; i < list.size(); i++) {
                    Object item = list.get(i);
                    String indexedKey = key + "[" + i + "]";
                    if (item instanceof Map) {
                        flatten(indexedKey, (Map<String, Object>) item, target, depth + 1);
                    } else if (item != null) {
                        target.put(indexedKey, item.toString());
                    }
                }
                // Also store as comma-separated for simple lists
                if (!list.isEmpty() && !(list.getFirst() instanceof Map)) {
                    target.put(key, listToString(list));
                }
            } else if (value != null) {
                // Scalar value
                target.put(key, value.toString());
            }
        }
    }

    /**
     * Convert list to comma-separated string
     * 将列表转换为逗号分隔字符串
     */
    private String listToString(List<?> list) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            Object item = list.get(i);
            if (item != null) {
                sb.append(item);
            }
        }
        return sb.toString();
    }
}
