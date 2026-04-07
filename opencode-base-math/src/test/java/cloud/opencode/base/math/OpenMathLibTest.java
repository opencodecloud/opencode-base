package cloud.opencode.base.math;

import cloud.opencode.base.math.linalg.Matrix;
import cloud.opencode.base.math.linalg.Vector;
import cloud.opencode.base.math.stats.Regression;
import cloud.opencode.base.math.stats.StreamingStatistics;
import cloud.opencode.base.math.stats.inference.TestResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Tests for the OpenMathLib facade class.
 * OpenMathLib 门面类测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-math V1.0.3
 */
@DisplayName("OpenMathLib 门面类测试")
class OpenMathLibTest {

    @Nested
    @DisplayName("统计方法")
    class StatisticsTests {

        @Test
        @DisplayName("百分位数")
        void percentile() {
            double[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
            assertThat(OpenMathLib.percentile(data, 50)).isCloseTo(5.5, within(0.01));
        }

        @Test
        @DisplayName("Pearson 相关")
        void correlation() {
            double[] x = {1, 2, 3, 4, 5};
            double[] y = {2, 4, 6, 8, 10};
            assertThat(OpenMathLib.correlation(x, y)).isCloseTo(1.0, within(1e-10));
        }

        @Test
        @DisplayName("Spearman 相关")
        void spearmanCorrelation() {
            double[] x = {1, 2, 3, 4, 5};
            double[] y = {2, 4, 6, 8, 10};
            assertThat(OpenMathLib.spearmanCorrelation(x, y)).isCloseTo(1.0, within(1e-10));
        }

        @Test
        @DisplayName("线性回归")
        void linearRegression() {
            double[] x = {1, 2, 3, 4, 5};
            double[] y = {2, 4, 6, 8, 10};
            Regression.LinearModel model = OpenMathLib.linearRegression(x, y);
            assertThat(model.slope()).isCloseTo(2.0, within(1e-10));
            assertThat(model.intercept()).isCloseTo(0.0, within(1e-10));
        }

        @Test
        @DisplayName("流式统计")
        void streamingStats() {
            StreamingStatistics ss = OpenMathLib.streamingStats();
            for (int i = 1; i <= 100; i++) {
                ss.add(i);
            }
            assertThat(ss.count()).isEqualTo(100);
            assertThat(ss.mean()).isCloseTo(50.5, within(1e-10));
        }
    }

    @Nested
    @DisplayName("假设检验")
    class InferenceTests {

        @Test
        @DisplayName("单样本 t 检验")
        void tTestOneSample() {
            double[] data = {5, 6, 7, 8, 9};
            TestResult result = OpenMathLib.tTestOneSample(data, 5.0);
            assertThat(result.statistic()).isGreaterThan(0);
            assertThat(result.pValue()).isBetween(0.0, 1.0);
        }

        @Test
        @DisplayName("双样本 t 检验")
        void tTestTwoSample() {
            double[] x = {1, 2, 3, 4, 5};
            double[] y = {10, 11, 12, 13, 14};
            TestResult result = OpenMathLib.tTestTwoSample(x, y);
            assertThat(result.isSignificant()).isTrue();
        }
    }

    @Nested
    @DisplayName("线性代数")
    class LinalgTests {

        @Test
        @DisplayName("向量创建")
        void vector() {
            Vector v = OpenMathLib.vector(1, 2, 3);
            assertThat(v.dimension()).isEqualTo(3);
            assertThat(v.magnitude()).isCloseTo(Math.sqrt(14), within(1e-10));
        }

        @Test
        @DisplayName("矩阵创建和逆")
        void matrix() {
            Matrix m = OpenMathLib.matrix(new double[][]{{1, 2}, {3, 4}});
            Matrix inv = m.inverse();
            Matrix product = m.multiply(inv);
            Matrix identity = OpenMathLib.identityMatrix(2);
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 2; j++) {
                    assertThat(product.get(i, j)).isCloseTo(identity.get(i, j), within(1e-10));
                }
            }
        }
    }

    @Nested
    @DisplayName("数值分析")
    class AnalysisTests {

        @Test
        @DisplayName("求根")
        void findRoot() {
            double sqrt2 = OpenMathLib.findRoot(x -> x * x - 2, 0, 2);
            assertThat(sqrt2).isCloseTo(Math.sqrt(2), within(1e-10));
        }

        @Test
        @DisplayName("数值导数")
        void derivative() {
            double d = OpenMathLib.derivative(Math::sin, Math.PI / 4);
            assertThat(d).isCloseTo(Math.cos(Math.PI / 4), within(1e-6));
        }

        @Test
        @DisplayName("数值积分")
        void integrate() {
            double result = OpenMathLib.integrate(Math::sin, 0, Math.PI);
            assertThat(result).isCloseTo(2.0, within(1e-10));
        }

        @Test
        @DisplayName("线性插值")
        void interpolate() {
            double[] x = {0, 1, 2, 3};
            double[] y = {0, 1, 4, 9};
            assertThat(OpenMathLib.interpolate(x, y, 1.5)).isCloseTo(2.5, within(1e-10));
        }
    }

    @Nested
    @DisplayName("组合数学")
    class CombinatoricsTests {

        @Test
        @DisplayName("二项式系数")
        void binomial() {
            assertThat(OpenMathLib.binomial(10, 5)).isEqualTo(252);
            assertThat(OpenMathLib.binomial(20, 10)).isEqualTo(184756);
        }

        @Test
        @DisplayName("排列")
        void permutation() {
            assertThat(OpenMathLib.permutation(5, 3)).isEqualTo(60);
        }
    }

    @Nested
    @DisplayName("概率分布")
    class DistributionTests {

        @Test
        @DisplayName("标准正态")
        void standardNormal() {
            assertThat(OpenMathLib.standardNormal().cdf(0)).isCloseTo(0.5, within(1e-10));
            assertThat(OpenMathLib.standardNormal().cdf(1.96)).isCloseTo(0.975, within(1e-3));
        }
    }

    @Nested
    @DisplayName("特殊函数")
    class SpecialFunctionsTests {

        @Test
        @DisplayName("Gamma 函数")
        void gamma() {
            assertThat(OpenMathLib.gamma(5.0)).isCloseTo(24.0, within(1e-10));
        }

        @Test
        @DisplayName("误差函数")
        void erf() {
            assertThat(OpenMathLib.erf(0.0)).isCloseTo(0.0, within(1e-10));
        }

        @Test
        @DisplayName("Beta 函数")
        void beta() {
            assertThat(OpenMathLib.beta(2.0, 3.0)).isCloseTo(1.0 / 12.0, within(1e-10));
        }
    }
}
