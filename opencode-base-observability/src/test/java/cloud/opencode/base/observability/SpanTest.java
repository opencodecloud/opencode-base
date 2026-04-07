package cloud.opencode.base.observability;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link Span} interface and its NOOP singleton.
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-observability V1.0.0
 */
@DisplayName("Span")
class SpanTest {

    @Nested
    @DisplayName("NOOP singleton")
    class NoopSpan {

        @Test
        @DisplayName("should not be null")
        void shouldNotBeNull() {
            assertThat(Span.NOOP).isNotNull();
        }

        @Test
        @DisplayName("should return same instance on repeated access")
        void shouldReturnSameInstance() {
            assertThat(Span.NOOP).isSameAs(Span.NOOP);
        }

        @Test
        @DisplayName("should implement Span interface")
        void shouldImplementSpan() {
            assertThat(Span.NOOP).isInstanceOf(Span.class);
        }

        @Test
        @DisplayName("should implement AutoCloseable")
        void shouldImplementAutoCloseable() {
            assertThat(Span.NOOP).isInstanceOf(AutoCloseable.class);
        }

        @Test
        @DisplayName("setHit should not throw for true")
        void setHitTrueShouldNotThrow() {
            assertThatCode(() -> Span.NOOP.setHit(true)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("setHit should not throw for false")
        void setHitFalseShouldNotThrow() {
            assertThatCode(() -> Span.NOOP.setHit(false)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("setError should not throw for non-null error")
        void setErrorShouldNotThrow() {
            assertThatCode(() -> Span.NOOP.setError(new RuntimeException("test")))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("setError should not throw for null error")
        void setErrorNullShouldNotThrow() {
            assertThatCode(() -> Span.NOOP.setError(null)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("setAttribute should not throw")
        void setAttributeShouldNotThrow() {
            assertThatCode(() -> Span.NOOP.setAttribute("key", "value"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("setAttribute should not throw for null key")
        void setAttributeNullKeyShouldNotThrow() {
            assertThatCode(() -> Span.NOOP.setAttribute(null, "value"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("setAttribute should not throw for null value")
        void setAttributeNullValueShouldNotThrow() {
            assertThatCode(() -> Span.NOOP.setAttribute("key", null))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("end should not throw")
        void endShouldNotThrow() {
            assertThatCode(() -> Span.NOOP.end()).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("end should be idempotent - multiple calls should not throw")
        void endShouldBeIdempotent() {
            assertThatCode(() -> {
                Span.NOOP.end();
                Span.NOOP.end();
                Span.NOOP.end();
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("close should not throw")
        void closeShouldNotThrow() {
            assertThatCode(() -> Span.NOOP.close()).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("close should be idempotent - multiple calls should not throw")
        void closeShouldBeIdempotent() {
            assertThatCode(() -> {
                Span.NOOP.close();
                Span.NOOP.close();
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should work with try-with-resources")
        void shouldWorkWithTryWithResources() {
            assertThatCode(() -> {
                try (Span span = Span.NOOP) {
                    span.setHit(true);
                    span.setAttribute("test", "value");
                }
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should tolerate full lifecycle: set attributes, set error, end")
        void shouldTolerateFullLifecycle() {
            assertThatCode(() -> {
                Span.NOOP.setAttribute("op", "GET");
                Span.NOOP.setHit(false);
                Span.NOOP.setError(new IllegalStateException("fail"));
                Span.NOOP.end();
                Span.NOOP.close();
            }).doesNotThrowAnyException();
        }
    }
}
