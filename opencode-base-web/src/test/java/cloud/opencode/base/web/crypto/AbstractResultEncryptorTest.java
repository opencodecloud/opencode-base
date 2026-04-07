package cloud.opencode.base.web.crypto;

import cloud.opencode.base.json.TypeReference;
import cloud.opencode.base.web.Result;
import org.junit.jupiter.api.*;

import cloud.opencode.base.crypto.OpenDigest;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AbstractResultEncryptor 抽象基类测试")
class AbstractResultEncryptorTest {

    private TestEncryptor encryptor;

    @BeforeEach
    void setup() {
        encryptor = new TestEncryptor();
    }

    @Nested
    @DisplayName("encrypt和decrypt方法测试")
    class EncryptDecryptTests {

        @Test
        @DisplayName("encrypt加密Result并带签名")
        void testEncryptWithSign() {
            Result<String> result = new Result<>("00000", "Success", "hello", true, Instant.now(), null);
            EncryptedResult encrypted = encryptor.encrypt(result);
            assertThat(encrypted).isNotNull();
            assertThat(encrypted.code()).isEqualTo("00000");
            assertThat(encrypted.message()).isEqualTo("Success");
            assertThat(encrypted.algorithm()).isEqualTo("TEST");
            assertThat(encrypted.encryptedData()).isNotNull();
            assertThat(encrypted.sign()).isNotNull().isNotEmpty();
        }

        @Test
        @DisplayName("encrypt处理null数据")
        void testEncryptNullData() {
            Result<String> result = new Result<>("00000", "Success", null, true, Instant.now(), null);
            EncryptedResult encrypted = encryptor.encrypt(result);
            assertThat(encrypted).isNotNull();
            assertThat(encrypted.sign()).isNotNull();
        }

        @Test
        @DisplayName("decrypt解密String类型")
        void testDecryptString() {
            Result<String> original = new Result<>("00000", "Success", "hello", true, Instant.now(), null);
            EncryptedResult encrypted = encryptor.encrypt(original);
            Result<String> decrypted = encryptor.decrypt(encrypted, String.class);
            assertThat(decrypted).isNotNull();
            assertThat(decrypted.data()).isEqualTo("hello");
            assertThat(decrypted.message()).isEqualTo("Success");
        }

        @Test
        @DisplayName("decrypt解密Integer类型")
        void testDecryptInteger() {
            Result<Integer> original = new Result<>("00000", "Success", 42, true, Instant.now(), null);
            EncryptedResult encrypted = encryptor.encrypt(original);
            Result<Integer> decrypted = encryptor.decrypt(encrypted, Integer.class);
            assertThat(decrypted.data()).isEqualTo(42);
        }

        @Test
        @DisplayName("decrypt解密null数据")
        void testDecryptNull() {
            Result<String> original = new Result<>("00000", "Success", null, true, Instant.now(), null);
            EncryptedResult encrypted = encryptor.encrypt(original);
            Result<String> decrypted = encryptor.decrypt(encrypted, String.class);
            assertThat(decrypted.data()).isNull();
        }
    }

    @Nested
    @DisplayName("TypeReference泛型解密测试")
    class TypeReferenceDecryptTests {

        @Test
        @DisplayName("decrypt解密List泛型")
        void testDecryptList() {
            List<String> list = List.of("a", "b", "c");
            Result<List<String>> original = new Result<>("00000", "Success", list, true, Instant.now(), null);
            EncryptedResult encrypted = encryptor.encrypt(original);
            Result<List<String>> decrypted = encryptor.decrypt(encrypted, new TypeReference<>() {});
            assertThat(decrypted.data()).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("decrypt解密Map泛型")
        void testDecryptMap() {
            Map<String, Integer> map = Map.of("x", 1, "y", 2);
            Result<Map<String, Integer>> original = new Result<>("00000", "Success", map, true, Instant.now(), null);
            EncryptedResult encrypted = encryptor.encrypt(original);
            Result<Map<String, Integer>> decrypted = encryptor.decrypt(encrypted, new TypeReference<>() {});
            assertThat(decrypted.data()).containsEntry("x", 1).containsEntry("y", 2);
        }

        @Test
        @DisplayName("decrypt保留message")
        void testDecryptPreservesMessage() {
            Result<String> original = new Result<>("A0404", "Not Found", null, false, Instant.now(), null);
            EncryptedResult encrypted = encryptor.encrypt(original);
            Result<String> decrypted = encryptor.decrypt(encrypted, new TypeReference<>() {});
            assertThat(decrypted.code()).isEqualTo("A0404");
            assertThat(decrypted.message()).isEqualTo("Not Found");
            assertThat(decrypted.success()).isFalse();
        }
    }

    @Nested
    @DisplayName("签名验证测试")
    class SignatureVerificationTests {

        @Test
        @DisplayName("篡改encryptedData应抛出异常")
        void tamperEncryptedDataShouldThrow() {
            Result<String> original = new Result<>("00000", "Success", "secret", true, Instant.now(), null);
            EncryptedResult encrypted = encryptor.encrypt(original);

            EncryptedResult tampered = new EncryptedResult(
                encrypted.code(), encrypted.message(), "TAMPERED_DATA",
                encrypted.algorithm(), encrypted.timestamp(), encrypted.traceId(), encrypted.sign()
            );

            assertThatThrownBy(() -> encryptor.decrypt(tampered, String.class))
                .isInstanceOf(OpenCryptoException.class)
                .hasMessageContaining("tampered");
        }

        @Test
        @DisplayName("篡改code应抛出异常")
        void tamperCodeShouldThrow() {
            Result<String> original = new Result<>("00000", "Success", "data", true, Instant.now(), null);
            EncryptedResult encrypted = encryptor.encrypt(original);

            EncryptedResult tampered = new EncryptedResult(
                "A0400", encrypted.message(), encrypted.encryptedData(),
                encrypted.algorithm(), encrypted.timestamp(), encrypted.traceId(), encrypted.sign()
            );

            assertThatThrownBy(() -> encryptor.decrypt(tampered, String.class))
                .isInstanceOf(OpenCryptoException.class)
                .hasMessageContaining("tampered");
        }

        @Test
        @DisplayName("篡改message应抛出异常")
        void tamperMessageShouldThrow() {
            Result<String> original = new Result<>("00000", "Success", "data", true, Instant.now(), null);
            EncryptedResult encrypted = encryptor.encrypt(original);

            EncryptedResult tampered = new EncryptedResult(
                encrypted.code(), "Hacked", encrypted.encryptedData(),
                encrypted.algorithm(), encrypted.timestamp(), encrypted.traceId(), encrypted.sign()
            );

            assertThatThrownBy(() -> encryptor.decrypt(tampered, String.class))
                .isInstanceOf(OpenCryptoException.class)
                .hasMessageContaining("tampered");
        }

        @Test
        @DisplayName("无签名的数据应拒绝解密（防止签名绕过）")
        void noSignShouldRejectDecrypt() {
            Result<String> original = new Result<>("00000", "Success", "hello", true, Instant.now(), null);
            EncryptedResult encrypted = encryptor.encrypt(original);

            // Remove sign
            EncryptedResult noSign = new EncryptedResult(
                encrypted.code(), encrypted.message(), encrypted.encryptedData(),
                encrypted.algorithm(), encrypted.timestamp(), encrypted.traceId(), null
            );

            assertThatThrownBy(() -> encryptor.decrypt(noSign, String.class))
                .isInstanceOf(OpenCryptoException.class)
                .hasMessageContaining("missing");
        }
    }

    /**
     * Test implementation that simply reverses bytes (no real encryption).
     *
     * @author Leon Soo
     * <a href="https://leonsoo.com">www.LeonSoo.com</a>
     * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
     * @since JDK 25, opencode-base-web V1.0.0
     */
    static class TestEncryptor extends AbstractResultEncryptor {

        @Override
        protected byte[] doEncrypt(byte[] data) {
            byte[] result = new byte[data.length];
            for (int i = 0; i < data.length; i++) {
                result[i] = data[data.length - 1 - i];
            }
            return result;
        }

        @Override
        protected byte[] doDecrypt(byte[] data) {
            return doEncrypt(data);
        }

        @Override
        protected byte[] doSign(byte[] data) {
            byte[] prefix = "test-hmac:".getBytes(StandardCharsets.UTF_8);
            byte[] combined = new byte[prefix.length + data.length];
            System.arraycopy(prefix, 0, combined, 0, prefix.length);
            System.arraycopy(data, 0, combined, prefix.length, data.length);
            return OpenDigest.sha256().digest(combined);
        }

        @Override
        public String getAlgorithm() {
            return "TEST";
        }
    }
}
