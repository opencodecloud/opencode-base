package cloud.opencode.base.string.template;

import java.util.*;
import java.util.regex.*;

/**
 * Template Utility - Provides template helper methods.
 * 模板工具 - 提供模板辅助方法。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Simple ${var} template rendering - 简单${var}模板渲染</li>
 *   <li>Variable extraction from templates - 从模板提取变量</li>
 *   <li>Default value support via ${var:default} - 默认值支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * String result = TemplateUtil.render("Hello ${name}!", Map.of("name", "World"));
 * Set<String> vars = TemplateUtil.extractVariables("${a} and ${b}");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Yes (returns null for null template) - 空值安全: 是</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n + v) where n = template length, v = variables - O(n + v), n为模板长度, v为变量数</li>
 *   <li>Space complexity: O(n) for rendered output - 渲染输出 O(n)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
public final class TemplateUtil {
    private TemplateUtil() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static String render(String template, Map<String, Object> values) {
        if (template == null) return null;
        if (values == null || values.isEmpty()) return template;
        
        String result = template;
        Pattern pattern = Pattern.compile("\\$\\{([^}]+)\\}");
        Matcher matcher = pattern.matcher(template);
        
        while (matcher.find()) {
            String placeholder = matcher.group(0);
            String varName = matcher.group(1);
            
            String[] parts = varName.split(":", 2);
            String name = parts[0].trim();
            String defaultValue = parts.length > 1 ? parts[1].trim() : "";
            
            Object value = values.get(name);
            String replacement = value != null ? value.toString() : defaultValue;
            result = result.replace(placeholder, replacement);
        }
        
        return result;
    }

    public static Set<String> extractVariables(String template) {
        if (template == null) return Set.of();
        
        Set<String> variables = new HashSet<>();
        Pattern pattern = Pattern.compile("\\$\\{([^}:]+)");
        Matcher matcher = pattern.matcher(template);
        
        while (matcher.find()) {
            variables.add(matcher.group(1).trim());
        }
        
        return variables;
    }
}
