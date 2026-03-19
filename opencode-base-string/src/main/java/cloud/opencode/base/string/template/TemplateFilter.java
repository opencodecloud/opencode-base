package cloud.opencode.base.string.template;

/**
 * Template Filter - Filter interface for template value transformation.
 * 模板过滤器 - 用于模板值转换的过滤器接口。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Functional interface for lambda-based filters - 函数式接口支持Lambda过滤器</li>
 *   <li>Value transformation with optional arguments - 值转换支持可选参数</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * TemplateFilter upper = (value, args) -> value.toUpperCase();
 * String result = upper.apply("hello", new String[]{}); // "HELLO"
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Depends on implementation - 线程安全: 取决于实现</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@FunctionalInterface
public interface TemplateFilter {
    String apply(String value, String[] args);
}
