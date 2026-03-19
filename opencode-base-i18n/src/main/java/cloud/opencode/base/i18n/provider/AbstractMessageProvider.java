package cloud.opencode.base.i18n.provider;

import cloud.opencode.base.i18n.spi.MessageProvider;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Abstract base class for message providers
 * 消息提供者的抽象基类
 *
 * <p>Provides common functionality for message provider implementations,
 * including locale fallback handling.</p>
 * <p>为消息提供者实现提供通用功能，包括Locale回退处理。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Locale fallback chain - Locale回退链</li>
 *   <li>Template method pattern - 模板方法模式</li>
 *   <li>Common utilities - 通用工具方法</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Extend to create a custom message provider
 * // 扩展以创建自定义消息提供者
 * public class DbMessageProvider extends AbstractMessageProvider {
 *     @Override
 *     protected String resolveMessage(String key, Locale locale) {
 *         return db.findMessage(key, locale);
 *     }
 * }
 * }</pre>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-i18n V1.0.0
 */
public abstract class AbstractMessageProvider implements MessageProvider {

    private Locale defaultLocale = Locale.getDefault();
    private boolean useCodeAsDefaultMessage = false;

    /**
     * Creates an abstract message provider
     * 创建抽象消息提供者
     */
    protected AbstractMessageProvider() {
    }

    @Override
    public Optional<String> getMessageTemplate(String key, Locale locale) {
        // Try exact locale (e.g., zh_TW_HK)
        Optional<String> message = doGetMessage(key, locale);
        if (message.isPresent()) {
            return message;
        }

        // Try language + country without variant (e.g., zh_TW)
        if (!locale.getVariant().isEmpty()) {
            Locale langCountry = Locale.of(locale.getLanguage(), locale.getCountry());
            message = doGetMessage(key, langCountry);
            if (message.isPresent()) {
                return message;
            }
        }

        // Try language only (e.g., zh)
        if (!locale.getCountry().isEmpty()) {
            Locale languageOnly = Locale.of(locale.getLanguage());
            message = doGetMessage(key, languageOnly);
            if (message.isPresent()) {
                return message;
            }
        }

        // Try default locale fallback chain
        if (!locale.equals(defaultLocale)) {
            message = doGetMessage(key, defaultLocale);
            if (message.isPresent()) {
                return message;
            }

            // Try default locale language + country (if has variant)
            if (!defaultLocale.getVariant().isEmpty()) {
                Locale defaultLangCountry = Locale.of(defaultLocale.getLanguage(), defaultLocale.getCountry());
                message = doGetMessage(key, defaultLangCountry);
                if (message.isPresent()) {
                    return message;
                }
            }

            // Try default locale language only
            if (!defaultLocale.getCountry().isEmpty()) {
                Locale defaultLanguageOnly = Locale.of(defaultLocale.getLanguage());
                message = doGetMessage(key, defaultLanguageOnly);
                if (message.isPresent()) {
                    return message;
                }
            }
        }

        // Try root locale
        message = doGetMessage(key, Locale.ROOT);
        if (message.isPresent()) {
            return message;
        }

        return useCodeAsDefaultMessage ? Optional.of(key) : Optional.empty();
    }

    /**
     * Gets a message from the data source
     * 从数据源获取消息
     *
     * @param key    the message key | 消息键
     * @param locale the locale | 地区
     * @return optional containing message | 包含消息的Optional
     */
    protected abstract Optional<String> doGetMessage(String key, Locale locale);

    /**
     * Sets the default locale
     * 设置默认Locale
     *
     * @param defaultLocale the default locale | 默认地区
     */
    public void setDefaultLocale(Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
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

    /**
     * Sets whether to use the key as the default message
     * 设置是否使用键作为默认消息
     *
     * @param useCodeAsDefaultMessage whether to use key | 是否使用键
     */
    public void setUseCodeAsDefaultMessage(boolean useCodeAsDefaultMessage) {
        this.useCodeAsDefaultMessage = useCodeAsDefaultMessage;
    }

    /**
     * Creates a cache key combining message key and locale
     * 创建组合消息键和Locale的缓存键
     *
     * @param key    the message key | 消息键
     * @param locale the locale | 地区
     * @return cache key | 缓存键
     */
    protected String createCacheKey(String key, Locale locale) {
        return key + "_" + locale.toString();
    }
}
