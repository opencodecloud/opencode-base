package cloud.opencode.base.timeseries;

import org.junit.jupiter.api.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * BoundedTimeSeriesTest Tests
 * BoundedTimeSeriesTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
@DisplayName("BoundedTimeSeries Tests")
class BoundedTimeSeriesTest {

    private Instant baseTime;

    @BeforeEach
    void setUp() {
        baseTime = Instant.now();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("should create bounded series with max size")
        void shouldCreateBoundedSeriesWithMaxSize() {
            BoundedTimeSeries series = new BoundedTimeSeries("test", 100);

            assertThat(series.getName()).isEqualTo("test");
            assertThat(series.getMaxSize()).isEqualTo(100);
            assertThat(series.getMaxAge()).isEqualTo(Duration.ofDays(365));
        }

        @Test
        @DisplayName("should create bounded series with max size and max age")
        void shouldCreateBoundedSeriesWithMaxSizeAndMaxAge() {
            BoundedTimeSeries series = new BoundedTimeSeries("test", 100, Duration.ofHours(1));

            assertThat(series.getMaxSize()).isEqualTo(100);
            assertThat(series.getMaxAge()).isEqualTo(Duration.ofHours(1));
        }

        @Test
        @DisplayName("should throw for non-positive max size")
        void shouldThrowForNonPositiveMaxSize() {
            assertThatThrownBy(() -> new BoundedTimeSeries("test", 0))
                .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> new BoundedTimeSeries("test", -1))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("of should create bounded series with max size")
        void ofShouldCreateBoundedSeriesWithMaxSize() {
            BoundedTimeSeries series = BoundedTimeSeries.of("test", 50);

            assertThat(series.getMaxSize()).isEqualTo(50);
        }

        @Test
        @DisplayName("of should create bounded series with max size and max age")
        void ofShouldCreateBoundedSeriesWithMaxSizeAndMaxAge() {
            BoundedTimeSeries series = BoundedTimeSeries.of("test", 50, Duration.ofMinutes(30));

            assertThat(series.getMaxSize()).isEqualTo(50);
            assertThat(series.getMaxAge()).isEqualTo(Duration.ofMinutes(30));
        }
    }

    @Nested
    @DisplayName("Size Eviction Tests")
    class SizeEvictionTests {

        @Test
        @DisplayName("add should evict oldest when max size exceeded")
        void addShouldEvictOldestWhenMaxSizeExceeded() {
            BoundedTimeSeries series = new BoundedTimeSeries("test", 3);

            series.add(baseTime, 1.0);
            series.add(baseTime.plusSeconds(1), 2.0);
            series.add(baseTime.plusSeconds(2), 3.0);
            series.add(baseTime.plusSeconds(3), 4.0);

            assertThat(series.size()).isEqualTo(3);
            assertThat(series.getFirst().map(DataPoint::value)).contains(2.0);
        }

        @Test
        @DisplayName("addAll should evict oldest when max size exceeded")
        void addAllShouldEvictOldestWhenMaxSizeExceeded() {
            BoundedTimeSeries series = new BoundedTimeSeries("test", 3);

            List<DataPoint> points = List.of(
                DataPoint.of(baseTime, 1.0),
                DataPoint.of(baseTime.plusSeconds(1), 2.0),
                DataPoint.of(baseTime.plusSeconds(2), 3.0),
                DataPoint.of(baseTime.plusSeconds(3), 4.0),
                DataPoint.of(baseTime.plusSeconds(4), 5.0)
            );
            series.addAll(points);

            assertThat(series.size()).isEqualTo(3);
            assertThat(series.getFirst().map(DataPoint::value)).contains(3.0);
        }
    }

    @Nested
    @DisplayName("Capacity Tests")
    class CapacityTests {

        @Test
        @DisplayName("remainingCapacity should return correct value")
        void remainingCapacityShouldReturnCorrectValue() {
            BoundedTimeSeries series = new BoundedTimeSeries("test", 10);

            series.add(baseTime, 1.0);
            series.add(baseTime.plusSeconds(1), 2.0);
            series.add(baseTime.plusSeconds(2), 3.0);

            assertThat(series.remainingCapacity()).isEqualTo(7);
        }

        @Test
        @DisplayName("isFull should return true when at max size")
        void isFullShouldReturnTrueWhenAtMaxSize() {
            BoundedTimeSeries series = new BoundedTimeSeries("test", 3);

            series.add(baseTime, 1.0);
            series.add(baseTime.plusSeconds(1), 2.0);
            series.add(baseTime.plusSeconds(2), 3.0);

            assertThat(series.isFull()).isTrue();
        }

        @Test
        @DisplayName("isFull should return false when not at max size")
        void isFullShouldReturnFalseWhenNotAtMaxSize() {
            BoundedTimeSeries series = new BoundedTimeSeries("test", 10);

            series.add(baseTime, 1.0);

            assertThat(series.isFull()).isFalse();
        }
    }

    @Nested
    @DisplayName("Getter Tests")
    class GetterTests {

        @Test
        @DisplayName("getMaxSize should return configured max size")
        void getMaxSizeShouldReturnConfiguredMaxSize() {
            BoundedTimeSeries series = new BoundedTimeSeries("test", 50);

            assertThat(series.getMaxSize()).isEqualTo(50);
        }

        @Test
        @DisplayName("getMaxAge should return configured max age")
        void getMaxAgeShouldReturnConfiguredMaxAge() {
            BoundedTimeSeries series = new BoundedTimeSeries("test", 100, Duration.ofMinutes(30));

            assertThat(series.getMaxAge()).isEqualTo(Duration.ofMinutes(30));
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("toString should contain relevant information")
        void toStringShouldContainRelevantInformation() {
            BoundedTimeSeries series = new BoundedTimeSeries("test", 100, Duration.ofHours(1));
            series.add(baseTime, 1.0);

            String str = series.toString();

            assertThat(str).contains("test");
            assertThat(str).contains("1");
            assertThat(str).contains("100");
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle max size of 1")
        void shouldHandleMaxSizeOf1() {
            BoundedTimeSeries series = new BoundedTimeSeries("test", 1);

            series.add(baseTime, 1.0);
            series.add(baseTime.plusSeconds(1), 2.0);

            assertThat(series.size()).isEqualTo(1);
            assertThat(series.getFirst().map(DataPoint::value)).contains(2.0);
        }

        @Test
        @DisplayName("should handle very large max size")
        void shouldHandleVeryLargeMaxSize() {
            BoundedTimeSeries series = new BoundedTimeSeries("test", Integer.MAX_VALUE);

            series.add(baseTime, 1.0);

            assertThat(series.size()).isEqualTo(1);
            assertThat(series.isFull()).isFalse();
        }
    }
}
