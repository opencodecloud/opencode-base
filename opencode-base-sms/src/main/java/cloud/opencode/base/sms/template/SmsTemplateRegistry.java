package cloud.opencode.base.sms.template;

import cloud.opencode.base.sms.exception.SmsTemplateException;
import cloud.opencode.base.sms.message.SmsMessage;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SMS Template Registry
 * 短信模板注册表
 *
 * <p>Registry for managing SMS templates.</p>
 * <p>管理短信模板的注册表。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Template registration and lookup - 模板注册和查找</li>
 *   <li>Message creation from templates - 从模板创建消息</li>
 *   <li>Fluent API with method chaining - 流式API方法链</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SmsTemplateRegistry registry = new SmsTemplateRegistry();
 * registry.register("verify", "Your code is ${code}")
 *         .register("welcome", "Welcome, ${name}!");
 * SmsMessage msg = registry.createMessage("verify", "13800138000",
 *     Map.of("code", "1234"));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (ConcurrentHashMap storage) - 线程安全: 是（ConcurrentHashMap存储）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 */
public class SmsTemplateRegistry {

    private final Map<String, SmsTemplate> templates = new ConcurrentHashMap<>();

    /**
     * Register template
     * 注册模板
     *
     * @param template the template | 模板
     * @return this registry | 此注册表
     */
    public SmsTemplateRegistry register(SmsTemplate template) {
        templates.put(template.id(), template);
        return this;
    }

    /**
     * Register template from content
     * 从内容注册模板
     *
     * @param id the template ID | 模板ID
     * @param content the content | 内容
     * @return this registry | 此注册表
     */
    public SmsTemplateRegistry register(String id, String content) {
        return register(SmsTemplate.of(id, content));
    }

    /**
     * Unregister template
     * 取消注册模板
     *
     * @param id the template ID | 模板ID
     * @return this registry | 此注册表
     */
    public SmsTemplateRegistry unregister(String id) {
        templates.remove(id);
        return this;
    }

    /**
     * Get template by ID
     * 按ID获取模板
     *
     * @param id the template ID | 模板ID
     * @return the template or null | 模板或null
     */
    public SmsTemplate get(String id) {
        return templates.get(id);
    }

    /**
     * Get template as Optional
     * 以Optional获取模板
     *
     * @param id the template ID | 模板ID
     * @return the optional template | 可选模板
     */
    public Optional<SmsTemplate> find(String id) {
        return Optional.ofNullable(templates.get(id));
    }

    /**
     * Check if template exists
     * 检查模板是否存在
     *
     * @param id the template ID | 模板ID
     * @return true if exists | 如果存在返回true
     */
    public boolean contains(String id) {
        return templates.containsKey(id);
    }

    /**
     * Get template count
     * 获取模板数量
     *
     * @return the count | 数量
     */
    public int size() {
        return templates.size();
    }

    /**
     * Get all templates
     * 获取所有模板
     *
     * @return the templates | 模板
     */
    public Map<String, SmsTemplate> getAll() {
        return Map.copyOf(templates);
    }

    /**
     * Create message from template
     * 从模板创建消息
     *
     * @param templateId the template ID | 模板ID
     * @param phoneNumber the phone number | 手机号码
     * @param variables the variables | 变量
     * @return the message | 消息
     * @throws SmsTemplateException if template not found or variables missing
     */
    public SmsMessage createMessage(String templateId, String phoneNumber, Map<String, String> variables) {
        SmsTemplate template = templates.get(templateId);
        if (template == null) {
            throw SmsTemplateException.notFound(templateId);
        }

        // Check for missing variables
        var missing = template.getMissingVariables(variables);
        if (!missing.isEmpty()) {
            throw SmsTemplateException.variableMissing(templateId, missing.get(0));
        }

        String content = template.render(variables);
        return SmsMessage.builder()
            .phoneNumber(phoneNumber)
            .content(content)
            .templateId(templateId)
            .variables(variables)
            .build();
    }

    /**
     * Clear all templates
     * 清除所有模板
     */
    public void clear() {
        templates.clear();
    }
}
