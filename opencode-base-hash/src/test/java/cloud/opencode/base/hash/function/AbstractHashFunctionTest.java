package cloud.opencode.base.hash.function;

import cloud.opencode.base.hash.HashCode;
import cloud.opencode.base.hash.HashFunction;
import cloud.opencode.base.hash.OpenHash;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * AbstractHashFunctionTest Tests
 * AbstractHashFunctionTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.0
 */
@DisplayName("AbstractHashFunction 抽象基类测试")
class AbstractHashFunctionTest {

    @Nested
    @DisplayName("bits方法测试")
    class BitsTests {

        @Test
        @DisplayName("murmur3_32返回32位")
        void testMurmur3_32Bits() {
            assertThat(OpenHash.murmur3_32().bits()).isEqualTo(32);
        }

        @Test
        @DisplayName("murmur3_128返回128位")
        void testMurmur3_128Bits() {
            assertThat(OpenHash.murmur3_128().bits()).isEqualTo(128);
        }

        @Test
        @DisplayName("xxHash64返回64位")
        void testXxHash64Bits() {
            assertThat(OpenHash.xxHash64().bits()).isEqualTo(64);
        }
    }

    @Nested
    @DisplayName("name方法测试")
    class NameTests {

        @Test
        @DisplayName("每个实现返回唯一名称")
        void testNames() {
            assertThat(OpenHash.murmur3_128().name()).isNotNull();
            assertThat(OpenHash.crc32().name()).isNotNull();
            assertThat(OpenHash.sha256().name()).isNotNull();
        }
    }

    @Nested
    @DisplayName("hashBytes委托测试")
    class HashBytesDelegationTests {

        @Test
        @DisplayName("hashBytes(byte[])委托到hashBytes(byte[], 0, length)")
        void testHashBytesDelegation() {
            HashFunction fn = OpenHash.murmur3_128();
            byte[] data = {1, 2, 3, 4, 5};
            HashCode h1 = fn.hashBytes(data);
            HashCode h2 = fn.hashBytes(data, 0, data.length);
            assertThat(h1).isEqualTo(h2);
        }
    }

    @Nested
    @DisplayName("hashInt和hashLong测试")
    class PrimitiveHashTests {

        @Test
        @DisplayName("hashInt产生确定性结果")
        void testHashIntDeterministic() {
            HashFunction fn = OpenHash.murmur3_128();
            assertThat(fn.hashInt(42)).isEqualTo(fn.hashInt(42));
        }

        @Test
        @DisplayName("hashLong产生确定性结果")
        void testHashLongDeterministic() {
            HashFunction fn = OpenHash.murmur3_128();
            assertThat(fn.hashLong(123L)).isEqualTo(fn.hashLong(123L));
        }

        @Test
        @DisplayName("不同int产生不同哈希")
        void testDifferentInts() {
            HashFunction fn = OpenHash.murmur3_128();
            assertThat(fn.hashInt(1)).isNotEqualTo(fn.hashInt(2));
        }
    }

    @Nested
    @DisplayName("toString测试")
    class ToStringTests {

        @Test
        @DisplayName("toString包含名称和位数")
        void testToString() {
            String str = OpenHash.murmur3_128().toString();
            assertThat(str).contains("128");
        }
    }

    @Nested
    @DisplayName("newHasher(int)委托测试")
    class NewHasherDelegationTests {

        @Test
        @DisplayName("newHasher(int)返回可用的Hasher")
        void testNewHasherWithSize() {
            HashFunction fn = OpenHash.murmur3_128();
            var hasher = fn.newHasher(1024);
            assertThat(hasher).isNotNull();
            HashCode hash = hasher.putUtf8("test").hash();
            assertThat(hash).isNotNull();
        }
    }
}
