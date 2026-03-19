package cloud.opencode.base.cache.protection;

import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * SingleFlight Test
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
class SingleFlightTest {

    @Test
    void shouldExecuteLoader() {
        SingleFlight<String, String> flight = new SingleFlight<>();

        String result = flight.execute("key", k -> "value-" + k);

        assertThat(result).isEqualTo("value-key");
    }

    @Test
    void shouldMergeMultipleConcurrentRequests() throws Exception {
        SingleFlight<String, String> flight = new SingleFlight<>();
        AtomicInteger loadCount = new AtomicInteger(0);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(5);

        ExecutorService executor = Executors.newFixedThreadPool(5);
        try {
            for (int i = 0; i < 5; i++) {
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        flight.execute("key", k -> {
                            loadCount.incrementAndGet();
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                            return "value";
                        });
                    } catch (Exception e) {
                        // ignore
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            startLatch.countDown();
            doneLatch.await(5, TimeUnit.SECONDS);

            // Should only load once despite 5 concurrent requests
            assertThat(loadCount.get()).isEqualTo(1);
        } finally {
            executor.shutdown();
        }
    }

    @Test
    void shouldExecuteDifferentKeysIndependently() {
        SingleFlight<String, String> flight = new SingleFlight<>();
        AtomicInteger loadCount = new AtomicInteger(0);

        flight.execute("key1", k -> {
            loadCount.incrementAndGet();
            return "value1";
        });

        flight.execute("key2", k -> {
            loadCount.incrementAndGet();
            return "value2";
        });

        assertThat(loadCount.get()).isEqualTo(2);
    }

    @Test
    void shouldPropagateException() {
        SingleFlight<String, String> flight = new SingleFlight<>();

        assertThatThrownBy(() ->
                flight.execute("key", k -> {
                    throw new RuntimeException("Test error");
                })
        ).isInstanceOf(RuntimeException.class)
                .hasMessage("Test error");
    }

    @Test
    void shouldPropagateCheckedException() {
        SingleFlight<String, String> flight = new SingleFlight<>();

        assertThatThrownBy(() ->
                flight.execute("key", k -> {
                    throw new RuntimeException(new Exception("Checked exception"));
                })
        ).isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldExecuteWithTimeout() throws Exception {
        SingleFlight<String, String> flight = new SingleFlight<>();

        String result = flight.execute("key", k -> "value", Duration.ofSeconds(1));

        assertThat(result).isEqualTo("value");
    }

    @Test
    void shouldTimeoutWhenLoaderTakesTooLong() {
        SingleFlight<String, String> flight = new SingleFlight<>();

        assertThatThrownBy(() ->
                flight.execute("key", k -> {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return "value";
                }, Duration.ofMillis(100))
        ).isInstanceOf(SingleFlight.TimeoutException.class)
                .hasMessageContaining("timed out");
    }

    @Test
    void shouldExecuteAsync() throws Exception {
        SingleFlight<String, String> flight = new SingleFlight<>();

        CompletableFuture<String> future = flight.executeAsync("key",
                k -> CompletableFuture.supplyAsync(() -> "value-" + k));

        assertThat(future.get(1, TimeUnit.SECONDS)).isEqualTo("value-key");
    }

    @Test
    void shouldMergeAsyncRequests() throws Exception {
        SingleFlight<String, String> flight = new SingleFlight<>();
        AtomicInteger loadCount = new AtomicInteger(0);

        CompletableFuture<String> f1 = flight.executeAsync("key",
                k -> CompletableFuture.supplyAsync(() -> {
                    loadCount.incrementAndGet();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return "value";
                }));

        CompletableFuture<String> f2 = flight.executeAsync("key",
                k -> CompletableFuture.supplyAsync(() -> {
                    loadCount.incrementAndGet();
                    return "value2";
                }));

        f1.get(5, TimeUnit.SECONDS);
        f2.get(5, TimeUnit.SECONDS);

        // Both futures share the same result from first loader
        assertThat(loadCount.get()).isEqualTo(1);
    }

    @Test
    void shouldTrackInflightCount() throws Exception {
        SingleFlight<String, String> flight = new SingleFlight<>();
        CountDownLatch insideLatch = new CountDownLatch(1);
        CountDownLatch continueLatch = new CountDownLatch(1);

        CompletableFuture.runAsync(() -> {
            flight.execute("key", k -> {
                insideLatch.countDown();
                try {
                    continueLatch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return "value";
            });
        });

        insideLatch.await(1, TimeUnit.SECONDS);
        assertThat(flight.inflightCount()).isEqualTo(1);

        continueLatch.countDown();
        Thread.sleep(100);
        assertThat(flight.inflightCount()).isEqualTo(0);
    }

    @Test
    void shouldCancelInflight() throws Exception {
        SingleFlight<String, String> flight = new SingleFlight<>();
        CountDownLatch insideLatch = new CountDownLatch(1);

        CompletableFuture.runAsync(() -> {
            try {
                flight.execute("key", k -> {
                    insideLatch.countDown();
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return "value";
                });
            } catch (Exception e) {
                // Expected
            }
        });

        insideLatch.await(1, TimeUnit.SECONDS);
        assertThat(flight.inflightCount()).isEqualTo(1);

        flight.cancel("key");
        Thread.sleep(100);
        assertThat(flight.inflightCount()).isEqualTo(0);
    }

    @Test
    void shouldCancelAllInflight() throws Exception {
        SingleFlight<String, String> flight = new SingleFlight<>();
        CountDownLatch insideLatch = new CountDownLatch(2);
        CountDownLatch startedLatch = new CountDownLatch(2);

        for (String key : new String[]{"key1", "key2"}) {
            CompletableFuture.runAsync(() -> {
                startedLatch.countDown();
                try {
                    flight.execute(key, k -> {
                        insideLatch.countDown();
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        return "value";
                    });
                } catch (Exception e) {
                    // Expected
                }
            });
        }

        // Wait for both tasks to enter the loader
        boolean allStarted = insideLatch.await(2, TimeUnit.SECONDS);
        assertThat(allStarted).isTrue();
        assertThat(flight.inflightCount()).isGreaterThanOrEqualTo(1);

        flight.cancelAll();
        Thread.sleep(100);
        assertThat(flight.inflightCount()).isEqualTo(0);
    }
}
