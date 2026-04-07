package cloud.opencode.base.math.distribution;

import cloud.opencode.base.math.exception.MathException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

/**
 * Tests for {@link TDistribution}.
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-math V1.0.3
 */
@DisplayName("TDistribution 学生 t 分布测试")
class TDistributionTest {

    private static final double TOLERANCE = 1e-3;

    @Nested
    @DisplayName("工厂方法与属性")
    class FactoryAndProperties {

        @Test
        @DisplayName("自由度属性正确")
        void degreesOfFreedom() {
            TDistribution t = TDistribution.of(10);
            assertThat(t.degreesOfFreedom()).isEqualTo(10.0);
        }

        @Test
        @DisplayName("df <= 0 抛出异常")
        void invalidDf() {
            assertThatThrownBy(() -> TDistribution.of(0))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> TDistribution.of(-5))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("NaN 参数抛出异常")
        void nanDf() {
            assertThatThrownBy(() -> TDistribution.of(Double.NaN))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Infinity 参数抛出异常")
        void infiniteDf() {
            assertThatThrownBy(() -> TDistribution.of(Double.POSITIVE_INFINITY))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("toString 包含自由度")
        void toStringTest() {
            assertThat(TDistribution.of(10).toString()).contains("10");
        }
    }

    @Nested
    @DisplayName("pdf - 概率密度函数")
    class PdfTest {

        @Test
        @DisplayName("pdf(0) 为正值且在均值处最大")
        void pdfAtZero() {
            TDistribution t = TDistribution.of(10);
            double pdfAt0 = t.pdf(0);
            assertThat(pdfAt0).isGreaterThan(0);
            assertThat(pdfAt0).isGreaterThan(t.pdf(1));
            assertThat(pdfAt0).isGreaterThan(t.pdf(-1));
        }

        @Test
        @DisplayName("pdf 对称性: pdf(-x) = pdf(x)")
        void pdfSymmetry() {
            TDistribution t = TDistribution.of(5);
            assertThat(t.pdf(-2.0)).isCloseTo(t.pdf(2.0), within(1e-10));
        }

        @Test
        @DisplayName("pdf(NaN) = NaN")
        void pdfNaN() {
            assertThat(TDistribution.of(5).pdf(Double.NaN)).isNaN();
        }

        @Test
        @DisplayName("pdf(+-Infinity) = 0")
        void pdfInfinity() {
            TDistribution t = TDistribution.of(5);
            assertThat(t.pdf(Double.POSITIVE_INFINITY)).isEqualTo(0.0);
            assertThat(t.pdf(Double.NEGATIVE_INFINITY)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("t(1) 即 Cauchy 分布, pdf(0) = 1/pi")
        void cauchyPdf() {
            TDistribution t = TDistribution.of(1);
            assertThat(t.pdf(0)).isCloseTo(1.0 / Math.PI, within(1e-6));
        }
    }

    @Nested
    @DisplayName("cdf - 累积分布函数")
    class CdfTest {

        @Test
        @DisplayName("t(10).cdf(0) = 0.5")
        void cdfAtZero() {
            assertThat(TDistribution.of(10).cdf(0)).isCloseTo(0.5, within(TOLERANCE));
        }

        @Test
        @DisplayName("t(10).cdf(1.812) 约等于 0.95")
        void cdfKnownValue() {
            assertThat(TDistribution.of(10).cdf(1.812)).isCloseTo(0.95, within(0.01));
        }

        @Test
        @DisplayName("t(1).cdf(1) 约等于 0.75 (Cauchy)")
        void cauchyCdf() {
            assertThat(TDistribution.of(1).cdf(1.0)).isCloseTo(0.75, within(0.01));
        }

        @Test
        @DisplayName("cdf 单调递增")
        void cdfMonotonic() {
            TDistribution t = TDistribution.of(5);
            assertThat(t.cdf(-2)).isLessThan(t.cdf(0));
            assertThat(t.cdf(0)).isLessThan(t.cdf(2));
        }

        @Test
        @DisplayName("cdf(-Infinity) = 0, cdf(+Infinity) = 1")
        void cdfAtInfinity() {
            TDistribution t = TDistribution.of(5);
            assertThat(t.cdf(Double.NEGATIVE_INFINITY)).isEqualTo(0.0);
            assertThat(t.cdf(Double.POSITIVE_INFINITY)).isEqualTo(1.0);
        }

        @Test
        @DisplayName("cdf(NaN) = NaN")
        void cdfNaN() {
            assertThat(TDistribution.of(5).cdf(Double.NaN)).isNaN();
        }

        @Test
        @DisplayName("对称性: cdf(-x) + cdf(x) = 1")
        void cdfSymmetry() {
            TDistribution t = TDistribution.of(10);
            double x = 1.5;
            assertThat(t.cdf(-x) + t.cdf(x)).isCloseTo(1.0, within(1e-6));
        }
    }

    @Nested
    @DisplayName("inverseCdf - 逆累积分布函数")
    class InverseCdfTest {

        @Test
        @DisplayName("inverseCdf(0.5) = 0")
        void inverseCdfAtHalf() {
            assertThat(TDistribution.of(10).inverseCdf(0.5)).isCloseTo(0.0, within(TOLERANCE));
        }

        @Test
        @DisplayName("t(10).inverseCdf(0.95) 约等于 1.812")
        void inverseCdfKnownValue() {
            assertThat(TDistribution.of(10).inverseCdf(0.95)).isCloseTo(1.812, within(0.01));
        }

        @Test
        @DisplayName("cdf(inverseCdf(p)) 约等于 p")
        void roundTrip() {
            TDistribution t = TDistribution.of(10);
            double[] probs = {0.05, 0.1, 0.25, 0.5, 0.75, 0.9, 0.95};
            for (double p : probs) {
                double x = t.inverseCdf(p);
                assertThat(t.cdf(x)).as("roundTrip for p=%f", p).isCloseTo(p, within(1e-4));
            }
        }

        @Test
        @DisplayName("p 超出 (0,1) 范围抛出异常")
        void invalidP() {
            TDistribution t = TDistribution.of(10);
            assertThatThrownBy(() -> t.inverseCdf(0)).isInstanceOf(MathException.class);
            assertThatThrownBy(() -> t.inverseCdf(1)).isInstanceOf(MathException.class);
            assertThatThrownBy(() -> t.inverseCdf(Double.NaN)).isInstanceOf(MathException.class);
        }
    }
}
