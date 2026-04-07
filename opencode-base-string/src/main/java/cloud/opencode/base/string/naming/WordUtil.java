package cloud.opencode.base.string.naming;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Word Splitting Utility
 * 单词分割工具类
 *
 * <p>Utility for splitting compound names into individual words.</p>
 * <p>用于将复合名称分割为单个单词的工具类。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Split camelCase and PascalCase - 分割驼峰命名</li>
 *   <li>Split snake_case and kebab-case - 分割蛇形和短横线命名</li>
 *   <li>Handle mixed naming styles - 处理混合命名风格</li>
 *   <li>Preserve acronyms - 保留缩写词</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Split camelCase
 * String[] words = WordUtil.splitWords("getUserName");
 * // -> ["get", "User", "Name"]
 *
 * // Split snake_case
 * String[] words = WordUtil.splitWords("get_user_name");
 * // -> ["get", "user", "name"]
 *
 * // Split with acronyms
 * String[] words = WordUtil.splitWords("parseHTMLDocument");
 * // -> ["parse", "HTML", "Document"]
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
public final class WordUtil {

    private static final Pattern SEPARATOR_PATTERN = Pattern.compile("[_\\-./\\s]+");

    private WordUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Split a compound name into words.
     * 将复合名称分割为单词。
     *
     * <p>Handles multiple naming conventions:</p>
     * <p>处理多种命名约定：</p>
     * <ul>
     *   <li>camelCase / PascalCase</li>
     *   <li>snake_case / UPPER_SNAKE_CASE</li>
     *   <li>kebab-case</li>
     *   <li>dot.case</li>
     *   <li>path/case</li>
     *   <li>Space separated</li>
     * </ul>
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * splitWords("getUserName")     = ["get", "User", "Name"]
     * splitWords("get_user_name")   = ["get", "user", "name"]
     * splitWords("get-user-name")   = ["get", "user", "name"]
     * splitWords("XMLParser")       = ["XML", "Parser"]
     * splitWords("parseHTMLDoc")    = ["parse", "HTML", "Doc"]
     * </pre>
     *
     * @param name the compound name | 复合名称
     * @return array of words | 单词数组
     */
    public static String[] splitWords(String name) {
        if (name == null || name.isEmpty()) {
            return new String[0];
        }

        List<String> words = new ArrayList<>();

        // If contains common separators, split by them first
        if (name.contains("_") || name.contains("-") || name.contains(".") ||
            name.contains("/") || name.contains(" ")) {
            String[] parts = SEPARATOR_PATTERN.split(name);
            for (String part : parts) {
                if (!part.isEmpty()) {
                    // Recursively split camelCase parts
                    if (isCamelCase(part)) {
                        String[] subWords = splitCamelCase(part);
                        for (String word : subWords) {
                            if (!word.isEmpty()) {
                                words.add(word);
                            }
                        }
                    } else {
                        words.add(part);
                    }
                }
            }
        } else {
            // Pure camelCase or PascalCase
            String[] camelWords = splitCamelCase(name);
            for (String word : camelWords) {
                if (!word.isEmpty()) {
                    words.add(word);
                }
            }
        }

        return words.toArray(new String[0]);
    }

    /**
     * Split camelCase or PascalCase into words.
     * 将驼峰命名分割为单词。
     *
     * @param name camelCase or PascalCase string | 驼峰命名字符串
     * @return array of words | 单词数组
     */
    private static String[] splitCamelCase(String name) {
        List<String> words = new ArrayList<>();
        StringBuilder currentWord = new StringBuilder();

        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);

            if (i == 0) {
                currentWord.append(c);
            } else {
                char prev = name.charAt(i - 1);

                // Check for transition points
                if (Character.isUpperCase(c)) {
                    // Check if it's start of acronym or new word
                    if (Character.isLowerCase(prev)) {
                        // Transition from lower to upper -> new word
                        words.add(currentWord.toString());
                        currentWord = new StringBuilder();
                        currentWord.append(c);
                    } else if (i + 1 < name.length() && Character.isLowerCase(name.charAt(i + 1))) {
                        // Last letter of acronym (e.g., XMLParser: L before P)
                        words.add(currentWord.toString());
                        currentWord = new StringBuilder();
                        currentWord.append(c);
                    } else {
                        // Continue acronym
                        currentWord.append(c);
                    }
                } else {
                    currentWord.append(c);
                }
            }
        }

        if (currentWord.length() > 0) {
            words.add(currentWord.toString());
        }

        return words.toArray(new String[0]);
    }

    /**
     * Check if a string is in camelCase or PascalCase.
     * 检查字符串是否为驼峰命名。
     *
     * @param str the string to check | 要检查的字符串
     * @return true if camelCase or PascalCase | 如果是驼峰命名则返回true
     */
    private static boolean isCamelCase(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        // charAt() loop avoids the char[] allocation of toCharArray().
        // charAt() 循环避免 toCharArray() 的数组分配。
        boolean hasLower = false;
        boolean hasUpper = false;

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isUpperCase(c)) hasUpper = true;
            if (hasLower && hasUpper) return true; // early exit
        }

        return false;
    }

    /**
     * Join words with a separator.
     * 使用分隔符连接单词。
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * joinWords(["get", "user", "name"], "_") = "get_user_name"
     * joinWords(["get", "user", "name"], "-") = "get-user-name"
     * </pre>
     *
     * @param words     the words to join | 要连接的单词
     * @param separator the separator | 分隔符
     * @return joined string | 连接后的字符串
     */
    public static String joinWords(String[] words, String separator) {
        if (words == null || words.length == 0) {
            return "";
        }

        return String.join(separator, words);
    }

    /**
     * Normalize a word (lowercase and trim).
     * 规范化单词（小写并去除空格）。
     *
     * @param word the word to normalize | 要规范化的单词
     * @return normalized word | 规范化后的单词
     */
    public static String normalizeWord(String word) {
        if (word == null) {
            return "";
        }
        return word.trim().toLowerCase();
    }

    /**
     * Capitalize first letter of a word.
     * 将单词首字母大写。
     *
     * @param word the word | 单词
     * @return capitalized word | 首字母大写的单词
     */
    public static String capitalizeWord(String word) {
        if (word == null || word.isEmpty()) {
            return word;
        }
        return Character.toUpperCase(word.charAt(0)) + word.substring(1);
    }

    /**
     * Uncapitalize first letter of a word.
     * 将单词首字母小写。
     *
     * @param word the word | 单词
     * @return uncapitalized word | 首字母小写的单词
     */
    public static String uncapitalizeWord(String word) {
        if (word == null || word.isEmpty()) {
            return word;
        }
        return Character.toLowerCase(word.charAt(0)) + word.substring(1);
    }
}
