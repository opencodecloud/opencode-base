package cloud.opencode.base.string.desensitize.handler;

import org.junit.jupiter.api.*;

import java.lang.reflect.*;

import static org.assertj.core.api.Assertions.*;

/**
 * NumberHandlerTest Tests
 * NumberHandlerTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("NumberHandler Tests")
class NumberHandlerTest {

    @Nested
    @DisplayName("Utility Class Tests")
    class UtilityClassTests {

        @Test
        @DisplayName("Should not be instantiable")
        void shouldNotBeInstantiable() throws Exception {
            Constructor<NumberHandler> constructor = NumberHandler.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            assertThatThrownBy(constructor::newInstance)
                .isInstanceOf(InvocationTargetException.class);
        }
    }

    @Nested
    @DisplayName("Handle Tests")
    class HandleTests {

        @Test
        @DisplayName("Should return masked string for integer")
        void shouldReturnMaskedStringForInteger() {
            assertThat(NumberHandler.handle(123)).isEqualTo("***");
        }

        @Test
        @DisplayName("Should return masked string for long")
        void shouldReturnMaskedStringForLong() {
            assertThat(NumberHandler.handle(123456789L)).isEqualTo("***");
        }

        @Test
        @DisplayName("Should return masked string for double")
        void shouldReturnMaskedStringForDouble() {
            assertThat(NumberHandler.handle(3.14159)).isEqualTo("***");
        }

        @Test
        @DisplayName("Should return masked string for float")
        void shouldReturnMaskedStringForFloat() {
            assertThat(NumberHandler.handle(2.5f)).isEqualTo("***");
        }

        @Test
        @DisplayName("Should return null for null input")
        void shouldReturnNullForNullInput() {
            assertThat(NumberHandler.handle(null)).isNull();
        }

        @Test
        @DisplayName("Should return masked string for zero")
        void shouldReturnMaskedStringForZero() {
            assertThat(NumberHandler.handle(0)).isEqualTo("***");
        }

        @Test
        @DisplayName("Should return masked string for negative number")
        void shouldReturnMaskedStringForNegativeNumber() {
            assertThat(NumberHandler.handle(-100)).isEqualTo("***");
        }
    }
}
