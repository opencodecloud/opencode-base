package cloud.opencode.base.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenBit 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("OpenBit 测试")
class OpenBitTest {

    @Nested
    @DisplayName("位设置测试")
    class SetBitTests {

        @Test
        @DisplayName("setBit int")
        void testSetBitInt() {
            assertThat(OpenBit.setBit(0, 0)).isEqualTo(1);     // 0000 -> 0001
            assertThat(OpenBit.setBit(0, 3)).isEqualTo(8);     // 0000 -> 1000
            assertThat(OpenBit.setBit(1, 1)).isEqualTo(3);     // 0001 -> 0011
            assertThat(OpenBit.setBit(8, 3)).isEqualTo(8);     // 已设置，无变化
        }

        @Test
        @DisplayName("setBit long")
        void testSetBitLong() {
            assertThat(OpenBit.setBit(0L, 0)).isEqualTo(1L);
            assertThat(OpenBit.setBit(0L, 63)).isEqualTo(Long.MIN_VALUE); // 最高位
        }
    }

    @Nested
    @DisplayName("位清除测试")
    class ClearBitTests {

        @Test
        @DisplayName("clearBit int")
        void testClearBitInt() {
            assertThat(OpenBit.clearBit(8, 3)).isEqualTo(0);   // 1000 -> 0000
            assertThat(OpenBit.clearBit(15, 0)).isEqualTo(14); // 1111 -> 1110
            assertThat(OpenBit.clearBit(0, 0)).isEqualTo(0);   // 未设置，无变化
        }

        @Test
        @DisplayName("clearBit long")
        void testClearBitLong() {
            assertThat(OpenBit.clearBit(8L, 3)).isEqualTo(0L);
            assertThat(OpenBit.clearBit(Long.MIN_VALUE, 63)).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("位翻转测试")
    class FlipBitTests {

        @Test
        @DisplayName("flipBit int")
        void testFlipBitInt() {
            assertThat(OpenBit.flipBit(0, 0)).isEqualTo(1);    // 0 -> 1
            assertThat(OpenBit.flipBit(1, 0)).isEqualTo(0);    // 1 -> 0
            assertThat(OpenBit.flipBit(5, 1)).isEqualTo(7);    // 101 -> 111
        }

        @Test
        @DisplayName("flipBit long")
        void testFlipBitLong() {
            assertThat(OpenBit.flipBit(0L, 0)).isEqualTo(1L);
            assertThat(OpenBit.flipBit(1L, 0)).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("位测试")
    class TestBitTests {

        @Test
        @DisplayName("testBit int")
        void testTestBitInt() {
            assertThat(OpenBit.testBit(8, 3)).isTrue();        // 1000 的第 3 位是 1
            assertThat(OpenBit.testBit(8, 0)).isFalse();       // 1000 的第 0 位是 0
            assertThat(OpenBit.testBit(15, 2)).isTrue();       // 1111 的第 2 位是 1
        }

        @Test
        @DisplayName("testBit long")
        void testTestBitLong() {
            assertThat(OpenBit.testBit(8L, 3)).isTrue();
            assertThat(OpenBit.testBit(Long.MIN_VALUE, 63)).isTrue();
        }
    }

    @Nested
    @DisplayName("位计数测试")
    class CountBitsTests {

        @Test
        @DisplayName("countBits int")
        void testCountBitsInt() {
            assertThat(OpenBit.countBits(0)).isEqualTo(0);
            assertThat(OpenBit.countBits(1)).isEqualTo(1);
            assertThat(OpenBit.countBits(7)).isEqualTo(3);     // 111
            assertThat(OpenBit.countBits(0b1010_1010)).isEqualTo(4);
        }

        @Test
        @DisplayName("countBits long")
        void testCountBitsLong() {
            assertThat(OpenBit.countBits(0L)).isEqualTo(0);
            assertThat(OpenBit.countBits(0xFFFFFFFFL)).isEqualTo(32);
        }

        @Test
        @DisplayName("countLeadingZeros int")
        void testCountLeadingZerosInt() {
            assertThat(OpenBit.countLeadingZeros(0)).isEqualTo(32);
            assertThat(OpenBit.countLeadingZeros(1)).isEqualTo(31);
            assertThat(OpenBit.countLeadingZeros(Integer.MAX_VALUE)).isEqualTo(1);
        }

        @Test
        @DisplayName("countLeadingZeros long")
        void testCountLeadingZerosLong() {
            assertThat(OpenBit.countLeadingZeros(0L)).isEqualTo(64);
            assertThat(OpenBit.countLeadingZeros(1L)).isEqualTo(63);
        }

        @Test
        @DisplayName("countTrailingZeros int")
        void testCountTrailingZerosInt() {
            assertThat(OpenBit.countTrailingZeros(0)).isEqualTo(32);
            assertThat(OpenBit.countTrailingZeros(1)).isEqualTo(0);
            assertThat(OpenBit.countTrailingZeros(8)).isEqualTo(3);   // 1000
        }

        @Test
        @DisplayName("countTrailingZeros long")
        void testCountTrailingZerosLong() {
            assertThat(OpenBit.countTrailingZeros(0L)).isEqualTo(64);
            assertThat(OpenBit.countTrailingZeros(8L)).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("位旋转测试")
    class RotateTests {

        @Test
        @DisplayName("rotateLeft int")
        void testRotateLeftInt() {
            assertThat(OpenBit.rotateLeft(1, 1)).isEqualTo(2);
            assertThat(OpenBit.rotateLeft(0x80000000, 1)).isEqualTo(1);
        }

        @Test
        @DisplayName("rotateLeft long")
        void testRotateLeftLong() {
            assertThat(OpenBit.rotateLeft(1L, 1)).isEqualTo(2L);
        }

        @Test
        @DisplayName("rotateRight int")
        void testRotateRightInt() {
            assertThat(OpenBit.rotateRight(2, 1)).isEqualTo(1);
            assertThat(OpenBit.rotateRight(1, 1)).isEqualTo(0x80000000);
        }

        @Test
        @DisplayName("rotateRight long")
        void testRotateRightLong() {
            assertThat(OpenBit.rotateRight(2L, 1)).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("位反转测试")
    class ReverseTests {

        @Test
        @DisplayName("reverse int")
        void testReverseInt() {
            // 0x80000000 反转后应该是 1
            assertThat(OpenBit.reverse(0x80000000)).isEqualTo(1);
            // 1 反转后应该是 0x80000000
            assertThat(OpenBit.reverse(1)).isEqualTo(0x80000000);
        }

        @Test
        @DisplayName("reverse long")
        void testReverseLong() {
            assertThat(OpenBit.reverse(1L)).isEqualTo(Long.MIN_VALUE);
        }

        @Test
        @DisplayName("reverseBytes int")
        void testReverseBytesInt() {
            // 0x12345678 反转字节后应该是 0x78563412
            assertThat(OpenBit.reverseBytes(0x12345678)).isEqualTo(0x78563412);
        }

        @Test
        @DisplayName("reverseBytes long")
        void testReverseBytesLong() {
            assertThat(OpenBit.reverseBytes(0x123456789ABCDEF0L)).isEqualTo(0xF0DEBC9A78563412L);
        }
    }

    @Nested
    @DisplayName("位字段操作测试")
    class FieldTests {

        @Test
        @DisplayName("extractField int")
        void testExtractFieldInt() {
            // 从 0b11110000 提取从位置 4 开始的 4 位
            assertThat(OpenBit.extractField(0b11110000, 4, 4)).isEqualTo(0b1111);
            // 从 0xABCD 提取低 8 位
            assertThat(OpenBit.extractField(0xABCD, 0, 8)).isEqualTo(0xCD);
        }

        @Test
        @DisplayName("extractField long")
        void testExtractFieldLong() {
            assertThat(OpenBit.extractField(0xFFL, 0, 8)).isEqualTo(0xFFL);
        }

        @Test
        @DisplayName("insertField int")
        void testInsertFieldInt() {
            // 在 0 的位置 4 插入 0b1111（4 位）
            assertThat(OpenBit.insertField(0, 0b1111, 4, 4)).isEqualTo(0b11110000);
            // 替换 0xABCD 的低 8 位为 0xEF
            assertThat(OpenBit.insertField(0xABCD, 0xEF, 0, 8)).isEqualTo(0xABEF);
        }

        @Test
        @DisplayName("insertField long")
        void testInsertFieldLong() {
            assertThat(OpenBit.insertField(0L, 0xFFL, 0, 8)).isEqualTo(0xFFL);
        }
    }

    @Nested
    @DisplayName("位掩码测试")
    class MaskTests {

        @Test
        @DisplayName("createMask")
        void testCreateMask() {
            assertThat(OpenBit.createMask(0)).isEqualTo(0);
            assertThat(OpenBit.createMask(1)).isEqualTo(1);
            assertThat(OpenBit.createMask(4)).isEqualTo(0b1111);
            assertThat(OpenBit.createMask(8)).isEqualTo(0xFF);
            assertThat(OpenBit.createMask(32)).isEqualTo(-1);
        }

        @Test
        @DisplayName("createMaskLong")
        void testCreateMaskLong() {
            assertThat(OpenBit.createMaskLong(0)).isEqualTo(0L);
            assertThat(OpenBit.createMaskLong(8)).isEqualTo(0xFFL);
            assertThat(OpenBit.createMaskLong(64)).isEqualTo(-1L);
        }
    }

    @Nested
    @DisplayName("2 的幂测试")
    class PowerOfTwoTests {

        @Test
        @DisplayName("isPowerOfTwo int")
        void testIsPowerOfTwoInt() {
            assertThat(OpenBit.isPowerOfTwo(1)).isTrue();
            assertThat(OpenBit.isPowerOfTwo(2)).isTrue();
            assertThat(OpenBit.isPowerOfTwo(4)).isTrue();
            assertThat(OpenBit.isPowerOfTwo(1024)).isTrue();
            assertThat(OpenBit.isPowerOfTwo(0)).isFalse();
            assertThat(OpenBit.isPowerOfTwo(3)).isFalse();
            assertThat(OpenBit.isPowerOfTwo(-1)).isFalse();
        }

        @Test
        @DisplayName("isPowerOfTwo long")
        void testIsPowerOfTwoLong() {
            assertThat(OpenBit.isPowerOfTwo(1L)).isTrue();
            assertThat(OpenBit.isPowerOfTwo(1L << 40)).isTrue();
            assertThat(OpenBit.isPowerOfTwo(0L)).isFalse();
        }

        @Test
        @DisplayName("nextPowerOfTwo int")
        void testNextPowerOfTwoInt() {
            assertThat(OpenBit.nextPowerOfTwo(0)).isEqualTo(1);
            assertThat(OpenBit.nextPowerOfTwo(1)).isEqualTo(1);
            assertThat(OpenBit.nextPowerOfTwo(3)).isEqualTo(4);
            assertThat(OpenBit.nextPowerOfTwo(7)).isEqualTo(8);
            assertThat(OpenBit.nextPowerOfTwo(8)).isEqualTo(8);
        }

        @Test
        @DisplayName("nextPowerOfTwo long")
        void testNextPowerOfTwoLong() {
            assertThat(OpenBit.nextPowerOfTwo(0L)).isEqualTo(1L);
            assertThat(OpenBit.nextPowerOfTwo(7L)).isEqualTo(8L);
        }
    }

    @Nested
    @DisplayName("位置测试")
    class PositionTests {

        @Test
        @DisplayName("highestOneBitPosition int")
        void testHighestOneBitPositionInt() {
            assertThat(OpenBit.highestOneBitPosition(1)).isEqualTo(0);
            assertThat(OpenBit.highestOneBitPosition(8)).isEqualTo(3);
            assertThat(OpenBit.highestOneBitPosition(0xFF)).isEqualTo(7);
        }

        @Test
        @DisplayName("highestOneBitPosition long")
        void testHighestOneBitPositionLong() {
            assertThat(OpenBit.highestOneBitPosition(1L)).isEqualTo(0);
            assertThat(OpenBit.highestOneBitPosition(1L << 40)).isEqualTo(40);
        }

        @Test
        @DisplayName("lowestOneBitPosition int")
        void testLowestOneBitPositionInt() {
            assertThat(OpenBit.lowestOneBitPosition(1)).isEqualTo(0);
            assertThat(OpenBit.lowestOneBitPosition(8)).isEqualTo(3);
            assertThat(OpenBit.lowestOneBitPosition(0b1100)).isEqualTo(2);
        }

        @Test
        @DisplayName("lowestOneBitPosition long")
        void testLowestOneBitPositionLong() {
            assertThat(OpenBit.lowestOneBitPosition(1L)).isEqualTo(0);
            assertThat(OpenBit.lowestOneBitPosition(8L)).isEqualTo(3);
        }
    }
}
