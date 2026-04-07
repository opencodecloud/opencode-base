package cloud.opencode.base.observability;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link OpenTelemetryTracer} with OTel NOT on classpath (graceful fallback).
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-observability V1.0.0
 */
@DisplayName("OpenTelemetryTracer")
class OpenTelemetryTracerTest {

    @Nested
    @DisplayName("create() factory method")
    class CreateFactory {

        @Test
        @DisplayName("should return non-null tracer")
        void shouldReturnNonNull() {
            OpenTelemetryTracer tracer = OpenTelemetryTracer.create("test-service");
            assertThat(tracer).isNotNull();
            tracer.close();
        }

        @Test
        @DisplayName("should throw NullPointerException for null service name")
        void shouldThrowForNullServiceName() {
            assertThatThrownBy(() -> OpenTelemetryTracer.create(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("serviceName");
        }

        @Test
        @DisplayName("should store the service name")
        void shouldStoreServiceName() {
            OpenTelemetryTracer tracer = OpenTelemetryTracer.create("my-service");
            assertThat(tracer.serviceName()).isEqualTo("my-service");
            tracer.close();
        }

        @Test
        @DisplayName("should accept empty service name")
        void shouldAcceptEmptyServiceName() {
            OpenTelemetryTracer tracer = OpenTelemetryTracer.create("");
            assertThat(tracer.serviceName()).isEmpty();
            tracer.close();
        }
    }

    @Nested
    @DisplayName("OTel not on classpath (fallback behavior)")
    class NoOtelOnClasspath {

        @Test
        @DisplayName("isOtelAvailable should return false when OTel is not on classpath")
        void shouldReportOtelUnavailable() {
            OpenTelemetryTracer tracer = OpenTelemetryTracer.create("test-service");
            assertThat(tracer.isOtelAvailable()).isFalse();
            tracer.close();
        }

        @Test
        @DisplayName("startSpan should return NOOP span when OTel is unavailable")
        void startSpanShouldReturnNoopSpan() {
            OpenTelemetryTracer tracer = OpenTelemetryTracer.create("test-service");
            Span span = tracer.startSpan("GET", "key:1");
            assertThat(span).isSameAs(Span.NOOP);
            tracer.close();
        }

        @Test
        @DisplayName("startSpan should return NOOP for various operations")
        void startSpanShouldReturnNoopForVariousOps() {
            OpenTelemetryTracer tracer = OpenTelemetryTracer.create("test-service");
            assertThat(tracer.startSpan("PUT", "key:2")).isSameAs(Span.NOOP);
            assertThat(tracer.startSpan("DELETE", "key:3")).isSameAs(Span.NOOP);
            assertThat(tracer.startSpan("COMPUTE", "data")).isSameAs(Span.NOOP);
            tracer.close();
        }
    }

    @Nested
    @DisplayName("startSpan() null checks")
    class StartSpanNullChecks {

        @Test
        @DisplayName("should throw NullPointerException for null operationName")
        void shouldThrowForNullOperationName() {
            OpenTelemetryTracer tracer = OpenTelemetryTracer.create("test-service");
            assertThatThrownBy(() -> tracer.startSpan(null, "key"))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("operationName");
            tracer.close();
        }

        @Test
        @DisplayName("should throw NullPointerException for null key")
        void shouldThrowForNullKey() {
            OpenTelemetryTracer tracer = OpenTelemetryTracer.create("test-service");
            assertThatThrownBy(() -> tracer.startSpan("GET", null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("key");
            tracer.close();
        }
    }

    @Nested
    @DisplayName("close()")
    class CloseTests {

        @Test
        @DisplayName("close should not throw")
        void closeShouldNotThrow() {
            OpenTelemetryTracer tracer = OpenTelemetryTracer.create("test-service");
            assertThatCode(tracer::close).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("close should be idempotent")
        void closeShouldBeIdempotent() {
            OpenTelemetryTracer tracer = OpenTelemetryTracer.create("test-service");
            assertThatCode(() -> {
                tracer.close();
                tracer.close();
                tracer.close();
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("startSpan should return NOOP after close")
        void startSpanShouldReturnNoopAfterClose() {
            OpenTelemetryTracer tracer = OpenTelemetryTracer.create("test-service");
            tracer.close();
            Span span = tracer.startSpan("GET", "key");
            assertThat(span).isSameAs(Span.NOOP);
        }
    }

    @Nested
    @DisplayName("Tracer interface compliance")
    class TracerInterfaceCompliance {

        @Test
        @DisplayName("should implement Tracer interface")
        void shouldImplementTracer() {
            OpenTelemetryTracer tracer = OpenTelemetryTracer.create("test-service");
            assertThat(tracer).isInstanceOf(Tracer.class);
            tracer.close();
        }

        @Test
        @DisplayName("should be a permitted subclass of Tracer")
        void shouldBePermittedSubclass() {
            assertThat(OpenTelemetryTracer.class.isSealed()).isFalse();
            // OpenTelemetryTracer is final
            assertThat(java.lang.reflect.Modifier.isFinal(OpenTelemetryTracer.class.getModifiers()))
                    .isTrue();
        }
    }

    @Nested
    @DisplayName("try-with-resources lifecycle")
    class TryWithResourcesLifecycle {

        @Test
        @DisplayName("should support full tracing lifecycle without OTel")
        void shouldSupportFullLifecycle() {
            assertThatCode(() -> {
                OpenTelemetryTracer tracer = OpenTelemetryTracer.create("my-service");
                try (Span span = tracer.startSpan("GET", "user:42")) {
                    span.setHit(true);
                    span.setAttribute("cache.tier", "L1");
                }
                tracer.close();
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should handle error recording in lifecycle")
        void shouldHandleErrorRecording() {
            assertThatCode(() -> {
                OpenTelemetryTracer tracer = OpenTelemetryTracer.create("my-service");
                try (Span span = tracer.startSpan("PUT", "data:99")) {
                    span.setError(new RuntimeException("simulated failure"));
                    span.setHit(false);
                }
                tracer.close();
            }).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("serviceName()")
    class ServiceNameTests {

        @Test
        @DisplayName("should return configured service name")
        void shouldReturnServiceName() {
            OpenTelemetryTracer tracer = OpenTelemetryTracer.create("cache-service");
            assertThat(tracer.serviceName()).isEqualTo("cache-service");
            tracer.close();
        }

        @Test
        @DisplayName("should preserve service name after close")
        void shouldPreserveServiceNameAfterClose() {
            OpenTelemetryTracer tracer = OpenTelemetryTracer.create("my-svc");
            tracer.close();
            assertThat(tracer.serviceName()).isEqualTo("my-svc");
        }
    }
}
