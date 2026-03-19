package cloud.opencode.base.string.naming;

/**
 * Naming Convention Enumeration
 * 命名风格枚举
 *
 * <p>Defines various naming conventions for string conversion.</p>
 * <p>定义字符串转换的各种命名约定。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Common naming styles - 常用命名风格</li>
 *   <li>Style detection and conversion - 风格检测和转换</li>
 *   <li>Separator configuration - 分隔符配置</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Get separator
 * String sep = NamingCase.SNAKE_CASE.getSeparator(); // "_"
 *
 * // Check capitalization
 * boolean cap = NamingCase.PASCAL_CASE.isCapitalized(); // true
 * }</pre>
 *
 * <p><strong>Supported Styles | 支持的风格:</strong></p>
 * <ul>
 *   <li>CAMEL_CASE: camelCase - 小驼峰</li>
 *   <li>PASCAL_CASE: PascalCase - 大驼峰/帕斯卡</li>
 *   <li>SNAKE_CASE: snake_case - 蛇形</li>
 *   <li>UPPER_SNAKE_CASE: UPPER_SNAKE_CASE - 大写蛇形/常量</li>
 *   <li>KEBAB_CASE: kebab-case - 短横线</li>
 *   <li>DOT_CASE: dot.case - 点分隔</li>
 *   <li>PATH_CASE: path/case - 路径</li>
 *   <li>TITLE_CASE: Title Case - 标题</li>
 *   <li>SENTENCE_CASE: Sentence case - 句子</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (enum is immutable) - 线程安全: 是（枚举不可变）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
public enum NamingCase {

    /** 小驼峰（camelCase） */
    CAMEL_CASE("", true, false, false),

    /** 大驼峰/帕斯卡（PascalCase） */
    PASCAL_CASE("", true, true, false),

    /** 蛇形（snake_case） */
    SNAKE_CASE("_", false, false, false),

    /** 大写蛇形/常量（UPPER_SNAKE_CASE） */
    UPPER_SNAKE_CASE("_", false, false, true),

    /** 短横线（kebab-case） */
    KEBAB_CASE("-", false, false, false),

    /** 点分隔（dot.case） */
    DOT_CASE(".", false, false, false),

    /** 路径（path/case） */
    PATH_CASE("/", false, false, false),

    /** 标题（Title Case） - 每个单词首字母大写，空格分隔 */
    TITLE_CASE(" ", true, true, false),

    /** 句子（Sentence case） - 首词首字母大写，其余小写，空格分隔 */
    SENTENCE_CASE(" ", false, true, false);

    private final String separator;
    private final boolean capitalizeWords;
    private final boolean capitalizeFirst;
    private final boolean upperCase;

    NamingCase(String separator, boolean capitalizeWords, boolean capitalizeFirst, boolean upperCase) {
        this.separator = separator;
        this.capitalizeWords = capitalizeWords;
        this.capitalizeFirst = capitalizeFirst;
        this.upperCase = upperCase;
    }

    /**
     * Get the separator for this naming style.
     * 获取此命名风格的分隔符。
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * SNAKE_CASE.getSeparator() = "_"
     * KEBAB_CASE.getSeparator() = "-"
     * CAMEL_CASE.getSeparator() = ""
     * </pre>
     *
     * @return separator string | 分隔符字符串
     */
    public String getSeparator() {
        return separator;
    }

    /**
     * Check if first letter should be capitalized.
     * 检查首字母是否应该大写。
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * PASCAL_CASE.isCapitalized() = true
     * CAMEL_CASE.isCapitalized() = false
     * TITLE_CASE.isCapitalized() = true
     * </pre>
     *
     * @return true if first letter is capitalized | 如果首字母大写则返回true
     */
    public boolean isCapitalized() {
        return capitalizeFirst;
    }

    /**
     * Check if all words should be capitalized.
     * 检查是否所有单词都应该大写。
     *
     * @return true if words are capitalized | 如果单词大写则返回true
     */
    public boolean isCapitalizeWords() {
        return capitalizeWords;
    }

    /**
     * Check if the style uses uppercase.
     * 检查风格是否使用全大写。
     *
     * @return true if uppercase | 如果全大写则返回true
     */
    public boolean isUpperCase() {
        return upperCase;
    }

    /**
     * Check if the style has a separator.
     * 检查风格是否有分隔符。
     *
     * @return true if has separator | 如果有分隔符则返回true
     */
    public boolean hasSeparator() {
        return !separator.isEmpty();
    }
}
