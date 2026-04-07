package cloud.opencode.base.math.analysis;

import cloud.opencode.base.math.exception.MathException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

/**
 * Tests for {@link Differentiation}.
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-math V1.0.3
 */
@DisplayName("Differentiation 数值微分测试")
class DifferentiationTest {

    @Nested
    @DisplayName("derivative 一阶导数")
    class FirstDerivativeTests {

        @Test
        @DisplayName("sin 在 pi/4 处的导数约等于 cos(pi/4)")
        void sinDerivative() {
            double x = Math.PI / 4;
            double result = Differentiation.derivative(Math::sin, x);
            assertThat(result).isCloseTo(Math.cos(x), within(1e-6));
        }

        @Test
        @DisplayName("x^3 在 x=2 处的导数约等于 12")
        void cubicDerivative() {
            double result = Differentiation.derivative(x -> x * x * x, 2.0);
            assertThat(result).isCloseTo(12.0, within(1e-4));
        }

        @Test
        @DisplayName("e^x 在 x=1 处的导数约等于 e")
        void expDerivative() {
            double result = Differentiation.derivative(Math::exp, 1.0);
            assertThat(result).isCloseTo(Math.E, within(1e-5));
        }

        @Test
        @DisplayName("指定步长的一阶导数")
        void withExplicitH() {
            double result = Differentiation.derivative(x -> x * x, 3.0, 1e-6);
            assertThat(result).isCloseTo(6.0, within(1e-4));
        }

        @Test
        @DisplayName("x=0 处的导数也能正确计算")
        void atZero() {
            double result = Differentiation.derivative(Math::sin, 0.0);
            assertThat(result).isCloseTo(1.0, within(1e-6));
        }
    }

    @Nested
    @DisplayName("secondDerivative 二阶导数")
    class SecondDerivativeTests {

        @Test
        @DisplayName("sin 在 pi/4 处的二阶导数约等于 -sin(pi/4)")
        void sinSecondDerivative() {
            double x = Math.PI / 4;
            double result = Differentiation.secondDerivative(Math::sin, x);
            assertThat(result).isCloseTo(-Math.sin(x), within(1e-4));
        }

        @Test
        @DisplayName("x^3 在 x=2 处的二阶导数约等于 12")
        void cubicSecondDerivative() {
            double result = Differentiation.secondDerivative(x -> x * x * x, 2.0);
            assertThat(result).isCloseTo(12.0, within(1e-3));
        }

        @Test
        @DisplayName("指定步长的二阶导数")
        void withExplicitH() {
            double result = Differentiation.secondDerivative(x -> x * x * x * x, 1.0, 1e-3);
            // 4th derivative: 12x^2, at x=1 -> 12
            assertThat(result).isCloseTo(12.0, within(1e-2));
        }
    }

    @Nested
    @DisplayName("richardson 理查森外推")
    class RichardsonTests {

        @Test
        @DisplayName("理查森外推比简单中心差分更精确")
        void moreAccurateThanCentralDifference() {
            double x = Math.PI / 4;
            double expected = Math.cos(x);

            double simple = Differentiation.derivative(Math::sin, x);
            double rich = Differentiation.richardson(Math::sin, x, 4);

            double simpleError = Math.abs(simple - expected);
            double richError = Math.abs(rich - expected);

            assertThat(richError).isLessThan(simpleError);
            assertThat(rich).isCloseTo(expected, within(1e-12));
        }

        @Test
        @DisplayName("x^5 在 x=1 处的导数（理查森阶数 3）")
        void polynomialDerivative() {
            double result = Differentiation.richardson(x -> Math.pow(x, 5), 1.0, 3);
            assertThat(result).isCloseTo(5.0, within(1e-8));
        }

        @Test
        @DisplayName("阶数 1 也能工作")
        void orderOne() {
            double result = Differentiation.richardson(x -> x * x, 3.0, 1);
            assertThat(result).isCloseTo(6.0, within(1e-6));
        }

        @Test
        @DisplayName("阶数超出范围时抛出异常")
        void invalidOrder() {
            assertThatThrownBy(() -> Differentiation.richardson(x -> x, 0, 0))
                    .isInstanceOf(MathException.class)
                    .hasMessageContaining("[1, 6]");
            assertThatThrownBy(() -> Differentiation.richardson(x -> x, 0, 7))
                    .isInstanceOf(MathException.class)
                    .hasMessageContaining("[1, 6]");
        }
    }

    @Nested
    @DisplayName("参数校验")
    class ValidationTests {

        @Test
        @DisplayName("函数为 null 时抛出异常")
        void nullFunction() {
            assertThatThrownBy(() -> Differentiation.derivative(null, 0))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("x 为 NaN 时抛出异常")
        void nanX() {
            assertThatThrownBy(() -> Differentiation.derivative(Math::sin, Double.NaN))
                    .isInstanceOf(MathException.class)
                    .hasMessageContaining("finite");
        }

        @Test
        @DisplayName("x 为 Infinity 时抛出异常")
        void infinityX() {
            assertThatThrownBy(() -> Differentiation.derivative(Math::sin, Double.POSITIVE_INFINITY))
                    .isInstanceOf(MathException.class)
                    .hasMessageContaining("finite");
        }

        @Test
        @DisplayName("步长非正时抛出异常")
        void nonPositiveH() {
            assertThatThrownBy(() -> Differentiation.derivative(Math::sin, 0, -1))
                    .isInstanceOf(MathException.class)
                    .hasMessageContaining("positive");
            assertThatThrownBy(() -> Differentiation.derivative(Math::sin, 0, 0))
                    .isInstanceOf(MathException.class)
                    .hasMessageContaining("positive");
        }
    }
}
