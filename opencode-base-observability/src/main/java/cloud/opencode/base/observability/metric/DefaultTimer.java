package cloud.opencode.base.observability.metric;

import cloud.opencode.base.observability.exception.ObservabilityException;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.LongAdder;

/**
 * DefaultTimer - Thread-safe timer implementation with CAS-based max tracking
 * DefaultTimer - 使用 CAS 更新最大值的线程安全计时器实现
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-observability V1.0.3
 */
final class DefaultTimer implements Timer {

    private static final VarHandle MAX_HANDLE;

    static {
        try {
            MAX_HANDLE = MethodHandles.lookup()
                    .findVarHandle(DefaultTimer.class, "maxNanos", long.class);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final MetricId id;
    private final LongAdder totalNanos = new LongAdder();
    private final LongAdder count = new LongAdder();
    @SuppressWarnings("unused") // accessed via VarHandle
    private volatile long maxNanos;

    DefaultTimer(MetricId id) {
        this.id = id;
    }

    @Override
    public void record(Duration duration) {
        if (duration == null) {
            throw new ObservabilityException("INVALID_METRIC", "Duration must not be null");
        }
        long nanos = duration.toNanos();
        if (nanos < 0) {
            throw new ObservabilityException("INVALID_METRIC", "Duration must not be negative");
        }
        recordNanos(nanos);
    }

    /**
     * Internal recording method that avoids Duration boxing.
     * 内部记录方法，避免 Duration 装箱。
     */
    private void recordNanos(long nanos) {
        totalNanos.add(nanos);
        count.increment();
        // CAS loop to update max
        long current;
        do {
            current = (long) MAX_HANDLE.get(this);
        } while (nanos > current && !MAX_HANDLE.compareAndSet(this, current, nanos));
    }

    @Override
    public void time(Runnable task) {
        if (task == null) {
            throw new ObservabilityException("INVALID_METRIC", "Task must not be null");
        }
        long start = System.nanoTime();
        try {
            task.run();
        } finally {
            recordNanos(System.nanoTime() - start);
        }
    }

    @Override
    public <T> T time(Callable<T> task) {
        if (task == null) {
            throw new ObservabilityException("INVALID_METRIC", "Task must not be null");
        }
        long start = System.nanoTime();
        try {
            return task.call();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new ObservabilityException("TIMER_ERROR", "Callable task threw a checked exception", e);
        } finally {
            recordNanos(System.nanoTime() - start);
        }
    }

    @Override
    public long count() {
        return count.sum();
    }

    @Override
    public Duration totalTime() {
        return Duration.ofNanos(totalNanos.sum());
    }

    @Override
    public Duration max() {
        return Duration.ofNanos((long) MAX_HANDLE.get(this));
    }

    @Override
    public Duration mean() {
        long cnt = count.sum();
        if (cnt == 0) {
            return Duration.ZERO;
        }
        return Duration.ofNanos(totalNanos.sum() / cnt);
    }

    @Override
    public MetricId id() {
        return id;
    }
}
