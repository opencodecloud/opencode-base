package cloud.opencode.base.math.benchmark;

import cloud.opencode.base.math.distribution.BinomialDistribution;
import cloud.opencode.base.math.distribution.FDistribution;
import cloud.opencode.base.math.distribution.TDistribution;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Benchmarks for Perf Round 17 — Distribution class precomputed constants.
 * 性能优化第 17 轮基准测试 — 分布类常量预计算。
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-math V1.0.3
 */
@DisplayName("Perf Round 17 分布类预计算验证基准")
class PerfRound17Benchmark {

    private static final int WARMUP = 500;
    private static final int ITERATIONS = 5_000;

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

    // ==================== TDistribution ====================

    @Test
    @DisplayName("TDistribution — cdf/pdf/inverseCdf 预计算")
    void tDistribution() {
        System.out.println("\n=== TDistribution (precomputed halfDf, negHalfDfP1, invDf) ===");
        System.out.println("  Optimization: eliminate per-call divisions in pdf()/cdf()\n");

        for (double df : new double[]{1, 5, 30, 100}) {
            TDistribution t = TDistribution.of(df);
            benchmark("T(df=" + (int) df + ").pdf(1.5)", () -> t.pdf(1.5));
            benchmark("T(df=" + (int) df + ").cdf(1.5)", () -> t.cdf(1.5));
        }
        System.out.println();

        // inverseCdf exercises bisection (~100 cdf() calls internally)
        TDistribution t10 = TDistribution.of(10);
        benchmark("T(df=10).inverseCdf(0.025)", () -> t10.inverseCdf(0.025));
        benchmark("T(df=10).inverseCdf(0.5)", () -> t10.inverseCdf(0.5));
        benchmark("T(df=10).inverseCdf(0.975)", () -> t10.inverseCdf(0.975));
    }

    // ==================== FDistribution ====================

    @Test
    @DisplayName("FDistribution — cdf/pdf/inverseCdf 预计算")
    void fDistribution() {
        System.out.println("\n=== FDistribution (precomputed halfDf1, halfDf2, halfDf1Sum) ===");

        for (double[] dfs : new double[][]{{1, 1}, {5, 10}, {10, 30}, {50, 50}}) {
            FDistribution f = FDistribution.of(dfs[0], dfs[1]);
            benchmark("F(" + (int) dfs[0] + "," + (int) dfs[1] + ").cdf(2.0)", () -> f.cdf(2.0));
            benchmark("F(" + (int) dfs[0] + "," + (int) dfs[1] + ").pdf(2.0)", () -> f.pdf(2.0));
        }
        System.out.println();

        FDistribution f5_10 = FDistribution.of(5, 10);
        benchmark("F(5,10).inverseCdf(0.05)", () -> f5_10.inverseCdf(0.05));
        benchmark("F(5,10).inverseCdf(0.95)", () -> f5_10.inverseCdf(0.95));
    }

    // ==================== BinomialDistribution ====================

    @Test
    @DisplayName("BinomialDistribution — pmf 预计算 logGamma/logP/logQ")
    void binomialDistribution() {
        System.out.println("\n=== BinomialDistribution (precomputed logGamma(n+1), logP, logQ) ===");
        System.out.println("  Optimization: eliminate 1 logGamma + 2 Math.log per pmf() call\n");

        for (int n : new int[]{10, 50, 100, 1000}) {
            BinomialDistribution b = BinomialDistribution.of(n, 0.3);
            int k = n / 2;
            benchmark("B(n=" + n + ",p=0.3).pmf(" + k + ")", () -> b.pmf(k));
        }
        System.out.println();

        // Batch PMF: build full PMF table (real use case)
        BinomialDistribution b100 = BinomialDistribution.of(100, 0.5);
        benchmark("B(100,0.5) full PMF table (k=0..100)", () -> {
            double sum = 0;
            for (int k = 0; k <= 100; k++) {
                sum += b100.pmf(k);
            }
        });

        BinomialDistribution b1000 = BinomialDistribution.of(1000, 0.5);
        benchmark("B(1000,0.5) full PMF table (k=0..1000)", () -> {
            double sum = 0;
            for (int k = 0; k <= 1000; k++) {
                sum += b1000.pmf(k);
            }
        });
    }
}
