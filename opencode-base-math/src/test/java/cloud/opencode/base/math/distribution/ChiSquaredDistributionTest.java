package cloud.opencode.base.math.distribution;

import cloud.opencode.base.math.exception.MathException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

/**
 * Tests for {@link ChiSquaredDistribution}.
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-math V1.0.3
 */
@DisplayName("ChiSquaredDistribution 卡方分布测试")
class ChiSquaredDistributionTest {

    private static final double TOLERANCE = 1e-3;

    @Nested
    @DisplayName("工厂方法与属性")
    class FactoryAndProperties {

        @Test
        @DisplayName("自由度属性正确")
        void degreesOfFreedom() {
            assertThat(ChiSquaredDistribution.of(5).degreesOfFreedom()).isEqualTo(5.0);
        }

        @Test
        @DisplayName("df <= 0 抛出异常")
        void invalidDf() {
            assertThatThrownBy(() -> ChiSquaredDistribution.of(0))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> ChiSquaredDistribution.of(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("NaN/Infinity 参数抛出异常")
        void nanInfDf() {
            assertThatThrownBy(() -> ChiSquaredDistribution.of(Double.NaN))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> ChiSquaredDistribution.of(Double.POSITIVE_INFINITY))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("toString 包含自由度")
        void toStringTest() {
            assertThat(ChiSquaredDistribution.of(5).toString()).contains("5");
        }
    }

    @Nested
    @DisplayName("pdf - 概率密度函数")
    class PdfTest {

        @Test
        @DisplayName("x < 0 返回 0")
        void pdfNegativeX() {
            assertThat(ChiSquaredDistribution.of(5).pdf(-1)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("pdf 为正值")
        void pdfPositive() {
            assertThat(ChiSquaredDistribution.of(5).pdf(3.0)).isGreaterThan(0.0);
        }

        @Test
        @DisplayName("df=2 时 pdf(0)=0.5 (指数分布)")
        void pdfDf2AtZero() {
            assertThat(ChiSquaredDistribution.of(2).pdf(0)).isCloseTo(0.5, within(1e-10));
        }

        @Test
        @DisplayName("pdf(NaN) = NaN")
        void pdfNaN() {
            assertThat(ChiSquaredDistribution.of(5).pdf(Double.NaN)).isNaN();
        }

        @Test
        @DisplayName("pdf(+Infinity) = 0")
        void pdfInfinity() {
            assertThat(ChiSquaredDistribution.of(5).pdf(Double.POSITIVE_INFINITY)).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("cdf - 累积分布函数")
    class CdfTest {

        @Test
        @DisplayName("chi2(5).cdf(11.07) 约等于 0.95")
        void cdfKnownValue() {
            assertThat(ChiSquaredDistribution.of(5).cdf(11.07)).isCloseTo(0.95, within(0.01));
        }

        @Test
        @DisplayName("chi2(1).cdf(3.841) 约等于 0.95")
        void cdfDf1() {
            assertThat(ChiSquaredDistribution.of(1).cdf(3.841)).isCloseTo(0.95, within(0.01));
        }

        @Test
        @DisplayName("cdf(0) = 0")
        void cdfAtZero() {
            assertThat(ChiSquaredDistribution.of(5).cdf(0)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("cdf 单调递增")
        void cdfMonotonic() {
            ChiSquaredDistribution chi2 = ChiSquaredDistribution.of(5);
            assertThat(chi2.cdf(1)).isLessThan(chi2.cdf(5));
            assertThat(chi2.cdf(5)).isLessThan(chi2.cdf(10));
        }

        @Test
        @DisplayName("cdf(+Infinity) = 1")
        void cdfInfinity() {
            assertThat(ChiSquaredDistribution.of(5).cdf(Double.POSITIVE_INFINITY)).isEqualTo(1.0);
        }

        @Test
        @DisplayName("cdf(NaN) = NaN")
        void cdfNaN() {
            assertThat(ChiSquaredDistribution.of(5).cdf(Double.NaN)).isNaN();
        }
    }

    @Nested
    @DisplayName("inverseCdf - 逆累积分布函数")
    class InverseCdfTest {

        @Test
        @DisplayName("chi2(5).inverseCdf(0.95) 约等于 11.07")
        void inverseCdfKnownValue() {
            assertThat(ChiSquaredDistribution.of(5).inverseCdf(0.95))
                    .isCloseTo(11.07, within(0.05));
        }

        @Test
        @DisplayName("cdf(inverseCdf(p)) 约等于 p")
        void roundTrip() {
            ChiSquaredDistribution chi2 = ChiSquaredDistribution.of(10);
            double[] probs = {0.05, 0.1, 0.25, 0.5, 0.75, 0.9, 0.95};
            for (double p : probs) {
                double x = chi2.inverseCdf(p);
                assertThat(chi2.cdf(x)).as("roundTrip for p=%f", p).isCloseTo(p, within(1e-4));
            }
        }

        @Test
        @DisplayName("p 超出 (0,1) 范围抛出异常")
        void invalidP() {
            ChiSquaredDistribution chi2 = ChiSquaredDistribution.of(5);
            assertThatThrownBy(() -> chi2.inverseCdf(0)).isInstanceOf(MathException.class);
            assertThatThrownBy(() -> chi2.inverseCdf(1)).isInstanceOf(MathException.class);
        }
    }
}
