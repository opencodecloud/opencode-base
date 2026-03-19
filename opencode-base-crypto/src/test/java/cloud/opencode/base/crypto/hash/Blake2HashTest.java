package cloud.opencode.base.crypto.hash;

import cloud.opencode.base.crypto.exception.OpenCryptoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.security.Security;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Unit tests for {@link Blake2Hash}.
 * BLAKE2哈希函数单元测试。
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("Blake2Hash Tests / BLAKE2哈希函数测试")
class Blake2HashTest {

    private static final String TEST_DATA = "Hello, World!";
    private static final byte[] TEST_BYTES = "Hello, World!".getBytes();

    /**
     * Check if Bouncy Castle is available
     */
    private static boolean isBouncyCastleAvailable() {
        return Security.getProvider("BC") != null;
    }

    @Nested
    @DisplayName("Factory Method Tests / 工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("blake2b256()创建BLAKE2b-256哈希器")
        void testBlake2b256() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Blake2Hash hash = Blake2Hash.blake2b256();
            assertThat(hash).isNotNull();
            assertThat(hash.getAlgorithm()).isEqualTo("BLAKE2B-256");
            assertThat(hash.getDigestLength()).isEqualTo(32);
        }

        @Test
        @DisplayName("blake2b512()创建BLAKE2b-512哈希器")
        void testBlake2b512() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Blake2Hash hash = Blake2Hash.blake2b512();
            assertThat(hash).isNotNull();
            assertThat(hash.getAlgorithm()).isEqualTo("BLAKE2B-512");
            assertThat(hash.getDigestLength()).isEqualTo(64);
        }

        @Test
        @DisplayName("blake2b(16)创建自定义长度哈希器")
        void testBlake2bCustomLength() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Blake2Hash hash = Blake2Hash.blake2b(16);
            assertThat(hash).isNotNull();
            assertThat(hash.getDigestLength()).isEqualTo(16);
        }

        @Test
        @DisplayName("blake2s256()创建BLAKE2s-256哈希器")
        void testBlake2s256() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Blake2Hash hash = Blake2Hash.blake2s256();
            assertThat(hash).isNotNull();
            assertThat(hash.getAlgorithm()).isEqualTo("BLAKE2S-256");
            assertThat(hash.getDigestLength()).isEqualTo(32);
        }

        @Test
        @DisplayName("blake2s(16)创建自定义长度哈希器")
        void testBlake2sCustomLength() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Blake2Hash hash = Blake2Hash.blake2s(16);
            assertThat(hash).isNotNull();
            assertThat(hash.getDigestLength()).isEqualTo(16);
        }

        @Test
        @DisplayName("blake2b(0)抛出异常")
        void testBlake2bInvalidLength0() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            assertThatThrownBy(() -> Blake2Hash.blake2b(0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("1");
        }

        @Test
        @DisplayName("blake2b(65)抛出异常")
        void testBlake2bInvalidLength65() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            assertThatThrownBy(() -> Blake2Hash.blake2b(65))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("64");
        }

        @Test
        @DisplayName("blake2s(0)抛出异常")
        void testBlake2sInvalidLength0() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            assertThatThrownBy(() -> Blake2Hash.blake2s(0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("1");
        }

        @Test
        @DisplayName("blake2s(33)抛出异常")
        void testBlake2sInvalidLength33() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            assertThatThrownBy(() -> Blake2Hash.blake2s(33))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("32");
        }

        @Test
        @DisplayName("无BC库抛出异常")
        void testNoBouncyCastle() {
            if (isBouncyCastleAvailable()) {
                // Skip this test if BC is available
                return;
            }
            assertThatThrownBy(Blake2Hash::blake2b256)
                    .isInstanceOf(OpenCryptoException.class)
                    .hasMessageContaining("Bouncy Castle");
        }
    }

    @Nested
    @DisplayName("Hash Tests / 哈希测试")
    class HashTests {

        @Test
        @DisplayName("hash(byte[])返回正确长度")
        void testHashBytesLength() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Blake2Hash hash256 = Blake2Hash.blake2b256();
            Blake2Hash hash512 = Blake2Hash.blake2b512();

            assertThat(hash256.hash(TEST_BYTES)).hasSize(32);
            assertThat(hash512.hash(TEST_BYTES)).hasSize(64);
        }

        @Test
        @DisplayName("hash(String)返回正确长度")
        void testHashStringLength() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Blake2Hash hash = Blake2Hash.blake2b256();
            byte[] result = hash.hash(TEST_DATA);
            assertThat(result).hasSize(32);
        }

        @Test
        @DisplayName("hash(null byte[])抛出异常")
        void testHashNullBytes() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Blake2Hash hash = Blake2Hash.blake2b256();
            assertThatThrownBy(() -> hash.hash((byte[]) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("hash(null String)抛出异常")
        void testHashNullString() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Blake2Hash hash = Blake2Hash.blake2b256();
            assertThatThrownBy(() -> hash.hash((String) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("hash相同数据返回相同结果")
        void testHashDeterministic() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Blake2Hash hash = Blake2Hash.blake2b256();
            byte[] result1 = hash.hash(TEST_DATA);
            byte[] result2 = hash.hash(TEST_DATA);
            assertThat(result1).isEqualTo(result2);
        }

        @Test
        @DisplayName("hash不同数据返回不同结果")
        void testHashDifferentData() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Blake2Hash hash = Blake2Hash.blake2b256();
            byte[] result1 = hash.hash("Hello");
            byte[] result2 = hash.hash("World");
            assertThat(result1).isNotEqualTo(result2);
        }
    }

    @Nested
    @DisplayName("HashHex Tests / 十六进制哈希测试")
    class HashHexTests {

        @Test
        @DisplayName("hashHex(byte[])返回十六进制字符串")
        void testHashHexBytes() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Blake2Hash hash = Blake2Hash.blake2b256();
            String hex = hash.hashHex(TEST_BYTES);
            assertThat(hex).isNotNull();
            assertThat(hex).hasSize(64);
            assertThat(hex).matches("[0-9a-f]+");
        }

        @Test
        @DisplayName("hashHex(String)返回十六进制字符串")
        void testHashHexString() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Blake2Hash hash = Blake2Hash.blake2b256();
            String hex = hash.hashHex(TEST_DATA);
            assertThat(hex).isNotNull();
            assertThat(hex).hasSize(64);
        }
    }

    @Nested
    @DisplayName("HashBase64 Tests / Base64哈希测试")
    class HashBase64Tests {

        @Test
        @DisplayName("hashBase64(byte[])返回Base64字符串")
        void testHashBase64Bytes() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Blake2Hash hash = Blake2Hash.blake2b256();
            String base64 = hash.hashBase64(TEST_BYTES);
            assertThat(base64).isNotNull();
            assertThat(base64).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Interface Implementation Tests / 接口实现测试")
    class InterfaceImplementationTests {

        @Test
        @DisplayName("Blake2Hash实现HashFunction接口")
        void testImplementsHashFunction() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Blake2Hash hash = Blake2Hash.blake2b256();
            assertThat(hash).isInstanceOf(HashFunction.class);
        }
    }
}
