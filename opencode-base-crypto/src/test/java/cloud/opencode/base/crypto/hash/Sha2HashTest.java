package cloud.opencode.base.crypto.hash;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;

/**
 * Sha2Hash 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("Sha2Hash 测试")
class Sha2HashTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("创建SHA-224")
        void testSha224Factory() {
            Sha2Hash hash = Sha2Hash.sha224();
            assertThat(hash.getAlgorithm()).isEqualTo("SHA-224");
            assertThat(hash.getDigestLength()).isEqualTo(28);
        }

        @Test
        @DisplayName("创建SHA-256")
        void testSha256Factory() {
            Sha2Hash hash = Sha2Hash.sha256();
            assertThat(hash.getAlgorithm()).isEqualTo("SHA-256");
            assertThat(hash.getDigestLength()).isEqualTo(32);
        }

        @Test
        @DisplayName("创建SHA-384")
        void testSha384Factory() {
            Sha2Hash hash = Sha2Hash.sha384();
            assertThat(hash.getAlgorithm()).isEqualTo("SHA-384");
            assertThat(hash.getDigestLength()).isEqualTo(48);
        }

        @Test
        @DisplayName("创建SHA-512")
        void testSha512Factory() {
            Sha2Hash hash = Sha2Hash.sha512();
            assertThat(hash.getAlgorithm()).isEqualTo("SHA-512");
            assertThat(hash.getDigestLength()).isEqualTo(64);
        }
    }

    @Nested
    @DisplayName("hash(byte[]) 测试")
    class HashByteArrayTests {

        @Test
        @DisplayName("SHA-256哈希字节数组")
        void testHashByteArray() {
            Sha2Hash hash = Sha2Hash.sha256();
            byte[] data = "hello".getBytes(StandardCharsets.UTF_8);
            byte[] result = hash.hash(data);

            assertThat(result).hasSize(32);
        }

        @Test
        @DisplayName("相同输入相同输出")
        void testHashDeterministic() {
            Sha2Hash hash = Sha2Hash.sha256();
            byte[] data = "test".getBytes(StandardCharsets.UTF_8);

            assertThat(hash.hash(data)).isEqualTo(hash.hash(data));
        }

        @Test
        @DisplayName("不同输入不同输出")
        void testHashDifferentInputs() {
            Sha2Hash hash = Sha2Hash.sha256();
            byte[] data1 = "hello".getBytes(StandardCharsets.UTF_8);
            byte[] data2 = "world".getBytes(StandardCharsets.UTF_8);

            assertThat(hash.hash(data1)).isNotEqualTo(hash.hash(data2));
        }

        @Test
        @DisplayName("null输入抛出异常")
        void testHashNullByteArray() {
            Sha2Hash hash = Sha2Hash.sha256();

            assertThatThrownBy(() -> hash.hash((byte[]) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("空数组有效")
        void testHashEmptyArray() {
            Sha2Hash hash = Sha2Hash.sha256();
            byte[] result = hash.hash(new byte[0]);

            assertThat(result).hasSize(32);
        }
    }

    @Nested
    @DisplayName("hash(String) 测试")
    class HashStringTests {

        @Test
        @DisplayName("SHA-256哈希字符串")
        void testHashString() {
            Sha2Hash hash = Sha2Hash.sha256();
            byte[] result = hash.hash("hello");

            assertThat(result).hasSize(32);
        }

        @Test
        @DisplayName("字符串和字节数组哈希一致")
        void testHashStringEqualsBytes() {
            Sha2Hash hash = Sha2Hash.sha256();
            String text = "test string";
            byte[] bytes = text.getBytes(StandardCharsets.UTF_8);

            assertThat(hash.hash(text)).isEqualTo(hash.hash(bytes));
        }

        @Test
        @DisplayName("null字符串抛出异常")
        void testHashNullString() {
            Sha2Hash hash = Sha2Hash.sha256();

            assertThatThrownBy(() -> hash.hash((String) null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("hashHex 测试")
    class HashHexTests {

        @Test
        @DisplayName("hashHex返回十六进制字符串")
        void testHashHex() {
            Sha2Hash hash = Sha2Hash.sha256();
            String result = hash.hashHex("hello".getBytes(StandardCharsets.UTF_8));

            assertThat(result).hasSize(64); // 32字节 = 64个十六进制字符
            assertThat(result).matches("[0-9a-f]+");
        }

        @Test
        @DisplayName("hashHex(String)返回十六进制字符串")
        void testHashHexString() {
            Sha2Hash hash = Sha2Hash.sha256();
            String result = hash.hashHex("hello");

            assertThat(result).hasSize(64);
            assertThat(result).matches("[0-9a-f]+");
        }

        @Test
        @DisplayName("SHA-256 hello的已知哈希值")
        void testKnownHash() {
            Sha2Hash hash = Sha2Hash.sha256();
            String result = hash.hashHex("hello");

            // SHA-256("hello") = 2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824
            assertThat(result).isEqualTo("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824");
        }
    }

    @Nested
    @DisplayName("hashBase64 测试")
    class HashBase64Tests {

        @Test
        @DisplayName("hashBase64返回Base64字符串")
        void testHashBase64() {
            Sha2Hash hash = Sha2Hash.sha256();
            String result = hash.hashBase64("hello".getBytes(StandardCharsets.UTF_8));

            // 32字节Base64编码 = 44字符（含填充）
            assertThat(result).matches("[A-Za-z0-9+/=]+");
        }
    }

    @Nested
    @DisplayName("不同算法测试")
    class DifferentAlgorithmsTests {

        @Test
        @DisplayName("不同算法产生不同长度的哈希")
        void testDifferentAlgorithms() {
            String data = "test";

            assertThat(Sha2Hash.sha224().hash(data)).hasSize(28);
            assertThat(Sha2Hash.sha256().hash(data)).hasSize(32);
            assertThat(Sha2Hash.sha384().hash(data)).hasSize(48);
            assertThat(Sha2Hash.sha512().hash(data)).hasSize(64);
        }

        @Test
        @DisplayName("不同算法产生不同的哈希值")
        void testDifferentAlgorithmsDifferentHashes() {
            String data = "test";

            byte[] sha256 = Sha2Hash.sha256().hash(data);
            byte[] sha512 = Sha2Hash.sha512().hash(data);

            // 即使只比较前32字节也不应该相同
            assertThat(sha256).isNotEqualTo(java.util.Arrays.copyOf(sha512, 32));
        }
    }

    @Nested
    @DisplayName("HashFunction接口实现测试")
    class HashFunctionInterfaceTests {

        @Test
        @DisplayName("实现HashFunction接口")
        void testImplementsHashFunction() {
            Sha2Hash hash = Sha2Hash.sha256();
            assertThat(hash).isInstanceOf(HashFunction.class);
        }

        @Test
        @DisplayName("getAlgorithm返回正确算法名")
        void testGetAlgorithm() {
            assertThat(Sha2Hash.sha224().getAlgorithm()).isEqualTo("SHA-224");
            assertThat(Sha2Hash.sha256().getAlgorithm()).isEqualTo("SHA-256");
            assertThat(Sha2Hash.sha384().getAlgorithm()).isEqualTo("SHA-384");
            assertThat(Sha2Hash.sha512().getAlgorithm()).isEqualTo("SHA-512");
        }

        @Test
        @DisplayName("getDigestLength返回正确长度")
        void testGetDigestLength() {
            assertThat(Sha2Hash.sha224().getDigestLength()).isEqualTo(28);
            assertThat(Sha2Hash.sha256().getDigestLength()).isEqualTo(32);
            assertThat(Sha2Hash.sha384().getDigestLength()).isEqualTo(48);
            assertThat(Sha2Hash.sha512().getDigestLength()).isEqualTo(64);
        }
    }
}
