package cloud.opencode.base.core;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Base64 Encoding/Decoding Utility Class - Standard, URL-safe and MIME encodings
 * Base64 编解码工具类 - 标准、URL 安全和 MIME 编码
 *
 * <p>Based on JDK java.util.Base64, provides three encoding modes: standard, URL-safe and MIME.</p>
 * <p>基于 JDK java.util.Base64，提供标准、URL 安全和 MIME 三种编码方式。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Standard encoding (encode, decode) - 标准编码</li>
 *   <li>URL-safe encoding (encodeUrlSafe, decodeUrlSafe) - URL 安全编码</li>
 *   <li>MIME encoding (encodeMime, decodeMime) - MIME 编码</li>
 *   <li>No-padding encoding (encodeNoPadding) - 无填充编码</li>
 *   <li>Validation (isBase64, isBase64UrlSafe) - 验证</li>
 *   <li>Stream wrapping (encodingWrap, decodingWrap) - 流包装</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Standard encoding - 标准编码
 * String encoded = OpenBase64.encode("Hello");
 * String decoded = OpenBase64.decodeToString(encoded);
 *
 * // URL-safe encoding - URL 安全编码
 * String urlSafe = OpenBase64.encodeUrlSafe(bytes);
 * byte[] data = OpenBase64.decodeUrlSafe(urlSafe);
 *
 * // Validation - 验证
 * boolean valid = OpenBase64.isBase64(str);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless, uses JDK encoders) - 线程安全: 是 (无状态, 使用 JDK 编码器)</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public final class OpenBase64 {

    private OpenBase64() {
    }

    // ==================== 标准 Base64 编码 ====================

    /**
     * Encodes a byte array to a Base64 string
     * 将字节数组编码为 Base64 字符串
     */
    public static String encode(byte[] data) {
        if (data == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(data);
    }

    /**
     * Encodes a string to Base64 (using UTF-8)
     * 将字符串编码为 Base64（使用 UTF-8）
     */
    public static String encode(String data) {
        return encode(data, StandardCharsets.UTF_8);
    }

    /**
     * Encodes a string to Base64 (with specified charset)
     * 将字符串编码为 Base64（指定字符集）
     */
    public static String encode(String data, Charset charset) {
        if (data == null) {
            return null;
        }
        return encode(data.getBytes(charset));
    }

    /**
     * Encodes a byte array to a Base64 byte array
     * 将字节数组编码为 Base64 字节数组
     */
    public static byte[] encodeToBytes(byte[] data) {
        if (data == null) {
            return null;
        }
        return Base64.getEncoder().encode(data);
    }

    // ==================== 标准 Base64 解码 ====================

    /**
     * Decodes a Base64 string to a byte array
     * 将 Base64 字符串解码为字节数组
     */
    public static byte[] decode(String base64) {
        if (base64 == null) {
            return null;
        }
        return Base64.getDecoder().decode(base64);
    }

    /**
     * Decodes a Base64 string to a string (using UTF-8)
     * 将 Base64 字符串解码为字符串（使用 UTF-8）
     */
    public static String decodeToString(String base64) {
        return decodeToString(base64, StandardCharsets.UTF_8);
    }

    /**
     * Decodes a Base64 string to a string (with specified charset)
     * 将 Base64 字符串解码为字符串（指定字符集）
     */
    public static String decodeToString(String base64, Charset charset) {
        byte[] bytes = decode(base64);
        return bytes != null ? new String(bytes, charset) : null;
    }

    /**
     * Decodes a Base64 byte array
     * 将 Base64 字节数组解码
     */
    public static byte[] decode(byte[] base64Bytes) {
        if (base64Bytes == null) {
            return null;
        }
        return Base64.getDecoder().decode(base64Bytes);
    }

    // ==================== URL 安全 Base64 ====================

    /**
     * URL 安全编码（使用 - 和 _ 替代 + 和 /，无填充）
     */
    public static String encodeUrlSafe(byte[] data) {
        if (data == null) {
            return null;
        }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    /**
     * URL 安全编码字符串
     */
    public static String encodeUrlSafe(String data) {
        if (data == null) {
            return null;
        }
        return encodeUrlSafe(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * URL 安全解码
     */
    public static byte[] decodeUrlSafe(String base64) {
        if (base64 == null) {
            return null;
        }
        return Base64.getUrlDecoder().decode(base64);
    }

    /**
     * URL 安全解码为字符串
     */
    public static String decodeUrlSafeToString(String base64) {
        byte[] bytes = decodeUrlSafe(base64);
        return bytes != null ? new String(bytes, StandardCharsets.UTF_8) : null;
    }

    // ==================== MIME Base64 ====================

    /**
     * MIME 编码（每 76 字符换行）
     */
    public static String encodeMime(byte[] data) {
        if (data == null) {
            return null;
        }
        return Base64.getMimeEncoder().encodeToString(data);
    }

    /**
     * MIME 解码
     */
    public static byte[] decodeMime(String base64) {
        if (base64 == null) {
            return null;
        }
        return Base64.getMimeDecoder().decode(base64);
    }

    // ==================== 无填充编码 ====================

    /**
     * Encodes
     * 编码但不添加填充字符 =
     */
    public static String encodeNoPadding(byte[] data) {
        if (data == null) {
            return null;
        }
        return Base64.getEncoder().withoutPadding().encodeToString(data);
    }

    /**
     * URL 安全编码且无填充
     */
    public static String encodeUrlSafeNoPadding(byte[] data) {
        return encodeUrlSafe(data); // URL safe 默认就是无填充
    }

    // ==================== 验证 ====================

    /**
     * Checks if the string is valid standard Base64 encoding (without decoding).
     * 检查字符串是否为有效的标准 Base64 编码（不执行解码）。
     *
     * <p>Validates that the string contains only valid Base64 characters (A-Z, a-z, 0-9, +, /, =)
     * and that the non-whitespace content length is a multiple of 4. Whitespace characters
     * (spaces, tabs, CR, LF) are ignored to support MIME-formatted input.</p>
     * <p>验证字符串仅包含有效的 Base64 字符（A-Z、a-z、0-9、+、/、=），
     * 且去除空白后的内容长度为 4 的倍数。忽略空白字符（空格、制表符、CR、LF）以支持 MIME 格式输入。</p>
     *
     * @param str the string to check | 要检查的字符串
     * @return true if the string is valid Base64 | 如果字符串是有效的 Base64 则返回 true
     * @see #isBase64UrlSafe(String)
     */
    public static boolean isBase64(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        int contentLen = 0;
        boolean paddingStarted = false;
        int paddingCount = 0;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            // Skip whitespace (for MIME compatibility)
            if (c == ' ' || c == '\t' || c == '\r' || c == '\n') {
                continue;
            }
            if (c == '=') {
                paddingStarted = true;
                paddingCount++;
                contentLen++;
                if (paddingCount > 2) {
                    return false;
                }
            } else if (paddingStarted) {
                // Non-padding character after padding
                return false;
            } else if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')
                    || (c >= '0' && c <= '9') || c == '+' || c == '/') {
                contentLen++;
            } else {
                return false;
            }
        }
        return contentLen > 0 && contentLen % 4 == 0;
    }

    /**
     * Checks if the string is valid URL-safe Base64 encoding (without decoding).
     * 检查字符串是否为有效的 URL 安全 Base64 编码（不执行解码）。
     *
     * <p>Validates that the string contains only valid URL-safe Base64 characters
     * (A-Z, a-z, 0-9, -, _). URL-safe Base64 typically omits padding ('='),
     * so length is not required to be a multiple of 4.</p>
     * <p>验证字符串仅包含有效的 URL 安全 Base64 字符（A-Z、a-z、0-9、-、_）。
     * URL 安全 Base64 通常省略填充字符（'='），因此长度不要求为 4 的倍数。</p>
     *
     * @param str the string to check | 要检查的字符串
     * @return true if the string is valid URL-safe Base64 | 如果字符串是有效的 URL 安全 Base64 则返回 true
     * @see #isBase64(String)
     */
    public static boolean isBase64UrlSafe(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')
                    || (c >= '0' && c <= '9') || c == '-' || c == '_') {
                continue;
            }
            return false;
        }
        return true;
    }

    // ==================== 流式编解码 ====================

    /**
     * Creates
     * 创建 Base64 编码输出流包装器
     */
    public static OutputStream encodingWrap(OutputStream out) {
        return Base64.getEncoder().wrap(out);
    }

    /**
     * Creates
     * 创建 Base64 解码输入流包装器
     */
    public static InputStream decodingWrap(InputStream in) {
        return Base64.getDecoder().wrap(in);
    }

    /**
     * Creates
     * 创建 URL 安全 Base64 编码输出流包装器
     */
    public static OutputStream encodingWrapUrlSafe(OutputStream out) {
        return Base64.getUrlEncoder().wrap(out);
    }

    /**
     * Creates
     * 创建 URL 安全 Base64 解码输入流包装器
     */
    public static InputStream decodingWrapUrlSafe(InputStream in) {
        return Base64.getUrlDecoder().wrap(in);
    }
}
