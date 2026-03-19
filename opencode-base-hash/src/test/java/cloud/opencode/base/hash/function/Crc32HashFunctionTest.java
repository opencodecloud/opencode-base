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
 * Crc32HashFunction 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.0
 */
@DisplayName("Crc32HashFunction 测试")
class Crc32HashFunctionTest {

    @Nested
    @DisplayName("CRC32测试")
    class Crc32Tests {

        @Test
        @DisplayName("创建CRC32哈希函数")
        void testCrc32() {
            HashFunction hf = Crc32HashFunction.crc32();

            assertThat(hf.bits()).isEqualTo(32);
            assertThat(hf.name()).containsIgnoringCase("crc32");
        }

        @Test
        @DisplayName("哈希一致性")
        void testConsistency() {
            HashFunction hf = Crc32HashFunction.crc32();

            HashCode h1 = hf.hashUtf8("hello");
            HashCode h2 = hf.hashUtf8("hello");

            assertThat(h1).isEqualTo(h2);
        }

        @Test
        @DisplayName("不同输入产生不同哈希")
        void testDifferentInputs() {
            HashFunction hf = Crc32HashFunction.crc32();

            HashCode h1 = hf.hashUtf8("foo");
            HashCode h2 = hf.hashUtf8("bar");

            assertThat(h1).isNotEqualTo(h2);
        }

        @Test
        @DisplayName("哈希字节数组")
        void testHashBytes() {
            HashFunction hf = Crc32HashFunction.crc32();
            byte[] data = "test".getBytes(StandardCharsets.UTF_8);

            HashCode hash = hf.hashBytes(data);

            assertThat(hash.bits()).isEqualTo(32);
        }

        @Test
        @DisplayName("哈希整数")
        void testHashInt() {
            HashFunction hf = Crc32HashFunction.crc32();

            HashCode hash = hf.hashInt(42);

            assertThat(hash.bits()).isEqualTo(32);
        }

        @Test
        @DisplayName("哈希长整数")
        void testHashLong() {
            HashFunction hf = Crc32HashFunction.crc32();

            HashCode hash = hf.hashLong(123456789L);

            assertThat(hash.bits()).isEqualTo(32);
        }
    }

    @Nested
    @DisplayName("CRC32C测试")
    class Crc32cTests {

        @Test
        @DisplayName("创建CRC32C哈希函数")
        void testCrc32c() {
            HashFunction hf = Crc32HashFunction.crc32c();

            assertThat(hf.bits()).isEqualTo(32);
            assertThat(hf.name()).containsIgnoringCase("crc32c");
        }

        @Test
        @DisplayName("哈希一致性")
        void testConsistency() {
            HashFunction hf = Crc32HashFunction.crc32c();

            HashCode h1 = hf.hashUtf8("hello");
            HashCode h2 = hf.hashUtf8("hello");

            assertThat(h1).isEqualTo(h2);
        }

        @Test
        @DisplayName("不同输入产生不同哈希")
        void testDifferentInputs() {
            HashFunction hf = Crc32HashFunction.crc32c();

            HashCode h1 = hf.hashUtf8("abc");
            HashCode h2 = hf.hashUtf8("xyz");

            assertThat(h1).isNotEqualTo(h2);
        }

        @Test
        @DisplayName("CRC32和CRC32C产生不同哈希")
        void testCrc32VsCrc32c() {
            HashFunction crc32 = Crc32HashFunction.crc32();
            HashFunction crc32c = Crc32HashFunction.crc32c();

            HashCode h1 = crc32.hashUtf8("test");
            HashCode h2 = crc32c.hashUtf8("test");

            assertThat(h1).isNotEqualTo(h2);
        }
    }

    @Nested
    @DisplayName("Hasher流式API测试")
    class HasherTests {

        @Test
        @DisplayName("创建CRC32 Hasher")
        void testNewHasherCrc32() {
            HashFunction hf = Crc32HashFunction.crc32();
            Hasher hasher = hf.newHasher();

            assertThat(hasher).isNotNull();
        }

        @Test
        @DisplayName("创建CRC32C Hasher")
        void testNewHasherCrc32c() {
            HashFunction hf = Crc32HashFunction.crc32c();
            Hasher hasher = hf.newHasher();

            assertThat(hasher).isNotNull();
        }

        @Test
        @DisplayName("流式添加数据")
        void testStreamingHash() {
            HashFunction hf = Crc32HashFunction.crc32();
            Hasher hasher = hf.newHasher();

            hasher.putByte((byte) 1);
            hasher.putInt(42);
            hasher.putUtf8("test");
            HashCode hash = hasher.hash();

            assertThat(hash.bits()).isEqualTo(32);
        }

        @Test
        @DisplayName("putBytes流式哈希")
        void testPutBytes() {
            HashFunction hf = Crc32HashFunction.crc32();
            Hasher hasher = hf.newHasher();

            hasher.putBytes(new byte[]{1, 2, 3, 4});
            HashCode hash = hasher.hash();

            assertThat(hash.bits()).isEqualTo(32);
        }

        @Test
        @DisplayName("putShort流式哈希")
        void testPutShort() {
            HashFunction hf = Crc32HashFunction.crc32c();
            Hasher hasher = hf.newHasher();

            hasher.putShort((short) 12345);
            HashCode hash = hasher.hash();

            assertThat(hash.bits()).isEqualTo(32);
        }

        @Test
        @DisplayName("putFloat流式哈希")
        void testPutFloat() {
            HashFunction hf = Crc32HashFunction.crc32();
            Hasher hasher = hf.newHasher();

            hasher.putFloat(3.14f);
            HashCode hash = hasher.hash();

            assertThat(hash.bits()).isEqualTo(32);
        }

        @Test
        @DisplayName("putDouble流式哈希")
        void testPutDouble() {
            HashFunction hf = Crc32HashFunction.crc32c();
            Hasher hasher = hf.newHasher();

            hasher.putDouble(3.14159);
            HashCode hash = hasher.hash();

            assertThat(hash.bits()).isEqualTo(32);
        }

        @Test
        @DisplayName("putBoolean流式哈希")
        void testPutBoolean() {
            HashFunction hf = Crc32HashFunction.crc32();
            Hasher hasher = hf.newHasher();

            hasher.putBoolean(true);
            HashCode hash = hasher.hash();

            assertThat(hash.bits()).isEqualTo(32);
        }

        @Test
        @DisplayName("putChar流式哈希")
        void testPutChar() {
            HashFunction hf = Crc32HashFunction.crc32();
            Hasher hasher = hf.newHasher();

            hasher.putChar('Z');
            HashCode hash = hasher.hash();

            assertThat(hash.bits()).isEqualTo(32);
        }

        @Test
        @DisplayName("putLong流式哈希")
        void testPutLong() {
            HashFunction hf = Crc32HashFunction.crc32c();
            Hasher hasher = hf.newHasher();

            hasher.putLong(123456789L);
            HashCode hash = hasher.hash();

            assertThat(hash.bits()).isEqualTo(32);
        }
    }

    @Nested
    @DisplayName("空输入测试")
    class EmptyInputTests {

        @Test
        @DisplayName("空字节数组哈希CRC32")
        void testEmptyBytesCrc32() {
            HashFunction hf = Crc32HashFunction.crc32();

            HashCode hash = hf.hashBytes(new byte[0]);

            assertThat(hash.bits()).isEqualTo(32);
        }

        @Test
        @DisplayName("空字节数组哈希CRC32C")
        void testEmptyBytesCrc32c() {
            HashFunction hf = Crc32HashFunction.crc32c();

            HashCode hash = hf.hashBytes(new byte[0]);

            assertThat(hash.bits()).isEqualTo(32);
        }

        @Test
        @DisplayName("空字符串哈希")
        void testEmptyString() {
            HashFunction hf = Crc32HashFunction.crc32();

            HashCode hash = hf.hashUtf8("");

            assertThat(hash.bits()).isEqualTo(32);
        }
    }

    @Nested
    @DisplayName("字符串哈希测试")
    class StringHashTests {

        @Test
        @DisplayName("哈希UTF-8字符串")
        void testHashUtf8() {
            HashFunction hf = Crc32HashFunction.crc32();

            HashCode hash = hf.hashUtf8("Hello, World!");

            assertThat(hash.bits()).isEqualTo(32);
        }

        @Test
        @DisplayName("Unicode字符串哈希")
        void testUnicodeString() {
            HashFunction hf = Crc32HashFunction.crc32c();

            HashCode hash = hf.hashUtf8("你好世界");

            assertThat(hash.bits()).isEqualTo(32);
        }
    }

    @Nested
    @DisplayName("大数据量测试")
    class LargeDataTests {

        @Test
        @DisplayName("哈希大字节数组")
        void testLargeBytes() {
            HashFunction hf = Crc32HashFunction.crc32();
            byte[] data = new byte[100000];
            for (int i = 0; i < data.length; i++) {
                data[i] = (byte) (i % 256);
            }

            HashCode hash = hf.hashBytes(data);

            assertThat(hash.bits()).isEqualTo(32);
        }

        @Test
        @DisplayName("流式哈希大量数据")
        void testStreamingLargeData() {
            HashFunction hf = Crc32HashFunction.crc32c();
            Hasher hasher = hf.newHasher();

            for (int i = 0; i < 10000; i++) {
                hasher.putInt(i);
            }
            HashCode hash = hasher.hash();

            assertThat(hash.bits()).isEqualTo(32);
        }
    }
}
