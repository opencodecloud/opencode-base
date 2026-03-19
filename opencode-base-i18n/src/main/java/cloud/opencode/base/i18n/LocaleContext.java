package cloud.opencode.base.i18n;

import java.util.Locale;
import java.util.TimeZone;

/**
 * Locale context containing locale and timezone information
 * Locale上下文，包含地区和时区信息
 *
 * <p>A record that encapsulates both Locale and TimeZone for comprehensive
 * internationalization context management.</p>
 * <p>一个封装了Locale和TimeZone的记录，用于全面的国际化上下文管理。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable context - 不可变上下文</li>
 *   <li>Locale and TimeZone combination - Locale和TimeZone组合</li>
 *   <li>Factory methods for creation - 工厂方法创建</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * LocaleContext ctx = LocaleContext.of(Locale.CHINESE);
 * LocaleContext ctxWithTz = LocaleContext.of(Locale.US, TimeZone.getTimeZone("America/New_York"));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 * </ul>
 *
 * @param locale   the locale | 地区
 * @param timeZone the timezone | 时区
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-i18n V1.0.0
 */
public record LocaleContext(
        Locale locale,
        TimeZone timeZone
) {

    /**
     * Creates a context with only locale
     * 创建仅包含Locale的上下文
     *
     * @param locale the locale | 地区
     * @return locale context | Locale上下文
     */
    public static LocaleContext of(Locale locale) {
        return new LocaleContext(locale, null);
    }

    /**
     * Creates a context with locale and timezone
     * 创建包含Locale和TimeZone的上下文
     *
     * @param locale   the locale | 地区
     * @param timeZone the timezone | 时区
     * @return locale context | Locale上下文
     */
    public static LocaleContext of(Locale locale, TimeZone timeZone) {
        return new LocaleContext(locale, timeZone);
    }

    /**
     * Gets the default context
     * 获取默认上下文
     *
     * @return default context with system locale and timezone | 使用系统地区和时区的默认上下文
     */
    public static LocaleContext getDefault() {
        return new LocaleContext(Locale.getDefault(), TimeZone.getDefault());
    }

    /**
     * Creates a context with default timezone
     * 创建使用默认时区的上下文
     *
     * @param locale the locale | 地区
     * @return locale context | Locale上下文
     */
    public static LocaleContext withDefaultTimeZone(Locale locale) {
        return new LocaleContext(locale, TimeZone.getDefault());
    }
}
