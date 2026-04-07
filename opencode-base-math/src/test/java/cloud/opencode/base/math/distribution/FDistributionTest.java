package cloud.opencode.base.math.distribution;

import cloud.opencode.base.math.exception.MathException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

/**
 * Tests for {@link FDistribution}.
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-math V1.0.3
 */
@DisplayName("FDistribution F 分布测试")
class FDistributionTest {

    private static final double TOLERANCE = 1e-3;

    @Nested
    @DisplayName("工厂方法与属性")
    class FactoryAndProperties {

        @Test
        @DisplayName("自由度属性正确")
        void degreesOfFreedom() {
            FDistribution f = FDistribution.of(5, 10);
            assertThat(f.numeratorDf()).isEqualTo(5.0);
            assertThat(f.denominatorDf()).isEqualTo(10.0);
        }

        @Test
        @DisplayName("df <= 0 抛出异常")
        void invalidDf() {
            assertThatThrownBy(() -> FDistribution.of(0, 10))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> FDistribution.of(5, 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("NaN 参数抛出异常")
        void nanDf() {
            assertThatThrownBy(() -> FDistribution.of(Double.NaN, 10))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("toString 包含自由度")
        void toStringTest() {
            assertThat(FDistribution.of(5, 10).toString()).contains("5").contains("10");
        }
    }

    @Nested
    @DisplayName("pdf - 概率密度函数")
    class PdfTest {

        @Test
        @DisplayName("x < 0 返回 0")
        void pdfNegativeX() {
            assertThat(FDistribution.of(5, 10).pdf(-1)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("pdf 为正值")
        void pdfPositive() {
            assertThat(FDistribution.of(5, 10).pdf(1.0)).isGreaterThan(0.0);
        }

        @Test
        @DisplayName("pdf(NaN) = NaN")
        void pdfNaN() {
            assertThat(FDistribution.of(5, 10).pdf(Double.NaN)).isNaN();
        }

        @Test
        @DisplayName("pdf(+Infinity) = 0")
        void pdfInfinity() {
            assertThat(FDistribution.of(5, 10).pdf(Double.POSITIVE_INFINITY)).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("cdf - 累积分布函数")
    class CdfTest {

        @Test
        @DisplayName("F(5,10).cdf(3.326) 约等于 0.95")
        void cdfKnownValue() {
            assertThat(FDistribution.of(5, 10).cdf(3.326)).isCloseTo(0.95, within(0.01));
        }

        @Test
        @DisplayName("F(1,1).cdf(1) 约等于 0.5")
        void cdfF11() {
            assertThat(FDistribution.of(1, 1).cdf(1.0)).isCloseTo(0.5, within(0.01));
        }

        @Test
        @DisplayName("cdf(0) = 0")
        void cdfAtZero() {
            assertThat(FDistribution.of(5, 10).cdf(0)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("cdf 单调递增")
        void cdfMonotonic() {
            FDistribution f = FDistribution.of(5, 10);
            assertThat(f.cdf(1)).isLessThan(f.cdf(2));
            assertThat(f.cdf(2)).isLessThan(f.cdf(5));
        }

        @Test
        @DisplayName("cdf(+Infinity) = 1")
        void cdfInfinity() {
            assertThat(FDistribution.of(5, 10).cdf(Double.POSITIVE_INFINITY)).isEqualTo(1.0);
        }

        @Test
        @DisplayName("cdf(NaN) = NaN")
        void cdfNaN() {
            assertThat(FDistribution.of(5, 10).cdf(Double.NaN)).isNaN();
        }
    }

    @Nested
    @DisplayName("inverseCdf - 逆累积分布函数")
    class InverseCdfTest {

        @Test
        @DisplayName("F(5,10).inverseCdf(0.95) 约等于 3.326")
        void inverseCdfKnownValue() {
            assertThat(FDistribution.of(5, 10).inverseCdf(0.95))
                    .isCloseTo(3.326, within(0.05));
        }

        @Test
        @DisplayName("cdf(inverseCdf(p)) 约等于 p")
        void roundTrip() {
            FDistribution f = FDistribution.of(5, 10);
            double[] probs = {0.05, 0.1, 0.25, 0.5, 0.75, 0.9, 0.95};
            for (double p : probs) {
                double x = f.inverseCdf(p);
                assertThat(f.cdf(x)).as("roundTrip for p=%f", p).isCloseTo(p, within(1e-4));
            }
        }

        @Test
        @DisplayName("p 超出 (0,1) 范围抛出异常")
        void invalidP() {
            FDistribution f = FDistribution.of(5, 10);
            assertThatThrownBy(() -> f.inverseCdf(0)).isInstanceOf(MathException.class);
            assertThatThrownBy(() -> f.inverseCdf(1)).isInstanceOf(MathException.class);
        }
    }
}
