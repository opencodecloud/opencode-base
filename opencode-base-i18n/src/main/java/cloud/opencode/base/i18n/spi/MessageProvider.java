package cloud.opencode.base.i18n.spi;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Message provider SPI interface
 * 消息提供者SPI接口
 *
 * <p>Defines the contract for loading messages from any data source.
 * Implementations can load from properties files, databases, Redis, etc.</p>
 * <p>定义从任意数据源加载消息的契约。实现可以从properties文件、数据库、Redis等加载。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Message template retrieval - 消息模板获取</li>
 *   <li>Message existence check - 消息存在性检查</li>
 *   <li>Key enumeration - 键枚举</li>
 *   <li>Cache refresh - 缓存刷新</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * MessageProvider provider = new ResourceBundleProvider("messages");
 * Optional<String> template = provider.getMessageTemplate("user.welcome", Locale.CHINESE);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation dependent - 线程安全: 取决于实现</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-i18n V1.0.0
 */
@FunctionalInterface
public interface MessageProvider {

    /**
     * Gets a message template
     * 获取消息模板
     *
     * @param key    the message key | 消息键
     * @param locale the locale | 地区
     * @return optional containing template if found | 如果找到则包含模板的Optional
     */
    Optional<String> getMessageTemplate(String key, Locale locale);

    /**
     * Checks if a message exists
     * 判断消息是否存在
     *
     * @param key    the message key | 消息键
     * @param locale the locale | 地区
     * @return true if exists | 如果存在返回true
     */
    default boolean containsMessage(String key, Locale locale) {
        return getMessageTemplate(key, locale).isPresent();
    }

    /**
     * Gets all message keys for a locale
     * 获取某个Locale的所有消息键
     *
     * @param locale the locale | 地区
     * @return set of keys | 键集合
     */
    default Set<String> getKeys(Locale locale) {
        return Set.of();
    }

    /**
     * Gets all supported locales
     * 获取所有支持的Locale
     *
     * @return set of locales | Locale集合
     */
    default Set<Locale> getSupportedLocales() {
        return Set.of();
    }

    /**
     * Refreshes the cache
     * 刷新缓存
     */
    default void refresh() {
    }
}
