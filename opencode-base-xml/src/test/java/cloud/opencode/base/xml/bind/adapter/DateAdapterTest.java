package cloud.opencode.base.xml.bind.adapter;

import org.junit.jupiter.api.*;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static org.assertj.core.api.Assertions.*;

/**
 * DateAdapterTest Tests
 * DateAdapterTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
@DisplayName("DateAdapter Tests")
class DateAdapterTest {

    @Nested
    @DisplayName("LocalDateAdapter Tests")
    class LocalDateAdapterTests {

        @Test
        @DisplayName("unmarshal should parse ISO date")
        void unmarshalShouldParseIsoDate() throws Exception {
            DateAdapter.LocalDateAdapter adapter = new DateAdapter.LocalDateAdapter();

            LocalDate result = adapter.unmarshal("2024-01-15");

            assertThat(result).isEqualTo(LocalDate.of(2024, 1, 15));
        }

        @Test
        @DisplayName("marshal should format to ISO date")
        void marshalShouldFormatToIsoDate() throws Exception {
            DateAdapter.LocalDateAdapter adapter = new DateAdapter.LocalDateAdapter();
            LocalDate date = LocalDate.of(2024, 1, 15);

            String result = adapter.marshal(date);

            assertThat(result).isEqualTo("2024-01-15");
        }

        @Test
        @DisplayName("unmarshal should return null for null input")
        void unmarshalShouldReturnNullForNullInput() throws Exception {
            DateAdapter.LocalDateAdapter adapter = new DateAdapter.LocalDateAdapter();

            LocalDate result = adapter.unmarshal(null);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("marshal should return null for null input")
        void marshalShouldReturnNullForNullInput() throws Exception {
            DateAdapter.LocalDateAdapter adapter = new DateAdapter.LocalDateAdapter();

            String result = adapter.marshal(null);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("custom formatter should work")
        void customFormatterShouldWork() throws Exception {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateAdapter.LocalDateAdapter adapter = new DateAdapter.LocalDateAdapter(formatter);

            LocalDate date = adapter.unmarshal("15/01/2024");
            String formatted = adapter.marshal(date);

            assertThat(date).isEqualTo(LocalDate.of(2024, 1, 15));
            assertThat(formatted).isEqualTo("15/01/2024");
        }
    }

    @Nested
    @DisplayName("LocalDateTimeAdapter Tests")
    class LocalDateTimeAdapterTests {

        @Test
        @DisplayName("unmarshal should parse ISO date time")
        void unmarshalShouldParseIsoDateTime() throws Exception {
            DateAdapter.LocalDateTimeAdapter adapter = new DateAdapter.LocalDateTimeAdapter();

            LocalDateTime result = adapter.unmarshal("2024-01-15T10:30:00");

            assertThat(result).isEqualTo(LocalDateTime.of(2024, 1, 15, 10, 30, 0));
        }

        @Test
        @DisplayName("marshal should format to ISO date time")
        void marshalShouldFormatToIsoDateTime() throws Exception {
            DateAdapter.LocalDateTimeAdapter adapter = new DateAdapter.LocalDateTimeAdapter();
            LocalDateTime dateTime = LocalDateTime.of(2024, 1, 15, 10, 30, 0);

            String result = adapter.marshal(dateTime);

            assertThat(result).isEqualTo("2024-01-15T10:30:00");
        }

        @Test
        @DisplayName("unmarshal should return null for null input")
        void unmarshalShouldReturnNullForNullInput() throws Exception {
            DateAdapter.LocalDateTimeAdapter adapter = new DateAdapter.LocalDateTimeAdapter();

            assertThat(adapter.unmarshal(null)).isNull();
        }
    }

    @Nested
    @DisplayName("LocalTimeAdapter Tests")
    class LocalTimeAdapterTests {

        @Test
        @DisplayName("unmarshal should parse ISO time")
        void unmarshalShouldParseIsoTime() throws Exception {
            DateAdapter.LocalTimeAdapter adapter = new DateAdapter.LocalTimeAdapter();

            LocalTime result = adapter.unmarshal("10:30:00");

            assertThat(result).isEqualTo(LocalTime.of(10, 30, 0));
        }

        @Test
        @DisplayName("marshal should format to ISO time")
        void marshalShouldFormatToIsoTime() throws Exception {
            DateAdapter.LocalTimeAdapter adapter = new DateAdapter.LocalTimeAdapter();
            LocalTime time = LocalTime.of(10, 30, 0);

            String result = adapter.marshal(time);

            assertThat(result).isEqualTo("10:30:00");
        }

        @Test
        @DisplayName("unmarshal should return null for null input")
        void unmarshalShouldReturnNullForNullInput() throws Exception {
            DateAdapter.LocalTimeAdapter adapter = new DateAdapter.LocalTimeAdapter();

            assertThat(adapter.unmarshal(null)).isNull();
        }
    }

    @Nested
    @DisplayName("InstantAdapter Tests")
    class InstantAdapterTests {

        @Test
        @DisplayName("unmarshal should parse ISO instant")
        void unmarshalShouldParseIsoInstant() throws Exception {
            DateAdapter.InstantAdapter adapter = new DateAdapter.InstantAdapter();

            Instant result = adapter.unmarshal("2024-01-15T10:30:00Z");

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("marshal should format to ISO instant")
        void marshalShouldFormatToIsoInstant() throws Exception {
            DateAdapter.InstantAdapter adapter = new DateAdapter.InstantAdapter();
            Instant instant = Instant.parse("2024-01-15T10:30:00Z");

            String result = adapter.marshal(instant);

            assertThat(result).isEqualTo("2024-01-15T10:30:00Z");
        }

        @Test
        @DisplayName("unmarshal should return null for null input")
        void unmarshalShouldReturnNullForNullInput() throws Exception {
            DateAdapter.InstantAdapter adapter = new DateAdapter.InstantAdapter();

            assertThat(adapter.unmarshal(null)).isNull();
        }
    }

    @Nested
    @DisplayName("ZonedDateTimeAdapter Tests")
    class ZonedDateTimeAdapterTests {

        @Test
        @DisplayName("unmarshal should parse ISO zoned date time")
        void unmarshalShouldParseIsoZonedDateTime() throws Exception {
            DateAdapter.ZonedDateTimeAdapter adapter = new DateAdapter.ZonedDateTimeAdapter();

            ZonedDateTime result = adapter.unmarshal("2024-01-15T10:30:00+08:00[Asia/Shanghai]");

            assertThat(result).isNotNull();
            assertThat(result.getZone()).isEqualTo(ZoneId.of("Asia/Shanghai"));
        }

        @Test
        @DisplayName("marshal should format to ISO zoned date time")
        void marshalShouldFormatToIsoZonedDateTime() throws Exception {
            DateAdapter.ZonedDateTimeAdapter adapter = new DateAdapter.ZonedDateTimeAdapter();
            ZonedDateTime dateTime = ZonedDateTime.of(2024, 1, 15, 10, 30, 0, 0, ZoneId.of("UTC"));

            String result = adapter.marshal(dateTime);

            assertThat(result).contains("2024-01-15");
        }

        @Test
        @DisplayName("unmarshal should return null for null input")
        void unmarshalShouldReturnNullForNullInput() throws Exception {
            DateAdapter.ZonedDateTimeAdapter adapter = new DateAdapter.ZonedDateTimeAdapter();

            assertThat(adapter.unmarshal(null)).isNull();
        }
    }

    @Nested
    @DisplayName("LegacyDateAdapter Tests")
    class LegacyDateAdapterTests {

        @Test
        @DisplayName("unmarshal should parse ISO instant to Date")
        void unmarshalShouldParseIsoInstantToDate() throws Exception {
            DateAdapter.LegacyDateAdapter adapter = new DateAdapter.LegacyDateAdapter();

            Date result = adapter.unmarshal("2024-01-15T10:30:00Z");

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("marshal should format Date to ISO instant")
        void marshalShouldFormatDateToIsoInstant() throws Exception {
            DateAdapter.LegacyDateAdapter adapter = new DateAdapter.LegacyDateAdapter();
            Date date = Date.from(Instant.parse("2024-01-15T10:30:00Z"));

            String result = adapter.marshal(date);

            assertThat(result).contains("2024-01-15");
        }

        @Test
        @DisplayName("unmarshal should return null for null input")
        void unmarshalShouldReturnNullForNullInput() throws Exception {
            DateAdapter.LegacyDateAdapter adapter = new DateAdapter.LegacyDateAdapter();

            assertThat(adapter.unmarshal(null)).isNull();
        }
    }

    @Nested
    @DisplayName("DurationAdapter Tests")
    class DurationAdapterTests {

        @Test
        @DisplayName("unmarshal should parse ISO duration")
        void unmarshalShouldParseIsoDuration() throws Exception {
            DateAdapter.DurationAdapter adapter = new DateAdapter.DurationAdapter();

            Duration result = adapter.unmarshal("PT2H30M");

            assertThat(result).isEqualTo(Duration.ofHours(2).plusMinutes(30));
        }

        @Test
        @DisplayName("marshal should format to ISO duration")
        void marshalShouldFormatToIsoDuration() throws Exception {
            DateAdapter.DurationAdapter adapter = new DateAdapter.DurationAdapter();
            Duration duration = Duration.ofHours(2).plusMinutes(30);

            String result = adapter.marshal(duration);

            assertThat(result).isEqualTo("PT2H30M");
        }

        @Test
        @DisplayName("unmarshal should return null for null input")
        void unmarshalShouldReturnNullForNullInput() throws Exception {
            DateAdapter.DurationAdapter adapter = new DateAdapter.DurationAdapter();

            assertThat(adapter.unmarshal(null)).isNull();
        }
    }

    @Nested
    @DisplayName("PeriodAdapter Tests")
    class PeriodAdapterTests {

        @Test
        @DisplayName("unmarshal should parse ISO period")
        void unmarshalShouldParseIsoPeriod() throws Exception {
            DateAdapter.PeriodAdapter adapter = new DateAdapter.PeriodAdapter();

            Period result = adapter.unmarshal("P1Y2M3D");

            assertThat(result).isEqualTo(Period.of(1, 2, 3));
        }

        @Test
        @DisplayName("marshal should format to ISO period")
        void marshalShouldFormatToIsoPeriod() throws Exception {
            DateAdapter.PeriodAdapter adapter = new DateAdapter.PeriodAdapter();
            Period period = Period.of(1, 2, 3);

            String result = adapter.marshal(period);

            assertThat(result).isEqualTo("P1Y2M3D");
        }

        @Test
        @DisplayName("unmarshal should return null for null input")
        void unmarshalShouldReturnNullForNullInput() throws Exception {
            DateAdapter.PeriodAdapter adapter = new DateAdapter.PeriodAdapter();

            assertThat(adapter.unmarshal(null)).isNull();
        }
    }
}
