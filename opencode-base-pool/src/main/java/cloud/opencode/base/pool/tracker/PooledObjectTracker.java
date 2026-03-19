package cloud.opencode.base.pool.tracker;

import cloud.opencode.base.pool.PooledObject;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * PooledObjectTracker - Pooled Object Tracker
 * PooledObjectTracker - 池化对象追踪器
 *
 * <p>Tracks borrowed objects to detect leaks and abandoned objects.
 * Useful for debugging and monitoring pool usage.</p>
 * <p>追踪借用的对象以检测泄漏和废弃对象。用于调试和监控池使用。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Track borrow/return operations - 追踪借用/归还操作</li>
 *   <li>Detect abandoned objects - 检测废弃对象</li>
 *   <li>Stack trace capture for debugging - 捕获调用栈用于调试</li>
 *   <li>Automatic cleanup of abandoned objects - 自动清理废弃对象</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * PooledObjectTracker<Connection> tracker = new PooledObjectTracker<>(
 *     Duration.ofMinutes(5),
 *     true,
 *     abandoned -> {
 *         logger.warn("Abandoned connection detected: " + abandoned);
 *     });
 *
 * // Track borrow
 * tracker.trackBorrow(pooledObject);
 *
 * // Track return
 * tracker.trackReturn(pooledObject);
 *
 * // Check for abandoned
 * List<TrackedObject<Connection>> abandoned = tracker.getAbandonedObjects();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 * </ul>
 *
 * @param <T> the type of object being tracked - 追踪的对象类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.0
 */
public class PooledObjectTracker<T> {

    private final Duration abandonedTimeout;
    private final boolean captureStackTrace;
    private final Consumer<TrackedObject<T>> abandonedCallback;
    private final Map<PooledObject<T>, TrackedObject<T>> trackedObjects;

    /**
     * Creates a tracker with default settings.
     * 使用默认设置创建追踪器。
     */
    public PooledObjectTracker() {
        this(Duration.ofMinutes(5), false, null);
    }

    /**
     * Creates a tracker with custom settings.
     * 使用自定义设置创建追踪器。
     *
     * @param abandonedTimeout  timeout to consider object abandoned - 认为对象被废弃的超时
     * @param captureStackTrace whether to capture borrow stack trace - 是否捕获借用调用栈
     * @param abandonedCallback callback when abandoned object detected - 检测到废弃对象时的回调
     */
    public PooledObjectTracker(
            Duration abandonedTimeout,
            boolean captureStackTrace,
            Consumer<TrackedObject<T>> abandonedCallback) {
        this.abandonedTimeout = abandonedTimeout;
        this.captureStackTrace = captureStackTrace;
        this.abandonedCallback = abandonedCallback;
        this.trackedObjects = new ConcurrentHashMap<>();
    }

    /**
     * Tracks a borrowed object.
     * 追踪借用的对象。
     *
     * @param pooledObject the borrowed object - 借用的对象
     */
    public void trackBorrow(PooledObject<T> pooledObject) {
        StackTraceElement[] stackTrace = captureStackTrace ?
                Thread.currentThread().getStackTrace() : null;
        TrackedObject<T> tracked = new TrackedObject<>(
                pooledObject,
                Instant.now(),
                Thread.currentThread().getName(),
                stackTrace
        );
        trackedObjects.put(pooledObject, tracked);
    }

    /**
     * Tracks a returned object.
     * 追踪归还的对象。
     *
     * @param pooledObject the returned object - 归还的对象
     */
    public void trackReturn(PooledObject<T> pooledObject) {
        trackedObjects.remove(pooledObject);
    }

    /**
     * Gets all currently tracked objects.
     * 获取所有当前追踪的对象。
     *
     * @return list of tracked objects - 追踪对象列表
     */
    public List<TrackedObject<T>> getTrackedObjects() {
        return List.copyOf(trackedObjects.values());
    }

    /**
     * Gets abandoned objects (borrowed longer than timeout).
     * 获取废弃对象（借用超过超时时间）。
     *
     * @return list of abandoned objects - 废弃对象列表
     */
    public List<TrackedObject<T>> getAbandonedObjects() {
        Instant threshold = Instant.now().minus(abandonedTimeout);
        return trackedObjects.values().stream()
                .filter(tracked -> tracked.borrowTime().isBefore(threshold))
                .toList();
    }

    /**
     * Checks for abandoned objects and invokes callback.
     * 检查废弃对象并调用回调。
     *
     * @return number of abandoned objects found - 发现的废弃对象数量
     */
    public int checkAndHandleAbandoned() {
        List<TrackedObject<T>> abandoned = getAbandonedObjects();
        if (abandonedCallback != null) {
            abandoned.forEach(abandonedCallback);
        }
        return abandoned.size();
    }

    /**
     * Gets the number of currently tracked objects.
     * 获取当前追踪的对象数量。
     *
     * @return the tracked count - 追踪数量
     */
    public int getTrackedCount() {
        return trackedObjects.size();
    }

    /**
     * Clears all tracked objects.
     * 清除所有追踪对象。
     */
    public void clear() {
        trackedObjects.clear();
    }

    /**
     * Tracked object information.
     * 追踪对象信息。
     *
     * @param <T>        the object type - 对象类型
     * @param pooledObject the pooled object - 池化对象
     * @param borrowTime   the borrow time - 借用时间
     * @param threadName   the borrowing thread name - 借用线程名称
     * @param stackTrace   the borrow stack trace (may be null) - 借用调用栈（可能为null）
     */
    public record TrackedObject<T>(
            PooledObject<T> pooledObject,
            Instant borrowTime,
            String threadName,
            StackTraceElement[] stackTrace
    ) {
        /**
         * Gets the duration since borrow.
         * 获取自借用以来的时长。
         *
         * @return the duration - 时长
         */
        public Duration borrowDuration() {
            return Duration.between(borrowTime, Instant.now());
        }

        /**
         * Gets the stack trace as string.
         * 获取调用栈字符串。
         *
         * @return the stack trace string or "N/A" - 调用栈字符串或"N/A"
         */
        public String stackTraceString() {
            if (stackTrace == null) {
                return "N/A (stack trace capture disabled)";
            }
            StringBuilder sb = new StringBuilder();
            for (StackTraceElement element : stackTrace) {
                sb.append("\tat ").append(element).append("\n");
            }
            return sb.toString();
        }

        @Override
        public String toString() {
            return "TrackedObject{" +
                    "object=" + pooledObject.getObject() +
                    ", borrowTime=" + borrowTime +
                    ", borrowDuration=" + borrowDuration() +
                    ", thread=" + threadName +
                    '}';
        }
    }
}
