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

package cloud.opencode.base.cache;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for TenantCache
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("TenantCache Tests")
class TenantCacheTest {

    private TenantCache<String, String> tenantCache;

    @BeforeEach
    void setUp() {
        tenantCache = TenantCache.<String, String>builder("test-tenant-cache")
                .defaultMaxSize(100)
                .defaultTtl(Duration.ofMinutes(30))
                .tenantQuota("premium", 10000)
                .tenantQuota("free", 10)
                .build();
    }

    @AfterEach
    void tearDown() {
        // Clear all tenant data to prevent test pollution
        if (tenantCache != null) {
            tenantCache.invalidateAll();
        }
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("builder() creates builder")
        void builderCreatesBuilder() {
            TenantCache.Builder<String, String> builder = TenantCache.builder("test");
            assertNotNull(builder);
        }

        @Test
        @DisplayName("builder() with null name throws exception")
        void builderWithNullNameThrows() {
            assertThrows(NullPointerException.class, () -> TenantCache.builder(null));
        }

        @Test
        @DisplayName("build with defaults")
        void buildWithDefaults() {
            TenantCache<String, String> cache = TenantCache.<String, String>builder("default")
                    .build();
            assertNotNull(cache);
        }

        @Test
        @DisplayName("build with defaultMaxSize")
        void buildWithDefaultMaxSize() {
            TenantCache<String, String> cache = TenantCache.<String, String>builder("max-size")
                    .defaultMaxSize(500)
                    .build();
            assertNotNull(cache);
        }

        @Test
        @DisplayName("build with defaultTtl")
        void buildWithDefaultTtl() {
            TenantCache<String, String> cache = TenantCache.<String, String>builder("ttl")
                    .defaultTtl(Duration.ofMinutes(10))
                    .build();
            assertNotNull(cache);
        }

        @Test
        @DisplayName("build with tenantQuota")
        void buildWithTenantQuota() {
            TenantCache<String, String> cache = TenantCache.<String, String>builder("quota")
                    .tenantQuota("tenant1", 1000)
                    .build();
            assertNotNull(cache);
        }

        @Test
        @DisplayName("build with tenantQuota and TTL")
        void buildWithTenantQuotaAndTtl() {
            TenantCache<String, String> cache = TenantCache.<String, String>builder("quota-ttl")
                    .tenantQuota("tenant1", 1000, Duration.ofMinutes(5))
                    .build();
            assertNotNull(cache);
        }

        @Test
        @DisplayName("build with custom cacheFactory")
        void buildWithCacheFactory() {
            TenantCache<String, String> cache = TenantCache.<String, String>builder("factory")
                    .cacheFactory(() -> OpenCache.<String, String>builder().maximumSize(50).build())
                    .build();
            assertNotNull(cache);
        }
    }

    @Nested
    @DisplayName("Get Operations")
    class GetOperationsTests {

        @Test
        @DisplayName("get returns null for missing")
        void getReturnsNullForMissing() {
            assertNull(tenantCache.get("tenant1", "key"));
        }

        @Test
        @DisplayName("get returns cached value")
        void getReturnsCachedValue() {
            tenantCache.put("tenant1", "key", "value");
            assertEquals("value", tenantCache.get("tenant1", "key"));
        }

        @Test
        @DisplayName("get isolates tenants")
        void getIsolatesTenants() {
            tenantCache.put("tenant1", "key", "value1");
            tenantCache.put("tenant2", "key", "value2");

            assertEquals("value1", tenantCache.get("tenant1", "key"));
            assertEquals("value2", tenantCache.get("tenant2", "key"));
        }

        @Test
        @DisplayName("get with loader loads value")
        void getWithLoaderLoadsValue() {
            String result = tenantCache.get("tenant1", "key", k -> "loaded-" + k);
            assertEquals("loaded-key", result);
        }
    }

    @Nested
    @DisplayName("Put Operations")
    class PutOperationsTests {

        @Test
        @DisplayName("put stores value")
        void putStoresValue() {
            tenantCache.put("tenant1", "key", "value");
            assertEquals("value", tenantCache.get("tenant1", "key"));
        }

        @Test
        @DisplayName("put with TTL stores value")
        void putWithTtlStoresValue() {
            tenantCache.put("tenant1", "key", "value", Duration.ofMinutes(5));
            assertEquals("value", tenantCache.get("tenant1", "key"));
        }

        @Test
        @DisplayName("putAll stores values")
        void putAllStoresValues() {
            tenantCache.putAll("tenant1", Map.of("a", "1", "b", "2"));
            assertEquals("1", tenantCache.get("tenant1", "a"));
            assertEquals("2", tenantCache.get("tenant1", "b"));
        }

        @Test
        @DisplayName("put throws on quota exceeded")
        void putThrowsOnQuotaExceeded() {
            // Fill up the "free" tenant quota (10 items)
            for (int i = 0; i < 10; i++) {
                tenantCache.put("free", "key" + i, "value" + i);
            }

            // Next put should throw
            assertThrows(TenantCache.TenantQuotaExceededException.class, () ->
                    tenantCache.put("free", "overflow", "value"));
        }
    }

    @Nested
    @DisplayName("Invalidation Operations")
    class InvalidationOperationsTests {

        @Test
        @DisplayName("invalidate removes key for tenant")
        void invalidateRemovesKeyForTenant() {
            tenantCache.put("tenant1", "key", "value");
            tenantCache.invalidate("tenant1", "key");
            assertFalse(tenantCache.containsKey("tenant1", "key"));
        }

        @Test
        @DisplayName("invalidate does not affect other tenants")
        void invalidateDoesNotAffectOtherTenants() {
            tenantCache.put("tenant1", "key", "value1");
            tenantCache.put("tenant2", "key", "value2");

            tenantCache.invalidate("tenant1", "key");

            assertFalse(tenantCache.containsKey("tenant1", "key"));
            assertTrue(tenantCache.containsKey("tenant2", "key"));
        }

        @Test
        @DisplayName("invalidateAll clears tenant cache")
        void invalidateAllClearsTenantCache() {
            tenantCache.put("tenant1", "a", "1");
            tenantCache.put("tenant1", "b", "2");
            tenantCache.invalidateAll("tenant1");

            assertEquals(0, tenantCache.tenantSize("tenant1"));
        }

        @Test
        @DisplayName("invalidateAll clears all tenants")
        void invalidateAllClearsAllTenants() {
            tenantCache.put("tenant1", "key", "value1");
            tenantCache.put("tenant2", "key", "value2");
            tenantCache.invalidateAll();

            assertEquals(0, tenantCache.totalSize());
        }
    }

    @Nested
    @DisplayName("Tenant View")
    class TenantViewTests {

        @Test
        @DisplayName("forTenant returns scoped cache")
        void forTenantReturnsScopedCache() {
            Cache<String, String> view = tenantCache.forTenant("tenant1");
            assertNotNull(view);
        }

        @Test
        @DisplayName("tenant view put is scoped")
        void tenantViewPutIsScoped() {
            Cache<String, String> view = tenantCache.forTenant("tenant1");
            view.put("key", "value");

            assertEquals("value", tenantCache.get("tenant1", "key"));
            assertNull(tenantCache.get("tenant2", "key"));
        }

        @Test
        @DisplayName("tenant view get is scoped")
        void tenantViewGetIsScoped() {
            tenantCache.put("tenant1", "key", "value");
            Cache<String, String> view = tenantCache.forTenant("tenant1");

            assertEquals("value", view.get("key"));
        }

        @Test
        @DisplayName("tenant view invalidate is scoped")
        void tenantViewInvalidateIsScoped() {
            tenantCache.put("tenant1", "key", "value1");
            tenantCache.put("tenant2", "key", "value2");

            Cache<String, String> view = tenantCache.forTenant("tenant1");
            view.invalidate("key");

            assertFalse(tenantCache.containsKey("tenant1", "key"));
            assertTrue(tenantCache.containsKey("tenant2", "key"));
        }

        @Test
        @DisplayName("tenant view name includes tenant")
        void tenantViewNameIncludesTenant() {
            Cache<String, String> view = tenantCache.forTenant("tenant1");
            assertTrue(view.name().contains("tenant1"));
        }

        @Test
        @DisplayName("tenant view size returns tenant size")
        void tenantViewSizeReturnsTenantSize() {
            tenantCache.put("tenant1", "a", "1");
            tenantCache.put("tenant1", "b", "2");
            tenantCache.put("tenant2", "c", "3");

            Cache<String, String> view = tenantCache.forTenant("tenant1");
            assertEquals(2, view.size());
        }

        @Test
        @DisplayName("tenant view stats returns tenant stats")
        void tenantViewStatsReturnsTenantStats() {
            tenantCache.put("tenant1", "key", "value");
            Cache<String, String> view = tenantCache.forTenant("tenant1");

            assertNotNull(view.stats());
        }

        @Test
        @DisplayName("tenant view all operations")
        void tenantViewAllOperations() {
            Cache<String, String> view = tenantCache.forTenant("tenant1");

            // Put operations
            view.put("key1", "value1");
            view.putAll(Map.of("key2", "value2", "key3", "value3"));
            assertTrue(view.putIfAbsent("key4", "value4"));
            view.putWithTtl("key5", "value5", Duration.ofMinutes(5));
            view.putAllWithTtl(Map.of("key6", "value6"), Duration.ofMinutes(5));
            assertTrue(view.putIfAbsentWithTtl("key7", "value7", Duration.ofMinutes(5)));

            // Query operations
            assertEquals("value1", view.get("key1"));
            assertTrue(view.containsKey("key1"));
            assertTrue(view.keys().contains("key1"));
            assertTrue(view.values().contains("value1"));
            assertFalse(view.entries().isEmpty());
            assertNotNull(view.asMap());

            // Get operations
            Map<String, String> result = view.getAll(List.of("key1", "key2"));
            assertEquals(2, result.size());

            // Invalidation
            view.invalidateAll(List.of("key5", "key6"));
            view.invalidateAll();

            assertEquals(0, view.estimatedSize());
        }
    }

    @Nested
    @DisplayName("Tenant Management")
    class TenantManagementTests {

        @Test
        @DisplayName("tenants returns active tenant IDs")
        void tenantsReturnsActiveTenantIds() {
            tenantCache.put("tenant1", "key", "value");
            tenantCache.put("tenant2", "key", "value");

            assertTrue(tenantCache.tenants().contains("tenant1"));
            assertTrue(tenantCache.tenants().contains("tenant2"));
        }

        @Test
        @DisplayName("tenantSize returns tenant cache size")
        void tenantSizeReturnsTenantCacheSize() {
            tenantCache.put("tenant1", "a", "1");
            tenantCache.put("tenant1", "b", "2");

            assertEquals(2, tenantCache.tenantSize("tenant1"));
            assertEquals(0, tenantCache.tenantSize("nonexistent"));
        }

        @Test
        @DisplayName("tenantStats returns tenant statistics")
        void tenantStatsReturnsTenantStatistics() {
            tenantCache.put("tenant1", "key", "value");
            tenantCache.get("tenant1", "key");

            CacheStats stats = tenantCache.tenantStats("tenant1");
            assertNotNull(stats);
        }

        @Test
        @DisplayName("tenantStats returns empty for nonexistent")
        void tenantStatsReturnsEmptyForNonexistent() {
            CacheStats stats = tenantCache.tenantStats("nonexistent");
            assertNotNull(stats);
            assertEquals(0, stats.hitCount());
        }

        @Test
        @DisplayName("aggregatedStats returns combined stats")
        void aggregatedStatsReturnsCombinedStats() {
            tenantCache.put("tenant1", "key", "value1");
            tenantCache.put("tenant2", "key", "value2");
            tenantCache.get("tenant1", "key");
            tenantCache.get("tenant2", "key");

            CacheStats stats = tenantCache.aggregatedStats();
            assertNotNull(stats);
        }

        @Test
        @DisplayName("removeTenant removes tenant and data")
        void removeTenantRemovesTenantAndData() {
            tenantCache.put("tenant1", "key", "value");
            tenantCache.removeTenant("tenant1");

            assertFalse(tenantCache.tenants().contains("tenant1"));
            assertEquals(0, tenantCache.tenantSize("tenant1"));
        }

        @Test
        @DisplayName("setTenantQuota sets quota")
        void setTenantQuotaSetsQuota() {
            tenantCache.setTenantQuota("custom", 5);

            for (int i = 0; i < 5; i++) {
                tenantCache.put("custom", "key" + i, "value" + i);
            }

            assertThrows(TenantCache.TenantQuotaExceededException.class, () ->
                    tenantCache.put("custom", "overflow", "value"));
        }

        @Test
        @DisplayName("setTenantQuota with TTL sets quota and TTL")
        void setTenantQuotaWithTtlSetsQuotaAndTtl() {
            // Use a different tenant ID to avoid conflict with other quota tests
            tenantCache.setTenantQuota("custom-ttl", 5, Duration.ofMinutes(10));

            for (int i = 0; i < 5; i++) {
                tenantCache.put("custom-ttl", "key" + i, "value" + i);
            }

            assertThrows(TenantCache.TenantQuotaExceededException.class, () ->
                    tenantCache.put("custom-ttl", "overflow", "value"));
        }
    }

    @Nested
    @DisplayName("Other Operations")
    class OtherOperationsTests {

        @Test
        @DisplayName("name returns cache name")
        void nameReturnsCacheName() {
            assertEquals("test-tenant-cache", tenantCache.name());
        }

        @Test
        @DisplayName("totalSize returns combined size")
        void totalSizeReturnsCombinedSize() {
            tenantCache.put("tenant1", "a", "1");
            tenantCache.put("tenant1", "b", "2");
            tenantCache.put("tenant2", "c", "3");

            assertEquals(3, tenantCache.totalSize());
        }

        @Test
        @DisplayName("cleanUp cleans all tenant caches")
        void cleanUpCleansAllTenantCaches() {
            tenantCache.put("tenant1", "key", "value");
            tenantCache.put("tenant2", "key", "value");

            assertDoesNotThrow(() -> tenantCache.cleanUp());
        }

        @Test
        @DisplayName("containsKey checks tenant cache")
        void containsKeyChecksTenantCache() {
            tenantCache.put("tenant1", "key", "value");

            assertTrue(tenantCache.containsKey("tenant1", "key"));
            assertFalse(tenantCache.containsKey("tenant1", "other"));
            assertFalse(tenantCache.containsKey("tenant2", "key"));
        }
    }

    @Nested
    @DisplayName("Exception Tests")
    class ExceptionTests {

        @Test
        @DisplayName("TenantQuotaExceededException contains details")
        void tenantQuotaExceededExceptionContainsDetails() {
            TenantCache.TenantQuotaExceededException ex =
                    new TenantCache.TenantQuotaExceededException("tenant1", 100, 100);

            assertEquals("tenant1", ex.getTenantId());
            assertEquals(100, ex.getQuota());
            assertEquals(100, ex.getCurrentSize());
            assertTrue(ex.getMessage().contains("tenant1"));
        }
    }
}
