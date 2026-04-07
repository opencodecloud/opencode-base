package cloud.opencode.base.web.util;

import cloud.opencode.base.web.http.HttpHeaders;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Client IP Resolver - Resolves real client IP from proxy headers
 * 客户端 IP 解析器 - 从代理头部解析真实客户端 IP
 *
 * <p>Extracts the real client IP address from HTTP proxy headers in the following
 * priority order: X-Forwarded-For, X-Real-IP, CF-Connecting-IP, True-Client-IP.</p>
 * <p>按以下优先顺序从 HTTP 代理头部提取真实客户端 IP 地址：
 * X-Forwarded-For、X-Real-IP、CF-Connecting-IP、True-Client-IP。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Multi-header proxy support - 多头部代理支持</li>
 *   <li>Trusted proxy filtering for X-Forwarded-For - X-Forwarded-For 信任代理过滤</li>
 *   <li>IPv4 and IPv6 format validation - IPv4 和 IPv6 格式校验</li>
 *   <li>Right-to-left traversal for X-Forwarded-For - X-Forwarded-For 从右往左遍历</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * HttpHeaders headers = HttpHeaders.of()
 *     .add("X-Forwarded-For", "203.0.113.50, 70.41.3.18, 150.172.238.178");
 *
 * // Without trusted proxies
 * String ip = ClientIp.resolve(headers); // "203.0.113.50"
 *
 * // With trusted proxies (right-to-left, skip trusted)
 * String ip = ClientIp.resolve(headers, Set.of("150.172.238.178", "70.41.3.18"));
 * // returns "203.0.113.50"
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具）</li>
 *   <li>Null-safe: Returns null when IP cannot be determined - 空值安全: 无法确定时返回 null</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.3
 */
public final class ClientIp {

    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$");

    private static final String X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String X_REAL_IP = "X-Real-IP";
    private static final String CF_CONNECTING_IP = "CF-Connecting-IP";
    private static final String TRUE_CLIENT_IP = "True-Client-IP";

    private ClientIp() {
        // Utility class
    }

    /**
     * Resolves client IP from headers without trusted proxy filtering.
     * 从头部解析客户端 IP，不进行信任代理过滤。
     *
     * <p>For X-Forwarded-For, traverses right-to-left and returns the rightmost valid IP.
     * To obtain the true client IP when behind trusted proxies, use
     * {@link #resolve(HttpHeaders, Set)} with the known proxy addresses.</p>
     * <p>对于 X-Forwarded-For，从右往左遍历并返回最右端有效 IP。
     * 如需在信任代理后面获取真实客户端 IP，请使用 {@link #resolve(HttpHeaders, Set)}。</p>
     *
     * @param headers the HTTP headers | HTTP 头部
     * @return the client IP or null if undetermined | 客户端 IP，无法确定时返回 null
     */
    public static String resolve(HttpHeaders headers) {
        return resolve(headers, Set.of());
    }

    /**
     * Resolves client IP from headers with trusted proxy filtering.
     * 从头部解析客户端 IP，支持信任代理过滤。
     *
     * <p>Checks headers in order: X-Forwarded-For, X-Real-IP, CF-Connecting-IP, True-Client-IP.
     * For X-Forwarded-For, traverses right-to-left and returns the first IP that is not
     * in the trusted proxies set.</p>
     * <p>按顺序检查头部：X-Forwarded-For、X-Real-IP、CF-Connecting-IP、True-Client-IP。
     * 对于 X-Forwarded-For，从右往左遍历，返回第一个不在信任代理集合中的 IP。</p>
     *
     * @param headers the HTTP headers | HTTP 头部
     * @param trustedProxies the set of trusted proxy IPs | 信任代理 IP 集合
     * @return the client IP or null if undetermined | 客户端 IP，无法确定时返回 null
     */
    public static String resolve(HttpHeaders headers, Set<String> trustedProxies) {
        if (headers == null) {
            return null;
        }
        Set<String> trusted = trustedProxies != null ? trustedProxies : Set.of();

        // 1. X-Forwarded-For
        String xForwardedFor = headers.get(X_FORWARDED_FOR);
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            String resolved = resolveFromXForwardedFor(xForwardedFor, trusted);
            if (resolved != null) {
                return resolved;
            }
        }

        // 2. X-Real-IP
        String xRealIp = headers.get(X_REAL_IP);
        if (xRealIp != null) {
            String trimmed = xRealIp.trim();
            if (isValidIp(trimmed)) {
                return trimmed;
            }
        }

        // 3. CF-Connecting-IP
        String cfIp = headers.get(CF_CONNECTING_IP);
        if (cfIp != null) {
            String trimmed = cfIp.trim();
            if (isValidIp(trimmed)) {
                return trimmed;
            }
        }

        // 4. True-Client-IP
        String trueClientIp = headers.get(TRUE_CLIENT_IP);
        if (trueClientIp != null) {
            String trimmed = trueClientIp.trim();
            if (isValidIp(trimmed)) {
                return trimmed;
            }
        }

        return null;
    }

    /**
     * Resolves client IP from X-Forwarded-For header.
     * 从 X-Forwarded-For 头部解析客户端 IP。
     *
     * <p>Traverses the IP list from right to left, skipping trusted proxies,
     * and returns the first valid non-trusted IP.</p>
     */
    private static String resolveFromXForwardedFor(String value, Set<String> trusted) {
        // Right-to-left traversal using lastIndexOf — avoids split() String[] allocation
        int end = value.length();
        while (end > 0) {
            int comma = value.lastIndexOf(',', end - 1);
            int start = comma < 0 ? 0 : comma + 1;
            String ip = value.substring(start, end).trim();
            if (!ip.isEmpty() && isValidIp(ip) && !trusted.contains(ip)) {
                return ip;
            }
            if (comma < 0) break;
            end = comma;
        }
        return null;
    }

    /**
     * Validates whether the string is a valid IP address (IPv4 or IPv6).
     * 校验字符串是否为有效的 IP 地址（IPv4 或 IPv6）。
     *
     * @param ip the IP string | IP 字符串
     * @return true if valid IP | 如果是有效 IP 返回 true
     */
    static boolean isValidIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        // Fast path for IPv4
        if (IPV4_PATTERN.matcher(ip).matches()) {
            return true;
        }
        // IPv6 validation via InetAddress
        return isValidIpv6(ip);
    }

    /**
     * IPv6 character set pattern: only hex digits and colons (no DNS resolution).
     * IPv6 字符集模式：仅允许十六进制数字和冒号（无 DNS 解析）。
     */
    private static final Pattern IPV6_PATTERN = Pattern.compile(
            "^([0-9a-fA-F]{0,4}:){2,7}[0-9a-fA-F]{0,4}$");

    private static boolean isValidIpv6(String ip) {
        if (!ip.contains(":")) {
            return false;
        }
        // Pure regex validation — no DNS resolution to prevent SSRF
        return IPV6_PATTERN.matcher(ip).matches();
    }
}
