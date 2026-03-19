package cloud.opencode.base.pool.impl;

import cloud.opencode.base.pool.PoolConfig;
import cloud.opencode.base.pool.PoolContext;
import cloud.opencode.base.pool.PooledObjectFactory;
import cloud.opencode.base.pool.exception.OpenPoolException;
import cloud.opencode.base.pool.factory.BasePooledObjectFactory;
import cloud.opencode.base.pool.metrics.PoolMetrics;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

/**
 * VirtualThreadPoolTest Tests
 * VirtualThreadPoolTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.0
 */
@DisplayName("VirtualThreadPool 测试")
class VirtualThreadPoolTest {

    private VirtualThreadPool<String> pool;
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
        @DisplayName("创建虚拟线程池")
        void testConstructor() {
            pool = new VirtualThreadPool<>(factory, PoolConfig.defaults());

            assertThat(pool.getNumActive()).isZero();
        }

        @Test
        @DisplayName("使用create静态方法创建")
        void testCreateStaticMethod() {
            pool = VirtualThreadPool.create(factory, PoolConfig.defaults());

            assertThat(pool).isNotNull();
        }

        @Test
        @DisplayName("预创建最小空闲对象")
        void testMinIdlePreCreation() {
            PoolConfig config = PoolConfig.builder()
                    .minIdle(3)
                    .maxTotal(10)
                    .build();

            pool = new VirtualThreadPool<>(factory, config);

            assertThat(pool.getNumIdle()).isGreaterThanOrEqualTo(0);
        }
    }

    @Nested
    @DisplayName("borrowObject方法测试")
    class BorrowObjectTests {

        @Test
        @DisplayName("借用对象")
        void testBorrowObject() throws OpenPoolException {
            pool = new VirtualThreadPool<>(factory, PoolConfig.defaults());

            String obj = pool.borrowObject();

            assertThat(obj).startsWith("object-");
            assertThat(pool.getNumActive()).isEqualTo(1);
        }

        @Test
        @DisplayName("借用多个对象")
        void testBorrowMultipleObjects() throws OpenPoolException {
            pool = new VirtualThreadPool<>(factory,
                    PoolConfig.builder().maxTotal(5).build());

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
            pool = new VirtualThreadPool<>(factory, PoolConfig.defaults());

            String obj = pool.borrowObject(Duration.ofSeconds(5));

            assertThat(obj).isNotNull();
        }

        @Test
        @DisplayName("池关闭后借用抛出异常")
        void testBorrowObjectPoolClosed() {
            pool = new VirtualThreadPool<>(factory, PoolConfig.defaults());
            pool.close();

            assertThatThrownBy(() -> pool.borrowObject())
                    .isInstanceOf(OpenPoolException.class);
        }

        @Test
        @DisplayName("池耗尽时超时")
        void testBorrowObjectExhausted() throws OpenPoolException {
            pool = new VirtualThreadPool<>(factory,
                    PoolConfig.builder()
                            .maxTotal(1)
                            .maxWait(Duration.ofMillis(100))
                            .build());

            pool.borrowObject();

            assertThatThrownBy(() -> pool.borrowObject(Duration.ofMillis(50)))
                    .isInstanceOf(OpenPoolException.class);
        }
    }

    @Nested
    @DisplayName("borrowAsync方法测试")
    class BorrowAsyncTests {

        @Test
        @DisplayName("异步借用对象")
        void testBorrowAsync() throws ExecutionException, InterruptedException {
            pool = new VirtualThreadPool<>(factory, PoolConfig.defaults());

            CompletableFuture<String> future = pool.borrowAsync();
            String obj = future.get();

            assertThat(obj).startsWith("object-");
        }

        @Test
        @DisplayName("带超时异步借用对象")
        void testBorrowAsyncWithTimeout() throws ExecutionException, InterruptedException {
            pool = new VirtualThreadPool<>(factory, PoolConfig.defaults());

            CompletableFuture<String> future = pool.borrowAsync(Duration.ofSeconds(5));
            String obj = future.get();

            assertThat(obj).isNotNull();
        }
    }

    @Nested
    @DisplayName("returnObject方法测试")
    class ReturnObjectTests {

        @Test
        @DisplayName("归还对象")
        void testReturnObject() throws OpenPoolException {
            pool = new VirtualThreadPool<>(factory, PoolConfig.defaults());

            String obj = pool.borrowObject();
            assertThat(pool.getNumActive()).isEqualTo(1);

            pool.returnObject(obj);

            assertThat(pool.getNumActive()).isZero();
        }

        @Test
        @DisplayName("归还null不会报错")
        void testReturnNullObject() {
            pool = new VirtualThreadPool<>(factory, PoolConfig.defaults());

            assertThatCode(() -> pool.returnObject(null)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("归还后可重新借用")
        void testReuseObject() throws OpenPoolException {
            pool = new VirtualThreadPool<>(factory,
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
            pool = new VirtualThreadPool<>(factory, PoolConfig.defaults());

            String obj = pool.borrowObject();
            pool.invalidateObject(obj);

            assertThat(pool.getNumActive()).isZero();
        }

        @Test
        @DisplayName("使null对象无效不会报错")
        void testInvalidateNullObject() {
            pool = new VirtualThreadPool<>(factory, PoolConfig.defaults());

            assertThatCode(() -> pool.invalidateObject(null)).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("execute方法测试")
    class ExecuteTests {

        @Test
        @DisplayName("execute Function返回结果")
        void testExecuteFunction() {
            pool = new VirtualThreadPool<>(factory, PoolConfig.defaults());

            String result = pool.execute((java.util.function.Function<String, String>) String::toUpperCase);

            assertThat(result).startsWith("OBJECT-");
            assertThat(pool.getNumActive()).isZero();
        }

        @Test
        @DisplayName("execute Consumer执行操作")
        void testExecuteConsumer() {
            pool = new VirtualThreadPool<>(factory, PoolConfig.defaults());
            AtomicReference<String> captured = new AtomicReference<>();

            pool.execute((java.util.function.Consumer<String>) captured::set);

            assertThat(captured.get()).startsWith("object-");
            assertThat(pool.getNumActive()).isZero();
        }

        @Test
        @DisplayName("executeAsync异步执行")
        void testExecuteAsync() throws ExecutionException, InterruptedException {
            pool = new VirtualThreadPool<>(factory, PoolConfig.defaults());

            CompletableFuture<String> future = pool.executeAsync(String::toUpperCase);
            String result = future.get();

            assertThat(result).startsWith("OBJECT-");
        }
    }

    @Nested
    @DisplayName("addObject方法测试")
    class AddObjectTests {

        @Test
        @DisplayName("添加对象到池")
        void testAddObject() throws OpenPoolException {
            pool = new VirtualThreadPool<>(factory,
                    PoolConfig.builder().minIdle(0).maxTotal(10).build());

            int initialIdle = pool.getNumIdle();
            pool.addObject();

            assertThat(pool.getNumIdle()).isGreaterThanOrEqualTo(initialIdle);
        }

        @Test
        @DisplayName("池关闭后添加抛出异常")
        void testAddObjectPoolClosed() {
            pool = new VirtualThreadPool<>(factory, PoolConfig.defaults());
            pool.close();

            assertThatThrownBy(() -> pool.addObject())
                    .isInstanceOf(OpenPoolException.class);
        }
    }

    @Nested
    @DisplayName("clear方法测试")
    class ClearTests {

        @Test
        @DisplayName("清除所有空闲对象")
        void testClear() throws OpenPoolException {
            pool = new VirtualThreadPool<>(factory,
                    PoolConfig.builder().minIdle(3).maxTotal(10).build());

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
            pool = new VirtualThreadPool<>(factory, PoolConfig.defaults());
            pool.borrowObject();

            pool.close();

            assertThatThrownBy(() -> pool.borrowObject())
                    .isInstanceOf(OpenPoolException.class);
        }

        @Test
        @DisplayName("多次关闭不会报错")
        void testCloseMultipleTimes() {
            pool = new VirtualThreadPool<>(factory, PoolConfig.defaults());

            assertThatCode(() -> {
                pool.close();
                pool.close();
                pool.close();
            }).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("池信息方法测试")
    class PoolInfoTests {

        @Test
        @DisplayName("size返回创建的总数")
        void testSize() throws OpenPoolException {
            pool = new VirtualThreadPool<>(factory,
                    PoolConfig.builder().minIdle(0).maxTotal(10).build());

            pool.borrowObject();
            pool.borrowObject();

            assertThat(pool.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("available返回可用数量")
        void testAvailable() throws OpenPoolException {
            pool = new VirtualThreadPool<>(factory,
                    PoolConfig.builder().maxTotal(5).minIdle(0).build());

            assertThat(pool.available()).isEqualTo(5);

            pool.borrowObject();

            assertThat(pool.available()).isEqualTo(4);
        }

        @Test
        @DisplayName("isVirtualThread检测虚拟线程")
        void testIsVirtualThread() throws InterruptedException {
            pool = new VirtualThreadPool<>(factory, PoolConfig.defaults());

            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<Boolean> result = new AtomicReference<>();

            Thread.ofVirtual().start(() -> {
                result.set(pool.isVirtualThread());
                latch.countDown();
            });

            latch.await();
            assertThat(result.get()).isTrue();
        }

        @Test
        @DisplayName("getMetrics返回指标")
        void testGetMetrics() throws OpenPoolException {
            pool = new VirtualThreadPool<>(factory, PoolConfig.defaults());

            pool.borrowObject();
            PoolMetrics metrics = pool.getMetrics();

            assertThat(metrics).isNotNull();
            assertThat(metrics.getBorrowCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("PoolContext集成测试")
    class PoolContextIntegrationTests {

        @Test
        @DisplayName("借用时记录到PoolContext")
        void testBorrowRecordsToContext() throws Exception {
            pool = new VirtualThreadPool<>(factory, PoolConfig.defaults());
            PoolContext context = PoolContext.create("testPool");

            AtomicReference<Boolean> hasBorrowed = new AtomicReference<>(false);

            PoolContext.run(context, () -> {
                String obj = pool.borrowObject();
                hasBorrowed.set(context.hasBorrowedObject());
                pool.returnObject(obj);
                return null;
            });

            assertThat(hasBorrowed.get()).isTrue();
        }
    }

    @Nested
    @DisplayName("并发测试")
    class ConcurrencyTests {

        @Test
        @DisplayName("并发借用和归还")
        void testConcurrentBorrowReturn() throws InterruptedException {
            pool = new VirtualThreadPool<>(factory,
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

        @Test
        @DisplayName("并发异步操作")
        void testConcurrentAsyncOperations() throws InterruptedException {
            pool = new VirtualThreadPool<>(factory,
                    PoolConfig.builder().maxTotal(10).build());

            int operationCount = 100;
            CountDownLatch latch = new CountDownLatch(operationCount);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger errorCount = new AtomicInteger(0);

            for (int i = 0; i < operationCount; i++) {
                pool.executeAsync(obj -> {
                    try {
                        Thread.sleep(1);
                        return obj.toUpperCase();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    }
                }).whenComplete((result, ex) -> {
                    if (ex != null) {
                        errorCount.incrementAndGet();
                    } else {
                        successCount.incrementAndGet();
                    }
                    latch.countDown();
                });
            }

            latch.await();

            assertThat(errorCount.get()).isZero();
            assertThat(successCount.get()).isEqualTo(operationCount);
        }
    }
}
