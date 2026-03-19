package cloud.opencode.base.i18n.exception;

import cloud.opencode.base.core.exception.OpenException;

import java.util.Locale;

/**
 * Exception thrown when a message cannot be found
 * 当消息无法找到时抛出的异常
 *
 * <p>This exception is thrown when attempting to retrieve a message that
 * does not exist for the requested key and locale.</p>
 * <p>当尝试获取对于请求的键和Locale不存在的消息时抛出此异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Message code tracking - 消息代码追踪</li>
 *   <li>Locale information - Locale信息</li>
 *   <li>Descriptive error messages - 描述性错误消息</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * try {
 *     String msg = messageSource.getMessage("unknown.key", Locale.CHINESE);
 * } catch (OpenNoSuchMessageException e) {
 *     log.warn("Message not found: key={}, locale={}", e.key(), e.locale());
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable exception) - 线程安全: 是（不可变异常）</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-i18n V1.0.0
 */
public class OpenNoSuchMessageException extends OpenException {

    private static final String COMPONENT = "i18n";

    private final String key;
    private final Locale locale;

    // ==================== 构造方法 ====================

    /**
     * Creates an exception with message key and locale
     * 使用消息键和Locale创建异常
     *
     * @param key    the message key | 消息键
     * @param locale the locale | 地区
     */
    public OpenNoSuchMessageException(String key, Locale locale) {
        super(COMPONENT, null, String.format("Message not found for key '%s' with locale '%s'", key, locale), null);
        this.key = key;
        this.locale = locale;
    }

    /**
     * Creates an exception with message key, locale and cause
     * 使用消息键、Locale和原因创建异常
     *
     * @param key    the message key | 消息键
     * @param locale the locale | 地区
     * @param cause  the cause | 原因
     */
    public OpenNoSuchMessageException(String key, Locale locale, Throwable cause) {
        super(COMPONENT, null, String.format("Message not found for key '%s' with locale '%s'", key, locale), cause);
        this.key = key;
        this.locale = locale;
    }

    // ==================== 访问方法 ====================

    /**
     * Gets the message key that was not found
     * 获取未找到的消息键
     *
     * @return message key | 消息键
     */
    public String key() {
        return key;
    }

    /**
     * Gets the locale used when searching for the message
     * 获取搜索消息时使用的Locale
     *
     * @return locale | 地区
     */
    public Locale locale() {
        return locale;
    }

    // ==================== 工厂方法 ====================

    /**
     * Creates an exception for a missing message
     * 为缺失的消息创建异常
     *
     * @param key    the message key | 消息键
     * @param locale the locale | 地区
     * @return exception | 异常
     */
    public static OpenNoSuchMessageException messageNotFound(String key, Locale locale) {
        return new OpenNoSuchMessageException(key, locale);
    }

    /**
     * Creates an exception for message format failure
     * 为消息格式化失败创建异常
     *
     * @param key    the message key | 消息键
     * @param locale the locale | 地区
     * @param cause  the cause | 原因
     * @return exception | 异常
     */
    public static OpenNoSuchMessageException formatFailed(String key, Locale locale, Throwable cause) {
        return new OpenNoSuchMessageException(key, locale, cause);
    }

    /**
     * Creates an exception for bundle load failure
     * 为资源包加载失败创建异常
     *
     * @param baseName the bundle base name | 资源包基础名称
     * @param locale   the locale | 地区
     * @param cause    the cause | 原因
     * @return exception | 异常
     */
    public static OpenException bundleLoadFailed(String baseName, Locale locale, Throwable cause) {
        return new OpenException(COMPONENT, null,
                String.format("Failed to load resource bundle '%s' for locale '%s'", baseName, locale), cause);
    }
}
