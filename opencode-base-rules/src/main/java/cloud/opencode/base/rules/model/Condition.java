package cloud.opencode.base.rules.model;

import cloud.opencode.base.rules.RuleContext;

/**
 * Condition Interface - Rule Condition Abstraction
 * 条件接口 - 规则条件抽象
 *
 * <p>Represents a condition that can be evaluated against a rule context
 * to determine if a rule should fire.</p>
 * <p>表示可以针对规则上下文评估的条件，以确定规则是否应该触发。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Context-based evaluation - 基于上下文的评估</li>
 *   <li>Composable (AND, OR, NOT) - 可组合（AND、OR、NOT）</li>
 *   <li>Functional interface - 函数式接口</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Lambda condition
 * Condition c1 = ctx -> ctx.get("age") > 18;
 *
 * // Composite conditions
 * Condition c2 = c1.and(ctx -> ctx.get("verified"));
 * Condition c3 = c1.or(ctx -> ctx.get("admin"));
 * Condition c4 = c1.negate();
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
public interface Condition {

    /**
     * Evaluates this condition against the given context
     * 针对给定上下文评估此条件
     *
     * @param context the rule context | 规则上下文
     * @return true if the condition is satisfied | 如果条件满足返回true
     */
    boolean evaluate(RuleContext context);

    /**
     * Returns a condition that is the logical AND of this and another
     * 返回此条件与另一个条件的逻辑AND
     *
     * @param other the other condition | 另一个条件
     * @return the combined condition | 组合条件
     */
    default Condition and(Condition other) {
        return ctx -> this.evaluate(ctx) && other.evaluate(ctx);
    }

    /**
     * Returns a condition that is the logical OR of this and another
     * 返回此条件与另一个条件的逻辑OR
     *
     * @param other the other condition | 另一个条件
     * @return the combined condition | 组合条件
     */
    default Condition or(Condition other) {
        return ctx -> this.evaluate(ctx) || other.evaluate(ctx);
    }

    /**
     * Returns a condition that is the logical negation of this
     * 返回此条件的逻辑否定
     *
     * @return the negated condition | 否定条件
     */
    default Condition negate() {
        return ctx -> !this.evaluate(ctx);
    }

    /**
     * A condition that always returns true
     * 始终返回true的条件
     *
     * @return the always-true condition | 始终为true的条件
     */
    static Condition alwaysTrue() {
        return ctx -> true;
    }

    /**
     * A condition that always returns false
     * 始终返回false的条件
     *
     * @return the always-false condition | 始终为false的条件
     */
    static Condition alwaysFalse() {
        return ctx -> false;
    }
}
