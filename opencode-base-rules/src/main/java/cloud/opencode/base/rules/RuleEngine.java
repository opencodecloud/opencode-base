package cloud.opencode.base.rules;

import cloud.opencode.base.rules.conflict.ConflictResolver;
import cloud.opencode.base.rules.listener.RuleListener;
import cloud.opencode.base.rules.model.RuleGroup;
import cloud.opencode.base.rules.trace.ExecutionTrace;
import cloud.opencode.base.rules.trace.TracingRuleListener;

import java.util.List;

/**
 * Rule Engine Interface - Manages and Executes Business Rules
 * 规则引擎接口 - 管理和执行业务规则
 *
 * <p>The core component for registering, managing, and executing business rules.
 * Supports rule groups, conflict resolution, and execution listeners.</p>
 * <p>注册、管理和执行业务规则的核心组件。支持规则分组、冲突解决和执行监听器。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Rule registration and management - 规则注册和管理</li>
 *   <li>Multiple firing modes (all, first, until halt) - 多种触发模式</li>
 *   <li>Rule group support - 规则分组支持</li>
 *   <li>Conflict resolution - 冲突解决</li>
 *   <li>Execution listeners - 执行监听器</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create and configure engine
 * RuleEngine engine = OpenRules.engine()
 *     .register(rule1, rule2)
 *     .setConflictResolver(OpenRules.priorityResolver())
 *     .addListener(OpenRules.loggingListener())
 *     .build();
 *
 * // Fire all matching rules
 * RuleResult result = engine.fire(context);
 *
 * // Fire only first matching rule
 * RuleResult result = engine.fireFirst(context);
 *
 * // Fire rules in a specific group
 * RuleResult result = engine.fire(context, "discount-rules");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation dependent - 线程安全: 取决于实现</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see Rule
 * @see RuleContext
 * @see RuleResult
 * @since JDK 25, opencode-base-rules V1.0.0
 */
public interface RuleEngine {

    /**
     * Registers one or more rules
     * 注册一个或多个规则
     *
     * @param rules the rules to register | 要注册的规则
     * @return this engine for chaining | 此引擎用于链式调用
     */
    RuleEngine register(Rule... rules);

    /**
     * Registers a rule group
     * 注册规则组
     *
     * @param group the rule group | 规则组
     * @return this engine for chaining | 此引擎用于链式调用
     */
    RuleEngine register(RuleGroup group);

    /**
     * Unregisters a rule by name
     * 按名称注销规则
     *
     * @param ruleName the rule name | 规则名称
     * @return this engine for chaining | 此引擎用于链式调用
     */
    RuleEngine unregister(String ruleName);

    /**
     * Fires all matching rules
     * 触发所有匹配的规则
     *
     * @param context the rule context | 规则上下文
     * @return the execution result | 执行结果
     */
    RuleResult fire(RuleContext context);

    /**
     * Fires all matching rules in a specific group
     * 触发特定分组中所有匹配的规则
     *
     * @param context the rule context | 规则上下文
     * @param group   the rule group name | 规则组名称
     * @return the execution result | 执行结果
     */
    RuleResult fire(RuleContext context, String group);

    /**
     * Fires only the first matching rule
     * 只触发第一个匹配的规则
     *
     * @param context the rule context | 规则上下文
     * @return the execution result | 执行结果
     */
    RuleResult fireFirst(RuleContext context);

    /**
     * Fires rules until no more rules can fire (for inference)
     * 触发规则直到没有更多规则可以触发（用于推理）
     *
     * @param context the rule context | 规则上下文
     * @return the execution result | 执行结果
     */
    RuleResult fireUntilHalt(RuleContext context);

    /**
     * Gets all registered rules
     * 获取所有已注册的规则
     *
     * @return list of rules | 规则列表
     */
    List<Rule> getRules();

    /**
     * Gets rules in a specific group
     * 获取特定分组中的规则
     *
     * @param group the group name | 组名
     * @return list of rules in the group | 组中的规则列表
     */
    List<Rule> getRules(String group);

    /**
     * Gets a rule by name
     * 按名称获取规则
     *
     * @param name the rule name | 规则名称
     * @return the rule, or null if not found | 规则，如果未找到则为null
     */
    Rule getRule(String name);

    /**
     * Checks if a rule is registered
     * 检查规则是否已注册
     *
     * @param name the rule name | 规则名称
     * @return true if registered | 如果已注册返回true
     */
    boolean hasRule(String name);

    /**
     * Gets the count of registered rules
     * 获取已注册规则的数量
     *
     * @return rule count | 规则数量
     */
    int getRuleCount();

    /**
     * Adds a rule execution listener
     * 添加规则执行监听器
     *
     * @param listener the listener | 监听器
     * @return this engine for chaining | 此引擎用于链式调用
     */
    RuleEngine addListener(RuleListener listener);

    /**
     * Removes a rule execution listener
     * 移除规则执行监听器
     *
     * @param listener the listener | 监听器
     * @return this engine for chaining | 此引擎用于链式调用
     */
    RuleEngine removeListener(RuleListener listener);

    /**
     * Sets the conflict resolver
     * 设置冲突解决器
     *
     * @param resolver the conflict resolver | 冲突解决器
     * @return this engine for chaining | 此引擎用于链式调用
     */
    RuleEngine setConflictResolver(ConflictResolver resolver);

    /**
     * Clears all registered rules
     * 清除所有已注册的规则
     */
    void clear();

    /**
     * Fires all matching rules and returns an execution trace
     * 触发所有匹配规则并返回执行轨迹
     *
     * <p><strong>Note:</strong> This method temporarily adds and removes a tracing listener.
     * It is not safe for concurrent invocation on the same engine instance.</p>
     * <p><strong>注意:</strong> 此方法会临时添加和移除追踪监听器。
     * 不支持在同一引擎实例上并发调用。</p>
     *
     * @param context the rule context | 规则上下文
     * @return the execution trace | 执行轨迹
     * @since JDK 25, opencode-base-rules V1.0.3
     */
    default ExecutionTrace fireAndTrace(RuleContext context) {
        TracingRuleListener tracer = new TracingRuleListener();
        addListener(tracer);
        try {
            fire(context);
            return tracer.getTrace();
        } finally {
            removeListener(tracer);
        }
    }
}
