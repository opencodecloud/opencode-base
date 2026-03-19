package cloud.opencode.base.web.crypto;

import cloud.opencode.base.web.Result;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * ResultEncryptorTest Tests
 * ResultEncryptorTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
@DisplayName("ResultEncryptor Tests")
class ResultEncryptorTest {

    @Nested
    @DisplayName("Interface Default Method Tests")
    class InterfaceDefaultMethodTests {

        @Test
        @DisplayName("supports should check algorithm case insensitively")
        void supportsShouldCheckAlgorithmCaseInsensitively() {
            ResultEncryptor encryptor = new ResultEncryptor() {
                @Override
                public <T> EncryptedResult encrypt(Result<T> result) {
                    return null;
                }

                @Override
                public <T> Result<T> decrypt(EncryptedResult encrypted, Class<T> dataType) {
                    return null;
                }

                @Override
                public String getAlgorithm() {
                    return "TEST-ALGO";
                }
            };

            assertThat(encryptor.supports("TEST-ALGO")).isTrue();
            assertThat(encryptor.supports("test-algo")).isTrue();
            assertThat(encryptor.supports("Test-Algo")).isTrue();
            assertThat(encryptor.supports("OTHER")).isFalse();
        }
    }

    @Nested
    @DisplayName("Custom Implementation Tests")
    class CustomImplementationTests {

        @Test
        @DisplayName("custom encryptor should implement interface")
        void customEncryptorShouldImplementInterface() {
            ResultEncryptor encryptor = new ResultEncryptor() {
                @Override
                public <T> EncryptedResult encrypt(Result<T> result) {
                    return EncryptedResult.of(result.code(), "encrypted", getAlgorithm());
                }

                @Override
                public <T> Result<T> decrypt(EncryptedResult encrypted, Class<T> dataType) {
                    return Result.ok();
                }

                @Override
                public String getAlgorithm() {
                    return "CUSTOM";
                }
            };

            Result<String> original = Result.ok("data");
            EncryptedResult encrypted = encryptor.encrypt(original);

            assertThat(encrypted.algorithm()).isEqualTo("CUSTOM");
            assertThat(encryptor.supports("CUSTOM")).isTrue();
        }
    }
}
