package cloud.opencode.base.cache.write;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Write Coalescer - Batches and coalesces write operations
 * 写合并器 - 批处理和合并写操作
 *
 * <p>Aggregates multiple write operations into batches for efficient
 * backend persistence. Supports deduplication, batching by time/count,
 * and async write queuing.</p>
 * <p>将多个写操作聚合为批次以进行高效的后端持久化。
 * 支持去重、按时间/计数批处理和异步写队列。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Batch write aggregation - 批量写聚合</li>
 *   <li>Write deduplication - 写去重</li>
 *   <li>Time-based flushing - 基于时间的刷新</li>
 *   <li>Count-based flushing - 基于计数的刷新</li>
 *   <li>Async write queue - 异步写队列</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create write coalescer
 * WriteCoalescer<String, User> coalescer = WriteCoalescer.<String, User>builder()
 *     .batchSize(100)
 *     .flushInterval(Duration.ofSeconds(5))
 *     .writer(batch -> userRepository.saveAll(batch))
 *     .build();
 *
 * // Queue writes
 * coalescer.write("user:1001", user1);
 * coalescer.write("user:1002", user2);
 * coalescer.write("user:1001", user1Updated);  // Deduplicates
 *
 * // Flush manually if needed
 * coalescer.flush();
 *
 * // Close when done
 * coalescer.close();
 * }</pre>
 *
 * @param <K> key type | 键类型
 * @param <V> value type | 值类型
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Partial (null values not allowed) - 空值安全: 部分（不允许 null 值）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V2.0.5
 */
public class WriteCoalescer<K, V> implements AutoCloseable {

    private final ConcurrentHashMap<K, WriteEntry<V>> pendingWrites = new ConcurrentHashMap<>();
    private final BatchWriter<K, V> writer;
    private final int batchSize;
    private final Duration flushInterval;
    private final boolean deduplicateWrites;
    private final WriteErrorHandler<K, V> errorHandler;

    private final ScheduledExecutorService scheduler;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    // Statistics
    private final AtomicLong totalWrites = new AtomicLong(0);
    private final AtomicLong totalFlushes = new AtomicLong(0);
    private final AtomicLong totalBatched = new AtomicLong(0);
    private final AtomicLong totalDeduplicated = new AtomicLong(0);
    private final AtomicLong totalErrors = new AtomicLong(0);

    private WriteCoalescer(BatchWriter<K, V> writer, int batchSize, Duration flushInterval,
                          boolean deduplicateWrites, WriteErrorHandler<K, V> errorHandler) {
        this.writer = writer;
        this.batchSize = batchSize;
        this.flushInterval = flushInterval;
        this.deduplicateWrites = deduplicateWrites;
        this.errorHandler = errorHandler;

        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "WriteCoalescer-Scheduler");
            t.setDaemon(true);
            return t;
        });

        // Schedule periodic flush
        if (flushInterval != null && !flushInterval.isZero()) {
            scheduler.scheduleAtFixedRate(
                    this::flushIfNeeded,
                    flushInterval.toMillis(),
                    flushInterval.toMillis(),
                    TimeUnit.MILLISECONDS
            );
        }
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Create a builder
     * 创建构建器
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return builder | 构建器
     */
    public static <K, V> Builder<K, V> builder() {
        return new Builder<>();
    }

    // ==================== Write Operations | 写操作 ====================

    /**
     * Queue a write operation
     * 队列写操作
     *
     * @param key   the key | 键
     * @param value the value | 值
     */
    public void write(K key, V value) {
        if (closed.get()) {
            throw new IllegalStateException("WriteCoalescer is closed");
        }

        totalWrites.incrementAndGet();

        if (deduplicateWrites) {
            WriteEntry<V> existing = pendingWrites.put(key, new WriteEntry<>(value, System.currentTimeMillis()));
            if (existing != null) {
                totalDeduplicated.incrementAndGet();
            }
        } else {
            pendingWrites.put(key, new WriteEntry<>(value, System.currentTimeMillis()));
        }

        // Check if batch size reached
        if (pendingWrites.size() >= batchSize) {
            flush();
        }
    }

    /**
     * Queue multiple write operations
     * 队列多个写操作
     *
     * @param entries entries to write | 要写的条目
     */
    public void writeAll(Map<? extends K, ? extends V> entries) {
        for (Map.Entry<? extends K, ? extends V> entry : entries.entrySet()) {
            write(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Queue a delete operation
     * 队列删除操作
     *
     * @param key the key to delete | 要删除的键
     */
    public void delete(K key) {
        Objects.requireNonNull(key, "key cannot be null");
        write(key, null);  // null indicates delete
    }

    // ==================== Flush Operations | 刷新操作 ====================

    /**
     * Flush all pending writes
     * 刷新所有待处理的写操作
     *
     * @return number of entries flushed | 刷新的条目数
     */
    public synchronized int flush() {
        if (pendingWrites.isEmpty()) {
            return 0;
        }

        Map<K, V> batch = new LinkedHashMap<>();
        Set<K> keys = new HashSet<>(pendingWrites.keySet());

        for (K key : keys) {
            WriteEntry<V> entry = pendingWrites.remove(key);
            if (entry != null) {
                batch.put(key, entry.value());
            }
        }

        if (batch.isEmpty()) {
            return 0;
        }

        try {
            writer.writeBatch(batch);
            totalFlushes.incrementAndGet();
            totalBatched.addAndGet(batch.size());
            return batch.size();
        } catch (Exception e) {
            totalErrors.addAndGet(batch.size());
            errorHandler.handleError(batch, e);
            return 0;
        }
    }

    /**
     * Flush if interval has passed since last flush
     * 如果自上次刷新以来已过间隔则刷新
     */
    private void flushIfNeeded() {
        if (!pendingWrites.isEmpty()) {
            flush();
        }
    }

    // ==================== Query Operations | 查询操作 ====================

    /**
     * Get pending write for a key
     * 获取键的待处理写操作
     *
     * @param key the key | 键
     * @return pending value or null | 待处理的值或 null
     */
    public V getPending(K key) {
        WriteEntry<V> entry = pendingWrites.get(key);
        return entry != null ? entry.value() : null;
    }

    /**
     * Check if key has pending write
     * 检查键是否有待处理的写操作
     *
     * @param key the key | 键
     * @return true if pending | 如果待处理返回 true
     */
    public boolean hasPending(K key) {
        return pendingWrites.containsKey(key);
    }

    /**
     * Get all pending writes
     * 获取所有待处理的写操作
     *
     * @return map of pending writes | 待处理写操作的映射
     */
    public Map<K, V> getAllPending() {
        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, WriteEntry<V>> entry : pendingWrites.entrySet()) {
            result.put(entry.getKey(), entry.getValue().value());
        }
        return result;
    }

    /**
     * Get number of pending writes
     * 获取待处理写操作的数量
     *
     * @return pending count | 待处理计数
     */
    public int pendingCount() {
        return pendingWrites.size();
    }

    // ==================== Statistics | 统计 ====================

    /**
     * Get coalescer statistics
     * 获取合并器统计
     *
     * @return statistics | 统计
     */
    public CoalescerStats getStats() {
        return new CoalescerStats(
                totalWrites.get(),
                totalFlushes.get(),
                totalBatched.get(),
                totalDeduplicated.get(),
                totalErrors.get(),
                pendingWrites.size()
        );
    }

    /**
     * Reset statistics
     * 重置统计
     */
    public void resetStats() {
        totalWrites.set(0);
        totalFlushes.set(0);
        totalBatched.set(0);
        totalDeduplicated.set(0);
        totalErrors.set(0);
    }

    // ==================== Lifecycle | 生命周期 ====================

    /**
     * Close the coalescer, flushing pending writes
     * 关闭合并器，刷新待处理的写操作
     */
    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            scheduler.shutdown();
            try {
                flush();  // Final flush
                if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Check if coalescer is closed
     * 检查合并器是否已关闭
     *
     * @return true if closed | 如果已关闭返回 true
     */
    public boolean isClosed() {
        return closed.get();
    }

    // ==================== Inner Classes | 内部类 ====================

    private record WriteEntry<V>(V value, long timestamp) {}

    /**
     * Coalescer statistics
     * 合并器统计
     */
    public record CoalescerStats(
            long totalWrites,
            long totalFlushes,
            long totalBatched,
            long totalDeduplicated,
            long totalErrors,
            int currentPending
    ) {
        /**
         * Get deduplication rate
         * 获取去重率
         *
         * @return deduplication rate | 去重率
         */
        public double deduplicationRate() {
            return totalWrites > 0 ? (double) totalDeduplicated / totalWrites : 0;
        }

        /**
         * Get average batch size
         * 获取平均批次大小
         *
         * @return average batch size | 平均批次大小
         */
        public double averageBatchSize() {
            return totalFlushes > 0 ? (double) totalBatched / totalFlushes : 0;
        }

        /**
         * Get error rate
         * 获取错误率
         *
         * @return error rate | 错误率
         */
        public double errorRate() {
            long total = totalBatched + totalErrors;
            return total > 0 ? (double) totalErrors / total : 0;
        }
    }

    // ==================== Functional Interfaces | 函数式接口 ====================

    /**
     * Batch writer interface
     * 批量写入器接口
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     */
    @FunctionalInterface
    public interface BatchWriter<K, V> {
        /**
         * Write a batch of entries
         * 写入一批条目
         *
         * @param batch entries to write (null value = delete) | 要写的条目（null 值 = 删除）
         * @throws Exception if write fails | 写入失败时抛出
         */
        void writeBatch(Map<K, V> batch) throws Exception;
    }

    /**
     * Write error handler
     * 写错误处理器
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     */
    @FunctionalInterface
    public interface WriteErrorHandler<K, V> {
        /**
         * Handle write error
         * 处理写错误
         *
         * @param failedBatch the failed batch | 失败的批次
         * @param error       the error | 错误
         */
        void handleError(Map<K, V> failedBatch, Exception error);

        /**
         * Default handler that logs errors
         * 记录错误的默认处理器
         *
         * @param <K> key type | 键类型
         * @param <V> value type | 值类型
         * @return error handler | 错误处理器
         */
        static <K, V> WriteErrorHandler<K, V> logAndDiscard() {
            System.Logger logger = System.getLogger(WriteCoalescer.class.getName());
            return (batch, error) -> {
                logger.log(System.Logger.Level.WARNING,
                        "Write coalescer batch failed ({0} entries): {1}", batch.size(), error.getMessage(), error);
            };
        }

        /**
         * Handler that rethrows errors
         * 重新抛出错误的处理器
         *
         * @param <K> key type | 键类型
         * @param <V> value type | 值类型
         * @return error handler | 错误处理器
         */
        static <K, V> WriteErrorHandler<K, V> rethrow() {
            return (batch, error) -> {
                if (error instanceof RuntimeException re) {
                    throw re;
                }
                throw new RuntimeException("Batch write failed", error);
            };
        }
    }

    // ==================== Builder | 构建器 ====================

    /**
     * Builder for WriteCoalescer
     * WriteCoalescer 构建器
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     */
    public static class Builder<K, V> {
        private BatchWriter<K, V> writer;
        private int batchSize = 100;
        private Duration flushInterval = Duration.ofSeconds(5);
        private boolean deduplicateWrites = true;
        private WriteErrorHandler<K, V> errorHandler = WriteErrorHandler.logAndDiscard();

        /**
         * Set batch writer
         * 设置批量写入器
         *
         * @param writer the batch writer | 批量写入器
         * @return this builder | 此构建器
         */
        public Builder<K, V> writer(BatchWriter<K, V> writer) {
            this.writer = writer;
            return this;
        }

        /**
         * Set batch writer from simple consumer
         * 从简单消费者设置批量写入器
         *
         * @param consumer the consumer | 消费者
         * @return this builder | 此构建器
         */
        public Builder<K, V> writer(Consumer<Map<K, V>> consumer) {
            this.writer = consumer::accept;
            return this;
        }

        /**
         * Set batch size
         * 设置批次大小
         *
         * @param size batch size | 批次大小
         * @return this builder | 此构建器
         */
        public Builder<K, V> batchSize(int size) {
            this.batchSize = size;
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
         * Enable/disable write deduplication
         * 启用/禁用写去重
         *
         * @param deduplicate true to deduplicate | true 表示去重
         * @return this builder | 此构建器
         */
        public Builder<K, V> deduplicateWrites(boolean deduplicate) {
            this.deduplicateWrites = deduplicate;
            return this;
        }

        /**
         * Set error handler
         * 设置错误处理器
         *
         * @param handler the error handler | 错误处理器
         * @return this builder | 此构建器
         */
        public Builder<K, V> errorHandler(WriteErrorHandler<K, V> handler) {
            this.errorHandler = handler;
            return this;
        }

        /**
         * Build the write coalescer
         * 构建写合并器
         *
         * @return write coalescer | 写合并器
         */
        public WriteCoalescer<K, V> build() {
            Objects.requireNonNull(writer, "writer cannot be null");
            return new WriteCoalescer<>(writer, batchSize, flushInterval, deduplicateWrites, errorHandler);
        }
    }
}
