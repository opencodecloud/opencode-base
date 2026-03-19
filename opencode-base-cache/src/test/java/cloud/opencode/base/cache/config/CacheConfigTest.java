package cloud.opencode.base.cache.config;

import cloud.opencode.base.cache.model.RemovalCause;
import cloud.opencode.base.cache.spi.*;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.*;

/**
 * CacheConfig Test
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
class CacheConfigTest {

    @Test
    void shouldCreateDefaultConfig() {
        CacheConfig<String, String> config = CacheConfig.defaultConfig();

        assertThat(config.maximumSize()).isEqualTo(10000);
        assertThat(config.maximumWeight()).isEqualTo(-1);
        assertThat(config.expireAfterWrite()).isNull();
        assertThat(config.expireAfterAccess()).isNull();
        assertThat(config.refreshAfterWrite()).isNull();
        assertThat(config.evictionPolicy()).isNull();
        assertThat(config.expiryPolicy()).isNull();
        assertThat(config.loader()).isNull();
        assertThat(config.removalListener()).isNull();
        assertThat(config.recordStats()).isFalse();
        assertThat(config.useVirtualThreads()).isFalse();
        assertThat(config.executor()).isNull();
        assertThat(config.concurrencyLevel()).isEqualTo(16);
        assertThat(config.initialCapacity()).isEqualTo(16);
    }

    @Test
    void shouldBuildConfigWithMaximumSize() {
        CacheConfig<String, String> config = CacheConfig.<String, String>builder()
                .maximumSize(5000)
                .build();

        assertThat(config.maximumSize()).isEqualTo(5000);
    }

    @Test
    void shouldBuildConfigWithMaximumWeight() {
        CacheConfig<String, String> config = CacheConfig.<String, String>builder()
                .maximumWeight(1000000)
                .weigher(v -> v != null ? v.length() : 0)
                .build();

        assertThat(config.maximumWeight()).isEqualTo(1000000);
    }

    @Test
    void shouldBuildConfigWithExpireAfterWrite() {
        Duration duration = Duration.ofMinutes(30);
        CacheConfig<String, String> config = CacheConfig.<String, String>builder()
                .expireAfterWrite(duration)
                .build();

        assertThat(config.expireAfterWrite()).isEqualTo(duration);
    }

    @Test
    void shouldBuildConfigWithExpireAfterAccess() {
        Duration duration = Duration.ofMinutes(10);
        CacheConfig<String, String> config = CacheConfig.<String, String>builder()
                .expireAfterAccess(duration)
                .build();

        assertThat(config.expireAfterAccess()).isEqualTo(duration);
    }

    @Test
    void shouldBuildConfigWithRefreshAfterWrite() {
        Duration duration = Duration.ofMinutes(5);
        CacheConfig<String, String> config = CacheConfig.<String, String>builder()
                .expireAfterWrite(Duration.ofMinutes(30))
                .refreshAfterWrite(duration)
                .build();

        assertThat(config.refreshAfterWrite()).isEqualTo(duration);
    }

    @Test
    void shouldBuildConfigWithEvictionPolicy() {
        EvictionPolicy<String, String> policy = EvictionPolicy.lru();
        CacheConfig<String, String> config = CacheConfig.<String, String>builder()
                .evictionPolicy(policy)
                .build();

        assertThat(config.evictionPolicy()).isEqualTo(policy);
    }

    @Test
    void shouldBuildConfigWithExpiryPolicy() {
        ExpiryPolicy<String, String> policy = ExpiryPolicy.ttl(Duration.ofHours(1));
        CacheConfig<String, String> config = CacheConfig.<String, String>builder()
                .expiryPolicy(policy)
                .build();

        assertThat(config.expiryPolicy()).isEqualTo(policy);
    }

    @Test
    void shouldBuildConfigWithLoader() {
        CacheLoader<String, String> loader = key -> "value-" + key;
        CacheConfig<String, String> config = CacheConfig.<String, String>builder()
                .loader(loader)
                .build();

        assertThat(config.loader()).isEqualTo(loader);
    }

    @Test
    void shouldBuildConfigWithLoaderFunction() throws Exception {
        java.util.function.Function<String, String> loadFunc = key -> "value-" + key;
        CacheConfig<String, String> config = CacheConfig.<String, String>builder()
                .loader(loadFunc)
                .build();

        assertThat(config.loader()).isNotNull();
        assertThat(config.loader().load("test")).isEqualTo("value-test");
    }

    @Test
    void shouldBuildConfigWithRemovalListener() {
        AtomicBoolean called = new AtomicBoolean(false);
        RemovalListener<String, String> listener = (key, value, cause) -> called.set(true);

        CacheConfig<String, String> config = CacheConfig.<String, String>builder()
                .removalListener(listener)
                .build();

        assertThat(config.removalListener()).isEqualTo(listener);
        config.removalListener().onRemoval("key", "value", RemovalCause.EXPLICIT);
        assertThat(called.get()).isTrue();
    }

    @Test
    void shouldBuildConfigWithRecordStats() {
        CacheConfig<String, String> config = CacheConfig.<String, String>builder()
                .recordStats()
                .build();

        assertThat(config.recordStats()).isTrue();
    }

    @Test
    void shouldBuildConfigWithVirtualThreads() {
        CacheConfig<String, String> config = CacheConfig.<String, String>builder()
                .useVirtualThreads()
                .build();

        assertThat(config.useVirtualThreads()).isTrue();
    }

    @Test
    void shouldBuildConfigWithExecutor() {
        var executor = Executors.newSingleThreadExecutor();
        try {
            CacheConfig<String, String> config = CacheConfig.<String, String>builder()
                    .executor(executor)
                    .build();

            assertThat(config.executor()).isEqualTo(executor);
        } finally {
            executor.shutdown();
        }
    }

    @Test
    void shouldBuildConfigWithConcurrencyLevel() {
        CacheConfig<String, String> config = CacheConfig.<String, String>builder()
                .concurrencyLevel(32)
                .build();

        assertThat(config.concurrencyLevel()).isEqualTo(32);
    }

    @Test
    void shouldBuildConfigWithInitialCapacity() {
        CacheConfig<String, String> config = CacheConfig.<String, String>builder()
                .initialCapacity(64)
                .build();

        assertThat(config.initialCapacity()).isEqualTo(64);
    }

    @Test
    void shouldDetectExpiration() {
        CacheConfig<String, String> noExpiration = CacheConfig.defaultConfig();
        assertThat(noExpiration.hasExpiration()).isFalse();

        CacheConfig<String, String> withTtl = CacheConfig.<String, String>builder()
                .expireAfterWrite(Duration.ofMinutes(1))
                .build();
        assertThat(withTtl.hasExpiration()).isTrue();

        CacheConfig<String, String> withTti = CacheConfig.<String, String>builder()
                .expireAfterAccess(Duration.ofMinutes(1))
                .build();
        assertThat(withTti.hasExpiration()).isTrue();

        CacheConfig<String, String> withPolicy = CacheConfig.<String, String>builder()
                .expiryPolicy(ExpiryPolicy.ttl(Duration.ofMinutes(1)))
                .build();
        assertThat(withPolicy.hasExpiration()).isTrue();
    }

    @Test
    void shouldDetectSizeLimit() {
        CacheConfig<String, String> noLimit = CacheConfig.<String, String>builder()
                .maximumSize(0)
                .build();
        assertThat(noLimit.hasSizeLimit()).isFalse();

        CacheConfig<String, String> withSize = CacheConfig.<String, String>builder()
                .maximumSize(100)
                .build();
        assertThat(withSize.hasSizeLimit()).isTrue();

        CacheConfig<String, String> withWeight = CacheConfig.<String, String>builder()
                .maximumWeight(1000)
                .weigher(v -> v != null ? v.length() : 0)
                .build();
        assertThat(withWeight.hasSizeLimit()).isTrue();
    }

    @Test
    void shouldBuildCompleteConfig() {
        CacheConfig<String, String> config = CacheConfig.<String, String>builder()
                .maximumSize(5000)
                .expireAfterWrite(Duration.ofMinutes(30))
                .expireAfterAccess(Duration.ofMinutes(10))
                .refreshAfterWrite(Duration.ofMinutes(5))
                .evictionPolicy(EvictionPolicy.lru())
                .recordStats()
                .concurrencyLevel(32)
                .initialCapacity(64)
                .build();

        assertThat(config.maximumSize()).isEqualTo(5000);
        assertThat(config.expireAfterWrite()).isEqualTo(Duration.ofMinutes(30));
        assertThat(config.expireAfterAccess()).isEqualTo(Duration.ofMinutes(10));
        assertThat(config.refreshAfterWrite()).isEqualTo(Duration.ofMinutes(5));
        assertThat(config.evictionPolicy()).isNotNull();
        assertThat(config.recordStats()).isTrue();
        assertThat(config.concurrencyLevel()).isEqualTo(32);
        assertThat(config.initialCapacity()).isEqualTo(64);
    }
}
