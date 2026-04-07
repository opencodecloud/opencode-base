package cloud.opencode.base.oauth2.par;

import java.time.Instant;
import java.util.Objects;

/**
 * Pushed Authorization Request Response (RFC 9126)
 * 推送授权请求响应（RFC 9126）
 *
 * <p>Immutable record representing the response from a Pushed Authorization Request (PAR)
 * endpoint as defined in RFC 9126. Contains the request_uri that the client uses to reference
 * the authorization request in a subsequent authorization request.</p>
 * <p>不可变记录，表示 RFC 9126 定义的推送授权请求（PAR）端点的响应。包含客户端在后续授权请求中
 * 用于引用授权请求的 request_uri。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>RFC 9126 compliant PAR response - 符合 RFC 9126 的 PAR 响应</li>
 *   <li>Expiration tracking with createdAt timestamp - 通过 createdAt 时间戳跟踪过期</li>
 *   <li>Convenience methods for expiration checking - 便捷的过期检查方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Check PAR response validity
 * // 检查 PAR 响应的有效性
 * ParResponse response = par.push(params);
 * if (!response.isExpired()) {
 *     String authUrl = par.buildAuthorizationUrl(authEndpoint, response, clientId);
 *     // Redirect user to authUrl
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: Yes (validates requestUri) - 空值安全: 是（验证 requestUri）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc9126">RFC 9126 - Pushed Authorization Requests</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.3
 */
public record ParResponse(
        String requestUri,
        int expiresIn,
        Instant createdAt
) {

    /**
     * Compact constructor with validation.
     * 带验证的紧凑构造器。
     */
    public ParResponse {
        Objects.requireNonNull(requestUri, "requestUri cannot be null");
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    /**
     * Check if the PAR response has expired.
     * 检查 PAR 响应是否已过期。
     *
     * @return true if expired | 已过期返回 true
     */
    public boolean isExpired() {
        return Instant.now().isAfter(createdAt.plusSeconds(expiresIn));
    }

    /**
     * Get the expiration time.
     * 获取过期时间。
     *
     * @return the expiration instant | 过期时间
     */
    public Instant expiresAt() {
        return createdAt.plusSeconds(expiresIn);
    }

    /**
     * Get the remaining time in seconds before expiration.
     * 获取过期前的剩余时间（秒）。
     *
     * @return remaining seconds, or 0 if expired | 剩余秒数，已过期则返回 0
     */
    public long remainingSeconds() {
        Instant now = Instant.now();
        Instant expiry = expiresAt();
        if (!now.isBefore(expiry)) return 0L;
        return expiry.getEpochSecond() - now.getEpochSecond();
    }
}
