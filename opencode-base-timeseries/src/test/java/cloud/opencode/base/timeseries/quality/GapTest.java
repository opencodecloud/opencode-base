package cloud.opencode.base.timeseries.quality;

import org.junit.jupiter.api.*;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

/**
 * Gap Record Tests
 * Gap 记录测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-timeseries V1.0.3
 */
@DisplayName("Gap Tests")
class GapTest {

    @Nested
    @DisplayName("Construction Tests")
    class ConstructionTests {

        @Test
        @DisplayName("should store start and end correctly")
        void shouldStoreStartAndEndCorrectly() {
            Instant start = Instant.parse("2026-01-01T00:00:00Z");
            Instant end = Instant.parse("2026-01-01T01:00:00Z");

            Gap gap = new Gap(start, end);

            assertThat(gap.start()).isEqualTo(start);
            assertThat(gap.end()).isEqualTo(end);
        }

        @Test
        @DisplayName("should reject null start")
        void shouldRejectNullStart() {
            Instant end = Instant.now();
            assertThatNullPointerException()
                    .isThrownBy(() -> new Gap(null, end))
                    .withMessageContaining("start");
        }

        @Test
        @DisplayName("should reject null end")
        void shouldRejectNullEnd() {
            Instant start = Instant.now();
            assertThatNullPointerException()
                    .isThrownBy(() -> new Gap(start, null))
                    .withMessageContaining("end");
        }
    }

    @Nested
    @DisplayName("Length Tests")
    class LengthTests {

        @Test
        @DisplayName("length should return correct duration")
        void lengthShouldReturnCorrectDuration() {
            Instant start = Instant.parse("2026-01-01T00:00:00Z");
            Instant end = Instant.parse("2026-01-01T01:30:00Z");

            Gap gap = new Gap(start, end);

            assertThat(gap.length()).isEqualTo(Duration.ofMinutes(90));
        }

        @Test
        @DisplayName("length should return zero for same start and end")
        void lengthShouldReturnZeroForSameStartAndEnd() {
            Instant instant = Instant.parse("2026-06-15T12:00:00Z");

            Gap gap = new Gap(instant, instant);

            assertThat(gap.length()).isEqualTo(Duration.ZERO);
        }

        @Test
        @DisplayName("length should handle sub-second precision")
        void lengthShouldHandleSubSecondPrecision() {
            Instant start = Instant.parse("2026-01-01T00:00:00.000Z");
            Instant end = Instant.parse("2026-01-01T00:00:00.500Z");

            Gap gap = new Gap(start, end);

            assertThat(gap.length()).isEqualTo(Duration.ofMillis(500));
        }
    }

    @Nested
    @DisplayName("Record Equality Tests")
    class EqualityTests {

        @Test
        @DisplayName("equal gaps should be equal")
        void equalGapsShouldBeEqual() {
            Instant start = Instant.parse("2026-01-01T00:00:00Z");
            Instant end = Instant.parse("2026-01-01T01:00:00Z");

            Gap gap1 = new Gap(start, end);
            Gap gap2 = new Gap(start, end);

            assertThat(gap1).isEqualTo(gap2);
            assertThat(gap1.hashCode()).isEqualTo(gap2.hashCode());
        }

        @Test
        @DisplayName("different gaps should not be equal")
        void differentGapsShouldNotBeEqual() {
            Instant start = Instant.parse("2026-01-01T00:00:00Z");
            Instant end1 = Instant.parse("2026-01-01T01:00:00Z");
            Instant end2 = Instant.parse("2026-01-01T02:00:00Z");

            Gap gap1 = new Gap(start, end1);
            Gap gap2 = new Gap(start, end2);

            assertThat(gap1).isNotEqualTo(gap2);
        }
    }
}
