package cloud.opencode.base.core;

/**
 * Character Utility Class - Type checking, case conversion, ASCII and Unicode operations
 * 字符工具类 - 类型检查、大小写转换、ASCII 和 Unicode 操作
 *
 * <p>Provides comprehensive character operations including type validation, case conversion, and encoding operations.</p>
 * <p>提供全面的字符操作，包括类型检查、大小写转换、ASCII 和 Unicode 操作。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Type checking (isLetter, isDigit, isWhitespace, isAscii) - 类型检查</li>
 *   <li>Case conversion (toUpperCase, toLowerCase, toggleCase) - 大小写转换</li>
 *   <li>Character conversion (toString, toCodePoint, toUnicode) - 字符转换</li>
 *   <li>Hex/Octal digit checking (isHexDigit, isOctalDigit) - 十六进制/八进制检查</li>
 *   <li>Cached string representation for ASCII chars - ASCII 字符缓存优化</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Type checking - 类型检查
 * boolean isLetter = OpenChar.isLetter('A');      // true
 * boolean isAscii = OpenChar.isAscii('中');       // false
 *
 * // Case conversion - 大小写转换
 * char upper = OpenChar.toUpperCase('a');         // 'A'
 * char toggled = OpenChar.toggleCase('A');        // 'a'
 *
 * // Unicode - Unicode 转换
 * String unicode = OpenChar.toUnicode('中');      // "\\u4e2d"
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless, immutable cache) - 线程安全: 是 (无状态, 不可变缓存)</li>
 *   <li>Null-safe: N/A (primitive type) - 空值安全: 不适用 (原始类型)</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public final class OpenChar {

    private OpenChar() {
    }

    // 缓存常用字符的字符串表示
    private static final String[] CHAR_STRING_CACHE = new String[128];

    static {
        for (int i = 0; i < 128; i++) {
            CHAR_STRING_CACHE[i] = String.valueOf((char) i);
        }
    }

    // ==================== 类型检查 ====================

    /**
     * Checks whether the character is a letter
     * 检查是否为字母
     */
    public static boolean isLetter(char ch) {
        return Character.isLetter(ch);
    }

    /**
     * Checks whether the character is a digit
     * 检查是否为数字
     */
    public static boolean isDigit(char ch) {
        return Character.isDigit(ch);
    }

    /**
     * Checks whether the character is a letter or digit
     * 检查是否为字母或数字
     */
    public static boolean isAlphanumeric(char ch) {
        return Character.isLetterOrDigit(ch);
    }

    /**
     * Checks whether the character is whitespace
     * 检查是否为空白字符
     */
    public static boolean isWhitespace(char ch) {
        return Character.isWhitespace(ch);
    }

    /**
     * Checks whether the character is ASCII (0-127)
     * 检查是否为 ASCII 字符 (0-127)
     */
    public static boolean isAscii(char ch) {
        return ch < 128;
    }

    /**
     * Checks whether the character is a printable ASCII character (32-126)
     * 检查是否为可打印 ASCII 字符 (32-126)
     */
    public static boolean isPrintableAscii(char ch) {
        return ch >= 32 && ch < 127;
    }

    /**
     * Checks whether the character is a control character
     * 检查是否为控制字符
     */
    public static boolean isControl(char ch) {
        return Character.isISOControl(ch);
    }

    /**
     * Checks whether the character is uppercase
     * 检查是否为大写字母
     */
    public static boolean isUpperCase(char ch) {
        return Character.isUpperCase(ch);
    }

    /**
     * Checks whether the character is lowercase
     * 检查是否为小写字母
     */
    public static boolean isLowerCase(char ch) {
        return Character.isLowerCase(ch);
    }

    /**
     * Checks whether the character is a hex digit
     * 检查是否为十六进制数字
     */
    public static boolean isHexDigit(char ch) {
        return (ch >= '0' && ch <= '9') ||
                (ch >= 'a' && ch <= 'f') ||
                (ch >= 'A' && ch <= 'F');
    }

    /**
     * Checks whether the character is an octal digit
     * 检查是否为八进制数字
     */
    public static boolean isOctalDigit(char ch) {
        return ch >= '0' && ch <= '7';
    }

    // ==================== 大小写转换 ====================

    /**
     * Converts to uppercase
     * 转为大写
     */
    public static char toUpperCase(char ch) {
        return Character.toUpperCase(ch);
    }

    /**
     * Converts to lowercase
     * 转为小写
     */
    public static char toLowerCase(char ch) {
        return Character.toLowerCase(ch);
    }

    /**
     * Toggles the case
     * 切换大小写
     */
    public static char toggleCase(char ch) {
        if (Character.isUpperCase(ch)) {
            return Character.toLowerCase(ch);
        } else if (Character.isLowerCase(ch)) {
            return Character.toUpperCase(ch);
        }
        return ch;
    }

    // ==================== 转换 ====================

    /**
     * Converts character to string (cache-optimized)
     * 字符转字符串（缓存优化）
     */
    public static String toString(char ch) {
        if (ch < 128) {
            return CHAR_STRING_CACHE[ch];
        }
        return String.valueOf(ch);
    }

    /**
     * Converts to a Unicode code point
     * 转为 Unicode 码点
     */
    public static int toCodePoint(char ch) {
        return ch;
    }

    /**
     * Converts from a code point to a character
     * 从码点转字符
     */
    public static char fromCodePoint(int codePoint) {
        if (codePoint < 0 || codePoint > Character.MAX_VALUE) {
            throw new IllegalArgumentException("Invalid code point: " + codePoint);
        }
        return (char) codePoint;
    }

    /**
     * Converts to a hexadecimal string
     * 转十六进制字符串
     */
    public static String toHexString(char ch) {
        return Integer.toHexString(ch);
    }

    /**
     * Converts to Unicode representation (\\uXXXX)
     * 转 Unicode 表示 (\\uXXXX)
     */
    public static String toUnicode(char ch) {
        return String.format("\\u%04x", (int) ch);
    }

    /**
     * Converts character to its numeric value (0-9)
     * 字符转数字值（0-9）
     */
    public static int toDigit(char ch) {
        return Character.digit(ch, 10);
    }

    /**
     * Converts character to its numeric value with the specified radix
     * 字符转数字值（指定进制）
     */
    public static int toDigit(char ch, int radix) {
        return Character.digit(ch, radix);
    }

    // ==================== 字符操作 ====================

    /**
     * Repeats the character n times
     * 重复字符 n 次
     */
    public static String repeat(char ch, int count) {
        if (count <= 0) {
            return "";
        }
        return String.valueOf(ch).repeat(count);
    }

    /**
     * Compares two characters ignoring case
     * 比较两个字符（忽略大小写）
     */
    public static boolean equalsIgnoreCase(char ch1, char ch2) {
        return Character.toLowerCase(ch1) == Character.toLowerCase(ch2);
    }

    /**
     * Gets the numeric value of the character
     * 获取字符的数值（用于比较）
     */
    public static int getNumericValue(char ch) {
        return Character.getNumericValue(ch);
    }

    /**
     * Checks whether the character is within the specified range
     * 检查字符是否在指定范围内
     */
    public static boolean inRange(char ch, char start, char end) {
        return ch >= start && ch <= end;
    }
}
