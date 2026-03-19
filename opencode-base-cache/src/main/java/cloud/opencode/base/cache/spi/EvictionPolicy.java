package cloud.opencode.base.cache.spi;

import cloud.opencode.base.cache.model.CacheEntry;

import java.util.Map;
import java.util.Optional;

/**
 * Eviction Policy SPI - Cache entry eviction strategy interface
 * 淘汰策略 SPI - 缓存条目淘汰策略接口
 *
 * <p>Provides interface for selecting which entries to evict when cache is full.</p>
 * <p>提供当缓存已满时选择要淘汰哪些条目的接口。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>LRU - Least Recently Used - 最近最少使用</li>
 *   <li>LFU - Least Frequently Used - 最不经常使用</li>
 *   <li>FIFO - First In First Out - 先进先出</li>
 *   <li>W-TinyLFU - Window TinyLFU - 窗口 TinyLFU</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Use LRU policy - 使用 LRU 策略
 * EvictionPolicy<String, User> policy = EvictionPolicy.lru();
 *
 * // Use W-TinyLFU for better hit rate - 使用 W-TinyLFU 获得更好的命中率
 * EvictionPolicy<String, User> policy = EvictionPolicy.wTinyLfu();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
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
public interface EvictionPolicy<K, V> {

    /**
     * Record access to an entry
     * 记录条目访问
     *
     * @param entry the accessed entry | 被访问的条目
     */
    void recordAccess(CacheEntry<K, V> entry);

    /**
     * Record write to an entry
     * 记录条目写入
     *
     * @param entry the written entry | 被写入的条目
     */
    void recordWrite(CacheEntry<K, V> entry);

    /**
     * Select victim entry to evict
     * 选择要淘汰的条目
     *
     * @param entries current cache entries | 当前缓存条目
     * @return key of entry to evict | 要淘汰的条目的键
     */
    Optional<K> selectVictim(Map<K, CacheEntry<K, V>> entries);

    /**
     * Called when entry is removed
     * 当条目被移除时调用
     *
     * @param key the removed key | 被移除的键
     */
    void onRemoval(K key);

    /**
     * Reset policy state
     * 重置策略状态
     */
    default void reset() {
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Create LRU (Least Recently Used) policy
     * 创建 LRU（最近最少使用）策略
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return LRU policy | LRU 策略
     */
    static <K, V> EvictionPolicy<K, V> lru() {
        return new cloud.opencode.base.cache.internal.eviction.LruEvictionPolicy<>();
    }

    /**
     * Create LFU (Least Frequently Used) policy
     * 创建 LFU（最不经常使用）策略
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return LFU policy | LFU 策略
     */
    static <K, V> EvictionPolicy<K, V> lfu() {
        return new cloud.opencode.base.cache.internal.eviction.LfuEvictionPolicy<>();
    }

    /**
     * Create FIFO (First In First Out) policy
     * 创建 FIFO（先进先出）策略
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return FIFO policy | FIFO 策略
     */
    static <K, V> EvictionPolicy<K, V> fifo() {
        return new cloud.opencode.base.cache.internal.eviction.FifoEvictionPolicy<>();
    }

    /**
     * Create W-TinyLFU (Window TinyLFU) policy
     * 创建 W-TinyLFU（窗口 TinyLFU）策略
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return W-TinyLFU policy | W-TinyLFU 策略
     */
    static <K, V> EvictionPolicy<K, V> wTinyLfu() {
        return new cloud.opencode.base.cache.internal.eviction.WTinyLfuEvictionPolicy<>();
    }

    // ==================== Composition Methods | 组合方法 ====================

    /**
     * Combine with another policy using OR logic (evict if either suggests eviction)
     * 使用 OR 逻辑与另一个策略组合（任一策略建议淘汰则淘汰）
     *
     * <p>The combined policy selects a victim if either policy returns one.
     * Priority is given to this policy's selection.</p>
     * <p>组合策略在任一策略返回候选时选择该候选。优先选择当前策略的选择。</p>
     *
     * @param other the other policy | 另一个策略
     * @return combined policy | 组合后的策略
     */
    default EvictionPolicy<K, V> or(EvictionPolicy<K, V> other) {
        return new CompositeEvictionPolicy<>(this, other, CompositeEvictionPolicy.Mode.OR);
    }

    /**
     * Combine with another policy using AND logic (evict only if both suggest same victim)
     * 使用 AND 逻辑与另一个策略组合（仅当两个策略建议相同候选时淘汰）
     *
     * <p>The combined policy only evicts if both policies agree on the same victim.</p>
     * <p>组合策略仅在两个策略同意相同候选时淘汰。</p>
     *
     * @param other the other policy | 另一个策略
     * @return combined policy | 组合后的策略
     */
    default EvictionPolicy<K, V> and(EvictionPolicy<K, V> other) {
        return new CompositeEvictionPolicy<>(this, other, CompositeEvictionPolicy.Mode.AND);
    }

    /**
     * Create a weighted composite of multiple policies
     * 创建多个策略的加权组合
     *
     * <p>Scores each candidate by weighted vote from each policy.</p>
     * <p>通过每个策略的加权投票对每个候选评分。</p>
     *
     * @param policies policies with weights | 带权重的策略
     * @param <K>      key type | 键类型
     * @param <V>      value type | 值类型
     * @return weighted composite policy | 加权组合策略
     */
    @SafeVarargs
    static <K, V> EvictionPolicy<K, V> weighted(WeightedPolicy<K, V>... policies) {
        return new WeightedEvictionPolicy<>(java.util.List.of(policies));
    }

    /**
     * Weighted policy wrapper
     * 加权策略包装器
     *
     * @param policy the eviction policy | 淘汰策略
     * @param weight the weight for this policy | 此策略的权重
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     */
    record WeightedPolicy<K, V>(EvictionPolicy<K, V> policy, double weight) {
        /**
         * Creates a weighted policy | 创建加权策略
         *
         * @param <K> the key type | 键类型
         * @param <V> the value type | 值类型
         * @param policy the eviction policy | 淘汰策略
         * @param weight the weight | 权重
         * @return the weighted policy | 加权策略
         */
        public static <K, V> WeightedPolicy<K, V> of(EvictionPolicy<K, V> policy, double weight) {
            return new WeightedPolicy<>(policy, weight);
        }
    }
}

/**
 * Composite eviction policy for OR/AND composition
 * OR/AND 组合的组合淘汰策略
 */
class CompositeEvictionPolicy<K, V> implements EvictionPolicy<K, V> {
    enum Mode { OR, AND }

    private final EvictionPolicy<K, V> first;
    private final EvictionPolicy<K, V> second;
    private final Mode mode;

    CompositeEvictionPolicy(EvictionPolicy<K, V> first, EvictionPolicy<K, V> second, Mode mode) {
        this.first = first;
        this.second = second;
        this.mode = mode;
    }

    @Override
    public void recordAccess(CacheEntry<K, V> entry) {
        first.recordAccess(entry);
        second.recordAccess(entry);
    }

    @Override
    public void recordWrite(CacheEntry<K, V> entry) {
        first.recordWrite(entry);
        second.recordWrite(entry);
    }

    @Override
    public Optional<K> selectVictim(Map<K, CacheEntry<K, V>> entries) {
        Optional<K> firstVictim = first.selectVictim(entries);
        Optional<K> secondVictim = second.selectVictim(entries);

        return switch (mode) {
            case OR -> firstVictim.or(() -> secondVictim);
            case AND -> {
                if (firstVictim.isPresent() && secondVictim.isPresent()
                        && firstVictim.get().equals(secondVictim.get())) {
                    yield firstVictim;
                }
                yield Optional.empty();
            }
        };
    }

    @Override
    public void onRemoval(K key) {
        first.onRemoval(key);
        second.onRemoval(key);
    }

    @Override
    public void reset() {
        first.reset();
        second.reset();
    }
}

/**
 * Weighted eviction policy combining multiple policies with scores
 * 结合多个策略评分的加权淘汰策略
 */
class WeightedEvictionPolicy<K, V> implements EvictionPolicy<K, V> {
    private final java.util.List<EvictionPolicy.WeightedPolicy<K, V>> policies;

    WeightedEvictionPolicy(java.util.List<EvictionPolicy.WeightedPolicy<K, V>> policies) {
        this.policies = policies;
    }

    @Override
    public void recordAccess(CacheEntry<K, V> entry) {
        policies.forEach(wp -> wp.policy().recordAccess(entry));
    }

    @Override
    public void recordWrite(CacheEntry<K, V> entry) {
        policies.forEach(wp -> wp.policy().recordWrite(entry));
    }

    @Override
    public Optional<K> selectVictim(Map<K, CacheEntry<K, V>> entries) {
        // Collect votes from each policy with weights
        Map<K, Double> scores = new java.util.HashMap<>();

        for (var wp : policies) {
            wp.policy().selectVictim(entries).ifPresent(victim ->
                    scores.merge(victim, wp.weight(), Double::sum));
        }

        // Return the key with highest weighted score
        return scores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }

    @Override
    public void onRemoval(K key) {
        policies.forEach(wp -> wp.policy().onRemoval(key));
    }

    @Override
    public void reset() {
        policies.forEach(wp -> wp.policy().reset());
    }
}
