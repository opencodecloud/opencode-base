package cloud.opencode.base.lock.benchmark;

import cloud.opencode.base.lock.Lock;
import cloud.opencode.base.lock.OpenLock;
import cloud.opencode.base.lock.ReadWriteLock;
import cloud.opencode.base.lock.event.ObservableLock;
import cloud.opencode.base.lock.local.LocalLock;
import cloud.opencode.base.lock.local.LocalReadWriteLock;
import cloud.opencode.base.lock.local.RetryLock;
import cloud.opencode.base.lock.local.SegmentLock;
import cloud.opencode.base.lock.local.SpinLock;
import cloud.opencode.base.lock.local.StampedLockAdapter;
import cloud.opencode.base.lock.local.TtlLock;
import cloud.opencode.base.lock.manager.NamedLockFactory;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Performance benchmarks for opencode-base-lock v1.0.3 components.
 * 锁模块 v1.0.3 性能基准测试（轻量 nanoTime 循环，非 JMH）。
 *
 * <p>Run with: mvn test -pl opencode-base-lock -Dtest="LockBenchmark"</p>
 *
 * <p>Each benchmark uses warmup + measurement phases to reduce JIT noise.</p>
 *
 * @author Leon Soo
 */
class LockBenchmark {

    private static final int WARMUP = 50_000;
    private static final int ITERATIONS = 500_000;
    private static final int CONCURRENT_ITERATIONS = 100_000;

    /** Measure ops/ms for a given operation */
    private static double benchmark(String name, Runnable op) {
        // Warmup
        for (int i = 0; i < WARMUP; i++) op.run();

        // Measure
        long start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) op.run();
        long elapsed = System.nanoTime() - start;

        double opsPerMs = (double) ITERATIONS / (elapsed / 1_000_000.0);
        double nsPerOp = (double) elapsed / ITERATIONS;
        System.out.printf("  %-55s %10.0f ops/ms  %8.1f ns/op%n", name, opsPerMs, nsPerOp);
        return nsPerOp;
    }

    /** Measure concurrent throughput with N platform threads */
    private static double concurrentBenchmark(String name, int threads, Runnable op) throws Exception {
        // Warmup
        for (int i = 0; i < 10_000; i++) op.run();

        var latch = new CountDownLatch(threads);
        var totalOps = new AtomicLong(0);

        long start = System.nanoTime();
        try (var exec = Executors.newFixedThreadPool(threads)) {
            for (int t = 0; t < threads; t++) {
                exec.submit(() -> {
                    for (int i = 0; i < CONCURRENT_ITERATIONS; i++) {
                        op.run();
                    }
                    totalOps.addAndGet(CONCURRENT_ITERATIONS);
                    latch.countDown();
                });
            }
            latch.await();
        }
        long elapsed = System.nanoTime() - start;

        long total = totalOps.get();
        double opsPerMs = (double) total / (elapsed / 1_000_000.0);
        double nsPerOp = (double) elapsed / total;
        System.out.printf("  %-55s %10.0f ops/ms  %8.1f ns/op  [%d threads]%n",
                name, opsPerMs, nsPerOp, threads);
        return nsPerOp;
    }

    // ======================= Single-Thread Lock Throughput =======================

    @Nested
    class SingleThreadThroughput {

        @Test
        void lockUnlock_allTypes() {
            System.out.println("\n=== Single-Thread Lock/Unlock Throughput ===");

            var localLock = new LocalLock();
            var spinLock = new SpinLock();
            var stampedAdapter = new StampedLockAdapter();
            var ttlLock = new TtlLock(Duration.ofSeconds(60));
            var retryLock = new RetryLock<>(new LocalLock());
            var observableNoListener = new ObservableLock<>(new LocalLock(), "bench");
            var observableWithListener = new ObservableLock<>(new LocalLock(), "bench",
                    event -> { /* no-op listener */ });

            double localNs = benchmark("LocalLock lock/unlock", () -> {
                try (var guard = localLock.lock()) { /* noop */ }
            });

            double spinNs = benchmark("SpinLock lock/unlock", () -> {
                try (var guard = spinLock.lock()) { /* noop */ }
            });

            double stampedReadNs = benchmark("StampedLockAdapter readLock/unlock", () -> {
                try (var guard = stampedAdapter.readLock().lock()) { /* noop */ }
            });

            double stampedWriteNs = benchmark("StampedLockAdapter writeLock/unlock", () -> {
                try (var guard = stampedAdapter.writeLock().lock()) { /* noop */ }
            });

            double ttlNs = benchmark("TtlLock lock/unlock", () -> {
                try (var guard = ttlLock.lock()) { /* noop */ }
            });

            double retryNs = benchmark("RetryLock lock/unlock (first-attempt)", () -> {
                try (var guard = retryLock.lock()) { /* noop */ }
            });

            double obsNoListenerNs = benchmark("ObservableLock (no listener) lock/unlock", () -> {
                try (var guard = observableNoListener.lock()) { /* noop */ }
            });

            double obsWithListenerNs = benchmark("ObservableLock (1 listener) lock/unlock", () -> {
                try (var guard = observableWithListener.lock()) { /* noop */ }
            });

            // Guard: decorators should be < 5x overhead vs raw LocalLock
            assertThat(retryNs / localNs).as("RetryLock overhead").isLessThan(5);
            assertThat(obsNoListenerNs / localNs).as("ObservableLock (no listener) overhead").isLessThan(3);
        }

        @Test
        void executeMethod_allTypes() {
            System.out.println("\n=== Single-Thread execute() Throughput ===");

            var localLock = new LocalLock();
            var spinLock = new SpinLock();
            var stampedAdapter = new StampedLockAdapter();

            benchmark("LocalLock.execute(noop)", () -> localLock.execute(() -> {}));
            benchmark("SpinLock.execute(noop)", () -> spinLock.execute(() -> {}));
            benchmark("StampedLockAdapter.executeRead(noop)",
                    () -> stampedAdapter.executeRead(() -> {}));
            benchmark("StampedLockAdapter.executeWrite(noop)",
                    () -> stampedAdapter.executeWrite(() -> {}));
        }
    }

    // ======================= StampedLock Optimistic Read =======================

    @Nested
    class StampedLockOptimisticRead {

        @Test
        void optimisticRead_vs_readLock_noContention() {
            System.out.println("\n=== StampedLock: Optimistic Read vs Read Lock (no contention) ===");

            var adapter = new StampedLockAdapter();
            var rwLock = new LocalReadWriteLock();
            final String[] sharedData = {"hello"};

            double optimisticNs = benchmark("StampedLockAdapter.optimisticRead()", () ->
                    adapter.optimisticRead(() -> sharedData[0]));

            double readLockNs = benchmark("StampedLockAdapter.readLock().executeWithResult()", () ->
                    adapter.readLock().executeWithResult(() -> sharedData[0]));

            double rwReadNs = benchmark("LocalReadWriteLock.readLock().executeWithResult()", () ->
                    rwLock.readLock().executeWithResult(() -> sharedData[0]));

            double ratio = readLockNs / optimisticNs;
            System.out.printf("  → Optimistic speedup vs read lock: %.2fx%n", ratio);

            // Optimistic read should be faster (no actual lock)
            // But JIT might optimize both — just ensure it's not slower
            assertThat(optimisticNs).as("Optimistic read should be fast").isLessThan(200);
        }

        @Test
        void optimisticRead_underWriteContention() throws Exception {
            System.out.println("\n=== StampedLock: Optimistic Read under Write Contention ===");

            var adapter = new StampedLockAdapter();
            var running = new java.util.concurrent.atomic.AtomicBoolean(true);
            final long[] sharedData = {0};

            // Background writer thread
            var writer = Thread.ofPlatform().daemon(true).start(() -> {
                while (running.get()) {
                    adapter.executeWrite(() -> sharedData[0]++);
                }
            });

            // Give writer time to start
            Thread.sleep(10);

            // Measure optimistic reads under contention
            int iters = 100_000;
            for (int i = 0; i < 10_000; i++) adapter.optimisticRead(() -> sharedData[0]);

            long start = System.nanoTime();
            long result = 0;
            for (int i = 0; i < iters; i++) {
                result += adapter.optimisticRead(() -> sharedData[0]);
            }
            long elapsed = System.nanoTime() - start;

            running.set(false);
            writer.join(1000);

            double nsPerOp = (double) elapsed / iters;
            double opsPerMs = (double) iters / (elapsed / 1_000_000.0);
            System.out.printf("  Optimistic read (under contention):  %10.0f ops/ms  %8.1f ns/op%n",
                    opsPerMs, nsPerOp);
            System.out.printf("  (Some reads may have fallen back to pessimistic read lock)%n");

            // Under contention, should still complete within reasonable time
            assertThat(nsPerOp).isLessThan(10_000); // < 10μs per read
        }
    }

    // ======================= Decorator Overhead =======================

    @Nested
    class DecoratorOverhead {

        @Test
        void decoratorOverhead_vs_rawLocalLock() {
            System.out.println("\n=== Decorator Overhead vs Raw LocalLock ===");

            var raw = new LocalLock();
            var ttlLock = new TtlLock(Duration.ofMinutes(5));
            var retryLock = new RetryLock<>(new LocalLock());
            var observableEmpty = new ObservableLock<>(new LocalLock(), "bench");
            var observableWith1 = new ObservableLock<>(new LocalLock(), "bench",
                    event -> { /* no-op */ });
            var observableWith3 = new ObservableLock<>(new LocalLock(), "bench",
                    event -> { /* no-op */ }, event -> { /* no-op */ }, event -> { /* no-op */ });

            double rawNs = benchmark("Raw LocalLock", () -> {
                try (var g = raw.lock()) {}
            });

            double ttlNs = benchmark("TtlLock (5min TTL)", () -> {
                try (var g = ttlLock.lock()) {}
            });

            double retryNs = benchmark("RetryLock (wrapping LocalLock)", () -> {
                try (var g = retryLock.lock()) {}
            });

            double obs0Ns = benchmark("ObservableLock (0 listeners)", () -> {
                try (var g = observableEmpty.lock()) {}
            });

            double obs1Ns = benchmark("ObservableLock (1 no-op listener)", () -> {
                try (var g = observableWith1.lock()) {}
            });

            double obs3Ns = benchmark("ObservableLock (3 no-op listeners)", () -> {
                try (var g = observableWith3.lock()) {}
            });

            System.out.printf("\n  Overhead ratios vs raw LocalLock:%n");
            System.out.printf("    TtlLock:               %.2fx%n", ttlNs / rawNs);
            System.out.printf("    RetryLock:              %.2fx%n", retryNs / rawNs);
            System.out.printf("    ObservableLock (0):     %.2fx%n", obs0Ns / rawNs);
            System.out.printf("    ObservableLock (1):     %.2fx%n", obs1Ns / rawNs);
            System.out.printf("    ObservableLock (3):     %.2fx%n", obs3Ns / rawNs);

            // Guard: decorators should not be more than 5x slower
            assertThat(ttlNs / rawNs).as("TtlLock overhead").isLessThan(5);
            assertThat(obs0Ns / rawNs).as("Observable (empty) overhead").isLessThan(3);
        }
    }

    // ======================= Concurrent Contention =======================

    @Nested
    class ConcurrentContention {

        @Test
        void contention_localLock_vs_spinLock_vs_stampedLock() throws Exception {
            System.out.println("\n=== Concurrent Contention: Lock Types (4 threads) ===");

            var localLock = new LocalLock();
            var spinLock = new SpinLock();
            var stampedWrite = new StampedLockAdapter();
            var ttlLock = new TtlLock(Duration.ofMinutes(5));
            int threads = 4;

            final long[] counter1 = {0};
            double localNs = concurrentBenchmark("LocalLock (4-thread contention)", threads, () -> {
                try (var g = localLock.lock()) { counter1[0]++; }
            });

            final long[] counter2 = {0};
            double spinNs = concurrentBenchmark("SpinLock (4-thread contention)", threads, () -> {
                try (var g = spinLock.lock()) { counter2[0]++; }
            });

            final long[] counter3 = {0};
            double stampedNs = concurrentBenchmark("StampedLock write (4-thread contention)", threads, () -> {
                stampedWrite.executeWrite(() -> counter3[0]++);
            });

            final long[] counter4 = {0};
            double ttlNs = concurrentBenchmark("TtlLock (4-thread contention)", threads, () -> {
                try (var g = ttlLock.lock()) { counter4[0]++; }
            });

            System.out.printf("\n  Contention ratios vs LocalLock:%n");
            System.out.printf("    SpinLock:      %.2fx%n", spinNs / localNs);
            System.out.printf("    StampedLock:   %.2fx%n", stampedNs / localNs);
            System.out.printf("    TtlLock:       %.2fx%n", ttlNs / localNs);
        }

        @Test
        void readHeavy_stampedLock_vs_readWriteLock() throws Exception {
            System.out.println("\n=== Read-Heavy Contention (90% read, 10% write, 4 threads) ===");

            var stampedAdapter = new StampedLockAdapter();
            var rwLock = new LocalReadWriteLock();
            int threads = 4;
            final long[] data = {0};

            double stampedNs = concurrentBenchmark("StampedLock optimisticRead (90/10)", threads, () -> {
                if (Thread.currentThread().hashCode() % 10 == 0) {
                    stampedAdapter.executeWrite(() -> data[0]++);
                } else {
                    stampedAdapter.optimisticRead(() -> data[0]);
                }
            });

            final long[] data2 = {0};
            double rwNs = concurrentBenchmark("ReentrantReadWriteLock (90/10)", threads, () -> {
                if (Thread.currentThread().hashCode() % 10 == 0) {
                    rwLock.executeWrite(() -> data2[0]++);
                } else {
                    rwLock.executeRead(() -> data2[0]);
                }
            });

            double ratio = rwNs / stampedNs;
            System.out.printf("  → StampedLock speedup: %.2fx%n", ratio);
        }
    }

    // ======================= Segment Lock Scalability =======================

    @Nested
    class SegmentLockScalability {

        @Test
        void segmentLock_vs_singleLock() throws Exception {
            System.out.println("\n=== Segment Lock Scalability (4 threads, key-based) ===");

            var singleLock = new LocalLock();
            var segmentLock16 = new SegmentLock<Integer>(16);
            var segmentLock64 = new SegmentLock<Integer>(64);
            int threads = 4;

            double singleNs = concurrentBenchmark("Single LocalLock (all keys)", threads, () -> {
                int key = (int) (Thread.currentThread().threadId() & 0xFF);
                try (var g = singleLock.lock()) { /* key-based work */ }
            });

            double seg16Ns = concurrentBenchmark("SegmentLock(16) (distributed keys)", threads, () -> {
                int key = (int) (Thread.currentThread().threadId() & 0xFF);
                segmentLock16.execute(key, () -> { /* key-based work */ });
            });

            double seg64Ns = concurrentBenchmark("SegmentLock(64) (distributed keys)", threads, () -> {
                int key = (int) (Thread.currentThread().threadId() & 0xFF);
                segmentLock64.execute(key, () -> { /* key-based work */ });
            });

            System.out.printf("\n  Scalability improvement vs single lock:%n");
            System.out.printf("    SegmentLock(16): %.2fx%n", singleNs / seg16Ns);
            System.out.printf("    SegmentLock(64): %.2fx%n", singleNs / seg64Ns);
        }
    }

    // ======================= Named Lock Factory =======================

    @Nested
    class NamedLockFactoryBenchmarks {

        @Test
        void namedLockFactory_lookup_and_execute() {
            System.out.println("\n=== Named Lock Factory Performance ===");

            var striped = new NamedLockFactory(64, true, cloud.opencode.base.lock.LockConfig.defaults());
            var localLock = new LocalLock();

            double directNs = benchmark("Direct LocalLock.execute(noop)", () ->
                    localLock.execute(() -> {}));

            double stripedNs = benchmark("NamedLockFactory(64).execute(\"key\", noop)", () ->
                    striped.execute("order:12345", () -> {}));

            double ratio = stripedNs / directNs;
            System.out.printf("  → Factory / direct ratio: %.2fx%n", ratio);
            assertThat(ratio).as("Factory lookup overhead").isLessThan(5);
        }
    }

    // ======================= Virtual Thread Lock Performance =======================

    @Nested
    class VirtualThreadBenchmarks {

        @Test
        void virtualThread_lock_throughput() throws Exception {
            System.out.println("\n=== Virtual Thread Lock Throughput (1000 VTs) ===");

            var localLock = new LocalLock();
            int virtualThreads = 1000;
            int opsPerThread = 1000;
            var counter = new AtomicLong(0);

            // Warmup
            try (var exec = Executors.newVirtualThreadPerTaskExecutor()) {
                var futures = IntStream.range(0, 100)
                        .mapToObj(i -> exec.submit(() -> {
                            for (int j = 0; j < 100; j++) {
                                localLock.execute(counter::incrementAndGet);
                            }
                        }))
                        .toList();
                for (var f : futures) f.get();
            }

            counter.set(0);
            long start = System.nanoTime();
            try (var exec = Executors.newVirtualThreadPerTaskExecutor()) {
                var futures = IntStream.range(0, virtualThreads)
                        .mapToObj(i -> exec.submit(() -> {
                            for (int j = 0; j < opsPerThread; j++) {
                                localLock.execute(counter::incrementAndGet);
                            }
                        }))
                        .toList();
                for (var f : futures) f.get();
            }
            long elapsed = System.nanoTime() - start;

            long totalOps = counter.get();
            double opsPerMs = (double) totalOps / (elapsed / 1_000_000.0);
            double nsPerOp = (double) elapsed / totalOps;
            System.out.printf("  LocalLock with %d VTs × %d ops:%n", virtualThreads, opsPerThread);
            System.out.printf("    Total ops:   %,d%n", totalOps);
            System.out.printf("    Throughput:   %,.0f ops/ms%n", opsPerMs);
            System.out.printf("    Avg latency:  %.1f ns/op%n", nsPerOp);
            long elapsedMs = elapsed / 1_000_000;
            System.out.printf("    Wall time:    %d ms%n", elapsedMs);

            assertThat(totalOps).isEqualTo((long) virtualThreads * opsPerThread);
            assertThat(elapsedMs).isLessThan(30000); // should be < 10s typically
        }
    }

    // ======================= Summary =======================

    @Test
    void printSummaryHeader() {
        System.out.println("\n" + "=".repeat(75));
        System.out.println("  OpenCode-Base-Lock v1.0.3 Performance Benchmark");
        System.out.println("  JDK: " + Runtime.version());
        System.out.println("  OS:  " + System.getProperty("os.name") + " " + System.getProperty("os.arch"));
        System.out.println("  CPU: " + Runtime.getRuntime().availableProcessors() + " cores");
        System.out.println("  Warmup: " + WARMUP + " iterations, Measure: " + ITERATIONS + " iterations");
        System.out.println("  Concurrent Measure: " + CONCURRENT_ITERATIONS + " iterations/thread");
        System.out.println("=".repeat(75));
    }
}
