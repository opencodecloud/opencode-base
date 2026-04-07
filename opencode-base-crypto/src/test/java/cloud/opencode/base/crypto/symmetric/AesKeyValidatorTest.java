package cloud.opencode.base.crypto.symmetric;

import cloud.opencode.base.crypto.exception.OpenKeyException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("AesKeyValidator")
class AesKeyValidatorTest {

    @Nested
    @DisplayName("validateKeyBytes")
    class ValidateKeyBytes {

        @Test
        @DisplayName("should accept 128-bit (16 byte) key")
        void shouldAccept128BitKey() {
            byte[] key = new byte[16];
            assertThatCode(() -> AesKeyValidator.validateKeyBytes(key, "AES"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should accept 192-bit (24 byte) key")
        void shouldAccept192BitKey() {
            byte[] key = new byte[24];
            assertThatCode(() -> AesKeyValidator.validateKeyBytes(key, "AES"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should accept 256-bit (32 byte) key")
        void shouldAccept256BitKey() {
            byte[] key = new byte[32];
            assertThatCode(() -> AesKeyValidator.validateKeyBytes(key, "AES"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should reject null key")
        void shouldRejectNullKey() {
            assertThatThrownBy(() -> AesKeyValidator.validateKeyBytes(null, "AES"))
                    .isInstanceOf(OpenKeyException.class)
                    .hasMessageContaining("128, 192, or 256 bits");
        }

        @Test
        @DisplayName("should reject 8-byte key")
        void shouldReject8ByteKey() {
            assertThatThrownBy(() -> AesKeyValidator.validateKeyBytes(new byte[8], "AES"))
                    .isInstanceOf(OpenKeyException.class);
        }

        @Test
        @DisplayName("should reject 15-byte key")
        void shouldReject15ByteKey() {
            assertThatThrownBy(() -> AesKeyValidator.validateKeyBytes(new byte[15], "AES"))
                    .isInstanceOf(OpenKeyException.class);
        }

        @Test
        @DisplayName("should reject 17-byte key")
        void shouldReject17ByteKey() {
            assertThatThrownBy(() -> AesKeyValidator.validateKeyBytes(new byte[17], "AES"))
                    .isInstanceOf(OpenKeyException.class);
        }

        @Test
        @DisplayName("should reject empty key")
        void shouldRejectEmptyKey() {
            assertThatThrownBy(() -> AesKeyValidator.validateKeyBytes(new byte[0], "AES"))
                    .isInstanceOf(OpenKeyException.class);
        }

        @Test
        @DisplayName("should include algorithm label in error message")
        void shouldIncludeAlgorithmLabel() {
            assertThatThrownBy(() -> AesKeyValidator.validateKeyBytes(new byte[10], "AES-GCM"))
                    .isInstanceOf(OpenKeyException.class)
                    .hasMessageContaining("AES-GCM");
        }
    }
}
