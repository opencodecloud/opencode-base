package cloud.opencode.base.rules.model;

import cloud.opencode.base.rules.RuleContext;

/**
 * Action Interface - Rule Action Abstraction
 * 动作接口 - 规则动作抽象
 *
 * <p>Represents an action to be executed when a rule fires.</p>
 * <p>表示规则触发时要执行的动作。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Context modification - 上下文修改</li>
 *   <li>Result setting - 结果设置</li>
 *   <li>Chainable execution - 可链式执行</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Simple action
 * Action action = ctx -> ctx.setResult("discount", 0.1);
 *
 * // Chained actions
 * Action combined = action1.andThen(action2);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: No (context must not be null) - 空值安全: 否（上下文不能为null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
@FunctionalInterface
public interface Action {

    /**
     * Executes this action with the given context
     * 使用给定上下文执行此动作
     *
     * @param context the rule context | 规则上下文
     */
    void execute(RuleContext context);

    /**
     * Returns an action that executes this action followed by another
     * 返回先执行此动作然后执行另一个动作的动作
     *
     * @param after the action to execute after this | 在此之后执行的动作
     * @return the combined action | 组合动作
     */
    default Action andThen(Action after) {
        return ctx -> {
            this.execute(ctx);
            after.execute(ctx);
        };
    }

    /**
     * An action that does nothing
     * 什么都不做的动作
     *
     * @return the no-op action | 空操作动作
     */
    static Action noOp() {
        return ctx -> {};
    }
}
