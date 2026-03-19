package cloud.opencode.base.core.internal;

import java.util.Optional;
import java.util.function.Function;

/**
 * Internal Cache Interface - Contract for internal cache implementations
 * 内部缓存接口 - 内部缓存实现契约
 *
 * <p>Base interface for internal cache implementations. For internal use only, API stability not guaranteed.</p>
 * <p>内部缓存实现的基础接口。仅供内部使用，不保证 API 稳定性。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Cache operations (get, put, remove) - 缓存操作</li>
 *   <li>Compute if absent (computeIfAbsent) - 不存在时计算</li>
 *   <li>Optional wrapper (getOptional) - Optional 包装</li>
 *   <li>Cache management (clear, size, isEmpty) - 缓存管理</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * InternalCache<String, Object> cache = InternalLRUCache.create(100);
 * cache.put("key", value);
 * Object result = cache.computeIfAbsent("key", k -> compute(k));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Depends on implementation - 线程安全: 取决于实现</li>
 *   <li>Internal API: Not for public use - 内部 API: 非公开使用</li>
 * </ul>
 *
 * @param <K> Key type - Key 类型
 * @param <V> Value type - Value 类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public interface InternalCache<K, V> {

    /**
     * Gets
     * 获取缓存值
     */
    V get(K key);

    /**
     * Gets
     * 获取缓存值（Optional 包装）
     */
    default Optional<V> getOptional(K key) {
        return Optional.ofNullable(get(key));
    }

    /**
     * Gets
     * 获取缓存值，不存在时计算并缓存
     */
    V computeIfAbsent(K key, Function<K, V> mappingFunction);

    /**
     * Puts into the cache
     * 放入缓存
     */
    V put(K key, V value);

    /**
     * Puts into the cache (if not already present)
     * 放入缓存（如果不存在）
     */
    V putIfAbsent(K key, V value);

    /**
     * Removes from the cache
     * 移除缓存
     */
    V remove(K key);

    /**
     * Checks
     * 检查是否包含 key
     */
    boolean containsKey(K key);

    /**
     * Gets
     * 获取缓存大小
     */
    int size();

    /**
     * Clears the cache
     * 清空缓存
     */
    void clear();

    /**
     * Checks
     * 检查是否为空
     */
    default boolean isEmpty() {
        return size() == 0;
    }
}
