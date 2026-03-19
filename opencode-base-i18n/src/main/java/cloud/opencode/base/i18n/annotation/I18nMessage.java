package cloud.opencode.base.i18n.annotation;

import java.lang.annotation.*;

/**
 * Annotation for marking methods or fields with i18n message keys
 * 用于标记方法或字段的国际化消息键注解
 *
 * <p>This annotation can be used to associate message keys with methods,
 * parameters, or fields for automatic message resolution.</p>
 * <p>此注解可用于将消息键与方法、参数或字段关联，以实现自动消息解析。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Message key binding - 消息键绑定</li>
 *   <li>Default value support - 默认值支持</li>
 *   <li>Multiple target support - 多目标支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // On method
 * @I18nMessage("user.welcome")
 * public String getWelcomeMessage() { ... }
 *
 * // On field with default
 * @I18nMessage(value = "error.notFound", defaultValue = "Resource not found")
 * private String errorMessage;
 *
 * // On exception
 * @I18nMessage("error.validation.failed")
 * public class ValidationException extends RuntimeException { ... }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (annotation) - 线程安全: 是（注解）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-i18n V1.0.0
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface I18nMessage {

    /**
     * The message key
     * 消息键
     *
     * @return message key | 消息键
     */
    String value();

    /**
     * The default message if key is not found
     * 键未找到时的默认消息
     *
     * @return default message | 默认消息
     */
    String defaultValue() default "";

    /**
     * The message bundle base name
     * 消息包的基础名称
     *
     * @return base name | 基础名称
     */
    String bundle() default "";

    /**
     * Argument names for the message template
     * 消息模板的参数名称
     *
     * @return argument names | 参数名称
     */
    String[] args() default {};

    /**
     * Whether to use the key as default if message is not found
     * 如果未找到消息是否使用键作为默认值
     *
     * @return true to use key as default | 如果使用键作为默认值返回true
     */
    boolean useKeyAsDefault() default false;
}
