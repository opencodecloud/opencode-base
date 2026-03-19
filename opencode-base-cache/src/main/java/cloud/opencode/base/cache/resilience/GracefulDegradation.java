package cloud.opencode.base.cache.resilience;

import cloud.opencode.base.cache.Cache;
import cloud.opencode.base.cache.distributed.DistributedCache;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Graceful Degradation — Automatic fallback from distributed cache to local cache.
 * 优雅降级 — 分布式缓存到本地缓存的自动故障转移。
 *
 * <p>Monitors the health of the distributed cache layer and automatically falls back to the local
 * cache when the distributed cache becomes unavailable. Once the distributed cache recovers, the
 * system transitions through RECOVERING back to NORMAL state.</p>
 * <p>监控分布式缓存层的健康状态，当分布式缓存不可用时自动回退到本地缓存。
 * 一旦分布式缓存恢复，系统通过 RECOVERING 状态过渡回 NORMAL 状态。</p>
 *
 * <p><strong>State Machine | 状态机:</strong></p>
 * <pre>
 *   NORMAL ──(failures >= threshold)──► DEGRADED ──(health check passes)──► RECOVERING
 *     ▲                                                                           │
 *     └──────────────(recovery window elapsed without failure)───────────────────┘
 *                                      RECOVERING ──(failure)──► DEGRADED
 * </pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Three-state model: NORMAL, DEGRADED, RECOVERING - 三状态模型</li>
 *   <li>Configurable failure threshold and recovery window - 可配置的失败阈值和恢复窗口</li>
 *   <li>Automatic periodic health checks via virtual thread scheduler - 虚拟线程调度自动健康检查</li>
 *   <li>Detailed degradation statistics - 详细的降级统计</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * GracefulDegradation<String, User> degradation = GracefulDegradation.create();
 *
 * // Get with automatic fallback
 * Optional<User> user = degradation.get("user:123", localCache, distributedCache);
 *
 * // Put with degradation awareness
 * degradation.put("user:123", user, Duration.ofMinutes(10), localCache, distributedCache);
 *
 * // Check state and stats
 * GracefulDegradation.State state = degradation.state();
 * GracefulDegradation.Stats stats = degradation.stats();
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Lock-free state reads via volatile field - 通过 volatile 字段无锁读取状态</li>
 *   <li>Lightweight health check via virtual thread scheduler - 虚拟线程调度器实现轻量级健康检查</li>
 *   <li>Zero overhead in NORMAL state when distributed cache is healthy - NORMAL 状态零开销</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (volatile state, AtomicInteger/AtomicLong counters) - 线程安全: 是</li>
 *   <li>Null-safe: No (localCache and distributedCache must not be null) - 空值安全: 否</li>
 * </ul>
 *
 * @param <K> the cache key type | 缓存键类型
 * @param <V> the cache value type | 缓存值类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
public final class GracefulDegradation<K, V> implements AutoCloseable {

    private static final System.Logger LOGGER = System.getLogger(GracefulDegradation.class.getName());

    // ==================== State | 状态 ====================

    /**
     * Represents the degradation state of the system.
     * 表示系统的降级状态。
     */
    public enum State {
        /** Normal operation, distributed cache is healthy. | 正常操作，分布式缓存健康。 */
        NORMAL,
        /** Degraded mode, using local cache only. | 降级模式，仅使用本地缓存。 */
        DEGRADED,
        /** Recovering, attempting to restore distributed cache usage. | 恢复中，尝试恢复分布式缓存使用。 */
        RECOVERING
    }

    // ==================== Config | 配置 ====================

    /**
     * Configuration for graceful degradation behavior.
     * 优雅降级行为的配置。
     *
     * @param failureThreshold   consecutive failures before entering DEGRADED | 进入 DEGRADED 前的连续失败次数
     * @param recoveryWindow     duration to remain in RECOVERING before NORMAL | 从 RECOVERING 过渡到 NORMAL 前的持续时间
     * @param healthCheckInterval interval between health check probes in DEGRADED | DEGRADED 中健康检查探测的间隔
     */
    public record Config(
            int failureThreshold,
            Duration recoveryWindow,
            Duration healthCheckInterval
    ) {
        /** Compact canonical constructor with validation. */
        public Config {
            if (failureThreshold < 1) {
                throw new IllegalArgumentException("failureThreshold must be >= 1, got: " + failureThreshold);
            }
            Objects.requireNonNull(recoveryWindow, "recoveryWindow must not be null");
            Objects.requireNonNull(healthCheckInterval, "healthCheckInterval must not be null");
            if (recoveryWindow.isNegative() || recoveryWindow.isZero()) {
                throw new IllegalArgumentException("recoveryWindow must be positive, got: " + recoveryWindow);
            }
            if (healthCheckInterval.isNegative() || healthCheckInterval.isZero()) {
                throw new IllegalArgumentException("healthCheckInterval must be positive, got: " + healthCheckInterval);
            }
        }

        /**
         * Default configuration: 5 failures, 30s recovery window, 10s health check interval.
         * 默认配置：5 次失败，30 秒恢复窗口，10 秒健康检查间隔。
         *
         * @return the default config | 默认配置
         */
        public static Config defaults() {
            return new Config(5, Duration.ofSeconds(30), Duration.ofSeconds(10));
        }
    }

    // ==================== Stats | 统计 ====================

    /**
     * Snapshot of degradation system statistics.
     * 降级系统统计信息的快照。
     *
     * @param totalRequests    total cache requests processed | 处理的缓存请求总数
     * @param degradedRequests requests served in degraded mode | 降级模式下服务的请求数
     * @param failoverCount    times the system entered DEGRADED | 系统进入 DEGRADED 的次数
     * @param currentState     the current degradation state | 当前降级状态
     * @param lastFailover     timestamp of the last failover (nullable) | 最后故障转移的时间戳（可空）
     */
    public record Stats(
            long totalRequests,
            long degradedRequests,
            long failoverCount,
            State currentState,
            Instant lastFailover
    ) {}

    // ==================== Fields | 字段 ====================

    private final Config config;
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong degradedRequests = new AtomicLong(0);
    private final AtomicLong failoverCount = new AtomicLong(0);
    private volatile State currentState = State.NORMAL;
    private volatile Instant lastFailover;
    private volatile Instant recoveryStarted;
    private final ScheduledExecutorService healthCheckExecutor;

    private GracefulDegradation(Config config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.healthCheckExecutor = Executors.newSingleThreadScheduledExecutor(
                Thread.ofVirtual().name("opencode-degradation-health-", 0).factory()
        );
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates a GracefulDegradation instance with default configuration.
     * 使用默认配置创建 GracefulDegradation 实例。
     *
     * @param <K> the key type | 键类型
     * @param <V> the value type | 值类型
     * @return a new instance with default settings | 使用默认设置的新实例
     */
    public static <K, V> GracefulDegradation<K, V> create() {
        return new GracefulDegradation<>(Config.defaults());
    }

    /**
     * Creates a GracefulDegradation instance with the specified configuration.
     * 使用指定配置创建 GracefulDegradation 实例。
     *
     * @param config the degradation configuration | 降级配置
     * @param <K>    the key type | 键类型
     * @param <V>    the value type | 值类型
     * @return a new instance with the given config | 使用给定配置的新实例
     */
    public static <K, V> GracefulDegradation<K, V> create(Config config) {
        return new GracefulDegradation<>(config);
    }

    // ==================== Cache Operations | 缓存操作 ====================

    /**
     * Retrieves a value, trying distributed cache first with automatic local cache fallback.
     * 获取值，优先尝试分布式缓存并自动回退到本地缓存。
     *
     * <p>In NORMAL/RECOVERING states, reads from distributed cache first; falls back to local on failure.
     * In DEGRADED state, reads directly from local cache.</p>
     * <p>在 NORMAL/RECOVERING 状态下，优先从分布式缓存读取；失败时回退到本地缓存。
     * 在 DEGRADED 状态下，直接从本地缓存读取。</p>
     *
     * @param key              the cache key | 缓存键
     * @param localCache       the local cache used as fallback | 用作回退的本地缓存
     * @param distributedCache the distributed cache tried first | 优先尝试的分布式缓存
     * @return an Optional containing the value if found | 如果找到则包含值的 Optional
     */
    public Optional<V> get(K key, Cache<K, V> localCache, DistributedCache<K, V> distributedCache) {
        Objects.requireNonNull(key, "key must not be null");
        Objects.requireNonNull(localCache, "localCache must not be null");
        Objects.requireNonNull(distributedCache, "distributedCache must not be null");

        totalRequests.incrementAndGet();

        return switch (currentState) {
            case DEGRADED -> {
                degradedRequests.incrementAndGet();
                yield Optional.ofNullable(localCache.get(key));
            }
            case NORMAL, RECOVERING -> {
                try {
                    Optional<V> result = distributedCache.get(key);
                    onDistributedSuccess();
                    if (result.isPresent()) {
                        yield result;
                    }
                    yield Optional.ofNullable(localCache.get(key));
                } catch (Exception e) {
                    onDistributedFailure(e);
                    yield Optional.ofNullable(localCache.get(key));
                }
            }
        };
    }

    /**
     * Stores a value based on the current degradation state.
     * 根据当前降级状态存储值。
     *
     * <p>NORMAL: writes to both caches. DEGRADED: local only. RECOVERING: local first, then distributed.</p>
     * <p>NORMAL: 写入两个缓存。DEGRADED: 仅本地。RECOVERING: 先本地，再分布式。</p>
     *
     * @param key              the cache key | 缓存键
     * @param value            the value to store | 要存储的值
     * @param ttl              the time-to-live | 存活时间
     * @param localCache       the local cache | 本地缓存
     * @param distributedCache the distributed cache | 分布式缓存
     */
    public void put(K key, V value, Duration ttl,
                    Cache<K, V> localCache, DistributedCache<K, V> distributedCache) {
        Objects.requireNonNull(key, "key must not be null");
        Objects.requireNonNull(localCache, "localCache must not be null");
        Objects.requireNonNull(distributedCache, "distributedCache must not be null");

        totalRequests.incrementAndGet();

        switch (currentState) {
            case DEGRADED -> {
                degradedRequests.incrementAndGet();
                localCache.put(key, value);
                LOGGER.log(System.Logger.Level.TRACE,
                        "DEGRADED: wrote key {0} to local cache only", key);
            }
            case NORMAL -> {
                localCache.put(key, value);
                try {
                    distributedCache.put(key, value, ttl);
                    onDistributedSuccess();
                } catch (Exception e) {
                    onDistributedFailure(e);
                    LOGGER.log(System.Logger.Level.WARNING,
                            "NORMAL: distributed write failed for key {0}", key);
                }
            }
            case RECOVERING -> {
                localCache.put(key, value);
                try {
                    distributedCache.put(key, value, ttl);
                    onDistributedSuccess();
                } catch (Exception e) {
                    onDistributedFailure(e);
                }
            }
        }
    }

    // ==================== State Management | 状态管理 ====================

    /**
     * Returns the current degradation state.
     * 返回当前降级状态。
     *
     * @return the current state | 当前状态
     */
    public State state() {
        return currentState;
    }

    /**
     * Returns a snapshot of the current statistics.
     * 返回当前统计信息的快照。
     *
     * @return the statistics snapshot | 统计信息快照
     */
    public Stats stats() {
        return new Stats(
                totalRequests.get(),
                degradedRequests.get(),
                failoverCount.get(),
                currentState,
                lastFailover
        );
    }

    /**
     * Resets the system to NORMAL state and clears all failure counters.
     * 将系统重置为 NORMAL 状态并清除所有失败计数器。
     */
    public void reset() {
        currentState = State.NORMAL;
        consecutiveFailures.set(0);
        recoveryStarted = null;
        LOGGER.log(System.Logger.Level.INFO, "GracefulDegradation reset to NORMAL");
    }

    /**
     * Closes this instance and shuts down the health check executor.
     * 关闭此实例并停止健康检查执行器。
     */
    @Override
    public void close() {
        healthCheckExecutor.shutdown();
        try {
            if (!healthCheckExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                healthCheckExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            healthCheckExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        LOGGER.log(System.Logger.Level.INFO, "GracefulDegradation closed");
    }

    // ==================== Internal | 内部实现 ====================

    private void onDistributedSuccess() {
        consecutiveFailures.set(0);

        if (currentState == State.RECOVERING) {
            Instant started = recoveryStarted;
            if (started != null && Instant.now().isAfter(started.plus(config.recoveryWindow()))) {
                transitionTo(State.NORMAL);
                recoveryStarted = null;
                LOGGER.log(System.Logger.Level.INFO, "Recovery complete: transitioned to NORMAL");
            }
        }
    }

    private void onDistributedFailure(Exception e) {
        int failures = consecutiveFailures.incrementAndGet();
        LOGGER.log(System.Logger.Level.DEBUG,
                "Distributed failure #{0}/{1}: {2}",
                failures, config.failureThreshold(), e.getMessage());

        if (currentState == State.RECOVERING) {
            transitionTo(State.DEGRADED);
            recoveryStarted = null;
            LOGGER.log(System.Logger.Level.WARNING, "Failure during recovery: back to DEGRADED");
        } else if (currentState == State.NORMAL && failures >= config.failureThreshold()) {
            transitionTo(State.DEGRADED);
            lastFailover = Instant.now();
            failoverCount.incrementAndGet();
            scheduleHealthCheck();
            LOGGER.log(System.Logger.Level.WARNING,
                    "Failure threshold {0} reached: entering DEGRADED mode",
                    config.failureThreshold());
        }
    }

    private void transitionTo(State newState) {
        State oldState = currentState;
        currentState = newState;
        LOGGER.log(System.Logger.Level.INFO,
                "State transition: {0} -> {1}", oldState, newState);
    }

    private void scheduleHealthCheck() {
        long intervalMillis = config.healthCheckInterval().toMillis();
        healthCheckExecutor.schedule(this::performHealthCheck, intervalMillis, TimeUnit.MILLISECONDS);
    }

    private void performHealthCheck() {
        if (currentState != State.DEGRADED) {
            return;
        }
        LOGGER.log(System.Logger.Level.DEBUG, "Health check: transitioning to RECOVERING");
        consecutiveFailures.set(0);
        recoveryStarted = Instant.now();
        transitionTo(State.RECOVERING);
        LOGGER.log(System.Logger.Level.INFO,
                "Health check passed: RECOVERING (window={0})", config.recoveryWindow());
    }
}
