package cloud.opencode.base.email.protocol.mime;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MIME Encoding and Decoding Utility
 * MIME 编解码工具类
 *
 * <p>Provides RFC 2045 Base64 and Quoted-Printable encoding/decoding,
 * RFC 2047 encoded-word encoding/decoding for message headers,
 * and MIME boundary/message-id generation.</p>
 * <p>提供 RFC 2045 Base64 和 Quoted-Printable 编解码，
 * RFC 2047 编码字（encoded-word）编解码用于消息头，
 * 以及 MIME 边界/消息ID 生成。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Base64 MIME encoding (76-char line wrap) - Base64 MIME 编码（76字符换行）</li>
 *   <li>Quoted-Printable encoding/decoding (RFC 2045) - Quoted-Printable 编解码</li>
 *   <li>RFC 2047 encoded-word for non-ASCII headers - RFC 2047 编码字用于非ASCII邮件头</li>
 *   <li>Unique boundary and message-id generation - 唯一边界和消息ID生成</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: No (callers must provide non-null arguments) - 空值安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.3
 */
public final class MimeEncoder {

    /** RFC 2047 encoded-word pattern: =?charset?encoding?text?= */
    private static final Pattern ENCODED_WORD_PATTERN =
            Pattern.compile("=\\?([^?]+)\\?([BbQq])\\?([^?]*)\\?=");

    /** Hex digit lookup table for fast Quoted-Printable encoding. */
    private static final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();

    /** Maximum line length for Quoted-Printable encoding (RFC 2045). */
    private static final int QP_LINE_LENGTH = 76;

    private MimeEncoder() {
        // utility class
    }

    // ========== Base64 ==========

    /**
     * Encode binary data to MIME Base64 with 76-character line wrapping
     * 将二进制数据编码为 MIME Base64（76字符换行）
     *
     * @param data the data to encode | 要编码的数据
     * @return the Base64 encoded string | Base64 编码字符串
     */
    public static String encodeBase64(byte[] data) {
        return Base64.getMimeEncoder().encodeToString(data);
    }

    /**
     * Decode a MIME Base64 encoded string to binary data
     * 将 MIME Base64 编码字符串解码为二进制数据
     *
     * @param data the Base64 string to decode | 要解码的 Base64 字符串
     * @return the decoded bytes | 解码后的字节
     */
    public static byte[] decodeBase64(String data) {
        return Base64.getMimeDecoder().decode(data);
    }

    // ========== Quoted-Printable ==========

    /**
     * Encode text to Quoted-Printable encoding per RFC 2045
     * 按 RFC 2045 将文本编码为 Quoted-Printable 编码
     *
     * <p>Non-printable characters and '=' are encoded as =XX hex pairs.
     * Lines are soft-wrapped at 76 characters with '=' continuation.</p>
     * <p>不可打印字符和'='被编码为 =XX 十六进制对。
     * 行在76字符处用'='软换行。</p>
     *
     * @param text    the text to encode | 要编码的文本
     * @param charset the charset name for byte conversion | 字节转换的字符集名称
     * @return the Quoted-Printable encoded string | Quoted-Printable 编码字符串
     */
    public static String encodeQuotedPrintable(String text, String charset) {
        byte[] bytes = text.getBytes(Charset.forName(charset));
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        int lineLen = 0;

        for (byte b : bytes) {
            int unsigned = b & 0xFF;

            // Printable ASCII (33-126) except '=' (61) pass through;
            // also space (32) and tab (9) pass through unless at end of line
            boolean printable = (unsigned >= 33 && unsigned <= 126 && unsigned != 61)
                    || unsigned == 9 || unsigned == 32;

            if (unsigned == '\r' || unsigned == '\n') {
                // Pass through CRLF literally, reset line length
                sb.append((char) unsigned);
                if (unsigned == '\n') {
                    lineLen = 0;
                }
                continue;
            }

            String encoded;
            if (printable) {
                encoded = String.valueOf((char) unsigned);
            } else {
                encoded = hexEncode(unsigned);
            }

            // Soft line break if adding this token would exceed 76 chars
            // (need room for '=' soft break marker, so limit is 75 for content)
            if (lineLen + encoded.length() > QP_LINE_LENGTH - 1) {
                sb.append("=\r\n");
                lineLen = 0;
            }

            sb.append(encoded);
            lineLen += encoded.length();
        }

        // Trailing spaces/tabs at end of line must be encoded per RFC 2045.
        // We handle this by post-processing: replace trailing SP/HT before CRLF or EOF.
        return encodeTrailingWhitespace(sb.toString());
    }

    /**
     * Decode a Quoted-Printable encoded string
     * 解码 Quoted-Printable 编码字符串
     *
     * @param text the Quoted-Printable text to decode | 要解码的 Quoted-Printable 文本
     * @return the decoded string (UTF-8) | 解码后的字符串（UTF-8）
     */
    public static String decodeQuotedPrintable(String text) {
        return decodeQuotedPrintable(text, StandardCharsets.UTF_8);
    }

    /**
     * Decode a Quoted-Printable encoded string with the specified charset
     * 使用指定字符集解码 Quoted-Printable 编码字符串
     *
     * @param text    the Quoted-Printable text to decode | 要解码的 Quoted-Printable 文本
     * @param charset the charset for decoding | 解码字符集
     * @return the decoded string | 解码后的字符串
     */
    public static String decodeQuotedPrintable(String text, Charset charset) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(text.length());
        int i = 0;
        while (i < text.length()) {
            char c = text.charAt(i);
            if (c == '=') {
                if (i + 2 < text.length()) {
                    char c1 = text.charAt(i + 1);
                    char c2 = text.charAt(i + 2);
                    if (c1 == '\r' && c2 == '\n') {
                        // Soft line break - skip
                        i += 3;
                        continue;
                    }
                    if (isHexDigit(c1) && isHexDigit(c2)) {
                        int value = Character.digit(c1, 16) * 16 + Character.digit(c2, 16);
                        baos.write(value);
                        i += 3;
                        continue;
                    }
                } else if (i + 1 < text.length() && text.charAt(i + 1) == '\n') {
                    // Soft line break with bare LF
                    i += 2;
                    continue;
                }
                // Malformed =, pass through
                baos.write('=');
                i++;
            } else {
                // Write char bytes in UTF-8 context; for raw QP these are single-byte ASCII
                if (c <= 0x7F) {
                    baos.write(c);
                } else {
                    byte[] charBytes = String.valueOf(c).getBytes(charset);
                    baos.write(charBytes, 0, charBytes.length);
                }
                i++;
            }
        }
        return baos.toString(charset);
    }

    // ========== RFC 2047 Encoded Words ==========

    /**
     * Encode a header word using RFC 2047 Base64 encoding
     * 使用 RFC 2047 Base64 编码对邮件头字词进行编码
     *
     * <p>Produces encoded-words of the form {@code =?charset?B?base64?=}.
     * Only encodes if the word contains non-ASCII characters.</p>
     * <p>生成形如 {@code =?charset?B?base64?=} 的编码字。
     * 仅在字词包含非ASCII字符时进行编码。</p>
     *
     * @param word    the word to encode | 要编码的字词
     * @param charset the charset name (e.g., "UTF-8") | 字符集名称
     * @return the RFC 2047 encoded word, or the original if pure ASCII | 编码后的字词，纯ASCII则返回原文
     */
    public static String encodeWord(String word, String charset) {
        if (word == null || word.isEmpty()) {
            return word;
        }
        // Check if encoding is needed
        boolean needsEncoding = false;
        for (int i = 0; i < word.length(); i++) {
            if (word.charAt(i) > 127) {
                needsEncoding = true;
                break;
            }
        }
        if (!needsEncoding) {
            return word;
        }

        byte[] bytes = word.getBytes(Charset.forName(charset));
        String base64 = Base64.getEncoder().encodeToString(bytes);
        return "=?" + charset + "?B?" + base64 + "?=";
    }

    /**
     * Decode an RFC 2047 encoded-word string
     * 解码 RFC 2047 编码字字符串
     *
     * <p>Handles both B (Base64) and Q (Quoted-Printable) encodings.
     * Non-encoded text is returned as-is.</p>
     * <p>支持 B（Base64）和 Q（Quoted-Printable）编码。
     * 非编码文本原样返回。</p>
     *
     * @param encoded the encoded string (may contain multiple encoded-words) | 编码字符串
     * @return the decoded string | 解码后的字符串
     */
    public static String decodeWord(String encoded) {
        if (encoded == null || encoded.isEmpty()) {
            return encoded;
        }
        Matcher matcher = ENCODED_WORD_PATTERN.matcher(encoded);
        if (!matcher.find()) {
            return encoded;
        }

        StringBuilder result = new StringBuilder(encoded.length());
        int lastEnd = 0;
        matcher.reset();

        while (matcher.find()) {
            // Append text between encoded words, but skip whitespace-only gaps
            // between consecutive encoded-words per RFC 2047 section 6.2
            String gap = encoded.substring(lastEnd, matcher.start());
            if (lastEnd > 0 && gap.isBlank()) {
                // Drop whitespace between adjacent encoded-words
            } else {
                result.append(gap);
            }

            String charsetName = matcher.group(1);
            String encoding = matcher.group(2).toUpperCase();
            String text = matcher.group(3);
            Charset cs = Charset.forName(charsetName);

            if ("B".equals(encoding)) {
                byte[] decoded = Base64.getDecoder().decode(text);
                result.append(new String(decoded, cs));
            } else if ("Q".equals(encoding)) {
                result.append(decodeQEncoding(text, cs));
            }

            lastEnd = matcher.end();
        }

        // Append trailing text
        if (lastEnd < encoded.length()) {
            result.append(encoded.substring(lastEnd));
        }

        return result.toString();
    }

    // ========== Boundary & Message-ID ==========

    /**
     * Generate a unique MIME boundary string
     * 生成唯一的 MIME 边界字符串
     *
     * @return the boundary string (without leading "--") | 边界字符串（不含前导"--"）
     */
    public static String generateBoundary() {
        return "----=_Part_" + UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Generate a unique Message-ID header value
     * 生成唯一的 Message-ID 邮件头值
     *
     * @param domain the domain for the message-id (e.g., "example.com") | 消息ID的域名
     * @return the message-id in angle brackets (e.g., "&lt;uuid@domain&gt;") | 尖括号内的消息ID
     */
    public static String generateMessageId(String domain) {
        return "<" + UUID.randomUUID().toString().replace("-", "") + "@" + domain + ">";
    }

    // ========== Internal Helpers ==========

    /**
     * Encode trailing whitespace (SP/HT) before CRLF or at end of string
     * in Quoted-Printable.
     */
    private static String encodeTrailingWhitespace(String text) {
        StringBuilder sb = new StringBuilder(text.length());
        int len = text.length();
        for (int i = 0; i < len; i++) {
            char c = text.charAt(i);
            if ((c == ' ' || c == '\t') && isTrailingWhitespace(text, i)) {
                sb.append(hexEncode(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Check if whitespace at position i is trailing (followed by CRLF/LF or at end of string).
     */
    private static boolean isTrailingWhitespace(String text, int pos) {
        int next = pos + 1;
        while (next < text.length()) {
            char nc = text.charAt(next);
            if (nc == ' ' || nc == '\t') {
                next++;
                continue;
            }
            return nc == '\r' || nc == '\n';
        }
        // At end of string
        return true;
    }

    /**
     * Decode RFC 2047 Q-encoding (similar to QP but underscores represent spaces).
     */
    private static String decodeQEncoding(String text, Charset charset) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(text.length());
        int i = 0;
        while (i < text.length()) {
            char c = text.charAt(i);
            if (c == '_') {
                baos.write(' ');
                i++;
            } else if (c == '=' && i + 2 < text.length()) {
                char c1 = text.charAt(i + 1);
                char c2 = text.charAt(i + 2);
                if (isHexDigit(c1) && isHexDigit(c2)) {
                    int value = Character.digit(c1, 16) * 16 + Character.digit(c2, 16);
                    baos.write(value);
                    i += 3;
                } else {
                    baos.write(c);
                    i++;
                }
            } else {
                baos.write(c);
                i++;
            }
        }
        return baos.toString(charset);
    }

    /**
     * Fast hex encoding for a single byte value (0-255) as "=XX".
     */
    private static String hexEncode(int b) {
        return "=" + HEX_DIGITS[(b >> 4) & 0xF] + HEX_DIGITS[b & 0xF];
    }

    private static boolean isHexDigit(char c) {
        return (c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f');
    }
}
