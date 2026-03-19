package cloud.opencode.base.timeseries.validation;

import cloud.opencode.base.timeseries.DataPoint;
import cloud.opencode.base.timeseries.exception.TimeSeriesErrorCode;
import cloud.opencode.base.timeseries.exception.TimeSeriesException;
import org.junit.jupiter.api.*;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

/**
 * DataPointValidatorTest Tests
 * DataPointValidatorTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
@DisplayName("DataPointValidator Tests")
class DataPointValidatorTest {

    @Nested
    @DisplayName("Validate Tests")
    class ValidateTests {

        @Test
        @DisplayName("validate should pass for valid data point")
        void validateShouldPassForValidDataPoint() {
            DataPoint point = DataPoint.of(Instant.parse("2024-01-01T00:00:00Z"), 42.0);

            assertThatCode(() -> DataPointValidator.validate(point))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("validate should throw for null data point")
        void validateShouldThrowForNullDataPoint() {
            assertThatThrownBy(() -> DataPointValidator.validate(null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("validate should throw for invalid timestamp")
        void validateShouldThrowForInvalidTimestamp() {
            DataPoint point = DataPoint.of(Instant.parse("1900-01-01T00:00:00Z"), 42.0);

            assertThatThrownBy(() -> DataPointValidator.validate(point))
                .isInstanceOf(TimeSeriesException.class)
                .extracting(e -> ((TimeSeriesException) e).errorCode())
                .isEqualTo(TimeSeriesErrorCode.INVALID_TIMESTAMP);
        }

        @Test
        @DisplayName("validate should throw for NaN value")
        void validateShouldThrowForNaNValue() {
            DataPoint point = DataPoint.of(Instant.parse("2024-01-01T00:00:00Z"), Double.NaN);

            assertThatThrownBy(() -> DataPointValidator.validate(point))
                .isInstanceOf(TimeSeriesException.class)
                .extracting(e -> ((TimeSeriesException) e).errorCode())
                .isEqualTo(TimeSeriesErrorCode.INVALID_VALUE);
        }

        @Test
        @DisplayName("validate should throw for infinite value")
        void validateShouldThrowForInfiniteValue() {
            DataPoint point = DataPoint.of(Instant.parse("2024-01-01T00:00:00Z"), Double.POSITIVE_INFINITY);

            assertThatThrownBy(() -> DataPointValidator.validate(point))
                .isInstanceOf(TimeSeriesException.class)
                .extracting(e -> ((TimeSeriesException) e).errorCode())
                .isEqualTo(TimeSeriesErrorCode.INVALID_VALUE);
        }
    }

    @Nested
    @DisplayName("ValidateTimestamp Tests")
    class ValidateTimestampTests {

        @Test
        @DisplayName("validateTimestamp should pass for valid timestamp")
        void validateTimestampShouldPassForValidTimestamp() {
            Instant valid = Instant.parse("2024-01-01T00:00:00Z");

            assertThatCode(() -> DataPointValidator.validateTimestamp(valid))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("validateTimestamp should throw for null timestamp")
        void validateTimestampShouldThrowForNullTimestamp() {
            assertThatThrownBy(() -> DataPointValidator.validateTimestamp(null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("validateTimestamp should throw for timestamp before min")
        void validateTimestampShouldThrowForTimestampBeforeMin() {
            Instant tooOld = Instant.parse("1999-01-01T00:00:00Z");

            assertThatThrownBy(() -> DataPointValidator.validateTimestamp(tooOld))
                .isInstanceOf(TimeSeriesException.class)
                .extracting(e -> ((TimeSeriesException) e).errorCode())
                .isEqualTo(TimeSeriesErrorCode.INVALID_TIMESTAMP);
        }

        @Test
        @DisplayName("validateTimestamp should throw for timestamp after max")
        void validateTimestampShouldThrowForTimestampAfterMax() {
            Instant tooFuture = Instant.parse("2101-01-01T00:00:00Z");

            assertThatThrownBy(() -> DataPointValidator.validateTimestamp(tooFuture))
                .isInstanceOf(TimeSeriesException.class)
                .extracting(e -> ((TimeSeriesException) e).errorCode())
                .isEqualTo(TimeSeriesErrorCode.INVALID_TIMESTAMP);
        }
    }

    @Nested
    @DisplayName("ValidateValue Tests")
    class ValidateValueTests {

        @Test
        @DisplayName("validateValue should pass for valid value")
        void validateValueShouldPassForValidValue() {
            assertThatCode(() -> DataPointValidator.validateValue(42.0))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("validateValue should pass for zero")
        void validateValueShouldPassForZero() {
            assertThatCode(() -> DataPointValidator.validateValue(0.0))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("validateValue should pass for negative value")
        void validateValueShouldPassForNegativeValue() {
            assertThatCode(() -> DataPointValidator.validateValue(-42.0))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("validateValue should throw for NaN")
        void validateValueShouldThrowForNaN() {
            assertThatThrownBy(() -> DataPointValidator.validateValue(Double.NaN))
                .isInstanceOf(TimeSeriesException.class)
                .extracting(e -> ((TimeSeriesException) e).errorCode())
                .isEqualTo(TimeSeriesErrorCode.INVALID_VALUE);
        }

        @Test
        @DisplayName("validateValue should throw for positive infinity")
        void validateValueShouldThrowForPositiveInfinity() {
            assertThatThrownBy(() -> DataPointValidator.validateValue(Double.POSITIVE_INFINITY))
                .isInstanceOf(TimeSeriesException.class)
                .extracting(e -> ((TimeSeriesException) e).errorCode())
                .isEqualTo(TimeSeriesErrorCode.INVALID_VALUE);
        }

        @Test
        @DisplayName("validateValue should throw for negative infinity")
        void validateValueShouldThrowForNegativeInfinity() {
            assertThatThrownBy(() -> DataPointValidator.validateValue(Double.NEGATIVE_INFINITY))
                .isInstanceOf(TimeSeriesException.class)
                .extracting(e -> ((TimeSeriesException) e).errorCode())
                .isEqualTo(TimeSeriesErrorCode.INVALID_VALUE);
        }
    }

    @Nested
    @DisplayName("IsValidValue Tests")
    class IsValidValueTests {

        @Test
        @DisplayName("isValidValue should return true for valid value")
        void isValidValueShouldReturnTrueForValidValue() {
            assertThat(DataPointValidator.isValidValue(42.0)).isTrue();
        }

        @Test
        @DisplayName("isValidValue should return false for NaN")
        void isValidValueShouldReturnFalseForNaN() {
            assertThat(DataPointValidator.isValidValue(Double.NaN)).isFalse();
        }

        @Test
        @DisplayName("isValidValue should return false for infinity")
        void isValidValueShouldReturnFalseForInfinity() {
            assertThat(DataPointValidator.isValidValue(Double.POSITIVE_INFINITY)).isFalse();
            assertThat(DataPointValidator.isValidValue(Double.NEGATIVE_INFINITY)).isFalse();
        }
    }

    @Nested
    @DisplayName("IsValidTimestamp Tests")
    class IsValidTimestampTests {

        @Test
        @DisplayName("isValidTimestamp should return true for valid timestamp")
        void isValidTimestampShouldReturnTrueForValidTimestamp() {
            assertThat(DataPointValidator.isValidTimestamp(Instant.parse("2024-01-01T00:00:00Z"))).isTrue();
        }

        @Test
        @DisplayName("isValidTimestamp should return false for null")
        void isValidTimestampShouldReturnFalseForNull() {
            assertThat(DataPointValidator.isValidTimestamp(null)).isFalse();
        }

        @Test
        @DisplayName("isValidTimestamp should return false for timestamp out of range")
        void isValidTimestampShouldReturnFalseForTimestampOutOfRange() {
            assertThat(DataPointValidator.isValidTimestamp(Instant.parse("1990-01-01T00:00:00Z"))).isFalse();
            assertThat(DataPointValidator.isValidTimestamp(Instant.parse("2200-01-01T00:00:00Z"))).isFalse();
        }
    }

    @Nested
    @DisplayName("IsValid Tests")
    class IsValidTests {

        @Test
        @DisplayName("isValid should return true for valid data point")
        void isValidShouldReturnTrueForValidDataPoint() {
            DataPoint point = DataPoint.of(Instant.parse("2024-01-01T00:00:00Z"), 42.0);

            assertThat(DataPointValidator.isValid(point)).isTrue();
        }

        @Test
        @DisplayName("isValid should return false for null data point")
        void isValidShouldReturnFalseForNullDataPoint() {
            assertThat(DataPointValidator.isValid(null)).isFalse();
        }

        @Test
        @DisplayName("isValid should return false for invalid timestamp")
        void isValidShouldReturnFalseForInvalidTimestamp() {
            DataPoint point = DataPoint.of(Instant.parse("1990-01-01T00:00:00Z"), 42.0);

            assertThat(DataPointValidator.isValid(point)).isFalse();
        }

        @Test
        @DisplayName("isValid should return false for invalid value")
        void isValidShouldReturnFalseForInvalidValue() {
            DataPoint point = DataPoint.of(Instant.parse("2024-01-01T00:00:00Z"), Double.NaN);

            assertThat(DataPointValidator.isValid(point)).isFalse();
        }
    }

    @Nested
    @DisplayName("Boundary Tests")
    class BoundaryTests {

        @Test
        @DisplayName("getMinTimestamp should return minimum valid timestamp")
        void getMinTimestampShouldReturnMinimumValidTimestamp() {
            Instant minTimestamp = DataPointValidator.getMinTimestamp();

            assertThat(minTimestamp).isEqualTo(Instant.parse("2000-01-01T00:00:00Z"));
        }

        @Test
        @DisplayName("getMaxTimestamp should return maximum valid timestamp")
        void getMaxTimestampShouldReturnMaximumValidTimestamp() {
            Instant maxTimestamp = DataPointValidator.getMaxTimestamp();

            assertThat(maxTimestamp).isEqualTo(Instant.parse("2100-01-01T00:00:00Z"));
        }

        @Test
        @DisplayName("timestamp at min boundary should be valid")
        void timestampAtMinBoundaryShouldBeValid() {
            Instant minTimestamp = DataPointValidator.getMinTimestamp();

            assertThat(DataPointValidator.isValidTimestamp(minTimestamp)).isTrue();
        }

        @Test
        @DisplayName("timestamp at max boundary should be valid")
        void timestampAtMaxBoundaryShouldBeValid() {
            Instant maxTimestamp = DataPointValidator.getMaxTimestamp();

            assertThat(DataPointValidator.isValidTimestamp(maxTimestamp)).isTrue();
        }

        @Test
        @DisplayName("timestamp just before min should be invalid")
        void timestampJustBeforeMinShouldBeInvalid() {
            Instant justBeforeMin = DataPointValidator.getMinTimestamp().minusMillis(1);

            assertThat(DataPointValidator.isValidTimestamp(justBeforeMin)).isFalse();
        }

        @Test
        @DisplayName("timestamp just after max should be invalid")
        void timestampJustAfterMaxShouldBeInvalid() {
            Instant justAfterMax = DataPointValidator.getMaxTimestamp().plusMillis(1);

            assertThat(DataPointValidator.isValidTimestamp(justAfterMax)).isFalse();
        }
    }
}
