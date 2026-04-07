package cloud.opencode.base.math.stats.inference;

import cloud.opencode.base.math.exception.MathException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

/**
 * Comprehensive tests for {@link TTest}.
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-math V1.0.3
 */
@DisplayName("TTest t 检验测试")
class TTestTest {

    @Nested
    @DisplayName("oneSample 单样本 t 检验")
    class OneSampleTests {

        @Test
        @DisplayName("已知数据集：data=[5,6,7,8,9], mu0=5 → t≈2.828, p≈0.047")
        void knownDataSet() {
            // mean=7, s=sqrt(2.5)≈1.5811, n=5, t=(7-5)/(1.5811/sqrt(5))≈2.8284, df=4
            double[] data = {5, 6, 7, 8, 9};
            TestResult result = TTest.oneSample(data, 5.0);

            assertThat(result.testName()).isEqualTo("One-Sample T-Test");
            assertThat(result.statistic()).isCloseTo(2.8284271, within(0.001));
            assertThat(result.pValue()).isCloseTo(0.0474, within(0.005));
            assertThat(result.degreesOfFreedom()).isCloseTo(4.0, within(1e-10));
            assertThat(result.isSignificant(0.05)).isTrue();
        }

        @Test
        @DisplayName("均值等于假设值时 p 值应接近 1")
        void meanEqualsHypothesizedValue() {
            double[] data = {3, 5, 7};
            // mean = 5, mu0 = 5 → t = 0
            TestResult result = TTest.oneSample(data, 5.0);

            assertThat(result.statistic()).isCloseTo(0.0, within(1e-10));
            assertThat(result.pValue()).isCloseTo(1.0, within(1e-10));
        }

        @Test
        @DisplayName("所有值相同且等于 mu0 时 t=0, p=1")
        void allValuesSameEqualMu0() {
            double[] data = {5, 5, 5, 5};
            TestResult result = TTest.oneSample(data, 5.0);

            assertThat(result.statistic()).isEqualTo(0.0);
            assertThat(result.pValue()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("所有值相同但不等于 mu0 时 p=0")
        void allValuesSameNotEqualMu0() {
            double[] data = {5, 5, 5, 5};
            TestResult result = TTest.oneSample(data, 3.0);

            assertThat(result.pValue()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("大样本显著检验")
        void largeSampleSignificant() {
            // 100 values with mean around 1.0, testing against mu0=0
            double[] data = new double[100];
            for (int i = 0; i < 100; i++) {
                data[i] = 1.0 + (i % 3 - 1) * 0.1; // values around 1.0
            }
            TestResult result = TTest.oneSample(data, 0.0);

            assertThat(result.isSignificant()).isTrue();
        }

        @Test
        @DisplayName("null 数组抛出 MathException")
        void nullArrayThrows() {
            assertThatThrownBy(() -> TTest.oneSample(null, 0.0))
                    .isInstanceOf(MathException.class);
        }

        @Test
        @DisplayName("单元素数组抛出 MathException")
        void singleElementThrows() {
            assertThatThrownBy(() -> TTest.oneSample(new double[]{1.0}, 0.0))
                    .isInstanceOf(MathException.class);
        }

        @Test
        @DisplayName("包含 NaN 的数组抛出 MathException")
        void nanInArrayThrows() {
            assertThatThrownBy(() -> TTest.oneSample(new double[]{1.0, Double.NaN}, 0.0))
                    .isInstanceOf(MathException.class);
        }

        @Test
        @DisplayName("包含 Infinity 的数组抛出 MathException")
        void infinityInArrayThrows() {
            assertThatThrownBy(() -> TTest.oneSample(new double[]{1.0, Double.POSITIVE_INFINITY}, 0.0))
                    .isInstanceOf(MathException.class);
        }

        @Test
        @DisplayName("mu0 为 NaN 时抛出 MathException")
        void nanMu0Throws() {
            assertThatThrownBy(() -> TTest.oneSample(new double[]{1, 2, 3}, Double.NaN))
                    .isInstanceOf(MathException.class);
        }
    }

    @Nested
    @DisplayName("twoSample Welch 双样本 t 检验")
    class TwoSampleTests {

        @Test
        @DisplayName("两组均值明显不同时应显著")
        void differentMeansSignificant() {
            double[] x = {10, 12, 14, 16, 18};
            double[] y = {1, 2, 3, 4, 5};
            TestResult result = TTest.twoSample(x, y);

            assertThat(result.testName()).isEqualTo("Two-Sample T-Test (Welch)");
            assertThat(result.isSignificant()).isTrue();
            assertThat(result.statistic()).isGreaterThan(0);
            assertThat(result.pValue()).isLessThan(0.01);
        }

        @Test
        @DisplayName("两组相同数据时不显著")
        void sameMeansNotSignificant() {
            double[] x = {5, 6, 7, 8, 9};
            double[] y = {5, 6, 7, 8, 9};
            TestResult result = TTest.twoSample(x, y);

            assertThat(result.statistic()).isCloseTo(0.0, within(1e-10));
            assertThat(result.pValue()).isCloseTo(1.0, within(1e-10));
        }

        @Test
        @DisplayName("不等长数组的 Welch 检验")
        void unequalLengthArrays() {
            double[] x = {10, 12, 14};
            double[] y = {1, 2, 3, 4, 5, 6};
            TestResult result = TTest.twoSample(x, y);

            assertThat(result.isSignificant()).isTrue();
            // Welch-Satterthwaite df should be a non-integer
            assertThat(result.degreesOfFreedom()).isGreaterThan(0);
        }

        @Test
        @DisplayName("已知 Welch t 检验值验证")
        void knownWelchValues() {
            // Two groups: x = {1,2,3,4,5}, y = {4,5,6,7,8}
            // meanX=3, meanY=6, varX=2.5, varY=2.5
            // t = (3-6)/sqrt(2.5/5+2.5/5) = -3/1 = -3
            // df via Welch: (0.5+0.5)^2 / (0.25/4+0.25/4) = 1/0.125 = 8
            double[] x = {1, 2, 3, 4, 5};
            double[] y = {4, 5, 6, 7, 8};
            TestResult result = TTest.twoSample(x, y);

            assertThat(result.statistic()).isCloseTo(-3.0, within(1e-10));
            assertThat(result.degreesOfFreedom()).isCloseTo(8.0, within(1e-10));
        }

        @Test
        @DisplayName("null 数组抛出 MathException")
        void nullArrayThrows() {
            assertThatThrownBy(() -> TTest.twoSample(null, new double[]{1, 2}))
                    .isInstanceOf(MathException.class);
            assertThatThrownBy(() -> TTest.twoSample(new double[]{1, 2}, null))
                    .isInstanceOf(MathException.class);
        }

        @Test
        @DisplayName("单元素数组抛出 MathException")
        void singleElementThrows() {
            assertThatThrownBy(() -> TTest.twoSample(new double[]{1}, new double[]{1, 2}))
                    .isInstanceOf(MathException.class);
        }
    }

    @Nested
    @DisplayName("paired 配对 t 检验")
    class PairedTests {

        @Test
        @DisplayName("配对数据有显著差异")
        void significantPairedDifference() {
            double[] before = {200, 190, 210, 180, 205};
            double[] after = {150, 160, 170, 140, 155};
            TestResult result = TTest.paired(before, after);

            assertThat(result.testName()).isEqualTo("Paired T-Test");
            assertThat(result.isSignificant()).isTrue();
            assertThat(result.degreesOfFreedom()).isCloseTo(4.0, within(1e-10));
        }

        @Test
        @DisplayName("配对数据无差异时不显著")
        void noDifferenceNotSignificant() {
            double[] x = {1, 2, 3, 4, 5};
            double[] y = {1, 2, 3, 4, 5};
            TestResult result = TTest.paired(x, y);

            assertThat(result.statistic()).isEqualTo(0.0);
            assertThat(result.pValue()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("不等长数组抛出 MathException")
        void unequalLengthThrows() {
            assertThatThrownBy(() -> TTest.paired(new double[]{1, 2, 3}, new double[]{1, 2}))
                    .isInstanceOf(MathException.class)
                    .hasMessageContaining("equal-length");
        }

        @Test
        @DisplayName("null 数组抛出 MathException")
        void nullArrayThrows() {
            assertThatThrownBy(() -> TTest.paired(null, new double[]{1, 2}))
                    .isInstanceOf(MathException.class);
        }
    }

    @Nested
    @DisplayName("tCdf 内部 t 分布 CDF")
    class TCdfTests {

        @Test
        @DisplayName("t=0 时 CDF=0.5")
        void tZeroReturnHalf() {
            assertThat(TTest.tCdf(0.0, 10)).isCloseTo(0.5, within(1e-10));
        }

        @Test
        @DisplayName("大自由度下接近标准正态")
        void largeDfApproachesNormal() {
            // For very large df, t-distribution approaches normal
            // CDF(1.96, inf) ≈ 0.975
            double cdf = TTest.tCdf(1.96, 100000);
            assertThat(cdf).isCloseTo(0.975, within(0.001));
        }
    }
}
