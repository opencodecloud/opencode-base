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
 * Murmur3HashFunction 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.0
 */
@DisplayName("Murmur3HashFunction 测试")
class Murmur3HashFunctionTest {

    @Nested
    @DisplayName("murmur3_32测试")
    class Murmur3_32Tests {

        @Test
        @DisplayName("创建默认种子的murmur3_32")
        void testMurmur3_32Default() {
            HashFunction hf = Murmur3HashFunction.murmur3_32();

            assertThat(hf.bits()).isEqualTo(32);
            assertThat(hf.name()).contains("murmur3_32");
        }

        @Test
        @DisplayName("创建自定义种子的murmur3_32")
        void testMurmur3_32WithSeed() {
            HashFunction hf = Murmur3HashFunction.murmur3_32(12345);

            assertThat(hf.bits()).isEqualTo(32);
        }

        @Test
        @DisplayName("不同种子产生不同哈希")
        void testDifferentSeeds() {
            HashFunction hf1 = Murmur3HashFunction.murmur3_32(1);
            HashFunction hf2 = Murmur3HashFunction.murmur3_32(2);

            HashCode h1 = hf1.hashUtf8("test");
            HashCode h2 = hf2.hashUtf8("test");

            assertThat(h1).isNotEqualTo(h2);
        }

        @Test
        @DisplayName("哈希一致性")
        void testConsistency() {
            HashFunction hf = Murmur3HashFunction.murmur3_32();

            HashCode h1 = hf.hashUtf8("hello");
            HashCode h2 = hf.hashUtf8("hello");

            assertThat(h1).isEqualTo(h2);
        }

        @Test
        @DisplayName("哈希字节数组")
        void testHashBytes() {
            HashFunction hf = Murmur3HashFunction.murmur3_32();
            byte[] data = "test".getBytes(StandardCharsets.UTF_8);

            HashCode hash = hf.hashBytes(data);

            assertThat(hash.bits()).isEqualTo(32);
        }

        @Test
        @DisplayName("哈希整数")
        void testHashInt() {
            HashFunction hf = Murmur3HashFunction.murmur3_32();

            HashCode hash = hf.hashInt(42);

            assertThat(hash.bits()).isEqualTo(32);
        }

        @Test
        @DisplayName("哈希长整数")
        void testHashLong() {
            HashFunction hf = Murmur3HashFunction.murmur3_32();

            HashCode hash = hf.hashLong(123456789L);

            assertThat(hash.bits()).isEqualTo(32);
        }
    }

    @Nested
    @DisplayName("murmur3_128测试")
    class Murmur3_128Tests {

        @Test
        @DisplayName("创建默认种子的murmur3_128")
        void testMurmur3_128Default() {
            HashFunction hf = Murmur3HashFunction.murmur3_128();

            assertThat(hf.bits()).isEqualTo(128);
            assertThat(hf.name()).contains("murmur3_128");
        }

        @Test
        @DisplayName("创建自定义种子的murmur3_128")
        void testMurmur3_128WithSeed() {
            HashFunction hf = Murmur3HashFunction.murmur3_128(12345);

            assertThat(hf.bits()).isEqualTo(128);
        }

        @Test
        @DisplayName("哈希一致性")
        void testConsistency() {
            HashFunction hf = Murmur3HashFunction.murmur3_128();

            HashCode h1 = hf.hashUtf8("hello world");
            HashCode h2 = hf.hashUtf8("hello world");

            assertThat(h1).isEqualTo(h2);
        }

        @Test
        @DisplayName("不同输入产生不同哈希")
        void testDifferentInputs() {
            HashFunction hf = Murmur3HashFunction.murmur3_128();

            HashCode h1 = hf.hashUtf8("foo");
            HashCode h2 = hf.hashUtf8("bar");

            assertThat(h1).isNotEqualTo(h2);
        }

        @Test
        @DisplayName("可以获取asLong")
        void testAsLong() {
            HashFunction hf = Murmur3HashFunction.murmur3_128();

            HashCode hash = hf.hashUtf8("test");

            assertThat(hash.asLong()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Hasher流式API测试")
    class HasherTests {

        @Test
        @DisplayName("创建32位Hasher")
        void testNewHasher32() {
            HashFunction hf = Murmur3HashFunction.murmur3_32();
            Hasher hasher = hf.newHasher();

            assertThat(hasher).isNotNull();
        }

        @Test
        @DisplayName("创建128位Hasher")
        void testNewHasher128() {
            HashFunction hf = Murmur3HashFunction.murmur3_128();
            Hasher hasher = hf.newHasher();

            assertThat(hasher).isNotNull();
        }

        @Test
        @DisplayName("流式添加数据")
        void testStreamingHash() {
            HashFunction hf = Murmur3HashFunction.murmur3_32();
            Hasher hasher = hf.newHasher();

            hasher.putByte((byte) 1);
            hasher.putInt(42);
            hasher.putLong(123L);
            hasher.putUtf8("test");
            HashCode hash = hasher.hash();

            assertThat(hash.bits()).isEqualTo(32);
        }

        @Test
        @DisplayName("putBytes流式哈希")
        void testPutBytes() {
            HashFunction hf = Murmur3HashFunction.murmur3_32();
            Hasher hasher = hf.newHasher();

            hasher.putBytes(new byte[]{1, 2, 3, 4});
            HashCode hash = hasher.hash();

            assertThat(hash.bits()).isEqualTo(32);
        }

        @Test
        @DisplayName("putShort流式哈希")
        void testPutShort() {
            HashFunction hf = Murmur3HashFunction.murmur3_32();
            Hasher hasher = hf.newHasher();

            hasher.putShort((short) 12345);
            HashCode hash = hasher.hash();

            assertThat(hash.bits()).isEqualTo(32);
        }

        @Test
        @DisplayName("putFloat流式哈希")
        void testPutFloat() {
            HashFunction hf = Murmur3HashFunction.murmur3_32();
            Hasher hasher = hf.newHasher();

            hasher.putFloat(3.14f);
            HashCode hash = hasher.hash();

            assertThat(hash.bits()).isEqualTo(32);
        }

        @Test
        @DisplayName("putDouble流式哈希")
        void testPutDouble() {
            HashFunction hf = Murmur3HashFunction.murmur3_32();
            Hasher hasher = hf.newHasher();

            hasher.putDouble(3.14159);
            HashCode hash = hasher.hash();

            assertThat(hash.bits()).isEqualTo(32);
        }

        @Test
        @DisplayName("putBoolean流式哈希")
        void testPutBoolean() {
            HashFunction hf = Murmur3HashFunction.murmur3_32();
            Hasher hasher = hf.newHasher();

            hasher.putBoolean(true);
            hasher.putBoolean(false);
            HashCode hash = hasher.hash();

            assertThat(hash.bits()).isEqualTo(32);
        }

        @Test
        @DisplayName("putChar流式哈希")
        void testPutChar() {
            HashFunction hf = Murmur3HashFunction.murmur3_32();
            Hasher hasher = hf.newHasher();

            hasher.putChar('A');
            HashCode hash = hasher.hash();

            assertThat(hash.bits()).isEqualTo(32);
        }

        @Test
        @DisplayName("带初始容量的Hasher")
        void testNewHasherWithCapacity() {
            HashFunction hf = Murmur3HashFunction.murmur3_32();
            Hasher hasher = hf.newHasher(256);

            hasher.putUtf8("test");
            HashCode hash = hasher.hash();

            assertThat(hash.bits()).isEqualTo(32);
        }
    }

    @Nested
    @DisplayName("空输入测试")
    class EmptyInputTests {

        @Test
        @DisplayName("空字节数组哈希")
        void testEmptyBytes() {
            HashFunction hf = Murmur3HashFunction.murmur3_32();

            HashCode hash = hf.hashBytes(new byte[0]);

            assertThat(hash.bits()).isEqualTo(32);
        }

        @Test
        @DisplayName("空字符串哈希")
        void testEmptyString() {
            HashFunction hf = Murmur3HashFunction.murmur3_32();

            HashCode hash = hf.hashUtf8("");

            assertThat(hash.bits()).isEqualTo(32);
        }
    }

    @Nested
    @DisplayName("大数据量测试")
    class LargeDataTests {

        @Test
        @DisplayName("哈希大字节数组")
        void testLargeBytes() {
            HashFunction hf = Murmur3HashFunction.murmur3_128();
            byte[] data = new byte[10000];
            for (int i = 0; i < data.length; i++) {
                data[i] = (byte) (i % 256);
            }

            HashCode hash = hf.hashBytes(data);

            assertThat(hash.bits()).isEqualTo(128);
        }

        @Test
        @DisplayName("流式哈希大量数据")
        void testStreamingLargeData() {
            HashFunction hf = Murmur3HashFunction.murmur3_128();
            Hasher hasher = hf.newHasher();

            for (int i = 0; i < 10000; i++) {
                hasher.putInt(i);
            }
            HashCode hash = hasher.hash();

            assertThat(hash.bits()).isEqualTo(128);
        }
    }
}
