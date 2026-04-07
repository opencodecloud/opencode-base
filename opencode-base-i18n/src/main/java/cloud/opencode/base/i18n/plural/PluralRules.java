package cloud.opencode.base.i18n.plural;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongUnaryOperator;

/**
 * CLDR plural rules engine for major world languages
 * 主要世界语言的 CLDR 复数规则引擎
 *
 * <p>Provides CLDR-compliant plural category selection for over 50 language families,
 * covering the major plural patterns used in world languages without requiring ICU4J.</p>
 * <p>为 50 多个语言族提供符合 CLDR 规范的复数类别选择，涵盖世界语言中使用的主要复数模式，
 * 无需 ICU4J 依赖。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>CLDR plural rules for 50+ language families - 50+ 语言族的 CLDR 复数规则</li>
 *   <li>Cached instances per language - 每种语言的缓存实例</li>
 *   <li>Integer and decimal number support - 整数和小数支持</li>
 *   <li>Thread-safe - 线程安全</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * PluralRules en = PluralRules.forLocale(Locale.ENGLISH);
 * en.select(1L);   // ONE
 * en.select(2L);   // OTHER
 *
 * PluralRules ru = PluralRules.forLocale(Locale.of("ru"));
 * ru.select(1L);   // ONE
 * ru.select(2L);   // FEW
 * ru.select(5L);   // MANY
 * ru.select(21L);  // ONE
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) per select call - 时间复杂度: 每次调用 O(1)</li>
 *   <li>Space complexity: O(L) for L cached languages - 空间复杂度: O(L) 缓存语言数</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: locale null uses default rules - 空值安全: locale 为 null 使用默认规则</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-i18n V1.0.3
 */
public final class PluralRules {

    // Category int codes (avoids enum allocation in hot path) | 类别整数编码（避免热路径枚举分配）
    private static final int CAT_ZERO  = 0;
    private static final int CAT_ONE   = 1;
    private static final int CAT_TWO   = 2;
    private static final int CAT_FEW   = 3;
    private static final int CAT_MANY  = 5;
    // OTHER = all other values

    /** Cache of PluralRules instances keyed by language code | 按语言代码缓存的实例 */
    private static final Map<String, PluralRules> CACHE = new ConcurrentHashMap<>();

    /** The rule function: given abs(n), returns the category code | 规则函数 */
    private final LongUnaryOperator rule;

    private PluralRules(LongUnaryOperator rule) {
        this.rule = rule;
    }

    // ==================== Factory | 工厂方法 ====================

    /**
     * Returns plural rules for the given locale
     * 返回给定区域的复数规则
     *
     * <p>Falls back from language+region to language only. Returns English-like rules
     * (n=1 → ONE, else OTHER) when no specific rules are found.</p>
     * <p>从语言+地区回退到仅语言。找不到特定规则时返回英语风格规则。</p>
     *
     * @param locale the locale | 区域（null 使用默认规则）
     * @return plural rules | 复数规则
     */
    public static PluralRules forLocale(Locale locale) {
        if (locale == null) {
            return CACHE.computeIfAbsent("en", PluralRules::buildRules);
        }
        String language = locale.getLanguage().toLowerCase(Locale.ROOT);
        if (language.isEmpty()) {
            return CACHE.computeIfAbsent("en", PluralRules::buildRules);
        }
        return CACHE.computeIfAbsent(language, PluralRules::buildRules);
    }

    // ==================== Public API | 公开方法 ====================

    /**
     * Selects the plural category for an integer value
     * 为整数值选择复数类别
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * en.select(1L)  = ONE     // English singular
     * en.select(2L)  = OTHER   // English plural
     * ru.select(21L) = ONE     // Russian: 21 ends in 1
     * ar.select(0L)  = ZERO    // Arabic zero form
     * </pre>
     *
     * @param n the integer number | 整数（支持负数，取绝对值）
     * @return plural category | 复数类别
     */
    public PluralCategory select(long n) {
        long abs = Math.abs(n);
        int cat = (int) rule.applyAsLong(abs);
        return switch (cat) {
            case CAT_ZERO -> PluralCategory.ZERO;
            case CAT_ONE  -> PluralCategory.ONE;
            case CAT_TWO  -> PluralCategory.TWO;
            case CAT_FEW  -> PluralCategory.FEW;
            case CAT_MANY -> PluralCategory.MANY;
            default       -> PluralCategory.OTHER;
        };
    }

    /**
     * Selects the plural category for a decimal value
     * 为小数值选择复数类别
     *
     * <p>Non-integer decimals return OTHER for most languages.
     * When visibleDecimalDigits is 0, delegates to integer rules.</p>
     * <p>大多数语言中非整数小数返回 OTHER。visibleDecimalDigits 为 0 时委托整数规则。</p>
     *
     * @param n                    the decimal number | 小数
     * @param visibleDecimalDigits number of visible decimal digits | 可见小数位数
     * @return plural category | 复数类别
     */
    public PluralCategory select(double n, int visibleDecimalDigits) {
        if (visibleDecimalDigits > 0 && n != Math.floor(n)) {
            return PluralCategory.OTHER;
        }
        return select((long) Math.abs(n));
    }

    // ==================== Rule Builders | 规则构建器 ====================

    @SuppressWarnings("java:S1142")
    private static PluralRules buildRules(String lang) {
        return switch (lang) {
            // ── East Asian & others: always OTHER ────────────────────────
            case "zh", "ja", "ko", "vi", "th", "id", "ms", "my", "km", "lo",
                 "yo", "wo", "bo", "dz", "ii", "to", "sg", "bm" ->
                    new PluralRules(n -> 6);

            // ── French pattern: 0 and 1 → ONE ────────────────────────────
            case "fr", "ff", "kab", "hy" ->
                    new PluralRules(n -> n == 0 || n == 1 ? CAT_ONE : 6);

            // ── Latvian: zero/one/other ───────────────────────────────────
            case "lv" -> new PluralRules(n -> {
                long m100 = n % 100;
                long m10  = n % 10;
                if (m10 == 0 || (m100 >= 11 && m100 <= 19)) return CAT_ZERO;
                if (m10 == 1 && m100 != 11) return CAT_ONE;
                return 6;
            });

            // ── Lithuanian: one/few/other ─────────────────────────────────
            case "lt" -> new PluralRules(n -> {
                long m10  = n % 10;
                long m100 = n % 100;
                if (m10 == 1 && !(m100 >= 11 && m100 <= 19)) return CAT_ONE;
                if (m10 >= 2 && m10 <= 9 && !(m100 >= 11 && m100 <= 19)) return CAT_FEW;
                return 6;
            });

            // ── Russian, Ukrainian, Belarusian: one/few/many ──────────────
            case "ru", "uk", "be" -> new PluralRules(n -> {
                long m10  = n % 10;
                long m100 = n % 100;
                if (m10 == 1 && m100 != 11) return CAT_ONE;
                if (m10 >= 2 && m10 <= 4 && !(m100 >= 12 && m100 <= 14)) return CAT_FEW;
                return CAT_MANY;
            });

            // ── Polish: one/few/many ──────────────────────────────────────
            case "pl" -> new PluralRules(n -> {
                long m10  = n % 10;
                long m100 = n % 100;
                if (n == 1) return CAT_ONE;
                if (m10 >= 2 && m10 <= 4 && !(m100 >= 12 && m100 <= 14)) return CAT_FEW;
                return CAT_MANY;
            });

            // ── Croatian, Serbian, Bosnian: one/few/other ─────────────────
            case "hr", "sr", "bs", "sh" -> new PluralRules(n -> {
                long m10  = n % 10;
                long m100 = n % 100;
                if (m10 == 1 && m100 != 11) return CAT_ONE;
                if (m10 >= 2 && m10 <= 4 && !(m100 >= 12 && m100 <= 14)) return CAT_FEW;
                return 6;
            });

            // ── Czech, Slovak: one/few/other ──────────────────────────────
            case "cs", "sk" -> new PluralRules(n -> {
                if (n == 1) return CAT_ONE;
                if (n >= 2 && n <= 4) return CAT_FEW;
                return 6;
            });

            // ── Slovenian: one/two/few/other (by mod 100) ─────────────────
            case "sl" -> new PluralRules(n -> {
                long m100 = n % 100;
                if (m100 == 1) return CAT_ONE;
                if (m100 == 2) return CAT_TWO;
                if (m100 == 3 || m100 == 4) return CAT_FEW;
                return 6;
            });

            // ── Romanian: one/few/other ───────────────────────────────────
            case "ro", "mo" -> new PluralRules(n -> {
                long m100 = n % 100;
                if (n == 1) return CAT_ONE;
                if (n == 0 || (m100 >= 1 && m100 <= 19)) return CAT_FEW;
                return 6;
            });

            // ── Arabic: zero/one/two/few/many/other ───────────────────────
            case "ar", "ars" -> new PluralRules(n -> {
                long m100 = n % 100;
                if (n == 0) return CAT_ZERO;
                if (n == 1) return CAT_ONE;
                if (n == 2) return CAT_TWO;
                if (m100 >= 3 && m100 <= 10) return CAT_FEW;
                if (m100 >= 11 && m100 <= 99) return CAT_MANY;
                return 6;
            });

            // ── Welsh: zero/one/two/few/many/other ────────────────────────
            case "cy" -> new PluralRules(n -> {
                if (n == 0) return CAT_ZERO;
                if (n == 1) return CAT_ONE;
                if (n == 2) return CAT_TWO;
                if (n == 3) return CAT_FEW;
                if (n == 6) return CAT_MANY;
                return 6;
            });

            // ── Irish: one/two/few/many/other ─────────────────────────────
            case "ga" -> new PluralRules(n -> {
                if (n == 1) return CAT_ONE;
                if (n == 2) return CAT_TWO;
                if (n >= 3 && n <= 6) return CAT_FEW;
                if (n >= 7 && n <= 10) return CAT_MANY;
                return 6;
            });

            // ── Macedonian: one by last digit ─────────────────────────────
            case "mk" -> new PluralRules(n -> n % 10 == 1 ? CAT_ONE : 6);

            // ── Maltese: one/few/many/other ───────────────────────────────
            case "mt" -> new PluralRules(n -> {
                long m100 = n % 100;
                if (n == 1) return CAT_ONE;
                if (n == 0 || (m100 >= 2 && m100 <= 10)) return CAT_FEW;
                if (m100 >= 11 && m100 <= 19) return CAT_MANY;
                return 6;
            });

            // ── Hebrew: one/two/many/other ────────────────────────────────
            case "he", "iw" -> new PluralRules(n -> {
                if (n == 1) return CAT_ONE;
                if (n == 2) return CAT_TWO;
                if (n >= 11 && n % 10 == 0) return CAT_MANY;
                return 6;
            });

            // ── Default: n=1 → ONE, else OTHER ───────────────────────────
            default -> new PluralRules(n -> n == 1 ? CAT_ONE : 6);
        };
    }
}
