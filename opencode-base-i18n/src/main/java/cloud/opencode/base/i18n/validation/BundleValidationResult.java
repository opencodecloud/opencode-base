package cloud.opencode.base.i18n.validation;

import java.util.Locale;
import java.util.Set;

/**
 * Result of comparing message bundle keys between a base and target locale
 * 比较基准区域和目标区域消息包键的结果
 *
 * <p>Contains the set of missing keys (in base but not target), extra keys
 * (in target but not base), and a coverage percentage.</p>
 * <p>包含缺失键集合（在基准中但不在目标中）、多余键集合（在目标中但不在基准中）和覆盖率百分比。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Missing key detection - 缺失键检测</li>
 *   <li>Extra key detection - 多余键检测</li>
 *   <li>Coverage calculation - 覆盖率计算</li>
 *   <li>Completeness check - 完整性检查</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * BundleValidationResult result = validator.validate(Locale.ENGLISH, Locale.FRENCH);
 * if (!result.isComplete()) {
 *     log.warn("FR bundle missing {} keys: {}", result.missingKeys().size(), result.missingKeys());
 * }
 * System.out.printf("Coverage: %.1f%%\n", result.coverage() * 100);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 * </ul>
 *
 * @param baseLocale     the locale used as the reference | 用作参考的区域
 * @param targetLocale   the locale being validated | 被验证的区域
 * @param missingKeys    keys in base but not in target | 在基准中但不在目标中的键
 * @param extraKeys      keys in target but not in base | 在目标中但不在基准中的键
 * @param baseKeyCount   total keys in base bundle | 基准包中的总键数
 * @param targetKeyCount total keys in target bundle | 目标包中的总键数
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-i18n V1.0.3
 */
public record BundleValidationResult(
        Locale baseLocale,
        Locale targetLocale,
        Set<String> missingKeys,
        Set<String> extraKeys,
        int baseKeyCount,
        int targetKeyCount
) {

    /**
     * Validates and creates a defensive copy of collections
     * 验证并创建集合的防御性副本
     */
    public BundleValidationResult {
        java.util.Objects.requireNonNull(baseLocale,   "baseLocale must not be null");
        java.util.Objects.requireNonNull(targetLocale, "targetLocale must not be null");
        missingKeys = missingKeys != null ? Set.copyOf(missingKeys) : Set.of();
        extraKeys   = extraKeys   != null ? Set.copyOf(extraKeys)   : Set.of();
    }

    /**
     * Calculates the coverage ratio: 1.0 - (missing / base)
     * 计算覆盖率：1.0 - （缺失数 / 基准数）
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * baseKeyCount=10, missingKeys=2 → coverage() = 0.8
     * baseKeyCount=0                 → coverage() = 1.0
     * missingKeys=0                  → coverage() = 1.0
     * </pre>
     *
     * @return coverage ratio in [0.0, 1.0] | 覆盖率 [0.0, 1.0]
     */
    public double coverage() {
        if (baseKeyCount == 0) return 1.0;
        return (double) (baseKeyCount - missingKeys.size()) / baseKeyCount;
    }

    /**
     * Returns true if the target bundle contains all keys from the base bundle
     * 如果目标包包含基准包中的所有键则返回 true
     *
     * @return true if no missing keys | 如果没有缺失键则为 true
     */
    public boolean isComplete() {
        return missingKeys.isEmpty();
    }

    /**
     * Returns a human-readable summary of the validation result
     * 返回验证结果的可读摘要
     *
     * @return summary string | 摘要字符串
     */
    public String summary() {
        return String.format("[%s → %s] coverage=%.1f%% missing=%d extra=%d",
                baseLocale, targetLocale, coverage() * 100,
                missingKeys.size(), extraKeys.size());
    }
}
