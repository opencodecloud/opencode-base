package cloud.opencode.base.string.template.node;

/**
 * Text Node - Template node for static text content.
 * 文本节点 - 用于静态文本内容的模板节点。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Static text output - 静态文本输出</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * TextNode node = new TextNode("Hello World");
 * String result = node.render(Map.of()); // "Hello World"
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
public final class TextNode implements TemplateNode {
    private final String text;

    public TextNode(String text) {
        this.text = text;
    }

    @Override
    public String render(java.util.Map<String, Object> context) {
        return text;
    }
}
