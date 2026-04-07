package cloud.opencode.base.math.combinatorics;

import cloud.opencode.base.math.exception.MathException;

import java.math.BigInteger;

/**
 * Combinatorial mathematics utility methods.
 * 组合数学工具方法集合
 *
 * <p>Provides binomial coefficients, permutations, Catalan numbers,
 * Stirling numbers of the second kind, Bell numbers, and derangements.
 * All methods are stateless and thread-safe.</p>
 * <p>提供二项式系数、排列数、Catalan 数、第二类 Stirling 数、Bell 数和错排数。
 * 所有方法无状态且线程安全。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-math V1.0.3
 */
public final class Combinatorics {

    private Combinatorics() {
        throw new AssertionError("No instances");
    }

    /**
     * Computes the binomial coefficient C(n, k) = n! / (k! * (n-k)!).
     * 计算二项式系数 C(n, k) = n! / (k! * (n-k)!)
     *
     * <p>Uses the multiplicative formula to avoid intermediate overflow where possible.
     * Throws {@link MathException} if the result overflows {@code long}.</p>
     * <p>使用乘法公式尽量避免中间溢出。如果结果超出 {@code long} 范围则抛出 {@link MathException}。</p>
     *
     * @param n total elements, must be &ge; 0 / 总元素数，必须 &ge; 0
     * @param k chosen elements, must satisfy 0 &le; k &le; n / 选择元素数，必须满足 0 &le; k &le; n
     * @return C(n, k) / 二项式系数
     * @throws IllegalArgumentException if n &lt; 0, k &lt; 0, or k &gt; n
     * @throws MathException            if result overflows long / 如果结果溢出 long
     */
    public static long binomial(int n, int k) {
        validateBinomialArgs(n, k);
        if (k == 0 || k == n) {
            return 1L;
        }
        // Exploit symmetry: C(n, k) = C(n, n-k)
        int kk = Math.min(k, n - k);
        long result = 1L;
        for (int i = 0; i < kk; i++) {
            // result = result * (n - i) / (i + 1)
            // Divide first when possible to keep numbers smaller
            try {
                result = Math.multiplyExact(result, n - i);
                result = result / (i + 1);
            } catch (ArithmeticException e) {
                throw new MathException("Binomial coefficient C(" + n + ", " + k + ") overflows long", e);
            }
        }
        return result;
    }

    /**
     * Computes the binomial coefficient C(n, k) with arbitrary precision.
     * 使用任意精度计算二项式系数 C(n, k)
     *
     * @param n total elements, must be &ge; 0 / 总元素数，必须 &ge; 0
     * @param k chosen elements, must satisfy 0 &le; k &le; n / 选择元素数，必须满足 0 &le; k &le; n
     * @return C(n, k) as BigInteger / BigInteger 形式的二项式系数
     * @throws IllegalArgumentException if n &lt; 0, k &lt; 0, or k &gt; n
     */
    public static BigInteger binomialBig(int n, int k) {
        validateBinomialArgs(n, k);
        if (k == 0 || k == n) {
            return BigInteger.ONE;
        }
        int kk = Math.min(k, n - k);
        BigInteger result = BigInteger.ONE;
        for (int i = 0; i < kk; i++) {
            result = result.multiply(BigInteger.valueOf(n - i))
                    .divide(BigInteger.valueOf(i + 1));
        }
        return result;
    }

    /**
     * Computes the permutation P(n, k) = n! / (n-k)!.
     * 计算排列数 P(n, k) = n! / (n-k)!
     *
     * @param n total elements, must be &ge; 0 / 总元素数，必须 &ge; 0
     * @param k chosen elements, must satisfy 0 &le; k &le; n / 选择元素数，必须满足 0 &le; k &le; n
     * @return P(n, k) / 排列数
     * @throws IllegalArgumentException if n &lt; 0, k &lt; 0, or k &gt; n
     * @throws MathException            if result overflows long / 如果结果溢出 long
     */
    public static long permutation(int n, int k) {
        validateBinomialArgs(n, k);
        if (k == 0) {
            return 1L;
        }
        long result = 1L;
        try {
            for (int i = 0; i < k; i++) {
                result = Math.multiplyExact(result, n - i);
            }
        } catch (ArithmeticException e) {
            throw new MathException("Permutation P(" + n + ", " + k + ") overflows long", e);
        }
        return result;
    }

    /**
     * Computes the permutation P(n, k) with arbitrary precision.
     * 使用任意精度计算排列数 P(n, k)
     *
     * @param n total elements, must be &ge; 0 / 总元素数，必须 &ge; 0
     * @param k chosen elements, must satisfy 0 &le; k &le; n / 选择元素数，必须满足 0 &le; k &le; n
     * @return P(n, k) as BigInteger / BigInteger 形式的排列数
     * @throws IllegalArgumentException if n &lt; 0, k &lt; 0, or k &gt; n
     */
    public static BigInteger permutationBig(int n, int k) {
        validateBinomialArgs(n, k);
        if (k == 0) {
            return BigInteger.ONE;
        }
        BigInteger result = BigInteger.ONE;
        for (int i = 0; i < k; i++) {
            result = result.multiply(BigInteger.valueOf(n - i));
        }
        return result;
    }

    /**
     * Computes the n-th Catalan number: C(2n, n) / (n + 1).
     * 计算第 n 个 Catalan 数：C(2n, n) / (n + 1)
     *
     * @param n the index, must be &ge; 0 / 索引，必须 &ge; 0
     * @return the n-th Catalan number / 第 n 个 Catalan 数
     * @throws IllegalArgumentException if n &lt; 0
     * @throws MathException            if result overflows long / 如果结果溢出 long
     */
    public static long catalanNumber(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n must be >= 0, got: " + n);
        }
        if (n == 0) {
            return 1L;
        }
        // C(2n, n) / (n+1) = product formula to avoid overflow as long as possible
        // Use BigInteger for intermediate if needed, but try long first
        try {
            long binom = binomial(Math.multiplyExact(2, n), n);
            return binom / (n + 1);
        } catch (MathException e) {
            // Overflow — delegate to big version and check
            BigInteger big = catalanBig(n);
            if (big.bitLength() > 62) {
                throw new MathException("Catalan number C(" + n + ") overflows long", e);
            }
            return big.longValueExact();
        }
    }

    /**
     * Computes the n-th Catalan number with arbitrary precision.
     * 使用任意精度计算第 n 个 Catalan 数
     *
     * @param n the index, must be &ge; 0 / 索引，必须 &ge; 0
     * @return the n-th Catalan number as BigInteger / BigInteger 形式的 Catalan 数
     * @throws IllegalArgumentException if n &lt; 0
     */
    public static BigInteger catalanBig(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n must be >= 0, got: " + n);
        }
        // Use long to avoid int overflow of 2*n
        long twoN = 2L * n;
        if (twoN > Integer.MAX_VALUE) {
            throw new MathException("Catalan number C(" + n + ") requires 2*n=" + twoN
                    + " which exceeds int range");
        }
        return binomialBig((int) twoN, n).divide(BigInteger.valueOf(n + 1));
    }

    /**
     * Computes the Stirling number of the second kind S(n, k).
     * 计算第二类 Stirling 数 S(n, k)
     *
     * <p>Uses the recurrence S(n, k) = k * S(n-1, k) + S(n-1, k-1),
     * with base cases S(n, 0) = 0 (n &gt; 0), S(0, 0) = 1, S(n, n) = 1.</p>
     * <p>使用递推关系 S(n, k) = k * S(n-1, k) + S(n-1, k-1)，
     * 边界条件 S(n, 0) = 0 (n &gt; 0)，S(0, 0) = 1，S(n, n) = 1。</p>
     *
     * @param n number of elements, must be &ge; 0 / 元素数，必须 &ge; 0
     * @param k number of non-empty subsets, must satisfy 0 &le; k &le; n / 非空子集数，必须满足 0 &le; k &le; n
     * @return S(n, k) / 第二类 Stirling 数
     * @throws IllegalArgumentException if n &lt; 0, k &lt; 0, or k &gt; n
     */
    public static long stirlingSecond(int n, int k) {
        validateBinomialArgs(n, k);
        if (k == 0) {
            return n == 0 ? 1L : 0L;
        }
        if (k == n || k == 1) {
            return 1L;
        }

        // DP with two pre-allocated rows (swap instead of allocating per iteration)
        long[] prev = new long[k + 1];
        long[] curr = new long[k + 1];
        // Row 0: S(0,0) = 1
        prev[0] = 1;

        for (int i = 1; i <= n; i++) {
            java.util.Arrays.fill(curr, 0);
            // S(i, 0) = 0 (already zero)
            for (int j = 1; j <= Math.min(i, k); j++) {
                if (j == i) {
                    curr[j] = 1;
                } else {
                    // S(i, j) = j * S(i-1, j) + S(i-1, j-1)
                    curr[j] = Math.addExact(Math.multiplyExact(j, prev[j]), prev[j - 1]);
                }
            }
            long[] tmp = prev;
            prev = curr;
            curr = tmp;
        }
        return prev[k];
    }

    /**
     * Computes the n-th Bell number (sum of Stirling numbers of the second kind).
     * 计算第 n 个 Bell 数（第二类 Stirling 数之和）
     *
     * <p>B(n) = sum_{k=0}^{n} S(n, k).</p>
     *
     * @param n the index, must be &ge; 0 / 索引，必须 &ge; 0
     * @return the n-th Bell number / 第 n 个 Bell 数
     * @throws IllegalArgumentException if n &lt; 0
     * @throws MathException            if result overflows long / 如果结果溢出 long
     */
    public static long bellNumber(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n must be >= 0, got: " + n);
        }
        if (n == 0) {
            return 1L;
        }
        // Use the Bell triangle for efficiency
        // Row i: B[i][0] = B[i-1][i-1], B[i][j] = B[i][j-1] + B[i-1][j-1]
        long[] prev = new long[]{1}; // Row 0: {1}
        try {
            for (int i = 1; i <= n; i++) {
                long[] curr = new long[i + 1];
                curr[0] = prev[i - 1]; // left edge = last element of previous row
                for (int j = 1; j <= i; j++) {
                    curr[j] = Math.addExact(curr[j - 1], prev[j - 1]);
                }
                prev = curr;
            }
        } catch (ArithmeticException e) {
            throw new MathException("Bell number B(" + n + ") overflows long", e);
        }
        return prev[0];
    }

    /**
     * Computes the number of derangements (subfactorial) !n.
     * 计算错排数（子阶乘）!n
     *
     * <p>A derangement is a permutation where no element appears in its original position.
     * Uses the recurrence !n = (n-1) * (!(n-1) + !(n-2)).</p>
     * <p>错排是指没有元素出现在其原始位置的排列。
     * 使用递推关系 !n = (n-1) * (!(n-1) + !(n-2))。</p>
     *
     * @param n the number of elements, must be &ge; 0 / 元素数量，必须 &ge; 0
     * @return !n (the number of derangements) / 错排数
     * @throws IllegalArgumentException if n &lt; 0
     * @throws MathException            if result overflows long / 如果结果溢出 long
     */
    public static long derangements(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n must be >= 0, got: " + n);
        }
        if (n == 0) {
            return 1L; // !0 = 1 by convention
        }
        if (n == 1) {
            return 0L; // !1 = 0
        }

        long prev2 = 1L; // !0
        long prev1 = 0L; // !1
        try {
            for (int i = 2; i <= n; i++) {
                long current = Math.multiplyExact(i - 1, Math.addExact(prev1, prev2));
                prev2 = prev1;
                prev1 = current;
            }
        } catch (ArithmeticException e) {
            throw new MathException("Derangement !(" + n + ") overflows long", e);
        }
        return prev1;
    }

    private static void validateBinomialArgs(int n, int k) {
        if (n < 0) {
            throw new IllegalArgumentException("n must be >= 0, got: " + n);
        }
        if (k < 0) {
            throw new IllegalArgumentException("k must be >= 0, got: " + k);
        }
        if (k > n) {
            throw new IllegalArgumentException("k must be <= n, got k=" + k + ", n=" + n);
        }
    }
}
