package cloud.opencode.base.core.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * InternalLRUCache 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("InternalLRUCache 测试")
class InternalLRUCacheTest {

    private InternalLRUCache<String, String> cache;

    @BeforeEach
    void setUp() {
        cache = InternalLRUCache.create(100);
    }

    @Nested
    @DisplayName("create 静态方法测试")
    class CreateTests {

        @Test
        @DisplayName("create 创建缓存")
        void testCreate() {
            InternalLRUCache<String, Object> lruCache = InternalLRUCache.create(50);
            assertThat(lruCache).isNotNull();
            assertThat(lruCache.getMaxSize()).isEqualTo(50);
        }
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

        @Test
        @DisplayName("computeIfAbsent 返回 null 不缓存")
        void testComputeIfAbsentNull() {
            String result = cache.computeIfAbsent("key", k -> null);
            assertThat(result).isNull();
            assertThat(cache.containsKey("key")).isFalse();
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
            assertThat(cache.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("getMaxSize 测试")
    class GetMaxSizeTests {

        @Test
        @DisplayName("getMaxSize 返回最大容量")
        void testGetMaxSize() {
            InternalLRUCache<String, String> smallCache = InternalLRUCache.create(10);
            assertThat(smallCache.getMaxSize()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("LRU 淘汰策略测试")
    class LRUEvictionTests {

        @Test
        @DisplayName("超过最大容量时淘汰最旧元素")
        void testEviction() {
            InternalLRUCache<String, String> smallCache = InternalLRUCache.create(3);

            smallCache.put("key1", "value1");
            smallCache.put("key2", "value2");
            smallCache.put("key3", "value3");

            // 添加第4个元素，应该淘汰 key1
            smallCache.put("key4", "value4");

            assertThat(smallCache.size()).isEqualTo(3);
            assertThat(smallCache.containsKey("key1")).isFalse();
            assertThat(smallCache.containsKey("key2")).isTrue();
            assertThat(smallCache.containsKey("key3")).isTrue();
            assertThat(smallCache.containsKey("key4")).isTrue();
        }

        @Test
        @DisplayName("访问后更新 LRU 顺序")
        void testAccessUpdatesLRU() {
            InternalLRUCache<String, String> smallCache = InternalLRUCache.create(3);

            smallCache.put("key1", "value1");
            smallCache.put("key2", "value2");
            smallCache.put("key3", "value3");

            // 访问 key1，使其成为最近使用
            smallCache.get("key1");

            // 添加第4个元素，应该淘汰 key2（现在最旧）
            smallCache.put("key4", "value4");

            assertThat(smallCache.containsKey("key1")).isTrue();
            assertThat(smallCache.containsKey("key2")).isFalse();
            assertThat(smallCache.containsKey("key3")).isTrue();
            assertThat(smallCache.containsKey("key4")).isTrue();
        }
    }

    @Nested
    @DisplayName("线程安全测试")
    class ThreadSafetyTests {

        @Test
        @DisplayName("多线程并发读写")
        void testConcurrentReadWrite() throws InterruptedException {
            int threadCount = 10;
            int operationsPerThread = 100;
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch endLatch = new CountDownLatch(threadCount);

            for (int i = 0; i < threadCount; i++) {
                final int threadIndex = i;
                new Thread(() -> {
                    try {
                        startLatch.await();
                        for (int j = 0; j < operationsPerThread; j++) {
                            String key = "key-" + threadIndex + "-" + j;
                            cache.put(key, "value-" + j);
                            cache.get(key);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        endLatch.countDown();
                    }
                }).start();
            }

            startLatch.countDown();
            endLatch.await();

            // 验证没有异常发生，缓存仍然可用
            assertThat(cache.size()).isGreaterThan(0);
        }

        @Test
        @DisplayName("多线程 computeIfAbsent")
        void testConcurrentComputeIfAbsent() throws InterruptedException {
            int threadCount = 10;
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch endLatch = new CountDownLatch(threadCount);
            AtomicInteger computeCount = new AtomicInteger(0);

            for (int i = 0; i < threadCount; i++) {
                new Thread(() -> {
                    try {
                        startLatch.await();
                        cache.computeIfAbsent("sharedKey", k -> {
                            computeCount.incrementAndGet();
                            return "computed";
                        });
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        endLatch.countDown();
                    }
                }).start();
            }

            startLatch.countDown();
            endLatch.await();

            // 只计算一次
            assertThat(computeCount.get()).isEqualTo(1);
            assertThat(cache.get("sharedKey")).isEqualTo("computed");
        }
    }

    @Nested
    @DisplayName("InternalCache 接口实现测试")
    class InterfaceImplementationTests {

        @Test
        @DisplayName("实现 InternalCache 接口")
        void testImplementsInternalCache() {
            assertThat(cache).isInstanceOf(InternalCache.class);
        }

        @Test
        @DisplayName("isEmpty 默认实现")
        void testIsEmpty() {
            assertThat(cache.isEmpty()).isTrue();
            cache.put("key", "value");
            assertThat(cache.isEmpty()).isFalse();
        }
    }
}
