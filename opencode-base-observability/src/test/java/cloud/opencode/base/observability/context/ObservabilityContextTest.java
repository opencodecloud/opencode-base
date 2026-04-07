package cloud.opencode.base.observability.context;

import cloud.opencode.base.observability.exception.ObservabilityException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link ObservabilityContext}.
 */
class ObservabilityContextTest {

    @AfterEach
    void cleanup() {
        ObservabilityContext.clear();
    }

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("create with traceId generates a spanId")
        void createWithTraceId() {
            ObservabilityContext ctx = ObservabilityContext.create("trace-1");
            assertThat(ctx.traceId()).isEqualTo("trace-1");
            assertThat(ctx.spanId()).isNotNull().hasSize(16);
        }

        @Test
        @DisplayName("create with traceId and spanId")
        void createWithTraceIdAndSpanId() {
            ObservabilityContext ctx = ObservabilityContext.create("trace-1", "span-1");
            assertThat(ctx.traceId()).isEqualTo("trace-1");
            assertThat(ctx.spanId()).isEqualTo("span-1");
        }

        @Test
        @DisplayName("null traceId throws ObservabilityException")
        void nullTraceId() {
            assertThatThrownBy(() -> ObservabilityContext.create(null))
                    .isInstanceOf(ObservabilityException.class)
                    .hasMessageContaining("traceId");
        }

        @Test
        @DisplayName("blank traceId throws ObservabilityException")
        void blankTraceId() {
            assertThatThrownBy(() -> ObservabilityContext.create("  "))
                    .isInstanceOf(ObservabilityException.class)
                    .hasMessageContaining("traceId");
        }
    }

    @Nested
    @DisplayName("ThreadLocalManagement")
    class ThreadLocalManagement {

        @Test
        @DisplayName("current() returns null initially")
        void currentReturnsNullInitially() {
            assertThat(ObservabilityContext.current()).isNull();
        }

        @Test
        @DisplayName("attach sets current context")
        void attachSetsCurrent() {
            ObservabilityContext ctx = ObservabilityContext.create("trace-1", "span-1");
            try (var scope = ctx.attach()) {
                assertThat(ObservabilityContext.current()).isSameAs(ctx);
            }
        }

        @Test
        @DisplayName("close restores previous context")
        void closeRestoresPrevious() {
            ObservabilityContext ctx1 = ObservabilityContext.create("trace-1", "span-1");
            ObservabilityContext ctx2 = ObservabilityContext.create("trace-2", "span-2");

            try (var scope1 = ctx1.attach()) {
                assertThat(ObservabilityContext.current()).isSameAs(ctx1);
                try (var scope2 = ctx2.attach()) {
                    assertThat(ObservabilityContext.current()).isSameAs(ctx2);
                }
                assertThat(ObservabilityContext.current()).isSameAs(ctx1);
            }
            assertThat(ObservabilityContext.current()).isNull();
        }

        @Test
        @DisplayName("nested attach and close work correctly")
        void nestedAttachClose() {
            ObservabilityContext outer = ObservabilityContext.create("trace-outer", "span-outer");
            ObservabilityContext middle = ObservabilityContext.create("trace-middle", "span-middle");
            ObservabilityContext inner = ObservabilityContext.create("trace-inner", "span-inner");

            try (var s1 = outer.attach()) {
                try (var s2 = middle.attach()) {
                    try (var s3 = inner.attach()) {
                        assertThat(ObservabilityContext.current()).isSameAs(inner);
                    }
                    assertThat(ObservabilityContext.current()).isSameAs(middle);
                }
                assertThat(ObservabilityContext.current()).isSameAs(outer);
            }
            assertThat(ObservabilityContext.current()).isNull();
        }

        @Test
        @DisplayName("clear removes current context")
        void clearRemovesCurrent() {
            ObservabilityContext ctx = ObservabilityContext.create("trace-1", "span-1");
            ctx.attach();
            assertThat(ObservabilityContext.current()).isNotNull();
            ObservabilityContext.clear();
            assertThat(ObservabilityContext.current()).isNull();
        }

        @Test
        @DisplayName("scope close on wrong thread is silently ignored")
        void scopeCloseOnWrongThreadIsIgnored() throws Exception {
            ObservabilityContext ctx = ObservabilityContext.create("trace-1", "span-1");
            ObservabilityContext.Scope scope = ctx.attach();
            assertThat(ObservabilityContext.current()).isSameAs(ctx);

            // Close the scope from a different thread
            Thread otherThread = new Thread(scope::close);
            otherThread.start();
            otherThread.join();

            // Main thread's context should be unchanged — cross-thread close was ignored
            assertThat(ObservabilityContext.current()).isSameAs(ctx);

            // Clean up properly on the correct thread
            scope.close();
            assertThat(ObservabilityContext.current()).isNull();
        }
    }

    @Nested
    @DisplayName("ImmutableCopy")
    class ImmutableCopy {

        @Test
        @DisplayName("withSpanId returns new instance with different spanId")
        void withSpanIdReturnsNew() {
            ObservabilityContext original = ObservabilityContext.create("trace-1", "span-1");
            ObservabilityContext updated = original.withSpanId("span-2");

            assertThat(updated).isNotSameAs(original);
            assertThat(updated.spanId()).isEqualTo("span-2");
            assertThat(updated.traceId()).isEqualTo("trace-1");
        }

        @Test
        @DisplayName("withBaggage returns new instance with added baggage")
        void withBaggageReturnsNew() {
            ObservabilityContext original = ObservabilityContext.create("trace-1", "span-1");
            ObservabilityContext updated = original.withBaggage("key", "value");

            assertThat(updated).isNotSameAs(original);
            assertThat(updated.baggage("key")).hasValue("value");
        }

        @Test
        @DisplayName("original unchanged after withBaggage")
        void originalUnchanged() {
            ObservabilityContext original = ObservabilityContext.create("trace-1", "span-1");
            original.withBaggage("key", "value");

            assertThat(original.baggage("key")).isEmpty();
            assertThat(original.allBaggage()).isEmpty();
        }
    }

    @Nested
    @DisplayName("ContextPropagation")
    class ContextPropagation {

        @Test
        @DisplayName("wrap(Runnable) propagates context to another thread")
        void wrapRunnable() throws Exception {
            ObservabilityContext ctx = ObservabilityContext.create("trace-1", "span-1");
            AtomicReference<ObservabilityContext> captured = new AtomicReference<>();
            CountDownLatch latch = new CountDownLatch(1);

            Runnable wrapped = ctx.wrap(() -> {
                captured.set(ObservabilityContext.current());
                latch.countDown();
            });

            ExecutorService executor = Executors.newSingleThreadExecutor();
            try {
                executor.submit(wrapped);
                latch.await();
                assertThat(captured.get()).isNotNull();
                assertThat(captured.get().traceId()).isEqualTo("trace-1");
                assertThat(captured.get().spanId()).isEqualTo("span-1");
            } finally {
                executor.shutdown();
            }
        }

        @Test
        @DisplayName("wrap(Callable) propagates context to another thread")
        void wrapCallable() throws Exception {
            ObservabilityContext ctx = ObservabilityContext.create("trace-1", "span-1");

            Callable<String> wrapped = ctx.wrap(() -> {
                ObservabilityContext current = ObservabilityContext.current();
                return current != null ? current.traceId() : null;
            });

            ExecutorService executor = Executors.newSingleThreadExecutor();
            try {
                Future<String> future = executor.submit(wrapped);
                assertThat(future.get()).isEqualTo("trace-1");
            } finally {
                executor.shutdown();
            }
        }

        @Test
        @DisplayName("wrap(null Runnable) throws ObservabilityException")
        void wrapNullRunnable() {
            ObservabilityContext ctx = ObservabilityContext.create("trace-1", "span-1");
            assertThatThrownBy(() -> ctx.wrap((Runnable) null))
                    .isInstanceOf(ObservabilityException.class)
                    .hasMessageContaining("Task");
        }

        @Test
        @DisplayName("wrap(null Callable) throws ObservabilityException")
        void wrapNullCallable() {
            ObservabilityContext ctx = ObservabilityContext.create("trace-1", "span-1");
            assertThatThrownBy(() -> ctx.wrap((Callable<?>) null))
                    .isInstanceOf(ObservabilityException.class)
                    .hasMessageContaining("Task");
        }
    }

    @Nested
    @DisplayName("Baggage")
    class Baggage {

        @Test
        @DisplayName("baggage(key) returns value when present")
        void baggageReturnsValue() {
            ObservabilityContext ctx = ObservabilityContext.create("trace-1", "span-1")
                    .withBaggage("tenant", "acme");
            assertThat(ctx.baggage("tenant")).hasValue("acme");
        }

        @Test
        @DisplayName("baggage(null) returns empty")
        void baggageNullKey() {
            ObservabilityContext ctx = ObservabilityContext.create("trace-1", "span-1");
            assertThat(ctx.baggage(null)).isEmpty();
        }

        @Test
        @DisplayName("baggage(missing) returns empty")
        void baggageMissingKey() {
            ObservabilityContext ctx = ObservabilityContext.create("trace-1", "span-1");
            assertThat(ctx.baggage("nonexistent")).isEmpty();
        }

        @Test
        @DisplayName("allBaggage() returns unmodifiable map")
        void allBaggageUnmodifiable() {
            ObservabilityContext ctx = ObservabilityContext.create("trace-1", "span-1")
                    .withBaggage("key", "value");

            assertThatThrownBy(() -> ctx.allBaggage().put("another", "val"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("withBaggage with null key throws")
        void withBaggageNullKey() {
            ObservabilityContext ctx = ObservabilityContext.create("trace-1", "span-1");
            assertThatThrownBy(() -> ctx.withBaggage(null, "value"))
                    .isInstanceOf(ObservabilityException.class)
                    .hasMessageContaining("key");
        }

        @Test
        @DisplayName("withBaggage with null value throws")
        void withBaggageNullValue() {
            ObservabilityContext ctx = ObservabilityContext.create("trace-1", "span-1");
            assertThatThrownBy(() -> ctx.withBaggage("key", null))
                    .isInstanceOf(ObservabilityException.class)
                    .hasMessageContaining("value");
        }
    }
}
