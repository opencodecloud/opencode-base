package cloud.opencode.base.sms.provider;

import cloud.opencode.base.sms.config.SmsConfig;
import cloud.opencode.base.sms.exception.SmsErrorCode;
import cloud.opencode.base.sms.exception.SmsSendException;
import cloud.opencode.base.sms.message.SmsMessage;
import cloud.opencode.base.sms.message.SmsResult;

import java.util.regex.Pattern;

/**
 * Abstract SMS Provider
 * 抽象短信提供商
 *
 * <p>Base class for SMS providers with common functionality.</p>
 * <p>具有通用功能的短信提供商基类。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Phone number validation - 手机号验证</li>
 *   <li>Message content validation (length, emptiness) - 消息内容验证（长度、空值）</li>
 *   <li>Template method pattern (doSend) - 模板方法模式（doSend）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * public class MyProvider extends AbstractSmsProvider {
 *     public MyProvider(SmsConfig config) { super(config); }
 *     protected SmsResult doSend(SmsMessage message) {
 *         // implementation
 *     }
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless validation methods) - 线程安全: 是（无状态验证方法）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 */
public abstract class AbstractSmsProvider implements SmsProvider {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[1-9]\\d{6,14}$");
    private static final Pattern PHONE_SEPARATOR_PATTERN = Pattern.compile("[\\s\\-()]");
    private static final int MAX_MESSAGE_LENGTH = 500;

    protected final SmsConfig config;

    protected AbstractSmsProvider(SmsConfig config) {
        this.config = config;
    }

    @Override
    public SmsResult send(SmsMessage message) {
        // Validate before sending
        validateMessage(message);

        try {
            return doSend(message);
        } catch (SmsSendException e) {
            throw e;
        } catch (Exception e) {
            throw SmsSendException.failed(message.phoneNumber(), e);
        }
    }

    /**
     * Actual send implementation
     * 实际发送实现
     *
     * @param message the message | 消息
     * @return the result | 结果
     */
    protected abstract SmsResult doSend(SmsMessage message);

    /**
     * Validate message
     * 验证消息
     *
     * @param message the message | 消息
     */
    protected void validateMessage(SmsMessage message) {
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }
        validatePhoneNumber(message.phoneNumber());
        validateContent(message.content());
    }

    /**
     * Validate phone number
     * 验证手机号码
     *
     * @param phoneNumber the phone number | 手机号码
     */
    protected void validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new SmsSendException(SmsErrorCode.INVALID_PHONE_NUMBER, phoneNumber);
        }
        String normalized = PHONE_SEPARATOR_PATTERN.matcher(phoneNumber).replaceAll("");
        if (!PHONE_PATTERN.matcher(normalized).matches()) {
            throw new SmsSendException(SmsErrorCode.INVALID_PHONE_NUMBER, phoneNumber);
        }
    }

    /**
     * Validate content
     * 验证内容
     *
     * @param content the content | 内容
     */
    protected void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new SmsSendException(SmsErrorCode.MESSAGE_EMPTY, null);
        }
        if (content.length() > MAX_MESSAGE_LENGTH) {
            throw new SmsSendException(SmsErrorCode.MESSAGE_TOO_LONG, null);
        }
    }

    /**
     * Get sign name
     * 获取签名名称
     *
     * @return the sign name | 签名名称
     */
    protected String getSignName() {
        return config.signName();
    }

    /**
     * Get config
     * 获取配置
     *
     * @return the config | 配置
     */
    protected SmsConfig getConfig() {
        return config;
    }
}
