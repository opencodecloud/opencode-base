package cloud.opencode.base.i18n.formatter;

import cloud.opencode.base.i18n.spi.MessageFormatter;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Named parameter message formatter
 * 命名参数消息格式化器
 *
 * <p>Supports named parameters in the format ${name} for more readable templates.</p>
 * <p>支持${name}格式的命名参数，使模板更具可读性。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Named parameter syntax ${name} - 命名参数语法${name}</li>
 *   <li>Fallback for missing values - 缺失值回退</li>
 *   <li>Pattern caching - 模式缓存</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * NamedParameterFormatter formatter = new NamedParameterFormatter();
 * Map<String, Object> params = Map.of("name", "Alice", "count", 5);
 * String result = formatter.format("Hello, ${name}! You have ${count} messages.", Locale.ENGLISH, params);
 * // result: "Hello, Alice! You have 5 messages."
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n×p) where n is template length and p is number of parameters - 时间复杂度: O(n×p)，n 为模板长度，p 为参数数量</li>
 *   <li>Space complexity: O(n) for result buffer - 空间复杂度: O(n) 结果缓冲区</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-i18n V1.0.0
 */
public class NamedParameterFormatter implements MessageFormatter {

    private static final Pattern PARAM_PATTERN = Pattern.compile("\\$\\{([^}]+)}");
    private static final Pattern ESCAPED_PATTERN = Pattern.compile("\\\\\\$\\{");

    private final Map<String, Pattern> patternCache = new ConcurrentHashMap<>();
    private final String missingValuePrefix;
    private final String missingValueSuffix;
    private final boolean keepMissingParams;

    /**
     * Creates a formatter with default settings
     * 使用默认设置创建格式化器
     */
    public NamedParameterFormatter() {
        this(false, "${", "}");
    }

    /**
     * Creates a formatter with missing value handling
     * 使用缺失值处理创建格式化器
     *
     * @param keepMissingParams whether to keep missing params in output | 是否在输出中保留缺失参数
     */
    public NamedParameterFormatter(boolean keepMissingParams) {
        this(keepMissingParams, "${", "}");
    }

    /**
     * Creates a formatter with custom missing value format
     * 使用自定义缺失值格式创建格式化器
     *
     * @param keepMissingParams    whether to keep missing params | 是否保留缺失参数
     * @param missingValuePrefix   prefix for missing values | 缺失值前缀
     * @param missingValueSuffix   suffix for missing values | 缺失值后缀
     */
    public NamedParameterFormatter(boolean keepMissingParams, String missingValuePrefix, String missingValueSuffix) {
        this.keepMissingParams = keepMissingParams;
        this.missingValuePrefix = missingValuePrefix;
        this.missingValueSuffix = missingValueSuffix;
    }

    @Override
    public String format(String template, Locale locale, Object... args) {
        if (template == null) {
            return null;
        }
        if (args == null || args.length == 0) {
            return template;
        }

        // Simple replacement: replace ${0}, ${1}, etc.
        String result = template;
        for (int i = 0; i < args.length; i++) {
            String placeholder = "${" + i + "}";
            String value = args[i] != null ? args[i].toString() : "";
            result = result.replace(placeholder, value);
        }
        return result;
    }

    @Override
    public String format(String template, Locale locale, Map<String, Object> params) {
        if (template == null) {
            return null;
        }
        if (params == null || params.isEmpty()) {
            return processEscapes(template);
        }

        StringBuffer result = new StringBuffer();
        Matcher matcher = PARAM_PATTERN.matcher(template);

        while (matcher.find()) {
            String paramName = matcher.group(1);
            Object value = params.get(paramName);

            String replacement;
            if (value != null) {
                replacement = Matcher.quoteReplacement(value.toString());
            } else if (keepMissingParams) {
                replacement = Matcher.quoteReplacement(missingValuePrefix + paramName + missingValueSuffix);
            } else {
                replacement = "";
            }
            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);

        return processEscapes(result.toString());
    }

    @Override
    public void clearCache() {
        patternCache.clear();
    }

    /**
     * Formats with support for nested parameters
     * 格式化并支持嵌套参数
     *
     * @param template the template | 模板
     * @param params   the parameters | 参数
     * @param maxDepth maximum nesting depth | 最大嵌套深度
     * @return formatted string | 格式化后的字符串
     */
    public String formatNested(String template, Map<String, Object> params, int maxDepth) {
        if (template == null || params == null || maxDepth <= 0) {
            return template;
        }

        String result = template;
        for (int i = 0; i < maxDepth; i++) {
            String previous = result;
            result = format(result, Locale.getDefault(), params);
            if (result.equals(previous)) {
                break;
            }
        }
        return result;
    }

    /**
     * Checks if the template contains parameters
     * 检查模板是否包含参数
     *
     * @param template the template | 模板
     * @return true if contains parameters | 如果包含参数返回true
     */
    public boolean containsParameters(String template) {
        if (template == null) {
            return false;
        }
        return PARAM_PATTERN.matcher(template).find();
    }

    /**
     * Extracts parameter names from template
     * 从模板中提取参数名
     *
     * @param template the template | 模板
     * @return set of parameter names | 参数名集合
     */
    public java.util.Set<String> extractParameterNames(String template) {
        java.util.Set<String> names = new java.util.LinkedHashSet<>();
        if (template == null) {
            return names;
        }

        Matcher matcher = PARAM_PATTERN.matcher(template);
        while (matcher.find()) {
            names.add(matcher.group(1));
        }
        return names;
    }

    private String processEscapes(String text) {
        return ESCAPED_PATTERN.matcher(text).replaceAll("\\${");
    }
}
