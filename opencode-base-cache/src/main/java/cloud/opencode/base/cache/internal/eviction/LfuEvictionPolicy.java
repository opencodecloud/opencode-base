package cloud.opencode.base.cache.internal.eviction;

import cloud.opencode.base.cache.model.CacheEntry;
import cloud.opencode.base.cache.spi.EvictionPolicy;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * LFU Eviction Policy - Least Frequently Used eviction strategy
 * LFU 淘汰策略 - 最不经常使用淘汰策略
 *
 * <p>Evicts the least frequently accessed entry when cache is full.</p>
 * <p>当缓存已满时淘汰访问频率最低的条目。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Frequency-based tracking - 频率跟踪</li>
 *   <li>O(n) eviction selection - O(n) 淘汰选择</li>
 *   <li>Good for stable hot data - 适合稳定热点数据场景</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) access, O(n) eviction - 时间复杂度: O(1) 访问, O(n) 淘汰</li>
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
 * EvictionPolicy<String, User> policy = EvictionPolicy.lfu();
 * }</pre>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
public final class LfuEvictionPolicy<K, V> implements EvictionPolicy<K, V> {

    private final Map<K, Long> frequency = new HashMap<>();

    @Override
    public void recordAccess(CacheEntry<K, V> entry) {
        frequency.merge(entry.key(), 1L, Long::sum);
    }

    @Override
    public void recordWrite(CacheEntry<K, V> entry) {
        frequency.putIfAbsent(entry.key(), 0L);
    }

    @Override
    public Optional<K> selectVictim(Map<K, CacheEntry<K, V>> entries) {
        return frequency.entrySet().stream()
                .filter(e -> entries.containsKey(e.getKey()))
                .min(Comparator.comparingLong(Map.Entry::getValue))
                .map(Map.Entry::getKey);
    }

    @Override
    public void onRemoval(K key) {
        frequency.remove(key);
    }

    @Override
    public void reset() {
        frequency.clear();
    }
}
