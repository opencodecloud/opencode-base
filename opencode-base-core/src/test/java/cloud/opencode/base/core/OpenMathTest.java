package cloud.opencode.base.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenMath 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("OpenMath 测试")
class OpenMathTest {

    @Nested
    @DisplayName("基本运算测试")
    class BasicArithmeticTests {

        @Test
        @DisplayName("add 加法")
        void testAdd() {
            BigDecimal result = OpenMath.add(new BigDecimal("10"), new BigDecimal("20"));
            assertThat(result).isEqualTo(new BigDecimal("30"));
        }

        @Test
        @DisplayName("add null 处理")
        void testAddNull() {
            assertThat(OpenMath.add(null, new BigDecimal("10"))).isEqualTo(new BigDecimal("10"));
            assertThat(OpenMath.add(new BigDecimal("10"), null)).isEqualTo(new BigDecimal("10"));
        }

        @Test
        @DisplayName("subtract 减法")
        void testSubtract() {
            BigDecimal result = OpenMath.subtract(new BigDecimal("30"), new BigDecimal("10"));
            assertThat(result).isEqualTo(new BigDecimal("20"));
        }

        @Test
        @DisplayName("subtract null 处理")
        void testSubtractNull() {
            assertThat(OpenMath.subtract(null, null)).isEqualTo(BigDecimal.ZERO);
            assertThat(OpenMath.subtract(new BigDecimal("10"), null)).isEqualTo(new BigDecimal("10"));
        }

        @Test
        @DisplayName("multiply 乘法")
        void testMultiply() {
            BigDecimal result = OpenMath.multiply(new BigDecimal("3"), new BigDecimal("4"));
            assertThat(result).isEqualTo(new BigDecimal("12"));
        }

        @Test
        @DisplayName("multiply null 返回 0")
        void testMultiplyNull() {
            assertThat(OpenMath.multiply(null, new BigDecimal("5"))).isEqualTo(BigDecimal.ZERO);
            assertThat(OpenMath.multiply(new BigDecimal("5"), null)).isEqualTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("divide 除法")
        void testDivide() {
            BigDecimal result = OpenMath.divide(new BigDecimal("10"), new BigDecimal("3"), 2);
            assertThat(result).isEqualTo(new BigDecimal("3.33"));
        }

        @Test
        @DisplayName("divide 指定舍入模式")
        void testDivideWithMode() {
            BigDecimal result = OpenMath.divide(new BigDecimal("10"), new BigDecimal("3"), 2, RoundingMode.DOWN);
            assertThat(result).isEqualTo(new BigDecimal("3.33"));
        }

        @Test
        @DisplayName("divide null 处理")
        void testDivideNull() {
            assertThat(OpenMath.divide(null, new BigDecimal("5"), 2)).isEqualTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("divide 除零异常")
        void testDivideByZero() {
            assertThatThrownBy(() -> OpenMath.divide(new BigDecimal("10"), BigDecimal.ZERO, 2))
                    .isInstanceOf(ArithmeticException.class);
            assertThatThrownBy(() -> OpenMath.divide(new BigDecimal("10"), null, 2))
                    .isInstanceOf(ArithmeticException.class);
        }
    }

    @Nested
    @DisplayName("四舍五入测试")
    class RoundTests {

        @Test
        @DisplayName("round")
        void testRound() {
            assertThat(OpenMath.round(3.456, 2)).isEqualTo(3.46);
            assertThat(OpenMath.round(3.454, 2)).isEqualTo(3.45);
            assertThat(OpenMath.round(3.5, 0)).isEqualTo(4.0);
        }

        @Test
        @DisplayName("round 负数 scale 抛异常")
        void testRoundNegativeScale() {
            assertThatThrownBy(() -> OpenMath.round(3.456, -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("ceil 向上取整")
        void testCeil() {
            assertThat(OpenMath.ceil(3.11, 1)).isEqualTo(3.2);
            assertThat(OpenMath.ceil(3.19, 1)).isEqualTo(3.2);
            assertThat(OpenMath.ceil(-3.11, 1)).isEqualTo(-3.1);
        }

        @Test
        @DisplayName("floor 向下取整")
        void testFloor() {
            assertThat(OpenMath.floor(3.99, 1)).isEqualTo(3.9);
            assertThat(OpenMath.floor(3.11, 1)).isEqualTo(3.1);
            assertThat(OpenMath.floor(-3.11, 1)).isEqualTo(-3.2);
        }
    }

    @Nested
    @DisplayName("统计函数测试")
    class StatisticsTests {

        @Test
        @DisplayName("mean 平均值")
        void testMean() {
            assertThat(OpenMath.mean(1.0, 2.0, 3.0)).isEqualTo(2.0);
            assertThat(OpenMath.mean(10.0)).isEqualTo(10.0);
        }

        @Test
        @DisplayName("mean 空数组")
        void testMeanEmpty() {
            assertThat(OpenMath.mean()).isEqualTo(0.0);
            assertThat(OpenMath.mean((double[]) null)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("median 中位数")
        void testMedian() {
            assertThat(OpenMath.median(1.0, 2.0, 3.0)).isEqualTo(2.0);
            assertThat(OpenMath.median(1.0, 2.0, 3.0, 4.0)).isEqualTo(2.5);
            assertThat(OpenMath.median(3.0, 1.0, 2.0)).isEqualTo(2.0);
        }

        @Test
        @DisplayName("median 空数组")
        void testMedianEmpty() {
            assertThat(OpenMath.median()).isEqualTo(0.0);
            assertThat(OpenMath.median((double[]) null)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("variance 方差")
        void testVariance() {
            assertThat(OpenMath.variance(2.0, 4.0, 4.0, 4.0, 5.0, 5.0, 7.0, 9.0)).isEqualTo(4.0);
        }

        @Test
        @DisplayName("variance 空数组")
        void testVarianceEmpty() {
            assertThat(OpenMath.variance()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("stdDev 标准差")
        void testStdDev() {
            assertThat(OpenMath.stdDev(2.0, 4.0, 4.0, 4.0, 5.0, 5.0, 7.0, 9.0)).isEqualTo(2.0);
        }

        @Test
        @DisplayName("sum double")
        void testSumDouble() {
            assertThat(OpenMath.sum(1.0, 2.0, 3.0)).isEqualTo(6.0);
            assertThat(OpenMath.sum((double[]) null)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("sum long")
        void testSumLong() {
            assertThat(OpenMath.sum(1L, 2L, 3L)).isEqualTo(6L);
            assertThat(OpenMath.sum((long[]) null)).isEqualTo(0L);
        }

        @Test
        @DisplayName("sum int")
        void testSumInt() {
            assertThat(OpenMath.sum(1, 2, 3)).isEqualTo(6);
            assertThat(OpenMath.sum((int[]) null)).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("数论函数测试")
    class NumberTheoryTests {

        @Test
        @DisplayName("gcd 最大公约数 int")
        void testGcdInt() {
            assertThat(OpenMath.gcd(12, 18)).isEqualTo(6);
            assertThat(OpenMath.gcd(17, 13)).isEqualTo(1);
            assertThat(OpenMath.gcd(-12, 18)).isEqualTo(6);
            assertThat(OpenMath.gcd(0, 5)).isEqualTo(5);
        }

        @Test
        @DisplayName("gcd 最大公约数 long")
        void testGcdLong() {
            assertThat(OpenMath.gcd(12L, 18L)).isEqualTo(6L);
        }

        @Test
        @DisplayName("lcm 最小公倍数 int")
        void testLcmInt() {
            assertThat(OpenMath.lcm(4, 6)).isEqualTo(12);
            assertThat(OpenMath.lcm(3, 5)).isEqualTo(15);
            assertThat(OpenMath.lcm(0, 5)).isEqualTo(0);
        }

        @Test
        @DisplayName("lcm 最小公倍数 long")
        void testLcmLong() {
            assertThat(OpenMath.lcm(4L, 6L)).isEqualTo(12L);
        }

        @Test
        @DisplayName("factorial 阶乘")
        void testFactorial() {
            assertThat(OpenMath.factorial(0)).isEqualTo(1);
            assertThat(OpenMath.factorial(1)).isEqualTo(1);
            assertThat(OpenMath.factorial(5)).isEqualTo(120);
            assertThat(OpenMath.factorial(10)).isEqualTo(3628800);
        }

        @Test
        @DisplayName("factorial 负数抛异常")
        void testFactorialNegative() {
            assertThatThrownBy(() -> OpenMath.factorial(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("factorial 溢出抛异常")
        void testFactorialOverflow() {
            assertThatThrownBy(() -> OpenMath.factorial(21))
                    .isInstanceOf(ArithmeticException.class);
        }

        @Test
        @DisplayName("factorialBig 大数阶乘")
        void testFactorialBig() {
            assertThat(OpenMath.factorialBig(0)).isEqualTo(BigInteger.ONE);
            assertThat(OpenMath.factorialBig(5)).isEqualTo(new BigInteger("120"));
            assertThat(OpenMath.factorialBig(25)).isEqualTo(new BigInteger("15511210043330985984000000"));
        }

        @Test
        @DisplayName("isPrime 素数判断")
        void testIsPrime() {
            assertThat(OpenMath.isPrime(2)).isTrue();
            assertThat(OpenMath.isPrime(3)).isTrue();
            assertThat(OpenMath.isPrime(17)).isTrue();
            assertThat(OpenMath.isPrime(97)).isTrue();
            assertThat(OpenMath.isPrime(1)).isFalse();
            assertThat(OpenMath.isPrime(0)).isFalse();
            assertThat(OpenMath.isPrime(4)).isFalse();
            assertThat(OpenMath.isPrime(100)).isFalse();
        }

        @Test
        @DisplayName("fibonacci 斐波那契")
        void testFibonacci() {
            assertThat(OpenMath.fibonacci(0)).isEqualTo(0);
            assertThat(OpenMath.fibonacci(1)).isEqualTo(1);
            assertThat(OpenMath.fibonacci(2)).isEqualTo(1);
            assertThat(OpenMath.fibonacci(10)).isEqualTo(55);
            assertThat(OpenMath.fibonacci(20)).isEqualTo(6765);
        }

        @Test
        @DisplayName("fibonacci 负数抛异常")
        void testFibonacciNegative() {
            assertThatThrownBy(() -> OpenMath.fibonacci(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("pow 幂运算")
        void testPow() {
            assertThat(OpenMath.pow(2, 10)).isEqualTo(1024);
            assertThat(OpenMath.pow(3, 4)).isEqualTo(81);
            assertThat(OpenMath.pow(5, 0)).isEqualTo(1);
        }

        @Test
        @DisplayName("pow 负指数抛异常")
        void testPowNegativeExponent() {
            assertThatThrownBy(() -> OpenMath.pow(2, -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("modPow 模幂运算")
        void testModPow() {
            assertThat(OpenMath.modPow(2, 10, 1000)).isEqualTo(24);
            assertThat(OpenMath.modPow(3, 4, 10)).isEqualTo(1);
        }

        @Test
        @DisplayName("modPow 非正模数抛异常")
        void testModPowInvalidModulus() {
            assertThatThrownBy(() -> OpenMath.modPow(2, 10, 0))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> OpenMath.modPow(2, 10, -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("其他数学函数测试")
    class OtherMathTests {

        @Test
        @DisplayName("abs int")
        void testAbsInt() {
            assertThat(OpenMath.abs(5)).isEqualTo(5);
            assertThat(OpenMath.abs(-5)).isEqualTo(5);
            assertThat(OpenMath.abs(0)).isEqualTo(0);
        }

        @Test
        @DisplayName("abs long")
        void testAbsLong() {
            assertThat(OpenMath.abs(5L)).isEqualTo(5L);
            assertThat(OpenMath.abs(-5L)).isEqualTo(5L);
        }

        @Test
        @DisplayName("signum int")
        void testSignumInt() {
            assertThat(OpenMath.signum(5)).isEqualTo(1);
            assertThat(OpenMath.signum(-5)).isEqualTo(-1);
            assertThat(OpenMath.signum(0)).isEqualTo(0);
        }

        @Test
        @DisplayName("signum long")
        void testSignumLong() {
            assertThat(OpenMath.signum(5L)).isEqualTo(1);
            assertThat(OpenMath.signum(-5L)).isEqualTo(-1);
        }

        @Test
        @DisplayName("isEven")
        void testIsEven() {
            assertThat(OpenMath.isEven(0)).isTrue();
            assertThat(OpenMath.isEven(2)).isTrue();
            assertThat(OpenMath.isEven(-2)).isTrue();
            assertThat(OpenMath.isEven(1)).isFalse();
            assertThat(OpenMath.isEven(-1)).isFalse();
        }

        @Test
        @DisplayName("isOdd")
        void testIsOdd() {
            assertThat(OpenMath.isOdd(1)).isTrue();
            assertThat(OpenMath.isOdd(-1)).isTrue();
            assertThat(OpenMath.isOdd(3)).isTrue();
            assertThat(OpenMath.isOdd(0)).isFalse();
            assertThat(OpenMath.isOdd(2)).isFalse();
        }

        @Test
        @DisplayName("isNegative")
        void testIsNegative() {
            assertThat(OpenMath.isNegative(-1)).isTrue();
            assertThat(OpenMath.isNegative(0)).isFalse();
            assertThat(OpenMath.isNegative(1)).isFalse();
        }

        @Test
        @DisplayName("isPositive")
        void testIsPositive() {
            assertThat(OpenMath.isPositive(1)).isTrue();
            assertThat(OpenMath.isPositive(0)).isFalse();
            assertThat(OpenMath.isPositive(-1)).isFalse();
        }
    }
}
