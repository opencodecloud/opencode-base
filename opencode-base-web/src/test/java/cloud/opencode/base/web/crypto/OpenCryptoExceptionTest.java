package cloud.opencode.base.web.crypto;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenCryptoExceptionTest Tests
 * OpenCryptoExceptionTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
@DisplayName("OpenCryptoException Tests")
class OpenCryptoExceptionTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("constructor with message should set message")
        void constructorWithMessageShouldSetMessage() {
            OpenCryptoException exception = new OpenCryptoException("Test message");

            assertThat(exception.getMessage()).isEqualTo("Test message");
            assertThat(exception.getCode()).isEqualTo("C1001");
            assertThat(exception.getHttpStatus()).isEqualTo(500);
        }

        @Test
        @DisplayName("constructor with message and cause should set both")
        void constructorWithMessageAndCauseShouldSetBoth() {
            Exception cause = new RuntimeException("Root cause");
            OpenCryptoException exception = new OpenCryptoException("Test message", cause);

            assertThat(exception.getMessage()).isEqualTo("Test message");
            assertThat(exception.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("Factory Methods Tests")
    class FactoryMethodsTests {

        @Test
        @DisplayName("encryptionFailed should create encryption exception")
        void encryptionFailedShouldCreateEncryptionException() {
            OpenCryptoException exception = OpenCryptoException.encryptionFailed("cipher error");

            assertThat(exception.getMessage()).contains("Encryption failed");
            assertThat(exception.getMessage()).contains("cipher error");
        }

        @Test
        @DisplayName("decryptionFailed should create decryption exception")
        void decryptionFailedShouldCreateDecryptionException() {
            OpenCryptoException exception = OpenCryptoException.decryptionFailed("bad data");

            assertThat(exception.getMessage()).contains("Decryption failed");
            assertThat(exception.getMessage()).contains("bad data");
        }

        @Test
        @DisplayName("invalidKey should create key exception")
        void invalidKeyShouldCreateKeyException() {
            OpenCryptoException exception = OpenCryptoException.invalidKey("key too short");

            assertThat(exception.getMessage()).contains("Invalid key");
            assertThat(exception.getMessage()).contains("key too short");
        }
    }

    @Nested
    @DisplayName("Inheritance Tests")
    class InheritanceTests {

        @Test
        @DisplayName("should extend OpenWebException")
        void shouldExtendOpenWebException() {
            OpenCryptoException exception = new OpenCryptoException("Test");

            assertThat(exception).isInstanceOf(cloud.opencode.base.web.exception.OpenWebException.class);
            assertThat(exception).isInstanceOf(RuntimeException.class);
        }
    }
}
