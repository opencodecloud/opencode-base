package cloud.opencode.base.string.template.node;

import java.util.List;

/**
 * If Node - Template node for conditional rendering.
 * 条件节点 - 用于条件渲染的模板节点。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Condition-based then/else branch rendering - 基于条件的then/else分支渲染</li>
 *   <li>Truthiness evaluation of context variables - 上下文变量真值评估</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * IfNode node = new IfNode("showName", thenNodes, elseNodes);
 * String result = node.render(Map.of("showName", true));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
public final class IfNode implements TemplateNode {
    private final String condition;
    private final List<TemplateNode> thenNodes;
    private final List<TemplateNode> elseNodes;

    public IfNode(String condition, List<TemplateNode> thenNodes, List<TemplateNode> elseNodes) {
        this.condition = condition;
        this.thenNodes = thenNodes;
        this.elseNodes = elseNodes;
    }

    @Override
    public String render(java.util.Map<String, Object> context) {
        boolean conditionResult = evaluateCondition(context);
        List<TemplateNode> nodesToRender = conditionResult ? thenNodes : elseNodes;
        
        StringBuilder sb = new StringBuilder();
        for (TemplateNode node : nodesToRender) {
            sb.append(node.render(context));
        }
        return sb.toString();
    }

    private boolean evaluateCondition(java.util.Map<String, Object> context) {
        Object value = context.get(condition);
        if (value instanceof Boolean b) return b;
        return value != null;
    }
}
