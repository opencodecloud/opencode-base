package cloud.opencode.base.sms.message;

import java.util.Map;

/**
 * SMS Message
 * 短信消息
 *
 * <p>Represents an SMS message to be sent.</p>
 * <p>表示要发送的短信消息。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Content-based and template-based message creation - 基于内容和模板的消息创建</li>
 *   <li>Defensive copy of template variables - 模板变量防御性复制</li>
 *   <li>Builder pattern support - 构建器模式支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SmsMessage msg = SmsMessage.of("13800138000", "Your code is 1234");
 * SmsMessage tpl = SmsMessage.ofTemplate("13800138000", "TPL_001",
 *     Map.of("code", "1234"));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 * </ul>
 *
 * @param phoneNumber the phone number | 手机号码
 * @param content the message content | 消息内容
 * @param templateId the template ID (optional) | 模板ID（可选）
 * @param variables the template variables (optional) | 模板变量（可选）
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 */
public record SmsMessage(
    String phoneNumber,
    String content,
    String templateId,
    Map<String, String> variables
) {
    public SmsMessage {
        variables = variables != null ? Map.copyOf(variables) : Map.of();
    }

    /**
     * Create message with content
     * 使用内容创建消息
     *
     * @param phoneNumber the phone number | 手机号码
     * @param content the content | 内容
     * @return the message | 消息
     */
    public static SmsMessage of(String phoneNumber, String content) {
        return new SmsMessage(phoneNumber, content, null, null);
    }

    /**
     * Create message with template
     * 使用模板创建消息
     *
     * @param phoneNumber the phone number | 手机号码
     * @param templateId the template ID | 模板ID
     * @param variables the variables | 变量
     * @return the message | 消息
     */
    public static SmsMessage ofTemplate(String phoneNumber, String templateId, Map<String, String> variables) {
        return new SmsMessage(phoneNumber, null, templateId, variables);
    }

    /**
     * Create builder
     * 创建构建器
     *
     * @return the builder | 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for SmsMessage
     * SmsMessage构建器
     */
    public static class Builder {
        private String phoneNumber;
        private String content;
        private String templateId;
        private Map<String, String> variables;

        public Builder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder templateId(String templateId) {
            this.templateId = templateId;
            return this;
        }

        public Builder variables(Map<String, String> variables) {
            this.variables = variables;
            return this;
        }

        public Builder variable(String key, String value) {
            if (this.variables == null) {
                this.variables = new java.util.HashMap<>();
            }
            this.variables.put(key, value);
            return this;
        }

        public SmsMessage build() {
            return new SmsMessage(phoneNumber, content, templateId, variables);
        }
    }
}
