package cloud.opencode.base.math.distribution;

import cloud.opencode.base.math.exception.MathException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

/**
 * Tests for {@link LogNormalDistribution}.
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-math V1.0.3
 */
@DisplayName("LogNormalDistribution 对数正态分布测试")
class LogNormalDistributionTest {

    private static final double TOLERANCE = 1e-4;

    @Nested
    @DisplayName("工厂方法与属性")
    class FactoryAndProperties {

        @Test
        @DisplayName("属性正确")
        void properties() {
            LogNormalDistribution ln = LogNormalDistribution.of(0, 1);
            assertThat(ln.mu()).isEqualTo(0.0);
            assertThat(ln.sigma()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("sigma <= 0 抛出异常")
        void invalidSigma() {
            assertThatThrownBy(() -> LogNormalDistribution.of(0, 0))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> LogNormalDistribution.of(0, -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("NaN 参数抛出异常")
        void nanParams() {
            assertThatThrownBy(() -> LogNormalDistribution.of(Double.NaN, 1))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> LogNormalDistribution.of(0, Double.NaN))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Infinity 参数抛出异常")
        void infiniteParams() {
            assertThatThrownBy(() -> LogNormalDistribution.of(Double.POSITIVE_INFINITY, 1))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> LogNormalDistribution.of(0, Double.POSITIVE_INFINITY))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("toString 包含参数")
        void toStringTest() {
            assertThat(LogNormalDistribution.of(0, 1).toString()).contains("mu=0").contains("sigma=1");
        }
    }

    @Nested
    @DisplayName("mean 和 variance")
    class MeanAndVariance {

        @Test
        @DisplayName("LogNormal(0,1).mean() = e^0.5")
        void mean() {
            assertThat(LogNormalDistribution.of(0, 1).mean())
                    .isCloseTo(Math.exp(0.5), within(1e-10));
        }

        @Test
        @DisplayName("variance 正确")
        void variance() {
            // Var = (e^1 - 1) * e^1 = (e-1)*e
            LogNormalDistribution ln = LogNormalDistribution.of(0, 1);
            double expected = Math.expm1(1.0) * Math.exp(1.0);
            assertThat(ln.variance()).isCloseTo(expected, within(1e-10));
        }
    }

    @Nested
    @DisplayName("pdf - 概率密度函数")
    class PdfTest {

        @Test
        @DisplayName("x <= 0 返回 0")
        void pdfNonPositive() {
            LogNormalDistribution ln = LogNormalDistribution.of(0, 1);
            assertThat(ln.pdf(0)).isEqualTo(0.0);
            assertThat(ln.pdf(-1)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("pdf 为正值")
        void pdfPositive() {
            assertThat(LogNormalDistribution.of(0, 1).pdf(1.0)).isGreaterThan(0.0);
        }

        @Test
        @DisplayName("LogNormal(0,1).pdf(1) 已知值")
        void pdfKnown() {
            // At x=1, ln(x)=0, so pdf = 1/(1*1*sqrt(2pi)) * exp(0) = 1/sqrt(2pi)
            double expected = 1.0 / Math.sqrt(2.0 * Math.PI);
            assertThat(LogNormalDistribution.of(0, 1).pdf(1.0))
                    .isCloseTo(expected, within(1e-10));
        }

        @Test
        @DisplayName("pdf(NaN) = NaN")
        void pdfNaN() {
            assertThat(LogNormalDistribution.of(0, 1).pdf(Double.NaN)).isNaN();
        }

        @Test
        @DisplayName("pdf(+Infinity) = 0")
        void pdfInfinity() {
            assertThat(LogNormalDistribution.of(0, 1).pdf(Double.POSITIVE_INFINITY)).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("cdf - 累积分布函数")
    class CdfTest {

        @Test
        @DisplayName("LogNormal(0,1).cdf(1) = 0.5")
        void cdfAtMedian() {
            // Median of LogNormal(0,1) is e^0 = 1
            assertThat(LogNormalDistribution.of(0, 1).cdf(1.0))
                    .isCloseTo(0.5, within(TOLERANCE));
        }

        @Test
        @DisplayName("cdf(0) = 0")
        void cdfAtZero() {
            assertThat(LogNormalDistribution.of(0, 1).cdf(0)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("cdf(x) 对负值返回 0")
        void cdfNegative() {
            assertThat(LogNormalDistribution.of(0, 1).cdf(-1)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("cdf 单调递增")
        void cdfMonotonic() {
            LogNormalDistribution ln = LogNormalDistribution.of(0, 1);
            assertThat(ln.cdf(0.5)).isLessThan(ln.cdf(1.0));
            assertThat(ln.cdf(1.0)).isLessThan(ln.cdf(2.0));
        }

        @Test
        @DisplayName("cdf(+Infinity) = 1")
        void cdfInfinity() {
            assertThat(LogNormalDistribution.of(0, 1).cdf(Double.POSITIVE_INFINITY)).isEqualTo(1.0);
        }

        @Test
        @DisplayName("cdf(NaN) = NaN")
        void cdfNaN() {
            assertThat(LogNormalDistribution.of(0, 1).cdf(Double.NaN)).isNaN();
        }
    }

    @Nested
    @DisplayName("inverseCdf - 逆累积分布函数")
    class InverseCdfTest {

        @Test
        @DisplayName("inverseCdf(0.5) = 1 对于 LogNormal(0,1)")
        void inverseCdfAtMedian() {
            assertThat(LogNormalDistribution.of(0, 1).inverseCdf(0.5))
                    .isCloseTo(1.0, within(TOLERANCE));
        }

        @Test
        @DisplayName("cdf(inverseCdf(p)) 约等于 p")
        void roundTrip() {
            LogNormalDistribution ln = LogNormalDistribution.of(0, 1);
            double[] probs = {0.05, 0.1, 0.25, 0.5, 0.75, 0.9, 0.95};
            for (double p : probs) {
                double x = ln.inverseCdf(p);
                assertThat(ln.cdf(x)).as("roundTrip for p=%f", p).isCloseTo(p, within(1e-3));
            }
        }

        @Test
        @DisplayName("p 超出 (0,1) 范围抛出异常")
        void invalidP() {
            LogNormalDistribution ln = LogNormalDistribution.of(0, 1);
            assertThatThrownBy(() -> ln.inverseCdf(0)).isInstanceOf(MathException.class);
            assertThatThrownBy(() -> ln.inverseCdf(1)).isInstanceOf(MathException.class);
            assertThatThrownBy(() -> ln.inverseCdf(Double.NaN)).isInstanceOf(MathException.class);
        }
    }
}
