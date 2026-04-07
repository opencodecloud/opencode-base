package cloud.opencode.base.math.analysis;

import cloud.opencode.base.math.exception.MathException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

/**
 * Tests for {@link RootFinder}.
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-math V1.0.3
 */
@DisplayName("RootFinder 求根算法测试")
class RootFinderTest {

    private static final double TOL = 1e-12;

    @Nested
    @DisplayName("bisection 二分法")
    class BisectionTests {

        @Test
        @DisplayName("求 x^2 - 2 = 0 在 [0, 2] 上的根（应为 sqrt(2)）")
        void squareRootOfTwo() {
            double root = RootFinder.bisection(x -> x * x - 2, 0, 2, TOL);
            assertThat(root).isCloseTo(Math.sqrt(2), within(TOL));
        }

        @Test
        @DisplayName("求 cos(x) = 0 在 [0, 2] 上的根（应为 pi/2）")
        void cosineRoot() {
            double root = RootFinder.bisection(Math::cos, 0, 2, TOL);
            assertThat(root).isCloseTo(Math.PI / 2, within(TOL));
        }

        @Test
        @DisplayName("端点恰为根时直接返回")
        void exactRootAtEndpoint() {
            double root = RootFinder.bisection(x -> x - 1, 1, 3, TOL);
            assertThat(root).isCloseTo(1.0, within(TOL));
        }

        @Test
        @DisplayName("无符号变化时抛出异常")
        void noSignChange() {
            assertThatThrownBy(() -> RootFinder.bisection(x -> x * x + 1, 0, 2, TOL))
                    .isInstanceOf(MathException.class)
                    .hasMessageContaining("opposite signs");
        }

        @Test
        @DisplayName("函数为 null 时抛出异常")
        void nullFunction() {
            assertThatThrownBy(() -> RootFinder.bisection(null, 0, 1, TOL))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("容差非正时抛出异常")
        void nonPositiveTolerance() {
            assertThatThrownBy(() -> RootFinder.bisection(x -> x, 0, 1, -1))
                    .isInstanceOf(MathException.class)
                    .hasMessageContaining("positive");
        }

        @Test
        @DisplayName("端点为 NaN 时抛出异常")
        void nanEndpoint() {
            assertThatThrownBy(() -> RootFinder.bisection(x -> x, Double.NaN, 1, TOL))
                    .isInstanceOf(MathException.class)
                    .hasMessageContaining("finite");
        }

        @Test
        @DisplayName("端点为 Infinity 时抛出异常")
        void infinityEndpoint() {
            assertThatThrownBy(() -> RootFinder.bisection(x -> x, 0, Double.POSITIVE_INFINITY, TOL))
                    .isInstanceOf(MathException.class)
                    .hasMessageContaining("finite");
        }
    }

    @Nested
    @DisplayName("brent Brent 法")
    class BrentTests {

        @Test
        @DisplayName("求 x^2 - 2 = 0 在 [0, 2] 上的根（应为 sqrt(2)）")
        void squareRootOfTwo() {
            double root = RootFinder.brent(x -> x * x - 2, 0, 2, TOL);
            assertThat(root).isCloseTo(Math.sqrt(2), within(TOL));
        }

        @Test
        @DisplayName("求 cos(x) = 0 在 [0, 2] 上的根（应为 pi/2）")
        void cosineRoot() {
            double root = RootFinder.brent(Math::cos, 0, 2, TOL);
            assertThat(root).isCloseTo(Math.PI / 2, within(TOL));
        }

        @Test
        @DisplayName("求 x^3 - x - 2 = 0 在 [1, 2] 上的根")
        void cubicRoot() {
            double root = RootFinder.brent(x -> x * x * x - x - 2, 1, 2, TOL);
            // Verify by plugging back in
            assertThat(Math.abs(root * root * root - root - 2)).isLessThan(1e-10);
        }

        @Test
        @DisplayName("无符号变化时抛出异常")
        void noSignChange() {
            assertThatThrownBy(() -> RootFinder.brent(x -> x * x + 1, 0, 2, TOL))
                    .isInstanceOf(MathException.class)
                    .hasMessageContaining("opposite signs");
        }
    }

    @Nested
    @DisplayName("newton 牛顿法")
    class NewtonTests {

        @Test
        @DisplayName("求 x^2 - 2 = 0 的根，初始猜测 x0=1（应为 sqrt(2)）")
        void squareRootOfTwo() {
            double root = RootFinder.newton(x -> x * x - 2, x -> 2 * x, 1, TOL);
            assertThat(root).isCloseTo(Math.sqrt(2), within(TOL));
        }

        @Test
        @DisplayName("求 sin(x) = 0 的根，初始猜测 x0=3（应为 pi）")
        void sinRoot() {
            double root = RootFinder.newton(Math::sin, Math::cos, 3, TOL);
            assertThat(root).isCloseTo(Math.PI, within(TOL));
        }

        @Test
        @DisplayName("导数为零时抛出异常")
        void derivativeZero() {
            // f(x) = x^2, df(x) = 2x, at x=0 derivative is 0
            // Start at x0=0 where derivative is zero
            assertThatThrownBy(() -> RootFinder.newton(x -> x * x - 1, x -> 0.0, 5, TOL))
                    .isInstanceOf(MathException.class)
                    .hasMessageContaining("Derivative too close to zero");
        }

        @Test
        @DisplayName("导数函数为 null 时抛出异常")
        void nullDerivative() {
            assertThatThrownBy(() -> RootFinder.newton(x -> x, null, 1, TOL))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("secant 割线法")
    class SecantTests {

        @Test
        @DisplayName("求 x^2 - 2 = 0 的根，x0=1, x1=2（应为 sqrt(2)）")
        void squareRootOfTwo() {
            double root = RootFinder.secant(x -> x * x - 2, 1, 2, TOL);
            assertThat(root).isCloseTo(Math.sqrt(2), within(TOL));
        }

        @Test
        @DisplayName("求 e^x - 3 = 0 的根")
        void exponentialRoot() {
            double root = RootFinder.secant(x -> Math.exp(x) - 3, 0, 2, TOL);
            assertThat(root).isCloseTo(Math.log(3), within(TOL));
        }

        @Test
        @DisplayName("x0 为 NaN 时抛出异常")
        void nanX0() {
            assertThatThrownBy(() -> RootFinder.secant(x -> x, Double.NaN, 1, TOL))
                    .isInstanceOf(MathException.class)
                    .hasMessageContaining("finite");
        }
    }
}
