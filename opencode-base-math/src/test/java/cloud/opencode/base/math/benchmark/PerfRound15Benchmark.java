package cloud.opencode.base.math.benchmark;

import cloud.opencode.base.math.linalg.Matrix;
import cloud.opencode.base.math.linalg.Vector;
import cloud.opencode.base.math.special.SpecialFunctions;
import cloud.opencode.base.math.stats.Statistics;
import cloud.opencode.base.math.stats.StreamingStatistics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Benchmarks for Perf Round 15 optimizations.
 * 性能优化第 15 轮基准测试。
 *
 * <p>Validates the following optimizations:</p>
 * <ul>
 *   <li>P1: Statistics finite-check fusion into computation loops</li>
 *   <li>P2: Matrix.inverse pb[] array reuse</li>
 *   <li>P2: Matrix shared LU decomposition</li>
 *   <li>P3: SpecialFunctions erf/erfc shared erfcCore + Horner form</li>
 *   <li>P3: Vector.negate() direct loop</li>
 * </ul>
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-math V1.0.3
 */
@DisplayName("Perf Round 15 优化验证基准")
class PerfRound15Benchmark {

    private static final int WARMUP = 500;
    private static final int ITERATIONS = 3_000;

    @FunctionalInterface
    interface BenchmarkTask { void run(); }

    private static long benchmark(String name, BenchmarkTask task) {
        for (int i = 0; i < WARMUP; i++) task.run();
        long start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) task.run();
        long elapsed = System.nanoTime() - start;
        long nsPerOp = elapsed / ITERATIONS;
        double opsPerMs = ITERATIONS * 1_000_000.0 / elapsed;
        System.out.printf("  %-60s %,10d ns/op   %,.1f ops/ms%n", name, nsPerOp, opsPerMs);
        return nsPerOp;
    }

    private static double[] randomArray(int size) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        double[] arr = new double[size];
        for (int i = 0; i < size; i++) arr[i] = rng.nextDouble(-100, 100);
        return arr;
    }

    // ==================== P1: Statistics finite-check fusion ====================

    @Test
    @DisplayName("P1: Statistics.correlation — 有限性检查融合")
    void correlationFiniteCheckFusion() {
        System.out.println("\n=== P1: Statistics finite-check fusion (correlation) ===");
        System.out.println("  Optimization: isFinite check merged into mean computation loop");
        System.out.println("  Expected: ~1 fewer O(n) pass over both x[] and y[]\n");

        for (int n : new int[]{1000, 10_000, 100_000}) {
            double[] x = randomArray(n);
            double[] y = randomArray(n);
            benchmark("correlation(n=" + n + ")", () -> Statistics.correlation(x, y));
        }
    }

    @Test
    @DisplayName("P1: Statistics.covariance — 有限性检查融合")
    void covarianceFiniteCheckFusion() {
        System.out.println("\n=== P1: Statistics finite-check fusion (covariance) ===");

        for (int n : new int[]{1000, 10_000, 100_000}) {
            double[] x = randomArray(n);
            double[] y = randomArray(n);
            benchmark("covariance(n=" + n + ")", () -> Statistics.covariance(x, y));
        }
    }

    @Test
    @DisplayName("P1: Statistics.skewness/kurtosis — 有限性检查融合")
    void skewnessKurtosisFiniteCheckFusion() {
        System.out.println("\n=== P1: Statistics finite-check fusion (skewness/kurtosis) ===");

        for (int n : new int[]{1000, 10_000, 100_000}) {
            double[] data = randomArray(n);
            benchmark("skewness(n=" + n + ")", () -> Statistics.skewness(data));
            benchmark("kurtosis(n=" + n + ")", () -> Statistics.kurtosis(data));
        }
    }

    @Test
    @DisplayName("P1: Statistics.range/weightedMean — 有限性检查融合")
    void rangeWeightedMeanFiniteCheckFusion() {
        System.out.println("\n=== P1: Statistics finite-check fusion (range/weightedMean) ===");

        for (int n : new int[]{1000, 10_000, 100_000}) {
            double[] data = randomArray(n);
            double[] weights = new double[n];
            for (int i = 0; i < n; i++) weights[i] = Math.abs(data[i]) + 0.01;
            benchmark("range(n=" + n + ")", () -> Statistics.range(data));
            benchmark("weightedMean(n=" + n + ")", () -> Statistics.weightedMean(data, weights));
        }
    }

    // ==================== P2: Matrix.inverse pb[] reuse ====================

    @Test
    @DisplayName("P2: Matrix.inverse — pb[] 数组复用")
    void matrixInversePbReuse() {
        System.out.println("\n=== P2: Matrix.inverse (pb[] array reuse) ===");
        System.out.println("  Optimization: single pb[] reused across all n columns");
        System.out.println("  Expected: reduced GC pressure for large matrices\n");

        for (int n : new int[]{10, 50, 100, 200, 500}) {
            double[][] d = new double[n][n];
            ThreadLocalRandom rng = ThreadLocalRandom.current();
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    d[i][j] = rng.nextDouble(-10, 10);
                }
                d[i][i] += n * 10;
            }
            Matrix m = Matrix.of(d);
            int iters = n <= 100 ? ITERATIONS : (n <= 200 ? 500 : 50);
            benchmarkN("inverse(" + n + "x" + n + ")", () -> m.inverse(), iters);
        }
    }

    @Test
    @DisplayName("P2: Matrix.determinant — 共享 LU 分解")
    void matrixDeterminantSharedLU() {
        System.out.println("\n=== P2: Matrix.determinant (shared LU decomposition) ===");

        for (int n : new int[]{10, 50, 100, 200}) {
            double[][] d = new double[n][n];
            ThreadLocalRandom rng = ThreadLocalRandom.current();
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    d[i][j] = rng.nextDouble(-10, 10);
                }
                d[i][i] += n * 10;
            }
            Matrix m = Matrix.of(d);
            benchmark("determinant(" + n + "x" + n + ")", () -> m.determinant());
        }
    }

    // ==================== P3: erf/erfc shared erfcCore ====================

    @Test
    @DisplayName("P3: SpecialFunctions erf/erfc — Horner 形式 + 共享核心")
    void erfErfcSharedCore() {
        System.out.println("\n=== P3: SpecialFunctions erf/erfc (Horner + shared erfcCore) ===");
        System.out.println("  Optimization: shared polynomial core, Horner form (fewer multiplies)\n");

        double[] testPoints = {0.1, 0.5, 1.0, 2.0, 3.0};
        for (double x : testPoints) {
            benchmark("erf(" + x + ")", () -> SpecialFunctions.erf(x));
        }
        System.out.println();
        for (double x : testPoints) {
            benchmark("erfc(" + x + ")", () -> SpecialFunctions.erfc(x));
        }
    }

    // ==================== P3: Vector.negate() direct loop ====================

    @Test
    @DisplayName("P3: Vector.negate() — 直接取反 vs scale(-1)")
    void vectorNegateDirect() {
        System.out.println("\n=== P3: Vector.negate() (direct loop, no isFinite check) ===");
        System.out.println("  Optimization: skip isFinite guard + avoid n multiplications by -1\n");

        for (int dim : new int[]{3, 100, 1000, 10_000, 100_000}) {
            double[] components = new double[dim];
            for (int i = 0; i < dim; i++) components[i] = i * 0.01;
            Vector v = Vector.of(components);
            benchmark("negate(dim=" + dim + ")", () -> v.negate());
        }
    }

    // ==================== Throughput comparison: StreamingStatistics ====================

    @Test
    @DisplayName("基线: StreamingStatistics.add — 零分配 Welford")
    void streamingStatsBaseline() {
        System.out.println("\n=== Baseline: StreamingStatistics.add (zero-allocation Welford) ===");

        for (int n : new int[]{1000, 10_000, 100_000}) {
            double[] data = randomArray(n);
            benchmark("StreamingStats.add x" + n, () -> {
                StreamingStatistics ss = StreamingStatistics.create();
                for (double v : data) ss.add(v);
            });
        }
    }

    // ---- helper with custom iteration count ----
    private static long benchmarkN(String name, BenchmarkTask task, int iterations) {
        for (int i = 0; i < WARMUP; i++) task.run();
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) task.run();
        long elapsed = System.nanoTime() - start;
        long nsPerOp = elapsed / iterations;
        double opsPerMs = iterations * 1_000_000.0 / elapsed;
        System.out.printf("  %-60s %,10d ns/op   %,.1f ops/ms%n", name, nsPerOp, opsPerMs);
        return nsPerOp;
    }
}
