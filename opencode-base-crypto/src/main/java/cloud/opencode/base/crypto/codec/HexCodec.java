package cloud.opencode.base.crypto.codec;

import java.util.HexFormat;

/**
 * Hexadecimal encoding and decoding utility - Convert between byte arrays and hex strings
 * 十六进制编解码工具类 - 字节数组与十六进制字符串相互转换
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Hexadecimal encoding and decoding - 十六进制编码和解码</li>
 *   <li>Uppercase and lowercase support - 大小写支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * String hex = HexCodec.encode(bytes);
 * byte[] decoded = HexCodec.decode(hex);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
public final class HexCodec {

    private static final HexFormat HEX_LOWER = HexFormat.of();
    private static final HexFormat HEX_UPPER = HexFormat.of().withUpperCase();

    private HexCodec() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Encode byte array to lowercase hexadecimal string
     * 将字节数组编码为小写十六进制字符串
     *
     * @param data byte array to encode
     * @return lowercase hexadecimal string
     * @throws NullPointerException if data is null
     */
    public static String encode(byte[] data) {
        if (data == null) {
            throw new NullPointerException("Data cannot be null");
        }
        return HEX_LOWER.formatHex(data);
    }

    /**
     * Encode byte array to uppercase hexadecimal string
     * 将字节数组编码为大写十六进制字符串
     *
     * @param data byte array to encode
     * @return uppercase hexadecimal string
     * @throws NullPointerException if data is null
     */
    public static String encodeUpperCase(byte[] data) {
        if (data == null) {
            throw new NullPointerException("Data cannot be null");
        }
        return HEX_UPPER.formatHex(data);
    }

    /**
     * Decode hexadecimal string to byte array (case-insensitive)
     * 将十六进制字符串解码为字节数组（不区分大小写）
     *
     * @param hex hexadecimal string
     * @return decoded byte array
     * @throws NullPointerException if hex is null
     * @throws IllegalArgumentException if hex is not valid hexadecimal
     */
    public static byte[] decode(String hex) {
        if (hex == null) {
            throw new NullPointerException("Hex string cannot be null");
        }
        if (hex.isEmpty()) {
            return new byte[0];
        }
        try {
            return HEX_LOWER.parseHex(hex);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid hexadecimal string: " + hex, e);
        }
    }

    /**
     * Check if string is valid hexadecimal format
     * 检查字符串是否为有效的十六进制格式
     *
     * @param hex string to validate
     * @return true if valid hexadecimal, false otherwise
     */
    public static boolean isValidHex(String hex) {
        if (hex == null || hex.isEmpty()) {
            return false;
        }

        // Hex string must have even length
        if (hex.length() % 2 != 0) {
            return false;
        }

        // Check if all characters are valid hex digits
        for (int i = 0; i < hex.length(); i++) {
            char c = hex.charAt(i);
            if (!((c >= '0' && c <= '9') ||
                  (c >= 'a' && c <= 'f') ||
                  (c >= 'A' && c <= 'F'))) {
                return false;
            }
        }

        return true;
    }
}
