package cloud.opencode.base.i18n.resolver;

import cloud.opencode.base.i18n.spi.LocaleResolver;

import java.util.Locale;

/**
 * Fixed locale resolver that always returns the same locale
 * 始终返回固定Locale的解析器
 *
 * <p>Returns a fixed, immutable locale. Useful for single-locale applications
 * or when locale is determined at startup.</p>
 * <p>返回固定不变的Locale。适用于单Locale应用或启动时确定Locale的场景。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable locale - 不可变Locale</li>
 *   <li>Zero overhead - 零开销</li>
 *   <li>Thread-safe - 线程安全</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * FixedLocaleResolver resolver = new FixedLocaleResolver(Locale.CHINESE);
 * Locale current = resolver.resolve(); // Always zh
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Immutable: Yes - 不可变: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-i18n V1.0.0
 */
public class FixedLocaleResolver implements LocaleResolver {

    private final Locale locale;

    /**
     * Creates a resolver with the system default locale
     * 使用系统默认Locale创建解析器
     */
    public FixedLocaleResolver() {
        this(Locale.getDefault());
    }

    /**
     * Creates a resolver with the specified locale
     * 使用指定的Locale创建解析器
     *
     * @param locale the fixed locale | 固定地区
     */
    public FixedLocaleResolver(Locale locale) {
        this.locale = locale != null ? locale : Locale.getDefault();
    }

    @Override
    public Locale resolve() {
        return locale;
    }

    @Override
    public void setLocale(Locale locale) {
        throw new UnsupportedOperationException("FixedLocaleResolver does not support changing locale");
    }

    @Override
    public void reset() {
        // No-op for fixed resolver
    }

    /**
     * Gets the fixed locale
     * 获取固定的Locale
     *
     * @return the fixed locale | 固定地区
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Creates a resolver for a language tag
     * 为语言标签创建解析器
     *
     * @param languageTag the IETF BCP 47 language tag | IETF BCP 47语言标签
     * @return resolver | 解析器
     */
    public static FixedLocaleResolver forLanguageTag(String languageTag) {
        return new FixedLocaleResolver(Locale.forLanguageTag(languageTag));
    }

    /**
     * Creates a resolver for Chinese locale
     * 为中文Locale创建解析器
     *
     * @return resolver for Chinese | 中文解析器
     */
    public static FixedLocaleResolver chinese() {
        return new FixedLocaleResolver(Locale.CHINESE);
    }

    /**
     * Creates a resolver for English locale
     * 为英文Locale创建解析器
     *
     * @return resolver for English | 英文解析器
     */
    public static FixedLocaleResolver english() {
        return new FixedLocaleResolver(Locale.ENGLISH);
    }
}
