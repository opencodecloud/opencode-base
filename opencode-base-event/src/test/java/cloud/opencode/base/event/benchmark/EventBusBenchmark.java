package cloud.opencode.base.event.benchmark;

import cloud.opencode.base.event.*;
import cloud.opencode.base.event.interceptor.EventInterceptor;
import cloud.opencode.base.event.monitor.EventBusMetrics;
import org.junit.jupiter.api.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Performance Benchmarks for OpenEvent Event Bus
 * OpenEvent 事件总线性能基准测试
 *
 * <p>Measures throughput and latency of core event bus operations using
 * warmup + measurement iterations with nanoTime-based timing.</p>
 * <p>使用预热+测量迭代和 nanoTime 计时测量核心事件总线操作的吞吐量和延迟。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.3
 */
@DisplayName("Event Bus 性能基准测试")
@Tag("benchmark")
class EventBusBenchmark {

    private static final int WARMUP_OPS = 50_000;
    private static final int MEASURE_OPS = 200_000;

    static class SimpleEvent extends Event {
        SimpleEvent() { super(); }
    }

    static class TypeAEvent extends Event {}
    static class TypeBEvent extends Event {}
    static class TypeCEvent extends Event {}

    private record BenchResult(String name, long ops, long totalNanos) {
        double opsPerMs() { return ops * 1_000_000.0 / totalNanos; }
        double nsPerOp() { return (double) totalNanos / ops; }

        @Override
        public String toString() {
            return String.format("%-45s %,10d ops  %,.0f ops/ms  %,.0f ns/op",
                    name, ops, opsPerMs(), nsPerOp());
        }
    }

    @FunctionalInterface
    interface BenchAction { void run(); }

    private BenchResult bench(String name, int warmup, int measure, BenchAction action) {
        // Warmup
        for (int i = 0; i < warmup; i++) {
            action.run();
        }

        // Measure
        long start = System.nanoTime();
        for (int i = 0; i < measure; i++) {
            action.run();
        }
        long elapsed = System.nanoTime() - start;
        return new BenchResult(name, measure, elapsed);
    }

    @Nested
    @DisplayName("吞吐量基准测试")
    class ThroughputBenchmarks {

        @Test
        @DisplayName("核心操作吞吐量")
        void coreThroughput() {
            System.out.println("\n========== OpenEvent Performance Benchmarks ==========\n");

            // 1. Event creation
            BenchResult r1 = bench("Event creation (UUID+Instant)", WARMUP_OPS, MEASURE_OPS,
                    SimpleEvent::new);
            System.out.println(r1);

            // 2. Publish - 1 sync listener
            try (var bus = OpenEvent.create()) {
                bus.on(SimpleEvent.class, _ -> {});
                BenchResult r2 = bench("publish() - 1 sync listener", WARMUP_OPS, MEASURE_OPS,
                        () -> bus.publish(new SimpleEvent()));
                System.out.println(r2);
            }

            // 3. Publish - 10 sync listeners
            try (var bus = OpenEvent.create()) {
                for (int i = 0; i < 10; i++) bus.on(SimpleEvent.class, _ -> {});
                BenchResult r3 = bench("publish() - 10 sync listeners", WARMUP_OPS, MEASURE_OPS,
                        () -> bus.publish(new SimpleEvent()));
                System.out.println(r3);
            }

            // 4. Publish - with 2 interceptors
            try (var bus = OpenEvent.create()) {
                bus.addInterceptor(new EventInterceptor() {
                    @Override public boolean beforePublish(Event e) { return true; }
                    @Override public void afterPublish(Event e, boolean d) {}
                });
                bus.addInterceptor(new EventInterceptor() {
                    @Override public boolean beforePublish(Event e) { return true; }
                    @Override public void afterPublish(Event e, boolean d) {}
                });
                bus.on(SimpleEvent.class, _ -> {});
                BenchResult r4 = bench("publish() - 2 interceptors + 1 listener", WARMUP_OPS, MEASURE_OPS,
                        () -> bus.publish(new SimpleEvent()));
                System.out.println(r4);
            }

            // 5. Publish - with filter
            try (var bus = OpenEvent.create()) {
                bus.subscribe(SimpleEvent.class, _ -> {}, _ -> true);
                BenchResult r5 = bench("publish() - 1 filtered listener", WARMUP_OPS, MEASURE_OPS,
                        () -> bus.publish(new SimpleEvent()));
                System.out.println(r5);
            }

            // 6. Publish - 20 registered types, exact match
            try (var bus = OpenEvent.create()) {
                bus.on(SimpleEvent.class, _ -> {});
                bus.on(TypeAEvent.class, _ -> {});
                bus.on(TypeBEvent.class, _ -> {});
                bus.on(TypeCEvent.class, _ -> {});
                bus.on(DeadEvent.class, _ -> {});
                for (int i = 0; i < 15; i++) {
                    bus.subscribe(Event.class, _ -> {}, _ -> false);
                }
                BenchResult r6 = bench("publish() - 20 types, exact match", WARMUP_OPS, MEASURE_OPS,
                        () -> bus.publish(new SimpleEvent()));
                System.out.println(r6);
            }

            // 7. Dead event path (no listeners)
            try (var bus = OpenEvent.create()) {
                bus.on(DeadEvent.class, _ -> {});
                BenchResult r7 = bench("publish() - dead event path", WARMUP_OPS, MEASURE_OPS,
                        () -> bus.publish(new SimpleEvent()));
                System.out.println(r7);
            }

            // 8. getMetrics() - O(1) with LongAdder
            try (var bus = OpenEvent.create()) {
                bus.on(SimpleEvent.class, _ -> {});
                bus.publish(new SimpleEvent()); // seed some data
                BenchResult r8 = bench("getMetrics()", WARMUP_OPS, MEASURE_OPS,
                        bus::getMetrics);
                System.out.println(r8);
            }

            // 9. subscribe + unsubscribe cycle
            try (var bus = OpenEvent.create()) {
                BenchResult r9 = bench("subscribe() + unsubscribe()", WARMUP_OPS, MEASURE_OPS / 10,
                        () -> {
                            Subscription sub = bus.subscribe(SimpleEvent.class, _ -> {});
                            sub.unsubscribe();
                        });
                System.out.println(r9);
            }

            System.out.println("\n======================================================\n");

            // Sanity check - all benchmarks should complete, not just be a correctness test
            assertThat(r1.opsPerMs()).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("并发吞吐量测试")
    class ConcurrencyBenchmarks {

        @Test
        @DisplayName("多线程并发发布吞吐量")
        void concurrentPublishThroughput() throws InterruptedException {
            System.out.println("\n========== Concurrent Publish Benchmarks ==========\n");

            int[] threadCounts = {1, 2, 4, 8};

            for (int threads : threadCounts) {
                try (var bus = OpenEvent.create()) {
                    LongAdder opCount = new LongAdder();
                    bus.on(SimpleEvent.class, _ -> opCount.increment());

                    int opsPerThread = 50_000;
                    CountDownLatch startLatch = new CountDownLatch(1);
                    CountDownLatch doneLatch = new CountDownLatch(threads);

                    // Warmup
                    for (int i = 0; i < 10_000; i++) bus.publish(new SimpleEvent());
                    bus.resetMetrics();
                    opCount.reset();

                    for (int t = 0; t < threads; t++) {
                        Thread.ofVirtual().start(() -> {
                            try {
                                startLatch.await();
                                for (int i = 0; i < opsPerThread; i++) {
                                    bus.publish(new SimpleEvent());
                                }
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            } finally {
                                doneLatch.countDown();
                            }
                        });
                    }

                    long start = System.nanoTime();
                    startLatch.countDown();
                    doneLatch.await(30, TimeUnit.SECONDS);
                    long elapsed = System.nanoTime() - start;

                    long totalOps = (long) threads * opsPerThread;
                    double opsPerMs = totalOps * 1_000_000.0 / elapsed;

                    System.out.printf("  %d threads × %,d ops = %,d total  →  %,.0f ops/ms  %,.0f ns/op%n",
                            threads, opsPerThread, totalOps, opsPerMs, (double) elapsed / totalOps);

                    EventBusMetrics metrics = bus.getMetrics();
                    assertThat(metrics.totalPublished()).isEqualTo(totalOps);
                    assertThat(metrics.totalDelivered()).isEqualTo(totalOps);
                }
            }

            System.out.println("\n===================================================\n");
        }
    }
}
