package cloud.opencode.base.hash;

import org.junit.jupiter.api.*;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;

/**
 * HasherTest Tests
 * HasherTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.0
 */
@DisplayName("Hasher 接口测试")
class HasherTest {

    @Nested
    @DisplayName("流式API测试")
    class StreamingApiTests {

        @Test
        @DisplayName("putByte添加字节")
        void testPutByte() {
            Hasher hasher = OpenHash.murmur3_128().newHasher();
            HashCode hash = hasher.putByte((byte) 42).hash();
            assertThat(hash).isNotNull();
        }

        @Test
        @DisplayName("putBytes添加字节数组")
        void testPutBytes() {
            Hasher hasher = OpenHash.murmur3_128().newHasher();
            HashCode hash = hasher.putBytes(new byte[]{1, 2, 3}).hash();
            assertThat(hash).isNotNull();
        }

        @Test
        @DisplayName("putInt添加int值")
        void testPutInt() {
            Hasher hasher = OpenHash.murmur3_128().newHasher();
            HashCode hash = hasher.putInt(42).hash();
            assertThat(hash).isNotNull();
        }

        @Test
        @DisplayName("putLong添加long值")
        void testPutLong() {
            Hasher hasher = OpenHash.murmur3_128().newHasher();
            HashCode hash = hasher.putLong(123456789L).hash();
            assertThat(hash).isNotNull();
        }

        @Test
        @DisplayName("putBoolean添加boolean值")
        void testPutBoolean() {
            Hasher hasher = OpenHash.murmur3_128().newHasher();
            HashCode hash = hasher.putBoolean(true).hash();
            assertThat(hash).isNotNull();
        }

        @Test
        @DisplayName("putChar添加char值")
        void testPutChar() {
            Hasher hasher = OpenHash.murmur3_128().newHasher();
            HashCode hash = hasher.putChar('A').hash();
            assertThat(hash).isNotNull();
        }

        @Test
        @DisplayName("putFloat添加float值")
        void testPutFloat() {
            Hasher hasher = OpenHash.murmur3_128().newHasher();
            HashCode hash = hasher.putFloat(3.14f).hash();
            assertThat(hash).isNotNull();
        }

        @Test
        @DisplayName("putDouble添加double值")
        void testPutDouble() {
            Hasher hasher = OpenHash.murmur3_128().newHasher();
            HashCode hash = hasher.putDouble(2.718).hash();
            assertThat(hash).isNotNull();
        }

        @Test
        @DisplayName("putShort添加short值")
        void testPutShort() {
            Hasher hasher = OpenHash.murmur3_128().newHasher();
            HashCode hash = hasher.putShort((short) 100).hash();
            assertThat(hash).isNotNull();
        }

        @Test
        @DisplayName("putString添加字符串")
        void testPutString() {
            Hasher hasher = OpenHash.murmur3_128().newHasher();
            HashCode hash = hasher.putString("hello", StandardCharsets.UTF_8).hash();
            assertThat(hash).isNotNull();
        }
    }

    @Nested
    @DisplayName("默认方法测试")
    class DefaultMethodTests {

        @Test
        @DisplayName("putUtf8添加UTF-8字符串")
        void testPutUtf8() {
            Hasher hasher = OpenHash.murmur3_128().newHasher();
            HashCode hash = hasher.putUtf8("hello").hash();
            assertThat(hash).isNotNull();
        }

        @Test
        @DisplayName("putUtf8与putString(UTF_8)结果一致")
        void testPutUtf8Consistency() {
            HashCode h1 = OpenHash.murmur3_128().newHasher().putUtf8("test").hash();
            HashCode h2 = OpenHash.murmur3_128().newHasher()
                    .putString("test", StandardCharsets.UTF_8).hash();
            assertThat(h1).isEqualTo(h2);
        }
    }

    @Nested
    @DisplayName("方法链测试")
    class MethodChainingTests {

        @Test
        @DisplayName("多种类型混合哈希")
        void testMixedTypes() {
            Hasher hasher = OpenHash.murmur3_128().newHasher();
            HashCode hash = hasher
                    .putUtf8("hello")
                    .putInt(42)
                    .putLong(123456789L)
                    .putBoolean(true)
                    .hash();
            assertThat(hash).isNotNull();
            assertThat(hash.bits()).isEqualTo(128);
        }
    }

    @Nested
    @DisplayName("一次性使用测试")
    class OneTimeUseTests {

        @Test
        @DisplayName("hash()后再调用hash()抛出异常")
        void testDoubleHashThrows() {
            Hasher hasher = OpenHash.murmur3_128().newHasher();
            hasher.putUtf8("test").hash();
            assertThatThrownBy(hasher::hash)
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
