package cloud.opencode.base.string.template;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Template Engine - Simple and flexible template rendering engine
 * 模板引擎 - 简单灵活的模板渲染引擎
 *
 * <p>A lightweight template engine that supports variable substitution, custom functions,
 * and configurable delimiters. Provides a fluent API for template configuration and rendering.</p>
 * <p>轻量级模板引擎，支持变量替换、自定义函数和可配置分隔符。提供流畅的API用于模板配置和渲染。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Variable substitution - 变量替换</li>
 *   <li>Custom function registration - 自定义函数注册</li>
 *   <li>Configurable variable delimiters - 可配置变量分隔符</li>
 *   <li>Fluent builder API - 流畅构建器API</li>
 *   <li>Multiple context types support - 支持多种上下文类型</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create engine with default settings
 * TemplateEngine engine = TemplateEngine.create();
 *
 * // Render with Map context
 * Map<String, Object> context = Map.of("name", "World");
 * String result = engine.render("Hello ${name}!", context);
 * // Output: "Hello World!"
 *
 * // Custom delimiters
 * engine = TemplateEngine.create()
 *     .variablePrefix("{{")
 *     .variableSuffix("}}");
 * result = engine.render("Hello {{name}}!", context);
 *
 * // Register custom function
 * engine.registerFunction("upper", args -> args[0].toString().toUpperCase());
 * result = engine.render("${upper(name)}", context);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) for rendering - 渲染时间复杂度: O(n)</li>
 *   <li>Space complexity: O(m) for context - 上下文空间复杂度: O(m)</li>
 *   <li>Function lookup: O(1) using HashMap - 函数查找: O(1) 使用HashMap</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 线程安全: 否</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 *   <li>Immutable: No (builder pattern) - 不可变: 否（构建器模式）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
public final class TemplateEngine {

    /** Default variable prefix | 默认变量前缀 */
    private static final String DEFAULT_PREFIX = "${";

    /** Default variable suffix | 默认变量后缀 */
    private static final String DEFAULT_SUFFIX = "}";

    /** Variable prefix | 变量前缀 */
    private String variablePrefix;

    /** Variable suffix | 变量后缀 */
    private String variableSuffix;

    /** Registered functions | 注册的函数 */
    private final Map<String, Function<Object[], Object>> functions;

    /**
     * Private constructor for builder pattern
     * 构建器模式的私有构造函数
     */
    private TemplateEngine() {
        this.variablePrefix = DEFAULT_PREFIX;
        this.variableSuffix = DEFAULT_SUFFIX;
        this.functions = new HashMap<>();
    }

    /**
     * Create a new template engine instance
     * 创建新的模板引擎实例
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * TemplateEngine engine = TemplateEngine.create();
     * </pre>
     *
     * @return new engine instance | 新的引擎实例
     */
    public static TemplateEngine create() {
        return new TemplateEngine();
    }

    /**
     * Render template with Map context
     * 使用Map上下文渲染模板
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * engine.render("Hello ${name}!", Map.of("name", "World"))  = "Hello World!"
     * engine.render("${x} + ${y}", Map.of("x", 1, "y", 2))      = "1 + 2"
     * </pre>
     *
     * @param template template string | 模板字符串
     * @param context  variable context | 变量上下文
     * @return rendered result | 渲染结果
     */
    public String render(String template, Map<String, Object> context) {
        if (template == null || template.isEmpty()) {
            return template;
        }
        if (context == null || context.isEmpty()) {
            return template;
        }

        // append(template, start, end) avoids intermediate String allocation from substring().
        // append(template, start, end) 避免 substring() 的中间 String 分配。
        StringBuilder result = new StringBuilder(template.length());
        int pos = 0;

        while (pos < template.length()) {
            int start = template.indexOf(variablePrefix, pos);
            if (start == -1) {
                result.append(template, pos, template.length());
                break;
            }

            // Append text before variable — no substring allocation
            result.append(template, pos, start);

            int end = template.indexOf(variableSuffix, start + variablePrefix.length());
            if (end == -1) {
                result.append(template, start, template.length());
                break;
            }

            // Variable name still needs substring (short-lived, needed for map lookup)
            String varExpr = template.substring(start + variablePrefix.length(), end);
            Object value = resolveVariable(varExpr, context);
            result.append(value != null ? value.toString() : "");

            pos = end + variableSuffix.length();
        }

        return result.toString();
    }

    /**
     * Render template with TemplateContext
     * 使用TemplateContext渲染模板
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * TemplateContext ctx = TemplateContext.create().put("name", "World");
     * engine.render("Hello ${name}!", ctx) = "Hello World!"
     * </pre>
     *
     * @param template template string | 模板字符串
     * @param context  template context | 模板上下文
     * @return rendered result | 渲染结果
     */
    public String render(String template, TemplateContext context) {
        return render(template, context != null ? context.getVariables() : Map.of());
    }

    /**
     * Register a custom function
     * 注册自定义函数
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * engine.registerFunction("upper", args -> args[0].toString().toUpperCase());
     * engine.render("${upper(name)}", Map.of("name", "hello")) = "HELLO"
     * </pre>
     *
     * @param name     function name | 函数名
     * @param function function implementation | 函数实现
     * @return this engine instance for chaining | 用于链式调用的引擎实例
     */
    public TemplateEngine registerFunction(String name, Function<Object[], Object> function) {
        if (name != null && function != null) {
            this.functions.put(name, function);
        }
        return this;
    }

    /**
     * Set variable prefix
     * 设置变量前缀
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * engine.variablePrefix("{{");
     * engine.render("{{name}}", context) // Uses {{ instead of ${
     * </pre>
     *
     * @param prefix variable prefix | 变量前缀
     * @return this engine instance for chaining | 用于链式调用的引擎实例
     */
    public TemplateEngine variablePrefix(String prefix) {
        if (prefix != null && !prefix.isEmpty()) {
            this.variablePrefix = prefix;
        }
        return this;
    }

    /**
     * Set variable suffix
     * 设置变量后缀
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * engine.variableSuffix("}}");
     * engine.render("{{name}}", context) // Uses }} instead of }
     * </pre>
     *
     * @param suffix variable suffix | 变量后缀
     * @return this engine instance for chaining | 用于链式调用的引擎实例
     */
    public TemplateEngine variableSuffix(String suffix) {
        if (suffix != null && !suffix.isEmpty()) {
            this.variableSuffix = suffix;
        }
        return this;
    }

    /**
     * Resolve variable expression
     * 解析变量表达式
     *
     * @param expr    variable expression | 变量表达式
     * @param context variable context | 变量上下文
     * @return resolved value | 解析的值
     */
    private Object resolveVariable(String expr, Map<String, Object> context) {
        expr = expr.trim();

        // Check if it's a function call (e.g., "upper(name)")
        int parenIndex = expr.indexOf('(');
        if (parenIndex > 0 && expr.endsWith(")")) {
            String funcName = expr.substring(0, parenIndex).trim();
            String argsStr = expr.substring(parenIndex + 1, expr.length() - 1).trim();

            Function<Object[], Object> func = functions.get(funcName);
            if (func != null) {
                // Simple argument parsing (supports single argument for now)
                Object argValue = context.get(argsStr);
                return func.apply(new Object[]{argValue});
            }
        }

        // Simple variable lookup
        return context.get(expr);
    }
}
