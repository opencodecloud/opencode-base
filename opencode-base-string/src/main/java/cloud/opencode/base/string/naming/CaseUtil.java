package cloud.opencode.base.string.naming;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Naming Case Conversion Utility
 * 命名风格转换工具类
 *
 * <p>Converts between different naming conventions (camelCase, snake_case, kebab-case, etc.).</p>
 * <p>在不同命名约定之间转换（驼峰、蛇形、短横线等）。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Convert between 9 naming styles - 9种命名风格互转</li>
 *   <li>Auto-detect source naming style - 自动检测源命名风格</li>
 *   <li>Preserve acronyms where possible - 尽可能保留缩写</li>
 *   <li>Database/Java naming conversion - 数据库/Java命名转换</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Convert to camelCase
 * String camel = CaseUtil.toCamelCase("get_user_name"); // "getUserName"
 *
 * // Convert to snake_case
 * String snake = CaseUtil.toSnakeCase("getUserName"); // "get_user_name"
 *
 * // Convert to kebab-case
 * String kebab = CaseUtil.toKebabCase("getUserName"); // "get-user-name"
 *
 * // Auto-detect and convert
 * String result = CaseUtil.convert("get_user_name", NamingCase.CAMEL_CASE);
 * // -> "getUserName"
 *
 * // Detect naming style
 * NamingCase style = CaseUtil.detect("getUserName"); // CAMEL_CASE
 * }</pre>
 *
 * <p><strong>Performance | 性能:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) - 时间复杂度: O(n)</li>
 *   <li>Space complexity: O(n) - 空间复杂度: O(n)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
public final class CaseUtil {

    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

    private CaseUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Convert to camelCase.
     * 转换为小驼峰命名。
     *
     * @param name the name to convert | 要转换的名称
     * @return camelCase string | 小驼峰字符串
     */
    public static String toCamelCase(String name) {
        return convert(name, NamingCase.CAMEL_CASE);
    }

    /**
     * Convert to PascalCase.
     * 转换为大驼峰命名。
     *
     * @param name the name to convert | 要转换的名称
     * @return PascalCase string | 大驼峰字符串
     */
    public static String toPascalCase(String name) {
        return convert(name, NamingCase.PASCAL_CASE);
    }

    /**
     * Convert to snake_case.
     * 转换为蛇形命名。
     *
     * @param name the name to convert | 要转换的名称
     * @return snake_case string | 蛇形字符串
     */
    public static String toSnakeCase(String name) {
        return convert(name, NamingCase.SNAKE_CASE);
    }

    /**
     * Convert to UPPER_SNAKE_CASE.
     * 转换为大写蛇形命名。
     *
     * @param name the name to convert | 要转换的名称
     * @return UPPER_SNAKE_CASE string | 大写蛇形字符串
     */
    public static String toUpperSnakeCase(String name) {
        return convert(name, NamingCase.UPPER_SNAKE_CASE);
    }

    /**
     * Convert to kebab-case.
     * 转换为短横线命名。
     *
     * @param name the name to convert | 要转换的名称
     * @return kebab-case string | 短横线字符串
     */
    public static String toKebabCase(String name) {
        return convert(name, NamingCase.KEBAB_CASE);
    }

    /**
     * Convert to dot.case.
     * 转换为点分隔命名。
     *
     * @param name the name to convert | 要转换的名称
     * @return dot.case string | 点分隔字符串
     */
    public static String toDotCase(String name) {
        return convert(name, NamingCase.DOT_CASE);
    }

    /**
     * Convert to path/case.
     * 转换为路径命名。
     *
     * @param name the name to convert | 要转换的名称
     * @return path/case string | 路径字符串
     */
    public static String toPathCase(String name) {
        return convert(name, NamingCase.PATH_CASE);
    }

    /**
     * Convert to Title Case.
     * 转换为标题形式。
     *
     * @param name the name to convert | 要转换的名称
     * @return Title Case string | 标题字符串
     */
    public static String toTitleCase(String name) {
        return convert(name, NamingCase.TITLE_CASE);
    }

    /**
     * Convert to Sentence case.
     * 转换为句子形式。
     *
     * @param name the name to convert | 要转换的名称
     * @return Sentence case string | 句子字符串
     */
    public static String toSentenceCase(String name) {
        return convert(name, NamingCase.SENTENCE_CASE);
    }

    /**
     * Convert to specified naming case.
     * 转换为指定的命名风格。
     *
     * @param name       the name to convert | 要转换的名称
     * @param targetCase the target naming case | 目标命名风格
     * @return converted string | 转换后的字符串
     */
    public static String convert(String name, NamingCase targetCase) {
        if (name == null || name.isEmpty()) {
            return name;
        }

        // Split into words
        String[] words = WordUtil.splitWords(name);
        if (words.length == 0) {
            return name;
        }

        // Apply target case formatting
        return formatWords(words, targetCase);
    }

    /**
     * Convert from one naming case to another.
     * 从一种命名风格转换为另一种。
     *
     * @param name       the name to convert | 要转换的名称
     * @param sourceCase the source naming case | 源命名风格
     * @param targetCase the target naming case | 目标命名风格
     * @return converted string | 转换后的字符串
     */
    public static String convert(String name, NamingCase sourceCase, NamingCase targetCase) {
        // For now, we just use the general convert method
        // The source case is mainly for documentation/clarity
        return convert(name, targetCase);
    }

    /**
     * Detect the naming case of a string.
     * 检测字符串的命名风格。
     *
     * @param name the name to detect | 要检测的名称
     * @return detected naming case | 检测到的命名风格
     */
    public static NamingCase detect(String name) {
        if (name == null || name.isEmpty()) {
            return NamingCase.CAMEL_CASE; // Default
        }

        boolean hasUnderscore = name.contains("_");
        boolean hasHyphen = name.contains("-");
        boolean hasDot = name.contains(".");
        boolean hasSlash = name.contains("/");
        boolean hasSpace = name.contains(" ");
        boolean hasLower = name.chars().anyMatch(Character::isLowerCase);
        boolean hasUpper = name.chars().anyMatch(Character::isUpperCase);
        boolean startsWithUpper = Character.isUpperCase(name.charAt(0));

        // Check separator-based cases
        if (hasUnderscore) {
            return hasLower && !hasUpper ? NamingCase.SNAKE_CASE : NamingCase.UPPER_SNAKE_CASE;
        }
        if (hasHyphen) {
            return NamingCase.KEBAB_CASE;
        }
        if (hasDot) {
            return NamingCase.DOT_CASE;
        }
        if (hasSlash) {
            return NamingCase.PATH_CASE;
        }
        if (hasSpace) {
            // Check if all words start with uppercase
            String[] words = WHITESPACE_PATTERN.split(name);
            boolean allWordsCapitalized = Arrays.stream(words)
                .filter(w -> !w.isEmpty())
                .allMatch(w -> Character.isUpperCase(w.charAt(0)));
            return allWordsCapitalized ? NamingCase.TITLE_CASE : NamingCase.SENTENCE_CASE;
        }

        // Check camelCase variants
        if (hasLower && hasUpper) {
            return startsWithUpper ? NamingCase.PASCAL_CASE : NamingCase.CAMEL_CASE;
        }

        // Default
        return startsWithUpper ? NamingCase.PASCAL_CASE : NamingCase.CAMEL_CASE;
    }

    /**
     * Format words according to naming case.
     * 根据命名风格格式化单词。
     *
     * @param words      the words | 单词数组
     * @param namingCase the naming case | 命名风格
     * @return formatted string | 格式化后的字符串
     */
    private static String formatWords(String[] words, NamingCase namingCase) {
        String[] formattedWords = new String[words.length];

        for (int i = 0; i < words.length; i++) {
            String word = words[i].toLowerCase();

            switch (namingCase) {
                case CAMEL_CASE:
                    formattedWords[i] = i == 0 ? word : WordUtil.capitalizeWord(word);
                    break;

                case PASCAL_CASE:
                    formattedWords[i] = WordUtil.capitalizeWord(word);
                    break;

                case SNAKE_CASE:
                case KEBAB_CASE:
                case DOT_CASE:
                case PATH_CASE:
                    formattedWords[i] = word;
                    break;

                case UPPER_SNAKE_CASE:
                    formattedWords[i] = word.toUpperCase();
                    break;

                case TITLE_CASE:
                    formattedWords[i] = WordUtil.capitalizeWord(word);
                    break;

                case SENTENCE_CASE:
                    formattedWords[i] = i == 0 ? WordUtil.capitalizeWord(word) : word;
                    break;

                default:
                    formattedWords[i] = word;
            }
        }

        return WordUtil.joinWords(formattedWords, namingCase.getSeparator());
    }
}
