package cloud.opencode.base.hash.bloom;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * BitArray 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.0
 */
@DisplayName("BitArray 测试")
class BitArrayTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("创建指定大小的位数组")
        void testCreateWithSize() {
            BitArray bits = new BitArray(100);

            assertThat(bits.bitSize()).isEqualTo(100);
        }

        @Test
        @DisplayName("创建线程安全位数组")
        void testCreateThreadSafe() {
            BitArray bits = new BitArray(100, true);

            assertThat(bits.bitSize()).isEqualTo(100);
        }

        @Test
        @DisplayName("创建非线程安全位数组")
        void testCreateNonThreadSafe() {
            BitArray bits = new BitArray(100, false);

            assertThat(bits.bitSize()).isEqualTo(100);
        }

        @Test
        @DisplayName("零大小抛出异常")
        void testZeroSize() {
            assertThatThrownBy(() -> new BitArray(0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("负大小抛出异常")
        void testNegativeSize() {
            assertThatThrownBy(() -> new BitArray(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("set方法测试")
    class SetTests {

        @Test
        @DisplayName("设置位")
        void testSet() {
            BitArray bits = new BitArray(100);

            boolean changed = bits.set(50);

            assertThat(changed).isTrue();
            assertThat(bits.get(50)).isTrue();
        }

        @Test
        @DisplayName("重复设置返回false")
        void testSetDuplicate() {
            BitArray bits = new BitArray(100);

            bits.set(50);
            boolean changed = bits.set(50);

            assertThat(changed).isFalse();
        }

        @Test
        @DisplayName("设置第一位")
        void testSetFirst() {
            BitArray bits = new BitArray(100);

            bits.set(0);

            assertThat(bits.get(0)).isTrue();
        }

        @Test
        @DisplayName("设置最后一位")
        void testSetLast() {
            BitArray bits = new BitArray(100);

            bits.set(99);

            assertThat(bits.get(99)).isTrue();
        }

        @Test
        @DisplayName("越界索引抛出异常")
        void testSetOutOfBounds() {
            BitArray bits = new BitArray(100);

            assertThatThrownBy(() -> bits.set(100))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }

        @Test
        @DisplayName("负索引抛出异常")
        void testSetNegativeIndex() {
            BitArray bits = new BitArray(100);

            assertThatThrownBy(() -> bits.set(-1))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }
    }

    @Nested
    @DisplayName("get方法测试")
    class GetTests {

        @Test
        @DisplayName("获取未设置的位")
        void testGetUnset() {
            BitArray bits = new BitArray(100);

            assertThat(bits.get(50)).isFalse();
        }

        @Test
        @DisplayName("获取已设置的位")
        void testGetSet() {
            BitArray bits = new BitArray(100);
            bits.set(50);

            assertThat(bits.get(50)).isTrue();
        }

        @Test
        @DisplayName("越界索引抛出异常")
        void testGetOutOfBounds() {
            BitArray bits = new BitArray(100);

            assertThatThrownBy(() -> bits.get(100))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }
    }

    @Nested
    @DisplayName("clear方法测试")
    class ClearTests {

        @Test
        @DisplayName("清除位")
        void testClear() {
            BitArray bits = new BitArray(100);
            bits.set(50);

            bits.clear(50);

            assertThat(bits.get(50)).isFalse();
        }

        @Test
        @DisplayName("清除未设置的位")
        void testClearUnset() {
            BitArray bits = new BitArray(100);

            bits.clear(50);

            assertThat(bits.get(50)).isFalse();
        }
    }

    @Nested
    @DisplayName("bitSize方法测试")
    class BitSizeTests {

        @Test
        @DisplayName("获取位数组大小")
        void testBitSize() {
            BitArray bits = new BitArray(256);

            assertThat(bits.bitSize()).isEqualTo(256);
        }

        @Test
        @DisplayName("大位数组大小")
        void testLargeBitSize() {
            BitArray bits = new BitArray(10000);

            assertThat(bits.bitSize()).isEqualTo(10000);
        }
    }

    @Nested
    @DisplayName("bitCount方法测试")
    class BitCountTests {

        @Test
        @DisplayName("空位数组计数为0")
        void testEmptyCount() {
            BitArray bits = new BitArray(100);

            assertThat(bits.bitCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("设置位后计数增加")
        void testCountIncreases() {
            BitArray bits = new BitArray(100);

            bits.set(10);
            bits.set(20);
            bits.set(30);

            assertThat(bits.bitCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("重复设置不增加计数")
        void testDuplicateSetNoIncrease() {
            BitArray bits = new BitArray(100);

            bits.set(10);
            bits.set(10);

            assertThat(bits.bitCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("清除后计数减少")
        void testCountDecreases() {
            BitArray bits = new BitArray(100);

            bits.set(10);
            bits.set(20);
            bits.clear(10);

            assertThat(bits.bitCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("clearAll方法测试")
    class ClearAllTests {

        @Test
        @DisplayName("清除所有位")
        void testClearAll() {
            BitArray bits = new BitArray(100);

            bits.set(10);
            bits.set(50);
            bits.set(90);
            bits.clearAll();

            assertThat(bits.bitCount()).isEqualTo(0);
            assertThat(bits.get(10)).isFalse();
            assertThat(bits.get(50)).isFalse();
            assertThat(bits.get(90)).isFalse();
        }
    }

    @Nested
    @DisplayName("or方法测试")
    class OrTests {

        @Test
        @DisplayName("或操作")
        void testOr() {
            BitArray bits1 = new BitArray(100);
            bits1.set(10);
            bits1.set(20);

            BitArray bits2 = new BitArray(100);
            bits2.set(20);
            bits2.set(30);

            bits1.or(bits2);

            assertThat(bits1.get(10)).isTrue();
            assertThat(bits1.get(20)).isTrue();
            assertThat(bits1.get(30)).isTrue();
        }

        @Test
        @DisplayName("或空数组")
        void testOrEmpty() {
            BitArray bits1 = new BitArray(100);
            bits1.set(10);

            BitArray bits2 = new BitArray(100);

            bits1.or(bits2);

            assertThat(bits1.get(10)).isTrue();
            assertThat(bits1.bitCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("序列化测试")
    class SerializationTests {

        @Test
        @DisplayName("序列化和反序列化")
        void testToAndFromBytes() {
            BitArray original = new BitArray(100);
            original.set(10);
            original.set(50);
            original.set(99);

            byte[] bytes = original.toBytes();
            BitArray restored = BitArray.fromBytes(bytes);

            assertThat(restored.bitSize()).isEqualTo(100);
            assertThat(restored.get(10)).isTrue();
            assertThat(restored.get(50)).isTrue();
            assertThat(restored.get(99)).isTrue();
            assertThat(restored.get(0)).isFalse();
        }

        @Test
        @DisplayName("空数组序列化")
        void testEmptySerialization() {
            BitArray original = new BitArray(100);

            byte[] bytes = original.toBytes();
            BitArray restored = BitArray.fromBytes(bytes);

            assertThat(restored.bitCount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("线程安全测试")
    class ThreadSafetyTests {

        @Test
        @DisplayName("线程安全位数组并发操作")
        void testConcurrentOperations() throws InterruptedException {
            BitArray bits = new BitArray(1000, true);

            Thread[] threads = new Thread[10];
            for (int i = 0; i < threads.length; i++) {
                final int offset = i * 100;
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < 100; j++) {
                        bits.set(offset + j);
                    }
                });
            }

            for (Thread thread : threads) {
                thread.start();
            }
            for (Thread thread : threads) {
                thread.join();
            }

            assertThat(bits.bitCount()).isEqualTo(1000);
        }
    }

    @Nested
    @DisplayName("边界测试")
    class BoundaryTests {

        @Test
        @DisplayName("单位数组")
        void testSingleBit() {
            BitArray bits = new BitArray(1);

            bits.set(0);

            assertThat(bits.get(0)).isTrue();
            assertThat(bits.bitCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("64位边界")
        void test64BitBoundary() {
            BitArray bits = new BitArray(128);

            bits.set(63);
            bits.set(64);

            assertThat(bits.get(63)).isTrue();
            assertThat(bits.get(64)).isTrue();
        }

        @Test
        @DisplayName("非64倍数大小")
        void testNon64MultipleSizes() {
            BitArray bits = new BitArray(65);

            bits.set(64);

            assertThat(bits.get(64)).isTrue();
            assertThat(bits.bitSize()).isEqualTo(65);
        }
    }
}
