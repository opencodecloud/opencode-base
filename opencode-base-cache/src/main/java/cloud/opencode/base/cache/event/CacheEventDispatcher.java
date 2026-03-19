package cloud.opencode.base.cache.event;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Cache Event Dispatcher - Manages event listeners and dispatches events
 * 缓存事件分发器 - 管理事件监听器并分发事件
 *
 * <p>Central component for the cache event system. Supports synchronous
 * and asynchronous event dispatch with configurable error handling.</p>
 * <p>缓存事件系统的核心组件。支持同步和异步事件分发，可配置错误处理。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Synchronous and async dispatch - 同步和异步分发</li>
 *   <li>Event filtering by type - 按类型过滤事件</li>
 *   <li>Error isolation - 错误隔离</li>
 *   <li>Metrics collection - 指标收集</li>
 *   <li>Graceful shutdown - 优雅关闭</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create dispatcher - 创建分发器
 * CacheEventDispatcher<String, User> dispatcher = CacheEventDispatcher.create();
 *
 * // Register listeners - 注册监听器
 * dispatcher.addListener(event -> log.info("Event: {}", event));
 * dispatcher.addListener(CacheEventListener.onEvict(event ->
 *     metrics.recordEviction(event.cacheName())));
 *
 * // Dispatch events - 分发事件
 * dispatcher.dispatch(CacheEvent.put("users", "user1", user));
 *
 * // Async dispatch - 异步分发
 * dispatcher.dispatchAsync(CacheEvent.evict("users", "user2", oldUser, RemovalCause.SIZE));
 *
 * // Shutdown - 关闭
 * dispatcher.close();
 * }</pre>
 *
 * @param <K> key type | 键类型
 * @param <V> value type | 值类型
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (uses CopyOnWriteArrayList and atomic operations) - 线程安全: 是（使用 CopyOnWriteArrayList 和原子操作）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
public final class CacheEventDispatcher<K, V> implements AutoCloseable {

    private static final System.Logger LOGGER = System.getLogger(CacheEventDispatcher.class.getName());

    private final List<CacheEventListener<K, V>> listeners = new CopyOnWriteArrayList<>();
    private final ExecutorService asyncExecutor;
    private final EventErrorHandler<K, V> errorHandler;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    // Metrics
    private final AtomicLong totalEventsDispatched = new AtomicLong(0);
    private final AtomicLong totalEventsDropped = new AtomicLong(0);
    private final AtomicLong totalErrors = new AtomicLong(0);

    private CacheEventDispatcher(ExecutorService asyncExecutor, EventErrorHandler<K, V> errorHandler) {
        this.asyncExecutor = asyncExecutor;
        this.errorHandler = errorHandler;
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Create dispatcher with default settings
     * 使用默认设置创建分发器
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return dispatcher | 分发器
     */
    public static <K, V> CacheEventDispatcher<K, V> create() {
        return CacheEventDispatcher.<K, V>builder().build();
    }

    /**
     * Create dispatcher builder
     * 创建分发器构建器
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     * @return builder | 构建器
     */
    public static <K, V> Builder<K, V> builder() {
        return new Builder<>();
    }

    // ==================== Listener Management | 监听器管理 ====================

    /**
     * Add event listener
     * 添加事件监听器
     *
     * @param listener the listener | 监听器
     * @return this dispatcher | 此分发器
     */
    public CacheEventDispatcher<K, V> addListener(CacheEventListener<K, V> listener) {
        Objects.requireNonNull(listener, "listener must not be null");
        listeners.add(listener);
        return this;
    }

    /**
     * Remove event listener
     * 移除事件监听器
     *
     * @param listener the listener | 监听器
     * @return true if removed | 如果移除成功返回 true
     */
    public boolean removeListener(CacheEventListener<K, V> listener) {
        return listeners.remove(listener);
    }

    /**
     * Remove all listeners
     * 移除所有监听器
     */
    public void clearListeners() {
        listeners.clear();
    }

    /**
     * Get number of registered listeners
     * 获取注册的监听器数量
     *
     * @return listener count | 监听器数量
     */
    public int listenerCount() {
        return listeners.size();
    }

    // ==================== Event Dispatch | 事件分发 ====================

    /**
     * Dispatch event synchronously to all listeners
     * 同步分发事件到所有监听器
     *
     * @param event the event | 事件
     */
    public void dispatch(CacheEvent<K, V> event) {
        Objects.requireNonNull(event, "event must not be null");

        if (closed.get()) {
            totalEventsDropped.incrementAndGet();
            return;
        }

        totalEventsDispatched.incrementAndGet();

        for (CacheEventListener<K, V> listener : listeners) {
            if (listener.isInterestedIn(event.type())) {
                try {
                    listener.onEvent(event);
                } catch (Exception e) {
                    totalErrors.incrementAndGet();
                    errorHandler.handleError(event, listener, e);
                }
            }
        }
    }

    /**
     * Dispatch event asynchronously
     * 异步分发事件
     *
     * @param event the event | 事件
     * @return future completed when all listeners processed | 所有监听器处理完成时完成的 Future
     */
    public CompletableFuture<Void> dispatchAsync(CacheEvent<K, V> event) {
        Objects.requireNonNull(event, "event must not be null");

        if (closed.get()) {
            totalEventsDropped.incrementAndGet();
            return CompletableFuture.completedFuture(null);
        }

        try {
            return CompletableFuture.runAsync(() -> dispatch(event), asyncExecutor);
        } catch (RejectedExecutionException e) {
            // Executor was shut down between our closed check and submission
            totalEventsDropped.incrementAndGet();
            return CompletableFuture.completedFuture(null);
        }
    }

    /**
     * Dispatch multiple events synchronously
     * 同步分发多个事件
     *
     * @param events the events | 事件集合
     */
    public void dispatchAll(Iterable<CacheEvent<K, V>> events) {
        for (CacheEvent<K, V> event : events) {
            dispatch(event);
        }
    }

    /**
     * Dispatch multiple events asynchronously
     * 异步分发多个事件
     *
     * @param events the events | 事件集合
     * @return future completed when all events processed | 所有事件处理完成时完成的 Future
     */
    public CompletableFuture<Void> dispatchAllAsync(Iterable<CacheEvent<K, V>> events) {
        if (closed.get()) {
            totalEventsDropped.incrementAndGet();
            return CompletableFuture.completedFuture(null);
        }
        try {
            return CompletableFuture.runAsync(() -> dispatchAll(events), asyncExecutor);
        } catch (RejectedExecutionException e) {
            totalEventsDropped.incrementAndGet();
            return CompletableFuture.completedFuture(null);
        }
    }

    /**
     * Dispatch event with timeout (V2.0.4)
     * 带超时分发事件
     *
     * @param event   the event | 事件
     * @param timeout max wait time | 最大等待时间
     * @return true if completed within timeout | 如果在超时内完成返回 true
     * @since V2.0.4
     */
    public boolean dispatchWithTimeout(CacheEvent<K, V> event, java.time.Duration timeout) {
        try {
            return dispatchAsync(event).orTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                    .handle((v, ex) -> ex == null)
                    .join();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Dispatch event to specific listener only (V2.0.4)
     * 仅分发事件到特定监听器
     *
     * @param event    the event | 事件
     * @param listener the target listener | 目标监听器
     * @since V2.0.4
     */
    public void dispatchTo(CacheEvent<K, V> event, CacheEventListener<K, V> listener) {
        Objects.requireNonNull(event, "event must not be null");
        Objects.requireNonNull(listener, "listener must not be null");

        if (closed.get()) {
            totalEventsDropped.incrementAndGet();
            return;
        }

        if (listener.isInterestedIn(event.type())) {
            try {
                listener.onEvent(event);
                totalEventsDispatched.incrementAndGet();
            } catch (Exception e) {
                totalErrors.incrementAndGet();
                errorHandler.handleError(event, listener, e);
            }
        }
    }

    /**
     * Check if any listener is interested in the event type (V2.0.4)
     * 检查是否有任何监听器对事件类型感兴趣
     *
     * @param eventType the event type | 事件类型
     * @return true if any listener interested | 如果有监听器感兴趣返回 true
     * @since V2.0.4
     */
    public boolean hasListenersFor(CacheEvent.EventType eventType) {
        for (CacheEventListener<K, V> listener : listeners) {
            if (listener.isInterestedIn(eventType)) {
                return true;
            }
        }
        return false;
    }

    // ==================== Metrics | 指标 ====================

    /**
     * Get dispatcher metrics
     * 获取分发器指标
     *
     * @return metrics | 指标
     */
    public Metrics getMetrics() {
        return new Metrics(
                listeners.size(),
                totalEventsDispatched.get(),
                totalEventsDropped.get(),
                totalErrors.get()
        );
    }

    /**
     * Reset metrics counters
     * 重置指标计数器
     */
    public void resetMetrics() {
        totalEventsDispatched.set(0);
        totalEventsDropped.set(0);
        totalErrors.set(0);
    }

    /**
     * Dispatcher metrics
     * 分发器指标
     */
    public record Metrics(
            int listenerCount,
            long eventsDispatched,
            long eventsDropped,
            long errors
    ) {
        /**
         * Get error rate
         * 获取错误率
         *
         * @return error rate (0.0 to 1.0) | 错误率 (0.0 到 1.0)
         */
        public double errorRate() {
            return eventsDispatched == 0 ? 0.0 : (double) errors / eventsDispatched;
        }
    }

    // ==================== Lifecycle | 生命周期 ====================

    /**
     * Check if dispatcher is closed
     * 检查分发器是否已关闭
     *
     * @return true if closed | 如果已关闭返回 true
     */
    public boolean isClosed() {
        return closed.get();
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            asyncExecutor.shutdown();
            try {
                if (!asyncExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                    asyncExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                asyncExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    // ==================== Error Handler | 错误处理器 ====================

    /**
     * Error handler for listener exceptions
     * 监听器异常的错误处理器
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     */
    @FunctionalInterface
    public interface EventErrorHandler<K, V> {
        /**
         * Handle listener error
         * 处理监听器错误
         *
         * @param event    the event | 事件
         * @param listener the listener that failed | 失败的监听器
         * @param error    the exception | 异常
         */
        void handleError(CacheEvent<K, V> event, CacheEventListener<K, V> listener, Exception error);

        /**
         * Default handler that logs and continues
         * 默认处理器：记录日志并继续
         *
         * @param <K> key type | 键类型
         * @param <V> value type | 值类型
         * @return default handler | 默认处理器
         */
        static <K, V> EventErrorHandler<K, V> logAndContinue() {
            return (event, listener, error) -> {
                System.getLogger(CacheEventDispatcher.class.getName())
                        .log(System.Logger.Level.WARNING, "Error in cache event listener for event " + event.type()
                                + ": " + error.getMessage(), error);
            };
        }

        /**
         * Handler that rethrows exceptions
         * 重新抛出异常的处理器
         *
         * @param <K> key type | 键类型
         * @param <V> value type | 值类型
         * @return rethrowing handler | 重新抛出的处理器
         */
        static <K, V> EventErrorHandler<K, V> rethrow() {
            return (event, listener, error) -> {
                if (error instanceof RuntimeException re) {
                    throw re;
                }
                throw new RuntimeException("Error in cache event listener", error);
            };
        }

        /**
         * Handler that silently ignores errors
         * 静默忽略错误的处理器
         *
         * @param <K> key type | 键类型
         * @param <V> value type | 值类型
         * @return ignoring handler | 忽略的处理器
         */
        static <K, V> EventErrorHandler<K, V> ignore() {
            return (event, listener, error) -> {
                // Silently ignore
            };
        }
    }

    // ==================== Builder | 构建器 ====================

    /**
     * Builder for CacheEventDispatcher
     * CacheEventDispatcher 构建器
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     */
    public static class Builder<K, V> {
        private ExecutorService asyncExecutor;
        private EventErrorHandler<K, V> errorHandler = EventErrorHandler.logAndContinue();

        /**
         * Set async executor
         * 设置异步执行器
         *
         * @param executor the executor | 执行器
         * @return this builder | 此构建器
         */
        public Builder<K, V> asyncExecutor(ExecutorService executor) {
            this.asyncExecutor = executor;
            return this;
        }

        /**
         * Set error handler
         * 设置错误处理器
         *
         * @param errorHandler the error handler | 错误处理器
         * @return this builder | 此构建器
         */
        public Builder<K, V> errorHandler(EventErrorHandler<K, V> errorHandler) {
            this.errorHandler = errorHandler;
            return this;
        }

        /**
         * Build the dispatcher
         * 构建分发器
         *
         * @return dispatcher | 分发器
         */
        public CacheEventDispatcher<K, V> build() {
            ExecutorService executor = asyncExecutor != null
                    ? asyncExecutor
                    : Executors.newVirtualThreadPerTaskExecutor();
            return new CacheEventDispatcher<>(executor, errorHandler);
        }
    }
}
