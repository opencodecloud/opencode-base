package cloud.opencode.base.math.benchmark;

import cloud.opencode.base.math.analysis.Differentiation;
import cloud.opencode.base.math.analysis.RootFinder;
import cloud.opencode.base.math.distribution.*;
import cloud.opencode.base.math.stats.Statistics;
import cloud.opencode.base.math.stats.StreamingStatistics;
import cloud.opencode.base.math.stats.inference.AnovaTest;
import cloud.opencode.base.math.stats.inference.ChiSquareTest;
import cloud.opencode.base.math.stats.inference.TTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Performance benchmarks for Phase 2 features.
 * Phase 2 新增功能性能基准测试
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-math V1.0.3
 */
@DisplayName("Phase 2 性能基准")
class Phase2Benchmark {

    private static final int WARMUP = 500;
    private static final int ITERATIONS = 5_000;

    @FunctionalInterface
    interface BenchmarkTask { void run(); }

    private static void benchmark(String name, BenchmarkTask task) {
        for (int i = 0; i < WARMUP; i++) task.run();
        long start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) task.run();
        long elapsed = System.nanoTime() - start;
        long nsPerOp = elapsed / ITERATIONS;
        double opsPerMs = ITERATIONS * 1_000_000.0 / elapsed;
        System.out.printf("  %-50s %,8d ns/op   %,.0f ops/ms%n", name, nsPerOp, opsPerMs);
    }

    // ==================== 新增分布 ====================

    @Test
    @DisplayName("新增分布性能")
    void distributionBenchmark() {
        System.out.println("\n=== New Distributions Benchmark ===");

        TDistribution t10 = TDistribution.of(10);
        ChiSquaredDistribution chi5 = ChiSquaredDistribution.of(5);
        FDistribution f510 = FDistribution.of(5, 10);
        BinomialDistribution binom = BinomialDistribution.of(100, 0.5);
        GammaDistribution gamma = GammaDistribution.of(2, 1);
        BetaDistribution beta = BetaDistribution.of(2, 5);
        LogNormalDistribution logn = LogNormalDistribution.of(0, 1);

        // PDF
        benchmark("t(10).pdf(1.5)", () -> t10.pdf(1.5));
        benchmark("chi²(5).pdf(3.0)", () -> chi5.pdf(3.0));
        benchmark("F(5,10).pdf(2.0)", () -> f510.pdf(2.0));
        benchmark("Binom(100,0.5).pmf(50)", () -> binom.pmf(50));
        benchmark("Gamma(2,1).pdf(1.5)", () -> gamma.pdf(1.5));
        benchmark("Beta(2,5).pdf(0.3)", () -> beta.pdf(0.3));
        benchmark("LogN(0,1).pdf(1.0)", () -> logn.pdf(1.0));

        // CDF
        benchmark("t(10).cdf(1.812)", () -> t10.cdf(1.812));
        benchmark("chi²(5).cdf(11.07)", () -> chi5.cdf(11.07));
        benchmark("F(5,10).cdf(3.326)", () -> f510.cdf(3.326));
        benchmark("Binom(100,0.5).cdf(55)", () -> binom.cdf(55));
        benchmark("Gamma(2,1).cdf(3.0)", () -> gamma.cdf(3.0));
        benchmark("Beta(2,5).cdf(0.5)", () -> beta.cdf(0.5));
        benchmark("LogN(0,1).cdf(1.0)", () -> logn.cdf(1.0));

        // inverseCDF
        benchmark("t(10).inverseCdf(0.95)", () -> t10.inverseCdf(0.95));
        benchmark("chi²(5).inverseCdf(0.95)", () -> chi5.inverseCdf(0.95));
        benchmark("F(5,10).inverseCdf(0.95)", () -> f510.inverseCdf(0.95));
        benchmark("Gamma(2,1).inverseCdf(0.95)", () -> gamma.inverseCdf(0.95));
        benchmark("Beta(2,5).inverseCdf(0.5)", () -> beta.inverseCdf(0.5));
        benchmark("LogN(0,1).inverseCdf(0.95)", () -> logn.inverseCdf(0.95));
    }

    // ==================== 假设检验 ====================

    @Test
    @DisplayName("假设检验性能")
    void inferenceBenchmark() {
        System.out.println("\n=== Hypothesis Testing Benchmark ===");

        double[] d20 = randomArray(20);
        double[] d100 = randomArray(100);
        double[] d1000 = randomArray(1000);
        double[] y20 = randomArray(20);
        double[] y100 = randomArray(100);

        benchmark("TTest.oneSample(20 pts)", () -> TTest.oneSample(d20, 0));
        benchmark("TTest.oneSample(100 pts)", () -> TTest.oneSample(d100, 0));
        benchmark("TTest.twoSample(20 vs 20)", () -> TTest.twoSample(d20, y20));
        benchmark("TTest.twoSample(100 vs 100)", () -> TTest.twoSample(d100, y100));
        benchmark("TTest.paired(20 vs 20)", () -> TTest.paired(d20, y20));

        double[] obs = {10, 20, 30, 40};
        double[] exp = {25, 25, 25, 25};
        benchmark("ChiSquare.goodnessOfFit(4 bins)", () -> ChiSquareTest.goodnessOfFit(obs, exp));

        double[][] table = {{10, 20, 30}, {6, 9, 17}};
        benchmark("ChiSquare.independence(2x3)", () -> ChiSquareTest.independence(table));

        double[] g1 = randomArray(30);
        double[] g2 = randomArray(30);
        double[] g3 = randomArray(30);
        benchmark("ANOVA.oneWay(3 groups x 30)", () -> AnovaTest.oneWay(g1, g2, g3));
    }

    // ==================== 流式统计 ====================

    @Test
    @DisplayName("流式统计性能")
    void streamingStatsBenchmark() {
        System.out.println("\n=== Streaming Statistics Benchmark ===");

        benchmark("StreamingStats add 1000 values", () -> {
            StreamingStatistics ss = StreamingStatistics.create();
            for (int i = 0; i < 1000; i++) ss.add(i);
        });

        benchmark("StreamingStats add 10000 values", () -> {
            StreamingStatistics ss = StreamingStatistics.create();
            for (int i = 0; i < 10000; i++) ss.add(i);
        });

        StreamingStatistics a = StreamingStatistics.create();
        StreamingStatistics b = StreamingStatistics.create();
        for (int i = 0; i < 1000; i++) { a.add(i); b.add(i + 1000); }
        benchmark("StreamingStats merge(1000+1000)", () -> a.merge(b));

        benchmark("StreamingStats via collector(1000)", () ->
            java.util.stream.IntStream.range(0, 1000)
                .mapToDouble(i -> i)
                .boxed()
                .collect(StreamingStatistics.collector())
        );
    }

    // ==================== 相关性 ====================

    @Test
    @DisplayName("非参数相关性能")
    void correlationBenchmark() {
        System.out.println("\n=== Non-parametric Correlation Benchmark ===");

        double[] x100 = randomArray(100);
        double[] y100 = randomArray(100);
        double[] x1000 = randomArray(1000);
        double[] y1000 = randomArray(1000);

        benchmark("Pearson correlation(100)", () -> Statistics.correlation(x100, y100));
        benchmark("Spearman correlation(100)", () -> Statistics.spearmanCorrelation(x100, y100));
        benchmark("Kendall correlation(100)", () -> Statistics.kendallCorrelation(x100, y100));
        benchmark("Pearson correlation(1000)", () -> Statistics.correlation(x1000, y1000));
        benchmark("Spearman correlation(1000)", () -> Statistics.spearmanCorrelation(x1000, y1000));
    }

    // ==================== 求根 + 微分 ====================

    @Test
    @DisplayName("数值分析性能")
    void analysisBenchmark() {
        System.out.println("\n=== Root Finding & Differentiation Benchmark ===");

        benchmark("bisection(x²-2, [0,2])", () -> RootFinder.bisection(x -> x * x - 2, 0, 2, 1e-12));
        benchmark("brent(x²-2, [0,2])", () -> RootFinder.brent(x -> x * x - 2, 0, 2, 1e-12));
        benchmark("newton(x²-2, x0=1)", () -> RootFinder.newton(x -> x * x - 2, x -> 2 * x, 1.0, 1e-12));
        benchmark("secant(x²-2, 1, 2)", () -> RootFinder.secant(x -> x * x - 2, 1.0, 2.0, 1e-12));

        benchmark("bisection(cos(x), [0,2])", () -> RootFinder.bisection(Math::cos, 0, 2, 1e-12));
        benchmark("brent(cos(x), [0,2])", () -> RootFinder.brent(Math::cos, 0, 2, 1e-12));

        benchmark("derivative(sin, π/4)", () -> Differentiation.derivative(Math::sin, Math.PI / 4));
        benchmark("secondDerivative(sin, π/4)", () -> Differentiation.secondDerivative(Math::sin, Math.PI / 4));
        benchmark("richardson(sin, π/4, order=4)", () -> Differentiation.richardson(Math::sin, Math.PI / 4, 4));
    }

    private static double[] randomArray(int size) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        double[] arr = new double[size];
        for (int i = 0; i < size; i++) arr[i] = rng.nextDouble(-100, 100);
        return arr;
    }
}
