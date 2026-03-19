package cloud.opencode.base.timeseries.aggregation;

import org.junit.jupiter.api.*;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

/**
 * AggregationResultTest Tests
 * AggregationResultTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
@DisplayName("AggregationResult Tests")
class AggregationResultTest {

    private final Instant baseTime = Instant.parse("2024-01-01T00:00:00Z");

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("success should create Success instance")
        void successShouldCreateSuccessInstance() {
            AggregationResult result = AggregationResult.success(42.0, 10, baseTime, baseTime.plusSeconds(100));

            assertThat(result).isInstanceOf(AggregationResult.Success.class);
            assertThat(result.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("empty should create Empty instance")
        void emptyShouldCreateEmptyInstance() {
            AggregationResult result = AggregationResult.empty("test-series");

            assertThat(result).isInstanceOf(AggregationResult.Empty.class);
            assertThat(result.isSuccess()).isFalse();
        }

        @Test
        @DisplayName("error should create Error instance")
        void errorShouldCreateErrorInstance() {
            AggregationResult result = AggregationResult.error("Test error message");

            assertThat(result).isInstanceOf(AggregationResult.Error.class);
            assertThat(result.isSuccess()).isFalse();
        }
    }

    @Nested
    @DisplayName("Success Tests")
    class SuccessTests {

        @Test
        @DisplayName("isSuccess should return true")
        void isSuccessShouldReturnTrue() {
            AggregationResult.Success success = new AggregationResult.Success(100.0, 5, baseTime, baseTime.plusSeconds(50));

            assertThat(success.isSuccess()).isTrue();
        }

        @Test
        @DisplayName("value should return the value")
        void valueShouldReturnTheValue() {
            AggregationResult.Success success = new AggregationResult.Success(42.5, 10, baseTime, baseTime.plusSeconds(100));

            assertThat(success.value()).isEqualTo(42.5);
        }

        @Test
        @DisplayName("count should return the count")
        void countShouldReturnTheCount() {
            AggregationResult.Success success = new AggregationResult.Success(42.5, 10, baseTime, baseTime.plusSeconds(100));

            assertThat(success.count()).isEqualTo(10);
        }

        @Test
        @DisplayName("from and to should return time range")
        void fromAndToShouldReturnTimeRange() {
            Instant from = baseTime;
            Instant to = baseTime.plusSeconds(100);
            AggregationResult.Success success = new AggregationResult.Success(42.5, 10, from, to);

            assertThat(success.from()).isEqualTo(from);
            assertThat(success.to()).isEqualTo(to);
        }

        @Test
        @DisplayName("getValueOrDefault should return actual value")
        void getValueOrDefaultShouldReturnActualValue() {
            AggregationResult.Success success = new AggregationResult.Success(42.0, 5, baseTime, baseTime.plusSeconds(50));

            assertThat(success.getValueOrDefault(0.0)).isEqualTo(42.0);
        }

        @Test
        @DisplayName("equals should compare all fields")
        void equalsShouldCompareAllFields() {
            AggregationResult.Success s1 = new AggregationResult.Success(42.0, 5, baseTime, baseTime.plusSeconds(50));
            AggregationResult.Success s2 = new AggregationResult.Success(42.0, 5, baseTime, baseTime.plusSeconds(50));
            AggregationResult.Success s3 = new AggregationResult.Success(43.0, 5, baseTime, baseTime.plusSeconds(50));

            assertThat(s1).isEqualTo(s2);
            assertThat(s1).isNotEqualTo(s3);
        }

        @Test
        @DisplayName("hashCode should be consistent with equals")
        void hashCodeShouldBeConsistentWithEquals() {
            AggregationResult.Success s1 = new AggregationResult.Success(42.0, 5, baseTime, baseTime.plusSeconds(50));
            AggregationResult.Success s2 = new AggregationResult.Success(42.0, 5, baseTime, baseTime.plusSeconds(50));

            assertThat(s1.hashCode()).isEqualTo(s2.hashCode());
        }
    }

    @Nested
    @DisplayName("Empty Tests")
    class EmptyTests {

        @Test
        @DisplayName("isSuccess should return false")
        void isSuccessShouldReturnFalse() {
            AggregationResult.Empty empty = new AggregationResult.Empty("test-series");

            assertThat(empty.isSuccess()).isFalse();
        }

        @Test
        @DisplayName("seriesName should return the series name")
        void seriesNameShouldReturnTheSeriesName() {
            AggregationResult.Empty empty = new AggregationResult.Empty("my-series");

            assertThat(empty.seriesName()).isEqualTo("my-series");
        }

        @Test
        @DisplayName("getValueOrDefault should return default value")
        void getValueOrDefaultShouldReturnDefaultValue() {
            AggregationResult.Empty empty = new AggregationResult.Empty("test");

            assertThat(empty.getValueOrDefault(99.9)).isEqualTo(99.9);
        }

        @Test
        @DisplayName("Empty instances with same name should be equal")
        void emptyInstancesWithSameNameShouldBeEqual() {
            AggregationResult.Empty e1 = new AggregationResult.Empty("test");
            AggregationResult.Empty e2 = new AggregationResult.Empty("test");

            assertThat(e1).isEqualTo(e2);
        }
    }

    @Nested
    @DisplayName("Error Tests")
    class ErrorTests {

        @Test
        @DisplayName("isSuccess should return false")
        void isSuccessShouldReturnFalse() {
            AggregationResult.Error error = new AggregationResult.Error("Error message");

            assertThat(error.isSuccess()).isFalse();
        }

        @Test
        @DisplayName("message should return error message")
        void messageShouldReturnErrorMessage() {
            AggregationResult.Error error = new AggregationResult.Error("Test error");

            assertThat(error.message()).isEqualTo("Test error");
        }

        @Test
        @DisplayName("getValueOrDefault should return default value")
        void getValueOrDefaultShouldReturnDefaultValue() {
            AggregationResult.Error error = new AggregationResult.Error("Error");

            assertThat(error.getValueOrDefault(77.7)).isEqualTo(77.7);
        }

        @Test
        @DisplayName("equals should compare messages")
        void equalsShouldCompareMessages() {
            AggregationResult.Error e1 = new AggregationResult.Error("Error A");
            AggregationResult.Error e2 = new AggregationResult.Error("Error A");
            AggregationResult.Error e3 = new AggregationResult.Error("Error B");

            assertThat(e1).isEqualTo(e2);
            assertThat(e1).isNotEqualTo(e3);
        }
    }

    @Nested
    @DisplayName("Sealed Interface Tests")
    class SealedInterfaceTests {

        @Test
        @DisplayName("should support pattern matching")
        void shouldSupportPatternMatching() {
            AggregationResult success = AggregationResult.success(42.0, 5, baseTime, baseTime.plusSeconds(50));
            AggregationResult empty = AggregationResult.empty("test");
            AggregationResult error = AggregationResult.error("Oops");

            assertThat(extractValue(success)).isEqualTo("42.0");
            assertThat(extractValue(empty)).isEqualTo("empty: test");
            assertThat(extractValue(error)).isEqualTo("error: Oops");
        }

        private String extractValue(AggregationResult result) {
            return switch (result) {
                case AggregationResult.Success s -> String.valueOf(s.value());
                case AggregationResult.Empty e -> "empty: " + e.seriesName();
                case AggregationResult.Error err -> "error: " + err.message();
            };
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Success should handle zero value")
        void successShouldHandleZeroValue() {
            AggregationResult.Success success = new AggregationResult.Success(0.0, 1, baseTime, baseTime.plusSeconds(1));

            assertThat(success.isSuccess()).isTrue();
            assertThat(success.value()).isZero();
            assertThat(success.getValueOrDefault(99.0)).isZero();
        }

        @Test
        @DisplayName("Success should handle negative value")
        void successShouldHandleNegativeValue() {
            AggregationResult.Success success = new AggregationResult.Success(-42.0, 1, baseTime, baseTime.plusSeconds(1));

            assertThat(success.value()).isEqualTo(-42.0);
        }

        @Test
        @DisplayName("Success should handle NaN value")
        void successShouldHandleNaNValue() {
            AggregationResult.Success success = new AggregationResult.Success(Double.NaN, 1, baseTime, baseTime.plusSeconds(1));

            assertThat(Double.isNaN(success.value())).isTrue();
        }

        @Test
        @DisplayName("Error should handle empty message")
        void errorShouldHandleEmptyMessage() {
            AggregationResult.Error error = new AggregationResult.Error("");

            assertThat(error.message()).isEmpty();
        }

        @Test
        @DisplayName("Error should handle null message")
        void errorShouldHandleNullMessage() {
            AggregationResult.Error error = new AggregationResult.Error(null);

            assertThat(error.message()).isNull();
        }
    }
}
