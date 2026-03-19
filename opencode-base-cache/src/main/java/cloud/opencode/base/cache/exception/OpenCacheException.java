package cloud.opencode.base.cache.exception;

import cloud.opencode.base.core.exception.OpenException;

import java.util.Optional;

/**
 * Open Cache Exception - Unified exception for cache operations
 * OpenCache 异常 - 缓存操作的统一异常
 *
 * <p>Extends OpenException to maintain consistent exception hierarchy.</p>
 * <p>继承 OpenException 以保持一致的异常体系。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Cache name tracking - 缓存名称跟踪</li>
 *   <li>Key information - 键信息</li>
 *   <li>Cause chaining - 原因链</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create exception - 创建异常
 * throw new OpenCacheException("users", "user:1001", "Failed to load user");
 *
 * // With cause - 带原因
 * throw new OpenCacheException("users", "user:1001", cause);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
public class OpenCacheException extends OpenException {

    private static final String COMPONENT = "cache";

    private final String cacheName;
    private final Object key;

    /**
     * Create exception with message
     * 使用消息创建异常
     *
     * @param message the message | 消息
     */
    public OpenCacheException(String message) {
        super(COMPONENT, null, message);
        this.cacheName = null;
        this.key = null;
    }

    /**
     * Create exception with message and cause
     * 使用消息和原因创建异常
     *
     * @param message the message | 消息
     * @param cause   the cause | 原因
     */
    public OpenCacheException(String message, Throwable cause) {
        super(COMPONENT, null, message, cause);
        this.cacheName = null;
        this.key = null;
    }

    /**
     * Create exception with cache name, key and message
     * 使用缓存名称、键和消息创建异常
     *
     * @param cacheName the cache name | 缓存名称
     * @param key       the key | 键
     * @param message   the message | 消息
     */
    public OpenCacheException(String cacheName, Object key, String message) {
        super(COMPONENT, null, formatMessage(cacheName, key, message));
        this.cacheName = cacheName;
        this.key = key;
    }

    /**
     * Create exception with cache name, key and cause
     * 使用缓存名称、键和原因创建异常
     *
     * @param cacheName the cache name | 缓存名称
     * @param key       the key | 键
     * @param cause     the cause | 原因
     */
    public OpenCacheException(String cacheName, Object key, Throwable cause) {
        super(COMPONENT, null, formatMessage(cacheName, key, cause.getMessage()), cause);
        this.cacheName = cacheName;
        this.key = key;
    }

    /**
     * Create exception with cache name, key, message and cause
     * 使用缓存名称、键、消息和原因创建异常
     *
     * @param cacheName the cache name | 缓存名称
     * @param key       the key | 键
     * @param message   the message | 消息
     * @param cause     the cause | 原因
     */
    public OpenCacheException(String cacheName, Object key, String message, Throwable cause) {
        super(COMPONENT, null, formatMessage(cacheName, key, message), cause);
        this.cacheName = cacheName;
        this.key = key;
    }

    /**
     * Get cache name
     * 获取缓存名称
     *
     * @return optional containing cache name | 包含缓存名称的 Optional
     */
    public Optional<String> getCacheName() {
        return Optional.ofNullable(cacheName);
    }

    /**
     * Get key
     * 获取键
     *
     * @return optional containing key | 包含键的 Optional
     */
    public Optional<Object> getKey() {
        return Optional.ofNullable(key);
    }

    private static String formatMessage(String cacheName, Object key, String message) {
        if (cacheName != null && key != null) {
            return String.format("Cache[%s] key[%s]: %s", cacheName, key, message);
        } else if (cacheName != null) {
            return String.format("Cache[%s]: %s", cacheName, message);
        } else {
            return message;
        }
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Create loading exception
     * 创建加载异常
     *
     * @param cacheName cache name | 缓存名称
     * @param key       the key | 键
     * @param cause     the cause | 原因
     * @return exception | 异常
     */
    public static OpenCacheException loadingFailed(String cacheName, Object key, Throwable cause) {
        return new OpenCacheException(cacheName, key, "Failed to load value", cause);
    }

    /**
     * Create timeout exception
     * 创建超时异常
     *
     * @param cacheName cache name | 缓存名称
     * @param key       the key | 键
     * @return exception | 异常
     */
    public static OpenCacheException timeout(String cacheName, Object key) {
        return new OpenCacheException(cacheName, key, "Operation timed out");
    }

    /**
     * Create capacity exceeded exception
     * 创建容量超出异常
     *
     * @param cacheName   cache name | 缓存名称
     * @param maxCapacity maximum capacity | 最大容量
     * @return exception | 异常
     */
    public static OpenCacheException capacityExceeded(String cacheName, long maxCapacity) {
        return new OpenCacheException(cacheName, null,
                "Cache capacity exceeded: max=" + maxCapacity);
    }
}
