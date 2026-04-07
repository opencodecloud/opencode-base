package cloud.opencode.base.i18n.key;

import cloud.opencode.base.i18n.OpenI18n;

import java.util.Locale;
import java.util.Map;

/**
 * Type-safe interface for internationalization message keys
 * 国际化消息键的类型安全接口
 *
 * <p>Implementing this interface (typically via an enum) provides compile-time
 * checked access to i18n messages, eliminating typos in string-based key lookup.</p>
 * <p>实现此接口（通常通过枚举）提供编译时检查的 i18n 消息访问，消除基于字符串的键查找中的拼写错误。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Compile-time key safety - 编译时键安全</li>
 *   <li>Convenient default methods for message retrieval - 便捷的消息获取默认方法</li>
 *   <li>Supports positional and named parameters - 支持位置参数和命名参数</li>
 *   <li>Supports locale override - 支持区域覆盖</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * public enum AppMessage implements I18nKey {
 *     WELCOME("app.welcome"),
 *     ERROR_NOT_FOUND("app.error.notFound");
 *
 *     private final String key;
 *     AppMessage(String key) { this.key = key; }
 *
 *     @Override public String key() { return key; }
 * }
 *
 * // Usage
 * String msg = AppMessage.WELCOME.get("Alice");
 * String localized = AppMessage.ERROR_NOT_FOUND.get(Locale.FRENCH);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless interface) - 线程安全: 是（无状态接口）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-i18n V1.0.3
 */
public interface I18nKey {

    /**
     * Returns the message key string
     * 返回消息键字符串
     *
     * @return the message key | 消息键
     */
    String key();

    /**
     * Gets the message for the current locale with positional arguments
     * 使用位置参数获取当前区域的消息
     *
     * @param args positional format arguments | 位置格式化参数
     * @return formatted message | 格式化消息
     */
    default String get(Object... args) {
        return OpenI18n.get(key(), args);
    }

    /**
     * Gets the message for the specified locale with positional arguments
     * 使用位置参数获取指定区域的消息
     *
     * @param locale the locale | 区域
     * @param args   positional format arguments | 位置格式化参数
     * @return formatted message | 格式化消息
     */
    default String get(Locale locale, Object... args) {
        return OpenI18n.get(key(), locale, args);
    }

    /**
     * Gets the message for the current locale with named parameters
     * 使用命名参数获取当前区域的消息
     *
     * @param params named parameters | 命名参数
     * @return formatted message | 格式化消息
     */
    default String get(Map<String, Object> params) {
        return OpenI18n.get(key(), params);
    }

    /**
     * Gets the message for the specified locale with named parameters
     * 使用命名参数获取指定区域的消息
     *
     * @param locale the locale | 区域
     * @param params named parameters | 命名参数
     * @return formatted message | 格式化消息
     */
    default String get(Locale locale, Map<String, Object> params) {
        return OpenI18n.get(key(), locale, params);
    }

    /**
     * Gets the message or the provided default value if not found
     * 获取消息，未找到时返回提供的默认值
     *
     * @param defaultValue the default value | 默认值
     * @param args         positional format arguments | 位置格式化参数
     * @return formatted message or default | 格式化消息或默认值
     */
    default String getOrDefault(String defaultValue, Object... args) {
        return OpenI18n.getOrDefault(key(), defaultValue, args);
    }

    /**
     * Returns the message key as the string representation
     * 返回消息键作为字符串表示
     *
     * @return the message key | 消息键
     */
    default String toKeyString() {
        return key();
    }
}
