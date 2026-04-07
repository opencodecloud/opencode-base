package cloud.opencode.base.i18n.fallback;

import java.util.*;

/**
 * Configurable locale fallback chain implementation
 * 可配置的区域回退链实现
 *
 * <p>Allows defining custom fallback chains for specific locales, enabling
 * cross-language fallback strategies (e.g., pt-BR → pt-PT → es → en).</p>
 * <p>允许为特定区域定义自定义回退链，启用跨语言回退策略（例如 pt-BR → pt-PT → es → en）。</p>
 *
 * <p><strong>Resolution Order | 解析顺序:</strong></p>
 * <ol>
 *   <li>Exact locale match in configured chains - 配置链中的精确区域匹配</li>
 *   <li>Language-only match (e.g., {@code fr-CA} matches {@code fr} chain) - 仅语言匹配</li>
 *   <li>Default: [locale, language-only, ultimateFallback] - 默认：[区域, 纯语言, 最终兜底]</li>
 * </ol>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ChainedLocaleFallback fallback = ChainedLocaleFallback.builder()
 *     .chain(Locale.of("pt", "BR"), Locale.of("pt", "PT"), Locale.of("es"), Locale.ENGLISH)
 *     .chain(Locale.of("zh", "TW"), Locale.of("zh", "HK"), Locale.CHINESE, Locale.ENGLISH)
 *     .ultimateFallback(Locale.ENGLISH)
 *     .build();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after build) - 线程安全: 是（构建后不可变）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-i18n V1.0.3
 */
public final class ChainedLocaleFallback implements LocaleFallbackStrategy {

    /** Exact locale → ordered fallback list | 精确区域 → 有序回退列表 */
    private final Map<Locale, List<Locale>> chains;
    /** Language-only → ordered fallback list | 纯语言 → 有序回退列表 */
    private final Map<String, List<Locale>> languageChains;
    /** Ultimate fallback locale | 最终兜底区域 */
    private final Locale ultimateFallback;

    private ChainedLocaleFallback(Map<Locale, List<Locale>> chains,
                                   Map<String, List<Locale>> languageChains,
                                   Locale ultimateFallback) {
        this.chains          = Collections.unmodifiableMap(new LinkedHashMap<>(chains));
        this.languageChains  = Collections.unmodifiableMap(new LinkedHashMap<>(languageChains));
        this.ultimateFallback = ultimateFallback;
    }

    // ==================== LocaleFallbackStrategy ====================

    /**
     * Returns the fallback chain for the given locale
     * 返回给定区域的回退链
     *
     * @param locale the requested locale | 请求的区域
     * @return ordered fallback chain | 有序回退链（不能为空）
     */
    @Override
    public List<Locale> getFallbackChain(Locale locale) {
        // 1. Exact match
        List<Locale> chain = chains.get(locale);
        if (chain != null) return chain;

        // 2. Language-only match (e.g., fr-CA matches an "fr" chain)
        String language = locale.getLanguage();
        if (!locale.getCountry().isEmpty()) {
            chain = languageChains.get(language);
            if (chain != null) return chain;
        }

        // 3. Default chain: locale, language-only (if different), ultimate fallback
        List<Locale> fallback = new ArrayList<>(3);
        fallback.add(locale);
        if (!locale.getCountry().isEmpty()) {
            fallback.add(Locale.of(language));
        }
        if (ultimateFallback != null && !fallback.contains(ultimateFallback)) {
            fallback.add(ultimateFallback);
        }
        return Collections.unmodifiableList(fallback);
    }

    // ==================== Factory | 工厂方法 ====================

    /**
     * Returns a new builder for ChainedLocaleFallback
     * 返回 ChainedLocaleFallback 的新构建器
     *
     * @return builder | 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    // ==================== Builder ====================

    /**
     * Builder for ChainedLocaleFallback
     * ChainedLocaleFallback 的构建器
     */
    public static final class Builder {

        private final Map<Locale, List<Locale>> chains         = new LinkedHashMap<>();
        private final Map<String, List<Locale>> languageChains = new LinkedHashMap<>();
        private Locale ultimateFallback;

        private Builder() {}

        /**
         * Defines a fallback chain for a specific locale
         * 为特定区域定义回退链
         *
         * <p>The first argument is the source locale; the remaining are tried in order.</p>
         * <p>第一个参数是源区域；其余按顺序尝试。</p>
         *
         * @param source    the locale to configure | 要配置的区域
         * @param fallbacks ordered fallback locales | 按顺序的回退区域
         * @return this builder | 此构建器
         */
        public Builder chain(Locale source, Locale... fallbacks) {
            Objects.requireNonNull(source, "Source locale must not be null");
            List<Locale> chain = new ArrayList<>(1 + fallbacks.length);
            chain.add(source);
            for (Locale fb : fallbacks) {
                if (fb != null) chain.add(fb);
            }
            chains.put(source, Collections.unmodifiableList(chain));
            // Also register language-only entry if source has a country
            if (!source.getCountry().isEmpty()) {
                languageChains.putIfAbsent(source.getLanguage(),
                        Collections.unmodifiableList(chain));
            }
            return this;
        }

        /**
         * Sets the ultimate fallback locale (used when no chain matches)
         * 设置最终兜底区域（无链匹配时使用）
         *
         * @param locale the ultimate fallback | 最终兜底区域
         * @return this builder | 此构建器
         */
        public Builder ultimateFallback(Locale locale) {
            this.ultimateFallback = locale;
            return this;
        }

        /**
         * Builds the ChainedLocaleFallback
         * 构建 ChainedLocaleFallback
         *
         * @return the built instance | 构建的实例
         */
        public ChainedLocaleFallback build() {
            return new ChainedLocaleFallback(chains, languageChains, ultimateFallback);
        }
    }
}
