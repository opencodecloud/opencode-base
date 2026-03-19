package cloud.opencode.base.core;

import java.util.HexFormat;

/**
 * Hexadecimal Utility Class - Hex encoding, decoding and validation
 * 十六进制工具类 - 十六进制编码、解码和验证
 *
 * <p>Provides hexadecimal encoding and decoding operations.</p>
 * <p>提供十六进制编解码功能。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Encoding (encodeHex, encodeHexUpper, byteToHex) - 编码</li>
 *   <li>Decoding (decodeHex from String/char[]) - 解码</li>
 *   <li>Validation (isHexNumber, isHexString) - 验证</li>
 *   <li>Formatting (format with spaces, normalize) - 格式化</li>
 *   <li>Integer conversion (toHex, toInt, toLong) - 整数转换</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Encoding - 编码
 * String hex = OpenHex.encodeHex(bytes);           // "48656c6c6f"
 * String upper = OpenHex.encodeHexUpper(bytes);    // "48656C6C6F"
 *
 * // Decoding - 解码
 * byte[] data = OpenHex.decodeHex("48656c6c6f");
 *
 * // Validation - 验证
 * boolean valid = OpenHex.isHexString("0f1a2b");   // true
 *
 * // Formatting - 格式化
 * String formatted = OpenHex.format("48656c6c6f"); // "48 65 6c 6c 6f"
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是 (无状态)</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public final class OpenHex {

    private OpenHex() {
    }

    private static final HexFormat HEX_LOWER = HexFormat.of();
    private static final HexFormat HEX_UPPER = HexFormat.of().withUpperCase();

    // ==================== 编码 ====================

    /**
     * Converts byte array to lowercase hex string
     * 字节数组转十六进制字符串（小写）
     */
    public static String encodeHex(byte[] data) {
        if (data == null) {
            return null;
        }
        return HEX_LOWER.formatHex(data);
    }

    /**
     * Converts byte array to uppercase hex string
     * 字节数组转十六进制字符串（大写）
     */
    public static String encodeHexUpper(byte[] data) {
        if (data == null) {
            return null;
        }
        return HEX_UPPER.formatHex(data);
    }

    /**
     * Converts byte array to hex char array
     * 字节数组转十六进制字符数组
     */
    public static char[] encodeHexChars(byte[] data) {
        return encodeHexChars(data, true);
    }

    /**
     * Converts byte array to hex char array
     * 字节数组转十六进制字符数组
     */
    public static char[] encodeHexChars(byte[] data, boolean toLowerCase) {
        if (data == null) {
            return null;
        }
        HexFormat fmt = toLowerCase ? HEX_LOWER : HEX_UPPER;
        return fmt.formatHex(data).toCharArray();
    }

    /**
     * Converts a single byte to a hex string
     * 单字节转十六进制字符串
     */
    public static String byteToHex(byte b) {
        return HEX_LOWER.toHexDigits(b);
    }

    // ==================== 解码 ====================

    /**
     * Converts hex string to byte array
     * 十六进制字符串转字节数组
     */
    public static byte[] decodeHex(String hex) {
        if (hex == null) {
            return null;
        }
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException("Hex string must have even length");
        }
        try {
            return HEX_LOWER.parseHex(hex);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid hex character in: " + hex, e);
        }
    }

    /**
     * Converts hex char array to byte array
     * 十六进制字符数组转字节数组
     */
    public static byte[] decodeHex(char[] hex) {
        if (hex == null) {
            return null;
        }
        if (hex.length % 2 != 0) {
            throw new IllegalArgumentException("Hex array must have even length");
        }
        try {
            return HEX_LOWER.parseHex(new String(hex));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid hex character in array", e);
        }
    }

    // ==================== 验证 ====================

    /**
     * Checks whether the string is a hex number
     * 判断是否为十六进制数
     */
    public static boolean isHexNumber(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        // 处理 0x 或 0X 前缀
        int start = 0;
        if (str.startsWith("0x") || str.startsWith("0X")) {
            start = 2;
        } else if (str.startsWith("-0x") || str.startsWith("-0X")) {
            start = 3;
        } else if (str.startsWith("-")) {
            start = 1;
        }
        if (start >= str.length()) {
            return false;
        }
        for (int i = start; i < str.length(); i++) {
            if (!OpenChar.isHexDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks whether the string is a valid hex string
     * 判断是否为有效的十六进制字符串
     */
    public static boolean isHexString(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        for (int i = 0; i < str.length(); i++) {
            if (!OpenChar.isHexDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    // ==================== 工具方法 ====================

    /**
     * Formats hex string with spaces between each byte pair
     * 格式化十六进制字符串（每两位加空格）
     */
    public static String format(String hex) {
        if (hex == null || hex.length() <= 2) {
            return hex;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hex.length(); i += 2) {
            if (!sb.isEmpty()) {
                sb.append(' ');
            }
            sb.append(hex, i, Math.min(i + 2, hex.length()));
        }
        return sb.toString();
    }

    /**
     * Removes spaces and separators from a hex string
     * 移除十六进制字符串中的空格和分隔符
     */
    public static String normalize(String hex) {
        if (hex == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hex.length(); i++) {
            char c = hex.charAt(i);
            if (OpenChar.isHexDigit(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Converts an integer to a hex string
     * 整数转十六进制字符串
     */
    public static String toHex(int value) {
        return Integer.toHexString(value);
    }

    /**
     * Converts a long to a hex string
     * 长整数转十六进制字符串
     */
    public static String toHex(long value) {
        return Long.toHexString(value);
    }

    /**
     * Converts a hex string to an integer
     * 十六进制字符串转整数
     */
    public static int toInt(String hex) {
        return Integer.parseInt(normalize(hex), 16);
    }

    /**
     * Converts a hex string to a long
     * 十六进制字符串转长整数
     */
    public static long toLong(String hex) {
        return Long.parseLong(normalize(hex), 16);
    }
}
