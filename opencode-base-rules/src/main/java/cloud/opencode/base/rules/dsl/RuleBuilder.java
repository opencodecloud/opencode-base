package cloud.opencode.base.rules.dsl;

import cloud.opencode.base.rules.Rule;
import cloud.opencode.base.rules.RuleContext;
import cloud.opencode.base.rules.action.CompositeAction;
import cloud.opencode.base.rules.action.ConsumerAction;
import cloud.opencode.base.rules.condition.CompositeCondition;
import cloud.opencode.base.rules.condition.PredicateCondition;
import cloud.opencode.base.rules.engine.DefaultRule;
import cloud.opencode.base.rules.model.Action;
import cloud.opencode.base.rules.model.Condition;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Rule Builder - Fluent DSL for Rule Construction
 * 规则构建器 - 规则构建的流式DSL
 *
 * <p>Provides a fluent API for constructing rules with conditions and actions.</p>
 * <p>提供用于构建带条件和动作的规则的流式API。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fluent API - 流式API</li>
 *   <li>Multiple conditions (AND) - 多个条件（AND）</li>
 *   <li>Multiple actions - 多个动作</li>
 *   <li>Priority and grouping - 优先级和分组</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Rule rule = RuleBuilder.rule("vip-discount")
 *     .description("VIP customer discount rule")
 *     .priority(1)
 *     .group("discounts")
 *     .when(ctx -> ctx.get("customerType").equals("VIP"))
 *     .and(ctx -> ctx.<Double>get("amount") > 1000)
 *     .then(ctx -> ctx.setResult("discount", 0.15))
 *     .build();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (builder pattern, single-threaded use) - 线程安全: 否（构建器模式，单线程使用）</li>
 *   <li>Null-safe: No (conditions and actions must not be null) - 空值安全: 否（条件和动作不能为null）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) per condition/action addition - 每次条件/动作添加 O(1)</li>
 *   <li>Space complexity: O(c + a) where c = conditions, a = actions - O(c + a), c为条件数, a为动作数</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
public final class RuleBuilder {

    private final String name;
    private String description;
    private int priority = Rule.DEFAULT_PRIORITY;
    private String group;
    private boolean enabled = true;
    private boolean terminal = false;
    private final List<Condition> conditions = new ArrayList<>();
    private final List<Action> actions = new ArrayList<>();

    /**
     * Creates a rule builder with the given name
     * 使用给定名称创建规则构建器
     *
     * @param name the rule name | 规则名称
     */
    public RuleBuilder(String name) {
        this.name = name;
    }

    /**
     * Creates a rule builder with a default name
     * 使用默认名称创建规则构建器
     */
    public RuleBuilder() {
        this.name = "rule-" + System.nanoTime();
    }

    /**
     * Creates a rule builder with the given name
     * 使用给定名称创建规则构建器
     *
     * @param name the rule name | 规则名称
     * @return the builder | 构建器
     */
    public static RuleBuilder rule(String name) {
        return new RuleBuilder(name);
    }

    /**
     * Sets the rule description
     * 设置规则描述
     *
     * @param description the description | 描述
     * @return this builder | 此构建器
     */
    public RuleBuilder description(String description) {
        this.description = description;
        return this;
    }

    /**
     * Sets the rule priority (lower value = higher priority)
     * 设置规则优先级（值越小优先级越高）
     *
     * @param priority the priority | 优先级
     * @return this builder | 此构建器
     */
    public RuleBuilder priority(int priority) {
        this.priority = priority;
        return this;
    }

    /**
     * Sets the rule group
     * 设置规则分组
     *
     * @param group the group name | 组名
     * @return this builder | 此构建器
     */
    public RuleBuilder group(String group) {
        this.group = group;
        return this;
    }

    /**
     * Sets whether the rule is enabled
     * 设置规则是否启用
     *
     * @param enabled whether enabled | 是否启用
     * @return this builder | 此构建器
     */
    public RuleBuilder enabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Marks the rule as terminal (stops engine execution when fired)
     * 将规则标记为终止规则（触发时停止引擎执行）
     *
     * @return this builder | 此构建器
     */
    public RuleBuilder terminal() {
        this.terminal = true;
        return this;
    }

    /**
     * Sets whether the rule is terminal
     * 设置规则是否为终止规则
     *
     * @param terminal whether terminal | 是否为终止规则
     * @return this builder | 此构建器
     */
    public RuleBuilder terminal(boolean terminal) {
        this.terminal = terminal;
        return this;
    }

    /**
     * Adds a condition using a predicate
     * 使用谓词添加条件
     *
     * @param predicate the condition predicate | 条件谓词
     * @return this builder | 此构建器
     */
    public RuleBuilder when(Predicate<RuleContext> predicate) {
        conditions.add(new PredicateCondition(predicate));
        return this;
    }

    /**
     * Adds a condition object
     * 添加条件对象
     *
     * @param condition the condition | 条件
     * @return this builder | 此构建器
     */
    public RuleBuilder when(Condition condition) {
        conditions.add(condition);
        return this;
    }

    /**
     * Adds an additional AND condition
     * 添加额外的AND条件
     *
     * @param predicate the condition predicate | 条件谓词
     * @return this builder | 此构建器
     */
    public RuleBuilder and(Predicate<RuleContext> predicate) {
        conditions.add(new PredicateCondition(predicate));
        return this;
    }

    /**
     * Adds an additional AND condition object
     * 添加额外的AND条件对象
     *
     * @param condition the condition | 条件
     * @return this builder | 此构建器
     */
    public RuleBuilder and(Condition condition) {
        conditions.add(condition);
        return this;
    }

    /**
     * Adds an action using a consumer
     * 使用消费者添加动作
     *
     * @param action the action consumer | 动作消费者
     * @return this builder | 此构建器
     */
    public RuleBuilder then(Consumer<RuleContext> action) {
        actions.add(new ConsumerAction(action));
        return this;
    }

    /**
     * Adds an action object
     * 添加动作对象
     *
     * @param action the action | 动作
     * @return this builder | 此构建器
     */
    public RuleBuilder then(Action action) {
        actions.add(action);
        return this;
    }

    /**
     * Adds an additional action
     * 添加额外的动作
     *
     * @param action the action consumer | 动作消费者
     * @return this builder | 此构建器
     */
    public RuleBuilder andThen(Consumer<RuleContext> action) {
        actions.add(new ConsumerAction(action));
        return this;
    }

    /**
     * Adds an additional action object
     * 添加额外的动作对象
     *
     * @param action the action | 动作
     * @return this builder | 此构建器
     */
    public RuleBuilder andThen(Action action) {
        actions.add(action);
        return this;
    }

    /**
     * Builds the rule
     * 构建规则
     *
     * @return the constructed rule | 构建的规则
     * @throws IllegalStateException if no condition or action is set | 如果未设置条件或动作则抛出
     */
    public Rule build() {
        if (conditions.isEmpty()) {
            throw new IllegalStateException("Rule must have at least one condition");
        }
        if (actions.isEmpty()) {
            throw new IllegalStateException("Rule must have at least one action");
        }

        Condition finalCondition = conditions.size() == 1
                ? conditions.getFirst()
                : new CompositeCondition(CompositeCondition.Operator.AND, conditions);

        Action finalAction = actions.size() == 1
                ? actions.getFirst()
                : new CompositeAction(actions);

        return new DefaultRule(name, description, priority, group, enabled, finalCondition, finalAction, terminal);
    }
}
