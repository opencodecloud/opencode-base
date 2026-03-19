package cloud.opencode.base.rules.exception;

import cloud.opencode.base.core.exception.OpenException;

/**
 * Base Rules Exception for All Rule Engine Errors
 * 所有规则引擎错误的基础异常
 *
 * <p>Base exception class for all rule-related errors in the rules component,
 * providing detailed error information including rule name and error type.</p>
 * <p>规则组件中所有规则相关错误的基础异常类，提供包括规则名称和错误类型的详细错误信息。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Rule name tracking - 规则名称跟踪</li>
 *   <li>Error type classification - 错误类型分类</li>
 *   <li>Cause chaining - 原因链</li>
 *   <li>Component identification - 组件标识</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw new OpenRulesException("Rule evaluation failed");
 * throw new OpenRulesException("Invalid rule", "rule-1",
 *     OpenRulesException.RuleErrorType.INVALID_DEFINITION);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 *   <li>Null-safe: Partial (message required, ruleName optional) - 空值安全: 部分（消息必需，规则名称可选）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
public class OpenRulesException extends OpenException {

    private static final String COMPONENT = "RULES";

    private final String ruleName;
    private final RuleErrorType errorType;

    /**
     * Constructs a new rules exception with message
     * 使用消息构造新的规则异常
     *
     * @param message the error message | 错误消息
     */
    public OpenRulesException(String message) {
        super(COMPONENT, null, message);
        this.ruleName = null;
        this.errorType = RuleErrorType.GENERAL;
    }

    /**
     * Constructs a new rules exception with message and cause
     * 使用消息和原因构造新的规则异常
     *
     * @param message the error message | 错误消息
     * @param cause   the cause | 原因
     */
    public OpenRulesException(String message, Throwable cause) {
        super(COMPONENT, null, message, cause);
        this.ruleName = null;
        this.errorType = RuleErrorType.GENERAL;
    }

    /**
     * Constructs a new rules exception with error type and message
     * 使用错误类型和消息构造新的规则异常
     *
     * @param errorType the error type | 错误类型
     * @param message   the error message | 错误消息
     */
    public OpenRulesException(RuleErrorType errorType, String message) {
        super(COMPONENT, null, message);
        this.ruleName = null;
        this.errorType = errorType;
    }

    /**
     * Constructs a new rules exception with full details
     * 使用完整详情构造新的规则异常
     *
     * @param message   the error message | 错误消息
     * @param ruleName  the rule name | 规则名称
     * @param errorType the error type | 错误类型
     */
    public OpenRulesException(String message, String ruleName, RuleErrorType errorType) {
        super(COMPONENT, null, message);
        this.ruleName = ruleName;
        this.errorType = errorType;
    }

    /**
     * Constructs a new rules exception with full details and cause
     * 使用完整详情和原因构造新的规则异常
     *
     * @param message   the error message | 错误消息
     * @param ruleName  the rule name | 规则名称
     * @param errorType the error type | 错误类型
     * @param cause     the cause | 原因
     */
    public OpenRulesException(String message, String ruleName, RuleErrorType errorType, Throwable cause) {
        super(COMPONENT, null, message, cause);
        this.ruleName = ruleName;
        this.errorType = errorType;
    }

    /**
     * Gets the name of the rule that caused the exception
     * 获取导致异常的规则名称
     *
     * @return the rule name, or null if not specified | 规则名称，如果未指定则为null
     */
    public String ruleName() {
        return ruleName;
    }

    /**
     * Gets the type of rule error that occurred
     * 获取发生的规则错误类型
     *
     * @return the error type | 错误类型
     */
    public RuleErrorType errorType() {
        return errorType;
    }

    /**
     * Rule Error Type Enumeration
     * 规则错误类型枚举
     */
    public enum RuleErrorType {
        /** General error - 一般错误 */
        GENERAL,
        /** Condition evaluation error - 条件评估错误 */
        CONDITION_EVALUATION,
        /** Action execution error - 动作执行错误 */
        ACTION_EXECUTION,
        /** Rule not found - 规则未找到 */
        RULE_NOT_FOUND,
        /** Invalid rule definition - 无效的规则定义 */
        INVALID_DEFINITION,
        /** Conflict resolution error - 冲突解决错误 */
        CONFLICT_RESOLUTION,
        /** Decision table error - 决策表错误 */
        DECISION_TABLE,
        /** Timeout error - 超时错误 */
        TIMEOUT
    }
}
