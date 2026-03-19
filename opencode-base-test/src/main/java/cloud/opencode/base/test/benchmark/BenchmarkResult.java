package cloud.opencode.base.test.benchmark;

import java.util.Arrays;

/**
 * Benchmark Result - Immutable result of a benchmark run
 * 基准测试结果 - 基准测试运行的不可变结果
 *
 * <p>Contains timing statistics for a benchmark execution.</p>
 * <p>包含基准测试执行的计时统计。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Average, min, max timing statistics - 平均、最小、最大计时统计</li>
 *   <li>Percentile calculations (p50, p95, p99) - 百分位计算</li>
 *   <li>Throughput and standard deviation - 吞吐量和标准差</li>
 *   <li>Formatted summary output - 格式化摘要输出</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * BenchmarkResult result = BenchmarkRunner.runSingle("test", () -> doWork());
 * System.out.println("Average: " + result.averageMs() + "ms");
 * System.out.println("P95: " + result.p95Ms() + "ms");
 * System.out.println(result.summary());
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: No (requires non-null timesNanos array) - 空值安全: 否（需要非空timesNanos数组）</li>
 * </ul>
 *
 * @param name        the benchmark name | 基准测试名称
 * @param timesNanos  the execution times in nanoseconds | 执行时间（纳秒）
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
public record BenchmarkResult(String name, long[] timesNanos) {

    /**
     * Gets the number of iterations.
     * 获取迭代次数。
     *
     * @return the iteration count | 迭代次数
     */
    public int iterations() {
        return timesNanos.length;
    }

    /**
     * Gets the average time in milliseconds.
     * 获取平均时间（毫秒）。
     *
     * @return the average time | 平均时间
     */
    public double averageMs() {
        return Arrays.stream(timesNanos).average().orElse(0) / 1_000_000.0;
    }

    /**
     * Gets the average time in nanoseconds.
     * 获取平均时间（纳秒）。
     *
     * @return the average time | 平均时间
     */
    public double averageNanos() {
        return Arrays.stream(timesNanos).average().orElse(0);
    }

    /**
     * Gets the minimum time in milliseconds.
     * 获取最小时间（毫秒）。
     *
     * @return the minimum time | 最小时间
     */
    public double minMs() {
        return Arrays.stream(timesNanos).min().orElse(0) / 1_000_000.0;
    }

    /**
     * Gets the maximum time in milliseconds.
     * 获取最大时间（毫秒）。
     *
     * @return the maximum time | 最大时间
     */
    public double maxMs() {
        return Arrays.stream(timesNanos).max().orElse(0) / 1_000_000.0;
    }

    /**
     * Gets the percentile time in milliseconds.
     * 获取百分位时间（毫秒）。
     *
     * @param percentile the percentile (0-100) | 百分位
     * @return the percentile time | 百分位时间
     */
    public double percentileMs(int percentile) {
        if (percentile < 0 || percentile > 100) {
            throw new IllegalArgumentException("Percentile must be between 0 and 100");
        }
        long[] sorted = Arrays.copyOf(timesNanos, timesNanos.length);
        Arrays.sort(sorted);
        int index = (int) Math.ceil(percentile / 100.0 * sorted.length) - 1;
        return sorted[Math.max(0, index)] / 1_000_000.0;
    }

    /**
     * Gets the median time in milliseconds.
     * 获取中位数时间（毫秒）。
     *
     * @return the median time | 中位数时间
     */
    public double medianMs() {
        return percentileMs(50);
    }

    /**
     * Gets the 95th percentile time in milliseconds.
     * 获取第95百分位时间（毫秒）。
     *
     * @return the p95 time | P95时间
     */
    public double p95Ms() {
        return percentileMs(95);
    }

    /**
     * Gets the 99th percentile time in milliseconds.
     * 获取第99百分位时间（毫秒）。
     *
     * @return the p99 time | P99时间
     */
    public double p99Ms() {
        return percentileMs(99);
    }

    /**
     * Gets the total time in milliseconds.
     * 获取总时间（毫秒）。
     *
     * @return the total time | 总时间
     */
    public long totalMs() {
        return Arrays.stream(timesNanos).sum() / 1_000_000;
    }

    /**
     * Gets the throughput (operations per second).
     * 获取吞吐量（每秒操作数）。
     *
     * @return the throughput | 吞吐量
     */
    public double throughputPerSecond() {
        double avgNanos = averageNanos();
        if (avgNanos == 0) {
            return 0;
        }
        return 1_000_000_000.0 / avgNanos;
    }

    /**
     * Gets the standard deviation in milliseconds.
     * 获取标准差（毫秒）。
     *
     * @return the standard deviation | 标准差
     */
    public double stdDevMs() {
        double mean = averageNanos();
        double variance = Arrays.stream(timesNanos)
            .mapToDouble(t -> Math.pow(t - mean, 2))
            .average()
            .orElse(0);
        return Math.sqrt(variance) / 1_000_000.0;
    }

    /**
     * Returns a formatted summary string.
     * 返回格式化的摘要字符串。
     *
     * @return the summary | 摘要
     */
    public String summary() {
        return String.format(
            "%s: avg=%.3fms, min=%.3fms, max=%.3fms, p50=%.3fms, p95=%.3fms, p99=%.3fms, stdDev=%.3fms, throughput=%.0f ops/s",
            name, averageMs(), minMs(), maxMs(), medianMs(), p95Ms(), p99Ms(), stdDevMs(), throughputPerSecond()
        );
    }

    @Override
    public String toString() {
        return summary();
    }
}
