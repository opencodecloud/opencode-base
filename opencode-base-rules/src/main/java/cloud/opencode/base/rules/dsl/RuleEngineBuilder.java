package cloud.opencode.base.rules.dsl;

import cloud.opencode.base.rules.Rule;
import cloud.opencode.base.rules.RuleEngine;
import cloud.opencode.base.rules.conflict.ConflictResolver;
import cloud.opencode.base.rules.engine.DefaultRuleEngine;
import cloud.opencode.base.rules.listener.RuleListener;
import cloud.opencode.base.rules.model.RuleGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Rule Engine Builder - Fluent DSL for Rule Engine Configuration
 * 规则引擎构建器 - 规则引擎配置的流式DSL
 *
 * <p>Provides a fluent API for constructing and configuring rule engines.</p>
 * <p>提供用于构建和配置规则引擎的流式API。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Rule and group registration - 规则和组注册</li>
 *   <li>Conflict resolver configuration - 冲突解决器配置</li>
 *   <li>Listener configuration - 监听器配置</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * RuleEngine engine = new RuleEngineBuilder()
 *     .register(rule1, rule2)
 *     .setConflictResolver(OpenRules.priorityResolver())
 *     .addListener(new LoggingRuleListener())
 *     .build();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (builder pattern, single-threaded use) - 线程安全: 否（构建器模式，单线程使用）</li>
 *   <li>Null-safe: No (rules and listeners must not be null) - 空值安全: 否（规则和监听器不能为null）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) per rule addition - 每次规则添加 O(1)</li>
 *   <li>Space complexity: O(n) where n = rules - O(n), n为规则数</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
public final class RuleEngineBuilder {

    private final List<Rule> rules = new ArrayList<>();
    private final List<RuleGroup> groups = new ArrayList<>();
    private final List<RuleListener> listeners = new ArrayList<>();
    private ConflictResolver conflictResolver;

    /**
     * Registers one or more rules
     * 注册一个或多个规则
     *
     * @param rules the rules | 规则
     * @return this builder | 此构建器
     */
    public RuleEngineBuilder register(Rule... rules) {
        Collections.addAll(this.rules, rules);
        return this;
    }

    /**
     * Registers a rule group
     * 注册规则组
     *
     * @param group the rule group | 规则组
     * @return this builder | 此构建器
     */
    public RuleEngineBuilder register(RuleGroup group) {
        this.groups.add(group);
        return this;
    }

    /**
     * Sets the conflict resolver
     * 设置冲突解决器
     *
     * @param resolver the conflict resolver | 冲突解决器
     * @return this builder | 此构建器
     */
    public RuleEngineBuilder setConflictResolver(ConflictResolver resolver) {
        this.conflictResolver = resolver;
        return this;
    }

    /**
     * Adds a rule listener
     * 添加规则监听器
     *
     * @param listener the listener | 监听器
     * @return this builder | 此构建器
     */
    public RuleEngineBuilder addListener(RuleListener listener) {
        this.listeners.add(listener);
        return this;
    }

    /**
     * Builds the rule engine
     * 构建规则引擎
     *
     * @return the configured rule engine | 配置的规则引擎
     */
    public RuleEngine build() {
        RuleEngine engine = new DefaultRuleEngine();

        // Register rules
        if (!rules.isEmpty()) {
            engine.register(rules.toArray(new Rule[0]));
        }

        // Register groups
        for (RuleGroup group : groups) {
            engine.register(group);
        }

        // Set conflict resolver
        if (conflictResolver != null) {
            engine.setConflictResolver(conflictResolver);
        }

        // Add listeners
        for (RuleListener listener : listeners) {
            engine.addListener(listener);
        }

        return engine;
    }
}
