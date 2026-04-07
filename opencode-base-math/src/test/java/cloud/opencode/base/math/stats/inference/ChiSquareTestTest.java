package cloud.opencode.base.math.stats.inference;

import cloud.opencode.base.math.exception.MathException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

/**
 * Comprehensive tests for {@link ChiSquareTest}.
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-math V1.0.3
 */
@DisplayName("ChiSquareTest 卡方检验测试")
class ChiSquareTestTest {

    @Nested
    @DisplayName("goodnessOfFit 拟合优度检验")
    class GoodnessOfFitTests {

        @Test
        @DisplayName("已知数据集：observed=[10,20,30], expected=[20,20,20] → chi²=10, p<0.01")
        void knownDataSet() {
            double[] observed = {10, 20, 30};
            double[] expected = {20, 20, 20};
            TestResult result = ChiSquareTest.goodnessOfFit(observed, expected);

            assertThat(result.testName()).isEqualTo("Chi-Squared Goodness-of-Fit");
            assertThat(result.statistic()).isCloseTo(10.0, within(1e-10));
            assertThat(result.degreesOfFreedom()).isCloseTo(2.0, within(1e-10));
            assertThat(result.pValue()).isLessThan(0.01);
            assertThat(result.isSignificant()).isTrue();
        }

        @Test
        @DisplayName("观察值等于期望值时 chi²=0, p=1")
        void perfectFit() {
            double[] observed = {10, 20, 30};
            double[] expected = {10, 20, 30};
            TestResult result = ChiSquareTest.goodnessOfFit(observed, expected);

            assertThat(result.statistic()).isCloseTo(0.0, within(1e-10));
            assertThat(result.pValue()).isCloseTo(1.0, within(1e-10));
            assertThat(result.isSignificant()).isFalse();
        }

        @Test
        @DisplayName("骰子公平性检验（6个面）")
        void fairDiceTest() {
            // Fair dice: observed roughly uniform
            double[] observed = {18, 16, 15, 17, 17, 17};
            double[] expected = {100.0 / 6, 100.0 / 6, 100.0 / 6, 100.0 / 6, 100.0 / 6, 100.0 / 6};
            TestResult result = ChiSquareTest.goodnessOfFit(observed, expected);

            assertThat(result.degreesOfFreedom()).isCloseTo(5.0, within(1e-10));
            assertThat(result.isSignificant()).isFalse();
        }

        @Test
        @DisplayName("不等长数组抛出 MathException")
        void unequalLengthThrows() {
            assertThatThrownBy(() -> ChiSquareTest.goodnessOfFit(new double[]{1, 2}, new double[]{1, 2, 3}))
                    .isInstanceOf(MathException.class)
                    .hasMessageContaining("same length");
        }

        @Test
        @DisplayName("单个类别抛出 MathException")
        void singleCategoryThrows() {
            assertThatThrownBy(() -> ChiSquareTest.goodnessOfFit(new double[]{10}, new double[]{10}))
                    .isInstanceOf(MathException.class)
                    .hasMessageContaining("at least 2");
        }

        @Test
        @DisplayName("负期望值抛出 MathException")
        void negativeExpectedThrows() {
            assertThatThrownBy(() -> ChiSquareTest.goodnessOfFit(new double[]{10, 20}, new double[]{-5, 20}))
                    .isInstanceOf(MathException.class);
        }

        @Test
        @DisplayName("零期望值抛出 MathException")
        void zeroExpectedThrows() {
            assertThatThrownBy(() -> ChiSquareTest.goodnessOfFit(new double[]{10, 20}, new double[]{0, 20}))
                    .isInstanceOf(MathException.class);
        }

        @Test
        @DisplayName("null 数组抛出 MathException")
        void nullArrayThrows() {
            assertThatThrownBy(() -> ChiSquareTest.goodnessOfFit(null, new double[]{1, 2}))
                    .isInstanceOf(MathException.class);
            assertThatThrownBy(() -> ChiSquareTest.goodnessOfFit(new double[]{1, 2}, null))
                    .isInstanceOf(MathException.class);
        }

        @Test
        @DisplayName("包含 NaN 的观察值抛出 MathException")
        void nanObservedThrows() {
            assertThatThrownBy(() -> ChiSquareTest.goodnessOfFit(
                    new double[]{Double.NaN, 20}, new double[]{10, 20}))
                    .isInstanceOf(MathException.class);
        }
    }

    @Nested
    @DisplayName("independence 独立性检验")
    class IndependenceTests {

        @Test
        @DisplayName("2x2 列联表 — 显著关联")
        void twoByTwoSignificant() {
            double[][] table = {
                    {90, 10},
                    {10, 90}
            };
            TestResult result = ChiSquareTest.independence(table);

            assertThat(result.testName()).isEqualTo("Chi-Squared Test of Independence");
            assertThat(result.degreesOfFreedom()).isCloseTo(1.0, within(1e-10));
            assertThat(result.isSignificant()).isTrue();
            assertThat(result.pValue()).isLessThan(0.001);
        }

        @Test
        @DisplayName("2x2 列联表 — 独立（无关联）")
        void twoByTwoIndependent() {
            // Perfectly independent: row/col proportions match
            double[][] table = {
                    {25, 25},
                    {25, 25}
            };
            TestResult result = ChiSquareTest.independence(table);

            assertThat(result.statistic()).isCloseTo(0.0, within(1e-10));
            assertThat(result.pValue()).isCloseTo(1.0, within(1e-10));
            assertThat(result.isSignificant()).isFalse();
        }

        @Test
        @DisplayName("3x3 列联表")
        void threeByThreeTable() {
            double[][] table = {
                    {10, 20, 30},
                    {20, 15, 25},
                    {30, 25, 15}
            };
            TestResult result = ChiSquareTest.independence(table);

            assertThat(result.degreesOfFreedom()).isCloseTo(4.0, within(1e-10));
            assertThat(result.statistic()).isGreaterThan(0);
        }

        @Test
        @DisplayName("null 列联表抛出 MathException")
        void nullTableThrows() {
            assertThatThrownBy(() -> ChiSquareTest.independence(null))
                    .isInstanceOf(MathException.class);
        }

        @Test
        @DisplayName("单行列联表抛出 MathException")
        void singleRowThrows() {
            assertThatThrownBy(() -> ChiSquareTest.independence(new double[][]{{10, 20}}))
                    .isInstanceOf(MathException.class)
                    .hasMessageContaining("at least 2 rows");
        }

        @Test
        @DisplayName("单列列联表抛出 MathException")
        void singleColumnThrows() {
            assertThatThrownBy(() -> ChiSquareTest.independence(new double[][]{{10}, {20}}))
                    .isInstanceOf(MathException.class)
                    .hasMessageContaining("at least 2 columns");
        }

        @Test
        @DisplayName("非矩形列联表抛出 MathException")
        void jaggedTableThrows() {
            assertThatThrownBy(() -> ChiSquareTest.independence(new double[][]{{10, 20}, {30}}))
                    .isInstanceOf(MathException.class)
                    .hasMessageContaining("rectangular");
        }

        @Test
        @DisplayName("负值列联表抛出 MathException")
        void negativeValueThrows() {
            assertThatThrownBy(() -> ChiSquareTest.independence(new double[][]{{10, -5}, {20, 30}}))
                    .isInstanceOf(MathException.class);
        }

        @Test
        @DisplayName("全零列联表抛出 MathException")
        void allZeroTableThrows() {
            assertThatThrownBy(() -> ChiSquareTest.independence(new double[][]{{0, 0}, {0, 0}}))
                    .isInstanceOf(MathException.class)
                    .hasMessageContaining("positive");
        }
    }
}
