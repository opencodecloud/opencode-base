package cloud.opencode.base.observability.metric;

import cloud.opencode.base.observability.exception.ObservabilityException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link MetricRegistry}.
 */
@DisplayName("MetricRegistry")
class MetricRegistryTest {

    private MetricRegistry registry;

    @BeforeEach
    void setUp() {
        registry = MetricRegistry.create();
    }

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("should create registry with default limit")
        void shouldCreateWithDefaultLimit() {
            MetricRegistry reg = MetricRegistry.create();
            assertThat(reg.size()).isEqualTo(0);
        }

        @Test
        @DisplayName("should create registry with custom limit")
        void shouldCreateWithCustomLimit() {
            MetricRegistry reg = MetricRegistry.create(5);
            assertThat(reg.size()).isEqualTo(0);
        }

        @Test
        @DisplayName("should throw on non-positive maxMetrics")
        void shouldThrowOnNonPositiveMax() {
            assertThatThrownBy(() -> MetricRegistry.create(0))
                    .isInstanceOf(ObservabilityException.class)
                    .hasMessageContaining("maxMetrics must be positive");
            assertThatThrownBy(() -> MetricRegistry.create(-1))
                    .isInstanceOf(ObservabilityException.class)
                    .hasMessageContaining("maxMetrics must be positive");
        }
    }

    @Nested
    @DisplayName("Registration")
    class Registration {

        @Test
        @DisplayName("should register counter")
        void shouldRegisterCounter() {
            Counter counter = registry.counter("c");
            assertThat(counter).isNotNull();
            assertThat(registry.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("should register gauge")
        void shouldRegisterGauge() {
            Gauge gauge = registry.gauge("g", () -> 1.0);
            assertThat(gauge).isNotNull();
            assertThat(registry.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("should register timer")
        void shouldRegisterTimer() {
            Timer timer = registry.timer("t");
            assertThat(timer).isNotNull();
            assertThat(registry.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("should register histogram")
        void shouldRegisterHistogram() {
            Histogram histogram = registry.histogram("h");
            assertThat(histogram).isNotNull();
            assertThat(registry.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("should throw on null gauge supplier")
        void shouldThrowOnNullGaugeSupplier() {
            assertThatThrownBy(() -> registry.gauge("g", null))
                    .isInstanceOf(ObservabilityException.class)
                    .hasMessageContaining("Gauge supplier must not be null");
        }
    }

    @Nested
    @DisplayName("Idempotent registration")
    class IdempotentRegistration {

        @Test
        @DisplayName("should return same counter for same id")
        void shouldReturnSameCounter() {
            Counter c1 = registry.counter("c", Tag.of("env", "prod"));
            Counter c2 = registry.counter("c", Tag.of("env", "prod"));
            assertThat(c1).isSameAs(c2);
            assertThat(registry.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("should return same timer for same id")
        void shouldReturnSameTimer() {
            Timer t1 = registry.timer("t");
            Timer t2 = registry.timer("t");
            assertThat(t1).isSameAs(t2);
        }

        @Test
        @DisplayName("should distinguish by tags")
        void shouldDistinguishByTags() {
            Counter c1 = registry.counter("c", Tag.of("env", "prod"));
            Counter c2 = registry.counter("c", Tag.of("env", "staging"));
            assertThat(c1).isNotSameAs(c2);
            assertThat(registry.size()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Type conflict")
    class TypeConflict {

        @Test
        @DisplayName("should throw when registering different type for same id")
        void shouldThrowOnTypeConflict() {
            registry.counter("m");
            assertThatThrownBy(() -> registry.timer("m"))
                    .isInstanceOf(ObservabilityException.class)
                    .hasMessageContaining("already registered");
        }

        @Test
        @DisplayName("should throw when registering gauge over counter")
        void shouldThrowGaugeOverCounter() {
            registry.counter("m");
            assertThatThrownBy(() -> registry.gauge("m", () -> 1.0))
                    .isInstanceOf(ObservabilityException.class)
                    .hasMessageContaining("already registered");
        }
    }

    @Nested
    @DisplayName("maxMetrics limit")
    class MaxMetricsLimit {

        @Test
        @DisplayName("should throw when exceeding max metrics")
        void shouldThrowWhenExceedingMax() {
            MetricRegistry small = MetricRegistry.create(2);
            small.counter("c1");
            small.counter("c2");
            assertThatThrownBy(() -> small.counter("c3"))
                    .isInstanceOf(ObservabilityException.class)
                    .hasMessageContaining("full");
        }
    }

    @Nested
    @DisplayName("snapshot()")
    class Snapshot {

        @Test
        @DisplayName("should return snapshots of all metrics")
        void shouldReturnAllSnapshots() {
            registry.counter("c").increment(5);
            registry.gauge("g", () -> 3.14);
            registry.timer("t");
            registry.histogram("h");

            List<MetricSnapshot> snapshots = registry.snapshot();
            assertThat(snapshots).hasSize(4);
        }

        @Test
        @DisplayName("counter snapshot should contain count")
        void counterSnapshotShouldContainCount() {
            registry.counter("c").increment(7);
            List<MetricSnapshot> snapshots = registry.snapshot();
            MetricSnapshot cs = snapshots.stream()
                    .filter(s -> s.type().equals("counter"))
                    .findFirst().orElseThrow();
            assertThat(cs.values()).containsEntry("count", 7L);
        }

        @Test
        @DisplayName("gauge snapshot should contain value")
        void gaugeSnapshotShouldContainValue() {
            registry.gauge("g", () -> 42.0);
            List<MetricSnapshot> snapshots = registry.snapshot();
            MetricSnapshot gs = snapshots.stream()
                    .filter(s -> s.type().equals("gauge"))
                    .findFirst().orElseThrow();
            assertThat(gs.values()).containsEntry("value", 42.0);
        }

        @Test
        @DisplayName("should return empty list when no metrics")
        void shouldReturnEmptyWhenNoMetrics() {
            assertThat(registry.snapshot()).isEmpty();
        }

        @Test
        @DisplayName("returned list should be immutable")
        void shouldReturnImmutableList() {
            registry.counter("c");
            List<MetricSnapshot> snapshots = registry.snapshot();
            assertThatThrownBy(() -> snapshots.add(null))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("find()")
    class Find {

        @Test
        @DisplayName("should find existing metric by name")
        void shouldFindByName() {
            Counter c = registry.counter("requests");
            Optional<?> found = registry.find("requests");
            assertThat(found).isPresent();
            assertThat(found.get()).isSameAs(c);
        }

        @Test
        @DisplayName("should return empty for unknown name")
        void shouldReturnEmptyForUnknown() {
            assertThat(registry.find("nonexistent")).isEmpty();
        }
    }

    @Nested
    @DisplayName("remove()")
    class Remove {

        @Test
        @DisplayName("should remove existing metric")
        void shouldRemoveExisting() {
            Counter c = registry.counter("c");
            boolean removed = registry.remove(c.id());
            assertThat(removed).isTrue();
            assertThat(registry.size()).isEqualTo(0);
        }

        @Test
        @DisplayName("should return false for non-existing metric")
        void shouldReturnFalseForNonExisting() {
            boolean removed = registry.remove(MetricId.of("nonexistent"));
            assertThat(removed).isFalse();
        }
    }

    @Nested
    @DisplayName("clear()")
    class Clear {

        @Test
        @DisplayName("should remove all metrics")
        void shouldClearAll() {
            registry.counter("c1");
            registry.counter("c2");
            registry.timer("t1");
            assertThat(registry.size()).isEqualTo(3);
            registry.clear();
            assertThat(registry.size()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("size()")
    class Size {

        @Test
        @DisplayName("should return zero initially")
        void shouldReturnZeroInitially() {
            assertThat(registry.size()).isEqualTo(0);
        }

        @Test
        @DisplayName("should reflect registration count")
        void shouldReflectRegistrationCount() {
            registry.counter("c");
            registry.timer("t");
            assertThat(registry.size()).isEqualTo(2);
        }
    }
}
