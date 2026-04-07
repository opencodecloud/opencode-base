package cloud.opencode.base.core.codec;

import java.util.HexFormat;
import java.util.Objects;

/**
 * Hex Codec - Hexadecimal encoding/decoding
 * 十六进制编解码器
 *
 * <p>Delegates to JDK {@link HexFormat}. Produces lowercase hex by default.</p>
 * <p>委托给 JDK {@link HexFormat}。默认输出小写十六进制。</p>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (HexFormat is immutable and thread-safe) - 线程安全: 是</li>
 *   <li>Null-safe: null input throws {@link NullPointerException} - 空值: null 输入抛出 NPE</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see OpenCodec#hex()
 * @since JDK 25, opencode-base-core V1.0.3
 */
final class HexCodec implements Codec<byte[], String> {

    static final HexCodec LOWER = new HexCodec(HexFormat.of());
    static final HexCodec UPPER = new HexCodec(HexFormat.of().withUpperCase());

    private final HexFormat hexFormat;

    private HexCodec(HexFormat hexFormat) {
        this.hexFormat = hexFormat;
    }

    @Override
    public String encode(byte[] input) {
        Objects.requireNonNull(input, "input must not be null");
        return hexFormat.formatHex(input);
    }

    @Override
    public byte[] decode(String output) {
        Objects.requireNonNull(output, "output must not be null");
        if (output.length() % 2 != 0) {
            throw new IllegalArgumentException("Hex string must have even length");
        }
        return hexFormat.parseHex(output);
    }
}
