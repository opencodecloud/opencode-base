package cloud.opencode.base.cache.internal.eviction;

import cloud.opencode.base.cache.model.CacheEntry;
import cloud.opencode.base.cache.spi.EvictionPolicy;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * LRU Eviction Policy - Least Recently Used eviction strategy
 * LRU 淘汰策略 - 最近最少使用淘汰策略
 *
 * <p>Evicts the least recently accessed entry when cache is full.</p>
 * <p>当缓存已满时淘汰最近最少访问的条目。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Access-order tracking - 访问顺序跟踪</li>
 *   <li>O(1) access and eviction - O(1) 访问和淘汰</li>
 *   <li>Good for temporal locality - 适合时间局部性场景</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) - 时间复杂度: O(1)</li>
 *   <li>Space complexity: O(n) - 空间复杂度: O(n)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (external sync required) - 线程安全: 否（需外部同步）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @param <K> the type of keys | 键类型
 * @param <V> the type of values | 值类型
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * EvictionPolicy<String, User> policy = EvictionPolicy.lru();
 * }</pre>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
public final class LruEvictionPolicy<K, V> implements EvictionPolicy<K, V> {

    private final LinkedHashMap<K, Long> accessOrder = new LinkedHashMap<>(16, 0.75f, true);

    @Override
    public void recordAccess(CacheEntry<K, V> entry) {
        accessOrder.put(entry.key(), System.nanoTime());
    }

    @Override
    public void recordWrite(CacheEntry<K, V> entry) {
        accessOrder.put(entry.key(), System.nanoTime());
    }

    @Override
    public Optional<K> selectVictim(Map<K, CacheEntry<K, V>> entries) {
        if (accessOrder.isEmpty()) {
            return entries.keySet().stream().findFirst();
        }
        return accessOrder.keySet().stream().findFirst();
    }

    @Override
    public void onRemoval(K key) {
        accessOrder.remove(key);
    }

    @Override
    public void reset() {
        accessOrder.clear();
    }
}
