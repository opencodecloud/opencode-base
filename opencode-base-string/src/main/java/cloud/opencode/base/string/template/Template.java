package cloud.opencode.base.string.template;

import cloud.opencode.base.string.template.node.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Template - Core template implementation with node-based rendering.
 * 模板 - 基于节点渲染的核心模板实现。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>AST-based template compilation - 基于AST的模板编译</li>
 *   <li>Variable substitution with default values - 变量替换带默认值</li>
 *   <li>Compiled template reuse - 编译模板复用</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Template tpl = Template.compile("Hello ${name:World}!");
 * String result = tpl.render(Map.of("name", "Java")); // "Hello Java!"
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after compilation) - 线程安全: 是（编译后不可变）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
public final class Template {
    private static final Pattern VAR_DELIMITER_PATTERN = Pattern.compile("(\\$\\{|\\})");

    private final String source;
    private final List<TemplateNode> nodes;

    private Template(String source, List<TemplateNode> nodes) {
        this.source = source;
        this.nodes = nodes;
    }

    public static Template compile(String template) {
        // Simplified compilation - just handle variables
        List<TemplateNode> nodes = new ArrayList<>();
        
        String[] parts = VAR_DELIMITER_PATTERN.split(template);
        boolean isVariable = false;
        
        for (String part : parts) {
            if (part.isEmpty()) continue;
            
            if (isVariable) {
                String[] varParts = part.split(":", 2);
                String varName = varParts[0].trim();
                String defaultVal = varParts.length > 1 ? varParts[1].trim() : null;
                nodes.add(new VariableNode(varName, defaultVal));
            } else {
                if (part.contains("${")) {
                    // Mixed content
                    nodes.add(new TextNode(part));
                } else {
                    nodes.add(new TextNode(part));
                }
            }
            
            isVariable = !isVariable;
        }
        
        return new Template(template, nodes);
    }

    public String render(Map<String, Object> context) {
        StringBuilder sb = new StringBuilder();
        for (TemplateNode node : nodes) {
            sb.append(node.render(context));
        }
        return sb.toString();
    }

    public String render(TemplateContext context) {
        return render(context.getVariables());
    }

    public String getSource() {
        return source;
    }
}
