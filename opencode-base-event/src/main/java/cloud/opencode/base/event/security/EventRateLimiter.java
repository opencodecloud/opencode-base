package cloud.opencode.base.event.security;

import cloud.opencode.base.event.Event;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Event Rate Limiter
 * 事件频率限制器
 *
 * <p>Rate limiter for controlling event publishing frequency.</p>
 * <p>用于控制事件发布频率的频率限制器。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Per-event-type rate limiting - 按事件类型频率限制</li>
 *   <li>Sliding window algorithm - 滑动窗口算法</li>
 *   <li>Configurable limits - 可配置的限制</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * EventRateLimiter limiter = new EventRateLimiter(100);
 * limiter.setLimit(HighFrequencyEvent.class, 1000);
 *
 * if (!limiter.allowPublish(event)) {
 *     throw new EventSecurityException("Rate limit exceeded");
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (concurrent data structures) - 线程安全: 是（并发数据结构）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.0
 */
public class EventRateLimiter {

    private final Map<Class<?>, RateLimitInfo> limits;
    private final int defaultMaxPerSecond;

    /**
     * Create rate limiter with default limit
     * 使用默认限制创建频率限制器
     *
     * @param defaultMaxPerSecond default max events per second | 默认每秒最大事件数
     */
    public EventRateLimiter(int defaultMaxPerSecond) {
        this.limits = new ConcurrentHashMap<>();
        this.defaultMaxPerSecond = defaultMaxPerSecond;
    }

    /**
     * Set rate limit for a specific event type
     * 为特定事件类型设置频率限制
     *
     * @param eventType    the event type | 事件类型
     * @param maxPerSecond max events per second | 每秒最大事件数
     */
    public void setLimit(Class<? extends Event> eventType, int maxPerSecond) {
        limits.put(eventType, new RateLimitInfo(maxPerSecond));
    }

    /**
     * Check if publishing is allowed
     * 检查是否允许发布
     *
     * @param event the event to check | 要检查的事件
     * @return true if allowed | 如果允许返回true
     */
    public boolean allowPublish(Event event) {
        if (event == null) {
            return false;
        }

        RateLimitInfo info = limits.computeIfAbsent(
            event.getClass(),
            _ -> new RateLimitInfo(defaultMaxPerSecond)
        );
        return info.tryAcquire();
    }

    /**
     * Get current count for an event type
     * 获取事件类型的当前计数
     *
     * @param eventType the event type | 事件类型
     * @return current count in the window | 窗口中的当前计数
     */
    public int getCurrentCount(Class<? extends Event> eventType) {
        RateLimitInfo info = limits.get(eventType);
        return info != null ? info.getCurrentCount() : 0;
    }

    /**
     * Get the default max per second
     * 获取默认每秒最大值
     *
     * @return default max per second | 默认每秒最大值
     */
    public int getDefaultMaxPerSecond() {
        return defaultMaxPerSecond;
    }

    /**
     * Reset all rate limits
     * 重置所有频率限制
     */
    public void reset() {
        limits.values().forEach(RateLimitInfo::reset);
    }

    /**
     * Reset rate limit for a specific event type
     * 重置特定事件类型的频率限制
     *
     * @param eventType the event type | 事件类型
     */
    public void reset(Class<? extends Event> eventType) {
        RateLimitInfo info = limits.get(eventType);
        if (info != null) {
            info.reset();
        }
    }

    /**
     * Rate limit information for a single event type
     * 单个事件类型的频率限制信息
     */
    private static class RateLimitInfo {
        private final int maxPerSecond;
        private final AtomicInteger count;
        private final ReentrantLock lock = new ReentrantLock();
        private volatile long windowStart;

        RateLimitInfo(int maxPerSecond) {
            this.maxPerSecond = maxPerSecond;
            this.count = new AtomicInteger(0);
            this.windowStart = System.currentTimeMillis();
        }

        boolean tryAcquire() {
            long now = System.currentTimeMillis();
            lock.lock();
            try {
                if (now - windowStart > 1000) {
                    count.set(0);
                    windowStart = now;
                }
                return count.incrementAndGet() <= maxPerSecond;
            } finally {
                lock.unlock();
            }
        }

        int getCurrentCount() {
            long now = System.currentTimeMillis();
            if (now - windowStart > 1000) {
                return 0;
            }
            return count.get();
        }

        void reset() {
            count.set(0);
            windowStart = System.currentTimeMillis();
        }
    }
}
