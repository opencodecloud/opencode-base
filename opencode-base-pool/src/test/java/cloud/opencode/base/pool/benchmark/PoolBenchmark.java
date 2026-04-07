package cloud.opencode.base.pool.benchmark;

import cloud.opencode.base.pool.ObjectPool;
import cloud.opencode.base.pool.PoolConfig;
import cloud.opencode.base.pool.PoolLease;
import cloud.opencode.base.pool.factory.BasePooledObjectFactory;
import cloud.opencode.base.pool.impl.GenericObjectPool;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PoolBenchmark - Performance Benchmarks for GenericObjectPool
 * PoolBenchmark - GenericObjectPool 性能基准测试
 *
 * <p>Lightweight nanoTime-loop benchmarks for pool borrow/return throughput,
 * PoolLease overhead, concurrent contention, and metrics access cost.</p>
 * <p>使用 nanoTime 循环的轻量基准测试，覆盖借用/归还吞吐量、
 * PoolLease 开销、并发竞争和指标访问成本。</p>
 *
 * <p><strong>Run | 运行:</strong> {@code mvn test -pl opencode-base-pool -Dtest="PoolBenchmark"}</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.3
 */
class PoolBenchmark {

    private static final int WARMUP = 50_000;
    private static final int ITERATIONS = 500_000;

    private static GenericObjectPool<StringBuilder> createPool(int maxTotal) {
        PoolConfig config = PoolConfig.builder()
                .maxTotal(maxTotal)
                .maxIdle(maxTotal)
                .minIdle(maxTotal)
                .maxWait(Duration.ofSeconds(5))
                .build();

        GenericObjectPool<StringBuilder> pool = new GenericObjectPool<>(new BasePooledObjectFactory<>() {
            private final AtomicLong counter = new AtomicLong();

            @Override
            protected StringBuilder create() {
                return new StringBuilder("obj-" + counter.incrementAndGet());
            }
        }, config);
        return pool;
    }

    private static void report(String label, long iterations, long nanos) {
        double opsPerMs = (double) iterations / nanos * 1_000_000;
        double nsPerOp = (double) nanos / iterations;
        System.out.printf("  %-40s %,12.0f ops/ms  %8.1f ns/op%n", label, opsPerMs, nsPerOp);
    }

    // ==================== Single-threaded Benchmarks ====================

    @Nested
    class SingleThreadBenchmarks {

        /**
         * Measures raw borrow + return throughput (manual pattern).
         * 测量原始借用+归还吞吐量（手动模式）。
         */
        @Test
        void benchBorrowReturn() {
            try (GenericObjectPool<StringBuilder> pool = createPool(32)) {
                // Warmup
                for (int i = 0; i < WARMUP; i++) {
                    StringBuilder obj = pool.borrowObject();
                    pool.returnObject(obj);
                }

                // Measure
                long start = System.nanoTime();
                for (int i = 0; i < ITERATIONS; i++) {
                    StringBuilder obj = pool.borrowObject();
                    pool.returnObject(obj);
                }
                long elapsed = System.nanoTime() - start;

                report("borrowReturn (manual)", ITERATIONS, elapsed);
                assertThat(elapsed).as("borrow+return should complete in time").isPositive();
            }
        }

        /**
         * Measures PoolLease (try-with-resources) overhead vs manual.
         * 测量 PoolLease (try-with-resources) 相对手动模式的开销。
         */
        @Test
        void benchBorrowReturnLease() {
            try (GenericObjectPool<StringBuilder> pool = createPool(32)) {
                // Warmup
                for (int i = 0; i < WARMUP; i++) {
                    try (PoolLease<StringBuilder> lease = pool.borrowLease()) {
                        StringBuilder _ = lease.get();
                    }
                }

                // Measure
                long start = System.nanoTime();
                for (int i = 0; i < ITERATIONS; i++) {
                    try (PoolLease<StringBuilder> lease = pool.borrowLease()) {
                        StringBuilder _ = lease.get();
                    }
                }
                long elapsed = System.nanoTime() - start;

                report("borrowReturn (PoolLease)", ITERATIONS, elapsed);
                assertThat(elapsed).isPositive();
            }
        }

        /**
         * Measures execute pattern (functional) throughput.
         * 测量执行模式（函数式）吞吐量。
         */
        @Test
        void benchExecutePattern() {
            try (GenericObjectPool<StringBuilder> pool = createPool(32)) {
                final StringBuilder[] sink = new StringBuilder[1];

                // Warmup
                for (int i = 0; i < WARMUP; i++) {
                    pool.execute((java.util.function.Consumer<StringBuilder>) obj -> sink[0] = obj);
                }

                // Measure
                long start = System.nanoTime();
                for (int i = 0; i < ITERATIONS; i++) {
                    pool.execute((java.util.function.Consumer<StringBuilder>) obj -> sink[0] = obj);
                }
                long elapsed = System.nanoTime() - start;

                report("borrowReturn (execute)", ITERATIONS, elapsed);
                assertThat(elapsed).isPositive();
            }
        }

        /**
         * Measures getNumIdle/getNumActive access cost (O(1) with AtomicInteger).
         * 测量 getNumIdle/getNumActive 访问成本（AtomicInteger O(1)）。
         */
        @Test
        void benchMetricsAccess() {
            try (GenericObjectPool<StringBuilder> pool = createPool(32)) {
                int sum = 0;

                // Warmup
                for (int i = 0; i < WARMUP; i++) {
                    sum += pool.getNumIdle();
                    sum += pool.getNumActive();
                }

                // Measure
                long start = System.nanoTime();
                for (int i = 0; i < ITERATIONS; i++) {
                    sum += pool.getNumIdle();
                    sum += pool.getNumActive();
                }
                long elapsed = System.nanoTime() - start;

                report("getNumIdle+getNumActive", ITERATIONS, elapsed);
                assertThat(sum).as("prevent dead-code elimination").isNotNegative();
            }
        }
    }

    // ==================== Concurrent Benchmarks ====================

    @Nested
    class ConcurrentBenchmarks {

        /**
         * Measures concurrent borrow/return throughput with 4 threads.
         * 测量 4 线程并发借用/归还吞吐量。
         */
        @Test
        void benchConcurrent4Threads() throws InterruptedException {
            benchConcurrent(4, 64, ITERATIONS / 4);
        }

        /**
         * Measures concurrent borrow/return throughput with 16 threads.
         * 测量 16 线程并发借用/归还吞吐量。
         */
        @Test
        void benchConcurrent16Threads() throws InterruptedException {
            benchConcurrent(16, 64, ITERATIONS / 16);
        }

        /**
         * Measures concurrent borrow/return with virtual threads.
         * 测量虚拟线程并发借用/归还吞吐量。
         */
        @Test
        void benchConcurrentVirtualThreads() throws InterruptedException {
            int numVThreads = 100;
            int opsPerThread = ITERATIONS / numVThreads;

            try (GenericObjectPool<StringBuilder> pool = createPool(64)) {
                // Warmup
                for (int i = 0; i < WARMUP / 10; i++) {
                    StringBuilder obj = pool.borrowObject();
                    pool.returnObject(obj);
                }

                CountDownLatch start = new CountDownLatch(1);
                CountDownLatch done = new CountDownLatch(numVThreads);

                for (int t = 0; t < numVThreads; t++) {
                    Thread.ofVirtual().start(() -> {
                        try {
                            start.await();
                            for (int i = 0; i < opsPerThread; i++) {
                                StringBuilder obj = pool.borrowObject();
                                pool.returnObject(obj);
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        } finally {
                            done.countDown();
                        }
                    });
                }

                long startNanos = System.nanoTime();
                start.countDown();
                done.await();
                long elapsed = System.nanoTime() - startNanos;

                long totalOps = (long) numVThreads * opsPerThread;
                report("concurrent (" + numVThreads + " vthreads)", totalOps, elapsed);
                assertThat(elapsed).isPositive();
            }
        }

        private void benchConcurrent(int threads, int poolSize, int opsPerThread) throws InterruptedException {
            try (GenericObjectPool<StringBuilder> pool = createPool(poolSize)) {
                // Warmup
                for (int i = 0; i < WARMUP / 10; i++) {
                    StringBuilder obj = pool.borrowObject();
                    pool.returnObject(obj);
                }

                CountDownLatch start = new CountDownLatch(1);
                CountDownLatch done = new CountDownLatch(threads);

                for (int t = 0; t < threads; t++) {
                    Thread.ofPlatform().start(() -> {
                        try {
                            start.await();
                            for (int i = 0; i < opsPerThread; i++) {
                                StringBuilder obj = pool.borrowObject();
                                pool.returnObject(obj);
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        } finally {
                            done.countDown();
                        }
                    });
                }

                long startNanos = System.nanoTime();
                start.countDown();
                done.await();
                long elapsed = System.nanoTime() - startNanos;

                long totalOps = (long) threads * opsPerThread;
                report("concurrent (" + threads + " platform threads, pool=" + poolSize + ")", totalOps, elapsed);
                assertThat(elapsed).isPositive();
            }
        }
    }
}
