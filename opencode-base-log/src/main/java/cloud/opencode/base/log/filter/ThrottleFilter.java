package cloud.opencode.base.log.filter;

import cloud.opencode.base.log.LogEvent;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Throttle Filter - Rate-Limits Duplicate Log Messages
 * 节流过滤器 - 限制重复日志消息的速率
 *
 * <p>Denies log events with the same message if they were logged within
 * the configured time interval. Uses monotonic {@link System#nanoTime()}
 * for accurate timing.</p>
 * <p>如果相同消息在配置的时间间隔内已被记录，则拒绝日志事件。
 * 使用单调的 {@link System#nanoTime()} 进行精确计时。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Per-message rate limiting - 每消息速率限制</li>
 *   <li>Monotonic timing via nanoTime - 通过 nanoTime 的单调计时</li>
 *   <li>Cache size limit of 10000 entries with oldest eviction - 10000 条目的缓存大小限制，淘汰最旧条目</li>
 *   <li>Thread-safe via ConcurrentHashMap - 通过 ConcurrentHashMap 实现线程安全</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Throttle duplicate messages within 5 seconds
 * ThrottleFilter filter = new ThrottleFilter(Duration.ofSeconds(5));
 * chain.addFilter(filter);
 *
 * // Clear the throttle cache
 * filter.clearCache();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (ConcurrentHashMap) - 线程安全: 是（ConcurrentHashMap）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.3
 */
public final class ThrottleFilter implements LogFilter {

    private static final int MAX_CACHE_SIZE = 10_000;

    private final long intervalNanos;
    private final ConcurrentHashMap<String, Long> lastLogTimeMap = new ConcurrentHashMap<>();

    /**
     * Creates a throttle filter with the specified interval.
     * 使用指定间隔创建节流过滤器。
     *
     * @param interval the minimum interval between duplicate messages | 重复消息之间的最小间隔
     * @throws NullPointerException     if interval is null | 如果间隔为 null
     * @throws IllegalArgumentException if interval is negative or zero | 如果间隔为负数或零
     */
    public ThrottleFilter(Duration interval) {
        Objects.requireNonNull(interval, "interval must not be null");
        if (interval.isNegative() || interval.isZero()) {
            throw new IllegalArgumentException("interval must be positive: " + interval);
        }
        this.intervalNanos = interval.toNanos();
    }

    /**
     * Filters the event based on message throttling.
     * 根据消息节流过滤事件。
     *
     * @param event the log event | 日志事件
     * @return DENY if the same message was logged within the interval, NEUTRAL otherwise |
     *         如果相同消息在间隔内已被记录返回 DENY，否则返回 NEUTRAL
     */
    @Override
    public FilterAction filter(LogEvent event) {
        String message = event.message();
        long now = System.nanoTime();

        // Atomic check-and-update to avoid race conditions
        boolean[] denied = {false};
        lastLogTimeMap.compute(message, (key, lastTime) -> {
            if (lastTime != null && (now - lastTime) < intervalNanos) {
                denied[0] = true;
                return lastTime; // keep existing timestamp, deny this event
            }
            return now;
        });

        if (denied[0]) {
            return FilterAction.DENY;
        }

        // Evict a batch when cache is too large (amortized O(1))
        if (lastLogTimeMap.size() > MAX_CACHE_SIZE) {
            evictBatch(MAX_CACHE_SIZE / 10);
        }

        return FilterAction.NEUTRAL;
    }

    /**
     * Clears the throttle cache.
     * 清除节流缓存。
     */
    public void clearCache() {
        lastLogTimeMap.clear();
    }

    /**
     * Evicts expired entries from the cache using O(n) threshold scan.
     * 使用 O(n) 阈值扫描淘汰缓存中的过期条目。
     *
     * <p>Removes entries whose last-seen timestamp is older than 2x the
     * configured interval. Falls back to iterator-based batch eviction
     * if no entries qualify.</p>
     * <p>移除最后出现时间戳早于 2 倍配置间隔的条目。如果没有条目符合条件，
     * 则回退到基于迭代器的批量淘汰。</p>
     *
     * @param count the fallback number of entries to evict | 回退时要淘汰的条目数
     */
    private void evictBatch(int count) {
        long now = System.nanoTime();
        long threshold = intervalNanos * 2;
        int evicted = 0;

        // O(n) scan: remove entries older than 2x interval
        for (var it = lastLogTimeMap.entrySet().iterator(); it.hasNext(); ) {
            var entry = it.next();
            if ((now - entry.getValue()) > threshold) {
                it.remove();
                evicted++;
            }
        }

        // Fallback: if threshold eviction didn't free enough, evict by iteration order
        if (evicted == 0) {
            var it = lastLogTimeMap.keySet().iterator();
            for (int i = 0; i < count && it.hasNext(); i++) {
                it.next();
                it.remove();
            }
        }
    }
}
