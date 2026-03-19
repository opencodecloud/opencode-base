package cloud.opencode.base.timeseries.exception;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * TimeSeriesExceptionTest Tests
 * TimeSeriesExceptionTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
@DisplayName("TimeSeriesException Tests")
class TimeSeriesExceptionTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("should create exception with error code only")
        void shouldCreateExceptionWithErrorCodeOnly() {
            TimeSeriesException exception = new TimeSeriesException(TimeSeriesErrorCode.INVALID_TIMESTAMP);

            assertThat(exception.errorCode()).isEqualTo(TimeSeriesErrorCode.INVALID_TIMESTAMP);
            assertThat(exception.getMessage()).contains(TimeSeriesErrorCode.INVALID_TIMESTAMP.message());
        }

        @Test
        @DisplayName("should create exception with error code and detail")
        void shouldCreateExceptionWithErrorCodeAndDetail() {
            TimeSeriesException exception = new TimeSeriesException(
                TimeSeriesErrorCode.INVALID_VALUE,
                "Value must be positive"
            );

            assertThat(exception.errorCode()).isEqualTo(TimeSeriesErrorCode.INVALID_VALUE);
            assertThat(exception.getMessage()).contains("Value must be positive");
        }

        @Test
        @DisplayName("should create exception with error code and cause")
        void shouldCreateExceptionWithErrorCodeAndCause() {
            RuntimeException cause = new RuntimeException("Root cause");
            TimeSeriesException exception = new TimeSeriesException(
                TimeSeriesErrorCode.AGGREGATION_FAILED,
                cause
            );

            assertThat(exception.errorCode()).isEqualTo(TimeSeriesErrorCode.AGGREGATION_FAILED);
            assertThat(exception.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("should create exception with error code, detail, and cause")
        void shouldCreateExceptionWithErrorCodeDetailAndCause() {
            RuntimeException cause = new RuntimeException("Root cause");
            TimeSeriesException exception = new TimeSeriesException(
                TimeSeriesErrorCode.QUERY_RANGE_TOO_LARGE,
                "Range exceeds 365 days",
                cause
            );

            assertThat(exception.errorCode()).isEqualTo(TimeSeriesErrorCode.QUERY_RANGE_TOO_LARGE);
            assertThat(exception.getMessage()).contains("Range exceeds 365 days");
            assertThat(exception.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("Error Code Access Tests")
    class ErrorCodeAccessTests {

        @Test
        @DisplayName("errorCode should return the error code")
        void errorCodeShouldReturnTheErrorCode() {
            TimeSeriesException exception = new TimeSeriesException(TimeSeriesErrorCode.EMPTY_SERIES);

            assertThat(exception.errorCode()).isEqualTo(TimeSeriesErrorCode.EMPTY_SERIES);
        }

        @Test
        @DisplayName("getErrorCodeString should return code string")
        void getErrorCodeStringShouldReturnCodeString() {
            TimeSeriesException exception = new TimeSeriesException(TimeSeriesErrorCode.INVALID_TIMESTAMP);

            assertThat(exception.getErrorCodeString()).isEqualTo(TimeSeriesErrorCode.INVALID_TIMESTAMP.code());
        }
    }

    @Nested
    @DisplayName("Exception Hierarchy Tests")
    class ExceptionHierarchyTests {

        @Test
        @DisplayName("should extend RuntimeException")
        void shouldExtendRuntimeException() {
            TimeSeriesException exception = new TimeSeriesException(TimeSeriesErrorCode.INVALID_TIMESTAMP);

            assertThat(exception).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("should be throwable and catchable")
        void shouldBeThrowableAndCatchable() {
            assertThatThrownBy(() -> {
                throw new TimeSeriesException(TimeSeriesErrorCode.SERIES_NOT_FOUND);
            })
                .isInstanceOf(TimeSeriesException.class)
                .extracting(e -> ((TimeSeriesException) e).errorCode())
                .isEqualTo(TimeSeriesErrorCode.SERIES_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("All Error Codes Tests")
    class AllErrorCodesTests {

        @Test
        @DisplayName("should support INVALID_TIMESTAMP")
        void shouldSupportInvalidTimestamp() {
            TimeSeriesException e = new TimeSeriesException(TimeSeriesErrorCode.INVALID_TIMESTAMP);
            assertThat(e.errorCode()).isEqualTo(TimeSeriesErrorCode.INVALID_TIMESTAMP);
        }

        @Test
        @DisplayName("should support INVALID_VALUE")
        void shouldSupportInvalidValue() {
            TimeSeriesException e = new TimeSeriesException(TimeSeriesErrorCode.INVALID_VALUE);
            assertThat(e.errorCode()).isEqualTo(TimeSeriesErrorCode.INVALID_VALUE);
        }

        @Test
        @DisplayName("should support DUPLICATE_TIMESTAMP")
        void shouldSupportDuplicateTimestamp() {
            TimeSeriesException e = new TimeSeriesException(TimeSeriesErrorCode.DUPLICATE_TIMESTAMP);
            assertThat(e.errorCode()).isEqualTo(TimeSeriesErrorCode.DUPLICATE_TIMESTAMP);
        }

        @Test
        @DisplayName("should support EMPTY_SERIES")
        void shouldSupportEmptySeries() {
            TimeSeriesException e = new TimeSeriesException(TimeSeriesErrorCode.EMPTY_SERIES);
            assertThat(e.errorCode()).isEqualTo(TimeSeriesErrorCode.EMPTY_SERIES);
        }

        @Test
        @DisplayName("should support QUERY_RANGE_TOO_LARGE")
        void shouldSupportQueryRangeTooLarge() {
            TimeSeriesException e = new TimeSeriesException(TimeSeriesErrorCode.QUERY_RANGE_TOO_LARGE);
            assertThat(e.errorCode()).isEqualTo(TimeSeriesErrorCode.QUERY_RANGE_TOO_LARGE);
        }

        @Test
        @DisplayName("should support INVALID_TIME_RANGE")
        void shouldSupportInvalidTimeRange() {
            TimeSeriesException e = new TimeSeriesException(TimeSeriesErrorCode.INVALID_TIME_RANGE);
            assertThat(e.errorCode()).isEqualTo(TimeSeriesErrorCode.INVALID_TIME_RANGE);
        }

        @Test
        @DisplayName("should support SERIES_NOT_FOUND")
        void shouldSupportSeriesNotFound() {
            TimeSeriesException e = new TimeSeriesException(TimeSeriesErrorCode.SERIES_NOT_FOUND);
            assertThat(e.errorCode()).isEqualTo(TimeSeriesErrorCode.SERIES_NOT_FOUND);
        }

        @Test
        @DisplayName("should support AGGREGATION_FAILED")
        void shouldSupportAggregationFailed() {
            TimeSeriesException e = new TimeSeriesException(TimeSeriesErrorCode.AGGREGATION_FAILED);
            assertThat(e.errorCode()).isEqualTo(TimeSeriesErrorCode.AGGREGATION_FAILED);
        }

        @Test
        @DisplayName("should support WINDOW_SIZE_INVALID")
        void shouldSupportWindowSizeInvalid() {
            TimeSeriesException e = new TimeSeriesException(TimeSeriesErrorCode.WINDOW_SIZE_INVALID);
            assertThat(e.errorCode()).isEqualTo(TimeSeriesErrorCode.WINDOW_SIZE_INVALID);
        }

        @Test
        @DisplayName("should support INSUFFICIENT_DATA")
        void shouldSupportInsufficientData() {
            TimeSeriesException e = new TimeSeriesException(TimeSeriesErrorCode.INSUFFICIENT_DATA);
            assertThat(e.errorCode()).isEqualTo(TimeSeriesErrorCode.INSUFFICIENT_DATA);
        }

        @Test
        @DisplayName("should support CAPACITY_EXCEEDED")
        void shouldSupportCapacityExceeded() {
            TimeSeriesException e = new TimeSeriesException(TimeSeriesErrorCode.CAPACITY_EXCEEDED);
            assertThat(e.errorCode()).isEqualTo(TimeSeriesErrorCode.CAPACITY_EXCEEDED);
        }

        @Test
        @DisplayName("should support MEMORY_LIMIT_EXCEEDED")
        void shouldSupportMemoryLimitExceeded() {
            TimeSeriesException e = new TimeSeriesException(TimeSeriesErrorCode.MEMORY_LIMIT_EXCEEDED);
            assertThat(e.errorCode()).isEqualTo(TimeSeriesErrorCode.MEMORY_LIMIT_EXCEEDED);
        }
    }
}
