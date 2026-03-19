package cloud.opencode.base.sms.exception;

/**
 * SMS Template Exception
 * 短信模板异常
 *
 * <p>Exception thrown for template-related errors.</p>
 * <p>模板相关错误时抛出的异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Template ID and variable name tracking - 模板ID和变量名跟踪</li>
 *   <li>Factory methods for common errors - 常见错误的工厂方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw SmsTemplateException.notFound("TPL_001");
 * throw SmsTemplateException.variableMissing("TPL_001", "code");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 */
public class SmsTemplateException extends SmsException {

    private final String templateId;
    private final String variableName;

    public SmsTemplateException(SmsErrorCode errorCode, String templateId) {
        super(errorCode);
        this.templateId = templateId;
        this.variableName = null;
    }

    public SmsTemplateException(SmsErrorCode errorCode, String templateId, String variableName) {
        super(errorCode, "Variable '" + variableName + "' missing in template '" + templateId + "'");
        this.templateId = templateId;
        this.variableName = variableName;
    }

    public String getTemplateId() {
        return templateId;
    }

    public String getVariableName() {
        return variableName;
    }

    public static SmsTemplateException notFound(String templateId) {
        return new SmsTemplateException(SmsErrorCode.TEMPLATE_NOT_FOUND, templateId);
    }

    public static SmsTemplateException invalid(String templateId) {
        return new SmsTemplateException(SmsErrorCode.TEMPLATE_INVALID, templateId);
    }

    public static SmsTemplateException variableMissing(String templateId, String variableName) {
        return new SmsTemplateException(SmsErrorCode.TEMPLATE_VARIABLE_MISSING, templateId, variableName);
    }
}
