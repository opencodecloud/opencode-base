package cloud.opencode.base.math.distribution;

import cloud.opencode.base.math.exception.MathException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

/**
 * Tests for {@link BetaDistribution}.
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-math V1.0.3
 */
@DisplayName("BetaDistribution Beta 分布测试")
class BetaDistributionTest {

    private static final double TOLERANCE = 1e-4;

    @Nested
    @DisplayName("工厂方法与属性")
    class FactoryAndProperties {

        @Test
        @DisplayName("属性正确")
        void properties() {
            BetaDistribution b = BetaDistribution.of(2, 5);
            assertThat(b.alpha()).isEqualTo(2.0);
            assertThat(b.beta()).isEqualTo(5.0);
        }

        @Test
        @DisplayName("alpha <= 0 抛出异常")
        void invalidAlpha() {
            assertThatThrownBy(() -> BetaDistribution.of(0, 1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("beta <= 0 抛出异常")
        void invalidBeta() {
            assertThatThrownBy(() -> BetaDistribution.of(1, 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("NaN 参数抛出异常")
        void nanParams() {
            assertThatThrownBy(() -> BetaDistribution.of(Double.NaN, 1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("toString 包含参数")
        void toStringTest() {
            assertThat(BetaDistribution.of(2, 5).toString()).contains("2").contains("5");
        }
    }

    @Nested
    @DisplayName("mean 和 variance")
    class MeanAndVariance {

        @Test
        @DisplayName("Beta(2,5).mean() = 2/7")
        void mean() {
            assertThat(BetaDistribution.of(2, 5).mean()).isCloseTo(2.0 / 7.0, within(1e-10));
        }

        @Test
        @DisplayName("Beta(1,1).mean() = 0.5 (均匀分布)")
        void uniformMean() {
            assertThat(BetaDistribution.of(1, 1).mean()).isCloseTo(0.5, within(1e-10));
        }

        @Test
        @DisplayName("variance 正确")
        void variance() {
            // Beta(2,5): var = 2*5 / (7^2 * 8) = 10 / 392
            assertThat(BetaDistribution.of(2, 5).variance())
                    .isCloseTo(10.0 / 392.0, within(1e-10));
        }
    }

    @Nested
    @DisplayName("pdf - 概率密度函数")
    class PdfTest {

        @Test
        @DisplayName("x 超出 [0,1] 返回 0")
        void pdfOutOfRange() {
            BetaDistribution b = BetaDistribution.of(2, 5);
            assertThat(b.pdf(-0.1)).isEqualTo(0.0);
            assertThat(b.pdf(1.1)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Beta(1,1) = Uniform(0,1), pdf(0.5) = 1")
        void uniformPdf() {
            assertThat(BetaDistribution.of(1, 1).pdf(0.5)).isCloseTo(1.0, within(1e-6));
        }

        @Test
        @DisplayName("pdf 为正值")
        void pdfPositive() {
            assertThat(BetaDistribution.of(2, 5).pdf(0.3)).isGreaterThan(0.0);
        }

        @Test
        @DisplayName("pdf(NaN) = NaN")
        void pdfNaN() {
            assertThat(BetaDistribution.of(2, 5).pdf(Double.NaN)).isNaN();
        }
    }

    @Nested
    @DisplayName("cdf - 累积分布函数")
    class CdfTest {

        @Test
        @DisplayName("cdf(0) = 0")
        void cdfAtZero() {
            assertThat(BetaDistribution.of(2, 5).cdf(0)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("cdf(1) = 1")
        void cdfAtOne() {
            assertThat(BetaDistribution.of(2, 5).cdf(1)).isEqualTo(1.0);
        }

        @Test
        @DisplayName("Beta(1,1).cdf(0.5) = 0.5 (均匀分布)")
        void uniformCdf() {
            assertThat(BetaDistribution.of(1, 1).cdf(0.5)).isCloseTo(0.5, within(1e-6));
        }

        @Test
        @DisplayName("cdf 单调递增")
        void cdfMonotonic() {
            BetaDistribution b = BetaDistribution.of(2, 5);
            assertThat(b.cdf(0.1)).isLessThan(b.cdf(0.3));
            assertThat(b.cdf(0.3)).isLessThan(b.cdf(0.5));
        }

        @Test
        @DisplayName("Beta(2,5).cdf(0.5) 已知值")
        void cdfKnownValue() {
            // From statistical tables: Beta(2,5).cdf(0.5) ≈ 0.8906
            assertThat(BetaDistribution.of(2, 5).cdf(0.5)).isCloseTo(0.8906, within(0.01));
        }

        @Test
        @DisplayName("cdf(NaN) = NaN")
        void cdfNaN() {
            assertThat(BetaDistribution.of(2, 5).cdf(Double.NaN)).isNaN();
        }
    }

    @Nested
    @DisplayName("inverseCdf - 逆累积分布函数")
    class InverseCdfTest {

        @Test
        @DisplayName("cdf(inverseCdf(p)) 约等于 p")
        void roundTrip() {
            BetaDistribution b = BetaDistribution.of(2, 5);
            double[] probs = {0.05, 0.1, 0.25, 0.5, 0.75, 0.9, 0.95};
            for (double p : probs) {
                double x = b.inverseCdf(p);
                assertThat(b.cdf(x)).as("roundTrip for p=%f", p).isCloseTo(p, within(1e-4));
            }
        }

        @Test
        @DisplayName("Beta(1,1).inverseCdf(0.5) = 0.5 (均匀分布)")
        void uniformInverseCdf() {
            assertThat(BetaDistribution.of(1, 1).inverseCdf(0.5))
                    .isCloseTo(0.5, within(1e-6));
        }

        @Test
        @DisplayName("p 超出 (0,1) 范围抛出异常")
        void invalidP() {
            BetaDistribution b = BetaDistribution.of(2, 5);
            assertThatThrownBy(() -> b.inverseCdf(0)).isInstanceOf(MathException.class);
            assertThatThrownBy(() -> b.inverseCdf(1)).isInstanceOf(MathException.class);
        }
    }
}
