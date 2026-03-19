package cloud.opencode.base.email.exception;

/**
 * Email Template Exception
 * 邮件模板异常
 *
 * <p>Exception thrown when template rendering fails.</p>
 * <p>模板渲染失败时抛出的异常。</p>
 *
 * <p><strong>Examples | 示例:</strong></p>
 * <ul>
 *   <li>Template not found - 模板未找到</li>
 *   <li>Invalid template syntax - 无效的模板语法</li>
 *   <li>Missing required variable - 缺少必需变量</li>
 * </ul>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Template rendering error handling - 模板渲染错误处理</li>
 *   <li>Error code support - 错误码支持</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
public class EmailTemplateException extends EmailException {

    /**
     * Create template exception with message
     * 使用消息创建模板异常
     *
     * @param message the error message | 错误消息
     */
    public EmailTemplateException(String message) {
        super(message, EmailErrorCode.TEMPLATE_ERROR);
    }

    /**
     * Create template exception with message and cause
     * 使用消息和原因创建模板异常
     *
     * @param message the error message | 错误消息
     * @param cause   the cause | 原因
     */
    public EmailTemplateException(String message, Throwable cause) {
        super(message, cause, null, EmailErrorCode.TEMPLATE_ERROR);
    }
}
