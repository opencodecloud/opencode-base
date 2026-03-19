package cloud.opencode.base.test.benchmark;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Benchmark
 * 基准测试
 *
 * <p>Simple benchmarking utility.</p>
 * <p>简单基准测试工具。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Performance measurement utilities - 性能测量工具</li>
 *   <li>Comparison benchmarking - 对比基准测试</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Duration time = Benchmark.time(() -> heavyOperation());
 * Benchmark.BenchmarkResult result = Benchmark.run("test", () -> operation());
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
public final class Benchmark {

    private Benchmark() {
        // Utility class
    }

    /**
     * Measure execution time of runnable
     * 测量可运行对象的执行时间
     *
     * @param runnable the runnable | 可运行对象
     * @return the duration | 时长
     */
    public static Duration time(Runnable runnable) {
        long start = System.nanoTime();
        runnable.run();
        return Duration.ofNanos(System.nanoTime() - start);
    }

    /**
     * Measure execution time with result
     * 测量带结果的执行时间
     *
     * @param supplier the supplier | 供应者
     * @param <T> the result type | 结果类型
     * @return the result with timing | 带计时的结果
     */
    public static <T> TimedResult<T> time(Supplier<T> supplier) {
        long start = System.nanoTime();
        T result = supplier.get();
        return new TimedResult<>(result, Duration.ofNanos(System.nanoTime() - start));
    }

    /**
     * Run benchmark with warmup and iterations
     * 运行带预热和迭代的基准测试
     *
     * @param name the benchmark name | 基准测试名称
     * @param runnable the runnable | 可运行对象
     * @param warmupIterations warmup iterations | 预热迭代次数
     * @param measureIterations measure iterations | 测量迭代次数
     * @return the result | 结果
     */
    public static BenchmarkResult run(String name, Runnable runnable, int warmupIterations, int measureIterations) {
        // Warmup
        for (int i = 0; i < warmupIterations; i++) {
            runnable.run();
        }

        // Measure
        List<Long> times = new ArrayList<>(measureIterations);
        for (int i = 0; i < measureIterations; i++) {
            long start = System.nanoTime();
            runnable.run();
            times.add(System.nanoTime() - start);
        }

        return new BenchmarkResult(name, times);
    }

    /**
     * Run benchmark with defaults
     * 使用默认值运行基准测试
     *
     * @param name the name | 名称
     * @param runnable the runnable | 可运行对象
     * @return the result | 结果
     */
    public static BenchmarkResult run(String name, Runnable runnable) {
        return run(name, runnable, 5, 20);
    }

    /**
     * Compare two implementations
     * 比较两个实现
     *
     * @param name1 first name | 第一个名称
     * @param runnable1 first runnable | 第一个可运行对象
     * @param name2 second name | 第二个名称
     * @param runnable2 second runnable | 第二个可运行对象
     * @return comparison result | 比较结果
     */
    public static ComparisonResult compare(String name1, Runnable runnable1, String name2, Runnable runnable2) {
        BenchmarkResult result1 = run(name1, runnable1);
        BenchmarkResult result2 = run(name2, runnable2);
        return new ComparisonResult(result1, result2);
    }

    /**
     * Timed result
     * 计时结果
     *
     * @param result the result | 结果
     * @param duration the duration | 时长
     * @param <T> the result type | 结果类型
     */
    public record TimedResult<T>(T result, Duration duration) {
        public long millis() {
            return duration.toMillis();
        }

        public long nanos() {
            return duration.toNanos();
        }
    }

    /**
     * Benchmark result
     * 基准测试结果
     */
    public static class BenchmarkResult {
        private final String name;
        private final List<Long> times;
        private final LongSummaryStatistics stats;

        public BenchmarkResult(String name, List<Long> times) {
            this.name = name;
            this.times = List.copyOf(times);
            this.stats = times.stream().mapToLong(Long::longValue).summaryStatistics();
        }

        public String getName() { return name; }
        public long getMin() { return stats.getMin(); }
        public long getMax() { return stats.getMax(); }
        public double getAverage() { return stats.getAverage(); }
        public long getCount() { return stats.getCount(); }

        public Duration getMinDuration() { return Duration.ofNanos(getMin()); }
        public Duration getMaxDuration() { return Duration.ofNanos(getMax()); }
        public Duration getAverageDuration() { return Duration.ofNanos((long) getAverage()); }

        public double getOpsPerSecond() {
            return 1_000_000_000.0 / getAverage();
        }

        @Override
        public String toString() {
            return String.format("%s: avg=%.2fms, min=%.2fms, max=%.2fms, ops/s=%.0f",
                name,
                getAverage() / 1_000_000,
                getMin() / 1_000_000.0,
                getMax() / 1_000_000.0,
                getOpsPerSecond());
        }
    }

    /**
     * Comparison result
     * 比较结果
     */
    public record ComparisonResult(BenchmarkResult first, BenchmarkResult second) {
        public double getSpeedup() {
            return second.getAverage() / first.getAverage();
        }

        public String getFaster() {
            return first.getAverage() < second.getAverage() ? first.getName() : second.getName();
        }

        @Override
        public String toString() {
            return String.format("Comparison: %s is %.2fx faster than %s\n%s\n%s",
                getFaster(),
                getSpeedup() > 1 ? getSpeedup() : 1 / getSpeedup(),
                getSpeedup() > 1 ? second.getName() : first.getName(),
                first,
                second);
        }
    }
}
