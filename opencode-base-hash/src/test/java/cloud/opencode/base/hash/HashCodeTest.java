package cloud.opencode.base.hash;

import cloud.opencode.base.hash.exception.OpenHashException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * HashCode 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.0
 */
@DisplayName("HashCode 测试")
class HashCodeTest {

    @Nested
    @DisplayName("fromInt工厂方法测试")
    class FromIntTests {

        @Test
        @DisplayName("创建32位整数哈希码")
        void testFromInt() {
            HashCode hash = HashCode.fromInt(0x12345678);

            assertThat(hash.bits()).isEqualTo(32);
            assertThat(hash.asInt()).isEqualTo(0x12345678);
        }

        @Test
        @DisplayName("负数整数哈希码")
        void testFromNegativeInt() {
            HashCode hash = HashCode.fromInt(-1);

            assertThat(hash.asInt()).isEqualTo(-1);
        }

        @Test
        @DisplayName("零值整数哈希码")
        void testFromZeroInt() {
            HashCode hash = HashCode.fromInt(0);

            assertThat(hash.asInt()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("fromLong工厂方法测试")
    class FromLongTests {

        @Test
        @DisplayName("创建64位长整数哈希码")
        void testFromLong() {
            HashCode hash = HashCode.fromLong(0x123456789ABCDEF0L);

            assertThat(hash.bits()).isEqualTo(64);
            assertThat(hash.asLong()).isEqualTo(0x123456789ABCDEF0L);
        }

        @Test
        @DisplayName("负数长整数哈希码")
        void testFromNegativeLong() {
            HashCode hash = HashCode.fromLong(-1L);

            assertThat(hash.asLong()).isEqualTo(-1L);
        }

        @Test
        @DisplayName("零值长整数哈希码")
        void testFromZeroLong() {
            HashCode hash = HashCode.fromLong(0L);

            assertThat(hash.asLong()).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("fromBytes工厂方法测试")
    class FromBytesTests {

        @Test
        @DisplayName("创建字节数组哈希码")
        void testFromBytes() {
            byte[] bytes = {0x12, 0x34, 0x56, 0x78};
            HashCode hash = HashCode.fromBytes(bytes);

            assertThat(hash.bits()).isEqualTo(32);
            assertThat(hash.asBytes()).isEqualTo(bytes);
        }

        @Test
        @DisplayName("空字节数组抛出异常")
        void testFromEmptyBytes() {
            assertThatThrownBy(() -> HashCode.fromBytes(new byte[0]))
                    .isInstanceOf(OpenHashException.class);
        }

        @Test
        @DisplayName("字节数组独立性")
        void testBytesIndependence() {
            byte[] original = {0x12, 0x34, 0x56, 0x78};
            HashCode hash = HashCode.fromBytes(original);
            original[0] = 0;

            assertThat(hash.asBytes()[0]).isEqualTo((byte) 0x12);
        }
    }

    @Nested
    @DisplayName("fromHex工厂方法测试")
    class FromHexTests {

        @Test
        @DisplayName("从十六进制字符串创建哈希码")
        void testFromHex() {
            HashCode hash = HashCode.fromHex("12345678");

            assertThat(hash.bits()).isEqualTo(32);
            assertThat(hash.toHex()).isEqualTo("12345678");
        }

        @Test
        @DisplayName("大写十六进制字符串")
        void testFromHexUpperCase() {
            HashCode hash = HashCode.fromHex("ABCDEF");

            assertThat(hash.toHex()).isEqualTo("abcdef");
        }

        @Test
        @DisplayName("无效十六进制字符串抛出异常")
        void testFromInvalidHex() {
            assertThatThrownBy(() -> HashCode.fromHex("xyz"))
                    .isInstanceOf(OpenHashException.class);
        }

        @Test
        @DisplayName("奇数长度十六进制字符串抛出异常")
        void testFromOddLengthHex() {
            assertThatThrownBy(() -> HashCode.fromHex("123"))
                    .isInstanceOf(OpenHashException.class);
        }
    }

    @Nested
    @DisplayName("asInt方法测试")
    class AsIntTests {

        @Test
        @DisplayName("32位哈希码转整数")
        void testAsIntFrom32Bits() {
            HashCode hash = HashCode.fromInt(0x12345678);

            assertThat(hash.asInt()).isEqualTo(0x12345678);
        }

        @Test
        @DisplayName("64位哈希码转整数取低32位")
        void testAsIntFrom64Bits() {
            HashCode hash = HashCode.fromLong(0x123456789ABCDEF0L);

            assertThat(hash.asInt()).isEqualTo(0x9ABCDEF0);
        }
    }

    @Nested
    @DisplayName("asLong方法测试")
    class AsLongTests {

        @Test
        @DisplayName("64位哈希码转长整数")
        void testAsLongFrom64Bits() {
            HashCode hash = HashCode.fromLong(0x123456789ABCDEF0L);

            assertThat(hash.asLong()).isEqualTo(0x123456789ABCDEF0L);
        }

        @Test
        @DisplayName("32位哈希码不足64位抛出异常")
        void testAsLongFrom32Bits() {
            HashCode hash = HashCode.fromInt(0x12345678);

            assertThatThrownBy(hash::asLong)
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("padToLong方法测试")
    class PadToLongTests {

        @Test
        @DisplayName("64位哈希码padToLong")
        void testPadToLongFrom64Bits() {
            HashCode hash = HashCode.fromLong(0x123456789ABCDEF0L);

            assertThat(hash.padToLong()).isEqualTo(0x123456789ABCDEF0L);
        }

        @Test
        @DisplayName("32位哈希码padToLong")
        void testPadToLongFrom32Bits() {
            HashCode hash = HashCode.fromInt(0x12345678);

            long padded = hash.padToLong();
            assertThat(padded & 0xFFFFFFFFL).isEqualTo(0x12345678L);
        }
    }

    @Nested
    @DisplayName("asBytes方法测试")
    class AsBytesTests {

        @Test
        @DisplayName("获取字节数组")
        void testAsBytes() {
            byte[] bytes = {0x12, 0x34, 0x56, 0x78};
            HashCode hash = HashCode.fromBytes(bytes);

            assertThat(hash.asBytes()).isEqualTo(bytes);
        }

        @Test
        @DisplayName("字节数组独立性")
        void testAsBytesIndependence() {
            HashCode hash = HashCode.fromInt(0x12345678);
            byte[] bytes1 = hash.asBytes();
            byte[] bytes2 = hash.asBytes();
            bytes1[0] = 0;

            assertThat(bytes2[0]).isNotEqualTo((byte) 0);
        }
    }

    @Nested
    @DisplayName("writeBytesTo方法测试")
    class WriteBytesToTests {

        @Test
        @DisplayName("写入字节到数组")
        void testWriteBytesTo() {
            HashCode hash = HashCode.fromInt(0x12345678);
            byte[] dest = new byte[10];

            int written = hash.writeBytesTo(dest, 2);

            assertThat(written).isEqualTo(4);
        }

        @Test
        @DisplayName("从偏移位置写入字节")
        void testWriteBytesToWithOffset() {
            HashCode hash = HashCode.fromLong(0x123456789ABCDEF0L);
            byte[] dest = new byte[12];

            int written = hash.writeBytesTo(dest, 4);

            assertThat(written).isEqualTo(8);
        }
    }

    @Nested
    @DisplayName("toHex方法测试")
    class ToHexTests {

        @Test
        @DisplayName("整数转十六进制")
        void testToHexFromInt() {
            HashCode hash = HashCode.fromInt(0x12345678);

            assertThat(hash.toHex()).isEqualTo("78563412");
        }

        @Test
        @DisplayName("字节数组转十六进制")
        void testToHexFromBytes() {
            HashCode hash = HashCode.fromBytes(new byte[]{(byte) 0xAB, (byte) 0xCD});

            assertThat(hash.toHex()).isEqualTo("abcd");
        }

        @Test
        @DisplayName("零值转十六进制")
        void testToHexZero() {
            HashCode hash = HashCode.fromInt(0);

            assertThat(hash.toHex()).isEqualTo("00000000");
        }
    }

    @Nested
    @DisplayName("bits方法测试")
    class BitsTests {

        @Test
        @DisplayName("32位整数哈希码")
        void testBitsFromInt() {
            HashCode hash = HashCode.fromInt(123);

            assertThat(hash.bits()).isEqualTo(32);
        }

        @Test
        @DisplayName("64位长整数哈希码")
        void testBitsFromLong() {
            HashCode hash = HashCode.fromLong(123L);

            assertThat(hash.bits()).isEqualTo(64);
        }

        @Test
        @DisplayName("字节数组哈希码位数")
        void testBitsFromBytes() {
            HashCode hash = HashCode.fromBytes(new byte[]{1, 2, 3});

            assertThat(hash.bits()).isEqualTo(24);
        }
    }

    @Nested
    @DisplayName("equals和hashCode测试")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("相同值哈希码相等")
        void testEquals() {
            HashCode h1 = HashCode.fromInt(0x12345678);
            HashCode h2 = HashCode.fromInt(0x12345678);

            assertThat(h1).isEqualTo(h2);
            assertThat(h1.hashCode()).isEqualTo(h2.hashCode());
        }

        @Test
        @DisplayName("不同值哈希码不相等")
        void testNotEquals() {
            HashCode h1 = HashCode.fromInt(0x12345678);
            HashCode h2 = HashCode.fromInt(0x87654321);

            assertThat(h1).isNotEqualTo(h2);
        }

        @Test
        @DisplayName("与null不相等")
        void testNotEqualsNull() {
            HashCode hash = HashCode.fromInt(123);

            assertThat(hash).isNotEqualTo(null);
        }

        @Test
        @DisplayName("与其他类型不相等")
        void testNotEqualsOtherType() {
            HashCode hash = HashCode.fromInt(123);

            assertThat(hash).isNotEqualTo("123");
        }
    }

    @Nested
    @DisplayName("toString方法测试")
    class ToStringTests {

        @Test
        @DisplayName("toString包含十六进制字符串")
        void testToString() {
            HashCode hash = HashCode.fromInt(0x12345678);

            assertThat(hash.toString()).contains(hash.toHex());
        }
    }
}
