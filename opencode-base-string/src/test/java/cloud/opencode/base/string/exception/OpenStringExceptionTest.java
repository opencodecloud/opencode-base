package cloud.opencode.base.string.exception;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenStringExceptionTest Tests
 * OpenStringExceptionTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("OpenStringException Tests")
class OpenStringExceptionTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Constructor with message should set message")
        void constructorWithMessageShouldSetMessage() {
            OpenStringException exception = new OpenStringException("Test error");

            assertThat(exception.getMessage()).isEqualTo("Test error");
            assertThat(exception.getCause()).isNull();
        }

        @Test
        @DisplayName("Constructor with message and cause should set both")
        void constructorWithMessageAndCauseShouldSetBoth() {
            RuntimeException cause = new RuntimeException("Root cause");
            OpenStringException exception = new OpenStringException("Test error", cause);

            assertThat(exception.getMessage()).isEqualTo("Test error");
            assertThat(exception.getCause()).isSameAs(cause);
        }
    }

    @Nested
    @DisplayName("Inheritance Tests")
    class InheritanceTests {

        @Test
        @DisplayName("Should be a RuntimeException")
        void shouldBeRuntimeException() {
            OpenStringException exception = new OpenStringException("Test");

            assertThat(exception).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Should be throwable")
        void shouldBeThrowable() {
            assertThatThrownBy(() -> {
                throw new OpenStringException("Test error");
            }).isInstanceOf(OpenStringException.class)
              .hasMessage("Test error");
        }

        @Test
        @DisplayName("Should be catchable as Exception")
        void shouldBeCatchableAsException() {
            try {
                throw new OpenStringException("Test");
            } catch (Exception e) {
                assertThat(e).isInstanceOf(OpenStringException.class);
            }
        }
    }
}
