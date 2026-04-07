package cloud.opencode.base.math.combinatorics;

import cloud.opencode.base.math.exception.MathException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link Combinatorics}.
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-math V1.0.3
 */
@DisplayName("Combinatorics 组合数学工具类测试")
class CombinatoricsTest {

    @Nested
    @DisplayName("binomial - 二项式系数")
    class BinomialTest {

        @Test
        @DisplayName("已知值: C(10,3)=120")
        void knownValueC10_3() {
            assertThat(Combinatorics.binomial(10, 3)).isEqualTo(120L);
        }

        @Test
        @DisplayName("已知值: C(20,10)=184756")
        void knownValueC20_10() {
            assertThat(Combinatorics.binomial(20, 10)).isEqualTo(184756L);
        }

        @Test
        @DisplayName("边界值: C(0,0)=1")
        void boundaryC0_0() {
            assertThat(Combinatorics.binomial(0, 0)).isEqualTo(1L);
        }

        @Test
        @DisplayName("边界值: C(n,0)=1 和 C(n,n)=1")
        void boundaryEdgeCases() {
            assertThat(Combinatorics.binomial(10, 0)).isEqualTo(1L);
            assertThat(Combinatorics.binomial(10, 10)).isEqualTo(1L);
        }

        @Test
        @DisplayName("对称性: C(n,k) = C(n,n-k)")
        void symmetry() {
            assertThat(Combinatorics.binomial(15, 4)).isEqualTo(Combinatorics.binomial(15, 11));
        }

        @Test
        @DisplayName("溢出检测: C(68,34) 超出 long 范围")
        void overflowDetection() {
            assertThatThrownBy(() -> Combinatorics.binomial(68, 34))
                    .isInstanceOf(MathException.class)
                    .hasMessageContaining("overflows");
        }

        @Test
        @DisplayName("参数校验: n<0 抛出异常")
        void negativeN() {
            assertThatThrownBy(() -> Combinatorics.binomial(-1, 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("参数校验: k>n 抛出异常")
        void kGreaterThanN() {
            assertThatThrownBy(() -> Combinatorics.binomial(3, 5))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("参数校验: k<0 抛出异常")
        void negativeK() {
            assertThatThrownBy(() -> Combinatorics.binomial(5, -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("binomialBig - 大整数二项式系数")
    class BinomialBigTest {

        @Test
        @DisplayName("已知值与 long 版本一致")
        void matchesLongVersion() {
            assertThat(Combinatorics.binomialBig(10, 3)).isEqualTo(BigInteger.valueOf(120));
            assertThat(Combinatorics.binomialBig(20, 10)).isEqualTo(BigInteger.valueOf(184756));
        }

        @Test
        @DisplayName("大值: C(68,34) 不溢出")
        void largeValueNoOverflow() {
            BigInteger result = Combinatorics.binomialBig(68, 34);
            assertThat(result).isPositive();
            // C(68,34) is a known large number
            assertThat(result.bitLength()).isGreaterThan(62);
        }
    }

    @Nested
    @DisplayName("permutation - 排列数")
    class PermutationTest {

        @Test
        @DisplayName("已知值: P(5,3)=60")
        void knownValueP5_3() {
            assertThat(Combinatorics.permutation(5, 3)).isEqualTo(60L);
        }

        @Test
        @DisplayName("边界值: P(n,0)=1")
        void boundaryP_n_0() {
            assertThat(Combinatorics.permutation(10, 0)).isEqualTo(1L);
        }

        @Test
        @DisplayName("P(n,n)=n!")
        void fullPermutation() {
            // P(5,5) = 5! = 120
            assertThat(Combinatorics.permutation(5, 5)).isEqualTo(120L);
        }

        @Test
        @DisplayName("参数校验: k>n 抛出异常")
        void invalidK() {
            assertThatThrownBy(() -> Combinatorics.permutation(3, 5))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("permutationBig - 大整数排列数")
    class PermutationBigTest {

        @Test
        @DisplayName("与 long 版本一致")
        void matchesLongVersion() {
            assertThat(Combinatorics.permutationBig(5, 3)).isEqualTo(BigInteger.valueOf(60));
        }
    }

    @Nested
    @DisplayName("catalanNumber - Catalan 数")
    class CatalanTest {

        @Test
        @DisplayName("已知值: Catalan(5)=42")
        void knownValueCatalan5() {
            assertThat(Combinatorics.catalanNumber(5)).isEqualTo(42L);
        }

        @Test
        @DisplayName("边界值: Catalan(0)=1")
        void boundaryCatalan0() {
            assertThat(Combinatorics.catalanNumber(0)).isEqualTo(1L);
        }

        @Test
        @DisplayName("Catalan(1)=1")
        void catalan1() {
            assertThat(Combinatorics.catalanNumber(1)).isEqualTo(1L);
        }

        @Test
        @DisplayName("已知序列: 1,1,2,5,14,42,132")
        void catalanSequence() {
            long[] expected = {1, 1, 2, 5, 14, 42, 132};
            for (int i = 0; i < expected.length; i++) {
                assertThat(Combinatorics.catalanNumber(i)).isEqualTo(expected[i]);
            }
        }

        @Test
        @DisplayName("参数校验: n<0 抛出异常")
        void negativeN() {
            assertThatThrownBy(() -> Combinatorics.catalanNumber(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("stirlingSecond - 第二类 Stirling 数")
    class StirlingTest {

        @Test
        @DisplayName("已知值: S(5,3)=25")
        void knownValueS5_3() {
            assertThat(Combinatorics.stirlingSecond(5, 3)).isEqualTo(25L);
        }

        @Test
        @DisplayName("边界值: S(0,0)=1")
        void boundaryS0_0() {
            assertThat(Combinatorics.stirlingSecond(0, 0)).isEqualTo(1L);
        }

        @Test
        @DisplayName("S(n,0)=0 当 n>0")
        void sN0() {
            assertThat(Combinatorics.stirlingSecond(5, 0)).isEqualTo(0L);
        }

        @Test
        @DisplayName("S(n,1)=1")
        void sN1() {
            assertThat(Combinatorics.stirlingSecond(5, 1)).isEqualTo(1L);
        }

        @Test
        @DisplayName("S(n,n)=1")
        void sNN() {
            assertThat(Combinatorics.stirlingSecond(5, 5)).isEqualTo(1L);
        }

        @Test
        @DisplayName("已知值序列: S(4,k)")
        void stirlingRow4() {
            // S(4,0)=0, S(4,1)=1, S(4,2)=7, S(4,3)=6, S(4,4)=1
            assertThat(Combinatorics.stirlingSecond(4, 0)).isEqualTo(0L);
            assertThat(Combinatorics.stirlingSecond(4, 1)).isEqualTo(1L);
            assertThat(Combinatorics.stirlingSecond(4, 2)).isEqualTo(7L);
            assertThat(Combinatorics.stirlingSecond(4, 3)).isEqualTo(6L);
            assertThat(Combinatorics.stirlingSecond(4, 4)).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("bellNumber - Bell 数")
    class BellTest {

        @Test
        @DisplayName("已知值: B(5)=52")
        void knownValueB5() {
            assertThat(Combinatorics.bellNumber(5)).isEqualTo(52L);
        }

        @Test
        @DisplayName("边界值: B(0)=1")
        void boundaryB0() {
            assertThat(Combinatorics.bellNumber(0)).isEqualTo(1L);
        }

        @Test
        @DisplayName("已知序列: B(0..7) = 1,1,2,5,15,52,203,877")
        void bellSequence() {
            long[] expected = {1, 1, 2, 5, 15, 52, 203, 877};
            for (int i = 0; i < expected.length; i++) {
                assertThat(Combinatorics.bellNumber(i))
                        .as("B(%d)", i)
                        .isEqualTo(expected[i]);
            }
        }

        @Test
        @DisplayName("参数校验: n<0 抛出异常")
        void negativeN() {
            assertThatThrownBy(() -> Combinatorics.bellNumber(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("derangements - 错排数")
    class DerangementsTest {

        @Test
        @DisplayName("已知值: !5=44")
        void knownValueD5() {
            assertThat(Combinatorics.derangements(5)).isEqualTo(44L);
        }

        @Test
        @DisplayName("边界值: !0=1, !1=0")
        void boundaryValues() {
            assertThat(Combinatorics.derangements(0)).isEqualTo(1L);
            assertThat(Combinatorics.derangements(1)).isEqualTo(0L);
        }

        @Test
        @DisplayName("已知序列: !0..!6 = 1,0,1,2,9,44,265")
        void derangementSequence() {
            long[] expected = {1, 0, 1, 2, 9, 44, 265};
            for (int i = 0; i < expected.length; i++) {
                assertThat(Combinatorics.derangements(i))
                        .as("!%d", i)
                        .isEqualTo(expected[i]);
            }
        }

        @Test
        @DisplayName("参数校验: n<0 抛出异常")
        void negativeN() {
            assertThatThrownBy(() -> Combinatorics.derangements(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
