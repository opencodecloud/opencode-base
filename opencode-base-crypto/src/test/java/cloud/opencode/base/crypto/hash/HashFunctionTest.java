package cloud.opencode.base.crypto.hash;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link HashFunction} interface.
 * HashFunction接口单元测试。
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("HashFunction Interface Tests / HashFunction接口测试")
class HashFunctionTest {

    private static final String TEST_DATA = "Hello, World!";
    private static final byte[] TEST_BYTES = "Hello, World!".getBytes();

    @Nested
    @DisplayName("Interface Contract Tests / 接口契约测试")
    class InterfaceContractTests {

        @Test
        @DisplayName("Sha2Hash实现HashFunction接口")
        void testSha2HashImplementsInterface() {
            HashFunction hash = Sha2Hash.sha256();
            assertThat(hash).isInstanceOf(HashFunction.class);
        }

        @Test
        @DisplayName("Sha3Hash实现HashFunction接口")
        void testSha3HashImplementsInterface() {
            HashFunction hash = Sha3Hash.sha3_256();
            assertThat(hash).isInstanceOf(HashFunction.class);
        }

        @Test
        @DisplayName("通过接口调用hash(byte[])方法")
        void testHashBytesThroughInterface() {
            HashFunction hash = Sha2Hash.sha256();
            byte[] result = hash.hash(TEST_BYTES);
            assertThat(result).isNotNull();
            assertThat(result).hasSize(32);
        }

        @Test
        @DisplayName("通过接口调用hash(String)方法")
        void testHashStringThroughInterface() {
            HashFunction hash = Sha2Hash.sha256();
            byte[] result = hash.hash(TEST_DATA);
            assertThat(result).isNotNull();
            assertThat(result).hasSize(32);
        }

        @Test
        @DisplayName("通过接口调用hashHex方法")
        void testHashHexThroughInterface() {
            HashFunction hash = Sha2Hash.sha256();
            String hex = hash.hashHex(TEST_BYTES);
            assertThat(hex).isNotNull();
            assertThat(hex).hasSize(64);
            assertThat(hex).matches("[0-9a-f]+");
        }

        @Test
        @DisplayName("通过接口调用hashBase64方法")
        void testHashBase64ThroughInterface() {
            HashFunction hash = Sha2Hash.sha256();
            String base64 = hash.hashBase64(TEST_BYTES);
            assertThat(base64).isNotNull();
            assertThat(base64).isNotEmpty();
        }

        @Test
        @DisplayName("通过接口调用getDigestLength方法")
        void testGetDigestLengthThroughInterface() {
            HashFunction hash = Sha2Hash.sha256();
            int length = hash.getDigestLength();
            assertThat(length).isEqualTo(32);
        }

        @Test
        @DisplayName("通过接口调用getAlgorithm方法")
        void testGetAlgorithmThroughInterface() {
            HashFunction hash = Sha2Hash.sha256();
            String algorithm = hash.getAlgorithm();
            assertThat(algorithm).isEqualTo("SHA-256");
        }
    }

    @Nested
    @DisplayName("Polymorphism Tests / 多态测试")
    class PolymorphismTests {

        @Test
        @DisplayName("不同实现通过接口引用")
        void testDifferentImplementations() {
            HashFunction sha256 = Sha2Hash.sha256();
            HashFunction sha512 = Sha2Hash.sha512();
            HashFunction sha3_256 = Sha3Hash.sha3_256();

            assertThat(sha256.getDigestLength()).isEqualTo(32);
            assertThat(sha512.getDigestLength()).isEqualTo(64);
            assertThat(sha3_256.getDigestLength()).isEqualTo(32);
        }

        @Test
        @DisplayName("相同数据通过不同实现产生不同哈希")
        void testDifferentHashesFromDifferentAlgorithms() {
            HashFunction sha256 = Sha2Hash.sha256();
            HashFunction sha3_256 = Sha3Hash.sha3_256();

            String hash1 = sha256.hashHex(TEST_DATA);
            String hash2 = sha3_256.hashHex(TEST_DATA);

            assertThat(hash1).isNotEqualTo(hash2);
        }

        @Test
        @DisplayName("通过接口数组批量处理")
        void testBatchProcessingThroughInterface() {
            HashFunction[] hashers = {
                    Sha2Hash.sha256(),
                    Sha2Hash.sha512(),
                    Sha3Hash.sha3_256()
            };

            for (HashFunction hasher : hashers) {
                byte[] result = hasher.hash(TEST_DATA);
                assertThat(result).isNotNull();
                assertThat(result).hasSize(hasher.getDigestLength());
            }
        }
    }

    @Nested
    @DisplayName("Consistency Tests / 一致性测试")
    class ConsistencyTests {

        @Test
        @DisplayName("hash(byte[])和hash(String)结果一致")
        void testHashBytesAndStringConsistent() {
            HashFunction hash = Sha2Hash.sha256();
            byte[] fromBytes = hash.hash(TEST_BYTES);
            byte[] fromString = hash.hash(TEST_DATA);
            assertThat(fromBytes).isEqualTo(fromString);
        }

        @Test
        @DisplayName("hashHex与hash结果一致")
        void testHashHexConsistent() {
            HashFunction hash = Sha2Hash.sha256();
            byte[] raw = hash.hash(TEST_DATA);
            String hex = hash.hashHex(TEST_DATA);

            // Convert hex to bytes and compare
            StringBuilder sb = new StringBuilder();
            for (byte b : raw) {
                sb.append(String.format("%02x", b));
            }
            assertThat(hex).isEqualTo(sb.toString());
        }

        @Test
        @DisplayName("多次调用结果相同")
        void testDeterministic() {
            HashFunction hash = Sha2Hash.sha256();
            byte[] result1 = hash.hash(TEST_DATA);
            byte[] result2 = hash.hash(TEST_DATA);
            byte[] result3 = hash.hash(TEST_DATA);

            assertThat(result1).isEqualTo(result2);
            assertThat(result2).isEqualTo(result3);
        }
    }
}
