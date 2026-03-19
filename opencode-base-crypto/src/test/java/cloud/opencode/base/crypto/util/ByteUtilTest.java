package cloud.opencode.base.crypto.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * ByteUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("ByteUtil 测试")
class ByteUtilTest {

    @Nested
    @DisplayName("concat 测试")
    class ConcatTests {

        @Test
        @DisplayName("连接两个数组")
        void testConcatTwoArrays() {
            byte[] a = {1, 2, 3};
            byte[] b = {4, 5, 6};
            byte[] result = ByteUtil.concat(a, b);

            assertThat(result).containsExactly(1, 2, 3, 4, 5, 6);
        }

        @Test
        @DisplayName("连接多个数组")
        void testConcatMultipleArrays() {
            byte[] a = {1};
            byte[] b = {2, 3};
            byte[] c = {4, 5, 6};
            byte[] result = ByteUtil.concat(a, b, c);

            assertThat(result).containsExactly(1, 2, 3, 4, 5, 6);
        }

        @Test
        @DisplayName("连接空数组")
        void testConcatEmptyArrays() {
            byte[] a = {};
            byte[] b = {1, 2};
            byte[] result = ByteUtil.concat(a, b);

            assertThat(result).containsExactly(1, 2);
        }

        @Test
        @DisplayName("连接null数组抛出异常")
        void testConcatNullArray() {
            assertThatThrownBy(() -> ByteUtil.concat((byte[][]) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("连接包含null元素的数组抛出异常")
        void testConcatWithNullElement() {
            byte[] a = {1};
            assertThatThrownBy(() -> ByteUtil.concat(a, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("split 测试")
    class SplitTests {

        @Test
        @DisplayName("拆分数组")
        void testSplit() {
            byte[] data = {1, 2, 3, 4, 5, 6};
            byte[][] result = ByteUtil.split(data, 2, 3, 1);

            assertThat(result.length).isEqualTo(3);
            assertThat(result[0]).containsExactly(1, 2);
            assertThat(result[1]).containsExactly(3, 4, 5);
            assertThat(result[2]).containsExactly(6);
        }

        @Test
        @DisplayName("拆分为相等部分")
        void testSplitEqual() {
            byte[] data = {1, 2, 3, 4};
            byte[][] result = ByteUtil.split(data, 2, 2);

            assertThat(result.length).isEqualTo(2);
            assertThat(result[0]).containsExactly(1, 2);
            assertThat(result[1]).containsExactly(3, 4);
        }

        @Test
        @DisplayName("拆分null数组抛出异常")
        void testSplitNullArray() {
            assertThatThrownBy(() -> ByteUtil.split(null, 1))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("拆分null长度抛出异常")
        void testSplitNullLengths() {
            assertThatThrownBy(() -> ByteUtil.split(new byte[5], (int[]) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("拆分长度不匹配抛出异常")
        void testSplitLengthMismatch() {
            byte[] data = {1, 2, 3};
            assertThatThrownBy(() -> ByteUtil.split(data, 1, 1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("拆分负长度抛出异常")
        void testSplitNegativeLength() {
            byte[] data = {1, 2, 3};
            assertThatThrownBy(() -> ByteUtil.split(data, -1, 4))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("xor 测试")
    class XorTests {

        @Test
        @DisplayName("异或两个数组")
        void testXor() {
            byte[] a = {0x0F, (byte) 0xF0, (byte) 0xAA};
            byte[] b = {(byte) 0xFF, 0x0F, 0x55};
            byte[] result = ByteUtil.xor(a, b);

            assertThat(result).containsExactly((byte) 0xF0, (byte) 0xFF, (byte) 0xFF);
        }

        @Test
        @DisplayName("异或自身得到全零")
        void testXorSelf() {
            byte[] a = {1, 2, 3, 4};
            byte[] result = ByteUtil.xor(a, a);

            assertThat(result).containsExactly(0, 0, 0, 0);
        }

        @Test
        @DisplayName("异或null数组抛出异常")
        void testXorNull() {
            assertThatThrownBy(() -> ByteUtil.xor(null, new byte[1]))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> ByteUtil.xor(new byte[1], null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("异或不同长度数组抛出异常")
        void testXorDifferentLengths() {
            assertThatThrownBy(() -> ByteUtil.xor(new byte[2], new byte[3]))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("reverse 测试")
    class ReverseTests {

        @Test
        @DisplayName("反转数组")
        void testReverse() {
            byte[] data = {1, 2, 3, 4, 5};
            byte[] result = ByteUtil.reverse(data);

            assertThat(result).containsExactly(5, 4, 3, 2, 1);
        }

        @Test
        @DisplayName("反转空数组")
        void testReverseEmpty() {
            byte[] result = ByteUtil.reverse(new byte[0]);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("反转单元素数组")
        void testReverseSingleElement() {
            byte[] result = ByteUtil.reverse(new byte[]{42});
            assertThat(result).containsExactly(42);
        }

        @Test
        @DisplayName("反转null抛出异常")
        void testReverseNull() {
            assertThatThrownBy(() -> ByteUtil.reverse(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("intToBytes 测试")
    class IntToBytesTests {

        @Test
        @DisplayName("转换正整数")
        void testIntToBytes() {
            byte[] result = ByteUtil.intToBytes(0x12345678);

            assertThat(result).hasSize(4);
            assertThat(result).containsExactly(0x12, 0x34, 0x56, 0x78);
        }

        @Test
        @DisplayName("转换零")
        void testIntToBytesZero() {
            byte[] result = ByteUtil.intToBytes(0);

            assertThat(result).containsExactly(0, 0, 0, 0);
        }

        @Test
        @DisplayName("转换最大值")
        void testIntToBytesMax() {
            byte[] result = ByteUtil.intToBytes(Integer.MAX_VALUE);

            assertThat(result).hasSize(4);
        }
    }

    @Nested
    @DisplayName("bytesToInt 测试")
    class BytesToIntTests {

        @Test
        @DisplayName("转换字节到整数")
        void testBytesToInt() {
            byte[] data = {0x12, 0x34, 0x56, 0x78};
            int result = ByteUtil.bytesToInt(data);

            assertThat(result).isEqualTo(0x12345678);
        }

        @Test
        @DisplayName("转换null抛出异常")
        void testBytesToIntNull() {
            assertThatThrownBy(() -> ByteUtil.bytesToInt(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("转换短数组抛出异常")
        void testBytesToIntShortArray() {
            assertThatThrownBy(() -> ByteUtil.bytesToInt(new byte[3]))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("longToBytes 和 bytesToLong 测试")
    class LongConversionTests {

        @Test
        @DisplayName("long往返")
        void testLongRoundTrip() {
            long original = 0x123456789ABCDEF0L;
            byte[] bytes = ByteUtil.longToBytes(original);
            long result = ByteUtil.bytesToLong(bytes);

            assertThat(result).isEqualTo(original);
        }

        @Test
        @DisplayName("bytesToLong null抛出异常")
        void testBytesToLongNull() {
            assertThatThrownBy(() -> ByteUtil.bytesToLong(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("bytesToLong短数组抛出异常")
        void testBytesToLongShortArray() {
            assertThatThrownBy(() -> ByteUtil.bytesToLong(new byte[7]))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("padPkcs7 测试")
    class PadPkcs7Tests {

        @Test
        @DisplayName("填充数据")
        void testPadPkcs7() {
            byte[] data = {1, 2, 3};
            byte[] padded = ByteUtil.padPkcs7(data, 8);

            assertThat(padded).hasSize(8);
            assertThat(padded[0]).isEqualTo((byte) 1);
            assertThat(padded[7]).isEqualTo((byte) 5); // 填充值
        }

        @Test
        @DisplayName("填充满块时添加完整块")
        void testPadPkcs7FullBlock() {
            byte[] data = new byte[8];
            byte[] padded = ByteUtil.padPkcs7(data, 8);

            assertThat(padded).hasSize(16);
            assertThat(padded[15]).isEqualTo((byte) 8);
        }

        @Test
        @DisplayName("填充null抛出异常")
        void testPadPkcs7Null() {
            assertThatThrownBy(() -> ByteUtil.padPkcs7(null, 8))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("无效块大小抛出异常")
        void testPadPkcs7InvalidBlockSize() {
            assertThatThrownBy(() -> ByteUtil.padPkcs7(new byte[5], 0))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> ByteUtil.padPkcs7(new byte[5], 256))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("unpadPkcs7 测试")
    class UnpadPkcs7Tests {

        @Test
        @DisplayName("去除填充")
        void testUnpadPkcs7() {
            byte[] padded = {1, 2, 3, 5, 5, 5, 5, 5};
            byte[] result = ByteUtil.unpadPkcs7(padded);

            assertThat(result).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("填充和去除填充往返")
        void testPadUnpadRoundTrip() {
            byte[] original = {1, 2, 3, 4, 5};
            byte[] padded = ByteUtil.padPkcs7(original, 16);
            byte[] unpadded = ByteUtil.unpadPkcs7(padded);

            assertThat(unpadded).containsExactly(original);
        }

        @Test
        @DisplayName("去除null抛出异常")
        void testUnpadPkcs7Null() {
            assertThatThrownBy(() -> ByteUtil.unpadPkcs7(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("去除空数组抛出异常")
        void testUnpadPkcs7Empty() {
            assertThatThrownBy(() -> ByteUtil.unpadPkcs7(new byte[0]))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("无效填充抛出异常")
        void testUnpadPkcs7Invalid() {
            byte[] invalid = {1, 2, 3, 4, 5}; // 填充值5不匹配
            assertThatThrownBy(() -> ByteUtil.unpadPkcs7(invalid))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("实例化测试")
    class InstantiationTests {

        @Test
        @DisplayName("无法实例化工具类")
        void testCannotInstantiate() {
            assertThatThrownBy(() -> {
                var constructor = ByteUtil.class.getDeclaredConstructor();
                constructor.setAccessible(true);
                constructor.newInstance();
            }).hasCauseInstanceOf(AssertionError.class);
        }
    }
}
