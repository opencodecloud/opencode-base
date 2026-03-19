package cloud.opencode.base.hash.simhash;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Fingerprint 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.0
 */
@DisplayName("Fingerprint 测试")
class FingerprintTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("创建指纹")
        void testConstructor() {
            Fingerprint fp = new Fingerprint(0x12345678L, 64);

            assertThat(fp.value()).isEqualTo(0x12345678L);
            assertThat(fp.bits()).isEqualTo(64);
        }

        @Test
        @DisplayName("零位数抛出异常")
        void testZeroBits() {
            assertThatThrownBy(() -> new Fingerprint(0L, 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("负位数抛出异常")
        void testNegativeBits() {
            assertThatThrownBy(() -> new Fingerprint(0L, -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("超过64位抛出异常")
        void testTooManyBits() {
            assertThatThrownBy(() -> new Fingerprint(0L, 65))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("value方法测试")
    class ValueTests {

        @Test
        @DisplayName("获取指纹值")
        void testValue() {
            Fingerprint fp = new Fingerprint(0xABCDEF12L, 64);

            assertThat(fp.value()).isEqualTo(0xABCDEF12L);
        }

        @Test
        @DisplayName("负值指纹")
        void testNegativeValue() {
            Fingerprint fp = new Fingerprint(-1L, 64);

            assertThat(fp.value()).isEqualTo(-1L);
        }
    }

    @Nested
    @DisplayName("bits方法测试")
    class BitsTests {

        @Test
        @DisplayName("获取位数")
        void testBits() {
            Fingerprint fp64 = new Fingerprint(0L, 64);
            Fingerprint fp32 = new Fingerprint(0L, 32);

            assertThat(fp64.bits()).isEqualTo(64);
            assertThat(fp32.bits()).isEqualTo(32);
        }
    }

    @Nested
    @DisplayName("hammingDistance方法测试")
    class HammingDistanceTests {

        @Test
        @DisplayName("相同值距离为0")
        void testSameValue() {
            Fingerprint fp1 = new Fingerprint(0x12345678L, 64);
            Fingerprint fp2 = new Fingerprint(0x12345678L, 64);

            assertThat(fp1.hammingDistance(fp2)).isEqualTo(0);
        }

        @Test
        @DisplayName("完全不同的值")
        void testDifferentValues() {
            Fingerprint fp1 = new Fingerprint(0L, 64);
            Fingerprint fp2 = new Fingerprint(-1L, 64);

            assertThat(fp1.hammingDistance(fp2)).isEqualTo(64);
        }

        @Test
        @DisplayName("部分不同")
        void testPartialDifference() {
            Fingerprint fp1 = new Fingerprint(0b1010L, 64);
            Fingerprint fp2 = new Fingerprint(0b1100L, 64);

            assertThat(fp1.hammingDistance(fp2)).isEqualTo(2);
        }

        @Test
        @DisplayName("单个位不同")
        void testSingleBitDifference() {
            Fingerprint fp1 = new Fingerprint(0b1000L, 64);
            Fingerprint fp2 = new Fingerprint(0b0000L, 64);

            assertThat(fp1.hammingDistance(fp2)).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("similarity方法测试")
    class SimilarityTests {

        @Test
        @DisplayName("相同值相似度为1")
        void testSameValue() {
            Fingerprint fp1 = new Fingerprint(0x12345678L, 64);
            Fingerprint fp2 = new Fingerprint(0x12345678L, 64);

            assertThat(fp1.similarity(fp2)).isEqualTo(1.0);
        }

        @Test
        @DisplayName("完全不同相似度为0")
        void testCompletelyDifferent() {
            Fingerprint fp1 = new Fingerprint(0L, 64);
            Fingerprint fp2 = new Fingerprint(-1L, 64);

            assertThat(fp1.similarity(fp2)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("半数不同相似度为0.5")
        void testHalfDifferent() {
            // 32位不同
            Fingerprint fp1 = new Fingerprint(0L, 64);
            Fingerprint fp2 = new Fingerprint(0xFFFFFFFFL, 64);

            assertThat(fp1.similarity(fp2)).isEqualTo(0.5);
        }

        @Test
        @DisplayName("相似度在0和1之间")
        void testSimilarityRange() {
            Fingerprint fp1 = new Fingerprint(0x12345678L, 64);
            Fingerprint fp2 = new Fingerprint(0x12345670L, 64);

            double similarity = fp1.similarity(fp2);
            assertThat(similarity).isBetween(0.0, 1.0);
        }
    }

    @Nested
    @DisplayName("isSimilar方法测试")
    class IsSimilarTests {

        @Test
        @DisplayName("相同值是相似的")
        void testSameValue() {
            Fingerprint fp1 = new Fingerprint(0x12345678L, 64);
            Fingerprint fp2 = new Fingerprint(0x12345678L, 64);

            assertThat(fp1.isSimilar(fp2, 3)).isTrue();
        }

        @Test
        @DisplayName("在阈值内是相似的")
        void testWithinThreshold() {
            Fingerprint fp1 = new Fingerprint(0b1010L, 64);
            Fingerprint fp2 = new Fingerprint(0b1000L, 64);

            assertThat(fp1.isSimilar(fp2, 3)).isTrue();
        }

        @Test
        @DisplayName("超出阈值不相似")
        void testBeyondThreshold() {
            Fingerprint fp1 = new Fingerprint(0b1010L, 64);
            Fingerprint fp2 = new Fingerprint(0b0101L, 64);

            assertThat(fp1.isSimilar(fp2, 2)).isFalse();
        }

        @Test
        @DisplayName("阈值为0只有相同值相似")
        void testZeroThreshold() {
            Fingerprint fp1 = new Fingerprint(0x12345678L, 64);
            Fingerprint fp2 = new Fingerprint(0x12345678L, 64);
            Fingerprint fp3 = new Fingerprint(0x12345679L, 64);

            assertThat(fp1.isSimilar(fp2, 0)).isTrue();
            assertThat(fp1.isSimilar(fp3, 0)).isFalse();
        }
    }

    @Nested
    @DisplayName("toHex方法测试")
    class ToHexTests {

        @Test
        @DisplayName("转换为十六进制")
        void testToHex() {
            Fingerprint fp = new Fingerprint(0xABCDL, 64);

            String hex = fp.toHex();

            assertThat(hex).isEqualTo("000000000000abcd");
        }

        @Test
        @DisplayName("32位指纹十六进制")
        void test32BitHex() {
            Fingerprint fp = new Fingerprint(0xABCDL, 32);

            String hex = fp.toHex();

            assertThat(hex).hasSize(8);
        }

        @Test
        @DisplayName("零值")
        void testZeroValue() {
            Fingerprint fp = new Fingerprint(0L, 64);

            String hex = fp.toHex();

            assertThat(hex).isEqualTo("0000000000000000");
        }
    }

    @Nested
    @DisplayName("toBinary方法测试")
    class ToBinaryTests {

        @Test
        @DisplayName("转换为二进制")
        void testToBinary() {
            Fingerprint fp = new Fingerprint(0b1010L, 64);

            String binary = fp.toBinary();

            assertThat(binary).hasSize(64);
            assertThat(binary).endsWith("1010");
        }

        @Test
        @DisplayName("32位指纹二进制")
        void test32BitBinary() {
            Fingerprint fp = new Fingerprint(0b1111L, 32);

            String binary = fp.toBinary();

            assertThat(binary).hasSize(32);
        }

        @Test
        @DisplayName("零值二进制")
        void testZeroBinary() {
            Fingerprint fp = new Fingerprint(0L, 64);

            String binary = fp.toBinary();

            assertThat(binary).isEqualTo("0".repeat(64));
        }
    }

    @Nested
    @DisplayName("of64工厂方法测试")
    class Of64Tests {

        @Test
        @DisplayName("创建64位指纹")
        void testOf64() {
            Fingerprint fp = Fingerprint.of64(0x12345678L);

            assertThat(fp.bits()).isEqualTo(64);
            assertThat(fp.value()).isEqualTo(0x12345678L);
        }
    }

    @Nested
    @DisplayName("of32工厂方法测试")
    class Of32Tests {

        @Test
        @DisplayName("创建32位指纹")
        void testOf32() {
            Fingerprint fp = Fingerprint.of32(0x12345678);

            assertThat(fp.bits()).isEqualTo(32);
        }

        @Test
        @DisplayName("负整数转换")
        void testNegativeInt() {
            Fingerprint fp = Fingerprint.of32(-1);

            assertThat(fp.bits()).isEqualTo(32);
            assertThat(fp.value()).isEqualTo(0xFFFFFFFFL);
        }
    }

    @Nested
    @DisplayName("of工厂方法测试")
    class OfTests {

        @Test
        @DisplayName("创建指定位数的指纹")
        void testOf() {
            Fingerprint fp = Fingerprint.of(0x12345678L, 48);

            assertThat(fp.bits()).isEqualTo(48);
            assertThat(fp.value()).isEqualTo(0x12345678L);
        }
    }

    @Nested
    @DisplayName("fromHex工厂方法测试")
    class FromHexTests {

        @Test
        @DisplayName("从十六进制创建指纹")
        void testFromHex() {
            Fingerprint fp = Fingerprint.fromHex("abcd", 64);

            assertThat(fp.value()).isEqualTo(0xABCDL);
            assertThat(fp.bits()).isEqualTo(64);
        }

        @Test
        @DisplayName("大写十六进制")
        void testUpperCaseHex() {
            Fingerprint fp = Fingerprint.fromHex("ABCD", 64);

            assertThat(fp.value()).isEqualTo(0xABCDL);
        }
    }

    @Nested
    @DisplayName("equals和hashCode测试")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("相同值和位数相等")
        void testEquals() {
            Fingerprint fp1 = new Fingerprint(0x12345678L, 64);
            Fingerprint fp2 = new Fingerprint(0x12345678L, 64);

            assertThat(fp1).isEqualTo(fp2);
        }

        @Test
        @DisplayName("不同值不相等")
        void testNotEqualsDifferentValue() {
            Fingerprint fp1 = new Fingerprint(0x12345678L, 64);
            Fingerprint fp2 = new Fingerprint(0x87654321L, 64);

            assertThat(fp1).isNotEqualTo(fp2);
        }

        @Test
        @DisplayName("不同位数不相等")
        void testNotEqualsDifferentBits() {
            Fingerprint fp1 = new Fingerprint(0x12345678L, 64);
            Fingerprint fp2 = new Fingerprint(0x12345678L, 32);

            assertThat(fp1).isNotEqualTo(fp2);
        }

        @Test
        @DisplayName("hashCode一致性")
        void testHashCodeConsistency() {
            Fingerprint fp1 = new Fingerprint(0x12345678L, 64);
            Fingerprint fp2 = new Fingerprint(0x12345678L, 64);

            assertThat(fp1.hashCode()).isEqualTo(fp2.hashCode());
        }

        @Test
        @DisplayName("与null不相等")
        void testNotEqualsNull() {
            Fingerprint fp = new Fingerprint(0x12345678L, 64);

            assertThat(fp).isNotEqualTo(null);
        }

        @Test
        @DisplayName("与其他类型不相等")
        void testNotEqualsOtherType() {
            Fingerprint fp = new Fingerprint(0x12345678L, 64);

            assertThat(fp).isNotEqualTo("12345678");
        }
    }

    @Nested
    @DisplayName("toString方法测试")
    class ToStringTests {

        @Test
        @DisplayName("toString包含十六进制值和位数")
        void testToString() {
            Fingerprint fp = new Fingerprint(0xABCDL, 64);

            String str = fp.toString();

            assertThat(str).contains("abcd");
            assertThat(str).contains("64");
        }
    }
}
