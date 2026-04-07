package cloud.opencode.base.rules.validation;

/**
 * Validation Issue - Represents a Single Problem Found During Rule Validation
 * 验证问题 - 表示规则验证期间发现的单个问题
 *
 * <p>Encapsulates the severity, type, rule name, and message of a validation issue.
 * Issues can be errors (blocking) or warnings (informational).</p>
 * <p>封装验证问题的严重程度、类型、规则名称和消息。
 * 问题可以是错误（阻塞性）或警告（信息性）。</p>
 *
 * @param severity the severity of the issue | 问题的严重程度
 * @param type     the type of issue | 问题类型
 * @param ruleName the name of the rule, may be null for global issues | 规则名称，全局问题时可能为null
 * @param message  the human-readable message | 人类可读的消息
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see ValidationReport
 * @see RuleValidator
 * @since JDK 25, opencode-base-rules V1.0.3
 */
public record ValidationIssue(Severity severity, IssueType type, String ruleName, String message) {

    /**
     * Severity Level of a Validation Issue
     * 验证问题的严重级别
     */
    public enum Severity {
        /** Blocking error that makes the rule set invalid | 使规则集无效的阻塞性错误 */
        ERROR,
        /** Non-blocking warning for unusual but not invalid configuration | 不寻常但非无效配置的非阻塞性警告 */
        WARNING
    }

    /**
     * Type of Validation Issue
     * 验证问题类型
     */
    public enum IssueType {
        /** Two or more rules share the same name | 两个或更多规则共享相同名称 */
        DUPLICATE_RULE_NAME,
        /** Rule name is null or blank | 规则名称为null或空白 */
        EMPTY_RULE_NAME,
        /** Rule condition is null | 规则条件为null */
        NULL_CONDITION,
        /** Rule action is null | 规则动作为null */
        NULL_ACTION,
        /** Rule priority is negative | 规则优先级为负数 */
        NEGATIVE_PRIORITY,
        /** Rule group is an empty string | 规则分组为空字符串 */
        EMPTY_GROUP,
        /** Circular dependency detected among rules | 检测到规则之间的循环依赖 */
        CIRCULAR_DEPENDENCY
    }

    /**
     * Creates an ERROR-level validation issue
     * 创建ERROR级别的验证问题
     *
     * @param type     the issue type | 问题类型
     * @param ruleName the rule name, may be null | 规则名称，可能为null
     * @param message  the message | 消息
     * @return a new error issue | 新的错误问题
     */
    public static ValidationIssue error(IssueType type, String ruleName, String message) {
        return new ValidationIssue(Severity.ERROR, type, ruleName, message);
    }

    /**
     * Creates a WARNING-level validation issue
     * 创建WARNING级别的验证问题
     *
     * @param type     the issue type | 问题类型
     * @param ruleName the rule name, may be null | 规则名称，可能为null
     * @param message  the message | 消息
     * @return a new warning issue | 新的警告问题
     */
    public static ValidationIssue warning(IssueType type, String ruleName, String message) {
        return new ValidationIssue(Severity.WARNING, type, ruleName, message);
    }

    @Override
    public String toString() {
        String ruleRef = ruleName != null ? " [" + ruleName + "]" : "";
        return severity + ruleRef + " " + type + ": " + message;
    }
}
