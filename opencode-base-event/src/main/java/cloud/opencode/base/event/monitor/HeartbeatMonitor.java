package cloud.opencode.base.event.monitor;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Generic heartbeat monitor that detects missed heartbeats.
 * 通用心跳监控器，用于检测心跳超时。
 *
 * <p>Watches registered items and fires a callback when an item has not sent
 * a heartbeat within its expected interval. Uses a single virtual thread for
 * periodic checks.</p>
 *
 * <p>监控已注册项目，当某项目在其期望间隔内未发送心跳时触发回调。
 * 使用单个虚拟线程进行周期性检查。</p>
 *
 * <h3>Usage | 用法:</h3>
 * <pre>{@code
 * HeartbeatMonitor monitor = HeartbeatMonitor.builder()
 *     .checkPeriod(Duration.ofSeconds(30))
 *     .onMissed(id -> log.warn("Heartbeat missed: {}", id))
 *     .build();
 *
 * monitor.watch("service-a", Duration.ofMinutes(1));
 * monitor.start();
 *
 * // In the monitored component:
 * monitor.heartbeat("service-a");
 *
 * // On shutdown:
 * monitor.stop();
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Periodic heartbeat checking - 周期性心跳检查</li>
 *   <li>Missed heartbeat detection - 心跳超时检测</li>
 *   <li>Virtual thread based - 基于虚拟线程</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * HeartbeatMonitor monitor = HeartbeatMonitor.builder()
 *     .checkPeriod(Duration.ofSeconds(30))
 *     .onMissed(id -> log.warn("Heartbeat missed: {}", id))
 *     .build();
 *
 * monitor.watch("service-a", Duration.ofMinutes(1));
 * monitor.start();
 *
 * // In the monitored component:
 * monitor.heartbeat("service-a");
 *
 * // On shutdown:
 * monitor.stop();
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
public class HeartbeatMonitor implements AutoCloseable {

    private static final System.Logger LOGGER = System.getLogger(HeartbeatMonitor.class.getName());

    /** id -> expected max interval between heartbeats / ID -> 心跳之间的期望最大间隔 */
    private final ConcurrentHashMap<String, Duration> intervals = new ConcurrentHashMap<>();

    /** id -> time of last heartbeat / ID -> 上次心跳时间 */
    private final ConcurrentHashMap<String, Instant> lastHeartbeats = new ConcurrentHashMap<>();

    private final Duration checkPeriod;
    private final Consumer<String> onMissed;
    private volatile ScheduledExecutorService checker;

    private HeartbeatMonitor(Builder builder) {
        this.checkPeriod = builder.checkPeriod;
        this.onMissed = builder.onMissed;
    }

    /**
     * Create a new builder.
     * 创建新的构建器。
     *
     * @return a new builder instance / 新的构建器实例
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Register an item to be watched for missed heartbeats.
     * 注册一个需要监控心跳的项目。
     *
     * <p>The clock starts from the moment of registration, so the first missed
     * callback fires only if no heartbeat arrives within {@code expectedInterval}.</p>
     *
     * <p>时钟从注册时刻开始计时，仅当在 {@code expectedInterval} 内未收到心跳时
     * 才触发首次超时回调。</p>
     *
     * @param id               the unique identifier of the watched item / 被监控项目的唯一标识符
     * @param expectedInterval maximum time between heartbeats / 心跳之间的最大时间间隔
     * @throws NullPointerException if id or expectedInterval is null / 如果 id 或 expectedInterval 为 null
     */
    public void watch(String id, Duration expectedInterval) {
        Objects.requireNonNull(id, "id must not be null / id 不能为空");
        Objects.requireNonNull(expectedInterval, "expectedInterval must not be null / expectedInterval 不能为空");
        intervals.put(id, expectedInterval);
        lastHeartbeats.putIfAbsent(id, Instant.now());
    }

    /**
     * Record a heartbeat for the given item.
     * 为指定项目记录一次心跳。
     *
     * @param id the unique identifier of the watched item / 被监控项目的唯一标识符
     * @throws NullPointerException if id is null / 如果 id 为 null
     */
    public void heartbeat(String id) {
        Objects.requireNonNull(id, "id must not be null / id 不能为空");
        lastHeartbeats.put(id, Instant.now());
    }

    /**
     * Stop watching the given item.
     * 停止监控指定项目。
     *
     * @param id the unique identifier of the watched item / 被监控项目的唯一标识符
     */
    public void unwatch(String id) {
        if (id != null) {
            intervals.remove(id);
            lastHeartbeats.remove(id);
        }
    }

    /**
     * Return the IDs of items that have missed their heartbeat (exceeded their expected interval).
     * 返回已超时（超出期望间隔）的项目ID集合。
     *
     * @return immutable set of missed item IDs / 超时项目ID的不可变集合
     */
    public Set<String> getMissedIds() {
        Instant now = Instant.now();
        return intervals.entrySet().stream()
                .filter(entry -> {
                    Instant last = lastHeartbeats.get(entry.getKey());
                    return last != null && Duration.between(last, now).compareTo(entry.getValue()) > 0;
                })
                .map(Map.Entry::getKey)
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Return the number of items currently being watched.
     * 返回当前正在监控的项目数量。
     *
     * @return watched item count / 被监控项目数量
     */
    public int watchedCount() {
        return intervals.size();
    }

    /**
     * Start the periodic heartbeat checker.
     * 启动周期性心跳检查。
     *
     * <p>Uses a virtual thread for the scheduled check loop.</p>
     * <p>使用虚拟线程执行定时检查循环。</p>
     */
    public synchronized void start() {
        if (checker != null) {
            return;
        }
        checker = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = Thread.ofVirtual().name("heartbeat-monitor-checker").factory().newThread(r);
            t.setDaemon(true);
            return t;
        });
        checker.scheduleAtFixedRate(
                this::checkAll,
                checkPeriod.toMillis(),
                checkPeriod.toMillis(),
                TimeUnit.MILLISECONDS);
    }

    /**
     * Stop the periodic heartbeat checker.
     * 停止周期性心跳检查。
     */
    public synchronized void stop() {
        if (checker != null) {
            checker.shutdownNow();
            checker = null;
        }
    }

    /**
     * Alias for {@link #stop()}, implements {@link AutoCloseable}.
     * {@link #stop()} 的别名，实现 {@link AutoCloseable}。
     */
    @Override
    public void close() {
        stop();
    }

    private void checkAll() {
        Instant now = Instant.now();
        for (Map.Entry<String, Duration> entry : intervals.entrySet()) {
            String id = entry.getKey();
            Duration maxInterval = entry.getValue();
            Instant lastBeat = lastHeartbeats.get(id);
            if (lastBeat != null && Duration.between(lastBeat, now).compareTo(maxInterval) > 0) {
                if (onMissed != null) {
                    try {
                        onMissed.accept(id);
                    } catch (Exception e) {
                        // Swallow callback exceptions to keep the checker alive.
                        // 吞掉回调异常以保持检查器运行。
                        LOGGER.log(System.Logger.Level.WARNING,
                                "Heartbeat missed-callback threw for id ''{0}'': {1}", id, e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Builder for {@link HeartbeatMonitor}.
     * {@link HeartbeatMonitor} 的构建器。
     *
     * @author Leon Soo
     * <a href="https://leonsoo.com">www.LeonSoo.com</a>
     * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
     * @since JDK 25, opencode-base-event V1.0.0
     */
    public static class Builder {

        private Duration checkPeriod = Duration.ofSeconds(60);
        private Consumer<String> onMissed;

        private Builder() {}

        /**
         * Set the check period (how often missed heartbeats are scanned).
         * 设置检查周期（扫描心跳超时的频率）。
         *
         * <p>Default: 60 seconds.</p>
         * <p>默认值：60秒。</p>
         *
         * @param checkPeriod the check period / 检查周期
         * @return this builder / 当前构建器
         */
        public Builder checkPeriod(Duration checkPeriod) {
            this.checkPeriod = Objects.requireNonNull(checkPeriod, "checkPeriod must not be null");
            return this;
        }

        /**
         * Set the callback invoked when a heartbeat is missed.
         * 设置心跳超时时调用的回调。
         *
         * <p>The callback receives the ID of the item that missed its heartbeat.
         * Exceptions thrown by the callback are silently swallowed to keep the
         * monitor running.</p>
         *
         * <p>回调接收超时项目的ID。回调抛出的异常将被静默吞掉以保持监控器运行。</p>
         *
         * @param onMissed the callback / 回调函数
         * @return this builder / 当前构建器
         */
        public Builder onMissed(Consumer<String> onMissed) {
            this.onMissed = onMissed;
            return this;
        }

        /**
         * Build a new {@link HeartbeatMonitor} instance.
         * 构建新的 {@link HeartbeatMonitor} 实例。
         *
         * @return a new HeartbeatMonitor / 新的 HeartbeatMonitor 实例
         */
        public HeartbeatMonitor build() {
            return new HeartbeatMonitor(this);
        }
    }
}
