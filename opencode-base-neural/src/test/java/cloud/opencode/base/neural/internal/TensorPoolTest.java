package cloud.opencode.base.neural.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("TensorPool — 张量缓冲区对象池测试")
class TensorPoolTest {

    private TensorPool pool;

    @BeforeEach
    void setUp() {
        pool = new TensorPool(4);
    }

    @Nested
    @DisplayName("acquire — 获取数组")
    class AcquireTests {

        @Test
        @DisplayName("返回指定大小的数组")
        void acquireReturnsCorrectSize() {
            float[] arr = pool.acquire(128);
            assertThat(arr).hasSize(128);
        }

        @Test
        @DisplayName("大小为 0 时返回空数组")
        void acquireZeroSize() {
            float[] arr = pool.acquire(0);
            assertThat(arr).hasSize(0);
        }

        @Test
        @DisplayName("负数大小抛出异常")
        void acquireNegativeSize() {
            assertThatThrownBy(() -> pool.acquire(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("release 与 acquire 复用 — 归还后复用")
    class ReleaseAndReuseTests {

        @Test
        @DisplayName("归还后再获取复用同一数组（引用相等）")
        void releaseAndAcquireReusesSameArray() {
            float[] original = pool.acquire(64);
            pool.release(original);
            float[] reused = pool.acquire(64);
            assertThat(reused).isSameAs(original);
        }

        @Test
        @DisplayName("不同大小的数组不会混淆")
        void differentSizesAreSeparated() {
            float[] small = new float[10];
            float[] large = new float[20];
            pool.release(small);
            pool.release(large);

            float[] acquired10 = pool.acquire(10);
            float[] acquired20 = pool.acquire(20);
            assertThat(acquired10).isSameAs(small);
            assertThat(acquired20).isSameAs(large);
        }
    }

    @Nested
    @DisplayName("池满丢弃 — 超出容量")
    class PoolFullTests {

        @Test
        @DisplayName("桶满后归还不抛异常，多余数组被丢弃")
        void releaseWhenFullDiscardsWithoutException() {
            // maxPoolSize = 4
            for (int i = 0; i < 10; i++) {
                pool.release(new float[32]);
            }
            assertThat(pool.pooledCount()).isLessThanOrEqualTo(4);
        }
    }

    @Nested
    @DisplayName("clear — 清除池")
    class ClearTests {

        @Test
        @DisplayName("清除后池为空")
        void clearEmptiesPool() {
            pool.release(new float[10]);
            pool.release(new float[20]);
            assertThat(pool.pooledCount()).isEqualTo(2);

            pool.clear();
            assertThat(pool.pooledCount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("pooledCount — 诊断计数")
    class PooledCountTests {

        @Test
        @DisplayName("初始计数为 0")
        void initialCountIsZero() {
            assertThat(pool.pooledCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("归还后计数递增")
        void countIncrementsOnRelease() {
            pool.release(new float[5]);
            pool.release(new float[10]);
            assertThat(pool.pooledCount()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("null 归还 — 空指针处理")
    class NullReleaseTests {

        @Test
        @DisplayName("归还 null 数组抛出异常")
        void releaseNullThrowsException() {
            assertThatThrownBy(() -> pool.release(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("构造函数验证")
    class ConstructorTests {

        @Test
        @DisplayName("maxPoolSize 为 0 抛出异常")
        void zeroMaxPoolSizeThrows() {
            assertThatThrownBy(() -> new TensorPool(0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("默认构造函数创建成功")
        void defaultConstructor() {
            TensorPool defaultPool = new TensorPool();
            assertThat(defaultPool.pooledCount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("线程安全 — 并发测试")
    class ThreadSafetyTests {

        @Test
        @DisplayName("4 个线程并发 acquire/release 不抛异常")
        void concurrentAcquireRelease() throws Exception {
            TensorPool concurrentPool = new TensorPool(64);
            int threadCount = 4;
            int iterations = 1000;
            CyclicBarrier barrier = new CyclicBarrier(threadCount);
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);

            List<Future<?>> futures = new ArrayList<>();
            for (int t = 0; t < threadCount; t++) {
                futures.add(executor.submit(() -> {
                    try {
                        barrier.await();
                        for (int i = 0; i < iterations; i++) {
                            float[] arr = concurrentPool.acquire(64);
                            arr[0] = 1.0f; // use the array
                            concurrentPool.release(arr);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }));
            }

            for (Future<?> future : futures) {
                future.get();
            }
            executor.shutdown();
            // No exception means success. Pool may have some arrays left.
            assertThat(concurrentPool.pooledCount()).isGreaterThanOrEqualTo(0);
        }
    }
}
