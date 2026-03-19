package cloud.opencode.base.crypto.kdf;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;

/**
 * Hkdf 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("Hkdf 测试")
class HkdfTest {

    private static final byte[] TEST_IKM = "input-key-material".getBytes(StandardCharsets.UTF_8);
    private static final byte[] TEST_SALT = "random-salt-value".getBytes(StandardCharsets.UTF_8);
    private static final byte[] TEST_INFO = "context-info".getBytes(StandardCharsets.UTF_8);

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("sha256创建实例")
        void testSha256() {
            Hkdf hkdf = Hkdf.sha256();
            assertThat(hkdf).isNotNull();
            assertThat(hkdf.getAlgorithm()).isEqualTo("HmacSHA256");
            assertThat(hkdf.getHashLength()).isEqualTo(32);
        }

        @Test
        @DisplayName("sha384创建实例")
        void testSha384() {
            Hkdf hkdf = Hkdf.sha384();
            assertThat(hkdf).isNotNull();
            assertThat(hkdf.getAlgorithm()).isEqualTo("HmacSHA384");
            assertThat(hkdf.getHashLength()).isEqualTo(48);
        }

        @Test
        @DisplayName("sha512创建实例")
        void testSha512() {
            Hkdf hkdf = Hkdf.sha512();
            assertThat(hkdf).isNotNull();
            assertThat(hkdf.getAlgorithm()).isEqualTo("HmacSHA512");
            assertThat(hkdf.getHashLength()).isEqualTo(64);
        }
    }

    @Nested
    @DisplayName("KdfEngine接口测试")
    class KdfEngineTests {

        @Test
        @DisplayName("实现KdfEngine接口")
        void testImplementsKdfEngine() {
            Hkdf hkdf = Hkdf.sha256();
            assertThat(hkdf).isInstanceOf(KdfEngine.class);
        }

        @Test
        @DisplayName("derive(ikm, salt, info, length)")
        void testDeriveWithAllParams() {
            Hkdf hkdf = Hkdf.sha256();
            byte[] result = hkdf.derive(TEST_IKM, TEST_SALT, TEST_INFO, 32);

            assertThat(result).hasSize(32);
        }

        @Test
        @DisplayName("derive(ikm, length)")
        void testDeriveSimple() {
            Hkdf hkdf = Hkdf.sha256();
            byte[] result = hkdf.derive(TEST_IKM, 32);

            assertThat(result).hasSize(32);
        }
    }

    @Nested
    @DisplayName("deriveKey测试")
    class DeriveKeyTests {

        @Test
        @DisplayName("deriveKey带salt和info")
        void testDeriveKeyWithSaltAndInfo() {
            Hkdf hkdf = Hkdf.sha256();
            byte[] result = hkdf.deriveKey(TEST_IKM, TEST_SALT, TEST_INFO, 32);

            assertThat(result).hasSize(32);
        }

        @Test
        @DisplayName("deriveKey只带info")
        void testDeriveKeyWithInfo() {
            Hkdf hkdf = Hkdf.sha256();
            byte[] result = hkdf.deriveKey(TEST_IKM, TEST_INFO, 32);

            assertThat(result).hasSize(32);
        }

        @Test
        @DisplayName("deriveKey null盐值使用默认值")
        void testDeriveKeyNullSalt() {
            Hkdf hkdf = Hkdf.sha256();
            byte[] result = hkdf.deriveKey(TEST_IKM, null, TEST_INFO, 32);

            assertThat(result).hasSize(32);
        }

        @Test
        @DisplayName("deriveKey空盐值使用默认值")
        void testDeriveKeyEmptySalt() {
            Hkdf hkdf = Hkdf.sha256();
            byte[] result = hkdf.deriveKey(TEST_IKM, new byte[0], TEST_INFO, 32);

            assertThat(result).hasSize(32);
        }

        @Test
        @DisplayName("deriveKey null info")
        void testDeriveKeyNullInfo() {
            Hkdf hkdf = Hkdf.sha256();
            byte[] result = hkdf.deriveKey(TEST_IKM, TEST_SALT, null, 32);

            assertThat(result).hasSize(32);
        }

        @Test
        @DisplayName("deriveKey null ikm抛出异常")
        void testDeriveKeyNullIkm() {
            Hkdf hkdf = Hkdf.sha256();

            assertThatThrownBy(() -> hkdf.deriveKey(null, TEST_SALT, TEST_INFO, 32))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("deriveKey无效长度抛出异常")
        void testDeriveKeyInvalidLength() {
            Hkdf hkdf = Hkdf.sha256();

            assertThatThrownBy(() -> hkdf.deriveKey(TEST_IKM, TEST_SALT, TEST_INFO, 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("deriveKey长度过大抛出异常")
        void testDeriveKeyLengthTooLarge() {
            Hkdf hkdf = Hkdf.sha256();
            int maxLength = 255 * 32; // 255 * hashLength

            assertThatThrownBy(() -> hkdf.deriveKey(TEST_IKM, TEST_SALT, TEST_INFO, maxLength + 1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("deriveKey确定性")
        void testDeriveKeyDeterministic() {
            Hkdf hkdf = Hkdf.sha256();

            byte[] result1 = hkdf.deriveKey(TEST_IKM, TEST_SALT, TEST_INFO, 32);
            byte[] result2 = hkdf.deriveKey(TEST_IKM, TEST_SALT, TEST_INFO, 32);

            assertThat(result1).isEqualTo(result2);
        }

        @Test
        @DisplayName("deriveKey不同输入产生不同输出")
        void testDeriveKeyDifferentInputs() {
            Hkdf hkdf = Hkdf.sha256();

            byte[] result1 = hkdf.deriveKey(TEST_IKM, TEST_SALT, TEST_INFO, 32);
            byte[] result2 = hkdf.deriveKey("different".getBytes(StandardCharsets.UTF_8), TEST_SALT, TEST_INFO, 32);

            assertThat(result1).isNotEqualTo(result2);
        }

        @Test
        @DisplayName("deriveKey多种长度")
        void testDeriveKeyVariousLengths() {
            Hkdf hkdf = Hkdf.sha256();

            assertThat(hkdf.deriveKey(TEST_IKM, TEST_INFO, 16)).hasSize(16);
            assertThat(hkdf.deriveKey(TEST_IKM, TEST_INFO, 32)).hasSize(32);
            assertThat(hkdf.deriveKey(TEST_IKM, TEST_INFO, 64)).hasSize(64);
            assertThat(hkdf.deriveKey(TEST_IKM, TEST_INFO, 100)).hasSize(100);
        }
    }

    @Nested
    @DisplayName("extract测试")
    class ExtractTests {

        @Test
        @DisplayName("extract返回正确长度")
        void testExtract() {
            Hkdf hkdf = Hkdf.sha256();
            byte[] prk = hkdf.extract(TEST_SALT, TEST_IKM);

            assertThat(prk).hasSize(32);
        }

        @Test
        @DisplayName("extract null salt使用默认值")
        void testExtractNullSalt() {
            Hkdf hkdf = Hkdf.sha256();
            byte[] prk = hkdf.extract(null, TEST_IKM);

            assertThat(prk).hasSize(32);
        }

        @Test
        @DisplayName("extract null ikm抛出异常")
        void testExtractNullIkm() {
            Hkdf hkdf = Hkdf.sha256();

            assertThatThrownBy(() -> hkdf.extract(TEST_SALT, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("extract确定性")
        void testExtractDeterministic() {
            Hkdf hkdf = Hkdf.sha256();

            byte[] prk1 = hkdf.extract(TEST_SALT, TEST_IKM);
            byte[] prk2 = hkdf.extract(TEST_SALT, TEST_IKM);

            assertThat(prk1).isEqualTo(prk2);
        }
    }

    @Nested
    @DisplayName("expand测试")
    class ExpandTests {

        @Test
        @DisplayName("expand返回正确长度")
        void testExpand() {
            Hkdf hkdf = Hkdf.sha256();
            byte[] prk = hkdf.extract(TEST_SALT, TEST_IKM);
            byte[] okm = hkdf.expand(prk, TEST_INFO, 64);

            assertThat(okm).hasSize(64);
        }

        @Test
        @DisplayName("expand null info")
        void testExpandNullInfo() {
            Hkdf hkdf = Hkdf.sha256();
            byte[] prk = hkdf.extract(TEST_SALT, TEST_IKM);
            byte[] okm = hkdf.expand(prk, null, 32);

            assertThat(okm).hasSize(32);
        }

        @Test
        @DisplayName("expand null prk抛出异常")
        void testExpandNullPrk() {
            Hkdf hkdf = Hkdf.sha256();

            assertThatThrownBy(() -> hkdf.expand(null, TEST_INFO, 32))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("expand无效长度抛出异常")
        void testExpandInvalidLength() {
            Hkdf hkdf = Hkdf.sha256();
            byte[] prk = hkdf.extract(TEST_SALT, TEST_IKM);

            assertThatThrownBy(() -> hkdf.expand(prk, TEST_INFO, 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("expand长度过大抛出异常")
        void testExpandLengthTooLarge() {
            Hkdf hkdf = Hkdf.sha256();
            byte[] prk = hkdf.extract(TEST_SALT, TEST_IKM);
            int maxLength = 255 * 32;

            assertThatThrownBy(() -> hkdf.expand(prk, TEST_INFO, maxLength + 1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("extractAndExpand测试")
    class ExtractAndExpandTests {

        @Test
        @DisplayName("extractAndExpand等同于deriveKey")
        void testExtractAndExpandEquivalent() {
            Hkdf hkdf = Hkdf.sha256();

            byte[] result1 = hkdf.extractAndExpand(TEST_SALT, TEST_IKM, TEST_INFO, 32);
            byte[] result2 = hkdf.deriveKey(TEST_IKM, TEST_SALT, TEST_INFO, 32);

            assertThat(result1).isEqualTo(result2);
        }
    }

    @Nested
    @DisplayName("deriveKeys测试")
    class DeriveKeysTests {

        @Test
        @DisplayName("deriveKeys返回多个密钥")
        void testDeriveKeys() {
            Hkdf hkdf = Hkdf.sha256();
            byte[][] infos = {
                "key1".getBytes(StandardCharsets.UTF_8),
                "key2".getBytes(StandardCharsets.UTF_8),
                "key3".getBytes(StandardCharsets.UTF_8)
            };
            int[] lengths = {16, 32, 64};

            byte[][] keys = hkdf.deriveKeys(TEST_SALT, TEST_IKM, infos, lengths);

            assertThat(keys.length).isEqualTo(3);
            assertThat(keys[0]).hasSize(16);
            assertThat(keys[1]).hasSize(32);
            assertThat(keys[2]).hasSize(64);
        }

        @Test
        @DisplayName("deriveKeys派生的密钥不同")
        void testDeriveKeysDifferent() {
            Hkdf hkdf = Hkdf.sha256();
            byte[][] infos = {
                "key1".getBytes(StandardCharsets.UTF_8),
                "key2".getBytes(StandardCharsets.UTF_8)
            };
            int[] lengths = {32, 32};

            byte[][] keys = hkdf.deriveKeys(TEST_SALT, TEST_IKM, infos, lengths);

            assertThat(keys[0]).isNotEqualTo(keys[1]);
        }

        @Test
        @DisplayName("deriveKeys null ikm抛出异常")
        void testDeriveKeysNullIkm() {
            Hkdf hkdf = Hkdf.sha256();
            byte[][] infos = {TEST_INFO};
            int[] lengths = {32};

            assertThatThrownBy(() -> hkdf.deriveKeys(TEST_SALT, null, infos, lengths))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("deriveKeys null infos抛出异常")
        void testDeriveKeysNullInfos() {
            Hkdf hkdf = Hkdf.sha256();
            int[] lengths = {32};

            assertThatThrownBy(() -> hkdf.deriveKeys(TEST_SALT, TEST_IKM, null, lengths))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("deriveKeys null lengths抛出异常")
        void testDeriveKeysNullLengths() {
            Hkdf hkdf = Hkdf.sha256();
            byte[][] infos = {TEST_INFO};

            assertThatThrownBy(() -> hkdf.deriveKeys(TEST_SALT, TEST_IKM, infos, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("deriveKeys数组长度不匹配抛出异常")
        void testDeriveKeysArrayLengthMismatch() {
            Hkdf hkdf = Hkdf.sha256();
            byte[][] infos = {TEST_INFO, TEST_INFO};
            int[] lengths = {32};

            assertThatThrownBy(() -> hkdf.deriveKeys(TEST_SALT, TEST_IKM, infos, lengths))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("不同算法测试")
    class AlgorithmComparisonTests {

        @Test
        @DisplayName("不同算法产生不同结果")
        void testDifferentAlgorithmsDifferentResults() {
            Hkdf hkdf256 = Hkdf.sha256();
            Hkdf hkdf512 = Hkdf.sha512();

            byte[] result256 = hkdf256.deriveKey(TEST_IKM, TEST_SALT, TEST_INFO, 32);
            byte[] result512 = hkdf512.deriveKey(TEST_IKM, TEST_SALT, TEST_INFO, 32);

            assertThat(result256).isNotEqualTo(result512);
        }

        @Test
        @DisplayName("SHA512输出长度更长")
        void testSha512LongerHashLength() {
            Hkdf hkdf256 = Hkdf.sha256();
            Hkdf hkdf384 = Hkdf.sha384();
            Hkdf hkdf512 = Hkdf.sha512();

            assertThat(hkdf256.getHashLength()).isLessThan(hkdf384.getHashLength());
            assertThat(hkdf384.getHashLength()).isLessThan(hkdf512.getHashLength());
        }
    }
}
