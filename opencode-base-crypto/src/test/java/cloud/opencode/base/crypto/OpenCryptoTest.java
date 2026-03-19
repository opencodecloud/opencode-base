package cloud.opencode.base.crypto;

import cloud.opencode.base.crypto.enums.AsymmetricAlgorithm;
import cloud.opencode.base.crypto.enums.DigestAlgorithm;
import cloud.opencode.base.crypto.enums.SignatureAlgorithm;
import cloud.opencode.base.crypto.enums.SymmetricAlgorithm;
import cloud.opencode.base.crypto.envelope.EnvelopeCrypto;
import cloud.opencode.base.crypto.envelope.HybridCrypto;
import cloud.opencode.base.crypto.kdf.Argon2Kdf;
import cloud.opencode.base.crypto.kdf.Hkdf;
import cloud.opencode.base.crypto.kdf.Pbkdf2;
import cloud.opencode.base.crypto.keyexchange.KeyExchangeEngine;
import cloud.opencode.base.crypto.mac.Mac;
import cloud.opencode.base.crypto.password.PasswordHash;
import cloud.opencode.base.crypto.symmetric.AeadCipher;
import cloud.opencode.base.crypto.symmetric.Sm4Cipher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link OpenCrypto}.
 * OpenCrypto单元测试。
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
@DisplayName("OpenCrypto Tests / OpenCrypto测试")
class OpenCryptoTest {

    @Nested
    @DisplayName("Utility Class Tests / 工具类测试")
    class UtilityClassTests {

        @Test
        @DisplayName("私有构造函数抛出AssertionError")
        void testPrivateConstructorThrowsAssertionError() throws Exception {
            Constructor<OpenCrypto> constructor = OpenCrypto.class.getDeclaredConstructor();
            constructor.setAccessible(true);

            assertThatThrownBy(constructor::newInstance)
                    .isInstanceOf(InvocationTargetException.class)
                    .hasCauseInstanceOf(AssertionError.class);
        }
    }

    @Nested
    @DisplayName("Digest Method Tests / 摘要方法测试")
    class DigestMethodTests {

        @Test
        @DisplayName("sha256返回OpenDigest实例")
        void testSha256() {
            OpenDigest digester = OpenCrypto.sha256();

            assertThat(digester).isNotNull();
            assertThat(digester.getAlgorithm()).containsIgnoringCase("SHA-256");
        }

        @Test
        @DisplayName("sha384返回OpenDigest实例")
        void testSha384() {
            OpenDigest digester = OpenCrypto.sha384();

            assertThat(digester).isNotNull();
            assertThat(digester.getAlgorithm()).containsIgnoringCase("SHA-384");
        }

        @Test
        @DisplayName("sha512返回OpenDigest实例")
        void testSha512() {
            OpenDigest digester = OpenCrypto.sha512();

            assertThat(digester).isNotNull();
            assertThat(digester.getAlgorithm()).containsIgnoringCase("SHA-512");
        }

        @Test
        @DisplayName("sha3_256返回OpenDigest实例")
        void testSha3_256() {
            OpenDigest digester = OpenCrypto.sha3_256();

            assertThat(digester).isNotNull();
            assertThat(digester.getAlgorithm()).containsIgnoringCase("SHA3-256");
        }

        @Test
        @DisplayName("sha3_512返回OpenDigest实例")
        void testSha3_512() {
            OpenDigest digester = OpenCrypto.sha3_512();

            assertThat(digester).isNotNull();
            assertThat(digester.getAlgorithm()).containsIgnoringCase("SHA3-512");
        }

        @Test
        @DisplayName("sm3返回OpenDigest实例")
        void testSm3() {
            OpenDigest digester = OpenCrypto.sm3();

            assertThat(digester).isNotNull();
            assertThat(digester.getAlgorithm()).containsIgnoringCase("SM3");
        }

        @Test
        @DisplayName("blake2b返回OpenDigest实例")
        void testBlake2b() {
            OpenDigest digester = OpenCrypto.blake2b();

            assertThat(digester).isNotNull();
        }

        @Test
        @DisplayName("blake3返回OpenDigest实例")
        void testBlake3() {
            OpenDigest digester = OpenCrypto.blake3();

            assertThat(digester).isNotNull();
        }

        @Test
        @DisplayName("digester创建自定义摘要器")
        void testDigester() {
            OpenDigest digester = OpenCrypto.digester(DigestAlgorithm.SHA256);

            assertThat(digester).isNotNull();
        }

        @Test
        @DisplayName("digest方法可以正常工作")
        void testDigestFunctionality() {
            byte[] data = "test data".getBytes(StandardCharsets.UTF_8);

            byte[] hash = OpenCrypto.sha256().digest(data);

            assertThat(hash).isNotNull().hasSize(32);
        }
    }

    @Nested
    @DisplayName("Symmetric Encryption Tests / 对称加密测试")
    class SymmetricEncryptionTests {

        @Test
        @DisplayName("aesGcm返回AES-128-GCM实例")
        void testAesGcm() {
            AeadCipher cipher = OpenCrypto.aesGcm();

            assertThat(cipher).isNotNull();
        }

        @Test
        @DisplayName("aesGcm256返回AES-256-GCM实例")
        void testAesGcm256() {
            AeadCipher cipher = OpenCrypto.aesGcm256();

            assertThat(cipher).isNotNull();
        }

        @Test
        @DisplayName("aesCbc返回AES-CBC实例")
        void testAesCbc() {
            OpenSymmetric cipher = OpenCrypto.aesCbc();

            assertThat(cipher).isNotNull();
        }

        @Test
        @DisplayName("chacha20Poly1305返回ChaCha20实例")
        void testChacha20Poly1305() {
            AeadCipher cipher = OpenCrypto.chacha20Poly1305();

            assertThat(cipher).isNotNull();
        }

        @Test
        @DisplayName("sm4Gcm返回SM4-GCM实例")
        void testSm4Gcm() {
            AeadCipher cipher = OpenCrypto.sm4Gcm();

            assertThat(cipher).isNotNull();
        }

        @Test
        @DisplayName("sm4Cbc返回SM4-CBC实例")
        void testSm4Cbc() {
            Sm4Cipher cipher = OpenCrypto.sm4Cbc();

            assertThat(cipher).isNotNull();
        }

        @Test
        @DisplayName("symmetric创建自定义对称加密器")
        void testSymmetric() {
            // Use non-AEAD algorithm - AEAD algorithms should use aesGcm() etc.
            OpenSymmetric cipher = OpenCrypto.symmetric(SymmetricAlgorithm.AES_CBC_256);

            assertThat(cipher).isNotNull();
        }

        @Test
        @DisplayName("AES-GCM加密解密功能正常")
        void testAesGcmFunctionality() {
            byte[] key = new byte[16];
            Arrays.fill(key, (byte) 0x42);
            byte[] plaintext = "AES-GCM test".getBytes(StandardCharsets.UTF_8);

            AeadCipher cipher = OpenCrypto.aesGcm().setKey(key);
            byte[] encrypted = cipher.encrypt(plaintext);
            byte[] decrypted = cipher.decrypt(encrypted);

            assertThat(decrypted).isEqualTo(plaintext);
        }
    }

    @Nested
    @DisplayName("Asymmetric Encryption Tests / 非对称加密测试")
    class AsymmetricEncryptionTests {

        @Test
        @DisplayName("rsaOaep返回RSA-OAEP实例")
        void testRsaOaep() {
            OpenAsymmetric cipher = OpenCrypto.rsaOaep();

            assertThat(cipher).isNotNull();
        }

        @Test
        @DisplayName("rsa返回RSA-PKCS1实例")
        void testRsa() {
            OpenAsymmetric cipher = OpenCrypto.rsa();

            assertThat(cipher).isNotNull();
        }

        @Test
        @DisplayName("sm2返回SM2实例")
        void testSm2() {
            OpenAsymmetric cipher = OpenCrypto.sm2();

            assertThat(cipher).isNotNull();
        }

        @Test
        @DisplayName("asymmetric创建自定义非对称加密器")
        void testAsymmetric() {
            OpenAsymmetric cipher = OpenCrypto.asymmetric(AsymmetricAlgorithm.RSA_OAEP_SHA256);

            assertThat(cipher).isNotNull();
        }

        @Test
        @DisplayName("RSA加密解密功能正常")
        void testRsaFunctionality() {
            OpenAsymmetric cipher = OpenCrypto.rsaOaep().withGeneratedKeyPair();
            byte[] plaintext = "RSA test".getBytes(StandardCharsets.UTF_8);

            byte[] encrypted = cipher.encrypt(plaintext);
            byte[] decrypted = cipher.decrypt(encrypted);

            assertThat(decrypted).isEqualTo(plaintext);
        }
    }

    @Nested
    @DisplayName("Digital Signature Tests / 数字签名测试")
    class DigitalSignatureTests {

        @Test
        @DisplayName("ed25519返回Ed25519实例")
        void testEd25519() {
            OpenSign signer = OpenCrypto.ed25519();

            assertThat(signer).isNotNull();
        }

        @Test
        @DisplayName("ed448返回Ed448实例")
        void testEd448() {
            OpenSign signer = OpenCrypto.ed448();

            assertThat(signer).isNotNull();
        }

        @Test
        @DisplayName("ecdsaP256返回ECDSA P-256实例")
        void testEcdsaP256() {
            OpenSign signer = OpenCrypto.ecdsaP256();

            assertThat(signer).isNotNull();
        }

        @Test
        @DisplayName("ecdsaP384返回ECDSA P-384实例")
        void testEcdsaP384() {
            OpenSign signer = OpenCrypto.ecdsaP384();

            assertThat(signer).isNotNull();
        }

        @Test
        @DisplayName("sha256WithRsa返回RSA-SHA256实例")
        void testSha256WithRsa() {
            OpenSign signer = OpenCrypto.sha256WithRsa();

            assertThat(signer).isNotNull();
        }

        @Test
        @DisplayName("rsaPss返回RSA-PSS实例")
        void testRsaPss() {
            OpenSign signer = OpenCrypto.rsaPss();

            assertThat(signer).isNotNull();
        }

        @Test
        @DisplayName("sm2Sign返回SM2实例")
        void testSm2Sign() {
            OpenSign signer = OpenCrypto.sm2Sign();

            assertThat(signer).isNotNull();
        }

        @Test
        @DisplayName("signer创建自定义签名器")
        void testSigner() {
            OpenSign signer = OpenCrypto.signer(SignatureAlgorithm.ED25519);

            assertThat(signer).isNotNull();
        }

        @Test
        @DisplayName("Ed25519签名验证功能正常")
        void testEd25519Functionality() {
            OpenSign signer = OpenCrypto.ed25519().withGeneratedKeyPair();
            byte[] data = "sign this".getBytes(StandardCharsets.UTF_8);

            byte[] signature = signer.sign(data);
            boolean valid = signer.verify(data, signature);

            assertThat(valid).isTrue();
        }
    }

    @Nested
    @DisplayName("Password Hashing Tests / 密码哈希测试")
    class PasswordHashingTests {

        @Test
        @DisplayName("argon2返回Argon2实例")
        void testArgon2() {
            PasswordHash hasher = OpenCrypto.argon2();

            assertThat(hasher).isNotNull();
        }

        @Test
        @DisplayName("bcrypt返回BCrypt实例")
        void testBcrypt() {
            PasswordHash hasher = OpenCrypto.bcrypt();

            assertThat(hasher).isNotNull();
        }

        @Test
        @DisplayName("scrypt返回SCrypt实例")
        void testScrypt() {
            PasswordHash hasher = OpenCrypto.scrypt();

            assertThat(hasher).isNotNull();
        }

        @Test
        @DisplayName("pbkdf2返回PBKDF2实例")
        void testPbkdf2() {
            PasswordHash hasher = OpenCrypto.pbkdf2();

            assertThat(hasher).isNotNull();
        }

        @Test
        @DisplayName("Argon2哈希验证功能正常")
        void testArgon2Functionality() {
            PasswordHash hasher = OpenCrypto.argon2();
            String password = "password123";

            String hash = hasher.hash(password);
            boolean valid = hasher.verify(password, hash);

            assertThat(valid).isTrue();
        }
    }

    @Nested
    @DisplayName("HMAC Tests / HMAC测试")
    class HmacTests {

        @Test
        @DisplayName("hmacSha256返回HMAC-SHA256实例")
        void testHmacSha256() {
            byte[] key = new byte[32];
            Arrays.fill(key, (byte) 0x42);

            Mac mac = OpenCrypto.hmacSha256(key);

            assertThat(mac).isNotNull();
        }

        @Test
        @DisplayName("hmacSha512返回HMAC-SHA512实例")
        void testHmacSha512() {
            byte[] key = new byte[64];
            Arrays.fill(key, (byte) 0x42);

            Mac mac = OpenCrypto.hmacSha512(key);

            assertThat(mac).isNotNull();
        }

        @Test
        @DisplayName("HMAC-SHA256功能正常")
        void testHmacSha256Functionality() {
            byte[] key = new byte[32];
            Arrays.fill(key, (byte) 0x42);
            byte[] data = "HMAC test data".getBytes(StandardCharsets.UTF_8);

            Mac mac = OpenCrypto.hmacSha256(key);
            byte[] tag1 = mac.compute(data);
            byte[] tag2 = mac.compute(data);

            assertThat(tag1).isNotNull().hasSize(32);
            assertThat(tag1).isEqualTo(tag2);
        }
    }

    @Nested
    @DisplayName("Key Derivation Tests / 密钥派生测试")
    class KeyDerivationTests {

        @Test
        @DisplayName("hkdf返回HKDF-SHA256实例")
        void testHkdf() {
            Hkdf hkdf = OpenCrypto.hkdf();

            assertThat(hkdf).isNotNull();
        }

        @Test
        @DisplayName("pbkdf2Kdf返回PBKDF2实例")
        void testPbkdf2Kdf() {
            Pbkdf2 pbkdf2 = OpenCrypto.pbkdf2Kdf();

            assertThat(pbkdf2).isNotNull();
        }

        @Test
        @DisplayName("argon2Kdf返回Argon2实例")
        void testArgon2Kdf() {
            Argon2Kdf argon2 = OpenCrypto.argon2Kdf();

            assertThat(argon2).isNotNull();
        }

        @Test
        @DisplayName("HKDF派生功能正常")
        void testHkdfFunctionality() {
            Hkdf hkdf = OpenCrypto.hkdf();
            byte[] ikm = new byte[32];
            Arrays.fill(ikm, (byte) 0x42);

            byte[] derived = hkdf.derive(ikm, null, null, 32);

            assertThat(derived).isNotNull().hasSize(32);
        }
    }

    @Nested
    @DisplayName("Key Exchange Tests / 密钥协商测试")
    class KeyExchangeTests {

        @Test
        @DisplayName("x25519返回X25519实例")
        void testX25519() {
            KeyExchangeEngine engine = OpenCrypto.x25519();

            assertThat(engine).isNotNull();
        }

        @Test
        @DisplayName("ecdhP256返回ECDH P-256实例")
        void testEcdhP256() {
            KeyExchangeEngine engine = OpenCrypto.ecdhP256();

            assertThat(engine).isNotNull();
        }

        @Test
        @DisplayName("X25519密钥协商功能正常")
        void testX25519Functionality() {
            KeyExchangeEngine aliceEngine = OpenCrypto.x25519();
            KeyExchangeEngine bobEngine = OpenCrypto.x25519();

            KeyPair alice = aliceEngine.generateKeyPair();
            KeyPair bob = bobEngine.generateKeyPair();

            byte[] aliceSecret = aliceEngine.setPrivateKey(alice.getPrivate())
                    .setRemotePublicKey(bob.getPublic())
                    .computeSharedSecret();
            byte[] bobSecret = bobEngine.setPrivateKey(bob.getPrivate())
                    .setRemotePublicKey(alice.getPublic())
                    .computeSharedSecret();

            assertThat(aliceSecret).isEqualTo(bobSecret);
        }
    }

    @Nested
    @DisplayName("Envelope Encryption Tests / 信封加密测试")
    class EnvelopeEncryptionTests {

        @Test
        @DisplayName("envelope返回EnvelopeCrypto实例")
        void testEnvelope() {
            EnvelopeCrypto crypto = OpenCrypto.envelope();

            assertThat(crypto).isNotNull();
        }

        @Test
        @DisplayName("hybrid返回HybridCrypto实例")
        void testHybrid() {
            HybridCrypto crypto = OpenCrypto.hybrid();

            assertThat(crypto).isNotNull();
        }
    }

    @Nested
    @DisplayName("Integration Tests / 集成测试")
    class IntegrationTests {

        @Test
        @DisplayName("完整加密流程 - SHA256哈希")
        void testSha256Integration() {
            String data = "Integration test data";

            String hash = OpenCrypto.sha256().digestHex(data);

            assertThat(hash).isNotNull().hasSize(64); // 32 bytes = 64 hex chars
        }

        @Test
        @DisplayName("完整加密流程 - AES-GCM加密")
        void testAesGcmIntegration() {
            byte[] key = new byte[16];
            Arrays.fill(key, (byte) 0x42);
            byte[] plaintext = "AES-GCM integration test".getBytes(StandardCharsets.UTF_8);

            AeadCipher cipher = OpenCrypto.aesGcm().setKey(key);
            byte[] encrypted = cipher.encrypt(plaintext);
            byte[] decrypted = cipher.decrypt(encrypted);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("完整加密流程 - RSA加密")
        void testRsaIntegration() {
            byte[] plaintext = "RSA integration test".getBytes(StandardCharsets.UTF_8);

            OpenAsymmetric crypto = OpenCrypto.rsaOaep().withGeneratedKeyPair();
            byte[] encrypted = crypto.encrypt(plaintext);
            byte[] decrypted = crypto.decrypt(encrypted);

            assertThat(decrypted).isEqualTo(plaintext);
        }

        @Test
        @DisplayName("完整签名流程 - Ed25519")
        void testEd25519Integration() {
            byte[] data = "Ed25519 integration test".getBytes(StandardCharsets.UTF_8);

            OpenSign signer = OpenCrypto.ed25519().withGeneratedKeyPair();
            byte[] signature = signer.sign(data);
            boolean valid = signer.verify(data, signature);

            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("完整密码哈希流程 - Argon2")
        void testArgon2Integration() {
            String password = "integration_password_123";

            PasswordHash hasher = OpenCrypto.argon2();
            String hash = hasher.hash(password);
            boolean valid = hasher.verify(password, hash);

            assertThat(valid).isTrue();
        }
    }
}
