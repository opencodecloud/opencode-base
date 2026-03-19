package cloud.opencode.base.i18n.resolver;

import cloud.opencode.base.i18n.spi.LocaleResolver;

import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Locale resolver based on Accept-Language header
 * 基于Accept-Language头的Locale解析器
 *
 * <p>Parses the Accept-Language HTTP header to determine the locale.
 * Supports quality values and locale matching.</p>
 * <p>解析Accept-Language HTTP头来确定Locale。支持质量值和Locale匹配。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Accept-Language parsing - Accept-Language解析</li>
 *   <li>Quality value support - 质量值支持</li>
 *   <li>Supported locale filtering - 支持的Locale过滤</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver(
 *     () -> "zh-CN,zh;q=0.9,en;q=0.8"
 * );
 * Locale current = resolver.resolve(); // zh_CN
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-i18n V1.0.0
 */
public class AcceptHeaderLocaleResolver implements LocaleResolver {

    private static final Pattern LOCALE_PATTERN = Pattern.compile(
            "([a-zA-Z]{1,8}(?:-[a-zA-Z0-9]{1,8})*)(?:;q=([0-9](?:\\.[0-9]+)?))?"
    );

    private final Supplier<String> headerSupplier;
    private final Locale defaultLocale;
    private final Set<Locale> supportedLocales;

    /**
     * Creates a resolver with header supplier
     * 使用头部提供者创建解析器
     *
     * @param headerSupplier supplier for Accept-Language header | Accept-Language头的提供者
     */
    public AcceptHeaderLocaleResolver(Supplier<String> headerSupplier) {
        this(headerSupplier, Locale.getDefault(), null);
    }

    /**
     * Creates a resolver with header supplier and default locale
     * 使用头部提供者和默认Locale创建解析器
     *
     * @param headerSupplier supplier for Accept-Language header | Accept-Language头的提供者
     * @param defaultLocale  the default locale | 默认地区
     */
    public AcceptHeaderLocaleResolver(Supplier<String> headerSupplier, Locale defaultLocale) {
        this(headerSupplier, defaultLocale, null);
    }

    /**
     * Creates a resolver with header supplier, default locale and supported locales
     * 使用头部提供者、默认Locale和支持的Locale创建解析器
     *
     * @param headerSupplier   supplier for Accept-Language header | Accept-Language头的提供者
     * @param defaultLocale    the default locale | 默认地区
     * @param supportedLocales set of supported locales | 支持的Locale集合
     */
    public AcceptHeaderLocaleResolver(Supplier<String> headerSupplier, Locale defaultLocale,
                                      Set<Locale> supportedLocales) {
        this.headerSupplier = headerSupplier;
        this.defaultLocale = defaultLocale != null ? defaultLocale : Locale.getDefault();
        this.supportedLocales = supportedLocales != null ? new HashSet<>(supportedLocales) : null;
    }

    @Override
    public Locale resolve() {
        String header = headerSupplier.get();
        if (header == null || header.isBlank()) {
            return defaultLocale;
        }

        List<LocaleQuality> locales = parseAcceptLanguage(header);
        if (locales.isEmpty()) {
            return defaultLocale;
        }

        // Sort by quality descending
        locales.sort((a, b) -> Double.compare(b.quality(), a.quality()));

        for (LocaleQuality lq : locales) {
            Locale locale = lq.locale();
            if (supportedLocales == null) {
                return locale;
            }
            if (supportedLocales.contains(locale)) {
                return locale;
            }
            // Try language only match
            Locale languageOnly = Locale.of(locale.getLanguage());
            if (supportedLocales.contains(languageOnly)) {
                return languageOnly;
            }
            // Try to find a match for the language
            for (Locale supported : supportedLocales) {
                if (supported.getLanguage().equals(locale.getLanguage())) {
                    return supported;
                }
            }
        }

        return defaultLocale;
    }

    @Override
    public void setLocale(Locale locale) {
        throw new UnsupportedOperationException(
                "AcceptHeaderLocaleResolver does not support setting locale directly"
        );
    }

    @Override
    public void reset() {
        // No-op
    }

    /**
     * Parses Accept-Language header value
     * 解析Accept-Language头值
     *
     * @param header the header value | 头值
     * @return list of locale with quality | 带质量值的Locale列表
     */
    public List<LocaleQuality> parseAcceptLanguage(String header) {
        List<LocaleQuality> result = new ArrayList<>();
        Matcher matcher = LOCALE_PATTERN.matcher(header);

        while (matcher.find()) {
            String languageTag = matcher.group(1);
            String qualityStr = matcher.group(2);
            double quality = qualityStr != null ? Double.parseDouble(qualityStr) : 1.0;

            try {
                Locale locale = Locale.forLanguageTag(languageTag);
                if (!locale.getLanguage().isEmpty()) {
                    result.add(new LocaleQuality(locale, quality));
                }
            } catch (Exception e) {
                // Skip invalid locale tags
            }
        }

        return result;
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
     * Gets the supported locales
     * 获取支持的Locale集合
     *
     * @return supported locales | 支持的Locale集合
     */
    public Set<Locale> getSupportedLocales() {
        return supportedLocales != null ? Collections.unmodifiableSet(supportedLocales) : null;
    }

    /**
     * Locale with quality value
     * 带质量值的Locale
     *
     * @param locale  the locale | 地区
     * @param quality the quality value (0.0-1.0) | 质量值（0.0-1.0）
     */
    public record LocaleQuality(Locale locale, double quality) {
    }
}
