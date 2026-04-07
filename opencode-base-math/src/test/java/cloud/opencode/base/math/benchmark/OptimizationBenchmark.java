package cloud.opencode.base.math.benchmark;

import cloud.opencode.base.math.integration.NumericalIntegration;
import cloud.opencode.base.math.interpolation.Interpolation;
import cloud.opencode.base.math.linalg.Matrix;
import cloud.opencode.base.math.stats.Statistics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Benchmarks to validate performance optimizations in opencode-base-math V1.0.3.
 * 验证 opencode-base-math V1.0.3 性能优化的基准测试。
 *
 * <p>Covers:</p>
 * <ul>
 *   <li>P0: Kendall O(n²)→O(n log n) merge-sort algorithm</li>
 *   <li>P0: cubicSpline precompute/evaluate split</li>
 *   <li>P1: fractionalRanks unboxing (int[] merge sort)</li>
 *   <li>P1: Simpson loop unrolling (modulo elimination)</li>
 *   <li>P2: Matrix.inverse LU decomposition</li>
 *   <li>P2: Lagrange → Barycentric interpolation</li>
 * </ul>
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-math V1.0.3
 */
@DisplayName("性能优化验证基准")
class OptimizationBenchmark {

    private static final int WARMUP = 500;
    private static final int ITERATIONS = 2_000;

    @FunctionalInterface
    interface BenchmarkTask { void run(); }

    private static long benchmark(String name, BenchmarkTask task) {
        for (int i = 0; i < WARMUP; i++) task.run();
        long start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) task.run();
        long elapsed = System.nanoTime() - start;
        long nsPerOp = elapsed / ITERATIONS;
        double opsPerMs = ITERATIONS * 1_000_000.0 / elapsed;
        System.out.printf("  %-55s %,10d ns/op   %,.1f ops/ms%n", name, nsPerOp, opsPerMs);
        return nsPerOp;
    }

    private static double[] randomArray(int size) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        double[] arr = new double[size];
        for (int i = 0; i < size; i++) arr[i] = rng.nextDouble(-100, 100);
        return arr;
    }

    private static double[] sortedX(int size) {
        double[] x = new double[size];
        for (int i = 0; i < size; i++) x[i] = i * 0.1;
        return x;
    }

    private static double[] sinY(double[] x) {
        double[] y = new double[x.length];
        for (int i = 0; i < x.length; i++) y[i] = Math.sin(x[i]);
        return y;
    }

    // ==================== P0: Kendall O(n log n) ====================

    @Test
    @DisplayName("P0: Kendall 相关系数 — 规模扩展性验证")
    void kendallScalability() {
        System.out.println("\n=== P0: Kendall tau-b Scalability (O(n log n)) ===");
        System.out.println("  Expected: ~linear growth in ns/op as n doubles\n");

        for (int n : new int[]{100, 500, 1000, 2000, 5000}) {
            double[] x = randomArray(n);
            double[] y = randomArray(n);
            benchmark("Kendall(n=" + n + ")", () -> Statistics.kendallCorrelation(x, y));
        }
    }

    // ==================== P0: cubicSpline Precompute ====================

    @Test
    @DisplayName("P0: cubicSpline 预计算 vs 逐次调用")
    void cubicSplinePrecompute() {
        System.out.println("\n=== P0: cubicSpline Precompute vs Per-call ===");

        double[] x500 = sortedX(500);
        double[] y500 = sinY(x500);
        int queryCount = 50;
        double[] queries = new double[queryCount];
        for (int i = 0; i < queryCount; i++) {
            queries[i] = x500[0] + (x500[x500.length - 1] - x500[0]) * i / (queryCount - 1.0);
        }

        benchmark("cubicSpline per-call x" + queryCount + " (500 pts)", () -> {
            for (double q : queries) {
                Interpolation.cubicSpline(x500, y500, q);
            }
        });

        benchmark("precomputeSpline + evaluate x" + queryCount + " (500 pts)", () -> {
            var coeff = Interpolation.precomputeSpline(x500, y500);
            for (double q : queries) {
                coeff.evaluate(q);
            }
        });

        // Single-point comparison
        benchmark("cubicSpline single-point (500 pts)", () ->
                Interpolation.cubicSpline(x500, y500, 25.05));

        benchmark("precompute+evaluate single-point (500 pts)", () -> {
            var coeff = Interpolation.precomputeSpline(x500, y500);
            coeff.evaluate(25.05);
        });
    }

    // ==================== P1: Spearman (fractionalRanks unboxing) ====================

    @Test
    @DisplayName("P1: Spearman 等级相关 — int[] 归并排序")
    void spearmanUnboxing() {
        System.out.println("\n=== P1: Spearman Correlation (int[] merge sort) ===");

        for (int n : new int[]{100, 500, 1000, 5000}) {
            double[] x = randomArray(n);
            double[] y = randomArray(n);
            benchmark("Spearman(n=" + n + ")", () -> Statistics.spearmanCorrelation(x, y));
        }
    }

    // ==================== P1: Simpson Loop Unrolling ====================

    @Test
    @DisplayName("P1: Simpson 循环展开 — 消除取模")
    void simpsonUnrolling() {
        System.out.println("\n=== P1: Simpson 1/3 & 3/8 Loop Unrolling ===");

        benchmark("Simpson 1/3 (sin, 0→π, n=100)", () ->
                NumericalIntegration.simpson(Math::sin, 0, Math.PI, 100));
        benchmark("Simpson 1/3 (sin, 0→π, n=10000)", () ->
                NumericalIntegration.simpson(Math::sin, 0, Math.PI, 10000));
        benchmark("Simpson 1/3 (sin, 0→π, n=1000000)", () ->
                NumericalIntegration.simpson(Math::sin, 0, Math.PI, 1000000));

        benchmark("Simpson 3/8 (sin, 0→π, n=99)", () ->
                NumericalIntegration.simpsonThreeEighths(Math::sin, 0, Math.PI, 99));
        benchmark("Simpson 3/8 (sin, 0→π, n=9999)", () ->
                NumericalIntegration.simpsonThreeEighths(Math::sin, 0, Math.PI, 9999));
        benchmark("Simpson 3/8 (sin, 0→π, n=999999)", () ->
                NumericalIntegration.simpsonThreeEighths(Math::sin, 0, Math.PI, 999999));
    }

    // ==================== P2: Matrix.inverse LU ====================

    @Test
    @DisplayName("P2: Matrix.inverse LU 分解")
    void matrixInverseLU() {
        System.out.println("\n=== P2: Matrix.inverse (LU decomposition) ===");

        for (int n : new int[]{4, 10, 50, 100}) {
            double[][] d = new double[n][n];
            ThreadLocalRandom rng = ThreadLocalRandom.current();
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    d[i][j] = rng.nextDouble(-10, 10);
                }
                d[i][i] += n * 10; // diagonal dominance for invertibility
            }
            Matrix m = Matrix.of(d);
            benchmark("inverse(" + n + "x" + n + ")", () -> m.inverse());
        }
    }

    // ==================== P2: Lagrange Barycentric ====================

    @Test
    @DisplayName("P2: Lagrange Barycentric 插值")
    void lagrangeBarycentric() {
        System.out.println("\n=== P2: Lagrange Barycentric Interpolation ===");

        for (int n : new int[]{5, 10, 20, 50}) {
            double[] x = sortedX(n);
            double[] y = sinY(x);
            double xi = x[0] + (x[x.length - 1] - x[0]) * 0.37;
            benchmark("Lagrange(n=" + n + ", single eval)", () -> Interpolation.lagrange(x, y, xi));
        }
    }
}
