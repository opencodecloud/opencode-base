package cloud.opencode.base.i18n.fallback;

import java.util.List;
import java.util.Locale;

/**
 * SPI for customizing the locale fallback chain in message resolution
 * 用于自定义消息解析中区域回退链的 SPI
 *
 * <p>Implementations define the ordered list of locales to try when a message
 * cannot be found for the requested locale. The default behavior falls back from
 * the requested locale directly to the global default locale.</p>
 * <p>实现类定义了当请求区域找不到消息时要尝试的有序区域列表。默认行为从请求区域直接回退到全局默认区域。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Custom fallback chains per locale - 每个区域的自定义回退链</li>
 *   <li>Language-region to language-only fallback - 语言+地区到纯语言的回退</li>
 *   <li>Cross-language fallback (e.g., pt-BR → es → en) - 跨语言回退</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * LocaleFallbackStrategy strategy = ChainedLocaleFallback.builder()
 *     .chain(Locale.of("pt", "BR"), Locale.of("pt", "PT"), Locale.of("es"), Locale.ENGLISH)
 *     .chain(Locale.of("zh", "TW"), Locale.of("zh", "HK"), Locale.CHINESE)
 *     .ultimateFallback(Locale.ENGLISH)
 *     .build();
 *
 * OpenI18n.setFallbackStrategy(strategy);
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
 * @since JDK 25, opencode-base-i18n V1.0.3
 */
public interface LocaleFallbackStrategy {

    /**
     * Returns the ordered fallback chain for the given locale
     * 返回给定区域的有序回退链
     *
     * <p>The returned list should include the original locale as the first element,
     * followed by fallback locales in priority order. The list must not be empty.</p>
     * <p>返回的列表应以原始区域为第一个元素，后跟按优先级排列的回退区域。列表不能为空。</p>
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * getFallbackChain(Locale.of("pt","BR"))
     *   = [pt-BR, pt-PT, es, en]  // custom chain
     *
     * getFallbackChain(Locale.of("fr","CA"))
     *   = [fr-CA, fr, en]         // default language fallback
     * </pre>
     *
     * @param locale the requested locale | 请求的区域
     * @return ordered list of locales to try | 按顺序尝试的区域列表（不能为空）
     */
    List<Locale> getFallbackChain(Locale locale);

    /**
     * Returns a default strategy that falls back from region to language only
     * 返回从地区回退到纯语言的默认策略
     *
     * @return default fallback strategy | 默认回退策略
     */
    static LocaleFallbackStrategy defaultStrategy() {
        return locale -> {
            String language = locale.getLanguage();
            String country  = locale.getCountry();
            if (!country.isEmpty()) {
                return List.of(locale, Locale.of(language));
            }
            return List.of(locale);
        };
    }
}
