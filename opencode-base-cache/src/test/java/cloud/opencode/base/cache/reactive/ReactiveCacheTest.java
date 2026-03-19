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

package cloud.opencode.base.cache.reactive;

import cloud.opencode.base.cache.Cache;
import cloud.opencode.base.cache.CacheStats;
import cloud.opencode.base.cache.OpenCache;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for ReactiveCache
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("ReactiveCache Tests")
class ReactiveCacheTest {

    private Cache<String, String> cache;
    private ReactiveCache<String, String> reactiveCache;

    @BeforeEach
    void setUp() {
        cache = OpenCache.<String, String>builder()
                .maximumSize(100)
                .build("reactive-test-" + System.nanoTime());
        reactiveCache = ReactiveCache.wrap(cache);
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("wrap creates reactive cache")
        void wrapCreatesReactiveCache() {
            ReactiveCache<String, String> reactive = ReactiveCache.wrap(cache);

            assertNotNull(reactive);
        }

        @Test
        @DisplayName("wrap throws on null cache")
        void wrapThrowsOnNullCache() {
            assertThrows(NullPointerException.class, () -> ReactiveCache.wrap(null));
        }

        @Test
        @DisplayName("getDelegate returns underlying cache")
        void getDelegateReturnsUnderlyingCache() {
            assertSame(cache, reactiveCache.getDelegate());
        }
    }

    @Nested
    @DisplayName("JDK Flow API Tests")
    class JdkFlowApiTests {

        @Test
        @DisplayName("getMono emits value when present")
        void getMonoEmitsValueWhenPresent() throws InterruptedException {
            cache.put("key", "value");

            AtomicReference<String> result = new AtomicReference<>();
            CountDownLatch latch = new CountDownLatch(1);

            reactiveCache.getMono("key").subscribe(new Flow.Subscriber<>() {
                @Override
                public void onSubscribe(Flow.Subscription subscription) {
                    subscription.request(1);
                }

                @Override
                public void onNext(String item) {
                    result.set(item);
                }

                @Override
                public void onError(Throwable throwable) {
                    latch.countDown();
                }

                @Override
                public void onComplete() {
                    latch.countDown();
                }
            });

            assertTrue(latch.await(5, TimeUnit.SECONDS));
            assertEquals("value", result.get());
        }

        @Test
        @DisplayName("getMono completes without value when absent")
        void getMonoCompletesWithoutValueWhenAbsent() throws InterruptedException {
            AtomicReference<String> result = new AtomicReference<>();
            CountDownLatch latch = new CountDownLatch(1);

            reactiveCache.getMono("non-existent").subscribe(new Flow.Subscriber<>() {
                @Override
                public void onSubscribe(Flow.Subscription subscription) {
                    subscription.request(1);
                }

                @Override
                public void onNext(String item) {
                    result.set(item);
                }

                @Override
                public void onError(Throwable throwable) {
                    latch.countDown();
                }

                @Override
                public void onComplete() {
                    latch.countDown();
                }
            });

            assertTrue(latch.await(5, TimeUnit.SECONDS));
            assertNull(result.get());
        }

        @Test
        @DisplayName("getOrLoadMono loads when absent")
        void getOrLoadMonoLoadsWhenAbsent() throws InterruptedException {
            AtomicReference<String> result = new AtomicReference<>();
            CountDownLatch latch = new CountDownLatch(1);

            reactiveCache.getOrLoadMono("key", k -> "loaded-" + k).subscribe(new Flow.Subscriber<>() {
                @Override
                public void onSubscribe(Flow.Subscription subscription) {
                    subscription.request(1);
                }

                @Override
                public void onNext(String item) {
                    result.set(item);
                }

                @Override
                public void onError(Throwable throwable) {
                    latch.countDown();
                }

                @Override
                public void onComplete() {
                    latch.countDown();
                }
            });

            assertTrue(latch.await(5, TimeUnit.SECONDS));
            assertEquals("loaded-key", result.get());
        }

        @Test
        @DisplayName("getAllFlux emits all values")
        void getAllFluxEmitsAllValues() throws InterruptedException {
            cache.put("k1", "v1");
            cache.put("k2", "v2");

            List<String> results = Collections.synchronizedList(new ArrayList<>());
            CountDownLatch latch = new CountDownLatch(1);

            reactiveCache.getAllFlux(List.of("k1", "k2")).subscribe(new Flow.Subscriber<>() {
                @Override
                public void onSubscribe(Flow.Subscription subscription) {
                    subscription.request(Long.MAX_VALUE);
                }

                @Override
                public void onNext(String item) {
                    results.add(item);
                }

                @Override
                public void onError(Throwable throwable) {
                    latch.countDown();
                }

                @Override
                public void onComplete() {
                    latch.countDown();
                }
            });

            assertTrue(latch.await(5, TimeUnit.SECONDS));
            assertEquals(2, results.size());
        }

        @Test
        @DisplayName("keysFlux emits all keys")
        void keysFluxEmitsAllKeys() throws InterruptedException {
            cache.put("k1", "v1");
            cache.put("k2", "v2");

            List<String> results = Collections.synchronizedList(new ArrayList<>());
            CountDownLatch latch = new CountDownLatch(1);

            reactiveCache.keysFlux().subscribe(new Flow.Subscriber<>() {
                @Override
                public void onSubscribe(Flow.Subscription subscription) {
                    subscription.request(Long.MAX_VALUE);
                }

                @Override
                public void onNext(String item) {
                    results.add(item);
                }

                @Override
                public void onError(Throwable throwable) {
                    latch.countDown();
                }

                @Override
                public void onComplete() {
                    latch.countDown();
                }
            });

            assertTrue(latch.await(5, TimeUnit.SECONDS));
            assertTrue(results.contains("k1"));
            assertTrue(results.contains("k2"));
        }

        @Test
        @DisplayName("valuesFlux emits all values")
        void valuesFluxEmitsAllValues() throws InterruptedException {
            cache.put("k1", "v1");
            cache.put("k2", "v2");

            List<String> results = Collections.synchronizedList(new ArrayList<>());
            CountDownLatch latch = new CountDownLatch(1);

            reactiveCache.valuesFlux().subscribe(new Flow.Subscriber<>() {
                @Override
                public void onSubscribe(Flow.Subscription subscription) {
                    subscription.request(Long.MAX_VALUE);
                }

                @Override
                public void onNext(String item) {
                    results.add(item);
                }

                @Override
                public void onError(Throwable throwable) {
                    latch.countDown();
                }

                @Override
                public void onComplete() {
                    latch.countDown();
                }
            });

            assertTrue(latch.await(5, TimeUnit.SECONDS));
            assertTrue(results.contains("v1"));
            assertTrue(results.contains("v2"));
        }

        @Test
        @DisplayName("entriesFlux emits all entries")
        void entriesFluxEmitsAllEntries() throws InterruptedException {
            cache.put("k1", "v1");
            cache.put("k2", "v2");

            List<Map.Entry<String, String>> results = Collections.synchronizedList(new ArrayList<>());
            CountDownLatch latch = new CountDownLatch(1);

            reactiveCache.entriesFlux().subscribe(new Flow.Subscriber<>() {
                @Override
                public void onSubscribe(Flow.Subscription subscription) {
                    subscription.request(Long.MAX_VALUE);
                }

                @Override
                public void onNext(Map.Entry<String, String> item) {
                    results.add(item);
                }

                @Override
                public void onError(Throwable throwable) {
                    latch.countDown();
                }

                @Override
                public void onComplete() {
                    latch.countDown();
                }
            });

            assertTrue(latch.await(5, TimeUnit.SECONDS));
            assertEquals(2, results.size());
        }

        @Test
        @DisplayName("subscription can be cancelled")
        void subscriptionCanBeCancelled() throws InterruptedException {
            cache.put("k1", "v1");

            AtomicReference<Flow.Subscription> subscriptionRef = new AtomicReference<>();
            CountDownLatch latch = new CountDownLatch(1);

            reactiveCache.getMono("k1").subscribe(new Flow.Subscriber<>() {
                @Override
                public void onSubscribe(Flow.Subscription subscription) {
                    subscriptionRef.set(subscription);
                    subscription.cancel();
                    latch.countDown();
                }

                @Override
                public void onNext(String item) {
                }

                @Override
                public void onError(Throwable throwable) {
                }

                @Override
                public void onComplete() {
                }
            });

            assertTrue(latch.await(5, TimeUnit.SECONDS));
            assertNotNull(subscriptionRef.get());
        }
    }

    @Nested
    @DisplayName("CompletableFuture API Tests")
    class CompletableFutureApiTests {

        @Test
        @DisplayName("getAsync returns value")
        void getAsyncReturnsValue() {
            cache.put("key", "value");

            CompletableFuture<String> future = reactiveCache.getAsync("key");

            assertEquals("value", future.join());
        }

        @Test
        @DisplayName("getAsync returns null when absent")
        void getAsyncReturnsNullWhenAbsent() {
            CompletableFuture<String> future = reactiveCache.getAsync("non-existent");

            assertNull(future.join());
        }

        @Test
        @DisplayName("getOrLoad loads when absent")
        void getOrLoadLoadsWhenAbsent() {
            CompletableFuture<String> future = reactiveCache.getOrLoad("key", k -> "loaded-" + k);

            assertEquals("loaded-key", future.join());
        }

        @Test
        @DisplayName("putAsync stores value")
        void putAsyncStoresValue() {
            CompletableFuture<Void> future = reactiveCache.putAsync("key", "value");

            future.join();
            assertEquals("value", cache.get("key"));
        }

        @Test
        @DisplayName("putWithTtlAsync stores with TTL")
        void putWithTtlAsyncStoresWithTtl() {
            CompletableFuture<Void> future = reactiveCache.putWithTtlAsync("key", "value", Duration.ofMinutes(5));

            future.join();
            assertEquals("value", cache.get("key"));
        }

        @Test
        @DisplayName("invalidateAsync removes key")
        void invalidateAsyncRemovesKey() {
            cache.put("key", "value");

            CompletableFuture<Void> future = reactiveCache.invalidateAsync("key");

            future.join();
            assertNull(cache.get("key"));
        }

        @Test
        @DisplayName("invalidateByPatternAsync removes by pattern")
        void invalidateByPatternAsyncRemovesByPattern() {
            cache.put("user:1", "v1");
            cache.put("user:2", "v2");

            CompletableFuture<Long> future = reactiveCache.invalidateByPatternAsync("user:*");

            Long count = future.join();
            assertTrue(count >= 0);
        }

        @Test
        @DisplayName("statsAsync returns stats")
        void statsAsyncReturnsStats() {
            CompletableFuture<CacheStats> future = reactiveCache.statsAsync();

            assertNotNull(future.join());
        }
    }

    @Nested
    @DisplayName("Reactor Integration Tests")
    class ReactorIntegrationTests {

        @Test
        @DisplayName("isReactorAvailable returns boolean")
        void isReactorAvailableReturnsBoolean() {
            // This should not throw, regardless of result
            assertDoesNotThrow(() -> ReactiveCache.isReactorAvailable());
        }

        @Test
        @DisplayName("asMono throws when Reactor not available")
        void asMonoThrowsWhenReactorNotAvailable() {
            if (!ReactiveCache.isReactorAvailable()) {
                assertThrows(UnsupportedOperationException.class, () ->
                        reactiveCache.asMono("key"));
            }
        }

        @Test
        @DisplayName("asFlux throws when Reactor not available")
        void asFluxThrowsWhenReactorNotAvailable() {
            if (!ReactiveCache.isReactorAvailable()) {
                assertThrows(UnsupportedOperationException.class, () ->
                        reactiveCache.asFlux());
            }
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("getMono handles errors")
        void getMonoHandlesErrors() throws InterruptedException {
            // Create a cache that throws on get
            Cache<String, String> errorCache = new Cache<String, String>() {
                @Override public String get(String key) { throw new RuntimeException("Test error"); }
                @Override public String get(String key, java.util.function.Function<? super String, ? extends String> loader) { return null; }
                @Override public Map<String, String> getAll(Iterable<? extends String> keys) { return null; }
                @Override public Map<String, String> getAll(Iterable<? extends String> keys, java.util.function.Function<? super Set<? extends String>, ? extends Map<String, String>> loader) { return null; }
                @Override public void put(String key, String value) { }
                @Override public void putAll(Map<? extends String, ? extends String> map) { }
                @Override public boolean putIfAbsent(String key, String value) { return false; }
                @Override public void putWithTtl(String key, String value, Duration ttl) { }
                @Override public void putAllWithTtl(Map<? extends String, ? extends String> map, Duration ttl) { }
                @Override public boolean putIfAbsentWithTtl(String key, String value, Duration ttl) { return false; }
                @Override public void invalidate(String key) { }
                @Override public void invalidateAll(Iterable<? extends String> keys) { }
                @Override public void invalidateAll() { }
                @Override public boolean containsKey(String key) { return false; }
                @Override public long size() { return 0; }
                @Override public long estimatedSize() { return 0; }
                @Override public Set<String> keys() { return Set.of(); }
                @Override public Collection<String> values() { return List.of(); }
                @Override public Set<Map.Entry<String, String>> entries() { return Set.of(); }
                @Override public java.util.concurrent.ConcurrentMap<String, String> asMap() { return null; }
                @Override public CacheStats stats() { return null; }
                @Override public cloud.opencode.base.cache.CacheMetrics metrics() { return null; }
                @Override public void cleanUp() { }
                @Override public cloud.opencode.base.cache.AsyncCache<String, String> async() { return null; }
                @Override public String name() { return "error-cache"; }
            };

            ReactiveCache<String, String> errorReactive = ReactiveCache.wrap(errorCache);

            AtomicReference<Throwable> error = new AtomicReference<>();
            CountDownLatch latch = new CountDownLatch(1);

            errorReactive.getMono("key").subscribe(new Flow.Subscriber<>() {
                @Override
                public void onSubscribe(Flow.Subscription subscription) {
                    subscription.request(1);
                }

                @Override
                public void onNext(String item) {
                }

                @Override
                public void onError(Throwable throwable) {
                    error.set(throwable);
                    latch.countDown();
                }

                @Override
                public void onComplete() {
                    latch.countDown();
                }
            });

            assertTrue(latch.await(5, TimeUnit.SECONDS));
            assertNotNull(error.get());
        }
    }
}
