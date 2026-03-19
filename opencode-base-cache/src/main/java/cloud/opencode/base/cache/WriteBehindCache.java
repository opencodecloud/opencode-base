package cloud.opencode.base.cache;

import cloud.opencode.base.cache.model.RemovalCause;
import cloud.opencode.base.cache.spi.RemovalListener;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Write-Behind Cache - Asynchronous batch persistence cache wrapper
 * 写后缓存 - 异步批量持久化缓存包装器
 *
 * <p>Implements the Write-Behind (Write-Back) caching pattern where writes are
 * collected and asynchronously persisted in batches, reducing write latency
 * and database load.</p>
 * <p>实现写后（写回）缓存模式，写入操作被收集并异步批量持久化，
 * 降低写入延迟和数据库负载。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Asynchronous batch writes - 异步批量写入</li>
 *   <li>Write coalescing (multiple writes to same key) - 写合并</li>
 *   <li>Configurable batch size and flush interval - 可配置批量大小和刷新间隔</li>
 *   <li>Retry on write failure - 写入失败重试</li>
 *   <li>Graceful shutdown with flush - 优雅关闭并刷新</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create write-behind cache - 创建写后缓存
 * Cache<String, User> cache = OpenCache.getOrCreate("users");
 *
 * WriteBehindCache<String, User> writeBehind = WriteBehindCache.builder(cache)
 *     .writer(users -> userRepository.saveAll(users))
 *     .batchSize(100)
 *     .flushInterval(Duration.ofSeconds(5))
 *     .maxRetries(3)
 *     .build();
 *
 * // Writes are batched and persisted asynchronously
 * writeBehind.put("user:1", user1);
 * writeBehind.put("user:2", user2);
 *
 * // Ensure all pending writes are flushed before shutdown
 * writeBehind.shutdown();
 * }</pre>
 *
 * @param <K> the type of keys | 键类型
 * @param <V> the type of values | 值类型
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Partial (null values not allowed) - 空值安全: 部分（不允许 null 值）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
public class WriteBehindCache<K, V> implements Cache<K, V>, AutoCloseable {

    private static final System.Logger LOG = System.getLogger(WriteBehindCache.class.getName());

    private final Cache<K, V> delegate;
    private final BatchWriter<K, V> writer;
    private final int batchSize;
    private final int maxQueueSize;
    private final Duration flushInterval;
    private final int maxRetries;
    private final Duration retryDelay;
    private final Consumer<WriteFailure<K, V>> failureHandler;

    // Write queue for pending writes - guarded by queueLock for consistency
    private final ConcurrentLinkedQueue<WriteEntry<K, V>> writeQueue = new ConcurrentLinkedQueue<>();
    private final ConcurrentHashMap<K, WriteEntry<K, V>> pendingWrites = new ConcurrentHashMap<>();
    private final AtomicLong pendingCount = new AtomicLong(0);
    private final ReentrantLock queueLock = new ReentrantLock();

    // Flush scheduler
    private final ScheduledExecutorService scheduler;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private volatile ScheduledFuture<?> flushTask;

    // Statistics
    private final AtomicLong totalWrites = new AtomicLong(0);
    private final AtomicLong batchedWrites = new AtomicLong(0);
    private final AtomicLong failedWrites = new AtomicLong(0);
    private final AtomicLong coalescedWrites = new AtomicLong(0);

    private WriteBehindCache(Builder<K, V> builder) {
        this.delegate = builder.cache;
        this.writer = builder.writer;
        this.batchSize = builder.batchSize;
        this.maxQueueSize = builder.maxQueueSize;
        this.flushInterval = builder.flushInterval;
        this.maxRetries = builder.maxRetries;
        this.retryDelay = builder.retryDelay;
        this.failureHandler = builder.failureHandler;

        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "write-behind-" + delegate.name());
            t.setDaemon(true);
            return t;
        });

        startFlushScheduler();
    }

    private void startFlushScheduler() {
        flushTask = scheduler.scheduleAtFixedRate(
                this::flushBatch,
                flushInterval.toMillis(),
                flushInterval.toMillis(),
                TimeUnit.MILLISECONDS
        );
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Create a builder for WriteBehindCache
     * 创建 WriteBehindCache 构建器
     *
     * @param cache the underlying cache | 底层缓存
     * @param <K>   key type | 键类型
     * @param <V>   value type | 值类型
     * @return builder | 构建器
     */
    public static <K, V> Builder<K, V> builder(Cache<K, V> cache) {
        return new Builder<>(cache);
    }

    // ==================== Write Operations | 写操作 ====================

    @Override
    public void put(K key, V value) {
        delegate.put(key, value);
        queueWrite(key, value, WriteType.PUT);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        delegate.putAll(map);
        for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            queueWrite(entry.getKey(), entry.getValue(), WriteType.PUT);
        }
    }

    @Override
    public boolean putIfAbsent(K key, V value) {
        boolean result = delegate.putIfAbsent(key, value);
        if (result) {
            queueWrite(key, value, WriteType.PUT);
        }
        return result;
    }

    @Override
    public void putWithTtl(K key, V value, Duration ttl) {
        delegate.putWithTtl(key, value, ttl);
        queueWrite(key, value, WriteType.PUT);
    }

    @Override
    public void putAllWithTtl(Map<? extends K, ? extends V> map, Duration ttl) {
        delegate.putAllWithTtl(map, ttl);
        for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            queueWrite(entry.getKey(), entry.getValue(), WriteType.PUT);
        }
    }

    @Override
    public boolean putIfAbsentWithTtl(K key, V value, Duration ttl) {
        boolean result = delegate.putIfAbsentWithTtl(key, value, ttl);
        if (result) {
            queueWrite(key, value, WriteType.PUT);
        }
        return result;
    }

    @Override
    public void invalidate(K key) {
        delegate.invalidate(key);
        queueWrite(key, null, WriteType.DELETE);
    }

    @Override
    public void invalidateAll(Iterable<? extends K> keys) {
        delegate.invalidateAll(keys);
        for (K key : keys) {
            queueWrite(key, null, WriteType.DELETE);
        }
    }

    @Override
    public void invalidateAll() {
        delegate.invalidateAll();
        // Clear pending writes atomically since we're clearing everything
        queueLock.lock();
        try {
            pendingWrites.clear();
            writeQueue.clear();
            pendingCount.set(0);
        } finally {
            queueLock.unlock();
        }
    }

    // ==================== Read Operations (Delegated) | 读操作（委托）====================

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
    public Collection<V> values() {
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

    // ==================== Write-Behind Specific | 写后特有方法 ====================

    /**
     * Get count of pending writes
     * 获取待写入数量
     *
     * @return pending write count | 待写入数量
     */
    public long pendingWriteCount() {
        return pendingCount.get();
    }

    /**
     * Force flush all pending writes immediately
     * 立即强制刷新所有待写入
     */
    public void flush() {
        flushBatch();
    }

    /**
     * Get write-behind statistics
     * 获取写后统计信息
     *
     * @return write-behind stats | 写后统计
     */
    public WriteBehindStats writeBehindStats() {
        return new WriteBehindStats(
                totalWrites.get(),
                batchedWrites.get(),
                failedWrites.get(),
                coalescedWrites.get(),
                pendingCount.get()
        );
    }

    /**
     * Shutdown the write-behind cache, flushing pending writes
     * 关闭写后缓存，刷新待写入
     */
    public void shutdown() {
        shutdown(Duration.ofSeconds(30));
    }

    /**
     * Shutdown with timeout
     * 带超时关闭
     *
     * @param timeout maximum wait time | 最大等待时间
     */
    public void shutdown(Duration timeout) {
        running.set(false);

        if (flushTask != null) {
            flushTask.cancel(false);
        }

        // Flush remaining writes
        flushBatch();

        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void close() {
        shutdown();
    }

    // ==================== Private Methods | 私有方法 ====================

    private void queueWrite(K key, V value, WriteType type) {
        if (!running.get()) {
            LOG.log(System.Logger.Level.WARNING,
                    "Write-behind cache is shut down, rejecting write for key: {0}", key);
            return;
        }
        totalWrites.incrementAndGet();

        WriteEntry<K, V> entry = new WriteEntry<>(key, value, type, System.currentTimeMillis());

        // Synchronize writeQueue and pendingWrites/pendingCount to prevent inconsistency
        boolean lockHeld = true;
        queueLock.lock();
        try {
            // Coalesce writes to same key
            WriteEntry<K, V> existing = pendingWrites.put(key, entry);
            if (existing != null) {
                coalescedWrites.incrementAndGet();
            } else {
                // Check queue size limit before adding
                if (pendingCount.get() >= maxQueueSize) {
                    // Remove from pendingWrites since we won't queue it
                    pendingWrites.remove(key, entry);
                    // Release lock before synchronous write to avoid blocking other writers
                    queueLock.unlock();
                    lockHeld = false;
                    LOG.log(System.Logger.Level.WARNING,
                            "Write-behind queue full ({0} entries), executing write synchronously for key: {1}",
                            maxQueueSize, key);
                    try {
                        Map<K, V> syncPuts = type == WriteType.PUT ? Map.of(key, value) : Map.of();
                        Set<K> syncDeletes = type == WriteType.DELETE ? Set.of(key) : Set.of();
                        writer.writeBatch(syncPuts, syncDeletes);
                        batchedWrites.incrementAndGet();
                    } catch (Exception e) {
                        failedWrites.incrementAndGet();
                        LOG.log(System.Logger.Level.WARNING,
                                () -> "Synchronous fallback write failed for key: " + key, e);
                    }
                    return;
                }
                writeQueue.offer(entry);
                pendingCount.incrementAndGet();
            }
        } finally {
            if (lockHeld) {
                queueLock.unlock();
            }
        }

        // Flush if batch size reached
        if (pendingCount.get() >= batchSize) {
            scheduler.execute(this::flushBatch);
        }
    }

    private void flushBatch() {
        if (!running.get() && pendingWrites.isEmpty()) {
            return;
        }

        // Collect batch - synchronize with queueWrite to prevent inconsistency
        Map<K, V> puts = new LinkedHashMap<>();
        Set<K> deletes = new LinkedHashSet<>();

        queueLock.lock();
        try {
            int count = 0;
            WriteEntry<K, V> entry;
            while (count < batchSize && (entry = writeQueue.poll()) != null) {
                // Get latest value for this key (in case of coalescing)
                WriteEntry<K, V> latest = pendingWrites.remove(entry.key);
                if (latest != null) {
                    pendingCount.decrementAndGet();
                    if (latest.type == WriteType.PUT) {
                        puts.put(latest.key, latest.value);
                    } else {
                        deletes.add(latest.key);
                    }
                    count++;
                }
            }
        } finally {
            queueLock.unlock();
        }

        if (puts.isEmpty() && deletes.isEmpty()) {
            return;
        }

        // Execute batch write with retry
        int attempt = 0;
        while (attempt < maxRetries) {
            try {
                writer.writeBatch(puts, deletes);
                batchedWrites.addAndGet(puts.size() + deletes.size());
                return;
            } catch (Exception e) {
                attempt++;
                if (attempt < maxRetries) {
                    LOG.log(System.Logger.Level.WARNING,
                            "Write-behind batch failed (attempt {0}/{1}), retrying: {2}",
                            attempt, maxRetries, e.getMessage());
                    try {
                        Thread.sleep(retryDelay.toMillis() * attempt);
                    } catch (InterruptedException ie) {
                        LOG.log(System.Logger.Level.WARNING,
                                "Write-behind retry interrupted", ie);
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    LOG.log(System.Logger.Level.ERROR,
                            "Write-behind batch failed after {0} attempts", attempt, e);
                    failedWrites.addAndGet(puts.size() + deletes.size());
                    if (failureHandler != null) {
                        failureHandler.accept(new WriteFailure<>(puts, deletes, e, attempt));
                    }
                }
            }
        }
    }

    // ==================== Inner Classes | 内部类 ====================

    private enum WriteType {
        PUT, DELETE
    }

    private record WriteEntry<K, V>(K key, V value, WriteType type, long timestamp) {
    }

    /**
     * Batch writer interface for persisting cache changes
     * 批量写入器接口，用于持久化缓存变更
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     */
    @FunctionalInterface
    public interface BatchWriter<K, V> {
        /**
         * Write batch of changes to underlying store
         * 将批量变更写入底层存储
         *
         * @param puts    entries to put/update | 要放入/更新的条目
         * @param deletes keys to delete | 要删除的键
         * @throws Exception on write failure | 写入失败时抛出异常
         */
        void writeBatch(Map<K, V> puts, Set<K> deletes) throws Exception;

        /**
         * Create writer for puts only
         * 创建仅处理放入的写入器
         *
         * @param consumer put consumer | 放入消费者
         * @param <K>      key type | 键类型
         * @param <V>      value type | 值类型
         * @return batch writer | 批量写入器
         */
        static <K, V> BatchWriter<K, V> putsOnly(Consumer<Map<K, V>> consumer) {
            return (puts, deletes) -> consumer.accept(puts);
        }

        /**
         * Create writer with separate put and delete handlers
         * 创建分别处理放入和删除的写入器
         *
         * @param putHandler    put handler | 放入处理器
         * @param deleteHandler delete handler | 删除处理器
         * @param <K>           key type | 键类型
         * @param <V>           value type | 值类型
         * @return batch writer | 批量写入器
         */
        static <K, V> BatchWriter<K, V> of(Consumer<Map<K, V>> putHandler, Consumer<Set<K>> deleteHandler) {
            return (puts, deletes) -> {
                if (!puts.isEmpty()) {
                    putHandler.accept(puts);
                }
                if (!deletes.isEmpty()) {
                    deleteHandler.accept(deletes);
                }
            };
        }
    }

    /**
     * Write failure information
     * 写入失败信息
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     */
    public record WriteFailure<K, V>(
            Map<K, V> failedPuts,
            Set<K> failedDeletes,
            Throwable cause,
            int attempts
    ) {
        /**
         * Compact constructor - make collections immutable.
         * 紧凑构造器 - 使集合不可变。
         */
        public WriteFailure {
            failedPuts = Map.copyOf(failedPuts);
            failedDeletes = Set.copyOf(failedDeletes);
        }
    }

    /**
     * Write-behind statistics
     * 写后统计信息
     */
    public record WriteBehindStats(
            long totalWrites,
            long batchedWrites,
            long failedWrites,
            long coalescedWrites,
            long pendingWrites
    ) {
        /**
         * Get coalescing ratio (how many writes were merged)
         * 获取合并比率（多少写入被合并）
         *
         * @return coalescing ratio | 合并比率
         */
        public double coalescingRatio() {
            return totalWrites == 0 ? 0 : (double) coalescedWrites / totalWrites;
        }

        /**
         * Get success ratio
         * 获取成功比率
         *
         * @return success ratio | 成功比率
         */
        public double successRatio() {
            long total = batchedWrites + failedWrites;
            return total == 0 ? 1.0 : (double) batchedWrites / total;
        }
    }

    /**
     * Builder for WriteBehindCache
     * WriteBehindCache 构建器
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     */
    public static class Builder<K, V> {
        private final Cache<K, V> cache;
        private BatchWriter<K, V> writer;
        private int batchSize = 100;
        private int maxQueueSize = 10_000;
        private Duration flushInterval = Duration.ofSeconds(5);
        private int maxRetries = 3;
        private Duration retryDelay = Duration.ofMillis(100);
        private Consumer<WriteFailure<K, V>> failureHandler;

        private Builder(Cache<K, V> cache) {
            this.cache = Objects.requireNonNull(cache, "cache must not be null");
        }

        /**
         * Set the batch writer
         * 设置批量写入器
         *
         * @param writer batch writer | 批量写入器
         * @return this builder | 此构建器
         */
        public Builder<K, V> writer(BatchWriter<K, V> writer) {
            this.writer = writer;
            return this;
        }

        /**
         * Set writer using simple consumer (puts only)
         * 使用简单消费者设置写入器（仅放入）
         *
         * @param consumer put consumer | 放入消费者
         * @return this builder | 此构建器
         */
        public Builder<K, V> writer(Consumer<Map<K, V>> consumer) {
            this.writer = BatchWriter.putsOnly(consumer);
            return this;
        }

        /**
         * Set batch size before auto-flush
         * 设置自动刷新前的批量大小
         *
         * @param batchSize batch size | 批量大小
         * @return this builder | 此构建器
         */
        public Builder<K, V> batchSize(int batchSize) {
            this.batchSize = batchSize;
            return this;
        }

        /**
         * Set maximum queue size before synchronous fallback
         * 设置同步回退前的最大队列大小
         *
         * @param maxQueueSize max queue size (default 10000) | 最大队列大小（默认10000）
         * @return this builder | 此构建器
         */
        public Builder<K, V> maxQueueSize(int maxQueueSize) {
            this.maxQueueSize = maxQueueSize;
            return this;
        }

        /**
         * Set flush interval
         * 设置刷新间隔
         *
         * @param interval flush interval | 刷新间隔
         * @return this builder | 此构建器
         */
        public Builder<K, V> flushInterval(Duration interval) {
            this.flushInterval = interval;
            return this;
        }

        /**
         * Set maximum retry attempts on failure
         * 设置失败时的最大重试次数
         *
         * @param maxRetries max retries | 最大重试次数
         * @return this builder | 此构建器
         */
        public Builder<K, V> maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        /**
         * Set delay between retries
         * 设置重试间隔
         *
         * @param delay retry delay | 重试间隔
         * @return this builder | 此构建器
         */
        public Builder<K, V> retryDelay(Duration delay) {
            this.retryDelay = delay;
            return this;
        }

        /**
         * Set failure handler for write failures after all retries
         * 设置所有重试后写入失败的处理器
         *
         * @param handler failure handler | 失败处理器
         * @return this builder | 此构建器
         */
        public Builder<K, V> onFailure(Consumer<WriteFailure<K, V>> handler) {
            this.failureHandler = handler;
            return this;
        }

        /**
         * Build the write-behind cache
         * 构建写后缓存
         *
         * @return write-behind cache | 写后缓存
         */
        public WriteBehindCache<K, V> build() {
            Objects.requireNonNull(writer, "writer must not be null");
            return new WriteBehindCache<>(this);
        }
    }
}
