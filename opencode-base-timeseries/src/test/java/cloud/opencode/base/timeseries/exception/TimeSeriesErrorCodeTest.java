package cloud.opencode.base.timeseries.exception;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * TimeSeriesErrorCodeTest Tests
 * TimeSeriesErrorCodeTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
@DisplayName("TimeSeriesErrorCode Tests")
class TimeSeriesErrorCodeTest {

    @Nested
    @DisplayName("Enum Values Tests")
    class EnumValuesTests {

        @Test
        @DisplayName("should have all expected error codes")
        void shouldHaveAllExpectedErrorCodes() {
            TimeSeriesErrorCode[] codes = TimeSeriesErrorCode.values();

            assertThat(codes).contains(
                TimeSeriesErrorCode.INVALID_TIMESTAMP,
                TimeSeriesErrorCode.INVALID_VALUE,
                TimeSeriesErrorCode.DUPLICATE_TIMESTAMP,
                TimeSeriesErrorCode.EMPTY_SERIES,
                TimeSeriesErrorCode.QUERY_RANGE_TOO_LARGE,
                TimeSeriesErrorCode.INVALID_TIME_RANGE,
                TimeSeriesErrorCode.SERIES_NOT_FOUND,
                TimeSeriesErrorCode.AGGREGATION_FAILED,
                TimeSeriesErrorCode.WINDOW_SIZE_INVALID,
                TimeSeriesErrorCode.INSUFFICIENT_DATA,
                TimeSeriesErrorCode.CAPACITY_EXCEEDED,
                TimeSeriesErrorCode.MEMORY_LIMIT_EXCEEDED
            );
        }

        @Test
        @DisplayName("valueOf should return correct enum")
        void valueOfShouldReturnCorrectEnum() {
            assertThat(TimeSeriesErrorCode.valueOf("INVALID_TIMESTAMP")).isEqualTo(TimeSeriesErrorCode.INVALID_TIMESTAMP);
            assertThat(TimeSeriesErrorCode.valueOf("EMPTY_SERIES")).isEqualTo(TimeSeriesErrorCode.EMPTY_SERIES);
        }

        @Test
        @DisplayName("valueOf should throw for invalid name")
        void valueOfShouldThrowForInvalidName() {
            assertThatThrownBy(() -> TimeSeriesErrorCode.valueOf("INVALID_CODE"))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Code Method Tests")
    class CodeMethodTests {

        @Test
        @DisplayName("code should return unique code string")
        void codeShouldReturnUniqueCodeString() {
            assertThat(TimeSeriesErrorCode.INVALID_TIMESTAMP.code()).isNotBlank();
            assertThat(TimeSeriesErrorCode.INVALID_VALUE.code()).isNotBlank();
        }

        @Test
        @DisplayName("all codes should be unique")
        void allCodesShouldBeUnique() {
            String[] codes = new String[TimeSeriesErrorCode.values().length];
            int i = 0;
            for (TimeSeriesErrorCode errorCode : TimeSeriesErrorCode.values()) {
                codes[i++] = errorCode.code();
            }

            assertThat(codes).doesNotHaveDuplicates();
        }
    }

    @Nested
    @DisplayName("Message Method Tests")
    class MessageMethodTests {

        @Test
        @DisplayName("message should return descriptive message")
        void messageShouldReturnDescriptiveMessage() {
            assertThat(TimeSeriesErrorCode.INVALID_TIMESTAMP.message()).isNotBlank();
            assertThat(TimeSeriesErrorCode.EMPTY_SERIES.message()).isNotBlank();
            assertThat(TimeSeriesErrorCode.QUERY_RANGE_TOO_LARGE.message()).isNotBlank();
        }

        @Test
        @DisplayName("all error codes should have messages")
        void allErrorCodesShouldHaveMessages() {
            for (TimeSeriesErrorCode errorCode : TimeSeriesErrorCode.values()) {
                assertThat(errorCode.message())
                    .as("Error code %s should have a message", errorCode.name())
                    .isNotBlank();
            }
        }
    }

    @Nested
    @DisplayName("Individual Error Code Tests")
    class IndividualErrorCodeTests {

        @Test
        @DisplayName("INVALID_TIMESTAMP should have correct properties")
        void invalidTimestampShouldHaveCorrectProperties() {
            TimeSeriesErrorCode code = TimeSeriesErrorCode.INVALID_TIMESTAMP;

            assertThat(code.code()).isNotBlank();
            assertThat(code.message()).containsIgnoringCase("timestamp");
        }

        @Test
        @DisplayName("INVALID_VALUE should have correct properties")
        void invalidValueShouldHaveCorrectProperties() {
            TimeSeriesErrorCode code = TimeSeriesErrorCode.INVALID_VALUE;

            assertThat(code.code()).isNotBlank();
            assertThat(code.message()).containsIgnoringCase("value");
        }

        @Test
        @DisplayName("DUPLICATE_TIMESTAMP should have correct properties")
        void duplicateTimestampShouldHaveCorrectProperties() {
            TimeSeriesErrorCode code = TimeSeriesErrorCode.DUPLICATE_TIMESTAMP;

            assertThat(code.code()).isNotBlank();
            assertThat(code.message()).isNotBlank();
        }

        @Test
        @DisplayName("EMPTY_SERIES should have correct properties")
        void emptySeriesShouldHaveCorrectProperties() {
            TimeSeriesErrorCode code = TimeSeriesErrorCode.EMPTY_SERIES;

            assertThat(code.code()).isNotBlank();
            assertThat(code.message()).isNotBlank();
        }

        @Test
        @DisplayName("QUERY_RANGE_TOO_LARGE should have correct properties")
        void queryRangeTooLargeShouldHaveCorrectProperties() {
            TimeSeriesErrorCode code = TimeSeriesErrorCode.QUERY_RANGE_TOO_LARGE;

            assertThat(code.code()).isNotBlank();
            assertThat(code.message()).containsIgnoringCase("range");
        }

        @Test
        @DisplayName("SERIES_NOT_FOUND should have correct properties")
        void seriesNotFoundShouldHaveCorrectProperties() {
            TimeSeriesErrorCode code = TimeSeriesErrorCode.SERIES_NOT_FOUND;

            assertThat(code.code()).isNotBlank();
            assertThat(code.message()).containsIgnoringCase("series");
        }

        @Test
        @DisplayName("WINDOW_SIZE_INVALID should have correct properties")
        void windowSizeInvalidShouldHaveCorrectProperties() {
            TimeSeriesErrorCode code = TimeSeriesErrorCode.WINDOW_SIZE_INVALID;

            assertThat(code.code()).isNotBlank();
            assertThat(code.message()).containsIgnoringCase("window");
        }

        @Test
        @DisplayName("INSUFFICIENT_DATA should have correct properties")
        void insufficientDataShouldHaveCorrectProperties() {
            TimeSeriesErrorCode code = TimeSeriesErrorCode.INSUFFICIENT_DATA;

            assertThat(code.code()).isNotBlank();
            assertThat(code.message()).containsIgnoringCase("data");
        }

        @Test
        @DisplayName("CAPACITY_EXCEEDED should have correct properties")
        void capacityExceededShouldHaveCorrectProperties() {
            TimeSeriesErrorCode code = TimeSeriesErrorCode.CAPACITY_EXCEEDED;

            assertThat(code.code()).isNotBlank();
            assertThat(code.message()).containsIgnoringCase("capacity");
        }

        @Test
        @DisplayName("MEMORY_LIMIT_EXCEEDED should have correct properties")
        void memoryLimitExceededShouldHaveCorrectProperties() {
            TimeSeriesErrorCode code = TimeSeriesErrorCode.MEMORY_LIMIT_EXCEEDED;

            assertThat(code.code()).isNotBlank();
            assertThat(code.message()).containsIgnoringCase("memory");
        }
    }
}
