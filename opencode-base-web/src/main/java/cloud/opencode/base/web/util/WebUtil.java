package cloud.opencode.base.web.util;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Web Utility
 * Web工具类
 *
 * <p>Common web utilities for validation and IP operations.</p>
 * <p>常用Web验证和IP操作工具。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>URL encoding and decoding - URL编码和解码</li>
 *   <li>Query string parsing - 查询字符串解析</li>
 *   <li>IP, email, and URL validation - IP、邮箱和URL验证</li>
 *   <li>IP address conversion - IP地址转换</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * String encoded = WebUtil.urlEncode("hello world");
 * boolean validIp = WebUtil.isValidIp("192.168.1.1");
 * boolean privateIp = WebUtil.isPrivateIp("10.0.0.1");
 * long ipLong = WebUtil.ipToLong("192.168.1.1");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 是（无状态工具）</li>
 *   <li>Null-safe: No (throws on null for conversion methods) - 否（转换方法对null抛出异常）</li>
 * </ul>
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) per utility operation - 每次工具操作 O(1)</li>
 *   <li>Space complexity: O(1) - O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
public final class WebUtil {

    private static final Pattern IP_PATTERN = Pattern.compile(
        "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern URL_PATTERN = Pattern.compile(
        "^https?://[\\w.-]+(:\\d+)?(/.*)?$");

    private WebUtil() {
        // Utility class
    }

    // === URL encoding/decoding ===

    public static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    public static String urlDecode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    // === Query string parsing ===

    public static Map<String, String> parseQueryString(String queryString) {
        Map<String, String> params = new HashMap<>();
        if (queryString == null || queryString.isBlank()) {
            return params;
        }
        String query = queryString.startsWith("?") ? queryString.substring(1) : queryString;
        for (String pair : query.split("&")) {
            int idx = pair.indexOf('=');
            if (idx > 0) {
                String key = urlDecode(pair.substring(0, idx));
                String value = idx < pair.length() - 1 ? urlDecode(pair.substring(idx + 1)) : "";
                params.put(key, value);
            }
        }
        return params;
    }

    public static String buildQueryString(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!sb.isEmpty()) sb.append("&");
            sb.append(urlEncode(entry.getKey()))
              .append("=")
              .append(urlEncode(entry.getValue()));
        }
        return sb.toString();
    }

    // === Validation ===

    public static boolean isValidIp(String ip) {
        return ip != null && IP_PATTERN.matcher(ip).matches();
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidUrl(String url) {
        return url != null && URL_PATTERN.matcher(url).matches();
    }

    // === IP utilities ===

    public static boolean isPrivateIp(String ip) {
        if (!isValidIp(ip)) return false;
        String[] parts = ip.split("\\.");
        int first = Integer.parseInt(parts[0]);
        int second = Integer.parseInt(parts[1]);
        return first == 10 ||
               (first == 172 && second >= 16 && second <= 31) ||
               (first == 192 && second == 168) ||
               first == 127;
    }

    public static long ipToLong(String ip) {
        if (ip == null || ip.isBlank()) {
            throw new IllegalArgumentException("Invalid IP address: " + ip);
        }
        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid IP address: " + ip);
        }
        long result = 0;
        for (int i = 0; i < 4; i++) {
            int octet = Integer.parseInt(parts[i]);
            if (octet < 0 || octet > 255) {
                throw new IllegalArgumentException("Invalid IP address: " + ip);
            }
            result = result * 256 + octet;
        }
        return result;
    }

    public static String longToIp(long ip) {
        return String.format("%d.%d.%d.%d",
            (ip >> 24) & 0xFF,
            (ip >> 16) & 0xFF,
            (ip >> 8) & 0xFF,
            ip & 0xFF);
    }
}
