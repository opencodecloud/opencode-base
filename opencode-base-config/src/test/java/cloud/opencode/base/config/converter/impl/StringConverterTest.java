package cloud.opencode.base.config.converter.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for StringConverter.
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
@DisplayName("StringConverter Tests")
class StringConverterTest {

    private StringConverter converter;

    @BeforeEach
    void setUp() {
        converter = new StringConverter();
    }

    @Nested
    @DisplayName("Convert Tests")
    class ConvertTests {

        @Test
        @DisplayName("convert returns same string value")
        void testPassthrough() {
            assertThat(converter.convert("hello")).isEqualTo("hello");
        }

        @Test
        @DisplayName("convert preserves empty string")
        void testEmptyString() {
            assertThat(converter.convert("")).isEmpty();
        }

        @Test
        @DisplayName("convert preserves whitespace")
        void testWhitespace() {
            assertThat(converter.convert("  spaces  ")).isEqualTo("  spaces  ");
        }

        @Test
        @DisplayName("convert preserves special characters")
        void testSpecialCharacters() {
            String special = "!@#$%^&*()_+-={}|[];':\",.<>?/`~";
            assertThat(converter.convert(special)).isEqualTo(special);
        }
    }

    @Nested
    @DisplayName("Type Tests")
    class TypeTests {

        @Test
        @DisplayName("getType returns String.class")
        void testGetType() {
            assertThat(converter.getType()).isEqualTo(String.class);
        }
    }
}
