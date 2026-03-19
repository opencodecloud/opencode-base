package cloud.opencode.base.crypto.codec;

import java.util.Base64;

/**
 * Base64URL encoding and decoding utility - URL-safe Base64 codec for JWT and web tokens
 * Base64URL 编解码工具类 - 用于 JWT 和 Web 令牌的 URL 安全 Base64 编解码
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>URL-safe Base64 encoding and decoding - URL 安全的 Base64 编码和解码</li>
 *   <li>Without padding option - 无填充选项</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * String encoded = Base64UrlCodec.encode(bytes);
 * byte[] decoded = Base64UrlCodec.decode(encoded);
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
public final class Base64UrlCodec {

    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder();
    private static final Base64.Encoder URL_ENCODER_NO_PAD = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();

    private Base64UrlCodec() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Encode byte array to Base64URL string with padding
     * 将字节数组编码为带填充的 Base64URL 字符串
     *
     * @param data byte array to encode
     * @return Base64URL encoded string
     * @throws NullPointerException if data is null
     */
    public static String encode(byte[] data) {
        if (data == null) {
            throw new NullPointerException("Data cannot be null");
        }
        return URL_ENCODER.encodeToString(data);
    }

    /**
     * Decode Base64URL string to byte array
     * 将 Base64URL 字符串解码为字节数组
     *
     * @param data Base64URL encoded string
     * @return decoded byte array
     * @throws NullPointerException if data is null
     * @throws IllegalArgumentException if data is not valid Base64URL
     */
    public static byte[] decode(String data) {
        if (data == null) {
            throw new NullPointerException("Data cannot be null");
        }
        return URL_DECODER.decode(data);
    }

    /**
     * Encode byte array to Base64URL string without padding (standard for JWT)
     * 将字节数组编码为无填充的 Base64URL 字符串（JWT 标准格式）
     *
     * @param data byte array to encode
     * @return Base64URL encoded string without padding
     * @throws NullPointerException if data is null
     */
    public static String encodeNoPadding(byte[] data) {
        if (data == null) {
            throw new NullPointerException("Data cannot be null");
        }
        return URL_ENCODER_NO_PAD.encodeToString(data);
    }

    /**
     * Decode Base64URL string without padding to byte array
     * 将无填充的 Base64URL 字符串解码为字节数组
     *
     * @param data Base64URL encoded string without padding
     * @return decoded byte array
     * @throws NullPointerException if data is null
     * @throws IllegalArgumentException if data is not valid Base64URL
     */
    public static byte[] decodeNoPadding(String data) {
        if (data == null) {
            throw new NullPointerException("Data cannot be null");
        }
        return URL_DECODER.decode(data);
    }
}
