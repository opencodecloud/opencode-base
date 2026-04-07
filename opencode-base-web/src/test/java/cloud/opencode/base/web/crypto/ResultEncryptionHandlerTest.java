package cloud.opencode.base.web.crypto;

import cloud.opencode.base.json.TypeReference;
import cloud.opencode.base.web.Result;
import org.junit.jupiter.api.*;

import java.lang.annotation.Annotation;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * ResultEncryptionHandler Tests
 * ResultEncryptionHandler 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
@DisplayName("ResultEncryptionHandler 测试")
class ResultEncryptionHandlerTest {

    private static final byte[] TEST_KEY = new byte[32];
    static {
        for (int i = 0; i < 32; i++) TEST_KEY[i] = (byte) i;
    }

    private ResultEncryptionHandler handler;

    @BeforeEach
    void setup() {
        EncryptionKeyResolver resolver = alias -> TEST_KEY;
        handler = new ResultEncryptionHandler(resolver);
    }

    @Nested
    @DisplayName("构造器测试")
    class ConstructorTests {

        @Test
        @DisplayName("null keyResolver应抛出异常")
        void nullResolverShouldThrow() {
            assertThatThrownBy(() -> new ResultEncryptionHandler(null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("encrypt加密测试")
    class EncryptTests {

        @Test
        @DisplayName("通过注解加密")
        void encryptWithAnnotation() {
            Result<String> result = Result.ok("secret");
            EncryptResult annotation = createEncryptAnnotation("", "", true);

            EncryptedResult encrypted = handler.encrypt(result, annotation);

            assertThat(encrypted.code()).isEqualTo("00000");
            assertThat(encrypted.encryptedData()).isNotBlank();
            assertThat(encrypted.sign()).isNotBlank();
            assertThat(encrypted.algorithm()).isEqualTo("AES-GCM");
        }

        @Test
        @DisplayName("通过keyAlias和algorithm加密")
        void encryptWithExplicitParams() {
            Result<String> result = Result.ok("secret");

            EncryptedResult encrypted = handler.encrypt(result, "", "AES-GCM");

            assertThat(encrypted.encryptedData()).isNotBlank();
            assertThat(encrypted.sign()).isNotBlank();
        }
    }

    @Nested
    @DisplayName("decrypt解密测试")
    class DecryptTests {

        @Test
        @DisplayName("通过Class解密")
        void decryptWithClass() {
            Result<String> original = Result.ok("hello");
            EncryptedResult encrypted = handler.encrypt(original, "", "");
            DecryptResult annotation = createDecryptAnnotation("", "");

            Result<String> decrypted = handler.decrypt(encrypted, String.class, annotation);

            assertThat(decrypted.data()).isEqualTo("hello");
        }

        @Test
        @DisplayName("通过TypeReference解密泛型")
        void decryptWithTypeReference() {
            Result<List<String>> original = new Result<>("00000", "Success",
                List.of("a", "b"), true, Instant.now(), null);
            EncryptedResult encrypted = handler.encrypt(original, "", "");
            DecryptResult annotation = createDecryptAnnotation("", "");

            Result<List<String>> decrypted = handler.decrypt(encrypted,
                new TypeReference<List<String>>() {}, annotation);

            assertThat(decrypted.data()).containsExactly("a", "b");
        }

        @Test
        @DisplayName("通过显式参数解密")
        void decryptWithExplicitParams() {
            Result<Integer> original = Result.ok(42);
            EncryptedResult encrypted = handler.encrypt(original, "", "");

            Result<Integer> decrypted = handler.decrypt(encrypted, Integer.class, "", "");

            assertThat(decrypted.data()).isEqualTo(42);
        }

        @Test
        @DisplayName("篡改后解密应抛出异常")
        void tamperShouldThrow() {
            Result<String> original = Result.ok("secret");
            EncryptedResult encrypted = handler.encrypt(original, "", "");

            EncryptedResult tampered = new EncryptedResult(
                encrypted.code(), "TAMPERED", encrypted.encryptedData(),
                encrypted.algorithm(), encrypted.timestamp(), encrypted.traceId(), encrypted.sign()
            );

            assertThatThrownBy(() -> handler.decrypt(tampered, String.class, "", ""))
                .isInstanceOf(OpenCryptoException.class)
                .hasMessageContaining("tampered");
        }
    }

    @Nested
    @DisplayName("shouldEncrypt判断测试")
    class ShouldEncryptTests {

        @Test
        @DisplayName("annotation为null返回false")
        void nullAnnotationReturnsFalse() {
            assertThat(handler.shouldEncrypt(null)).isFalse();
        }

        @Test
        @DisplayName("enabled=true返回true")
        void enabledReturnsTrue() {
            assertThat(handler.shouldEncrypt(createEncryptAnnotation("", "", true))).isTrue();
        }

        @Test
        @DisplayName("enabled=false返回false")
        void disabledReturnsFalse() {
            assertThat(handler.shouldEncrypt(createEncryptAnnotation("", "", false))).isFalse();
        }
    }

    @Nested
    @DisplayName("密钥解析测试")
    class KeyResolverTests {

        @Test
        @DisplayName("不同keyAlias使用不同密钥")
        void differentKeysForDifferentAliases() {
            byte[] key1 = new byte[32];
            byte[] key2 = new byte[32];
            for (int i = 0; i < 32; i++) { key1[i] = (byte) i; key2[i] = (byte) (i + 1); }

            EncryptionKeyResolver multiKeyResolver = alias ->
                "partner".equals(alias) ? key2 : key1;
            ResultEncryptionHandler multiHandler = new ResultEncryptionHandler(multiKeyResolver);

            Result<String> result = Result.ok("data");
            EncryptedResult enc1 = multiHandler.encrypt(result, "", "");
            EncryptedResult enc2 = multiHandler.encrypt(result, "partner", "");

            // Different keys produce different ciphertext
            assertThat(enc1.encryptedData()).isNotEqualTo(enc2.encryptedData());
        }

        @Test
        @DisplayName("keyResolver返回null应抛出异常")
        void nullKeyThrows() {
            ResultEncryptionHandler badHandler = new ResultEncryptionHandler(alias -> null);
            Result<String> result = Result.ok("data");

            assertThatThrownBy(() -> badHandler.encrypt(result, "", ""))
                .isInstanceOf(OpenCryptoException.class)
                .hasMessageContaining("No key found");
        }

        @Test
        @DisplayName("不支持的算法应抛出异常")
        void unsupportedAlgorithmThrows() {
            Result<String> result = Result.ok("data");

            assertThatThrownBy(() -> handler.encrypt(result, "", "SM4-GCM"))
                .isInstanceOf(OpenCryptoException.class)
                .hasMessageContaining("Unsupported algorithm");
        }
    }

    // === Helper methods to create annotation instances ===

    private static EncryptResult createEncryptAnnotation(String keyAlias, String algorithm, boolean enabled) {
        return new EncryptResult() {
            @Override public Class<? extends Annotation> annotationType() { return EncryptResult.class; }
            @Override public String keyAlias() { return keyAlias; }
            @Override public String algorithm() { return algorithm; }
            @Override public boolean enabled() { return enabled; }
        };
    }

    private static DecryptResult createDecryptAnnotation(String keyAlias, String algorithm) {
        return new DecryptResult() {
            @Override public Class<? extends Annotation> annotationType() { return DecryptResult.class; }
            @Override public String keyAlias() { return keyAlias; }
            @Override public String algorithm() { return algorithm; }
        };
    }
}
