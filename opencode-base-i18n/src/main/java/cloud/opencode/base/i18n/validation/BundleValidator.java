package cloud.opencode.base.i18n.validation;

import cloud.opencode.base.i18n.spi.MessageProvider;

import java.util.*;

/**
 * Validates message bundle completeness across locales
 * 验证跨区域消息包的完整性
 *
 * <p>Compares key sets between a base locale and one or more target locales,
 * identifying missing and extra translations.</p>
 * <p>比较基准区域和一个或多个目标区域之间的键集，识别缺失和多余的翻译。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Single locale validation - 单区域验证</li>
 *   <li>Batch validation of all supported locales - 所有支持区域的批量验证</li>
 *   <li>Coverage percentage reporting - 覆盖率百分比报告</li>
 *   <li>Missing and extra key identification - 缺失和多余键识别</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * BundleValidator validator = new BundleValidator(provider);
 *
 * // Validate French against English
 * BundleValidationResult result = validator.validate(Locale.ENGLISH, Locale.FRENCH);
 * System.out.println(result.summary());
 *
 * // Validate all supported locales
 * Map<Locale, BundleValidationResult> all = validator.validateAll(Locale.ENGLISH);
 * all.forEach((locale, r) -> System.out.println(r.summary()));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless after construction) - 线程安全: 是（构建后无状态）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-i18n V1.0.3
 */
public final class BundleValidator {

    private final MessageProvider provider;

    /**
     * Creates a BundleValidator backed by the given MessageProvider
     * 创建由给定 MessageProvider 支持的 BundleValidator
     *
     * @param provider the message provider | 消息提供者
     */
    public BundleValidator(MessageProvider provider) {
        this.provider = Objects.requireNonNull(provider, "MessageProvider must not be null");
    }

    /**
     * Validates a target locale's bundle against the base locale
     * 对目标区域包进行基准区域验证
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * validate(Locale.ENGLISH, Locale.FRENCH)
     *   // Returns BundleValidationResult with missing/extra keys and coverage
     * </pre>
     *
     * @param baseLocale   the reference locale | 参考区域
     * @param targetLocale the locale to validate | 要验证的区域
     * @return validation result | 验证结果
     */
    public BundleValidationResult validate(Locale baseLocale, Locale targetLocale) {
        Objects.requireNonNull(baseLocale,   "baseLocale must not be null");
        Objects.requireNonNull(targetLocale, "targetLocale must not be null");

        Set<String> baseKeys   = provider.getKeys(baseLocale);
        Set<String> targetKeys = provider.getKeys(targetLocale);

        // missing: in base but not target
        Set<String> missing = new LinkedHashSet<>();
        for (String key : baseKeys) {
            if (!targetKeys.contains(key)) missing.add(key);
        }

        // extra: in target but not base
        Set<String> extra = new LinkedHashSet<>();
        for (String key : targetKeys) {
            if (!baseKeys.contains(key)) extra.add(key);
        }

        return new BundleValidationResult(
                baseLocale, targetLocale,
                missing, extra,
                baseKeys.size(), targetKeys.size()
        );
    }

    /**
     * Validates all supported locales against the base locale
     * 验证所有支持区域对基准区域的完整性
     *
     * <p>Iterates over {@link MessageProvider#getSupportedLocales()} and validates
     * each against the base locale, excluding the base locale itself.</p>
     * <p>遍历 {@link MessageProvider#getSupportedLocales()} 并对每个区域进行验证，
     * 排除基准区域本身。</p>
     *
     * @param baseLocale the reference locale | 参考区域
     * @return map of locale → validation result | 区域 → 验证结果的映射
     */
    public Map<Locale, BundleValidationResult> validateAll(Locale baseLocale) {
        Objects.requireNonNull(baseLocale, "baseLocale must not be null");

        Set<Locale> supported = provider.getSupportedLocales();
        Map<Locale, BundleValidationResult> results = new LinkedHashMap<>();

        for (Locale locale : supported) {
            if (!locale.equals(baseLocale)) {
                results.put(locale, validate(baseLocale, locale));
            }
        }
        return Collections.unmodifiableMap(results);
    }

    /**
     * Returns whether the target locale bundle is complete relative to the base
     * 返回目标区域包相对于基准是否完整
     *
     * @param baseLocale   the reference locale | 参考区域
     * @param targetLocale the locale to check | 要检查的区域
     * @return true if no missing keys | 如果没有缺失键则为 true
     */
    public boolean isComplete(Locale baseLocale, Locale targetLocale) {
        return validate(baseLocale, targetLocale).isComplete();
    }

    /**
     * Returns the coverage ratio for the target locale relative to the base
     * 返回目标区域相对于基准的覆盖率
     *
     * @param baseLocale   the reference locale | 参考区域
     * @param targetLocale the locale to measure | 要测量的区域
     * @return coverage in [0.0, 1.0] | 覆盖率 [0.0, 1.0]
     */
    public double coverage(Locale baseLocale, Locale targetLocale) {
        return validate(baseLocale, targetLocale).coverage();
    }
}
