package cloud.opencode.base.email.internal;

import java.util.Map;

/**
 * Email Template Engine Interface
 * 邮件模板引擎接口
 *
 * <p>Internal interface for template rendering implementations.</p>
 * <p>模板渲染实现的内部接口。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Render templates with variables - 使用变量渲染模板</li>
 *   <li>Support multiple template engines - 支持多种模板引擎</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation dependent - 线程安全: 取决于实现</li>
 * </ul>
 *
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * EmailTemplate engine = SimpleEmailTemplate.getInstance();
 * String html = engine.render("Hello ${name}", Map.of("name", "John"));
 * }</pre>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
public interface EmailTemplate {

    /**
     * Render a template with the given variables
     * 使用给定变量渲染模板
     *
     * @param template  the template content or name | 模板内容或名称
     * @param variables the variables to substitute | 要替换的变量
     * @return the rendered content | 渲染后的内容
     * @throws cloud.opencode.base.email.exception.EmailTemplateException if rendering fails | 渲染失败时抛出
     */
    String render(String template, Map<String, Object> variables);
}
