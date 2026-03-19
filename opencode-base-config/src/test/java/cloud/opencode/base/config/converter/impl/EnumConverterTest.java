package cloud.opencode.base.config.converter.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for EnumConverter.
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
@DisplayName("EnumConverter Tests")
class EnumConverterTest {

    enum Status { ACTIVE, INACTIVE, PENDING }

    enum SingleValue { ONLY }

    @Nested
    @DisplayName("Conversion Tests")
    class ConversionTests {

        private final EnumConverter<Status> converter = new EnumConverter<>(Status.class);

        @Test
        @DisplayName("converts uppercase value")
        void testUppercase() {
            assertThat(converter.convert("ACTIVE")).isEqualTo(Status.ACTIVE);
        }

        @Test
        @DisplayName("converts lowercase value to uppercase")
        void testLowercase() {
            assertThat(converter.convert("active")).isEqualTo(Status.ACTIVE);
        }

        @Test
        @DisplayName("converts mixed case value to uppercase")
        void testMixedCase() {
            assertThat(converter.convert("Active")).isEqualTo(Status.ACTIVE);
        }

        @Test
        @DisplayName("converts all enum values")
        void testAllValues() {
            assertThat(converter.convert("ACTIVE")).isEqualTo(Status.ACTIVE);
            assertThat(converter.convert("INACTIVE")).isEqualTo(Status.INACTIVE);
            assertThat(converter.convert("PENDING")).isEqualTo(Status.PENDING);
        }

        @Test
        @DisplayName("throws for non-existent enum value")
        void testNonExistent() {
            assertThatThrownBy(() -> converter.convert("UNKNOWN"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Type Tests")
    class TypeTests {

        @Test
        @DisplayName("getType returns the enum class")
        void testGetType() {
            EnumConverter<Status> converter = new EnumConverter<>(Status.class);
            assertThat(converter.getType()).isEqualTo(Status.class);
        }

        @Test
        @DisplayName("getType returns correct class for different enum")
        void testGetTypeDifferentEnum() {
            EnumConverter<SingleValue> converter = new EnumConverter<>(SingleValue.class);
            assertThat(converter.getType()).isEqualTo(SingleValue.class);
        }
    }
}
