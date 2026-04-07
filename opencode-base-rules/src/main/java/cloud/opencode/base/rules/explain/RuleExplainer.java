package cloud.opencode.base.rules.explain;

import cloud.opencode.base.rules.trace.ExecutionTrace;
import cloud.opencode.base.rules.trace.RuleTrace;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Rule Explainer - Generates Human-Readable Explanations from Execution Traces
 * 规则解释器 - 从执行轨迹生成人类可读的解释
 *
 * <p>Converts an {@link ExecutionTrace} into an {@link Explanation} with
 * human-readable descriptions for each rule's outcome.</p>
 * <p>将{@link ExecutionTrace}转换为{@link Explanation}，为每个规则的结果提供人类可读的描述。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.3
 */
public final class RuleExplainer {

    private RuleExplainer() {
        // utility class
    }

    /**
     * Generates an explanation from an execution trace
     * 从执行轨迹生成解释
     *
     * @param trace the execution trace | 执行轨迹
     * @return the explanation | 解释
     * @throws NullPointerException if trace is null | 如果trace为null则抛出
     */
    public static Explanation explain(ExecutionTrace trace) {
        Objects.requireNonNull(trace, "trace must not be null");

        List<RuleExplanation> details = new ArrayList<>();

        for (RuleTrace rt : trace.ruleTraces()) {
            String name = rt.ruleName();
            String reason;
            boolean fired;

            if (rt.hasFailed()) {
                String msg = rt.error().getMessage();
                reason = "Rule '" + name + "' failed: "
                        + (msg != null ? msg : rt.error().getClass().getSimpleName());
                fired = false;
            } else if (rt.skipped()) {
                reason = "Rule '" + name + "' was skipped (disabled)";
                fired = false;
            } else if (rt.hasFired()) {
                reason = "Rule '" + name + "' fired (condition matched, executed in "
                        + rt.duration().toMillis() + "ms)";
                fired = true;
            } else {
                reason = "Rule '" + name + "' did not fire (condition not met)";
                fired = false;
            }

            details.add(new RuleExplanation(name, fired, reason));
        }

        String summary = "Evaluated " + trace.ruleTraces().size() + " rules: "
                + trace.firedCount() + " fired, "
                + trace.skippedCount() + " skipped, "
                + trace.failedCount() + " failed in "
                + trace.totalDuration().toMillis() + "ms";

        return new Explanation(summary, details);
    }
}
