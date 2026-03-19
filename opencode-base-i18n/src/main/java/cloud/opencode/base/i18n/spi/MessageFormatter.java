package cloud.opencode.base.i18n.spi;

import java.util.Locale;
import java.util.Map;

/**
 * Message formatter SPI interface
 * 消息格式化器SPI接口
 *
 * <p>Defines how to format message templates with parameters.
 * Supports both positional and named parameters.</p>
 * <p>定义如何用参数格式化消息模板。支持位置参数和命名参数。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Positional parameter formatting - 位置参数格式化</li>
 *   <li>Named parameter formatting - 命名参数格式化</li>
 *   <li>Cache management - 缓存管理</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * MessageFormatter formatter = new DefaultMessageFormatter();
 * String msg = formatter.format("Hello, {0}!", Locale.ENGLISH, "World");
 * String msg2 = formatter.format("Hello, ${name}!", Locale.ENGLISH, Map.of("name", "World"));
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
public interface MessageFormatter {

    /**
     * Formats a message with positional parameters
     * 使用位置参数格式化消息
     *
     * @param template the message template | 消息模板
     * @param locale   the locale | 地区
     * @param args     format arguments | 格式化参数
     * @return formatted message | 格式化后的消息
     */
    String format(String template, Locale locale, Object... args);

    /**
     * Formats a message with named parameters
     * 使用命名参数格式化消息
     *
     * @param template the message template | 消息模板
     * @param locale   the locale | 地区
     * @param params   named parameters | 命名参数
     * @return formatted message | 格式化后的消息
     */
    String format(String template, Locale locale, Map<String, Object> params);

    /**
     * Clears the formatter cache
     * 清除格式化器缓存
     */
    default void clearCache() {
    }
}
