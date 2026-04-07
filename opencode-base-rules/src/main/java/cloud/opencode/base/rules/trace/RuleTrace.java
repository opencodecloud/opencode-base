package cloud.opencode.base.rules.trace;

import java.time.Duration;
import java.util.Objects;

/**
 * Rule Trace - Records the Execution Trace of a Single Rule
 * 规则轨迹 - 记录单个规则的执行轨迹
 *
 * <p>Captures whether a rule's condition matched, whether its action was executed,
 * the duration of evaluation and execution, and any error that occurred.</p>
 * <p>捕获规则条件是否匹配、动作是否执行、评估和执行的持续时间以及发生的任何错误。</p>
 *
 * @param ruleName        the rule name | 规则名称
 * @param conditionResult whether condition evaluated to true | 条件是否评估为true
 * @param executed        whether action was executed | 动作是否已执行
 * @param duration        evaluation + execution time | 评估和执行时间
 * @param error           null if no error | 如果没有错误则为null
 * @param skipped         true if rule was disabled/skipped | 如果规则被禁用/跳过则为true
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.3
 */
public record RuleTrace(
        String ruleName,
        boolean conditionResult,
        boolean executed,
        Duration duration,
        Throwable error,
        boolean skipped
) {

    /**
     * Canonical constructor with validation
     * 带验证的规范构造函数
     *
     * @param ruleName        the rule name | 规则名称
     * @param conditionResult whether condition evaluated to true | 条件是否评估为true
     * @param executed        whether action was executed | 动作是否已执行
     * @param duration        evaluation + execution time | 评估和执行时间
     * @param error           null if no error | 如果没有错误则为null
     * @param skipped         true if rule was disabled/skipped | 如果规则被禁用/跳过则为true
     */
    public RuleTrace {
        Objects.requireNonNull(ruleName, "ruleName must not be null");
        Objects.requireNonNull(duration, "duration must not be null");
    }

    /**
     * Creates a trace for a rule that fired (condition matched and action executed)
     * 创建已触发规则的轨迹（条件匹配且动作已执行）
     *
     * @param name     the rule name | 规则名称
     * @param duration the evaluation + execution duration | 评估和执行持续时间
     * @return the rule trace | 规则轨迹
     */
    public static RuleTrace fired(String name, Duration duration) {
        return new RuleTrace(name, true, true, duration, null, false);
    }

    /**
     * Creates a trace for a rule whose condition did not match
     * 创建条件不匹配的规则轨迹
     *
     * @param name     the rule name | 规则名称
     * @param duration the evaluation duration | 评估持续时间
     * @return the rule trace | 规则轨迹
     */
    public static RuleTrace notMatched(String name, Duration duration) {
        return new RuleTrace(name, false, false, duration, null, false);
    }

    /**
     * Creates a trace for a rule that was skipped (disabled)
     * 创建被跳过（禁用）的规则轨迹
     *
     * @param name the rule name | 规则名称
     * @return the rule trace | 规则轨迹
     */
    public static RuleTrace skipped(String name) {
        return new RuleTrace(name, false, false, Duration.ZERO, null, true);
    }

    /**
     * Creates a trace for a rule that failed during execution
     * 创建执行期间失败的规则轨迹
     *
     * @param name     the rule name | 规则名称
     * @param duration the evaluation + execution duration | 评估和执行持续时间
     * @param error    the error that occurred | 发生的错误
     * @return the rule trace | 规则轨迹
     */
    public static RuleTrace failed(String name, Duration duration, Throwable error) {
        return new RuleTrace(name, true, false, duration, error, false);
    }

    /**
     * Returns whether this rule fired (condition matched and action executed)
     * 返回此规则是否已触发（条件匹配且动作已执行）
     *
     * @return true if the rule fired | 如果规则已触发返回true
     */
    public boolean hasFired() {
        return conditionResult && executed;
    }

    /**
     * Returns whether this rule failed during execution
     * 返回此规则在执行期间是否失败
     *
     * @return true if the rule failed | 如果规则失败返回true
     */
    public boolean hasFailed() {
        return error != null;
    }
}
