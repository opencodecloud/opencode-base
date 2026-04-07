package cloud.opencode.base.rules.engine;

import cloud.opencode.base.rules.Rule;
import cloud.opencode.base.rules.RuleContext;
import cloud.opencode.base.rules.model.Action;
import cloud.opencode.base.rules.model.Condition;

import java.util.Objects;

/**
 * Default Rule Implementation
 * 默认规则实现
 *
 * <p>Standard implementation of the Rule interface with condition and action.</p>
 * <p>带条件和动作的规则接口标准实现。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable rule configuration - 不可变规则配置</li>
 *   <li>Priority and group support - 优先级和分组支持</li>
 *   <li>Enable/disable toggle - 启用/禁用开关</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Rule rule = new DefaultRule("discount", "VIP discount", 1, "pricing",
 *     true,
 *     ctx -> "VIP".equals(ctx.get("type")),
 *     ctx -> ctx.setResult("discount", 0.15));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
public class DefaultRule implements Rule {

    private final String name;
    private final String description;
    private final int priority;
    private final String group;
    private final boolean enabled;
    private final Condition condition;
    private final Action action;
    private final boolean terminal;

    /**
     * Creates a default rule
     * 创建默认规则
     *
     * @param name        the rule name | 规则名称
     * @param description the description | 描述
     * @param priority    the priority | 优先级
     * @param group       the group | 分组
     * @param enabled     whether enabled | 是否启用
     * @param condition   the condition | 条件
     * @param action      the action | 动作
     */
    public DefaultRule(String name, String description, int priority, String group,
                       boolean enabled, Condition condition, Action action) {
        this(name, description, priority, group, enabled, condition, action, false);
    }

    /**
     * Creates a default rule with terminal flag
     * 创建带终止标志的默认规则
     *
     * @param name        the rule name | 规则名称
     * @param description the description | 描述
     * @param priority    the priority | 优先级
     * @param group       the group | 分组
     * @param enabled     whether enabled | 是否启用
     * @param condition   the condition | 条件
     * @param action      the action | 动作
     * @param terminal    whether the rule is terminal | 是否为终止规则
     * @since JDK 25, opencode-base-rules V1.0.3
     */
    public DefaultRule(String name, String description, int priority, String group,
                       boolean enabled, Condition condition, Action action, boolean terminal) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.description = description;
        this.priority = priority;
        this.group = group;
        this.enabled = enabled;
        this.condition = Objects.requireNonNull(condition, "condition must not be null");
        this.action = Objects.requireNonNull(action, "action must not be null");
        this.terminal = terminal;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean evaluate(RuleContext context) {
        return condition.evaluate(context);
    }

    @Override
    public void execute(RuleContext context) {
        action.execute(context);
    }

    /**
     * Gets the condition
     * 获取条件
     *
     * @return the condition | 条件
     */
    public Condition getCondition() {
        return condition;
    }

    /**
     * Gets the action
     * 获取动作
     *
     * @return the action | 动作
     */
    public Action getAction() {
        return action;
    }

    @Override
    public boolean isTerminal() {
        return terminal;
    }

    @Override
    public String toString() {
        if (terminal) {
            return "Rule{name='" + name + "', priority=" + priority + ", group='" + group + "', terminal=true}";
        }
        return "Rule{name='" + name + "', priority=" + priority + ", group='" + group + "'}";
    }
}
