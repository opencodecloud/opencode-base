package cloud.opencode.base.crypto.pgp;

import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link PgpKeyPair}.
 * PgpKeyPair单元测试。
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("PgpKeyPair Tests / PgpKeyPair测试")
class PgpKeyPairTest {

    private static PgpKeyPair testKeyPair;
    private static final String TEST_USER_ID = "test@example.com";
    private static final String TEST_PASSPHRASE = "testPassphrase123!";

    @BeforeAll
    static void setup() {
        testKeyPair = PgpKeyUtil.generateKeyPair(TEST_USER_ID, TEST_PASSPHRASE, 2048);
    }

    @Nested
    @DisplayName("Record Constructor Tests / 记录构造函数测试")
    class RecordConstructorTests {

        @Test
        @DisplayName("正常创建PgpKeyPair成功")
        void testNormalCreation() {
            PGPPublicKey publicKey = testKeyPair.publicKey();
            PGPSecretKey secretKey = testKeyPair.secretKey();

            PgpKeyPair keyPair = new PgpKeyPair(publicKey, secretKey, TEST_USER_ID, secretKey.getKeyID());

            assertThat(keyPair.publicKey()).isEqualTo(publicKey);
            assertThat(keyPair.secretKey()).isEqualTo(secretKey);
            assertThat(keyPair.userId()).isEqualTo(TEST_USER_ID);
            assertThat(keyPair.keyId()).isEqualTo(secretKey.getKeyID());
        }

        @Test
        @DisplayName("publicKey为null抛出NullPointerException")
        void testNullPublicKeyThrows() {
            PGPSecretKey secretKey = testKeyPair.secretKey();

            assertThatThrownBy(() -> new PgpKeyPair(null, secretKey, TEST_USER_ID, 12345L))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("publicKey");
        }

        @Test
        @DisplayName("secretKey为null抛出NullPointerException")
        void testNullSecretKeyThrows() {
            PGPPublicKey publicKey = testKeyPair.publicKey();

            assertThatThrownBy(() -> new PgpKeyPair(publicKey, null, TEST_USER_ID, 12345L))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("secretKey");
        }

        @Test
        @DisplayName("userId可以为null")
        void testNullUserIdAllowed() {
            PGPPublicKey publicKey = testKeyPair.publicKey();
            PGPSecretKey secretKey = testKeyPair.secretKey();

            PgpKeyPair keyPair = new PgpKeyPair(publicKey, secretKey, null, secretKey.getKeyID());

            assertThat(keyPair.userId()).isNull();
        }
    }

    @Nested
    @DisplayName("fromSecretKey Tests / fromSecretKey方法测试")
    class FromSecretKeyTests {

        @Test
        @DisplayName("从secretKey创建KeyPair成功")
        void testFromSecretKeySuccess() {
            PGPSecretKey secretKey = testKeyPair.secretKey();

            PgpKeyPair keyPair = PgpKeyPair.fromSecretKey(secretKey, TEST_USER_ID);

            assertThat(keyPair.publicKey()).isEqualTo(secretKey.getPublicKey());
            assertThat(keyPair.secretKey()).isEqualTo(secretKey);
            assertThat(keyPair.userId()).isEqualTo(TEST_USER_ID);
            assertThat(keyPair.keyId()).isEqualTo(secretKey.getKeyID());
        }

        @Test
        @DisplayName("secretKey为null抛出异常")
        void testFromSecretKeyNullThrows() {
            assertThatThrownBy(() -> PgpKeyPair.fromSecretKey(null, TEST_USER_ID))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("secretKey");
        }
    }

    @Nested
    @DisplayName("keyIdHex Tests / keyIdHex方法测试")
    class KeyIdHexTests {

        @Test
        @DisplayName("keyIdHex返回大写十六进制字符串")
        void testKeyIdHexReturnsUppercaseHex() {
            String hex = testKeyPair.keyIdHex();

            assertThat(hex).isNotNull().isNotEmpty();
            assertThat(hex).matches("[0-9A-F]+");
        }

        @Test
        @DisplayName("keyIdHex与keyId一致")
        void testKeyIdHexMatchesKeyId() {
            String hex = testKeyPair.keyIdHex();
            String expected = Long.toHexString(testKeyPair.keyId()).toUpperCase();

            assertThat(hex).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("canEncrypt Tests / canEncrypt方法测试")
    class CanEncryptTests {

        @Test
        @DisplayName("RSA密钥对可以加密")
        void testRsaKeyPairCanEncrypt() {
            boolean canEncrypt = testKeyPair.canEncrypt();

            assertThat(canEncrypt).isTrue();
        }
    }

    @Nested
    @DisplayName("canSign Tests / canSign方法测试")
    class CanSignTests {

        @Test
        @DisplayName("RSA密钥对可以签名")
        void testRsaKeyPairCanSign() {
            boolean canSign = testKeyPair.canSign();

            assertThat(canSign).isTrue();
        }
    }

    @Nested
    @DisplayName("Record Getter Tests / 记录getter测试")
    class RecordGetterTests {

        @Test
        @DisplayName("publicKey()返回正确的公钥")
        void testPublicKeyGetter() {
            PGPPublicKey publicKey = testKeyPair.publicKey();

            assertThat(publicKey).isNotNull();
            assertThat(publicKey.isEncryptionKey()).isTrue();
        }

        @Test
        @DisplayName("secretKey()返回正确的私钥")
        void testSecretKeyGetter() {
            PGPSecretKey secretKey = testKeyPair.secretKey();

            assertThat(secretKey).isNotNull();
            assertThat(secretKey.isSigningKey()).isTrue();
        }

        @Test
        @DisplayName("userId()返回正确的用户ID")
        void testUserIdGetter() {
            String userId = testKeyPair.userId();

            assertThat(userId).isEqualTo(TEST_USER_ID);
        }

        @Test
        @DisplayName("keyId()返回非零密钥ID")
        void testKeyIdGetter() {
            long keyId = testKeyPair.keyId();

            assertThat(keyId).isNotEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("Record Equals/HashCode Tests / 记录equals/hashCode测试")
    class RecordEqualsHashCodeTests {

        @Test
        @DisplayName("相同KeyPair相等")
        void testSameKeyPairEquals() {
            PgpKeyPair keyPair1 = testKeyPair;
            PgpKeyPair keyPair2 = testKeyPair;

            assertThat(keyPair1).isEqualTo(keyPair2);
            assertThat(keyPair1.hashCode()).isEqualTo(keyPair2.hashCode());
        }

        @Test
        @DisplayName("从相同secretKey创建的KeyPair相等")
        void testKeyPairsFromSameSecretKeyEqual() {
            PgpKeyPair keyPair1 = PgpKeyPair.fromSecretKey(testKeyPair.secretKey(), TEST_USER_ID);
            PgpKeyPair keyPair2 = PgpKeyPair.fromSecretKey(testKeyPair.secretKey(), TEST_USER_ID);

            assertThat(keyPair1).isEqualTo(keyPair2);
            assertThat(keyPair1.hashCode()).isEqualTo(keyPair2.hashCode());
        }

        @Test
        @DisplayName("不同userId的KeyPair不相等")
        void testDifferentUserIdNotEqual() {
            PgpKeyPair keyPair1 = PgpKeyPair.fromSecretKey(testKeyPair.secretKey(), "user1@example.com");
            PgpKeyPair keyPair2 = PgpKeyPair.fromSecretKey(testKeyPair.secretKey(), "user2@example.com");

            assertThat(keyPair1).isNotEqualTo(keyPair2);
        }
    }

    @Nested
    @DisplayName("Record ToString Tests / 记录toString测试")
    class RecordToStringTests {

        @Test
        @DisplayName("toString包含必要信息")
        void testToStringContainsInfo() {
            String str = testKeyPair.toString();

            assertThat(str).contains("PgpKeyPair");
            assertThat(str).contains("publicKey");
            assertThat(str).contains("secretKey");
            assertThat(str).contains("userId");
            assertThat(str).contains("keyId");
        }
    }

    @Nested
    @DisplayName("Integration Tests / 集成测试")
    class IntegrationTests {

        @Test
        @DisplayName("生成的密钥对可以用于加密解密")
        void testKeyPairCanEncryptDecrypt() {
            String plaintext = "Hello, PGP World!";

            // Encrypt using public key
            String encrypted = PgpCipher.create()
                    .withPublicKey(testKeyPair.publicKey())
                    .encryptArmored(plaintext);

            // Decrypt using secret key
            String decrypted = PgpCipher.create()
                    .withKeyPair(testKeyPair, TEST_PASSPHRASE)
                    .decryptArmored(encrypted);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("导出并重新导入密钥对")
        void testExportImportKeyPair() {
            // Export secret key
            String exportedSecretKey = PgpKeyUtil.exportSecretKey(testKeyPair.secretKey());

            // Import key pair
            PgpKeyPair importedKeyPair = PgpKeyUtil.importKeyPair(exportedSecretKey, TEST_PASSPHRASE);

            assertThat(importedKeyPair.keyId()).isEqualTo(testKeyPair.keyId());
            assertThat(importedKeyPair.canEncrypt()).isEqualTo(testKeyPair.canEncrypt());
            assertThat(importedKeyPair.canSign()).isEqualTo(testKeyPair.canSign());
        }
    }
}
