package cloud.opencode.base.i18n.spi;

import java.util.Locale;

/**
 * Locale resolver SPI interface
 * Locale解析器SPI接口
 *
 * <p>Defines how to determine the current context's Locale.
 * Implementations can resolve from ThreadLocal, request headers, etc.</p>
 * <p>定义如何确定当前上下文的Locale。实现可以从ThreadLocal、请求头等解析。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Locale resolution - Locale解析</li>
 *   <li>Optional locale setting - 可选的Locale设置</li>
 *   <li>Context reset - 上下文重置</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * LocaleResolver resolver = new ThreadLocalLocaleResolver();
 * Locale current = resolver.resolve();
 * resolver.setLocale(Locale.JAPANESE);
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
public interface LocaleResolver {

    /**
     * Resolves the current locale
     * 解析当前Locale
     *
     * @return current locale | 当前Locale
     */
    Locale resolve();

    /**
     * Sets the locale (optional operation)
     * 设置Locale（可选操作）
     *
     * @param locale the locale | 地区
     * @throws UnsupportedOperationException if not supported | 如果不支持则抛出
     */
    default void setLocale(Locale locale) {
        throw new UnsupportedOperationException("setLocale not supported");
    }

    /**
     * Resets the locale (optional operation)
     * 重置Locale（可选操作）
     */
    default void reset() {
    }
}
