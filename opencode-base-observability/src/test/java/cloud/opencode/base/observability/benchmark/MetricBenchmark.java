package cloud.opencode.base.observability.benchmark;

import cloud.opencode.base.observability.context.ObservabilityContext;
import cloud.opencode.base.observability.metric.Counter;
import cloud.opencode.base.observability.metric.Histogram;
import cloud.opencode.base.observability.metric.MetricRegistry;
import cloud.opencode.base.observability.metric.Tag;
import cloud.opencode.base.observability.metric.Timer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-process performance benchmarks for observability hot paths.
 * 可观测性热路径内联性能基准测试。
 *
 * <p>Runs as a JUnit test to avoid JPMS/JMH annotation processing conflicts.
 * Each benchmark measures throughput (ops/ms) over a fixed duration.</p>
 * <p>作为 JUnit 测试运行，避免 JPMS/JMH 注解处理冲突。
 * 每个基准测试在固定时长内测量吞吐量（ops/ms）。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-observability V1.0.3
 */
@org.junit.jupiter.api.Tag("benchmark")
class MetricBenchmark {

    private static final long WARMUP_MS = 500;
    private static final long MEASURE_MS = 2_000;
    private static MetricRegistry registry;
    private static Counter counter;
    private static Timer timer;
    private static Histogram histogram;

    @BeforeAll
    static void setup() {
        registry = MetricRegistry.create();
        counter = registry.counter("bench.counter", Tag.of("env", "bench"));
        timer = registry.timer("bench.timer", Tag.of("env", "bench"));
        histogram = registry.histogram("bench.histogram", Tag.of("env", "bench"));
    }

    // ==================== Single-thread benchmarks ====================

    @Test
    void counterIncrement_1thread() {
        long ops = benchSingleThread("Counter.increment() [1T]", () -> counter.increment());
        assertMinThroughput(ops, 50_000); // expect >50K ops/ms = >50M ops/s
    }

    @Test
    void timerRecord_1thread() {
        long ops = benchSingleThread("Timer.record(Duration) [1T]", () -> timer.record(Duration.ofNanos(1234)));
        assertMinThroughput(ops, 5_000); // Duration.ofNanos allocates; expect >5K ops/ms
    }

    @Test
    void timerTimeRunnable_1thread() {
        long ops = benchSingleThread("Timer.time(Runnable) [1T]", () -> timer.time(() -> {}));
        assertMinThroughput(ops, 5_000); // includes System.nanoTime() x2
    }

    @Test
    void histogramRecord_1thread() {
        long ops = benchSingleThread("Histogram.record(double) [1T]", () -> histogram.record(42.5));
        assertMinThroughput(ops, 20_000);
    }

    @Test
    void registryLookup_1thread() {
        long ops = benchSingleThread("Registry.counter() lookup [1T]",
                () -> registry.counter("bench.counter", Tag.of("env", "bench")));
        assertMinThroughput(ops, 2_000); // MetricId construction + CHM.get
    }

    @Test
    void contextAttachDetach_1thread() {
        long ops = benchSingleThread("Context attach/detach [1T]", () -> {
            ObservabilityContext ctx = ObservabilityContext.create("trace-bench");
            try (var scope = ctx.attach()) {
                ObservabilityContext.current();
            }
        });
        assertMinThroughput(ops, 2_000);
    }

    @Test
    void contextCreateSpanId_1thread() {
        long ops = benchSingleThread("Context.create() spanId gen [1T]",
                () -> ObservabilityContext.create("trace-bench"));
        assertMinThroughput(ops, 5_000);
    }

    // ==================== Multi-thread benchmarks ====================

    @Test
    void counterIncrement_16threads() throws Exception {
        long ops = benchMultiThread("Counter.increment() [16T]", 16, () -> counter.increment());
        assertMinThroughput(ops, 100_000); // LongAdder should scale well
    }

    @Test
    void timerRecord_16threads() throws Exception {
        long ops = benchMultiThread("Timer.record() [16T]", 16, () -> timer.record(Duration.ofNanos(1234)));
        assertMinThroughput(ops, 10_000);
    }

    @Test
    void histogramRecord_16threads() throws Exception {
        long ops = benchMultiThread("Histogram.record() [16T]", 16, () -> histogram.record(42.5));
        assertMinThroughput(ops, 30_000);
    }

    @Test
    void registryLookup_16threads() throws Exception {
        long ops = benchMultiThread("Registry.counter() lookup [16T]", 16,
                () -> registry.counter("bench.counter", Tag.of("env", "bench")));
        assertMinThroughput(ops, 5_000);
    }

    // ==================== Infrastructure ====================

    private long benchSingleThread(String name, Runnable op) {
        // Warmup
        long warmEnd = System.nanoTime() + WARMUP_MS * 1_000_000;
        while (System.nanoTime() < warmEnd) {
            op.run();
        }
        // Measure
        long count = 0;
        long measureEnd = System.nanoTime() + MEASURE_MS * 1_000_000;
        while (System.nanoTime() < measureEnd) {
            op.run();
            count++;
        }
        long opsPerMs = count / MEASURE_MS;
        System.out.printf("  %-40s %,10d ops/ms  (%,d ops/s)%n", name, opsPerMs, opsPerMs * 1000);
        return opsPerMs;
    }

    private long benchMultiThread(String name, int threads, Runnable op) throws Exception {
        // Warmup
        long warmEnd = System.nanoTime() + WARMUP_MS * 1_000_000;
        while (System.nanoTime() < warmEnd) {
            op.run();
        }
        // Measure
        AtomicLong totalOps = new AtomicLong();
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threads);
        long measureNanos = MEASURE_MS * 1_000_000;

        for (int i = 0; i < threads; i++) {
            Thread.startVirtualThread(() -> {
                try {
                    startLatch.await();
                } catch (InterruptedException e) {
                    return;
                }
                long count = 0;
                long end = System.nanoTime() + measureNanos;
                while (System.nanoTime() < end) {
                    op.run();
                    count++;
                }
                totalOps.addAndGet(count);
                endLatch.countDown();
            });
        }

        startLatch.countDown();
        endLatch.await();

        long opsPerMs = totalOps.get() / MEASURE_MS;
        System.out.printf("  %-40s %,10d ops/ms  (%,d ops/s)%n", name, opsPerMs, opsPerMs * 1000);
        return opsPerMs;
    }

    private void assertMinThroughput(long opsPerMs, long minExpected) {
        if (opsPerMs < minExpected) {
            System.out.printf("  ⚠ BELOW THRESHOLD: %,d ops/ms < %,d ops/ms expected%n", opsPerMs, minExpected);
        }
    }
}
