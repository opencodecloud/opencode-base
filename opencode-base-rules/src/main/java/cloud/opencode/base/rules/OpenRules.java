package cloud.opencode.base.rules;

import cloud.opencode.base.rules.conflict.ConflictResolver;
import cloud.opencode.base.rules.conflict.OrderConflictResolver;
import cloud.opencode.base.rules.conflict.PriorityConflictResolver;
import cloud.opencode.base.rules.decision.DecisionTable;
import cloud.opencode.base.rules.decision.HitPolicy;
import cloud.opencode.base.rules.dsl.DecisionTableBuilder;
import cloud.opencode.base.rules.dsl.RuleBuilder;
import cloud.opencode.base.rules.dsl.RuleEngineBuilder;
import cloud.opencode.base.rules.dsl.RuleGroupBuilder;
import cloud.opencode.base.rules.engine.DefaultRuleEngine;
import cloud.opencode.base.rules.listener.LoggingRuleListener;
import cloud.opencode.base.rules.listener.RuleListener;
import cloud.opencode.base.rules.model.Action;
import cloud.opencode.base.rules.model.Condition;
import cloud.opencode.base.rules.model.RuleGroup;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * OpenRules Facade - Unified Entry Point for Rules Engine
 * OpenRules门面 - 规则引擎统一入口
 *
 * <p>Provides factory methods and utilities for creating rules, rule engines,
 * decision tables, and related components.</p>
 * <p>提供创建规则、规则引擎、决策表和相关组件的工厂方法和工具。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Rule creation via fluent DSL - 通过流式DSL创建规则</li>
 *   <li>Rule engine configuration - 规则引擎配置</li>
 *   <li>Decision table building - 决策表构建</li>
 *   <li>Conflict resolution strategies - 冲突解决策略</li>
 *   <li>Rule execution listeners - 规则执行监听器</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create a simple rule
 * Rule discountRule = OpenRules.rule("discount-rule")
 *     .description("Apply discount for VIP customers")
 *     .priority(100)
 *     .when(ctx -> "VIP".equals(ctx.get("customerType")))
 *     .then(ctx -> ctx.put("discount", 0.15))
 *     .build();
 *
 * // Create and configure rule engine
 * RuleEngine engine = OpenRules.engine()
 *     .register(discountRule)
 *     .setConflictResolver(OpenRules.priorityResolver())
 *     .addListener(OpenRules.loggingListener())
 *     .build();
 *
 * // Create and evaluate decision table
 * DecisionTable table = OpenRules.decisionTable()
 *     .name("pricing")
 *     .hitPolicy(HitPolicy.FIRST)
 *     .input("customerType", String.class)
 *     .input("amount", Double.class)
 *     .output("discount", Double.class)
 *     .row(new Object[]{"VIP", ">= 1000"}, new Object[]{0.15})
 *     .row(new Object[]{"VIP", "-"}, new Object[]{0.10})
 *     .row(new Object[]{"-", "-"}, new Object[]{0.0})
 *     .build();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: No (arguments must not be null) - 空值安全: 否（参数不能为null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
public final class OpenRules {

    private OpenRules() {
        // Utility class, prevent instantiation
    }

    // ==================== Rule Creation ====================

    /**
     * Creates a new rule builder with the given name
     * 使用给定名称创建新的规则构建器
     *
     * @param name the rule name | 规则名称
     * @return the rule builder | 规则构建器
     */
    public static RuleBuilder rule(String name) {
        return new RuleBuilder(name);
    }

    /**
     * Creates a new rule builder
     * 创建新的规则构建器
     *
     * @return the rule builder | 规则构建器
     */
    public static RuleBuilder rule() {
        return new RuleBuilder();
    }

    // ==================== Rule Group Creation ====================

    /**
     * Creates a new rule group builder with the given name
     * 使用给定名称创建新的规则组构建器
     *
     * @param name the group name | 组名称
     * @return the rule group builder | 规则组构建器
     */
    public static RuleGroupBuilder group(String name) {
        return new RuleGroupBuilder(name);
    }

    // ==================== Engine Creation ====================

    /**
     * Creates a new rule engine builder
     * 创建新的规则引擎构建器
     *
     * @return the rule engine builder | 规则引擎构建器
     */
    public static RuleEngineBuilder engine() {
        return new RuleEngineBuilder();
    }

    /**
     * Creates a new default rule engine
     * 创建新的默认规则引擎
     *
     * @return the rule engine | 规则引擎
     */
    public static RuleEngine defaultEngine() {
        return new DefaultRuleEngine();
    }

    /**
     * Creates a rule engine with the given rules
     * 使用给定规则创建规则引擎
     *
     * @param rules the rules to register | 要注册的规则
     * @return the rule engine | 规则引擎
     */
    public static RuleEngine engineWith(Rule... rules) {
        RuleEngine engine = new DefaultRuleEngine();
        engine.register(rules);
        return engine;
    }

    /**
     * Creates a rule engine with the given rule group
     * 使用给定规则组创建规则引擎
     *
     * @param group the rule group to register | 要注册的规则组
     * @return the rule engine | 规则引擎
     */
    public static RuleEngine engineWith(RuleGroup group) {
        RuleEngine engine = new DefaultRuleEngine();
        engine.register(group);
        return engine;
    }

    // ==================== Decision Table Creation ====================

    /**
     * Creates a new decision table builder
     * 创建新的决策表构建器
     *
     * @return the decision table builder | 决策表构建器
     */
    public static DecisionTableBuilder decisionTable() {
        return new DecisionTableBuilder();
    }

    /**
     * Creates a new decision table builder with the given name
     * 使用给定名称创建新的决策表构建器
     *
     * @param name the table name | 表名称
     * @return the decision table builder | 决策表构建器
     */
    public static DecisionTableBuilder decisionTable(String name) {
        return new DecisionTableBuilder().name(name);
    }

    // ==================== Context Creation ====================

    /**
     * Creates a new empty rule context
     * 创建新的空规则上下文
     *
     * @return the rule context | 规则上下文
     */
    public static RuleContext context() {
        return RuleContext.create();
    }

    /**
     * Creates a rule context with initial facts
     * 使用初始事实创建规则上下文
     *
     * @param keyValues alternating key-value pairs | 交替的键值对
     * @return the rule context | 规则上下文
     */
    public static RuleContext contextOf(Object... keyValues) {
        return RuleContext.of(keyValues);
    }

    // ==================== Condition Factories ====================

    /**
     * Creates a condition from a predicate
     * 从谓词创建条件
     *
     * @param predicate the predicate | 谓词
     * @return the condition | 条件
     */
    public static Condition condition(Predicate<RuleContext> predicate) {
        return predicate::test;
    }

    /**
     * Creates an always-true condition
     * 创建始终为真的条件
     *
     * @return the condition | 条件
     */
    public static Condition alwaysTrue() {
        return ctx -> true;
    }

    /**
     * Creates an always-false condition
     * 创建始终为假的条件
     *
     * @return the condition | 条件
     */
    public static Condition alwaysFalse() {
        return ctx -> false;
    }

    // ==================== Action Factories ====================

    /**
     * Creates an action from a consumer
     * 从消费者创建动作
     *
     * @param consumer the consumer | 消费者
     * @return the action | 动作
     */
    public static Action action(Consumer<RuleContext> consumer) {
        return consumer::accept;
    }

    /**
     * Creates a no-op action
     * 创建无操作动作
     *
     * @return the action | 动作
     */
    public static Action noOp() {
        return ctx -> {};
    }

    // ==================== Conflict Resolvers ====================

    /**
     * Gets the priority-based conflict resolver
     * 获取基于优先级的冲突解决器
     *
     * <p>Rules are ordered by priority (lower number = higher priority).</p>
     * <p>规则按优先级排序（数字越小 = 优先级越高）。</p>
     *
     * @return the priority conflict resolver | 优先级冲突解决器
     */
    public static ConflictResolver priorityResolver() {
        return PriorityConflictResolver.INSTANCE;
    }

    /**
     * Gets the order-based conflict resolver
     * 获取基于顺序的冲突解决器
     *
     * <p>Rules are kept in registration order.</p>
     * <p>规则保持注册顺序。</p>
     *
     * @return the order conflict resolver | 顺序冲突解决器
     */
    public static ConflictResolver orderResolver() {
        return OrderConflictResolver.INSTANCE;
    }

    // ==================== Listeners ====================

    /**
     * Creates a logging rule listener
     * 创建日志规则监听器
     *
     * @return the logging listener | 日志监听器
     */
    public static RuleListener loggingListener() {
        return new LoggingRuleListener();
    }

    // ==================== Utility Methods ====================

    /**
     * Gets the library version
     * 获取库版本
     *
     * @return the version string | 版本字符串
     */
    public static String version() {
        return "1.0.0";
    }

    /**
     * Gets the library information
     * 获取库信息
     *
     * @return the library info | 库信息
     */
    public static String info() {
        return "opencode-base-rules v" + version() + " - Lightweight Rule Engine for Java";
    }
}
