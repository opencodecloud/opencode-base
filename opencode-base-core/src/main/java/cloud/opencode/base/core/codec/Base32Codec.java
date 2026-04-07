package cloud.opencode.base.core.codec;

import java.util.Objects;

/**
 * Base32 Codec - RFC 4648 Base32 encoding/decoding
 * Base32 编解码器 - RFC 4648 Base32 编解码
 *
 * <p>Implements the standard Base32 alphabet (A-Z, 2-7) with optional padding.
 * Both padding and no-padding modes are supported.</p>
 * <p>使用标准 Base32 字母表（A-Z、2-7），支持有填充和无填充模式。</p>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 *   <li>Null-safe: null input throws {@link NullPointerException} - 空值: null 输入抛出 NPE</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc4648#section-6">RFC 4648 Section 6</a>
 * @see OpenCodec#base32()
 * @see OpenCodec#base32NoPadding()
 * @since JDK 25, opencode-base-core V1.0.3
 */
final class Base32Codec implements Codec<byte[], String> {

    static final Base32Codec WITH_PADDING = new Base32Codec(true);
    static final Base32Codec NO_PADDING = new Base32Codec(false);

    private static final char[] ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".toCharArray();
    private static final int[] DECODE_TABLE = new int[128];

    static {
        java.util.Arrays.fill(DECODE_TABLE, -1);
        for (int i = 0; i < ALPHABET.length; i++) {
            DECODE_TABLE[ALPHABET[i]] = i;
            // Support lowercase input
            if (ALPHABET[i] >= 'A' && ALPHABET[i] <= 'Z') {
                DECODE_TABLE[ALPHABET[i] + 32] = i;
            }
        }
    }

    private final boolean padding;

    private Base32Codec(boolean padding) {
        this.padding = padding;
    }

    @Override
    public String encode(byte[] input) {
        Objects.requireNonNull(input, "input must not be null");
        if (input.length == 0) {
            return "";
        }
        // Each 5-byte group produces 8 chars; ceiling division (long to prevent overflow)
        long rawLen = ((long) input.length * 8 + 4) / 5;
        if (padding) {
            rawLen = ((rawLen + 7) / 8) * 8;
        }
        if (rawLen > Integer.MAX_VALUE - 8) {
            throw new IllegalArgumentException("Input too large to Base32-encode: " + input.length + " bytes");
        }
        int outputLen = (int) rawLen;
        char[] result = new char[outputLen];
        int pos = 0;
        int i = 0;
        // Process full 5-byte groups
        while (i + 5 <= input.length) {
            long block = ((long) (input[i] & 0xFF) << 32)
                    | ((long) (input[i + 1] & 0xFF) << 24)
                    | ((long) (input[i + 2] & 0xFF) << 16)
                    | ((long) (input[i + 3] & 0xFF) << 8)
                    | (input[i + 4] & 0xFF);
            result[pos++] = ALPHABET[(int) ((block >> 35) & 0x1F)];
            result[pos++] = ALPHABET[(int) ((block >> 30) & 0x1F)];
            result[pos++] = ALPHABET[(int) ((block >> 25) & 0x1F)];
            result[pos++] = ALPHABET[(int) ((block >> 20) & 0x1F)];
            result[pos++] = ALPHABET[(int) ((block >> 15) & 0x1F)];
            result[pos++] = ALPHABET[(int) ((block >> 10) & 0x1F)];
            result[pos++] = ALPHABET[(int) ((block >> 5) & 0x1F)];
            result[pos++] = ALPHABET[(int) (block & 0x1F)];
            i += 5;
        }
        // Handle remaining bytes
        int remaining = input.length - i;
        if (remaining > 0) {
            long block = 0;
            for (int j = 0; j < remaining; j++) {
                block |= ((long) (input[i + j] & 0xFF)) << (32 - j * 8);
            }
            int charCount = switch (remaining) {
                case 1 -> 2;
                case 2 -> 4;
                case 3 -> 5;
                case 4 -> 7;
                default -> 0;
            };
            for (int j = 0; j < charCount; j++) {
                result[pos++] = ALPHABET[(int) ((block >> (35 - j * 5)) & 0x1F)];
            }
            if (padding) {
                while (pos < result.length) {
                    result[pos++] = '=';
                }
            }
        }
        return new String(result, 0, pos);
    }

    @Override
    public byte[] decode(String output) {
        Objects.requireNonNull(output, "output must not be null");
        if (output.isEmpty()) {
            return new byte[0];
        }
        // Strip padding
        int len = output.length();
        while (len > 0 && output.charAt(len - 1) == '=') {
            len--;
        }
        int byteCount = len * 5 / 8;
        byte[] result = new byte[byteCount];
        int bitBuffer = 0;
        int bitsInBuffer = 0;
        int pos = 0;
        for (int i = 0; i < len; i++) {
            char c = output.charAt(i);
            if (c >= 128 || DECODE_TABLE[c] < 0) {
                throw new IllegalArgumentException(
                        "Invalid Base32 character '" + c + "' at index " + i);
            }
            bitBuffer = ((bitBuffer << 5) | DECODE_TABLE[c]) & 0x7FFFFFFF;
            bitsInBuffer += 5;
            if (bitsInBuffer >= 8) {
                bitsInBuffer -= 8;
                result[pos++] = (byte) ((bitBuffer >> bitsInBuffer) & 0xFF);
                bitBuffer &= (1 << bitsInBuffer) - 1;
            }
        }
        return result;
    }
}
