package cloud.opencode.base.config;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Unified Configuration Access Interface
 * 统一配置访问接口
 *
 * <p>This interface provides type-safe configuration access with support for multiple sources,
 * type conversion, placeholder resolution, and hot reloading.</p>
 * <p>此接口提供类型安全的配置访问,支持多配置源、类型转换、占位符解析和热更新。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Type-safe configuration retrieval - 类型安全的配置检索</li>
 *   <li>Multiple configuration sources with priority - 支持优先级的多配置源</li>
 *   <li>Placeholder resolution ${key} - 占位符解析${key}</li>
 *   <li>Configuration change listeners - 配置变更监听</li>
 *   <li>Sub-configuration by prefix - 按前缀获取子配置</li>
 *   <li>Configuration binding to POJOs/Records - 配置绑定到POJO/Record</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Basic retrieval
 * String url = config.getString("database.url");
 * int port = config.getInt("server.port", 8080);
 * Duration timeout = config.getDuration("http.timeout");
 *
 * // Type-safe retrieval
 * LocalDate date = config.get("start.date", LocalDate.class);
 * List<String> hosts = config.getList("redis.hosts", String.class);
 *
 * // Optional retrieval
 * Optional<String> apiKey = config.getOptional("api.key");
 *
 * // Sub-configuration
 * Config dbConfig = config.getSubConfig("database");
 * String dbUrl = dbConfig.getString("url"); // reads database.url
 *
 * // Configuration binding
 * DatabaseConfig dbCfg = config.bind("database", DatabaseConfig.class);
 *
 * // Change listeners
 * config.addListener("log.level", event -> {
 *     System.out.println("Log level changed: " + event.newValue());
 * });
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) for direct key access - 时间复杂度: 直接键访问为O(1)</li>
 *   <li>Type conversion cached - 类型转换结果缓存</li>
 *   <li>Immutable snapshots for thread safety - 不可变快照保证线程安全</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 *   <li>Immutable interface - 不可变接口</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
public interface Config {

    // ============ Basic Retrieval | 基础获取 ============

    /**
     * Get string configuration value
     * 获取字符串配置值
     *
     * @param key configuration key | 配置键
     * @return configuration value | 配置值
     * @throws OpenConfigException if key not found | 如果键未找到
     */
    String getString(String key);

    /**
     * Get string configuration value with default
     * 获取字符串配置值(带默认值)
     *
     * @param key configuration key | 配置键
     * @param defaultValue default value if key not found | 键未找到时的默认值
     * @return configuration value or default | 配置值或默认值
     */
    String getString(String key, String defaultValue);

    /**
     * Get integer configuration value
     * 获取整数配置值
     *
     * @param key configuration key | 配置键
     * @return configuration value | 配置值
     * @throws OpenConfigException if key not found or conversion failed | 如果键未找到或转换失败
     */
    int getInt(String key);

    /**
     * Get integer configuration value with default
     * 获取整数配置值(带默认值)
     *
     * @param key configuration key | 配置键
     * @param defaultValue default value | 默认值
     * @return configuration value or default | 配置值或默认值
     */
    int getInt(String key, int defaultValue);

    /**
     * Get long configuration value
     * 获取长整数配置值
     *
     * @param key configuration key | 配置键
     * @return configuration value | 配置值
     * @throws OpenConfigException if key not found or conversion failed | 如果键未找到或转换失败
     */
    long getLong(String key);

    /**
     * Get long configuration value with default
     * 获取长整数配置值(带默认值)
     *
     * @param key configuration key | 配置键
     * @param defaultValue default value | 默认值
     * @return configuration value or default | 配置值或默认值
     */
    long getLong(String key, long defaultValue);

    /**
     * Get double configuration value
     * 获取双精度配置值
     *
     * @param key configuration key | 配置键
     * @return configuration value | 配置值
     * @throws OpenConfigException if key not found or conversion failed | 如果键未找到或转换失败
     */
    double getDouble(String key);

    /**
     * Get double configuration value with default
     * 获取双精度配置值(带默认值)
     *
     * @param key configuration key | 配置键
     * @param defaultValue default value | 默认值
     * @return configuration value or default | 配置值或默认值
     */
    double getDouble(String key, double defaultValue);

    /**
     * Get boolean configuration value
     * 获取布尔配置值
     *
     * @param key configuration key | 配置键
     * @return configuration value | 配置值
     * @throws OpenConfigException if key not found or conversion failed | 如果键未找到或转换失败
     */
    boolean getBoolean(String key);

    /**
     * Get boolean configuration value with default
     * 获取布尔配置值(带默认值)
     *
     * @param key configuration key | 配置键
     * @param defaultValue default value | 默认值
     * @return configuration value or default | 配置值或默认值
     */
    boolean getBoolean(String key, boolean defaultValue);

    /**
     * Get Duration configuration value
     * 获取Duration配置值
     *
     * @param key configuration key | 配置键
     * @return configuration value | 配置值
     * @throws OpenConfigException if key not found or conversion failed | 如果键未找到或转换失败
     */
    Duration getDuration(String key);

    /**
     * Get Duration configuration value with default
     * 获取Duration配置值(带默认值)
     *
     * @param key configuration key | 配置键
     * @param defaultValue default value | 默认值
     * @return configuration value or default | 配置值或默认值
     */
    Duration getDuration(String key, Duration defaultValue);

    // ============ Generic Retrieval | 泛型获取 ============

    /**
     * Get typed configuration value
     * 获取指定类型配置值
     *
     * @param <T> target type | 目标类型
     * @param key configuration key | 配置键
     * @param type target class | 目标类
     * @return configuration value | 配置值
     * @throws OpenConfigException if key not found or conversion failed | 如果键未找到或转换失败
     */
    <T> T get(String key, Class<T> type);

    /**
     * Get typed configuration value with default
     * 获取指定类型配置值(带默认值)
     *
     * @param <T> target type | 目标类型
     * @param key configuration key | 配置键
     * @param type target class | 目标类
     * @param defaultValue default value | 默认值
     * @return configuration value or default | 配置值或默认值
     */
    <T> T get(String key, Class<T> type, T defaultValue);

    /**
     * Get list configuration value
     * 获取列表配置值
     *
     * @param <T> element type | 元素类型
     * @param key configuration key | 配置键
     * @param elementType element class | 元素类
     * @return configuration value list | 配置值列表
     */
    <T> List<T> getList(String key, Class<T> elementType);

    /**
     * Get map configuration value
     * 获取映射配置值
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @param key configuration key | 配置键
     * @param keyType key class | 键类
     * @param valueType value class | 值类
     * @return configuration value map | 配置值映射
     */
    <K, V> Map<K, V> getMap(String key, Class<K> keyType, Class<V> valueType);

    // ============ Optional Retrieval | Optional获取 ============

    /**
     * Get optional string configuration value
     * 获取Optional字符串配置值
     *
     * @param key configuration key | 配置键
     * @return optional configuration value | Optional配置值
     */
    Optional<String> getOptional(String key);

    /**
     * Get optional typed configuration value
     * 获取Optional类型配置值
     *
     * @param <T> target type | 目标类型
     * @param key configuration key | 配置键
     * @param type target class | 目标类
     * @return optional configuration value | Optional配置值
     */
    <T> Optional<T> getOptional(String key, Class<T> type);

    // ============ Sub-configuration | 子配置 ============

    /**
     * Get sub-configuration by prefix
     * 根据前缀获取子配置
     *
     * @param prefix configuration prefix | 配置前缀
     * @return sub-configuration | 子配置
     */
    Config getSubConfig(String prefix);

    /**
     * Get all configurations with specified prefix
     * 获取指定前缀的所有配置
     *
     * @param prefix configuration prefix | 配置前缀
     * @return configuration map | 配置映射
     */
    Map<String, String> getByPrefix(String prefix);

    // ============ Configuration Checks | 配置检查 ============

    /**
     * Check if configuration key exists
     * 检查配置键是否存在
     *
     * @param key configuration key | 配置键
     * @return true if exists | 如果存在返回true
     */
    boolean hasKey(String key);

    /**
     * Get all configuration keys
     * 获取所有配置键
     *
     * @return set of all keys | 所有键的集合
     */
    Set<String> getKeys();

    // ============ Listeners | 监听器 ============

    /**
     * Add configuration change listener
     * 添加配置变更监听器
     *
     * @param listener configuration listener | 配置监听器
     */
    void addListener(ConfigListener listener);

    /**
     * Add listener for specific configuration key
     * 添加指定键的监听器
     *
     * @param key configuration key | 配置键
     * @param listener configuration listener | 配置监听器
     */
    void addListener(String key, ConfigListener listener);

    /**
     * Remove configuration change listener
     * 移除配置变更监听器
     *
     * @param listener configuration listener | 配置监听器
     */
    void removeListener(ConfigListener listener);

    // ============ Configuration Binding | 配置绑定 ============

    /**
     * Bind configuration to POJO/Record
     * 绑定配置到POJO/Record
     *
     * @param <T> target type | 目标类型
     * @param prefix configuration prefix | 配置前缀
     * @param type target class | 目标类
     * @return bound instance | 绑定的实例
     * @throws OpenConfigException if binding failed | 如果绑定失败
     */
    <T> T bind(String prefix, Class<T> type);

    /**
     * Bind configuration to existing object
     * 绑定配置到现有对象
     *
     * @param <T> target type | 目标类型
     * @param prefix configuration prefix | 配置前缀
     * @param target target object | 目标对象
     * @throws OpenConfigException if binding failed | 如果绑定失败
     */
    <T> void bindTo(String prefix, T target);
}
