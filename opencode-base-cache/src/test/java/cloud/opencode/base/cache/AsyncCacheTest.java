package cloud.opencode.base.cache;

import cloud.opencode.base.cache.config.CacheConfig;
import cloud.opencode.base.cache.internal.DefaultCache;
import org.junit.jupiter.api.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * AsyncCache Test
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
class AsyncCacheTest {

    private Cache<String, String> syncCache;
    private AsyncCache<String, String> asyncCache;

    @BeforeEach
    void setup() {
        CacheManager.getInstance().reset();
        syncCache = new DefaultCache<>("test", CacheConfig.defaultConfig());
        asyncCache = syncCache.async();
    }

    @Test
    void shouldGetAsync() throws Exception {
        syncCache.put("key", "value");

        CompletableFuture<String> future = asyncCache.getAsync("key");

        assertThat(future.get(1, TimeUnit.SECONDS)).isEqualTo("value");
    }

    @Test
    void shouldReturnNullForMissingAsync() throws Exception {
        CompletableFuture<String> future = asyncCache.getAsync("missing");

        assertThat(future.get(1, TimeUnit.SECONDS)).isNull();
    }

    @Test
    void shouldGetAsyncWithLoader() throws Exception {
        CompletableFuture<String> future = asyncCache.getAsync("key",
                (k, executor) -> CompletableFuture.supplyAsync(() -> "loaded-" + k, executor));

        String result = future.get(1, TimeUnit.SECONDS);

        assertThat(result).isEqualTo("loaded-key");
        assertThat(syncCache.get("key")).isEqualTo("loaded-key");
    }

    @Test
    void shouldNotReloadExistingAsync() throws Exception {
        syncCache.put("key", "existing");

        CompletableFuture<String> future = asyncCache.getAsync("key",
                (k, executor) -> CompletableFuture.supplyAsync(() -> "loaded-" + k, executor));

        assertThat(future.get(1, TimeUnit.SECONDS)).isEqualTo("existing");
    }

    @Test
    void shouldGetAllAsync() throws Exception {
        syncCache.put("k1", "v1");
        syncCache.put("k2", "v2");

        CompletableFuture<Map<String, String>> future = asyncCache.getAllAsync(java.util.List.of("k1", "k2", "k3"));

        Map<String, String> result = future.get(1, TimeUnit.SECONDS);

        assertThat(result).hasSize(2);
        assertThat(result).containsEntry("k1", "v1");
        assertThat(result).containsEntry("k2", "v2");
    }

    @Test
    void shouldPutAsync() throws Exception {
        CompletableFuture<Void> future = asyncCache.putAsync("key", "value");

        future.get(1, TimeUnit.SECONDS);

        assertThat(syncCache.get("key")).isEqualTo("value");
    }

    @Test
    void shouldPutAllAsync() throws Exception {
        Map<String, String> data = Map.of("k1", "v1", "k2", "v2", "k3", "v3");

        CompletableFuture<Void> future = asyncCache.putAllAsync(data);

        future.get(1, TimeUnit.SECONDS);

        assertThat(syncCache.get("k1")).isEqualTo("v1");
        assertThat(syncCache.get("k2")).isEqualTo("v2");
        assertThat(syncCache.get("k3")).isEqualTo("v3");
    }

    @Test
    void shouldInvalidateAsync() throws Exception {
        syncCache.put("key", "value");

        CompletableFuture<Void> future = asyncCache.invalidateAsync("key");

        future.get(1, TimeUnit.SECONDS);

        assertThat(syncCache.get("key")).isNull();
    }

    @Test
    void shouldInvalidateAllAsync() throws Exception {
        syncCache.put("k1", "v1");
        syncCache.put("k2", "v2");

        CompletableFuture<Void> future = asyncCache.invalidateAllAsync(java.util.List.of("k1", "k2"));

        future.get(1, TimeUnit.SECONDS);

        assertThat(syncCache.get("k1")).isNull();
        assertThat(syncCache.get("k2")).isNull();
    }

    @Test
    void shouldReturnSyncView() {
        assertThat(asyncCache.sync()).isEqualTo(syncCache);
    }

    @Test
    void shouldHandleConcurrentAsyncOperations() throws Exception {
        int count = 100;
        CompletableFuture<?>[] futures = new CompletableFuture[count];

        for (int i = 0; i < count; i++) {
            final int idx = i;
            futures[i] = asyncCache.putAsync("key" + idx, "value" + idx);
        }

        CompletableFuture.allOf(futures).get(5, TimeUnit.SECONDS);

        for (int i = 0; i < count; i++) {
            assertThat(syncCache.get("key" + i)).isEqualTo("value" + i);
        }
    }

    @Test
    void shouldChainAsyncOperations() throws Exception {
        CompletableFuture<String> result = asyncCache.putAsync("key", "value")
                .thenCompose(v -> asyncCache.getAsync("key"))
                .thenApply(v -> "got: " + v);

        assertThat(result.get(1, TimeUnit.SECONDS)).isEqualTo("got: value");
    }
}
