package cloud.opencode.base.hash;

import org.junit.jupiter.api.*;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;

/**
 * HashFunctionTest Tests
 * HashFunctionTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.0
 */
@DisplayName("HashFunction 接口测试")
class HashFunctionTest {

    @Nested
    @DisplayName("默认方法测试")
    class DefaultMethodTests {

        @Test
        @DisplayName("hashString使用指定字符集")
        void testHashString() {
            HashFunction fn = OpenHash.murmur3_128();
            HashCode hash = fn.hashString("hello", StandardCharsets.UTF_8);
            assertThat(hash).isNotNull();
            assertThat(hash.bits()).isEqualTo(128);
        }

        @Test
        @DisplayName("hashUtf8使用UTF-8编码")
        void testHashUtf8() {
            HashFunction fn = OpenHash.murmur3_128();
            HashCode hash = fn.hashUtf8("hello");
            assertThat(hash).isNotNull();
        }

        @Test
        @DisplayName("hashUtf8与hashString(UTF_8)结果一致")
        void testHashUtf8ConsistentWithHashString() {
            HashFunction fn = OpenHash.murmur3_128();
            HashCode fromUtf8 = fn.hashUtf8("test");
            HashCode fromString = fn.hashString("test", StandardCharsets.UTF_8);
            assertThat(fromUtf8).isEqualTo(fromString);
        }
    }

    @Nested
    @DisplayName("接口方法测试")
    class InterfaceMethodTests {

        @Test
        @DisplayName("newHasher创建新Hasher")
        void testNewHasher() {
            HashFunction fn = OpenHash.murmur3_128();
            Hasher hasher = fn.newHasher();
            assertThat(hasher).isNotNull();
        }

        @Test
        @DisplayName("newHasher(int)创建带预期大小的Hasher")
        void testNewHasherWithSize() {
            HashFunction fn = OpenHash.murmur3_128();
            Hasher hasher = fn.newHasher(256);
            assertThat(hasher).isNotNull();
        }

        @Test
        @DisplayName("hashBytes计算字节数组哈希")
        void testHashBytes() {
            HashFunction fn = OpenHash.murmur3_128();
            HashCode hash = fn.hashBytes(new byte[]{1, 2, 3, 4, 5});
            assertThat(hash).isNotNull();
        }

        @Test
        @DisplayName("hashBytes(offset, length)计算部分字节数组哈希")
        void testHashBytesWithOffset() {
            HashFunction fn = OpenHash.murmur3_128();
            HashCode hash = fn.hashBytes(new byte[]{1, 2, 3, 4, 5}, 1, 3);
            assertThat(hash).isNotNull();
        }

        @Test
        @DisplayName("hashInt计算int哈希")
        void testHashInt() {
            HashFunction fn = OpenHash.murmur3_128();
            HashCode hash = fn.hashInt(42);
            assertThat(hash).isNotNull();
        }

        @Test
        @DisplayName("hashLong计算long哈希")
        void testHashLong() {
            HashFunction fn = OpenHash.murmur3_128();
            HashCode hash = fn.hashLong(123456789L);
            assertThat(hash).isNotNull();
        }

        @Test
        @DisplayName("bits返回位数")
        void testBits() {
            assertThat(OpenHash.murmur3_128().bits()).isEqualTo(128);
            assertThat(OpenHash.murmur3_32().bits()).isEqualTo(32);
        }

        @Test
        @DisplayName("name返回算法名称")
        void testName() {
            assertThat(OpenHash.murmur3_128().name()).isNotNull().isNotEmpty();
        }
    }

    @Nested
    @DisplayName("确定性测试")
    class DeterminismTests {

        @Test
        @DisplayName("相同输入产生相同哈希")
        void testDeterministic() {
            HashFunction fn = OpenHash.murmur3_128();
            HashCode h1 = fn.hashUtf8("hello");
            HashCode h2 = fn.hashUtf8("hello");
            assertThat(h1).isEqualTo(h2);
        }

        @Test
        @DisplayName("不同输入产生不同哈希")
        void testDifferentInput() {
            HashFunction fn = OpenHash.murmur3_128();
            HashCode h1 = fn.hashUtf8("hello");
            HashCode h2 = fn.hashUtf8("world");
            assertThat(h1).isNotEqualTo(h2);
        }
    }
}
