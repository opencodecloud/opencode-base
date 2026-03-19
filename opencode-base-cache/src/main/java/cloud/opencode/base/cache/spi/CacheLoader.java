package cloud.opencode.base.cache.spi;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Cache Loader SPI - Synchronous cache value loader interface
 * 缓存加载器 SPI - 同步缓存值加载接口
 *
 * <p>Provides interface for loading cache values when they are not present.</p>
 * <p>提供缓存值不存在时的加载接口。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Single value loading - 单值加载</li>
 *   <li>Batch loading - 批量加载</li>
 *   <li>Reload/refresh support - 重新加载/刷新支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CacheLoader<String, User> loader = key -> userDao.findById(key);
 *
 * // Or with batch loading - 或者批量加载
 * CacheLoader<String, User> batchLoader = new CacheLoader<>() {
 *     public User load(String key) { return userDao.findById(key); }
 *     public Map<String, User> loadAll(Set<String> keys) {
 *         return userDao.findByIds(keys);
 *     }
 * };
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: May return null for missing values - 空值安全: 可为缺失值返回 null</li>
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
public interface CacheLoader<K, V> {

    /**
     * Load value for single key
     * 加载单个键的值
     *
     * @param key the key | 键
     * @return the value, or null if not found | 值，未找到返回 null
     * @throws Exception if loading fails | 加载失败时抛出异常
     */
    V load(K key) throws Exception;

    /**
     * Batch load values for multiple keys (default: load one by one)
     * 批量加载多个键的值（默认：逐个加载）
     *
     * @param keys the keys | 键集合
     * @return map of key-value pairs | 键值对 Map
     * @throws Exception if loading fails | 加载失败时抛出异常
     */
    default Map<K, V> loadAll(Set<? extends K> keys) throws Exception {
        Map<K, V> result = new HashMap<>();
        for (K key : keys) {
            V value = load(key);
            if (value != null) {
                result.put(key, value);
            }
        }
        return result;
    }

    /**
     * Reload value (default: just load again)
     * 重新加载值（默认：直接重新加载）
     *
     * @param key      the key | 键
     * @param oldValue the old value | 旧值
     * @return the new value | 新值
     * @throws Exception if reloading fails | 重新加载失败时抛出异常
     */
    default V reload(K key, V oldValue) throws Exception {
        return load(key);
    }
}
