package cloud.opencode.base.observability.metric;

import cloud.opencode.base.observability.exception.ObservabilityException;

import java.util.concurrent.atomic.LongAdder;

/**
 * DefaultCounter - Thread-safe counter using LongAdder for high-concurrency throughput
 * DefaultCounter - 使用 LongAdder 的线程安全计数器，优化高并发吞吐量
 *
 * <p>Uses {@link LongAdder} instead of {@code AtomicLong} to distribute CAS contention
 * across striped cells, achieving ~5x higher throughput under 16+ thread contention.</p>
 * <p>使用 {@link LongAdder} 代替 {@code AtomicLong}，将 CAS 竞争分散到条带化 cells，
 * 在 16+ 线程竞争下吞吐量提升约 5 倍。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-observability V1.0.3
 */
final class DefaultCounter implements Counter {

    private final MetricId id;
    private final LongAdder value = new LongAdder();

    DefaultCounter(MetricId id) {
        this.id = id;
    }

    @Override
    public void increment() {
        value.increment();
    }

    @Override
    public void increment(long amount) {
        if (amount < 0) {
            throw new ObservabilityException("INVALID_METRIC", "Counter increment amount must not be negative");
        }
        value.add(amount);
    }

    @Override
    public long count() {
        return value.sum();
    }

    @Override
    public void reset() {
        value.reset();
    }

    @Override
    public MetricId id() {
        return id;
    }
}
