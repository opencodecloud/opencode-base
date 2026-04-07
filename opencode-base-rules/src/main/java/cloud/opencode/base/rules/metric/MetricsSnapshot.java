package cloud.opencode.base.rules.metric;

/**
 * Metrics Snapshot - Immutable Point-in-Time Snapshot of Rule Execution Metrics
 * 指标快照 - 规则执行指标的不可变时间点快照
 *
 * <p>Captures per-rule evaluation counts, fire counts, failure counts, and total
 * duration at the time the snapshot was taken.</p>
 * <p>捕获快照拍摄时每条规则的评估次数、触发次数、失败次数和总持续时间。</p>
 *
 * @param ruleName           the rule name | 规则名称
 * @param evaluationCount    the number of times the rule was evaluated | 规则被评估的次数
 * @param fireCount          the number of times the rule fired | 规则被触发的次数
 * @param failCount          the number of times the rule failed | 规则失败的次数
 * @param totalDurationNanos the total evaluation duration in nanoseconds | 总评估持续时间（纳秒）
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see RuleMetrics
 * @since JDK 25, opencode-base-rules V1.0.3
 */
public record MetricsSnapshot(
        String ruleName,
        long evaluationCount,
        long fireCount,
        long failCount,
        long totalDurationNanos
) {

    /**
     * Computes the average evaluation duration in nanoseconds
     * 计算平均评估持续时间（纳秒）
     *
     * @return average duration, or 0 if no evaluations | 平均持续时间，如果没有评估则为0
     */
    public double avgDurationNanos() {
        return evaluationCount == 0 ? 0.0 : (double) totalDurationNanos / evaluationCount;
    }

    /**
     * Computes the fire rate (fireCount / evaluationCount)
     * 计算触发率（触发次数 / 评估次数）
     *
     * @return fire rate between 0.0 and 1.0, or 0 if no evaluations | 0.0到1.0之间的触发率，如果没有评估则为0
     */
    public double fireRate() {
        return evaluationCount == 0 ? 0.0 : (double) fireCount / evaluationCount;
    }
}
