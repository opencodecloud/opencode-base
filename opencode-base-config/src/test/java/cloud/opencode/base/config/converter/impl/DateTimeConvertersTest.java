package cloud.opencode.base.config.converter.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for DateTimeConverters.
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
@DisplayName("DateTimeConverters Tests")
class DateTimeConvertersTest {

    @Nested
    @DisplayName("LocalDateConverter Tests")
    class LocalDateConverterTests {

        private final DateTimeConverters.LocalDateConverter converter = new DateTimeConverters.LocalDateConverter();

        @Test
        @DisplayName("converts valid date string")
        void testValidConversion() {
            assertThat(converter.convert("2024-01-15")).isEqualTo(LocalDate.of(2024, 1, 15));
        }

        @Test
        @DisplayName("converts end of month date")
        void testEndOfMonth() {
            assertThat(converter.convert("2024-02-29")).isEqualTo(LocalDate.of(2024, 2, 29));
        }

        @Test
        @DisplayName("throws for invalid date")
        void testInvalid() {
            assertThatThrownBy(() -> converter.convert("not-a-date"))
                    .isInstanceOf(DateTimeException.class);
        }

        @Test
        @DisplayName("getType returns LocalDate.class")
        void testGetType() {
            assertThat(converter.getType()).isEqualTo(LocalDate.class);
        }
    }

    @Nested
    @DisplayName("LocalTimeConverter Tests")
    class LocalTimeConverterTests {

        private final DateTimeConverters.LocalTimeConverter converter = new DateTimeConverters.LocalTimeConverter();

        @Test
        @DisplayName("converts valid time string")
        void testValidConversion() {
            assertThat(converter.convert("10:30:00")).isEqualTo(LocalTime.of(10, 30, 0));
        }

        @Test
        @DisplayName("converts time without seconds")
        void testWithoutSeconds() {
            assertThat(converter.convert("10:30")).isEqualTo(LocalTime.of(10, 30));
        }

        @Test
        @DisplayName("throws for invalid time")
        void testInvalid() {
            assertThatThrownBy(() -> converter.convert("invalid"))
                    .isInstanceOf(DateTimeException.class);
        }

        @Test
        @DisplayName("getType returns LocalTime.class")
        void testGetType() {
            assertThat(converter.getType()).isEqualTo(LocalTime.class);
        }
    }

    @Nested
    @DisplayName("LocalDateTimeConverter Tests")
    class LocalDateTimeConverterTests {

        private final DateTimeConverters.LocalDateTimeConverter converter = new DateTimeConverters.LocalDateTimeConverter();

        @Test
        @DisplayName("converts valid datetime string")
        void testValidConversion() {
            assertThat(converter.convert("2024-01-15T10:30:00"))
                    .isEqualTo(LocalDateTime.of(2024, 1, 15, 10, 30, 0));
        }

        @Test
        @DisplayName("throws for invalid datetime")
        void testInvalid() {
            assertThatThrownBy(() -> converter.convert("invalid"))
                    .isInstanceOf(DateTimeException.class);
        }

        @Test
        @DisplayName("getType returns LocalDateTime.class")
        void testGetType() {
            assertThat(converter.getType()).isEqualTo(LocalDateTime.class);
        }
    }

    @Nested
    @DisplayName("InstantConverter Tests")
    class InstantConverterTests {

        private final DateTimeConverters.InstantConverter converter = new DateTimeConverters.InstantConverter();

        @Test
        @DisplayName("converts valid instant string")
        void testValidConversion() {
            Instant expected = Instant.parse("2024-01-15T10:30:00Z");
            assertThat(converter.convert("2024-01-15T10:30:00Z")).isEqualTo(expected);
        }

        @Test
        @DisplayName("throws for invalid instant")
        void testInvalid() {
            assertThatThrownBy(() -> converter.convert("invalid"))
                    .isInstanceOf(DateTimeException.class);
        }

        @Test
        @DisplayName("getType returns Instant.class")
        void testGetType() {
            assertThat(converter.getType()).isEqualTo(Instant.class);
        }
    }

    @Nested
    @DisplayName("ZonedDateTimeConverter Tests")
    class ZonedDateTimeConverterTests {

        private final DateTimeConverters.ZonedDateTimeConverter converter = new DateTimeConverters.ZonedDateTimeConverter();

        @Test
        @DisplayName("converts valid zoned datetime string")
        void testValidConversion() {
            String input = "2024-01-15T10:30:00+08:00[Asia/Shanghai]";
            ZonedDateTime result = converter.convert(input);
            assertThat(result.getYear()).isEqualTo(2024);
            assertThat(result.getMonth()).isEqualTo(Month.JANUARY);
            assertThat(result.getDayOfMonth()).isEqualTo(15);
            assertThat(result.getZone()).isEqualTo(ZoneId.of("Asia/Shanghai"));
        }

        @Test
        @DisplayName("throws for invalid zoned datetime")
        void testInvalid() {
            assertThatThrownBy(() -> converter.convert("invalid"))
                    .isInstanceOf(DateTimeException.class);
        }

        @Test
        @DisplayName("getType returns ZonedDateTime.class")
        void testGetType() {
            assertThat(converter.getType()).isEqualTo(ZonedDateTime.class);
        }
    }
}
