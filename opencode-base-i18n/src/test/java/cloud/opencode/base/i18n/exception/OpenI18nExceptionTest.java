package cloud.opencode.base.i18n.exception;

import cloud.opencode.base.core.exception.OpenException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for OpenI18nException
 */
@DisplayName("OpenI18nException")
class OpenI18nExceptionTest {

    @Nested
    @DisplayName("Extends OpenException")
    class Hierarchy {
        @Test void extendsOpenException() {
            assertThat(new OpenI18nException("test")).isInstanceOf(OpenException.class);
        }
        @Test void isRuntimeException() {
            assertThat(new OpenI18nException("test")).isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("Constructors")
    class Constructors {
        @Test void messageOnly() {
            OpenI18nException ex = new OpenI18nException("Something went wrong");
            assertThat(ex.getRawMessage()).isEqualTo("Something went wrong");
            assertThat(ex.getComponent()).isEqualTo("i18n");
        }

        @Test void messageAndCause() {
            RuntimeException cause = new RuntimeException("root");
            OpenI18nException ex = new OpenI18nException("Failed", cause);
            assertThat(ex.getCause()).isSameAs(cause);
        }

        @Test void errorCodeAndMessage() {
            OpenI18nException ex = new OpenI18nException("MY_CODE", "My message");
            assertThat(ex.getErrorCode()).isEqualTo("MY_CODE");
        }
    }

    @Nested
    @DisplayName("Factory methods")
    class FactoryMethods {
        @Test void formatError() {
            RuntimeException cause = new RuntimeException("parse fail");
            OpenI18nException ex = OpenI18nException.formatError("{broken}", cause);
            assertThat(ex.getErrorCode()).isEqualTo("FORMAT_ERROR");
            assertThat(ex.getMessage()).contains("{broken}");
            assertThat(ex.getCause()).isSameAs(cause);
        }

        @Test void parseError() {
            OpenI18nException ex = OpenI18nException.parseError("{bad", "Unclosed brace");
            assertThat(ex.getErrorCode()).isEqualTo("PARSE_ERROR");
            assertThat(ex.getMessage()).contains("{bad").contains("Unclosed brace");
        }
    }
}
