/*
 * Copyright 2025 OpenCode Cloud Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.opencode.base.cache.event;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for CacheEventDispatcher
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("CacheEventDispatcher Tests")
class CacheEventDispatcherTest {

    private CacheEventDispatcher<String, String> dispatcher;

    @BeforeEach
    void setUp() {
        dispatcher = CacheEventDispatcher.create();
    }

    @AfterEach
    void tearDown() {
        if (dispatcher != null && !dispatcher.isClosed()) {
            dispatcher.close();
        }
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("create creates dispatcher")
        void createCreatesDispatcher() {
            assertNotNull(dispatcher);
            assertFalse(dispatcher.isClosed());
        }

        @Test
        @DisplayName("builder creates dispatcher")
        void builderCreatesDispatcher() {
            CacheEventDispatcher<String, String> built = CacheEventDispatcher.<String, String>builder().build();
            assertNotNull(built);
            built.close();
        }
    }

    @Nested
    @DisplayName("Listener Management Tests")
    class ListenerManagementTests {

        @Test
        @DisplayName("addListener adds listener")
        void addListenerAddsListener() {
            assertEquals(0, dispatcher.listenerCount());

            dispatcher.addListener(event -> {});

            assertEquals(1, dispatcher.listenerCount());
        }

        @Test
        @DisplayName("addListener returns this")
        void addListenerReturnsThis() {
            CacheEventDispatcher<String, String> result = dispatcher.addListener(event -> {});
            assertSame(dispatcher, result);
        }

        @Test
        @DisplayName("addListener throws on null")
        void addListenerThrowsOnNull() {
            assertThrows(NullPointerException.class, () -> dispatcher.addListener(null));
        }

        @Test
        @DisplayName("removeListener removes listener")
        void removeListenerRemovesListener() {
            CacheEventListener<String, String> listener = event -> {};
            dispatcher.addListener(listener);
            assertEquals(1, dispatcher.listenerCount());

            assertTrue(dispatcher.removeListener(listener));
            assertEquals(0, dispatcher.listenerCount());
        }

        @Test
        @DisplayName("removeListener returns false for unknown listener")
        void removeListenerReturnsFalseForUnknownListener() {
            CacheEventListener<String, String> listener = event -> {};
            assertFalse(dispatcher.removeListener(listener));
        }

        @Test
        @DisplayName("clearListeners removes all listeners")
        void clearListenersRemovesAllListeners() {
            dispatcher.addListener(event -> {});
            dispatcher.addListener(event -> {});
            assertEquals(2, dispatcher.listenerCount());

            dispatcher.clearListeners();

            assertEquals(0, dispatcher.listenerCount());
        }
    }

    @Nested
    @DisplayName("Synchronous Dispatch Tests")
    class SynchronousDispatchTests {

        @Test
        @DisplayName("dispatch invokes listener")
        void dispatchInvokesListener() {
            List<CacheEvent<String, String>> received = new ArrayList<>();
            dispatcher.addListener(received::add);

            CacheEvent<String, String> event = CacheEvent.put("cache", "key", "value");
            dispatcher.dispatch(event);

            assertEquals(1, received.size());
            assertEquals(event, received.get(0));
        }

        @Test
        @DisplayName("dispatch invokes all listeners")
        void dispatchInvokesAllListeners() {
            AtomicInteger count = new AtomicInteger(0);
            dispatcher.addListener(event -> count.incrementAndGet());
            dispatcher.addListener(event -> count.incrementAndGet());
            dispatcher.addListener(event -> count.incrementAndGet());

            dispatcher.dispatch(CacheEvent.put("cache", "key", "value"));

            assertEquals(3, count.get());
        }

        @Test
        @DisplayName("dispatch throws on null event")
        void dispatchThrowsOnNullEvent() {
            assertThrows(NullPointerException.class, () -> dispatcher.dispatch(null));
        }

        @Test
        @DisplayName("dispatch filters by interest")
        void dispatchFiltersByInterest() {
            List<CacheEvent<String, String>> received = new ArrayList<>();
            dispatcher.addListener(CacheEventListener.onPut(received::add));

            dispatcher.dispatch(CacheEvent.put("cache", "key", "value"));
            dispatcher.dispatch(CacheEvent.getHit("cache", "key", "value"));

            assertEquals(1, received.size());
        }

        @Test
        @DisplayName("dispatch drops events when closed")
        void dispatchDropsEventsWhenClosed() {
            List<CacheEvent<String, String>> received = new ArrayList<>();
            dispatcher.addListener(received::add);
            dispatcher.close();

            dispatcher.dispatch(CacheEvent.put("cache", "key", "value"));

            assertEquals(0, received.size());
        }
    }

    @Nested
    @DisplayName("Asynchronous Dispatch Tests")
    class AsynchronousDispatchTests {

        @Test
        @DisplayName("dispatchAsync returns CompletableFuture")
        void dispatchAsyncReturnsCompletableFuture() {
            CompletableFuture<Void> future = dispatcher.dispatchAsync(CacheEvent.put("cache", "key", "value"));
            assertNotNull(future);
            future.join();
        }

        @Test
        @DisplayName("dispatchAsync invokes listener")
        void dispatchAsyncInvokesListener() throws Exception {
            CountDownLatch latch = new CountDownLatch(1);
            List<CacheEvent<String, String>> received = new ArrayList<>();
            dispatcher.addListener(event -> {
                received.add(event);
                latch.countDown();
            });

            CacheEvent<String, String> event = CacheEvent.put("cache", "key", "value");
            dispatcher.dispatchAsync(event);

            assertTrue(latch.await(5, TimeUnit.SECONDS));
            assertEquals(1, received.size());
        }

        @Test
        @DisplayName("dispatchAsync drops events when closed")
        void dispatchAsyncDropsEventsWhenClosed() {
            dispatcher.close();

            CompletableFuture<Void> future = dispatcher.dispatchAsync(CacheEvent.put("cache", "key", "value"));
            future.join(); // Should complete immediately without error
        }
    }

    @Nested
    @DisplayName("Batch Dispatch Tests")
    class BatchDispatchTests {

        @Test
        @DisplayName("dispatchAll dispatches multiple events")
        void dispatchAllDispatchesMultipleEvents() {
            List<CacheEvent<String, String>> received = new ArrayList<>();
            dispatcher.addListener(received::add);

            List<CacheEvent<String, String>> events = List.of(
                    CacheEvent.put("cache", "key1", "value1"),
                    CacheEvent.put("cache", "key2", "value2"),
                    CacheEvent.put("cache", "key3", "value3")
            );
            dispatcher.dispatchAll(events);

            assertEquals(3, received.size());
        }

        @Test
        @DisplayName("dispatchAllAsync dispatches multiple events asynchronously")
        void dispatchAllAsyncDispatchesMultipleEventsAsynchronously() throws Exception {
            CountDownLatch latch = new CountDownLatch(3);
            dispatcher.addListener(event -> latch.countDown());

            List<CacheEvent<String, String>> events = List.of(
                    CacheEvent.put("cache", "key1", "value1"),
                    CacheEvent.put("cache", "key2", "value2"),
                    CacheEvent.put("cache", "key3", "value3")
            );
            dispatcher.dispatchAllAsync(events).join();

            assertTrue(latch.await(5, TimeUnit.SECONDS));
        }
    }

    @Nested
    @DisplayName("Dispatch With Timeout Tests")
    class DispatchWithTimeoutTests {

        @Test
        @DisplayName("dispatchWithTimeout returns true on success")
        void dispatchWithTimeoutReturnsTrueOnSuccess() {
            dispatcher.addListener(event -> {});

            boolean result = dispatcher.dispatchWithTimeout(
                    CacheEvent.put("cache", "key", "value"),
                    Duration.ofSeconds(5)
            );

            assertTrue(result);
        }

        @Test
        @DisplayName("dispatchWithTimeout returns false on timeout")
        void dispatchWithTimeoutReturnsFalseOnTimeout() {
            dispatcher.addListener(event -> {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            boolean result = dispatcher.dispatchWithTimeout(
                    CacheEvent.put("cache", "key", "value"),
                    Duration.ofMillis(50)
            );

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("Dispatch To Specific Listener Tests")
    class DispatchToSpecificListenerTests {

        @Test
        @DisplayName("dispatchTo invokes specific listener")
        void dispatchToInvokesSpecificListener() {
            AtomicBoolean invoked = new AtomicBoolean(false);
            CacheEventListener<String, String> targetListener = event -> invoked.set(true);

            dispatcher.dispatchTo(
                    CacheEvent.put("cache", "key", "value"),
                    targetListener
            );

            assertTrue(invoked.get());
        }

        @Test
        @DisplayName("dispatchTo throws on null event")
        void dispatchToThrowsOnNullEvent() {
            assertThrows(NullPointerException.class,
                    () -> dispatcher.dispatchTo(null, event -> {}));
        }

        @Test
        @DisplayName("dispatchTo throws on null listener")
        void dispatchToThrowsOnNullListener() {
            assertThrows(NullPointerException.class,
                    () -> dispatcher.dispatchTo(CacheEvent.put("cache", "key", "value"), null));
        }

        @Test
        @DisplayName("dispatchTo respects listener interest")
        void dispatchToRespectsListenerInterest() {
            AtomicBoolean invoked = new AtomicBoolean(false);
            CacheEventListener<String, String> putListener = CacheEventListener.onPut(event -> invoked.set(true));

            dispatcher.dispatchTo(CacheEvent.getHit("cache", "key", "value"), putListener);

            assertFalse(invoked.get());
        }

        @Test
        @DisplayName("dispatchTo drops events when closed")
        void dispatchToDropsEventsWhenClosed() {
            AtomicBoolean invoked = new AtomicBoolean(false);
            dispatcher.close();

            dispatcher.dispatchTo(
                    CacheEvent.put("cache", "key", "value"),
                    event -> invoked.set(true)
            );

            assertFalse(invoked.get());
        }
    }

    @Nested
    @DisplayName("Has Listeners For Tests")
    class HasListenersForTests {

        @Test
        @DisplayName("hasListenersFor returns true when has interested listener")
        void hasListenersForReturnsTrueWhenHasInterestedListener() {
            dispatcher.addListener(CacheEventListener.onPut(event -> {}));

            assertTrue(dispatcher.hasListenersFor(CacheEvent.EventType.PUT));
        }

        @Test
        @DisplayName("hasListenersFor returns false when no interested listener")
        void hasListenersForReturnsFalseWhenNoInterestedListener() {
            dispatcher.addListener(CacheEventListener.onPut(event -> {}));

            assertFalse(dispatcher.hasListenersFor(CacheEvent.EventType.GET));
        }

        @Test
        @DisplayName("hasListenersFor returns false when no listeners")
        void hasListenersForReturnsFalseWhenNoListeners() {
            assertFalse(dispatcher.hasListenersFor(CacheEvent.EventType.PUT));
        }
    }

    @Nested
    @DisplayName("Metrics Tests")
    class MetricsTests {

        @Test
        @DisplayName("getMetrics returns metrics")
        void getMetricsReturnsMetrics() {
            CacheEventDispatcher.Metrics metrics = dispatcher.getMetrics();
            assertNotNull(metrics);
            assertEquals(0, metrics.listenerCount());
            assertEquals(0, metrics.eventsDispatched());
            assertEquals(0, metrics.eventsDropped());
            assertEquals(0, metrics.errors());
        }

        @Test
        @DisplayName("metrics tracks events dispatched")
        void metricsTracksEventsDispatched() {
            dispatcher.addListener(event -> {});
            dispatcher.dispatch(CacheEvent.put("cache", "key", "value"));
            dispatcher.dispatch(CacheEvent.put("cache", "key", "value"));

            CacheEventDispatcher.Metrics metrics = dispatcher.getMetrics();
            assertEquals(2, metrics.eventsDispatched());
        }

        @Test
        @DisplayName("metrics tracks events dropped")
        void metricsTracksEventsDropped() {
            dispatcher.close();
            dispatcher.dispatch(CacheEvent.put("cache", "key", "value"));

            CacheEventDispatcher.Metrics metrics = dispatcher.getMetrics();
            assertEquals(1, metrics.eventsDropped());
        }

        @Test
        @DisplayName("metrics tracks errors")
        void metricsTracksErrors() {
            dispatcher.addListener(event -> {
                throw new RuntimeException("test error");
            });
            dispatcher.dispatch(CacheEvent.put("cache", "key", "value"));

            CacheEventDispatcher.Metrics metrics = dispatcher.getMetrics();
            assertEquals(1, metrics.errors());
        }

        @Test
        @DisplayName("metrics errorRate calculation")
        void metricsErrorRateCalculation() {
            CacheEventDispatcher.Metrics metrics = new CacheEventDispatcher.Metrics(0, 10, 0, 5);
            assertEquals(0.5, metrics.errorRate());
        }

        @Test
        @DisplayName("metrics errorRate when no events")
        void metricsErrorRateWhenNoEvents() {
            CacheEventDispatcher.Metrics metrics = new CacheEventDispatcher.Metrics(0, 0, 0, 0);
            assertEquals(0.0, metrics.errorRate());
        }

        @Test
        @DisplayName("resetMetrics clears metrics")
        void resetMetricsClearsMetrics() {
            dispatcher.addListener(event -> {});
            dispatcher.dispatch(CacheEvent.put("cache", "key", "value"));

            dispatcher.resetMetrics();

            CacheEventDispatcher.Metrics metrics = dispatcher.getMetrics();
            assertEquals(0, metrics.eventsDispatched());
        }
    }

    @Nested
    @DisplayName("Error Handler Tests")
    class ErrorHandlerTests {

        @Test
        @DisplayName("default error handler logs and continues")
        void defaultErrorHandlerLogsAndContinues() {
            List<CacheEvent<String, String>> received = new ArrayList<>();
            dispatcher.addListener(event -> {
                throw new RuntimeException("test error");
            });
            dispatcher.addListener(received::add);

            // Should not throw and second listener should still be called
            dispatcher.dispatch(CacheEvent.put("cache", "key", "value"));

            assertEquals(1, received.size());
        }

        @Test
        @DisplayName("logAndContinue handler does not throw")
        void logAndContinueHandlerDoesNotThrow() {
            CacheEventDispatcher<String, String> d = CacheEventDispatcher.<String, String>builder()
                    .errorHandler(CacheEventDispatcher.EventErrorHandler.logAndContinue())
                    .build();

            d.addListener(event -> {
                throw new RuntimeException("test");
            });

            assertDoesNotThrow(() -> d.dispatch(CacheEvent.put("cache", "key", "value")));
            d.close();
        }

        @Test
        @DisplayName("rethrow handler throws RuntimeException")
        void rethrowHandlerThrowsRuntimeException() {
            CacheEventDispatcher<String, String> d = CacheEventDispatcher.<String, String>builder()
                    .errorHandler(CacheEventDispatcher.EventErrorHandler.rethrow())
                    .build();

            d.addListener(event -> {
                throw new RuntimeException("test error");
            });

            assertThrows(RuntimeException.class, () ->
                    d.dispatch(CacheEvent.put("cache", "key", "value")));
            d.close();
        }

        @Test
        @DisplayName("rethrow handler wraps checked exception")
        void rethrowHandlerWrapsCheckedException() {
            CacheEventDispatcher.EventErrorHandler<String, String> handler =
                    CacheEventDispatcher.EventErrorHandler.rethrow();

            Exception checkedException = new Exception("checked");
            RuntimeException thrown = assertThrows(RuntimeException.class, () ->
                    handler.handleError(
                            CacheEvent.put("cache", "key", "value"),
                            event -> {},
                            checkedException
                    ));

            assertEquals(checkedException, thrown.getCause());
        }

        @Test
        @DisplayName("ignore handler silently ignores errors")
        void ignoreHandlerSilentlyIgnoresErrors() {
            CacheEventDispatcher<String, String> d = CacheEventDispatcher.<String, String>builder()
                    .errorHandler(CacheEventDispatcher.EventErrorHandler.ignore())
                    .build();

            d.addListener(event -> {
                throw new RuntimeException("test");
            });

            assertDoesNotThrow(() -> d.dispatch(CacheEvent.put("cache", "key", "value")));
            d.close();
        }
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("builder with custom executor")
        void builderWithCustomExecutor() {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            CacheEventDispatcher<String, String> d = CacheEventDispatcher.<String, String>builder()
                    .asyncExecutor(executor)
                    .build();

            assertNotNull(d);
            d.close();
        }

        @Test
        @DisplayName("builder with custom error handler")
        void builderWithCustomErrorHandler() {
            AtomicInteger errorCount = new AtomicInteger(0);
            CacheEventDispatcher<String, String> d = CacheEventDispatcher.<String, String>builder()
                    .errorHandler((event, listener, error) -> errorCount.incrementAndGet())
                    .build();

            d.addListener(event -> {
                throw new RuntimeException("test");
            });
            d.dispatch(CacheEvent.put("cache", "key", "value"));

            assertEquals(1, errorCount.get());
            d.close();
        }
    }

    @Nested
    @DisplayName("Lifecycle Tests")
    class LifecycleTests {

        @Test
        @DisplayName("isClosed returns false initially")
        void isClosedReturnsFalseInitially() {
            assertFalse(dispatcher.isClosed());
        }

        @Test
        @DisplayName("isClosed returns true after close")
        void isClosedReturnsTrueAfterClose() {
            dispatcher.close();
            assertTrue(dispatcher.isClosed());
        }

        @Test
        @DisplayName("close is idempotent")
        void closeIsIdempotent() {
            dispatcher.close();
            assertDoesNotThrow(() -> dispatcher.close());
        }

        @Test
        @DisplayName("close shuts down executor")
        void closeShutdownsExecutor() throws Exception {
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch endLatch = new CountDownLatch(1);

            dispatcher.addListener(event -> {
                startLatch.countDown();
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                endLatch.countDown();
            });

            dispatcher.dispatchAsync(CacheEvent.put("cache", "key", "value"));
            startLatch.await(5, TimeUnit.SECONDS);

            dispatcher.close();

            assertTrue(dispatcher.isClosed());
        }
    }
}
