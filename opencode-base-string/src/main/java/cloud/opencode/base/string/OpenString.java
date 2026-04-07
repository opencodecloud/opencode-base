package cloud.opencode.base.string;

import java.nio.charset.Charset;
import java.text.Normalizer;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
        // StringBuilder avoids the intermediate String allocation of valueOf().repeat() + concat.
        // StringBuilder 避免 valueOf().repeat() + 拼接的中间 String 分配。
        int padLen = minLength - str.length();
        var sb = new StringBuilder(minLength);
        for (int i = 0; i < padLen; i++) sb.append(padChar);
        sb.append(str);
        return sb.toString();
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
        int padLen = minLength - str.length();
        var sb = new StringBuilder(minLength);
        sb.append(str);
        for (int i = 0; i < padLen; i++) sb.append(padChar);
        return sb.toString();
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

    // ==================== Null-safe Checks | 空值安全检查 ====================

    /**
     * Check if a string is blank (null, empty, or whitespace only).
     * 检查字符串是否为空白（null、空字符串或仅包含空白字符）。
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * isBlank(null)   = true
     * isBlank("")     = true
     * isBlank("  ")   = true
     * isBlank("abc")  = false
     * </pre>
     *
     * @param str the string to check, may be {@code null} | 待检查的字符串，可为 {@code null}
     * @return {@code true} if the string is null, empty, or whitespace only | 如果字符串为null、空或仅含空白则返回true
     * @since JDK 25, opencode-base-string V1.0.3
     */
    public static boolean isBlank(String str) {
        return str == null || str.isBlank();
    }

    /**
     * Check if a string is not blank.
     * 检查字符串是否不为空白。
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * isNotBlank(null)   = false
     * isNotBlank("")     = false
     * isNotBlank("  ")   = false
     * isNotBlank("abc")  = true
     * </pre>
     *
     * @param str the string to check, may be {@code null} | 待检查的字符串，可为 {@code null}
     * @return {@code true} if the string is not null, not empty, and not whitespace only | 如果字符串非null、非空且非纯空白则返回true
     * @since JDK 25, opencode-base-string V1.0.3
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * Check if a string is empty (null or zero length).
     * 检查字符串是否为空（null或长度为零）。
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * isEmpty(null)   = true
     * isEmpty("")     = true
     * isEmpty("  ")   = false
     * isEmpty("abc")  = false
     * </pre>
     *
     * @param str the string to check, may be {@code null} | 待检查的字符串，可为 {@code null}
     * @return {@code true} if the string is null or empty | 如果字符串为null或空则返回true
     * @since JDK 25, opencode-base-string V1.0.3
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * Check if a string is not empty.
     * 检查字符串是否不为空。
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * isNotEmpty(null)   = false
     * isNotEmpty("")     = false
     * isNotEmpty("  ")   = true
     * isNotEmpty("abc")  = true
     * </pre>
     *
     * @param str the string to check, may be {@code null} | 待检查的字符串，可为 {@code null}
     * @return {@code true} if the string is not null and not empty | 如果字符串非null且非空则返回true
     * @since JDK 25, opencode-base-string V1.0.3
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    // ==================== Contains Matching | 包含匹配 ====================

    /**
     * Check if a string contains any of the given search strings.
     * 检查字符串是否包含给定搜索字符串中的任意一个。
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * containsAny("hello world", "world", "foo") = true
     * containsAny("hello", "foo", "bar")         = false
     * containsAny(null, "foo")                    = false
     * </pre>
     *
     * @param str           the string to check, may be {@code null} | 待检查的字符串，可为 {@code null}
     * @param searchStrings the strings to search for | 要搜索的字符串
     * @return {@code true} if the string contains any of the search strings | 如果字符串包含任一搜索字符串则返回true
     * @since JDK 25, opencode-base-string V1.0.3
     */
    public static boolean containsAny(String str, CharSequence... searchStrings) {
        if (str == null || searchStrings == null) return false;
        for (CharSequence search : searchStrings) {
            if (search != null && str.contains(search)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if a string contains none of the given invalid characters.
     * 检查字符串是否不包含任何给定的无效字符。
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * containsNone("hello", "xyz")  = true
     * containsNone("hello", "hxyz") = false
     * containsNone(null, "xyz")     = true
     * </pre>
     *
     * @param str          the string to check, may be {@code null} | 待检查的字符串，可为 {@code null}
     * @param invalidChars characters that should not appear | 不应出现的字符
     * @return {@code true} if the string contains none of the invalid characters | 如果字符串不包含任何无效字符则返回true
     * @since JDK 25, opencode-base-string V1.0.3
     */
    public static boolean containsNone(String str, String invalidChars) {
        if (str == null || invalidChars == null) return true;
        for (int i = 0; i < str.length(); i++) {
            if (invalidChars.indexOf(str.charAt(i)) >= 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if a string contains only the given valid characters.
     * 检查字符串是否仅包含给定的有效字符。
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * containsOnly("aab", "abc")  = true
     * containsOnly("abd", "abc")  = false
     * containsOnly(null, "abc")   = false
     * </pre>
     *
     * @param str        the string to check, may be {@code null} | 待检查的字符串，可为 {@code null}
     * @param validChars characters that are allowed | 允许的字符
     * @return {@code true} if the string contains only valid characters | 如果字符串仅包含有效字符则返回true
     * @since JDK 25, opencode-base-string V1.0.3
     */
    public static boolean containsOnly(String str, String validChars) {
        if (str == null || validChars == null) return false;
        for (int i = 0; i < str.length(); i++) {
            if (validChars.indexOf(str.charAt(i)) < 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if a string contains another string, ignoring case.
     * 忽略大小写检查字符串是否包含另一个字符串。
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * containsIgnoreCase("Hello World", "hello") = true
     * containsIgnoreCase("Hello World", "foo")   = false
     * containsIgnoreCase(null, "hello")           = false
     * </pre>
     *
     * @param str    the string to check, may be {@code null} | 待检查的字符串，可为 {@code null}
     * @param search the string to search for, may be {@code null} | 要搜索的字符串，可为 {@code null}
     * @return {@code true} if the string contains the search string ignoring case | 如果忽略大小写后字符串包含搜索字符串则返回true
     * @since JDK 25, opencode-base-string V1.0.3
     */
    public static boolean containsIgnoreCase(String str, String search) {
        if (str == null || search == null) return false;
        int searchLen = search.length();
        if (searchLen == 0) return true;
        // regionMatches(ignoreCase=true) avoids two toLowerCase() String allocations.
        // regionMatches(ignoreCase=true) 避免两次 toLowerCase() 的 String 分配。
        int maxStart = str.length() - searchLen;
        for (int i = 0; i <= maxStart; i++) {
            if (str.regionMatches(true, i, search, 0, searchLen)) {
                return true;
            }
        }
        return false;
    }

    // ==================== StartsWith/EndsWith Matching | 前缀后缀匹配 ====================

    /**
     * Check if a string starts with any of the given prefixes.
     * 检查字符串是否以给定前缀中的任意一个开头。
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * startsWithAny("hello", "he", "ho") = true
     * startsWithAny("hello", "foo")      = false
     * startsWithAny(null, "he")          = false
     * </pre>
     *
     * @param str      the string to check, may be {@code null} | 待检查的字符串，可为 {@code null}
     * @param prefixes the prefixes to check | 要检查的前缀
     * @return {@code true} if the string starts with any of the prefixes | 如果字符串以任一前缀开头则返回true
     * @since JDK 25, opencode-base-string V1.0.3
     */
    public static boolean startsWithAny(String str, String... prefixes) {
        if (str == null || prefixes == null) return false;
        for (String prefix : prefixes) {
            if (prefix != null && str.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if a string ends with any of the given suffixes.
     * 检查字符串是否以给定后缀中的任意一个结尾。
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * endsWithAny("hello", "lo", "la") = true
     * endsWithAny("hello", "foo")      = false
     * endsWithAny(null, "lo")          = false
     * </pre>
     *
     * @param str      the string to check, may be {@code null} | 待检查的字符串，可为 {@code null}
     * @param suffixes the suffixes to check | 要检查的后缀
     * @return {@code true} if the string ends with any of the suffixes | 如果字符串以任一后缀结尾则返回true
     * @since JDK 25, opencode-base-string V1.0.3
     */
    public static boolean endsWithAny(String str, String... suffixes) {
        if (str == null || suffixes == null) return false;
        for (String suffix : suffixes) {
            if (suffix != null && str.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if a string starts with a prefix, ignoring case.
     * 忽略大小写检查字符串是否以指定前缀开头。
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * startsWithIgnoreCase("Hello", "he")  = true
     * startsWithIgnoreCase("Hello", "HE")  = true
     * startsWithIgnoreCase("Hello", "foo") = false
     * </pre>
     *
     * @param str    the string to check, may be {@code null} | 待检查的字符串，可为 {@code null}
     * @param prefix the prefix to check, may be {@code null} | 要检查的前缀，可为 {@code null}
     * @return {@code true} if the string starts with the prefix ignoring case | 如果忽略大小写后字符串以该前缀开头则返回true
     * @since JDK 25, opencode-base-string V1.0.3
     */
    public static boolean startsWithIgnoreCase(String str, String prefix) {
        if (str == null || prefix == null) return false;
        if (prefix.length() > str.length()) return false;
        return str.regionMatches(true, 0, prefix, 0, prefix.length());
    }

    /**
     * Check if a string ends with a suffix, ignoring case.
     * 忽略大小写检查字符串是否以指定后缀结尾。
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * endsWithIgnoreCase("Hello", "LO")  = true
     * endsWithIgnoreCase("Hello", "lo")  = true
     * endsWithIgnoreCase("Hello", "foo") = false
     * </pre>
     *
     * @param str    the string to check, may be {@code null} | 待检查的字符串，可为 {@code null}
     * @param suffix the suffix to check, may be {@code null} | 要检查的后缀，可为 {@code null}
     * @return {@code true} if the string ends with the suffix ignoring case | 如果忽略大小写后字符串以该后缀结尾则返回true
     * @since JDK 25, opencode-base-string V1.0.3
     */
    public static boolean endsWithIgnoreCase(String str, String suffix) {
        if (str == null || suffix == null) return false;
        if (suffix.length() > str.length()) return false;
        return str.regionMatches(true, str.length() - suffix.length(), suffix, 0, suffix.length());
    }

    // ==================== Single-pass Multi-pattern Replace | 单趟多模式替换 ====================

    /**
     * Replace all occurrences of search strings with corresponding replacement strings in a single pass.
     * 单趟扫描替换所有搜索字符串为对应的替换字符串，不递归替换。
     *
     * <p>This method scans the input text once and replaces each occurrence of a search string
     * with the corresponding replacement. Replacements are not applied recursively.</p>
     * <p>本方法对输入文本进行单趟扫描，将每个搜索字符串替换为对应的替换字符串。替换不会递归应用。</p>
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * replaceEach("aabbcc", new String[]{"aa","bb"}, new String[]{"11","22"}) = "1122cc"
     * replaceEach("abcde", new String[]{"ab","d"}, new String[]{"w","t"})     = "wcte"
     * </pre>
     *
     * @param text            the text to search and replace in, may be {@code null} | 要搜索替换的文本，可为 {@code null}
     * @param searchList      the strings to search for, may be {@code null} | 要搜索的字符串数组，可为 {@code null}
     * @param replacementList the strings to replace them with, may be {@code null} | 替换字符串数组，可为 {@code null}
     * @return the text with replacements applied, or original text if no replacements needed | 替换后的文本
     * @throws IllegalArgumentException if searchList and replacementList have different lengths | 如果搜索和替换数组长度不同
     * @since JDK 25, opencode-base-string V1.0.3
     */
    public static String replaceEach(String text, String[] searchList, String[] replacementList) {
        if (text == null || text.isEmpty() || searchList == null || replacementList == null) {
            return text;
        }
        if (searchList.length != replacementList.length) {
            throw new IllegalArgumentException(
                    "searchList and replacementList must have the same length: "
                            + searchList.length + " vs " + replacementList.length);
        }

        int textLen = text.length();
        StringBuilder sb = new StringBuilder(textLen);
        int i = 0;
        while (i < textLen) {
            int bestIndex = -1;
            int bestLen = 0;
            // find the earliest (and longest) match at position i
            for (int j = 0; j < searchList.length; j++) {
                String search = searchList[j];
                if (search == null || search.isEmpty()) continue;
                if (text.startsWith(search, i)) {
                    if (bestIndex == -1 || search.length() > bestLen) {
                        bestIndex = j;
                        bestLen = search.length();
                    }
                }
            }
            if (bestIndex >= 0) {
                sb.append(replacementList[bestIndex] != null ? replacementList[bestIndex] : "");
                i += bestLen;
            } else {
                sb.append(text.charAt(i));
                i++;
            }
        }
        return sb.toString();
    }

    // ==================== SLF4J-style Format | SLF4J风格格式化 ====================

    /**
     * Format a string using SLF4J-style {@code {}} placeholders.
     * 使用SLF4J风格的 {@code {}} 占位符格式化字符串。
     *
     * <p>Placeholders are replaced left-to-right with the provided arguments.
     * Use {@code \{}} to escape a placeholder (outputs literal {@code {}}).
     * Excess arguments are ignored; insufficient arguments leave placeholders intact.</p>
     * <p>占位符从左到右依次替换为提供的参数。
     * 使用 {@code \{}} 转义占位符（输出字面量 {@code {}}）。
     * 多余参数被忽略；参数不足时保留未替换的占位符。</p>
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * format("{} has {} items", "Alice", 3) = "Alice has 3 items"
     * format("{} and {}", "A")              = "A and {}"
     * format("literal \\{}", "ignored")      = "literal {}"
     * </pre>
     *
     * @param pattern the pattern string with {@code {}} placeholders, may be {@code null} | 包含占位符的模式字符串，可为 {@code null}
     * @param args    the arguments to fill in placeholders | 用于填充占位符的参数
     * @return the formatted string, or {@code null} if pattern is {@code null} | 格式化后的字符串，pattern为null时返回null
     * @since JDK 25, opencode-base-string V1.0.3
     */
    public static String format(String pattern, Object... args) {
        if (pattern == null) return null;
        if (args == null) args = new Object[0];

        // quick check: if no escape sequences and no args, return as-is
        if (args.length == 0 && pattern.indexOf('\\') < 0) return pattern;

        // Batch-append literal runs instead of char-by-char to reduce append() call count.
        // 批量追加字面量段，减少 append() 调用次数。
        StringBuilder sb = new StringBuilder(pattern.length() + 50);
        int argIndex = 0;
        int i = 0;
        int len = pattern.length();
        int literalStart = 0; // start of current literal run
        while (i < len) {
            if (i + 1 < len && pattern.charAt(i) == '\\' && pattern.charAt(i + 1) == '{') {
                if (i + 2 < len && pattern.charAt(i + 2) == '}') {
                    sb.append(pattern, literalStart, i);
                    sb.append("{}");
                    i += 3;
                    literalStart = i;
                } else {
                    i++;
                }
            } else if (i + 1 < len && pattern.charAt(i) == '{' && pattern.charAt(i + 1) == '}') {
                sb.append(pattern, literalStart, i);
                if (argIndex < args.length) {
                    sb.append(args[argIndex]);
                    argIndex++;
                } else {
                    sb.append("{}");
                }
                i += 2;
                literalStart = i;
            } else {
                i++;
            }
        }
        // Flush remaining literal tail
        if (literalStart < len) {
            sb.append(pattern, literalStart, len);
        }
        return sb.toString();
    }

    // ==================== Null/Empty/Blank Conversion | 空值转换 ====================

    /**
     * Convert a {@code null} string to an empty string.
     * 将 {@code null} 字符串转换为空字符串。
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * nullToEmpty(null)    = ""
     * nullToEmpty("")      = ""
     * nullToEmpty("hello") = "hello"
     * </pre>
     *
     * @param str the string, may be {@code null} | 字符串，可为 {@code null}
     * @return the original string or empty string if null | 原字符串或null时返回空字符串
     * @since JDK 25, opencode-base-string V1.0.3
     */
    public static String nullToEmpty(String str) {
        return str == null ? "" : str;
    }

    /**
     * Convert an empty string to {@code null}.
     * 将空字符串转换为 {@code null}。
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * emptyToNull("")      = null
     * emptyToNull(null)    = null
     * emptyToNull("hello") = "hello"
     * emptyToNull("  ")    = "  "
     * </pre>
     *
     * @param str the string, may be {@code null} | 字符串，可为 {@code null}
     * @return {@code null} if the string is empty, otherwise the original string | 字符串为空时返回null，否则返回原字符串
     * @since JDK 25, opencode-base-string V1.0.3
     */
    public static String emptyToNull(String str) {
        return str != null && str.isEmpty() ? null : str;
    }

    /**
     * Convert a blank string (null, empty, or whitespace only) to {@code null}.
     * 将空白字符串（null、空字符串或仅含空白字符）转换为 {@code null}。
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * blankToNull("")      = null
     * blankToNull("  ")    = null
     * blankToNull(null)    = null
     * blankToNull("hello") = "hello"
     * </pre>
     *
     * @param str the string, may be {@code null} | 字符串，可为 {@code null}
     * @return {@code null} if the string is blank, otherwise the original string | 字符串为空白时返回null，否则返回原字符串
     * @since JDK 25, opencode-base-string V1.0.3
     */
    public static String blankToNull(String str) {
        return isBlank(str) ? null : str;
    }

    // ==================== Repeat with Separator | 带分隔符重复 ====================

    /**
     * Repeat a string with a separator between each occurrence.
     * 使用分隔符重复字符串。
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * repeat("abc", ", ", 3) = "abc, abc, abc"
     * repeat("x", "-", 1)   = "x"
     * repeat("x", "-", 0)   = ""
     * repeat(null, "-", 3)  = null
     * </pre>
     *
     * @param str       the string to repeat, may be {@code null} | 要重复的字符串，可为 {@code null}
     * @param separator the separator between repetitions | 重复之间的分隔符
     * @param count     the number of repetitions; {@code <= 0} returns empty string | 重复次数；小于等于0返回空字符串
     * @return the repeated string with separators, or {@code null} if str is {@code null} | 带分隔符的重复字符串，str为null时返回null
     * @since JDK 25, opencode-base-string V1.0.3
     */
    public static String repeat(String str, String separator, int count) {
        if (str == null) return null;
        if (count <= 0) return "";
        if (count == 1) return str;
        StringBuilder sb = new StringBuilder(str.length() * count + (separator != null ? separator.length() : 0) * (count - 1));
        for (int i = 0; i < count; i++) {
            if (i > 0 && separator != null) {
                sb.append(separator);
            }
            sb.append(str);
        }
        return sb.toString();
    }

    // ==================== Ignore-case Prefix/Suffix Removal | 忽略大小写前缀后缀移除 ====================

    /**
     * Remove a prefix from a string, ignoring case.
     * 忽略大小写移除字符串的前缀。
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * removePrefixIgnoreCase("HelloWorld", "hello") = "World"
     * removePrefixIgnoreCase("HelloWorld", "HELLO") = "World"
     * removePrefixIgnoreCase("HelloWorld", "foo")   = "HelloWorld"
     * </pre>
     *
     * @param str    the string, may be {@code null} | 字符串，可为 {@code null}
     * @param prefix the prefix to remove, may be {@code null} | 要移除的前缀，可为 {@code null}
     * @return the string without the prefix, or original if no match | 移除前缀后的字符串，不匹配时返回原字符串
     * @since JDK 25, opencode-base-string V1.0.3
     */
    public static String removePrefixIgnoreCase(String str, String prefix) {
        if (str == null || prefix == null) return str;
        if (startsWithIgnoreCase(str, prefix)) {
            return str.substring(prefix.length());
        }
        return str;
    }

    /**
     * Remove a suffix from a string, ignoring case.
     * 忽略大小写移除字符串的后缀。
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * removeSuffixIgnoreCase("HelloWorld", "world") = "Hello"
     * removeSuffixIgnoreCase("HelloWorld", "WORLD") = "Hello"
     * removeSuffixIgnoreCase("HelloWorld", "foo")   = "HelloWorld"
     * </pre>
     *
     * @param str    the string, may be {@code null} | 字符串，可为 {@code null}
     * @param suffix the suffix to remove, may be {@code null} | 要移除的后缀，可为 {@code null}
     * @return the string without the suffix, or original if no match | 移除后缀后的字符串，不匹配时返回原字符串
     * @since JDK 25, opencode-base-string V1.0.3
     */
    public static String removeSuffixIgnoreCase(String str, String suffix) {
        if (str == null || suffix == null) return str;
        if (endsWithIgnoreCase(str, suffix)) {
            return str.substring(0, str.length() - suffix.length());
        }
        return str;
    }

    // ==================== Split/Join | 分割与合并 ====================

    /**
     * Split a string by separator, returning a {@link List}. Null-safe.
     * 按分隔符分割字符串，返回 {@link List}。空值安全。
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * split("a,b,c", ",")  = ["a", "b", "c"]
     * split(null, ",")     = []
     * split("", ",")       = [""]
     * </pre>
     *
     * @param str       the string to split, may be {@code null} | 要分割的字符串，可为 {@code null}
     * @param separator the separator string | 分隔符字符串
     * @return a list of split parts, never {@code null} | 分割后的部分列表，不会返回 {@code null}
     * @since JDK 25, opencode-base-string V1.0.3
     */
    public static List<String> split(String str, String separator) {
        if (str == null) return List.of();
        if (separator == null || separator.isEmpty()) return List.of(str);
        List<String> result = new ArrayList<>();
        int start = 0;
        int idx;
        while ((idx = str.indexOf(separator, start)) != -1) {
            result.add(str.substring(start, idx));
            start = idx + separator.length();
        }
        result.add(str.substring(start));
        return result;
    }

    /**
     * Split a string into a key-value map using entry and key-value separators.
     * 使用条目分隔符和键值分隔符将字符串分割为键值映射。
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * splitToMap("a=1&b=2", "&", "=") = {a=1, b=2}
     * splitToMap(null, "&", "=")       = {}
     * </pre>
     *
     * @param str      the string to split, may be {@code null} | 要分割的字符串，可为 {@code null}
     * @param entrySep the separator between entries | 条目之间的分隔符
     * @param kvSep    the separator between key and value | 键值之间的分隔符
     * @return a map of key-value pairs, never {@code null} | 键值对映射，不会返回 {@code null}
     * @since JDK 25, opencode-base-string V1.0.3
     */
    public static Map<String, String> splitToMap(String str, String entrySep, String kvSep) {
        if (str == null || str.isEmpty()) return Map.of();
        if (entrySep == null || kvSep == null) return Map.of();
        Map<String, String> map = new LinkedHashMap<>();
        List<String> entries = split(str, entrySep);
        for (String entry : entries) {
            int idx = entry.indexOf(kvSep);
            if (idx >= 0) {
                map.put(entry.substring(0, idx), entry.substring(idx + kvSep.length()));
            }
        }
        return map;
    }

    /**
     * Join elements with a separator.
     * 使用分隔符合并元素。
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * join(", ", "a", "b", "c") = "a, b, c"
     * join(", ", "a", null, "c") = "a, null, c"
     * </pre>
     *
     * @param separator the separator string | 分隔符字符串
     * @param elements  the elements to join | 要合并的元素
     * @return the joined string | 合并后的字符串
     * @since JDK 25, opencode-base-string V1.0.3
     */
    public static String join(String separator, Object... elements) {
        if (elements == null) return "";
        StringJoiner joiner = new StringJoiner(separator != null ? separator : "");
        for (Object element : elements) {
            joiner.add(String.valueOf(element));
        }
        return joiner.toString();
    }

    /**
     * Join elements with a separator, skipping {@code null} elements.
     * 使用分隔符合并元素，跳过 {@code null} 元素。
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * joinSkipNulls(", ", "a", null, "c") = "a, c"
     * </pre>
     *
     * @param separator the separator string | 分隔符字符串
     * @param elements  the elements to join | 要合并的元素
     * @return the joined string without null elements | 跳过null后合并的字符串
     * @since JDK 25, opencode-base-string V1.0.3
     */
    public static String joinSkipNulls(String separator, Object... elements) {
        if (elements == null) return "";
        StringJoiner joiner = new StringJoiner(separator != null ? separator : "");
        for (Object element : elements) {
            if (element != null) {
                joiner.add(String.valueOf(element));
            }
        }
        return joiner.toString();
    }

    /**
     * Join elements with a separator, skipping blank elements.
     * 使用分隔符合并元素，跳过空白元素。
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * joinSkipBlanks(", ", "a", "", "  ", "c") = "a, c"
     * </pre>
     *
     * @param separator the separator string | 分隔符字符串
     * @param elements  the elements to join | 要合并的元素
     * @return the joined string without blank elements | 跳过空白后合并的字符串
     * @since JDK 25, opencode-base-string V1.0.3
     */
    public static String joinSkipBlanks(String separator, CharSequence... elements) {
        if (elements == null) return "";
        StringJoiner joiner = new StringJoiner(separator != null ? separator : "");
        for (CharSequence element : elements) {
            if (element != null && !element.toString().isBlank()) {
                joiner.add(element);
            }
        }
        return joiner.toString();
    }

    // ==================== Abbreviation | 简写 ====================

    /**
     * Abbreviate a string using ellipsis ("...") if it exceeds the maximum width.
     * 当字符串超过最大宽度时使用省略号("...")进行简写。
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * abbreviate("Hello World", 8) = "Hello..."
     * abbreviate("Hi", 8)          = "Hi"
     * abbreviate(null, 8)          = null
     * </pre>
     *
     * @param str      the string to abbreviate, may be {@code null} | 要简写的字符串，可为 {@code null}
     * @param maxWidth the maximum width; must be {@code >= 4} | 最大宽度；必须 {@code >= 4}
     * @return the abbreviated string | 简写后的字符串
     * @throws IllegalArgumentException if maxWidth is less than 4 | 如果maxWidth小于4
     * @since JDK 25, opencode-base-string V1.0.3
     */
    public static String abbreviate(String str, int maxWidth) {
        return abbreviate(str, 0, maxWidth);
    }

    /**
     * Abbreviate a string using ellipsis ("...") starting from an offset.
     * 从指定偏移位置开始使用省略号("...")简写字符串。
     *
     * <p>If the offset is greater than 0 and the string needs abbreviation, the result
     * may start with "..." to indicate truncation from the left.</p>
     * <p>如果偏移量大于0且字符串需要简写，结果可能以"..."开头表示左侧被截断。</p>
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * abbreviate("Hello World Test", 0, 11) = "Hello Wo..."
     * abbreviate("Hello World Test", 6, 11) = "...orld ..."
     * abbreviate(null, 0, 8)                = null
     * </pre>
     *
     * @param str      the string to abbreviate, may be {@code null} | 要简写的字符串，可为 {@code null}
     * @param offset   the left edge of the source string; if positive and truncation is needed, prepends "..." | 源字符串的左边缘偏移
     * @param maxWidth the maximum width; must be {@code >= 4} | 最大宽度；必须 {@code >= 4}
     * @return the abbreviated string | 简写后的字符串
     * @throws IllegalArgumentException if maxWidth is less than 4 | 如果maxWidth小于4
     * @since JDK 25, opencode-base-string V1.0.3
     */
    public static String abbreviate(String str, int offset, int maxWidth) {
        if (maxWidth < 4) {
            throw new IllegalArgumentException("maxWidth must be at least 4, was: " + maxWidth);
        }
        if (str == null || str.length() <= maxWidth) return str;

        if (offset > 0) {
            // "..." at both ends needs at least 7 chars: "...X..."
            if (maxWidth < 7) {
                // not enough room for "...X...", fall through to simple right truncation
                return str.substring(0, maxWidth - 3) + "...";
            }

            // clamp offset so we don't go past the string
            if (offset >= str.length()) {
                offset = str.length() - (maxWidth - 3);
            }
            if (offset < 0) offset = 0;

            if (offset > 0) {
                // need "..." at the beginning
                if (str.length() - offset <= maxWidth - 3) {
                    return "..." + str.substring(str.length() - (maxWidth - 3));
                } else {
                    // "..." at both ends
                    return "..." + str.substring(offset, offset + maxWidth - 6) + "...";
                }
            }
        }

        // simple right truncation
        return str.substring(0, maxWidth - 3) + "...";
    }
}
