package cloud.opencode.base.rules.metric;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

/**
 * Rule Metrics - Thread-Safe Per-Rule Metrics Collector
 * 规则指标 - 线程安全的按规则指标收集器
 *
 * <p>Collects execution metrics (evaluation count, fire count, fail count, duration)
 * for each rule in a thread-safe manner using {@link LongAdder}.</p>
 * <p>使用{@link LongAdder}以线程安全方式收集每条规则的执行指标
 * （评估次数、触发次数、失败次数、持续时间）。</p>
 *
 * <p><strong>Thread Safety | 线程安全:</strong> All methods are safe for concurrent use.</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see MetricsSnapshot
 * @see MetricsListener
 * @since JDK 25, opencode-base-rules V1.0.3
 */
public final class RuleMetrics {

    private static final int MAX_TRACKED_RULES = 10_000;

    private final ConcurrentHashMap<String, RuleStats> statsMap = new ConcurrentHashMap<>();

    /**
     * Records a rule evaluation
     * 记录规则评估
     *
     * @param ruleName      the rule name | 规则名称
     * @param durationNanos the evaluation duration in nanoseconds | 评估持续时间（纳秒）
     * @param fired         whether the rule fired (condition was true) | 规则是否触发（条件为真）
     */
    public void recordEvaluation(String ruleName, long durationNanos, boolean fired) {
        RuleStats stats = statsMap.get(ruleName);
        if (stats == null) {
            if (statsMap.size() >= MAX_TRACKED_RULES) {
                return;
            }
            stats = statsMap.computeIfAbsent(ruleName, _ -> new RuleStats());
        }
        stats.evaluationCount.increment();
        stats.totalDurationNanos.add(durationNanos);
        if (fired) {
            stats.fireCount.increment();
        }
    }

    /**
     * Records a rule failure
     * 记录规则失败
     *
     * @param ruleName the rule name | 规则名称
     */
    public void recordFailure(String ruleName) {
        RuleStats stats = statsMap.get(ruleName);
        if (stats == null) {
            if (statsMap.size() >= MAX_TRACKED_RULES) {
                return;
            }
            stats = statsMap.computeIfAbsent(ruleName, _ -> new RuleStats());
        }
        stats.failCount.increment();
    }

    /**
     * Returns a point-in-time snapshot of metrics for a specific rule
     * 返回特定规则的指标时间点快照
     *
     * @param ruleName the rule name | 规则名称
     * @return the metrics snapshot, or null if no data exists | 指标快照，如果没有数据则为null
     */
    public MetricsSnapshot getSnapshot(String ruleName) {
        RuleStats stats = statsMap.get(ruleName);
        if (stats == null) {
            return null;
        }
        return new MetricsSnapshot(
                ruleName,
                stats.evaluationCount.sum(),
                stats.fireCount.sum(),
                stats.failCount.sum(),
                stats.totalDurationNanos.sum()
        );
    }

    /**
     * Returns snapshots for all tracked rules
     * 返回所有跟踪规则的快照
     *
     * @return map of rule name to metrics snapshot | 规则名称到指标快照的映射
     */
    public Map<String, MetricsSnapshot> getAllSnapshots() {
        return statsMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> new MetricsSnapshot(
                                e.getKey(),
                                e.getValue().evaluationCount.sum(),
                                e.getValue().fireCount.sum(),
                                e.getValue().failCount.sum(),
                                e.getValue().totalDurationNanos.sum()
                        )
                ));
    }

    /**
     * Resets all metrics
     * 重置所有指标
     */
    public void reset() {
        statsMap.clear();
    }

    /**
     * Resets metrics for a specific rule
     * 重置特定规则的指标
     *
     * @param ruleName the rule name | 规则名称
     */
    public void reset(String ruleName) {
        statsMap.remove(ruleName);
    }

    /**
     * Internal per-rule statistics holder using LongAdder for thread safety.
     * 使用LongAdder实现线程安全的内部按规则统计持有者。
     */
    private static final class RuleStats {
        final LongAdder evaluationCount = new LongAdder();
        final LongAdder fireCount = new LongAdder();
        final LongAdder failCount = new LongAdder();
        final LongAdder totalDurationNanos = new LongAdder();
    }
}
