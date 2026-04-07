package cloud.opencode.base.i18n.plural;

/**
 * CLDR plural category enumeration
 * CLDR 复数类别枚举
 *
 * <p>Represents the six plural categories defined by the Unicode CLDR specification.
 * Each language maps numeric values to one of these categories according to its
 * pluralization rules.</p>
 * <p>表示 Unicode CLDR 规范定义的六种复数类别。
 * 每种语言根据其复数规则将数值映射到这些类别之一。</p>
 *
 * <p><strong>Categories | 类别:</strong></p>
 * <ul>
 *   <li>{@link #ZERO} - Zero form (e.g., Arabic, Welsh) - 零数形式</li>
 *   <li>{@link #ONE} - Singular form - 单数形式</li>
 *   <li>{@link #TWO} - Dual form (e.g., Arabic, Welsh) - 双数形式</li>
 *   <li>{@link #FEW} - Paucal/few form (e.g., Slavic languages) - 少数形式</li>
 *   <li>{@link #MANY} - Many form (e.g., Slavic languages, Arabic) - 多数形式</li>
 *   <li>{@link #OTHER} - General/default form - 通用/默认形式</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * PluralCategory cat = PluralCategory.ONE;
 * String keyword = cat.keyword(); // "one"
 * PluralCategory parsed = PluralCategory.fromKeyword("few"); // FEW
 * }</pre>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-i18n V1.0.3
 */
public enum PluralCategory {

    /**
     * Zero form - used by Arabic, Welsh, Latvian, etc.
     * 零数形式 - 用于阿拉伯语、威尔士语、拉脱维亚语等
     */
    ZERO("zero"),

    /**
     * Singular form - used by most languages
     * 单数形式 - 大多数语言使用
     */
    ONE("one"),

    /**
     * Dual form - used by Arabic, Welsh, Irish, etc.
     * 双数形式 - 用于阿拉伯语、威尔士语、爱尔兰语等
     */
    TWO("two"),

    /**
     * Paucal/few form - used by Slavic languages, Arabic, Romanian, etc.
     * 少数形式 - 用于斯拉夫语、阿拉伯语、罗马尼亚语等
     */
    FEW("few"),

    /**
     * Many form - used by Slavic languages, Arabic, etc.
     * 多数形式 - 用于斯拉夫语、阿拉伯语等
     */
    MANY("many"),

    /**
     * General/default form - used by all languages as fallback
     * 通用/默认形式 - 所有语言都使用的兜底形式
     */
    OTHER("other");

    private final String keyword;

    PluralCategory(String keyword) {
        this.keyword = keyword;
    }

    /**
     * Returns the CLDR keyword for this category
     * 返回此类别的 CLDR 关键字
     *
     * @return lowercase keyword (e.g., "zero", "one", "two", "few", "many", "other")
     *         小写关键字
     */
    public String keyword() {
        return keyword;
    }

    /**
     * Parses a CLDR keyword into a PluralCategory
     * 将 CLDR 关键字解析为 PluralCategory
     *
     * @param keyword the keyword to parse (case-insensitive) | 要解析的关键字（不区分大小写）
     * @return the matching PluralCategory | 匹配的复数类别
     * @throws IllegalArgumentException if the keyword is unknown | 如果关键字未知
     * @throws NullPointerException     if keyword is null | 如果关键字为null
     */
    public static PluralCategory fromKeyword(String keyword) {
        if (keyword == null) {
            throw new NullPointerException("Plural keyword must not be null");
        }
        String lower = keyword.strip().toLowerCase(java.util.Locale.ROOT);
        for (PluralCategory cat : values()) {
            if (cat.keyword.equals(lower)) {
                return cat;
            }
        }
        throw new IllegalArgumentException("Unknown plural keyword: '" + keyword + "'");
    }
}
