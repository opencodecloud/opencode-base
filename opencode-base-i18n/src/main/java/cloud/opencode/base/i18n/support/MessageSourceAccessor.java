package cloud.opencode.base.i18n.support;

import cloud.opencode.base.i18n.MessageSource;
import cloud.opencode.base.i18n.exception.OpenNoSuchMessageException;

import java.util.Locale;
import java.util.Optional;

/**
 * Helper class for convenient access to MessageSource
 * 便捷访问MessageSource的辅助类
 *
 * <p>Provides simplified methods for message retrieval with consistent
 * locale handling.</p>
 * <p>提供简化的消息获取方法，具有一致的Locale处理。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Simplified message access - 简化的消息访问</li>
 *   <li>Default locale support - 默认Locale支持</li>
 *   <li>Exception handling options - 异常处理选项</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * MessageSourceAccessor accessor = new MessageSourceAccessor(messageSource);
 * String msg = accessor.getMessage("user.welcome");
 * String msgWithArgs = accessor.getMessage("user.greeting", "Alice");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Depends on underlying MessageSource - 线程安全: 取决于底层MessageSource</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-i18n V1.0.0
 */
public class MessageSourceAccessor {

    private final MessageSource messageSource;
    private final Locale defaultLocale;

    /**
     * Creates an accessor with the system default locale
     * 使用系统默认Locale创建访问器
     *
     * @param messageSource the message source | 消息源
     */
    public MessageSourceAccessor(MessageSource messageSource) {
        this(messageSource, Locale.getDefault());
    }

    /**
     * Creates an accessor with a specified default locale
     * 使用指定的默认Locale创建访问器
     *
     * @param messageSource the message source | 消息源
     * @param defaultLocale the default locale | 默认地区
     */
    public MessageSourceAccessor(MessageSource messageSource, Locale defaultLocale) {
        this.messageSource = messageSource;
        this.defaultLocale = defaultLocale != null ? defaultLocale : Locale.getDefault();
    }

    /**
     * Gets a message using the default locale
     * 使用默认Locale获取消息
     *
     * @param code the message code | 消息代码
     * @return the message | 消息
     * @throws OpenNoSuchMessageException if message not found | 如果未找到消息
     */
    public String getMessage(String code) {
        return getMessage(code, defaultLocale);
    }

    /**
     * Gets a message with arguments using the default locale
     * 使用默认Locale获取带参数的消息
     *
     * @param code the message code | 消息代码
     * @param args the arguments | 参数
     * @return the message | 消息
     * @throws OpenNoSuchMessageException if message not found | 如果未找到消息
     */
    public String getMessage(String code, Object... args) {
        return getMessage(code, defaultLocale, args);
    }

    /**
     * Gets a message with specified locale
     * 使用指定Locale获取消息
     *
     * @param code   the message code | 消息代码
     * @param locale the locale | 地区
     * @return the message | 消息
     * @throws OpenNoSuchMessageException if message not found | 如果未找到消息
     */
    public String getMessage(String code, Locale locale) {
        return messageSource.getMessage(code, locale);
    }

    /**
     * Gets a message with specified locale and arguments
     * 使用指定Locale和参数获取消息
     *
     * @param code   the message code | 消息代码
     * @param locale the locale | 地区
     * @param args   the arguments | 参数
     * @return the message | 消息
     * @throws OpenNoSuchMessageException if message not found | 如果未找到消息
     */
    public String getMessage(String code, Locale locale, Object... args) {
        return messageSource.getMessage(code, locale, args);
    }

    /**
     * Gets a message with default value using the default locale
     * 使用默认Locale获取消息，如果未找到返回默认值
     *
     * @param code         the message code | 消息代码
     * @param defaultValue the default value | 默认值
     * @return the message or default value | 消息或默认值
     */
    public String getMessageOrDefault(String code, String defaultValue) {
        return getMessageOrDefault(code, defaultValue, defaultLocale);
    }

    /**
     * Gets a message with default value using specified locale
     * 使用指定Locale获取消息，如果未找到返回默认值
     *
     * @param code         the message code | 消息代码
     * @param defaultValue the default value | 默认值
     * @param locale       the locale | 地区
     * @return the message or default value | 消息或默认值
     */
    public String getMessageOrDefault(String code, String defaultValue, Locale locale) {
        Optional<String> msg = messageSource.getMessageOptional(code, locale);
        return msg.orElse(defaultValue);
    }

    /**
     * Gets a message with default value and arguments
     * 使用默认值和参数获取消息
     *
     * @param code         the message code | 消息代码
     * @param defaultValue the default value | 默认值
     * @param args         the arguments | 参数
     * @return the message or default value | 消息或默认值
     */
    public String getMessageOrDefault(String code, String defaultValue, Object... args) {
        return getMessageOrDefault(code, defaultValue, defaultLocale, args);
    }

    /**
     * Gets a message with default value, locale and arguments
     * 使用默认值、Locale和参数获取消息
     *
     * @param code         the message code | 消息代码
     * @param defaultValue the default value | 默认值
     * @param locale       the locale | 地区
     * @param args         the arguments | 参数
     * @return the message or default value | 消息或默认值
     */
    public String getMessageOrDefault(String code, String defaultValue, Locale locale, Object... args) {
        try {
            return messageSource.getMessage(code, locale, args);
        } catch (OpenNoSuchMessageException e) {
            return defaultValue;
        }
    }

    /**
     * Gets a message optionally using the default locale
     * 使用默认Locale可选地获取消息
     *
     * @param code the message code | 消息代码
     * @return optional containing message | 包含消息的Optional
     */
    public Optional<String> getMessageOptional(String code) {
        return getMessageOptional(code, defaultLocale);
    }

    /**
     * Gets a message optionally using specified locale
     * 使用指定Locale可选地获取消息
     *
     * @param code   the message code | 消息代码
     * @param locale the locale | 地区
     * @return optional containing message | 包含消息的Optional
     */
    public Optional<String> getMessageOptional(String code, Locale locale) {
        return messageSource.getMessageOptional(code, locale);
    }

    /**
     * Gets the underlying message source
     * 获取底层消息源
     *
     * @return message source | 消息源
     */
    public MessageSource getMessageSource() {
        return messageSource;
    }

    /**
     * Gets the default locale
     * 获取默认Locale
     *
     * @return default locale | 默认地区
     */
    public Locale getDefaultLocale() {
        return defaultLocale;
    }
}
