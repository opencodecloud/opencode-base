package cloud.opencode.base.math.distribution;

import cloud.opencode.base.math.exception.MathException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

/**
 * Tests for {@link GammaDistribution}.
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-math V1.0.3
 */
@DisplayName("GammaDistribution Gamma 分布测试")
class GammaDistributionTest {

    private static final double TOLERANCE = 1e-4;

    @Nested
    @DisplayName("工厂方法与属性")
    class FactoryAndProperties {

        @Test
        @DisplayName("属性正确")
        void properties() {
            GammaDistribution g = GammaDistribution.of(2, 3);
            assertThat(g.shape()).isEqualTo(2.0);
            assertThat(g.scale()).isEqualTo(3.0);
        }

        @Test
        @DisplayName("shape <= 0 抛出异常")
        void invalidShape() {
            assertThatThrownBy(() -> GammaDistribution.of(0, 1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("scale <= 0 抛出异常")
        void invalidScale() {
            assertThatThrownBy(() -> GammaDistribution.of(1, 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("NaN 参数抛出异常")
        void nanParams() {
            assertThatThrownBy(() -> GammaDistribution.of(Double.NaN, 1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("toString 包含参数")
        void toStringTest() {
            assertThat(GammaDistribution.of(2, 3).toString()).contains("2").contains("3");
        }
    }

    @Nested
    @DisplayName("mean 和 variance")
    class MeanAndVariance {

        @Test
        @DisplayName("mean = shape * scale")
        void mean() {
            assertThat(GammaDistribution.of(2, 3).mean()).isCloseTo(6.0, within(1e-10));
        }

        @Test
        @DisplayName("variance = shape * scale^2")
        void variance() {
            assertThat(GammaDistribution.of(2, 3).variance()).isCloseTo(18.0, within(1e-10));
        }
    }

    @Nested
    @DisplayName("pdf - 概率密度函数")
    class PdfTest {

        @Test
        @DisplayName("x < 0 返回 0")
        void pdfNegativeX() {
            assertThat(GammaDistribution.of(2, 1).pdf(-1)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Gamma(1,1) = Exp(1), pdf(0) = 1")
        void exponentialPdfAtZero() {
            assertThat(GammaDistribution.of(1, 1).pdf(0)).isCloseTo(1.0, within(1e-10));
        }

        @Test
        @DisplayName("pdf 为正值")
        void pdfPositive() {
            assertThat(GammaDistribution.of(2, 1).pdf(1.0)).isGreaterThan(0.0);
        }

        @Test
        @DisplayName("pdf(NaN) = NaN")
        void pdfNaN() {
            assertThat(GammaDistribution.of(2, 1).pdf(Double.NaN)).isNaN();
        }

        @Test
        @DisplayName("pdf(+Infinity) = 0")
        void pdfInfinity() {
            assertThat(GammaDistribution.of(2, 1).pdf(Double.POSITIVE_INFINITY)).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("cdf - 累积分布函数")
    class CdfTest {

        @Test
        @DisplayName("Gamma(2,1).cdf(3) 约等于 0.8009")
        void cdfKnownValue() {
            assertThat(GammaDistribution.of(2, 1).cdf(3))
                    .isCloseTo(0.8009, within(0.01));
        }

        @Test
        @DisplayName("Gamma(1,1) = Exp(1), cdf(1) = 1-e^-1")
        void exponentialCdf() {
            assertThat(GammaDistribution.of(1, 1).cdf(1))
                    .isCloseTo(1.0 - Math.exp(-1.0), within(1e-6));
        }

        @Test
        @DisplayName("cdf(0) = 0")
        void cdfAtZero() {
            assertThat(GammaDistribution.of(2, 1).cdf(0)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("cdf 单调递增")
        void cdfMonotonic() {
            GammaDistribution g = GammaDistribution.of(2, 1);
            assertThat(g.cdf(1)).isLessThan(g.cdf(2));
            assertThat(g.cdf(2)).isLessThan(g.cdf(5));
        }

        @Test
        @DisplayName("cdf(+Infinity) = 1")
        void cdfInfinity() {
            assertThat(GammaDistribution.of(2, 1).cdf(Double.POSITIVE_INFINITY)).isEqualTo(1.0);
        }

        @Test
        @DisplayName("cdf(NaN) = NaN")
        void cdfNaN() {
            assertThat(GammaDistribution.of(2, 1).cdf(Double.NaN)).isNaN();
        }
    }

    @Nested
    @DisplayName("inverseCdf - 逆累积分布函数")
    class InverseCdfTest {

        @Test
        @DisplayName("cdf(inverseCdf(p)) 约等于 p")
        void roundTrip() {
            GammaDistribution g = GammaDistribution.of(2, 1);
            double[] probs = {0.05, 0.1, 0.25, 0.5, 0.75, 0.9, 0.95};
            for (double p : probs) {
                double x = g.inverseCdf(p);
                assertThat(g.cdf(x)).as("roundTrip for p=%f", p).isCloseTo(p, within(1e-4));
            }
        }

        @Test
        @DisplayName("Gamma(2,1).inverseCdf(0.8009) 约等于 3")
        void inverseCdfKnownValue() {
            assertThat(GammaDistribution.of(2, 1).inverseCdf(0.8009))
                    .isCloseTo(3.0, within(0.05));
        }

        @Test
        @DisplayName("p 超出 (0,1) 范围抛出异常")
        void invalidP() {
            GammaDistribution g = GammaDistribution.of(2, 1);
            assertThatThrownBy(() -> g.inverseCdf(0)).isInstanceOf(MathException.class);
            assertThatThrownBy(() -> g.inverseCdf(1)).isInstanceOf(MathException.class);
        }
    }
}
