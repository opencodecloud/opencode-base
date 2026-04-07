package cloud.opencode.base.observability.metric;

import cloud.opencode.base.observability.exception.ObservabilityException;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;

/**
 * DefaultHistogram - Thread-safe histogram with ring-buffer percentile support
 * DefaultHistogram - 带环形缓冲区百分位数支持的线程安全直方图
 *
 * <p>Uses a fixed-size ring buffer (8192 entries) for approximate percentile calculation
 * and lock-free atomic operations for count, sum, and max tracking.</p>
 * <p>使用固定大小的环形缓冲区（8192 条目）进行近似百分位数计算，
 * 使用无锁原子操作跟踪计数、总和和最大值。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-observability V1.0.3
 */
final class DefaultHistogram implements Histogram {

    private static final VarHandle MAX_HANDLE;

    static {
        try {
            MAX_HANDLE = MethodHandles.lookup()
                    .findVarHandle(DefaultHistogram.class, "maxValue", double.class);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static final int BUFFER_SIZE = 8192;

    private final MetricId id;
    private final LongAdder count = new LongAdder();
    private final DoubleAdder totalSum = new DoubleAdder();
    @SuppressWarnings("unused") // accessed via VarHandle
    private volatile double maxValue = 0.0;
    private final double[] buffer = new double[BUFFER_SIZE];
    private final AtomicLong writeIndex = new AtomicLong();

    DefaultHistogram(MetricId id) {
        this.id = id;
    }

    @Override
    public void record(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new ObservabilityException("INVALID_METRIC",
                    "Histogram value must be finite, got: " + value);
        }
        totalSum.add(value);
        // CAS update max
        double current;
        do {
            current = (double) MAX_HANDLE.get(this);
        } while (value > current && !MAX_HANDLE.compareAndSet(this, current, value));
        // Write to ring buffer first, then increment count (so readers never see count > actual writes)
        long idx = writeIndex.getAndIncrement();
        buffer[(int) (idx & (BUFFER_SIZE - 1))] = value;
        count.increment();
    }

    @Override
    public long count() {
        return count.sum();
    }

    @Override
    public double totalAmount() {
        return totalSum.sum();
    }

    @Override
    public double max() {
        return (double) MAX_HANDLE.get(this);
    }

    @Override
    public double mean() {
        long cnt = count.sum();
        if (cnt == 0) {
            return 0.0;
        }
        return totalSum.sum() / cnt;
    }

    @Override
    public double percentile(double p) {
        if (p < 0.0 || p > 1.0) {
            throw new ObservabilityException("INVALID_METRIC",
                    "Percentile must be in range [0.0, 1.0], got: " + p);
        }
        // Use writeIndex (not count) for sizing: writeIndex is incremented BEFORE the buffer
        // write and before count, so it reflects the number of slots that have been assigned.
        // This avoids reading uninitialized (zero) buffer slots.
        // 使用 writeIndex（而非 count）确定大小：writeIndex 在缓冲区写入前递增，
        // 反映已分配的槽位数，避免读取未初始化的零值槽位。
        long wIdx = writeIndex.get();
        if (wIdx == 0) {
            return 0.0;
        }
        int size = (int) Math.min(wIdx, BUFFER_SIZE);
        double[] sorted = new double[size];
        if (wIdx <= BUFFER_SIZE) {
            System.arraycopy(buffer, 0, sorted, 0, size);
        } else {
            int start = (int) (wIdx & (BUFFER_SIZE - 1));
            System.arraycopy(buffer, start, sorted, 0, BUFFER_SIZE - start);
            System.arraycopy(buffer, 0, sorted, BUFFER_SIZE - start, start);
        }
        Arrays.sort(sorted);
        int index = (int) Math.ceil(p * size) - 1;
        return sorted[Math.max(0, Math.min(index, size - 1))];
    }

    @Override
    public MetricId id() {
        return id;
    }
}
