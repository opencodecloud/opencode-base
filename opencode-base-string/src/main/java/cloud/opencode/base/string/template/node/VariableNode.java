package cloud.opencode.base.string.template.node;

/**
 * Variable Node - Template node for variable substitution.
 * 变量节点 - 用于变量替换的模板节点。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Variable lookup with default value fallback - 变量查找带默认值回退</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * VariableNode node = new VariableNode("name", "World");
 * String result = node.render(Map.of("name", "Java")); // "Java"
 * String def = node.render(Map.of());                   // "World"
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
 * @since JDK 25, opencode-base-string V1.0.0
 */
public final class VariableNode implements TemplateNode {
    private final String variableName;
    private final String defaultValue;

    public VariableNode(String variableName, String defaultValue) {
        this.variableName = variableName;
        this.defaultValue = defaultValue;
    }

    @Override
    public String render(java.util.Map<String, Object> context) {
        Object value = context.get(variableName);
        if (value != null) {
            return value.toString();
        }
        return defaultValue != null ? defaultValue : "";
    }
}
