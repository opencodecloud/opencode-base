package cloud.opencode.base.core.codec;

import java.util.Base64;
import java.util.Objects;

/**
 * Base64 Codec - Standard Base64 encoding/decoding (RFC 4648)
 * Base64 编解码器 - 标准 Base64 编解码（RFC 4648）
 *
 * <p>Delegates to JDK {@link java.util.Base64}. Supports standard, URL-safe, and no-padding modes.</p>
 * <p>委托给 JDK {@link java.util.Base64}。支持标准、URL 安全和无填充模式。</p>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless, JDK encoder/decoder are thread-safe) - 线程安全: 是</li>
 *   <li>Null-safe: null input throws {@link NullPointerException} - 空值: null 输入抛出 NPE</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see OpenCodec#base64()
 * @see OpenCodec#base64UrlSafe()
 * @see OpenCodec#base64NoPadding()
 * @since JDK 25, opencode-base-core V1.0.3
 */
final class Base64Codec implements Codec<byte[], String> {

    static final Base64Codec STANDARD = new Base64Codec(
            Base64.getEncoder(), Base64.getDecoder(), false);
    static final Base64Codec URL_SAFE = new Base64Codec(
            Base64.getUrlEncoder().withoutPadding(), Base64.getUrlDecoder(), false);
    static final Base64Codec NO_PADDING = new Base64Codec(
            Base64.getEncoder().withoutPadding(), Base64.getDecoder(), true);

    private final Base64.Encoder encoder;
    private final Base64.Decoder decoder;
    private final boolean repadOnDecode;

    private Base64Codec(Base64.Encoder encoder, Base64.Decoder decoder, boolean repadOnDecode) {
        this.encoder = encoder;
        this.decoder = decoder;
        this.repadOnDecode = repadOnDecode;
    }

    @Override
    public String encode(byte[] input) {
        Objects.requireNonNull(input, "input must not be null");
        return encoder.encodeToString(input);
    }

    @Override
    public byte[] decode(String output) {
        Objects.requireNonNull(output, "output must not be null");
        if (repadOnDecode) {
            // Re-pad if necessary for JDK compatibility
            int pad = (4 - output.length() % 4) % 4;
            if (pad > 0) {
                output = output + "=".repeat(pad);
            }
        }
        return decoder.decode(output);
    }
}
