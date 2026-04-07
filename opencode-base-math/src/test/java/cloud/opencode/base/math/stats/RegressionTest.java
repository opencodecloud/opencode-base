package cloud.opencode.base.math.stats;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

/**
 * Comprehensive tests for {@link Regression}.
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-math V1.0.3
 */
@DisplayName("Regression 回归分析测试")
class RegressionTest {

    // ==================== Linear Regression Tests ====================

    @Nested
    @DisplayName("linear 线性回归")
    class LinearTests {

        @Test
        @DisplayName("完美线性关系 y = 2x + 1")
        void perfectLinear() {
            double[] x = {1, 2, 3, 4, 5};
            double[] y = {3, 5, 7, 9, 11};
            Regression.LinearModel model = Regression.linear(x, y);

            assertThat(model.slope()).isCloseTo(2.0, within(1e-10));
            assertThat(model.intercept()).isCloseTo(1.0, within(1e-10));
            assertThat(model.rSquared()).isCloseTo(1.0, within(1e-10));
        }

        @Test
        @DisplayName("完美线性关系 y = -3x + 10")
        void perfectLinearNegativeSlope() {
            double[] x = {0, 1, 2, 3, 4};
            double[] y = {10, 7, 4, 1, -2};
            Regression.LinearModel model = Regression.linear(x, y);

            assertThat(model.slope()).isCloseTo(-3.0, within(1e-10));
            assertThat(model.intercept()).isCloseTo(10.0, within(1e-10));
            assertThat(model.rSquared()).isCloseTo(1.0, within(1e-10));
        }

        @Test
        @DisplayName("水平线 y = 5")
        void horizontalLine() {
            double[] x = {1, 2, 3, 4, 5};
            double[] y = {5, 5, 5, 5, 5};
            Regression.LinearModel model = Regression.linear(x, y);

            assertThat(model.slope()).isCloseTo(0.0, within(1e-10));
            assertThat(model.intercept()).isCloseTo(5.0, within(1e-10));
            assertThat(model.rSquared()).isCloseTo(1.0, within(1e-10));
        }

        @Test
        @DisplayName("所有x值相同")
        void allSameX() {
            double[] x = {3, 3, 3, 3};
            double[] y = {1, 2, 3, 4};
            Regression.LinearModel model = Regression.linear(x, y);

            assertThat(model.slope()).isCloseTo(0.0, within(1e-10));
            assertThat(model.intercept()).isCloseTo(2.5, within(1e-10));
            assertThat(model.rSquared()).isCloseTo(0.0, within(1e-10));
        }

        @Test
        @DisplayName("带噪声数据的R^2")
        void scatteredData() {
            double[] x = {1, 2, 3, 4, 5};
            double[] y = {2.1, 3.9, 6.2, 7.8, 10.1};
            Regression.LinearModel model = Regression.linear(x, y);

            assertThat(model.slope()).isGreaterThan(0);
            assertThat(model.rSquared()).isBetween(0.0, 1.0);
            assertThat(model.rSquared()).isGreaterThan(0.95);
        }

        @Test
        @DisplayName("两个数据点")
        void twoPoints() {
            double[] x = {0, 10};
            double[] y = {0, 30};
            Regression.LinearModel model = Regression.linear(x, y);

            assertThat(model.slope()).isCloseTo(3.0, within(1e-10));
            assertThat(model.intercept()).isCloseTo(0.0, within(1e-10));
            assertThat(model.rSquared()).isCloseTo(1.0, within(1e-10));
        }
    }

    // ==================== Predict Tests ====================

    @Nested
    @DisplayName("predict 预测")
    class PredictTests {

        @Test
        @DisplayName("预测已知数据点")
        void predictKnownPoint() {
            double[] x = {1, 2, 3, 4, 5};
            double[] y = {3, 5, 7, 9, 11};
            Regression.LinearModel model = Regression.linear(x, y);

            assertThat(model.predict(3)).isCloseTo(7.0, within(1e-10));
        }

        @Test
        @DisplayName("外推预测")
        void extrapolate() {
            double[] x = {1, 2, 3, 4, 5};
            double[] y = {3, 5, 7, 9, 11};
            Regression.LinearModel model = Regression.linear(x, y);

            assertThat(model.predict(10)).isCloseTo(21.0, within(1e-10));
        }

        @Test
        @DisplayName("预测负数x")
        void predictNegativeX() {
            double[] x = {1, 2, 3, 4, 5};
            double[] y = {3, 5, 7, 9, 11};
            Regression.LinearModel model = Regression.linear(x, y);

            assertThat(model.predict(-1)).isCloseTo(-1.0, within(1e-10));
        }
    }

    // ==================== Residuals Tests ====================

    @Nested
    @DisplayName("residuals 残差")
    class ResidualsTests {

        @Test
        @DisplayName("完美拟合残差全部为0")
        void perfectFitResiduals() {
            double[] x = {1, 2, 3, 4, 5};
            double[] y = {3, 5, 7, 9, 11};
            Regression.LinearModel model = Regression.linear(x, y);
            double[] residuals = model.residuals(x, y);

            for (double r : residuals) {
                assertThat(r).isCloseTo(0.0, within(1e-10));
            }
        }

        @Test
        @DisplayName("残差之和接近0")
        void residualsSumToZero() {
            double[] x = {1, 2, 3, 4, 5};
            double[] y = {2.1, 3.9, 6.2, 7.8, 10.1};
            Regression.LinearModel model = Regression.linear(x, y);
            double[] residuals = model.residuals(x, y);

            double sum = 0;
            for (double r : residuals) {
                sum += r;
            }
            assertThat(sum).isCloseTo(0.0, within(1e-10));
        }

        @Test
        @DisplayName("残差数组长度等于输入长度")
        void residualsLength() {
            double[] x = {1, 2, 3};
            double[] y = {2, 4, 6};
            Regression.LinearModel model = Regression.linear(x, y);
            double[] residuals = model.residuals(x, y);

            assertThat(residuals).hasSize(3);
        }

        @Test
        @DisplayName("null x数组抛出异常")
        void nullX() {
            Regression.LinearModel model = new Regression.LinearModel(1, 0, 1);
            assertThatThrownBy(() -> model.residuals(null, new double[]{1}))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null y数组抛出异常")
        void nullY() {
            Regression.LinearModel model = new Regression.LinearModel(1, 0, 1);
            assertThatThrownBy(() -> model.residuals(new double[]{1}, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("数组长度不同抛出异常")
        void differentLengths() {
            Regression.LinearModel model = new Regression.LinearModel(1, 0, 1);
            assertThatThrownBy(() -> model.residuals(new double[]{1, 2}, new double[]{1}))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("空数组抛出异常")
        void emptyArrays() {
            Regression.LinearModel model = new Regression.LinearModel(1, 0, 1);
            assertThatThrownBy(() -> model.residuals(new double[]{}, new double[]{}))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ==================== Input Validation Tests ====================

    @Nested
    @DisplayName("输入验证")
    class InputValidationTests {

        @Test
        @DisplayName("null x数组抛出异常")
        void nullX() {
            assertThatThrownBy(() -> Regression.linear(null, new double[]{1, 2}))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null y数组抛出异常")
        void nullY() {
            assertThatThrownBy(() -> Regression.linear(new double[]{1, 2}, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("空数组抛出异常")
        void emptyArrays() {
            assertThatThrownBy(() -> Regression.linear(new double[]{}, new double[]{}))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("单个数据点抛出异常")
        void singlePoint() {
            assertThatThrownBy(() -> Regression.linear(new double[]{1}, new double[]{2}))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("数组长度不同抛出异常")
        void differentLengths() {
            assertThatThrownBy(() -> Regression.linear(new double[]{1, 2, 3}, new double[]{1, 2}))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ==================== Record Equality Tests ====================

    @Nested
    @DisplayName("LinearModel record 特性")
    class RecordTests {

        @Test
        @DisplayName("相同参数的record相等")
        void equalModels() {
            var m1 = new Regression.LinearModel(2.0, 1.0, 0.99);
            var m2 = new Regression.LinearModel(2.0, 1.0, 0.99);
            assertThat(m1).isEqualTo(m2);
        }

        @Test
        @DisplayName("不同参数的record不等")
        void differentModels() {
            var m1 = new Regression.LinearModel(2.0, 1.0, 0.99);
            var m2 = new Regression.LinearModel(3.0, 1.0, 0.99);
            assertThat(m1).isNotEqualTo(m2);
        }

        @Test
        @DisplayName("toString 包含所有字段")
        void toStringContainsFields() {
            var model = new Regression.LinearModel(2.0, 1.0, 0.99);
            String s = model.toString();
            assertThat(s).contains("2.0").contains("1.0").contains("0.99");
        }
    }
}
