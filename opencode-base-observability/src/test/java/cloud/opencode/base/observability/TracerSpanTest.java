package cloud.opencode.base.observability;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for {@link Span}, {@link Tracer}, and {@link OpenTelemetryTracer}.
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-observability V1.0.0
 */
@DisplayName("Tracer 和 Span - 追踪器与跨度")
class TracerSpanTest {

    // ==================== Span.NOOP ====================

    @Nested
    @DisplayName("Span.NOOP - 空操作 Span")
    class SpanNoopTests {

        @Test
        @DisplayName("setHit 不抛出异常")
        void setHitDoesNotThrow() {
            assertThatNoException().isThrownBy(() -> Span.NOOP.setHit(true));
            assertThatNoException().isThrownBy(() -> Span.NOOP.setHit(false));
        }

        @Test
        @DisplayName("setError 不抛出异常")
        void setErrorDoesNotThrow() {
            assertThatNoException().isThrownBy(() -> Span.NOOP.setError(new RuntimeException("test")));
            assertThatNoException().isThrownBy(() -> Span.NOOP.setError(null));
        }

        @Test
        @DisplayName("setAttribute 不抛出异常")
        void setAttributeDoesNotThrow() {
            assertThatNoException().isThrownBy(() -> Span.NOOP.setAttribute("key", "value"));
            assertThatNoException().isThrownBy(() -> Span.NOOP.setAttribute(null, null));
        }

        @Test
        @DisplayName("end 不抛出异常")
        void endDoesNotThrow() {
            assertThatNoException().isThrownBy(Span.NOOP::end);
        }

        @Test
        @DisplayName("close 不抛出异常")
        void closeDoesNotThrow() {
            assertThatNoException().isThrownBy(Span.NOOP::close);
        }

        @Test
        @DisplayName("多次调用 end 和 close 不抛出异常（幂等）")
        void multipleEndAndCloseCallsAreIdempotent() {
            assertThatNoException().isThrownBy(() -> {
                Span.NOOP.end();
                Span.NOOP.end();
                Span.NOOP.close();
                Span.NOOP.close();
                Span.NOOP.end();
            });
        }

        @Test
        @DisplayName("NOOP 实现 AutoCloseable 接口")
        void noopImplementsAutoCloseable() {
            assertThat(Span.NOOP).isInstanceOf(AutoCloseable.class);
        }

        @Test
        @DisplayName("NOOP 是 Span 的实例")
        void noopIsSpanInstance() {
            assertThat(Span.NOOP).isInstanceOf(Span.class);
        }
    }

    // ==================== Span try-with-resources ====================

    @Nested
    @DisplayName("Span try-with-resources 模式")
    class SpanTryWithResourcesTests {

        @Test
        @DisplayName("NOOP Span 可在 try-with-resources 中使用")
        void noopSpanWorksWithTryWithResources() {
            assertThatNoException().isThrownBy(() -> {
                try (Span span = Span.NOOP) {
                    span.setHit(true);
                    span.setAttribute("cache.tier", "L1");
                }
            });
        }

        @Test
        @DisplayName("try-with-resources 中设置错误后正常关闭")
        void tryWithResourcesWithError() {
            assertThatNoException().isThrownBy(() -> {
                try (Span span = Span.NOOP) {
                    span.setError(new RuntimeException("simulated error"));
                    span.setHit(false);
                }
            });
        }

        @Test
        @DisplayName("嵌套 try-with-resources 正常工作")
        void nestedTryWithResources() {
            assertThatNoException().isThrownBy(() -> {
                try (Span outer = Span.NOOP) {
                    outer.setAttribute("level", "outer");
                    try (Span inner = Span.NOOP) {
                        inner.setAttribute("level", "inner");
                        inner.setHit(true);
                    }
                    outer.setHit(true);
                }
            });
        }
    }

    // ==================== Tracer.noop() ====================

    @Nested
    @DisplayName("Tracer.noop() - 空操作追踪器")
    class TracerNoopTests {

        @Test
        @DisplayName("返回 NoopTracer 实例")
        void returnsNoopTracerInstance() {
            Tracer tracer = Tracer.noop();

            assertThat(tracer).isNotNull();
            assertThat(tracer).isInstanceOf(Tracer.NoopTracer.class);
        }

        @Test
        @DisplayName("多次调用返回同一实例（单例）")
        void returnsSameSingletonInstance() {
            Tracer first = Tracer.noop();
            Tracer second = Tracer.noop();

            assertThat(first).isSameAs(second);
        }

        @Test
        @DisplayName("startSpan 返回 Span.NOOP")
        void startSpanReturnsNoopSpan() {
            Tracer tracer = Tracer.noop();

            Span span = tracer.startSpan("GET", "user:123");

            assertThat(span).isSameAs(Span.NOOP);
        }

        @Test
        @DisplayName("close 不抛出异常")
        void closeDoesNotThrow() {
            Tracer tracer = Tracer.noop();

            assertThatNoException().isThrownBy(tracer::close);
        }

        @Test
        @DisplayName("多次调用 close 不抛出异常")
        void multipleCloseDoesNotThrow() {
            Tracer tracer = Tracer.noop();

            assertThatNoException().isThrownBy(() -> {
                tracer.close();
                tracer.close();
            });
        }

        @Test
        @DisplayName("toString 返回描述性字符串")
        void toStringReturnsDescriptiveString() {
            assertThat(Tracer.noop().toString()).isEqualTo("Tracer.noop()");
        }

        @Test
        @DisplayName("startSpan 后可在 try-with-resources 中使用")
        void startSpanWorksWithTryWithResources() {
            Tracer tracer = Tracer.noop();

            assertThatNoException().isThrownBy(() -> {
                try (Span span = tracer.startSpan("PUT", "item:42")) {
                    span.setHit(true);
                    span.setAttribute("size", "1024");
                }
            });
        }
    }

    // ==================== OpenTelemetryTracer ====================

    @Nested
    @DisplayName("OpenTelemetryTracer - OpenTelemetry 追踪器")
    class OpenTelemetryTracerTests {

        @Test
        @DisplayName("创建时未检测到 OTel，isOtelAvailable 返回 false")
        void createWithoutOtelReturnsFalseForAvailability() {
            OpenTelemetryTracer tracer = OpenTelemetryTracer.create("test-service");

            assertThat(tracer.isOtelAvailable()).isFalse();
        }

        @Test
        @DisplayName("serviceName 返回正确的服务名称")
        void serviceNameReturnsCorrectValue() {
            OpenTelemetryTracer tracer = OpenTelemetryTracer.create("my-cache-service");

            assertThat(tracer.serviceName()).isEqualTo("my-cache-service");
        }

        @Test
        @DisplayName("不同服务名称创建不同的追踪器")
        void differentServiceNamesCreateDifferentTracers() {
            OpenTelemetryTracer tracer1 = OpenTelemetryTracer.create("service-a");
            OpenTelemetryTracer tracer2 = OpenTelemetryTracer.create("service-b");

            assertThat(tracer1.serviceName()).isEqualTo("service-a");
            assertThat(tracer2.serviceName()).isEqualTo("service-b");
            assertThat(tracer1).isNotSameAs(tracer2);
        }

        @Test
        @DisplayName("OTel 不可用时 startSpan 返回 Span.NOOP")
        void startSpanReturnsNoopWhenOtelUnavailable() {
            OpenTelemetryTracer tracer = OpenTelemetryTracer.create("test-service");

            Span span = tracer.startSpan("GET", "user:1");

            assertThat(span).isSameAs(Span.NOOP);
        }

        @Test
        @DisplayName("空服务名称抛出 NullPointerException")
        void nullServiceNameThrows() {
            assertThatNullPointerException()
                    .isThrownBy(() -> OpenTelemetryTracer.create(null));
        }

        @Test
        @DisplayName("close 后 startSpan 仍返回 Span.NOOP")
        void startSpanAfterCloseReturnsNoop() {
            OpenTelemetryTracer tracer = OpenTelemetryTracer.create("test-service");
            tracer.close();

            Span span = tracer.startSpan("GET", "key:1");

            assertThat(span).isSameAs(Span.NOOP);
        }

        @Test
        @DisplayName("多次调用 close 不抛出异常")
        void multipleCloseDoesNotThrow() {
            OpenTelemetryTracer tracer = OpenTelemetryTracer.create("test-service");

            assertThatNoException().isThrownBy(() -> {
                tracer.close();
                tracer.close();
                tracer.close();
            });
        }

        @Test
        @DisplayName("实现 Tracer 接口")
        void implementsTracerInterface() {
            OpenTelemetryTracer tracer = OpenTelemetryTracer.create("test-service");

            assertThat(tracer).isInstanceOf(Tracer.class);
        }

        @Test
        @DisplayName("完整的 try-with-resources 工作流")
        void fullTryWithResourcesWorkflow() {
            OpenTelemetryTracer tracer = OpenTelemetryTracer.create("test-service");

            assertThatNoException().isThrownBy(() -> {
                try (Span span = tracer.startSpan("GET", "user:42")) {
                    span.setHit(true);
                    span.setAttribute("cache.tier", "L1");
                    span.setAttribute("cache.size", "256");
                }
            });

            tracer.close();
        }
    }
}
