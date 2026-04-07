package cloud.opencode.base.web.url;

import java.net.IDN;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Open URL - URL Utility Class
 * Open URL - URL 工具类
 *
 * <p>This class provides static utility methods for URL encoding, decoding,
 * parsing, and validation.</p>
 * <p>此类提供 URL 编码、解码、解析和验证的静态工具方法。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * // Encoding
 * String encoded = OpenUrl.encode("hello world"); // "hello+world"
 * String pathEncoded = OpenUrl.encodePath("path/to file"); // "path/to%20file"
 *
 * // Decoding
 * String decoded = OpenUrl.decode("hello%20world"); // "hello world"
 *
 * // Parsing
 * String host = OpenUrl.getHost("https://example.com/path"); // "example.com"
 * String path = OpenUrl.getPath("https://example.com/path"); // "/path"
 *
 * // Validation
 * boolean valid = OpenUrl.isValid("https://example.com"); // true
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>URL encoding and decoding - URL编码和解码</li>
 *   <li>URL parsing (scheme, host, port, path) - URL解析</li>
 *   <li>URL validation - URL验证</li>
 *   <li>IDN (Punycode) support - IDN（Punycode）支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * String encoded = OpenUrl.encode("hello world");
 * String host = OpenUrl.getHost("https://example.com/path");
 * boolean valid = OpenUrl.isValid("https://example.com");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 是（无状态工具）</li>
 *   <li>Null-safe: Yes (returns null for null input) - 是（null输入返回null）</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
public final class OpenUrl {

    private static final Pattern URL_PATTERN = Pattern.compile(
            "^(https?|ftp)://[\\w\\-]+(\\.[\\w\\-]+)+([\\w\\-.,@?^=%&:/~+#]*[\\w\\-@?^=%&/~+#])?$",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern DOMAIN_PATTERN = Pattern.compile(
            "^([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,}$");

    private OpenUrl() {
        // Static utility class
    }

    // ==================== Encoding ====================

    /**
     * URL encodes a string.
     * URL 编码字符串。
     *
     * @param value the value to encode - 要编码的值
     * @return the encoded string - 编码的字符串
     */
    public static String encode(String value) {
        if (value == null) {
            return null;
        }
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /**
     * URL decodes a string.
     * URL 解码字符串。
     *
     * @param value the value to decode - 要解码的值
     * @return the decoded string - 解码的字符串
     */
    public static String decode(String value) {
        if (value == null) {
            return null;
        }
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    /**
     * Encodes a path segment (preserves slashes).
     * 编码路径段（保留斜杠）。
     *
     * @param path the path to encode - 要编码的路径
     * @return the encoded path - 编码的路径
     */
    public static String encodePath(String path) {
        if (path == null) {
            return null;
        }
        // Iterate over slash-separated segments using indexOf to avoid String[] allocation.
        // URLEncoder.encode uses '+' for spaces (application/x-www-form-urlencoded),
        // but path segments per RFC 3986 require '%20' for spaces — rewrite inline.
        StringBuilder sb = new StringBuilder(path.length() + 16);
        int start = 0;
        int len = path.length();
        while (start <= len) {
            int slash = path.indexOf('/', start);
            int end = slash < 0 ? len : slash;
            if (sb.length() > 0) {
                sb.append('/');
            }
            String encoded = URLEncoder.encode(path.substring(start, end), StandardCharsets.UTF_8);
            // Replace '+' (space encoding) with '%20' character-by-character — no extra String alloc
            for (int i = 0; i < encoded.length(); i++) {
                char c = encoded.charAt(i);
                if (c == '+') {
                    sb.append("%20");
                } else {
                    sb.append(c);
                }
            }
            if (slash < 0) break;
            start = slash + 1;
        }
        return sb.toString();
    }

    /**
     * Encodes query parameters from map.
     * 从 Map 编码查询参数。
     *
     * @param params the parameters - 参数
     * @return the encoded query string - 编码的查询字符串
     */
    public static String encodeParams(Map<String, String> params) {
        return QueryString.of(params).toString();
    }

    // ==================== URL Builder ====================

    /**
     * Creates a URL builder.
     * 创建 URL 构建器。
     *
     * @return the builder - 构建器
     */
    public static UrlBuilder builder() {
        return UrlBuilder.create();
    }

    /**
     * Creates a URL builder from existing URL.
     * 从现有 URL 创建构建器。
     *
     * @param url the base URL - 基础 URL
     * @return the builder - 构建器
     */
    public static UrlBuilder builder(String url) {
        return UrlBuilder.from(url);
    }

    // ==================== URL Parsing ====================

    /**
     * Gets the scheme from URL.
     * 从 URL 获取协议。
     *
     * @param url the URL - URL
     * @return the scheme or null - 协议或 null
     */
    public static String getScheme(String url) {
        try {
            return URI.create(url).getScheme();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Gets the host from URL.
     * 从 URL 获取主机。
     *
     * @param url the URL - URL
     * @return the host or null - 主机或 null
     */
    public static String getHost(String url) {
        try {
            return URI.create(url).getHost();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Gets the port from URL.
     * 从 URL 获取端口。
     *
     * @param url the URL - URL
     * @return the port or -1 - 端口或 -1
     */
    public static int getPort(String url) {
        try {
            return URI.create(url).getPort();
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Gets the effective port (considering default ports).
     * 获取有效端口（考虑默认端口）。
     *
     * @param url the URL - URL
     * @return the effective port - 有效端口
     */
    public static int getEffectivePort(String url) {
        try {
            URI uri = URI.create(url);
            int port = uri.getPort();
            if (port > 0) {
                return port;
            }
            String scheme = uri.getScheme();
            if ("https".equalsIgnoreCase(scheme)) {
                return 443;
            }
            if ("http".equalsIgnoreCase(scheme)) {
                return 80;
            }
        } catch (Exception ignored) {
            // Fall through
        }
        return -1;
    }

    /**
     * Gets the path from URL.
     * 从 URL 获取路径。
     *
     * @param url the URL - URL
     * @return the path or null - 路径或 null
     */
    public static String getPath(String url) {
        try {
            return URI.create(url).getPath();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Gets the query string from URL.
     * 从 URL 获取查询字符串。
     *
     * @param url the URL - URL
     * @return the query string or null - 查询字符串或 null
     */
    public static String getQuery(String url) {
        try {
            return URI.create(url).getQuery();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Gets the fragment from URL.
     * 从 URL 获取片段。
     *
     * @param url the URL - URL
     * @return the fragment or null - 片段或 null
     */
    public static String getFragment(String url) {
        try {
            return URI.create(url).getFragment();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Parses query string from URL.
     * 从 URL 解析查询字符串。
     *
     * @param url the URL - URL
     * @return the parsed query string - 解析的查询字符串
     */
    public static QueryString parseQuery(String url) {
        String query = getQuery(url);
        return QueryString.parse(query);
    }

    /**
     * Gets query parameter from URL.
     * 从 URL 获取查询参数。
     *
     * @param url  the URL - URL
     * @param name the parameter name - 参数名
     * @return the parameter value or null - 参数值或 null
     */
    public static String getQueryParam(String url, String name) {
        return parseQuery(url).get(name);
    }

    // ==================== URL Manipulation ====================

    /**
     * Joins URL parts.
     * 连接 URL 部分。
     *
     * @param base  the base URL - 基础 URL
     * @param parts the parts to append - 要追加的部分
     * @return the joined URL - 连接的 URL
     */
    public static String join(String base, String... parts) {
        if (base == null) {
            base = "";
        }
        StringBuilder sb = new StringBuilder(base);

        for (String part : parts) {
            if (part == null || part.isEmpty()) {
                continue;
            }
            boolean sbEndsWithSlash = sb.length() > 0 && sb.charAt(sb.length() - 1) == '/';
            boolean partStartsWithSlash = part.startsWith("/");
            if (sb.length() > 0 && !sbEndsWithSlash && !partStartsWithSlash) {
                sb.append('/');
            } else if (sbEndsWithSlash && partStartsWithSlash) {
                part = part.substring(1);
            }
            sb.append(part);
        }

        return sb.toString();
    }

    /**
     * Normalizes a URL.
     * 规范化 URL。
     *
     * @param url the URL - URL
     * @return the normalized URL - 规范化的 URL
     */
    public static String normalize(String url) {
        try {
            URI uri = URI.create(url).normalize();
            return uri.toString();
        } catch (Exception e) {
            return url;
        }
    }

    /**
     * Removes query string from URL.
     * 从 URL 删除查询字符串。
     *
     * @param url the URL - URL
     * @return the URL without query - 不含查询的 URL
     */
    public static String removeQuery(String url) {
        if (url == null) {
            return null;
        }
        int queryIndex = url.indexOf('?');
        if (queryIndex >= 0) {
            return url.substring(0, queryIndex);
        }
        return url;
    }

    /**
     * Removes fragment from URL.
     * 从 URL 删除片段。
     *
     * @param url the URL - URL
     * @return the URL without fragment - 不含片段的 URL
     */
    public static String removeFragment(String url) {
        if (url == null) {
            return null;
        }
        int fragmentIndex = url.indexOf('#');
        if (fragmentIndex >= 0) {
            return url.substring(0, fragmentIndex);
        }
        return url;
    }

    /**
     * Gets the base URL (scheme + host + port).
     * 获取基础 URL（协议 + 主机 + 端口）。
     *
     * @param url the URL - URL
     * @return the base URL - 基础 URL
     */
    public static String getBaseUrl(String url) {
        try {
            URI uri = URI.create(url);
            StringBuilder sb = new StringBuilder();
            sb.append(uri.getScheme()).append("://").append(uri.getHost());
            if (uri.getPort() > 0) {
                sb.append(":").append(uri.getPort());
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    // ==================== Validation ====================

    /**
     * Checks if URL is valid.
     * 检查 URL 是否有效。
     *
     * @param url the URL - URL
     * @return true if valid - 如果有效返回 true
     */
    public static boolean isValid(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        try {
            URI uri = URI.create(url);
            return uri.getScheme() != null && uri.getHost() != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if URL matches pattern.
     * 检查 URL 是否匹配模式。
     *
     * @param url the URL - URL
     * @return true if matches - 如果匹配返回 true
     */
    public static boolean isValidUrl(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        return URL_PATTERN.matcher(url).matches();
    }

    /**
     * Checks if domain is valid.
     * 检查域名是否有效。
     *
     * @param domain the domain - 域名
     * @return true if valid - 如果有效返回 true
     */
    public static boolean isValidDomain(String domain) {
        if (domain == null || domain.isEmpty()) {
            return false;
        }
        return DOMAIN_PATTERN.matcher(domain).matches();
    }

    /**
     * Checks if URL is HTTPS.
     * 检查 URL 是否为 HTTPS。
     *
     * @param url the URL - URL
     * @return true if HTTPS - 如果是 HTTPS 返回 true
     */
    public static boolean isHttps(String url) {
        return "https".equalsIgnoreCase(getScheme(url));
    }

    /**
     * Checks if URL is HTTP.
     * 检查 URL 是否为 HTTP。
     *
     * @param url the URL - URL
     * @return true if HTTP - 如果是 HTTP 返回 true
     */
    public static boolean isHttp(String url) {
        return "http".equalsIgnoreCase(getScheme(url));
    }

    // ==================== IDN Support ====================

    /**
     * Converts domain to ASCII (Punycode).
     * 将域名转换为 ASCII（Punycode）。
     *
     * @param domain the domain - 域名
     * @return the ASCII domain - ASCII 域名
     */
    public static String toAscii(String domain) {
        if (domain == null) {
            return null;
        }
        return IDN.toASCII(domain);
    }

    /**
     * Converts domain from ASCII (Punycode) to Unicode.
     * 将域名从 ASCII（Punycode）转换为 Unicode。
     *
     * @param domain the ASCII domain - ASCII 域名
     * @return the Unicode domain - Unicode 域名
     */
    public static String toUnicode(String domain) {
        if (domain == null) {
            return null;
        }
        return IDN.toUnicode(domain);
    }
}
