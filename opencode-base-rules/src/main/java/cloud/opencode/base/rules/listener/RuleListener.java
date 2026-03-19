package cloud.opencode.base.rules.listener;

import cloud.opencode.base.rules.Rule;
import cloud.opencode.base.rules.RuleContext;

/**
 * Rule Listener Interface - Observes Rule Execution Events
 * 规则监听器接口 - 观察规则执行事件
 *
 * <p>Receives notifications about rule execution lifecycle events.</p>
 * <p>接收关于规则执行生命周期事件的通知。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Before/after evaluation callbacks - 评估前/后回调</li>
 *   <li>Before/after execution callbacks - 执行前/后回调</li>
 *   <li>Failure notification - 失败通知</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * engine.addListener(new RuleListener() {
 *     @Override
 *     public void beforeEvaluate(Rule rule, RuleContext context) {
 *         System.out.println("Evaluating: " + rule.getName());
 *     }
 * });
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: No (rule and context must not be null) - 空值安全: 否（规则和上下文不能为null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
public interface RuleListener {

    /**
     * Called before a rule is evaluated
     * 在规则评估前调用
     *
     * @param rule    the rule | 规则
     * @param context the context | 上下文
     */
    default void beforeEvaluate(Rule rule, RuleContext context) {}

    /**
     * Called after a rule is evaluated
     * 在规则评估后调用
     *
     * @param rule      the rule | 规则
     * @param context   the context | 上下文
     * @param satisfied whether the condition was satisfied | 条件是否满足
     */
    default void afterEvaluate(Rule rule, RuleContext context, boolean satisfied) {}

    /**
     * Called before a rule is executed
     * 在规则执行前调用
     *
     * @param rule    the rule | 规则
     * @param context the context | 上下文
     */
    default void beforeExecute(Rule rule, RuleContext context) {}

    /**
     * Called after a rule is executed successfully
     * 在规则成功执行后调用
     *
     * @param rule    the rule | 规则
     * @param context the context | 上下文
     */
    default void afterExecute(Rule rule, RuleContext context) {}

    /**
     * Called when a rule execution fails
     * 当规则执行失败时调用
     *
     * @param rule      the rule | 规则
     * @param context   the context | 上下文
     * @param exception the exception | 异常
     */
    default void onFailure(Rule rule, RuleContext context, Exception exception) {}

    /**
     * Called when rule engine starts firing
     * 当规则引擎开始触发时调用
     *
     * @param context the context | 上下文
     */
    default void onStart(RuleContext context) {}

    /**
     * Called when rule engine finishes firing
     * 当规则引擎完成触发时调用
     *
     * @param context       the context | 上下文
     * @param firedCount    number of rules fired | 触发的规则数量
     * @param elapsedMillis execution time in milliseconds | 执行时间（毫秒）
     */
    default void onFinish(RuleContext context, int firedCount, long elapsedMillis) {}
}
