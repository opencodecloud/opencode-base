package cloud.opencode.base.math.distribution;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

/**
 * Tests for {@link BinomialDistribution}.
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-math V1.0.3
 */
@DisplayName("BinomialDistribution 二项分布测试")
class BinomialDistributionTest {

    private static final double TOLERANCE = 1e-4;

    @Nested
    @DisplayName("工厂方法与属性")
    class FactoryAndProperties {

        @Test
        @DisplayName("属性正确")
        void properties() {
            BinomialDistribution b = BinomialDistribution.of(10, 0.5);
            assertThat(b.trials()).isEqualTo(10);
            assertThat(b.probability()).isEqualTo(0.5);
        }

        @Test
        @DisplayName("n < 0 抛出异常")
        void invalidN() {
            assertThatThrownBy(() -> BinomialDistribution.of(-1, 0.5))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("p 超出 [0,1] 范围抛出异常")
        void invalidP() {
            assertThatThrownBy(() -> BinomialDistribution.of(10, -0.1))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> BinomialDistribution.of(10, 1.1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("NaN 概率抛出异常")
        void nanP() {
            assertThatThrownBy(() -> BinomialDistribution.of(10, Double.NaN))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("toString 包含参数")
        void toStringTest() {
            assertThat(BinomialDistribution.of(10, 0.5).toString()).contains("10").contains("0.5");
        }
    }

    @Nested
    @DisplayName("mean 和 variance")
    class MeanAndVariance {

        @Test
        @DisplayName("mean = n * p")
        void mean() {
            assertThat(BinomialDistribution.of(10, 0.3).mean()).isCloseTo(3.0, within(1e-10));
        }

        @Test
        @DisplayName("variance = n * p * (1-p)")
        void variance() {
            assertThat(BinomialDistribution.of(10, 0.3).variance()).isCloseTo(2.1, within(1e-10));
        }
    }

    @Nested
    @DisplayName("pmf - 概率质量函数")
    class PmfTest {

        @Test
        @DisplayName("Binom(10, 0.5).pmf(5) 约等于 0.2461")
        void pmfKnownValue() {
            assertThat(BinomialDistribution.of(10, 0.5).pmf(5))
                    .isCloseTo(0.2461, within(TOLERANCE));
        }

        @Test
        @DisplayName("Binom(1, 0.5).pmf(0) = 0.5")
        void bernoulli() {
            assertThat(BinomialDistribution.of(1, 0.5).pmf(0)).isCloseTo(0.5, within(1e-10));
            assertThat(BinomialDistribution.of(1, 0.5).pmf(1)).isCloseTo(0.5, within(1e-10));
        }

        @Test
        @DisplayName("k < 0 或 k > n 返回 0")
        void pmfOutOfRange() {
            BinomialDistribution b = BinomialDistribution.of(10, 0.5);
            assertThat(b.pmf(-1)).isEqualTo(0.0);
            assertThat(b.pmf(11)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("所有 PMF 之和为 1")
        void pmfSumToOne() {
            BinomialDistribution b = BinomialDistribution.of(10, 0.3);
            double sum = 0;
            for (int k = 0; k <= 10; k++) {
                sum += b.pmf(k);
            }
            assertThat(sum).isCloseTo(1.0, within(1e-10));
        }

        @Test
        @DisplayName("p=0 时仅 pmf(0)=1")
        void pZero() {
            BinomialDistribution b = BinomialDistribution.of(5, 0.0);
            assertThat(b.pmf(0)).isEqualTo(1.0);
            assertThat(b.pmf(1)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("p=1 时仅 pmf(n)=1")
        void pOne() {
            BinomialDistribution b = BinomialDistribution.of(5, 1.0);
            assertThat(b.pmf(5)).isEqualTo(1.0);
            assertThat(b.pmf(4)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("n=0 时 pmf(0)=1")
        void nZero() {
            BinomialDistribution b = BinomialDistribution.of(0, 0.5);
            assertThat(b.pmf(0)).isEqualTo(1.0);
        }
    }

    @Nested
    @DisplayName("cdf - 累积分布函数")
    class CdfTest {

        @Test
        @DisplayName("cdf(n) = 1")
        void cdfAtN() {
            assertThat(BinomialDistribution.of(10, 0.5).cdf(10)).isEqualTo(1.0);
        }

        @Test
        @DisplayName("cdf(-1) = 0")
        void cdfNegative() {
            assertThat(BinomialDistribution.of(10, 0.5).cdf(-1)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Binom(10, 0.5).cdf(5) 约等于 0.6230")
        void cdfKnownValue() {
            assertThat(BinomialDistribution.of(10, 0.5).cdf(5))
                    .isCloseTo(0.6230, within(0.01));
        }

        @Test
        @DisplayName("cdf 单调不减")
        void cdfMonotonic() {
            BinomialDistribution b = BinomialDistribution.of(10, 0.5);
            double prev = 0;
            for (int k = 0; k <= 10; k++) {
                double current = b.cdf(k);
                assertThat(current).isGreaterThanOrEqualTo(prev);
                prev = current;
            }
        }

        @Test
        @DisplayName("p=0 时 cdf(k)=1 对所有 k>=0")
        void pZeroCdf() {
            BinomialDistribution b = BinomialDistribution.of(5, 0.0);
            assertThat(b.cdf(0)).isEqualTo(1.0);
            assertThat(b.cdf(3)).isEqualTo(1.0);
        }
    }
}
