package cloud.opencode.base.core.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * InternalCache 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("InternalCache 测试")
class InternalCacheTest {

    private InternalCache<String, String> cache;

    @BeforeEach
    void setUp() {
        cache = new TestInternalCache<>();
    }

    @Nested
    @DisplayName("get 测试")
    class GetTests {

        @Test
        @DisplayName("get 存在的键")
        void testGetExisting() {
            cache.put("key", "value");
            assertThat(cache.get("key")).isEqualTo("value");
        }

        @Test
        @DisplayName("get 不存在的键")
        void testGetNonExisting() {
            assertThat(cache.get("nonExistent")).isNull();
        }
    }

    @Nested
    @DisplayName("getOptional 测试")
    class GetOptionalTests {

        @Test
        @DisplayName("getOptional 存在的键")
        void testGetOptionalExisting() {
            cache.put("key", "value");
            Optional<String> result = cache.getOptional("key");
            assertThat(result).isPresent().contains("value");
        }

        @Test
        @DisplayName("getOptional 不存在的键")
        void testGetOptionalNonExisting() {
            Optional<String> result = cache.getOptional("nonExistent");
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("getOptional null 值")
        void testGetOptionalNull() {
            cache.put("key", null);
            Optional<String> result = cache.getOptional("key");
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("computeIfAbsent 测试")
    class ComputeIfAbsentTests {

        @Test
        @DisplayName("computeIfAbsent 不存在时计算")
        void testComputeIfAbsentNew() {
            AtomicInteger computeCount = new AtomicInteger(0);

            String result = cache.computeIfAbsent("key", k -> {
                computeCount.incrementAndGet();
                return "computed-" + k;
            });

            assertThat(result).isEqualTo("computed-key");
            assertThat(computeCount.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("computeIfAbsent 存在时不计算")
        void testComputeIfAbsentExisting() {
            cache.put("key", "existing");
            AtomicInteger computeCount = new AtomicInteger(0);

            String result = cache.computeIfAbsent("key", k -> {
                computeCount.incrementAndGet();
                return "computed";
            });

            assertThat(result).isEqualTo("existing");
            assertThat(computeCount.get()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("put 测试")
    class PutTests {

        @Test
        @DisplayName("put 新键")
        void testPutNew() {
            String result = cache.put("key", "value");
            assertThat(result).isNull();
            assertThat(cache.get("key")).isEqualTo("value");
        }

        @Test
        @DisplayName("put 返回旧值")
        void testPutReturnsOldValue() {
            cache.put("key", "old");
            String result = cache.put("key", "new");
            assertThat(result).isEqualTo("old");
            assertThat(cache.get("key")).isEqualTo("new");
        }
    }

    @Nested
    @DisplayName("putIfAbsent 测试")
    class PutIfAbsentTests {

        @Test
        @DisplayName("putIfAbsent 不存在时放入")
        void testPutIfAbsentNew() {
            String result = cache.putIfAbsent("key", "value");
            assertThat(result).isNull();
            assertThat(cache.get("key")).isEqualTo("value");
        }

        @Test
        @DisplayName("putIfAbsent 存在时不放入")
        void testPutIfAbsentExisting() {
            cache.put("key", "existing");
            String result = cache.putIfAbsent("key", "new");
            assertThat(result).isEqualTo("existing");
            assertThat(cache.get("key")).isEqualTo("existing");
        }
    }

    @Nested
    @DisplayName("remove 测试")
    class RemoveTests {

        @Test
        @DisplayName("remove 存在的键")
        void testRemoveExisting() {
            cache.put("key", "value");
            String result = cache.remove("key");
            assertThat(result).isEqualTo("value");
            assertThat(cache.get("key")).isNull();
        }

        @Test
        @DisplayName("remove 不存在的键")
        void testRemoveNonExisting() {
            String result = cache.remove("nonExistent");
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("containsKey 测试")
    class ContainsKeyTests {

        @Test
        @DisplayName("containsKey 存在")
        void testContainsKeyExists() {
            cache.put("key", "value");
            assertThat(cache.containsKey("key")).isTrue();
        }

        @Test
        @DisplayName("containsKey 不存在")
        void testContainsKeyNotExists() {
            assertThat(cache.containsKey("key")).isFalse();
        }
    }

    @Nested
    @DisplayName("size 测试")
    class SizeTests {

        @Test
        @DisplayName("size 初始为 0")
        void testSizeInitially() {
            assertThat(cache.size()).isEqualTo(0);
        }

        @Test
        @DisplayName("size 添加后增加")
        void testSizeAfterPut() {
            cache.put("key1", "value1");
            cache.put("key2", "value2");
            assertThat(cache.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("size 删除后减少")
        void testSizeAfterRemove() {
            cache.put("key1", "value1");
            cache.put("key2", "value2");
            cache.remove("key1");
            assertThat(cache.size()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("clear 测试")
    class ClearTests {

        @Test
        @DisplayName("clear 清空缓存")
        void testClear() {
            cache.put("key1", "value1");
            cache.put("key2", "value2");

            cache.clear();

            assertThat(cache.size()).isEqualTo(0);
            assertThat(cache.get("key1")).isNull();
        }
    }

    @Nested
    @DisplayName("isEmpty 测试")
    class IsEmptyTests {

        @Test
        @DisplayName("isEmpty 空缓存")
        void testIsEmptyTrue() {
            assertThat(cache.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("isEmpty 非空缓存")
        void testIsEmptyFalse() {
            cache.put("key", "value");
            assertThat(cache.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("isEmpty 清空后")
        void testIsEmptyAfterClear() {
            cache.put("key", "value");
            cache.clear();
            assertThat(cache.isEmpty()).isTrue();
        }
    }

    /**
     * 测试用简单缓存实现
     */
    static class TestInternalCache<K, V> implements InternalCache<K, V> {
        private final java.util.Map<K, V> map = new java.util.HashMap<>();

        @Override
        public V get(K key) {
            return map.get(key);
        }

        @Override
        public V computeIfAbsent(K key, java.util.function.Function<K, V> mappingFunction) {
            return map.computeIfAbsent(key, mappingFunction);
        }

        @Override
        public V put(K key, V value) {
            return map.put(key, value);
        }

        @Override
        public V putIfAbsent(K key, V value) {
            return map.putIfAbsent(key, value);
        }

        @Override
        public V remove(K key) {
            return map.remove(key);
        }

        @Override
        public boolean containsKey(K key) {
            return map.containsKey(key);
        }

        @Override
        public int size() {
            return map.size();
        }

        @Override
        public void clear() {
            map.clear();
        }
    }
}
