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

package cloud.opencode.base.cache.testing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for CacheTestSupport
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("CacheTestSupport Tests")
class CacheTestSupportTest {

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("mockCache creates mock cache")
        void mockCacheCreatesMockCache() {
            CacheTestSupport.MockCache<String, String> cache = CacheTestSupport.mockCache();

            assertNotNull(cache);
            assertEquals("mock-cache", cache.name());
        }

        @Test
        @DisplayName("mockCache with clock creates mock cache")
        void mockCacheWithClockCreatesMockCache() {
            CacheTestSupport.TestClock clock = CacheTestSupport.testClock();
            CacheTestSupport.MockCache<String, String> cache = CacheTestSupport.mockCache(clock);

            assertNotNull(cache);
            assertSame(clock, cache.clock());
        }

        @Test
        @DisplayName("mockCache with name creates named mock cache")
        void mockCacheWithNameCreatesNamedMockCache() {
            CacheTestSupport.MockCache<String, String> cache = CacheTestSupport.mockCache("my-cache");

            assertEquals("my-cache", cache.name());
        }

        @Test
        @DisplayName("recordingCache creates recording cache")
        void recordingCacheCreatesRecordingCache() {
            CacheTestSupport.RecordingCache<String, String> cache = CacheTestSupport.recordingCache();

            assertNotNull(cache);
            assertEquals("recording-cache", cache.name());
        }

        @Test
        @DisplayName("testClock creates clock")
        void testClockCreatesClock() {
            CacheTestSupport.TestClock clock = CacheTestSupport.testClock();

            assertNotNull(clock);
        }

        @Test
        @DisplayName("testClock with initial time creates clock")
        void testClockWithInitialTimeCreatesClock() {
            Instant initial = Instant.parse("2024-01-01T00:00:00Z");
            CacheTestSupport.TestClock clock = CacheTestSupport.testClock(initial);

            assertEquals(initial.toEpochMilli(), clock.millis());
        }
    }

    @Nested
    @DisplayName("TestClock Tests")
    class TestClockTests {

        @Test
        @DisplayName("millis returns current time")
        void millisReturnsCurrentTime() {
            CacheTestSupport.TestClock clock = CacheTestSupport.testClock();

            assertTrue(clock.millis() > 0);
        }

        @Test
        @DisplayName("advance adds duration")
        void advanceAddsDuration() {
            CacheTestSupport.TestClock clock = CacheTestSupport.testClock();
            long before = clock.millis();

            clock.advance(Duration.ofMinutes(5));

            assertEquals(before + 300_000, clock.millis());
        }

        @Test
        @DisplayName("setTime with Instant sets time")
        void setTimeWithInstantSetsTime() {
            CacheTestSupport.TestClock clock = CacheTestSupport.testClock();
            Instant target = Instant.parse("2025-06-15T12:00:00Z");

            clock.setTime(target);

            assertEquals(target.toEpochMilli(), clock.millis());
        }

        @Test
        @DisplayName("setTime with millis sets time")
        void setTimeWithMillisSetsTime() {
            CacheTestSupport.TestClock clock = CacheTestSupport.testClock();

            clock.setTime(12345678L);

            assertEquals(12345678L, clock.millis());
        }
    }

    @Nested
    @DisplayName("MockCache Tests")
    class MockCacheTests {

        @Test
        @DisplayName("put and get work")
        void putAndGetWork() {
            CacheTestSupport.MockCache<String, String> cache = CacheTestSupport.mockCache();

            cache.put("key", "value");

            assertEquals("value", cache.get("key"));
        }

        @Test
        @DisplayName("get returns null for absent key")
        void getReturnsNullForAbsentKey() {
            CacheTestSupport.MockCache<String, String> cache = CacheTestSupport.mockCache();

            assertNull(cache.get("non-existent"));
        }

        @Test
        @DisplayName("get with loader loads absent key")
        void getWithLoaderLoadsAbsentKey() {
            CacheTestSupport.MockCache<String, String> cache = CacheTestSupport.mockCache();

            String result = cache.get("key", k -> "loaded-" + k);

            assertEquals("loaded-key", result);
            assertEquals("loaded-key", cache.get("key"));
        }

        @Test
        @DisplayName("get with loader returns existing value")
        void getWithLoaderReturnsExistingValue() {
            CacheTestSupport.MockCache<String, String> cache = CacheTestSupport.mockCache();
            cache.put("key", "existing");

            String result = cache.get("key", k -> "loaded-" + k);

            assertEquals("existing", result);
        }

        @Test
        @DisplayName("getAll returns multiple values")
        void getAllReturnsMultipleValues() {
            CacheTestSupport.MockCache<String, String> cache = CacheTestSupport.mockCache();
            cache.put("k1", "v1");
            cache.put("k2", "v2");

            Map<String, String> result = cache.getAll(List.of("k1", "k2", "k3"));

            assertEquals(2, result.size());
            assertEquals("v1", result.get("k1"));
            assertEquals("v2", result.get("k2"));
        }

        @Test
        @DisplayName("getAll with loader loads missing")
        void getAllWithLoaderLoadsMissing() {
            CacheTestSupport.MockCache<String, String> cache = CacheTestSupport.mockCache();
            cache.put("k1", "v1");

            Map<String, String> result = cache.getAll(List.of("k1", "k2"),
                    keys -> Map.of("k2", "loaded-k2"));

            assertEquals("v1", result.get("k1"));
            assertEquals("loaded-k2", result.get("k2"));
        }

        @Test
        @DisplayName("putAll adds multiple entries")
        void putAllAddsMultipleEntries() {
            CacheTestSupport.MockCache<String, String> cache = CacheTestSupport.mockCache();

            cache.putAll(Map.of("k1", "v1", "k2", "v2"));

            assertEquals("v1", cache.get("k1"));
            assertEquals("v2", cache.get("k2"));
        }

        @Test
        @DisplayName("putIfAbsent only puts if absent")
        void putIfAbsentOnlyPutsIfAbsent() {
            CacheTestSupport.MockCache<String, String> cache = CacheTestSupport.mockCache();

            assertTrue(cache.putIfAbsent("key", "value1"));
            assertFalse(cache.putIfAbsent("key", "value2"));
            assertEquals("value1", cache.get("key"));
        }

        @Test
        @DisplayName("putWithTtl stores with expiration")
        void putWithTtlStoresWithExpiration() {
            CacheTestSupport.TestClock clock = CacheTestSupport.testClock();
            CacheTestSupport.MockCache<String, String> cache = CacheTestSupport.mockCache(clock);

            cache.putWithTtl("key", "value", Duration.ofMinutes(5));

            assertEquals("value", cache.get("key"));

            clock.advance(Duration.ofMinutes(6));

            assertNull(cache.get("key"));
        }

        @Test
        @DisplayName("putAllWithTtl stores with expiration")
        void putAllWithTtlStoresWithExpiration() {
            CacheTestSupport.TestClock clock = CacheTestSupport.testClock();
            CacheTestSupport.MockCache<String, String> cache = CacheTestSupport.mockCache(clock);

            cache.putAllWithTtl(Map.of("k1", "v1", "k2", "v2"), Duration.ofMinutes(5));

            assertEquals("v1", cache.get("k1"));
            assertEquals("v2", cache.get("k2"));

            clock.advance(Duration.ofMinutes(6));

            assertNull(cache.get("k1"));
            assertNull(cache.get("k2"));
        }

        @Test
        @DisplayName("putIfAbsentWithTtl respects both conditions")
        void putIfAbsentWithTtlRespectsBothConditions() {
            CacheTestSupport.TestClock clock = CacheTestSupport.testClock();
            CacheTestSupport.MockCache<String, String> cache = CacheTestSupport.mockCache(clock);

            assertTrue(cache.putIfAbsentWithTtl("key", "value", Duration.ofMinutes(5)));
            assertFalse(cache.putIfAbsentWithTtl("key", "value2", Duration.ofMinutes(5)));

            clock.advance(Duration.ofMinutes(6));
            // cleanUp removes expired entries so putIfAbsent can succeed
            cache.cleanUp();

            assertTrue(cache.putIfAbsentWithTtl("key", "value3", Duration.ofMinutes(5)));
        }

        @Test
        @DisplayName("invalidate removes key")
        void invalidateRemovesKey() {
            CacheTestSupport.MockCache<String, String> cache = CacheTestSupport.mockCache();
            cache.put("key", "value");

            cache.invalidate("key");

            assertNull(cache.get("key"));
        }

        @Test
        @DisplayName("invalidateAll with keys removes specific keys")
        void invalidateAllWithKeysRemovesSpecificKeys() {
            CacheTestSupport.MockCache<String, String> cache = CacheTestSupport.mockCache();
            cache.put("k1", "v1");
            cache.put("k2", "v2");
            cache.put("k3", "v3");

            cache.invalidateAll(List.of("k1", "k2"));

            assertNull(cache.get("k1"));
            assertNull(cache.get("k2"));
            assertEquals("v3", cache.get("k3"));
        }

        @Test
        @DisplayName("invalidateAll removes all keys")
        void invalidateAllRemovesAllKeys() {
            CacheTestSupport.MockCache<String, String> cache = CacheTestSupport.mockCache();
            cache.put("k1", "v1");
            cache.put("k2", "v2");

            cache.invalidateAll();

            assertNull(cache.get("k1"));
            assertNull(cache.get("k2"));
        }

        @Test
        @DisplayName("containsKey returns correct result")
        void containsKeyReturnsCorrectResult() {
            CacheTestSupport.MockCache<String, String> cache = CacheTestSupport.mockCache();
            cache.put("key", "value");

            assertTrue(cache.containsKey("key"));
            assertFalse(cache.containsKey("non-existent"));
        }

        @Test
        @DisplayName("containsKey respects expiration")
        void containsKeyRespectsExpiration() {
            CacheTestSupport.TestClock clock = CacheTestSupport.testClock();
            CacheTestSupport.MockCache<String, String> cache = CacheTestSupport.mockCache(clock);

            cache.putWithTtl("key", "value", Duration.ofMinutes(5));

            assertTrue(cache.containsKey("key"));

            clock.advance(Duration.ofMinutes(6));

            assertFalse(cache.containsKey("key"));
        }

        @Test
        @DisplayName("size returns count")
        void sizeReturnsCount() {
            CacheTestSupport.MockCache<String, String> cache = CacheTestSupport.mockCache();
            cache.put("k1", "v1");
            cache.put("k2", "v2");

            assertEquals(2, cache.size());
        }

        @Test
        @DisplayName("estimatedSize returns count")
        void estimatedSizeReturnsCount() {
            CacheTestSupport.MockCache<String, String> cache = CacheTestSupport.mockCache();
            cache.put("k1", "v1");
            cache.put("k2", "v2");

            assertEquals(2, cache.estimatedSize());
        }

        @Test
        @DisplayName("keys returns all keys")
        void keysReturnsAllKeys() {
            CacheTestSupport.MockCache<String, String> cache = CacheTestSupport.mockCache();
            cache.put("k1", "v1");
            cache.put("k2", "v2");

            Set<String> keys = cache.keys();

            assertTrue(keys.contains("k1"));
            assertTrue(keys.contains("k2"));
        }

        @Test
        @DisplayName("values returns all values")
        void valuesReturnsAllValues() {
            CacheTestSupport.MockCache<String, String> cache = CacheTestSupport.mockCache();
            cache.put("k1", "v1");
            cache.put("k2", "v2");

            Collection<String> values = cache.values();

            assertTrue(values.contains("v1"));
            assertTrue(values.contains("v2"));
        }

        @Test
        @DisplayName("entries returns all entries")
        void entriesReturnsAllEntries() {
            CacheTestSupport.MockCache<String, String> cache = CacheTestSupport.mockCache();
            cache.put("k1", "v1");

            Set<Map.Entry<String, String>> entries = cache.entries();

            assertEquals(1, entries.size());
        }

        @Test
        @DisplayName("asMap returns map view")
        void asMapReturnsMapView() {
            CacheTestSupport.MockCache<String, String> cache = CacheTestSupport.mockCache();
            cache.put("k1", "v1");

            assertEquals("v1", cache.asMap().get("k1"));
        }

        @Test
        @DisplayName("stats returns stats with hit/miss counts")
        void statsReturnsStatsWithHitMissCounts() {
            CacheTestSupport.MockCache<String, String> cache = CacheTestSupport.mockCache();
            cache.put("key", "value");

            cache.get("key"); // hit
            cache.get("missing"); // miss

            assertEquals(1, cache.stats().hitCount());
            assertEquals(1, cache.stats().missCount());
        }

        @Test
        @DisplayName("metrics returns null")
        void metricsReturnsNull() {
            CacheTestSupport.MockCache<String, String> cache = CacheTestSupport.mockCache();

            assertNull(cache.metrics());
        }

        @Test
        @DisplayName("cleanUp removes expired entries")
        void cleanUpRemovesExpiredEntries() {
            CacheTestSupport.TestClock clock = CacheTestSupport.testClock();
            CacheTestSupport.MockCache<String, String> cache = CacheTestSupport.mockCache(clock);

            cache.putWithTtl("k1", "v1", Duration.ofMinutes(5));
            cache.put("k2", "v2");

            clock.advance(Duration.ofMinutes(6));
            cache.cleanUp();

            assertEquals(1, cache.size());
            assertNull(cache.get("k1"));
            assertEquals("v2", cache.get("k2"));
        }

        @Test
        @DisplayName("async throws UnsupportedOperationException")
        void asyncThrowsUnsupportedOperationException() {
            CacheTestSupport.MockCache<String, String> cache = CacheTestSupport.mockCache();

            assertThrows(UnsupportedOperationException.class, cache::async);
        }

        @Test
        @DisplayName("reset clears data and stats")
        void resetClearsDataAndStats() {
            CacheTestSupport.MockCache<String, String> cache = CacheTestSupport.mockCache();
            cache.put("key", "value");
            cache.get("key");

            cache.reset();

            assertEquals(0, cache.size());
            assertEquals(0, cache.stats().hitCount());
        }
    }

    @Nested
    @DisplayName("RecordingCache Tests")
    class RecordingCacheTests {

        @Test
        @DisplayName("records get operation")
        void recordsGetOperation() {
            CacheTestSupport.RecordingCache<String, String> cache = CacheTestSupport.recordingCache();

            cache.get("key");

            List<CacheTestSupport.CacheOperation<String, String>> ops = cache.operations();
            assertEquals(1, ops.size());
            assertTrue(ops.get(0) instanceof CacheTestSupport.CacheOperation.Get);
        }

        @Test
        @DisplayName("records get with loader operation")
        void recordsGetWithLoaderOperation() {
            CacheTestSupport.RecordingCache<String, String> cache = CacheTestSupport.recordingCache();

            cache.get("key", k -> "value");

            List<CacheTestSupport.CacheOperation<String, String>> ops = cache.operations();
            assertTrue(ops.get(0) instanceof CacheTestSupport.CacheOperation.GetWithLoader);
        }

        @Test
        @DisplayName("records getAll operation")
        void recordsGetAllOperation() {
            CacheTestSupport.RecordingCache<String, String> cache = CacheTestSupport.recordingCache();

            cache.getAll(List.of("k1", "k2"));

            assertTrue(cache.operations().get(0) instanceof CacheTestSupport.CacheOperation.GetAll);
        }

        @Test
        @DisplayName("records put operation")
        void recordsPutOperation() {
            CacheTestSupport.RecordingCache<String, String> cache = CacheTestSupport.recordingCache();

            cache.put("key", "value");

            CacheTestSupport.CacheOperation.Put<String, String> op =
                    (CacheTestSupport.CacheOperation.Put<String, String>) cache.operations().get(0);
            assertEquals("key", op.key());
            assertEquals("value", op.value());
        }

        @Test
        @DisplayName("records putAll operation")
        void recordsPutAllOperation() {
            CacheTestSupport.RecordingCache<String, String> cache = CacheTestSupport.recordingCache();

            cache.putAll(Map.of("k1", "v1"));

            assertTrue(cache.operations().get(0) instanceof CacheTestSupport.CacheOperation.PutAll);
        }

        @Test
        @DisplayName("records putIfAbsent operation")
        void recordsPutIfAbsentOperation() {
            CacheTestSupport.RecordingCache<String, String> cache = CacheTestSupport.recordingCache();

            cache.putIfAbsent("key", "value");

            assertTrue(cache.operations().get(0) instanceof CacheTestSupport.CacheOperation.PutIfAbsent);
        }

        @Test
        @DisplayName("records putWithTtl operation")
        void recordsPutWithTtlOperation() {
            CacheTestSupport.RecordingCache<String, String> cache = CacheTestSupport.recordingCache();

            cache.putWithTtl("key", "value", Duration.ofMinutes(5));

            CacheTestSupport.CacheOperation.PutWithTtl<String, String> op =
                    (CacheTestSupport.CacheOperation.PutWithTtl<String, String>) cache.operations().get(0);
            assertEquals("key", op.key());
            assertEquals(Duration.ofMinutes(5), op.ttl());
        }

        @Test
        @DisplayName("records putAllWithTtl operation")
        void recordsPutAllWithTtlOperation() {
            CacheTestSupport.RecordingCache<String, String> cache = CacheTestSupport.recordingCache();

            cache.putAllWithTtl(Map.of("k1", "v1"), Duration.ofMinutes(5));

            assertTrue(cache.operations().get(0) instanceof CacheTestSupport.CacheOperation.PutAllWithTtl);
        }

        @Test
        @DisplayName("records putIfAbsentWithTtl operation")
        void recordsPutIfAbsentWithTtlOperation() {
            CacheTestSupport.RecordingCache<String, String> cache = CacheTestSupport.recordingCache();

            cache.putIfAbsentWithTtl("key", "value", Duration.ofMinutes(5));

            assertTrue(cache.operations().get(0) instanceof CacheTestSupport.CacheOperation.PutIfAbsentWithTtl);
        }

        @Test
        @DisplayName("records invalidate operation")
        void recordsInvalidateOperation() {
            CacheTestSupport.RecordingCache<String, String> cache = CacheTestSupport.recordingCache();

            cache.invalidate("key");

            CacheTestSupport.CacheOperation.Invalidate<String, String> op =
                    (CacheTestSupport.CacheOperation.Invalidate<String, String>) cache.operations().get(0);
            assertEquals("key", op.key());
        }

        @Test
        @DisplayName("records invalidateAll operation")
        void recordsInvalidateAllOperation() {
            CacheTestSupport.RecordingCache<String, String> cache = CacheTestSupport.recordingCache();

            cache.invalidateAll(List.of("k1", "k2"));

            assertTrue(cache.operations().get(0) instanceof CacheTestSupport.CacheOperation.InvalidateAll);
        }

        @Test
        @DisplayName("records invalidateAll without keys")
        void recordsInvalidateAllWithoutKeys() {
            CacheTestSupport.RecordingCache<String, String> cache = CacheTestSupport.recordingCache();

            cache.invalidateAll();

            assertTrue(cache.operations().get(0) instanceof CacheTestSupport.CacheOperation.InvalidateAll);
        }

        @Test
        @DisplayName("operationCount returns count")
        void operationCountReturnsCount() {
            CacheTestSupport.RecordingCache<String, String> cache = CacheTestSupport.recordingCache();

            cache.put("k1", "v1");
            cache.put("k2", "v2");
            cache.get("k1");

            assertEquals(3, cache.operationCount());
        }

        @Test
        @DisplayName("clearOperations clears operations")
        void clearOperationsClearsOperations() {
            CacheTestSupport.RecordingCache<String, String> cache = CacheTestSupport.recordingCache();

            cache.put("key", "value");
            cache.clearOperations();

            assertEquals(0, cache.operationCount());
        }

        @Test
        @DisplayName("reset clears data and operations")
        void resetClearsDataAndOperations() {
            CacheTestSupport.RecordingCache<String, String> cache = CacheTestSupport.recordingCache();

            cache.put("key", "value");
            cache.reset();

            assertEquals(0, cache.size());
            assertEquals(0, cache.operationCount());
        }
    }

    @Nested
    @DisplayName("CacheOperation Record Tests")
    class CacheOperationRecordTests {

        @Test
        @DisplayName("Get record accessors work")
        void getRecordAccessorsWork() {
            CacheTestSupport.CacheOperation.Get<String, String> op = new CacheTestSupport.CacheOperation.Get<>("key");

            assertEquals("key", op.key());
        }

        @Test
        @DisplayName("GetWithLoader record accessors work")
        void getWithLoaderRecordAccessorsWork() {
            CacheTestSupport.CacheOperation.GetWithLoader<String, String> op = new CacheTestSupport.CacheOperation.GetWithLoader<>("key");

            assertEquals("key", op.key());
        }

        @Test
        @DisplayName("GetAll record accessors work")
        void getAllRecordAccessorsWork() {
            List<String> keys = List.of("k1", "k2");
            CacheTestSupport.CacheOperation.GetAll<String, String> op = new CacheTestSupport.CacheOperation.GetAll<>(keys);

            assertEquals(keys, op.keys());
        }

        @Test
        @DisplayName("Put record accessors work")
        void putRecordAccessorsWork() {
            CacheTestSupport.CacheOperation.Put<String, String> op = new CacheTestSupport.CacheOperation.Put<>("key", "value");

            assertEquals("key", op.key());
            assertEquals("value", op.value());
        }

        @Test
        @DisplayName("PutAll record accessors work")
        void putAllRecordAccessorsWork() {
            Map<String, String> map = Map.of("k1", "v1");
            CacheTestSupport.CacheOperation.PutAll<String, String> op = new CacheTestSupport.CacheOperation.PutAll<>(map);

            assertEquals(map, op.map());
        }

        @Test
        @DisplayName("PutIfAbsent record accessors work")
        void putIfAbsentRecordAccessorsWork() {
            CacheTestSupport.CacheOperation.PutIfAbsent<String, String> op = new CacheTestSupport.CacheOperation.PutIfAbsent<>("key", "value");

            assertEquals("key", op.key());
            assertEquals("value", op.value());
        }

        @Test
        @DisplayName("PutWithTtl record accessors work")
        void putWithTtlRecordAccessorsWork() {
            CacheTestSupport.CacheOperation.PutWithTtl<String, String> op = new CacheTestSupport.CacheOperation.PutWithTtl<>("key", "value", Duration.ofMinutes(5));

            assertEquals("key", op.key());
            assertEquals("value", op.value());
            assertEquals(Duration.ofMinutes(5), op.ttl());
        }

        @Test
        @DisplayName("PutAllWithTtl record accessors work")
        void putAllWithTtlRecordAccessorsWork() {
            Map<String, String> map = Map.of("k1", "v1");
            CacheTestSupport.CacheOperation.PutAllWithTtl<String, String> op = new CacheTestSupport.CacheOperation.PutAllWithTtl<>(map, Duration.ofMinutes(5));

            assertEquals(map, op.map());
            assertEquals(Duration.ofMinutes(5), op.ttl());
        }

        @Test
        @DisplayName("PutIfAbsentWithTtl record accessors work")
        void putIfAbsentWithTtlRecordAccessorsWork() {
            CacheTestSupport.CacheOperation.PutIfAbsentWithTtl<String, String> op = new CacheTestSupport.CacheOperation.PutIfAbsentWithTtl<>("key", "value", Duration.ofMinutes(5));

            assertEquals("key", op.key());
            assertEquals("value", op.value());
            assertEquals(Duration.ofMinutes(5), op.ttl());
        }

        @Test
        @DisplayName("Invalidate record accessors work")
        void invalidateRecordAccessorsWork() {
            CacheTestSupport.CacheOperation.Invalidate<String, String> op = new CacheTestSupport.CacheOperation.Invalidate<>("key");

            assertEquals("key", op.key());
        }

        @Test
        @DisplayName("InvalidateAll record accessors work")
        void invalidateAllRecordAccessorsWork() {
            List<String> keys = List.of("k1", "k2");
            CacheTestSupport.CacheOperation.InvalidateAll<String, String> op = new CacheTestSupport.CacheOperation.InvalidateAll<>(keys);

            assertEquals(keys, op.keys());
        }
    }
}
