package cloud.opencode.base.sms.provider;

import cloud.opencode.base.sms.config.SmsConfig;
import cloud.opencode.base.sms.config.SmsProviderType;
import cloud.opencode.base.sms.message.SmsMessage;
import cloud.opencode.base.sms.message.SmsResult;

import java.time.Instant;
import java.util.UUID;

/**
 * Console SMS Provider
 * 控制台短信提供商
 *
 * <p>Mock provider that prints SMS to console for testing.</p>
 * <p>用于测试的将短信打印到控制台的模拟提供商。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Console logging of SMS details - 控制台打印短信详情</li>
 *   <li>Phone number masking in output - 输出中手机号脱敏</li>
 *   <li>Always returns success result - 始终返回成功结果</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SmsProvider console = new ConsoleSmsProvider();
 * SmsResult result = console.send(SmsMessage.of("13800138000", "Test"));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless send) - 线程安全: 是（无状态发送）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 */
public class ConsoleSmsProvider extends AbstractSmsProvider {

    private static final System.Logger logger = System.getLogger(ConsoleSmsProvider.class.getName());
    private static final String NAME = "console";

    public ConsoleSmsProvider(SmsConfig config) {
        super(config);
    }

    public ConsoleSmsProvider() {
        super(SmsConfig.builder()
                .providerType(SmsProviderType.CONSOLE)
                .build());
    }

    @Override
    protected SmsResult doSend(SmsMessage message) {
        String messageId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String maskedPhone = maskPhoneNumber(message.phoneNumber());

        StringBuilder sb = new StringBuilder();
        sb.append("========== SMS ==========\n");
        sb.append("To: ").append(maskedPhone).append('\n');
        sb.append("Content: ").append(message.content()).append('\n');
        if (message.templateId() != null) {
            sb.append("Template: ").append(message.templateId()).append('\n');
        }
        if (message.variables() != null && !message.variables().isEmpty()) {
            sb.append("Variables: ").append(message.variables()).append('\n');
        }
        sb.append("Time: ").append(Instant.now()).append('\n');
        sb.append("MessageId: ").append(messageId).append('\n');
        sb.append("=========================");

        logger.log(System.Logger.Level.INFO, sb.toString());

        return SmsResult.success(messageId, message.phoneNumber());
    }

    /**
     * Masks a phone number, keeping first 3 and last 4 digits.
     * 对手机号进行脱敏，保留前3位和后4位。
     *
     * @param phone the phone number | 手机号
     * @return the masked phone number | 脱敏后的手机号
     */
    private static String maskPhoneNumber(String phone) {
        if (phone == null || phone.length() <= 7) {
            return "****";
        }
        return phone.substring(0, 3) + "*".repeat(phone.length() - 7) + phone.substring(phone.length() - 4);
    }

    @Override
    protected void validateMessage(SmsMessage message) {
        // More lenient validation for console provider
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }
        if (message.phoneNumber() == null || message.phoneNumber().isBlank()) {
            throw new IllegalArgumentException("Phone number cannot be null or blank");
        }
        if (message.content() == null || message.content().isBlank()) {
            throw new IllegalArgumentException("Content cannot be null or blank");
        }
    }

    @Override
    public String getName() {
        return NAME;
    }
}
