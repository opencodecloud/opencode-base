package cloud.opencode.base.math.distribution;

import cloud.opencode.base.math.exception.MathException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

/**
 * Tests for {@link NormalDistribution}.
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-math V1.0.3
 */
@DisplayName("NormalDistribution 正态分布测试")
class NormalDistributionTest {

    private static final double TOLERANCE = 1e-4;

    @Nested
    @DisplayName("工厂方法与属性")
    class FactoryAndProperties {

        @Test
        @DisplayName("标准正态分布属性")
        void standardNormalProperties() {
            NormalDistribution std = NormalDistribution.STANDARD;
            assertThat(std.mean()).isEqualTo(0.0);
            assertThat(std.standardDeviation()).isEqualTo(1.0);
            assertThat(std.variance()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("自定义分布属性: N(100,15)")
        void customDistributionProperties() {
            NormalDistribution dist = NormalDistribution.of(100, 15);
            assertThat(dist.mean()).isEqualTo(100.0);
            assertThat(dist.standardDeviation()).isEqualTo(15.0);
            assertThat(dist.variance()).isCloseTo(225.0, within(1e-10));
        }

        @Test
        @DisplayName("stdDev <= 0 抛出异常")
        void invalidStdDev() {
            assertThatThrownBy(() -> NormalDistribution.of(0, 0))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> NormalDistribution.of(0, -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("NaN 参数抛出异常")
        void nanParameters() {
            assertThatThrownBy(() -> NormalDistribution.of(Double.NaN, 1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("pdf - 概率密度函数")
    class PdfTest {

        @Test
        @DisplayName("标准正态 pdf(0) 约等于 0.3989")
        void standardPdfAtZero() {
            double pdf = NormalDistribution.STANDARD.pdf(0);
            assertThat(pdf).isCloseTo(0.3989, within(TOLERANCE));
        }

        @Test
        @DisplayName("pdf 在均值处最大")
        void pdfMaxAtMean() {
            NormalDistribution dist = NormalDistribution.of(5, 2);
            double pdfAtMean = dist.pdf(5);
            assertThat(pdfAtMean).isGreaterThan(dist.pdf(4));
            assertThat(pdfAtMean).isGreaterThan(dist.pdf(6));
        }

        @Test
        @DisplayName("pdf 对称性: pdf(mu-x) = pdf(mu+x)")
        void pdfSymmetry() {
            NormalDistribution dist = NormalDistribution.of(3, 2);
            assertThat(dist.pdf(1)).isCloseTo(dist.pdf(5), within(1e-10));
        }

        @Test
        @DisplayName("pdf(+-Infinity) = 0")
        void pdfAtInfinity() {
            assertThat(NormalDistribution.STANDARD.pdf(Double.POSITIVE_INFINITY)).isEqualTo(0.0);
            assertThat(NormalDistribution.STANDARD.pdf(Double.NEGATIVE_INFINITY)).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("cdf - 累积分布函数")
    class CdfTest {

        @Test
        @DisplayName("标准正态 cdf(0) = 0.5")
        void standardCdfAtZero() {
            assertThat(NormalDistribution.STANDARD.cdf(0)).isCloseTo(0.5, within(TOLERANCE));
        }

        @Test
        @DisplayName("标准正态 cdf(1.96) 约等于 0.975")
        void standardCdfAt196() {
            assertThat(NormalDistribution.STANDARD.cdf(1.96)).isCloseTo(0.975, within(TOLERANCE));
        }

        @Test
        @DisplayName("标准正态 cdf(-1.96) 约等于 0.025")
        void standardCdfAtNeg196() {
            assertThat(NormalDistribution.STANDARD.cdf(-1.96)).isCloseTo(0.025, within(TOLERANCE));
        }

        @Test
        @DisplayName("cdf 单调递增")
        void cdfMonotonic() {
            NormalDistribution std = NormalDistribution.STANDARD;
            assertThat(std.cdf(-2)).isLessThan(std.cdf(-1));
            assertThat(std.cdf(-1)).isLessThan(std.cdf(0));
            assertThat(std.cdf(0)).isLessThan(std.cdf(1));
            assertThat(std.cdf(1)).isLessThan(std.cdf(2));
        }

        @Test
        @DisplayName("cdf(-Infinity) = 0, cdf(+Infinity) = 1")
        void cdfAtInfinity() {
            assertThat(NormalDistribution.STANDARD.cdf(Double.NEGATIVE_INFINITY)).isEqualTo(0.0);
            assertThat(NormalDistribution.STANDARD.cdf(Double.POSITIVE_INFINITY)).isEqualTo(1.0);
        }

        @Test
        @DisplayName("自定义 N(100,15): cdf(115) 约等于 0.8413")
        void customCdf() {
            NormalDistribution dist = NormalDistribution.of(100, 15);
            // (115 - 100) / 15 = 1 sigma, so cdf ≈ 0.8413
            assertThat(dist.cdf(115)).isCloseTo(0.8413, within(TOLERANCE));
        }
    }

    @Nested
    @DisplayName("inverseCdf - 逆累积分布函数")
    class InverseCdfTest {

        @Test
        @DisplayName("inverseCdf(0.5) = 0 (标准正态)")
        void inverseCdfAtHalf() {
            assertThat(NormalDistribution.STANDARD.inverseCdf(0.5)).isCloseTo(0.0, within(TOLERANCE));
        }

        @Test
        @DisplayName("inverseCdf(0.975) 约等于 1.96")
        void inverseCdfAt975() {
            assertThat(NormalDistribution.STANDARD.inverseCdf(0.975)).isCloseTo(1.96, within(0.01));
        }

        @Test
        @DisplayName("inverseCdf(0.025) 约等于 -1.96")
        void inverseCdfAt025() {
            assertThat(NormalDistribution.STANDARD.inverseCdf(0.025)).isCloseTo(-1.96, within(0.01));
        }

        @Test
        @DisplayName("cdf(inverseCdf(p)) 约等于 p")
        void roundTrip() {
            NormalDistribution std = NormalDistribution.STANDARD;
            double[] probs = {0.01, 0.1, 0.25, 0.5, 0.75, 0.9, 0.99};
            for (double p : probs) {
                double x = std.inverseCdf(p);
                assertThat(std.cdf(x)).as("roundTrip for p=%f", p).isCloseTo(p, within(1e-3));
            }
        }

        @Test
        @DisplayName("p 超出 (0,1) 范围抛出异常")
        void invalidP() {
            assertThatThrownBy(() -> NormalDistribution.STANDARD.inverseCdf(0))
                    .isInstanceOf(MathException.class);
            assertThatThrownBy(() -> NormalDistribution.STANDARD.inverseCdf(1))
                    .isInstanceOf(MathException.class);
            assertThatThrownBy(() -> NormalDistribution.STANDARD.inverseCdf(-0.1))
                    .isInstanceOf(MathException.class);
            assertThatThrownBy(() -> NormalDistribution.STANDARD.inverseCdf(1.1))
                    .isInstanceOf(MathException.class);
        }
    }

    @Nested
    @DisplayName("sample - 随机抽样")
    class SampleTest {

        @Test
        @DisplayName("sample() 返回有限值")
        void singleSampleIsFinite() {
            double s = NormalDistribution.STANDARD.sample();
            assertThat(Double.isFinite(s)).isTrue();
        }

        @Test
        @DisplayName("sample(n) 返回正确长度的数组")
        void multiSampleLength() {
            double[] samples = NormalDistribution.STANDARD.sample(100);
            assertThat(samples).hasSize(100);
        }

        @Test
        @DisplayName("大量样本的均值接近分布均值")
        void sampleMeanConverges() {
            NormalDistribution dist = NormalDistribution.of(10, 3);
            double[] samples = dist.sample(10_000);
            double mean = 0;
            for (double s : samples) {
                mean += s;
            }
            mean /= samples.length;
            assertThat(mean).isCloseTo(10.0, within(0.5));
        }

        @Test
        @DisplayName("n <= 0 抛出异常")
        void invalidN() {
            assertThatThrownBy(() -> NormalDistribution.STANDARD.sample(0))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> NormalDistribution.STANDARD.sample(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Distributions 工具类")
    class DistributionsTest {

        @Test
        @DisplayName("uniform PDF: 区间内为常数，区间外为0")
        void uniformPdf() {
            var pdf = Distributions.uniform(2, 5);
            assertThat(pdf.applyAsDouble(3)).isCloseTo(1.0 / 3.0, within(1e-10));
            assertThat(pdf.applyAsDouble(1)).isEqualTo(0.0);
            assertThat(pdf.applyAsDouble(6)).isEqualTo(0.0);
            assertThat(pdf.applyAsDouble(2)).isCloseTo(1.0 / 3.0, within(1e-10));
            assertThat(pdf.applyAsDouble(5)).isCloseTo(1.0 / 3.0, within(1e-10));
        }

        @Test
        @DisplayName("uniform: min >= max 抛出异常")
        void uniformInvalid() {
            assertThatThrownBy(() -> Distributions.uniform(5, 2))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> Distributions.uniform(3, 3))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("exponentialPdf: lambda=1, x=0 返回 1")
        void exponentialPdfAtZero() {
            assertThat(Distributions.exponentialPdf(1.0, 0.0)).isCloseTo(1.0, within(1e-10));
        }

        @Test
        @DisplayName("exponentialPdf: lambda=2, x=1 约等于 2*e^-2")
        void exponentialPdfKnown() {
            assertThat(Distributions.exponentialPdf(2.0, 1.0))
                    .isCloseTo(2.0 * Math.exp(-2.0), within(1e-10));
        }

        @Test
        @DisplayName("exponentialPdf: x<0 返回 0")
        void exponentialPdfNegativeX() {
            assertThat(Distributions.exponentialPdf(1.0, -1.0)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("exponentialCdf: lambda=1, x=0 返回 0")
        void exponentialCdfAtZero() {
            assertThat(Distributions.exponentialCdf(1.0, 0.0)).isCloseTo(0.0, within(1e-10));
        }

        @Test
        @DisplayName("exponentialCdf: lambda=1, x=ln(2) 约等于 0.5")
        void exponentialCdfKnown() {
            assertThat(Distributions.exponentialCdf(1.0, Math.log(2)))
                    .isCloseTo(0.5, within(1e-6));
        }

        @Test
        @DisplayName("poissonPmf: lambda=3, k=3 已知值")
        void poissonPmfKnown() {
            // P(X=3) = 3^3 * e^-3 / 3! = 27 * e^-3 / 6
            double expected = 27.0 * Math.exp(-3.0) / 6.0;
            assertThat(Distributions.poissonPmf(3.0, 3)).isCloseTo(expected, within(1e-10));
        }

        @Test
        @DisplayName("poissonPmf: lambda=1, k=0 约等于 e^-1")
        void poissonPmfK0() {
            assertThat(Distributions.poissonPmf(1.0, 0)).isCloseTo(Math.exp(-1.0), within(1e-10));
        }

        @Test
        @DisplayName("poissonCdf: 所有 PMF 之和趋近于 1")
        void poissonCdfConverges() {
            assertThat(Distributions.poissonCdf(5.0, 50)).isCloseTo(1.0, within(1e-6));
        }

        @Test
        @DisplayName("poissonCdf: lambda=1, k=0 约等于 e^-1")
        void poissonCdfK0() {
            assertThat(Distributions.poissonCdf(1.0, 0)).isCloseTo(Math.exp(-1.0), within(1e-10));
        }

        @Test
        @DisplayName("参数校验: lambda <= 0 抛出异常")
        void invalidLambda() {
            assertThatThrownBy(() -> Distributions.exponentialPdf(0, 1))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> Distributions.poissonPmf(-1, 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("参数校验: k < 0 抛出异常")
        void negativeK() {
            assertThatThrownBy(() -> Distributions.poissonPmf(1.0, -1))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> Distributions.poissonCdf(1.0, -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
