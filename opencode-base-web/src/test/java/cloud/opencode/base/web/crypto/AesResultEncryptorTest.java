package cloud.opencode.base.web.crypto;

import cloud.opencode.base.web.Result;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * AesResultEncryptorTest Tests
 * AesResultEncryptorTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
@DisplayName("AesResultEncryptor Tests")
class AesResultEncryptorTest {

    private AesResultEncryptor encryptor;

    @BeforeEach
    void setUp() {
        // Use a 32-byte key for AES-256
        byte[] key = new byte[32];
        for (int i = 0; i < 32; i++) {
            key[i] = (byte) i;
        }
        encryptor = new AesResultEncryptor(key);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("constructor with valid key should create encryptor")
        void constructorWithValidKeyShouldCreateEncryptor() {
            byte[] key = new byte[32];
            AesResultEncryptor enc = new AesResultEncryptor(key);

            assertThat(enc.getAlgorithm()).isEqualTo("AES-GCM");
        }

        @Test
        @DisplayName("constructor with invalid key length should throw exception")
        void constructorWithInvalidKeyLengthShouldThrowException() {
            byte[] shortKey = new byte[16];

            assertThatThrownBy(() -> new AesResultEncryptor(shortKey))
                .isInstanceOf(OpenCryptoException.class)
                .hasMessageContaining("32 bytes");
        }

        @Test
        @DisplayName("constructor with null key should throw exception")
        void constructorWithNullKeyShouldThrowException() {
            assertThatThrownBy(() -> new AesResultEncryptor((byte[]) null))
                .isInstanceOf(OpenCryptoException.class);
        }

        @Test
        @DisplayName("constructor with string key should create encryptor")
        void constructorWithStringKeyShouldCreateEncryptor() {
            AesResultEncryptor enc = new AesResultEncryptor("my-secret-key");

            assertThat(enc.getAlgorithm()).isEqualTo("AES-GCM");
        }

        @Test
        @DisplayName("constructor with blank string key should throw exception")
        void constructorWithBlankStringKeyShouldThrowException() {
            assertThatThrownBy(() -> new AesResultEncryptor("   "))
                .isInstanceOf(OpenCryptoException.class);
        }

        @Test
        @DisplayName("constructor with null string key should throw exception")
        void constructorWithNullStringKeyShouldThrowException() {
            assertThatThrownBy(() -> new AesResultEncryptor((String) null))
                .isInstanceOf(OpenCryptoException.class);
        }
    }

    @Nested
    @DisplayName("Encryption/Decryption Tests")
    class EncryptionDecryptionTests {

        @Test
        @DisplayName("encrypt should create encrypted result")
        void encryptShouldCreateEncryptedResult() {
            Result<String> result = Result.ok("secret data");

            EncryptedResult encrypted = encryptor.encrypt(result);

            assertThat(encrypted.code()).isEqualTo(result.code());
            assertThat(encrypted.encryptedData()).isNotBlank();
            assertThat(encrypted.algorithm()).isEqualTo("AES-GCM");
        }

        @Test
        @DisplayName("decrypt should recover original data")
        void decryptShouldRecoverOriginalData() {
            Result<String> original = Result.ok("secret message");

            EncryptedResult encrypted = encryptor.encrypt(original);
            Result<String> decrypted = encryptor.decrypt(encrypted, String.class);

            assertThat(decrypted.data()).isEqualTo("secret message");
        }

        @Test
        @DisplayName("encrypt and decrypt should work with integer data")
        void encryptAndDecryptShouldWorkWithIntegerData() {
            Result<Integer> original = Result.ok(42);

            EncryptedResult encrypted = encryptor.encrypt(original);
            Result<Integer> decrypted = encryptor.decrypt(encrypted, Integer.class);

            assertThat(decrypted.data()).isEqualTo(42);
        }

        @Test
        @DisplayName("encrypt and decrypt should work with null data")
        void encryptAndDecryptShouldWorkWithNullData() {
            Result<String> original = Result.ok();

            EncryptedResult encrypted = encryptor.encrypt(original);
            Result<String> decrypted = encryptor.decrypt(encrypted, String.class);

            assertThat(decrypted.data()).isNull();
        }
    }

    @Nested
    @DisplayName("Algorithm Tests")
    class AlgorithmTests {

        @Test
        @DisplayName("getAlgorithm should return AES-GCM")
        void getAlgorithmShouldReturnAesGcm() {
            assertThat(encryptor.getAlgorithm()).isEqualTo("AES-GCM");
        }

        @Test
        @DisplayName("supports should return true for AES-GCM")
        void supportsShouldReturnTrueForAesGcm() {
            assertThat(encryptor.supports("AES-GCM")).isTrue();
            assertThat(encryptor.supports("aes-gcm")).isTrue();
        }

        @Test
        @DisplayName("supports should return false for other algorithms")
        void supportsShouldReturnFalseForOtherAlgorithms() {
            assertThat(encryptor.supports("RSA")).isFalse();
        }
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("withRandomKey should create encryptor with random key")
        void withRandomKeyShouldCreateEncryptorWithRandomKey() {
            AesResultEncryptor.KeyAndEncryptor pair = AesResultEncryptor.withRandomKey();

            assertThat(pair.key()).hasSize(32);
            assertThat(pair.encryptor()).isNotNull();
            assertThat(pair.encryptor().getAlgorithm()).isEqualTo("AES-GCM");
        }

        @Test
        @DisplayName("encryptor from withRandomKey should work correctly")
        void encryptorFromWithRandomKeyShouldWorkCorrectly() {
            AesResultEncryptor.KeyAndEncryptor pair = AesResultEncryptor.withRandomKey();
            AesResultEncryptor enc = pair.encryptor();

            Result<String> original = Result.ok("test data");
            EncryptedResult encrypted = enc.encrypt(original);
            Result<String> decrypted = enc.decrypt(encrypted, String.class);

            assertThat(decrypted.data()).isEqualTo("test data");
        }
    }
}
