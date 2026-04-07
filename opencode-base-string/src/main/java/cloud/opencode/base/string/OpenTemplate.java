package cloud.opencode.base.string;

import cloud.opencode.base.string.template.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Template Utility - Provides simple string template rendering.
 * 模板工具 - 提供简单的字符串模板渲染。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Variable placeholder rendering - 变量占位符渲染</li>
 *   <li>Named and indexed parameter support - 命名和索引参数支持</li>
 *   <li>Template compilation and caching - 模板编译和缓存</li>
 *   <li>Built-in filters (upper, lower, truncate, etc.) - 内置过滤器</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Simple placeholder rendering
 * String result = OpenTemplate.render("Hello ${name}!", Map.of("name", "World"));
 * // "Hello World!"
 *
 * // Indexed format
 * String formatted = OpenTemplate.format("Hello {0}, welcome to {1}!", "Alice", "Java");
 *
 * // Compiled template
 * Template tpl = OpenTemplate.compile("${greeting} ${name}");
 * String output = tpl.render(Map.of("greeting", "Hi", "name", "Bob"));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (ConcurrentHashMap for filters and cache) - 线程安全: 是（过滤器和缓存使用 ConcurrentHashMap）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 *   <li>Cache bounded: Yes (max 1024 templates) - 缓存有界: 是（最多1024个模板）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
public final class OpenTemplate {
    private OpenTemplate() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    private static final Pattern INDEXED_PLACEHOLDER_PATTERN = Pattern.compile("\\{(\\d+)\\}");

    /**
     * Thread-safe filter registry. Registration typically happens at startup,
     * but ConcurrentHashMap prevents data races if filters are added at runtime.
     * 线程安全的过滤器注册表。注册通常在启动时进行，
     * 但 ConcurrentHashMap 可防止运行时添加过滤器时的数据竞争。
     */
    private static final Map<String, TemplateFilter> GLOBAL_FILTERS = new ConcurrentHashMap<>();

    /**
     * Maximum number of cached templates to prevent memory exhaustion (DoS).
     * 缓存模板最大数量，防止内存耗尽（DoS）。
     */
    private static final int MAX_CACHE_SIZE = 1024;

    /**
     * Thread-safe, bounded template cache. Rejects new entries beyond MAX_CACHE_SIZE
     * to prevent unbounded memory growth from attacker-controlled template registration.
     * 线程安全且有界的模板缓存。超过 MAX_CACHE_SIZE 时拒绝新条目，
     * 防止攻击者通过注册模板导致无限内存增长。
     */
    private static final Map<String, String> TEMPLATE_CACHE = new ConcurrentHashMap<>();

    static {
        registerDefaultFilters();
    }

    private static void registerDefaultFilters() {
        GLOBAL_FILTERS.put("upper", (v, args) -> v != null ? v.toUpperCase() : "");
        GLOBAL_FILTERS.put("lower", (v, args) -> v != null ? v.toLowerCase() : "");
        GLOBAL_FILTERS.put("capitalize", (v, args) -> v != null && !v.isEmpty() ? 
            Character.toUpperCase(v.charAt(0)) + v.substring(1) : v);
        GLOBAL_FILTERS.put("truncate", (v, args) -> {
            if (v == null) return "";
            int len = args.length > 0 ? Integer.parseInt(args[0]) : 50;
            return v.length() > len ? v.substring(0, len) + "..." : v;
        });
        GLOBAL_FILTERS.put("default", (v, args) -> 
            v != null && !v.isEmpty() ? v : (args.length > 0 ? args[0] : ""));
        GLOBAL_FILTERS.put("escape", (v, args) -> {
            if (v == null) return "";
            // Delegate to HtmlUtil.escape() which covers all 5 entities: & < > " '
            // 委托给 HtmlUtil.escape()，覆盖全部 5 个实体：& < > " '
            return cloud.opencode.base.string.escape.HtmlUtil.escape(v);
        });
    }

    // Placeholder replacement
    public static String render(String template, Map<String, Object> values) {
        return TemplateUtil.render(template, values);
    }

    public static String render(String template, Map<String, Object> values, String defaultValue) {
        Map<String, Object> allValues = new HashMap<>(values);
        String result = render(template, allValues);
        return result != null ? result : defaultValue;
    }

    public static String format(String template, Map<String, Object> values) {
        return render(template, values);
    }

    public static String format(String template, Object... args) {
        Map<String, Object> values = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            values.put(String.valueOf(i), args[i]);
        }
        return render(INDEXED_PLACEHOLDER_PATTERN.matcher(template).replaceAll("\\${$1}"), values);
    }

    // Template objects
    public static StringTemplate of(String template) {
        return StringTemplate.of(template);
    }

    public static PlaceholderTemplate placeholder(String template, String prefix, String suffix) {
        return PlaceholderTemplate.of(template, prefix, suffix);
    }

    // Compiled templates
    public static Template compile(String template) {
        return Template.compile(template);
    }

    public static String renderInline(String template, Map<String, Object> context) {
        return Template.compile(template).render(context);
    }

    // Filter management
    public static void registerFilter(String name, TemplateFilter filter) {
        GLOBAL_FILTERS.put(name, filter);
    }

    public static TemplateFilter getFilter(String name) {
        return GLOBAL_FILTERS.get(name);
    }

    // Template cache
    /**
     * Register a named template. Cache is bounded to {@value #MAX_CACHE_SIZE} entries
     * to prevent memory exhaustion from unbounded registration.
     * 注册命名模板。缓存限制为 {@value #MAX_CACHE_SIZE} 条，防止无限注册导致内存耗尽。
     *
     * @param name     the template name | 模板名
     * @param template the template string | 模板字符串
     * @throws IllegalStateException if the cache is full and the name is new |
     *                               如果缓存已满且名称是新的
     */
    public static void register(String name, String template) {
        if (!TEMPLATE_CACHE.containsKey(name) && TEMPLATE_CACHE.size() >= MAX_CACHE_SIZE) {
            throw new IllegalStateException(
                    "Template cache is full (max " + MAX_CACHE_SIZE + "). "
                            + "Call clearCache() to remove old templates.");
        }
        TEMPLATE_CACHE.put(name, template);
    }

    public static String renderNamed(String templateName, Map<String, Object> context) {
        String template = TEMPLATE_CACHE.get(templateName);
        if (template == null) {
            throw new IllegalArgumentException("Template not found: " + templateName);
        }
        return render(template, context);
    }

    public static void clearCache() {
        TEMPLATE_CACHE.clear();
    }
}
