package cloud.opencode.base.observability;

import cloud.opencode.base.observability.exception.ObservabilityException;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Bounded slow-operation log collector, inspired by Redis SLOWLOG.
 * 有界慢操作日志收集器，灵感来自 Redis SLOWLOG。
 *
 * <p>Records operations whose execution time exceeds a configurable threshold.
 * Maintains a bounded, thread-safe log for diagnostics and performance tuning.
 * When the buffer is full, the oldest entry is evicted automatically.</p>
 * <p>记录执行时间超过可配置阈值的操作。维护有界、线程安全的日志，用于诊断和性能调优。
 * 当缓冲区满时，自动驱逐最旧的条目。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Configurable slow operation threshold - 可配置的慢操作阈值</li>
 *   <li>Bounded log buffer with configurable max entries - 可配置最大条目数的有界日志缓冲区</li>
 *   <li>Thread-safe concurrent collection - 线程安全的并发收集</li>
 *   <li>Statistical aggregation of slow operations - 慢操作的统计聚合</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Default: 10ms threshold, 1024 max entries
 * SlowLogCollector collector = SlowLogCollector.create();
 *
 * // Custom threshold
 * SlowLogCollector collector = SlowLogCollector.create(Duration.ofMillis(50));
 *
 * // Record a slow operation
 * collector.record("GET", "user:123", Duration.ofMillis(75));
 *
 * // Query
 * List<SlowLogCollector.Entry> recent = collector.getEntries(10);
 * SlowLogCollector.Stats stats = collector.stats();
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Lock-free ConcurrentLinkedDeque for log storage - 无锁 ConcurrentLinkedDeque</li>
 *   <li>AtomicLong counters for thread-safe statistics - AtomicLong 线程安全统计</li>
 *   <li>Bounded buffer prevents unbounded memory growth - 有界缓冲区防止内存无限增长</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (ConcurrentLinkedDeque + AtomicLong) - 线程安全: 是</li>
 *   <li>Null-safe: Yes (rejects null parameters) - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-observability V1.0.0
 */
public final class SlowLogCollector {

    private static final System.Logger LOGGER = System.getLogger(SlowLogCollector.class.getName());

    private static final Duration DEFAULT_THRESHOLD = Duration.ofMillis(10);
    private static final int DEFAULT_MAX_ENTRIES = 1024;

    /**
     * Strips CR/LF characters to prevent log injection.
     * 去除 CR/LF 字符以防止日志注入。
     */
    private static String sanitizeForLog(String value) {
        if (value == null) {
            return "";
        }
        // Fast path: no CR/LF → return original string (zero allocation).
        // 快速路径：无 CR/LF 时返回原字符串（零分配）。
        if (value.indexOf('\r') < 0 && value.indexOf('\n') < 0) {
            return value;
        }
        return value.replace('\r', '_').replace('\n', '_');
    }

    // ==================== Entry | 条目 ====================

    /**
     * A single slow log entry capturing details of a slow operation.
     * 单条慢日志条目，记录慢操作的详细信息。
     *
     * @param operation  the operation name (e.g., GET, PUT) | 操作名称
     * @param key        the key or resource involved | 涉及的键或资源
     * @param elapsed    the elapsed duration of the operation | 操作的耗时
     * @param timestamp  the instant when the operation was recorded | 操作被记录的时间戳
     * @param threadName the name of the thread that executed the operation | 执行线程名称
     */
    public record Entry(
            String operation,
            String key,
            Duration elapsed,
            Instant timestamp,
            String threadName
    ) {
        /**
         * Compact canonical constructor with null validation.
         * 带空值验证的紧凑规范构造器。
         */
        public Entry {
            Objects.requireNonNull(operation, "operation must not be null");
            Objects.requireNonNull(key, "key must not be null");
            Objects.requireNonNull(elapsed, "elapsed must not be null");
            Objects.requireNonNull(timestamp, "timestamp must not be null");
            Objects.requireNonNull(threadName, "threadName must not be null");
        }
    }

    // ==================== Stats | 统计 ====================

    /**
     * Aggregated statistics for recorded slow operations.
     * 已记录慢操作的聚合统计信息。
     *
     * @param totalSlowOps     total number of slow operations recorded (cumulative) | 记录的慢操作总数（累积）
     * @param maxDuration      the longest duration among all buffered slow operations | 缓冲的最长耗时
     * @param avgDuration      the average duration of all buffered slow operations | 缓冲的平均耗时
     * @param slowestOperation the operation type with the longest duration | 耗时最长的操作类型
     */
    public record Stats(
            long totalSlowOps,
            Duration maxDuration,
            Duration avgDuration,
            String slowestOperation
    ) {
        /** Empty stats instance returned when no slow operations have been recorded. 未记录慢操作时返回的空统计实例。 */
        public static final Stats EMPTY = new Stats(0, Duration.ZERO, Duration.ZERO, "");
    }

    // ==================== Fields | 字段 ====================

    private final Duration threshold;
    private final int maxEntries;
    private final ConcurrentLinkedDeque<Entry> entries;
    private final AtomicLong totalCount = new AtomicLong(0);
    private final AtomicLong currentSize = new AtomicLong(0);

    private SlowLogCollector(Duration threshold, int maxEntries) {
        Objects.requireNonNull(threshold, "threshold must not be null");
        if (threshold.isNegative() || threshold.isZero()) {
            throw new ObservabilityException("INVALID_CONFIG", "threshold must be positive, got: " + threshold);
        }
        this.threshold = threshold;
        if (maxEntries <= 0) {
            throw new ObservabilityException("INVALID_CONFIG", "maxEntries must be positive, got: " + maxEntries);
        }
        this.maxEntries = maxEntries;
        this.entries = new ConcurrentLinkedDeque<>();
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates a collector with the default 10ms threshold and 1024 max entries.
     * 使用默认 10ms 阈值和 1024 最大条目数创建收集器。
     *
     * @return a new collector | 新的收集器
     */
    public static SlowLogCollector create() {
        return new SlowLogCollector(DEFAULT_THRESHOLD, DEFAULT_MAX_ENTRIES);
    }

    /**
     * Creates a collector with the given threshold and the default 1024 max entries.
     * 使用给定阈值和默认 1024 最大条目数创建收集器。
     *
     * @param threshold the slow operation threshold | 慢操作阈值
     * @return a new collector | 新的收集器
     */
    public static SlowLogCollector create(Duration threshold) {
        return new SlowLogCollector(threshold, DEFAULT_MAX_ENTRIES);
    }

    /**
     * Creates a collector with the given threshold and max entries.
     * 使用给定阈值和最大条目数创建收集器。
     *
     * @param threshold  the slow operation threshold | 慢操作阈值
     * @param maxEntries the maximum number of entries to retain | 保留的最大条目数
     * @return a new collector | 新的收集器
     */
    public static SlowLogCollector create(Duration threshold, int maxEntries) {
        return new SlowLogCollector(threshold, maxEntries);
    }

    // ==================== Operations | 操作 ====================

    /**
     * Records an operation if its elapsed time exceeds the configured threshold.
     * 如果操作耗时超过配置的阈值，则记录该操作。
     *
     * <p>Captures the current thread name and timestamp automatically. If the buffer
     * is full, the oldest entry is evicted to make room.</p>
     * <p>自动捕获当前线程名称和时间戳。如果缓冲区已满，将驱逐最旧的条目。</p>
     *
     * @param operation the operation name | 操作名称
     * @param key       the key or resource | 键或资源
     * @param elapsed   the elapsed duration | 耗时时长
     */
    public void record(String operation, String key, Duration elapsed) {
        Objects.requireNonNull(operation, "operation must not be null");
        Objects.requireNonNull(key, "key must not be null");
        Objects.requireNonNull(elapsed, "elapsed must not be null");

        if (elapsed.compareTo(threshold) <= 0) {
            return;
        }

        var entry = new Entry(operation, key, elapsed, Instant.now(), Thread.currentThread().getName());

        entries.addFirst(entry);
        totalCount.incrementAndGet();
        long size = currentSize.incrementAndGet();

        while (size > maxEntries) {
            Entry removed = entries.pollLast();
            if (removed != null) {
                size = currentSize.decrementAndGet();
            } else {
                // Deque is empty but size counter is stale; reconcile
                currentSize.set(0);
                break;
            }
        }

        // Log only operation and elapsed — key is omitted to prevent accidental PII/token disclosure.
        // Operation is sanitized to prevent CRLF log injection.
        // 仅记录操作名和耗时，key 可能含敏感数据，不写入日志。operation 过滤 CRLF 防止日志注入。
        LOGGER.log(System.Logger.Level.DEBUG,
                "Slow operation recorded: {0} elapsed={1}ms",
                sanitizeForLog(operation), elapsed.toMillis());
    }

    /**
     * Returns all buffered slow log entries ordered from newest to oldest.
     * 返回所有缓冲的慢日志条目，按从新到旧排序。
     *
     * @return an immutable list of entries | 条目的不可变列表
     */
    public List<Entry> getEntries() {
        return List.copyOf(entries);
    }

    /**
     * Returns up to {@code limit} of the latest slow log entries.
     * 返回最新的最多 {@code limit} 条慢日志条目。
     *
     * @param limit the maximum number of entries to return | 返回的最大条目数
     * @return an immutable list of the latest entries | 最新条目的不可变列表
     * @throws ObservabilityException if limit is negative | 如果 limit 为负数
     */
    public List<Entry> getEntries(int limit) {
        if (limit < 0) {
            throw new ObservabilityException("INVALID_CONFIG", "limit must not be negative, got: " + limit);
        }
        if (limit == 0) {
            return List.of();
        }
        // Clamp capacity to [0, limit] — currentSize can be temporarily negative under concurrent clear+record.
        // 容量限制在 [0, limit]，currentSize 在并发 clear+record 时可能暂时为负。
        int capacity = (int) Math.min(limit, Math.max(0, currentSize.get()));
        List<Entry> result = new ArrayList<>(capacity);
        int count = 0;
        for (Entry entry : entries) {
            if (count >= limit) break;
            result.add(entry);
            count++;
        }
        return List.copyOf(result);
    }

    /**
     * Clears all buffered slow log entries and resets the current-size counter.
     * 清除所有缓冲的慢日志条目并重置当前大小计数器。
     *
     * <p>Note: {@link #count()} (cumulative total) is NOT reset by this method.</p>
     * <p>注意：{@link #count()}（累积总数）不会被此方法重置。</p>
     */
    public void clear() {
        entries.clear();
        currentSize.set(0);
    }

    /**
     * Returns the total number of slow operations ever recorded (cumulative, never reset by clear).
     * 返回曾经记录的慢操作总数（累积值，不会被 clear 重置）。
     *
     * @return the cumulative slow operation count | 累积慢操作计数
     */
    public long count() {
        return totalCount.get();
    }

    /**
     * Computes aggregated statistics for all currently buffered entries.
     * 计算当前缓冲的所有条目的聚合统计信息。
     *
     * @return the statistics, or {@link Stats#EMPTY} if no entries are buffered |
     *         统计信息；如果没有缓冲条目则返回 {@link Stats#EMPTY}
     */
    public Stats stats() {
        List<Entry> snapshot = List.copyOf(entries);
        if (snapshot.isEmpty()) {
            return Stats.EMPTY;
        }
        Entry slowest = snapshot.stream()
                .max(Comparator.comparing(Entry::elapsed))
                .orElseThrow();
        long totalNanos;
        try {
            totalNanos = 0;
            for (Entry e : snapshot) {
                totalNanos = Math.addExact(totalNanos, e.elapsed().toNanos());
            }
        } catch (ArithmeticException _) {
            // Overflow: cap at Long.MAX_VALUE rather than producing a negative average
            totalNanos = Long.MAX_VALUE;
        }
        return new Stats(
                totalCount.get(),
                slowest.elapsed(),
                Duration.ofNanos(totalNanos / snapshot.size()),
                slowest.operation()
        );
    }

    /**
     * Returns the configured slow operation threshold.
     * 返回配置的慢操作阈值。
     *
     * @return the threshold | 阈值
     */
    public Duration threshold() {
        return threshold;
    }

    /**
     * Returns the configured maximum number of buffered entries.
     * 返回配置的最大缓冲条目数。
     *
     * @return the max entries limit | 最大条目数限制
     */
    public int maxEntries() {
        return maxEntries;
    }
}
