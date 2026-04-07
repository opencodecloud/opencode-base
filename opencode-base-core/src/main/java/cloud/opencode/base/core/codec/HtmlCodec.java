package cloud.opencode.base.core.codec;

import java.util.Objects;

/**
 * HTML Codec - HTML entity escaping/unescaping (OWASP compliant)
 * HTML 编解码器 - HTML 实体转义/反转义（符合 OWASP 规范）
 *
 * <p>Escapes the 5 OWASP-recommended characters: {@code < > & " '}.
 * Unescaping supports named entities and both decimal ({@code &#60;}) and
 * hexadecimal ({@code &#x3C;}) numeric character references.</p>
 * <p>转义 OWASP 推荐的 5 个字符：{@code < > & " '}。
 * 反转义支持命名实体以及十进制（{@code &#60;}）和十六进制（{@code &#x3C;}）数字字符引用。</p>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 *   <li>Null-safe: null input throws {@link NullPointerException} - 空值: null 输入抛出 NPE</li>
 *   <li>XSS prevention: escapes all OWASP-recommended characters - XSS 防护: 转义所有 OWASP 推荐字符</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see OpenCodec#html()
 * @since JDK 25, opencode-base-core V1.0.3
 */
final class HtmlCodec implements Codec<String, String> {

    static final HtmlCodec INSTANCE = new HtmlCodec();

    private HtmlCodec() {
    }

    @Override
    public String encode(String input) {
        Objects.requireNonNull(input, "input must not be null");
        StringBuilder sb = null;
        int mark = 0;
        for (int i = 0; i < input.length(); i++) {
            String entity = switch (input.charAt(i)) {
                case '<' -> "&lt;";
                case '>' -> "&gt;";
                case '&' -> "&amp;";
                case '"' -> "&quot;";
                case '\'' -> "&#39;";
                default -> null;
            };
            if (entity != null) {
                if (sb == null) {
                    sb = new StringBuilder(input.length() + 16);
                }
                if (i > mark) {
                    sb.append(input, mark, i);
                }
                sb.append(entity);
                mark = i + 1;
            }
        }
        if (sb == null) {
            return input;
        }
        if (mark < input.length()) {
            sb.append(input, mark, input.length());
        }
        return sb.toString();
    }

    @Override
    public String decode(String output) {
        Objects.requireNonNull(output, "output must not be null");
        int ampIdx = output.indexOf('&');
        if (ampIdx < 0) {
            return output;
        }
        StringBuilder sb = new StringBuilder(output.length());
        int mark = 0;
        int i = ampIdx;
        while (i < output.length()) {
            if (output.charAt(i) != '&') {
                i++;
                continue;
            }
            sb.append(output, mark, i);
            int semi = output.indexOf(';', i + 1);
            if (semi < 0 || semi - i > 10) {
                // No semicolon found or entity too long, treat as literal
                sb.append('&');
                mark = i + 1;
                i++;
                continue;
            }
            String ref = output.substring(i + 1, semi);
            int decoded = decodeEntity(ref);
            if (decoded >= 0) {
                sb.appendCodePoint(decoded);
                mark = semi + 1;
                i = semi + 1;
            } else {
                sb.append('&');
                mark = i + 1;
                i++;
            }
        }
        if (mark < output.length()) {
            sb.append(output, mark, output.length());
        }
        return sb.toString();
    }

    private static int decodeEntity(String ref) {
        // Named entities
        return switch (ref) {
            case "lt" -> '<';
            case "gt" -> '>';
            case "amp" -> '&';
            case "quot" -> '"';
            case "apos" -> '\'';
            case "#39" -> '\'';
            default -> decodeNumericEntity(ref);
        };
    }

    private static int decodeNumericEntity(String ref) {
        if (ref.isEmpty() || ref.charAt(0) != '#') {
            return -1;
        }
        try {
            if (ref.length() > 1 && (ref.charAt(1) == 'x' || ref.charAt(1) == 'X')) {
                // Hexadecimal: &#xHHHH;
                int cp = Integer.parseInt(ref.substring(2), 16);
                return isValidScalarValue(cp) ? cp : -1;
            } else {
                // Decimal: &#DDDD;
                int cp = Integer.parseInt(ref.substring(1));
                return isValidScalarValue(cp) ? cp : -1;
            }
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Checks if the code point is a valid Unicode scalar value (excludes surrogates 0xD800-0xDFFF).
     */
    private static boolean isValidScalarValue(int cp) {
        return Character.isValidCodePoint(cp) && (cp < 0xD800 || cp > 0xDFFF);
    }
}
