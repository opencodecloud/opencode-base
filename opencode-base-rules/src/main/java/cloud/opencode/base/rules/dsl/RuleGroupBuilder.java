package cloud.opencode.base.rules.dsl;

import cloud.opencode.base.rules.Rule;
import cloud.opencode.base.rules.model.RuleGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Rule Group Builder - Fluent DSL for Rule Group Construction
 * 规则组构建器 - 规则组构建的流式DSL
 *
 * <p>Provides a fluent API for constructing rule groups.</p>
 * <p>提供用于构建规则组的流式API。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fluent builder API - 流式构建器API</li>
 *   <li>Rule collection management - 规则集合管理</li>
 *   <li>Priority and description support - 优先级和描述支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * RuleGroup group = RuleGroupBuilder.group("discount-rules")
 *     .description("Order discount calculation rules")
 *     .priority(1)
 *     .addRule(vipRule)
 *     .addRule(bulkRule)
 *     .build();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (builder pattern, single-threaded use) - 线程安全: 否（构建器模式，单线程使用）</li>
 *   <li>Null-safe: No (name and rules must not be null) - 空值安全: 否（名称和规则不能为null）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) per rule addition - 每次规则添加 O(1)</li>
 *   <li>Space complexity: O(n) where n = rules in group - O(n), n为组内规则数</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
public final class RuleGroupBuilder {

    private final String name;
    private String description;
    private int priority = Rule.DEFAULT_PRIORITY;
    private final List<Rule> rules = new ArrayList<>();

    /**
     * Creates a rule group builder with the given name
     * 使用给定名称创建规则组构建器
     *
     * @param name the group name | 组名
     */
    public RuleGroupBuilder(String name) {
        this.name = name;
    }

    /**
     * Creates a rule group builder with the given name
     * 使用给定名称创建规则组构建器
     *
     * @param name the group name | 组名
     * @return the builder | 构建器
     */
    public static RuleGroupBuilder group(String name) {
        return new RuleGroupBuilder(name);
    }

    /**
     * Sets the group description
     * 设置组描述
     *
     * @param description the description | 描述
     * @return this builder | 此构建器
     */
    public RuleGroupBuilder description(String description) {
        this.description = description;
        return this;
    }

    /**
     * Sets the group priority
     * 设置组优先级
     *
     * @param priority the priority | 优先级
     * @return this builder | 此构建器
     */
    public RuleGroupBuilder priority(int priority) {
        this.priority = priority;
        return this;
    }

    /**
     * Adds a rule to the group
     * 向组添加规则
     *
     * @param rule the rule | 规则
     * @return this builder | 此构建器
     */
    public RuleGroupBuilder addRule(Rule rule) {
        this.rules.add(rule);
        return this;
    }

    /**
     * Adds multiple rules to the group
     * 向组添加多个规则
     *
     * @param rules the rules | 规则
     * @return this builder | 此构建器
     */
    public RuleGroupBuilder addRules(Rule... rules) {
        Collections.addAll(this.rules, rules);
        return this;
    }

    /**
     * Adds multiple rules to the group
     * 向组添加多个规则
     *
     * @param rules the rules | 规则
     * @return this builder | 此构建器
     */
    public RuleGroupBuilder addRules(List<Rule> rules) {
        this.rules.addAll(rules);
        return this;
    }

    /**
     * Builds the rule group
     * 构建规则组
     *
     * @return the rule group | 规则组
     */
    public RuleGroup build() {
        return RuleGroup.builder(name)
                .description(description)
                .priority(priority)
                .addRules(rules)
                .build();
    }
}
