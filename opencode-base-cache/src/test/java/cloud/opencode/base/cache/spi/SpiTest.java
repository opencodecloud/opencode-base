package cloud.opencode.base.cache.spi;

import cloud.opencode.base.cache.CacheStats;
import cloud.opencode.base.cache.internal.stats.LongAdderStatsCounter;
import cloud.opencode.base.cache.model.RemovalCause;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * SPI Interface Tests
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
class SpiTest {

    // ==================== CacheLoader Tests ====================

    @Nested
    class CacheLoaderTest {

        @Test
        void shouldLoadSingleValue() throws Exception {
            CacheLoader<String, String> loader = key -> "value-" + key;

            String value = loader.load("test");

            assertThat(value).isEqualTo("value-test");
        }

        @Test
        void shouldLoadAllByDefault() throws Exception {
            CacheLoader<String, String> loader = key -> "value-" + key;

            Map<String, String> result = loader.loadAll(Set.of("a", "b", "c"));

            assertThat(result).containsEntry("a", "value-a");
            assertThat(result).containsEntry("b", "value-b");
            assertThat(result).containsEntry("c", "value-c");
        }

        @Test
        void shouldExcludeNullValuesInLoadAll() throws Exception {
            CacheLoader<String, String> loader = key -> key.equals("b") ? null : "value-" + key;

            Map<String, String> result = loader.loadAll(Set.of("a", "b", "c"));

            assertThat(result).hasSize(2);
            assertThat(result).doesNotContainKey("b");
        }

        @Test
        void shouldReloadByDefault() throws Exception {
            CacheLoader<String, String> loader = key -> "new-value-" + key;

            String value = loader.reload("test", "old-value");

            assertThat(value).isEqualTo("new-value-test");
        }

        @Test
        void shouldBeFunction() throws Exception {
            CacheLoader<String, Integer> loader = Integer::parseInt;

            assertThat(loader.load("42")).isEqualTo(42);
        }
    }

    // ==================== AsyncCacheLoader Tests ====================

    @Nested
    class AsyncCacheLoaderTest {

        @Test
        void shouldAsyncLoadSingleValue() throws Exception {
            var executor = Executors.newSingleThreadExecutor();
            try {
                AsyncCacheLoader<String, String> loader = (key, exec) ->
                        CompletableFuture.supplyAsync(() -> "value-" + key, exec);

                String value = loader.asyncLoad("test", executor).get(1, TimeUnit.SECONDS);

                assertThat(value).isEqualTo("value-test");
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void shouldAsyncLoadAll() throws Exception {
            var executor = Executors.newFixedThreadPool(4);
            try {
                AsyncCacheLoader<String, String> loader = (key, exec) ->
                        CompletableFuture.supplyAsync(() -> "value-" + key, exec);

                Map<String, String> result = loader.asyncLoadAll(Set.of("a", "b", "c"), executor)
                        .get(1, TimeUnit.SECONDS);

                assertThat(result).hasSize(3);
                assertThat(result).containsEntry("a", "value-a");
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void shouldAsyncReload() throws Exception {
            var executor = Executors.newSingleThreadExecutor();
            try {
                AsyncCacheLoader<String, String> loader = (key, exec) ->
                        CompletableFuture.supplyAsync(() -> "reloaded-" + key, exec);

                String value = loader.asyncReload("test", "old", executor).get(1, TimeUnit.SECONDS);

                assertThat(value).isEqualTo("reloaded-test");
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void shouldCreateFromSyncLoader() throws Exception {
            var executor = Executors.newSingleThreadExecutor();
            try {
                CacheLoader<String, String> syncLoader = key -> "sync-" + key;
                AsyncCacheLoader<String, String> asyncLoader = AsyncCacheLoader.from(syncLoader);

                String value = asyncLoader.asyncLoad("test", executor).get(1, TimeUnit.SECONDS);

                assertThat(value).isEqualTo("sync-test");
            } finally {
                executor.shutdown();
            }
        }
    }

    // ==================== RemovalListener Tests ====================

    @Nested
    class RemovalListenerTest {

        @Test
        void shouldReceiveRemovalNotification() {
            AtomicBoolean called = new AtomicBoolean(false);
            RemovalListener<String, String> listener = (key, value, cause) -> called.set(true);

            listener.onRemoval("key", "value", RemovalCause.EXPLICIT);

            assertThat(called.get()).isTrue();
        }

        @Test
        void shouldReceiveRemovalDetails() {
            var capturedKey = new AtomicReference<String>();
            var capturedValue = new AtomicReference<String>();
            var capturedCause = new AtomicReference<RemovalCause>();

            RemovalListener<String, String> listener = (key, value, cause) -> {
                capturedKey.set(key);
                capturedValue.set(value);
                capturedCause.set(cause);
            };

            listener.onRemoval("myKey", "myValue", RemovalCause.EXPIRED);

            assertThat(capturedKey.get()).isEqualTo("myKey");
            assertThat(capturedValue.get()).isEqualTo("myValue");
            assertThat(capturedCause.get()).isEqualTo(RemovalCause.EXPIRED);
        }

        @Test
        void shouldCreateNoOpListener() {
            RemovalListener<String, String> listener = RemovalListener.noOp();

            // Should not throw
            listener.onRemoval("key", "value", RemovalCause.EXPLICIT);
        }

        @Test
        void shouldCreateLoggingListener() {
            RemovalListener<String, String> listener = RemovalListener.logging();

            // Should not throw
            listener.onRemoval("key", "value", RemovalCause.SIZE);
        }

        @Test
        void shouldCombineListeners() {
            AtomicInteger count = new AtomicInteger(0);
            RemovalListener<String, String> l1 = (k, v, c) -> count.incrementAndGet();
            RemovalListener<String, String> l2 = (k, v, c) -> count.incrementAndGet();
            RemovalListener<String, String> l3 = (k, v, c) -> count.incrementAndGet();

            RemovalListener<String, String> combined = RemovalListener.combine(l1, l2, l3);

            combined.onRemoval("key", "value", RemovalCause.EXPLICIT);

            assertThat(count.get()).isEqualTo(3);
        }
    }

    // ==================== StatsCounter Tests ====================

    @Nested
    class StatsCounterTest {

        @Test
        void shouldCreateDisabledCounter() {
            StatsCounter counter = StatsCounter.disabled();

            counter.recordHits(10);
            counter.recordMisses(5);

            CacheStats stats = counter.snapshot();
            assertThat(stats.hitCount()).isEqualTo(0);
            assertThat(stats.missCount()).isEqualTo(0);
        }

        @Test
        void shouldCreateConcurrentCounter() {
            StatsCounter counter = StatsCounter.concurrent();
            assertThat(counter).isInstanceOf(LongAdderStatsCounter.class);
        }

        @Test
        void shouldRecordHits() {
            StatsCounter counter = StatsCounter.concurrent();

            counter.recordHits(5);
            counter.recordHits(3);

            assertThat(counter.snapshot().hitCount()).isEqualTo(8);
        }

        @Test
        void shouldRecordMisses() {
            StatsCounter counter = StatsCounter.concurrent();

            counter.recordMisses(3);
            counter.recordMisses(2);

            assertThat(counter.snapshot().missCount()).isEqualTo(5);
        }

        @Test
        void shouldRecordLoadSuccess() {
            StatsCounter counter = StatsCounter.concurrent();

            counter.recordLoadSuccess(1000000);
            counter.recordLoadSuccess(2000000);

            CacheStats stats = counter.snapshot();
            assertThat(stats.loadSuccessCount()).isEqualTo(2);
            assertThat(stats.totalLoadTime()).isEqualTo(3000000);
        }

        @Test
        void shouldRecordLoadFailure() {
            StatsCounter counter = StatsCounter.concurrent();

            counter.recordLoadFailure(500000);

            CacheStats stats = counter.snapshot();
            assertThat(stats.loadFailureCount()).isEqualTo(1);
        }

        @Test
        void shouldRecordEviction() {
            StatsCounter counter = StatsCounter.concurrent();

            counter.recordEviction(1);
            counter.recordEviction(2);

            CacheStats stats = counter.snapshot();
            assertThat(stats.evictionCount()).isEqualTo(2);
            assertThat(stats.evictionWeight()).isEqualTo(3);
        }
    }

    // ==================== CacheSerializer Tests ====================

    @Nested
    class CacheSerializerTest {

        @Test
        void shouldSerializeWithJdk() {
            CacheSerializer<String> serializer = CacheSerializer.jdk();

            byte[] bytes = serializer.serialize("test");
            String result = serializer.deserialize(bytes);

            assertThat(result).isEqualTo("test");
        }

        @Test
        void shouldSerializeString() {
            CacheSerializer<String> serializer = CacheSerializer.string();

            byte[] bytes = serializer.serialize("hello");
            String result = serializer.deserialize(bytes);

            assertThat(result).isEqualTo("hello");
        }

        @Test
        void shouldSerializeIdentity() {
            CacheSerializer<byte[]> serializer = CacheSerializer.identity();

            byte[] original = {1, 2, 3, 4, 5};
            byte[] serialized = serializer.serialize(original);
            byte[] deserialized = serializer.deserialize(serialized);

            assertThat(deserialized).isEqualTo(original);
        }

        @Test
        void shouldEstimateSize() {
            CacheSerializer<String> serializer = CacheSerializer.string();

            long size = serializer.estimateSize("hello");

            assertThat(size).isEqualTo(5);
        }
    }

    // ==================== CacheWarmer Tests ====================

    @Nested
    class CacheWarmerTest {

        @Test
        void shouldWarmUp() {
            CacheWarmer<String, String> warmer = () -> Map.of("k1", "v1", "k2", "v2");

            Map<String, String> data = warmer.warmUp();

            assertThat(data).containsEntry("k1", "v1");
            assertThat(data).containsEntry("k2", "v2");
        }

        @Test
        void shouldWarmUpAsync() throws Exception {
            var executor = Executors.newSingleThreadExecutor();
            try {
                CacheWarmer<String, String> warmer = () -> Map.of("k1", "v1");

                Map<String, String> data = warmer.warmUpAsync(executor).get(1, TimeUnit.SECONDS);

                assertThat(data).containsEntry("k1", "v1");
            } finally {
                executor.shutdown();
            }
        }

        @Test
        void shouldReturnDefaultPriority() {
            CacheWarmer<String, String> warmer = () -> Map.of();

            assertThat(warmer.priority()).isEqualTo(0);
        }

        @Test
        void shouldBeEnabledByDefault() {
            CacheWarmer<String, String> warmer = () -> Map.of();

            assertThat(warmer.isEnabled()).isTrue();
        }

        @Test
        void shouldCreateFromSupplier() {
            CacheWarmer<String, String> warmer = CacheWarmer.from(() -> Map.of("k", "v"));

            assertThat(warmer.warmUp()).containsEntry("k", "v");
        }

        @Test
        void shouldCreatePagedWarmer() {
            AtomicInteger pageCount = new AtomicInteger(0);
            CacheWarmer<Integer, String> warmer = CacheWarmer.paged(
                    offset -> {
                        int page = pageCount.incrementAndGet();
                        if (page > 2) {
                            return Map.of();
                        }
                        return Map.of(offset, "value-" + offset);
                    },
                    10,
                    5
            );

            Map<Integer, String> data = warmer.warmUp();

            assertThat(data).hasSize(2);
            assertThat(pageCount.get()).isEqualTo(3); // 2 pages with data + 1 empty
        }

        @Test
        void shouldCreateEmptyWarmer() {
            CacheWarmer<String, String> warmer = CacheWarmer.empty();

            assertThat(warmer.warmUp()).isEmpty();
        }

        @Test
        void shouldCallOnComplete() {
            AtomicBoolean completed = new AtomicBoolean(false);
            AtomicInteger loadedCount = new AtomicInteger(-1);

            CacheWarmer<String, String> warmer = new CacheWarmer<>() {
                @Override
                public Map<String, String> warmUp() {
                    return Map.of("k", "v");
                }

                @Override
                public void onComplete(int count, java.time.Duration duration) {
                    completed.set(true);
                    loadedCount.set(count);
                }
            };

            warmer.onComplete(10, java.time.Duration.ofSeconds(1));

            assertThat(completed.get()).isTrue();
            assertThat(loadedCount.get()).isEqualTo(10);
        }

        @Test
        void shouldCallOnError() {
            AtomicBoolean errorCalled = new AtomicBoolean(false);

            CacheWarmer<String, String> warmer = new CacheWarmer<>() {
                @Override
                public Map<String, String> warmUp() {
                    return Map.of();
                }

                @Override
                public void onError(Throwable cause) {
                    errorCalled.set(true);
                }
            };

            warmer.onError(new RuntimeException("Test"));

            assertThat(errorCalled.get()).isTrue();
        }
    }

    private static class AtomicReference<T> {
        private volatile T value;

        T get() { return value; }
        void set(T value) { this.value = value; }
    }
}
