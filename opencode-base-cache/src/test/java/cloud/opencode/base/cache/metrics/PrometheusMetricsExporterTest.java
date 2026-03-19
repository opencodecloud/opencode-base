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

package cloud.opencode.base.cache.metrics;

import cloud.opencode.base.cache.Cache;
import cloud.opencode.base.cache.CacheManager;
import cloud.opencode.base.cache.OpenCache;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for PrometheusMetricsExporter
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("PrometheusMetricsExporter Tests")
class PrometheusMetricsExporterTest {

    private Cache<String, String> cache;

    @BeforeEach
    void setUp() {
        cache = OpenCache.<String, String>builder()
                .maximumSize(100)
                .recordStats()
                .build("prometheus-test-" + System.nanoTime());
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("create returns exporter")
        void createReturnsExporter() {
            PrometheusMetricsExporter exporter = PrometheusMetricsExporter.create();

            assertNotNull(exporter);
        }

        @Test
        @DisplayName("create with namespace returns exporter")
        void createWithNamespaceReturnsExporter() {
            PrometheusMetricsExporter exporter = PrometheusMetricsExporter.create("myapp");

            assertNotNull(exporter);
        }

        @Test
        @DisplayName("builder creates exporter")
        void builderCreatesExporter() {
            PrometheusMetricsExporter exporter = PrometheusMetricsExporter.builder()
                    .namespace("myapp")
                    .label("env", "test")
                    .build();

            assertNotNull(exporter);
        }

        @Test
        @DisplayName("builder handles null namespace")
        void builderHandlesNullNamespace() {
            PrometheusMetricsExporter exporter = PrometheusMetricsExporter.builder()
                    .namespace(null)
                    .build();

            assertNotNull(exporter);
        }
    }

    @Nested
    @DisplayName("Registration Tests")
    class RegistrationTests {

        @Test
        @DisplayName("register adds cache")
        void registerAddsCache() {
            PrometheusMetricsExporter exporter = PrometheusMetricsExporter.create();

            PrometheusMetricsExporter result = exporter.register("test", cache);

            assertSame(exporter, result);
        }

        @Test
        @DisplayName("register throws on null name")
        void registerThrowsOnNullName() {
            PrometheusMetricsExporter exporter = PrometheusMetricsExporter.create();

            assertThrows(NullPointerException.class, () ->
                    exporter.register(null, cache));
        }

        @Test
        @DisplayName("register throws on null cache")
        void registerThrowsOnNullCache() {
            PrometheusMetricsExporter exporter = PrometheusMetricsExporter.create();

            assertThrows(NullPointerException.class, () ->
                    exporter.register("test", null));
        }

        @Test
        @DisplayName("registerAll registers all caches from manager")
        void registerAllRegistersAllCachesFromManager() {
            CacheManager manager = CacheManager.getInstance();
            String cacheName = "prometheus-manager-cache1-" + System.nanoTime();
            Cache<String, String> cache1 = manager.getOrCreateCache(cacheName, config -> config.maximumSize(100));

            PrometheusMetricsExporter exporter = PrometheusMetricsExporter.create();
            PrometheusMetricsExporter result = exporter.registerAll(manager);

            assertSame(exporter, result);
            String metrics = exporter.export();
            assertTrue(metrics.contains(cacheName));
        }

        @Test
        @DisplayName("unregister removes cache")
        void unregisterRemovesCache() {
            PrometheusMetricsExporter exporter = PrometheusMetricsExporter.create();
            exporter.register("test", cache);

            PrometheusMetricsExporter result = exporter.unregister("test");

            assertSame(exporter, result);
            String metrics = exporter.export();
            assertFalse(metrics.contains("test"));
        }
    }

    @Nested
    @DisplayName("Export Tests")
    class ExportTests {

        @Test
        @DisplayName("export returns Prometheus format")
        void exportReturnsPrometheusFormat() {
            PrometheusMetricsExporter exporter = PrometheusMetricsExporter.create();
            exporter.register("test", cache);

            // Generate some activity
            cache.put("key", "value");
            cache.get("key");
            cache.get("missing");

            String metrics = exporter.export();

            // Verify Prometheus format elements
            assertTrue(metrics.contains("# HELP"));
            assertTrue(metrics.contains("# TYPE"));
            assertTrue(metrics.contains("cache_hits_total"));
            assertTrue(metrics.contains("cache_misses_total"));
            assertTrue(metrics.contains("cache_size"));
        }

        @Test
        @DisplayName("export writes to writer")
        void exportWritesToWriter() throws IOException {
            PrometheusMetricsExporter exporter = PrometheusMetricsExporter.create();
            exporter.register("test", cache);

            StringWriter writer = new StringWriter();
            exporter.export(writer);

            String metrics = writer.toString();
            assertTrue(metrics.contains("cache_"));
        }

        @Test
        @DisplayName("export with namespace includes prefix")
        void exportWithNamespaceIncludesPrefix() {
            PrometheusMetricsExporter exporter = PrometheusMetricsExporter.create("myapp");
            exporter.register("test", cache);

            String metrics = exporter.export();

            assertTrue(metrics.contains("myapp_cache_"));
        }

        @Test
        @DisplayName("export with labels includes labels")
        void exportWithLabelsIncludesLabels() {
            PrometheusMetricsExporter exporter = PrometheusMetricsExporter.builder()
                    .label("env", "production")
                    .label("region", "us-east")
                    .build();
            exporter.register("test", cache);

            String metrics = exporter.export();

            assertTrue(metrics.contains("env=\"production\""));
            assertTrue(metrics.contains("region=\"us-east\""));
        }

        @Test
        @DisplayName("export escapes label values")
        void exportEscapesLabelValues() {
            PrometheusMetricsExporter exporter = PrometheusMetricsExporter.create();

            Cache<String, String> cacheWithSpecialName = OpenCache.<String, String>builder()
                    .maximumSize(100)
                    .build("test\"cache");
            exporter.register("test\"cache", cacheWithSpecialName);

            String metrics = exporter.export();

            assertTrue(metrics.contains("test\\\"cache"));
        }

        @Test
        @DisplayName("export handles empty exporter")
        void exportHandlesEmptyExporter() {
            PrometheusMetricsExporter exporter = PrometheusMetricsExporter.create();

            String metrics = exporter.export();

            assertNotNull(metrics);
        }

        @Test
        @DisplayName("export includes counter metrics")
        void exportIncludesCounterMetrics() {
            PrometheusMetricsExporter exporter = PrometheusMetricsExporter.create();
            exporter.register("test", cache);

            // Generate activity
            cache.put("k1", "v1");
            cache.put("k2", "v2");
            cache.get("k1");
            cache.get("k2");
            cache.get("k3"); // miss

            String metrics = exporter.export();

            assertTrue(metrics.contains("cache_hits_total"));
            assertTrue(metrics.contains("cache_misses_total"));
            assertTrue(metrics.contains("cache_requests_total"));
            assertTrue(metrics.contains("counter"));
        }

        @Test
        @DisplayName("export includes gauge metrics")
        void exportIncludesGaugeMetrics() {
            PrometheusMetricsExporter exporter = PrometheusMetricsExporter.create();
            exporter.register("test", cache);

            cache.put("k1", "v1");

            String metrics = exporter.export();

            assertTrue(metrics.contains("cache_size"));
            assertTrue(metrics.contains("cache_hit_ratio"));
            assertTrue(metrics.contains("gauge"));
        }

        @Test
        @DisplayName("export handles NaN values")
        void exportHandlesNaNValues() {
            PrometheusMetricsExporter exporter = PrometheusMetricsExporter.create();

            // Fresh cache with no stats will have NaN hit ratio
            Cache<String, String> freshCache = OpenCache.<String, String>builder()
                    .maximumSize(100)
                    .recordStats()
                    .build("fresh-cache-" + System.nanoTime());
            exporter.register("fresh", freshCache);

            String metrics = exporter.export();

            // Should handle NaN gracefully
            assertNotNull(metrics);
        }
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("builder with multiple labels")
        void builderWithMultipleLabels() {
            PrometheusMetricsExporter exporter = PrometheusMetricsExporter.builder()
                    .namespace("app")
                    .label("env", "test")
                    .label("version", "1.0")
                    .label("instance", "node1")
                    .build();

            exporter.register("test", cache);
            String metrics = exporter.export();

            assertTrue(metrics.contains("app_cache_"));
            assertTrue(metrics.contains("env=\"test\""));
            assertTrue(metrics.contains("version=\"1.0\""));
            assertTrue(metrics.contains("instance=\"node1\""));
        }

        @Test
        @DisplayName("builder returns new exporter each time")
        void builderReturnsNewExporterEachTime() {
            var builder = PrometheusMetricsExporter.builder();

            PrometheusMetricsExporter exporter1 = builder.build();
            PrometheusMetricsExporter exporter2 = builder.build();

            assertNotSame(exporter1, exporter2);
        }
    }

    @Nested
    @DisplayName("Multiple Cache Tests")
    class MultipleCacheTests {

        @Test
        @DisplayName("exports metrics for multiple caches")
        void exportsMetricsForMultipleCaches() {
            PrometheusMetricsExporter exporter = PrometheusMetricsExporter.create();

            Cache<String, String> cache1 = OpenCache.<String, String>builder()
                    .maximumSize(100)
                    .recordStats()
                    .build("users-" + System.nanoTime());
            Cache<String, String> cache2 = OpenCache.<String, String>builder()
                    .maximumSize(100)
                    .recordStats()
                    .build("products-" + System.nanoTime());

            exporter.register("users", cache1);
            exporter.register("products", cache2);

            cache1.put("u1", "user1");
            cache2.put("p1", "product1");

            String metrics = exporter.export();

            assertTrue(metrics.contains("cache=\"users\""));
            assertTrue(metrics.contains("cache=\"products\""));
        }

        @Test
        @DisplayName("unregister only affects specified cache")
        void unregisterOnlyAffectsSpecifiedCache() {
            PrometheusMetricsExporter exporter = PrometheusMetricsExporter.create();

            Cache<String, String> cache1 = OpenCache.<String, String>builder()
                    .maximumSize(100)
                    .build("cache1-" + System.nanoTime());
            Cache<String, String> cache2 = OpenCache.<String, String>builder()
                    .maximumSize(100)
                    .build("cache2-" + System.nanoTime());

            exporter.register("cache1", cache1);
            exporter.register("cache2", cache2);
            exporter.unregister("cache1");

            String metrics = exporter.export();

            assertFalse(metrics.contains("cache=\"cache1\""));
            assertTrue(metrics.contains("cache=\"cache2\""));
        }
    }
}
