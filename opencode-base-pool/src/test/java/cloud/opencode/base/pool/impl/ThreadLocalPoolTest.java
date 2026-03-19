package cloud.opencode.base.pool.impl;

import cloud.opencode.base.pool.PooledObject;
import cloud.opencode.base.pool.PooledObjectFactory;
import cloud.opencode.base.pool.exception.OpenPoolException;
import cloud.opencode.base.pool.factory.BasePooledObjectFactory;
import cloud.opencode.base.pool.metrics.PoolMetrics;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

/**
 * ThreadLocalPoolTest Tests
 * ThreadLocalPoolTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.0
 */
@DisplayName("ThreadLocalPool 测试")
class ThreadLocalPoolTest {

    private ThreadLocalPool<String> pool;
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
        @DisplayName("创建线程本地池")
        void testConstructor() {
            pool = new ThreadLocalPool<>(factory);

            assertThat(pool.getNumIdle()).isZero();
            assertThat(pool.getNumActive()).isZero();
        }
    }

    @Nested
    @DisplayName("borrowObject方法测试")
    class BorrowObjectTests {

        @Test
        @DisplayName("借用对象")
        void testBorrowObject() throws OpenPoolException {
            pool = new ThreadLocalPool<>(factory);

            String obj = pool.borrowObject();

            assertThat(obj).startsWith("object-");
            assertThat(pool.getNumActive()).isEqualTo(1);
        }

        @Test
        @DisplayName("同一线程借用返回同一对象")
        void testBorrowSameObject() throws OpenPoolException {
            pool = new ThreadLocalPool<>(factory);

            String obj1 = pool.borrowObject();
            pool.returnObject(obj1);
            String obj2 = pool.borrowObject();

            assertThat(obj2).isEqualTo(obj1);
        }

        @Test
        @DisplayName("带超时借用忽略超时参数")
        void testBorrowObjectWithTimeout() throws OpenPoolException {
            pool = new ThreadLocalPool<>(factory);

            String obj = pool.borrowObject(Duration.ofSeconds(5));

            assertThat(obj).isNotNull();
        }

        @Test
        @DisplayName("池关闭后借用抛出异常")
        void testBorrowObjectPoolClosed() {
            pool = new ThreadLocalPool<>(factory);
            pool.close();

            assertThatThrownBy(() -> pool.borrowObject())
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
            pool = new ThreadLocalPool<>(factory);

            String obj = pool.borrowObject();
            assertThat(pool.getNumActive()).isEqualTo(1);

            pool.returnObject(obj);

            assertThat(pool.getNumActive()).isZero();
        }

        @Test
        @DisplayName("归还null不会报错")
        void testReturnNullObject() {
            pool = new ThreadLocalPool<>(factory);

            assertThatCode(() -> pool.returnObject(null)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("passivateObject在归还时被调用")
        void testPassivateOnReturn() throws OpenPoolException {
            AtomicInteger passivateCount = new AtomicInteger(0);
            PooledObjectFactory<String> passivatableFactory = new BasePooledObjectFactory<>() {
                @Override
                protected String create() {
                    return "test";
                }

                @Override
                public void passivateObject(PooledObject<String> obj) {
                    passivateCount.incrementAndGet();
                }
            };

            pool = new ThreadLocalPool<>(passivatableFactory);

            String obj = pool.borrowObject();
            pool.returnObject(obj);

            assertThat(passivateCount.get()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("invalidateObject方法测试")
    class InvalidateObjectTests {

        @Test
        @DisplayName("使对象无效")
        void testInvalidateObject() throws OpenPoolException {
            pool = new ThreadLocalPool<>(factory);

            String obj = pool.borrowObject();
            pool.invalidateObject(obj);

            assertThat(pool.getNumActive()).isZero();
        }

        @Test
        @DisplayName("使null对象无效不会报错")
        void testInvalidateNullObject() {
            pool = new ThreadLocalPool<>(factory);

            assertThatCode(() -> pool.invalidateObject(null)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("无效化后创建新对象")
        void testInvalidateAndBorrowNew() throws OpenPoolException {
            pool = new ThreadLocalPool<>(factory);

            String obj1 = pool.borrowObject();
            pool.invalidateObject(obj1);
            String obj2 = pool.borrowObject();

            assertThat(obj2).isNotEqualTo(obj1);
        }
    }

    @Nested
    @DisplayName("addObject方法测试")
    class AddObjectTests {

        @Test
        @DisplayName("预初始化当前线程的对象")
        void testAddObject() throws OpenPoolException {
            pool = new ThreadLocalPool<>(factory);

            pool.addObject();

            // addObject initializes for current thread
            assertThat(createCounter.get()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("clear方法测试")
    class ClearTests {

        @Test
        @DisplayName("清除当前线程的对象")
        void testClear() throws OpenPoolException {
            pool = new ThreadLocalPool<>(factory);

            pool.borrowObject();
            pool.clear();

            // After clear, a new borrow should create a new object
            String newObj = pool.borrowObject();
            assertThat(newObj).isNotNull();
            assertThat(createCounter.get()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("close方法测试")
    class CloseTests {

        @Test
        @DisplayName("关闭池")
        void testClose() throws OpenPoolException {
            pool = new ThreadLocalPool<>(factory);
            pool.borrowObject();

            pool.close();

            assertThatThrownBy(() -> pool.borrowObject())
                    .isInstanceOf(OpenPoolException.class);
        }

        @Test
        @DisplayName("多次关闭不会报错")
        void testCloseMultipleTimes() {
            pool = new ThreadLocalPool<>(factory);

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
            pool = new ThreadLocalPool<>(factory);

            pool.borrowObject();
            PoolMetrics metrics = pool.getMetrics();

            assertThat(metrics).isNotNull();
            assertThat(metrics.getBorrowCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("getNumIdle方法测试")
    class GetNumIdleTests {

        @Test
        @DisplayName("总是返回零")
        void testGetNumIdle() throws OpenPoolException {
            pool = new ThreadLocalPool<>(factory);

            pool.borrowObject();

            // ThreadLocalPool doesn't track idle across threads
            assertThat(pool.getNumIdle()).isZero();
        }
    }

    @Nested
    @DisplayName("多线程测试")
    class MultiThreadTests {

        @Test
        @DisplayName("不同线程使用不同对象")
        void testDifferentThreadsDifferentObjects() throws InterruptedException {
            pool = new ThreadLocalPool<>(factory);

            int threadCount = 5;
            CountDownLatch latch = new CountDownLatch(threadCount);
            AtomicReference<String>[] objects = new AtomicReference[threadCount];
            for (int i = 0; i < threadCount; i++) {
                objects[i] = new AtomicReference<>();
            }

            for (int i = 0; i < threadCount; i++) {
                final int idx = i;
                Thread.ofVirtual().start(() -> {
                    try {
                        String obj = pool.borrowObject();
                        objects[idx].set(obj);
                        pool.returnObject(obj);
                    } catch (Exception e) {
                        // ignore
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();

            // All objects should be unique
            for (int i = 0; i < threadCount; i++) {
                for (int j = i + 1; j < threadCount; j++) {
                    assertThat(objects[i].get()).isNotEqualTo(objects[j].get());
                }
            }
        }

        @Test
        @DisplayName("同一线程多次借用返回同一对象")
        void testSameThreadSameObject() throws InterruptedException {
            pool = new ThreadLocalPool<>(factory);

            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<Boolean> result = new AtomicReference<>();

            Thread.ofVirtual().start(() -> {
                try {
                    String obj1 = pool.borrowObject();
                    pool.returnObject(obj1);
                    String obj2 = pool.borrowObject();
                    pool.returnObject(obj2);

                    result.set(obj1.equals(obj2));
                } catch (Exception e) {
                    result.set(false);
                } finally {
                    latch.countDown();
                }
            });

            latch.await();

            assertThat(result.get()).isTrue();
        }
    }
}
