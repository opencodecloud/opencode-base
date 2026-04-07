package cloud.opencode.base.rules.trace;

import cloud.opencode.base.rules.Rule;
import cloud.opencode.base.rules.RuleContext;
import cloud.opencode.base.rules.listener.RuleListener;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tracing Rule Listener - Collects Execution Data to Build an ExecutionTrace
 * 追踪规则监听器 - 收集执行数据以构建执行轨迹
 *
 * <p>Records timing, condition results, execution status, and errors for each rule
 * during a single engine fire() call. After the engine completes, call
 * {@link #getTrace()} to retrieve the assembled {@link ExecutionTrace}.</p>
 * <p>在单次引擎fire()调用期间，记录每个规则的计时、条件结果、执行状态和错误。
 * 引擎完成后，调用{@link #getTrace()}获取组装好的{@link ExecutionTrace}。</p>
 *
 * <p><strong>Thread Safety | 线程安全:</strong></p>
 * <p>This listener is designed for single-threaded use within one fire() call.
 * It uses simple collections (ArrayList, HashMap) since a single fire() call is single-threaded.</p>
 * <p>此监听器设计用于单次fire()调用中的单线程使用。
 * 它使用简单集合（ArrayList、HashMap），因为单次fire()调用是单线程的。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.3
 */
public class TracingRuleListener implements RuleListener {

    private final Map<String, Long> startTimes = new HashMap<>();
    private final Map<String, Boolean> conditionResults = new HashMap<>();
    private final Set<String> executedRules = new HashSet<>();
    private final Map<String, Throwable> errors = new HashMap<>();
    private final List<String> ruleOrder = new ArrayList<>();
    private final Set<String> ruleOrderSet = new HashSet<>();
    private final Set<String> skippedRules = new HashSet<>();
    private long totalStartTime;
    private long totalEndTime;

    @Override
    public void onStart(RuleContext context) {
        reset();
        totalStartTime = System.nanoTime();
    }

    @Override
    public void beforeEvaluate(Rule rule, RuleContext context) {
        String name = rule.getName();
        startTimes.put(name, System.nanoTime());
        if (ruleOrderSet.add(name)) {
            ruleOrder.add(name);
        }
    }

    @Override
    public void afterEvaluate(Rule rule, RuleContext context, boolean satisfied) {
        conditionResults.put(rule.getName(), satisfied);
    }

    @Override
    public void afterExecute(Rule rule, RuleContext context) {
        executedRules.add(rule.getName());
    }

    @Override
    public void onFailure(Rule rule, RuleContext context, Exception exception) {
        errors.put(rule.getName(), exception);
    }

    @Override
    public void onFinish(RuleContext context, int firedCount, long elapsedMillis) {
        totalEndTime = System.nanoTime();
    }

    /**
     * Returns the assembled execution trace after engine.fire() completes
     * 在engine.fire()完成后返回组装好的执行轨迹
     *
     * <p>Must be called after the engine has finished firing rules.</p>
     * <p>必须在引擎完成触发规则后调用。</p>
     *
     * @return the execution trace | 执行轨迹
     */
    public ExecutionTrace getTrace() {
        List<RuleTrace> traces = new ArrayList<>();

        for (String name : ruleOrder) {
            long startNano = startTimes.getOrDefault(name, totalStartTime);
            long endNano = totalEndTime > 0 ? totalEndTime : System.nanoTime();
            Duration duration = Duration.ofNanos(endNano - startNano);

            if (skippedRules.contains(name)) {
                traces.add(RuleTrace.skipped(name));
            } else if (errors.containsKey(name)) {
                traces.add(RuleTrace.failed(name, duration, errors.get(name)));
            } else if (executedRules.contains(name)) {
                traces.add(RuleTrace.fired(name, duration));
            } else {
                traces.add(RuleTrace.notMatched(name, duration));
            }
        }

        // Add skipped rules that were never evaluated (disabled rules)
        for (String name : skippedRules) {
            if (!ruleOrderSet.contains(name)) {
                traces.add(RuleTrace.skipped(name));
            }
        }

        Duration totalDuration = Duration.ofNanos(
                totalEndTime > 0 ? totalEndTime - totalStartTime : System.nanoTime() - totalStartTime
        );

        return ExecutionTrace.of(traces, totalDuration);
    }

    /**
     * Clears all state for reuse
     * 清除所有状态以便重用
     */
    public void reset() {
        startTimes.clear();
        conditionResults.clear();
        executedRules.clear();
        errors.clear();
        ruleOrder.clear();
        ruleOrderSet.clear();
        skippedRules.clear();
        totalStartTime = 0;
        totalEndTime = 0;
    }

    /**
     * Marks a rule as skipped (for use by engine implementations that track disabled rules)
     * 将规则标记为已跳过（供跟踪禁用规则的引擎实现使用）
     *
     * @param ruleName the rule name | 规则名称
     */
    void markSkipped(String ruleName) {
        skippedRules.add(ruleName);
    }
}
