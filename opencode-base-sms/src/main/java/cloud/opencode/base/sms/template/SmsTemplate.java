package cloud.opencode.base.sms.template;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SMS Template
 * 短信模板
 *
 * <p>Template for SMS messages with variable substitution.</p>
 * <p>支持变量替换的短信消息模板。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Automatic variable name extraction from content - 从内容自动提取变量名</li>
 *   <li>Variable substitution rendering - 变量替换渲染</li>
 *   <li>Immutable record design - 不可变记录设计</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SmsTemplate tpl = SmsTemplate.of("verify", "Your code is ${code}");
 * List<String> vars = tpl.variableNames(); // ["code"]
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 * </ul>
 *
 * @param id the template ID | 模板ID
 * @param name the template name | 模板名称
 * @param content the template content | 模板内容
 * @param variableNames the variable names | 变量名称列表
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 */
public record SmsTemplate(
    String id,
    String name,
    String content,
    List<String> variableNames
) {
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([^}]+)}");

    public SmsTemplate {
        variableNames = variableNames != null ? List.copyOf(variableNames) : extractVariables(content);
    }

    /**
     * Create template from content
     * 从内容创建模板
     *
     * @param id the template ID | 模板ID
     * @param content the content | 内容
     * @return the template | 模板
     */
    public static SmsTemplate of(String id, String content) {
        return new SmsTemplate(id, id, content, null);
    }

    /**
     * Create template with name
     * 使用名称创建模板
     *
     * @param id the template ID | 模板ID
     * @param name the name | 名称
     * @param content the content | 内容
     * @return the template | 模板
     */
    public static SmsTemplate of(String id, String name, String content) {
        return new SmsTemplate(id, name, content, null);
    }

    /**
     * Render template with variables
     * 使用变量渲染模板
     *
     * @param variables the variables | 变量
     * @return the rendered content | 渲染后的内容
     */
    public String render(Map<String, String> variables) {
        String result = content;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("${" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }

    /**
     * Check if all variables are provided
     * 检查是否提供了所有变量
     *
     * @param variables the variables | 变量
     * @return true if all provided | 如果全部提供返回true
     */
    public boolean hasAllVariables(Map<String, String> variables) {
        if (variables == null) {
            return variableNames.isEmpty();
        }
        return variables.keySet().containsAll(variableNames);
    }

    /**
     * Get missing variables
     * 获取缺失的变量
     *
     * @param variables the variables | 变量
     * @return the missing variable names | 缺失的变量名称
     */
    public List<String> getMissingVariables(Map<String, String> variables) {
        if (variables == null) {
            return variableNames;
        }
        return variableNames.stream()
            .filter(v -> !variables.containsKey(v))
            .toList();
    }

    private static List<String> extractVariables(String content) {
        if (content == null) {
            return List.of();
        }
        java.util.List<String> vars = new java.util.ArrayList<>();
        Matcher matcher = VARIABLE_PATTERN.matcher(content);
        while (matcher.find()) {
            String varName = matcher.group(1);
            if (!vars.contains(varName)) {
                vars.add(varName);
            }
        }
        return vars;
    }
}
