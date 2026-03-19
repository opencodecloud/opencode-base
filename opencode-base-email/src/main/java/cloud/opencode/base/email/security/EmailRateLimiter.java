package cloud.opencode.base.email.security;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Email Rate Limiter
 * 邮件发送频率限制器
 *
 * <p>Limits email sending rate to prevent abuse.</p>
 * <p>限制邮件发送频率以防止滥用。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Per-minute rate limiting - 每分钟频率限制</li>
 *   <li>Per-hour rate limiting - 每小时频率限制</li>
 *   <li>Per-day rate limiting - 每天频率限制</li>
 *   <li>Per-recipient tracking - 按收件人跟踪</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * EmailRateLimiter limiter = new EmailRateLimiter(10, 100, 500);
 * if (!limiter.allowSend("recipient@example.com")) {
 *     throw new EmailException("Rate limit exceeded");
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-email V1.0.0
 */
public class EmailRateLimiter {

    private final ConcurrentMap<String, RateLimitInfo> limits = new ConcurrentHashMap<>();
    private final int maxPerMinute;
    private final int maxPerHour;
    private final int maxPerDay;

    /**
     * Create rate limiter with default limits
     * 使用默认限制创建频率限制器
     */
    public EmailRateLimiter() {
        this(10, 100, 1000);
    }

    /**
     * Create rate limiter with custom limits
     * 使用自定义限制创建频率限制器
     *
     * @param maxPerMinute max emails per minute | 每分钟最大邮件数
     * @param maxPerHour   max emails per hour | 每小时最大邮件数
     * @param maxPerDay    max emails per day | 每天最大邮件数
     */
    public EmailRateLimiter(int maxPerMinute, int maxPerHour, int maxPerDay) {
        this.maxPerMinute = maxPerMinute;
        this.maxPerHour = maxPerHour;
        this.maxPerDay = maxPerDay;
    }

    /**
     * Check if sending is allowed for recipient
     * 检查是否允许向收件人发送
     *
     * @param recipient the recipient email | 收件人邮箱
     * @return true if allowed | 允许返回true
     */
    public boolean allowSend(String recipient) {
        RateLimitInfo info = limits.computeIfAbsent(recipient, k -> new RateLimitInfo());

        synchronized (info) {
            info.cleanup();

            if (info.minuteCount.get() >= maxPerMinute ||
                    info.hourCount.get() >= maxPerHour ||
                    info.dayCount.get() >= maxPerDay) {
                return false;
            }

            info.recordSend();
            return true;
        }
    }

    /**
     * Check if sending is allowed (global, not per recipient)
     * 检查是否允许发送（全局，非按收件人）
     *
     * @return true if allowed | 允许返回true
     */
    public boolean allowSend() {
        return allowSend("__global__");
    }

    /**
     * Get remaining quota for recipient
     * 获取收件人剩余配额
     *
     * @param recipient the recipient email | 收件人邮箱
     * @return the remaining quota | 剩余配额
     */
    public RateLimitQuota getQuota(String recipient) {
        RateLimitInfo info = limits.get(recipient);
        if (info == null) {
            return new RateLimitQuota(maxPerMinute, maxPerHour, maxPerDay);
        }
        synchronized (info) {
            info.cleanup();
            return new RateLimitQuota(
                    maxPerMinute - info.minuteCount.get(),
                    maxPerHour - info.hourCount.get(),
                    maxPerDay - info.dayCount.get()
            );
        }
    }

    /**
     * Reset rate limit for recipient
     * 重置收件人的频率限制
     *
     * @param recipient the recipient email | 收件人邮箱
     */
    public void reset(String recipient) {
        limits.remove(recipient);
    }

    /**
     * Reset all rate limits
     * 重置所有频率限制
     */
    public void resetAll() {
        limits.clear();
    }

    /**
     * Get max per minute limit
     * 获取每分钟最大限制
     *
     * @return the limit | 限制值
     */
    public int getMaxPerMinute() {
        return maxPerMinute;
    }

    /**
     * Get max per hour limit
     * 获取每小时最大限制
     *
     * @return the limit | 限制值
     */
    public int getMaxPerHour() {
        return maxPerHour;
    }

    /**
     * Get max per day limit
     * 获取每天最大限制
     *
     * @return the limit | 限制值
     */
    public int getMaxPerDay() {
        return maxPerDay;
    }

    /**
     * Rate limit quota record
     * 频率限制配额记录
     *
     * @param minuteRemaining remaining quota for current minute | 当前分钟剩余配额
     * @param hourRemaining   remaining quota for current hour | 当前小时剩余配额
     * @param dayRemaining    remaining quota for current day | 当前天剩余配额
     */
    public record RateLimitQuota(int minuteRemaining, int hourRemaining, int dayRemaining) {
    }

    /**
     * Internal rate limit tracking info
     */
    private static class RateLimitInfo {
        final AtomicInteger minuteCount = new AtomicInteger(0);
        final AtomicInteger hourCount = new AtomicInteger(0);
        final AtomicInteger dayCount = new AtomicInteger(0);
        volatile long minuteStart = currentMinute();
        volatile long hourStart = currentHour();
        volatile long dayStart = currentDay();

        void recordSend() {
            minuteCount.incrementAndGet();
            hourCount.incrementAndGet();
            dayCount.incrementAndGet();
        }

        void cleanup() {
            long now = System.currentTimeMillis();
            long currentMinute = now / 60000;
            long currentHour = now / 3600000;
            long currentDay = now / 86400000;

            if (currentMinute != minuteStart) {
                minuteStart = currentMinute;
                minuteCount.set(0);
            }
            if (currentHour != hourStart) {
                hourStart = currentHour;
                hourCount.set(0);
            }
            if (currentDay != dayStart) {
                dayStart = currentDay;
                dayCount.set(0);
            }
        }

        private static long currentMinute() {
            return System.currentTimeMillis() / 60000;
        }

        private static long currentHour() {
            return System.currentTimeMillis() / 3600000;
        }

        private static long currentDay() {
            return System.currentTimeMillis() / 86400000;
        }
    }
}
