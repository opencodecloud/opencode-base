package cloud.opencode.base.cache;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Write-Through Cache - Synchronous write to both cache and backend
 * 写穿透缓存 - 同步写入缓存和后端存储
 *
 * <p>Ensures data consistency by writing to backend storage synchronously
 * before updating the cache. If backend write fails, cache is not updated.</p>
 * <p>通过同步写入后端存储来确保数据一致性。如果后端写入失败，缓存不会更新。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Synchronous backend write - 同步后端写入</li>
 *   <li>Strong consistency guarantee - 强一致性保证</li>
 *   <li>Automatic rollback on failure - 失败时自动回滚</li>
 *   <li>Batch write support - 批量写入支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create write-through cache
 * WriteThroughCache<String, User> cache = WriteThroughCache.wrap(baseCache)
 *     .writer(user -> userRepository.save(user))
 *     .deleter(key -> userRepository.deleteById(key))
 *     .build();
 *
 * // Put - writes to DB first, then cache
 * cache.put("user:1001", user);  // DB write happens synchronously
 *
 * // If DB write fails, cache is not updated
 * }</pre>
 *
 * <p><strong>Comparison with Write-Behind | 与写后缓存对比:</strong></p>
 * <ul>
 *   <li>Write-Through: Sync write, strong consistency, higher latency</li>
 *   <li>Write-Behind: Async write, eventual consistency, lower latency</li>
 * </ul>
 *
 * @param <K> the type of keys | 键类型
 * @param <V> the type of values | 值类型
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (delegates to thread-safe cache) - 线程安全: 是（委托给线程安全的缓存）</li>
 *   <li>Null-safe: Partial (null values not allowed) - 空值安全: 部分（不允许 null 值）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see WriteBehindCache
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V2.0.4
 */
public class WriteThroughCache<K, V> implements Cache<K, V> {

    private static final System.Logger LOGGER = System.getLogger(WriteThroughCache.class.getName());

    private final Cache<K, V> delegate;
    private final CacheWriter<K, V> writer;
    private final java.util.function.Consumer<K> deleter;
    private final java.util.function.BiConsumer<Iterable<K>, Throwable> errorHandler;

    private WriteThroughCache(Builder<K, V> builder) {
        this.delegate = builder.delegate;
        this.writer = builder.writer;
        this.deleter = builder.deleter;
        this.errorHandler = builder.errorHandler;
    }

    /**
     * Wrap an existing cache with write-through behavior
     * 用写穿透行为包装现有缓存
     *
     * @param cache the cache to wrap | 要包装的缓存
     * @param <K>   key type | 键类型
     * @param <V>   value type | 值类型
     * @return builder | 构建器
     */
    public static <K, V> Builder<K, V> wrap(Cache<K, V> cache) {
        return new Builder<>(cache);
    }

    @Override
    public V get(K key) {
        return delegate.get(key);
    }

    @Override
    public V get(K key, Function<? super K, ? extends V> loader) {
        return delegate.get(key, loader);
    }

    @Override
    public Map<K, V> getAll(Iterable<? extends K> keys) {
        return delegate.getAll(keys);
    }

    @Override
    public Map<K, V> getAll(Iterable<? extends K> keys,
                           Function<? super Set<? extends K>, ? extends Map<K, V>> loader) {
        return delegate.getAll(keys, loader);
    }

    @Override
    public void put(K key, V value) {
        // Write to backend first
        try {
            writer.write(key, value);
        } catch (Exception e) {
            handleWriteError(java.util.Collections.singleton(key), e);
            throw new CacheWriteException("Failed to write-through for key: " + key, e);
        }

        // Then update cache - if this fails, backend is already written (data inconsistency)
        try {
            delegate.put(key, value);
        } catch (Exception e) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "Cache update failed after backend write succeeded for key: " + key +
                    ". Data inconsistency possible.", e);
            throw new CacheWriteException("Failed to update cache after backend write for key: " + key, e);
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        @SuppressWarnings("unchecked")
        Iterable<K> keys = (Iterable<K>) map.keySet();

        // Write all to backend first
        try {
            writer.writeAll(map);
        } catch (Exception e) {
            handleWriteError(keys, e);
            throw new CacheWriteException("Failed to write-through for batch", e);
        }

        // Then update cache - if this fails, backend is already written
        try {
            delegate.putAll(map);
        } catch (Exception e) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "Cache batch update failed after backend write succeeded. Data inconsistency possible.", e);
            throw new CacheWriteException("Failed to update cache after backend write for batch", e);
        }
    }

    @Override
    public boolean putIfAbsent(K key, V value) {
        // Atomically check-and-insert in delegate first to avoid TOCTOU race
        boolean inserted = delegate.putIfAbsent(key, value);
        if (!inserted) {
            return false;
        }
        try {
            writer.write(key, value);
            return true;
        } catch (Exception e) {
            // Rollback cache on backend write failure
            delegate.invalidate(key);
            handleWriteError(java.util.Collections.singleton(key), e);
            throw new CacheWriteException("Failed to write-through for key: " + key, e);
        }
    }

    @Override
    public void putWithTtl(K key, V value, Duration ttl) {
        // Write to backend first
        try {
            writer.write(key, value);
        } catch (Exception e) {
            handleWriteError(java.util.Collections.singleton(key), e);
            throw new CacheWriteException("Failed to write-through for key: " + key, e);
        }

        // Then update cache with TTL
        try {
            delegate.putWithTtl(key, value, ttl);
        } catch (Exception e) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "Cache update with TTL failed after backend write succeeded for key: " + key +
                    ". Data inconsistency possible.", e);
            throw new CacheWriteException("Failed to update cache after backend write for key: " + key, e);
        }
    }

    @Override
    public void putAllWithTtl(Map<? extends K, ? extends V> map, Duration ttl) {
        @SuppressWarnings("unchecked")
        Iterable<K> keys = (Iterable<K>) map.keySet();

        // Write all to backend first
        try {
            writer.writeAll(map);
        } catch (Exception e) {
            handleWriteError(keys, e);
            throw new CacheWriteException("Failed to write-through for batch", e);
        }

        // Then update cache with TTL
        try {
            delegate.putAllWithTtl(map, ttl);
        } catch (Exception e) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "Cache batch update with TTL failed after backend write succeeded. Data inconsistency possible.", e);
            throw new CacheWriteException("Failed to update cache after backend write for batch", e);
        }
    }

    @Override
    public boolean putIfAbsentWithTtl(K key, V value, Duration ttl) {
        // Atomically check-and-insert in delegate first to avoid TOCTOU race
        boolean inserted = delegate.putIfAbsentWithTtl(key, value, ttl);
        if (!inserted) {
            return false;
        }
        try {
            writer.write(key, value);
            return true;
        } catch (Exception e) {
            // Rollback cache on backend write failure
            delegate.invalidate(key);
            handleWriteError(java.util.Collections.singleton(key), e);
            throw new CacheWriteException("Failed to write-through for key: " + key, e);
        }
    }

    @Override
    public void invalidate(K key) {
        try {
            if (deleter != null) {
                deleter.accept(key);
            }
            delegate.invalidate(key);
        } catch (Exception e) {
            handleWriteError(java.util.Collections.singleton(key), e);
            throw new CacheWriteException("Failed to delete-through for key: " + key, e);
        }
    }

    @Override
    public void invalidateAll(Iterable<? extends K> keys) {
        try {
            if (deleter != null) {
                for (K key : keys) {
                    deleter.accept(key);
                }
            }
            delegate.invalidateAll(keys);
        } catch (Exception e) {
            @SuppressWarnings("unchecked")
            Iterable<K> keyList = (Iterable<K>) keys;
            handleWriteError(keyList, e);
            throw new CacheWriteException("Failed to delete-through for batch", e);
        }
    }

    @Override
    public void invalidateAll() {
        delegate.invalidateAll();
    }

    private void handleWriteError(Iterable<K> keys, Throwable e) {
        if (errorHandler != null) {
            try {
                errorHandler.accept(keys, e);
            } catch (Exception ignored) {
                // Ignore error handler exceptions
            }
        }
    }

    // ==================== Delegate Methods ====================

    @Override
    public boolean containsKey(K key) {
        return delegate.containsKey(key);
    }

    @Override
    public long size() {
        return delegate.size();
    }

    @Override
    public long estimatedSize() {
        return delegate.estimatedSize();
    }

    @Override
    public Set<K> keys() {
        return delegate.keys();
    }

    @Override
    public java.util.Collection<V> values() {
        return delegate.values();
    }

    @Override
    public Set<Map.Entry<K, V>> entries() {
        return delegate.entries();
    }

    @Override
    public ConcurrentMap<K, V> asMap() {
        return delegate.asMap();
    }

    @Override
    public CacheStats stats() {
        return delegate.stats();
    }

    @Override
    public CacheMetrics metrics() {
        return delegate.metrics();
    }

    @Override
    public void cleanUp() {
        delegate.cleanUp();
    }

    @Override
    public AsyncCache<K, V> async() {
        return delegate.async();
    }

    @Override
    public String name() {
        return delegate.name();
    }

    // ==================== Builder ====================

    /**
     * Builder for WriteThroughCache
     * WriteThroughCache 构建器
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     */
    public static class Builder<K, V> {
        private final Cache<K, V> delegate;
        private CacheWriter<K, V> writer;
        private java.util.function.Consumer<K> deleter;
        private java.util.function.BiConsumer<Iterable<K>, Throwable> errorHandler;

        Builder(Cache<K, V> delegate) {
            this.delegate = Objects.requireNonNull(delegate, "delegate cannot be null");
        }

        /**
         * Set the cache writer for single entry writes
         * 设置单条目写入器
         *
         * @param writer the writer function | 写入函数
         * @return this builder | 此构建器
         */
        public Builder<K, V> writer(java.util.function.BiConsumer<K, V> writer) {
            Objects.requireNonNull(writer, "writer cannot be null");
            this.writer = new CacheWriter<K, V>() {
                @Override
                public void write(K key, V value) {
                    writer.accept(key, value);
                }
            };
            return this;
        }

        /**
         * Set the cache writer with full control
         * 设置完整控制的缓存写入器
         *
         * @param writer the cache writer | 缓存写入器
         * @return this builder | 此构建器
         */
        public Builder<K, V> writer(CacheWriter<K, V> writer) {
            this.writer = Objects.requireNonNull(writer, "writer cannot be null");
            return this;
        }

        /**
         * Set the deleter for cache invalidation
         * 设置删除器用于缓存失效
         *
         * @param deleter the deleter function | 删除函数
         * @return this builder | 此构建器
         */
        public Builder<K, V> deleter(java.util.function.Consumer<K> deleter) {
            this.deleter = deleter;
            return this;
        }

        /**
         * Set error handler for write failures
         * 设置写入失败的错误处理器
         *
         * @param errorHandler the error handler | 错误处理器
         * @return this builder | 此构建器
         */
        public Builder<K, V> onError(java.util.function.BiConsumer<Iterable<K>, Throwable> errorHandler) {
            this.errorHandler = errorHandler;
            return this;
        }

        /**
         * Build the write-through cache
         * 构建写穿透缓存
         *
         * @return write-through cache | 写穿透缓存
         */
        public WriteThroughCache<K, V> build() {
            if (writer == null) {
                throw new IllegalStateException("writer must be set");
            }
            return new WriteThroughCache<>(this);
        }
    }

    // ==================== Cache Writer Interface ====================

    /**
     * Cache Writer - Writes cache entries to backend storage
     * 缓存写入器 - 将缓存条目写入后端存储
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     */
    @FunctionalInterface
    public interface CacheWriter<K, V> {

        /**
         * Write a single entry to backend
         * 写入单个条目到后端
         *
         * @param key   the key | 键
         * @param value the value | 值
         * @throws Exception if write fails | 写入失败时抛出异常
         */
        void write(K key, V value) throws Exception;

        /**
         * Write multiple entries to backend (default: write one by one)
         * 写入多个条目到后端（默认：逐个写入）
         *
         * @param entries the entries | 条目集合
         * @throws Exception if write fails | 写入失败时抛出异常
         */
        default void writeAll(Map<? extends K, ? extends V> entries) throws Exception {
            for (Map.Entry<? extends K, ? extends V> entry : entries.entrySet()) {
                write(entry.getKey(), entry.getValue());
            }
        }
    }

    // ==================== Exception ====================

    /**
     * Exception thrown when cache write-through fails
     * 缓存写穿透失败时抛出的异常
     */
    public static class CacheWriteException extends RuntimeException {
        public CacheWriteException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
