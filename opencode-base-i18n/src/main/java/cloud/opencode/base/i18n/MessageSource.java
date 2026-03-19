package cloud.opencode.base.i18n;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Message source interface for internationalized message retrieval
 * 国际化消息获取的消息源接口
 *
 * <p>Defines the core contract for retrieving and formatting internationalized
 * messages from various data sources.</p>
 * <p>定义从各种数据源获取和格式化国际化消息的核心契约。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Message retrieval with parameters - 带参数的消息获取</li>
 *   <li>Optional message support - 可选消息支持</li>
 *   <li>Template access - 模板访问</li>
 *   <li>Locale enumeration - Locale枚举</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * MessageSource source = ...;
 * String msg = source.getMessage("user.welcome", Locale.CHINESE, "张三");
 * Optional<String> opt = source.getMessageOptional("unknown.key", Locale.ENGLISH);
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
public interface MessageSource {

    /**
     * Gets a formatted message
     * 获取格式化后的消息
     *
     * @param key    the message key | 消息键
     * @param locale the locale | 地区
     * @param args   format arguments | 格式化参数
     * @return formatted message | 格式化后的消息
     */
    String getMessage(String key, Locale locale, Object... args);

    /**
     * Gets a formatted message as Optional
     * 获取格式化后的消息（Optional）
     *
     * @param key    the message key | 消息键
     * @param locale the locale | 地区
     * @param args   format arguments | 格式化参数
     * @return optional containing message if found | 如果找到则包含消息的Optional
     */
    Optional<String> getMessageOptional(String key, Locale locale, Object... args);

    /**
     * Gets the raw message template
     * 获取原始消息模板
     *
     * @param key    the message key | 消息键
     * @param locale the locale | 地区
     * @return optional containing template if found | 如果找到则包含模板的Optional
     */
    Optional<String> getMessageTemplate(String key, Locale locale);

    /**
     * Checks if a message exists
     * 检查消息是否存在
     *
     * @param key    the message key | 消息键
     * @param locale the locale | 地区
     * @return true if message exists | 如果消息存在返回true
     */
    boolean containsMessage(String key, Locale locale);

    /**
     * Gets all message keys for a locale
     * 获取某个Locale的所有消息键
     *
     * @param locale the locale | 地区
     * @return set of message keys | 消息键集合
     */
    Set<String> getKeys(Locale locale);

    /**
     * Gets all supported locales
     * 获取所有支持的Locale
     *
     * @return set of supported locales | 支持的Locale集合
     */
    Set<Locale> getSupportedLocales();
}
