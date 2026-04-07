package cloud.opencode.base.test.concurrent;

import cloud.opencode.base.test.exception.TestErrorCode;
import cloud.opencode.base.test.exception.TestException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Concurrent Tester
 * 并发测试器
 *
 * <p>Utility for testing concurrent behavior.</p>
 * <p>用于测试并发行为的工具。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Concurrent execution testing - 并发执行测试</li>
 *   <li>Thread safety verification - 线程安全验证</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ConcurrentTester.ConcurrentResult result =
 *     ConcurrentTester.runConcurrently(() -> counter.incrementAndGet(), 10, 1000);
 * assertTrue(result.allSucceeded());
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
public final class ConcurrentTester {

    private ConcurrentTester() {
        // Utility class
    }

    /**
     * Run task concurrently
     * 并发运行任务
     *
     * @param task the task | 任务
     * @param threads the number of threads | 线程数
     * @param iterations iterations per thread | 每线程迭代次数
     * @return the result | 结果
     */
    public static ConcurrentResult runConcurrently(Runnable task, int threads, int iterations) {
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        List<Throwable> errors = new CopyOnWriteArrayList<>();

        long startTime = System.nanoTime();

        for (int t = 0; t < threads; t++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    for (int i = 0; i < iterations; i++) {
                        try {
                            task.run();
                            successCount.incrementAndGet();
                        } catch (Throwable e) {
                            failureCount.incrementAndGet();
                            errors.add(e);
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();

        try {
            endLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long endTime = System.nanoTime();

        try {
            executor.shutdown();
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        return new ConcurrentResult(
            threads,
            iterations,
            successCount.get(),
            failureCount.get(),
            Duration.ofNanos(endTime - startTime),
            errors
        );
    }

    /**
     * Run task concurrently with index
     * 带索引并发运行任务
     *
     * @param task the task with thread index | 带线程索引的任务
     * @param threads the number of threads | 线程数
     * @return the result | 结果
     */
    public static ConcurrentResult runConcurrently(Consumer<Integer> task, int threads) {
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        List<Throwable> errors = new CopyOnWriteArrayList<>();

        long startTime = System.nanoTime();

        for (int t = 0; t < threads; t++) {
            final int threadIndex = t;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    task.accept(threadIndex);
                    successCount.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Throwable e) {
                    failureCount.incrementAndGet();
                    errors.add(e);
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();

        try {
            endLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long endTime = System.nanoTime();

        try {
            executor.shutdown();
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        return new ConcurrentResult(
            threads,
            1,
            successCount.get(),
            failureCount.get(),
            Duration.ofNanos(endTime - startTime),
            errors
        );
    }

    /**
     * Assert task is thread-safe
     * 断言任务是线程安全的
     *
     * @param task the task | 任务
     * @param threads the number of threads | 线程数
     * @param iterations iterations per thread | 每线程迭代次数
     */
    public static void assertThreadSafe(Runnable task, int threads, int iterations) {
        ConcurrentResult result = runConcurrently(task, threads, iterations);
        if (result.failureCount() > 0) {
            throw new TestException(
                TestErrorCode.CONCURRENT_ERROR,
                "Thread safety test failed: " + result.failureCount() + " failures"
            );
        }
    }

    /**
     * Concurrent test result
     * 并发测试结果
     *
     * @param threads the number of threads | 线程数
     * @param iterationsPerThread iterations per thread | 每线程迭代次数
     * @param successCount success count | 成功次数
     * @param failureCount failure count | 失败次数
     * @param totalDuration total duration | 总时长
     * @param errors the errors | 错误列表
     */
    public record ConcurrentResult(
        int threads,
        int iterationsPerThread,
        int successCount,
        int failureCount,
        Duration totalDuration,
        List<Throwable> errors
    ) {
        public int totalIterations() {
            return Math.multiplyExact(threads, iterationsPerThread);
        }

        public double throughput() {
            return successCount / (totalDuration.toNanos() / 1_000_000_000.0);
        }

        public boolean allSucceeded() {
            return failureCount == 0;
        }

        @Override
        public String toString() {
            return String.format(
                "ConcurrentResult[threads=%d, iterations=%d, success=%d, failures=%d, duration=%sms, throughput=%.0f/s]",
                threads, totalIterations(), successCount, failureCount, totalDuration.toMillis(), throughput()
            );
        }
    }
}
