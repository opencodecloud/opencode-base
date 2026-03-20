package cloud.opencode.base.core;

/**
 * Radix Conversion Utility Class - Binary, octal, decimal, hexadecimal and custom radix conversions
 * 进制转换工具类 - 二进制、八进制、十进制、十六进制和自定义进制转换
 *
 * <p>Supports conversions between binary, octal, decimal, hexadecimal and custom radixes (2-62).</p>
 * <p>支持二进制、八进制、十进制、十六进制和自定义进制之间的转换。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Decimal to other radix (decimalToBinary, decimalToOctal, decimalToHexadecimal) - 十进制转其他进制</li>
 *   <li>Other radix to decimal (binaryToDecimal, octalToDecimal, hexadecimalToDecimal) - 其他进制转十进制</li>
 *   <li>Inter-radix conversion (binaryToHex, hexToBinary, convert) - 进制间转换</li>
 *   <li>Extended radix support (toBaseExtended, fromBaseExtended for 2-62) - 扩展进制支持</li>
 *   <li>Formatting (formatBinary, formatHex) - 格式化</li>
 *   <li>Validation (isBinary, isOctal, isHexadecimal) - 验证</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Decimal to binary - 十进制转二进制
 * String binary = OpenRadix.decimalToBinary(255);    // "11111111"
 *
 * // Binary to hex - 二进制转十六进制
 * String hex = OpenRadix.binaryToHex("11111111");    // "FF"
 *
 * // Custom radix - 自定义进制
 * String base36 = OpenRadix.toBase(1000, 36);        // "RS"
 *
 * // Validation - 验证
 * boolean valid = OpenRadix.isBinary("1010");        // true
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是 (无状态)</li>
 *   <li>Null-safe: Partially (throws on null input) - 空值安全: 部分 (null 输入抛异常)</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public final class OpenRadix {

    private OpenRadix() {
    }

    // 自定义进制字符表（支持 2-62 进制）
    private static final char[] DIGITS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

    // ==================== 十进制转其他进制 ====================

    /**
     * Converts decimal to binary
     * 十进制转二进制
     */
    public static String decimalToBinary(long value) {
        return Long.toBinaryString(value);
    }

    /**
     * Converts decimal to octal
     * 十进制转八进制
     */
    public static String decimalToOctal(long value) {
        return Long.toOctalString(value);
    }

    /**
     * Converts decimal to hexadecimal
     * 十进制转十六进制
     */
    public static String decimalToHexadecimal(long value) {
        return Long.toHexString(value).toUpperCase();
    }

    /**
     * Converts decimal to hexadecimal (lowercase)
     * 十进制转十六进制（小写）
     */
    public static String decimalToHexadecimalLower(long value) {
        return Long.toHexString(value);
    }

    /**
     * Converts decimal to any radix (2-36)
     * 十进制转任意进制（2-36）
     */
    public static String toBase(long value, int radix) {
        if (radix < 2 || radix > 36) {
            throw new IllegalArgumentException("Radix must be between 2 and 36");
        }
        return Long.toString(value, radix).toUpperCase();
    }

    /**
     * Converts decimal to any radix (2-62, extended)
     * 十进制转任意进制（2-62，扩展）
     */
    public static String toBaseExtended(long value, int radix) {
        if (radix < 2 || radix > 62) {
            throw new IllegalArgumentException("Radix must be between 2 and 62");
        }
        if (radix <= 36) {
            return toBase(value, radix);
        }

        if (value == 0) {
            return "0";
        }

        boolean negative = value < 0;

        StringBuilder sb = new StringBuilder();
        // Use negative range to avoid overflow (Long.MIN_VALUE cannot be negated)
        long work = negative ? value : -value;
        while (work < 0) {
            // work is negative, so -(work % radix) gives the positive digit
            sb.append(DIGITS[(int) -(work % radix)]);
            work /= radix;
        }

        if (negative) {
            sb.append('-');
        }

        return sb.reverse().toString();
    }

    // ==================== 其他进制转十进制 ====================

    /**
     * Converts binary to decimal
     * 二进制转十进制
     */
    public static long binaryToDecimal(String binary) {
        return Long.parseLong(binary, 2);
    }

    /**
     * Converts octal to decimal
     * 八进制转十进制
     */
    public static long octalToDecimal(String octal) {
        return Long.parseLong(octal, 8);
    }

    /**
     * Converts hexadecimal to decimal
     * 十六进制转十进制
     */
    public static long hexadecimalToDecimal(String hex) {
        return Long.parseLong(hex, 16);
    }

    /**
     * Converts any radix to decimal (2-36)
     * 任意进制转十进制（2-36）
     */
    public static long fromBase(String value, int radix) {
        if (radix < 2 || radix > 36) {
            throw new IllegalArgumentException("Radix must be between 2 and 36");
        }
        return Long.parseLong(value, radix);
    }

    /**
     * Converts any radix to decimal (2-62, extended)
     * 任意进制转十进制（2-62，扩展）
     */
    public static long fromBaseExtended(String value, int radix) {
        if (radix < 2 || radix > 62) {
            throw new IllegalArgumentException("Radix must be between 2 and 62");
        }
        if (radix <= 36) {
            return fromBase(value, radix);
        }

        boolean negative = value.startsWith("-");
        int start = negative ? 1 : 0;

        long result = 0;
        for (int i = start; i < value.length(); i++) {
            char c = value.charAt(i);
            int digit = digitValue(c);
            if (digit >= radix) {
                throw new NumberFormatException("Invalid digit for radix " + radix + ": " + c);
            }
            result = Math.addExact(Math.multiplyExact(result, radix), digit);
        }

        return negative ? -result : result;
    }

    private static int digitValue(char c) {
        if (c >= '0' && c <= '9') return c - '0';
        if (c >= 'A' && c <= 'Z') return c - 'A' + 10;
        if (c >= 'a' && c <= 'z') return c - 'a' + 36;
        throw new NumberFormatException("Invalid digit: " + c);
    }

    // ==================== 进制间转换 ====================

    /**
     * Converts binary to hexadecimal
     * 二进制转十六进制
     */
    public static String binaryToHex(String binary) {
        return decimalToHexadecimal(binaryToDecimal(binary));
    }

    /**
     * Converts hexadecimal to binary
     * 十六进制转二进制
     */
    public static String hexToBinary(String hex) {
        return decimalToBinary(hexadecimalToDecimal(hex));
    }

    /**
     * Converts octal to hexadecimal
     * 八进制转十六进制
     */
    public static String octalToHex(String octal) {
        return decimalToHexadecimal(octalToDecimal(octal));
    }

    /**
     * Converts hexadecimal to octal
     * 十六进制转八进制
     */
    public static String hexToOctal(String hex) {
        return decimalToOctal(hexadecimalToDecimal(hex));
    }

    /**
     * General radix conversion
     * 通用进制转换
     */
    public static String convert(String value, int sourceRadix, int targetRadix) {
        long decimal = fromBase(value, sourceRadix);
        return toBase(decimal, targetRadix);
    }

    // ==================== 格式化 ====================

    /**
     * Formats binary (groups of 4)
     * 格式化二进制（每 4 位一组）
     */
    public static String formatBinary(long value) {
        String binary = decimalToBinary(value);
        return formatWithSeparator(binary, 4, "_");
    }

    /**
     * Formats hexadecimal (groups of 2)
     * 格式化十六进制（每 2 位一组）
     */
    public static String formatHex(long value) {
        String hex = decimalToHexadecimal(value);
        return formatWithSeparator(hex, 2, " ");
    }

    private static String formatWithSeparator(String str, int groupSize, String separator) {
        int len = str.length();
        StringBuilder sb = new StringBuilder();
        int start = len % groupSize;
        if (start > 0) {
            sb.append(str, 0, start);
        }
        for (int i = start; i < len; i += groupSize) {
            if (!sb.isEmpty()) {
                sb.append(separator);
            }
            sb.append(str, i, Math.min(i + groupSize, len));
        }
        return sb.toString();
    }

    // ==================== 验证 ====================

    /**
     * Checks if the string is a valid binary string
     * 验证是否为有效的二进制字符串
     */
    public static boolean isBinary(String str) {
        if (str == null || str.isEmpty()) return false;
        for (char c : str.toCharArray()) {
            if (c != '0' && c != '1') return false;
        }
        return true;
    }

    /**
     * Checks if the string is a valid octal string
     * 验证是否为有效的八进制字符串
     */
    public static boolean isOctal(String str) {
        if (str == null || str.isEmpty()) return false;
        for (char c : str.toCharArray()) {
            if (c < '0' || c > '7') return false;
        }
        return true;
    }

    /**
     * Checks if the string is a valid hexadecimal string
     * 验证是否为有效的十六进制字符串
     */
    public static boolean isHexadecimal(String str) {
        if (str == null || str.isEmpty()) return false;
        for (char c : str.toCharArray()) {
            if (!OpenChar.isHexDigit(c)) return false;
        }
        return true;
    }
}
