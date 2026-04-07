package cloud.opencode.base.math.interpolation;

import cloud.opencode.base.math.exception.MathException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link Interpolation}.
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-math V1.0.3
 */
@DisplayName("Interpolation 插值工具类测试")
class InterpolationTest {

    @Nested
    @DisplayName("linear - 分段线性插值")
    class LinearTest {

        @Test
        @DisplayName("对线性数据精确插值")
        void exactForLinearData() {
            double[] x = {0, 1, 2, 3, 4};
            double[] y = {0, 2, 4, 6, 8}; // y = 2x
            assertThat(Interpolation.linear(x, y, 0.5)).isCloseTo(1.0, within(1e-15));
            assertThat(Interpolation.linear(x, y, 2.5)).isCloseTo(5.0, within(1e-15));
            assertThat(Interpolation.linear(x, y, 3.7)).isCloseTo(7.4, within(1e-14));
        }

        @Test
        @DisplayName("在节点处精确返回")
        void exactAtKnots() {
            double[] x = {1, 2, 3};
            double[] y = {10, 20, 30};
            assertThat(Interpolation.linear(x, y, 1.0)).isEqualTo(10.0);
            assertThat(Interpolation.linear(x, y, 2.0)).isEqualTo(20.0);
            assertThat(Interpolation.linear(x, y, 3.0)).isEqualTo(30.0);
        }

        @Test
        @DisplayName("两个点的线性插值")
        void twoPoints() {
            double[] x = {0, 10};
            double[] y = {0, 100};
            assertThat(Interpolation.linear(x, y, 5.0)).isCloseTo(50.0, within(1e-15));
        }

        @Test
        @DisplayName("xi 超出范围抛出异常")
        void outOfRangeThrows() {
            double[] x = {1, 2, 3};
            double[] y = {1, 4, 9};
            assertThatThrownBy(() -> Interpolation.linear(x, y, 0.5))
                    .isInstanceOf(MathException.class)
                    .hasMessageContaining("out of range");
            assertThatThrownBy(() -> Interpolation.linear(x, y, 3.5))
                    .isInstanceOf(MathException.class)
                    .hasMessageContaining("out of range");
        }

        @Test
        @DisplayName("x 未排序抛出异常")
        void unsortedThrows() {
            double[] x = {3, 1, 2};
            double[] y = {9, 1, 4};
            assertThatThrownBy(() -> Interpolation.linear(x, y, 1.5))
                    .isInstanceOf(MathException.class)
                    .hasMessageContaining("ascending");
        }

        @Test
        @DisplayName("在端点处精确返回")
        void atEndpoints() {
            double[] x = {0, 1, 2};
            double[] y = {5, 10, 15};
            assertThat(Interpolation.linear(x, y, 0.0)).isEqualTo(5.0);
            assertThat(Interpolation.linear(x, y, 2.0)).isEqualTo(15.0);
        }
    }

    @Nested
    @DisplayName("lagrange - 拉格朗日多项式插值")
    class LagrangeTest {

        @Test
        @DisplayName("对线性数据精确")
        void exactForLinearData() {
            double[] x = {0, 1, 2};
            double[] y = {1, 3, 5}; // y = 2x + 1
            assertThat(Interpolation.lagrange(x, y, 0.5)).isCloseTo(2.0, within(1e-14));
            assertThat(Interpolation.lagrange(x, y, 1.5)).isCloseTo(4.0, within(1e-14));
        }

        @Test
        @DisplayName("对二次多项式精确（3 个点）")
        void exactForQuadratic() {
            // y = x^2, points at 0, 1, 2
            double[] x = {0, 1, 2};
            double[] y = {0, 1, 4};
            assertThat(Interpolation.lagrange(x, y, 0.5)).isCloseTo(0.25, within(1e-14));
            assertThat(Interpolation.lagrange(x, y, 1.5)).isCloseTo(2.25, within(1e-14));
        }

        @Test
        @DisplayName("对三次多项式精确（4 个点）")
        void exactForCubic() {
            // y = x^3, points at -1, 0, 1, 2
            double[] x = {-1, 0, 1, 2};
            double[] y = {-1, 0, 1, 8};
            assertThat(Interpolation.lagrange(x, y, 0.5)).isCloseTo(0.125, within(1e-13));
            assertThat(Interpolation.lagrange(x, y, 1.5)).isCloseTo(3.375, within(1e-13));
        }

        @Test
        @DisplayName("在节点处精确返回")
        void exactAtKnots() {
            double[] x = {1, 3, 5};
            double[] y = {2, 8, 18};
            for (int i = 0; i < x.length; i++) {
                assertThat(Interpolation.lagrange(x, y, x[i])).isCloseTo(y[i], within(1e-14));
            }
        }

        @Test
        @DisplayName("x 值重复抛出异常")
        void duplicateXThrows() {
            double[] x = {1, 2, 2};
            double[] y = {1, 4, 4};
            assertThatThrownBy(() -> Interpolation.lagrange(x, y, 1.5))
                    .isInstanceOf(MathException.class)
                    .hasMessageContaining("distinct");
        }

        @Test
        @DisplayName("允许无序的 x 值")
        void unsortedXAllowed() {
            // Lagrange doesn't require sorted x
            double[] x = {2, 0, 1};
            double[] y = {4, 0, 1}; // y = x^2
            assertThat(Interpolation.lagrange(x, y, 0.5)).isCloseTo(0.25, within(1e-14));
        }
    }

    @Nested
    @DisplayName("newtonDividedDifference - 牛顿差商插值")
    class NewtonTest {

        @Test
        @DisplayName("对二次多项式精确（3 个点）")
        void exactForQuadratic() {
            double[] x = {0, 1, 2};
            double[] y = {0, 1, 4}; // y = x^2
            assertThat(Interpolation.newtonDividedDifference(x, y, 0.5))
                    .isCloseTo(0.25, within(1e-14));
            assertThat(Interpolation.newtonDividedDifference(x, y, 1.5))
                    .isCloseTo(2.25, within(1e-14));
        }

        @Test
        @DisplayName("与拉格朗日结果一致")
        void consistentWithLagrange() {
            double[] x = {-1, 0, 1, 2, 3};
            double[] y = {1, 0, 1, 8, 27}; // random polynomial-like data

            for (double xi = -0.5; xi <= 2.5; xi += 0.5) {
                double lagrangeVal = Interpolation.lagrange(x, y, xi);
                double newtonVal = Interpolation.newtonDividedDifference(x, y, xi);
                assertThat(newtonVal).isCloseTo(lagrangeVal, within(1e-10));
            }
        }

        @Test
        @DisplayName("在节点处精确返回")
        void exactAtKnots() {
            double[] x = {1, 2, 4, 7};
            double[] y = {3, 5, 11, 23};
            for (int i = 0; i < x.length; i++) {
                assertThat(Interpolation.newtonDividedDifference(x, y, x[i]))
                        .isCloseTo(y[i], within(1e-12));
            }
        }

        @Test
        @DisplayName("不修改输入数组")
        void doesNotMutateInput() {
            double[] x = {0, 1, 2};
            double[] y = {0, 1, 4};
            double[] yCopy = y.clone();
            Interpolation.newtonDividedDifference(x, y, 0.5);
            assertThat(y).containsExactly(yCopy);
        }
    }

    @Nested
    @DisplayName("cubicSpline - 自然三次样条插值")
    class CubicSplineTest {

        @Test
        @DisplayName("在节点处精确返回")
        void exactAtKnots() {
            double[] x = {0, 1, 2, 3, 4};
            double[] y = {0, 1, 4, 9, 16};
            for (int i = 0; i < x.length; i++) {
                assertThat(Interpolation.cubicSpline(x, y, x[i]))
                        .isCloseTo(y[i], within(1e-12));
            }
        }

        @Test
        @DisplayName("对线性数据精确")
        void exactForLinearData() {
            double[] x = {0, 1, 2, 3};
            double[] y = {0, 2, 4, 6}; // y = 2x
            assertThat(Interpolation.cubicSpline(x, y, 0.5)).isCloseTo(1.0, within(1e-14));
            assertThat(Interpolation.cubicSpline(x, y, 1.5)).isCloseTo(3.0, within(1e-14));
            assertThat(Interpolation.cubicSpline(x, y, 2.5)).isCloseTo(5.0, within(1e-14));
        }

        @Test
        @DisplayName("平滑插值接近真实函数")
        void smoothApproximation() {
            // Sample sin(x) at several points
            double[] x = {0, Math.PI / 6, Math.PI / 3, Math.PI / 2, 2 * Math.PI / 3, 5 * Math.PI / 6, Math.PI};
            double[] y = new double[x.length];
            for (int i = 0; i < x.length; i++) {
                y[i] = Math.sin(x[i]);
            }

            // Test at midpoints — should be close to sin(xi)
            double xi = Math.PI / 4;
            double expected = Math.sin(xi);
            double actual = Interpolation.cubicSpline(x, y, xi);
            assertThat(actual).isCloseTo(expected, within(0.01));
        }

        @Test
        @DisplayName("两个点退化为线性插值")
        void twoPointsFallsBackToLinear() {
            double[] x = {0, 1};
            double[] y = {0, 1};
            assertThat(Interpolation.cubicSpline(x, y, 0.5)).isCloseTo(0.5, within(1e-15));
        }

        @Test
        @DisplayName("在端点处精确返回")
        void atEndpoints() {
            double[] x = {0, 1, 2, 3};
            double[] y = {5, 7, 3, 10};
            assertThat(Interpolation.cubicSpline(x, y, 0.0)).isCloseTo(5.0, within(1e-14));
            assertThat(Interpolation.cubicSpline(x, y, 3.0)).isCloseTo(10.0, within(1e-14));
        }

        @Test
        @DisplayName("xi 超出范围抛出异常")
        void outOfRangeThrows() {
            double[] x = {1, 2, 3};
            double[] y = {1, 4, 9};
            assertThatThrownBy(() -> Interpolation.cubicSpline(x, y, 0.5))
                    .isInstanceOf(MathException.class);
        }
    }

    @Nested
    @DisplayName("输入验证")
    class ValidationTest {

        @Test
        @DisplayName("null 数组抛出异常")
        void nullArrays() {
            assertThatThrownBy(() -> Interpolation.linear(null, new double[]{1}, 0))
                    .isInstanceOf(MathException.class)
                    .hasMessageContaining("null");
            assertThatThrownBy(() -> Interpolation.linear(new double[]{1}, null, 0))
                    .isInstanceOf(MathException.class)
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("长度不匹配抛出异常")
        void lengthMismatch() {
            assertThatThrownBy(() -> Interpolation.lagrange(new double[]{1, 2}, new double[]{1}, 1))
                    .isInstanceOf(MathException.class)
                    .hasMessageContaining("same length");
        }

        @Test
        @DisplayName("少于 2 个点抛出异常")
        void tooFewPoints() {
            assertThatThrownBy(() -> Interpolation.linear(new double[]{1}, new double[]{2}, 1))
                    .isInstanceOf(MathException.class)
                    .hasMessageContaining("2");
        }

        @Test
        @DisplayName("NaN 或 Infinity 输入抛出异常")
        void nanOrInfinityInput() {
            assertThatThrownBy(() -> Interpolation.linear(
                    new double[]{Double.NaN, 1}, new double[]{1, 2}, 0.5))
                    .isInstanceOf(MathException.class)
                    .hasMessageContaining("not finite");
            assertThatThrownBy(() -> Interpolation.lagrange(
                    new double[]{0, 1}, new double[]{Double.POSITIVE_INFINITY, 2}, 0.5))
                    .isInstanceOf(MathException.class)
                    .hasMessageContaining("not finite");
        }
    }
}
