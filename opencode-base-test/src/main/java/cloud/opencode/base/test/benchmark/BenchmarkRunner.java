package cloud.opencode.base.test.benchmark;

import cloud.opencode.base.test.exception.BenchmarkException;

import java.io.PrintStream;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * Benchmark Runner - Configurable benchmark test runner
 * 基准测试运行器 - 可配置的基准测试运行器
 *
 * <p>Provides a fluent API for running and comparing benchmarks.</p>
 * <p>提供用于运行和比较基准测试的流式API。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Configurable warmup and measurement iterations - 可配置的预热和测量迭代</li>
 *   <li>Multiple benchmark comparison - 多基准测试比较</li>
 *   <li>Timeout protection - 超时保护</li>
 *   <li>Formatted results output with comparison - 格式化结果输出与比较</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * BenchmarkRunner runner = BenchmarkRunner.create()
 *     .warmup(100)
 *     .iterations(1000)
 *     .add("ArrayList", () -> new ArrayList<>().add("test"))
 *     .add("LinkedList", () -> new LinkedList<>().add("test"));
 *
 * List<BenchmarkResult> results = runner.run();
 * runner.printResults();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (not designed for concurrent use) - 线程安全: 否（非设计用于并发使用）</li>
 *   <li>Null-safe: Yes (validates non-null inputs) - 空值安全: 是（验证非空输入）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
public final class BenchmarkRunner {

    private int warmupIterations = 10;
    private int measureIterations = 100;
    private Duration timeout = Duration.ofMinutes(5);
    private PrintStream output = System.out;
    private boolean verbose = false;
    private final Map<String, Runnable> benchmarks = new LinkedHashMap<>();
    private List<BenchmarkResult> results;

    private BenchmarkRunner() {
    }

    /**
     * Creates new benchmark runner.
     * 创建新的基准测试运行器。
     *
     * @return the runner | 运行器
     */
    public static BenchmarkRunner create() {
        return new BenchmarkRunner();
    }

    /**
     * Creates and runs single benchmark.
     * 创建并运行单个基准测试。
     *
     * @param name     the benchmark name | 基准测试名称
     * @param runnable the code to benchmark | 要基准测试的代码
     * @return the result | 结果
     */
    public static BenchmarkResult runSingle(String name, Runnable runnable) {
        return create().add(name, runnable).run().getFirst();
    }

    // ============ Configuration | 配置 ============

    /**
     * Sets warmup iterations.
     * 设置预热迭代次数。
     *
     * @param iterations the iterations | 迭代次数
     * @return this | 此对象
     */
    public BenchmarkRunner warmup(int iterations) {
        if (iterations < 0) {
            throw new IllegalArgumentException("warmup iterations must be >= 0");
        }
        this.warmupIterations = iterations;
        return this;
    }

    /**
     * Sets measure iterations.
     * 设置测量迭代次数。
     *
     * @param iterations the iterations | 迭代次数
     * @return this | 此对象
     */
    public BenchmarkRunner iterations(int iterations) {
        if (iterations <= 0) {
            throw new IllegalArgumentException("measure iterations must be > 0");
        }
        this.measureIterations = iterations;
        return this;
    }

    /**
     * Sets timeout for each benchmark.
     * 设置每个基准测试的超时时间。
     *
     * @param timeout the timeout | 超时时间
     * @return this | 此对象
     */
    public BenchmarkRunner timeout(Duration timeout) {
        this.timeout = Objects.requireNonNull(timeout, "timeout cannot be null");
        return this;
    }

    /**
     * Sets output stream.
     * 设置输出流。
     *
     * @param output the output stream | 输出流
     * @return this | 此对象
     */
    public BenchmarkRunner output(PrintStream output) {
        this.output = Objects.requireNonNull(output, "output cannot be null");
        return this;
    }

    /**
     * Enables verbose output.
     * 启用详细输出。
     *
     * @return this | 此对象
     */
    public BenchmarkRunner verbose() {
        this.verbose = true;
        return this;
    }

    // ============ Benchmark Registration | 基准测试注册 ============

    /**
     * Adds benchmark.
     * 添加基准测试。
     *
     * @param name     the benchmark name | 基准测试名称
     * @param runnable the code to benchmark | 要基准测试的代码
     * @return this | 此对象
     */
    public BenchmarkRunner add(String name, Runnable runnable) {
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(runnable, "runnable cannot be null");
        if (benchmarks.containsKey(name)) {
            throw new IllegalArgumentException("Benchmark already exists: " + name);
        }
        benchmarks.put(name, runnable);
        return this;
    }

    /**
     * Adds benchmark with return value (result ignored).
     * 添加带返回值的基准测试（忽略结果）。
     *
     * @param name     the benchmark name | 基准测试名称
     * @param callable the code to benchmark | 要基准测试的代码
     * @param <T>      the return type | 返回类型
     * @return this | 此对象
     */
    public <T> BenchmarkRunner add(String name, Callable<T> callable) {
        return add(name, (Runnable) () -> {
            try {
                callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    // ============ Execution | 执行 ============

    /**
     * Runs all benchmarks.
     * 运行所有基准测试。
     *
     * @return the results | 结果列表
     */
    public List<BenchmarkResult> run() {
        if (benchmarks.isEmpty()) {
            throw new IllegalStateException("No benchmarks registered");
        }

        results = new ArrayList<>();
        Instant startTime = Instant.now();

        if (verbose) {
            output.println("Starting benchmarks...");
            output.printf("  Warmup: %d iterations%n", warmupIterations);
            output.printf("  Measure: %d iterations%n", measureIterations);
            output.printf("  Benchmarks: %d%n", benchmarks.size());
            output.println();
        }

        for (Map.Entry<String, Runnable> entry : benchmarks.entrySet()) {
            String name = entry.getKey();
            Runnable runnable = entry.getValue();

            if (verbose) {
                output.printf("Running: %s%n", name);
            }

            try {
                BenchmarkResult result = runBenchmark(name, runnable);
                results.add(result);

                if (verbose) {
                    output.printf("  Completed: avg=%.3fms%n", result.averageMs());
                }
            } catch (Exception e) {
                throw new BenchmarkException("Benchmark failed: " + name, e);
            }

            // Check timeout
            if (Duration.between(startTime, Instant.now()).compareTo(timeout) > 0) {
                throw new BenchmarkException("Benchmark suite timeout exceeded");
            }
        }

        if (verbose) {
            Duration totalTime = Duration.between(startTime, Instant.now());
            output.printf("%nCompleted in %.2f seconds%n", totalTime.toMillis() / 1000.0);
        }

        return results;
    }

    private BenchmarkResult runBenchmark(String name, Runnable runnable) {
        // Warmup
        for (int i = 0; i < warmupIterations; i++) {
            runnable.run();
        }

        // Force GC before measurement
        System.gc();

        // Measure
        long[] times = new long[measureIterations];
        for (int i = 0; i < measureIterations; i++) {
            long start = System.nanoTime();
            runnable.run();
            times[i] = System.nanoTime() - start;
        }

        return new BenchmarkResult(name, times);
    }

    // ============ Results | 结果 ============

    /**
     * Gets the results (must call run() first).
     * 获取结果（必须先调用run()）。
     *
     * @return the results | 结果列表
     */
    public List<BenchmarkResult> getResults() {
        if (results == null) {
            throw new IllegalStateException("Must call run() first");
        }
        return List.copyOf(results);
    }

    /**
     * Prints results to output stream.
     * 将结果打印到输出流。
     *
     * @return this | 此对象
     */
    public BenchmarkRunner printResults() {
        if (results == null) {
            throw new IllegalStateException("Must call run() first");
        }

        output.println();
        output.println("=".repeat(70));
        output.println("BENCHMARK RESULTS");
        output.println("=".repeat(70));
        output.printf("%-30s %10s %10s %10s %10s%n", "Name", "Avg (ms)", "Min (ms)", "Max (ms)", "Ops/s");
        output.println("-".repeat(70));

        for (BenchmarkResult result : results) {
            output.printf("%-30s %10.3f %10.3f %10.3f %10.0f%n",
                result.name(),
                result.averageMs(),
                result.minMs(),
                result.maxMs(),
                result.throughputPerSecond());
        }

        output.println("=".repeat(70));

        // Find fastest
        if (results.size() > 1) {
            BenchmarkResult fastest = results.stream()
                .min((a, b) -> Double.compare(a.averageMs(), b.averageMs()))
                .orElse(results.getFirst());
            output.printf("Fastest: %s%n", fastest.name());
        }

        return this;
    }

    /**
     * Prints comparison of results.
     * 打印结果比较。
     *
     * @return this | 此对象
     */
    public BenchmarkRunner printComparison() {
        if (results == null || results.size() < 2) {
            return this;
        }

        output.println();
        output.println("COMPARISON");
        output.println("-".repeat(50));

        BenchmarkResult baseline = results.getFirst();
        output.printf("Baseline: %s (%.3fms)%n", baseline.name(), baseline.averageMs());
        output.println();

        for (int i = 1; i < results.size(); i++) {
            BenchmarkResult result = results.get(i);
            double resultAvg = result.averageMs();
            if (resultAvg == 0 || baseline.averageMs() == 0) {
                output.printf("  %s: N/A (too fast to measure)%n", result.name());
                continue;
            }
            double ratio = baseline.averageMs() / resultAvg;
            String comparison = ratio > 1 ? "faster" : "slower";
            output.printf("  %s: %.2fx %s%n", result.name(), Math.max(ratio, 1 / ratio), comparison);
        }

        return this;
    }

    /**
     * Executes callback with results.
     * 使用结果执行回调。
     *
     * @param consumer the consumer | 消费者
     * @return this | 此对象
     */
    public BenchmarkRunner onComplete(Consumer<List<BenchmarkResult>> consumer) {
        if (results != null) {
            consumer.accept(List.copyOf(results));
        }
        return this;
    }
}
