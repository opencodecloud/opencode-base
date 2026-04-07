package cloud.opencode.base.observability.metric;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link Gauge} via {@link MetricRegistry}.
 */
@DisplayName("Gauge")
class GaugeTest {

    private MetricRegistry registry;

    @BeforeEach
    void setUp() {
        registry = MetricRegistry.create();
    }

    @Nested
    @DisplayName("value()")
    class Value {

        @Test
        @DisplayName("should reflect supplier value in real time")
        void shouldReflectSupplierValue() {
            AtomicInteger backing = new AtomicInteger(42);
            Gauge gauge = registry.gauge("queue.size", () -> (double) backing.get());
            assertThat(gauge.value()).isEqualTo(42.0);

            backing.set(100);
            assertThat(gauge.value()).isEqualTo(100.0);

            backing.set(0);
            assertThat(gauge.value()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("should return constant value from constant supplier")
        void shouldReturnConstantValue() {
            Gauge gauge = registry.gauge("constant", () -> 3.14);
            assertThat(gauge.value()).isEqualTo(3.14);
        }

        @Test
        @DisplayName("should return 0.0 when supplier returns null")
        void shouldReturnZeroWhenSupplierReturnsNull() {
            Gauge gauge = registry.gauge("nullable", () -> null);
            assertThat(gauge.value()).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("id()")
    class Id {

        @Test
        @DisplayName("should return correct metric id")
        void shouldReturnCorrectId() {
            Gauge gauge = registry.gauge("mem.used", () -> 512.0, Tag.of("unit", "MB"));
            assertThat(gauge.id().name()).isEqualTo("mem.used");
            assertThat(gauge.id().tags()).hasSize(1);
        }
    }
}
