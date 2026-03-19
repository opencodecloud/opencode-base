package cloud.opencode.base.captcha.exception;

import java.time.Duration;

/**
 * Captcha Rate Limit Exception - Thrown when rate limit is exceeded
 * 验证码速率限制异常 - 当超过速率限制时抛出
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Carries client ID and optional retry-after duration - 携带客户端 ID 和可选的重试等待时间</li>
 *   <li>Extends CaptchaException - 继承 CaptchaException</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw new CaptchaRateLimitException(clientId);
 * throw new CaptchaRateLimitException(clientId, Duration.ofMinutes(1));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 *   <li>Null-safe: No (clientId may be null) - 空值安全: 否（clientId 可能为空）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
public class CaptchaRateLimitException extends CaptchaException {

    private final String clientId;
    private final Duration retryAfter;

    /**
     * Constructs a new exception with the client ID.
     * 使用客户端 ID 构造新异常。
     *
     * @param clientId the client ID | 客户端 ID
     */
    public CaptchaRateLimitException(String clientId) {
        super("Rate limit exceeded for client: " + clientId);
        this.clientId = clientId;
        this.retryAfter = null;
    }

    /**
     * Constructs a new exception with client ID and retry duration.
     * 使用客户端 ID 和重试时间构造新异常。
     *
     * @param clientId   the client ID | 客户端 ID
     * @param retryAfter the duration to wait before retry | 重试前等待的时间
     */
    public CaptchaRateLimitException(String clientId, Duration retryAfter) {
        super("Rate limit exceeded for client: " + clientId +
              ". Retry after: " + retryAfter.toSeconds() + " seconds");
        this.clientId = clientId;
        this.retryAfter = retryAfter;
    }

    /**
     * Gets the client ID.
     * 获取客户端 ID。
     *
     * @return the client ID | 客户端 ID
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Gets the retry after duration.
     * 获取重试等待时间。
     *
     * @return the retry after duration | 重试等待时间
     */
    public Duration getRetryAfter() {
        return retryAfter;
    }
}
