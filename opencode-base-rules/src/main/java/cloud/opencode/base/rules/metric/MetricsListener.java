package cloud.opencode.base.rules.metric;

import cloud.opencode.base.rules.Rule;
import cloud.opencode.base.rules.RuleContext;
import cloud.opencode.base.rules.listener.RuleListener;

/**
 * Metrics Listener - RuleListener That Collects Execution Metrics
 * 指标监听器 - 收集执行指标的规则监听器
 *
 * <p>Implements {@link RuleListener} to automatically collect per-rule timing,
 * fire, and failure metrics during rule engine execution.</p>
 * <p>实现{@link RuleListener}以在规则引擎执行期间自动收集每条规则的时间、触发和失败指标。</p>
 *
 * <p><strong>Thread Safety | 线程安全:</strong> Uses ThreadLocal for start times and
 * delegates to thread-safe {@link RuleMetrics} for accumulation.</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see RuleMetrics
 * @see RuleListener
 * @since JDK 25, opencode-base-rules V1.0.3
 */
public final class MetricsListener implements RuleListener {

    private final RuleMetrics metrics;
    private final ThreadLocal<Long> lastStartNano = new ThreadLocal<>();

    /**
     * Creates a MetricsListener with the given RuleMetrics instance
     * 使用给定的RuleMetrics实例创建MetricsListener
     *
     * @param metrics the metrics collector | 指标收集器
     */
    public MetricsListener(RuleMetrics metrics) {
        this.metrics = metrics;
    }

    /**
     * Creates a MetricsListener with an internal RuleMetrics instance
     * 使用内部RuleMetrics实例创建MetricsListener
     */
    public MetricsListener() {
        this(new RuleMetrics());
    }

    /**
     * Returns the underlying RuleMetrics instance
     * 返回底层的RuleMetrics实例
     *
     * @return the metrics collector | 指标收集器
     */
    public RuleMetrics getMetrics() {
        return metrics;
    }

    /**
     * Records the start time before rule evaluation
     * 在规则评估前记录开始时间
     *
     * @param rule    the rule being evaluated | 正在评估的规则
     * @param context the rule context | 规则上下文
     */
    @Override
    public void beforeEvaluate(Rule rule, RuleContext context) {
        lastStartNano.set(System.nanoTime());
    }

    /**
     * Computes duration and records the evaluation metric
     * 计算持续时间并记录评估指标
     *
     * @param rule      the rule that was evaluated | 已评估的规则
     * @param context   the rule context | 规则上下文
     * @param satisfied whether the condition was satisfied | 条件是否满足
     */
    @Override
    public void afterEvaluate(Rule rule, RuleContext context, boolean satisfied) {
        Long startNano = lastStartNano.get();
        long durationNanos = startNano != null ? System.nanoTime() - startNano : 0L;
        metrics.recordEvaluation(rule.getName(), durationNanos, satisfied);
    }

    /**
     * Records a rule failure
     * 记录规则失败
     *
     * @param rule      the rule that failed | 失败的规则
     * @param context   the rule context | 规则上下文
     * @param exception the exception thrown | 抛出的异常
     */
    @Override
    public void onFailure(Rule rule, RuleContext context, Exception exception) {
        metrics.recordFailure(rule.getName());
    }

    /**
     * Cleans up ThreadLocal state when execution finishes
     * 执行完成时清理ThreadLocal状态
     *
     * @param context       the rule context | 规则上下文
     * @param firedCount    the number of fired rules | 触发的规则数
     * @param elapsedMillis the elapsed time in milliseconds | 耗时（毫秒）
     */
    @Override
    public void onFinish(RuleContext context, int firedCount, long elapsedMillis) {
        lastStartNano.remove();
    }
}
