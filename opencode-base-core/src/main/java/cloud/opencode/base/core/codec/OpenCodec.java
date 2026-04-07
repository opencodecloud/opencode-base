package cloud.opencode.base.core.codec;

/**
 * Codec Facade - Central entry point for encoding and decoding operations
 * 编解码门面类 - 编解码操作的统一入口
 *
 * <p>Provides static factory methods to obtain codec instances for various encoding schemes.
 * All returned codecs are thread-safe singletons.</p>
 * <p>提供静态工厂方法获取各种编码方案的编解码器实例。所有返回的编解码器均为线程安全的单例。</p>
 *
 * <p><strong>Supported Codecs | 支持的编解码器:</strong></p>
 * <ul>
 *   <li>Base64 (standard, URL-safe, no-padding) - Base64（标准、URL 安全、无填充）</li>
 *   <li>Hex (lowercase, uppercase) - 十六进制（小写、大写）</li>
 *   <li>URL (RFC 3986 percent-encoding) - URL（RFC 3986 百分号编码）</li>
 *   <li>HTML (OWASP entity escaping) - HTML（OWASP 实体转义）</li>
 *   <li>Base32 (RFC 4648, padding/no-padding) - Base32（RFC 4648，有/无填充）</li>
 *   <li>ASCII85 (Base85) - ASCII85（Base85）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Base64 roundtrip
 * Codec<byte[], String> b64 = OpenCodec.base64();
 * String encoded = b64.encode("Hello".getBytes());
 * byte[] decoded = b64.decode(encoded);
 *
 * // URL encoding
 * Codec<String, String> url = OpenCodec.url();
 * String safe = url.encode("hello world&foo=bar");
 * // "hello%20world%26foo%3Dbar"
 *
 * // HTML escaping
 * Codec<String, String> html = OpenCodec.html();
 * String escaped = html.encode("<script>alert('xss')</script>");
 * // "&lt;script&gt;alert(&#39;xss&#39;)&lt;/script&gt;"
 *
 * // Codec composition
 * Codec<byte[], String> hexThenUrl = OpenCodec.hex().andThen(OpenCodec.url());
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (all codecs are immutable singletons) - 线程安全: 是（所有编解码器为不可变单例）</li>
 *   <li>Null-safe: null input throws {@link NullPointerException} - 空值: null 输入抛出 NPE</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see Codec
 * @since JDK 25, opencode-base-core V1.0.3
 */
public final class OpenCodec {

    private OpenCodec() {
    }

    // ==================== Base64 ====================

    /**
     * Returns a standard Base64 codec (RFC 4648, with padding)
     * 返回标准 Base64 编解码器（RFC 4648，带填充）
     *
     * @return a Base64 codec | Base64 编解码器
     */
    public static Codec<byte[], String> base64() {
        return Base64Codec.STANDARD;
    }

    /**
     * Returns a URL-safe Base64 codec (RFC 4648, without padding)
     * 返回 URL 安全 Base64 编解码器（RFC 4648，无填充）
     *
     * @return a URL-safe Base64 codec | URL 安全 Base64 编解码器
     */
    public static Codec<byte[], String> base64UrlSafe() {
        return Base64Codec.URL_SAFE;
    }

    /**
     * Returns a Base64 codec without padding
     * 返回无填充 Base64 编解码器
     *
     * @return a no-padding Base64 codec | 无填充 Base64 编解码器
     */
    public static Codec<byte[], String> base64NoPadding() {
        return Base64Codec.NO_PADDING;
    }

    // ==================== Hex ====================

    /**
     * Returns a lowercase hex codec
     * 返回小写十六进制编解码器
     *
     * @return a hex codec | 十六进制编解码器
     */
    public static Codec<byte[], String> hex() {
        return HexCodec.LOWER;
    }

    /**
     * Returns an uppercase hex codec
     * 返回大写十六进制编解码器
     *
     * @return an uppercase hex codec | 大写十六进制编解码器
     */
    public static Codec<byte[], String> hexUpper() {
        return HexCodec.UPPER;
    }

    // ==================== URL ====================

    /**
     * Returns a URL codec (RFC 3986 percent-encoding)
     * 返回 URL 编解码器（RFC 3986 百分号编码）
     *
     * <p>Encodes all characters except unreserved characters (A-Z, a-z, 0-9, '-', '_', '.', '~').
     * Spaces are encoded as {@code %20} (not {@code +}).</p>
     *
     * @return a URL codec | URL 编解码器
     */
    public static Codec<String, String> url() {
        return UrlCodec.INSTANCE;
    }

    // ==================== HTML ====================

    /**
     * Returns an HTML entity codec (OWASP compliant)
     * 返回 HTML 实体编解码器（符合 OWASP 规范）
     *
     * <p>Escapes: {@code < > & " '}. Unescapes named entities and numeric character references.</p>
     *
     * @return an HTML codec | HTML 编解码器
     */
    public static Codec<String, String> html() {
        return HtmlCodec.INSTANCE;
    }

    // ==================== Base32 ====================

    /**
     * Returns a Base32 codec (RFC 4648, with padding)
     * 返回 Base32 编解码器（RFC 4648，带填充）
     *
     * @return a Base32 codec | Base32 编解码器
     */
    public static Codec<byte[], String> base32() {
        return Base32Codec.WITH_PADDING;
    }

    /**
     * Returns a Base32 codec without padding
     * 返回无填充 Base32 编解码器
     *
     * @return a no-padding Base32 codec | 无填充 Base32 编解码器
     */
    public static Codec<byte[], String> base32NoPadding() {
        return Base32Codec.NO_PADDING;
    }

    // ==================== ASCII85 ====================

    /**
     * Returns an ASCII85 (Base85) codec
     * 返回 ASCII85（Base85）编解码器
     *
     * @return an ASCII85 codec | ASCII85 编解码器
     */
    public static Codec<byte[], String> ascii85() {
        return Ascii85Codec.INSTANCE;
    }
}
