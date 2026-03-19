package cloud.opencode.base.timeseries;

import org.junit.jupiter.api.*;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * DataPointTest Tests
 * DataPointTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.0
 */
@DisplayName("DataPoint Tests")
class DataPointTest {

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("of should create data point with timestamp and value")
        void ofShouldCreateDataPointWithTimestampAndValue() {
            Instant timestamp = Instant.now();
            DataPoint point = DataPoint.of(timestamp, 42.0);

            assertThat(point.timestamp()).isEqualTo(timestamp);
            assertThat(point.value()).isEqualTo(42.0);
            assertThat(point.tags()).isEmpty();
        }

        @Test
        @DisplayName("of should create data point with timestamp, value, and tags")
        void ofShouldCreateDataPointWithTimestampValueAndTags() {
            Instant timestamp = Instant.now();
            Map<String, String> tags = Map.of("sensor", "temp1", "location", "room1");
            DataPoint point = DataPoint.of(timestamp, 25.5, tags);

            assertThat(point.timestamp()).isEqualTo(timestamp);
            assertThat(point.value()).isEqualTo(25.5);
            assertThat(point.tags()).containsAllEntriesOf(tags);
        }

        @Test
        @DisplayName("of should create data point from epoch millis")
        void ofShouldCreateDataPointFromEpochMillis() {
            long epochMillis = 1234567890123L;
            DataPoint point = DataPoint.of(epochMillis, 42.0);

            assertThat(point.timestamp()).isEqualTo(Instant.ofEpochMilli(epochMillis));
            assertThat(point.value()).isEqualTo(42.0);
        }

        @Test
        @DisplayName("now should create data point with current timestamp")
        void nowShouldCreateDataPointWithCurrentTimestamp() {
            Instant before = Instant.now();
            DataPoint point = DataPoint.now(100.0);
            Instant after = Instant.now();

            assertThat(point.timestamp()).isBetween(before, after);
            assertThat(point.value()).isEqualTo(100.0);
        }
    }

    @Nested
    @DisplayName("Utility Method Tests")
    class UtilityMethodTests {

        @Test
        @DisplayName("epochMillis should return timestamp in milliseconds")
        void epochMillisShouldReturnTimestampInMilliseconds() {
            Instant timestamp = Instant.ofEpochMilli(1234567890123L);
            DataPoint point = DataPoint.of(timestamp, 1.0);

            assertThat(point.epochMillis()).isEqualTo(1234567890123L);
        }

        @Test
        @DisplayName("isNaN should return true for NaN value")
        void isNaNShouldReturnTrueForNaNValue() {
            DataPoint point = DataPoint.of(Instant.now(), Double.NaN);

            assertThat(point.isNaN()).isTrue();
        }

        @Test
        @DisplayName("isNaN should return false for normal value")
        void isNaNShouldReturnFalseForNormalValue() {
            DataPoint point = DataPoint.of(Instant.now(), 42.0);

            assertThat(point.isNaN()).isFalse();
        }

        @Test
        @DisplayName("getTag should return tag value if exists")
        void getTagShouldReturnTagValueIfExists() {
            Map<String, String> tags = Map.of("sensor", "temp1");
            DataPoint point = DataPoint.of(Instant.now(), 1.0, tags);

            assertThat(point.getTag("sensor")).isEqualTo("temp1");
        }

        @Test
        @DisplayName("getTag should return null if tag not exists")
        void getTagShouldReturnNullIfTagNotExists() {
            DataPoint point = DataPoint.of(Instant.now(), 1.0);

            assertThat(point.getTag("nonexistent")).isNull();
        }

        @Test
        @DisplayName("withTag should add new tag")
        void withTagShouldAddNewTag() {
            DataPoint original = DataPoint.of(Instant.now(), 1.0);
            DataPoint withTag = original.withTag("key", "value");

            assertThat(withTag.getTag("key")).isEqualTo("value");
            assertThat(original.getTag("key")).isNull();
        }

        @Test
        @DisplayName("withTag should preserve existing data")
        void withTagShouldPreserveExistingData() {
            Instant timestamp = Instant.now();
            DataPoint original = DataPoint.of(timestamp, 42.0, Map.of("existing", "tag"));
            DataPoint withTag = original.withTag("new", "value");

            assertThat(withTag.timestamp()).isEqualTo(timestamp);
            assertThat(withTag.value()).isEqualTo(42.0);
            assertThat(withTag.getTag("existing")).isEqualTo("tag");
            assertThat(withTag.getTag("new")).isEqualTo("value");
        }
    }

    @Nested
    @DisplayName("Comparable Tests")
    class ComparableTests {

        @Test
        @DisplayName("compareTo should order by timestamp")
        void compareToShouldOrderByTimestamp() {
            Instant t1 = Instant.ofEpochMilli(1000);
            Instant t2 = Instant.ofEpochMilli(2000);
            DataPoint p1 = DataPoint.of(t1, 1.0);
            DataPoint p2 = DataPoint.of(t2, 1.0);

            assertThat(p1.compareTo(p2)).isLessThan(0);
            assertThat(p2.compareTo(p1)).isGreaterThan(0);
        }

        @Test
        @DisplayName("compareTo should return zero for same timestamp")
        void compareToShouldReturnZeroForSameTimestamp() {
            Instant timestamp = Instant.now();
            DataPoint p1 = DataPoint.of(timestamp, 1.0);
            DataPoint p2 = DataPoint.of(timestamp, 2.0);

            assertThat(p1.compareTo(p2)).isZero();
        }
    }

    @Nested
    @DisplayName("Record Method Tests")
    class RecordMethodTests {

        @Test
        @DisplayName("equals should compare by all fields")
        void equalsShouldCompareByAllFields() {
            Instant timestamp = Instant.now();
            Map<String, String> tags = Map.of("key", "value");
            DataPoint p1 = DataPoint.of(timestamp, 42.0, tags);
            DataPoint p2 = DataPoint.of(timestamp, 42.0, tags);
            DataPoint p3 = DataPoint.of(timestamp, 43.0, tags);

            assertThat(p1).isEqualTo(p2);
            assertThat(p1).isNotEqualTo(p3);
        }

        @Test
        @DisplayName("hashCode should be consistent with equals")
        void hashCodeShouldBeConsistentWithEquals() {
            Instant timestamp = Instant.now();
            DataPoint p1 = DataPoint.of(timestamp, 42.0);
            DataPoint p2 = DataPoint.of(timestamp, 42.0);

            assertThat(p1.hashCode()).isEqualTo(p2.hashCode());
        }

        @Test
        @DisplayName("toString should contain all fields")
        void toStringShouldContainAllFields() {
            DataPoint point = DataPoint.of(Instant.ofEpochMilli(1000), 42.0, Map.of("key", "value"));
            String str = point.toString();

            assertThat(str).contains("42.0");
            assertThat(str).contains("key");
            assertThat(str).contains("value");
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle empty tags map")
        void shouldHandleEmptyTagsMap() {
            DataPoint point = DataPoint.of(Instant.now(), 1.0, Map.of());

            assertThat(point.tags()).isEmpty();
        }

        @Test
        @DisplayName("should handle zero value")
        void shouldHandleZeroValue() {
            DataPoint point = DataPoint.of(Instant.now(), 0.0);

            assertThat(point.value()).isZero();
            assertThat(point.isNaN()).isFalse();
        }

        @Test
        @DisplayName("should handle negative value")
        void shouldHandleNegativeValue() {
            DataPoint point = DataPoint.of(Instant.now(), -42.0);

            assertThat(point.value()).isEqualTo(-42.0);
        }

        @Test
        @DisplayName("should handle infinity values")
        void shouldHandleInfinityValues() {
            DataPoint posInf = DataPoint.of(Instant.now(), Double.POSITIVE_INFINITY);
            DataPoint negInf = DataPoint.of(Instant.now(), Double.NEGATIVE_INFINITY);

            assertThat(posInf.value()).isEqualTo(Double.POSITIVE_INFINITY);
            assertThat(negInf.value()).isEqualTo(Double.NEGATIVE_INFINITY);
            assertThat(posInf.isNaN()).isFalse();
            assertThat(negInf.isNaN()).isFalse();
        }

        @Test
        @DisplayName("should reject null timestamp")
        void shouldRejectNullTimestamp() {
            assertThatThrownBy(() -> DataPoint.of(null, 1.0))
                .isInstanceOf(NullPointerException.class);
        }
    }
}
