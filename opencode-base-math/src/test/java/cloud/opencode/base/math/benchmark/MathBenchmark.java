package cloud.opencode.base.math.benchmark;

import cloud.opencode.base.math.combinatorics.Combinatorics;
import cloud.opencode.base.math.distribution.NormalDistribution;
import cloud.opencode.base.math.integration.NumericalIntegration;
import cloud.opencode.base.math.interpolation.Interpolation;
import cloud.opencode.base.math.linalg.Matrix;
import cloud.opencode.base.math.linalg.Vector;
import cloud.opencode.base.math.special.SpecialFunctions;
import cloud.opencode.base.math.stats.Regression;
import cloud.opencode.base.math.stats.Statistics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Lightweight performance benchmarks for opencode-base-math.
 * 轻量级性能基准测试
 *
 * <p>Uses nanoTime-based timing with warmup. Not JMH, but good enough
 * for relative comparisons and regression detection.</p>
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-math V1.0.3
 */
@DisplayName("性能基准测试")
class MathBenchmark {

    private static final int WARMUP = 500;
    private static final int ITERATIONS = 5_000;

    // ==================== 数据准备 ====================

    private static double[] randomArray(int size) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        double[] arr = new double[size];
        for (int i = 0; i < size; i++) {
            arr[i] = rng.nextDouble(-1000, 1000);
        }
        return arr;
    }

    private static double[] sortedX(int size) {
        double[] x = new double[size];
        for (int i = 0; i < size; i++) {
            x[i] = i * 0.1;
        }
        return x;
    }

    private static double[] valuesForX(double[] x) {
        double[] y = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            y[i] = Math.sin(x[i]);
        }
        return y;
    }

    // ==================== 基准测试工具 ====================

    @FunctionalInterface
    interface BenchmarkTask {
        void run();
    }

    private static long benchmark(String name, BenchmarkTask task) {
        // Warmup
        for (int i = 0; i < WARMUP; i++) {
            task.run();
        }

        // Measure
        long start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            task.run();
        }
        long elapsed = System.nanoTime() - start;
        long nsPerOp = elapsed / ITERATIONS;
        double opsPerMs = ITERATIONS * 1_000_000.0 / elapsed;

        System.out.printf("  %-45s %,8d ns/op   %,.0f ops/ms%n", name, nsPerOp, opsPerMs);
        return nsPerOp;
    }

    // ==================== Statistics 基准 ====================

    @Test
    @DisplayName("Statistics 性能")
    void statisticsBenchmark() {
        System.out.println("\n=== Statistics Benchmark ===");
        double[] data100 = randomArray(100);
        double[] data1000 = randomArray(1000);
        double[] dataX = randomArray(1000);
        double[] dataY = randomArray(1000);

        benchmark("percentile(100 elements, p=90)", () -> Statistics.percentile(data100, 90));
        benchmark("percentile(1000 elements, p=50)", () -> Statistics.percentile(data1000, 50));
        benchmark("mode(100 elements)", () -> Statistics.mode(data100));
        benchmark("mode(1000 elements)", () -> Statistics.mode(data1000));
        benchmark("skewness(100 elements)", () -> Statistics.skewness(data100));
        benchmark("kurtosis(1000 elements)", () -> Statistics.kurtosis(data1000));
        benchmark("covariance(1000 elements)", () -> Statistics.covariance(dataX, dataY));
        benchmark("correlation(1000 elements)", () -> Statistics.correlation(dataX, dataY));
        benchmark("weightedMean(100 elements)", () -> {
            double[] w = new double[100];
            java.util.Arrays.fill(w, 1.0);
            Statistics.weightedMean(data100, w);
        });
        benchmark("geometricMean(100 elements, abs)", () -> {
            double[] pos = new double[100];
            for (int i = 0; i < 100; i++) pos[i] = Math.abs(data100[i]) + 0.01;
            Statistics.geometricMean(pos);
        });
    }

    // ==================== Regression 基准 ====================

    @Test
    @DisplayName("Regression 性能")
    void regressionBenchmark() {
        System.out.println("\n=== Regression Benchmark ===");
        double[] x100 = randomArray(100);
        double[] y100 = randomArray(100);
        double[] x1000 = randomArray(1000);
        double[] y1000 = randomArray(1000);

        benchmark("linear regression(100 pts)", () -> Regression.linear(x100, y100));
        benchmark("linear regression(1000 pts)", () -> Regression.linear(x1000, y1000));
    }

    // ==================== Vector 基准 ====================

    @Test
    @DisplayName("Vector 性能")
    void vectorBenchmark() {
        System.out.println("\n=== Vector Benchmark ===");
        Vector v3a = Vector.of(1.0, 2.0, 3.0);
        Vector v3b = Vector.of(4.0, 5.0, 6.0);
        Vector v100a = Vector.of(randomArray(100));
        Vector v100b = Vector.of(randomArray(100));
        Vector v1000a = Vector.of(randomArray(1000));
        Vector v1000b = Vector.of(randomArray(1000));

        benchmark("dot(3D)", () -> v3a.dot(v3b));
        benchmark("dot(100D)", () -> v100a.dot(v100b));
        benchmark("dot(1000D)", () -> v1000a.dot(v1000b));
        benchmark("cross(3D)", () -> v3a.cross(v3b));
        benchmark("normalize(100D)", () -> v100a.normalize());
        benchmark("angle(100D)", () -> v100a.angle(v100b));
        benchmark("distanceTo(1000D)", () -> v1000a.distanceTo(v1000b));
        benchmark("add(1000D)", () -> v1000a.add(v1000b));
    }

    // ==================== Matrix 基准 ====================

    @Test
    @DisplayName("Matrix 性能")
    void matrixBenchmark() {
        System.out.println("\n=== Matrix Benchmark ===");
        double[][] d10 = new double[10][10];
        double[][] d50 = new double[50][50];
        double[][] d100 = new double[100][100];
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                double v = rng.nextDouble(-10, 10);
                if (i < 10 && j < 10) d10[i][j] = v;
                if (i < 50 && j < 50) d50[i][j] = v;
                d100[i][j] = v;
            }
            // Make diagonal dominant for invertibility
            if (i < 10) d10[i][i] += 50;
            if (i < 50) d50[i][i] += 200;
            d100[i][i] += 500;
        }
        Matrix m10 = Matrix.of(d10);
        Matrix m50 = Matrix.of(d50);
        Matrix m100 = Matrix.of(d100);

        benchmark("multiply(10x10 * 10x10)", () -> m10.multiply(m10));
        benchmark("multiply(50x50 * 50x50)", () -> m50.multiply(m50));
        benchmark("multiply(100x100 * 100x100)", () -> m100.multiply(m100));
        benchmark("transpose(100x100)", () -> m100.transpose());
        benchmark("determinant(10x10)", () -> m10.determinant());
        benchmark("determinant(50x50)", () -> m50.determinant());
        benchmark("inverse(10x10)", () -> m10.inverse());
        benchmark("inverse(50x50)", () -> m50.inverse());
    }

    // ==================== Interpolation 基准 ====================

    @Test
    @DisplayName("Interpolation 性能")
    void interpolationBenchmark() {
        System.out.println("\n=== Interpolation Benchmark ===");
        double[] x100 = sortedX(100);
        double[] y100 = valuesForX(x100);
        double[] x1000 = sortedX(1000);
        double[] y1000 = valuesForX(x1000);

        benchmark("linear interp(100 pts)", () -> Interpolation.linear(x100, y100, 5.05));
        benchmark("linear interp(1000 pts)", () -> Interpolation.linear(x1000, y1000, 50.05));
        benchmark("lagrange(20 pts)", () -> {
            double[] xx = sortedX(20);
            double[] yy = valuesForX(xx);
            Interpolation.lagrange(xx, yy, 1.05);
        });
        benchmark("cubicSpline(100 pts)", () -> Interpolation.cubicSpline(x100, y100, 5.05));
        benchmark("cubicSpline(1000 pts)", () -> Interpolation.cubicSpline(x1000, y1000, 50.05));
    }

    // ==================== Integration 基准 ====================

    @Test
    @DisplayName("Integration 性能")
    void integrationBenchmark() {
        System.out.println("\n=== Integration Benchmark ===");
        benchmark("trapezoid(sin, 0→π, n=100)", () ->
                NumericalIntegration.trapezoid(Math::sin, 0, Math.PI, 100));
        benchmark("trapezoid(sin, 0→π, n=1000)", () ->
                NumericalIntegration.trapezoid(Math::sin, 0, Math.PI, 1000));
        benchmark("simpson(sin, 0→π, n=100)", () ->
                NumericalIntegration.simpson(Math::sin, 0, Math.PI, 100));
        benchmark("romberg(sin, 0→π, iter=10, tol=1e-12)", () ->
                NumericalIntegration.romberg(Math::sin, 0, Math.PI, 10, 1e-12));
        benchmark("gaussLegendre(sin, 0→π, 5pt)", () ->
                NumericalIntegration.gaussLegendre(Math::sin, 0, Math.PI, 5));
    }

    // ==================== Combinatorics 基准 ====================

    @Test
    @DisplayName("Combinatorics 性能")
    void combinatoricsBenchmark() {
        System.out.println("\n=== Combinatorics Benchmark ===");
        benchmark("binomial(20, 10)", () -> Combinatorics.binomial(20, 10));
        benchmark("binomial(60, 30)", () -> Combinatorics.binomial(60, 30));
        benchmark("binomialBig(100, 50)", () -> Combinatorics.binomialBig(100, 50));
        benchmark("permutation(20, 10)", () -> Combinatorics.permutation(20, 10));
        benchmark("catalanNumber(15)", () -> Combinatorics.catalanNumber(15));
        benchmark("stirlingSecond(15, 7)", () -> Combinatorics.stirlingSecond(15, 7));
        benchmark("bellNumber(15)", () -> Combinatorics.bellNumber(15));
        benchmark("derangements(15)", () -> Combinatorics.derangements(15));
    }

    // ==================== Distribution 基准 ====================

    @Test
    @DisplayName("Distribution 性能")
    void distributionBenchmark() {
        System.out.println("\n=== Distribution Benchmark ===");
        NormalDistribution std = NormalDistribution.STANDARD;
        NormalDistribution custom = NormalDistribution.of(100, 15);

        benchmark("N(0,1).pdf(1.5)", () -> std.pdf(1.5));
        benchmark("N(0,1).cdf(1.96)", () -> std.cdf(1.96));
        benchmark("N(0,1).inverseCdf(0.975)", () -> std.inverseCdf(0.975));
        benchmark("N(0,1).sample(1000)", () -> std.sample(1000));
        benchmark("N(100,15).cdf(115)", () -> custom.cdf(115));
    }

    // ==================== Special Functions 基准 ====================

    @Test
    @DisplayName("SpecialFunctions 性能")
    void specialFunctionsBenchmark() {
        System.out.println("\n=== Special Functions Benchmark ===");
        benchmark("gamma(5.5)", () -> SpecialFunctions.gamma(5.5));
        benchmark("logGamma(100.0)", () -> SpecialFunctions.logGamma(100.0));
        benchmark("beta(2.0, 3.0)", () -> SpecialFunctions.beta(2.0, 3.0));
        benchmark("erf(1.0)", () -> SpecialFunctions.erf(1.0));
        benchmark("erfc(2.0)", () -> SpecialFunctions.erfc(2.0));
        benchmark("regularizedBeta(0.5, 2, 3)", () -> SpecialFunctions.regularizedBeta(0.5, 2, 3));
        benchmark("regularizedGammaP(3, 2)", () -> SpecialFunctions.regularizedGammaP(3, 2));
    }
}
