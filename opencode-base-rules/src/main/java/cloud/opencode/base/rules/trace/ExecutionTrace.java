package cloud.opencode.base.rules.trace;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Execution Trace - Records the Complete Execution Trace of a Rule Engine Run
 * 执行轨迹 - 记录规则引擎一次运行的完整执行轨迹
 *
 * <p>Aggregates individual rule traces with summary counts and total duration.</p>
 * <p>聚合单个规则轨迹，提供汇总计数和总持续时间。</p>
 *
 * @param ruleTraces    trace per rule | 每个规则的轨迹
 * @param totalDuration total engine execution time | 引擎执行总时间
 * @param firedCount    count of fired rules | 已触发规则的数量
 * @param skippedCount  count of skipped rules | 已跳过规则的数量
 * @param failedCount   count of failed rules | 已失败规则的数量
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.3
 */
public record ExecutionTrace(
        List<RuleTrace> ruleTraces,
        Duration totalDuration,
        int firedCount,
        int skippedCount,
        int failedCount
) {

    /**
     * Canonical constructor with validation
     * 带验证的规范构造函数
     *
     * @param ruleTraces    trace per rule | 每个规则的轨迹
     * @param totalDuration total engine execution time | 引擎执行总时间
     * @param firedCount    count of fired rules | 已触发规则的数量
     * @param skippedCount  count of skipped rules | 已跳过规则的数量
     * @param failedCount   count of failed rules | 已失败规则的数量
     */
    public ExecutionTrace {
        Objects.requireNonNull(ruleTraces, "ruleTraces must not be null");
        Objects.requireNonNull(totalDuration, "totalDuration must not be null");
        ruleTraces = List.copyOf(ruleTraces);
    }

    /**
     * Creates an execution trace from a list of rule traces and total duration,
     * computing fired/skipped/failed counts automatically
     * 从规则轨迹列表和总持续时间创建执行轨迹，自动计算触发/跳过/失败计数
     *
     * @param traces        the rule traces | 规则轨迹
     * @param totalDuration the total duration | 总持续时间
     * @return the execution trace | 执行轨迹
     */
    public static ExecutionTrace of(List<RuleTrace> traces, Duration totalDuration) {
        Objects.requireNonNull(traces, "traces must not be null");
        Objects.requireNonNull(totalDuration, "totalDuration must not be null");

        int fired = 0;
        int skipped = 0;
        int failed = 0;
        for (RuleTrace trace : traces) {
            if (trace.hasFired()) {
                fired++;
            }
            if (trace.skipped()) {
                skipped++;
            }
            if (trace.hasFailed()) {
                failed++;
            }
        }
        return new ExecutionTrace(traces, totalDuration, fired, skipped, failed);
    }

    /**
     * Returns the list of rule traces that fired
     * 返回已触发的规则轨迹列表
     *
     * @return list of fired rule traces | 已触发的规则轨迹列表
     */
    public List<RuleTrace> firedRules() {
        return ruleTraces.stream()
                .filter(RuleTrace::hasFired)
                .toList();
    }

    /**
     * Returns the list of rule traces that failed
     * 返回已失败的规则轨迹列表
     *
     * @return list of failed rule traces | 已失败的规则轨迹列表
     */
    public List<RuleTrace> failedRules() {
        return ruleTraces.stream()
                .filter(RuleTrace::hasFailed)
                .toList();
    }

    /**
     * Finds a rule trace by rule name
     * 按规则名称查找规则轨迹
     *
     * @param ruleName the rule name | 规则名称
     * @return optional containing the trace, empty if not found | 包含轨迹的Optional，如果未找到则为空
     */
    public Optional<RuleTrace> getTrace(String ruleName) {
        Objects.requireNonNull(ruleName, "ruleName must not be null");
        return ruleTraces.stream()
                .filter(t -> ruleName.equals(t.ruleName()))
                .findFirst();
    }
}
