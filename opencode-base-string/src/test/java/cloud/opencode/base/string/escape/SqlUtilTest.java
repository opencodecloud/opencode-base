package cloud.opencode.base.string.escape;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * SqlUtilTest Tests
 * SqlUtilTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
@DisplayName("SqlUtil Tests")
class SqlUtilTest {

    @Nested
    @DisplayName("escape Tests")
    class EscapeTests {

        @Test
        @DisplayName("Should escape single quote")
        void shouldEscapeSingleQuote() {
            assertThat(SqlUtil.escape("O'Brien")).isEqualTo("O''Brien");
        }

        @Test
        @DisplayName("Should escape multiple single quotes")
        void shouldEscapeMultipleSingleQuotes() {
            assertThat(SqlUtil.escape("it's John's")).isEqualTo("it''s John''s");
        }

        @Test
        @DisplayName("Should return null for null input")
        void shouldReturnNullForNullInput() {
            assertThat(SqlUtil.escape(null)).isNull();
        }

        @Test
        @DisplayName("Should preserve normal text")
        void shouldPreserveNormalText() {
            assertThat(SqlUtil.escape("Hello World")).isEqualTo("Hello World");
        }

        @Test
        @DisplayName("Should handle empty string")
        void shouldHandleEmptyString() {
            assertThat(SqlUtil.escape("")).isEmpty();
        }
    }

    @Nested
    @DisplayName("Utility Class Tests")
    class UtilityClassTests {

        @Test
        @DisplayName("Should not be instantiable")
        void shouldNotBeInstantiable() throws Exception {
            var constructor = SqlUtil.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            assertThatCode(constructor::newInstance).isInstanceOf(Exception.class);
        }
    }
}
