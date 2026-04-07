package cloud.opencode.base.i18n.key;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for I18nEnum interface - key derivation
 */
@DisplayName("I18nEnum")
class I18nEnumTest {

    enum ErrorCode implements I18nEnum { USER_NOT_FOUND, INVALID_INPUT, ACCESS_DENIED }
    enum HttpStatus implements I18nEnum { NOT_FOUND, INTERNAL_SERVER_ERROR }
    enum Simple implements I18nEnum { OK }

    @Nested
    @DisplayName("Automatic key derivation")
    class KeyDerivation {
        @Test void userNotFound() {
            assertThat(ErrorCode.USER_NOT_FOUND.key()).isEqualTo("error.code.user.not.found");
        }
        @Test void invalidInput() {
            assertThat(ErrorCode.INVALID_INPUT.key()).isEqualTo("error.code.invalid.input");
        }
        @Test void accessDenied() {
            assertThat(ErrorCode.ACCESS_DENIED.key()).isEqualTo("error.code.access.denied");
        }
        @Test void httpNotFound() {
            assertThat(HttpStatus.NOT_FOUND.key()).isEqualTo("http.status.not.found");
        }
        @Test void httpInternalServerError() {
            assertThat(HttpStatus.INTERNAL_SERVER_ERROR.key())
                    .isEqualTo("http.status.internal.server.error");
        }
        @Test void simpleOk() {
            assertThat(Simple.OK.key()).isEqualTo("simple.ok");
        }
    }

    @Nested
    @DisplayName("Override key()")
    class OverrideKey {
        enum Custom implements I18nEnum {
            MY_ERROR {
                @Override public String key() { return "custom.error"; }
            };
        }

        @Test void canOverrideKey() {
            assertThat(Custom.MY_ERROR.key()).isEqualTo("custom.error");
        }
    }
}
