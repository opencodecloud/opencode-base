package cloud.opencode.base.oauth2.security;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

/**
 * Resource Indicator (RFC 8707)
 * 资源指示器（RFC 8707）
 *
 * <p>Represents an OAuth 2.0 resource indicator as defined in RFC 8707. Resource indicators
 * allow clients to indicate the target resource server when requesting tokens, enabling
 * the authorization server to restrict tokens to specific audiences.</p>
 * <p>表示 RFC 8707 定义的 OAuth 2.0 资源指示器。资源指示器允许客户端在请求 Token 时指示
 * 目标资源服务器，使授权服务器能够将 Token 限制到特定受众。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>RFC 8707 compliant resource indicator - 符合 RFC 8707 的资源指示器</li>
 *   <li>URI validation on construction - 构造时进行 URI 验证</li>
 *   <li>Immutable record - 不可变记录</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create a resource indicator
 * // 创建资源指示器
 * ResourceIndicator resource = ResourceIndicator.of("https://api.example.com");
 *
 * // Use in authorization request
 * // 在授权请求中使用
 * params.put("resource", resource.toParam());
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: Yes (validates inputs) - 空值安全: 是（验证输入）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc8707">RFC 8707 - Resource Indicators for OAuth 2.0</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.3
 */
public record ResourceIndicator(String resource) {

    /**
     * Compact constructor with validation.
     * 带验证的紧凑构造器。
     *
     * @throws NullPointerException     if resource is null | 如果 resource 为 null 则抛出
     * @throws IllegalArgumentException if resource is blank or not a valid URI
     *                                  | 如果 resource 为空白或不是有效的 URI 则抛出
     */
    public ResourceIndicator {
        Objects.requireNonNull(resource, "resource cannot be null");
        if (resource.isBlank()) {
            throw new IllegalArgumentException("resource cannot be blank");
        }
        try {
            URI uri = new URI(resource);
            if (!uri.isAbsolute()) {
                throw new IllegalArgumentException(
                        "resource must be an absolute URI per RFC 8707: " + resource);
            }
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("resource must be a valid URI: " + resource, e);
        }
    }

    /**
     * Get the resource URI string suitable for use as a request parameter.
     * 获取适合用作请求参数的资源 URI 字符串。
     *
     * @return the resource URI string | 资源 URI 字符串
     */
    public String toParam() {
        return resource;
    }

    /**
     * Create a ResourceIndicator from a URI string.
     * 从 URI 字符串创建 ResourceIndicator。
     *
     * @param resource the resource URI string | 资源 URI 字符串
     * @return the resource indicator | 资源指示器
     * @throws NullPointerException     if resource is null | 如果 resource 为 null 则抛出
     * @throws IllegalArgumentException if resource is blank or not a valid URI
     *                                  | 如果 resource 为空白或不是有效的 URI 则抛出
     */
    public static ResourceIndicator of(String resource) {
        return new ResourceIndicator(resource);
    }
}
