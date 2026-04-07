package cloud.opencode.base.web.ratelimit;

import cloud.opencode.base.web.http.HttpHeaders;

import java.time.Instant;
import java.util.Objects;

/**
 * Rate Limit Info - HTTP Rate Limiting Header Parser and Builder
 * 限流信息 - HTTP 限流头部解析器和构建器
 *
 * <p>An immutable record representing rate limit information typically conveyed via
 * HTTP response headers. Supports both parsing rate limit headers from responses
 * and generating them for responses.</p>
 * <p>一个不可变记录，表示通常通过 HTTP 响应头部传递的限流信息。支持从响应中解析限流头部
 * 以及为响应生成限流头部。</p>
 *
 * <p><strong>Standard Headers | 标准头部:</strong></p>
 * <ul>
 *   <li>{@code X-RateLimit-Limit} — Maximum requests allowed in the window - 窗口内允许的最大请求数</li>
 *   <li>{@code X-RateLimit-Remaining} — Remaining requests in the window - 窗口内剩余请求数</li>
 *   <li>{@code X-RateLimit-Reset} — Unix epoch second when the window resets - 窗口重置的 Unix 纪元秒</li>
 *   <li>{@code Retry-After} — Seconds until the client should retry - 客户端应重试的秒数</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create rate limit info
 * RateLimitInfo info = RateLimitInfo.of(100, 42, Instant.now().plusSeconds(3600).getEpochSecond());
 *
 * // Apply to response headers
 * HttpHeaders headers = HttpHeaders.of();
 * info.applyTo(headers);
 *
 * // Parse from response headers
 * RateLimitInfo parsed = RateLimitInfo.fromHeaders(headers);
 * if (parsed != null && parsed.isExhausted()) {
 *     long waitSeconds = parsed.retryAfterSeconds();
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 是（不可变记录）</li>
 *   <li>Null-safe: Factory methods validate arguments - 工厂方法验证参数</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.3
 */
public record RateLimitInfo(
        long limit,
        long remaining,
        long resetEpochSecond
) {

    // ==================== Header Name Constants ====================

    /** X-RateLimit-Limit header name. */
    public static final String HEADER_LIMIT = "X-RateLimit-Limit";

    /** X-RateLimit-Remaining header name. */
    public static final String HEADER_REMAINING = "X-RateLimit-Remaining";

    /** X-RateLimit-Reset header name (Unix epoch seconds). */
    public static final String HEADER_RESET = "X-RateLimit-Reset";

    /** Retry-After header name (seconds). */
    public static final String HEADER_RETRY_AFTER = "Retry-After";

    // ==================== Compact Constructor ====================

    /**
     * Compact constructor with validation.
     * 带验证的紧凑构造函数。
     *
     * @throws IllegalArgumentException if limit is negative, remaining is negative,
     *                                  or remaining exceeds limit
     *                                  - 如果 limit 或 remaining 为负数，或 remaining 超过 limit
     */
    public RateLimitInfo {
        if (limit < 0) {
            throw new IllegalArgumentException("limit must not be negative: " + limit);
        }
        if (remaining < 0) {
            throw new IllegalArgumentException("remaining must not be negative: " + remaining);
        }
        if (remaining > limit) {
            throw new IllegalArgumentException(
                    "remaining must not exceed limit: remaining=" + remaining + ", limit=" + limit);
        }
    }

    // ==================== Factory Methods ====================

    /**
     * Creates a RateLimitInfo instance.
     * 创建 RateLimitInfo 实例。
     *
     * @param limit            the rate limit ceiling - 限流上限
     * @param remaining        the remaining requests in the current window - 当前窗口中剩余的请求数
     * @param resetEpochSecond the Unix epoch second when the window resets - 窗口重置的 Unix 纪元秒
     * @return a new RateLimitInfo - 新的 RateLimitInfo
     */
    public static RateLimitInfo of(long limit, long remaining, long resetEpochSecond) {
        return new RateLimitInfo(limit, remaining, resetEpochSecond);
    }

    /**
     * Parses rate limit information from HTTP response headers.
     * 从 HTTP 响应头部解析限流信息。
     *
     * <p>Returns {@code null} if the required headers ({@code X-RateLimit-Limit},
     * {@code X-RateLimit-Remaining}, {@code X-RateLimit-Reset}) are not present
     * or cannot be parsed as valid numbers.</p>
     * <p>如果所需头部不存在或无法解析为有效数字，则返回 {@code null}。</p>
     *
     * @param headers the HTTP headers to parse - 要解析的 HTTP 头部
     * @return the parsed RateLimitInfo or null - 解析的 RateLimitInfo 或 null
     */
    public static RateLimitInfo fromHeaders(HttpHeaders headers) {
        Objects.requireNonNull(headers, "headers must not be null");

        String limitStr = headers.get(HEADER_LIMIT);
        String remainingStr = headers.get(HEADER_REMAINING);
        String resetStr = headers.get(HEADER_RESET);

        if (limitStr == null || remainingStr == null || resetStr == null) {
            return null;
        }

        try {
            long limit = Long.parseLong(limitStr.trim());
            long remaining = Long.parseLong(remainingStr.trim());
            long reset = Long.parseLong(resetStr.trim());
            if (limit < 0 || remaining < 0) {
                return null;
            }
            return new RateLimitInfo(limit, remaining, reset);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ==================== Instance Methods ====================

    /**
     * Checks if the rate limit has been exhausted (no remaining requests).
     * 检查限流是否已耗尽（没有剩余请求）。
     *
     * @return true if remaining is zero - 如果剩余为零返回 true
     */
    public boolean isExhausted() {
        return remaining <= 0;
    }

    /**
     * Calculates the number of seconds until the rate limit window resets.
     * 计算距离限流窗口重置的秒数。
     *
     * <p>Returns 0 if the reset time is in the past.</p>
     * <p>如果重置时间已过则返回 0。</p>
     *
     * @return seconds until reset (non-negative) - 距离重置的秒数（非负）
     */
    public long retryAfterSeconds() {
        long diff = resetEpochSecond - System.currentTimeMillis() / 1000L;
        return Math.max(0, diff);
    }

    /**
     * Returns the reset time as an Instant.
     * 返回重置时间的 Instant。
     *
     * @return the reset Instant - 重置的 Instant
     */
    public Instant resetInstant() {
        return Instant.ofEpochSecond(resetEpochSecond);
    }

    /**
     * Applies rate limit headers to the given HttpHeaders.
     * 将限流头部应用到给定的 HttpHeaders。
     *
     * <p>Sets the standard rate limit headers and, if the limit is exhausted,
     * also sets the {@code Retry-After} header.</p>
     * <p>设置标准限流头部，如果限流已耗尽，还会设置 {@code Retry-After} 头部。</p>
     *
     * @param headers the headers to apply to - 要应用的头部
     */
    public void applyTo(HttpHeaders headers) {
        Objects.requireNonNull(headers, "headers must not be null");

        headers.set(HEADER_LIMIT, String.valueOf(limit));
        headers.set(HEADER_REMAINING, String.valueOf(remaining));
        headers.set(HEADER_RESET, String.valueOf(resetEpochSecond));

        if (isExhausted()) {
            long retryAfter = retryAfterSeconds();
            headers.set(HEADER_RETRY_AFTER, String.valueOf(retryAfter));
        }
    }
}
