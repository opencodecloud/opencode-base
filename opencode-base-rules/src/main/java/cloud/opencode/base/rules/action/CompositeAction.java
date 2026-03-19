package cloud.opencode.base.rules.action;

import cloud.opencode.base.rules.RuleContext;
import cloud.opencode.base.rules.model.Action;

import java.util.ArrayList;
import java.util.List;

/**
 * Composite Action - Executes Multiple Actions in Sequence
 * 组合动作 - 按顺序执行多个动作
 *
 * <p>Combines multiple actions into a single action that executes them sequentially.</p>
 * <p>将多个动作组合成一个按顺序执行的单一动作。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Sequential action execution - 顺序动作执行</li>
 *   <li>Error aggregation with suppressed exceptions - 使用抑制异常的错误聚合</li>
 *   <li>Factory method support - 工厂方法支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Action combined = new CompositeAction(List.of(
 *     ctx -> ctx.setResult("step1", "done"),
 *     ctx -> ctx.setResult("step2", "done")
 * ));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (not synchronized) - 线程安全: 否（未同步）</li>
 *   <li>Null-safe: No (actions must not be null) - 空值安全: 否（动作不能为null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-rules V1.0.0
 */
public class CompositeAction implements Action {

    private final List<Action> actions;

    /**
     * Creates a composite action
     * 创建组合动作
     *
     * @param actions the actions to execute | 要执行的动作
     */
    public CompositeAction(List<Action> actions) {
        this.actions = new ArrayList<>(actions);
    }

    @Override
    public void execute(RuleContext context) {
        List<Exception> errors = new ArrayList<>();
        for (Action action : actions) {
            try {
                action.execute(context);
            } catch (Exception e) {
                errors.add(e);
            }
        }
        if (!errors.isEmpty()) {
            RuntimeException primary = errors.getFirst() instanceof RuntimeException re
                ? re : new RuntimeException(errors.getFirst());
            for (int i = 1; i < errors.size(); i++) {
                primary.addSuppressed(errors.get(i));
            }
            throw primary;
        }
    }

    /**
     * Gets the actions
     * 获取动作列表
     *
     * @return the actions | 动作列表
     */
    public List<Action> getActions() {
        return List.copyOf(actions);
    }

    /**
     * Creates a composite action from multiple actions
     * 从多个动作创建组合动作
     *
     * @param actions the actions | 动作
     * @return the composite action | 组合动作
     */
    public static CompositeAction of(Action... actions) {
        return new CompositeAction(List.of(actions));
    }
}
