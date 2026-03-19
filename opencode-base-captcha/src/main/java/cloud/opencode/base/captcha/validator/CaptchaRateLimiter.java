package cloud.opencode.base.captcha.validator;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Captcha Rate Limiter - Rate limiting for CAPTCHA requests
 * 验证码速率限制器 - 验证码请求的速率限制
 *
 * <p>This class provides rate limiting to prevent abuse of CAPTCHA services.</p>
 * <p>此类提供速率限制以防止验证码服务被滥用。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Per-client rate limiting - 每客户端速率限制</li>
 *   <li>Configurable time window - 可配置时间窗口</li>
 *   <li>Automatic cleanup - 自动清理</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CaptchaRateLimiter limiter = new CaptchaRateLimiter(10, Duration.ofMinutes(1));
 * if (limiter.tryAcquire(clientId)) {
 *     // proceed with CAPTCHA generation
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (uses ConcurrentHashMap and AtomicInteger) - 线程安全: 是（使用ConcurrentHashMap和AtomicInteger）</li>
 *   <li>Null-safe: No (clientId must not be null) - 空值安全: 否（客户端ID不能为null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
public final class CaptchaRateLimiter {

    private static final int CLEANUP_THRESHOLD = 1000;

    private final int maxRequests;
    private final Duration window;
    private final Map<String, RateLimitEntry> entries = new ConcurrentHashMap<>();
    private final java.util.concurrent.atomic.AtomicInteger operationCounter = new java.util.concurrent.atomic.AtomicInteger(0);

    /**
     * Creates a rate limiter with default settings (10 requests per minute).
     * 创建具有默认设置的速率限制器（每分钟 10 个请求）。
     */
    public CaptchaRateLimiter() {
        this(10, Duration.ofMinutes(1));
    }

    /**
     * Creates a rate limiter with specified settings.
     * 创建具有指定设置的速率限制器。
     *
     * @param maxRequests the maximum requests per window | 每个窗口的最大请求数
     * @param window      the time window | 时间窗口
     */
    public CaptchaRateLimiter(int maxRequests, Duration window) {
        this.maxRequests = maxRequests;
        this.window = window;
    }

    /**
     * Checks if a client is allowed to make a request.
     * 检查客户端是否被允许发出请求。
     *
     * @param clientId the client identifier (IP, session, etc.) | 客户端标识符
     * @return true if allowed | 如果允许返回 true
     */
    public boolean isAllowed(String clientId) {
        // Periodically clean up expired entries to prevent unbounded growth
        if (operationCounter.incrementAndGet() % CLEANUP_THRESHOLD == 0) {
            clearExpired();
        }

        RateLimitEntry entry = entries.compute(clientId, (k, v) -> {
            Instant now = Instant.now();
            if (v == null || v.isExpired(now, window)) {
                return new RateLimitEntry(now);
            }
            v.increment();
            return v;
        });
        return entry.getCount() <= maxRequests;
    }

    /**
     * Gets remaining requests for a client.
     * 获取客户端剩余请求数。
     *
     * @param clientId the client identifier | 客户端标识符
     * @return the remaining requests | 剩余请求数
     */
    public int getRemainingRequests(String clientId) {
        RateLimitEntry entry = entries.get(clientId);
        if (entry == null || entry.isExpired(Instant.now(), window)) {
            return maxRequests;
        }
        return Math.max(0, maxRequests - entry.getCount());
    }

    /**
     * Gets the time until reset for a client.
     * 获取客户端重置前的时间。
     *
     * @param clientId the client identifier | 客户端标识符
     * @return the duration until reset | 重置前的时间
     */
    public Duration getTimeUntilReset(String clientId) {
        RateLimitEntry entry = entries.get(clientId);
        if (entry == null) {
            return Duration.ZERO;
        }
        Instant resetTime = entry.getStartTime().plus(window);
        Instant now = Instant.now();
        if (now.isAfter(resetTime)) {
            return Duration.ZERO;
        }
        return Duration.between(now, resetTime);
    }

    /**
     * Clears a client's rate limit.
     * 清除客户端的速率限制。
     *
     * @param clientId the client identifier | 客户端标识符
     */
    public void clear(String clientId) {
        entries.remove(clientId);
    }

    /**
     * Clears all expired entries.
     * 清除所有过期条目。
     */
    public void clearExpired() {
        Instant now = Instant.now();
        entries.entrySet().removeIf(e -> e.getValue().isExpired(now, window));
    }

    /**
     * Internal rate limit entry.
     */
    private static final class RateLimitEntry {
        private final Instant startTime;
        private final AtomicInteger count = new AtomicInteger(1);

        RateLimitEntry(Instant startTime) {
            this.startTime = startTime;
        }

        Instant getStartTime() {
            return startTime;
        }

        int getCount() {
            return count.get();
        }

        void increment() {
            count.incrementAndGet();
        }

        boolean isExpired(Instant now, Duration window) {
            return now.isAfter(startTime.plus(window));
        }
    }
}
