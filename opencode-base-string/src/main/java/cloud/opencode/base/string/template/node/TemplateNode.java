package cloud.opencode.base.string.template.node;

/**
 * Template Node - Sealed interface for template AST nodes.
 * 模板节点 - 模板AST节点的密封接口。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Sealed interface with permitted node types - 密封接口限定节点类型</li>
 *   <li>Context-based rendering - 基于上下文的渲染</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * TemplateNode node = new TextNode("Hello");
 * String result = node.render(Map.of());
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Depends on implementation - 线程安全: 取决于实现</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
public sealed interface TemplateNode 
    permits TextNode, VariableNode, IfNode, ForNode, IncludeNode {
    
    String render(java.util.Map<String, Object> context);
}
