package cloud.opencode.base.cache.bulk;

import cloud.opencode.base.cache.Cache;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Bulk Operations - Enhanced bulk operations for cache
 * 批量操作 - 缓存的增强批量操作
 *
 * <p>Provides advanced bulk operations with atomic semantics,
 * conditional updates, and batch processing.</p>
 * <p>提供具有原子语义、条件更新和批处理的高级批量操作。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Atomic compare-and-swap-all - 原子比较并交换全部</li>
 *   <li>Conditional bulk put - 条件批量放置</li>
 *   <li>Batch processing with callback - 带回调的批处理</li>
 *   <li>Parallel batch operations - 并行批量操作</li>
 *   <li>Bulk compute operations - 批量计算操作</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * BulkOperations<String, User> bulk = BulkOperations.on(cache);
 *
 * // Bulk put all - best-effort rollback on failure
 * boolean success = bulk.bulkPutAll(users);
 *
 * // Conditional put - only if all keys present
 * boolean success = bulk.putAllIfAllPresent(updates);
 *
 * // Batch process with callback
 * bulk.batchProcess(keys, 100, batch -> {
 *     // Process each batch of 100
 * });
 *
 * // Compute all values
 * bulk.computeAll(keys, (key, oldValue) -> newValue);
 * }</pre>
 *
 * @param <K> key type | 键类型
 * @param <V> value type | 值类型
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (delegates to thread-safe cache) - 线程安全: 是（委托给线程安全的缓存）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V2.0.5
 */
public class BulkOperations<K, V> {

    private final Cache<K, V> cache;

    private BulkOperations(Cache<K, V> cache) {
        this.cache = Objects.requireNonNull(cache, "cache cannot be null");
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Create bulk operations for a cache
     * 为缓存创建批量操作
     *
     * @param cache the cache | 缓存
     * @param <K>   key type | 键类型
     * @param <V>   value type | 值类型
     * @return bulk operations | 批量操作
     */
    public static <K, V> BulkOperations<K, V> on(Cache<K, V> cache) {
        return new BulkOperations<>(cache);
    }

    // ==================== Atomic Operations | 原子操作 ====================

    /**
     * Bulk put all entries with best-effort rollback on failure.
     * 批量放置所有条目，失败时尽最大努力回滚。
     *
     * <p>Note: This is NOT truly atomic. Entries are put one by one and concurrent
     * readers may see partial updates. If an exception occurs mid-way, previously
     * inserted entries are rolled back on a best-effort basis.</p>
     *
     * @param entries entries to put | 要放置的条目
     * @return true if all succeeded | 如果全部成功返回 true
     */
    public boolean bulkPutAll(Map<? extends K, ? extends V> entries) {
        // Save current values for rollback
        Map<K, V> backup = new LinkedHashMap<>();
        Set<K> newKeys = new LinkedHashSet<>();

        try {
            for (Map.Entry<? extends K, ? extends V> entry : entries.entrySet()) {
                K key = entry.getKey();
                if (cache.containsKey(key)) {
                    backup.put(key, cache.get(key));
                } else {
                    newKeys.add(key);
                }
                cache.put(key, entry.getValue());
            }
            return true;
        } catch (Exception e) {
            // Rollback
            for (Map.Entry<K, V> entry : backup.entrySet()) {
                cache.put(entry.getKey(), entry.getValue());
            }
            for (K key : newKeys) {
                cache.invalidate(key);
            }
            return false;
        }
    }

    /**
     * Atomic invalidate all - either all succeed or none
     * 原子使全部无效 - 要么全部成功，要么都不
     *
     * @param keys keys to invalidate | 要使无效的键
     * @return map of invalidated entries for potential restore | 无效条目的映射以便可能恢复
     */
    public Map<K, V> atomicInvalidateAll(Iterable<? extends K> keys) {
        Map<K, V> backup = new LinkedHashMap<>();
        try {
            for (K key : keys) {
                V value = cache.get(key);
                if (value != null) {
                    backup.put(key, value);
                }
            }
            cache.invalidateAll(keys);
            return backup;
        } catch (Exception e) {
            // Restore
            cache.putAll(backup);
            throw e;
        }
    }

    // ==================== Conditional Operations | 条件操作 ====================

    /**
     * Put all only if all keys are already present
     * 仅当所有键都已存在时才放置全部
     *
     * @param entries entries to put | 要放置的条目
     * @return true if all keys present and updated | 如果所有键都存在并已更新返回 true
     */
    public boolean putAllIfAllPresent(Map<? extends K, ? extends V> entries) {
        // First check all keys exist
        for (K key : entries.keySet()) {
            if (!cache.containsKey(key)) {
                return false;
            }
        }
        // Then update all
        cache.putAll(entries);
        return true;
    }

    /**
     * Put all only if all keys are absent
     * 仅当所有键都不存在时才放置全部
     *
     * @param entries entries to put | 要放置的条目
     * @return true if all keys absent and inserted | 如果所有键都不存在并已插入返回 true
     */
    public boolean putAllIfAllAbsent(Map<? extends K, ? extends V> entries) {
        // First check all keys absent
        for (K key : entries.keySet()) {
            if (cache.containsKey(key)) {
                return false;
            }
        }
        // Then insert all
        cache.putAll(entries);
        return true;
    }

    /**
     * Put entries that are absent, skip existing
     * 放置不存在的条目，跳过已存在的
     *
     * @param entries entries to put | 要放置的条目
     * @return result with counts of inserted and skipped | 带有插入和跳过计数的结果
     */
    public BulkPutResult<K> putAllIfAbsent(Map<? extends K, ? extends V> entries) {
        Set<K> inserted = new LinkedHashSet<>();
        Set<K> skipped = new LinkedHashSet<>();

        for (Map.Entry<? extends K, ? extends V> entry : entries.entrySet()) {
            if (cache.putIfAbsent(entry.getKey(), entry.getValue())) {
                inserted.add(entry.getKey());
            } else {
                skipped.add(entry.getKey());
            }
        }

        return new BulkPutResult<>(inserted, skipped);
    }

    /**
     * Invalidate all only if all values match expected
     * 仅当所有值与预期匹配时才使全部无效
     *
     * @param expected expected key-value pairs | 预期的键值对
     * @return true if all matched and invalidated | 如果全部匹配并已使无效返回 true
     */
    public boolean invalidateAllIfMatch(Map<? extends K, ? extends V> expected) {
        // Check all values match
        for (Map.Entry<? extends K, ? extends V> entry : expected.entrySet()) {
            V current = cache.get(entry.getKey());
            if (!Objects.equals(current, entry.getValue())) {
                return false;
            }
        }
        // Invalidate all
        cache.invalidateAll(expected.keySet());
        return true;
    }

    // ==================== Batch Processing | 批处理 ====================

    /**
     * Process keys in batches
     * 分批处理键
     *
     * @param keys      keys to process | 要处理的键
     * @param batchSize batch size | 批次大小
     * @param processor batch processor | 批处理器
     * @return total processed count | 处理的总数
     */
    public int batchProcess(Iterable<? extends K> keys, int batchSize,
                           BatchProcessor<K, V> processor) {
        List<K> batch = new ArrayList<>(batchSize);
        int totalProcessed = 0;

        for (K key : keys) {
            batch.add(key);
            if (batch.size() >= batchSize) {
                Map<K, V> values = cache.getAll(batch);
                processor.process(values);
                totalProcessed += batch.size();
                batch.clear();
            }
        }

        // Process remaining
        if (!batch.isEmpty()) {
            Map<K, V> values = cache.getAll(batch);
            processor.process(values);
            totalProcessed += batch.size();
        }

        return totalProcessed;
    }

    /**
     * Process keys in parallel batches
     * 并行分批处理键
     *
     * @param keys        keys to process | 要处理的键
     * @param batchSize   batch size | 批次大小
     * @param parallelism number of parallel threads | 并行线程数
     * @param processor   batch processor | 批处理器
     * @return future completing when all batches done | 所有批次完成时完成的 Future
     */
    public CompletableFuture<BatchResult> batchProcessParallel(
            List<? extends K> keys, int batchSize, int parallelism,
            BatchProcessor<K, V> processor) {

        List<List<? extends K>> batches = new ArrayList<>();
        for (int i = 0; i < keys.size(); i += batchSize) {
            batches.add(keys.subList(i, Math.min(i + batchSize, keys.size())));
        }

        AtomicInteger processedCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        List<CompletableFuture<Void>> futures = batches.stream()
                .map(batch -> CompletableFuture.runAsync(() -> {
                    try {
                        Map<K, V> values = cache.getAll(batch);
                        processor.process(values);
                        processedCount.addAndGet(batch.size());
                    } catch (Exception e) {
                        errorCount.addAndGet(batch.size());
                    }
                }))
                .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> new BatchResult(processedCount.get(), errorCount.get(), batches.size()));
    }

    // ==================== Compute Operations | 计算操作 ====================

    /**
     * Compute values for all specified keys
     * 为所有指定的键计算值
     *
     * @param keys            keys to compute | 要计算的键
     * @param remappingFunction function to compute new value | 计算新值的函数
     * @return map of new values | 新值的映射
     */
    public Map<K, V> computeAll(Iterable<? extends K> keys,
                                BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Map<K, V> results = new LinkedHashMap<>();

        for (K key : keys) {
            V oldValue = cache.get(key);
            V newValue = remappingFunction.apply(key, oldValue);
            if (newValue != null) {
                cache.put(key, newValue);
                results.put(key, newValue);
            } else if (oldValue != null) {
                cache.invalidate(key);
            }
        }

        return results;
    }

    /**
     * Compute values for all absent keys
     * 为所有不存在的键计算值
     *
     * @param keys            keys to compute | 要计算的键
     * @param mappingFunction function to compute value | 计算值的函数
     * @return map of computed values | 计算值的映射
     */
    public Map<K, V> computeAllIfAbsent(Iterable<? extends K> keys,
                                        Function<? super K, ? extends V> mappingFunction) {
        Map<K, V> results = new LinkedHashMap<>();

        for (K key : keys) {
            if (!cache.containsKey(key)) {
                V value = mappingFunction.apply(key);
                if (value != null) {
                    cache.put(key, value);
                    results.put(key, value);
                }
            }
        }

        return results;
    }

    /**
     * Replace all values matching predicate
     * 替换所有匹配谓词的值
     *
     * @param predicate         filter for entries to replace | 要替换的条目过滤器
     * @param remappingFunction function to compute new value | 计算新值的函数
     * @return count of replaced entries | 替换的条目数
     */
    public int replaceAllMatching(java.util.function.BiPredicate<K, V> predicate,
                                  BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        int count = 0;
        for (Map.Entry<K, V> entry : cache.entries()) {
            K key = entry.getKey();
            V value = entry.getValue();
            if (predicate.test(key, value)) {
                V newValue = remappingFunction.apply(key, value);
                if (newValue != null) {
                    cache.put(key, newValue);
                    count++;
                }
            }
        }
        return count;
    }

    // ==================== Bulk Put with TTL | 带 TTL 的批量放置 ====================

    /**
     * Put all with individual TTLs
     * 放置全部并带有各自的 TTL
     *
     * @param entries entries with their TTLs | 带有 TTL 的条目
     */
    public void putAllWithTtl(Map<? extends K, ? extends TtlValue<V>> entries) {
        for (Map.Entry<? extends K, ? extends TtlValue<V>> entry : entries.entrySet()) {
            cache.putWithTtl(entry.getKey(), entry.getValue().value(), entry.getValue().ttl());
        }
    }

    // ==================== Result Classes | 结果类 ====================

    /**
     * Result of bulk put operation
     * 批量放置操作的结果
     *
     * @param inserted keys that were inserted | 插入的键
     * @param skipped  keys that were skipped | 跳过的键
     * @param <K>      key type | 键类型
     */
    public record BulkPutResult<K>(Set<K> inserted, Set<K> skipped) {
        /**
         * Returns the inserted count | 返回插入数
         *
         * @return inserted count | 插入数
         */
        public int insertedCount() {
            return inserted.size();
        }

        /**
         * Returns the skipped count | 返回跳过数
         *
         * @return skipped count | 跳过数
         */
        public int skippedCount() {
            return skipped.size();
        }

        /**
         * Returns the total count | 返回总数
         *
         * @return total count | 总数
         */
        public int totalCount() {
            return inserted.size() + skipped.size();
        }
    }

    /**
     * Result of batch processing
     * 批处理的结果
     *
     * @param processedCount number of items processed | 处理的项目数
     * @param errorCount     number of errors | 错误数
     * @param batchCount     number of batches | 批次数
     */
    public record BatchResult(int processedCount, int errorCount, int batchCount) {
        /**
         * Returns whether there were errors | 返回是否有错误
         *
         * @return true if errors occurred | 有错误返回 true
         */
        public boolean hasErrors() {
            return errorCount > 0;
        }

        /**
         * Returns the error rate | 返回错误率
         *
         * @return error rate (0.0 to 1.0) | 错误率
         */
        public double errorRate() {
            int total = processedCount + errorCount;
            return total > 0 ? (double) errorCount / total : 0;
        }
    }

    /**
     * Value with TTL for bulk put
     * 用于批量放置的带 TTL 的值
     *
     * @param value the value | 值
     * @param ttl   time to live | 存活时间
     * @param <V>   value type | 值类型
     */
    public record TtlValue<V>(V value, Duration ttl) {
        /**
         * Creates a TtlValue | 创建 TtlValue
         *
         * @param value the value | 值
         * @param ttl the time-to-live | 存活时间
         * @param <V> value type | 值类型
         * @return the TtlValue | TtlValue 实例
         */
        public static <V> TtlValue<V> of(V value, Duration ttl) {
            return new TtlValue<>(value, ttl);
        }
    }

    /**
     * Batch processor functional interface
     * 批处理器函数式接口
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     */
    @FunctionalInterface
    public interface BatchProcessor<K, V> {
        /**
         * Process a batch of entries
         * 处理一批条目
         *
         * @param batch map of key to value | 键到值的映射
         */
        void process(Map<K, V> batch);
    }
}
