package cloud.opencode.base.email.template;

import cloud.opencode.base.email.exception.EmailTemplateException;
import cloud.opencode.base.email.internal.EmailTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple Email Template Engine
 * 简单邮件模板引擎
 *
 * <p>Simple template engine with variable substitution.</p>
 * <p>支持变量替换的简单模板引擎。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Variable substitution: ${name} or {{name}} - 变量替换</li>
 *   <li>Default values: ${name:default} - 默认值</li>
 *   <li>HTML escaping support - HTML转义支持</li>
 *   <li>Template caching - 模板缓存</li>
 *   <li>File and classpath loading - 文件和类路径加载</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Direct template rendering
 * String result = SimpleEmailTemplate.render(
 *     "Hello ${name}, your order #${orderId} is confirmed.",
 *     Map.of("name", "John", "orderId", "12345")
 * );
 *
 * // Load from file
 * String template = SimpleEmailTemplate.loadTemplate(Path.of("templates/welcome.html"));
 * String result = SimpleEmailTemplate.render(template, variables);
 *
 * // Load from classpath
 * String template = SimpleEmailTemplate.loadTemplateFromClasspath("templates/welcome.html");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
public final class SimpleEmailTemplate implements EmailTemplate {

    /**
     * Variable pattern: ${name} or ${name:defaultValue}
     */
    private static final Pattern VAR_PATTERN_DOLLAR = Pattern.compile("\\$\\{([^}]+)}");

    /**
     * Variable pattern: {{name}} or {{name:defaultValue}}
     */
    private static final Pattern VAR_PATTERN_MUSTACHE = Pattern.compile("\\{\\{([^}]+)}}");

    /**
     * Template cache
     */
    private static final ConcurrentMap<String, String> templateCache = new ConcurrentHashMap<>();

    /**
     * Singleton instance
     */
    private static final SimpleEmailTemplate INSTANCE = new SimpleEmailTemplate();

    private SimpleEmailTemplate() {
        // Private constructor
    }

    /**
     * Get singleton instance
     * 获取单例实例
     *
     * @return the instance | 实例
     */
    public static SimpleEmailTemplate getInstance() {
        return INSTANCE;
    }

    @Override
    public String render(String template, Map<String, Object> variables) {
        return renderTemplate(template, variables, false);
    }

    /**
     * Render template with variable substitution (static method)
     * 使用变量替换渲染模板（静态方法）
     *
     * @param template  the template content | 模板内容
     * @param variables the variables to substitute | 要替换的变量
     * @return the rendered content | 渲染后的内容
     */
    public static String renderTemplate(String template, Map<String, Object> variables) {
        return renderTemplate(template, variables, false);
    }

    /**
     * Render template with optional HTML escaping
     * 使用可选HTML转义渲染模板
     *
     * @param template   the template content | 模板内容
     * @param variables  the variables to substitute | 要替换的变量
     * @param escapeHtml whether to HTML-escape values | 是否HTML转义值
     * @return the rendered content | 渲染后的内容
     */
    public static String renderTemplate(String template, Map<String, Object> variables, boolean escapeHtml) {
        if (template == null) {
            return null;
        }
        if (variables == null || variables.isEmpty()) {
            return template;
        }

        String result = template;

        // Process ${...} pattern
        result = processPattern(result, VAR_PATTERN_DOLLAR, variables, escapeHtml);

        // Process {{...}} pattern
        result = processPattern(result, VAR_PATTERN_MUSTACHE, variables, escapeHtml);

        return result;
    }

    /**
     * Process pattern and replace variables
     */
    private static String processPattern(String template, Pattern pattern,
                                         Map<String, Object> variables, boolean escapeHtml) {
        Matcher matcher = pattern.matcher(template);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String expression = matcher.group(1);
            String varName;
            String defaultValue = "";

            // Check for default value
            int colonIndex = expression.indexOf(':');
            if (colonIndex > 0) {
                varName = expression.substring(0, colonIndex).trim();
                defaultValue = expression.substring(colonIndex + 1).trim();
            } else {
                varName = expression.trim();
            }

            // Get value
            Object value = variables.get(varName);
            String replacement;

            if (value != null) {
                replacement = String.valueOf(value);
            } else if (!defaultValue.isEmpty()) {
                replacement = defaultValue;
            } else {
                // Keep original if variable not found and no default
                replacement = matcher.group(0);
            }

            // Escape HTML if needed
            if (escapeHtml && value != null) {
                replacement = escapeHtml(replacement);
            }

            // Escape replacement string for regex
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Load template from file
     * 从文件加载模板
     *
     * @param path the file path | 文件路径
     * @return the template content | 模板内容
     */
    public static String loadTemplate(Path path) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new EmailTemplateException("Failed to load template: " + path, e);
        }
    }

    /**
     * Load template from classpath
     * 从类路径加载模板
     *
     * @param resourcePath the classpath resource path | 类路径资源路径
     * @return the template content | 模板内容
     */
    public static String loadTemplateFromClasspath(String resourcePath) {
        return loadTemplateFromClasspath(resourcePath, SimpleEmailTemplate.class.getClassLoader());
    }

    /**
     * Load template from classpath with custom classloader
     * 使用自定义类加载器从类路径加载模板
     *
     * @param resourcePath the classpath resource path | 类路径资源路径
     * @param classLoader  the class loader | 类加载器
     * @return the template content | 模板内容
     */
    public static String loadTemplateFromClasspath(String resourcePath, ClassLoader classLoader) {
        try (InputStream is = classLoader.getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new EmailTemplateException("Template not found: " + resourcePath);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new EmailTemplateException("Failed to load template: " + resourcePath, e);
        }
    }

    /**
     * Load template with caching
     * 使用缓存加载模板
     *
     * @param path the file path | 文件路径
     * @return the template content | 模板内容
     */
    public static String loadTemplateCached(Path path) {
        String key = path.toAbsolutePath().toString();
        return templateCache.computeIfAbsent(key, k -> loadTemplate(path));
    }

    /**
     * Load template from classpath with caching
     * 使用缓存从类路径加载模板
     *
     * @param resourcePath the classpath resource path | 类路径资源路径
     * @return the template content | 模板内容
     */
    public static String loadTemplateFromClasspathCached(String resourcePath) {
        return templateCache.computeIfAbsent(resourcePath,
                k -> loadTemplateFromClasspath(resourcePath));
    }

    /**
     * Clear template cache
     * 清除模板缓存
     */
    public static void clearCache() {
        templateCache.clear();
    }

    /**
     * Remove specific template from cache
     * 从缓存中移除特定模板
     *
     * @param key the cache key | 缓存键
     */
    public static void removeFromCache(String key) {
        templateCache.remove(key);
    }

    /**
     * Get cache size
     * 获取缓存大小
     *
     * @return the cache size | 缓存大小
     */
    public static int getCacheSize() {
        return templateCache.size();
    }

    /**
     * Escape HTML special characters
     * 转义HTML特殊字符
     *
     * @param text the text to escape | 要转义的文本
     * @return the escaped text | 转义后的文本
     */
    public static String escapeHtml(String text) {
        if (text == null) {
            return null;
        }
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    /**
     * Create a simple HTML email template
     * 创建简单的HTML邮件模板
     *
     * @param title the email title | 邮件标题
     * @param body  the body content | 正文内容
     * @return the HTML template | HTML模板
     */
    public static String createHtmlTemplate(String title, String body) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>%s</title>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        %s
                    </div>
                </body>
                </html>
                """.formatted(escapeHtml(title), body);
    }
}
