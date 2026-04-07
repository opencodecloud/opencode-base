package cloud.opencode.base.core.codec;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * URL Codec - RFC 3986 percent-encoding
 * URL 编解码器 - RFC 3986 百分号编码
 *
 * <p>Encodes all characters except unreserved characters (A-Z, a-z, 0-9, '-', '_', '.', '~')
 * using percent-encoding. Spaces are encoded as {@code %20} (not {@code +}).</p>
 * <p>对除未保留字符（A-Z、a-z、0-9、'-'、'_'、'.'、'~'）外的所有字符进行百分号编码。
 * 空格编码为 {@code %20}（而非 {@code +}）。</p>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 *   <li>Null-safe: null input throws {@link NullPointerException} - 空值: null 输入抛出 NPE</li>
 *   <li>No double-encoding: only raw characters are encoded - 无双重编码: 仅编码原始字符</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc3986#section-2.1">RFC 3986 Section 2.1</a>
 * @see OpenCodec#url()
 * @since JDK 25, opencode-base-core V1.0.3
 */
final class UrlCodec implements Codec<String, String> {

    static final UrlCodec INSTANCE = new UrlCodec();

    private static final char[] HEX_UPPER = "0123456789ABCDEF".toCharArray();

    private UrlCodec() {
    }

    @Override
    public String encode(String input) {
        Objects.requireNonNull(input, "input must not be null");
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
        StringBuilder sb = null;
        int mark = 0;
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            if (isUnreserved(b)) {
                continue;
            }
            if (sb == null) {
                sb = new StringBuilder(Math.min(bytes.length * 3, Integer.MAX_VALUE - 8));
            }
            if (i > mark) {
                sb.append(new String(bytes, mark, i - mark, StandardCharsets.UTF_8));
            }
            sb.append('%');
            sb.append(HEX_UPPER[(b >> 4) & 0x0F]);
            sb.append(HEX_UPPER[b & 0x0F]);
            mark = i + 1;
        }
        if (sb == null) {
            return input;
        }
        if (mark < bytes.length) {
            sb.append(new String(bytes, mark, bytes.length - mark, StandardCharsets.UTF_8));
        }
        return sb.toString();
    }

    @Override
    public String decode(String output) {
        Objects.requireNonNull(output, "output must not be null");
        int percent = output.indexOf('%');
        if (percent < 0) {
            // Validate no non-ASCII characters even when there's no percent-encoding
            for (int j = 0; j < output.length(); j++) {
                if (output.charAt(j) > 0x7F) {
                    throw new IllegalArgumentException(
                            "Non-ASCII character U+" + Integer.toHexString(output.charAt(j))
                                    + " at index " + j + " must be percent-encoded");
                }
            }
            return output;
        }
        byte[] bytes = new byte[output.length()];
        int pos = 0;
        int i = 0;
        while (i < output.length()) {
            char c = output.charAt(i);
            if (c == '%') {
                if (i + 2 >= output.length()) {
                    throw new IllegalArgumentException(
                            "Incomplete percent-encoding at index " + i);
                }
                int hi = hexDigit(output.charAt(i + 1), i + 1);
                int lo = hexDigit(output.charAt(i + 2), i + 2);
                bytes[pos++] = (byte) ((hi << 4) | lo);
                i += 3;
            } else {
                if (c > 0x7F) {
                    throw new IllegalArgumentException(
                            "Non-ASCII character U+" + Integer.toHexString(c)
                                    + " at index " + i + " must be percent-encoded");
                }
                bytes[pos++] = (byte) c;
                i++;
            }
        }
        return new String(bytes, 0, pos, StandardCharsets.UTF_8);
    }

    private static boolean isUnreserved(byte b) {
        return (b >= 'A' && b <= 'Z') || (b >= 'a' && b <= 'z')
                || (b >= '0' && b <= '9')
                || b == '-' || b == '_' || b == '.' || b == '~';
    }

    private static int hexDigit(char c, int index) {
        if (c >= '0' && c <= '9') return c - '0';
        if (c >= 'A' && c <= 'F') return c - 'A' + 10;
        if (c >= 'a' && c <= 'f') return c - 'a' + 10;
        throw new IllegalArgumentException(
                "Invalid hex digit '" + c + "' at index " + index);
    }
}
