package cloud.opencode.base.timeseries.query;

import cloud.opencode.base.timeseries.DataPoint;
import cloud.opencode.base.timeseries.exception.TimeSeriesException;
import org.junit.jupiter.api.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * QueryLimiterTest Tests
 * QueryLimiterTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
@DisplayName("QueryLimiter Tests")
class QueryLimiterTest {

    @BeforeEach
    void setUp() {
        // Reset to defaults before each test
        QueryLimiter.resetDefaults();
    }

    @AfterEach
    void tearDown() {
        QueryLimiter.resetDefaults();
    }

    @Nested
    @DisplayName("Default Configuration Tests")
    class DefaultConfigurationTests {

        @Test
        @DisplayName("should have default max range days")
        void shouldHaveDefaultMaxRangeDays() {
            int maxDays = QueryLimiter.getMaxRangeDays();

            assertThat(maxDays).isGreaterThan(0);
        }

        @Test
        @DisplayName("should have default max result size")
        void shouldHaveDefaultMaxResultSize() {
            int maxSize = QueryLimiter.getMaxResultSize();

            assertThat(maxSize).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("Configuration Tests")
    class ConfigurationTests {

        @Test
        @DisplayName("setMaxRangeDays should update limit")
        void setMaxRangeDaysShouldUpdateLimit() {
            QueryLimiter.setMaxRangeDays(30);

            assertThat(QueryLimiter.getMaxRangeDays()).isEqualTo(30);
        }

        @Test
        @DisplayName("setMaxResultSize should update limit")
        void setMaxResultSizeShouldUpdateLimit() {
            QueryLimiter.setMaxResultSize(5000);

            assertThat(QueryLimiter.getMaxResultSize()).isEqualTo(5000);
        }

        @Test
        @DisplayName("resetDefaults should restore original values")
        void resetDefaultsShouldRestoreOriginalValues() {
            int originalMaxDays = QueryLimiter.getMaxRangeDays();
            int originalMaxSize = QueryLimiter.getMaxResultSize();

            QueryLimiter.setMaxRangeDays(1);
            QueryLimiter.setMaxResultSize(100);
            QueryLimiter.resetDefaults();

            assertThat(QueryLimiter.getMaxRangeDays()).isEqualTo(originalMaxDays);
            assertThat(QueryLimiter.getMaxResultSize()).isEqualTo(originalMaxSize);
        }

        @Test
        @DisplayName("setMaxRangeDays should throw for non-positive value")
        void setMaxRangeDaysShouldThrowForNonPositiveValue() {
            assertThatThrownBy(() -> QueryLimiter.setMaxRangeDays(0))
                .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> QueryLimiter.setMaxRangeDays(-1))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("setMaxResultSize should throw for non-positive value")
        void setMaxResultSizeShouldThrowForNonPositiveValue() {
            assertThatThrownBy(() -> QueryLimiter.setMaxResultSize(0))
                .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> QueryLimiter.setMaxResultSize(-1))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Validate Range Tests")
    class ValidateRangeTests {

        @Test
        @DisplayName("validateRange should pass for valid range")
        void validateRangeShouldPassForValidRange() {
            QueryLimiter.setMaxRangeDays(30);
            TimeRange validRange = TimeRange.last(java.time.Duration.ofDays(10));

            assertThatNoException().isThrownBy(() -> QueryLimiter.validateRange(validRange));
        }

        @Test
        @DisplayName("validateRange should throw for exceeding range")
        void validateRangeShouldThrowForExceedingRange() {
            QueryLimiter.setMaxRangeDays(7);
            TimeRange invalidRange = TimeRange.last(java.time.Duration.ofDays(30));

            assertThatThrownBy(() -> QueryLimiter.validateRange(invalidRange))
                .isInstanceOf(TimeSeriesException.class);
        }

        @Test
        @DisplayName("validateRange with Instant params should work")
        void validateRangeWithInstantParamsShouldWork() {
            QueryLimiter.setMaxRangeDays(30);
            Instant from = Instant.now().minusSeconds(86400 * 10);
            Instant to = Instant.now();

            assertThatNoException().isThrownBy(() -> QueryLimiter.validateRange(from, to));
        }

        @Test
        @DisplayName("validateRange should throw for negative range")
        void validateRangeShouldThrowForNegativeRange() {
            Instant from = Instant.now();
            Instant to = Instant.now().minusSeconds(86400);

            assertThatThrownBy(() -> QueryLimiter.validateRange(from, to))
                .isInstanceOf(TimeSeriesException.class);
        }
    }

    @Nested
    @DisplayName("Limit Result Tests")
    class LimitResultTests {

        @Test
        @DisplayName("limitResult should truncate exceeding list")
        void limitResultShouldTruncateExceedingList() {
            QueryLimiter.setMaxResultSize(10);
            List<DataPoint> points = createPoints(100);

            List<DataPoint> limited = QueryLimiter.limitResult(points);

            assertThat(limited).hasSize(10);
        }

        @Test
        @DisplayName("limitResult should not modify list within limit")
        void limitResultShouldNotModifyListWithinLimit() {
            QueryLimiter.setMaxResultSize(100);
            List<DataPoint> points = createPoints(50);

            List<DataPoint> limited = QueryLimiter.limitResult(points);

            assertThat(limited).hasSize(50);
        }

        @Test
        @DisplayName("limitResult should handle empty list")
        void limitResultShouldHandleEmptyList() {
            List<DataPoint> empty = List.of();

            List<DataPoint> limited = QueryLimiter.limitResult(empty);

            assertThat(limited).isEmpty();
        }
    }

    @Nested
    @DisplayName("Exceeds Limit Tests")
    class ExceedsLimitTests {

        @Test
        @DisplayName("exceedsLimit should return true for exceeding size")
        void exceedsLimitShouldReturnTrueForExceedingSize() {
            QueryLimiter.setMaxResultSize(10);

            assertThat(QueryLimiter.exceedsLimit(100)).isTrue();
        }

        @Test
        @DisplayName("exceedsLimit should return false for size within limit")
        void exceedsLimitShouldReturnFalseForSizeWithinLimit() {
            QueryLimiter.setMaxResultSize(100);

            assertThat(QueryLimiter.exceedsLimit(50)).isFalse();
        }

        @Test
        @DisplayName("exceedsLimit should return false for size at limit")
        void exceedsLimitShouldReturnFalseForSizeAtLimit() {
            QueryLimiter.setMaxResultSize(50);

            assertThat(QueryLimiter.exceedsLimit(50)).isFalse();
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle very large max result size")
        void shouldHandleVeryLargeMaxResultSize() {
            QueryLimiter.setMaxResultSize(Integer.MAX_VALUE);
            List<DataPoint> points = createPoints(100);

            List<DataPoint> limited = QueryLimiter.limitResult(points);

            assertThat(limited).hasSize(100);
        }
    }

    private List<DataPoint> createPoints(int count) {
        List<DataPoint> points = new ArrayList<>();
        Instant base = Instant.now();
        for (int i = 0; i < count; i++) {
            points.add(DataPoint.of(base.plusSeconds(i), i * 1.0));
        }
        return points;
    }
}
