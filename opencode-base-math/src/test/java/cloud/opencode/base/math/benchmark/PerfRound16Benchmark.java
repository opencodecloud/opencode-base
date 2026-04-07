package cloud.opencode.base.math.benchmark;

import cloud.opencode.base.math.analysis.Differentiation;
import cloud.opencode.base.math.distribution.Distributions;
import cloud.opencode.base.math.stats.Regression;
import cloud.opencode.base.math.stats.inference.AnovaTest;
import cloud.opencode.base.math.stats.inference.TTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Benchmarks for Perf Round 16 optimizations.
 * 性能优化第 16 轮基准测试。
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-math V1.0.3
 */
@DisplayName("Perf Round 16 优化验证基准")
class PerfRound16Benchmark {

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

    // ==================== P1: TTest single-pass Welford ====================

    @Test
    @DisplayName("P1: TTest.oneSample — 单遍 Welford")
    void tTestOneSample() {
        System.out.println("\n=== P1: TTest.oneSample (single-pass Welford) ===");
        System.out.println("  Optimization: fused mean+variance+validation into 1 pass (was 3)\n");

        for (int n : new int[]{100, 1000, 10_000, 100_000}) {
            double[] data = randomArray(n);
            benchmark("oneSample(n=" + n + ")", () -> TTest.oneSample(data, 0.0));
        }
    }

    @Test
    @DisplayName("P1: TTest.twoSample — 单遍 Welford x2")
    void tTestTwoSample() {
        System.out.println("\n=== P1: TTest.twoSample (single-pass Welford x2) ===");
        System.out.println("  Optimization: fused mean+variance+validation into 1 pass per array (was 3 each)\n");

        for (int n : new int[]{100, 1000, 10_000, 100_000}) {
            double[] x = randomArray(n);
            double[] y = randomArray(n);
            benchmark("twoSample(n=" + n + ")", () -> TTest.twoSample(x, y));
        }
    }

    @Test
    @DisplayName("P1: TTest.paired — 内联 Welford 无临时数组")
    void tTestPaired() {
        System.out.println("\n=== P1: TTest.paired (inline Welford, no temp array) ===");
        System.out.println("  Optimization: no temp d[] allocation, no re-validation via oneSample\n");

        for (int n : new int[]{100, 1000, 10_000, 100_000}) {
            double[] x = randomArray(n);
            double[] y = randomArray(n);
            benchmark("paired(n=" + n + ")", () -> TTest.paired(x, y));
        }
    }

    // ==================== P2: AnovaTest single-loop per group ====================

    @Test
    @DisplayName("P2: AnovaTest.oneWay — 组内单循环")
    void anovaOneWay() {
        System.out.println("\n=== P2: AnovaTest.oneWay (single loop per group) ===");
        System.out.println("  Optimization: fused groupSum + SSW into one loop via sum-of-squares identity\n");

        for (int groups : new int[]{3, 5, 10}) {
            int groupSize = 1000;
            double[][] data = new double[groups][];
            for (int g = 0; g < groups; g++) data[g] = randomArray(groupSize);
            benchmark("oneWay(" + groups + " groups x " + groupSize + ")",
                    () -> AnovaTest.oneWay(data));
        }
    }

    // ==================== P2: Differentiation.richardson ====================

    @Test
    @DisplayName("P2: Differentiation.richardson — 迭代替代 Math.pow")
    void richardsonIterative() {
        System.out.println("\n=== P2: Differentiation.richardson (iterative h/factor) ===");
        System.out.println("  Optimization: Math.pow(2,i)→h*=0.5, Math.pow(4,j)→factor*=4\n");

        for (int order : new int[]{1, 2, 3, 4, 5, 6}) {
            benchmark("richardson(sin, x=1.0, order=" + order + ")",
                    () -> Differentiation.richardson(Math::sin, 1.0, order));
        }
    }

    // ==================== P2: Regression.linear ====================

    @Test
    @DisplayName("P2: Regression.linear — 有限性检查融合")
    void regressionLinear() {
        System.out.println("\n=== P2: Regression.linear (finite check fusion) ===");

        for (int n : new int[]{100, 1000, 10_000, 100_000}) {
            double[] x = randomArray(n);
            double[] y = randomArray(n);
            benchmark("linear(n=" + n + ")", () -> Regression.linear(x, y));
        }
    }

    // ==================== P3: poissonCdf early termination ====================

    @Test
    @DisplayName("P3: Distributions.poissonCdf — 提前终止")
    void poissonCdfEarlyTermination() {
        System.out.println("\n=== P3: Distributions.poissonCdf (early termination) ===");
        System.out.println("  Optimization: stop when sum >= 1 - 1e-15\n");

        // For lambda=10, CDF should converge well before k=100
        benchmark("poissonCdf(lambda=10, k=50)", () -> Distributions.poissonCdf(10.0, 50));
        benchmark("poissonCdf(lambda=10, k=100)", () -> Distributions.poissonCdf(10.0, 100));
        benchmark("poissonCdf(lambda=10, k=1000)", () -> Distributions.poissonCdf(10.0, 1000));
        benchmark("poissonCdf(lambda=100, k=500)", () -> Distributions.poissonCdf(100.0, 500));
        benchmark("poissonCdf(lambda=100, k=10000)", () -> Distributions.poissonCdf(100.0, 10000));
    }
}
