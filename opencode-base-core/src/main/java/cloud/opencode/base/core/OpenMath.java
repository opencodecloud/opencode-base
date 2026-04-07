package cloud.opencode.base.core;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/**
 * Math Utility Class - High-precision arithmetic, statistics and number theory functions
 * 数学工具类 - 高精度算术运算、统计函数和数论函数
 *
 * <p>Provides high-performance, high-precision mathematical calculations.</p>
 * <p>提供高性能、高精度的数学计算，包括算术运算、统计函数、数论函数。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>High-precision arithmetic (add, subtract, multiply, divide) - 高精度运算</li>
 *   <li>Rounding (round, ceil, floor) - 取整</li>
 *   <li>Statistics (mean, median, variance, stdDev, sum) - 统计函数</li>
 *   <li>Number theory (gcd, lcm, factorial, isPrime, fibonacci) - 数论函数</li>
 *   <li>Power operations (pow, modPow) - 幂运算</li>
 *   <li>Utility (abs, signum, isEven, isOdd) - 工具方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // High-precision division - 高精度除法
 * BigDecimal result = OpenMath.divide(a, b, 2);
 *
 * // Statistics - 统计
 * double avg = OpenMath.mean(1.0, 2.0, 3.0);
 * double mid = OpenMath.median(1.0, 2.0, 3.0);
 *
 * // Number theory - 数论
 * int gcd = OpenMath.gcd(12, 18);         // 6
 * boolean prime = OpenMath.isPrime(17);    // true
 * long fib = OpenMath.fibonacci(10);       // 55
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是 (无状态)</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public final class OpenMath {

    private OpenMath() {
    }

    // ==================== 基本运算 ====================

    /**
     * High-precision addition of two BigDecimal values.
     * 高精度加法
     */
    public static BigDecimal add(BigDecimal a, BigDecimal b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.add(b);
    }

    /**
     * High-precision subtraction of two BigDecimal values.
     * 高精度减法
     */
    public static BigDecimal subtract(BigDecimal a, BigDecimal b) {
        if (a == null) a = BigDecimal.ZERO;
        if (b == null) b = BigDecimal.ZERO;
        return a.subtract(b);
    }

    /**
     * High-precision multiplication of two BigDecimal values.
     * 高精度乘法
     */
    public static BigDecimal multiply(BigDecimal a, BigDecimal b) {
        if (a == null || b == null) return BigDecimal.ZERO;
        return a.multiply(b);
    }

    /**
     * High-precision division with half-up rounding.
     * 高精度除法
     */
    public static BigDecimal divide(BigDecimal a, BigDecimal b, int scale) {
        return divide(a, b, scale, RoundingMode.HALF_UP);
    }

    /**
     * High-precision division with specified rounding mode.
     * 高精度除法（指定舍入模式）
     */
    public static BigDecimal divide(BigDecimal a, BigDecimal b, int scale, RoundingMode mode) {
        if (a == null) return BigDecimal.ZERO;
        if (b == null || b.compareTo(BigDecimal.ZERO) == 0) {
            throw new ArithmeticException("Division by zero");
        }
        return a.divide(b, scale, mode);
    }

    // ==================== 四舍五入 ====================

    /**
     * Rounds a double value to the specified number of decimal places using half-up rounding.
     * 四舍五入
     */
    public static double round(double value, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("Scale must not be negative");
        }
        BigDecimal bd = BigDecimal.valueOf(value);
        return bd.setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * Rounds a double value up to the specified number of decimal places.
     * 向上取整
     */
    public static double ceil(double value, int scale) {
        BigDecimal bd = BigDecimal.valueOf(value);
        return bd.setScale(scale, RoundingMode.CEILING).doubleValue();
    }

    /**
     * Rounds a double value down to the specified number of decimal places.
     * 向下取整
     */
    public static double floor(double value, int scale) {
        BigDecimal bd = BigDecimal.valueOf(value);
        return bd.setScale(scale, RoundingMode.FLOOR).doubleValue();
    }

    // ==================== 统计函数 ====================

    /**
     * Calculates the arithmetic mean of the given values.
     * 计算平均值
     */
    public static double mean(double... values) {
        if (values == null || values.length == 0) {
            return 0;
        }
        double sum = 0;
        for (double value : values) {
            sum += value;
        }
        return sum / values.length;
    }

    /**
     * Calculates the median of the given values.
     * 计算中位数
     */
    public static double median(double... values) {
        if (values == null || values.length == 0) {
            return 0;
        }
        double[] sorted = values.clone();
        java.util.Arrays.sort(sorted);
        int len = sorted.length;
        if (len % 2 == 0) {
            return (sorted[len / 2 - 1] + sorted[len / 2]) / 2.0;
        }
        return sorted[len / 2];
    }

    /**
     * Calculates the population variance of the given values (divides by n).
     * 计算总体方差（除以 n）
     *
     * <p>This computes the population variance, which divides the sum of squared deviations
     * by the number of values (n). Use {@link #sampleVariance(double...)} for sample variance
     * with Bessel's correction (divides by n-1).</p>
     * <p>此方法计算总体方差，以数据个数 n 为分母。如需样本方差（贝塞尔校正，以 n-1 为分母），
     * 请使用 {@link #sampleVariance(double...)}。</p>
     *
     * @param values the values | 数值数组
     * @return the population variance, or 0 if the array is null or empty
     *         总体方差，若数组为 null 或空则返回 0
     */
    public static double variance(double... values) {
        if (values == null || values.length == 0) {
            return 0;
        }
        double mean = mean(values);
        double sumSquares = 0;
        for (double value : values) {
            double diff = value - mean;
            sumSquares += diff * diff;
        }
        return sumSquares / values.length;
    }

    /**
     * Calculates the sample variance of the given values using Bessel's correction (divides by n-1).
     * 计算样本方差（贝塞尔校正，除以 n-1）
     *
     * <p>This computes the unbiased sample variance, which divides the sum of squared deviations
     * by (n-1) instead of n. This is the appropriate estimator when computing variance from a
     * sample rather than an entire population.</p>
     * <p>此方法计算无偏样本方差，以 (n-1) 为分母而非 n。当数据为样本而非总体时，
     * 应使用此方法以获得无偏估计。</p>
     *
     * @param values the values (must contain at least 2 elements) | 数值数组（至少需要 2 个元素）
     * @return the sample variance, or 0 if the array is null or empty;
     *         returns 0 for a single-element array (no variance)
     *         样本方差；若数组为 null 或空返回 0；单元素数组返回 0
     */
    public static double sampleVariance(double... values) {
        if (values == null || values.length <= 1) {
            return 0;
        }
        double mean = mean(values);
        double sumSquares = 0;
        for (double value : values) {
            double diff = value - mean;
            sumSquares += diff * diff;
        }
        return sumSquares / (values.length - 1);
    }

    /**
     * Calculates the population standard deviation of the given values (uses population variance).
     * 计算总体标准差（基于总体方差）
     *
     * <p>This is the square root of the {@linkplain #variance(double...) population variance}.
     * Use {@link #sampleStdDev(double...)} for the sample standard deviation.</p>
     * <p>此方法为 {@linkplain #variance(double...) 总体方差} 的平方根。
     * 如需样本标准差，请使用 {@link #sampleStdDev(double...)}。</p>
     *
     * @param values the values | 数值数组
     * @return the population standard deviation | 总体标准差
     */
    public static double stdDev(double... values) {
        return Math.sqrt(variance(values));
    }

    /**
     * Calculates the sample standard deviation of the given values (uses sample variance with Bessel's correction).
     * 计算样本标准差（基于样本方差，贝塞尔校正）
     *
     * <p>This is the square root of the {@linkplain #sampleVariance(double...) sample variance}.</p>
     * <p>此方法为 {@linkplain #sampleVariance(double...) 样本方差} 的平方根。</p>
     *
     * @param values the values (must contain at least 2 elements) | 数值数组（至少需要 2 个元素）
     * @return the sample standard deviation | 样本标准差
     */
    public static double sampleStdDev(double... values) {
        return Math.sqrt(sampleVariance(values));
    }

    /**
     * Calculates the sum of the given double values.
     * 计算总和
     */
    public static double sum(double... values) {
        if (values == null || values.length == 0) {
            return 0.0;
        }
        double sum = 0.0;
        for (double value : values) {
            sum += value;
        }
        return sum;
    }

    /**
     * Calculates the sum of the given long values using overflow-safe addition.
     * 计算总和（long）
     */
    public static long sum(long... values) {
        if (values == null || values.length == 0) {
            return 0;
        }
        long sum = 0;
        for (long value : values) {
            sum = Math.addExact(sum, value);
        }
        return sum;
    }

    /**
     * Calculates the sum of the given int values using overflow-safe addition.
     * 计算总和（int）
     */
    public static int sum(int... values) {
        if (values == null || values.length == 0) {
            return 0;
        }
        int sum = 0;
        for (int value : values) {
            sum = Math.addExact(sum, value);
        }
        return sum;
    }

    // ==================== 数论函数 ====================

    /**
     * Greatest Common Divisor (GCD)
     * 最大公约数（GCD）
     * Safe: throws ArithmeticException for Integer.MIN_VALUE inputs
     */
    public static int gcd(int a, int b) {
        a = Math.absExact(a);
        b = Math.absExact(b);
        while (b != 0) {
            int temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }

    /**
     * Greatest Common Divisor (long)
     * 最大公约数（long）
     * Safe: throws ArithmeticException for Long.MIN_VALUE inputs
     */
    public static long gcd(long a, long b) {
        a = Math.absExact(a);
        b = Math.absExact(b);
        while (b != 0) {
            long temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }

    /**
     * Least Common Multiple (LCM)
     * 最小公倍数（LCM）
     * Safe: uses Math.absExact to detect overflow
     */
    public static int lcm(int a, int b) {
        if (a == 0 || b == 0) return 0;
        return Math.absExact(Math.multiplyExact(a / gcd(a, b), b));
    }

    /**
     * Least Common Multiple (long)
     * 最小公倍数（long）
     * Safe: uses Math.absExact to detect overflow
     */
    public static long lcm(long a, long b) {
        if (a == 0 || b == 0) return 0;
        return Math.absExact(Math.multiplyExact(a / gcd(a, b), b));
    }

    /**
     * Calculates the factorial of n (n must be in [0, 20]).
     * 阶乘
     */
    public static long factorial(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n must not be negative");
        }
        if (n > 20) {
            throw new ArithmeticException("Factorial overflow for n > 20");
        }
        long result = 1;
        for (int i = 2; i <= n; i++) {
            result *= i;
        }
        return result;
    }

    /**
     * Calculates the factorial of n as a BigInteger (arbitrary precision).
     * 阶乘（大数）
     */
    public static BigInteger factorialBig(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n must not be negative");
        }
        BigInteger result = BigInteger.ONE;
        for (int i = 2; i <= n; i++) {
            result = result.multiply(BigInteger.valueOf(i));
        }
        return result;
    }

    /**
     * Returns true if n is a prime number.
     * 素数判断
     */
    public static boolean isPrime(long n) {
        if (n < 2) return false;
        if (n == 2) return true;
        if (n % 2 == 0) return false;
        for (long i = 3; i <= n / i; i += 2) {
            if (n % i == 0) return false;
        }
        return true;
    }

    /**
     * Returns the n-th Fibonacci number (0-indexed: F(0)=0, F(1)=1, F(2)=1, ...).
     * 返回第 n 个斐波那契数（从 0 开始：F(0)=0, F(1)=1, F(2)=1, ...）。
     *
     * <p>Valid range: 0 &le; n &le; 92. For n &gt; 92, the result overflows {@code long}.</p>
     * <p>有效范围：0 &le; n &le; 92。当 n &gt; 92 时，结果溢出 {@code long}。</p>
     *
     * @param n the index (must be in [0, 92]) | 索引（必须在 [0, 92] 范围内）
     * @return the n-th Fibonacci number | 第 n 个斐波那契数
     * @throws IllegalArgumentException if n is negative | 如果 n 为负数
     * @throws ArithmeticException if n &gt; 92 (result overflows long) | 如果 n &gt; 92（结果溢出 long）
     */
    public static long fibonacci(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n must not be negative");
        }
        if (n > 92) {
            throw new ArithmeticException("Fibonacci(n) overflows long for n > 92");
        }
        if (n <= 1) return n;
        long prev = 0, curr = 1;
        for (int i = 2; i <= n; i++) {
            long next = Math.addExact(prev, curr);
            prev = curr;
            curr = next;
        }
        return curr;
    }

    /**
     * Computes base raised to the power of exponent using overflow-safe multiplication.
     * 幂运算
     */
    public static long pow(long base, int exponent) {
        if (exponent < 0) {
            throw new IllegalArgumentException("Exponent must not be negative");
        }
        long result = 1;
        while (exponent > 0) {
            if ((exponent & 1) == 1) {
                result = Math.multiplyExact(result, base);
            }
            if (exponent > 1) {
                base = Math.multiplyExact(base, base);
            }
            exponent >>= 1;
        }
        return result;
    }

    /**
     * Computes (base ^ exponent) mod modulus using BigInteger.
     * 模幂运算
     */
    public static long modPow(long base, long exponent, long modulus) {
        if (modulus <= 0) {
            throw new IllegalArgumentException("Modulus must be positive");
        }
        java.math.BigInteger bBase = java.math.BigInteger.valueOf(base);
        java.math.BigInteger bExp = java.math.BigInteger.valueOf(exponent);
        java.math.BigInteger bMod = java.math.BigInteger.valueOf(modulus);
        return bBase.modPow(bExp, bMod).longValueExact();
    }

    // ==================== 其他数学函数 ====================

    /**
     * Absolute value (safe: throws on Integer.MIN_VALUE instead of returning negative)
     * 绝对值 (safe: throws on Integer.MIN_VALUE instead of returning negative)
     * 绝对值 (安全：对 Integer.MIN_VALUE 抛出异常而非返回负数)
     */
    public static int abs(int value) {
        return Math.absExact(value);
    }

    /**
     * Absolute value (long, safe: throws on Long.MIN_VALUE instead of returning negative)
     * 绝对值（long, safe: throws on Long.MIN_VALUE instead of returning negative）
     * 绝对值（long，安全：对 Long.MIN_VALUE 抛出异常而非返回负数）
     */
    public static long abs(long value) {
        return Math.absExact(value);
    }

    /**
     * Returns the signum of the value: -1, 0, or 1.
     * 符号函数
     */
    public static int signum(int value) {
        return Integer.signum(value);
    }

    /**
     * Returns the signum of the long value: -1, 0, or 1.
     * 符号函数（long）
     */
    public static int signum(long value) {
        return Long.signum(value);
    }

    /**
     * Returns true if the value is even.
     * 是否为偶数
     */
    public static boolean isEven(int value) {
        return (value & 1) == 0;
    }

    /**
     * Returns true if the value is odd.
     * 是否为奇数
     */
    public static boolean isOdd(int value) {
        return (value & 1) == 1;
    }

    /**
     * Returns true if the value is negative.
     * 是否为负数
     */
    public static boolean isNegative(int value) {
        return value < 0;
    }

    /**
     * Returns true if the value is positive.
     * 是否为正数
     */
    public static boolean isPositive(int value) {
        return value > 0;
    }
}
