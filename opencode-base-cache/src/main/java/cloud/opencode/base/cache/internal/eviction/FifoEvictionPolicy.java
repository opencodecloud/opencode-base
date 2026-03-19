package cloud.opencode.base.cache.internal.eviction;

import cloud.opencode.base.cache.model.CacheEntry;
import cloud.opencode.base.cache.spi.EvictionPolicy;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;

/**
 * FIFO Eviction Policy - First In First Out eviction strategy
 * FIFO 淘汰策略 - 先进先出淘汰策略
 *
 * <p>Evicts the oldest entry (first inserted) when cache is full.</p>
 * <p>当缓存已满时淘汰最老的条目（最先插入）。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Insertion-order tracking - 插入顺序跟踪</li>
 *   <li>O(1) access and eviction - O(1) 访问和淘汰</li>
 *   <li>Simple and predictable - 简单且可预测</li>
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
 * EvictionPolicy<String, User> policy = EvictionPolicy.fifo();
 * }</pre>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
public final class FifoEvictionPolicy<K, V> implements EvictionPolicy<K, V> {

    private final LinkedHashSet<K> insertionOrder = new LinkedHashSet<>();

    @Override
    public void recordAccess(CacheEntry<K, V> entry) {
        // FIFO does not update on access
    }

    @Override
    public void recordWrite(CacheEntry<K, V> entry) {
        // Remove and re-add to update position only if it's a new entry
        if (!insertionOrder.contains(entry.key())) {
            insertionOrder.add(entry.key());
        }
    }

    @Override
    public Optional<K> selectVictim(Map<K, CacheEntry<K, V>> entries) {
        return insertionOrder.stream()
                .filter(entries::containsKey)
                .findFirst();
    }

    @Override
    public void onRemoval(K key) {
        insertionOrder.remove(key);
    }

    @Override
    public void reset() {
        insertionOrder.clear();
    }
}
