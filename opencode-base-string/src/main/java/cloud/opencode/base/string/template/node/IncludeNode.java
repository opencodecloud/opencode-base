package cloud.opencode.base.string.template.node;

/**
 * Include Node - Template node for including other templates.
 * 包含节点 - 用于包含其他模板的模板节点。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Template inclusion by name - 按名称包含模板</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * IncludeNode node = new IncludeNode("header");
 * String result = node.render(context);
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
public final class IncludeNode implements TemplateNode {
    private final String templateName;

    public IncludeNode(String templateName) {
        this.templateName = templateName;
    }

    @Override
    public String render(java.util.Map<String, Object> context) {
        // Simplified implementation - would load and render the included template
        return "<!-- include: " + templateName + " -->";
    }
}
