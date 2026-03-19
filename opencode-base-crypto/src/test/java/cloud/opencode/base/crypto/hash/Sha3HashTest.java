package cloud.opencode.base.crypto.hash;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link Sha3Hash}.
 * SHA-3哈希函数单元测试。
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("Sha3Hash Tests / SHA-3哈希函数测试")
class Sha3HashTest {

    private static final String TEST_DATA = "Hello, World!";
    private static final byte[] TEST_BYTES = "Hello, World!".getBytes();

    @Nested
    @DisplayName("Factory Method Tests / 工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("sha3_224()创建SHA3-224哈希器")
        void testSha3_224() {
            Sha3Hash hash = Sha3Hash.sha3_224();
            assertThat(hash).isNotNull();
            assertThat(hash.getAlgorithm()).isEqualTo("SHA3-224");
            assertThat(hash.getDigestLength()).isEqualTo(28);
        }

        @Test
        @DisplayName("sha3_256()创建SHA3-256哈希器")
        void testSha3_256() {
            Sha3Hash hash = Sha3Hash.sha3_256();
            assertThat(hash).isNotNull();
            assertThat(hash.getAlgorithm()).isEqualTo("SHA3-256");
            assertThat(hash.getDigestLength()).isEqualTo(32);
        }

        @Test
        @DisplayName("sha3_384()创建SHA3-384哈希器")
        void testSha3_384() {
            Sha3Hash hash = Sha3Hash.sha3_384();
            assertThat(hash).isNotNull();
            assertThat(hash.getAlgorithm()).isEqualTo("SHA3-384");
            assertThat(hash.getDigestLength()).isEqualTo(48);
        }

        @Test
        @DisplayName("sha3_512()创建SHA3-512哈希器")
        void testSha3_512() {
            Sha3Hash hash = Sha3Hash.sha3_512();
            assertThat(hash).isNotNull();
            assertThat(hash.getAlgorithm()).isEqualTo("SHA3-512");
            assertThat(hash.getDigestLength()).isEqualTo(64);
        }
    }

    @Nested
    @DisplayName("Hash Tests / 哈希测试")
    class HashTests {

        @Test
        @DisplayName("hash(byte[])返回正确长度")
        void testHashBytesLength() {
            Sha3Hash hash224 = Sha3Hash.sha3_224();
            Sha3Hash hash256 = Sha3Hash.sha3_256();
            Sha3Hash hash384 = Sha3Hash.sha3_384();
            Sha3Hash hash512 = Sha3Hash.sha3_512();

            assertThat(hash224.hash(TEST_BYTES)).hasSize(28);
            assertThat(hash256.hash(TEST_BYTES)).hasSize(32);
            assertThat(hash384.hash(TEST_BYTES)).hasSize(48);
            assertThat(hash512.hash(TEST_BYTES)).hasSize(64);
        }

        @Test
        @DisplayName("hash(String)返回正确长度")
        void testHashStringLength() {
            Sha3Hash hash256 = Sha3Hash.sha3_256();
            byte[] result = hash256.hash(TEST_DATA);
            assertThat(result).hasSize(32);
        }

        @Test
        @DisplayName("hash(null byte[])抛出异常")
        void testHashNullBytes() {
            Sha3Hash hash = Sha3Hash.sha3_256();
            assertThatThrownBy(() -> hash.hash((byte[]) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("hash(null String)抛出异常")
        void testHashNullString() {
            Sha3Hash hash = Sha3Hash.sha3_256();
            assertThatThrownBy(() -> hash.hash((String) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("hash相同数据返回相同结果")
        void testHashDeterministic() {
            Sha3Hash hash = Sha3Hash.sha3_256();
            byte[] result1 = hash.hash(TEST_DATA);
            byte[] result2 = hash.hash(TEST_DATA);
            assertThat(result1).isEqualTo(result2);
        }

        @Test
        @DisplayName("hash不同数据返回不同结果")
        void testHashDifferentData() {
            Sha3Hash hash = Sha3Hash.sha3_256();
            byte[] result1 = hash.hash("Hello");
            byte[] result2 = hash.hash("World");
            assertThat(result1).isNotEqualTo(result2);
        }

        @Test
        @DisplayName("hash空数据")
        void testHashEmptyData() {
            Sha3Hash hash = Sha3Hash.sha3_256();
            byte[] result = hash.hash("");
            assertThat(result).hasSize(32);
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("hash空字节数组")
        void testHashEmptyBytes() {
            Sha3Hash hash = Sha3Hash.sha3_256();
            byte[] result = hash.hash(new byte[0]);
            assertThat(result).hasSize(32);
        }
    }

    @Nested
    @DisplayName("HashHex Tests / 十六进制哈希测试")
    class HashHexTests {

        @Test
        @DisplayName("hashHex(byte[])返回十六进制字符串")
        void testHashHexBytes() {
            Sha3Hash hash = Sha3Hash.sha3_256();
            String hex = hash.hashHex(TEST_BYTES);
            assertThat(hex).isNotNull();
            assertThat(hex).hasSize(64); // 32 bytes * 2
            assertThat(hex).matches("[0-9a-f]+");
        }

        @Test
        @DisplayName("hashHex(String)返回十六进制字符串")
        void testHashHexString() {
            Sha3Hash hash = Sha3Hash.sha3_256();
            String hex = hash.hashHex(TEST_DATA);
            assertThat(hex).isNotNull();
            assertThat(hex).hasSize(64);
        }

        @Test
        @DisplayName("hashHex相同数据返回相同结果")
        void testHashHexDeterministic() {
            Sha3Hash hash = Sha3Hash.sha3_256();
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
            Sha3Hash hash = Sha3Hash.sha3_256();
            String base64 = hash.hashBase64(TEST_BYTES);
            assertThat(base64).isNotNull();
            assertThat(base64).isNotEmpty();
        }

        @Test
        @DisplayName("hashBase64相同数据返回相同结果")
        void testHashBase64Deterministic() {
            Sha3Hash hash = Sha3Hash.sha3_256();
            String base641 = hash.hashBase64(TEST_BYTES);
            String base642 = hash.hashBase64(TEST_BYTES);
            assertThat(base641).isEqualTo(base642);
        }
    }

    @Nested
    @DisplayName("GetDigestLength Tests / 获取摘要长度测试")
    class GetDigestLengthTests {

        @Test
        @DisplayName("SHA3-224摘要长度为28")
        void testDigestLength224() {
            Sha3Hash hash = Sha3Hash.sha3_224();
            assertThat(hash.getDigestLength()).isEqualTo(28);
        }

        @Test
        @DisplayName("SHA3-256摘要长度为32")
        void testDigestLength256() {
            Sha3Hash hash = Sha3Hash.sha3_256();
            assertThat(hash.getDigestLength()).isEqualTo(32);
        }

        @Test
        @DisplayName("SHA3-384摘要长度为48")
        void testDigestLength384() {
            Sha3Hash hash = Sha3Hash.sha3_384();
            assertThat(hash.getDigestLength()).isEqualTo(48);
        }

        @Test
        @DisplayName("SHA3-512摘要长度为64")
        void testDigestLength512() {
            Sha3Hash hash = Sha3Hash.sha3_512();
            assertThat(hash.getDigestLength()).isEqualTo(64);
        }
    }

    @Nested
    @DisplayName("GetAlgorithm Tests / 获取算法名称测试")
    class GetAlgorithmTests {

        @Test
        @DisplayName("SHA3-224算法名称正确")
        void testAlgorithm224() {
            Sha3Hash hash = Sha3Hash.sha3_224();
            assertThat(hash.getAlgorithm()).isEqualTo("SHA3-224");
        }

        @Test
        @DisplayName("SHA3-256算法名称正确")
        void testAlgorithm256() {
            Sha3Hash hash = Sha3Hash.sha3_256();
            assertThat(hash.getAlgorithm()).isEqualTo("SHA3-256");
        }

        @Test
        @DisplayName("SHA3-384算法名称正确")
        void testAlgorithm384() {
            Sha3Hash hash = Sha3Hash.sha3_384();
            assertThat(hash.getAlgorithm()).isEqualTo("SHA3-384");
        }

        @Test
        @DisplayName("SHA3-512算法名称正确")
        void testAlgorithm512() {
            Sha3Hash hash = Sha3Hash.sha3_512();
            assertThat(hash.getAlgorithm()).isEqualTo("SHA3-512");
        }
    }

    @Nested
    @DisplayName("Special Data Tests / 特殊数据测试")
    class SpecialDataTests {

        @Test
        @DisplayName("hash处理Unicode数据")
        void testHashUnicode() {
            Sha3Hash hash = Sha3Hash.sha3_256();
            byte[] result = hash.hash("你好世界🌍");
            assertThat(result).hasSize(32);
        }

        @Test
        @DisplayName("hash处理大数据")
        void testHashLargeData() {
            Sha3Hash hash = Sha3Hash.sha3_256();
            String largeData = "a".repeat(100000);
            byte[] result = hash.hash(largeData);
            assertThat(result).hasSize(32);
        }

        @Test
        @DisplayName("hash处理特殊字符")
        void testHashSpecialCharacters() {
            Sha3Hash hash = Sha3Hash.sha3_256();
            byte[] result = hash.hash("!@#$%^&*()_+-=[]{}|;':\",./<>?");
            assertThat(result).hasSize(32);
        }
    }

    @Nested
    @DisplayName("Interface Implementation Tests / 接口实现测试")
    class InterfaceImplementationTests {

        @Test
        @DisplayName("Sha3Hash实现HashFunction接口")
        void testImplementsHashFunction() {
            Sha3Hash hash = Sha3Hash.sha3_256();
            assertThat(hash).isInstanceOf(HashFunction.class);
        }
    }
}
