package cloud.opencode.base.i18n.key;

import cloud.opencode.base.i18n.OpenI18n;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Structured internationalized message capturing key, locale, formatted text, and parameters
 * 结构化国际化消息，捕获键、区域、格式化文本和参数
 *
 * <p>Useful for logging, audit trails, and error responses where both the formatted
 * message AND the original key/parameters are needed.</p>
 * <p>适用于需要格式化消息和原始键/参数的日志记录、审计跟踪和错误响应场景。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable record - 不可变记录</li>
 *   <li>Captures key, locale, formatted text, and named params - 捕获键、区域、格式化文本和命名参数</li>
 *   <li>Static factories for convenient construction - 便捷构建的静态工厂</li>
 *   <li>Resolves via OpenI18n when built from I18nKey - 通过 I18nKey 构建时借助 OpenI18n 解析</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // From raw key and args
 * I18nMessage msg = I18nMessage.of("user.welcome", Locale.ENGLISH, "Alice");
 * log.info("i18n: key={}, locale={}, text={}", msg.key(), msg.locale(), msg.formatted());
 *
 * // From I18nKey enum
 * I18nMessage msg = I18nMessage.of(AppMessage.WELCOME, Locale.CHINESE, "张三");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: key and locale must not be null - 空值安全: key 和 locale 不能为 null</li>
 * </ul>
 *
 * @param key       the message key | 消息键
 * @param locale    the resolved locale | 解析的区域
 * @param formatted the formatted message text | 格式化的消息文本
 * @param params    the named parameters used (may be empty) | 使用的命名参数（可能为空）
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-i18n V1.0.3
 */
public record I18nMessage(String key, Locale locale, String formatted, Map<String, Object> params) {

    // ==================== Compact Constructor | 紧凑构造方法 ====================

    /**
     * Validates that key and locale are not null
     * 验证 key 和 locale 不为 null
     */
    public I18nMessage {
        Objects.requireNonNull(key, "Message key must not be null");
        if (key.isBlank()) throw new IllegalArgumentException("Message key must not be blank");
        Objects.requireNonNull(locale, "Locale must not be null");
        Objects.requireNonNull(formatted, "Formatted message must not be null");
        params = params != null ? Map.copyOf(params) : Map.of();
    }

    // ==================== Static Factories | 静态工厂 ====================

    /**
     * Creates an I18nMessage from a raw key and formatted text (no params)
     * 从原始键和格式化文本创建 I18nMessage（无参数）
     *
     * @param key       the message key | 消息键
     * @param locale    the locale | 区域
     * @param formatted the pre-formatted text | 预格式化文本
     * @return the I18nMessage | 国际化消息
     */
    public static I18nMessage of(String key, Locale locale, String formatted) {
        return new I18nMessage(key, locale, formatted, Map.of());
    }

    /**
     * Creates an I18nMessage from a raw key, formatted text and named params
     * 从原始键、格式化文本和命名参数创建 I18nMessage
     *
     * @param key       the message key | 消息键
     * @param locale    the locale | 区域
     * @param formatted the pre-formatted text | 预格式化文本
     * @param params    the named parameters | 命名参数
     * @return the I18nMessage | 国际化消息
     */
    public static I18nMessage of(String key, Locale locale, String formatted, Map<String, Object> params) {
        return new I18nMessage(key, locale, formatted, params);
    }

    /**
     * Creates an I18nMessage by resolving the key via OpenI18n with positional args
     * 通过 OpenI18n 使用位置参数解析键创建 I18nMessage
     *
     * @param key    the message key | 消息键
     * @param locale the locale | 区域
     * @param args   positional arguments | 位置参数
     * @return the I18nMessage | 国际化消息
     */
    public static I18nMessage resolve(String key, Locale locale, Object... args) {
        String text = OpenI18n.get(key, locale, args);
        return new I18nMessage(key, locale, text, Map.of());
    }

    /**
     * Creates an I18nMessage by resolving the key via OpenI18n with named params
     * 通过 OpenI18n 使用命名参数解析键创建 I18nMessage
     *
     * @param key    the message key | 消息键
     * @param locale the locale | 区域
     * @param params named parameters | 命名参数
     * @return the I18nMessage | 国际化消息
     */
    public static I18nMessage resolve(String key, Locale locale, Map<String, Object> params) {
        String text = OpenI18n.get(key, locale, params);
        return new I18nMessage(key, locale, text, params);
    }

    /**
     * Creates an I18nMessage by resolving from an I18nKey with positional args
     * 通过 I18nKey 使用位置参数解析创建 I18nMessage
     *
     * @param i18nKey the message key | I18n 键
     * @param locale  the locale | 区域
     * @param args    positional arguments | 位置参数
     * @return the I18nMessage | 国际化消息
     */
    public static I18nMessage of(I18nKey i18nKey, Locale locale, Object... args) {
        Objects.requireNonNull(i18nKey, "I18nKey must not be null");
        String key  = i18nKey.key();
        String text = OpenI18n.get(key, locale, args);
        return new I18nMessage(key, locale, text, Map.of());
    }

    // ==================== Convenience Methods | 便捷方法 ====================

    /**
     * Returns the formatted message text (alias for formatted())
     * 返回格式化消息文本（formatted() 的别名）
     *
     * @return formatted message | 格式化消息
     */
    public String text() {
        return formatted;
    }

    /**
     * Returns whether this message was resolved with any parameters
     * 返回此消息是否使用了参数
     *
     * @return true if params are present | 如果有参数则返回 true
     */
    public boolean hasParams() {
        return !params.isEmpty();
    }

    @Override
    public String toString() {
        return formatted;
    }
}
