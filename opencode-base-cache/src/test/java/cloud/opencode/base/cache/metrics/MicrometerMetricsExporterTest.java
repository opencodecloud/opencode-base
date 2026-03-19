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

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToDoubleFunction;
import java.util.function.ToLongFunction;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for MicrometerMetricsExporter
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("MicrometerMetricsExporter Tests")
class MicrometerMetricsExporterTest {

    private Cache<String, String> cache;
    private TestMeterRegistry testRegistry;

    @BeforeEach
    void setUp() {
        cache = OpenCache.<String, String>builder()
                .maximumSize(100)
                .recordStats()
                .build("micrometer-test-" + System.nanoTime());
        testRegistry = new TestMeterRegistry();
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("builder creates exporter")
        void builderCreatesExporter() {
            MicrometerMetricsExporter exporter = MicrometerMetricsExporter.builder()
                    .registry(testRegistry)
                    .build();

            assertNotNull(exporter);
        }

        @Test
        @DisplayName("builder throws on null registry")
        void builderThrowsOnNullRegistry() {
            assertThrows(NullPointerException.class, () ->
                    MicrometerMetricsExporter.builder().build());
        }

        @Test
        @DisplayName("create with registry returns exporter")
        void createWithRegistryReturnsExporter() {
            MicrometerMetricsExporter exporter = MicrometerMetricsExporter.create(testRegistry);

            assertNotNull(exporter);
        }
    }

    @Nested
    @DisplayName("Registration Tests")
    class RegistrationTests {

        @Test
        @DisplayName("register adds cache")
        void registerAddsCache() {
            MicrometerMetricsExporter exporter = MicrometerMetricsExporter.builder()
                    .registry(testRegistry)
                    .build();

            MicrometerMetricsExporter result = exporter.register("test", cache);

            assertSame(exporter, result);
            assertFalse(testRegistry.registeredGauges.isEmpty());
        }

        @Test
        @DisplayName("register throws on null name")
        void registerThrowsOnNullName() {
            MicrometerMetricsExporter exporter = MicrometerMetricsExporter.create(testRegistry);

            assertThrows(NullPointerException.class, () ->
                    exporter.register(null, cache));
        }

        @Test
        @DisplayName("register throws on null cache")
        void registerThrowsOnNullCache() {
            MicrometerMetricsExporter exporter = MicrometerMetricsExporter.create(testRegistry);

            assertThrows(NullPointerException.class, () ->
                    exporter.register("test", null));
        }

        @Test
        @DisplayName("registerAll registers all caches from manager")
        void registerAllRegistersAllCachesFromManager() {
            CacheManager manager = CacheManager.getInstance();
            // Create cache through manager
            Cache<String, String> cache1 = manager.getOrCreateCache("micrometer-test-cache1-" + System.nanoTime(),
                    config -> config.maximumSize(100));

            MicrometerMetricsExporter exporter = MicrometerMetricsExporter.create(testRegistry);
            MicrometerMetricsExporter result = exporter.registerAll(manager);

            assertSame(exporter, result);
            assertFalse(testRegistry.registeredGauges.isEmpty());
        }

        @Test
        @DisplayName("unregister removes cache")
        void unregisterRemovesCache() {
            MicrometerMetricsExporter exporter = MicrometerMetricsExporter.create(testRegistry);
            exporter.register("test", cache);

            int gaugesBefore = testRegistry.registeredGauges.size();

            MicrometerMetricsExporter result = exporter.unregister("test");

            assertSame(exporter, result);
            assertTrue(testRegistry.removeWasCalled);
        }
    }

    @Nested
    @DisplayName("Metrics Binding Tests")
    class MetricsBindingTests {

        @Test
        @DisplayName("registers size gauge")
        void registersSizeGauge() {
            MicrometerMetricsExporter exporter = MicrometerMetricsExporter.create(testRegistry);
            exporter.register("test", cache);

            assertTrue(testRegistry.registeredGauges.stream()
                    .anyMatch(r -> r.name.contains("size")));
        }

        @Test
        @DisplayName("registers hit counters")
        void registersHitCounters() {
            MicrometerMetricsExporter exporter = MicrometerMetricsExporter.create(testRegistry);
            exporter.register("test", cache);

            assertTrue(testRegistry.registeredCounters.stream()
                    .anyMatch(r -> r.name.contains("gets")));
        }

        @Test
        @DisplayName("registers eviction counter")
        void registersEvictionCounter() {
            MicrometerMetricsExporter exporter = MicrometerMetricsExporter.create(testRegistry);
            exporter.register("test", cache);

            assertTrue(testRegistry.registeredCounters.stream()
                    .anyMatch(r -> r.name.contains("evictions")));
        }

        @Test
        @DisplayName("registers load timer")
        void registersLoadTimer() {
            MicrometerMetricsExporter exporter = MicrometerMetricsExporter.create(testRegistry);
            exporter.register("test", cache);

            assertTrue(testRegistry.registeredTimers.stream()
                    .anyMatch(r -> r.name.contains("load")));
        }

        @Test
        @DisplayName("registers hit ratio gauge")
        void registersHitRatioGauge() {
            MicrometerMetricsExporter exporter = MicrometerMetricsExporter.create(testRegistry);
            exporter.register("test", cache);

            assertTrue(testRegistry.registeredGauges.stream()
                    .anyMatch(r -> r.name.contains("hit.ratio")));
        }
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("builder with custom prefix")
        void builderWithCustomPrefix() {
            MicrometerMetricsExporter exporter = MicrometerMetricsExporter.builder()
                    .registry(testRegistry)
                    .prefix("myapp")
                    .build();

            exporter.register("test", cache);

            assertTrue(testRegistry.registeredGauges.stream()
                    .anyMatch(r -> r.name.startsWith("myapp.")));
        }

        @Test
        @DisplayName("builder with tags")
        void builderWithTags() {
            MicrometerMetricsExporter exporter = MicrometerMetricsExporter.builder()
                    .registry(testRegistry)
                    .tag("env", "production")
                    .tag("region", "us-east")
                    .build();

            exporter.register("test", cache);

            // Verify tags are included
            assertTrue(testRegistry.registeredGauges.stream()
                    .anyMatch(r -> containsTag(r.tags, "env", "production")));
        }

        @Test
        @DisplayName("default prefix is cache")
        void defaultPrefixIsCache() {
            MicrometerMetricsExporter exporter = MicrometerMetricsExporter.builder()
                    .registry(testRegistry)
                    .build();

            exporter.register("test", cache);

            assertTrue(testRegistry.registeredGauges.stream()
                    .anyMatch(r -> r.name.startsWith("cache.")));
        }
    }

    @Nested
    @DisplayName("MeterRegistry Interface Tests")
    class MeterRegistryInterfaceTests {

        @Test
        @DisplayName("wrap creates reflective registry")
        void wrapCreatesReflectiveRegistry() {
            // This tests the static wrap method
            Object mockRegistry = new Object();
            MicrometerMetricsExporter.MeterRegistry wrapped =
                    MicrometerMetricsExporter.MeterRegistry.wrap(mockRegistry);

            assertNotNull(wrapped);
        }

        @Test
        @DisplayName("reflective registry handles missing Micrometer gracefully")
        void reflectiveRegistryHandlesMissingMicrometerGracefully() {
            Object mockRegistry = new Object();
            MicrometerMetricsExporter.MeterRegistry wrapped =
                    MicrometerMetricsExporter.MeterRegistry.wrap(mockRegistry);

            // These should not throw even without actual Micrometer
            assertDoesNotThrow(() ->
                    wrapped.gauge("test", new String[]{"cache", "test"}, cache, c -> 0.0));
            assertDoesNotThrow(() ->
                    wrapped.counter("test", new String[]{"cache", "test"}, cache, c -> 0L));
            assertDoesNotThrow(() ->
                    wrapped.timer("test", new String[]{"cache", "test"}, cache, c -> 0L, c -> 0.0));
            assertDoesNotThrow(() ->
                    wrapped.remove("test"));
        }
    }

    @Nested
    @DisplayName("Multiple Cache Tests")
    class MultipleCacheTests {

        @Test
        @DisplayName("registers metrics for multiple caches")
        void registersMetricsForMultipleCaches() {
            MicrometerMetricsExporter exporter = MicrometerMetricsExporter.create(testRegistry);

            Cache<String, String> cache1 = OpenCache.<String, String>builder()
                    .maximumSize(100)
                    .build("users-" + System.nanoTime());
            Cache<String, String> cache2 = OpenCache.<String, String>builder()
                    .maximumSize(100)
                    .build("products-" + System.nanoTime());

            exporter.register("users", cache1);
            exporter.register("products", cache2);

            assertTrue(testRegistry.registeredGauges.stream()
                    .anyMatch(r -> containsTag(r.tags, "cache", "users")));
            assertTrue(testRegistry.registeredGauges.stream()
                    .anyMatch(r -> containsTag(r.tags, "cache", "products")));
        }
    }

    // ==================== Helper Classes ====================

    private boolean containsTag(String[] tags, String key, String value) {
        for (int i = 0; i < tags.length - 1; i += 2) {
            if (tags[i].equals(key) && tags[i + 1].equals(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Test implementation of MeterRegistry for testing
     */
    private static class TestMeterRegistry implements MicrometerMetricsExporter.MeterRegistry {

        final List<MetricRegistration> registeredGauges = new ArrayList<>();
        final List<MetricRegistration> registeredCounters = new ArrayList<>();
        final List<MetricRegistration> registeredTimers = new ArrayList<>();
        boolean removeWasCalled = false;

        @Override
        public <T> void gauge(String name, String[] tags, T obj, ToDoubleFunction<T> function) {
            registeredGauges.add(new MetricRegistration(name, tags));
        }

        @Override
        public <T> void counter(String name, String[] tags, T obj, ToLongFunction<T> function) {
            registeredCounters.add(new MetricRegistration(name, tags));
        }

        @Override
        public <T> void timer(String name, String[] tags, T obj,
                              ToLongFunction<T> countFunction, ToDoubleFunction<T> totalFunction) {
            registeredTimers.add(new MetricRegistration(name, tags));
        }

        @Override
        public void remove(String namePrefix) {
            removeWasCalled = true;
        }

        static class MetricRegistration {
            final String name;
            final String[] tags;

            MetricRegistration(String name, String[] tags) {
                this.name = name;
                this.tags = tags;
            }
        }
    }
}
