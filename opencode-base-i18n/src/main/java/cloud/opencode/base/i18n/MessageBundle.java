package cloud.opencode.base.i18n;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Message bundle interface for a specific locale
 * 特定地区的消息包接口
 *
 * <p>Encapsulates a collection of messages for a specific Locale,
 * providing access to individual messages by key.</p>
 * <p>封装特定Locale的消息集合，提供按键访问单个消息的功能。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Message retrieval by key - 按键获取消息</li>
 *   <li>Key existence check - 键存在性检查</li>
 *   <li>Parent bundle support - 父消息包支持</li>
 *   <li>Conversion to Map - 转换为Map</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * MessageBundle bundle = ...;
 * String message = bundle.getMessage("user.welcome");
 * if (bundle.containsKey("error.notFound")) {
 *     // handle error message
 * }
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
public interface MessageBundle {

    /**
     * Gets a message by key
     * 按键获取消息
     *
     * @param key the message key | 消息键
     * @return the message, or null if not found | 消息，如果未找到则返回null
     */
    String getMessage(String key);

    /**
     * Checks if a key exists
     * 检查键是否存在
     *
     * @param key the message key | 消息键
     * @return true if key exists | 如果键存在返回true
     */
    boolean containsKey(String key);

    /**
     * Gets all keys in this bundle
     * 获取此消息包中的所有键
     *
     * @return set of keys | 键集合
     */
    Set<String> getKeys();

    /**
     * Converts to a Map
     * 转换为Map
     *
     * @return message map | 消息Map
     */
    Map<String, String> toMap();

    /**
     * Gets the locale of this bundle
     * 获取此消息包的Locale
     *
     * @return the locale | 地区
     */
    Locale getLocale();

    /**
     * Gets the parent bundle
     * 获取父消息包
     *
     * @return parent bundle, or null if none | 父消息包，如果没有则返回null
     */
    MessageBundle getParent();
}
