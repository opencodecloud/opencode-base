package cloud.opencode.base.observability.health;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link HealthStatus}.
 */
class HealthStatusTest {

    @Nested
    @DisplayName("AggregateTests")
    class AggregateTests {

        @Test
        @DisplayName("allUp returns UP")
        void allUp() {
            assertThat(HealthStatus.aggregate(List.of(HealthStatus.UP, HealthStatus.UP, HealthStatus.UP)))
                    .isEqualTo(HealthStatus.UP);
        }

        @Test
        @DisplayName("anyDown returns DOWN")
        void anyDown() {
            assertThat(HealthStatus.aggregate(List.of(HealthStatus.UP, HealthStatus.DOWN, HealthStatus.UP)))
                    .isEqualTo(HealthStatus.DOWN);
        }

        @Test
        @DisplayName("anyDegraded with no DOWN returns DEGRADED")
        void anyDegraded_noDown() {
            assertThat(HealthStatus.aggregate(List.of(HealthStatus.UP, HealthStatus.DEGRADED, HealthStatus.UP)))
                    .isEqualTo(HealthStatus.DEGRADED);
        }

        @Test
        @DisplayName("emptyCollection returns UP")
        void emptyCollection() {
            assertThat(HealthStatus.aggregate(List.of()))
                    .isEqualTo(HealthStatus.UP);
        }

        @Test
        @DisplayName("nullCollection returns UP")
        void nullCollection() {
            assertThat(HealthStatus.aggregate(null))
                    .isEqualTo(HealthStatus.UP);
        }

        @Test
        @DisplayName("mixed statuses with DOWN returns DOWN (DOWN takes priority)")
        void mixed() {
            assertThat(HealthStatus.aggregate(List.of(HealthStatus.UP, HealthStatus.DEGRADED, HealthStatus.DOWN)))
                    .isEqualTo(HealthStatus.DOWN);
        }
    }
}
