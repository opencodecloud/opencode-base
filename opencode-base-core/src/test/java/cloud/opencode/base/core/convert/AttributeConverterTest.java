package cloud.opencode.base.core.convert;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link AttributeConverter}.
 */
@DisplayName("AttributeConverter Tests")
class AttributeConverterTest {

    /**
     * A simple test implementation: Integer to String converter.
     */
    static class IntToStringConverter implements AttributeConverter<Integer, String> {
        @Override
        public String convertTo(Integer attribute) {
            return attribute == null ? null : attribute.toString();
        }

        @Override
        public Integer convertFrom(String stored) {
            return stored == null ? null : Integer.parseInt(stored);
        }
    }

    /**
     * LocalDate to String converter for testing.
     */
    static class LocalDateStringConverter implements AttributeConverter<LocalDate, String> {
        @Override
        public String convertTo(LocalDate attribute) {
            return attribute == null ? null : attribute.toString();
        }

        @Override
        public LocalDate convertFrom(String stored) {
            return stored == null ? null : LocalDate.parse(stored);
        }
    }

    @Nested
    @DisplayName("convertTo()")
    class ConvertToTests {

        @Test
        @DisplayName("converts attribute to storage type")
        void convertsToStorageType() {
            var converter = new IntToStringConverter();
            assertThat(converter.convertTo(42)).isEqualTo("42");
            assertThat(converter.convertTo(0)).isEqualTo("0");
            assertThat(converter.convertTo(-1)).isEqualTo("-1");
        }

        @Test
        @DisplayName("handles null attribute")
        void handlesNullAttribute() {
            var converter = new IntToStringConverter();
            assertThat(converter.convertTo(null)).isNull();
        }

        @Test
        @DisplayName("works with LocalDate")
        void worksWithLocalDate() {
            var converter = new LocalDateStringConverter();
            LocalDate date = LocalDate.of(2025, 1, 15);
            assertThat(converter.convertTo(date)).isEqualTo("2025-01-15");
        }
    }

    @Nested
    @DisplayName("convertFrom()")
    class ConvertFromTests {

        @Test
        @DisplayName("converts storage type back to attribute")
        void convertsFromStorageType() {
            var converter = new IntToStringConverter();
            assertThat(converter.convertFrom("42")).isEqualTo(42);
            assertThat(converter.convertFrom("0")).isZero();
            assertThat(converter.convertFrom("-1")).isEqualTo(-1);
        }

        @Test
        @DisplayName("handles null stored value")
        void handlesNullStored() {
            var converter = new IntToStringConverter();
            assertThat(converter.convertFrom(null)).isNull();
        }

        @Test
        @DisplayName("works with LocalDate")
        void worksWithLocalDate() {
            var converter = new LocalDateStringConverter();
            assertThat(converter.convertFrom("2025-01-15")).isEqualTo(LocalDate.of(2025, 1, 15));
        }
    }

    @Nested
    @DisplayName("Round-trip conversion")
    class RoundTripTests {

        @Test
        @DisplayName("convertTo then convertFrom returns original value")
        void roundTrip() {
            var converter = new IntToStringConverter();
            Integer original = 123;
            String stored = converter.convertTo(original);
            Integer restored = converter.convertFrom(stored);
            assertThat(restored).isEqualTo(original);
        }

        @Test
        @DisplayName("convertFrom then convertTo returns original value")
        void reverseRoundTrip() {
            var converter = new IntToStringConverter();
            String original = "456";
            Integer attribute = converter.convertFrom(original);
            String restored = converter.convertTo(attribute);
            assertThat(restored).isEqualTo(original);
        }

        @Test
        @DisplayName("null round-trips correctly")
        void nullRoundTrip() {
            var converter = new IntToStringConverter();
            assertThat(converter.convertFrom(converter.convertTo(null))).isNull();
        }
    }

    @Nested
    @DisplayName("Interface contract")
    class InterfaceContractTests {

        @Test
        @DisplayName("implementation is an instance of AttributeConverter")
        void isInstanceOf() {
            var converter = new IntToStringConverter();
            assertThat(converter).isInstanceOf(AttributeConverter.class);
        }

        @Test
        @DisplayName("anonymous implementation works")
        void anonymousImplementation() {
            AttributeConverter<Boolean, String> converter = new AttributeConverter<>() {
                @Override
                public String convertTo(Boolean attribute) {
                    return attribute == null ? null : attribute.toString();
                }

                @Override
                public Boolean convertFrom(String stored) {
                    return stored == null ? null : Boolean.parseBoolean(stored);
                }
            };

            assertThat(converter.convertTo(true)).isEqualTo("true");
            assertThat(converter.convertFrom("true")).isTrue();
            assertThat(converter.convertFrom("false")).isFalse();
        }
    }
}
