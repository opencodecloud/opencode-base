package cloud.opencode.base.io.checksum;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Checksum 记录测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.0
 */
@DisplayName("Checksum 记录测试")
class ChecksumTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("完整参数构造")
        void testFullConstructor() {
            byte[] bytes = new byte[]{0x01, 0x02, 0x03};
            Checksum checksum = new Checksum("MD5", bytes, "010203");

            assertThat(checksum.algorithm()).isEqualTo("MD5");
            assertThat(checksum.hex()).isEqualTo("010203");
        }

        @Test
        @DisplayName("简化构造函数自动计算hex")
        void testSimplifiedConstructor() {
            byte[] bytes = new byte[]{0x0a, 0x0b, 0x0c};
            Checksum checksum = new Checksum("SHA-256", bytes);

            assertThat(checksum.algorithm()).isEqualTo("SHA-256");
            assertThat(checksum.hex()).isEqualTo("0a0b0c");
        }

        @Test
        @DisplayName("null algorithm抛出异常")
        void testNullAlgorithm() {
            assertThatThrownBy(() -> new Checksum(null, new byte[]{1}, "01"))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null bytes抛出异常")
        void testNullBytes() {
            assertThatThrownBy(() -> new Checksum("MD5", null, "01"))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null hex抛出异常")
        void testNullHex() {
            assertThatThrownBy(() -> new Checksum("MD5", new byte[]{1}, null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("bytes方法测试")
    class BytesTests {

        @Test
        @DisplayName("返回字节数组副本")
        void testReturnsCopy() {
            byte[] original = new byte[]{1, 2, 3};
            Checksum checksum = new Checksum("MD5", original);

            byte[] returned = checksum.bytes();
            returned[0] = 99;

            // 原始数组和内部数组不受影响
            assertThat(checksum.bytes()[0]).isEqualTo((byte) 1);
        }
    }

    @Nested
    @DisplayName("matches方法测试")
    class MatchesTests {

        @Test
        @DisplayName("与另一个Checksum匹配")
        void testMatchesChecksum() {
            Checksum cs1 = new Checksum("MD5", new byte[]{1, 2, 3});
            Checksum cs2 = new Checksum("MD5", new byte[]{1, 2, 3});

            assertThat(cs1.matches(cs2)).isTrue();
        }

        @Test
        @DisplayName("与不同Checksum不匹配")
        void testNotMatchesChecksum() {
            Checksum cs1 = new Checksum("MD5", new byte[]{1, 2, 3});
            Checksum cs2 = new Checksum("MD5", new byte[]{4, 5, 6});

            assertThat(cs1.matches(cs2)).isFalse();
        }

        @Test
        @DisplayName("与null Checksum不匹配")
        void testMatchesNullChecksum() {
            Checksum cs = new Checksum("MD5", new byte[]{1, 2, 3});

            assertThat(cs.matches((Checksum) null)).isFalse();
        }

        @Test
        @DisplayName("与hex字符串匹配")
        void testMatchesHexString() {
            Checksum cs = new Checksum("MD5", new byte[]{0x0a, 0x0b, 0x0c});

            assertThat(cs.matches("0a0b0c")).isTrue();
            assertThat(cs.matches("0A0B0C")).isTrue(); // 大小写不敏感
        }

        @Test
        @DisplayName("与不同hex字符串不匹配")
        void testNotMatchesHexString() {
            Checksum cs = new Checksum("MD5", new byte[]{0x0a, 0x0b, 0x0c});

            assertThat(cs.matches("010203")).isFalse();
        }

        @Test
        @DisplayName("与null字符串不匹配")
        void testMatchesNullString() {
            Checksum cs = new Checksum("MD5", new byte[]{1, 2, 3});

            assertThat(cs.matches((String) null)).isFalse();
        }
    }

    @Nested
    @DisplayName("fromHex方法测试")
    class FromHexTests {

        @Test
        @DisplayName("从hex字符串创建")
        void testFromHex() {
            Checksum cs = Checksum.fromHex("SHA-256", "0a0b0c");

            assertThat(cs.algorithm()).isEqualTo("SHA-256");
            assertThat(cs.hex()).isEqualTo("0a0b0c");
            assertThat(cs.bytes()).containsExactly(0x0a, 0x0b, 0x0c);
        }

        @Test
        @DisplayName("大写hex转小写")
        void testFromHexUpperCase() {
            Checksum cs = Checksum.fromHex("MD5", "AABBCC");

            assertThat(cs.hex()).isEqualTo("aabbcc");
        }
    }

    @Nested
    @DisplayName("equals和hashCode测试")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("相同内容相等")
        void testEquals() {
            Checksum cs1 = new Checksum("MD5", new byte[]{1, 2, 3});
            Checksum cs2 = new Checksum("MD5", new byte[]{1, 2, 3});

            assertThat(cs1).isEqualTo(cs2);
            assertThat(cs1.hashCode()).isEqualTo(cs2.hashCode());
        }

        @Test
        @DisplayName("不同算法不相等")
        void testNotEqualsDifferentAlgorithm() {
            Checksum cs1 = new Checksum("MD5", new byte[]{1, 2, 3});
            Checksum cs2 = new Checksum("SHA-1", new byte[]{1, 2, 3});

            assertThat(cs1).isNotEqualTo(cs2);
        }

        @Test
        @DisplayName("不同字节不相等")
        void testNotEqualsDifferentBytes() {
            Checksum cs1 = new Checksum("MD5", new byte[]{1, 2, 3});
            Checksum cs2 = new Checksum("MD5", new byte[]{4, 5, 6});

            assertThat(cs1).isNotEqualTo(cs2);
        }

        @Test
        @DisplayName("与null不相等")
        void testNotEqualsNull() {
            Checksum cs = new Checksum("MD5", new byte[]{1, 2, 3});

            assertThat(cs).isNotEqualTo(null);
        }
    }

    @Nested
    @DisplayName("toString测试")
    class ToStringTests {

        @Test
        @DisplayName("格式为algorithm:hex")
        void testToString() {
            Checksum cs = new Checksum("MD5", new byte[]{0x0a, 0x0b, 0x0c});

            assertThat(cs.toString()).isEqualTo("MD5:0a0b0c");
        }
    }
}
