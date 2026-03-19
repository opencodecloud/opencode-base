package cloud.opencode.base.string.template;

import java.util.*;

/**
 * String Template - Simple string template implementation.
 * 字符串模板 - 简单的字符串模板实现。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fluent variable setting API - 流式变量设置API</li>
 *   <li>Strict and lenient modes - 严格和宽松模式</li>
 *   <li>Default value support - 默认值支持</li>
 *   <li>Variable extraction - 变量提取</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * String result = StringTemplate.of("Hello ${name}!")
 *     .set("name", "World")
 *     .render(); // "Hello World!"
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
public final class StringTemplate {
    private final String template;
    private final Map<String, Object> values;
    private String defaultValue;
    private boolean strict;
    private volatile Set<String> cachedVariables;

    private StringTemplate(String template) {
        this.template = template;
        this.values = new HashMap<>();
        this.strict = false;
    }

    public static StringTemplate of(String template) {
        return new StringTemplate(template);
    }

    public StringTemplate set(String name, Object value) {
        values.put(name, value);
        return this;
    }

    public StringTemplate setAll(Map<String, Object> values) {
        this.values.putAll(values);
        return this;
    }

    public StringTemplate defaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public StringTemplate strict(boolean strict) {
        this.strict = strict;
        return this;
    }

    public String render() {
        return render(Map.of());
    }

    public String render(Map<String, Object> additionalValues) {
        Map<String, Object> allValues = new HashMap<>(values);
        allValues.putAll(additionalValues);
        return TemplateUtil.render(template, allValues);
    }

    public Set<String> getVariables() {
        Set<String> result = cachedVariables;
        if (result == null) {
            result = Collections.unmodifiableSet(TemplateUtil.extractVariables(template));
            cachedVariables = result;
        }
        return result;
    }

    public boolean hasVariable(String name) {
        return getVariables().contains(name);
    }
}
