package cloud.opencode.base.observability.metric;

import cloud.opencode.base.observability.exception.ObservabilityException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link MetricSnapshot}.
 */
@DisplayName("MetricSnapshot")
class MetricSnapshotTest {

    @Nested
    @DisplayName("Construction")
    class Construction {

        @Test
        @DisplayName("should create with valid parameters")
        void shouldCreateWithValidParameters() {
            MetricId id = MetricId.of("cpu.usage", Tag.of("host", "server1"));
            Map<String, Object> values = Map.of("value", 42.0);

            MetricSnapshot snapshot = new MetricSnapshot(id, "gauge", values);

            assertThat(snapshot.id()).isSameAs(id);
            assertThat(snapshot.type()).isEqualTo("gauge");
            assertThat(snapshot.values()).containsEntry("value", 42.0);
        }

        @Test
        @DisplayName("null id throws ObservabilityException")
        void nullIdThrows() {
            assertThatThrownBy(() -> new MetricSnapshot(null, "gauge", Map.of()))
                    .isInstanceOf(ObservabilityException.class)
                    .hasMessageContaining("MetricId must not be null");
        }

        @Test
        @DisplayName("null type throws ObservabilityException")
        void nullTypeThrows() {
            MetricId id = MetricId.of("test");
            assertThatThrownBy(() -> new MetricSnapshot(id, null, Map.of()))
                    .isInstanceOf(ObservabilityException.class)
                    .hasMessageContaining("type");
        }

        @Test
        @DisplayName("blank type throws ObservabilityException")
        void blankTypeThrows() {
            MetricId id = MetricId.of("test");
            assertThatThrownBy(() -> new MetricSnapshot(id, "   ", Map.of()))
                    .isInstanceOf(ObservabilityException.class)
                    .hasMessageContaining("type");
        }
    }

    @Nested
    @DisplayName("Values")
    class Values {

        @Test
        @DisplayName("null values becomes empty map")
        void nullValuesBecomesEmptyMap() {
            MetricId id = MetricId.of("test");
            MetricSnapshot snapshot = new MetricSnapshot(id, "counter", null);

            assertThat(snapshot.values()).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("defensive copy of values — mutating original does not affect snapshot")
        void defensiveCopyOfValues() {
            MetricId id = MetricId.of("test");
            HashMap<String, Object> mutable = new HashMap<>();
            mutable.put("count", 10L);

            MetricSnapshot snapshot = new MetricSnapshot(id, "counter", mutable);

            mutable.put("extra", "should not appear");
            assertThat(snapshot.values()).doesNotContainKey("extra");
            assertThat(snapshot.values()).hasSize(1).containsEntry("count", 10L);
        }

        @Test
        @DisplayName("values map is unmodifiable")
        void valuesMapIsUnmodifiable() {
            MetricId id = MetricId.of("test");
            MetricSnapshot snapshot = new MetricSnapshot(id, "counter", Map.of("a", 1));

            assertThatThrownBy(() -> snapshot.values().put("b", 2))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}
