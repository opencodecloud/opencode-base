package cloud.opencode.base.sms.validation;

import cloud.opencode.base.sms.exception.SmsRateLimitException;

import java.time.Duration;
import java.util.Map;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * SMS Rate Limiter
 * 短信频率限制器
 *
 * <p>Prevents SMS abuse by limiting send frequency.</p>
 * <p>通过限制发送频率防止短信滥用。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Per-minute, per-hour, per-day rate limits - 每分钟、每小时、每天限制</li>
 *   <li>Automatic record cleanup - 自动记录清理</li>
 *   <li>AutoCloseable for resource cleanup - AutoCloseable资源清理</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * try (SmsRateLimiter limiter = new SmsRateLimiter(1, 5, 10)) {
 *     limiter.checkAndRecord("13800138000"); // throws SmsRateLimitException if exceeded
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (ConcurrentHashMap + atomic operations) - 线程安全: 是（ConcurrentHashMap + 原子操作）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 */
public final class SmsRateLimiter implements AutoCloseable {

    private static final int DEFAULT_LIMIT_PER_MINUTE = 1;
    private static final int DEFAULT_LIMIT_PER_HOUR = 5;
    private static final int DEFAULT_LIMIT_PER_DAY = 10;

    private final int limitPerMinute;
    private final int limitPerHour;
    private final int limitPerDay;

    private final Map<String, NavigableMap<Long, AtomicInteger>> phoneRecords =
        new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupScheduler;

    /**
     * Create rate limiter with defaults
     * 使用默认值创建限流器
     */
    public SmsRateLimiter() {
        this(DEFAULT_LIMIT_PER_MINUTE, DEFAULT_LIMIT_PER_HOUR, DEFAULT_LIMIT_PER_DAY);
    }

    /**
     * Create rate limiter
     * 创建限流器
     *
     * @param limitPerMinute the limit per minute | 每分钟限制
     * @param limitPerHour the limit per hour | 每小时限制
     * @param limitPerDay the limit per day | 每天限制
     */
    public SmsRateLimiter(int limitPerMinute, int limitPerHour, int limitPerDay) {
        this.limitPerMinute = limitPerMinute;
        this.limitPerHour = limitPerHour;
        this.limitPerDay = limitPerDay;
        this.cleanupScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "SmsRateLimiter-cleanup");
            t.setDaemon(true);
            return t;
        });
        this.cleanupScheduler.scheduleAtFixedRate(this::cleanupExpiredRecords,
                1, 1, TimeUnit.MINUTES);
    }

    /**
     * Try to acquire permit
     * 尝试获取许可
     *
     * @param phone the phone number | 手机号
     * @return true if allowed | 如果允许返回true
     */
    public boolean tryAcquire(String phone) {
        long now = System.currentTimeMillis();

        NavigableMap<Long, AtomicInteger> records = phoneRecords.computeIfAbsent(
            phone, k -> new ConcurrentSkipListMap<>()
        );

        synchronized (records) {
            // Clean up old records (older than 24 hours)
            records.headMap(now - 24 * 60 * 60 * 1000).clear();

            // Check limits
            int countMinute = countInWindow(records, now, 60 * 1000);
            if (countMinute >= limitPerMinute) {
                return false;
            }

            int countHour = countInWindow(records, now, 60 * 60 * 1000);
            if (countHour >= limitPerHour) {
                return false;
            }

            int countDay = countInWindow(records, now, 24 * 60 * 60 * 1000);
            if (countDay >= limitPerDay) {
                return false;
            }

            // Record this send
            records.computeIfAbsent(now, k -> new AtomicInteger(0)).incrementAndGet();
            return true;
        }
    }

    /**
     * Acquire permit or throw
     * 获取许可或抛出异常
     *
     * @param phone the phone number | 手机号
     * @throws SmsRateLimitException if rate limited | 如果被限流
     */
    public void acquire(String phone) {
        if (!tryAcquire(phone)) {
            Duration retryAfter = getRetryAfter(phone);
            throw new SmsRateLimitException(phone, retryAfter);
        }
    }

    /**
     * Get retry after duration
     * 获取重试等待时间
     *
     * @param phone the phone number | 手机号
     * @return the duration to wait | 等待时间
     */
    public Duration getRetryAfter(String phone) {
        NavigableMap<Long, AtomicInteger> records = phoneRecords.get(phone);
        if (records == null || records.isEmpty()) {
            return Duration.ZERO;
        }

        long now = System.currentTimeMillis();

        // Check which window is full
        int countMinute = countInWindow(records, now, 60 * 1000);
        if (countMinute >= limitPerMinute) {
            java.util.SortedMap<Long, AtomicInteger> tailMap = records.tailMap(now - 60 * 1000);
            if (!tailMap.isEmpty()) {
                Long oldest = tailMap.firstKey();
                return Duration.ofMillis(oldest + 60 * 1000 - now);
            }
        }

        return Duration.ofMinutes(1);
    }

    /**
     * Get current count for phone
     * 获取手机号当前计数
     *
     * @param phone the phone number | 手机号
     * @return the count today | 今日计数
     */
    public int getCurrentCount(String phone) {
        NavigableMap<Long, AtomicInteger> records = phoneRecords.get(phone);
        if (records == null) {
            return 0;
        }
        long now = System.currentTimeMillis();
        return countInWindow(records, now, 24 * 60 * 60 * 1000);
    }

    /**
     * Reset limits for phone
     * 重置手机号的限制
     *
     * @param phone the phone number | 手机号
     */
    public void reset(String phone) {
        phoneRecords.remove(phone);
    }

    /**
     * Clear all records
     * 清除所有记录
     */
    public void clear() {
        phoneRecords.clear();
    }

    /**
     * Count sends in time window
     * 计算时间窗口内的发送次数
     */
    private int countInWindow(NavigableMap<Long, AtomicInteger> records, long now, long windowMs) {
        return records.tailMap(now - windowMs).values().stream()
            .mapToInt(AtomicInteger::get)
            .sum();
    }

    private void cleanupExpiredRecords() {
        long now = System.currentTimeMillis();
        long dayMs = 24 * 60 * 60 * 1000L;
        phoneRecords.entrySet().removeIf(entry -> {
            NavigableMap<Long, AtomicInteger> records = entry.getValue();
            records.headMap(now - dayMs).clear();
            return records.isEmpty();
        });
    }

    @Override
    public void close() {
        cleanupScheduler.shutdownNow();
    }

    /**
     * Get limit per minute
     * 获取每分钟限制
     *
     * @return the limit | 限制
     */
    public int getLimitPerMinute() {
        return limitPerMinute;
    }

    /**
     * Get limit per hour
     * 获取每小时限制
     *
     * @return the limit | 限制
     */
    public int getLimitPerHour() {
        return limitPerHour;
    }

    /**
     * Get limit per day
     * 获取每天限制
     *
     * @return the limit | 限制
     */
    public int getLimitPerDay() {
        return limitPerDay;
    }
}
