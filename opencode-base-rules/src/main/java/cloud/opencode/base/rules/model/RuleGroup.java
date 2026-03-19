package cloud.opencode.base.rules.model;

import cloud.opencode.base.rules.Rule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Rule Group - Collection of Related Rules
 * 规则组 - 相关规则的集合
 *
 * <p>Groups related rules together for organization and selective execution.</p>
 * <p>将相关规则组织在一起以便组织和选择性执行。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Named grouping - 命名分组</li>
 *   <li>Priority ordering - 优先级排序</li>
 *   <li>Description support - 描述支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * RuleGroup group = RuleGroup.builder("discount-rules")
 *     .description("Order discount calculation rules")
 *     .priority(1)
 *     .addRule(vipRule)
 *     .addRule(bulkRule)
 *     .build();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 *   <li>Null-safe: No (name must not be null) - 空值安全: 否（名称不能为null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
public final class RuleGroup implements Comparable<RuleGroup> {

    private final String name;
    private final String description;
    private final int priority;
    private final List<Rule> rules;

    private RuleGroup(String name, String description, int priority, List<Rule> rules) {
        this.name = name;
        this.description = description;
        this.priority = priority;
        this.rules = Collections.unmodifiableList(new ArrayList<>(rules));
    }

    /**
     * Gets the group name
     * 获取组名
     *
     * @return the group name | 组名
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the group description
     * 获取组描述
     *
     * @return the description | 描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the group priority
     * 获取组优先级
     *
     * @return the priority | 优先级
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Gets the rules in this group
     * 获取此组中的规则
     *
     * @return immutable list of rules | 不可变的规则列表
     */
    public List<Rule> getRules() {
        return rules;
    }

    /**
     * Gets the count of rules in this group
     * 获取此组中的规则数量
     *
     * @return rule count | 规则数量
     */
    public int size() {
        return rules.size();
    }

    /**
     * Checks if this group is empty
     * 检查此组是否为空
     *
     * @return true if empty | 如果为空返回true
     */
    public boolean isEmpty() {
        return rules.isEmpty();
    }

    @Override
    public int compareTo(RuleGroup other) {
        return Integer.compare(this.priority, other.priority);
    }

    /**
     * Creates a builder for RuleGroup
     * 创建RuleGroup的构建器
     *
     * @param name the group name | 组名
     * @return the builder | 构建器
     */
    public static Builder builder(String name) {
        return new Builder(name);
    }

    /**
     * Builder for RuleGroup
     * RuleGroup的构建器
     */
    public static final class Builder {
        private final String name;
        private String description;
        private int priority = Rule.DEFAULT_PRIORITY;
        private final List<Rule> rules = new ArrayList<>();

        private Builder(String name) {
            this.name = name;
        }

        /**
         * Sets the description
         * 设置描述
         *
         * @param description the description | 描述
         * @return this builder | 此构建器
         */
        public Builder description(String description) {
            this.description = description;
            return this;
        }

        /**
         * Sets the priority
         * 设置优先级
         *
         * @param priority the priority | 优先级
         * @return this builder | 此构建器
         */
        public Builder priority(int priority) {
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
        public Builder addRule(Rule rule) {
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
        public Builder addRules(Rule... rules) {
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
        public Builder addRules(List<Rule> rules) {
            this.rules.addAll(rules);
            return this;
        }

        /**
         * Builds the RuleGroup
         * 构建RuleGroup
         *
         * @return the rule group | 规则组
         */
        public RuleGroup build() {
            return new RuleGroup(name, description, priority, rules);
        }
    }
}
