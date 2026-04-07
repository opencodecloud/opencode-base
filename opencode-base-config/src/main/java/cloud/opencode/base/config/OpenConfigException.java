package cloud.opencode.base.config;

import cloud.opencode.base.core.exception.OpenException;

/**
 * Configuration Component Exception
 * 配置组件异常
 *
 * <p>This exception is thrown when configuration operations fail, including loading errors,
 * conversion errors, validation errors, and other configuration-related issues.</p>
 * <p>此异常在配置操作失败时抛出,包括加载错误、转换错误、验证错误和其他配置相关问题。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Configuration key tracking - 配置键跟踪</li>
 *   <li>Configuration source identification - 配置源识别</li>
 *   <li>Type conversion error details - 类型转换错误详情</li>
 *   <li>Factory methods for common errors - 常见错误的工厂方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Key not found
 * throw OpenConfigException.keyNotFound("database.url");
 *
 * // Conversion failed
 * throw OpenConfigException.conversionFailed("port", "abc", Integer.class);
 *
 * // Source load failed
 * throw OpenConfigException.sourceLoadFailed("config.properties", cause);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Immutable: Yes - 不可变: 是</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
public class OpenConfigException extends OpenException {

    private static final String COMPONENT = "config";

    /** Configuration key | 配置键 */
    private final String configKey;

    /** Configuration source | 配置源 */
    private final String configSource;

    // ==================== Constructors | 构造方法 ====================

    /**
     * Create exception with message
     * 创建带消息的异常
     *
     * @param message error message | 错误消息
     */
    public OpenConfigException(String message) {
        super(COMPONENT, null, message);
        this.configKey = null;
        this.configSource = null;
    }

    /**
     * Create exception with message and cause
     * 创建带消息和原因的异常
     *
     * @param message error message | 错误消息
     * @param cause throwable cause | 异常原因
     */
    public OpenConfigException(String message, Throwable cause) {
        super(COMPONENT, null, message, cause);
        this.configKey = null;
        this.configSource = null;
    }

    /**
     * Create exception with config key, source and message
     * 创建带配置键、源和消息的异常
     *
     * @param configKey configuration key | 配置键
     * @param configSource configuration source | 配置源
     * @param message error message | 错误消息
     */
    public OpenConfigException(String configKey, String configSource, String message) {
        super(COMPONENT, null, message);
        this.configKey = configKey;
        this.configSource = configSource;
    }

    /**
     * Create exception with config key, source, message and cause
     * 创建带配置键、源、消息和原因的异常
     *
     * @param configKey configuration key | 配置键
     * @param configSource configuration source | 配置源
     * @param message error message | 错误消息
     * @param cause throwable cause | 异常原因
     */
    public OpenConfigException(String configKey, String configSource, String message, Throwable cause) {
        super(COMPONENT, null, message, cause);
        this.configKey = configKey;
        this.configSource = configSource;
    }

    // ==================== Accessor Methods | 访问方法 ====================

    /**
     * Get configuration key
     * 获取配置键
     *
     * @return configuration key or null | 配置键或null
     */
    public String configKey() {
        return configKey;
    }

    /**
     * Get configuration source
     * 获取配置源
     *
     * @return configuration source or null | 配置源或null
     */
    public String configSource() {
        return configSource;
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Configuration key not found
     * 配置键未找到
     *
     * @param key configuration key | 配置键
     * @return exception instance | 异常实例
     */
    public static OpenConfigException keyNotFound(String key) {
        return new OpenConfigException(key, null,
            String.format("Configuration key not found: %s", key));
    }

    /**
     * Required configuration missing
     * 必填配置缺失
     *
     * @param key configuration key | 配置键
     * @return exception instance | 异常实例
     */
    public static OpenConfigException requiredKeyMissing(String key) {
        return new OpenConfigException(key, null,
            String.format("Required configuration key is missing: %s", key));
    }

    /**
     * Type conversion failed
     * 类型转换失败
     *
     * @param key configuration key | 配置键
     * @param value configuration value | 配置值
     * @param targetType target type | 目标类型
     * @return exception instance | 异常实例
     */
    public static OpenConfigException conversionFailed(String key, String value, Class<?> targetType) {
        return new OpenConfigException(key, null,
            String.format("Failed to convert config '%s' to type %s (value redacted for security)",
                key, targetType.getName()));
    }

    /**
     * Type conversion failed with cause
     * 类型转换失败(带原因)
     *
     * @param key configuration key | 配置键
     * @param value configuration value | 配置值
     * @param targetType target type | 目标类型
     * @param cause throwable cause | 异常原因
     * @return exception instance | 异常实例
     */
    public static OpenConfigException conversionFailed(String key, String value, Class<?> targetType, Throwable cause) {
        return new OpenConfigException(key, null,
            String.format("Failed to convert config '%s' to type %s (value redacted for security)",
                key, targetType.getName()), cause);
    }

    /**
     * Configuration source load failed
     * 配置源加载失败
     *
     * @param source configuration source | 配置源
     * @param cause throwable cause | 异常原因
     * @return exception instance | 异常实例
     */
    public static OpenConfigException sourceLoadFailed(String source, Throwable cause) {
        return new OpenConfigException(null, source,
            String.format("Failed to load configuration source: %s", source), cause);
    }

    /**
     * Configuration source not supported
     * 配置源不支持
     *
     * @param uri configuration source URI | 配置源URI
     * @return exception instance | 异常实例
     */
    public static OpenConfigException sourceNotSupported(String uri) {
        return new OpenConfigException(null, uri,
            String.format("Unsupported configuration source URI: %s", uri));
    }

    /**
     * Placeholder resolution failed
     * 占位符解析失败
     *
     * @param placeholder placeholder expression | 占位符表达式
     * @return exception instance | 异常实例
     */
    public static OpenConfigException placeholderResolveFailed(String placeholder) {
        return new OpenConfigException(placeholder, null,
            String.format("Cannot resolve placeholder: ${%s}", placeholder));
    }

    /**
     * Placeholder recursion too deep
     * 占位符递归过深
     *
     * @param value configuration value | 配置值
     * @return exception instance | 异常实例
     */
    public static OpenConfigException placeholderRecursionTooDeep(String value) {
        return new OpenConfigException(value, null,
            String.format("Placeholder recursion too deep: %s", value));
    }

    /**
     * Configuration binding failed
     * 配置绑定失败
     *
     * @param prefix configuration prefix | 配置前缀
     * @param targetType target type | 目标类型
     * @param cause throwable cause | 异常原因
     * @return exception instance | 异常实例
     */
    public static OpenConfigException bindFailed(String prefix, Class<?> targetType, Throwable cause) {
        return new OpenConfigException(prefix, null,
            String.format("Failed to bind config prefix '%s' to type %s", prefix, targetType.getName()), cause);
    }

    /**
     * Field binding failed
     * 字段绑定失败
     *
     * @param fieldName field name | 字段名
     * @param cause throwable cause | 异常原因
     * @return exception instance | 异常实例
     */
    public static OpenConfigException fieldBindFailed(String fieldName, Throwable cause) {
        return new OpenConfigException(fieldName, null,
            String.format("Failed to bind configuration to field: %s", fieldName), cause);
    }

    /**
     * Configuration validation failed
     * 配置验证失败
     *
     * @param errors validation errors | 验证错误
     * @return exception instance | 异常实例
     */
    public static OpenConfigException validationFailed(String errors) {
        return new OpenConfigException(null, null,
            String.format("Configuration validation failed: %s", errors));
    }

    /**
     * Invalid boolean value
     * 无效布尔值
     *
     * @param value configuration value | 配置值
     * @return exception instance | 异常实例
     */
    public static OpenConfigException invalidBoolean(String value) {
        return new OpenConfigException(null, null,
            String.format("Invalid boolean value: %s", value));
    }

    /**
     * Invalid URL
     * 无效URL
     *
     * @param url URL string | URL字符串
     * @param cause throwable cause | 异常原因
     * @return exception instance | 异常实例
     */
    public static OpenConfigException invalidUrl(String url, Throwable cause) {
        return new OpenConfigException(null, null,
            String.format("Invalid URL: %s", url), cause);
    }

    /**
     * Converter not found for type
     * 类型转换器未找到
     *
     * @param type target type | 目标类型
     * @return exception instance | 异常实例
     */
    public static OpenConfigException converterNotFound(Class<?> type) {
        return new OpenConfigException(null, null,
            String.format("No converter found for type: %s", type.getName()));
    }

    /**
     * Configuration decryption failed
     * 配置解密失败
     *
     * @param cause throwable cause | 异常原因
     * @return exception instance | 异常实例
     */
    public static OpenConfigException decryptionFailed(Throwable cause) {
        return new OpenConfigException(null, null,
            "Failed to decrypt configuration value", cause);
    }
}
