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

package cloud.opencode.base.cache.ttl;

import cloud.opencode.base.cache.Cache;
import cloud.opencode.base.cache.OpenCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for VariableTtlCache
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("VariableTtlCache Tests")
class VariableTtlCacheTest {

    private Cache<String, String> baseCache;

    @BeforeEach
    void setUp() {
        baseCache = OpenCache.<String, String>builder()
                .maximumSize(100)
                .build("test-base-cache");
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("wrap creates builder")
        void wrapCreatesBuilder() {
            VariableTtlCache.Builder<String, String> builder = VariableTtlCache.wrap(baseCache);
            assertNotNull(builder);
        }

        @Test
        @DisplayName("wrap throws on null cache")
        void wrapThrowsOnNullCache() {
            assertThrows(NullPointerException.class, () -> VariableTtlCache.wrap(null));
        }

        @Test
        @DisplayName("build creates cache with defaults")
        void buildCreatesCacheWithDefaults() {
            VariableTtlCache<String, String> cache = VariableTtlCache.wrap(baseCache).build();
            assertNotNull(cache);
        }

        @Test
        @DisplayName("ttlPolicy sets custom policy")
        void ttlPolicySetsCustomPolicy() {
            VariableTtlCache<String, String> cache = VariableTtlCache.wrap(baseCache)
                    .ttlPolicy(TtlPolicy.fixed(Duration.ofHours(2)))
                    .build();
            assertNotNull(cache);
        }

        @Test
        @DisplayName("ttlPolicy throws on null")
        void ttlPolicyThrowsOnNull() {
            assertThrows(NullPointerException.class, () ->
                    VariableTtlCache.wrap(baseCache).ttlPolicy(null));
        }

        @Test
        @DisplayName("decayPolicy sets decay policy")
        void decayPolicySetsDecayPolicy() {
            VariableTtlCache<String, String> cache = VariableTtlCache.wrap(baseCache)
                    .decayPolicy(TtlDecayPolicy.linear(Duration.ofHours(1), Duration.ofMinutes(5), 10))
                    .build();
            assertNotNull(cache);
        }

        @Test
        @DisplayName("trackAccess enables access tracking")
        void trackAccessEnablesAccessTracking() {
            VariableTtlCache<String, String> cache = VariableTtlCache.wrap(baseCache)
                    .trackAccess()
                    .build();
            assertNotNull(cache);
        }
    }

    @Nested
    @DisplayName("Basic Operations Tests")
    class BasicOperationsTests {

        private VariableTtlCache<String, String> cache;

        @BeforeEach
        void setUp() {
            cache = VariableTtlCache.wrap(baseCache)
                    .ttlPolicy(TtlPolicy.fixed(Duration.ofMinutes(10)))
                    .build();
        }

        @Test
        @DisplayName("put and get work")
        void putAndGetWork() {
            cache.put("key1", "value1");
            assertEquals("value1", cache.get("key1"));
        }

        @Test
        @DisplayName("get returns null for missing key")
        void getReturnsNullForMissingKey() {
            assertNull(cache.get("nonexistent"));
        }

        @Test
        @DisplayName("get with loader works")
        void getWithLoaderWorks() {
            String value = cache.get("key", k -> "loaded-" + k);
            assertEquals("loaded-key", value);
        }

        @Test
        @DisplayName("putAll works")
        void putAllWorks() {
            cache.putAll(Map.of("k1", "v1", "k2", "v2"));
            assertEquals("v1", cache.get("k1"));
            assertEquals("v2", cache.get("k2"));
        }

        @Test
        @DisplayName("putIfAbsent works")
        void putIfAbsentWorks() {
            assertTrue(cache.putIfAbsent("key", "value1"));
            assertFalse(cache.putIfAbsent("key", "value2"));
            assertEquals("value1", cache.get("key"));
        }

        @Test
        @DisplayName("putWithTtl overrides policy")
        void putWithTtlOverridesPolicy() {
            cache.putWithTtl("key", "value", Duration.ofHours(1));
            assertEquals("value", cache.get("key"));
        }

        @Test
        @DisplayName("putAllWithTtl works")
        void putAllWithTtlWorks() {
            cache.putAllWithTtl(Map.of("k1", "v1", "k2", "v2"), Duration.ofHours(1));
            assertEquals("v1", cache.get("k1"));
            assertEquals("v2", cache.get("k2"));
        }

        @Test
        @DisplayName("putIfAbsentWithTtl works")
        void putIfAbsentWithTtlWorks() {
            assertTrue(cache.putIfAbsentWithTtl("key", "value", Duration.ofHours(1)));
            assertEquals("value", cache.get("key"));
        }

        @Test
        @DisplayName("invalidate removes entry")
        void invalidateRemovesEntry() {
            cache.put("key", "value");
            cache.invalidate("key");
            assertNull(cache.get("key"));
        }

        @Test
        @DisplayName("invalidateAll with keys works")
        void invalidateAllWithKeysWorks() {
            cache.put("k1", "v1");
            cache.put("k2", "v2");
            cache.put("k3", "v3");
            cache.invalidateAll(Set.of("k1", "k2"));
            assertNull(cache.get("k1"));
            assertNull(cache.get("k2"));
            assertEquals("v3", cache.get("k3"));
        }

        @Test
        @DisplayName("invalidateAll clears cache")
        void invalidateAllClearsCache() {
            cache.put("k1", "v1");
            cache.put("k2", "v2");
            cache.invalidateAll();
            assertEquals(0, cache.size());
        }

        @Test
        @DisplayName("containsKey works")
        void containsKeyWorks() {
            cache.put("key", "value");
            assertTrue(cache.containsKey("key"));
            assertFalse(cache.containsKey("other"));
        }

        @Test
        @DisplayName("size returns correct count")
        void sizeReturnsCorrectCount() {
            cache.put("k1", "v1");
            cache.put("k2", "v2");
            assertEquals(2, cache.size());
        }

        @Test
        @DisplayName("estimatedSize works")
        void estimatedSizeWorks() {
            cache.put("k1", "v1");
            cache.put("k2", "v2");
            assertTrue(cache.estimatedSize() >= 2);
        }

        @Test
        @DisplayName("keys returns all keys")
        void keysReturnsAllKeys() {
            cache.put("k1", "v1");
            cache.put("k2", "v2");
            Set<String> keys = cache.keys();
            assertTrue(keys.contains("k1"));
            assertTrue(keys.contains("k2"));
        }

        @Test
        @DisplayName("values returns all values")
        void valuesReturnsAllValues() {
            cache.put("k1", "v1");
            cache.put("k2", "v2");
            assertTrue(cache.values().contains("v1"));
            assertTrue(cache.values().contains("v2"));
        }

        @Test
        @DisplayName("entries returns all entries")
        void entriesReturnsAllEntries() {
            cache.put("k1", "v1");
            assertEquals(1, cache.entries().size());
        }

        @Test
        @DisplayName("asMap returns concurrent map")
        void asMapReturnsConcurrentMap() {
            cache.put("key", "value");
            assertNotNull(cache.asMap());
            assertEquals("value", cache.asMap().get("key"));
        }

        @Test
        @DisplayName("stats returns stats")
        void statsReturnsStats() {
            assertNotNull(cache.stats());
        }

        @Test
        @DisplayName("metrics returns null when stats not enabled")
        void metricsReturnsMetrics() {
            // metrics() returns null when stats recording is not enabled
            assertNull(cache.metrics());
        }

        @Test
        @DisplayName("cleanUp runs without error")
        void cleanUpRunsWithoutError() {
            cache.put("key", "value");
            assertDoesNotThrow(() -> cache.cleanUp());
        }

        @Test
        @DisplayName("async returns async cache")
        void asyncReturnsAsyncCache() {
            assertNotNull(cache.async());
        }

        @Test
        @DisplayName("name returns delegate name")
        void nameReturnsDelegateName() {
            assertEquals("test-base-cache", cache.name());
        }
    }

    @Nested
    @DisplayName("GetAll Tests")
    class GetAllTests {

        private VariableTtlCache<String, String> cache;

        @BeforeEach
        void setUp() {
            cache = VariableTtlCache.wrap(baseCache)
                    .ttlPolicy(TtlPolicy.fixed(Duration.ofMinutes(10)))
                    .trackAccess()
                    .build();
        }

        @Test
        @DisplayName("getAll returns existing entries")
        void getAllReturnsExistingEntries() {
            cache.put("k1", "v1");
            cache.put("k2", "v2");

            Map<String, String> result = cache.getAll(Set.of("k1", "k2", "k3"));
            assertEquals(2, result.size());
            assertEquals("v1", result.get("k1"));
            assertEquals("v2", result.get("k2"));
        }

        @Test
        @DisplayName("getAll with loader loads missing entries")
        void getAllWithLoaderLoadsMissingEntries() {
            cache.put("k1", "v1");

            Map<String, String> result = cache.getAll(Set.of("k1", "k2"),
                    keys -> Map.of("k2", "loaded-v2"));
            assertEquals(2, result.size());
            assertEquals("v1", result.get("k1"));
            assertEquals("loaded-v2", result.get("k2"));
        }
    }

    @Nested
    @DisplayName("Access Tracking Tests")
    class AccessTrackingTests {

        private VariableTtlCache<String, String> cache;

        @BeforeEach
        void setUp() {
            cache = VariableTtlCache.wrap(baseCache)
                    .ttlPolicy(TtlPolicy.fixed(Duration.ofMinutes(10)))
                    .trackAccess()
                    .build();
        }

        @Test
        @DisplayName("accessCount tracks reads")
        void accessCountTracksReads() {
            cache.put("key", "value");
            assertEquals(0, cache.accessCount("key"));

            cache.get("key");
            assertEquals(1, cache.accessCount("key"));

            cache.get("key");
            assertEquals(2, cache.accessCount("key"));
        }

        @Test
        @DisplayName("accessCount returns 0 for missing key")
        void accessCountReturnsZeroForMissingKey() {
            assertEquals(0, cache.accessCount("nonexistent"));
        }

        @Test
        @DisplayName("put resets access count")
        void putResetsAccessCount() {
            cache.put("key", "value");
            cache.get("key");
            cache.get("key");
            assertEquals(2, cache.accessCount("key"));

            cache.put("key", "new-value");
            assertEquals(0, cache.accessCount("key"));
        }

        @Test
        @DisplayName("allAccessCounts returns all counts")
        void allAccessCountsReturnsAllCounts() {
            cache.put("k1", "v1");
            cache.put("k2", "v2");
            cache.get("k1");
            cache.get("k1");
            cache.get("k2");

            Map<String, Long> counts = cache.allAccessCounts();
            assertEquals(2, counts.get("k1"));
            assertEquals(1, counts.get("k2"));
        }

        @Test
        @DisplayName("invalidate removes access count")
        void invalidateRemovesAccessCount() {
            cache.put("key", "value");
            cache.get("key");
            cache.invalidate("key");
            assertEquals(0, cache.accessCount("key"));
        }

        @Test
        @DisplayName("invalidateAll clears access counts")
        void invalidateAllClearsAccessCounts() {
            cache.put("k1", "v1");
            cache.put("k2", "v2");
            cache.get("k1");
            cache.get("k2");
            cache.invalidateAll();
            assertTrue(cache.allAccessCounts().isEmpty());
        }
    }

    @Nested
    @DisplayName("No Access Tracking Tests")
    class NoAccessTrackingTests {

        @Test
        @DisplayName("accessCount returns 0 when tracking disabled")
        void accessCountReturnsZeroWhenTrackingDisabled() {
            VariableTtlCache<String, String> cache = VariableTtlCache.wrap(baseCache)
                    .ttlPolicy(TtlPolicy.fixed(Duration.ofMinutes(10)))
                    .build();

            cache.put("key", "value");
            cache.get("key");
            assertEquals(0, cache.accessCount("key"));
        }

        @Test
        @DisplayName("allAccessCounts returns empty when tracking disabled")
        void allAccessCountsReturnsEmptyWhenTrackingDisabled() {
            VariableTtlCache<String, String> cache = VariableTtlCache.wrap(baseCache)
                    .ttlPolicy(TtlPolicy.fixed(Duration.ofMinutes(10)))
                    .build();

            cache.put("key", "value");
            assertTrue(cache.allAccessCounts().isEmpty());
        }
    }

    @Nested
    @DisplayName("TTL Policy Tests")
    class TtlPolicyTests {

        @Test
        @DisplayName("pattern-based TTL policy works")
        void patternBasedTtlPolicyWorks() {
            VariableTtlCache<String, String> cache = VariableTtlCache.wrap(baseCache)
                    .ttlPolicy(TtlPolicy.<String, String>builder()
                            .pattern("session:*", Duration.ofHours(1))
                            .pattern("user:*", Duration.ofMinutes(30))
                            .defaultTtl(Duration.ofMinutes(10))
                            .build())
                    .build();

            cache.put("session:123", "data");
            cache.put("user:456", "data");
            cache.put("other:789", "data");

            assertNotNull(cache.get("session:123"));
            assertNotNull(cache.get("user:456"));
            assertNotNull(cache.get("other:789"));
        }

        @Test
        @DisplayName("getEffectiveTtl returns calculated TTL")
        void getEffectiveTtlReturnsCalculatedTtl() {
            VariableTtlCache<String, String> cache = VariableTtlCache.wrap(baseCache)
                    .ttlPolicy(TtlPolicy.fixed(Duration.ofMinutes(30)))
                    .build();

            cache.put("key", "value");
            Duration ttl = cache.getEffectiveTtl("key");
            assertEquals(Duration.ofMinutes(30), ttl);
        }

        @Test
        @DisplayName("getEffectiveTtl returns null for missing key")
        void getEffectiveTtlReturnsNullForMissingKey() {
            VariableTtlCache<String, String> cache = VariableTtlCache.wrap(baseCache)
                    .ttlPolicy(TtlPolicy.fixed(Duration.ofMinutes(30)))
                    .build();

            assertNull(cache.getEffectiveTtl("nonexistent"));
        }
    }

    @Nested
    @DisplayName("Decay Policy Tests")
    class DecayPolicyTests {

        @Test
        @DisplayName("decay policy affects TTL calculation")
        void decayPolicyAffectsTtlCalculation() {
            VariableTtlCache<String, String> cache = VariableTtlCache.wrap(baseCache)
                    .ttlPolicy(TtlPolicy.fixed(Duration.ofHours(1)))
                    .decayPolicy(TtlDecayPolicy.linear(Duration.ofHours(1), Duration.ofMinutes(10), 10))
                    .build();

            // Access tracking is automatically enabled with decay policy
            cache.put("key", "value");

            // Initial TTL should be based on decay (0 access = initial TTL)
            Duration initialTtl = cache.getEffectiveTtl("key");
            assertEquals(Duration.ofHours(1), initialTtl);
        }
    }

    @Nested
    @DisplayName("No Expiration Policy Tests")
    class NoExpirationPolicyTests {

        @Test
        @DisplayName("no expiration policy puts without TTL")
        void noExpirationPolicyPutsWithoutTtl() {
            VariableTtlCache<String, String> cache = VariableTtlCache.wrap(baseCache)
                    .ttlPolicy(TtlPolicy.noExpiration())
                    .build();

            cache.put("key", "value");
            assertEquals("value", cache.get("key"));
        }
    }
}
