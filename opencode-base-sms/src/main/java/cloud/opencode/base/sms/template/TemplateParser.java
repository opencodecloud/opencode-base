package cloud.opencode.base.sms.template;

import cloud.opencode.base.sms.exception.SmsErrorCode;
import cloud.opencode.base.sms.exception.SmsTemplateException;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Template Parser
 * 模板解析器
 *
 * <p>Parses SMS templates with variable substitution.</p>
 * <p>解析带变量替换的短信模板。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Multiple variable patterns (${}, #{}, {{}}) - 多种变量模式</li>
 *   <li>Strict and lenient parsing modes - 严格和宽松解析模式</li>
 *   <li>Default value fallback - 默认值回退</li>
 *   <li>Variable extraction - 变量提取</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * TemplateParser parser = TemplateParser.create();
 * String result = parser.parse("Hello ${name}!", Map.of("name", "World"));
 * Set<String> vars = parser.extractVariables("${a} and ${b}");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) where n is template length - 时间复杂度: O(n)，n 为模板长度</li>
 *   <li>Space complexity: O(n) for result buffer - 空间复杂度: O(n) 结果缓冲区</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 */
public final class TemplateParser {

    /**
     * Default variable pattern: ${varName}
     * 默认变量模式: ${varName}
     */
    private static final Pattern DEFAULT_PATTERN = Pattern.compile("\\$\\{([^}]+)}");

    /**
     * Alternative pattern: #{varName}
     * 替代模式: #{varName}
     */
    private static final Pattern HASH_PATTERN = Pattern.compile("#\\{([^}]+)}");

    /**
     * Double brace pattern: {{varName}}
     * 双大括号模式: {{varName}}
     */
    private static final Pattern BRACE_PATTERN = Pattern.compile("\\{\\{([^}]+)}}");

    private final Pattern variablePattern;
    private final boolean strictMode;
    private final String defaultValue;

    private TemplateParser(Pattern pattern, boolean strictMode, String defaultValue) {
        this.variablePattern = pattern;
        this.strictMode = strictMode;
        this.defaultValue = defaultValue;
    }

    /**
     * Create default parser
     * 创建默认解析器
     *
     * @return the parser | 解析器
     */
    public static TemplateParser create() {
        return new TemplateParser(DEFAULT_PATTERN, true, null);
    }

    /**
     * Create parser with pattern
     * 使用模式创建解析器
     *
     * @param pattern the variable pattern | 变量模式
     * @return the parser | 解析器
     */
    public static TemplateParser withPattern(Pattern pattern) {
        return new TemplateParser(pattern, true, null);
    }

    /**
     * Create hash pattern parser (#{var})
     * 创建井号模式解析器
     *
     * @return the parser | 解析器
     */
    public static TemplateParser hashPattern() {
        return new TemplateParser(HASH_PATTERN, true, null);
    }

    /**
     * Create brace pattern parser ({{var}})
     * 创建双大括号模式解析器
     *
     * @return the parser | 解析器
     */
    public static TemplateParser bracePattern() {
        return new TemplateParser(BRACE_PATTERN, true, null);
    }

    /**
     * Create lenient parser
     * 创建宽松模式解析器
     *
     * @param defaultValue the default value for missing variables | 缺失变量的默认值
     * @return the parser | 解析器
     */
    public static TemplateParser lenient(String defaultValue) {
        return new TemplateParser(DEFAULT_PATTERN, false, defaultValue);
    }

    /**
     * Parse template and extract variable names
     * 解析模板并提取变量名
     *
     * @param template the template | 模板
     * @return the variable names | 变量名列表
     */
    public List<String> extractVariables(String template) {
        if (template == null) {
            return List.of();
        }
        Set<String> variables = new LinkedHashSet<>();
        Matcher matcher = variablePattern.matcher(template);
        while (matcher.find()) {
            variables.add(matcher.group(1).trim());
        }
        return new ArrayList<>(variables);
    }

    /**
     * Render template with variables
     * 使用变量渲染模板
     *
     * @param template the template | 模板
     * @param variables the variables | 变量
     * @return the rendered content | 渲染后的内容
     * @throws SmsTemplateException if strict mode and variable missing | 严格模式下变量缺失
     */
    public String render(String template, Map<String, String> variables) {
        if (template == null) {
            return null;
        }
        if (variables == null || variables.isEmpty()) {
            if (strictMode && !extractVariables(template).isEmpty()) {
                throw SmsTemplateException.variableMissing(
                    "template",
                    extractVariables(template).getFirst()
                );
            }
            return template;
        }

        StringBuffer result = new StringBuffer();
        Matcher matcher = variablePattern.matcher(template);

        while (matcher.find()) {
            String varName = matcher.group(1).trim();
            String value = variables.get(varName);

            if (value == null) {
                if (strictMode) {
                    throw SmsTemplateException.variableMissing("template", varName);
                }
                value = defaultValue != null ? defaultValue : matcher.group(0);
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Validate template has all required variables
     * 验证模板包含所有必需变量
     *
     * @param template the template | 模板
     * @param variables the variables | 变量
     * @return the validation result | 验证结果
     */
    public ValidationResult validate(String template, Map<String, String> variables) {
        List<String> required = extractVariables(template);
        List<String> missing = new ArrayList<>();
        List<String> extra = new ArrayList<>();

        for (String varName : required) {
            if (variables == null || !variables.containsKey(varName)) {
                missing.add(varName);
            }
        }

        if (variables != null) {
            for (String varName : variables.keySet()) {
                if (!required.contains(varName)) {
                    extra.add(varName);
                }
            }
        }

        return new ValidationResult(
            missing.isEmpty(),
            required,
            missing,
            extra
        );
    }

    /**
     * Count variables in template
     * 统计模板中的变量数
     *
     * @param template the template | 模板
     * @return the count | 数量
     */
    public int countVariables(String template) {
        return extractVariables(template).size();
    }

    /**
     * Check if template has variables
     * 检查模板是否包含变量
     *
     * @param template the template | 模板
     * @return true if has variables | 如果包含变量返回true
     */
    public boolean hasVariables(String template) {
        if (template == null) {
            return false;
        }
        return variablePattern.matcher(template).find();
    }

    /**
     * Escape special characters in value
     * 转义值中的特殊字符
     *
     * @param value the value | 值
     * @return the escaped value | 转义后的值
     */
    public static String escapeValue(String value) {
        if (value == null) {
            return null;
        }
        return value
            .replace("$", "\\$")
            .replace("{", "\\{")
            .replace("}", "\\}");
    }

    /**
     * Validation Result
     * 验证结果
     *
     * @param valid whether valid | 是否有效
     * @param requiredVariables required variable names | 必需的变量名
     * @param missingVariables missing variable names | 缺失的变量名
     * @param extraVariables extra variable names | 多余的变量名
     */
    public record ValidationResult(
        boolean valid,
        List<String> requiredVariables,
        List<String> missingVariables,
        List<String> extraVariables
    ) {
        public ValidationResult {
            requiredVariables = requiredVariables != null ? List.copyOf(requiredVariables) : List.of();
            missingVariables = missingVariables != null ? List.copyOf(missingVariables) : List.of();
            extraVariables = extraVariables != null ? List.copyOf(extraVariables) : List.of();
        }

        /**
         * Check if has missing variables
         * 检查是否有缺失变量
         *
         * @return true if has missing | 如果有缺失返回true
         */
        public boolean hasMissing() {
            return !missingVariables.isEmpty();
        }

        /**
         * Check if has extra variables
         * 检查是否有多余变量
         *
         * @return true if has extra | 如果有多余返回true
         */
        public boolean hasExtra() {
            return !extraVariables.isEmpty();
        }

        /**
         * Get error message
         * 获取错误消息
         *
         * @return the error message or null | 错误消息或null
         */
        public String getErrorMessage() {
            if (valid) {
                return null;
            }
            if (!missingVariables.isEmpty()) {
                return "Missing variables: " + String.join(", ", missingVariables);
            }
            return null;
        }
    }
}
