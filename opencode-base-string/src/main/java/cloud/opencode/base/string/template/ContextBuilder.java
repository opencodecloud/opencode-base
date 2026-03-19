package cloud.opencode.base.string.template;

import java.util.*;

/**
 * Context Builder - Builder for template rendering context.
 * 上下文构建器 - 模板渲染上下文的构建器。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Fluent API for building template context - 流式API构建模板上下文</li>
 *   <li>Single and batch variable setting - 单个和批量变量设置</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * TemplateContext ctx = ContextBuilder.create()
 *     .set("name", "World")
 *     .set("greeting", "Hello")
 *     .build();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (mutable builder) - 线程安全: 否（可变构建器）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) per variable binding - 每次变量绑定 O(1)</li>
 *   <li>Space complexity: O(n) where n = context variables - O(n), n为上下文变量数</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
public final class ContextBuilder {
    private final Map<String, Object> variables = new HashMap<>();

    public static ContextBuilder create() {
        return new ContextBuilder();
    }

    public ContextBuilder set(String name, Object value) {
        variables.put(name, value);
        return this;
    }

    public ContextBuilder setAll(Map<String, Object> values) {
        variables.putAll(values);
        return this;
    }

    public TemplateContext build() {
        return new TemplateContext(variables);
    }
}
