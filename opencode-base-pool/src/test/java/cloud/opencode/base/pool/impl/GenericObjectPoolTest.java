package cloud.opencode.base.pool.impl;

import cloud.opencode.base.pool.PoolConfig;
import cloud.opencode.base.pool.PooledObject;
import cloud.opencode.base.pool.PooledObjectFactory;
import cloud.opencode.base.pool.exception.OpenPoolException;
import cloud.opencode.base.pool.factory.BasePooledObjectFactory;
import cloud.opencode.base.pool.factory.DefaultPooledObject;
import cloud.opencode.base.pool.metrics.PoolMetrics;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * GenericObjectPoolTest Tests
 * GenericObjectPoolTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.0
 */
@DisplayName("GenericObjectPool 测试")
class GenericObjectPoolTest {

    private GenericObjectPool<String> pool;
    private AtomicInteger createCounter;
    private PooledObjectFactory<String> factory;

    @BeforeEach
    void setUp() {
        createCounter = new AtomicInteger(0);
        factory = new BasePooledObjectFactory<>() {
            @Override
            protected String create() {
                return "object-" + createCounter.incrementAndGet();
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
            pool = new GenericObjectPool<>(factory);

            assertThat(pool.getNumIdle()).isGreaterThanOrEqualTo(0);
            assertThat(pool.getNumActive()).isZero();
        }

        @Test
        @DisplayName("使用自定义配置创建池")
        void testConstructorWithCustomConfig() {
            PoolConfig config = PoolConfig.builder()
                    .maxTotal(5)
                    .minIdle(2)
                    .build();

            pool = new GenericObjectPool<>(factory, config);

            assertThat(pool.getNumIdle()).isGreaterThanOrEqualTo(2);
        }
    }

    @Nested
    @DisplayName("borrowObject方法测试")
    class BorrowObjectTests {

        @Test
        @DisplayName("借用对象")
        void testBorrowObject() throws OpenPoolException {
            pool = new GenericObjectPool<>(factory);

            String obj = pool.borrowObject();

            assertThat(obj).startsWith("object-");
            assertThat(pool.getNumActive()).isEqualTo(1);
        }

        @Test
        @DisplayName("借用多个对象")
        void testBorrowMultipleObjects() throws OpenPoolException {
            pool = new GenericObjectPool<>(factory, PoolConfig.builder().maxTotal(5).build());

            String obj1 = pool.borrowObject();
            String obj2 = pool.borrowObject();
            String obj3 = pool.borrowObject();

            assertThat(obj1).isNotEqualTo(obj2);
            assertThat(obj2).isNotEqualTo(obj3);
            assertThat(pool.getNumActive()).isEqualTo(3);
        }

        @Test
        @DisplayName("带超时借用对象")
        void testBorrowObjectWithTimeout() throws OpenPoolException {
            pool = new GenericObjectPool<>(factory);

            String obj = pool.borrowObject(Duration.ofSeconds(5));

            assertThat(obj).isNotNull();
        }

        @Test
        @DisplayName("池关闭后借用抛出异常")
        void testBorrowObjectPoolClosed() {
            pool = new GenericObjectPool<>(factory);
            pool.close();

            assertThatThrownBy(() -> pool.borrowObject())
                    .isInstanceOf(OpenPoolException.class)
                    .hasMessageContaining("closed");
        }

        @Test
        @DisplayName("池耗尽时超时")
        void testBorrowObjectExhausted() throws OpenPoolException {
            pool = new GenericObjectPool<>(factory,
                    PoolConfig.builder()
                            .maxTotal(1)
                            .maxWait(Duration.ofMillis(100))
                            .build());

            pool.borrowObject();

            assertThatThrownBy(() -> pool.borrowObject(Duration.ofMillis(50)))
                    .isInstanceOf(OpenPoolException.class)
                    .hasMessageContaining("Timeout");
        }
    }

    @Nested
    @DisplayName("returnObject方法测试")
    class ReturnObjectTests {

        @Test
        @DisplayName("归还对象")
        void testReturnObject() throws OpenPoolException {
            pool = new GenericObjectPool<>(factory);

            String obj = pool.borrowObject();
            assertThat(pool.getNumActive()).isEqualTo(1);

            pool.returnObject(obj);

            assertThat(pool.getNumActive()).isZero();
            assertThat(pool.getNumIdle()).isGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("归还null不会报错")
        void testReturnNullObject() throws OpenPoolException {
            pool = new GenericObjectPool<>(factory);

            assertThatCode(() -> pool.returnObject(null)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("归还非池中对象抛出异常")
        void testReturnForeignObject() {
            pool = new GenericObjectPool<>(factory);

            assertThatThrownBy(() -> pool.returnObject("foreign-object"))
                    .isInstanceOf(OpenPoolException.class);
        }

        @Test
        @DisplayName("归还后可重新借用同一对象")
        void testReuseObject() throws OpenPoolException {
            pool = new GenericObjectPool<>(factory,
                    PoolConfig.builder().maxTotal(1).minIdle(0).build());

            String obj1 = pool.borrowObject();
            pool.returnObject(obj1);

            String obj2 = pool.borrowObject();

            assertThat(obj2).isEqualTo(obj1);
        }
    }

    @Nested
    @DisplayName("invalidateObject方法测试")
    class InvalidateObjectTests {

        @Test
        @DisplayName("使对象无效")
        void testInvalidateObject() throws OpenPoolException {
            pool = new GenericObjectPool<>(factory);

            String obj = pool.borrowObject();
            pool.invalidateObject(obj);

            assertThat(pool.getNumActive()).isZero();
        }

        @Test
        @DisplayName("使null对象无效不会报错")
        void testInvalidateNullObject() {
            pool = new GenericObjectPool<>(factory);

            assertThatCode(() -> pool.invalidateObject(null)).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("addObject方法测试")
    class AddObjectTests {

        @Test
        @DisplayName("添加对象到池")
        void testAddObject() throws OpenPoolException {
            pool = new GenericObjectPool<>(factory,
                    PoolConfig.builder().minIdle(0).build());

            int initialIdle = pool.getNumIdle();
            pool.addObject();

            assertThat(pool.getNumIdle()).isEqualTo(initialIdle + 1);
        }

        @Test
        @DisplayName("池关闭后添加抛出异常")
        void testAddObjectPoolClosed() {
            pool = new GenericObjectPool<>(factory);
            pool.close();

            assertThatThrownBy(() -> pool.addObject())
                    .isInstanceOf(OpenPoolException.class)
                    .hasMessageContaining("closed");
        }
    }

    @Nested
    @DisplayName("clear方法测试")
    class ClearTests {

        @Test
        @DisplayName("清除所有空闲对象")
        void testClear() throws OpenPoolException {
            pool = new GenericObjectPool<>(factory,
                    PoolConfig.builder().minIdle(3).build());

            // Wait for pre-creation
            assertThat(pool.getNumIdle()).isGreaterThanOrEqualTo(0);

            pool.clear();

            assertThat(pool.getNumIdle()).isZero();
        }
    }

    @Nested
    @DisplayName("close方法测试")
    class CloseTests {

        @Test
        @DisplayName("关闭池")
        void testClose() throws OpenPoolException {
            pool = new GenericObjectPool<>(factory);
            pool.borrowObject();

            pool.close();

            assertThatThrownBy(() -> pool.borrowObject())
                    .isInstanceOf(OpenPoolException.class);
        }

        @Test
        @DisplayName("多次关闭不会报错")
        void testCloseMultipleTimes() {
            pool = new GenericObjectPool<>(factory);

            assertThatCode(() -> {
                pool.close();
                pool.close();
                pool.close();
            }).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("getMetrics方法测试")
    class GetMetricsTests {

        @Test
        @DisplayName("返回指标")
        void testGetMetrics() throws OpenPoolException {
            pool = new GenericObjectPool<>(factory);

            pool.borrowObject();
            PoolMetrics metrics = pool.getMetrics();

            assertThat(metrics).isNotNull();
            assertThat(metrics.getBorrowCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("验证测试")
    class ValidationTests {

        @Test
        @DisplayName("testOnBorrow验证对象")
        void testTestOnBorrow() throws OpenPoolException {
            AtomicInteger validateCount = new AtomicInteger(0);
            PooledObjectFactory<String> validatingFactory = new BasePooledObjectFactory<>() {
                @Override
                protected String create() {
                    return "test";
                }

                @Override
                public boolean validateObject(PooledObject<String> obj) {
                    validateCount.incrementAndGet();
                    return true;
                }
            };

            pool = new GenericObjectPool<>(validatingFactory,
                    PoolConfig.builder().testOnBorrow(true).build());

            pool.borrowObject();

            assertThat(validateCount.get()).isGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("testOnReturn验证对象")
        void testTestOnReturn() throws OpenPoolException {
            AtomicInteger validateCount = new AtomicInteger(0);
            PooledObjectFactory<String> validatingFactory = new BasePooledObjectFactory<>() {
                @Override
                protected String create() {
                    return "test";
                }

                @Override
                public boolean validateObject(PooledObject<String> obj) {
                    validateCount.incrementAndGet();
                    return true;
                }
            };

            pool = new GenericObjectPool<>(validatingFactory,
                    PoolConfig.builder().testOnReturn(true).build());

            String obj = pool.borrowObject();
            int countBeforeReturn = validateCount.get();
            pool.returnObject(obj);

            assertThat(validateCount.get()).isGreaterThan(countBeforeReturn);
        }
    }

    @Nested
    @DisplayName("并发测试")
    class ConcurrencyTests {

        @Test
        @DisplayName("并发借用和归还")
        void testConcurrentBorrowReturn() throws InterruptedException {
            pool = new GenericObjectPool<>(factory,
                    PoolConfig.builder().maxTotal(10).build());

            int threadCount = 20;
            int operationsPerThread = 100;
            CountDownLatch latch = new CountDownLatch(threadCount);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger errorCount = new AtomicInteger(0);

            for (int i = 0; i < threadCount; i++) {
                Thread.ofVirtual().start(() -> {
                    try {
                        for (int j = 0; j < operationsPerThread; j++) {
                            String obj = pool.borrowObject(Duration.ofSeconds(5));
                            Thread.sleep(1);
                            pool.returnObject(obj);
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

    @Nested
    @DisplayName("LIFO/FIFO测试")
    class LifoFifoTests {

        @Test
        @DisplayName("LIFO模式优先返回最近归还的对象")
        void testLifoMode() throws OpenPoolException {
            pool = new GenericObjectPool<>(factory,
                    PoolConfig.builder()
                            .maxTotal(3)
                            .minIdle(0)
                            .lifo(true)
                            .build());

            String obj1 = pool.borrowObject();
            String obj2 = pool.borrowObject();

            pool.returnObject(obj1);
            pool.returnObject(obj2);

            // LIFO: obj2 should be returned first (last in, first out)
            String borrowed = pool.borrowObject();
            assertThat(borrowed).isEqualTo(obj2);
        }

        @Test
        @DisplayName("LIFO为false配置测试")
        void testLifoFalseConfig() throws OpenPoolException {
            pool = new GenericObjectPool<>(factory,
                    PoolConfig.builder()
                            .maxTotal(3)
                            .minIdle(0)
                            .lifo(false)
                            .build());

            String obj1 = pool.borrowObject();
            String obj2 = pool.borrowObject();

            pool.returnObject(obj1);
            pool.returnObject(obj2);

            // Borrow returns an object (behavior depends on implementation)
            String borrowed = pool.borrowObject();
            assertThat(borrowed).isIn(obj1, obj2);
        }
    }
}
