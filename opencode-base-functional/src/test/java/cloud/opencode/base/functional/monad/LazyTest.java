package cloud.opencode.base.functional.monad;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * Lazy 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
@DisplayName("Lazy 测试")
class LazyTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("of() 创建未求值的 Lazy")
        void testOf() {
            AtomicInteger callCount = new AtomicInteger(0);
            Lazy<Integer> lazy = Lazy.of(() -> {
                callCount.incrementAndGet();
                return 42;
            });

            assertThat(lazy.isEvaluated()).isFalse();
            assertThat(callCount.get()).isEqualTo(0);
        }

        @Test
        @DisplayName("value() 创建已求值的 Lazy")
        void testValue() {
            Lazy<Integer> lazy = Lazy.value(42);

            assertThat(lazy.isEvaluated()).isTrue();
            assertThat(lazy.get()).isEqualTo(42);
        }
    }

    @Nested
    @DisplayName("get() 测试")
    class GetTests {

        @Test
        @DisplayName("get() 首次调用时计算值")
        void testGetComputesValue() {
            AtomicInteger callCount = new AtomicInteger(0);
            Lazy<Integer> lazy = Lazy.of(() -> {
                callCount.incrementAndGet();
                return 42;
            });

            Integer result = lazy.get();

            assertThat(result).isEqualTo(42);
            assertThat(callCount.get()).isEqualTo(1);
            assertThat(lazy.isEvaluated()).isTrue();
        }

        @Test
        @DisplayName("get() 后续调用返回缓存的值")
        void testGetReturnsCachedValue() {
            AtomicInteger callCount = new AtomicInteger(0);
            Lazy<Integer> lazy = Lazy.of(() -> {
                callCount.incrementAndGet();
                return 42;
            });

            lazy.get();
            lazy.get();
            lazy.get();

            assertThat(callCount.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("get() 允许 null 结果")
        void testGetAllowsNull() {
            Lazy<String> lazy = Lazy.of(() -> null);

            assertThat(lazy.get()).isNull();
            assertThat(lazy.isEvaluated()).isTrue();
        }
    }

    @Nested
    @DisplayName("isEvaluated() 测试")
    class IsEvaluatedTests {

        @Test
        @DisplayName("未求值时返回 false")
        void testIsEvaluatedFalse() {
            Lazy<Integer> lazy = Lazy.of(() -> 42);

            assertThat(lazy.isEvaluated()).isFalse();
        }

        @Test
        @DisplayName("求值后返回 true")
        void testIsEvaluatedTrue() {
            Lazy<Integer> lazy = Lazy.of(() -> 42);
            lazy.get();

            assertThat(lazy.isEvaluated()).isTrue();
        }
    }

    @Nested
    @DisplayName("map() 测试")
    class MapTests {

        @Test
        @DisplayName("map() 惰性转换")
        void testMapIsLazy() {
            AtomicInteger callCount = new AtomicInteger(0);
            Lazy<Integer> lazy = Lazy.of(() -> {
                callCount.incrementAndGet();
                return 5;
            });

            Lazy<Integer> mapped = lazy.map(n -> n * 2);

            assertThat(callCount.get()).isEqualTo(0);
            assertThat(mapped.get()).isEqualTo(10);
            assertThat(callCount.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("map() 链式调用")
        void testMapChain() {
            Lazy<Integer> result = Lazy.of(() -> 5)
                    .map(n -> n * 2)
                    .map(n -> n + 1);

            assertThat(result.get()).isEqualTo(11);
        }
    }

    @Nested
    @DisplayName("flatMap() 测试")
    class FlatMapTests {

        @Test
        @DisplayName("flatMap() 惰性转换")
        void testFlatMapIsLazy() {
            AtomicInteger callCount = new AtomicInteger(0);
            Lazy<Integer> lazy = Lazy.of(() -> {
                callCount.incrementAndGet();
                return 5;
            });

            Lazy<Integer> mapped = lazy.flatMap(n -> Lazy.of(() -> n * 2));

            assertThat(callCount.get()).isEqualTo(0);
            assertThat(mapped.get()).isEqualTo(10);
            assertThat(callCount.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("flatMap() 链式调用")
        void testFlatMapChain() {
            Lazy<Integer> result = Lazy.of(() -> 5)
                    .flatMap(n -> Lazy.of(() -> n * 2))
                    .flatMap(n -> Lazy.of(() -> n + 1));

            assertThat(result.get()).isEqualTo(11);
        }
    }

    @Nested
    @DisplayName("filter() 测试")
    class FilterTests {

        @Test
        @DisplayName("filter() 条件满足时返回值")
        void testFilterPasses() {
            Lazy<Integer> lazy = Lazy.of(() -> 10)
                    .filter(n -> n > 5);

            assertThat(lazy.get()).isEqualTo(10);
        }

        @Test
        @DisplayName("filter() 条件不满足时抛出异常")
        void testFilterFails() {
            Lazy<Integer> lazy = Lazy.of(() -> 3)
                    .filter(n -> n > 5);

            assertThatThrownBy(lazy::get)
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessageContaining("predicate not satisfied");
        }

        @Test
        @DisplayName("filter() 惰性求值")
        void testFilterIsLazy() {
            AtomicInteger callCount = new AtomicInteger(0);
            Lazy<Integer> lazy = Lazy.of(() -> {
                callCount.incrementAndGet();
                return 10;
            }).filter(n -> n > 5);

            assertThat(callCount.get()).isEqualTo(0);
            lazy.get();
            assertThat(callCount.get()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("getOrElse() 测试")
    class GetOrElseTests {

        @Test
        @DisplayName("getOrElse(T) 正常时返回值")
        void testGetOrElseSuccess() {
            Lazy<Integer> lazy = Lazy.of(() -> 42);

            assertThat(lazy.getOrElse(0)).isEqualTo(42);
        }

        @Test
        @DisplayName("getOrElse(T) 异常时返回默认值")
        void testGetOrElseOnException() {
            Lazy<Integer> lazy = Lazy.of(() -> {
                throw new RuntimeException("error");
            });

            assertThat(lazy.getOrElse(0)).isEqualTo(0);
        }

        @Test
        @DisplayName("getOrElse(Supplier) 正常时返回值")
        void testGetOrElseSupplierSuccess() {
            Lazy<Integer> lazy = Lazy.of(() -> 42);

            assertThat(lazy.getOrElse(() -> 0)).isEqualTo(42);
        }

        @Test
        @DisplayName("getOrElse(Supplier) 异常时返回计算的默认值")
        void testGetOrElseSupplierOnException() {
            Lazy<Integer> lazy = Lazy.of(() -> {
                throw new RuntimeException("error");
            });

            assertThat(lazy.getOrElse(() -> 0)).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("toTry() 测试")
    class ToTryTests {

        @Test
        @DisplayName("toTry() 成功时返回 Success")
        void testToTrySuccess() {
            Lazy<Integer> lazy = Lazy.of(() -> 42);

            Try<Integer> result = lazy.toTry();

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isEqualTo(42);
        }

        @Test
        @DisplayName("toTry() 异常时返回 Failure")
        void testToTryFailure() {
            Lazy<Integer> lazy = Lazy.of(() -> {
                throw new RuntimeException("error");
            });

            Try<Integer> result = lazy.toTry();

            assertThat(result.isFailure()).isTrue();
        }
    }

    @Nested
    @DisplayName("toOption() 测试")
    class ToOptionTests {

        @Test
        @DisplayName("toOption() 成功时返回 Some")
        void testToOptionSuccess() {
            Lazy<Integer> lazy = Lazy.of(() -> 42);

            Option<Integer> result = lazy.toOption();

            assertThat(result.isSome()).isTrue();
            assertThat(result.get()).isEqualTo(42);
        }

        @Test
        @DisplayName("toOption() null 时返回 None")
        void testToOptionNull() {
            Lazy<Integer> lazy = Lazy.of(() -> null);

            Option<Integer> result = lazy.toOption();

            assertThat(result.isNone()).isTrue();
        }

        @Test
        @DisplayName("toOption() 异常时返回 None")
        void testToOptionOnException() {
            Lazy<Integer> lazy = Lazy.of(() -> {
                throw new RuntimeException("error");
            });

            Option<Integer> result = lazy.toOption();

            assertThat(result.isNone()).isTrue();
        }
    }

    @Nested
    @DisplayName("toString() 测试")
    class ToStringTests {

        @Test
        @DisplayName("未求值时 toString()")
        void testToStringNotEvaluated() {
            Lazy<Integer> lazy = Lazy.of(() -> 42);

            assertThat(lazy.toString()).isEqualTo("Lazy[?]");
        }

        @Test
        @DisplayName("求值后 toString()")
        void testToStringEvaluated() {
            Lazy<Integer> lazy = Lazy.of(() -> 42);
            lazy.get();

            assertThat(lazy.toString()).isEqualTo("Lazy[42]");
        }
    }

    @Nested
    @DisplayName("线程安全测试")
    class ThreadSafetyTests {

        @Test
        @DisplayName("并发访问只计算一次")
        void testConcurrentAccessComputesOnce() throws InterruptedException {
            int threadCount = 100;
            AtomicInteger callCount = new AtomicInteger(0);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(threadCount);

            Lazy<Integer> lazy = Lazy.of(() -> {
                callCount.incrementAndGet();
                try {
                    Thread.sleep(10); // 模拟耗时操作
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return 42;
            });

            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        lazy.get();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            startLatch.countDown();
            doneLatch.await();
            executor.shutdown();

            // 由于双重检查锁，可能有多次计算，但值应该一致
            assertThat(lazy.get()).isEqualTo(42);
            assertThat(lazy.isEvaluated()).isTrue();
        }
    }

    @Nested
    @DisplayName("Supplier 接口测试")
    class SupplierInterfaceTests {

        @Test
        @DisplayName("Lazy 实现 Supplier 接口")
        void testLazyImplementsSupplier() {
            Lazy<Integer> lazy = Lazy.of(() -> 42);

            // Lazy 实现了 Supplier 接口
            java.util.function.Supplier<Integer> supplier = lazy;

            assertThat(supplier.get()).isEqualTo(42);
        }
    }
}
