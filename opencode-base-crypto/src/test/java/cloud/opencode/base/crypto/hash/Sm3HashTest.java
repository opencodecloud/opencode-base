package cloud.opencode.base.crypto.hash;

import cloud.opencode.base.crypto.exception.OpenCryptoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.security.Security;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Unit tests for {@link Sm3Hash}.
 * SM3哈希函数单元测试。
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("Sm3Hash Tests / SM3哈希函数测试")
class Sm3HashTest {

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
        @DisplayName("create()创建SM3哈希器")
        void testCreate() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Sm3Hash hash = Sm3Hash.create();
            assertThat(hash).isNotNull();
            assertThat(hash.getAlgorithm()).isEqualTo("SM3");
            assertThat(hash.getDigestLength()).isEqualTo(32);
        }

        @Test
        @DisplayName("无BC库抛出异常")
        void testNoBouncyCastle() {
            if (isBouncyCastleAvailable()) {
                // Skip this test if BC is available
                return;
            }
            assertThatThrownBy(Sm3Hash::create)
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
            Sm3Hash hash = Sm3Hash.create();
            byte[] result = hash.hash(TEST_BYTES);
            assertThat(result).hasSize(32);
        }

        @Test
        @DisplayName("hash(String)返回正确长度")
        void testHashStringLength() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Sm3Hash hash = Sm3Hash.create();
            byte[] result = hash.hash(TEST_DATA);
            assertThat(result).hasSize(32);
        }

        @Test
        @DisplayName("hash(null byte[])抛出异常")
        void testHashNullBytes() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Sm3Hash hash = Sm3Hash.create();
            assertThatThrownBy(() -> hash.hash((byte[]) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("hash(null String)抛出异常")
        void testHashNullString() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Sm3Hash hash = Sm3Hash.create();
            assertThatThrownBy(() -> hash.hash((String) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("hash相同数据返回相同结果")
        void testHashDeterministic() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Sm3Hash hash = Sm3Hash.create();
            byte[] result1 = hash.hash(TEST_DATA);
            byte[] result2 = hash.hash(TEST_DATA);
            assertThat(result1).isEqualTo(result2);
        }

        @Test
        @DisplayName("hash不同数据返回不同结果")
        void testHashDifferentData() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Sm3Hash hash = Sm3Hash.create();
            byte[] result1 = hash.hash("Hello");
            byte[] result2 = hash.hash("World");
            assertThat(result1).isNotEqualTo(result2);
        }

        @Test
        @DisplayName("hash空数据")
        void testHashEmptyData() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Sm3Hash hash = Sm3Hash.create();
            byte[] result = hash.hash("");
            assertThat(result).hasSize(32);
        }
    }

    @Nested
    @DisplayName("HashHex Tests / 十六进制哈希测试")
    class HashHexTests {

        @Test
        @DisplayName("hashHex(byte[])返回十六进制字符串")
        void testHashHexBytes() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Sm3Hash hash = Sm3Hash.create();
            String hex = hash.hashHex(TEST_BYTES);
            assertThat(hex).isNotNull();
            assertThat(hex).hasSize(64);
            assertThat(hex).matches("[0-9a-f]+");
        }

        @Test
        @DisplayName("hashHex(String)返回十六进制字符串")
        void testHashHexString() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Sm3Hash hash = Sm3Hash.create();
            String hex = hash.hashHex(TEST_DATA);
            assertThat(hex).isNotNull();
            assertThat(hex).hasSize(64);
        }

        @Test
        @DisplayName("hashHex相同数据返回相同结果")
        void testHashHexDeterministic() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Sm3Hash hash = Sm3Hash.create();
            String hex1 = hash.hashHex(TEST_DATA);
            String hex2 = hash.hashHex(TEST_DATA);
            assertThat(hex1).isEqualTo(hex2);
        }
    }

    @Nested
    @DisplayName("HashBase64 Tests / Base64哈希测试")
    class HashBase64Tests {

        @Test
        @DisplayName("hashBase64(byte[])返回Base64字符串")
        void testHashBase64Bytes() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Sm3Hash hash = Sm3Hash.create();
            String base64 = hash.hashBase64(TEST_BYTES);
            assertThat(base64).isNotNull();
            assertThat(base64).isNotEmpty();
        }

        @Test
        @DisplayName("hashBase64相同数据返回相同结果")
        void testHashBase64Deterministic() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Sm3Hash hash = Sm3Hash.create();
            String base641 = hash.hashBase64(TEST_BYTES);
            String base642 = hash.hashBase64(TEST_BYTES);
            assertThat(base641).isEqualTo(base642);
        }
    }

    @Nested
    @DisplayName("GetDigestLength Tests / 获取摘要长度测试")
    class GetDigestLengthTests {

        @Test
        @DisplayName("SM3摘要长度为32")
        void testDigestLength() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Sm3Hash hash = Sm3Hash.create();
            assertThat(hash.getDigestLength()).isEqualTo(32);
        }
    }

    @Nested
    @DisplayName("GetAlgorithm Tests / 获取算法名称测试")
    class GetAlgorithmTests {

        @Test
        @DisplayName("算法名称为SM3")
        void testAlgorithm() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Sm3Hash hash = Sm3Hash.create();
            assertThat(hash.getAlgorithm()).isEqualTo("SM3");
        }
    }

    @Nested
    @DisplayName("Special Data Tests / 特殊数据测试")
    class SpecialDataTests {

        @Test
        @DisplayName("hash处理中文数据")
        void testHashChinese() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Sm3Hash hash = Sm3Hash.create();
            byte[] result = hash.hash("你好世界");
            assertThat(result).hasSize(32);
        }

        @Test
        @DisplayName("hash处理大数据")
        void testHashLargeData() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Sm3Hash hash = Sm3Hash.create();
            String largeData = "a".repeat(100000);
            byte[] result = hash.hash(largeData);
            assertThat(result).hasSize(32);
        }
    }

    @Nested
    @DisplayName("Interface Implementation Tests / 接口实现测试")
    class InterfaceImplementationTests {

        @Test
        @DisplayName("Sm3Hash实现HashFunction接口")
        void testImplementsHashFunction() {
            assumeTrue(isBouncyCastleAvailable(), "This test requires Bouncy Castle");
            Sm3Hash hash = Sm3Hash.create();
            assertThat(hash).isInstanceOf(HashFunction.class);
        }
    }
}
