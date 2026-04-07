package cloud.opencode.base.log.async;

import cloud.opencode.base.log.LogLevel;
import cloud.opencode.base.log.Logger;
import cloud.opencode.base.log.context.MDC;
import cloud.opencode.base.log.marker.Marker;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * Asynchronous Logger - Offloads log output to a virtual thread via a bounded queue
 * 异步日志记录器 - 通过有界队列将日志输出卸载到虚拟线程
 *
 * <p>AsyncLogger wraps a delegate Logger and dispatches log messages asynchronously
 * using a single daemon virtual thread that consumes from a bounded blocking queue.
 * When the queue is full, logging falls back to synchronous execution on the caller
 * thread to ensure no messages are dropped or blocked.</p>
 * <p>AsyncLogger 包装一个委托 Logger，使用单个守护虚拟线程从有界阻塞队列中消费，
 * 异步分发日志消息。当队列满时，日志回退到调用者线程上同步执行，确保消息不会丢失或阻塞。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Asynchronous log dispatch via virtual thread - 通过虚拟线程异步分发日志</li>
 *   <li>Bounded queue with configurable capacity - 可配置容量的有界队列</li>
 *   <li>Backpressure: synchronous fallback when queue is full - 背压：队列满时同步回退</li>
 *   <li>MDC context propagation across threads - MDC 上下文跨线程传播</li>
 *   <li>Flush and graceful shutdown support - 刷新和优雅关闭支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Logger delegate = LoggerFactory.getLogger(MyService.class);
 * try (AsyncLogger asyncLogger = AsyncLogger.wrap(delegate)) {
 *     asyncLogger.info("This is logged asynchronously");
 *     asyncLogger.flush(); // Wait for all pending messages to be processed
 * }
 *
 * // Custom queue capacity
 * AsyncLogger asyncLogger = AsyncLogger.wrap(delegate, 4096);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: No (delegate must not be null) - 空值安全: 否（委托不能为 null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.3
 */
public final class AsyncLogger implements Logger, AutoCloseable {

    /**
     * Default queue capacity.
     * 默认队列容量。
     */
    private static final int DEFAULT_QUEUE_CAPACITY = 8192;

    /**
     * Shutdown timeout in seconds.
     * 关闭超时时间（秒）。
     */
    private static final int SHUTDOWN_TIMEOUT_SECONDS = 5;

    /**
     * Poll timeout in milliseconds for the consumer thread.
     * 消费者线程的轮询超时时间（毫秒）。
     */
    private static final long POLL_TIMEOUT_MS = 100;

    private final Logger delegate;
    private final LinkedBlockingQueue<LogTask> queue;
    private final Thread consumerThread;
    private volatile boolean shutdown;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * Internal log task record holding all data needed to replay a log call.
     * 内部日志任务记录，保存重放日志调用所需的全部数据。
     *
     * @param level      the log level - 日志级别
     * @param message    the log message - 日志消息
     * @param args       the message arguments - 消息参数
     * @param throwable  the throwable - 异常
     * @param marker     the marker - 标记
     * @param threadName the originating thread name - 发起线程名称
     * @param mdcCopy    the MDC context snapshot - MDC 上下文快照
     * @param flushLatch latch to signal flush completion - 用于信号刷新完成的闩锁
     */
    private record LogTask(
            LogLevel level,
            String message,
            Object[] args,
            Throwable throwable,
            Marker marker,
            String threadName,
            Map<String, String> mdcCopy,
            CountDownLatch flushLatch
    ) {
    }

    /**
     * Private constructor. Use factory methods {@link #wrap(Logger)} or {@link #wrap(Logger, int)}.
     * 私有构造函数。使用工厂方法 {@link #wrap(Logger)} 或 {@link #wrap(Logger, int)}。
     *
     * @param delegate      the delegate logger - 委托日志记录器
     * @param queueCapacity the queue capacity - 队列容量
     */
    private AsyncLogger(Logger delegate, int queueCapacity) {
        this.delegate = Objects.requireNonNull(delegate, "Delegate logger must not be null");
        if (queueCapacity <= 0) {
            throw new IllegalArgumentException("Queue capacity must be positive, got: " + queueCapacity);
        }
        this.queue = new LinkedBlockingQueue<>(queueCapacity);
        this.shutdown = false;
        this.consumerThread = Thread.ofVirtual()
                .name("async-logger-" + delegate.getName())
                .start(this::processLoop);
    }

    /**
     * Wraps the given logger with async dispatch using the default queue capacity (8192).
     * 使用默认队列容量（8192）将给定日志记录器包装为异步分发。
     *
     * @param delegate the delegate logger - 委托日志记录器
     * @return a new AsyncLogger instance - 新的 AsyncLogger 实例
     * @throws NullPointerException if delegate is null - 如果委托为 null
     */
    public static AsyncLogger wrap(Logger delegate) {
        return new AsyncLogger(delegate, DEFAULT_QUEUE_CAPACITY);
    }

    /**
     * Wraps the given logger with async dispatch using the specified queue capacity.
     * 使用指定队列容量将给定日志记录器包装为异步分发。
     *
     * @param delegate      the delegate logger - 委托日志记录器
     * @param queueCapacity the queue capacity - 队列容量
     * @return a new AsyncLogger instance - 新的 AsyncLogger 实例
     * @throws NullPointerException     if delegate is null - 如果委托为 null
     * @throws IllegalArgumentException if queueCapacity is not positive - 如果队列容量不为正数
     */
    public static AsyncLogger wrap(Logger delegate, int queueCapacity) {
        return new AsyncLogger(delegate, queueCapacity);
    }

    // ==================== Consumer Loop ====================

    /**
     * Consumer loop that processes log tasks from the queue.
     * 从队列处理日志任务的消费者循环。
     */
    private void processLoop() {
        while (!shutdown || !queue.isEmpty()) {
            try {
                LogTask task = queue.poll(POLL_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                if (task != null) {
                    try {
                        dispatchTask(task);
                    } catch (RuntimeException e) {
                        // Isolate delegate exceptions to prevent consumer thread death
                        System.err.println("[AsyncLogger] Error dispatching log task: "
                                + e.getClass().getName() + ": " + e.getMessage());
                        e.printStackTrace(System.err);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        // Drain remaining tasks after shutdown signal
        LogTask remaining;
        while ((remaining = queue.poll()) != null) {
            try {
                dispatchTask(remaining);
            } catch (RuntimeException e) {
                System.err.println("[AsyncLogger] Error dispatching log task during drain: "
                        + e.getClass().getName() + ": " + e.getMessage());
                e.printStackTrace(System.err);
            }
        }
    }

    /**
     * Dispatches a single log task to the delegate logger.
     * 将单个日志任务分发到委托日志记录器。
     *
     * @param task the log task to dispatch - 要分发的日志任务
     */
    private void dispatchTask(LogTask task) {
        // Handle flush sentinel
        if (task.flushLatch() != null) {
            task.flushLatch().countDown();
            return;
        }

        // Save caller MDC to restore after dispatch (prevents corruption on sync fallback)
        Map<String, String> savedMdc = MDC.getCopyOfContextMap();
        Map<String, String> mdcCopy = task.mdcCopy();
        try {
            if (mdcCopy != null && !mdcCopy.isEmpty()) {
                MDC.setContextMap(mdcCopy);
            }
            dispatchToDelegate(task);
        } finally {
            if (savedMdc != null && !savedMdc.isEmpty()) {
                MDC.setContextMap(savedMdc);
            } else {
                MDC.clear();
            }
        }
    }

    /**
     * Dispatches the log task to the appropriate delegate method.
     * 将日志任务分发到适当的委托方法。
     *
     * @param task the log task - 日志任务
     */
    private void dispatchToDelegate(LogTask task) {
        LogLevel level = task.level();
        String message = task.message();
        Throwable throwable = task.throwable();
        Marker marker = task.marker();
        Object[] args = task.args();

        if (marker != null && throwable != null) {
            switch (level) {
                case WARN -> delegate.warn(marker, message, throwable);
                case ERROR -> delegate.error(marker, message, throwable);
                default -> delegate.log(level, message, throwable);
            }
        } else if (marker != null && args != null) {
            switch (level) {
                case TRACE -> delegate.trace(marker, message, args);
                case DEBUG -> delegate.debug(marker, message, args);
                case INFO -> delegate.info(marker, message, args);
                case WARN -> delegate.warn(marker, message, args);
                case ERROR -> delegate.error(marker, message, args);
                default -> delegate.log(level, message, args);
            }
        } else if (marker != null) {
            switch (level) {
                case TRACE -> delegate.trace(marker, message);
                case DEBUG -> delegate.debug(marker, message);
                case INFO -> delegate.info(marker, message);
                case WARN -> delegate.warn(marker, message);
                case ERROR -> delegate.error(marker, message);
                default -> delegate.log(level, message);
            }
        } else if (throwable != null && args == null) {
            if (level == LogLevel.ERROR && message == null) {
                delegate.error(throwable);
            } else {
                delegate.log(level, message, throwable);
            }
        } else if (args != null) {
            delegate.log(level, message, args);
        } else {
            delegate.log(level, message);
        }
    }

    // ==================== Queue Submission ====================

    /**
     * Enqueues a log task. If the queue is full, falls back to synchronous logging.
     * 将日志任务入队。如果队列满，则回退到同步日志记录。
     *
     * @param task the log task - 日志任务
     */
    private void enqueue(LogTask task) {
        if (!queue.offer(task)) {
            // Backpressure: synchronous fallback
            dispatchTask(task);
        }
    }

    /**
     * Creates a LogTask capturing the current thread name and MDC context.
     * 创建捕获当前线程名称和 MDC 上下文的 LogTask。
     *
     * @param level     the log level - 日志级别
     * @param message   the message - 消息
     * @param args      the arguments - 参数
     * @param throwable the throwable - 异常
     * @param marker    the marker - 标记
     * @return the log task - 日志任务
     */
    private LogTask createTask(LogLevel level, String message, Object[] args,
                               Throwable throwable, Marker marker) {
        Map<String, String> mdcCopy = MDC.getCopyOfContextMap();
        if (mdcCopy == null) {
            mdcCopy = Collections.emptyMap();
        }
        return new LogTask(level, message, args, throwable, marker,
                Thread.currentThread().getName(), mdcCopy, null);
    }

    // ==================== Flush & Shutdown ====================

    /**
     * Blocks until all currently queued log tasks have been processed, or timeout expires.
     * 阻塞直到所有当前排队的日志任务被处理，或超时到期。
     *
     * @throws InterruptedException if the current thread is interrupted while waiting
     *                              如果当前线程在等待时被中断
     */
    public void flush() throws InterruptedException {
        if (shutdown || !consumerThread.isAlive()) {
            return;
        }
        CountDownLatch latch = new CountDownLatch(1);
        LogTask sentinel = new LogTask(null, null, null, null, null, null, null, latch);
        if (!queue.offer(sentinel, SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            System.err.println("[AsyncLogger] flush() timed out enqueueing sentinel (queue size="
                    + queue.size() + ")");
            return;
        }
        if (!latch.await(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            System.err.println("[AsyncLogger] flush() timed out waiting for consumer drain");
        }
    }

    /**
     * Initiates graceful shutdown. Waits up to 5 seconds for queue drain, then interrupts.
     * 启动优雅关闭。等待最多 5 秒让队列清空，然后中断。
     */
    public void shutdown() {
        if (closed.compareAndSet(false, true)) {
            shutdown = true;
            try {
                consumerThread.join(TimeUnit.SECONDS.toMillis(SHUTDOWN_TIMEOUT_SECONDS));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            if (consumerThread.isAlive()) {
                consumerThread.interrupt();
            }
        }
    }

    /**
     * Closes this async logger, equivalent to {@link #shutdown()}.
     * 关闭此异步日志记录器，等同于 {@link #shutdown()}。
     */
    @Override
    public void close() {
        shutdown();
    }

    // ==================== Logger Interface - Name & Enabled ====================

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return delegate.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTraceEnabled() {
        return delegate.isTraceEnabled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTraceEnabled(Marker marker) {
        return delegate.isTraceEnabled(marker);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDebugEnabled() {
        return delegate.isDebugEnabled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDebugEnabled(Marker marker) {
        return delegate.isDebugEnabled(marker);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isInfoEnabled() {
        return delegate.isInfoEnabled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isInfoEnabled(Marker marker) {
        return delegate.isInfoEnabled(marker);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isWarnEnabled() {
        return delegate.isWarnEnabled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isWarnEnabled(Marker marker) {
        return delegate.isWarnEnabled(marker);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isErrorEnabled() {
        return delegate.isErrorEnabled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isErrorEnabled(Marker marker) {
        return delegate.isErrorEnabled(marker);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled(LogLevel level) {
        return delegate.isEnabled(level);
    }

    // ==================== TRACE Level ====================

    /**
     * {@inheritDoc}
     */
    @Override
    public void trace(String message) {
        if (isTraceEnabled()) {
            enqueue(createTask(LogLevel.TRACE, message, null, null, null));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trace(String format, Object... args) {
        if (isTraceEnabled()) {
            enqueue(createTask(LogLevel.TRACE, format, args, null, null));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trace(String message, Throwable throwable) {
        if (isTraceEnabled()) {
            enqueue(createTask(LogLevel.TRACE, message, null, throwable, null));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trace(Supplier<String> messageSupplier) {
        if (isTraceEnabled()) {
            enqueue(createTask(LogLevel.TRACE, messageSupplier.get(), null, null, null));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trace(Marker marker, String message) {
        if (isTraceEnabled(marker)) {
            enqueue(createTask(LogLevel.TRACE, message, null, null, marker));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trace(Marker marker, String format, Object... args) {
        if (isTraceEnabled(marker)) {
            enqueue(createTask(LogLevel.TRACE, format, args, null, marker));
        }
    }

    // ==================== DEBUG Level ====================

    /**
     * {@inheritDoc}
     */
    @Override
    public void debug(String message) {
        if (isDebugEnabled()) {
            enqueue(createTask(LogLevel.DEBUG, message, null, null, null));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void debug(String format, Object... args) {
        if (isDebugEnabled()) {
            enqueue(createTask(LogLevel.DEBUG, format, args, null, null));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void debug(String message, Throwable throwable) {
        if (isDebugEnabled()) {
            enqueue(createTask(LogLevel.DEBUG, message, null, throwable, null));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void debug(Supplier<String> messageSupplier) {
        if (isDebugEnabled()) {
            enqueue(createTask(LogLevel.DEBUG, messageSupplier.get(), null, null, null));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void debug(Marker marker, String message) {
        if (isDebugEnabled(marker)) {
            enqueue(createTask(LogLevel.DEBUG, message, null, null, marker));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void debug(Marker marker, String format, Object... args) {
        if (isDebugEnabled(marker)) {
            enqueue(createTask(LogLevel.DEBUG, format, args, null, marker));
        }
    }

    // ==================== INFO Level ====================

    /**
     * {@inheritDoc}
     */
    @Override
    public void info(String message) {
        if (isInfoEnabled()) {
            enqueue(createTask(LogLevel.INFO, message, null, null, null));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void info(String format, Object... args) {
        if (isInfoEnabled()) {
            enqueue(createTask(LogLevel.INFO, format, args, null, null));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void info(String message, Throwable throwable) {
        if (isInfoEnabled()) {
            enqueue(createTask(LogLevel.INFO, message, null, throwable, null));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void info(Supplier<String> messageSupplier) {
        if (isInfoEnabled()) {
            enqueue(createTask(LogLevel.INFO, messageSupplier.get(), null, null, null));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void info(Marker marker, String message) {
        if (isInfoEnabled(marker)) {
            enqueue(createTask(LogLevel.INFO, message, null, null, marker));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void info(Marker marker, String format, Object... args) {
        if (isInfoEnabled(marker)) {
            enqueue(createTask(LogLevel.INFO, format, args, null, marker));
        }
    }

    // ==================== WARN Level ====================

    /**
     * {@inheritDoc}
     */
    @Override
    public void warn(String message) {
        if (isWarnEnabled()) {
            enqueue(createTask(LogLevel.WARN, message, null, null, null));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void warn(String format, Object... args) {
        if (isWarnEnabled()) {
            enqueue(createTask(LogLevel.WARN, format, args, null, null));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void warn(String message, Throwable throwable) {
        if (isWarnEnabled()) {
            enqueue(createTask(LogLevel.WARN, message, null, throwable, null));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void warn(Supplier<String> messageSupplier) {
        if (isWarnEnabled()) {
            enqueue(createTask(LogLevel.WARN, messageSupplier.get(), null, null, null));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void warn(Marker marker, String message) {
        if (isWarnEnabled(marker)) {
            enqueue(createTask(LogLevel.WARN, message, null, null, marker));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void warn(Marker marker, String format, Object... args) {
        if (isWarnEnabled(marker)) {
            enqueue(createTask(LogLevel.WARN, format, args, null, marker));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void warn(Marker marker, String message, Throwable throwable) {
        if (isWarnEnabled(marker)) {
            enqueue(createTask(LogLevel.WARN, message, null, throwable, marker));
        }
    }

    // ==================== ERROR Level ====================

    /**
     * {@inheritDoc}
     */
    @Override
    public void error(String message) {
        if (isErrorEnabled()) {
            enqueue(createTask(LogLevel.ERROR, message, null, null, null));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error(String format, Object... args) {
        if (isErrorEnabled()) {
            enqueue(createTask(LogLevel.ERROR, format, args, null, null));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error(String message, Throwable throwable) {
        if (isErrorEnabled()) {
            enqueue(createTask(LogLevel.ERROR, message, null, throwable, null));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error(Throwable throwable) {
        if (isErrorEnabled()) {
            enqueue(createTask(LogLevel.ERROR, null, null, throwable, null));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error(Supplier<String> messageSupplier) {
        if (isErrorEnabled()) {
            enqueue(createTask(LogLevel.ERROR, messageSupplier.get(), null, null, null));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error(Supplier<String> messageSupplier, Throwable throwable) {
        if (isErrorEnabled()) {
            enqueue(createTask(LogLevel.ERROR, messageSupplier.get(), null, throwable, null));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error(Marker marker, String message) {
        if (isErrorEnabled(marker)) {
            enqueue(createTask(LogLevel.ERROR, message, null, null, marker));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error(Marker marker, String format, Object... args) {
        if (isErrorEnabled(marker)) {
            enqueue(createTask(LogLevel.ERROR, format, args, null, marker));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error(Marker marker, String message, Throwable throwable) {
        if (isErrorEnabled(marker)) {
            enqueue(createTask(LogLevel.ERROR, message, null, throwable, marker));
        }
    }

    // ==================== Generic Methods ====================

    /**
     * {@inheritDoc}
     */
    @Override
    public void log(LogLevel level, String message) {
        if (isEnabled(level)) {
            enqueue(createTask(level, message, null, null, null));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void log(LogLevel level, String format, Object... args) {
        if (isEnabled(level)) {
            enqueue(createTask(level, format, args, null, null));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void log(LogLevel level, String message, Throwable throwable) {
        if (isEnabled(level)) {
            enqueue(createTask(level, message, null, throwable, null));
        }
    }
}
