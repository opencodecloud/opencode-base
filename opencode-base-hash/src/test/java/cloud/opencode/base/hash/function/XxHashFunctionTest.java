package cloud.opencode.base.hash.function;

import cloud.opencode.base.hash.HashCode;
import cloud.opencode.base.hash.HashFunction;
import cloud.opencode.base.hash.Hasher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;

/**
 * XxHashFunction 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.0
 */
@DisplayName("XxHashFunction 测试")
class XxHashFunctionTest {

    @Nested
    @DisplayName("xxHash64测试")
    class XxHash64Tests {

        @Test
        @DisplayName("创建默认种子的xxHash64")
        void testXxHash64Default() {
            HashFunction hf = XxHashFunction.xxHash64();

            assertThat(hf.bits()).isEqualTo(64);
            assertThat(hf.name()).containsIgnoringCase("xxhash");
        }

        @Test
        @DisplayName("创建自定义种子的xxHash64")
        void testXxHash64WithSeed() {
            HashFunction hf = XxHashFunction.xxHash64(12345L);

            assertThat(hf.bits()).isEqualTo(64);
        }

        @Test
        @DisplayName("不同种子产生不同哈希")
        void testDifferentSeeds() {
            HashFunction hf1 = XxHashFunction.xxHash64(1L);
            HashFunction hf2 = XxHashFunction.xxHash64(2L);

            HashCode h1 = hf1.hashUtf8("test");
            HashCode h2 = hf2.hashUtf8("test");

            assertThat(h1).isNotEqualTo(h2);
        }

        @Test
        @DisplayName("哈希一致性")
        void testConsistency() {
            HashFunction hf = XxHashFunction.xxHash64();

            HashCode h1 = hf.hashUtf8("hello");
            HashCode h2 = hf.hashUtf8("hello");

            assertThat(h1).isEqualTo(h2);
        }

        @Test
        @DisplayName("不同输入产生不同哈希")
        void testDifferentInputs() {
            HashFunction hf = XxHashFunction.xxHash64();

            HashCode h1 = hf.hashUtf8("foo");
            HashCode h2 = hf.hashUtf8("bar");

            assertThat(h1).isNotEqualTo(h2);
        }
    }

    @Nested
    @DisplayName("哈希字节数据测试")
    class HashBytesTests {

        @Test
        @DisplayName("哈希字节数组")
        void testHashBytes() {
            HashFunction hf = XxHashFunction.xxHash64();
            byte[] data = "test".getBytes(StandardCharsets.UTF_8);

            HashCode hash = hf.hashBytes(data);

            assertThat(hash.bits()).isEqualTo(64);
        }

        @Test
        @DisplayName("哈希部分字节数组")
        void testHashBytesPartial() {
            HashFunction hf = XxHashFunction.xxHash64();
            byte[] data = "hello world".getBytes(StandardCharsets.UTF_8);

            HashCode hash = hf.hashBytes(data, 0, 5);

            assertThat(hash.bits()).isEqualTo(64);
        }

        @Test
        @DisplayName("空字节数组哈希")
        void testEmptyBytes() {
            HashFunction hf = XxHashFunction.xxHash64();

            HashCode hash = hf.hashBytes(new byte[0]);

            assertThat(hash.bits()).isEqualTo(64);
        }
    }

    @Nested
    @DisplayName("哈希基本类型测试")
    class HashPrimitivesTests {

        @Test
        @DisplayName("哈希整数")
        void testHashInt() {
            HashFunction hf = XxHashFunction.xxHash64();

            HashCode hash = hf.hashInt(42);

            assertThat(hash.bits()).isEqualTo(64);
        }

        @Test
        @DisplayName("哈希长整数")
        void testHashLong() {
            HashFunction hf = XxHashFunction.xxHash64();

            HashCode hash = hf.hashLong(123456789L);

            assertThat(hash.bits()).isEqualTo(64);
        }

        @Test
        @DisplayName("不同整数产生不同哈希")
        void testDifferentInts() {
            HashFunction hf = XxHashFunction.xxHash64();

            HashCode h1 = hf.hashInt(1);
            HashCode h2 = hf.hashInt(2);

            assertThat(h1).isNotEqualTo(h2);
        }
    }

    @Nested
    @DisplayName("Hasher流式API测试")
    class HasherTests {

        @Test
        @DisplayName("创建Hasher")
        void testNewHasher() {
            HashFunction hf = XxHashFunction.xxHash64();
            Hasher hasher = hf.newHasher();

            assertThat(hasher).isNotNull();
        }

        @Test
        @DisplayName("流式添加数据")
        void testStreamingHash() {
            HashFunction hf = XxHashFunction.xxHash64();
            Hasher hasher = hf.newHasher();

            hasher.putByte((byte) 1);
            hasher.putInt(42);
            hasher.putLong(123L);
            hasher.putUtf8("test");
            HashCode hash = hasher.hash();

            assertThat(hash.bits()).isEqualTo(64);
        }

        @Test
        @DisplayName("putBytes流式哈希")
        void testPutBytes() {
            HashFunction hf = XxHashFunction.xxHash64();
            Hasher hasher = hf.newHasher();

            hasher.putBytes(new byte[]{1, 2, 3, 4, 5, 6, 7, 8});
            HashCode hash = hasher.hash();

            assertThat(hash.bits()).isEqualTo(64);
        }

        @Test
        @DisplayName("putShort流式哈希")
        void testPutShort() {
            HashFunction hf = XxHashFunction.xxHash64();
            Hasher hasher = hf.newHasher();

            hasher.putShort((short) 12345);
            HashCode hash = hasher.hash();

            assertThat(hash.bits()).isEqualTo(64);
        }

        @Test
        @DisplayName("putFloat流式哈希")
        void testPutFloat() {
            HashFunction hf = XxHashFunction.xxHash64();
            Hasher hasher = hf.newHasher();

            hasher.putFloat(3.14f);
            HashCode hash = hasher.hash();

            assertThat(hash.bits()).isEqualTo(64);
        }

        @Test
        @DisplayName("putDouble流式哈希")
        void testPutDouble() {
            HashFunction hf = XxHashFunction.xxHash64();
            Hasher hasher = hf.newHasher();

            hasher.putDouble(3.14159);
            HashCode hash = hasher.hash();

            assertThat(hash.bits()).isEqualTo(64);
        }

        @Test
        @DisplayName("putBoolean流式哈希")
        void testPutBoolean() {
            HashFunction hf = XxHashFunction.xxHash64();
            Hasher hasher = hf.newHasher();

            hasher.putBoolean(true);
            HashCode hash = hasher.hash();

            assertThat(hash.bits()).isEqualTo(64);
        }

        @Test
        @DisplayName("putChar流式哈希")
        void testPutChar() {
            HashFunction hf = XxHashFunction.xxHash64();
            Hasher hasher = hf.newHasher();

            hasher.putChar('X');
            HashCode hash = hasher.hash();

            assertThat(hash.bits()).isEqualTo(64);
        }

        @Test
        @DisplayName("带初始容量的Hasher")
        void testNewHasherWithCapacity() {
            HashFunction hf = XxHashFunction.xxHash64();
            Hasher hasher = hf.newHasher(1024);

            hasher.putUtf8("test");
            HashCode hash = hasher.hash();

            assertThat(hash.bits()).isEqualTo(64);
        }
    }

    @Nested
    @DisplayName("字符串哈希测试")
    class StringHashTests {

        @Test
        @DisplayName("哈希UTF-8字符串")
        void testHashUtf8() {
            HashFunction hf = XxHashFunction.xxHash64();

            HashCode hash = hf.hashUtf8("Hello, World!");

            assertThat(hash.bits()).isEqualTo(64);
        }

        @Test
        @DisplayName("哈希带指定编码的字符串")
        void testHashString() {
            HashFunction hf = XxHashFunction.xxHash64();

            HashCode hash = hf.hashString("Hello", StandardCharsets.UTF_8);

            assertThat(hash.bits()).isEqualTo(64);
        }

        @Test
        @DisplayName("空字符串哈希")
        void testEmptyString() {
            HashFunction hf = XxHashFunction.xxHash64();

            HashCode hash = hf.hashUtf8("");

            assertThat(hash.bits()).isEqualTo(64);
        }

        @Test
        @DisplayName("Unicode字符串哈希")
        void testUnicodeString() {
            HashFunction hf = XxHashFunction.xxHash64();

            HashCode hash = hf.hashUtf8("你好世界");

            assertThat(hash.bits()).isEqualTo(64);
        }
    }

    @Nested
    @DisplayName("大数据量测试")
    class LargeDataTests {

        @Test
        @DisplayName("哈希大字节数组")
        void testLargeBytes() {
            HashFunction hf = XxHashFunction.xxHash64();
            byte[] data = new byte[100000];
            for (int i = 0; i < data.length; i++) {
                data[i] = (byte) (i % 256);
            }

            HashCode hash = hf.hashBytes(data);

            assertThat(hash.bits()).isEqualTo(64);
        }

        @Test
        @DisplayName("流式哈希大量数据")
        void testStreamingLargeData() {
            HashFunction hf = XxHashFunction.xxHash64();
            Hasher hasher = hf.newHasher();

            for (int i = 0; i < 50000; i++) {
                hasher.putLong(i);
            }
            HashCode hash = hasher.hash();

            assertThat(hash.bits()).isEqualTo(64);
        }
    }

    @Nested
    @DisplayName("asLong测试")
    class AsLongTests {

        @Test
        @DisplayName("获取64位哈希值")
        void testAsLong() {
            HashFunction hf = XxHashFunction.xxHash64();

            HashCode hash = hf.hashUtf8("test");
            long value = hash.asLong();

            assertThat(value).isNotZero();
        }
    }
}
