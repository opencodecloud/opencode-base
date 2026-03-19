package cloud.opencode.base.crypto.pgp;

import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.bcpg.PublicKeyAlgorithmTags;
import org.bouncycastle.bcpg.SymmetricKeyAlgorithmTags;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link PgpAlgorithm}.
 * PgpAlgorithm单元测试。
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("PgpAlgorithm Tests / PgpAlgorithm测试")
class PgpAlgorithmTest {

    @Nested
    @DisplayName("PublicKey Enum Tests / 公钥算法枚举测试")
    class PublicKeyEnumTests {

        @Test
        @DisplayName("RSA_GENERAL属性正确")
        void testRsaGeneral() {
            PgpAlgorithm.PublicKey alg = PgpAlgorithm.PublicKey.RSA_GENERAL;
            assertThat(alg.tag()).isEqualTo(PublicKeyAlgorithmTags.RSA_GENERAL);
            assertThat(alg.algorithmName()).isEqualTo("RSA");
        }

        @Test
        @DisplayName("RSA_ENCRYPT属性正确")
        void testRsaEncrypt() {
            PgpAlgorithm.PublicKey alg = PgpAlgorithm.PublicKey.RSA_ENCRYPT;
            assertThat(alg.tag()).isEqualTo(PublicKeyAlgorithmTags.RSA_ENCRYPT);
            assertThat(alg.algorithmName()).isEqualTo("RSA (Encrypt)");
        }

        @Test
        @DisplayName("RSA_SIGN属性正确")
        void testRsaSign() {
            PgpAlgorithm.PublicKey alg = PgpAlgorithm.PublicKey.RSA_SIGN;
            assertThat(alg.tag()).isEqualTo(PublicKeyAlgorithmTags.RSA_SIGN);
            assertThat(alg.algorithmName()).isEqualTo("RSA (Sign)");
        }

        @Test
        @DisplayName("ELGAMAL_ENCRYPT属性正确")
        void testElgamalEncrypt() {
            PgpAlgorithm.PublicKey alg = PgpAlgorithm.PublicKey.ELGAMAL_ENCRYPT;
            assertThat(alg.tag()).isEqualTo(PublicKeyAlgorithmTags.ELGAMAL_ENCRYPT);
            assertThat(alg.algorithmName()).isEqualTo("ElGamal");
        }

        @Test
        @DisplayName("DSA属性正确")
        void testDsa() {
            PgpAlgorithm.PublicKey alg = PgpAlgorithm.PublicKey.DSA;
            assertThat(alg.tag()).isEqualTo(PublicKeyAlgorithmTags.DSA);
            assertThat(alg.algorithmName()).isEqualTo("DSA");
        }

        @Test
        @DisplayName("ECDH属性正确")
        void testEcdh() {
            PgpAlgorithm.PublicKey alg = PgpAlgorithm.PublicKey.ECDH;
            assertThat(alg.tag()).isEqualTo(PublicKeyAlgorithmTags.ECDH);
            assertThat(alg.algorithmName()).isEqualTo("ECDH");
        }

        @Test
        @DisplayName("ECDSA属性正确")
        void testEcdsa() {
            PgpAlgorithm.PublicKey alg = PgpAlgorithm.PublicKey.ECDSA;
            assertThat(alg.tag()).isEqualTo(PublicKeyAlgorithmTags.ECDSA);
            assertThat(alg.algorithmName()).isEqualTo("ECDSA");
        }

        @Test
        @DisplayName("EDDSA属性正确")
        void testEddsa() {
            PgpAlgorithm.PublicKey alg = PgpAlgorithm.PublicKey.EDDSA;
            assertThat(alg.tag()).isEqualTo(PublicKeyAlgorithmTags.EDDSA);
            assertThat(alg.algorithmName()).isEqualTo("EdDSA");
        }

        @Test
        @DisplayName("所有PublicKey枚举值都有有效属性")
        void testAllPublicKeyValues() {
            for (PgpAlgorithm.PublicKey alg : PgpAlgorithm.PublicKey.values()) {
                assertThat(alg.tag()).isGreaterThan(0);
                assertThat(alg.algorithmName()).isNotNull().isNotEmpty();
            }
        }
    }

    @Nested
    @DisplayName("Symmetric Enum Tests / 对称算法枚举测试")
    class SymmetricEnumTests {

        @Test
        @DisplayName("AES_128属性正确")
        void testAes128() {
            PgpAlgorithm.Symmetric alg = PgpAlgorithm.Symmetric.AES_128;
            assertThat(alg.tag()).isEqualTo(SymmetricKeyAlgorithmTags.AES_128);
            assertThat(alg.algorithmName()).isEqualTo("AES-128");
            assertThat(alg.keySize()).isEqualTo(128);
        }

        @Test
        @DisplayName("AES_192属性正确")
        void testAes192() {
            PgpAlgorithm.Symmetric alg = PgpAlgorithm.Symmetric.AES_192;
            assertThat(alg.tag()).isEqualTo(SymmetricKeyAlgorithmTags.AES_192);
            assertThat(alg.algorithmName()).isEqualTo("AES-192");
            assertThat(alg.keySize()).isEqualTo(192);
        }

        @Test
        @DisplayName("AES_256属性正确")
        void testAes256() {
            PgpAlgorithm.Symmetric alg = PgpAlgorithm.Symmetric.AES_256;
            assertThat(alg.tag()).isEqualTo(SymmetricKeyAlgorithmTags.AES_256);
            assertThat(alg.algorithmName()).isEqualTo("AES-256");
            assertThat(alg.keySize()).isEqualTo(256);
        }

        @Test
        @DisplayName("TWOFISH属性正确")
        void testTwofish() {
            PgpAlgorithm.Symmetric alg = PgpAlgorithm.Symmetric.TWOFISH;
            assertThat(alg.tag()).isEqualTo(SymmetricKeyAlgorithmTags.TWOFISH);
            assertThat(alg.algorithmName()).isEqualTo("Twofish");
            assertThat(alg.keySize()).isEqualTo(256);
        }

        @Test
        @DisplayName("CAMELLIA_128属性正确")
        void testCamellia128() {
            PgpAlgorithm.Symmetric alg = PgpAlgorithm.Symmetric.CAMELLIA_128;
            assertThat(alg.tag()).isEqualTo(SymmetricKeyAlgorithmTags.CAMELLIA_128);
            assertThat(alg.algorithmName()).isEqualTo("Camellia-128");
            assertThat(alg.keySize()).isEqualTo(128);
        }

        @Test
        @DisplayName("CAMELLIA_192属性正确")
        void testCamellia192() {
            PgpAlgorithm.Symmetric alg = PgpAlgorithm.Symmetric.CAMELLIA_192;
            assertThat(alg.tag()).isEqualTo(SymmetricKeyAlgorithmTags.CAMELLIA_192);
            assertThat(alg.algorithmName()).isEqualTo("Camellia-192");
            assertThat(alg.keySize()).isEqualTo(192);
        }

        @Test
        @DisplayName("CAMELLIA_256属性正确")
        void testCamellia256() {
            PgpAlgorithm.Symmetric alg = PgpAlgorithm.Symmetric.CAMELLIA_256;
            assertThat(alg.tag()).isEqualTo(SymmetricKeyAlgorithmTags.CAMELLIA_256);
            assertThat(alg.algorithmName()).isEqualTo("Camellia-256");
            assertThat(alg.keySize()).isEqualTo(256);
        }

        @Test
        @DisplayName("所有Symmetric枚举值都有有效属性")
        void testAllSymmetricValues() {
            for (PgpAlgorithm.Symmetric alg : PgpAlgorithm.Symmetric.values()) {
                assertThat(alg.tag()).isGreaterThan(0);
                assertThat(alg.algorithmName()).isNotNull().isNotEmpty();
                assertThat(alg.keySize()).isIn(128, 192, 256);
            }
        }
    }

    @Nested
    @DisplayName("Hash Enum Tests / 哈希算法枚举测试")
    class HashEnumTests {

        @Test
        @DisplayName("SHA256属性正确")
        void testSha256() {
            PgpAlgorithm.Hash alg = PgpAlgorithm.Hash.SHA256;
            assertThat(alg.tag()).isEqualTo(HashAlgorithmTags.SHA256);
            assertThat(alg.algorithmName()).isEqualTo("SHA-256");
        }

        @Test
        @DisplayName("SHA384属性正确")
        void testSha384() {
            PgpAlgorithm.Hash alg = PgpAlgorithm.Hash.SHA384;
            assertThat(alg.tag()).isEqualTo(HashAlgorithmTags.SHA384);
            assertThat(alg.algorithmName()).isEqualTo("SHA-384");
        }

        @Test
        @DisplayName("SHA512属性正确")
        void testSha512() {
            PgpAlgorithm.Hash alg = PgpAlgorithm.Hash.SHA512;
            assertThat(alg.tag()).isEqualTo(HashAlgorithmTags.SHA512);
            assertThat(alg.algorithmName()).isEqualTo("SHA-512");
        }

        @Test
        @DisplayName("SHA3_256属性正确")
        void testSha3_256() {
            PgpAlgorithm.Hash alg = PgpAlgorithm.Hash.SHA3_256;
            assertThat(alg.tag()).isEqualTo(HashAlgorithmTags.SHA3_256);
            assertThat(alg.algorithmName()).isEqualTo("SHA3-256");
        }

        @Test
        @DisplayName("SHA3_512属性正确")
        void testSha3_512() {
            PgpAlgorithm.Hash alg = PgpAlgorithm.Hash.SHA3_512;
            assertThat(alg.tag()).isEqualTo(HashAlgorithmTags.SHA3_512);
            assertThat(alg.algorithmName()).isEqualTo("SHA3-512");
        }

        @Test
        @DisplayName("所有Hash枚举值都有有效属性")
        void testAllHashValues() {
            for (PgpAlgorithm.Hash alg : PgpAlgorithm.Hash.values()) {
                assertThat(alg.tag()).isGreaterThan(0);
                assertThat(alg.algorithmName()).isNotNull().isNotEmpty();
            }
        }
    }

    @Nested
    @DisplayName("Constants Tests / 常量测试")
    class ConstantsTests {

        @Test
        @DisplayName("DEFAULT_RSA_KEY_SIZE正确")
        void testDefaultRsaKeySize() {
            assertThat(PgpAlgorithm.DEFAULT_RSA_KEY_SIZE).isEqualTo(4096);
        }

        @Test
        @DisplayName("MIN_RSA_KEY_SIZE正确")
        void testMinRsaKeySize() {
            assertThat(PgpAlgorithm.MIN_RSA_KEY_SIZE).isEqualTo(2048);
        }

        @Test
        @DisplayName("DEFAULT_SYMMETRIC正确")
        void testDefaultSymmetric() {
            assertThat(PgpAlgorithm.DEFAULT_SYMMETRIC).isEqualTo(PgpAlgorithm.Symmetric.AES_256);
        }

        @Test
        @DisplayName("DEFAULT_HASH正确")
        void testDefaultHash() {
            assertThat(PgpAlgorithm.DEFAULT_HASH).isEqualTo(PgpAlgorithm.Hash.SHA256);
        }
    }

    @Nested
    @DisplayName("Utility Class Tests / 工具类测试")
    class UtilityClassTests {

        @Test
        @DisplayName("私有构造函数抛出AssertionError")
        void testPrivateConstructorThrowsAssertionError() throws Exception {
            Constructor<PgpAlgorithm> constructor = PgpAlgorithm.class.getDeclaredConstructor();
            constructor.setAccessible(true);

            assertThatThrownBy(constructor::newInstance)
                    .hasCauseInstanceOf(AssertionError.class);
        }
    }

    @Nested
    @DisplayName("Enum valueOf Tests / 枚举valueOf测试")
    class EnumValueOfTests {

        @Test
        @DisplayName("PublicKey.valueOf正确解析")
        void testPublicKeyValueOf() {
            assertThat(PgpAlgorithm.PublicKey.valueOf("RSA_GENERAL"))
                    .isEqualTo(PgpAlgorithm.PublicKey.RSA_GENERAL);
            assertThat(PgpAlgorithm.PublicKey.valueOf("ECDH"))
                    .isEqualTo(PgpAlgorithm.PublicKey.ECDH);
        }

        @Test
        @DisplayName("Symmetric.valueOf正确解析")
        void testSymmetricValueOf() {
            assertThat(PgpAlgorithm.Symmetric.valueOf("AES_256"))
                    .isEqualTo(PgpAlgorithm.Symmetric.AES_256);
            assertThat(PgpAlgorithm.Symmetric.valueOf("TWOFISH"))
                    .isEqualTo(PgpAlgorithm.Symmetric.TWOFISH);
        }

        @Test
        @DisplayName("Hash.valueOf正确解析")
        void testHashValueOf() {
            assertThat(PgpAlgorithm.Hash.valueOf("SHA256"))
                    .isEqualTo(PgpAlgorithm.Hash.SHA256);
            assertThat(PgpAlgorithm.Hash.valueOf("SHA3_512"))
                    .isEqualTo(PgpAlgorithm.Hash.SHA3_512);
        }

        @Test
        @DisplayName("无效枚举值抛出异常")
        void testInvalidEnumValueThrows() {
            assertThatThrownBy(() -> PgpAlgorithm.PublicKey.valueOf("INVALID"))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> PgpAlgorithm.Symmetric.valueOf("INVALID"))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> PgpAlgorithm.Hash.valueOf("INVALID"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
