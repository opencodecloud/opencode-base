package cloud.opencode.base.core.codec;

import java.io.ByteArrayOutputStream;
import java.util.Objects;

/**
 * ASCII85 Codec - ASCII85 (Base85) encoding/decoding
 * ASCII85 编解码器 - ASCII85（Base85）编解码
 *
 * <p>Implements the btoa/Adobe variant of ASCII85 encoding. Encodes 4 bytes into 5 ASCII
 * characters in the range '!' (33) to 'u' (117). Supports the 'z' shortcut for
 * all-zero groups.</p>
 * <p>实现 btoa/Adobe 变体的 ASCII85 编码。将 4 字节编码为 5 个 ASCII 字符，
 * 范围从 '!'（33）到 'u'（117）。支持全零组的 'z' 快捷方式。</p>
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
 * @see OpenCodec#ascii85()
 * @since JDK 25, opencode-base-core V1.0.3
 */
final class Ascii85Codec implements Codec<byte[], String> {

    static final Ascii85Codec INSTANCE = new Ascii85Codec();

    private static final long[] POW85 = {
            85L * 85 * 85 * 85,
            85L * 85 * 85,
            85L * 85,
            85L,
            1L
    };

    private Ascii85Codec() {
    }

    @Override
    public String encode(byte[] input) {
        Objects.requireNonNull(input, "input must not be null");
        if (input.length == 0) {
            return "";
        }
        int capacity = (int) Math.min((long) input.length * 5 / 4 + 4, Integer.MAX_VALUE - 8);
        StringBuilder sb = new StringBuilder(capacity);
        int i = 0;
        // Process full 4-byte groups
        while (i + 4 <= input.length) {
            long value = ((long) (input[i] & 0xFF) << 24)
                    | ((long) (input[i + 1] & 0xFF) << 16)
                    | ((long) (input[i + 2] & 0xFF) << 8)
                    | (input[i + 3] & 0xFF);
            if (value == 0) {
                sb.append('z');
            } else {
                encodeGroup(sb, value, 5);
            }
            i += 4;
        }
        // Handle remaining bytes
        int remaining = input.length - i;
        if (remaining > 0) {
            long value = 0;
            for (int j = 0; j < remaining; j++) {
                value |= ((long) (input[i + j] & 0xFF)) << (24 - j * 8);
            }
            encodeGroup(sb, value, remaining + 1);
        }
        return sb.toString();
    }

    @Override
    public byte[] decode(String output) {
        Objects.requireNonNull(output, "output must not be null");
        if (output.isEmpty()) {
            return new byte[0];
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream(output.length() * 4 / 5);
        int i = 0;
        while (i < output.length()) {
            char c = output.charAt(i);
            if (c == 'z') {
                baos.write(0);
                baos.write(0);
                baos.write(0);
                baos.write(0);
                i++;
                continue;
            }
            // Collect up to 5 characters
            int groupLen = Math.min(5, output.length() - i);
            // Check if we have a partial group (< 5 chars)
            if (groupLen < 2) {
                throw new IllegalArgumentException(
                        "Invalid ASCII85: trailing single character at index " + i);
            }
            long value = 0;
            int actualLen = 0;
            for (int j = 0; j < groupLen; j++) {
                char ch = output.charAt(i + j);
                if (ch == 'z') {
                    throw new IllegalArgumentException(
                            "'z' shortcut must appear at start of a group, not at index " + (i + j));
                }
                if (ch < '!' || ch > 'u') {
                    throw new IllegalArgumentException(
                            "Invalid ASCII85 character '" + ch + "' at index " + (i + j));
                }
                value += (ch - '!') * POW85[j];
                actualLen++;
            }
            if (actualLen == 5 && value > 0xFFFFFFFFL) {
                throw new IllegalArgumentException(
                        "Invalid ASCII85 group at index " + i + ": decoded value exceeds 0xFFFFFFFF");
            }
            if (actualLen < 5) {
                // Pad with 'u' (84) for partial group
                for (int j = actualLen; j < 5; j++) {
                    value += 84 * POW85[j];
                }
            }
            int bytesToWrite = actualLen - 1;
            for (int j = 0; j < bytesToWrite; j++) {
                baos.write((int) ((value >> (24 - j * 8)) & 0xFF));
            }
            i += actualLen;
        }
        return baos.toByteArray();
    }

    private static void encodeGroup(StringBuilder sb, long value, int count) {
        char[] group = new char[5];
        for (int i = 4; i >= 0; i--) {
            group[i] = (char) ('!' + (int) (value % 85));
            value /= 85;
        }
        for (int i = 0; i < count; i++) {
            sb.append(group[i]);
        }
    }
}
