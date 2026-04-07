package cloud.opencode.base.test.concurrent;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * Thread Safety Checker
 * 线程安全检查器
 *
 * <p>Utility for checking thread safety of objects.</p>
 * <p>用于检查对象线程安全性的工具。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Thread safety checking utilities - 线程安全检查工具</li>
 *   <li>Counter and operation safety checks - 计数器和操作安全检查</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CheckResult result = ThreadSafetyChecker.checkCounter(
 *     () -> counter.incrementAndGet(),
 *     counter::get, 10, 1000);
 * assertTrue(result.passed());
 * }</pre>

 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
public final class ThreadSafetyChecker {

    private ThreadSafetyChecker() {
        // Utility class
    }

    /**
     * Check if a counter is thread-safe
     * 检查计数器是否线程安全
     *
     * @param incrementTask the increment task | 递增任务
     * @param getCountTask the get count task | 获取计数任务
     * @param threads the number of threads | 线程数
     * @param incrementsPerThread increments per thread | 每线程递增次数
     * @return the result | 结果
     */
    public static CheckResult checkCounter(
            Runnable incrementTask,
            Supplier<Integer> getCountTask,
            int threads,
            int incrementsPerThread) {

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        try {
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch endLatch = new CountDownLatch(threads);

            for (int t = 0; t < threads; t++) {
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        for (int i = 0; i < incrementsPerThread; i++) {
                            incrementTask.run();
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
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        int expected = Math.multiplyExact(threads, incrementsPerThread);
        int actual = getCountTask.get();

        return new CheckResult(
            expected == actual,
            expected,
            actual,
            expected - actual
        );
    }

    /**
     * Check if object operations are thread-safe
     * 检查对象操作是否线程安全
     *
     * @param operation the operation | 操作
     * @param threads the number of threads | 线程数
     * @param iterations iterations per thread | 每线程迭代次数
     * @return true if thread-safe | 如果线程安全返回true
     */
    public static boolean isThreadSafe(Runnable operation, int threads, int iterations) {
        AtomicBoolean safe = new AtomicBoolean(true);
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        try {
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch endLatch = new CountDownLatch(threads);

            for (int t = 0; t < threads; t++) {
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        for (int i = 0; i < iterations; i++) {
                            try {
                                operation.run();
                            } catch (Exception e) {
                                safe.set(false);
                                break;
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
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        return safe.get();
    }

    /**
     * Check result
     * 检查结果
     *
     * @param passed whether check passed | 检查是否通过
     * @param expected expected value | 期望值
     * @param actual actual value | 实际值
     * @param difference difference | 差异
     */
    public record CheckResult(boolean passed, int expected, int actual, int difference) {
        public double accuracy() {
            if (expected == 0) return actual == 0 ? 1.0 : 0.0;
            return (double) actual / expected;
        }

        @Override
        public String toString() {
            return String.format(
                "CheckResult[%s, expected=%d, actual=%d, diff=%d, accuracy=%.2f%%]",
                passed ? "PASSED" : "FAILED", expected, actual, difference, accuracy() * 100
            );
        }
    }
}
