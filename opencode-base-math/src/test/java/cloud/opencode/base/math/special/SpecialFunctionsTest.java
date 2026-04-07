package cloud.opencode.base.math.special;

import cloud.opencode.base.math.exception.MathException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

/**
 * Tests for {@link SpecialFunctions}.
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-math V1.0.3
 */
@DisplayName("SpecialFunctions 特殊函数测试")
class SpecialFunctionsTest {

    private static final double TOLERANCE = 1e-6;

    @Nested
    @DisplayName("gamma - Gamma 函数")
    class GammaTest {

        @Test
        @DisplayName("Gamma(5) = 4! = 24")
        void gamma5() {
            assertThat(SpecialFunctions.gamma(5)).isCloseTo(24.0, within(1e-10));
        }

        @Test
        @DisplayName("Gamma(1) = 1")
        void gamma1() {
            assertThat(SpecialFunctions.gamma(1)).isCloseTo(1.0, within(1e-10));
        }

        @Test
        @DisplayName("Gamma(0.5) = sqrt(pi)")
        void gammaHalf() {
            assertThat(SpecialFunctions.gamma(0.5)).isCloseTo(Math.sqrt(Math.PI), within(TOLERANCE));
        }

        @Test
        @DisplayName("Gamma(n) = (n-1)! 对正整数")
        void gammaFactorial() {
            assertThat(SpecialFunctions.gamma(1)).isCloseTo(1.0, within(1e-10));
            assertThat(SpecialFunctions.gamma(2)).isCloseTo(1.0, within(1e-10));
            assertThat(SpecialFunctions.gamma(3)).isCloseTo(2.0, within(1e-10));
            assertThat(SpecialFunctions.gamma(4)).isCloseTo(6.0, within(1e-10));
            assertThat(SpecialFunctions.gamma(6)).isCloseTo(120.0, within(1e-8));
        }

        @Test
        @DisplayName("Gamma 递推关系: Gamma(x+1) = x * Gamma(x)")
        void gammaRecurrence() {
            double x = 3.7;
            assertThat(SpecialFunctions.gamma(x + 1))
                    .isCloseTo(x * SpecialFunctions.gamma(x), within(1e-8));
        }

        @Test
        @DisplayName("负非整数值使用反射公式")
        void gammaNegativeNonInteger() {
            // Gamma(-0.5) = -2*sqrt(pi)
            assertThat(SpecialFunctions.gamma(-0.5)).isCloseTo(-2.0 * Math.sqrt(Math.PI), within(TOLERANCE));
        }

        @Test
        @DisplayName("非正整数（极点）抛出异常")
        void gammaAtPoles() {
            assertThatThrownBy(() -> SpecialFunctions.gamma(0))
                    .isInstanceOf(MathException.class);
            assertThatThrownBy(() -> SpecialFunctions.gamma(-1))
                    .isInstanceOf(MathException.class);
            assertThatThrownBy(() -> SpecialFunctions.gamma(-2))
                    .isInstanceOf(MathException.class);
        }

        @Test
        @DisplayName("Gamma(+Infinity) = +Infinity")
        void gammaInfinity() {
            assertThat(SpecialFunctions.gamma(Double.POSITIVE_INFINITY))
                    .isEqualTo(Double.POSITIVE_INFINITY);
        }

        @Test
        @DisplayName("Gamma(NaN) = NaN")
        void gammaNaN() {
            assertThat(SpecialFunctions.gamma(Double.NaN)).isNaN();
        }
    }

    @Nested
    @DisplayName("logGamma - Gamma 函数对数")
    class LogGammaTest {

        @Test
        @DisplayName("logGamma(1) = 0")
        void logGamma1() {
            assertThat(SpecialFunctions.logGamma(1)).isCloseTo(0.0, within(1e-10));
        }

        @Test
        @DisplayName("logGamma(5) = ln(24)")
        void logGamma5() {
            assertThat(SpecialFunctions.logGamma(5)).isCloseTo(Math.log(24), within(1e-10));
        }

        @Test
        @DisplayName("logGamma(0.5) = ln(sqrt(pi))")
        void logGammaHalf() {
            assertThat(SpecialFunctions.logGamma(0.5))
                    .isCloseTo(Math.log(Math.sqrt(Math.PI)), within(TOLERANCE));
        }

        @Test
        @DisplayName("大值的数值稳定性")
        void logGammaLargeValue() {
            // logGamma(100) should be finite and positive
            double result = SpecialFunctions.logGamma(100);
            assertThat(result).isFinite().isPositive();
        }

        @Test
        @DisplayName("非正参数抛出异常")
        void logGammaInvalid() {
            assertThatThrownBy(() -> SpecialFunctions.logGamma(0))
                    .isInstanceOf(MathException.class);
            assertThatThrownBy(() -> SpecialFunctions.logGamma(-1))
                    .isInstanceOf(MathException.class);
        }
    }

    @Nested
    @DisplayName("beta - Beta 函数")
    class BetaTest {

        @Test
        @DisplayName("Beta(2,3) = 1/12")
        void beta2_3() {
            assertThat(SpecialFunctions.beta(2, 3)).isCloseTo(1.0 / 12.0, within(TOLERANCE));
        }

        @Test
        @DisplayName("Beta(1,1) = 1")
        void beta1_1() {
            assertThat(SpecialFunctions.beta(1, 1)).isCloseTo(1.0, within(1e-10));
        }

        @Test
        @DisplayName("Beta 对称性: Beta(a,b) = Beta(b,a)")
        void betaSymmetry() {
            assertThat(SpecialFunctions.beta(3, 5))
                    .isCloseTo(SpecialFunctions.beta(5, 3), within(1e-12));
        }

        @Test
        @DisplayName("Beta(0.5, 0.5) = pi")
        void betaHalfHalf() {
            assertThat(SpecialFunctions.beta(0.5, 0.5)).isCloseTo(Math.PI, within(TOLERANCE));
        }

        @Test
        @DisplayName("非正参数抛出异常")
        void betaInvalid() {
            assertThatThrownBy(() -> SpecialFunctions.beta(0, 1))
                    .isInstanceOf(MathException.class);
            assertThatThrownBy(() -> SpecialFunctions.beta(1, -1))
                    .isInstanceOf(MathException.class);
        }
    }

    @Nested
    @DisplayName("erf - 误差函数")
    class ErfTest {

        @Test
        @DisplayName("erf(0) = 0")
        void erfZero() {
            assertThat(SpecialFunctions.erf(0)).isCloseTo(0.0, within(1e-10));
        }

        @Test
        @DisplayName("erf(1) 约等于 0.8427")
        void erfOne() {
            assertThat(SpecialFunctions.erf(1)).isCloseTo(0.8427, within(1e-4));
        }

        @Test
        @DisplayName("erf(+Infinity) = 1")
        void erfInfinity() {
            assertThat(SpecialFunctions.erf(Double.POSITIVE_INFINITY)).isEqualTo(1.0);
        }

        @Test
        @DisplayName("erf(-Infinity) = -1")
        void erfNegInfinity() {
            assertThat(SpecialFunctions.erf(Double.NEGATIVE_INFINITY)).isEqualTo(-1.0);
        }

        @Test
        @DisplayName("erf 奇函数: erf(-x) = -erf(x)")
        void erfOddFunction() {
            assertThat(SpecialFunctions.erf(-1.5))
                    .isCloseTo(-SpecialFunctions.erf(1.5), within(1e-10));
        }

        @Test
        @DisplayName("erf(NaN) = NaN")
        void erfNaN() {
            assertThat(SpecialFunctions.erf(Double.NaN)).isNaN();
        }

        @Test
        @DisplayName("大 x 值趋近于 1")
        void erfLargeX() {
            assertThat(SpecialFunctions.erf(5)).isCloseTo(1.0, within(1e-6));
        }
    }

    @Nested
    @DisplayName("erfc - 互补误差函数")
    class ErfcTest {

        @Test
        @DisplayName("erfc(0) = 1")
        void erfcZero() {
            assertThat(SpecialFunctions.erfc(0)).isCloseTo(1.0, within(1e-6));
        }

        @Test
        @DisplayName("erfc(x) + erf(x) = 1")
        void erfcPlusErf() {
            double[] xs = {0, 0.5, 1, 2, 3};
            for (double x : xs) {
                assertThat(SpecialFunctions.erfc(x) + SpecialFunctions.erf(x))
                        .as("erfc(%f) + erf(%f) = 1", x, x)
                        .isCloseTo(1.0, within(1e-4));
            }
        }

        @Test
        @DisplayName("erfc(+Infinity) = 0")
        void erfcInfinity() {
            assertThat(SpecialFunctions.erfc(Double.POSITIVE_INFINITY)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("erfc(-Infinity) = 2")
        void erfcNegInfinity() {
            assertThat(SpecialFunctions.erfc(Double.NEGATIVE_INFINITY)).isEqualTo(2.0);
        }
    }

    @Nested
    @DisplayName("regularizedBeta - 正则化不完全 Beta 函数")
    class RegularizedBetaTest {

        @Test
        @DisplayName("I_0(a,b) = 0")
        void atZero() {
            assertThat(SpecialFunctions.regularizedBeta(0, 2, 3)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("I_1(a,b) = 1")
        void atOne() {
            assertThat(SpecialFunctions.regularizedBeta(1, 2, 3)).isEqualTo(1.0);
        }

        @Test
        @DisplayName("I_0.5(1,1) = 0.5 (均匀分布)")
        void uniformCase() {
            assertThat(SpecialFunctions.regularizedBeta(0.5, 1, 1))
                    .isCloseTo(0.5, within(1e-10));
        }

        @Test
        @DisplayName("已知值: I_0.5(2,3)")
        void knownValue() {
            // I_0.5(2,3) = 0.6875 (exact)
            assertThat(SpecialFunctions.regularizedBeta(0.5, 2, 3))
                    .isCloseTo(0.6875, within(1e-6));
        }

        @Test
        @DisplayName("参数校验: x 超出 [0,1] 抛出异常")
        void invalidX() {
            assertThatThrownBy(() -> SpecialFunctions.regularizedBeta(-0.1, 1, 1))
                    .isInstanceOf(MathException.class);
            assertThatThrownBy(() -> SpecialFunctions.regularizedBeta(1.1, 1, 1))
                    .isInstanceOf(MathException.class);
        }

        @Test
        @DisplayName("参数校验: a 或 b <= 0 抛出异常")
        void invalidParams() {
            assertThatThrownBy(() -> SpecialFunctions.regularizedBeta(0.5, 0, 1))
                    .isInstanceOf(MathException.class);
            assertThatThrownBy(() -> SpecialFunctions.regularizedBeta(0.5, 1, -1))
                    .isInstanceOf(MathException.class);
        }
    }

    @Nested
    @DisplayName("regularizedGammaP - 正则化下不完全 Gamma 函数")
    class RegularizedGammaPTest {

        @Test
        @DisplayName("P(a, 0) = 0")
        void atZero() {
            assertThat(SpecialFunctions.regularizedGammaP(2, 0)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("P(a, +Infinity) = 1")
        void atInfinity() {
            assertThat(SpecialFunctions.regularizedGammaP(2, Double.POSITIVE_INFINITY)).isEqualTo(1.0);
        }

        @Test
        @DisplayName("P(1, x) = 1 - e^(-x) (指数分布)")
        void exponentialCase() {
            double x = 2.0;
            assertThat(SpecialFunctions.regularizedGammaP(1, x))
                    .isCloseTo(1.0 - Math.exp(-x), within(1e-8));
        }

        @Test
        @DisplayName("P(a, x) 单调递增")
        void monotonic() {
            assertThat(SpecialFunctions.regularizedGammaP(3, 1))
                    .isLessThan(SpecialFunctions.regularizedGammaP(3, 3));
            assertThat(SpecialFunctions.regularizedGammaP(3, 3))
                    .isLessThan(SpecialFunctions.regularizedGammaP(3, 10));
        }

        @Test
        @DisplayName("参数校验: a <= 0 抛出异常")
        void invalidA() {
            assertThatThrownBy(() -> SpecialFunctions.regularizedGammaP(0, 1))
                    .isInstanceOf(MathException.class);
        }

        @Test
        @DisplayName("参数校验: x < 0 抛出异常")
        void invalidX() {
            assertThatThrownBy(() -> SpecialFunctions.regularizedGammaP(1, -1))
                    .isInstanceOf(MathException.class);
        }
    }
}
