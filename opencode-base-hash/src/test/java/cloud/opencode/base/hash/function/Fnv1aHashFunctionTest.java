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
 * Fnv1aHashFunction 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.0
 */
@DisplayName("Fnv1aHashFunction 测试")
class Fnv1aHashFunctionTest {

    @Nested
    @DisplayName("fnv1a_32测试")
    class Fnv1a32Tests {

        @Test
        @DisplayName("创建fnv1a_32哈希函数")
        void testFnv1a_32() {
            HashFunction hf = Fnv1aHashFunction.fnv1a_32();

            assertThat(hf.bits()).isEqualTo(32);
            assertThat(hf.name()).containsIgnoringCase("fnv");
        }

        @Test
        @DisplayName("哈希一致性")
        void testConsistency() {
            HashFunction hf = Fnv1aHashFunction.fnv1a_32();

            HashCode h1 = hf.hashUtf8("hello");
            HashCode h2 = hf.hashUtf8("hello");

            assertThat(h1).isEqualTo(h2);
        }

        @Test
        @DisplayName("不同输入产生不同哈希")
        void testDifferentInputs() {
            HashFunction hf = Fnv1aHashFunction.fnv1a_32();

            HashCode h1 = hf.hashUtf8("foo");
            HashCode h2 = hf.hashUtf8("bar");

            assertThat(h1).isNotEqualTo(h2);
        }

        @Test
        @DisplayName("哈希字节数组")
        void testHashBytes() {
            HashFunction hf = Fnv1aHashFunction.fnv1a_32();
            byte[] data = "test".getBytes(StandardCharsets.UTF_8);

            HashCode hash = hf.hashBytes(data);

            assertThat(hash.bits()).isEqualTo(32);
        }

        @Test
        @DisplayName("哈希整数")
        void testHashInt() {
            HashFunction hf = Fnv1aHashFunction.fnv1a_32();

            HashCode hash = hf.hashInt(42);

            assertThat(hash.bits()).isEqualTo(32);
        }

        @Test
        @DisplayName("哈希长整数")
        void testHashLong() {
            HashFunction hf = Fnv1aHashFunction.fnv1a_32();

            HashCode hash = hf.hashLong(123456789L);

            assertThat(hash.bits()).isEqualTo(32);
        }
    }

    @Nested
    @DisplayName("fnv1a_64测试")
    class Fnv1a64Tests {

        @Test
        @DisplayName("创建fnv1a_64哈希函数")
        void testFnv1a_64() {
            HashFunction hf = Fnv1aHashFunction.fnv1a_64();

            assertThat(hf.bits()).isEqualTo(64);
            assertThat(hf.name()).containsIgnoringCase("fnv");
        }

        @Test
        @DisplayName("哈希一致性")
        void testConsistency() {
            HashFunction hf = Fnv1aHashFunction.fnv1a_64();

            HashCode h1 = hf.hashUtf8("hello world");
            HashCode h2 = hf.hashUtf8("hello world");

            assertThat(h1).isEqualTo(h2);
        }

        @Test
        @DisplayName("不同输入产生不同哈希")
        void testDifferentInputs() {
            HashFunction hf = Fnv1aHashFunction.fnv1a_64();

            HashCode h1 = hf.hashUtf8("abc");
            HashCode h2 = hf.hashUtf8("def");

            assertThat(h1).isNotEqualTo(h2);
        }

        @Test
        @DisplayName("获取asLong")
        void testAsLong() {
            HashFunction hf = Fnv1aHashFunction.fnv1a_64();

            HashCode hash = hf.hashUtf8("test");

            assertThat(hash.asLong()).isNotZero();
        }
    }

    @Nested
    @DisplayName("Hasher流式API测试")
    class HasherTests {

        @Test
        @DisplayName("创建32位Hasher")
        void testNewHasher32() {
            HashFunction hf = Fnv1aHashFunction.fnv1a_32();
            Hasher hasher = hf.newHasher();

            assertThat(hasher).isNotNull();
        }

        @Test
        @DisplayName("创建64位Hasher")
        void testNewHasher64() {
            HashFunction hf = Fnv1aHashFunction.fnv1a_64();
            Hasher hasher = hf.newHasher();

            assertThat(hasher).isNotNull();
        }

        @Test
        @DisplayName("流式添加数据")
        void testStreamingHash() {
            HashFunction hf = Fnv1aHashFunction.fnv1a_32();
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
            HashFunction hf = Fnv1aHashFunction.fnv1a_64();
            Hasher hasher = hf.newHasher();

            hasher.putBytes(new byte[]{1, 2, 3, 4});
            HashCode hash = hasher.hash();

            assertThat(hash.bits()).isEqualTo(64);
        }

        @Test
        @DisplayName("putShort流式哈希")
        void testPutShort() {
            HashFunction hf = Fnv1aHashFunction.fnv1a_32();
            Hasher hasher = hf.newHasher();

            hasher.putShort((short) 12345);
            HashCode hash = hasher.hash();

            assertThat(hash.bits()).isEqualTo(32);
        }

        @Test
        @DisplayName("putFloat流式哈希")
        void testPutFloat() {
            HashFunction hf = Fnv1aHashFunction.fnv1a_32();
            Hasher hasher = hf.newHasher();

            hasher.putFloat(3.14f);
            HashCode hash = hasher.hash();

            assertThat(hash.bits()).isEqualTo(32);
        }

        @Test
        @DisplayName("putDouble流式哈希")
        void testPutDouble() {
            HashFunction hf = Fnv1aHashFunction.fnv1a_64();
            Hasher hasher = hf.newHasher();

            hasher.putDouble(3.14159);
            HashCode hash = hasher.hash();

            assertThat(hash.bits()).isEqualTo(64);
        }

        @Test
        @DisplayName("putBoolean流式哈希")
        void testPutBoolean() {
            HashFunction hf = Fnv1aHashFunction.fnv1a_32();
            Hasher hasher = hf.newHasher();

            hasher.putBoolean(true);
            hasher.putBoolean(false);
            HashCode hash = hasher.hash();

            assertThat(hash.bits()).isEqualTo(32);
        }

        @Test
        @DisplayName("putChar流式哈希")
        void testPutChar() {
            HashFunction hf = Fnv1aHashFunction.fnv1a_32();
            Hasher hasher = hf.newHasher();

            hasher.putChar('A');
            HashCode hash = hasher.hash();

            assertThat(hash.bits()).isEqualTo(32);
        }

        @Test
        @DisplayName("putLong流式哈希")
        void testPutLong() {
            HashFunction hf = Fnv1aHashFunction.fnv1a_64();
            Hasher hasher = hf.newHasher();

            hasher.putLong(123456789L);
            HashCode hash = hasher.hash();

            assertThat(hash.bits()).isEqualTo(64);
        }
    }

    @Nested
    @DisplayName("空输入测试")
    class EmptyInputTests {

        @Test
        @DisplayName("空字节数组哈希32位")
        void testEmptyBytes32() {
            HashFunction hf = Fnv1aHashFunction.fnv1a_32();

            HashCode hash = hf.hashBytes(new byte[0]);

            assertThat(hash.bits()).isEqualTo(32);
        }

        @Test
        @DisplayName("空字节数组哈希64位")
        void testEmptyBytes64() {
            HashFunction hf = Fnv1aHashFunction.fnv1a_64();

            HashCode hash = hf.hashBytes(new byte[0]);

            assertThat(hash.bits()).isEqualTo(64);
        }

        @Test
        @DisplayName("空字符串哈希")
        void testEmptyString() {
            HashFunction hf = Fnv1aHashFunction.fnv1a_32();

            HashCode hash = hf.hashUtf8("");

            assertThat(hash.bits()).isEqualTo(32);
        }
    }

    @Nested
    @DisplayName("32位和64位比较测试")
    class CompareTests {

        @Test
        @DisplayName("32位和64位哈希值不同")
        void testDifferentBitSizes() {
            HashFunction hf32 = Fnv1aHashFunction.fnv1a_32();
            HashFunction hf64 = Fnv1aHashFunction.fnv1a_64();

            HashCode h32 = hf32.hashUtf8("test");
            HashCode h64 = hf64.hashUtf8("test");

            assertThat(h32.bits()).isNotEqualTo(h64.bits());
        }
    }

    @Nested
    @DisplayName("字符串哈希测试")
    class StringHashTests {

        @Test
        @DisplayName("哈希UTF-8字符串")
        void testHashUtf8() {
            HashFunction hf = Fnv1aHashFunction.fnv1a_64();

            HashCode hash = hf.hashUtf8("Hello, World!");

            assertThat(hash.bits()).isEqualTo(64);
        }

        @Test
        @DisplayName("Unicode字符串哈希")
        void testUnicodeString() {
            HashFunction hf = Fnv1aHashFunction.fnv1a_64();

            HashCode hash = hf.hashUtf8("你好世界");

            assertThat(hash.bits()).isEqualTo(64);
        }
    }
}
