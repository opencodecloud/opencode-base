package cloud.opencode.base.crypto.hash;

import cloud.opencode.base.crypto.exception.OpenCryptoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.security.Security;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Unit tests for {@link Blake3Hash}.
 * BLAKE3哈希函数单元测试。
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("Blake3Hash Tests / BLAKE3哈希函数测试")
class Blake3HashTest {

    private static final String TEST_DATA = "Hello, World!";
    private static final byte[] TEST_BYTES = "Hello, World!".getBytes();

    /**
     * Check if BLAKE3 algorithm is available
     */
    private static boolean isBlake3Available() {
        if (Security.getProvider("BC") == null) {
            return false;
        }
        try {
            Blake3Hash hash = Blake3Hash.create();
            hash.hash("test");  // Actually test that hashing works
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Nested
    @DisplayName("Factory Method Tests / 工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("create()创建默认BLAKE3哈希器")
        void testCreate() {
            assumeTrue(isBlake3Available(), "This test requires BLAKE3 algorithm");
            Blake3Hash hash = Blake3Hash.create();
            assertThat(hash).isNotNull();
            assertThat(hash.getAlgorithm()).isEqualTo("BLAKE3");
            assertThat(hash.getDigestLength()).isEqualTo(32);
        }

        @Test
        @DisplayName("create(64)创建自定义长度哈希器")
        void testCreateCustomLength() {
            assumeTrue(isBlake3Available(), "This test requires BLAKE3 algorithm");
            Blake3Hash hash = Blake3Hash.create(64);
            assertThat(hash).isNotNull();
            assertThat(hash.getDigestLength()).isEqualTo(64);
        }

        @Test
        @DisplayName("create(0)抛出异常")
        void testCreateInvalidLength0() {
            assumeTrue(isBlake3Available(), "This test requires BLAKE3 algorithm");
            assertThatThrownBy(() -> Blake3Hash.create(0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("positive");
        }

        @Test
        @DisplayName("create(-1)抛出异常")
        void testCreateNegativeLength() {
            assumeTrue(isBlake3Available(), "This test requires BLAKE3 algorithm");
            assertThatThrownBy(() -> Blake3Hash.create(-1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("positive");
        }

        @Test
        @DisplayName("无BC库或BLAKE3算法不可用抛出异常")
        void testNoBouncyCastleOrAlgorithm() {
            // This test only makes sense when BLAKE3 is not available
            if (isBlake3Available()) {
                // Skip this test if BLAKE3 is fully available
                return;
            }
            // BC not present - should throw on create()
            if (Security.getProvider("BC") == null) {
                assertThatThrownBy(Blake3Hash::create)
                        .isInstanceOf(OpenCryptoException.class)
                        .hasMessageContaining("Bouncy Castle");
            } else {
                // BC present but BLAKE3 not supported - should throw on hash()
                Blake3Hash hash = Blake3Hash.create();
                assertThatThrownBy(() -> hash.hash("test"))
                        .isInstanceOf(OpenCryptoException.class);
            }
        }
    }

    @Nested
    @DisplayName("Hash Tests / 哈希测试")
    class HashTests {

        @Test
        @DisplayName("hash(byte[])返回正确长度")
        void testHashBytesLength() {
            assumeTrue(isBlake3Available(), "This test requires BLAKE3 algorithm");
            Blake3Hash hash = Blake3Hash.create();
            byte[] result = hash.hash(TEST_BYTES);
            assertThat(result).hasSize(32);
        }

        @Test
        @DisplayName("hash(String)返回正确长度")
        void testHashStringLength() {
            assumeTrue(isBlake3Available(), "This test requires BLAKE3 algorithm");
            Blake3Hash hash = Blake3Hash.create();
            byte[] result = hash.hash(TEST_DATA);
            assertThat(result).hasSize(32);
        }

        @Test
        @DisplayName("hash(null byte[])抛出异常")
        void testHashNullBytes() {
            assumeTrue(isBlake3Available(), "This test requires BLAKE3 algorithm");
            Blake3Hash hash = Blake3Hash.create();
            assertThatThrownBy(() -> hash.hash((byte[]) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("hash(null String)抛出异常")
        void testHashNullString() {
            assumeTrue(isBlake3Available(), "This test requires BLAKE3 algorithm");
            Blake3Hash hash = Blake3Hash.create();
            assertThatThrownBy(() -> hash.hash((String) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("hash相同数据返回相同结果")
        void testHashDeterministic() {
            assumeTrue(isBlake3Available(), "This test requires BLAKE3 algorithm");
            Blake3Hash hash = Blake3Hash.create();
            byte[] result1 = hash.hash(TEST_DATA);
            byte[] result2 = hash.hash(TEST_DATA);
            assertThat(result1).isEqualTo(result2);
        }

        @Test
        @DisplayName("hash不同数据返回不同结果")
        void testHashDifferentData() {
            assumeTrue(isBlake3Available(), "This test requires BLAKE3 algorithm");
            Blake3Hash hash = Blake3Hash.create();
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
            assumeTrue(isBlake3Available(), "This test requires BLAKE3 algorithm");
            Blake3Hash hash = Blake3Hash.create();
            String hex = hash.hashHex(TEST_BYTES);
            assertThat(hex).isNotNull();
            assertThat(hex).hasSize(64);
            assertThat(hex).matches("[0-9a-f]+");
        }

        @Test
        @DisplayName("hashHex(String)返回十六进制字符串")
        void testHashHexString() {
            assumeTrue(isBlake3Available(), "This test requires BLAKE3 algorithm");
            Blake3Hash hash = Blake3Hash.create();
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
            assumeTrue(isBlake3Available(), "This test requires BLAKE3 algorithm");
            Blake3Hash hash = Blake3Hash.create();
            String base64 = hash.hashBase64(TEST_BYTES);
            assertThat(base64).isNotNull();
            assertThat(base64).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("GetDigestLength Tests / 获取摘要长度测试")
    class GetDigestLengthTests {

        @Test
        @DisplayName("默认摘要长度为32")
        void testDigestLengthDefault() {
            assumeTrue(isBlake3Available(), "This test requires BLAKE3 algorithm");
            Blake3Hash hash = Blake3Hash.create();
            assertThat(hash.getDigestLength()).isEqualTo(32);
        }

        @Test
        @DisplayName("自定义摘要长度")
        void testDigestLengthCustom() {
            assumeTrue(isBlake3Available(), "This test requires BLAKE3 algorithm");
            Blake3Hash hash = Blake3Hash.create(64);
            assertThat(hash.getDigestLength()).isEqualTo(64);
        }
    }

    @Nested
    @DisplayName("GetAlgorithm Tests / 获取算法名称测试")
    class GetAlgorithmTests {

        @Test
        @DisplayName("算法名称为BLAKE3")
        void testAlgorithm() {
            assumeTrue(isBlake3Available(), "This test requires BLAKE3 algorithm");
            Blake3Hash hash = Blake3Hash.create();
            assertThat(hash.getAlgorithm()).isEqualTo("BLAKE3");
        }
    }

    @Nested
    @DisplayName("Interface Implementation Tests / 接口实现测试")
    class InterfaceImplementationTests {

        @Test
        @DisplayName("Blake3Hash实现HashFunction接口")
        void testImplementsHashFunction() {
            assumeTrue(isBlake3Available(), "This test requires BLAKE3 algorithm");
            Blake3Hash hash = Blake3Hash.create();
            assertThat(hash).isInstanceOf(HashFunction.class);
        }
    }
}
