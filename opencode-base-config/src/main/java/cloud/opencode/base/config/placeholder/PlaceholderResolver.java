package cloud.opencode.base.config.placeholder;

import cloud.opencode.base.config.OpenConfigException;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Placeholder Resolver
 * 占位符解析器
 *
 * <p>Resolves placeholders in configuration values with support for nested placeholders
 * and default values.</p>
 * <p>解析配置值中的占位符,支持嵌套占位符和默认值。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Placeholder syntax: ${key} - 占位符语法: ${key}</li>
 *   <li>Default values: ${key:default} - 默认值: ${key:default}</li>
 *   <li>Nested placeholders - 嵌套占位符</li>
 *   <li>Recursion depth limit - 递归深度限制</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Map<String, String> props = Map.of(
 *     "base.url", "http://localhost",
 *     "api.url", "${base.url}/api",
 *     "db.host", "${DB_HOST:localhost}"
 * );
 *
 * PlaceholderResolver resolver = new PlaceholderResolver(props::get);
 *
 * resolver.resolve("${api.url}");           // -> "http://localhost/api"
 * resolver.resolve("${db.host}");           // -> "localhost" (using default)
 * resolver.resolve("${base.url}/v1");       // -> "http://localhost/v1"
 * resolver.resolve("${${env}.database}");   // -> nested resolution
 * }</pre>
 *
 * <p><strong>Supported Syntax | 支持的语法:</strong></p>
 * <pre>
 * ${key}              - Simple placeholder
 * ${key:default}      - With default value
 * ${${nested}}        - Nested placeholder
 * prefix${key}suffix  - Embedded placeholder
 * </pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n*d) where d is recursion depth - 时间复杂度: O(n*d)</li>
 *   <li>Max recursion depth: 10 (configurable) - 最大递归深度: 10</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Recursion protection - 递归保护</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
public class PlaceholderResolver {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^}]+)}");

    private final Function<String, String> propertyResolver;
    private final int maxRecursionDepth;

    /**
     * Create placeholder resolver with default depth limit
     * 创建默认深度限制的占位符解析器
     *
     * @param propertyResolver property lookup function | 属性查找函数
     */
    public PlaceholderResolver(Function<String, String> propertyResolver) {
        this(propertyResolver, 10);
    }

    /**
     * Create placeholder resolver with custom depth limit
     * 创建自定义深度限制的占位符解析器
     *
     * @param propertyResolver property lookup function | 属性查找函数
     * @param maxRecursionDepth maximum recursion depth | 最大递归深度
     */
    public PlaceholderResolver(Function<String, String> propertyResolver,
                               int maxRecursionDepth) {
        if (maxRecursionDepth < 1 || maxRecursionDepth > 20) {
            throw new IllegalArgumentException(
                    "maxRecursionDepth must be between 1 and 20, got: " + maxRecursionDepth);
        }
        this.propertyResolver = propertyResolver;
        this.maxRecursionDepth = maxRecursionDepth;
    }

    /**
     * Resolve placeholders in value
     * 解析值中的占位符
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * resolve("${key}")              -> value of "key"
     * resolve("${key:default}")      -> value of "key" or "default"
     * resolve("prefix${key}suffix")  -> "prefix" + value + "suffix"
     * resolve("no placeholders")     -> "no placeholders"
     * </pre>
     *
     * @param value value to resolve | 要解析的值
     * @return resolved value | 解析后的值
     * @throws OpenConfigException if placeholder cannot be resolved | 如果占位符无法解析
     */
    public String resolve(String value) {
        if (value == null || !value.contains("${")) {
            return value;
        }
        return resolve(value, 0);
    }

    /**
     * Resolve placeholders recursively
     * 递归解析占位符
     *
     * @param value value to resolve | 要解析的值
     * @param depth current recursion depth | 当前递归深度
     * @return resolved value | 解析后的值
     */
    private String resolve(String value, int depth) {
        if (depth > maxRecursionDepth) {
            throw OpenConfigException.placeholderRecursionTooDeep(value);
        }

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(value);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String placeholder = matcher.group(1);
            String replacement = resolvePlaceholder(placeholder, depth);
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        String resolved = result.toString();

        // Recursively resolve nested placeholders
        if (resolved.contains("${") && !resolved.equals(value)) {
            return resolve(resolved, depth + 1);
        }

        return resolved;
    }

    /**
     * Resolve single placeholder
     * 解析单个占位符
     *
     * @param placeholder placeholder expression (without ${}) | 占位符表达式(不含${})
     * @param depth current recursion depth | 当前递归深度
     * @return resolved value | 解析后的值
     */
    private String resolvePlaceholder(String placeholder, int depth) {
        String key;
        String defaultValue = null;

        // Parse key:default syntax
        int colonIdx = placeholder.indexOf(':');
        if (colonIdx > 0) {
            key = placeholder.substring(0, colonIdx);
            defaultValue = placeholder.substring(colonIdx + 1);
        } else {
            key = placeholder;
        }

        // Lookup value
        String value = propertyResolver.apply(key);

        if (value != null) {
            // Recursively resolve the value
            return resolve(value, depth + 1);
        }

        if (defaultValue != null) {
            // Recursively resolve the default value
            return resolve(defaultValue, depth + 1);
        }

        throw OpenConfigException.placeholderResolveFailed(key);
    }
}
