package cloud.opencode.base.string;

import java.nio.charset.Charset;
import java.text.Normalizer;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

/**
 * Enhanced String Utility Facade
 * 字符串增强工具门面类
 *
 * <p>Provides comprehensive string operations including padding, truncation, transformation, etc.</p>
 * <p>提供全面的字符串操作，包括填充、截断、转换等。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Padding and truncation - 填充和截断</li>
 *   <li>Case conversion - 大小写转换</li>
 *   <li>String manipulation - 字符串操作</li>
 *   <li>Substring extraction - 子串提取</li>
 *   <li>Validation and checking - 验证和检查</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Padding
 * String padded = OpenString.padLeft("42", 5, '0'); // "00042"
 *
 * // Truncation
 * String truncated = OpenString.truncate("Hello World", 8); // "Hello..."
 *
 * // Substring extraction
 * String between = OpenString.substringBetween("[hello]", "[", "]"); // "hello"
 *
 * // Statistics
 * int count = OpenString.countMatches("banana", "an"); // 2
 * }</pre>
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
public final class OpenString {

    private static final Pattern NUMERIC_PATTERN = Pattern.compile("\\d+");
    private static final Pattern ALPHA_PATTERN = Pattern.compile("[a-zA-Z]+");
    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile("[a-zA-Z0-9]+");
    private static final Pattern CONTAINS_CHINESE_PATTERN = Pattern.compile(".*[\\u4e00-\\u9fa5]+.*");
    private static final Pattern ALL_CHINESE_PATTERN = Pattern.compile("[\\u4e00-\\u9fa5]+");

    // Precompiled patterns for cleaning methods
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
    private static final Pattern INVISIBLE_CHARS_PATTERN = Pattern.compile("\\p{C}");
    private static final Pattern SPECIAL_CHARS_PATTERN = Pattern.compile("[^a-zA-Z0-9\\s]");
    private static final Pattern NON_DIGIT_PATTERN = Pattern.compile("[^0-9]");
    private static final Pattern NON_LETTER_PATTERN = Pattern.compile("[^a-zA-Z]");
    private static final Pattern NON_ALPHANUMERIC_PATTERN = Pattern.compile("[^a-zA-Z0-9]");
    private static final Pattern NON_CHINESE_PATTERN = Pattern.compile("[^\\u4e00-\\u9fa5]");
    private static final Pattern DIACRITICAL_MARKS_PATTERN = Pattern.compile("\\p{M}");

    private OpenString() {
        throw new UnsupportedOperationException("Utility class");
    }

    // ==================== Padding | 填充 ====================

    /**
     * Pad string on the left to reach minimum length.
     * 在字符串左侧填充以达到最小长度。
     *
     * @param str       the input string, may be {@code null} (returns {@code null} if so)
     * @param minLength the minimum desired length; must not be negative
     * @param padChar   the character to pad with
     * @return the padded string, or {@code null} if input was {@code null}
     * @throws IllegalArgumentException if minLength is negative
     */
    public static String padLeft(String str, int minLength, char padChar) {
        if (minLength < 0) {
            throw new IllegalArgumentException("minLength must not be negative: " + minLength);
        }
        if (str == null || str.length() >= minLength) return str;
        return String.valueOf(padChar).repeat(minLength - str.length()) + str;
    }

    /**
     * Pad string on the right to reach minimum length.
     * 在字符串右侧填充以达到最小长度。
     *
     * @param str       the input string, may be {@code null} (returns {@code null} if so)
     * @param minLength the minimum desired length; must not be negative
     * @param padChar   the character to pad with
     * @return the padded string, or {@code null} if input was {@code null}
     * @throws IllegalArgumentException if minLength is negative
     */
    public static String padRight(String str, int minLength, char padChar) {
        if (minLength < 0) {
            throw new IllegalArgumentException("minLength must not be negative: " + minLength);
        }
        if (str == null || str.length() >= minLength) return str;
        return str + String.valueOf(padChar).repeat(minLength - str.length());
    }

    /**
     * Center string within the given minimum length using the pad character.
     * 使用填充字符将字符串居中至给定的最小长度。
     *
     * @param str       the input string, may be {@code null} (returns {@code null} if so)
     * @param minLength the minimum desired length; must not be negative
     * @param padChar   the character to pad with
     * @return the centered string, or {@code null} if input was {@code null}
     * @throws IllegalArgumentException if minLength is negative
     */
    public static String center(String str, int minLength, char padChar) {
        if (minLength < 0) {
            throw new IllegalArgumentException("minLength must not be negative: " + minLength);
        }
        if (str == null || str.length() >= minLength) return str;
        int padding = minLength - str.length();
        int left = padding / 2;
        int right = padding - left;
        return String.valueOf(padChar).repeat(left) + str + String.valueOf(padChar).repeat(right);
    }

    // ==================== Extraction | 提取 ====================

    /**
     * Get the leftmost {@code length} characters of a string.
     * 获取字符串最左侧的 {@code length} 个字符。
     *
     * @param str    the input string, may be {@code null} (returns {@code null} if so)
     * @param length the number of characters to extract; negative values return {@code null} input unchanged
     * @return the leftmost characters, or {@code null} if input was {@code null}
     */
    public static String left(String str, int length) {
        if (str == null || length < 0) return str;
        return str.length() <= length ? str : str.substring(0, length);
    }

    /**
     * Get the rightmost {@code length} characters of a string.
     * 获取字符串最右侧的 {@code length} 个字符。
     *
     * @param str    the input string, may be {@code null} (returns {@code null} if so)
     * @param length the number of characters to extract; negative values return {@code null} input unchanged
     * @return the rightmost characters, or {@code null} if input was {@code null}
     */
    public static String right(String str, int length) {
        if (str == null || length < 0) return str;
        return str.length() <= length ? str : str.substring(str.length() - length);
    }

    public static String mid(String str, int start, int end) {
        if (str == null || start < 0 || end > str.length() || start > end) return "";
        return str.substring(start, end);
    }

    // ==================== Truncation | 截断 ====================

    public static String truncate(String str, int maxLength) {
        return truncate(str, maxLength, "...");
    }

    public static String truncate(String str, int maxLength, String ellipsis) {
        if (str == null || str.length() <= maxLength) return str;
        if (maxLength < ellipsis.length()) return str.substring(0, maxLength);
        return str.substring(0, maxLength - ellipsis.length()) + ellipsis;
    }

    public static String truncateMiddle(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) return str;
        if (maxLength <= 3) return str.substring(0, maxLength);
        int keepLen = (maxLength - 3) / 2;
        int endKeep = maxLength - 3 - keepLen;
        return str.substring(0, keepLen) + "..." + str.substring(str.length() - endKeep);
    }

    public static String truncateByBytes(String str, int maxBytes, String charset) {
        if (str == null) return null;
        try {
            Charset cs = Charset.forName(charset);
            byte[] bytes = str.getBytes(cs);
            if (bytes.length <= maxBytes) return str;

            // Single-pass O(n) algorithm using UTF-8 encoding rules
            if (cs.name().equalsIgnoreCase("UTF-8") || cs.name().equalsIgnoreCase("UTF8")) {
                int byteCount = 0;
                int i = 0;
                while (i < str.length()) {
                    int cp = str.codePointAt(i);
                    int cpBytes;
                    if (cp <= 0x7F) {
                        cpBytes = 1;
                    } else if (cp <= 0x7FF) {
                        cpBytes = 2;
                    } else if (cp <= 0xFFFF) {
                        cpBytes = 3;
                    } else {
                        cpBytes = 4;
                    }
                    if (byteCount + cpBytes > maxBytes) {
                        return str.substring(0, i);
                    }
                    byteCount += cpBytes;
                    i += Character.charCount(cp);
                }
                return str;
            }

            // For non-UTF-8 charsets, use a forward scan with encoding
            int byteCount = 0;
            int i = 0;
            while (i < str.length()) {
                int cp = str.codePointAt(i);
                int charCount = Character.charCount(cp);
                String segment = str.substring(i, i + charCount);
                int segmentBytes = segment.getBytes(cs).length;
                if (byteCount + segmentBytes > maxBytes) {
                    return str.substring(0, i);
                }
                byteCount += segmentBytes;
                i += charCount;
            }
            return str;
        } catch (Exception e) {
            return str;
        }
    }

    // ==================== Case Conversion | 大小写转换 ====================

    public static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    public static String uncapitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }

    public static String swapCase(String str) {
        if (str == null) return null;
        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (Character.isUpperCase(c)) {
                chars[i] = Character.toLowerCase(c);
            } else if (Character.isLowerCase(c)) {
                chars[i] = Character.toUpperCase(c);
            }
        }
        return new String(chars);
    }

    public static String toTitleCase(String str) {
        if (str == null || str.isEmpty()) return str;
        String[] words = str.split("\\s+");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1).toLowerCase())
                      .append(" ");
            }
        }
        return result.toString().trim();
    }

    // ==================== Reverse and Shuffle | 反转和打乱 ====================

    public static String reverse(String str) {
        return str != null ? new StringBuilder(str).reverse().toString() : null;
    }

    public static String shuffle(String str) {
        return shuffle(str, ThreadLocalRandom.current());
    }

    public static String shuffle(String str, Random random) {
        if (str == null) return null;
        List<Character> chars = new ArrayList<>();
        for (char c : str.toCharArray()) chars.add(c);
        Collections.shuffle(chars, random);
        StringBuilder sb = new StringBuilder(chars.size());
        for (char c : chars) sb.append(c);
        return sb.toString();
    }

    // ==================== Statistics | 统计 ====================

    public static int countMatches(String str, String subStr) {
        if (str == null || subStr == null || subStr.isEmpty()) return 0;
        int count = 0;
        int index = 0;
        while ((index = str.indexOf(subStr, index)) != -1) {
            count++;
            index += subStr.length();
        }
        return count;
    }

    public static int countMatches(String str, char ch) {
        if (str == null) return 0;
        int count = 0;
        for (char c : str.toCharArray()) {
            if (c == ch) count++;
        }
        return count;
    }

    public static Map<Character, Integer> charFrequency(String str) {
        Map<Character, Integer> freq = new HashMap<>();
        if (str != null) {
            for (char c : str.toCharArray()) {
                freq.put(c, freq.getOrDefault(c, 0) + 1);
            }
        }
        return freq;
    }

    public static Map<String, Integer> wordFrequency(String str) {
        Map<String, Integer> freq = new HashMap<>();
        if (str != null) {
            String[] words = str.split("\\s+");
            for (String word : words) {
                if (!word.isEmpty()) {
                    freq.put(word, freq.getOrDefault(word, 0) + 1);
                }
            }
        }
        return freq;
    }

    // ==================== Cleaning | 清理 ====================

    public static String removeWhitespace(String str) {
        return str != null ? WHITESPACE_PATTERN.matcher(str).replaceAll("") : null;
    }

    public static String normalizeWhitespace(String str) {
        return str != null ? WHITESPACE_PATTERN.matcher(str).replaceAll(" ").trim() : null;
    }

    public static String removeInvisibleChars(String str) {
        return str != null ? INVISIBLE_CHARS_PATTERN.matcher(str).replaceAll("") : null;
    }

    public static String removeSpecialChars(String str) {
        return str != null ? SPECIAL_CHARS_PATTERN.matcher(str).replaceAll("") : null;
    }

    public static String keepDigits(String str) {
        return str != null ? NON_DIGIT_PATTERN.matcher(str).replaceAll("") : null;
    }

    public static String keepLetters(String str) {
        return str != null ? NON_LETTER_PATTERN.matcher(str).replaceAll("") : null;
    }

    public static String keepAlphanumeric(String str) {
        return str != null ? NON_ALPHANUMERIC_PATTERN.matcher(str).replaceAll("") : null;
    }

    public static String keepChinese(String str) {
        return str != null ? NON_CHINESE_PATTERN.matcher(str).replaceAll("") : null;
    }

    // ==================== Validation | 验证 ====================

    public static boolean isNumeric(String str) {
        return str != null && !str.isEmpty() && NUMERIC_PATTERN.matcher(str).matches();
    }

    public static boolean isAlpha(String str) {
        return str != null && !str.isEmpty() && ALPHA_PATTERN.matcher(str).matches();
    }

    public static boolean isAlphanumeric(String str) {
        return str != null && !str.isEmpty() && ALPHANUMERIC_PATTERN.matcher(str).matches();
    }

    public static boolean isAscii(String str) {
        return str != null && str.chars().allMatch(c -> c < 128);
    }

    public static boolean containsChinese(String str) {
        return str != null && CONTAINS_CHINESE_PATTERN.matcher(str).matches();
    }

    public static boolean isAllChinese(String str) {
        return str != null && !str.isEmpty() && ALL_CHINESE_PATTERN.matcher(str).matches();
    }

    public static boolean isAllLowerCase(String str) {
        return str != null && !str.isEmpty() && str.equals(str.toLowerCase());
    }

    public static boolean isAllUpperCase(String str) {
        return str != null && !str.isEmpty() && str.equals(str.toUpperCase());
    }

    public static boolean isMixedCase(String str) {
        if (str == null || str.isEmpty()) return false;
        boolean hasUpper = str.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = str.chars().anyMatch(Character::isLowerCase);
        return hasUpper && hasLower;
    }

    // ==================== Palindrome | 回文 ====================

    public static boolean isPalindrome(String str) {
        if (str == null || str.isEmpty()) return false;
        return str.equals(reverse(str));
    }

    public static boolean isPalindromeIgnoreCase(String str) {
        if (str == null) return false;
        String cleaned = WHITESPACE_PATTERN.matcher(str).replaceAll("").toLowerCase();
        return cleaned.equals(reverse(cleaned));
    }

    // ==================== Prefix/Suffix | 前缀后缀 ====================

    public static String ensurePrefix(String str, String prefix) {
        if (str == null || prefix == null) return str;
        return str.startsWith(prefix) ? str : prefix + str;
    }

    public static String ensureSuffix(String str, String suffix) {
        if (str == null || suffix == null) return str;
        return str.endsWith(suffix) ? str : str + suffix;
    }

    public static String ensureWrap(String str, String wrap) {
        return ensurePrefix(ensureSuffix(str, wrap), wrap);
    }

    public static String removePrefix(String str, String prefix) {
        if (str == null || prefix == null || !str.startsWith(prefix)) return str;
        return str.substring(prefix.length());
    }

    public static String removeSuffix(String str, String suffix) {
        if (str == null || suffix == null || !str.endsWith(suffix)) return str;
        return str.substring(0, str.length() - suffix.length());
    }

    public static String commonPrefix(String... strs) {
        if (strs == null || strs.length == 0) return "";
        String prefix = strs[0];
        for (int i = 1; i < strs.length; i++) {
            while (!strs[i].startsWith(prefix)) {
                prefix = prefix.substring(0, prefix.length() - 1);
                if (prefix.isEmpty()) return "";
            }
        }
        return prefix;
    }

    public static String commonSuffix(String... strs) {
        if (strs == null || strs.length == 0) return "";
        String suffix = strs[0];
        for (int i = 1; i < strs.length; i++) {
            while (!strs[i].endsWith(suffix)) {
                suffix = suffix.substring(1);
                if (suffix.isEmpty()) return "";
            }
        }
        return suffix;
    }

    // ==================== Search | 查找 ====================

    public static List<Integer> findAll(String str, String subStr) {
        List<Integer> positions = new ArrayList<>();
        if (str == null || subStr == null) return positions;
        int index = 0;
        while ((index = str.indexOf(subStr, index)) != -1) {
            positions.add(index);
            index += subStr.length();
        }
        return positions;
    }

    public static int indexOfNth(String str, String subStr, int n) {
        if (str == null || subStr == null || n < 1) return -1;
        int index = -1;
        for (int i = 0; i < n; i++) {
            index = str.indexOf(subStr, index + 1);
            if (index == -1) break;
        }
        return index;
    }

    public static int lastIndexOfNth(String str, String subStr, int n) {
        if (str == null || subStr == null || n < 1) return -1;
        int index = str.length();
        for (int i = 0; i < n; i++) {
            index = str.lastIndexOf(subStr, index - 1);
            if (index == -1) break;
        }
        return index;
    }

    // ==================== Substring Extraction | 子串提取 ====================

    public static String substringBefore(String str, String separator) {
        if (str == null || separator == null) return str;
        int pos = str.indexOf(separator);
        return pos == -1 ? str : str.substring(0, pos);
    }

    public static String substringAfter(String str, String separator) {
        if (str == null || separator == null) return "";
        int pos = str.indexOf(separator);
        return pos == -1 ? "" : str.substring(pos + separator.length());
    }

    public static String substringBeforeLast(String str, String separator) {
        if (str == null || separator == null) return str;
        int pos = str.lastIndexOf(separator);
        return pos == -1 ? str : str.substring(0, pos);
    }

    public static String substringAfterLast(String str, String separator) {
        if (str == null || separator == null) return "";
        int pos = str.lastIndexOf(separator);
        return pos == -1 ? "" : str.substring(pos + separator.length());
    }

    public static String substringBetween(String str, String open, String close) {
        if (str == null) return null;
        int start = str.indexOf(open);
        if (start == -1) return null;
        int end = str.indexOf(close, start + open.length());
        if (end == -1) return null;
        return str.substring(start + open.length(), end);
    }

    public static String[] substringsBetween(String str, String open, String close) {
        if (str == null) return new String[0];
        List<String> results = new ArrayList<>();
        int start = 0;
        while (true) {
            start = str.indexOf(open, start);
            if (start == -1) break;
            int end = str.indexOf(close, start + open.length());
            if (end == -1) break;
            results.add(str.substring(start + open.length(), end));
            start = end + close.length();
        }
        return results.toArray(new String[0]);
    }

    // ==================== Wrap/Unwrap | 包围与拆包 ====================

    public static String wrap(String str, char wrapChar) {
        return str != null ? wrapChar + str + wrapChar : null;
    }

    public static String wrap(String str, String wrapStr) {
        return str != null ? wrapStr + str + wrapStr : null;
    }

    public static String unwrap(String str, char wrapChar) {
        if (str == null || str.length() < 2) return str;
        if (str.charAt(0) == wrapChar && str.charAt(str.length() - 1) == wrapChar) {
            return str.substring(1, str.length() - 1);
        }
        return str;
    }

    public static String unwrap(String str, String wrapStr) {
        if (str == null || str.length() < wrapStr.length() * 2) return str;
        if (str.startsWith(wrapStr) && str.endsWith(wrapStr)) {
            return str.substring(wrapStr.length(), str.length() - wrapStr.length());
        }
        return str;
    }

    // ==================== Enhanced Cleaning | 增强清理 ====================

    public static String chomp(String str) {
        if (str == null || str.isEmpty()) return str;
        if (str.endsWith("\r\n")) return str.substring(0, str.length() - 2);
        if (str.endsWith("\n") || str.endsWith("\r")) return str.substring(0, str.length() - 1);
        return str;
    }

    public static String chop(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, str.length() - 1);
    }

    public static String stripAccents(String str) {
        if (str == null) return null;
        String normalized = Normalizer.normalize(str, Normalizer.Form.NFD);
        return DIACRITICAL_MARKS_PATTERN.matcher(normalized).replaceAll("");
    }

    public static String getDigits(String str) {
        return keepDigits(str);
    }

    // ==================== Rotation | 旋转 ====================

    public static String rotate(String str, int shift) {
        if (str == null || str.isEmpty()) return str;
        int len = str.length();
        shift = shift % len;
        if (shift < 0) shift += len;
        return str.substring(len - shift) + str.substring(0, len - shift);
    }

    // ==================== Difference | 差异 ====================

    public static String difference(String str1, String str2) {
        if (str1 == null) return str2;
        if (str2 == null) return str1;
        int index = indexOfDifference(str1, str2);
        return index == -1 ? "" : str2.substring(index);
    }

    public static int indexOfDifference(String str1, String str2) {
        if (str1 == str2) return -1;
        if (str1 == null || str2 == null) return 0;
        int minLen = Math.min(str1.length(), str2.length());
        for (int i = 0; i < minLen; i++) {
            if (str1.charAt(i) != str2.charAt(i)) return i;
        }
        if (str1.length() != str2.length()) return minLen;
        return -1;
    }

    // ==================== Abbreviation | 缩写 ====================

    public static String abbreviateMiddle(String str, String middle, int maxLength) {
        if (str == null || str.length() <= maxLength) return str;
        int targetLen = maxLength - middle.length();
        int startLen = targetLen / 2;
        int endLen = targetLen - startLen;
        return str.substring(0, startLen) + middle + str.substring(str.length() - endLen);
    }

    // ==================== Repetition Detection | 重复检测 ====================

    public static boolean isRepeated(String str) {
        return getRepeatedPattern(str) != null;
    }

    public static String getRepeatedPattern(String str) {
        if (str == null || str.isEmpty()) return null;
        int len = str.length();
        for (int i = 1; i <= len / 2; i++) {
            if (len % i == 0) {
                String pattern = str.substring(0, i);
                boolean matches = true;
                for (int j = i; j < len; j += i) {
                    if (!str.substring(j, j + i).equals(pattern)) {
                        matches = false;
                        break;
                    }
                }
                if (matches) return pattern;
            }
        }
        return null;
    }

    // ==================== Default Values | 默认值 ====================

    public static String defaultIfBlank(String str, String defaultStr) {
        return str == null || str.trim().isEmpty() ? defaultStr : str;
    }

    public static String defaultIfEmpty(String str, String defaultStr) {
        return str == null || str.isEmpty() ? defaultStr : str;
    }

    public static String firstNonBlank(String... strs) {
        if (strs == null) return null;
        for (String str : strs) {
            if (str != null && !str.trim().isEmpty()) return str;
        }
        return null;
    }

    public static String firstNonEmpty(String... strs) {
        if (strs == null) return null;
        for (String str : strs) {
            if (str != null && !str.isEmpty()) return str;
        }
        return null;
    }
}
