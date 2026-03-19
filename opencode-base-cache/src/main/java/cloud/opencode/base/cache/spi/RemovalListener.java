package cloud.opencode.base.cache.spi;

import cloud.opencode.base.cache.model.RemovalCause;

/**
 * Removal Listener SPI - Cache entry removal callback interface
 * 移除监听器 SPI - 缓存条目移除回调接口
 *
 * <p>Provides callback when cache entries are removed for any reason.</p>
 * <p>当缓存条目因任何原因被移除时提供回调。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Notification on entry removal - 条目移除时通知</li>
 *   <li>Access to removal cause - 访问移除原因</li>
 *   <li>Logging and auditing - 日志和审计</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * RemovalListener<String, User> listener = (key, value, cause) -> {
 *     if (cause.wasEvicted()) {
 *         log.info("User cache evicted: {}", key);
 *     }
 * };
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: value may be null - 空值安全: value 可能为 null</li>
 * </ul>
 *
 * @param <K> the type of keys | 键类型
 * @param <V> the type of values | 值类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@FunctionalInterface
public interface RemovalListener<K, V> {

    /**
     * Called when an entry is removed from cache
     * 当条目从缓存中移除时调用
     *
     * @param key   the key being removed | 被移除的键
     * @param value the value being removed (may be null) | 被移除的值（可能为 null）
     * @param cause the removal cause | 移除原因
     */
    void onRemoval(K key, V value, RemovalCause cause);

    /**
     * Create a listener that does nothing
     * 创建一个什么都不做的监听器
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return no-op listener | 空操作监听器
     */
    static <K, V> RemovalListener<K, V> noOp() {
        return (key, value, cause) -> {
        };
    }

    /**
     * Create a listener that logs removals
     * 创建一个记录移除日志的监听器
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return logging listener | 日志监听器
     */
    static <K, V> RemovalListener<K, V> logging() {
        System.Logger logger = System.getLogger(RemovalListener.class.getName());
        return (key, value, cause) ->
                logger.log(System.Logger.Level.INFO, "Cache entry removed: key=" + key + ", cause=" + cause);
    }

    /**
     * Combine multiple listeners
     * 组合多个监听器
     *
     * @param listeners the listeners | 监听器列表
     * @param <K>       key type | 键类型
     * @param <V>       value type | 值类型
     * @return combined listener | 组合后的监听器
     */
    @SafeVarargs
    static <K, V> RemovalListener<K, V> combine(RemovalListener<K, V>... listeners) {
        return (key, value, cause) -> {
            for (RemovalListener<K, V> listener : listeners) {
                listener.onRemoval(key, value, cause);
            }
        };
    }
}
