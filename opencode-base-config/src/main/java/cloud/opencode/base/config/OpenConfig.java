package cloud.opencode.base.config;

import cloud.opencode.base.config.source.*;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Global Configuration Manager Facade
 * 全局配置管理器门面
 *
 * <p>This class provides a unified entry point for configuration management with support for
 * multiple sources, type conversion, hot reloading, and more.</p>
 * <p>此类提供配置管理的统一入口,支持多配置源、类型转换、热重载等功能。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Global configuration singleton - 全局配置单例</li>
 *   <li>Fluent builder API - 流式构建器API</li>
 *   <li>Static delegate methods - 静态委托方法</li>
 *   <li>Multiple configuration sources - 多配置源支持</li>
 *   <li>Hot reload capability - 热重载能力</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Use default configuration
 * String dbUrl = OpenConfig.getString("database.url");
 * int port = OpenConfig.getInt("server.port", 8080);
 *
 * // Build custom configuration
 * Config config = OpenConfig.builder()
 *     .addClasspathResource("application.properties")
 *     .addEnvironmentVariables()
 *     .addSystemProperties()
 *     .enableHotReload()
 *     .build();
 *
 * OpenConfig.setGlobal(config);
 *
 * // Quick loading
 * Config fileConfig = OpenConfig.loadFromFile(Path.of("config.properties"));
 * Config classpathConfig = OpenConfig.loadFromClasspath("app.properties");
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) for singleton access - 时间复杂度: 单例访问为O(1)</li>
 *   <li>Lazy initialization with double-check locking - 双重检查锁的懒加载</li>
 *   <li>Thread-safe global configuration - 线程安全的全局配置</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Immutable facade - 不可变门面</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
public final class OpenConfig {

    private static volatile Config globalConfig;
    private static final Object LOCK = new Object();

    private OpenConfig() {
        // Prevent instantiation
    }

    // ============ Global Configuration Management | 全局配置管理 ============

    /**
     * Get global configuration instance
     * 获取全局配置实例
     *
     * <p>Uses lazy initialization with double-check locking pattern.</p>
     * <p>使用双重检查锁模式的懒加载。</p>
     *
     * @return global configuration | 全局配置
     */
    public static Config getGlobal() {
        if (globalConfig == null) {
            synchronized (LOCK) {
                if (globalConfig == null) {
                    globalConfig = createDefaultConfig();
                }
            }
        }
        return globalConfig;
    }

    /**
     * Set global configuration
     * 设置全局配置
     *
     * @param config configuration instance | 配置实例
     */
    public static void setGlobal(Config config) {
        synchronized (LOCK) {
            globalConfig = config;
        }
    }

    /**
     * Create configuration builder
     * 创建配置构建器
     *
     * @return new builder instance | 新的构建器实例
     */
    public static ConfigBuilder builder() {
        return new ConfigBuilder();
    }

    // ============ Static Delegate Methods | 静态委托方法 ============

    public static String getString(String key) {
        return getGlobal().getString(key);
    }

    public static String getString(String key, String defaultValue) {
        return getGlobal().getString(key, defaultValue);
    }

    public static int getInt(String key) {
        return getGlobal().getInt(key);
    }

    public static int getInt(String key, int defaultValue) {
        return getGlobal().getInt(key, defaultValue);
    }

    public static long getLong(String key) {
        return getGlobal().getLong(key);
    }

    public static long getLong(String key, long defaultValue) {
        return getGlobal().getLong(key, defaultValue);
    }

    public static double getDouble(String key) {
        return getGlobal().getDouble(key);
    }

    public static double getDouble(String key, double defaultValue) {
        return getGlobal().getDouble(key, defaultValue);
    }

    public static boolean getBoolean(String key) {
        return getGlobal().getBoolean(key);
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return getGlobal().getBoolean(key, defaultValue);
    }

    public static Duration getDuration(String key) {
        return getGlobal().getDuration(key);
    }

    public static Duration getDuration(String key, Duration defaultValue) {
        return getGlobal().getDuration(key, defaultValue);
    }

    public static <T> T get(String key, Class<T> type) {
        return getGlobal().get(key, type);
    }

    public static <T> T get(String key, Class<T> type, T defaultValue) {
        return getGlobal().get(key, type, defaultValue);
    }

    public static <T> List<T> getList(String key, Class<T> elementType) {
        return getGlobal().getList(key, elementType);
    }

    public static <K, V> Map<K, V> getMap(String key, Class<K> keyType, Class<V> valueType) {
        return getGlobal().getMap(key, keyType, valueType);
    }

    public static <T> Optional<T> getOptional(String key, Class<T> type) {
        return getGlobal().getOptional(key, type);
    }

    public static Optional<String> getOptional(String key) {
        return getGlobal().getOptional(key);
    }

    public static Config getSubConfig(String prefix) {
        return getGlobal().getSubConfig(prefix);
    }

    public static Map<String, String> getByPrefix(String prefix) {
        return getGlobal().getByPrefix(prefix);
    }

    public static boolean hasKey(String key) {
        return getGlobal().hasKey(key);
    }

    public static Set<String> getKeys() {
        return getGlobal().getKeys();
    }

    public static <T> T bind(String prefix, Class<T> type) {
        return getGlobal().bind(prefix, type);
    }

    public static <T> void bindTo(String prefix, T target) {
        getGlobal().bindTo(prefix, target);
    }

    // ============ Quick Loading Methods | 快捷加载方法 ============

    /**
     * Load configuration from classpath resources
     * 从类路径资源加载配置
     *
     * @param resources resource paths | 资源路径
     * @return configuration instance | 配置实例
     */
    public static Config loadFromClasspath(String... resources) {
        return builder()
            .addClasspathResources(resources)
            .build();
    }

    /**
     * Load configuration from files
     * 从文件加载配置
     *
     * @param files file paths | 文件路径
     * @return configuration instance | 配置实例
     */
    public static Config loadFromFile(Path... files) {
        return builder()
            .addFiles(files)
            .build();
    }

    /**
     * Load configuration from properties map
     * 从属性映射加载配置
     *
     * @param properties properties map | 属性映射
     * @return configuration instance | 配置实例
     */
    public static Config loadFromProperties(Map<String, String> properties) {
        return builder()
            .addProperties(properties)
            .build();
    }

    // ============ Private Methods | 私有方法 ============

    /**
     * Create default configuration
     * 创建默认配置
     *
     * <p>Loads configuration from:
     * 1. application.properties (classpath)
     * 2. Environment variables
     * 3. System properties</p>
     *
     * @return default configuration | 默认配置
     */
    private static Config createDefaultConfig() {
        return builder()
            .addClasspathResource("application.properties")
            .addEnvironmentVariables()
            .addSystemProperties()
            .build();
    }
}
