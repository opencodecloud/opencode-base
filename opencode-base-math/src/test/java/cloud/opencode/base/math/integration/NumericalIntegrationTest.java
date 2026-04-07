package cloud.opencode.base.math.integration;

import cloud.opencode.base.math.exception.MathException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link NumericalIntegration}.
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-math V1.0.3
 */
@DisplayName("NumericalIntegration 数值积分工具类测试")
class NumericalIntegrationTest {

    // Known integrals
    // integral of x^2 from 0 to 1 = 1/3
    // integral of sin(x) from 0 to pi = 2
    // integral of e^x from 0 to 1 = e - 1

    private static final double ONE_THIRD = 1.0 / 3.0;
    private static final double E_MINUS_1 = Math.E - 1.0;

    @Nested
    @DisplayName("trapezoid - 复合梯形法")
    class TrapezoidTest {

        @Test
        @DisplayName("积分 x^2 从 0 到 1")
        void integrateXSquared() {
            double result = NumericalIntegration.trapezoid(x -> x * x, 0, 1, 1000);
            assertThat(result).isCloseTo(ONE_THIRD, within(1e-6));
        }

        @Test
        @DisplayName("积分 sin(x) 从 0 到 pi")
        void integrateSin() {
            double result = NumericalIntegration.trapezoid(Math::sin, 0, Math.PI, 1000);
            assertThat(result).isCloseTo(2.0, within(1e-5));
        }

        @Test
        @DisplayName("积分 e^x 从 0 到 1")
        void integrateExp() {
            double result = NumericalIntegration.trapezoid(Math::exp, 0, 1, 1000);
            assertThat(result).isCloseTo(E_MINUS_1, within(1e-6));
        }

        @Test
        @DisplayName("增加子区间数提高精度")
        void convergenceWithMoreIntervals() {
            double exact = ONE_THIRD;
            double err100 = Math.abs(NumericalIntegration.trapezoid(x -> x * x, 0, 1, 100) - exact);
            double err1000 = Math.abs(NumericalIntegration.trapezoid(x -> x * x, 0, 1, 1000) - exact);
            assertThat(err1000).isLessThan(err100);
        }

        @Test
        @DisplayName("对线性函数精确")
        void exactForLinear() {
            // integral of (3x + 2) from 0 to 1 = 3/2 + 2 = 3.5
            double result = NumericalIntegration.trapezoid(x -> 3 * x + 2, 0, 1, 1);
            assertThat(result).isCloseTo(3.5, within(1e-15));
        }

        @Test
        @DisplayName("a > b 时结果取反")
        void reversedBounds() {
            double forward = NumericalIntegration.trapezoid(x -> x * x, 0, 1, 100);
            double reversed = NumericalIntegration.trapezoid(x -> x * x, 1, 0, 100);
            assertThat(reversed).isCloseTo(-forward, within(1e-15));
        }

        @Test
        @DisplayName("a == b 返回 0")
        void sameBoundsReturnsZero() {
            assertThat(NumericalIntegration.trapezoid(x -> x * x, 5, 5, 10)).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("simpson - 复合辛普森 1/3 法")
    class SimpsonTest {

        @Test
        @DisplayName("积分 x^2 从 0 到 1（高精度）")
        void integrateXSquared() {
            double result = NumericalIntegration.simpson(x -> x * x, 0, 1, 100);
            assertThat(result).isCloseTo(ONE_THIRD, within(1e-14));
        }

        @Test
        @DisplayName("积分 sin(x) 从 0 到 pi")
        void integrateSin() {
            double result = NumericalIntegration.simpson(Math::sin, 0, Math.PI, 100);
            assertThat(result).isCloseTo(2.0, within(1e-7));
        }

        @Test
        @DisplayName("积分 e^x 从 0 到 1")
        void integrateExp() {
            double result = NumericalIntegration.simpson(Math::exp, 0, 1, 100);
            assertThat(result).isCloseTo(E_MINUS_1, within(1e-8));
        }

        @Test
        @DisplayName("对三次多项式精确")
        void exactForCubic() {
            // integral of x^3 from 0 to 1 = 1/4
            double result = NumericalIntegration.simpson(x -> x * x * x, 0, 1, 2);
            assertThat(result).isCloseTo(0.25, within(1e-15));
        }

        @Test
        @DisplayName("n 为奇数抛出异常")
        void oddNThrows() {
            assertThatThrownBy(() -> NumericalIntegration.simpson(x -> x, 0, 1, 3))
                    .isInstanceOf(MathException.class)
                    .hasMessageContaining("even");
        }

        @Test
        @DisplayName("a > b 时结果取反")
        void reversedBounds() {
            double forward = NumericalIntegration.simpson(Math::sin, 0, Math.PI, 100);
            double reversed = NumericalIntegration.simpson(Math::sin, Math.PI, 0, 100);
            assertThat(reversed).isCloseTo(-forward, within(1e-15));
        }
    }

    @Nested
    @DisplayName("simpsonThreeEighths - 辛普森 3/8 法")
    class SimpsonThreeEighthsTest {

        @Test
        @DisplayName("积分 x^2 从 0 到 1")
        void integrateXSquared() {
            double result = NumericalIntegration.simpsonThreeEighths(x -> x * x, 0, 1, 99);
            assertThat(result).isCloseTo(ONE_THIRD, within(1e-10));
        }

        @Test
        @DisplayName("积分 sin(x) 从 0 到 pi")
        void integrateSin() {
            double result = NumericalIntegration.simpsonThreeEighths(Math::sin, 0, Math.PI, 99);
            assertThat(result).isCloseTo(2.0, within(1e-7));
        }

        @Test
        @DisplayName("积分 e^x 从 0 到 1")
        void integrateExp() {
            double result = NumericalIntegration.simpsonThreeEighths(Math::exp, 0, 1, 99);
            assertThat(result).isCloseTo(E_MINUS_1, within(1e-8));
        }

        @Test
        @DisplayName("对三次多项式精确")
        void exactForCubic() {
            // integral of x^3 from 0 to 1 = 1/4
            double result = NumericalIntegration.simpsonThreeEighths(x -> x * x * x, 0, 1, 3);
            assertThat(result).isCloseTo(0.25, within(1e-15));
        }

        @Test
        @DisplayName("n 不能被 3 整除抛出异常")
        void notDivisibleBy3Throws() {
            assertThatThrownBy(() -> NumericalIntegration.simpsonThreeEighths(x -> x, 0, 1, 4))
                    .isInstanceOf(MathException.class)
                    .hasMessageContaining("divisible by 3");
        }
    }

    @Nested
    @DisplayName("romberg - 龙贝格积分")
    class RombergTest {

        @Test
        @DisplayName("积分 x^2 从 0 到 1（高精度）")
        void integrateXSquared() {
            double result = NumericalIntegration.romberg(x -> x * x, 0, 1, 10, 1e-15);
            assertThat(result).isCloseTo(ONE_THIRD, within(1e-14));
        }

        @Test
        @DisplayName("积分 sin(x) 从 0 到 pi")
        void integrateSin() {
            double result = NumericalIntegration.romberg(Math::sin, 0, Math.PI, 15, 1e-12);
            assertThat(result).isCloseTo(2.0, within(1e-10));
        }

        @Test
        @DisplayName("积分 e^x 从 0 到 1")
        void integrateExp() {
            double result = NumericalIntegration.romberg(Math::exp, 0, 1, 15, 1e-12);
            assertThat(result).isCloseTo(E_MINUS_1, within(1e-10));
        }

        @Test
        @DisplayName("对多项式收敛极快")
        void fastConvergenceForPolynomials() {
            // Romberg with just a few iterations should be very accurate for x^2
            double result = NumericalIntegration.romberg(x -> x * x, 0, 1, 3, 1e-15);
            assertThat(result).isCloseTo(ONE_THIRD, within(1e-14));
        }

        @Test
        @DisplayName("a > b 时结果取反")
        void reversedBounds() {
            double forward = NumericalIntegration.romberg(Math::exp, 0, 1, 10, 1e-12);
            double reversed = NumericalIntegration.romberg(Math::exp, 1, 0, 10, 1e-12);
            assertThat(reversed).isCloseTo(-forward, within(1e-15));
        }

        @Test
        @DisplayName("maxIterations < 1 抛出异常")
        void invalidMaxIterationsThrows() {
            assertThatThrownBy(() -> NumericalIntegration.romberg(x -> x, 0, 1, 0, 1e-6))
                    .isInstanceOf(MathException.class)
                    .hasMessageContaining("maxIterations");
        }

        @Test
        @DisplayName("tolerance <= 0 抛出异常")
        void invalidToleranceThrows() {
            assertThatThrownBy(() -> NumericalIntegration.romberg(x -> x, 0, 1, 10, 0))
                    .isInstanceOf(MathException.class)
                    .hasMessageContaining("tolerance");
            assertThatThrownBy(() -> NumericalIntegration.romberg(x -> x, 0, 1, 10, -1e-6))
                    .isInstanceOf(MathException.class)
                    .hasMessageContaining("tolerance");
        }
    }

    @Nested
    @DisplayName("gaussLegendre - 高斯-勒让德求积")
    class GaussLegendreTest {

        @Test
        @DisplayName("2 点规则对三次多项式精确")
        void twoPointExactForCubic() {
            // 2-point GL is exact for polynomials up to degree 3
            // integral of x^3 from 0 to 1 = 1/4
            double result = NumericalIntegration.gaussLegendre(x -> x * x * x, 0, 1, 2);
            assertThat(result).isCloseTo(0.25, within(1e-14));
        }

        @Test
        @DisplayName("3 点规则对五次多项式精确")
        void threePointExactForQuintic() {
            // integral of x^5 from 0 to 1 = 1/6
            double result = NumericalIntegration.gaussLegendre(x -> Math.pow(x, 5), 0, 1, 3);
            assertThat(result).isCloseTo(1.0 / 6.0, within(1e-14));
        }

        @Test
        @DisplayName("5 点规则积分 sin(x)")
        void fivePointSin() {
            double result = NumericalIntegration.gaussLegendre(Math::sin, 0, Math.PI, 5);
            assertThat(result).isCloseTo(2.0, within(1e-6));
        }

        @Test
        @DisplayName("积分 e^x 从 0 到 1")
        void integrateExp() {
            double result = NumericalIntegration.gaussLegendre(Math::exp, 0, 1, 5);
            assertThat(result).isCloseTo(E_MINUS_1, within(1e-9));
        }

        @Test
        @DisplayName("4 点规则对七次多项式精确")
        void fourPointExactForSeptic() {
            // integral of x^7 from 0 to 1 = 1/8
            double result = NumericalIntegration.gaussLegendre(x -> Math.pow(x, 7), 0, 1, 4);
            assertThat(result).isCloseTo(1.0 / 8.0, within(1e-13));
        }

        @Test
        @DisplayName("points 超出范围抛出异常")
        void invalidPointsThrows() {
            assertThatThrownBy(() -> NumericalIntegration.gaussLegendre(x -> x, 0, 1, 1))
                    .isInstanceOf(MathException.class)
                    .hasMessageContaining("2-5");
            assertThatThrownBy(() -> NumericalIntegration.gaussLegendre(x -> x, 0, 1, 6))
                    .isInstanceOf(MathException.class)
                    .hasMessageContaining("2-5");
        }

        @Test
        @DisplayName("a > b 时结果取反")
        void reversedBounds() {
            double forward = NumericalIntegration.gaussLegendre(Math::exp, 0, 1, 4);
            double reversed = NumericalIntegration.gaussLegendre(Math::exp, 1, 0, 4);
            assertThat(reversed).isCloseTo(-forward, within(1e-15));
        }

        @Test
        @DisplayName("a == b 返回 0")
        void sameBoundsReturnsZero() {
            assertThat(NumericalIntegration.gaussLegendre(Math::sin, 2, 2, 3)).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("输入验证")
    class ValidationTest {

        @Test
        @DisplayName("null 函数抛出异常")
        void nullFunctionThrows() {
            assertThatThrownBy(() -> NumericalIntegration.trapezoid(null, 0, 1, 10))
                    .isInstanceOf(MathException.class)
                    .hasMessageContaining("null");
            assertThatThrownBy(() -> NumericalIntegration.simpson(null, 0, 1, 10))
                    .isInstanceOf(MathException.class)
                    .hasMessageContaining("null");
            assertThatThrownBy(() -> NumericalIntegration.romberg(null, 0, 1, 10, 1e-6))
                    .isInstanceOf(MathException.class)
                    .hasMessageContaining("null");
            assertThatThrownBy(() -> NumericalIntegration.gaussLegendre(null, 0, 1, 3))
                    .isInstanceOf(MathException.class)
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("n < 最小值抛出异常")
        void tooFewIntervalsThrows() {
            assertThatThrownBy(() -> NumericalIntegration.trapezoid(x -> x, 0, 1, 0))
                    .isInstanceOf(MathException.class);
            assertThatThrownBy(() -> NumericalIntegration.simpson(x -> x, 0, 1, 1))
                    .isInstanceOf(MathException.class);
            assertThatThrownBy(() -> NumericalIntegration.simpsonThreeEighths(x -> x, 0, 1, 2))
                    .isInstanceOf(MathException.class);
        }
    }

    @Nested
    @DisplayName("收敛性验证")
    class ConvergenceTest {

        @Test
        @DisplayName("辛普森法比梯形法更精确（相同子区间数）")
        void simpsonBetterThanTrapezoid() {
            double exact = ONE_THIRD;
            double trapErr = Math.abs(NumericalIntegration.trapezoid(x -> x * x, 0, 1, 10) - exact);
            double simpErr = Math.abs(NumericalIntegration.simpson(x -> x * x, 0, 1, 10) - exact);
            assertThat(simpErr).isLessThan(trapErr);
        }

        @Test
        @DisplayName("龙贝格法比梯形法收敛更快")
        void rombergConvergesFasterThanTrapezoid() {
            double exact = 2.0;
            double trapErr = Math.abs(NumericalIntegration.trapezoid(Math::sin, 0, Math.PI, 8) - exact);
            double rombErr = Math.abs(NumericalIntegration.romberg(Math::sin, 0, Math.PI, 4, 1e-15) - exact);
            assertThat(rombErr).isLessThan(trapErr);
        }
    }
}
