package cloud.opencode.base.functional.async;

import cloud.opencode.base.functional.monad.Try;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * LazyAsync 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
@DisplayName("LazyAsync 测试")
class LazyAsyncTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("of() 创建未启动的 LazyAsync")
        void testOf() {
            AtomicInteger callCount = new AtomicInteger(0);
            LazyAsync<Integer> lazy = LazyAsync.of(() -> {
                callCount.incrementAndGet();
                return 42;
            });

            assertThat(lazy.isStarted()).isFalse();
            assertThat(lazy.state()).isEqualTo(LazyAsync.State.NOT_STARTED);
            assertThat(callCount.get()).isEqualTo(0);
        }

        @Test
        @DisplayName("completed() 创建已完成的 LazyAsync")
        void testCompleted() {
            LazyAsync<Integer> lazy = LazyAsync.completed(42);

            assertThat(lazy.isStarted()).isTrue();
            assertThat(lazy.isCompleted()).isTrue();
            assertThat(lazy.state()).isEqualTo(LazyAsync.State.COMPLETED);
            assertThat(lazy.force()).isEqualTo(42);
        }

        @Test
        @DisplayName("failed() 创建失败的 LazyAsync")
        void testFailed() {
            LazyAsync<Integer> lazy = LazyAsync.failed(new RuntimeException("error"));

            assertThat(lazy.isStarted()).isTrue();
            assertThat(lazy.isFailed()).isTrue();
            assertThat(lazy.state()).isEqualTo(LazyAsync.State.FAILED);
        }
    }

    @Nested
    @DisplayName("start() 测试")
    class StartTests {

        @Test
        @DisplayName("start() 启动计算")
        void testStart() {
            AtomicInteger callCount = new AtomicInteger(0);
            LazyAsync<Integer> lazy = LazyAsync.of(() -> {
                callCount.incrementAndGet();
                return 42;
            });

            CompletableFuture<Integer> future = lazy.start();

            assertThat(lazy.isStarted()).isTrue();
            assertThat(future.join()).isEqualTo(42);
            assertThat(callCount.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("start() 多次调用只计算一次")
        void testStartMultipleTimes() {
            AtomicInteger callCount = new AtomicInteger(0);
            LazyAsync<Integer> lazy = LazyAsync.of(() -> {
                callCount.incrementAndGet();
                return 42;
            });

            lazy.start();
            lazy.start();
            lazy.start();

            // Wait for async computation to complete
            lazy.force();

            assertThat(callCount.get()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("force() 测试")
    class ForceTests {

        @Test
        @DisplayName("force() 阻塞获取结果")
        void testForce() {
            LazyAsync<Integer> lazy = LazyAsync.of(() -> 42);

            Integer result = lazy.force();

            assertThat(result).isEqualTo(42);
            assertThat(lazy.isCompleted()).isTrue();
        }

        @Test
        @DisplayName("force() 异常时抛出 RuntimeException")
        void testForceOnFailure() {
            LazyAsync<Integer> lazy = LazyAsync.of(() -> {
                throw new RuntimeException("error");
            });

            assertThatThrownBy(lazy::force)
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("get(Duration) 测试")
    class GetWithTimeoutTests {

        @Test
        @DisplayName("get() 成功返回 Try.Success")
        void testGetSuccess() {
            LazyAsync<Integer> lazy = LazyAsync.of(() -> 42);

            Try<Integer> result = lazy.get(Duration.ofSeconds(1));

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isEqualTo(42);
        }

        @Test
        @DisplayName("get() 超时返回 Try.Failure")
        void testGetTimeout() {
            LazyAsync<Integer> lazy = LazyAsync.of(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return 42;
            });

            Try<Integer> result = lazy.get(Duration.ofMillis(50));

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getCause().get()).isInstanceOf(TimeoutException.class);
        }
    }

    @Nested
    @DisplayName("toTry() 测试")
    class ToTryTests {

        @Test
        @DisplayName("toTry() 成功返回 Success")
        void testToTrySuccess() {
            LazyAsync<Integer> lazy = LazyAsync.of(() -> 42);

            Try<Integer> result = lazy.toTry();

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.get()).isEqualTo(42);
        }

        @Test
        @DisplayName("toTry() 失败返回 Failure")
        void testToTryFailure() {
            LazyAsync<Integer> lazy = LazyAsync.of(() -> {
                throw new RuntimeException("error");
            });

            Try<Integer> result = lazy.toTry();

            assertThat(result.isFailure()).isTrue();
        }
    }

    @Nested
    @DisplayName("状态查询测试")
    class StateQueryTests {

        @Test
        @DisplayName("isStarted() 测试")
        void testIsStarted() {
            LazyAsync<Integer> lazy = LazyAsync.of(() -> 42);

            assertThat(lazy.isStarted()).isFalse();
            lazy.start();
            assertThat(lazy.isStarted()).isTrue();
        }

        @Test
        @DisplayName("isRunning() 测试")
        void testIsRunning() {
            LazyAsync<Integer> lazy = LazyAsync.of(() -> {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return 42;
            });

            assertThat(lazy.isRunning()).isFalse();
            lazy.start();
            assertThat(lazy.isRunning()).isTrue();
            lazy.force();
            assertThat(lazy.isRunning()).isFalse();
        }

        @Test
        @DisplayName("isCompleted() 测试")
        void testIsCompleted() {
            LazyAsync<Integer> lazy = LazyAsync.of(() -> 42);

            assertThat(lazy.isCompleted()).isFalse();
            lazy.force();
            assertThat(lazy.isCompleted()).isTrue();
        }

        @Test
        @DisplayName("isFailed() 测试")
        void testIsFailed() {
            LazyAsync<Integer> lazy = LazyAsync.of(() -> {
                throw new RuntimeException("error");
            });

            assertThat(lazy.isFailed()).isFalse();
            try {
                lazy.force();
            } catch (Exception ignored) {
            }
            assertThat(lazy.isFailed()).isTrue();
        }

        @Test
        @DisplayName("state() 测试")
        void testState() {
            LazyAsync<Integer> lazy = LazyAsync.of(() -> 42);

            assertThat(lazy.state()).isEqualTo(LazyAsync.State.NOT_STARTED);
            lazy.force();
            assertThat(lazy.state()).isEqualTo(LazyAsync.State.COMPLETED);
        }
    }

    @Nested
    @DisplayName("map() 测试")
    class MapTests {

        @Test
        @DisplayName("map() 惰性转换")
        void testMapIsLazy() {
            AtomicInteger callCount = new AtomicInteger(0);
            LazyAsync<Integer> lazy = LazyAsync.of(() -> {
                callCount.incrementAndGet();
                return 5;
            });

            LazyAsync<Integer> mapped = lazy.map(n -> n * 2);

            assertThat(callCount.get()).isEqualTo(0);
            assertThat(mapped.force()).isEqualTo(10);
            assertThat(callCount.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("map() 链式调用")
        void testMapChain() {
            LazyAsync<Integer> result = LazyAsync.of(() -> 5)
                    .map(n -> n * 2)
                    .map(n -> n + 1);

            assertThat(result.force()).isEqualTo(11);
        }
    }

    @Nested
    @DisplayName("flatMap() 测试")
    class FlatMapTests {

        @Test
        @DisplayName("flatMap() 惰性转换")
        void testFlatMapIsLazy() {
            AtomicInteger callCount = new AtomicInteger(0);
            LazyAsync<Integer> lazy = LazyAsync.of(() -> {
                callCount.incrementAndGet();
                return 5;
            });

            LazyAsync<Integer> mapped = lazy.flatMap(n -> LazyAsync.of(() -> n * 2));

            assertThat(callCount.get()).isEqualTo(0);
            assertThat(mapped.force()).isEqualTo(10);
            assertThat(callCount.get()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("recover() 测试")
    class RecoverTests {

        @Test
        @DisplayName("recover() 从失败恢复")
        void testRecover() {
            LazyAsync<Integer> lazy = LazyAsync.of(() -> {
                throw new RuntimeException("error");
            });

            LazyAsync<Integer> recovered = lazy.recover(e -> 0);

            assertThat(recovered.force()).isEqualTo(0);
        }

        @Test
        @DisplayName("recover() 成功时不执行")
        void testRecoverOnSuccess() {
            LazyAsync<Integer> lazy = LazyAsync.of(() -> 42);

            LazyAsync<Integer> recovered = lazy.recover(e -> 0);

            assertThat(recovered.force()).isEqualTo(42);
        }
    }

    @Nested
    @DisplayName("orElse() 测试")
    class OrElseTests {

        @Test
        @DisplayName("orElse() 失败时使用回退")
        void testOrElseOnFailure() {
            LazyAsync<Integer> lazy = LazyAsync.of(() -> {
                throw new RuntimeException("error");
            });

            LazyAsync<Integer> withFallback = lazy.orElse(LazyAsync.of(() -> 0));

            assertThat(withFallback.force()).isEqualTo(0);
        }

        @Test
        @DisplayName("orElse() 成功时不使用回退")
        void testOrElseOnSuccess() {
            LazyAsync<Integer> lazy = LazyAsync.of(() -> 42);

            LazyAsync<Integer> withFallback = lazy.orElse(LazyAsync.of(() -> 0));

            assertThat(withFallback.force()).isEqualTo(42);
        }
    }

    @Nested
    @DisplayName("combine() 测试")
    class CombineTests {

        @Test
        @DisplayName("combine() 组合两个 LazyAsync")
        void testCombine() {
            LazyAsync<Integer> la1 = LazyAsync.of(() -> 1);
            LazyAsync<Integer> la2 = LazyAsync.of(() -> 2);

            LazyAsync<Integer> combined = LazyAsync.combine(la1, la2, Integer::sum);

            assertThat(combined.force()).isEqualTo(3);
        }

        @Test
        @DisplayName("combine() 并行启动两个计算")
        void testCombineParallel() throws InterruptedException {
            AtomicInteger counter = new AtomicInteger(0);
            LazyAsync<Integer> la1 = LazyAsync.of(() -> {
                counter.incrementAndGet();
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return 1;
            });
            LazyAsync<Integer> la2 = LazyAsync.of(() -> {
                counter.incrementAndGet();
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return 2;
            });

            LazyAsync<Integer> combined = LazyAsync.combine(la1, la2, Integer::sum);
            long start = System.currentTimeMillis();
            combined.force();
            long elapsed = System.currentTimeMillis() - start;

            // 如果并行执行，应该在约 50ms 内完成，而不是 100ms
            assertThat(elapsed).isLessThan(150);
        }
    }

    @Nested
    @DisplayName("race() 测试")
    class RaceTests {

        @Test
        @DisplayName("race() 返回先完成的结果")
        void testRace() {
            LazyAsync<Integer> slow = LazyAsync.of(() -> {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return 1;
            });
            LazyAsync<Integer> fast = LazyAsync.of(() -> 2);

            LazyAsync<Integer> raced = LazyAsync.race(slow, fast);

            assertThat(raced.force()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("getFuture() 测试")
    class GetFutureTests {

        @Test
        @DisplayName("getFuture() 未启动时返回 null")
        void testGetFutureNotStarted() {
            LazyAsync<Integer> lazy = LazyAsync.of(() -> 42);

            assertThat(lazy.getFuture()).isNull();
        }

        @Test
        @DisplayName("getFuture() 启动后返回 Future")
        void testGetFutureStarted() {
            LazyAsync<Integer> lazy = LazyAsync.of(() -> 42);
            lazy.start();

            assertThat(lazy.getFuture()).isNotNull();
            assertThat(lazy.getFuture().join()).isEqualTo(42);
        }
    }

    @Nested
    @DisplayName("toString() 测试")
    class ToStringTests {

        @Test
        @DisplayName("toString() 显示状态")
        void testToString() {
            LazyAsync<Integer> lazy = LazyAsync.of(() -> 42);

            assertThat(lazy.toString()).isEqualTo("LazyAsync[NOT_STARTED]");

            lazy.force();

            assertThat(lazy.toString()).isEqualTo("LazyAsync[COMPLETED]");
        }
    }

    @Nested
    @DisplayName("State 枚举测试")
    class StateEnumTests {

        @Test
        @DisplayName("State 枚举值测试")
        void testStateValues() {
            assertThat(LazyAsync.State.values()).containsExactly(
                    LazyAsync.State.NOT_STARTED,
                    LazyAsync.State.RUNNING,
                    LazyAsync.State.COMPLETED,
                    LazyAsync.State.FAILED
            );
        }
    }
}
