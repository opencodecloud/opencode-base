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

package cloud.opencode.base.cache.analysis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for AccessPatternAnalyzer
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
@DisplayName("AccessPatternAnalyzer Tests")
class AccessPatternAnalyzerTest {

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("builder creates analyzer")
        void builderCreatesAnalyzer() {
            AccessPatternAnalyzer<String> analyzer = AccessPatternAnalyzer.<String>builder()
                    .hotKeyThreshold(100)
                    .coldDataAge(Duration.ofHours(1))
                    .topKSize(10)
                    .windowDuration(Duration.ofHours(24))
                    .build();
            assertNotNull(analyzer);
        }

        @Test
        @DisplayName("create returns default analyzer")
        void createReturnsDefaultAnalyzer() {
            AccessPatternAnalyzer<String> analyzer = AccessPatternAnalyzer.create();
            assertNotNull(analyzer);
        }
    }

    @Nested
    @DisplayName("Recording Tests")
    class RecordingTests {

        private AccessPatternAnalyzer<String> analyzer;

        @BeforeEach
        void setUp() {
            analyzer = AccessPatternAnalyzer.<String>builder()
                    .hotKeyThreshold(10)
                    .build();
        }

        @Test
        @DisplayName("recordAccess records single access")
        void recordAccessRecordsSingleAccess() {
            analyzer.recordAccess("key1");

            assertEquals(1, analyzer.getAccessCount("key1"));
        }

        @Test
        @DisplayName("recordAccess with count records multiple")
        void recordAccessWithCountRecordsMultiple() {
            analyzer.recordAccess("key1", 5);

            assertEquals(5, analyzer.getAccessCount("key1"));
        }

        @Test
        @DisplayName("recordAccess throws on null key")
        void recordAccessThrowsOnNullKey() {
            assertThrows(NullPointerException.class, () -> analyzer.recordAccess(null));
        }

        @Test
        @DisplayName("recordMiss records miss")
        void recordMissRecordsMiss() {
            analyzer.recordMiss("key1");
            analyzer.recordMiss("key1");

            assertEquals(2, analyzer.getMissCount("key1"));
        }

        @Test
        @DisplayName("recordMiss throws on null key")
        void recordMissThrowsOnNullKey() {
            assertThrows(NullPointerException.class, () -> analyzer.recordMiss(null));
        }
    }

    @Nested
    @DisplayName("Hot Keys Tests")
    class HotKeysTests {

        @Test
        @DisplayName("getHotKeys returns frequently accessed keys")
        void getHotKeysReturnsFrequentlyAccessedKeys() {
            AccessPatternAnalyzer<String> analyzer = AccessPatternAnalyzer.<String>builder()
                    .hotKeyThreshold(10)
                    .build();

            analyzer.recordAccess("hot1", 15);
            analyzer.recordAccess("hot2", 12);
            analyzer.recordAccess("cold", 5);

            Set<String> hotKeys = analyzer.getHotKeys();
            assertEquals(2, hotKeys.size());
            assertTrue(hotKeys.contains("hot1"));
            assertTrue(hotKeys.contains("hot2"));
            assertFalse(hotKeys.contains("cold"));
        }
    }

    @Nested
    @DisplayName("Cold Keys Tests")
    class ColdKeysTests {

        @Test
        @DisplayName("getColdKeys returns keys not accessed recently")
        void getColdKeysReturnsKeysNotAccessedRecently() throws InterruptedException {
            AccessPatternAnalyzer<String> analyzer = AccessPatternAnalyzer.<String>builder()
                    .coldDataAge(Duration.ofMillis(50))
                    .build();

            analyzer.recordAccess("old");
            Thread.sleep(100);
            analyzer.recordAccess("recent");

            Set<String> coldKeys = analyzer.getColdKeys();
            assertTrue(coldKeys.contains("old"));
            assertFalse(coldKeys.contains("recent"));
        }
    }

    @Nested
    @DisplayName("Top-K Tests")
    class TopKTests {

        private AccessPatternAnalyzer<String> analyzer;

        @BeforeEach
        void setUp() {
            analyzer = AccessPatternAnalyzer.<String>builder()
                    .topKSize(3)
                    .build();
        }

        @Test
        @DisplayName("getTopK returns most accessed keys")
        void getTopKReturnsMostAccessedKeys() {
            analyzer.recordAccess("k1", 100);
            analyzer.recordAccess("k2", 50);
            analyzer.recordAccess("k3", 30);
            analyzer.recordAccess("k4", 10);

            List<AccessPatternAnalyzer.KeyAccessCount<String>> topK = analyzer.getTopK();

            assertEquals(3, topK.size());
            assertEquals("k1", topK.get(0).key());
            assertEquals(100, topK.get(0).count());
            assertEquals("k2", topK.get(1).key());
            assertEquals("k3", topK.get(2).key());
        }

        @Test
        @DisplayName("getTopK with custom K")
        void getTopKWithCustomK() {
            analyzer.recordAccess("k1", 100);
            analyzer.recordAccess("k2", 50);
            analyzer.recordAccess("k3", 30);

            List<AccessPatternAnalyzer.KeyAccessCount<String>> topK = analyzer.getTopK(2);

            assertEquals(2, topK.size());
        }

        @Test
        @DisplayName("getBottomK returns least accessed keys")
        void getBottomKReturnsLeastAccessedKeys() {
            analyzer.recordAccess("k1", 100);
            analyzer.recordAccess("k2", 50);
            analyzer.recordAccess("k3", 30);
            analyzer.recordAccess("k4", 10);

            List<AccessPatternAnalyzer.KeyAccessCount<String>> bottomK = analyzer.getBottomK(2);

            assertEquals(2, bottomK.size());
            assertEquals("k4", bottomK.get(0).key());
            assertEquals("k3", bottomK.get(1).key());
        }
    }

    @Nested
    @DisplayName("Access Query Tests")
    class AccessQueryTests {

        private AccessPatternAnalyzer<String> analyzer;

        @BeforeEach
        void setUp() {
            analyzer = AccessPatternAnalyzer.create();
        }

        @Test
        @DisplayName("getAccessCount returns count")
        void getAccessCountReturnsCount() {
            analyzer.recordAccess("key", 10);
            assertEquals(10, analyzer.getAccessCount("key"));
        }

        @Test
        @DisplayName("getAccessCount returns 0 for unknown key")
        void getAccessCountReturnsZeroForUnknownKey() {
            assertEquals(0, analyzer.getAccessCount("unknown"));
        }

        @Test
        @DisplayName("getMissCount returns count")
        void getMissCountReturnsCount() {
            analyzer.recordMiss("key");
            analyzer.recordMiss("key");
            assertEquals(2, analyzer.getMissCount("key"));
        }

        @Test
        @DisplayName("getMissCount returns 0 for unknown key")
        void getMissCountReturnsZeroForUnknownKey() {
            assertEquals(0, analyzer.getMissCount("unknown"));
        }

        @Test
        @DisplayName("getLastAccess returns last access time")
        void getLastAccessReturnsLastAccessTime() {
            analyzer.recordAccess("key");
            Instant lastAccess = analyzer.getLastAccess("key");
            assertNotNull(lastAccess);
            assertTrue(lastAccess.isAfter(Instant.now().minusSeconds(5)));
        }

        @Test
        @DisplayName("getLastAccess returns null for unknown key")
        void getLastAccessReturnsNullForUnknownKey() {
            assertNull(analyzer.getLastAccess("unknown"));
        }
    }

    @Nested
    @DisplayName("Analysis Report Tests")
    class AnalysisReportTests {

        @Test
        @DisplayName("analyze generates report")
        void analyzeGeneratesReport() {
            AccessPatternAnalyzer<String> analyzer = AccessPatternAnalyzer.<String>builder()
                    .hotKeyThreshold(10)
                    .topKSize(5)
                    .build();

            analyzer.recordAccess("hot", 20);
            analyzer.recordAccess("cold", 5);

            AccessPatternAnalyzer.AccessPatternReport<String> report = analyzer.analyze();

            assertNotNull(report);
            assertEquals(2, report.totalKeys());
            assertEquals(25, report.totalAccesses());
            assertEquals(12.5, report.averageAccessPerKey());
            assertEquals(1, report.hotKeyCount());
            assertTrue(report.hotKeys().contains("hot"));
        }

        @Test
        @DisplayName("report hotKeyPercentage calculation")
        void reportHotKeyPercentageCalculation() {
            AccessPatternAnalyzer<String> analyzer = AccessPatternAnalyzer.<String>builder()
                    .hotKeyThreshold(10)
                    .build();

            analyzer.recordAccess("hot", 20);
            analyzer.recordAccess("cold", 5);

            var report = analyzer.analyze();
            assertEquals(50.0, report.hotKeyPercentage());
        }

        @Test
        @DisplayName("report coldKeyPercentage calculation")
        void reportColdKeyPercentageCalculation() throws InterruptedException {
            AccessPatternAnalyzer<String> analyzer = AccessPatternAnalyzer.<String>builder()
                    .coldDataAge(Duration.ofMillis(50))
                    .build();

            analyzer.recordAccess("old");
            Thread.sleep(100);

            var report = analyzer.analyze();
            assertEquals(100.0, report.coldKeyPercentage());
        }

        @Test
        @DisplayName("report isSkewed detection")
        void reportIsSkewedDetection() {
            AccessPatternAnalyzer<String> analyzer = AccessPatternAnalyzer.create();

            // Create skewed distribution
            analyzer.recordAccess("hot", 900);
            for (int i = 0; i < 9; i++) {
                analyzer.recordAccess("cold" + i, 10);
            }

            var report = analyzer.analyze();
            assertTrue(report.isSkewed());
        }

        @Test
        @DisplayName("report toMap for serialization")
        void reportToMapForSerialization() {
            AccessPatternAnalyzer<String> analyzer = AccessPatternAnalyzer.create();
            analyzer.recordAccess("key", 10);

            var report = analyzer.analyze();
            Map<String, Object> map = report.toMap();

            assertNotNull(map.get("analysisTime"));
            assertEquals(1L, map.get("totalKeys"));
            assertEquals(10L, map.get("totalAccesses"));
        }
    }

    @Nested
    @DisplayName("Management Tests")
    class ManagementTests {

        private AccessPatternAnalyzer<String> analyzer;

        @BeforeEach
        void setUp() {
            analyzer = AccessPatternAnalyzer.create();
        }

        @Test
        @DisplayName("remove removes key tracking")
        void removeRemovesKeyTracking() {
            analyzer.recordAccess("key", 10);
            analyzer.remove("key");

            assertEquals(0, analyzer.getAccessCount("key"));
        }

        @Test
        @DisplayName("clear removes all tracking")
        void clearRemovesAllTracking() {
            analyzer.recordAccess("k1", 10);
            analyzer.recordAccess("k2", 20);
            analyzer.clear();

            assertEquals(0, analyzer.size());
        }

        @Test
        @DisplayName("pruneCold removes old entries")
        void pruneColdRemovesOldEntries() throws InterruptedException {
            analyzer.recordAccess("old");
            Thread.sleep(100);
            analyzer.recordAccess("recent");

            int pruned = analyzer.pruneCold(Duration.ofMillis(50));

            assertEquals(1, pruned);
            assertEquals(1, analyzer.size());
        }

        @Test
        @DisplayName("size returns tracked key count")
        void sizeReturnsTrackedKeyCount() {
            analyzer.recordAccess("k1");
            analyzer.recordAccess("k2");
            analyzer.recordAccess("k3");

            assertEquals(3, analyzer.size());
        }
    }

    @Nested
    @DisplayName("KeyAccessCount Tests")
    class KeyAccessCountTests {

        @Test
        @DisplayName("KeyAccessCount record accessors work")
        void keyAccessCountRecordAccessorsWork() {
            AccessPatternAnalyzer.KeyAccessCount<String> kac =
                    new AccessPatternAnalyzer.KeyAccessCount<>("key", 100);

            assertEquals("key", kac.key());
            assertEquals(100, kac.count());
        }
    }

    @Nested
    @DisplayName("Empty Analyzer Tests")
    class EmptyAnalyzerTests {

        @Test
        @DisplayName("analyze on empty returns valid report")
        void analyzeOnEmptyReturnsValidReport() {
            AccessPatternAnalyzer<String> analyzer = AccessPatternAnalyzer.create();

            var report = analyzer.analyze();

            assertEquals(0, report.totalKeys());
            assertEquals(0, report.totalAccesses());
            assertEquals(0.0, report.averageAccessPerKey());
        }
    }
}
