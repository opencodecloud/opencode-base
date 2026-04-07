package cloud.opencode.base.hash;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * HashCodes 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.3
 */
@DisplayName("HashCodes 测试")
class HashCodesTest {

    @Nested
    @DisplayName("combine方法测试")
    class CombineTest {

        @Test
        @DisplayName("combine(a,b) != combine(b,a) 顺序敏感")
        void testOrderMatters() {
            int ab = HashCodes.combine(1, 2);
            int ba = HashCodes.combine(2, 1);

            assertThat(ab).isNotEqualTo(ba);
        }

        @Test
        @DisplayName("不同输入产生不同结果")
        void testDifferentInputsDifferentResults() {
            int h1 = HashCodes.combine(1, 2);
            int h2 = HashCodes.combine(1, 3);
            int h3 = HashCodes.combine(2, 2);

            assertThat(h1).isNotEqualTo(h2);
            assertThat(h1).isNotEqualTo(h3);
            assertThat(h2).isNotEqualTo(h3);
        }

        @Test
        @DisplayName("combine(0,0)返回一致的值")
        void testCombineZeroZeroConsistent() {
            int h1 = HashCodes.combine(0, 0);
            int h2 = HashCodes.combine(0, 0);

            assertThat(h1).isEqualTo(h2);
        }

        @Test
        @DisplayName("2参数combine")
        void testCombine2() {
            int result = HashCodes.combine(10, 20);
            assertThat(result).isNotZero();
            assertThat(HashCodes.combine(10, 20)).isEqualTo(result);
        }

        @Test
        @DisplayName("3参数combine")
        void testCombine3() {
            int result = HashCodes.combine(1, 2, 3);
            assertThat(result).isNotZero();
            assertThat(HashCodes.combine(1, 2, 3)).isEqualTo(result);
            // Order matters
            assertThat(result).isNotEqualTo(HashCodes.combine(3, 2, 1));
        }

        @Test
        @DisplayName("4参数combine")
        void testCombine4() {
            int result = HashCodes.combine(1, 2, 3, 4);
            assertThat(HashCodes.combine(1, 2, 3, 4)).isEqualTo(result);
            assertThat(result).isNotEqualTo(HashCodes.combine(4, 3, 2, 1));
        }

        @Test
        @DisplayName("5参数combine")
        void testCombine5() {
            int result = HashCodes.combine(1, 2, 3, 4, 5);
            assertThat(HashCodes.combine(1, 2, 3, 4, 5)).isEqualTo(result);
            assertThat(result).isNotEqualTo(HashCodes.combine(5, 4, 3, 2, 1));
        }

        @Test
        @DisplayName("6参数combine")
        void testCombine6() {
            int result = HashCodes.combine(1, 2, 3, 4, 5, 6);
            assertThat(HashCodes.combine(1, 2, 3, 4, 5, 6)).isEqualTo(result);
            assertThat(result).isNotEqualTo(HashCodes.combine(6, 5, 4, 3, 2, 1));
        }

        @Test
        @DisplayName("7参数combine")
        void testCombine7() {
            int result = HashCodes.combine(1, 2, 3, 4, 5, 6, 7);
            assertThat(HashCodes.combine(1, 2, 3, 4, 5, 6, 7)).isEqualTo(result);
            assertThat(result).isNotEqualTo(HashCodes.combine(7, 6, 5, 4, 3, 2, 1));
        }

        @Test
        @DisplayName("8参数combine")
        void testCombine8() {
            int result = HashCodes.combine(1, 2, 3, 4, 5, 6, 7, 8);
            assertThat(HashCodes.combine(1, 2, 3, 4, 5, 6, 7, 8)).isEqualTo(result);
            assertThat(result).isNotEqualTo(HashCodes.combine(8, 7, 6, 5, 4, 3, 2, 1));
        }

        @Test
        @DisplayName("所有重载返回不同结果（不同参数数量）")
        void testDifferentAritiesProduceDifferentResults() {
            int h2 = HashCodes.combine(1, 2);
            int h3 = HashCodes.combine(1, 2, 3);
            int h4 = HashCodes.combine(1, 2, 3, 4);
            int h5 = HashCodes.combine(1, 2, 3, 4, 5);

            assertThat(h2).isNotEqualTo(h3);
            assertThat(h3).isNotEqualTo(h4);
            assertThat(h4).isNotEqualTo(h5);
        }
    }

    @Nested
    @DisplayName("Combiner测试")
    class CombinerTest {

        @Test
        @DisplayName("链式调用: start().add(1).add(2).result() == combine(1,2)")
        void testChainingMatchesCombine() {
            int combined = HashCodes.combine(1, 2);
            int chained = HashCodes.start().add(1).add(2).result();

            // start() uses SEED, then add(1) does mix(SEED ^ 1), add(2) does mix(prev ^ 2)
            // combine(1,2) does mix(mix(SEED ^ 1) ^ 2) -- same thing
            assertThat(chained).isEqualTo(combined);
        }

        @Test
        @DisplayName("add(long)拆分为两个int")
        void testAddLong() {
            long value = 0x1234567890ABCDEFL;
            int result = HashCodes.start().add(value).result();

            // Should be deterministic
            assertThat(HashCodes.start().add(value).result()).isEqualTo(result);

            // Different longs should produce different results
            assertThat(result).isNotEqualTo(HashCodes.start().add(value + 1).result());
        }

        @Test
        @DisplayName("add(boolean)")
        void testAddBoolean() {
            int trueHash = HashCodes.start().add(true).result();
            int falseHash = HashCodes.start().add(false).result();

            assertThat(trueHash).isNotEqualTo(falseHash);
        }

        @Test
        @DisplayName("add(Object)使用hashCode()")
        void testAddObject() {
            String s = "hello";
            int result = HashCodes.start().add((Object) s).result();

            // Same object should give same result
            assertThat(HashCodes.start().add((Object) "hello").result()).isEqualTo(result);

            // Different object should give different result
            assertThat(result).isNotEqualTo(HashCodes.start().add((Object) "world").result());
        }

        @Test
        @DisplayName("add(null)使用0")
        void testAddNull() {
            int nullHash = HashCodes.start().add((Object) null).result();
            int zeroHash = HashCodes.start().add(0).result();

            // null hashCode = 0, so add(null) should be same as add(int 0)
            assertThat(nullHash).isEqualTo(zeroHash);
        }

        @Test
        @DisplayName("start(initial)与start().add(initial)相同")
        void testStartWithInitial() {
            int withInitial = HashCodes.start(42).result();
            // start(42) does mix(SEED ^ 42) and returns that as result
            // start().add(42) does mix(SEED ^ 42) -- same thing
            int withAdd = HashCodes.start().add(42).result();

            assertThat(withInitial).isEqualTo(withAdd);
        }

        @Test
        @DisplayName("多步链式调用")
        void testMultiStepChaining() {
            int result = HashCodes.start()
                    .add(1)
                    .add(2L)
                    .add(true)
                    .add("test")
                    .add((Object) null)
                    .result();

            // Deterministic
            int result2 = HashCodes.start()
                    .add(1)
                    .add(2L)
                    .add(true)
                    .add("test")
                    .add((Object) null)
                    .result();

            assertThat(result).isEqualTo(result2);
        }
    }

    @Nested
    @DisplayName("雪崩效应测试")
    class AvalancheTest {

        @Test
        @DisplayName("单个位翻转导致显著变化（至少8位不同）")
        void testSingleBitFlipCausesSignificantChange() {
            int base = HashCodes.combine(0, 0);

            // Flip each bit in the first argument
            for (int bit = 0; bit < 32; bit++) {
                int flipped = HashCodes.combine(1 << bit, 0);
                int diff = base ^ flipped;
                int bitsChanged = Integer.bitCount(diff);

                assertThat(bitsChanged)
                        .as("Flipping bit %d should change at least 8 output bits, but changed %d", bit, bitsChanged)
                        .isGreaterThanOrEqualTo(8);
            }
        }

        @Test
        @DisplayName("单个位翻转在第二个参数导致显著变化")
        void testSingleBitFlipInSecondArg() {
            int base = HashCodes.combine(42, 0);

            for (int bit = 0; bit < 32; bit++) {
                int flipped = HashCodes.combine(42, 1 << bit);
                int diff = base ^ flipped;
                int bitsChanged = Integer.bitCount(diff);

                assertThat(bitsChanged)
                        .as("Flipping bit %d in second arg should change at least 8 output bits", bit)
                        .isGreaterThanOrEqualTo(8);
            }
        }
    }

    @Nested
    @DisplayName("一致性测试")
    class ConsistencyTest {

        @Test
        @DisplayName("相同输入始终产生相同输出")
        void testSameInputsSameOutput() {
            for (int i = 0; i < 100; i++) {
                int h1 = HashCodes.combine(i, i * 31);
                int h2 = HashCodes.combine(i, i * 31);
                assertThat(h1).isEqualTo(h2);
            }
        }

        @Test
        @DisplayName("Combiner多次调用一致性")
        void testCombinerConsistency() {
            for (int i = 0; i < 100; i++) {
                int h1 = HashCodes.start().add(i).add(i * 7).result();
                int h2 = HashCodes.start().add(i).add(i * 7).result();
                assertThat(h1).isEqualTo(h2);
            }
        }

        @Test
        @DisplayName("combine方法与等价Combiner链一致")
        void testCombineMatchesCombiner() {
            assertThat(HashCodes.combine(1, 2))
                    .isEqualTo(HashCodes.start().add(1).add(2).result());

            assertThat(HashCodes.combine(1, 2, 3))
                    .isEqualTo(HashCodes.start().add(1).add(2).add(3).result());

            assertThat(HashCodes.combine(1, 2, 3, 4))
                    .isEqualTo(HashCodes.start().add(1).add(2).add(3).add(4).result());

            assertThat(HashCodes.combine(1, 2, 3, 4, 5))
                    .isEqualTo(HashCodes.start().add(1).add(2).add(3).add(4).add(5).result());

            assertThat(HashCodes.combine(1, 2, 3, 4, 5, 6))
                    .isEqualTo(HashCodes.start().add(1).add(2).add(3).add(4).add(5).add(6).result());

            assertThat(HashCodes.combine(1, 2, 3, 4, 5, 6, 7))
                    .isEqualTo(HashCodes.start().add(1).add(2).add(3).add(4).add(5).add(6).add(7).result());

            assertThat(HashCodes.combine(1, 2, 3, 4, 5, 6, 7, 8))
                    .isEqualTo(HashCodes.start().add(1).add(2).add(3).add(4).add(5).add(6).add(7).add(8).result());
        }
    }
}
