package cloud.opencode.base.config.advanced;

import cloud.opencode.base.config.source.ConfigSource;
import cloud.opencode.base.config.source.InMemoryConfigSource;

import java.io.StringReader;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * HTTP Configuration Source Provider
 * HTTP配置源提供者
 *
 * <p>Example implementation for loading configuration from HTTP endpoints.</p>
 * <p>从HTTP端点加载配置的示例实现。</p>
 *
 * <p><strong>Supported URIs | 支持的URI:</strong></p>
 * <pre>
 * http://config-server/myapp/config
 * https://config-server/myapp/config
 * </pre>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ConfigSource source = ConfigSourceFactory.create("http://config-server/app/config");
 * }</pre>
 *
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Core HttpConfigSourceProvider functionality - HttpConfigSourceProvider核心功能</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Partial (validates inputs) - 空值安全: 部分（验证输入）</li>
 * </ul>
  * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
public class HttpConfigSourceProvider implements ConfigSourceProvider {

    @Override
    public boolean supports(String uri) {
        return uri.startsWith("http://") || uri.startsWith("https://");
    }

    private static final System.Logger LOGGER = System.getLogger(HttpConfigSourceProvider.class.getName());
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);
    private static final HttpClient SHARED_CLIENT = HttpClient.newBuilder()
            .connectTimeout(DEFAULT_TIMEOUT)
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    /**
     * Hostnames that are always considered internal/private.
     * 始终被视为内部/私有的主机名。
     */
    private static final Set<String> BLOCKED_HOSTNAMES = Set.of(
            "localhost", "localhost.localdomain", "[::1]"
    );

    @Override
    public ConfigSource create(String uri, Map<String, Object> options) {
        // Validate AND pin the resolved IP to prevent DNS rebinding SSRF.
        // The returned URI has the hostname replaced with the validated IP,
        // so the HTTP client connects to the exact same address we checked.
        String pinnedUri = validateAndPinIp(uri);
        URI originalUri = URI.create(uri);

        Duration timeout = options != null && options.containsKey("timeout")
                ? Duration.ofMillis(((Number) options.get("timeout")).longValue())
                : DEFAULT_TIMEOUT;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(pinnedUri))
                    .timeout(timeout)
                    .header("Accept", "application/json, text/plain, */*")
                    .header("Host", originalUri.getHost()) // preserve original Host for virtual hosting
                    .GET()
                    .build();

            HttpResponse<String> response = SHARED_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                Map<String, String> config = parseResponse(response.body(), response.headers()
                        .firstValue("Content-Type").orElse("text/plain"));
                return new InMemoryConfigSource("http[" + uri + "]", config);
            } else {
                LOGGER.log(System.Logger.Level.WARNING,
                        "HTTP config fetch failed with status {0} for {1}", response.statusCode(), uri);
                return new InMemoryConfigSource("http[" + uri + "]", Map.of());
            }
        } catch (Exception e) {
            LOGGER.log(System.Logger.Level.ERROR,
                    "Failed to fetch HTTP config from {0}: {1}", uri, e.getMessage());
            return new InMemoryConfigSource("http[" + uri + "]", Map.of());
        }
    }

    private Map<String, String> parseResponse(String body, String contentType) {
        Map<String, String> result = new LinkedHashMap<>();

        if (body == null || body.isBlank()) {
            return result;
        }

        try {
            if (contentType.contains("json")) {
                // Simple JSON parsing for key-value pairs
                // Format: {"key1": "value1", "key2": "value2"}
                parseSimpleJson(body, result);
            } else {
                // Treat as properties format
                Properties props = new Properties();
                props.load(new StringReader(body));
                for (String key : props.stringPropertyNames()) {
                    result.put(key, props.getProperty(key));
                }
            }
        } catch (Exception e) {
            LOGGER.log(System.Logger.Level.WARNING, "Failed to parse config response: {0}", e.getMessage());
        }

        return result;
    }

    private void parseSimpleJson(String json, Map<String, String> result) {
        // Simple JSON parser for flat key-value objects
        // This is a basic implementation; for production, consider using a JSON library
        String trimmed = json.trim();
        if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) {
            return;
        }

        String content = trimmed.substring(1, trimmed.length() - 1).trim();
        if (content.isEmpty()) {
            return;
        }

        // Split by comma, handling quoted values
        int start = 0;
        boolean inQuotes = false;
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '"' && (i == 0 || content.charAt(i - 1) != '\\')) {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                parseKeyValue(content.substring(start, i).trim(), result);
                start = i + 1;
            }
        }
        if (start < content.length()) {
            parseKeyValue(content.substring(start).trim(), result);
        }
    }

    private void parseKeyValue(String pair, Map<String, String> result) {
        int colonIndex = pair.indexOf(':');
        if (colonIndex > 0) {
            String key = unquote(pair.substring(0, colonIndex).trim());
            String value = unquote(pair.substring(colonIndex + 1).trim());
            result.put(key, value);
        }
    }

    private String unquote(String s) {
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    @Override
    public int priority() {
        return 100;
    }

    /**
     * Validates that the URL does not point to an internal/private network address,
     * and returns a new URI with the hostname replaced by the resolved IP address
     * to prevent DNS rebinding attacks.
     * 验证URL不指向内部/私有网络地址，并返回将主机名替换为已解析IP地址的新URI，
     * 以防止DNS重绑定攻击。
     *
     * <p>Rejects URLs targeting localhost, link-local (169.254.x.x), and RFC 1918
     * private ranges (10.x.x.x, 172.16-31.x.x, 192.168.x.x), as well as IPv6
     * loopback (::1) to prevent SSRF attacks. The returned URI pins the resolved
     * IP so the HTTP client connects to the exact address that was validated.</p>
     * <p>拒绝指向 localhost、链路本地 (169.254.x.x) 和 RFC 1918
     * 私有范围 (10.x.x.x, 172.16-31.x.x, 192.168.x.x) 的URL，
     * 以及 IPv6 回环 (::1)。返回的 URI 固定已解析的 IP，
     * 确保 HTTP 客户端连接到经过验证的确切地址。</p>
     *
     * @param uri the URI to validate | 要验证的URI
     * @return URI with hostname replaced by validated IP address | 主机名替换为已验证IP地址的URI
     * @throws IllegalArgumentException if the URL points to an internal network or cannot be resolved |
     *                                  如果URL指向内部网络或无法解析
     */
    private static String validateAndPinIp(String uri) {
        URI parsed = URI.create(uri);
        String host = parsed.getHost();
        if (host == null || host.isEmpty()) {
            throw new IllegalArgumentException("URL must have a valid host: " + uri);
        }

        String lowerHost = host.toLowerCase();

        // Check blocked hostnames
        if (BLOCKED_HOSTNAMES.contains(lowerHost)) {
            throw new IllegalArgumentException(
                "URL points to internal/private network (blocked hostname): " + uri);
        }

        // Resolve the host to IP address, validate all addresses, then pin the first safe one
        InetAddress[] addresses;
        try {
            addresses = InetAddress.getAllByName(host);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException(
                "Cannot resolve host for SSRF validation: " + host, e);
        }

        for (InetAddress addr : addresses) {
            if (addr.isLoopbackAddress() || addr.isLinkLocalAddress()
                    || addr.isSiteLocalAddress() || addr.isAnyLocalAddress()
                    || addr.isMulticastAddress()) {
                throw new IllegalArgumentException(
                    "URL points to internal/private network (" + addr.getHostAddress() + "): " + uri);
            }
        }

        // Pin to the first resolved IP to prevent DNS rebinding between validation and connection
        String pinnedIp = addresses[0].getHostAddress();
        // For IPv6, wrap in brackets for URI syntax
        if (pinnedIp.contains(":")) {
            pinnedIp = "[" + pinnedIp + "]";
        }
        return uri.replace(host, pinnedIp);
    }
}
