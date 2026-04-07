package cloud.opencode.base.rules.explain;

import java.util.Objects;

/**
 * Rule Explanation - Human-Readable Explanation of a Single Rule's Outcome
 * 规则解释 - 单个规则结果的人类可读解释
 *
 * <p>Captures whether a rule fired and provides a human-readable reason.</p>
 * <p>捕获规则是否触发并提供人类可读的原因。</p>
 *
 * @param ruleName the rule name | 规则名称
 * @param fired    whether the rule fired | 规则是否触发
 * @param reason   human-readable explanation of why it fired or didn't fire | 触发或未触发的人类可读原因
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.3
 */
public record RuleExplanation(
        String ruleName,
        boolean fired,
        String reason
) {

    /**
     * Canonical constructor with validation
     * 带验证的规范构造函数
     *
     * @param ruleName the rule name | 规则名称
     * @param fired    whether the rule fired | 规则是否触发
     * @param reason   human-readable explanation | 人类可读的解释
     */
    public RuleExplanation {
        Objects.requireNonNull(ruleName, "ruleName must not be null");
        Objects.requireNonNull(reason, "reason must not be null");
    }
}
