package cloud.opencode.base.string.template;

import java.util.*;

/**
 * Template Context - Holds variables and state for template rendering.
 * 模板上下文 - 持有模板渲染的变量和状态。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Variable storage for template rendering - 模板渲染变量存储</li>
 *   <li>Built-in filters (upper, lower, truncate, default) - 内置过滤器</li>
 *   <li>Custom filter registration - 自定义过滤器注册</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * TemplateContext ctx = new TemplateContext(Map.of("name", "World"));
 * Object value = ctx.get("name"); // "World"
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (mutable state) - 线程安全: 否（可变状态）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
public final class TemplateContext {
    private final Map<String, Object> variables;
    private final Map<String, TemplateFilter> filters;

    public TemplateContext() {
        this.variables = new HashMap<>();
        this.filters = new HashMap<>();
        registerDefaultFilters();
    }

    public TemplateContext(Map<String, Object> variables) {
        this();
        this.variables.putAll(variables);
    }

    private void registerDefaultFilters() {
        filters.put("upper", (v, args) -> v != null ? v.toUpperCase() : "");
        filters.put("lower", (v, args) -> v != null ? v.toLowerCase() : "");
        filters.put("truncate", (v, args) -> {
            if (v == null) return "";
            int len = args.length > 0 ? Integer.parseInt(args[0]) : 50;
            return v.length() > len ? v.substring(0, len) + "..." : v;
        });
        filters.put("default", (v, args) -> 
            v != null && !v.isEmpty() ? v : (args.length > 0 ? args[0] : ""));
    }

    public Object get(String name) {
        return variables.get(name);
    }

    public void set(String name, Object value) {
        variables.put(name, value);
    }

    public TemplateFilter getFilter(String name) {
        return filters.get(name);
    }

    public void registerFilter(String name, TemplateFilter filter) {
        filters.put(name, filter);
    }

    public Map<String, Object> getVariables() {
        return Collections.unmodifiableMap(variables);
    }
}
