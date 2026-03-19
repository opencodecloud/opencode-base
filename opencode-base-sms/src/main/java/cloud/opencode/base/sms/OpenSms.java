package cloud.opencode.base.sms;

import cloud.opencode.base.sms.config.SmsConfig;
import cloud.opencode.base.sms.message.SmsMessage;
import cloud.opencode.base.sms.message.SmsResult;
import cloud.opencode.base.sms.provider.SmsProvider;
import cloud.opencode.base.sms.provider.SmsProviderFactory;
import cloud.opencode.base.sms.template.SmsTemplate;
import cloud.opencode.base.sms.template.SmsTemplateRegistry;

import java.util.List;
import java.util.Map;

/**
 * Open SMS
 * 开放短信
 *
 * <p>Main facade for SMS operations.</p>
 * <p>短信操作的主要门面。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Send SMS by phone number and content - 通过手机号和内容发送短信</li>
 *   <li>Template-based SMS sending - 基于模板的短信发送</li>
 *   <li>Multiple provider support (Aliyun, Tencent, Huawei, etc.) - 多服务商支持</li>
 *   <li>Console mock mode for testing - 控制台模拟模式用于测试</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create from config
 * OpenSms sms = OpenSms.of(SmsConfig.builder()
 *     .providerType(SmsProviderType.ALIYUN)
 *     .accessKey("key").secretKey("secret")
 *     .signName("MyApp").build());
 *
 * // Send simple SMS
 * SmsResult result = sms.send("13800138000", "Your code is 1234");
 *
 * // Console mock for testing
 * OpenSms consoleSms = OpenSms.console();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 *   <li>Null-safe: No (requires non-null provider) - 空值安全: 否（需要非空提供商）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 */
public final class OpenSms {

    private final SmsProvider provider;
    private final SmsTemplateRegistry templateRegistry;

    private OpenSms(SmsProvider provider, SmsTemplateRegistry templateRegistry) {
        this.provider = provider;
        this.templateRegistry = templateRegistry;
    }

    /**
     * Create from config
     * 从配置创建
     *
     * @param config the config | 配置
     * @return the OpenSms instance | OpenSms实例
     */
    public static OpenSms of(SmsConfig config) {
        return new OpenSms(SmsProviderFactory.create(config), new SmsTemplateRegistry());
    }

    /**
     * Create from provider
     * 从提供商创建
     *
     * @param provider the provider | 提供商
     * @return the OpenSms instance | OpenSms实例
     */
    public static OpenSms of(SmsProvider provider) {
        return new OpenSms(provider, new SmsTemplateRegistry());
    }

    /**
     * Create console mock instance
     * 创建控制台模拟实例
     *
     * @return the OpenSms instance | OpenSms实例
     */
    public static OpenSms console() {
        return new OpenSms(SmsProviderFactory.console(), new SmsTemplateRegistry());
    }

    // === Send methods ===

    /**
     * Send SMS
     * 发送短信
     *
     * @param phoneNumber the phone number | 手机号码
     * @param content the content | 内容
     * @return the result | 结果
     */
    public SmsResult send(String phoneNumber, String content) {
        return provider.send(SmsMessage.of(phoneNumber, content));
    }

    /**
     * Send SMS message
     * 发送短信消息
     *
     * @param message the message | 消息
     * @return the result | 结果
     */
    public SmsResult send(SmsMessage message) {
        return provider.send(message);
    }

    /**
     * Send batch SMS
     * 批量发送短信
     *
     * @param messages the messages | 消息列表
     * @return the results | 结果列表
     */
    public List<SmsResult> sendBatch(List<SmsMessage> messages) {
        return provider.sendBatch(messages);
    }

    /**
     * Send to multiple recipients
     * 发送给多个收件人
     *
     * @param phoneNumbers the phone numbers | 手机号码列表
     * @param content the content | 内容
     * @return the results | 结果列表
     */
    public List<SmsResult> sendToAll(List<String> phoneNumbers, String content) {
        List<SmsMessage> messages = phoneNumbers.stream()
            .map(phone -> SmsMessage.of(phone, content))
            .toList();
        return provider.sendBatch(messages);
    }

    // === Template methods ===

    /**
     * Register template
     * 注册模板
     *
     * @param id the template ID | 模板ID
     * @param content the template content | 模板内容
     * @return this | 此对象
     */
    public OpenSms registerTemplate(String id, String content) {
        templateRegistry.register(id, content);
        return this;
    }

    /**
     * Register template
     * 注册模板
     *
     * @param template the template | 模板
     * @return this | 此对象
     */
    public OpenSms registerTemplate(SmsTemplate template) {
        templateRegistry.register(template);
        return this;
    }

    /**
     * Send using template
     * 使用模板发送
     *
     * @param templateId the template ID | 模板ID
     * @param phoneNumber the phone number | 手机号码
     * @param variables the variables | 变量
     * @return the result | 结果
     */
    public SmsResult sendTemplate(String templateId, String phoneNumber, Map<String, String> variables) {
        SmsMessage message = templateRegistry.createMessage(templateId, phoneNumber, variables);
        return provider.send(message);
    }

    /**
     * Send template to multiple recipients
     * 使用模板发送给多个收件人
     *
     * @param templateId the template ID | 模板ID
     * @param phoneNumbers the phone numbers | 手机号码列表
     * @param variables the variables | 变量
     * @return the results | 结果列表
     */
    public List<SmsResult> sendTemplateToAll(String templateId, List<String> phoneNumbers, Map<String, String> variables) {
        List<SmsMessage> messages = phoneNumbers.stream()
            .map(phone -> templateRegistry.createMessage(templateId, phone, variables))
            .toList();
        return provider.sendBatch(messages);
    }

    // === Utility methods ===

    /**
     * Get provider
     * 获取提供商
     *
     * @return the provider | 提供商
     */
    public SmsProvider getProvider() {
        return provider;
    }

    /**
     * Get template registry
     * 获取模板注册表
     *
     * @return the registry | 注册表
     */
    public SmsTemplateRegistry getTemplateRegistry() {
        return templateRegistry;
    }

    /**
     * Check if provider is available
     * 检查提供商是否可用
     *
     * @return true if available | 如果可用返回true
     */
    public boolean isAvailable() {
        return provider.isAvailable();
    }

    /**
     * Close provider
     * 关闭提供商
     */
    public void close() {
        provider.close();
    }
}
