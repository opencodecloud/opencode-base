package cloud.opencode.base.string.template;

import java.util.*;

/**
 * Placeholder Template - Template engine with placeholder-based substitution.
 * 占位符模板 - 基于占位符替换的模板引擎。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Configurable prefix and suffix delimiters - 可配置前缀和后缀分隔符</li>
 *   <li>Simple string replacement rendering - 简单字符串替换渲染</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * PlaceholderTemplate tpl = PlaceholderTemplate.of("Hello ${name}!", "${", "}");
 * String result = tpl.render(Map.of("name", "World")); // "Hello World!"
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 *   <li>Null-safe: Yes (returns null for null template) - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
public final class PlaceholderTemplate {
    private final String template;
    private final String prefix;
    private final String suffix;

    public PlaceholderTemplate(String template, String prefix, String suffix) {
        this.template = template;
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public static PlaceholderTemplate of(String template, String prefix, String suffix) {
        return new PlaceholderTemplate(template, prefix, suffix);
    }

    public String render(Map<String, Object> values) {
        if (template == null) return null;
        if (values == null || values.isEmpty()) return template;
        
        String result = template;
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            String placeholder = prefix + entry.getKey() + suffix;
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            result = result.replace(placeholder, value);
        }
        
        return result;
    }
}
