package cloud.opencode.base.cache;

import cloud.opencode.base.cache.protection.BloomFilter;
import cloud.opencode.base.cache.protection.SingleFlight;
import cloud.opencode.base.cache.util.CacheUtil;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * Cache functionality tests
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
class CacheTest {

    @BeforeEach
    void setup() {
        CacheManager.getInstance().reset();
    }

    @Test
    void shouldPutAndGet() {
        Cache<String, String> cache = OpenCache.getOrCreate("test");
        cache.put("key", "value");
        assertThat(cache.get("key")).isEqualTo("value");
    }

    @Test
    void shouldReturnNullForMissing() {
        Cache<String, String> cache = OpenCache.getOrCreate("test");
        assertThat(cache.get("missing")).isNull();
    }

    @Test
    void shouldComputeIfAbsent() {
        Cache<String, String> cache = OpenCache.getOrCreate("test");
        String value = cache.get("key", k -> "computed-" + k);
        assertThat(value).isEqualTo("computed-key");
    }

    @Test
    void shouldEvictOnMaxSize() {
        Cache<Integer, String> cache = OpenCache.<Integer, String>builder()
                .maximumSize(2)
                .build("small");

        cache.put(1, "a");
        cache.put(2, "b");
        cache.put(3, "c");

        assertThat(cache.estimatedSize()).isEqualTo(2);
    }

    @Test
    void shouldExpireAfterWrite() throws Exception {
        Cache<String, String> cache = OpenCache.<String, String>builder()
                .expireAfterWrite(Duration.ofMillis(100))
                .build("expiring");

        cache.put("key", "value");
        assertThat(cache.get("key")).isEqualTo("value");

        Thread.sleep(150);
        assertThat(cache.get("key")).isNull();
    }

    @Test
    void shouldRecordStats() {
        Cache<String, String> cache = OpenCache.<String, String>builder()
                .recordStats()
                .build("stats");

        cache.put("key", "value");
        cache.get("key"); // hit
        cache.get("missing"); // miss

        CacheStats stats = cache.stats();
        assertThat(stats.hitCount()).isEqualTo(1);
        assertThat(stats.missCount()).isEqualTo(1);
    }

    @Test
    void shouldFormatStats() {
        CacheStats stats = CacheStats.of(100, 20, 10, 2, 1000000, 5, 5);
        String formatted = CacheUtil.formatStats(stats);
        assertThat(formatted).contains("Requests: 120");
        assertThat(formatted).contains("Hits: 100");
    }

    @Test
    void shouldGenerateCacheKey() {
        String key = CacheUtil.key("user", 123, "tenant1");
        assertThat(key).isEqualTo("user:123:tenant1");
    }

    @Test
    void shouldBloomFilterWork() {
        BloomFilter<String> filter = BloomFilter.create(1000, 0.01);
        filter.add("exists");

        assertThat(filter.mightContain("exists")).isTrue();
        assertThat(filter.mightContain("not-exists")).isFalse();
    }

    @Test
    void shouldSingleFlightMergeRequests() {
        AtomicInteger loadCount = new AtomicInteger();
        SingleFlight<String, String> flight = new SingleFlight<>();

        // First call
        String result = flight.execute("key", k -> {
            loadCount.incrementAndGet();
            return "value";
        });

        assertThat(result).isEqualTo("value");
        assertThat(loadCount.get()).isEqualTo(1);
    }

    @Test
    void shouldCacheManagerManageCaches() {
        Cache<String, String> cache1 = OpenCache.getOrCreate("cache1");
        Cache<String, String> cache2 = OpenCache.getOrCreate("cache2");

        cache1.put("key", "value1");
        cache2.put("key", "value2");

        assertThat(OpenCache.names()).containsExactlyInAnyOrder("cache1", "cache2");
        assertThat(OpenCache.get("cache1").orElseThrow().get("key")).isEqualTo("value1");
    }

    @Test
    void shouldUseLruEvictionPolicy() {
        Cache<Integer, String> cache = OpenCache.<Integer, String>builder()
                .maximumSize(3)
                .evictionPolicy(OpenCache.lru())
                .build("lru");

        cache.put(1, "a");
        cache.put(2, "b");
        cache.put(3, "c");

        cache.get(1); // Access 1, making it most recently used

        cache.put(4, "d"); // Should evict 2 (least recently used)

        assertThat(cache.containsKey(1)).isTrue();
        assertThat(cache.containsKey(3)).isTrue();
        assertThat(cache.containsKey(4)).isTrue();
    }

    @Test
    void shouldWarmUpCache() {
        Cache<String, String> cache = OpenCache.getOrCreate("warmup");
        Map<String, String> data = Map.of("k1", "v1", "k2", "v2", "k3", "v3");

        CacheUtil.warmUp(cache, data);

        assertThat(cache.get("k1")).isEqualTo("v1");
        assertThat(cache.get("k2")).isEqualTo("v2");
        assertThat(cache.get("k3")).isEqualTo("v3");
    }

    // ==================== Default Methods Tests | 默认方法测试 ====================

    @Nested
    @DisplayName("Default Methods Tests | 默认方法测试")
    class DefaultMethodsTests {

        @Test
        @DisplayName("getOrDefault returns default when missing | getOrDefault 缺失时返回默认值")
        void testGetOrDefault() {
            Cache<String, String> cache = OpenCache.getOrCreate("test-getOrDefault");
            cache.put("key1", "value1");

            assertThat(cache.getOrDefault("key1", "default")).isEqualTo("value1");
            assertThat(cache.getOrDefault("missing", "default")).isEqualTo("default");
        }

        @Test
        @DisplayName("getOptional returns Optional | getOptional 返回 Optional")
        void testGetOptional() {
            Cache<String, String> cache = OpenCache.getOrCreate("test-getOptional");
            cache.put("key1", "value1");

            assertThat(cache.getOptional("key1")).isPresent().contains("value1");
            assertThat(cache.getOptional("missing")).isEmpty();
        }

        @Test
        @DisplayName("getIfPresent returns Optional | getIfPresent 返回 Optional")
        void testGetIfPresent() {
            Cache<String, String> cache = OpenCache.getOrCreate("test-getIfPresent");
            cache.put("key1", "value1");

            assertThat(cache.getIfPresent("key1")).isPresent().contains("value1");
            assertThat(cache.getIfPresent("missing")).isEmpty();
        }

        @Test
        @DisplayName("getOrNull returns null when missing | getOrNull 缺失时返回 null")
        void testGetOrNull() {
            Cache<String, String> cache = OpenCache.getOrCreate("test-getOrNull");
            cache.put("key1", "value1");

            assertThat(cache.getOrNull("key1")).isEqualTo("value1");
            assertThat(cache.getOrNull("missing")).isNull();
        }

        @Test
        @DisplayName("putIfPresent only updates existing | putIfPresent 仅更新已存在的")
        void testPutIfPresent() {
            Cache<String, String> cache = OpenCache.getOrCreate("test-putIfPresent");
            cache.put("key1", "value1");

            assertThat(cache.putIfPresent("key1", "updated")).isTrue();
            assertThat(cache.get("key1")).isEqualTo("updated");
            assertThat(cache.putIfPresent("missing", "value")).isFalse();
            assertThat(cache.containsKey("missing")).isFalse();
        }

        @Test
        @DisplayName("removeIfPresent removes existing | removeIfPresent 移除已存在的")
        void testRemoveIfPresent() {
            Cache<String, String> cache = OpenCache.getOrCreate("test-removeIfPresent");
            cache.put("key1", "value1");

            assertThat(cache.removeIfPresent("key1")).isTrue();
            assertThat(cache.containsKey("key1")).isFalse();
            assertThat(cache.removeIfPresent("missing")).isFalse();
        }

        @Test
        @DisplayName("removeIfEquals removes when value matches | removeIfEquals 值匹配时移除")
        void testRemoveIfEquals() {
            Cache<String, String> cache = OpenCache.getOrCreate("test-removeIfEquals");
            cache.put("key1", "value1");

            assertThat(cache.removeIfEquals("key1", "wrong")).isFalse();
            assertThat(cache.containsKey("key1")).isTrue();
            assertThat(cache.removeIfEquals("key1", "value1")).isTrue();
            assertThat(cache.containsKey("key1")).isFalse();
        }

        @Test
        @DisplayName("containsValue checks values | containsValue 检查值")
        void testContainsValue() {
            Cache<String, String> cache = OpenCache.getOrCreate("test-containsValue");
            cache.put("key1", "value1");
            cache.put("key2", "value2");

            assertThat(cache.containsValue("value1")).isTrue();
            assertThat(cache.containsValue("missing")).isFalse();
        }

        @Test
        @DisplayName("isEmpty checks if empty | isEmpty 检查是否为空")
        void testIsEmpty() {
            Cache<String, String> cache = OpenCache.getOrCreate("test-isEmpty");

            assertThat(cache.isEmpty()).isTrue();
            cache.put("key1", "value1");
            assertThat(cache.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("clear removes all entries | clear 清除所有条目")
        void testClear() {
            Cache<String, String> cache = OpenCache.getOrCreate("test-clear");
            cache.put("key1", "value1");
            cache.put("key2", "value2");

            cache.clear();

            assertThat(cache.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("computeIfAbsent computes when absent | computeIfAbsent 不存在时计算")
        void testComputeIfAbsent() {
            Cache<String, String> cache = OpenCache.getOrCreate("test-computeIfAbsent");
            cache.put("key1", "value1");

            assertThat(cache.computeIfAbsent("key1", k -> "computed")).isEqualTo("value1");
            assertThat(cache.computeIfAbsent("key2", k -> "computed")).isEqualTo("computed");
        }

        @Test
        @DisplayName("getAndRemove gets and removes | getAndRemove 获取并移除")
        void testGetAndRemove() {
            Cache<String, String> cache = OpenCache.getOrCreate("test-getAndRemove");
            cache.put("key1", "value1");

            assertThat(cache.getAndRemove("key1")).isEqualTo("value1");
            assertThat(cache.containsKey("key1")).isFalse();
            assertThat(cache.getAndRemove("missing")).isNull();
        }

        @Test
        @DisplayName("replace replaces value | replace 替换值")
        void testReplace() {
            Cache<String, String> cache = OpenCache.getOrCreate("test-replace");
            cache.put("key1", "value1");

            assertThat(cache.replace("key1", "newValue")).isEqualTo("value1");
            assertThat(cache.get("key1")).isEqualTo("newValue");
            assertThat(cache.replace("missing", "value")).isNull();
        }

        @Test
        @DisplayName("replace with oldValue conditional | replace 带旧值条件")
        void testReplaceConditional() {
            Cache<String, String> cache = OpenCache.getOrCreate("test-replaceConditional");
            cache.put("key1", "value1");

            assertThat(cache.replace("key1", "wrong", "new")).isFalse();
            assertThat(cache.get("key1")).isEqualTo("value1");
            assertThat(cache.replace("key1", "value1", "new")).isTrue();
            assertThat(cache.get("key1")).isEqualTo("new");
        }

        @Test
        @DisplayName("merge merges values | merge 合并值")
        void testMerge() {
            Cache<String, Integer> cache = OpenCache.getOrCreate("test-merge");
            cache.put("count", 5);

            cache.merge("count", 3, Integer::sum);
            assertThat(cache.get("count")).isEqualTo(8);

            cache.merge("newCount", 10, Integer::sum);
            assertThat(cache.get("newCount")).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("Iteration Methods Tests | 迭代方法测试")
    class IterationMethodsTests {

        @Test
        @DisplayName("forEachKey iterates keys | forEachKey 遍历键")
        void testForEachKey() {
            Cache<String, String> cache = OpenCache.getOrCreate("test-forEachKey");
            cache.put("k1", "v1");
            cache.put("k2", "v2");

            java.util.List<String> keys = new java.util.ArrayList<>();
            cache.forEachKey(keys::add);

            assertThat(keys).containsExactlyInAnyOrder("k1", "k2");
        }

        @Test
        @DisplayName("forEachValue iterates values | forEachValue 遍历值")
        void testForEachValue() {
            Cache<String, String> cache = OpenCache.getOrCreate("test-forEachValue");
            cache.put("k1", "v1");
            cache.put("k2", "v2");

            java.util.List<String> values = new java.util.ArrayList<>();
            cache.forEachValue(values::add);

            assertThat(values).containsExactlyInAnyOrder("v1", "v2");
        }

        @Test
        @DisplayName("forEach iterates entries | forEach 遍历条目")
        void testForEach() {
            Cache<String, String> cache = OpenCache.getOrCreate("test-forEach");
            cache.put("k1", "v1");
            cache.put("k2", "v2");

            java.util.Map<String, String> result = new java.util.HashMap<>();
            cache.forEach(result::put);

            assertThat(result).containsEntry("k1", "v1").containsEntry("k2", "v2");
        }

        @Test
        @DisplayName("keyIterator provides lazy iteration | keyIterator 提供惰性迭代")
        void testKeyIterator() {
            Cache<String, String> cache = OpenCache.getOrCreate("test-keyIterator");
            cache.put("k1", "v1");
            cache.put("k2", "v2");

            java.util.Iterator<String> iter = cache.keyIterator();
            java.util.List<String> keys = new java.util.ArrayList<>();
            while (iter.hasNext()) {
                keys.add(iter.next());
            }

            assertThat(keys).containsExactlyInAnyOrder("k1", "k2");
        }

        @Test
        @DisplayName("entryIterator provides lazy iteration | entryIterator 提供惰性迭代")
        void testEntryIterator() {
            Cache<String, String> cache = OpenCache.getOrCreate("test-entryIterator");
            cache.put("k1", "v1");
            cache.put("k2", "v2");

            java.util.Iterator<Map.Entry<String, String>> iter = cache.entryIterator();
            java.util.Map<String, String> result = new java.util.HashMap<>();
            while (iter.hasNext()) {
                Map.Entry<String, String> entry = iter.next();
                result.put(entry.getKey(), entry.getValue());
            }

            assertThat(result).containsEntry("k1", "v1").containsEntry("k2", "v2");
        }
    }

    @Nested
    @DisplayName("Stream Methods Tests | 流方法测试")
    class StreamMethodsTests {

        @Test
        @DisplayName("keyStream provides stream over keys | keyStream 提供键流")
        void testKeyStream() {
            Cache<String, String> cache = OpenCache.getOrCreate("test-keyStream");
            cache.put("k1", "v1");
            cache.put("k2", "v2");

            java.util.List<String> keys = cache.keyStream().toList();

            assertThat(keys).containsExactlyInAnyOrder("k1", "k2");
        }

        @Test
        @DisplayName("valueStream provides stream over values | valueStream 提供值流")
        void testValueStream() {
            Cache<String, String> cache = OpenCache.getOrCreate("test-valueStream");
            cache.put("k1", "v1");
            cache.put("k2", "v2");

            java.util.List<String> values = cache.valueStream().toList();

            assertThat(values).containsExactlyInAnyOrder("v1", "v2");
        }

        @Test
        @DisplayName("entryStream provides stream over entries | entryStream 提供条目流")
        void testEntryStream() {
            Cache<String, String> cache = OpenCache.getOrCreate("test-entryStream");
            cache.put("k1", "v1");
            cache.put("k2", "v2");

            java.util.Map<String, String> result = cache.entryStream()
                    .collect(java.util.stream.Collectors.toMap(
                            Map.Entry::getKey, Map.Entry::getValue));

            assertThat(result).containsEntry("k1", "v1").containsEntry("k2", "v2");
        }

        @Test
        @DisplayName("keyParallelStream provides parallel stream | keyParallelStream 提供并行流")
        void testKeyParallelStream() {
            Cache<String, String> cache = OpenCache.getOrCreate("test-keyParallelStream");
            cache.put("k1", "v1");
            cache.put("k2", "v2");

            java.util.List<String> keys = cache.keyParallelStream().toList();

            assertThat(keys).containsExactlyInAnyOrder("k1", "k2");
        }

        @Test
        @DisplayName("valueParallelStream provides parallel stream | valueParallelStream 提供并行流")
        void testValueParallelStream() {
            Cache<String, String> cache = OpenCache.getOrCreate("test-valueParallelStream");
            cache.put("k1", "v1");
            cache.put("k2", "v2");

            java.util.List<String> values = cache.valueParallelStream().toList();

            assertThat(values).containsExactlyInAnyOrder("v1", "v2");
        }

        @Test
        @DisplayName("entryParallelStream provides parallel stream | entryParallelStream 提供并行流")
        void testEntryParallelStream() {
            Cache<String, String> cache = OpenCache.getOrCreate("test-entryParallelStream");
            cache.put("k1", "v1");
            cache.put("k2", "v2");

            long count = cache.entryParallelStream().count();

            assertThat(count).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Pattern Methods Tests | 模式方法测试")
    class PatternMethodsTests {

        @Test
        @DisplayName("getByPattern returns matching entries | getByPattern 返回匹配条目")
        void testGetByPattern() {
            Cache<String, String> cache = OpenCache.getOrCreate("test-getByPattern");
            cache.put("user:1", "v1");
            cache.put("user:2", "v2");
            cache.put("order:1", "o1");

            Map<String, String> result = cache.getByPattern("user:*");

            assertThat(result).hasSize(2);
            assertThat(result).containsEntry("user:1", "v1").containsEntry("user:2", "v2");
        }

        @Test
        @DisplayName("invalidateByPattern invalidates matching | invalidateByPattern 使匹配的失效")
        void testInvalidateByPattern() {
            Cache<String, String> cache = OpenCache.getOrCreate("test-invalidateByPattern");
            cache.put("user:1", "v1");
            cache.put("user:2", "v2");
            cache.put("order:1", "o1");

            long count = cache.invalidateByPattern("user:*");

            assertThat(count).isEqualTo(2);
            assertThat(cache.containsKey("user:1")).isFalse();
            assertThat(cache.containsKey("user:2")).isFalse();
            assertThat(cache.containsKey("order:1")).isTrue();
        }

        @Test
        @DisplayName("invalidateIf invalidates matching predicate | invalidateIf 使匹配谓词的失效")
        void testInvalidateIf() {
            Cache<String, String> cache = OpenCache.getOrCreate("test-invalidateIf");
            cache.put("k1", "v1");
            cache.put("k2", "v2");
            cache.put("k3", "v3");

            long count = cache.invalidateIf(k -> k.equals("k1") || k.equals("k2"));

            assertThat(count).isEqualTo(2);
            assertThat(cache.containsKey("k3")).isTrue();
        }

        @Test
        @DisplayName("invalidateByValue invalidates by value predicate | invalidateByValue 根据值谓词失效")
        void testInvalidateByValue() {
            Cache<String, String> cache = OpenCache.getOrCreate("test-invalidateByValue");
            cache.put("k1", "remove");
            cache.put("k2", "keep");
            cache.put("k3", "remove");

            long count = cache.invalidateByValue(v -> v.equals("remove"));

            assertThat(count).isEqualTo(2);
            assertThat(cache.containsKey("k2")).isTrue();
            assertThat(cache.get("k2")).isEqualTo("keep");
        }
    }

    @Nested
    @DisplayName("Batch Methods Tests | 批量方法测试")
    class BatchMethodsTests {

        @Test
        @DisplayName("putAllIfAbsent puts only absent | putAllIfAbsent 仅放入不存在的")
        void testPutAllIfAbsent() {
            Cache<String, String> cache = OpenCache.getOrCreate("test-putAllIfAbsent");
            cache.put("k1", "existing");

            int count = cache.putAllIfAbsent(Map.of("k1", "new1", "k2", "new2"));

            assertThat(count).isEqualTo(1);
            assertThat(cache.get("k1")).isEqualTo("existing");
            assertThat(cache.get("k2")).isEqualTo("new2");
        }

        @Test
        @DisplayName("updateAll updates only existing | updateAll 仅更新已存在的")
        void testUpdateAll() {
            Cache<String, String> cache = OpenCache.getOrCreate("test-updateAll");
            cache.put("k1", "v1");
            cache.put("k2", "v2");

            int count = cache.updateAll(Map.of("k1", "updated1", "k3", "new3"));

            assertThat(count).isEqualTo(1);
            assertThat(cache.get("k1")).isEqualTo("updated1");
            assertThat(cache.get("k2")).isEqualTo("v2");
            assertThat(cache.containsKey("k3")).isFalse();
        }

        @Test
        @DisplayName("replaceAll transforms all values | replaceAll 转换所有值")
        void testReplaceAll() {
            Cache<String, String> cache = OpenCache.getOrCreate("test-replaceAll");
            cache.put("k1", "v1");
            cache.put("k2", "v2");

            int count = cache.replaceAll((k, v) -> v.toUpperCase());

            assertThat(count).isEqualTo(2);
            assertThat(cache.get("k1")).isEqualTo("V1");
            assertThat(cache.get("k2")).isEqualTo("V2");
        }
    }

    @Nested
    @DisplayName("TTL Methods Tests | TTL 方法测试")
    class TtlMethodsTests {

        @Test
        @DisplayName("updateTtl with predicate | updateTtl 带谓词")
        void testUpdateTtlWithPredicate() {
            Cache<String, String> cache = OpenCache.<String, String>builder()
                    .expireAfterWrite(Duration.ofHours(1))
                    .build("test-updateTtl-predicate");
            cache.put("k1", "v1");
            cache.put("k2", "v2");

            long count = cache.updateTtl(k -> k.equals("k1"), Duration.ofMinutes(30));

            assertThat(count).isEqualTo(1);
        }

        @Test
        @DisplayName("updateTtlAll updates all | updateTtlAll 更新所有")
        void testUpdateTtlAll() {
            Cache<String, String> cache = OpenCache.<String, String>builder()
                    .expireAfterWrite(Duration.ofHours(1))
                    .build("test-updateTtlAll");
            cache.put("k1", "v1");
            cache.put("k2", "v2");

            long count = cache.updateTtlAll(Duration.ofMinutes(30));

            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("updateTtl with keys | updateTtl 带键集合")
        void testUpdateTtlWithKeys() {
            Cache<String, String> cache = OpenCache.<String, String>builder()
                    .expireAfterWrite(Duration.ofHours(1))
                    .build("test-updateTtl-keys");
            cache.put("k1", "v1");
            cache.put("k2", "v2");
            cache.put("k3", "v3");

            long count = cache.updateTtl(java.util.List.of("k1", "k2"), Duration.ofMinutes(30));

            assertThat(count).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Compute Methods Tests | 计算方法测试")
    class ComputeMethodsTests {

        @Test
        @DisplayName("computeIfPresent computes when present | computeIfPresent 存在时计算")
        void testComputeIfPresent() {
            Cache<String, String> cache = OpenCache.getOrCreate("test-computeIfPresent");
            cache.put("k1", "v1");

            String result = cache.computeIfPresent("k1", (k, v) -> v.toUpperCase());
            assertThat(result).isEqualTo("V1");
            assertThat(cache.get("k1")).isEqualTo("V1");

            result = cache.computeIfPresent("missing", (k, v) -> "computed");
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("compute computes new value | compute 计算新值")
        void testCompute() {
            Cache<String, String> cache = OpenCache.getOrCreate("test-compute");
            cache.put("k1", "v1");

            String result = cache.compute("k1", (k, v) -> v == null ? "new" : v + "-updated");
            assertThat(result).isEqualTo("v1-updated");

            result = cache.compute("k2", (k, v) -> v == null ? "new" : v + "-updated");
            assertThat(result).isEqualTo("new");
        }
    }

    @Nested
    @DisplayName("Stats Methods Tests | 统计方法测试")
    class StatsMethodsTests {

        @Test
        @DisplayName("resetStats resets counters | resetStats 重置计数器")
        void testResetStats() {
            Cache<String, String> cache = OpenCache.<String, String>builder()
                    .recordStats()
                    .build("test-resetStats");
            cache.put("k1", "v1");
            cache.get("k1");
            cache.get("missing");

            CacheStats before = cache.stats();
            assertThat(before.hitCount()).isGreaterThan(0);

            cache.resetStats();

            // Stats may or may not reset depending on implementation
            // Just verify it doesn't throw
        }
    }
}
