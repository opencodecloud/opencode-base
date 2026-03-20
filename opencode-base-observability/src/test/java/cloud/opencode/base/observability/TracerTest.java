package cloud.opencode.base.observability;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link Tracer} sealed interface and its NoopTracer implementation.
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-observability V1.0.0
 */
@DisplayName("Tracer")
class TracerTest {

    @Nested
    @DisplayName("noop() factory method")
    class NoopFactory {

        @Test
        @DisplayName("should return non-null tracer")
        void shouldReturnNonNull() {
            assertThat(Tracer.noop()).isNotNull();
        }

        @Test
        @DisplayName("should return same singleton instance")
        void shouldReturnSingleton() {
            Tracer first = Tracer.noop();
            Tracer second = Tracer.noop();
            assertThat(first).isSameAs(second);
        }

        @Test
        @DisplayName("should return instance of Tracer")
        void shouldBeTracer() {
            assertThat(Tracer.noop()).isInstanceOf(Tracer.class);
        }

        @Test
        @DisplayName("should return NoopTracer type")
        void shouldBeNoopTracerType() {
            assertThat(Tracer.noop()).isInstanceOf(Tracer.NoopTracer.class);
        }
    }

    @Nested
    @DisplayName("NoopTracer")
    class NoopTracerTests {

        private final Tracer tracer = Tracer.noop();

        @Test
        @DisplayName("startSpan should return Span.NOOP")
        void startSpanShouldReturnNoopSpan() {
            Span span = tracer.startSpan("GET", "key:1");
            assertThat(span).isSameAs(Span.NOOP);
        }

        @Test
        @DisplayName("startSpan should return NOOP for any operation name")
        void startSpanShouldReturnNoopForAnyOp() {
            assertThat(tracer.startSpan("PUT", "key:2")).isSameAs(Span.NOOP);
            assertThat(tracer.startSpan("DELETE", "key:3")).isSameAs(Span.NOOP);
            assertThat(tracer.startSpan("COMPUTE", "data")).isSameAs(Span.NOOP);
        }

        @Test
        @DisplayName("startSpan should return NOOP for empty strings")
        void startSpanShouldReturnNoopForEmptyStrings() {
            assertThat(tracer.startSpan("", "")).isSameAs(Span.NOOP);
        }

        @Test
        @DisplayName("close should not throw")
        void closeShouldNotThrow() {
            assertThatCode(tracer::close).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("close should be idempotent")
        void closeShouldBeIdempotent() {
            assertThatCode(() -> {
                tracer.close();
                tracer.close();
                tracer.close();
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("startSpan should still work after close")
        void startSpanShouldWorkAfterClose() {
            tracer.close();
            Span span = tracer.startSpan("GET", "key");
            assertThat(span).isSameAs(Span.NOOP);
        }

        @Test
        @DisplayName("toString should return descriptive string")
        void toStringShouldReturnDescriptive() {
            assertThat(tracer.toString()).isEqualTo("Tracer.noop()");
        }
    }

    @Nested
    @DisplayName("sealed interface")
    class SealedInterface {

        @Test
        @DisplayName("Tracer should be a sealed interface")
        void shouldBeSealed() {
            assertThat(Tracer.class.isSealed()).isTrue();
        }

        @Test
        @DisplayName("permitted subclasses should include NoopTracer and OpenTelemetryTracer")
        void shouldHavePermittedSubclasses() {
            Class<?>[] permitted = Tracer.class.getPermittedSubclasses();
            assertThat(permitted).isNotNull();
            assertThat(permitted).hasSize(2);

            var names = java.util.Arrays.stream(permitted)
                    .map(Class::getSimpleName)
                    .toList();
            assertThat(names).contains("OpenTelemetryTracer", "NoopTracer");
        }
    }

    @Nested
    @DisplayName("try-with-resources pattern")
    class TryWithResources {

        @Test
        @DisplayName("should support full tracing lifecycle with noop")
        void shouldSupportFullLifecycle() {
            Tracer tracer = Tracer.noop();
            assertThatCode(() -> {
                try (Span span = tracer.startSpan("GET", "user:42")) {
                    span.setHit(true);
                    span.setAttribute("cache.tier", "L1");
                }
                tracer.close();
            }).doesNotThrowAnyException();
        }
    }
}
