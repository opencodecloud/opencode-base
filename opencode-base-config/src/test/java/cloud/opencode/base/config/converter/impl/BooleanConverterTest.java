package cloud.opencode.base.config.converter.impl;

import cloud.opencode.base.config.OpenConfigException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for BooleanConverter.
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
@DisplayName("BooleanConverter Tests")
class BooleanConverterTest {

    private BooleanConverter converter;

    @BeforeEach
    void setUp() {
        converter = new BooleanConverter();
    }

    @Nested
    @DisplayName("True Value Tests")
    class TrueValueTests {

        @ParameterizedTest
        @ValueSource(strings = {"true", "yes", "on", "1", "enabled"})
        @DisplayName("converts truthy values to true")
        void testTruthyValues(String value) {
            assertThat(converter.convert(value)).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {"TRUE", "True", "YES", "Yes", "ON", "On", "ENABLED", "Enabled"})
        @DisplayName("converts truthy values case-insensitively")
        void testTruthyCaseInsensitive(String value) {
            assertThat(converter.convert(value)).isTrue();
        }
    }

    @Nested
    @DisplayName("False Value Tests")
    class FalseValueTests {

        @ParameterizedTest
        @ValueSource(strings = {"false", "no", "off", "0", "disabled"})
        @DisplayName("converts falsy values to false")
        void testFalsyValues(String value) {
            assertThat(converter.convert(value)).isFalse();
        }

        @ParameterizedTest
        @ValueSource(strings = {"FALSE", "False", "NO", "No", "OFF", "Off", "DISABLED", "Disabled"})
        @DisplayName("converts falsy values case-insensitively")
        void testFalsyCaseInsensitive(String value) {
            assertThat(converter.convert(value)).isFalse();
        }
    }

    @Nested
    @DisplayName("Invalid Value Tests")
    class InvalidValueTests {

        @ParameterizedTest
        @ValueSource(strings = {"maybe", "2", "yep", "nope", "active", ""})
        @DisplayName("throws OpenConfigException for invalid values")
        void testInvalidValues(String value) {
            assertThatThrownBy(() -> converter.convert(value))
                    .isInstanceOf(OpenConfigException.class);
        }
    }

    @Nested
    @DisplayName("Type Tests")
    class TypeTests {

        @Test
        @DisplayName("getType returns Boolean.class")
        void testGetType() {
            assertThat(converter.getType()).isEqualTo(Boolean.class);
        }
    }
}
