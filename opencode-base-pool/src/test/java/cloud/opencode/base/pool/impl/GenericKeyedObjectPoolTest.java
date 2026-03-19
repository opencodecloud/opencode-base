package cloud.opencode.base.pool.impl;

import cloud.opencode.base.pool.PoolConfig;
import cloud.opencode.base.pool.exception.OpenPoolException;
import cloud.opencode.base.pool.factory.BaseKeyedPooledObjectFactory;
import cloud.opencode.base.pool.factory.KeyedPooledObjectFactory;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * GenericKeyedObjectPoolTest Tests
 * GenericKeyedObjectPoolTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.0
 */
@DisplayName("GenericKeyedObjectPool 测试")
class GenericKeyedObjectPoolTest {

    private GenericKeyedObjectPool<String, String> pool;
    private AtomicInteger createCounter;
    private KeyedPooledObjectFactory<String, String> factory;

    @BeforeEach
    void setUp() {
        createCounter = new AtomicInteger(0);
        factory = new BaseKeyedPooledObjectFactory<>() {
            @Override
            protected String create(String key) {
                return key + "-" + createCounter.incrementAndGet();
            }
        };
    }

    @AfterEach
    void tearDown() {
        if (pool != null) {
            pool.close();
        }
    }

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("使用默认配置创建池")
        void testConstructorWithDefaultConfig() {
            pool = new GenericKeyedObjectPool<>(factory);

            assertThat(pool.getNumKeys()).isZero();
        }

        @Test
        @DisplayName("使用自定义配置创建池")
        void testConstructorWithCustomConfig() {
            PoolConfig config = PoolConfig.builder()
                    .maxTotal(5)
                    .build();

            pool = new GenericKeyedObjectPool<>(factory, config);

            assertThat(pool.getNumKeys()).isZero();
        }
    }

    @Nested
    @DisplayName("borrowObject方法测试")
    class BorrowObjectTests {

        @Test
        @DisplayName("借用对象")
        void testBorrowObject() throws OpenPoolException {
            pool = new GenericKeyedObjectPool<>(factory);

            String obj = pool.borrowObject("key1");

            assertThat(obj).startsWith("key1-");
            assertThat(pool.getNumActive("key1")).isEqualTo(1);
        }

        @Test
        @DisplayName("借用不同键的对象")
        void testBorrowObjectDifferentKeys() throws OpenPoolException {
            pool = new GenericKeyedObjectPool<>(factory);

            String obj1 = pool.borrowObject("key1");
            String obj2 = pool.borrowObject("key2");

            assertThat(obj1).startsWith("key1-");
            assertThat(obj2).startsWith("key2-");
            assertThat(pool.getNumKeys()).isEqualTo(2);
        }

        @Test
        @DisplayName("带超时借用对象")
        void testBorrowObjectWithTimeout() throws OpenPoolException {
            pool = new GenericKeyedObjectPool<>(factory);

            String obj = pool.borrowObject("key1", Duration.ofSeconds(5));

            assertThat(obj).isNotNull();
        }

        @Test
        @DisplayName("池关闭后借用抛出异常")
        void testBorrowObjectPoolClosed() {
            pool = new GenericKeyedObjectPool<>(factory);
            pool.close();

            assertThatThrownBy(() -> pool.borrowObject("key1"))
                    .isInstanceOf(OpenPoolException.class)
                    .hasMessageContaining("closed");
        }
    }

    @Nested
    @DisplayName("returnObject方法测试")
    class ReturnObjectTests {

        @Test
        @DisplayName("归还对象")
        void testReturnObject() throws OpenPoolException {
            pool = new GenericKeyedObjectPool<>(factory);

            String obj = pool.borrowObject("key1");
            assertThat(pool.getNumActive("key1")).isEqualTo(1);

            pool.returnObject("key1", obj);

            assertThat(pool.getNumActive("key1")).isZero();
            assertThat(pool.getNumIdle("key1")).isGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("归还后可重新借用同一对象")
        void testReuseObject() throws OpenPoolException {
            pool = new GenericKeyedObjectPool<>(factory,
                    PoolConfig.builder().maxTotal(1).minIdle(0).build());

            String obj1 = pool.borrowObject("key1");
            pool.returnObject("key1", obj1);

            String obj2 = pool.borrowObject("key1");

            assertThat(obj2).isEqualTo(obj1);
        }
    }

    @Nested
    @DisplayName("invalidateObject方法测试")
    class InvalidateObjectTests {

        @Test
        @DisplayName("使对象无效")
        void testInvalidateObject() throws OpenPoolException {
            pool = new GenericKeyedObjectPool<>(factory);

            String obj = pool.borrowObject("key1");
            pool.invalidateObject("key1", obj);

            assertThat(pool.getNumActive("key1")).isZero();
        }
    }

    @Nested
    @DisplayName("getNumIdle方法测试")
    class GetNumIdleTests {

        @Test
        @DisplayName("返回指定键的空闲数")
        void testGetNumIdle() throws OpenPoolException {
            pool = new GenericKeyedObjectPool<>(factory);

            String obj = pool.borrowObject("key1");
            pool.returnObject("key1", obj);

            assertThat(pool.getNumIdle("key1")).isGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("不存在的键返回零")
        void testGetNumIdleNonexistent() {
            pool = new GenericKeyedObjectPool<>(factory);

            assertThat(pool.getNumIdle("nonexistent")).isZero();
        }
    }

    @Nested
    @DisplayName("getNumActive方法测试")
    class GetNumActiveTests {

        @Test
        @DisplayName("返回指定键的活跃数")
        void testGetNumActive() throws OpenPoolException {
            pool = new GenericKeyedObjectPool<>(factory);

            pool.borrowObject("key1");

            assertThat(pool.getNumActive("key1")).isEqualTo(1);
        }

        @Test
        @DisplayName("不存在的键返回零")
        void testGetNumActiveNonexistent() {
            pool = new GenericKeyedObjectPool<>(factory);

            assertThat(pool.getNumActive("nonexistent")).isZero();
        }
    }

    @Nested
    @DisplayName("clear方法测试")
    class ClearTests {

        @Test
        @DisplayName("清除指定键的空闲对象")
        void testClearKey() throws OpenPoolException {
            pool = new GenericKeyedObjectPool<>(factory);

            String obj = pool.borrowObject("key1");
            pool.returnObject("key1", obj);

            pool.clear("key1");

            assertThat(pool.getNumIdle("key1")).isZero();
        }

        @Test
        @DisplayName("清除所有键的空闲对象")
        void testClearAll() throws OpenPoolException {
            pool = new GenericKeyedObjectPool<>(factory);

            String obj1 = pool.borrowObject("key1");
            String obj2 = pool.borrowObject("key2");
            pool.returnObject("key1", obj1);
            pool.returnObject("key2", obj2);

            pool.clear();

            assertThat(pool.getNumIdle("key1")).isZero();
            assertThat(pool.getNumIdle("key2")).isZero();
        }
    }

    @Nested
    @DisplayName("close方法测试")
    class CloseTests {

        @Test
        @DisplayName("关闭池")
        void testClose() throws OpenPoolException {
            pool = new GenericKeyedObjectPool<>(factory);
            pool.borrowObject("key1");

            pool.close();

            assertThatThrownBy(() -> pool.borrowObject("key1"))
                    .isInstanceOf(OpenPoolException.class);
        }

        @Test
        @DisplayName("多次关闭不会报错")
        void testCloseMultipleTimes() {
            pool = new GenericKeyedObjectPool<>(factory);

            assertThatCode(() -> {
                pool.close();
                pool.close();
                pool.close();
            }).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("getNumKeys方法测试")
    class GetNumKeysTests {

        @Test
        @DisplayName("返回键数量")
        void testGetNumKeys() throws OpenPoolException {
            pool = new GenericKeyedObjectPool<>(factory);

            pool.borrowObject("key1");
            pool.borrowObject("key2");
            pool.borrowObject("key3");

            assertThat(pool.getNumKeys()).isEqualTo(3);
        }

        @Test
        @DisplayName("初始键数量为零")
        void testGetNumKeysInitial() {
            pool = new GenericKeyedObjectPool<>(factory);

            assertThat(pool.getNumKeys()).isZero();
        }
    }

    @Nested
    @DisplayName("getTotalNumIdle和getTotalNumActive方法测试")
    class TotalCountTests {

        @Test
        @DisplayName("返回所有键的空闲总数")
        void testGetTotalNumIdle() throws OpenPoolException {
            pool = new GenericKeyedObjectPool<>(factory);

            String obj1 = pool.borrowObject("key1");
            String obj2 = pool.borrowObject("key2");
            pool.returnObject("key1", obj1);
            pool.returnObject("key2", obj2);

            assertThat(pool.getTotalNumIdle()).isGreaterThanOrEqualTo(2);
        }

        @Test
        @DisplayName("返回所有键的活跃总数")
        void testGetTotalNumActive() throws OpenPoolException {
            pool = new GenericKeyedObjectPool<>(factory);

            pool.borrowObject("key1");
            pool.borrowObject("key2");
            pool.borrowObject("key3");

            assertThat(pool.getTotalNumActive()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("并发测试")
    class ConcurrencyTests {

        @Test
        @DisplayName("并发借用和归还多个键")
        void testConcurrentBorrowReturn() throws InterruptedException {
            pool = new GenericKeyedObjectPool<>(factory,
                    PoolConfig.builder().maxTotal(10).build());

            int threadCount = 20;
            int operationsPerThread = 50;
            CountDownLatch latch = new CountDownLatch(threadCount);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger errorCount = new AtomicInteger(0);

            for (int i = 0; i < threadCount; i++) {
                final int threadId = i;
                Thread.ofVirtual().start(() -> {
                    try {
                        String key = "key-" + (threadId % 5);
                        for (int j = 0; j < operationsPerThread; j++) {
                            String obj = pool.borrowObject(key, Duration.ofSeconds(5));
                            Thread.sleep(1);
                            pool.returnObject(key, obj);
                            successCount.incrementAndGet();
                        }
                    } catch (Exception e) {
                        errorCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();

            assertThat(errorCount.get()).isZero();
            assertThat(successCount.get()).isEqualTo(threadCount * operationsPerThread);
        }
    }
}
